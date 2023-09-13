/*
 * Certain versions of software accessible here may contain branding from Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.
 * This software was acquired by Micro Focus on September 1, 2017, and is now offered by OpenText.
 * Any reference to the HP and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright 2012-2023 Open Text
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors (“Open Text”) are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ___________________________________________________________________
 */

package com.adm.bamboo.plugin.uft.api;

import com.adm.utils.uft.*;
import com.adm.utils.uft.model.UftRunAsUser;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

public interface AbstractLauncherTask extends TaskType {
    String BUILD_KEY = "buildTimeStamp";
    String AES_256_SECRET_KEY = "AES_256_SECRET_KEY";
    String AES_256_SECRET_INIT_VECTOR = "AES_256_SECRET_INIT_VECTOR";
    String KEY_VALUE_FORMAT = "%s = %s";
    String UFT_RUN_AS_USER_NAME = "UFT_RUN_AS_USER_NAME";
    String UFT_RUN_AS_USER_ENCODED_PWD = "UFT_RUN_AS_USER_ENCODED_PASSWORD";
    String UFT_RUN_AS_USER_PWD = "UFT_RUN_AS_USER_PASSWORD";

    CustomVariableContext getCustomVariableContext();
    Properties getTaskProperties(final TaskContext taskContext) throws Exception;
    Aes256Encryptor getAes256Encryptor();
    TaskResult collateResults(@NotNull final TaskContext taskContext, final Properties mergedProperties);

    default TaskResult execute(@NotNull TaskContext taskContext) throws TaskException {
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        Properties mergedProperties = new Properties();
        try {
            Properties customTaskProperties = getTaskProperties(taskContext);
            if (customTaskProperties != null) {
                mergedProperties.putAll(customTaskProperties);
            }
        } catch (Exception e) {
            TaskUtils.logErrorMessage(e, buildLogger, taskContext);
        }

        //retrieve bamboo buildTimeStamp
        String buildTimeStamp = getBuildTimeStamp();

        UftRunAsUser uftRunAsUser;
        try {
            uftRunAsUser = getUftRunAsUser(buildLogger);
            if (uftRunAsUser != null) {
                mergedProperties.put("uftRunAsUserName", uftRunAsUser.getUsername());
                if (!StringUtils.isBlank(uftRunAsUser.getEncodedPassword())) {
                    mergedProperties.put("uftRunAsUserEncodedPassword", uftRunAsUser.getEncodedPasswordAsEncrypted());
                } else if (!StringUtils.isBlank(uftRunAsUser.getPassword())) {
                    mergedProperties.put("uftRunAsUserPassword", uftRunAsUser.getPasswordAsEncrypted());
                }
            }
        } catch(Exception e) {
            buildLogger.addErrorLogEntry(String.format("Build parameters check failed: %s.", e.getMessage()));
            return TaskResultBuilder.newBuilder(taskContext).failedWithError().build();
        }

        //build props file
        File workingDirectory = taskContext.getWorkingDirectory();
        File paramsFile = FilesHandler.buildPropertiesFile(taskContext, workingDirectory, mergedProperties, buildTimeStamp, buildLogger);

        //extract binary resources
        String launcherPath = null;
        String aborterPath = null;
        try {
            //copy HpToolsLauncher.exe
            launcherPath = FilesHandler.getToolPath(taskContext, workingDirectory, true);

            //copy HpToolsAborter.exe
            aborterPath = FilesHandler.getToolPath(taskContext, workingDirectory, false);
        } catch (IOException ioe) {
            TaskUtils.logErrorMessage(ioe, buildLogger, taskContext);
        }

        //run task
        try {
            Integer returnCode = runTask(workingDirectory, launcherPath, paramsFile.getAbsolutePath(), buildLogger);
            if (returnCode.equals(3)) {
                throw new InterruptedException();
            } else if (returnCode.equals(0)) {
                return collateResults(taskContext, mergedProperties);
            }
        } catch (IOException ioe) {
            TaskUtils.logErrorMessage(ioe, buildLogger, taskContext);
        } catch (InterruptedException e) {
            buildLogger.addErrorLogEntry("Aborted by user. Aborting process.");
            try {
                runTask(workingDirectory, aborterPath, paramsFile.getAbsolutePath(), buildLogger);
            } catch (IOException ioe) {
                TaskUtils.logErrorMessage(ioe, buildLogger, taskContext);
            } catch (InterruptedException ie) {
                TaskUtils.logErrorMessage(ie, buildLogger, taskContext);
            }
        }

        return collateResults(taskContext, mergedProperties);
    }

    default Integer runTask(final File workingDirectory, final String launcherPath, final String paramFile, final BuildLogger logger) throws IOException, InterruptedException {
        try {
            ProcessBuilder builder = new ProcessBuilder(launcherPath, "-paramfile", paramFile);
            builder.directory(workingDirectory);
            Aes256Encryptor encryptor = getAes256Encryptor();
            builder.environment().put(AES_256_SECRET_KEY, encryptor.getSecretKey());
            builder.environment().put(AES_256_SECRET_INIT_VECTOR, encryptor.getInitVector());
            logger.addBuildLogEntry(launcherPath + " -paramfile " + paramFile);
            Process process = builder.start();
            BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = output.readLine()) != null) {
                logger.addBuildLogEntry(line);
            }
            output.close();
            return process.waitFor();
        } catch (Throwable t) {
            if (t.getMessage() != null)
                logger.addBuildLogEntry(t.getMessage());
            return -1;
        }
    }

    default String getBuildTimeStamp(){
        Map<String, VariableDefinitionContext> variables = getCustomVariableContext().getVariableContexts();
        String buildTimeStamp = "";
        if(variables.containsKey(BUILD_KEY)) {
           buildTimeStamp = variables.get(BUILD_KEY).getValue();
        }

        return buildTimeStamp;
    }

    default UftRunAsUser getUftRunAsUser(BuildLogger logger) throws IllegalArgumentException {
        Map<String, VariableDefinitionContext> vars = getCustomVariableContext().getVariableContexts();
        if (vars != null && !vars.isEmpty()) {
            if (vars.keySet().contains(UFT_RUN_AS_USER_NAME)) {
                String username = vars.get(UFT_RUN_AS_USER_NAME).getValue();
                if (!StringUtils.isBlank(username)) {
                    logger.addBuildLogEntry(String.format(KEY_VALUE_FORMAT, UFT_RUN_AS_USER_NAME, username));
                    String password = null;
                    boolean isEncoded = false;
                    if (vars.keySet().contains(UFT_RUN_AS_USER_ENCODED_PWD)) {
                        password = vars.get(UFT_RUN_AS_USER_ENCODED_PWD).getValue();
                        isEncoded = true;
                    } else if (vars.keySet().contains(UFT_RUN_AS_USER_PWD)) {
                        password = vars.get(UFT_RUN_AS_USER_PWD).getValue();
                        return new UftRunAsUser(username, password, false, getAes256Encryptor());
                    }

                    if (StringUtils.isBlank(password)) {
                        throw new IllegalArgumentException(String.format("Either %s or %s is required.", UFT_RUN_AS_USER_PWD, UFT_RUN_AS_USER_ENCODED_PWD));
                    }
                    return new UftRunAsUser(username, password, isEncoded, getAes256Encryptor());
                }
            }
        }
        return null;
    }

    default Pair<String, String> getEncryptionKeyVectorPair() {
        final Map<String, VariableDefinitionContext> variables = getCustomVariableContext().getVariableContexts();
        String privateKey = null, initVect = null;
        if (variables.containsKey(AES_256_SECRET_KEY)) {
            privateKey = variables.get(AES_256_SECRET_KEY).getValue();
        }
        if (variables.containsKey(AES_256_SECRET_INIT_VECTOR)) {
            initVect = variables.get(AES_256_SECRET_INIT_VECTOR).getValue();
        }

        return new Pair<>(privateKey, initVect);
    }
}




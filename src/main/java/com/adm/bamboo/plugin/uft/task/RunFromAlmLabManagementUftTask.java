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

package com.adm.bamboo.plugin.uft.task;

import com.adm.bamboo.plugin.uft.api.AbstractLauncherTask;
import com.adm.utils.uft.Aes256Encryptor;
import com.adm.utils.uft.SSEException;
import com.adm.utils.uft.model.CdaDetails;
import com.adm.utils.uft.rest.RestClient;
import com.adm.utils.uft.result.ResultSerializer;
import com.adm.utils.uft.result.model.junit.Testsuites;
import com.adm.utils.uft.sdk.Args;
import com.adm.utils.uft.sdk.Logger;
import com.adm.utils.uft.sdk.RunManager;
import com.adm.utils.uft.enums.UFTConstants;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.test.TestCollationService;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.bamboo.utils.i18n.I18nBeanFactory;

import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.commons.lang.BooleanUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

import static com.adm.bamboo.plugin.uft.results.TestResultHelper.collateTestResults;
import static com.adm.bamboo.plugin.uft.results.TestResultHelperAlm.AddALMArtifacts;

public class RunFromAlmLabManagementUftTask implements AbstractLauncherTask {
    private final TestCollationService testCollationService;
    private static I18nBean i18nBean;
    private final CustomVariableContext customVariableContext;

    private final String LINK_SEARCH_FILTER = "run report for run id";

    public RunFromAlmLabManagementUftTask(final TestCollationService testCollationService, @NotNull final I18nBeanFactory i18nBeanFactory, @ComponentImport CustomVariableContext customVariableContext) {
        this.testCollationService = testCollationService;
        i18nBean = i18nBeanFactory.getI18nBean();
        this.customVariableContext = customVariableContext;
    }

    @Override
    public CustomVariableContext getCustomVariableContext() {
        return customVariableContext;
    }

    @NotNull
    @Override
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        final ConfigurationMap map = taskContext.getConfigurationMap();

        final String almServerPath = map.get(UFTConstants.ALM_SERVER.getValue());

        RunManager runManager = new RunManager();

        CdaDetails cdaDetails = null;
        boolean useCda = BooleanUtils.toBoolean(map.get(UFTConstants.USE_SDA_PARAM.getValue()));
        if (useCda) {
            cdaDetails = new CdaDetails(map.get(UFTConstants.DEPLOYMENT_ACTION_PARAM.getValue()),
                    map.get(UFTConstants.DEPLOYED_ENVIRONMENT_NAME.getValue()),
                    map.get(UFTConstants.DEPROVISIONING_ACTION_PARAM.getValue()));
        }

        Args args = new Args(
                almServerPath,
                map.get(UFTConstants.ALM_SSO.getValue()),
                map.get(UFTConstants.CLIENT_ID.getValue()),
                map.get(UFTConstants.API_KEY_SECRET.getValue()),
                map.get(UFTConstants.DOMAIN_PARAM.getValue()),
                map.get(UFTConstants.PROJECT_NAME_PARAM.getValue()),
                map.get(UFTConstants.USER_NAME.getValue()),
                map.get(UFTConstants.PASSWORD.getValue()),
                map.get(UFTConstants.RUN_TYPE_PARAM.getValue()),
                map.get(UFTConstants.TEST_ID_PARAM.getValue()),
                map.get(UFTConstants.DURATION_PARAM.getValue()),
                map.get(UFTConstants.DESCRIPTION_PARAM.getValue()),
                null,
                map.get(UFTConstants.ENVIRONMENT_ID_PARAM.getValue()),
                cdaDetails);

        RestClient restClient =
                new RestClient(
                        args.getUrl(),
                        args.getDomain(),
                        args.getProject(),
                        args.getUsername());

        try {
            Logger logger = new Logger() {

                public void log(String message) {
                    buildLogger.addBuildLogEntry(message);
                }
            };

            //run task
            Testsuites result = runManager.execute(restClient, args, logger);

            taskContext.getBuildLogger().addBuildLogEntry("Bamboo build timestamp variable has value: " + getBuildTimeStamp());
            ResultSerializer.saveResults(result, taskContext.getWorkingDirectory().getPath(), getBuildTimeStamp(), logger);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return TaskResultBuilder.newBuilder(taskContext).failed().build();
        } catch (SSEException e) {
            return TaskResultBuilder.newBuilder(taskContext).failed().build();
        }

        return collateResults(taskContext, null);
    }

    @Override
    public Properties getTaskProperties(TaskContext taskContext) {
        return null;
    }

    @Override
    public TaskResult collateResults(@NotNull TaskContext taskContext, Properties mergedProperties) {
        try {
            collateTestResults(testCollationService, taskContext);
            AddALMArtifacts(taskContext, null, LINK_SEARCH_FILTER, i18nBean);
            return TaskResultBuilder.newBuilder(taskContext).checkTestFailures().build();
        } catch (Exception ex) {
            return TaskResultBuilder.newBuilder(taskContext).failed().build();
        }
    }

    @Override
    public Aes256Encryptor getAes256Encryptor() { return null; }

}

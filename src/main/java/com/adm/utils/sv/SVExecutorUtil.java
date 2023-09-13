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
 * its affiliates and licensors ("Open Text") are as may be set forth
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

package com.adm.utils.sv;

import com.adm.bamboo.plugin.sv.model.ServiceInfo;
import com.adm.bamboo.plugin.sv.model.SvServiceSelectionModel;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.microfocus.sv.svconfigurator.build.ProjectBuilder;
import com.microfocus.sv.svconfigurator.core.IProject;
import com.microfocus.sv.svconfigurator.core.IService;
import com.microfocus.sv.svconfigurator.core.impl.exception.CommandExecutorException;
import com.microfocus.sv.svconfigurator.core.impl.exception.CommunicatorException;
import com.microfocus.sv.svconfigurator.core.impl.jaxb.atom.ServiceListAtom;
import com.microfocus.sv.svconfigurator.core.impl.processor.Credentials;
import com.microfocus.sv.svconfigurator.serverclient.ICommandExecutor;
import com.microfocus.sv.svconfigurator.serverclient.impl.CommandExecutorFactory;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SVExecutorUtil {
    /**
     * Returns the command executor
     *
     * @param  url
     *              URL of the server management endpoint
     * @param credentials
     *              Credential of the server management
     * @return the command executor
     */
    public static ICommandExecutor createCommandExecutor(URL url, Credentials credentials) throws Exception {
        return new CommandExecutorFactory().createCommandExecutor(url, true, credentials);
    }

    /**
     * Returns the list for the service info
     *
     * @param commandExecutor
     *              the command executor
     * @param svServiceSelectionModel
     *              service selection model
     * @param ignoreMissingServices
     *              ignore missing services
     * @param buildLogger
     * @return the list of service info
     */
    public static List<ServiceInfo> getServiceList(ICommandExecutor commandExecutor, SvServiceSelectionModel svServiceSelectionModel,  boolean ignoreMissingServices, BuildLogger buildLogger) throws Exception {
        String projectPassword = svServiceSelectionModel.getProjectPassword();
        String projectPath = svServiceSelectionModel.getProjectPath();
        ArrayList<ServiceInfo> res = new ArrayList<>();

        switch (svServiceSelectionModel.getSelectionType()) {
            case SERVICE:
                addServiceIfDeployed(svServiceSelectionModel.getService(), res, ignoreMissingServices, commandExecutor, buildLogger);
                break;
            case PROJECT:
                IProject project = new ProjectBuilder().buildProject(new File(projectPath), projectPassword);
                for (IService svc : project.getServices()) {
                    addServiceIfDeployed(svc.getId(), res, ignoreMissingServices, commandExecutor, buildLogger);
                }
                break;
            case ALL_DEPLOYED:
                for (ServiceListAtom.ServiceEntry entry : commandExecutor.getServiceList(null).getEntries()) {
                    res.add(new ServiceInfo(entry.getId(), entry.getTitle()));
                }
                break;
            case DEPLOY:
                break;
        }
        return res;
    }

    /**
     * add the selected virtual service to the service info list
     *
     * @param service
     *              the selected virtual service
     * @param results
     *              the list of the service info
     * @param ignoreMissingServices
     *              ignore missing services
     * @param exec
     *              the command executor
     * @param buildLogger
     * @return
     */
    private static void addServiceIfDeployed(String service, ArrayList<ServiceInfo> results, boolean ignoreMissingServices,
                                      ICommandExecutor exec, BuildLogger buildLogger) throws CommunicatorException, CommandExecutorException {
        try {
            IService svc = exec.findService(service, null);
            results.add(new ServiceInfo(svc.getId(), svc.getName()));
        } catch (CommandExecutorException e) {
            if (!ignoreMissingServices) {
                throw e;
            }
            buildLogger.addBuildLogEntry(String.format("Service '%s' is not deployed, ignoring%n", service));
        }
    }

    /**
     * Returns the table for command line interface
     *
     * @param svServiceSelectionModel
     * @param buildLogger
     * @param prefix
     * @return
     */
    public static void logConfig(SvServiceSelectionModel svServiceSelectionModel, BuildLogger buildLogger, String prefix){
        switch (svServiceSelectionModel.getSelectionType()) {
            case SERVICE:
                buildLogger.addBuildLogEntry(prefix + "Service name or id: " + svServiceSelectionModel.getService());
                break;
            case PROJECT:
                buildLogger.addBuildLogEntry(prefix + "Project path: " + svServiceSelectionModel.getProjectPath());
                buildLogger.addBuildLogEntry(prefix + "Project password: " + ((StringUtils.isNotBlank(svServiceSelectionModel.getProjectPassword())) ? "*****" : null));
                break;
            case ALL_DEPLOYED:
                buildLogger.addBuildLogEntry(prefix + "All deployed services");
                break;
            case DEPLOY:
                buildLogger.addBuildLogEntry(prefix + "Project path: " + svServiceSelectionModel.getProjectPath());
                buildLogger.addBuildLogEntry(prefix + "Project password: " + ((StringUtils.isNotBlank(svServiceSelectionModel.getProjectPassword())) ? "*****" : null));
                buildLogger.addBuildLogEntry(prefix + "Service name or id: " + svServiceSelectionModel.getService());
                break;
        }
    }

    /**
     * empty or null check
     *
     * @param input
     * @return the boolean value
     */
    public static boolean validInput(String input) {
        boolean valid = true;
        if(input == null || StringUtils.isEmpty(input)) {
            valid = false;
        }
        return valid;
    }
}

package com.adm.utils.sv;

import com.adm.bamboo.plugin.sv.model.ServiceInfo;
import com.adm.bamboo.plugin.sv.model.SvServiceSelectionModel;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.hp.sv.jsvconfigurator.build.ProjectBuilder;
import com.hp.sv.jsvconfigurator.core.IProject;
import com.hp.sv.jsvconfigurator.core.IService;
import com.hp.sv.jsvconfigurator.core.impl.exception.CommandExecutorException;
import com.hp.sv.jsvconfigurator.core.impl.exception.CommunicatorException;
import com.hp.sv.jsvconfigurator.core.impl.jaxb.atom.ServiceListAtom;
import com.hp.sv.jsvconfigurator.core.impl.processor.Credentials;
import com.hp.sv.jsvconfigurator.serverclient.ICommandExecutor;
import com.hp.sv.jsvconfigurator.serverclient.impl.CommandExecutorFactory;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SVExecutor {
    public ICommandExecutor createCommandExecutor(URL url, Credentials credentials) throws Exception {
        return new CommandExecutorFactory().createCommandExecutor(url, credentials);
    }

    public List<ServiceInfo> getServiceList(ICommandExecutor commandExecutor, SvServiceSelectionModel svServiceSelectionModel,  boolean ignoreMissingServices, BuildLogger buildLogger) throws Exception {
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

    private void addServiceIfDeployed(String service, ArrayList<ServiceInfo> results, boolean ignoreMissingServices,
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

    public void logConfig(SvServiceSelectionModel svServiceSelectionModel, BuildLogger buildLogger, String prefix){
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
}

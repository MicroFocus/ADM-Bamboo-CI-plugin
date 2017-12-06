package com.adm.bamboo.plugin.uft.api;

import com.adm.bamboo.plugin.uft.capability.CapabilityUftDefaultsHelper;
import com.adm.bamboo.plugin.uft.helpers.HpTasksArtifactRegistrator;
import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionManager;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskRequirementSupport;
import com.atlassian.bamboo.v2.build.agent.capability.Requirement;
import com.atlassian.bamboo.v2.build.agent.capability.RequirementImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AbstractLauncherTaskConfigurator extends AbstractTaskConfigurator implements TaskRequirementSupport {
    private static final String BUILD_WORKING_DIR = "bamboo.agentId";

    private ArtifactDefinitionManager artifactDefinitionManager;

    public void setArtifactDefinitionManager(ArtifactDefinitionManager artifactDefinitionManager) {
        this.artifactDefinitionManager = artifactDefinitionManager;
    }

    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put(BUILD_WORKING_DIR, "${bamboo.build.working.directory}");

        return config;
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);

        context.put(BUILD_WORKING_DIR, taskDefinition.getConfiguration().get(BUILD_WORKING_DIR));
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);

        (new HpTasksArtifactRegistrator()).registerCommonArtifact((Job) context.get("plan"), getI18nBean(), this.artifactDefinitionManager);
    }

    @NotNull
    @Override
    public Set<Requirement> calculateRequirements(TaskDefinition taskDefinition) {
        RequirementImpl uftReq = new RequirementImpl(CapabilityUftDefaultsHelper.CAPABILITY_UFT, true, ".*");
        Set<Requirement> result = new HashSet<Requirement>();
        result.add(uftReq);
        return result;
    }

}


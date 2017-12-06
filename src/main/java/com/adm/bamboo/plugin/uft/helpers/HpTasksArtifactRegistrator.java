package com.adm.bamboo.plugin.uft.helpers;

import com.adm.bamboo.plugin.uft.results.TestResultHelper;
import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionImpl;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionManager;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import org.jetbrains.annotations.NotNull;

public class HpTasksArtifactRegistrator {
    private static final String HP_UFT_PREFIX = "HP_UFT_Build_";

    public void registerCommonArtifact(@NotNull Job job, @NotNull I18nBean i18nBean, @NotNull ArtifactDefinitionManager artifactDefinitionManager) {
        if (job == null || i18nBean == null || artifactDefinitionManager == null) {
            return;
        }

        String name = i18nBean.getText("AllTasksArtifactDefinitionLabel");
        String ARTIFACT_COPY_PATTERN = HP_UFT_PREFIX + "${bamboo.buildNumber}/**";
        if (artifactDefinitionManager.findArtifactDefinition(job, name) == null) {
            ArtifactDefinitionImpl artifactDefinition = new ArtifactDefinitionImpl(name, "", ARTIFACT_COPY_PATTERN);
            artifactDefinition.setProducerJob(job);
            artifactDefinitionManager.saveArtifactDefinition(artifactDefinition);
        }
    }
}

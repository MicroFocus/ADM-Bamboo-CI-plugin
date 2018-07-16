/*
 * Copyright (c) EntIT Software LLC, a Micro Focus company
 *
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 *
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 */

package com.adm.bamboo.plugin.uft.helpers;

import com.adm.bamboo.plugin.uft.results.TestResultHelper;
import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionImpl;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionManager;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import org.jetbrains.annotations.NotNull;

public class HpTasksArtifactRegistrator {
    private static final String HP_UFT_PREFIX = "UFT_Build_";

    public void registerCommonArtifact(@NotNull Job job, @NotNull I18nBean i18nBean, @NotNull ArtifactDefinitionManager artifactDefinitionManager) {
        if (job == null || i18nBean == null || artifactDefinitionManager == null) {
            return;
        }

        String name = i18nBean.getText("Micro Focus Tasks Artifact Definition");
        String ARTIFACT_COPY_PATTERN = HP_UFT_PREFIX + "${bamboo.buildNumber}/**";
        if (artifactDefinitionManager.findArtifactDefinition(job, name) == null) {
            ArtifactDefinitionImpl artifactDefinition = new ArtifactDefinitionImpl(name, "", ARTIFACT_COPY_PATTERN);
            artifactDefinition.setProducerJob(job);
            artifactDefinitionManager.saveArtifactDefinition(artifactDefinition);
        }
    }
}

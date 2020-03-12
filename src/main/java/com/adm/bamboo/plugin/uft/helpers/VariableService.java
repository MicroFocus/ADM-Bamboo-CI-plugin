/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.adm.bamboo.plugin.uft.helpers;

import com.atlassian.bamboo.variable.VariableDefinition;
import com.atlassian.bamboo.variable.VariableDefinitionFactory;
import com.atlassian.bamboo.variable.VariableDefinitionFactoryImpl;
import com.atlassian.bamboo.variable.VariableDefinitionManager;

public class VariableService {
    private final VariableDefinitionManager variableDefinitionManager;

    public VariableService(VariableDefinitionManager variableDefinitionManager) {
        this.variableDefinitionManager = variableDefinitionManager;
    }

    public VariableDefinition getGlobalByKey(final String key) {
        return variableDefinitionManager.getGlobalVariableByKey(key);
    }

    public VariableDefinition saveGlobalVariable(final String key, final String value) {
        // enforce uniqueness --
        // bamboo seems to not check this constraint, but you do actually need to do it
        VariableDefinition searchVar = variableDefinitionManager.getGlobalVariableByKey(key);

        if (searchVar == null) { // doesn't exist, add it
            VariableDefinitionFactory variableDefinitionFactory = new VariableDefinitionFactoryImpl();
            VariableDefinition newVar = variableDefinitionFactory.createGlobalVariable(key, value);
            variableDefinitionManager.saveVariableDefinition(newVar);
            return newVar;
        } else { // does exist, find the existing VariableDefinition and update it
            long id = searchVar.getId();
            VariableDefinition updateVar = variableDefinitionManager.findVariableDefinition(id);
            if (updateVar != null) {
                updateVar.setValue(value);
            }
            variableDefinitionManager.saveVariableDefinition(updateVar);
            return updateVar;
        }
    }

    public void deleteGlobalVariable(final String variableKey) {
        VariableDefinition searchVar = variableDefinitionManager.getGlobalVariableByKey(variableKey);
        if (searchVar != null) {
            variableDefinitionManager.deleteVariableDefinition(searchVar);
        }
    }
}

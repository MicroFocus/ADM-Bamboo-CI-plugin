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
 * Copyright (c) EntIT Software LLC, a Micro Focus company
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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

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

package com.adm.bamboo.plugin.uft.helpers.locator;

public interface UFTLocatorService {

    /**
     * Checks by registry if the UFT is installed on the agent's machine
     * @return
     */
    boolean isInstalled();

    /**
     * Returns the absolute path of the UFT executable installation by registry
     * @return
     */
    String getPath();

    /**
     * Returns the absolute path of the UFT executable without validating it,
     * It will be validated each Task run
     * @param startingPathPoint The user given UFT path, which is not yet validated
     * @return
     */
    String getPathFromManualPoint(String startingPathPoint);

    /**
     * Validates by registry the UFT path
     * @param path
     * @return
     */
    boolean validateUFTPath(String path);

}

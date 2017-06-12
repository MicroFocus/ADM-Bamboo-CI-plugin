/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.bamboo.plugin.octane.listener;

import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.chains.plugins.PostJobAction;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;

public class OctanePostJobAction extends BaseListener implements PostJobAction {

	//  [YG] TODO: review the original intent for this listener and remove if not needed
	public void execute(StageExecution paramStageExecution, Job paramJob, BuildResultsSummary paramBuildResultsSummary) {
		PlanResultKey resultKey = paramBuildResultsSummary.getPlanResultKey();
	}
}

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

package com.hp.octane.plugins.bamboo.listener;

import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.chains.plugins.PreJobAction;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.integrations.dto.events.PhaseType;

import java.util.Arrays;

public class OctanePreJobAction extends BaseListener implements PreJobAction {

	public void execute(StageExecution paramStageExecution, BuildContext buildContext) {

		PlanResultKey resultKey = buildContext.getPlanResultKey();

		CIEventCause cause = CONVERTER.getCauseWithDetails(
				buildContext.getParentBuildIdentifier().getBuildResultKey(),
				buildContext.getParentBuildContext().getPlanResultKey().getPlanKey().getKey(), "admin");

		//create and send started event
		CIEvent event = CONVERTER.getEventWithDetails(
				resultKey.getPlanKey().getKey(),
				resultKey.getKey(),
				buildContext.getShortName(),
				CIEventType.STARTED,
				System.currentTimeMillis(),
				paramStageExecution.getChainExecution().getAverageDuration(),
				Arrays.asList(cause),
				String.valueOf(resultKey.getBuildNumber()),
				PhaseType.INTERNAL);

		OctaneSDK.getInstance().getEventsService().publishEvent(event);

		//create and send SCM event
		CIEvent scmEvent = CONVERTER.getEventWithDetails(
				resultKey.getPlanKey().getKey(),
				resultKey.getKey(),
				buildContext.getShortName(),
				CIEventType.SCM,
				System.currentTimeMillis(),
				paramStageExecution.getChainExecution().getAverageDuration(),
				Arrays.asList(cause),
				String.valueOf(resultKey.getBuildNumber()),
				CONVERTER.getScmData(buildContext),
				PhaseType.INTERNAL);

		OctaneSDK.getInstance().getEventsService().publishEvent(scmEvent);
	}
}

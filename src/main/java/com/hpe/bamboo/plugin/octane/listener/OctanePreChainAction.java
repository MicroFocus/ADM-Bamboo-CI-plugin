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

import com.atlassian.bamboo.chains.Chain;
import com.atlassian.bamboo.chains.ChainExecution;
import com.atlassian.bamboo.chains.plugins.PreChainAction;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.integrations.dto.events.PhaseType;

import java.util.ArrayList;
import java.util.List;

public class OctanePreChainAction extends BaseListener implements PreChainAction {

	public void execute(Chain chain, ChainExecution chainExecution) throws Exception {
		log.info("Executing chain " + chain.getName() + " build id "
				+ chainExecution.getBuildIdentifier().getBuildResultKey() + " build number "
				+ chainExecution.getBuildIdentifier().getBuildNumber());
		List<CIEventCause> causes = new ArrayList<CIEventCause>();
		CIEvent event = CONVERTER.getEventWithDetails(chainExecution.getPlanResultKey().getPlanKey().getKey(),
				chainExecution.getBuildIdentifier().getBuildResultKey(), chain.getName(), CIEventType.STARTED,
				chainExecution.getStartTime() != null ? chainExecution.getStartTime().getTime() : System.currentTimeMillis(),
				chainExecution.getAverageDuration(), causes,
				String.valueOf(chainExecution.getBuildIdentifier().getBuildNumber()),
				PhaseType.INTERNAL);

		OctaneSDK.getInstance().getEventsService().publishEvent(event);
	}
}

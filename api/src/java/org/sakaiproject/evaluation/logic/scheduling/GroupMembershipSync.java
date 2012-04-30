/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.logic.scheduling;

import org.quartz.Job;

public interface GroupMembershipSync extends Job {

	/**
	 * GroupMembershipSync: Name of class that handles synchronization of group memberships with external group provider.
	 */
	public static final String GROUP_MEMBERSHIP_SYNC_BEAN_NAME = "org.sakaiproject.evaluation.logic.scheduling.GroupMembershipSync";
	
	/**
	 * GroupMembershipSync: Key for property containing the list of eval states to be synchronized when memberships are updated.
	 */
	public static final String GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST = "org.sakaiproject.evaluation.logic.scheduling.GroupMembershipSync.stateList";

}
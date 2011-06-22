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
/**********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.evaluation.logic.impl.providers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.providers.EvalGroupsProvider;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * The University of Michigan implementation of EvalGroupsProvider.
 * 
 * Group id is comparable to CTools course site provider id (eid)
 * with one section. 
 * 
 * Note: This is currently relying on caching by other services,
 * but a cache for courses and enrollments could be implemented  
 * if performance needs to be improved per UMD's EvalCourseProvider.
 * 
 * Note: A very helpful "test EvalGroupProvider" page is available  
 * from the tool's Administrate page.
 * 
 * @author rwellis
 *
 */
public class EvalGroupsProviderUnivOfMichImpl implements EvalGroupsProvider {
	
	private static final Log log = LogFactory.getLog(EvalGroupsProviderUnivOfMichImpl.class);
	
	//UMIAC roles
	public static final String STUDENT_ROLE_STRING = "Student";
	public static final String INSTRUCTOR_ROLE_STRING = "Instructor";
	
	/* 
	 * STUDENT/TEACH sections are bogus, rigged up in 2005 so that they
	 * only show up in getUserSections so users can be permitted to a site 
	 * but are otherwise invisible to CTools.
	 */
	String[] skipSection = new String[]{",TEACH,000,000",",STUDENT,000,000"};
	
	//Spring injected
	private CourseManagementService courseManagementService;
	public void setCourseManagementService(CourseManagementService courseManagementService) {
		this.courseManagementService = courseManagementService;
	}
	private GroupProvider groupProvider;
	public void setGroupProvider(GroupProvider groupProvider) {
		this.groupProvider = groupProvider;
	}
	private SecurityService securityService;
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
	
	public void init() {
		//this is where we would build the cache
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.providers.EvalGroupsProvider#countEvalGroupsForUser(java.lang.String, java.lang.String)
	 */
	public int countEvalGroupsForUser(String userId, String permission) {
		return getEvalGroupsForUser(userId, permission).size();
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.providers.EvalGroupsProvider#countUserIdsForEvalGroups(java.lang.String[], java.lang.String)
	 */
	public int countUserIdsForEvalGroups(String[] groupIds, String permission) {
		return getUserIdsForEvalGroups(groupIds, permission).size();
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.providers.EvalGroupsProvider#getEvalGroupsForUser(java.lang.String, java.lang.String)
	 */
	public List getEvalGroupsForUser(String userId, String permission) {
		/*
		 * param permission is EvalConstants.PERM_TAKE_EVALUATION (provider.take.evaluation)
		 * or EvalConstants.PERM_BE_EVALUATED (provider.be.evaluated)
		 * param userId is Sakai user id (e.g., 8c285494-8e55-4a53-80f5-ac3f52ae18e5)
		 */
		if(log.isDebugEnabled())
			log.debug("userId '" + userId + "' permission '" + permission + "'");
		if(userId == null || "".equals((userId))) {
			if(log.isWarnEnabled())
				log.warn("getEvalGroupsForUser() parameter userId null or empty.");
			return null;
		}
		//TODO resolve EvalConstants.PERM_TAKE_EVALUATION & EvalGroupProvider.PERM_TAKE_EVALUATION issues if any
		if(permission == null || !(PERM_TAKE_EVALUATION.equals(permission) || 
				PERM_BE_EVALUATED.equals(permission))) {
			if(log.isWarnEnabled())
				log.warn("getEvalGroupsForUser() permission was null or not Student/Instructor.");
			return null;
		}
		List<EvalGroup> evalGroups = new ArrayList<EvalGroup>();
		String role = null;
		try
		{
			//UMIAC role (Student or Instructor)
			role = translatePermission(permission);
			User user = userDirectoryService.getUser(userId);
			//String eid = getEid(user.getEid());
			String eid = user.getEid();
			Map map = groupProvider.getGroupRolesForUser(eid);
			//i.e., base on Map map = getUmiac().getUserSections(eid);
			if (!map.isEmpty())
			{
				//get external group id -> role name map for this user in all external groups. (may be empty).
				for(Iterator i = map.entrySet().iterator(); i.hasNext();) {
					Map.Entry entry = (Map.Entry)i.next();
					//STUDENT/TEACH? compare to e.g., <PROVIDER_ID>2008,2,A,AAPTIS,217,001</PROVIDER_ID>
					if(((String)entry.getKey()).endsWith(skipSection[0]) || ((String)entry.getKey()).endsWith(skipSection[1])) {
						if(log.isDebugEnabled())
							log.debug("skipping section '" + (String)entry.getKey() + "'");
						continue;
					}
					//permission corresponds to role?
					if(((String)entry.getValue()).equalsIgnoreCase(role)) {
						evalGroups.add(getGroupByGroupId((String)entry.getKey()));
					}
				}
			}
		}
		catch(Exception e) {
			if(log.isWarnEnabled())
				log.warn("getEvalGroupsForUser() " + e);
		}
		return evalGroups;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.providers.EvalGroupsProvider#getGroupByGroupId(java.lang.String)
	 */
	public EvalGroup getGroupByGroupId(String groupId) {
		return new EvalGroup(groupId, getSectionTitle(groupId), EvalConstants.GROUP_TYPE_PROVIDED);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.providers.EvalGroupsProvider#getUserIdsForEvalGroups(java.lang.String[], java.lang.String)
	 */
	public Set getUserIdsForEvalGroups(String[] groupIds, String permission) {
		
		if(groupIds == null || groupIds.length < 1) {
			if(log.isWarnEnabled())
				log.warn("EvalGroup groupIds null or empty String[].");
			return null;
		}
		if(permission == null || !(PERM_TAKE_EVALUATION.equals(permission) || 
				PERM_BE_EVALUATED.equals(permission))) {
			if(log.isWarnEnabled())
				log.warn("Permission was null or not PERM_TAKE_EVALUATION/PERM_BE_EVALUATED.");
			return null;
		}
		Set<String> userIds = new HashSet<String>();
		String groupEid = null;
		String userId = null;
		String role = translatePermission(permission);
		for(int i = 0; i < groupIds.length; i++) {
			try
			{
				groupEid = groupIds[i];
				Map map = groupProvider.getUserRolesForGroup(groupEid);
				//i.e., base on Map map = getUmiac().getGroupRoles(eids);
				for(Iterator j = map.entrySet().iterator(); j.hasNext();) {
					Map.Entry<String, String> entry = (Map.Entry<String, String>)j.next();
					if(entry.getValue().equals(role)) {
						userId = ((User)userDirectoryService.getUserByEid(entry.getKey())).getId();
						userIds.add(userId);
					}
				}
			}
			catch (Exception e) {
				if(log.isWarnEnabled())
					log.warn("Group eid '" + groupEid + "' " + e);
			}
			continue;
		}
		return userIds;
	}

	/*
	* (non-Javadoc)
	* @see org.sakaiproject.evaluation.logic.providers.EvalGroupsProvider#isUserAllowedInGroup(java.lang.String, java.lang.String, java.lang.String)
	*/
	public boolean isUserAllowedInGroup(String userId, String permission, String groupId) {
		//parameter checks
		if(userId == null || "".equals(userId)) {
			if(log.isWarnEnabled())
				log.warn("isUserAllowedInGroup() userId parameter is null or an empty String.");
			return false;
		}
		if(permission == null || "".equals(permission)) {
			if(log.isWarnEnabled())
				log.warn("isUserAllowedInGroup() permision parameter is null or an empty String.");
			return false;
		}
		if(groupId == null || "".equals(groupId)) {
			if(log.isWarnEnabled())
				log.warn("isUserAllowedInGroup() groupId parameter is null or an empty String.");
			return false;
		}
		if(groupId.endsWith(skipSection[0]) || groupId.endsWith(skipSection[1])) {
			if(log.isDebugEnabled())
				log.debug("skipping section '" + groupId + "'");
			return false;
		}
		
		//test
		boolean answer = false;
		String role = translatePermission(permission);
		if(INSTRUCTOR_ROLE_STRING.equals(role) || STUDENT_ROLE_STRING.equals(role)) {
			try {
				User user = userDirectoryService.getUser(userId);
				answer = role.equals(groupProvider.getRole(groupId, user.getEid()));
				//i.e., base on Map map = getUmiac().getUserSections(user);
			}
			catch(Exception e) {
				if(log.isWarnEnabled())
					log.warn("isUserAllowedInGroup() userId '" + userId + "' permission '" + permission + "' groupId '" + groupId + "' " + e);
			}
		}
		return answer;
	}
	
	/**
	 * Get the User's eid from the id or log an error
	 * 
	 * @param userId - The sakai internal id
	 * @return - The external id
	 */
	public String getEid(String userId) {
		String eid = null;
		if(userId == null || "".equals(userId)) {
			log.error("getEid() - userId is null or an empty String.");
		}
		else
			{
			try {
				eid = userDirectoryService.getUserEid(userId);
			} catch(UserNotDefinedException e) {
				log.error("getEid() Could not get User with id '" + userId + "' " + e);
			}
			catch(Exception e) {
				log.error("getEid() Could not get eid from userId: " + userId, e);
			}
		}
		return eid;
	}
	
	/**
	 * Check if the current user is an admin
	 * 
	 * @param userId
	 * @return
	 */
	public boolean isUserAdmin(String userId) {
		log.info("Checking is eval super admin for: " + userId);
		return securityService.isSuperUser(userId);
	}

	/**
	 * Get the title of a section from the eid
	 * using the CourseManagementSystem
	 * 
	 * @param providerId the section eid e.g., 2008,2,A,AAPTIS,217,001
	 * @return the section title
	 */
	private String getSectionTitle(String providerId) {
		String title = providerId;
		Section section = null;
		section = courseManagementService.getSection(providerId);
		if(section != null)
			title = section.getTitle();
		return title;
	}
	
	 /**
     * Simple method to translate from Evaluation System permissions
     * to UMIAC role Student or Instructor
     * @param permission a PERM constant from {@link EvalConstants}
     * @return the UMIAC role
     */
    private String translatePermission(String permission) {
        if (PERM_TAKE_EVALUATION.equals(permission)) {
            return STUDENT_ROLE_STRING;
        } else if (PERM_BE_EVALUATED.equals(permission)) {
            return INSTRUCTOR_ROLE_STRING;
        }
        return "UNKNOWN";
    }
}

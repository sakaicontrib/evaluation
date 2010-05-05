/**
 * $Id$
 * $URL$
 * EvalExternalLogicImpl.java - evaluation - Dec 24, 2006 10:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.externals;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.entity.AssignGroupEntityProvider;
import org.sakaiproject.evaluation.logic.entity.ConfigEntityProvider;
import org.sakaiproject.evaluation.logic.entity.EvaluationEntityProvider;
import org.sakaiproject.evaluation.logic.entity.ItemEntityProvider;
import org.sakaiproject.evaluation.logic.entity.TemplateEntityProvider;
import org.sakaiproject.evaluation.logic.entity.TemplateItemEntityProvider;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalScheduledJob;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalConfig;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.providers.EvalGroupsProvider;
import org.sakaiproject.evaluation.utils.ArrayUtils;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.taskstream.client.TSSClientApi;
import org.sakaiproject.taskstream.client.TSSClientImpl;
import org.sakaiproject.taskstream.client.TSSQueryParameters;
import org.sakaiproject.taskstream.client.TSSResponseApi;
import org.sakaiproject.taskstream.client.TaskStatusStandardValues;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;


/**
 * This class handles the Sakai based implementation of the external logic interface<br/>
 * This is sort of the provider for the evaluation system though it should be broken up
 * if it is going to be used like that,
 * This is a BOTTOM level service and should depend on no other eval services (only those in Sakai)
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalExternalLogicImpl implements EvalExternalLogic {

   private static Log log = LogFactory.getLog(EvalExternalLogicImpl.class);
   private static Log metric = LogFactory.getLog("metrics." + EvalExternalLogicImpl.class.getName());

   private static final String SAKAI_SITE_TYPE = SiteService.SITE_SUBTYPE;
   private static final String SAKAI_GROUP_TYPE = SiteService.GROUP_SUBTYPE;

   private static final String ANON_USER_ATTRIBUTE = "AnonUserAttribute";
   private static final String ANON_USER_PREFIX = "Anon_User_";

   private static final String ADMIN_USER_ID = "admin";

   /**
    * This must match the id of the bean which implements {@link EvalScheduledInvocation}
    */
   protected final String SCHEDULER_SPRING_BEAN_ID = "org.sakaiproject.evaluation.logic.externals.EvalScheduledInvocation";

   private AuthzGroupService authzGroupService;
   public void setAuthzGroupService(AuthzGroupService authzGroupService) {
      this.authzGroupService = authzGroupService;
   }
   
   private ContentHostingService contentHostingService;
   public void setContentHostingService(ContentHostingService service) {
      this.contentHostingService = service;
   }

   private CourseManagementService courseManagementService;
   public void setCourseManagementService(CourseManagementService courseManagementService) {
	      this.courseManagementService = courseManagementService;
   }

   private EmailService emailService;
   public void setEmailService(EmailService emailService) {
      this.emailService = emailService;
   }

   private EntityBroker entityBroker;
   public void setEntityBroker(EntityBroker entityBroker) {
      this.entityBroker = entityBroker;
   }

   private EntityManager entityManager;
   public void setEntityManager(EntityManager entityManager) {
      this.entityManager = entityManager;
   }

   private FunctionManager functionManager;
   public void setFunctionManager(FunctionManager functionManager) {
      this.functionManager = functionManager;
   }
   
   protected ScheduledInvocationManager scheduledInvocationManager;
   public void setScheduledInvocationManager(ScheduledInvocationManager scheduledInvocationManager) {
      this.scheduledInvocationManager = scheduledInvocationManager;
   }

   private SecurityService securityService;
   public void setSecurityService(SecurityService securityService) {
      this.securityService = securityService;
   }

   private ServerConfigurationService serverConfigurationService;
   public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
      this.serverConfigurationService = serverConfigurationService;
   }

   private SessionManager sessionManager;
   public void setSessionManager(SessionManager sessionManager) {
      this.sessionManager = sessionManager;
   }
   
   private EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }

   private SiteService siteService;
   public void setSiteService(SiteService siteService) {
      this.siteService = siteService;
   }
   
  
   private TSSClientApi taskStatus;
   public void setTaskStatus(TSSClientApi taskStatus) {
	   this.taskStatus = taskStatus;
   }
 
   
   private ThreadLocalManager threadLocalManager;
   public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
	      this.threadLocalManager = threadLocalManager;
   }
   
   protected TimeService timeService;
   public void setTimeService(TimeService timeService) {
      this.timeService = timeService;
   }

   private ToolManager toolManager;
   public void setToolManager(ToolManager toolManager) {
      this.toolManager = toolManager;
   }

   private UserDirectoryService userDirectoryService;
   public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
      this.userDirectoryService = userDirectoryService;
   }

   public void init() {
      log.debug("init, register security perms");

      // register Sakai permissions for this tool
      registerPermissions();
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalComponents#getBean(java.lang.Class)
    */
   @SuppressWarnings("unchecked")
   public <T> T getBean(Class<T> type) {
      return (T) ComponentManager.get(type);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.EvalExternalLogic#getCurrentUserId()
    */
   public String getCurrentUserId() {
      String userId = sessionManager.getCurrentSessionUserId();
      if (userId == null) {
         // if no user found then fake like there is one for this session,
         // we do not want to actually create a user though
         Session session = sessionManager.getCurrentSession();
         userId = (String) session.getAttribute(ANON_USER_ATTRIBUTE);
         if (userId == null) {
            String sessionUserId = session.getId() + new Date().getTime();
            userId = ANON_USER_PREFIX + makeMD5(sessionUserId, 40);
            session.setAttribute(ANON_USER_ATTRIBUTE, userId);
         }
      }
      return userId;
   }

   /**
    * INTERNAL METHOD<br/>
    * Generate an invalid user with fields filled out correctly,
    * userId and email should not both be null
    * 
    * @param userId can be null
    * @param email can be null
    * @return
    */
   protected EvalUser makeInvalidUser(String userId, String email) {
      if (userId == null) {
         userId = "invalid:" + email;
      }
      EvalUser user = new EvalUser(userId, EvalConstants.USER_TYPE_INVALID, email);
      user.displayName = EvalConstants.USER_TYPE_INVALID;
      return user;
   }

   /**
    * INTERNAL METHOD<br/>
    * Generate an anonymous user with fields filled out correctly
    * 
    * @param userId
    * @return
    */
   protected EvalUser makeAnonymousUser(String userId) {
      return new EvalUser(userId, EvalConstants.USER_TYPE_ANONYMOUS,
            null, "eid:" + userId, EvalConstants.USER_TYPE_ANONYMOUS);
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalUsers#isUserAnonymous(java.lang.String)
    */
   public boolean isUserAnonymous(String userId) {
      String currentUserId = sessionManager.getCurrentSessionUserId();
      if (userId.equals(currentUserId)) {
         return false;
      }
      Session session = sessionManager.getCurrentSession();
      String sessionUserId = (String) session.getAttribute(ANON_USER_ATTRIBUTE);
      if (userId.equals(sessionUserId)) {
         return true;
      }
      // used up both cheap tests, now try the costly one
      try {
        userDirectoryService.getUser(userId);
      } catch (UserNotDefinedException e) {
         return true;
      }
      return false;
   }
   
   /*
    * (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.EvalExternalLogic#getEvalToolTitle()
    */
   public String getEvalToolTitle() {
	   String toolTitle = EvalConstants.EVAL_TOOL_TITLE;
	   //Tool tool = toolManager.getCurrentTool();
	   //String toolTitle = tool.getTitle();
	   return toolTitle;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalUsers#getUserId(java.lang.String)
    */
   public String getUserId(String username) {
      String userId = null;
      try {
         userId = userDirectoryService.getUserId(username);
      } catch(UserNotDefinedException ex) {
         log.error("Could not get userId from username: " + username);
      }
      return userId;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalUsers#getUserUsername(java.lang.String)
    */
   public String getUserUsername(String userId) {
      String username = "------";
      if (isUserAnonymous(userId)) {
         username = "anonymous";
      } else {
         try {
            username = userDirectoryService.getUserEid(userId);
         } catch(UserNotDefinedException ex) {
            log.warn("Sakai could not get username from userId: " + userId, ex);
         }
      }
      return username;
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalUsers#getEvalUserById(java.lang.String)
    */
   public EvalUser getEvalUserById(String userId) {
      EvalUser user = makeInvalidUser(userId, null);
      if (isUserAnonymous(userId)) {
         user = makeAnonymousUser(userId);
      }
      else {
         try {
            User sakaiUser = userDirectoryService.getUser(userId);
            user = new EvalUser(userId, EvalConstants.USER_TYPE_EXTERNAL,
                  sakaiUser.getEmail(), sakaiUser.getEid(), sakaiUser.getDisplayName());
         } catch(UserNotDefinedException ex) {
            log.debug("Sakai could not get user from userId: " + userId, ex);
         }
      }
      return user;
   }
   
   /*
    * (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.EvalExternalLogic#getEvalUserByEid(java.lang.String)
    */
   public EvalUser getEvalUserByEid(String eid) {
	   EvalUser user = makeInvalidUser(eid, null);
	   try {
		   User sakaiUser = userDirectoryService.getUserByEid(eid);
		   if (isUserAnonymous(sakaiUser.getId())) {
			   user = makeAnonymousUser(eid);
		   }
		   else {
			   /*
			   user = new EvalUser(eid, EvalConstants.USER_TYPE_EXTERNAL,
	    				 sakaiUser.getEmail(), sakaiUser.getEid(), sakaiUser.getDisplayName());
	    				 */
			   user = new EvalUser(sakaiUser.getId(), EvalConstants.USER_TYPE_EXTERNAL,
	    				 sakaiUser.getEmail(), sakaiUser.getEid(), sakaiUser.getDisplayName());
		   }
	     }
	     catch (UserNotDefinedException ex) {
	    	 log.debug("Sakai could not get user from user external id: " + eid, ex);
	     }
	     return user;
   }
   
   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalUsers#getEvalUserByEmail(java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public EvalUser getEvalUserByEmail(String email) {
      EvalUser user = makeInvalidUser(null, email);
      Collection<User> sakaiUsers = userDirectoryService.findUsersByEmail(email);
      if (sakaiUsers.size() > 0) {
         User sakaiUser = sakaiUsers.iterator().next(); // just get the first one
         user = new EvalUser(sakaiUser.getId(), EvalConstants.USER_TYPE_EXTERNAL,
               sakaiUser.getEmail(), sakaiUser.getEid(), sakaiUser.getDisplayName());
      }
      return user;
   }



   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalUsers#getEvalUsersByIds(java.lang.String[])
    */
   @SuppressWarnings("unchecked")
   public Map<String, EvalUser> getEvalUsersByIds(String[] userIds) {
      Map<String, EvalUser> users = new HashMap<String, EvalUser>();
      boolean foundAll = false;
      if (userIds == null 
            || userIds.length == 0) {
         foundAll = true;
      }

      if (! foundAll) {
         // get remaining users from Sakai
         Map<String, User> sakaiUsers = getSakaiUsers(userIds);
         for (String userId : sakaiUsers.keySet()) {
            User sakaiUser = sakaiUsers.get(userId);
            EvalUser user = new EvalUser(sakaiUser.getId(), EvalConstants.USER_TYPE_EXTERNAL,
                  sakaiUser.getEmail(), sakaiUser.getEid(), sakaiUser.getDisplayName());
            users.put(userId, user);
         }
      }
      return users;
   }
   
   public String getSectionTitle(String providerId) {
		String title = providerId;
		Section section = null;
		section = courseManagementService.getSection(providerId);
		if(section != null)
			title = section.getTitle();
		return title;
   }
   
   /*
    * (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#getMyWorkspaceUrl(java.lang.String)
    */
   public String getMyWorkspaceUrl(String userId) {
	   String url = EvalConstants.NO_LINK_AVAILABLE;
	   try {
		   String toolPage = null;
		   //userId is Sakai id
		   if(userId == null || userId.length() == 0) {
			   log.error("getMyWorkspaceUrl(String userId) userId is null or empty.");
		   }
		   else {
			   String myWorkspaceId = siteService.getUserSiteId(userId);
			   Site myWorkspace = siteService.getSite(myWorkspaceId);
			   List<SitePage> pages = myWorkspace.getPages();
			   for (Iterator<SitePage> i = pages.iterator(); i.hasNext();) {
				   SitePage page = i.next();
				   List<ToolConfiguration> tools = page.getTools();
				   for (Iterator<ToolConfiguration> j = tools.iterator(); j.hasNext();) {
					   ToolConfiguration tc = j.next();
					   if (tc.getToolId().equals("sakai.rsf.evaluation")) {
						   toolPage = page.getId();
						   break;
					   }
				   }
			   }
			   // e.g., https://testctools.ds.itd.umich.edu/portal/site/~37d8035e-54b3-425c-bcb5-961e881d2afe/page/866dd4e6-0323-43a1-807c-9522bb3167b7
			   url = getServerUrl() + "/site/" + myWorkspaceId + "/page/" + toolPage;
			   }
	   } catch (Exception e) {
		   log.warn("getMyWorkspaceUrl(String userId) '" + userId + "' " + e);
	   }
	   return url;
	}


   /**
    * Safe method for getting a large number of users from Sakai
    * 
    * @param userIds an array of internal user ids
    * @return a map of userId -> {@link User}
    */
   public Map<String, User> getSakaiUsers(String[] userIds) {
      // TODO - cannot use this because of the way the UDS works (it will not let us call this unless
      // the user already exists in Sakai -AZ
//    // get the list of users efficiently
//    List userIds = Arrays.asList( toUserIds );
//    List l = userDirectoryService.getUsers( userIds );

      // handling this in a much less efficient way for now (see above comment) -AZ
      Map<String, User> sakaiUsers = new HashMap<String, User>(); // fill this with users
      for (int i = 0; i < userIds.length; i++) {
         User user = null;
         try {
            user = userDirectoryService.getUser( userIds[i] );
         } catch (UserNotDefinedException e) {
            log.debug("Cannot find user object by id:" + userIds[i] );
            try {
               user = userDirectoryService.getUserByEid( userIds[i] );
            } catch (UserNotDefinedException e1) {
               log.debug("Cannot find user object by eid:" + userIds[i] );
            }
         }
         if (user != null) {
            sakaiUsers.put(user.getId(), user);
         }
      }
      return sakaiUsers;
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.EvalExternalLogic#isUserAdmin(java.lang.String)
    */
   public boolean isUserAdmin(String userId) {
      log.debug("Checking is eval super admin for: " + userId);
      return securityService.isSuperUser(userId);
   }
   
   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalUsers#getUserLocale(java.lang.String)
    */
   public Locale getUserLocale(String userId) {
      log.debug("userId: " + userId);
      //log.warn("can only get the locale for the current user right now...");
      // TODO - this sucks because there is no way to get the locale for anything but the
      // current user.... terrible -AZ
      return new ResourceLoader().getLocale();
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#makeEvalGroupObject(java.lang.String)
    */
   public EvalGroup makeEvalGroupObject(String evalGroupId) {
      if (evalGroupId == null) {
         throw new IllegalArgumentException("evalGroupId cannot be null");
      }

      EvalGroup c = null;

      if (c == null) {
         // check Sakai
         try {
            // try to get the site object based on the entity reference (which is the evalGroupId)
            Object entity = entityBroker.fetchEntity(evalGroupId);
            if (entity instanceof Site) {
               Site site = (Site) entityBroker.fetchEntity(evalGroupId);
               c = new EvalGroup( evalGroupId, site.getTitle(), 
                     getContextType(SAKAI_SITE_TYPE) );
            } else if (entity instanceof Group) {
               Group group = (Group) entityBroker.fetchEntity(evalGroupId);
               c = new EvalGroup( evalGroupId, group.getTitle(), 
                     getContextType(SAKAI_GROUP_TYPE) );
            }
         } catch (Exception e) {
            // invalid site reference
            log.debug("Could not get sakai site from evalGroupId:" + evalGroupId, e);
            c = null;
         }
      }

      if (c == null) {
    	  // changed from error to debug because it is not an error if the group type is Provided
         log.debug("Could not get group from evalGroupId:" + evalGroupId);
         // create a fake group placeholder as an error notice
         c = new EvalGroup( evalGroupId, "** INVALID: "+evalGroupId+" **", 
               EvalConstants.GROUP_TYPE_INVALID );
      }

      return c;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#getCurrentEvalGroup()
    */
   public String getCurrentEvalGroup() {
      String location = null;
      try {
         String context = toolManager.getCurrentPlacement().getContext();
         Site s = siteService.getSite( context );
         location = s.getReference(); // get the entity reference to the site
      } catch (Exception e) {
         // sakai failed to get us a location so we can assume we are not inside the portal
         location = null;
      }
      return location;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#countEvalGroupsForUser(java.lang.String, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public int countEvalGroupsForUser(String userId, String permission) {
      log.debug("userId: " + userId + ", permission: " + permission);

      int count = 0;
      Set<String> authzGroupIds = authzGroupService.getAuthzGroupsIsAllowed(userId, permission, null);
      Iterator<String> it = authzGroupIds.iterator();
      while (it.hasNext()) {
         String authzGroupId = it.next();
         Reference r = entityManager.newReference(authzGroupId);
         if(r.isKnownType()) {
            // check if this is a Sakai Site or Group
            if(r.getType().equals(SiteService.APPLICATION_ID)) {
               count++;
            }
         }
      }

      return count;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#getEvalGroupsForUser(java.lang.String, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public List<EvalGroup> getEvalGroupsForUser(String userId, String permission) {
      log.debug("userId: " + userId + ", permission: " + permission);

      List<EvalGroup> l = new ArrayList<EvalGroup>();

      // get the groups from Sakai
      Set<String> authzGroupIds = 
         authzGroupService.getAuthzGroupsIsAllowed(userId, permission, null);
      Iterator<String> it = authzGroupIds.iterator();
      while (it.hasNext()) {
         String authzGroupId = it.next();
         Reference r = entityManager.newReference(authzGroupId);
         if (r.isKnownType()) {
            // check if this is a Sakai Site or Group
            if (r.getType().equals(SiteService.APPLICATION_ID)) {
               String type = r.getSubType();
               if (SAKAI_SITE_TYPE.equals(type)) {
                  // this is a Site
                  String siteId = r.getId();
                  try {
                     Site site = siteService.getSite(siteId);
                     l.add(new EvalGroup(r.getReference(), site.getTitle(), 
                           getContextType(r.getType())));
                  } catch (IdUnusedException e) {
                     // invalid site Id returned
                     throw new RuntimeException("Could not get site from siteId:" + siteId);
                  }

               } else if (SAKAI_GROUP_TYPE.equals(type)) {
                  // this is a Group in a Site
                  String groupId = r.getId();
                  Group group = siteService.findGroup(groupId);
                  if (group == null) {
                     throw new RuntimeException("Could not get group from group id:" + groupId);
                  }
                  l.add(new EvalGroup(r.getReference(), group.getTitle(), 
                        getContextType(r.getType())));
               }
            }
         }
      }

      if (l.isEmpty()) log.info("Empty list of groups for user:" + userId + ", permission: " + permission);
      return l;
   }
   
   @SuppressWarnings("unchecked")
   public Map<String, List<EvalGroup>> getEvalGroupsForUser(String userId) {
	   Map<String, List<EvalGroup>> map = new HashMap<String, List<EvalGroup>>();
	   map.put(EvalConstants.PERM_TAKE_EVALUATION, getEvalGroupsForUser(userId, EvalConstants.PERM_TAKE_EVALUATION));
	   map.put(EvalConstants.PERM_BE_EVALUATED, getEvalGroupsForUser(userId, EvalConstants.PERM_BE_EVALUATED));
	   return map;
	}

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#countUserIdsForEvalGroup(java.lang.String, java.lang.String)
    */
   public int countUserIdsForEvalGroup(String evalGroupId, String permission) {
      // get the count from the method which retrieves all the groups,
      // this method might be better to retire
      return getUserIdsForEvalGroup(evalGroupId, permission).size();
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#getUserIdsForEvalGroup(java.lang.String, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public Set<String> getUserIdsForEvalGroup(String evalGroupId, String permission) {
      Set<String> userIds = new HashSet<String>();

      // only go on to check the Sakai sites/groups if nothing was found
      if (userIds.size() == 0) {
         String reference = evalGroupId;
         List<String> azGroups = new ArrayList<String>();
         azGroups.add(reference);
         userIds.addAll( authzGroupService.getUsersIsAllowed(permission, azGroups) );
         // need to remove the admin user or else they show up in unwanted places
         if (userIds.contains(ADMIN_USER_ID)) {
            userIds.remove(ADMIN_USER_ID);
         }
      }

      return userIds;
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#isUserAllowedInEvalGroup(java.lang.String, java.lang.String, java.lang.String)
    */
   public boolean isUserAllowedInEvalGroup(String userId, String permission, String evalGroupId) {

      /* NOTE: false checks end up being really costly and should probably be cached
       */

      if (evalGroupId == null) {
         // special check for the admin user
         if (isUserAdmin(userId)) {
            return true;
         }
         return false;
      }

      // try checking Sakai
      String reference = evalGroupId;
      if ( securityService.unlock(userId, permission, reference) ) {
         return true;
      }

      return false;
   }
   
   /*
    * EmailService batch email method
    * 	public void sendMail(InternetAddress from, InternetAddress[] to, Map Hsubject, Map Hcontent, InternetAddress[] headerTo,
			InternetAddress[] replyTo, List additionalHeader) {
    */
   
   /*
    * (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalEmail#sendEmailsToAddresses(java.lang.String, java.lang.String[], java.lang.String, java.lang.String, boolean)
    */
   public String[] sendEmailsToAddresses(String from, String[] to, String subject, String message, boolean deferExceptions) {
      String exceptionTracker = null;

      InternetAddress fromAddress;
      try {
         fromAddress = new InternetAddress(from);
      } catch (AddressException e) {
         // cannot recover from this failure
         throw new IllegalArgumentException("Invalid from address: " + from, e);
      }

      List<String> toEmails = new ArrayList<String>();
      for (int i = 0; i < to.length; i++) {
         String email = to[i];
         if (email == null || email.equals("")) {
            if (deferExceptions) {
               exceptionTracker += "blank or null to address in list ("+i+") :: ";
               log.error("blank or null to address in list ("+i+"): " + ArrayUtils.arrayToString(to));
               continue;
            } else {
               // die here since we were unable to find this user at all
               throw new IllegalArgumentException("blank or null to address ("+i+"): " + ArrayUtils.arrayToString(to) + ", cannot send emails");
            }
         }
         toEmails.add(email);
      }

      return sendEmails(fromAddress, toEmails, subject, message, deferExceptions, exceptionTracker);
   }
   
   /**
    * Handle the actual sending of the email
    * @param fromAddress
    * @param toEmails
    * @param subject
    * @param message
    * @param deferExceptions
    * @param exceptionTracker
    * @return
    */
   private String[] sendEmails(InternetAddress fromAddress, List<String> toEmails, String subject,
         String message, boolean deferExceptions, String exceptionTracker) {
      InternetAddress[] replyTo = new InternetAddress[1];
      List<InternetAddress> listAddresses = new ArrayList<InternetAddress>();
      for (int i = 0; i < toEmails.size(); i++) {
         String email = toEmails.get(i);
         try {
            InternetAddress toAddress = new InternetAddress(email);
            listAddresses.add(toAddress);
         } catch (AddressException e) {
            if (deferExceptions) {
               exceptionTracker += e.getMessage() + " :: ";
               log.error("Invalid to address: " + email + ", skipping...", e);
               continue;
            } else {
               // die here since we were unable to find this user at all
               throw new IllegalArgumentException("Invalid to address: " + email + ", cannot send emails", e);
            } 
         }
      }
      Object[] mailSettings = new Object[6];
      mailSettings = getSingleEmailSettings();
      String deliveryOption = (String)mailSettings[1];
      replyTo[0] = fromAddress;
      InternetAddress[] toAddresses = listAddresses.toArray(new InternetAddress[listAddresses.size()]);
      
      //dispose of email by sending, logging, or doing nothing
      if(EvalConstants.EMAIL_DELIVERY_DEFAULT.equals(deliveryOption) || EvalConstants.EMAIL_DELIVERY_SEND.equals(deliveryOption)) {
    	  emailService.sendMail(fromAddress, toAddresses, subject, message, null, replyTo, null);
      }
      else if(EvalConstants.EMAIL_DELIVERY_LOG.equals(deliveryOption)) {
    	  logEmails(fromAddress, toAddresses, subject, message, replyTo);
      }
      else if(EvalConstants.EMAIL_DELIVERY_NONE.equals(deliveryOption)) {
    	  if(log.isWarnEnabled())
    		  log.warn("Email delivery option is set to 'none'.");
      }

      if (deferExceptions && exceptionTracker != null) {
         // exceptions occurred so we have to die here
         throw new IllegalArgumentException("The following exceptions were deferred (full trace above in the log): " + 
               "Sent emails to " + toAddresses.length + " addresses before failure ocurred: Failure summary: " +
               exceptionTracker);
      }

      // now we send back the list of people who the email was sent to
      String[] addresses = new String[toAddresses.length];
      for (int i = 0; i < toAddresses.length; i++) {
         addresses[i] = toAddresses[i].getAddress();
      }
      return addresses;
   }
   
   /**
    * Log email messages
    * 
    * @param from
    * @param toUserIds
    * @param subject
    * @param message
    * @param replyTo
    */
   private void logEmails(InternetAddress from, InternetAddress[] toUserIds, String subject, String message, InternetAddress[] replyTo) {
	   if(metric.isInfoEnabled()) {
		   StringBuffer sbTo = new StringBuffer();
		   for(int i = 0; i < toUserIds.length; i++) {
			   if(sbTo.length() != 0)
				   sbTo.append(",");
			   sbTo = sbTo.append(toUserIds[i].getAddress());
		   }
		   String to = sbTo.toString();
		   StringBuffer sbReplyTo = new StringBuffer();
		   for(int i = 0; i < replyTo.length; i++) {
			   if(sbReplyTo.length() != 0)
				   sbReplyTo.append(",");
			   sbReplyTo = sbReplyTo.append(replyTo[i].getAddress());
		   }
		   String reply = sbReplyTo.toString();
		   metric.info("Email \nFrom: " + from.getAddress() + " \nReplyTo: " + reply +
				   " \nTo: " + "" + to + " \nSubject: " + subject + " \nMessage: " + message);
	   }
   }
	   
   // ENTITIES


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#getServerUrl()
    */
   public String getServerUrl() {
      return serverConfigurationService.getPortalUrl();
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#getEntityURL(java.io.Serializable)
    */
   public String getEntityURL(Serializable evaluationEntity) {
      String ref = getEntityReference(evaluationEntity);
      if (ref != null) {
         return entityBroker.getEntityURL(ref);
      } else {
         return serverConfigurationService.getPortalUrl();
      }
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#getEntityURL(java.lang.String, java.lang.String)
    */
   public String getEntityURL(String entityPrefix, String entityId) {
      String ref = new EntityReference(entityPrefix, entityId).toString();
      if (ref != null) {
         return entityBroker.getEntityURL(ref);
      } else {
         return serverConfigurationService.getPortalUrl();
      }
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#registerEntityEvent(java.lang.String, java.io.Serializable)
    */
   public void registerEntityEvent(String eventName, Serializable evaluationEntity) {
      String ref = getEntityReference(evaluationEntity);
      if (ref != null) {
         log.debug("Entity event: " + eventName + " for " + ref);
         entityBroker.fireEvent(eventName, ref);
      }
   }

   public void registerEntityEvent(String eventName, Class<? extends Serializable> entityClass, String entityId) {
      String ref = getEntityReference(entityClass, entityId);
      if (ref != null) {
         log.debug("Entity event: " + eventName + " for " + ref);
         entityBroker.fireEvent(eventName, ref);
      }
   }

   /**
    * Get an entity reference to any of the evaluation objects which are treated as entities
    * 
    * @param entity
    * @return an entity reference string or null if none can be found
    */
   protected String getEntityReference(Serializable entity) {
      String id = null;
      try {
         Class<? extends Serializable> elementClass = entity.getClass();
         Method getIdMethod = elementClass.getMethod("getId", new Class[] {});
         Long realId = (Long) getIdMethod.invoke(entity, (Object[]) null);
         id = realId.toString();
         return getEntityReference(elementClass, id);
      } catch (Exception e) {
         log.warn("Failed to get id from entity object", e);
         return null;
      }
   }

   protected String getEntityReference(Class<? extends Serializable> entityClass, String entityId) {
      String prefix = null;
      // make sure this class is supported and get the prefix
      if (entityClass == EvalEvaluation.class) {
         prefix = EvaluationEntityProvider.ENTITY_PREFIX;
      } else if (entityClass == EvalAssignGroup.class) {
         prefix = AssignGroupEntityProvider.ENTITY_PREFIX;
      } else if (entityClass == EvalAssignHierarchy.class) {
         prefix = "eval-assignhierarchy";
      } else if (entityClass == EvalGroup.class) {
         prefix = "eval-group";
      } else if (entityClass == EvalScale.class) {
         prefix = "eval-scale";
      } else if (entityClass == EvalItem.class) {
         prefix = ItemEntityProvider.ENTITY_PREFIX;
      } else if (entityClass == EvalTemplateItem.class) {
         prefix = TemplateItemEntityProvider.ENTITY_PREFIX;
      } else if (entityClass == EvalTemplate.class) {
         prefix = TemplateEntityProvider.ENTITY_PREFIX;
      } else if (entityClass == EvalResponse.class) {
         prefix = "eval-response";
      } else if (entityClass == EvalConfig.class) {
         prefix = ConfigEntityProvider.ENTITY_PREFIX;
      } else {
         return "eval:" + entityClass.getName();
      }

      return new EntityReference(prefix, entityId).toString();
   }

   /**
    * Register the various permissions
    */
   protected void registerPermissions() {
      // register Sakai permissions for this tool
      functionManager.registerFunction(EvalConstants.PERM_WRITE_TEMPLATE);
      functionManager.registerFunction(EvalConstants.PERM_ASSIGN_EVALUATION);
      functionManager.registerFunction(EvalConstants.PERM_BE_EVALUATED);
      functionManager.registerFunction(EvalConstants.PERM_TAKE_EVALUATION);
   }

   /**
    * Returns the appropriate internal site type identifier based on the given string
    * 
    * @param sakaiType a type identifier used in Sakai (should be a constant from Sakai)
    * @return a CONTEXT_TYPE constant from {@link EvalConstants}
    */
   protected String getContextType(String sakaiType) {
      if (sakaiType.equals(SAKAI_SITE_TYPE)) {
         return EvalConstants.GROUP_TYPE_SITE;
      } else if (sakaiType.equals(SAKAI_GROUP_TYPE)) {
         return EvalConstants.GROUP_TYPE_GROUP;
      }
      return EvalConstants.GROUP_TYPE_UNKNOWN;
   }

   /**
    * Simple method to translate one constant into another
    * (this allows the provider to have no dependency on the model)
    * @param permission a PERM constant from {@link EvalConstants}
    * @return the translated constant from {@link EvalGroupsProvider}
    */
   protected String translatePermission(String permission) {
      if (EvalConstants.PERM_TAKE_EVALUATION.equals(permission)) {
         return EvalGroupsProvider.PERM_TAKE_EVALUATION;
      } else if (EvalConstants.PERM_BE_EVALUATED.equals(permission)) {
         return EvalGroupsProvider.PERM_BE_EVALUATED;
      }
      return "UNKNOWN";
   }

   /**
    * @param text string to make MD5 hash from
    * @param maxLength
    * @return an MD5 hash no longer than maxLength
    */
   protected String makeMD5(String text, int maxLength) {

      MessageDigest md;
      try {
         md = MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException e) {
         throw new RuntimeException("Stupid java sucks for MD5", e);
      }
      md.update(text.getBytes());

      // convert the binary md5 hash into hex
      String md5 = "";
      byte[] b_arr = md.digest();

      for (int i = 0; i < b_arr.length; i++) {
         // convert the high nibble
         byte b = b_arr[i];
         b >>>= 4;
      b &= 0x0f; // this clears the top half of the byte
      md5 += Integer.toHexString(b);

      // convert the low nibble
      b = b_arr[i];
      b &= 0x0F;
      md5 += Integer.toHexString(b);
      }
      if (maxLength > 0 && md5.length() > maxLength) {
         md5 = md5.substring(0, maxLength);
      }
      return md5;
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.EvalExternalLogic#getConfigurationSetting(java.lang.String, java.lang.Object)
    */
   @SuppressWarnings("unchecked")
   public <T> T getConfigurationSetting(String settingName, T defaultValue) {
      T returnValue = defaultValue;
      if (SETTING_SERVER_NAME.equals(settingName)) {
         returnValue = (T) serverConfigurationService.getServerName();
      } else if (SETTING_SERVER_URL.equals(settingName)) {
         returnValue = (T) serverConfigurationService.getServerUrl();
      } else if (SETTING_PORTAL_URL.equals(settingName)) {
         returnValue = (T) serverConfigurationService.getPortalUrl();
      } else if (SETTING_SERVER_ID.equals(settingName)) {
         returnValue = (T) serverConfigurationService.getServerIdInstance();
      } else {
         if (defaultValue == null) {
            returnValue = (T) serverConfigurationService.getString(settingName);
            if ("".equals(returnValue)) { returnValue = null; }
         } else {
            if (defaultValue instanceof Number) {
               int num = ((Number) defaultValue).intValue();
               int value = serverConfigurationService.getInt(settingName, num);
               returnValue = (T) new Integer(value);
            } else if (defaultValue instanceof Boolean) {
               boolean bool = ((Boolean) defaultValue).booleanValue();
               boolean value = serverConfigurationService.getBoolean(settingName, bool);
               returnValue = (T) new Boolean(value);
            } else if (defaultValue instanceof String) {
               returnValue = (T) serverConfigurationService.getString(settingName, (String) defaultValue);
            }
         }
      }
      return returnValue;
   }
   
   /*
    * (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalContent#checkResource(java.lang.String)
    */
   public boolean checkResource(String resourceId) {
	   if(resourceId == null)
		   return false;
	   try {
		   contentHostingService.checkResource(resourceId);
		   return true;
	   }
	   catch(Exception e) {
		   return false;
	   }
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.ExternalContent#getFileContent(java.lang.String)
    */
   public byte[] getFileContent(String abspath) {
      try {
         ContentResource contentResource = contentHostingService.getResource(abspath);
         return contentResource.getContent();
      } catch (Exception e) {
         log.warn("Cannot byte array for Content Hosting File", e);
         return null;
      }
   }
   
   /*
    * (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalContent#getStreamContent(java.lang.String)
    */
   public InputStream getStreamContent(String resourceId) {
	   try {
			ContentResource resource = contentHostingService.getResource(resourceId);
			InputStream in = resource.streamContent();
			return in;
	   }
	   catch(Exception e) {
	         log.warn("Cannot get InputStream for ContentResource. " + e);
	         return null;
	   }
   }
   
   /*
    * (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalContent#setImportedResourceAttributes()
    */
   public void setImportedResourceAttributes() {
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		toolSession.setAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT,
				"XML File Data Import");
		toolSession.setAttribute(FilePickerHelper.FILE_PICKER_INSTRUCTION_TEXT,
				"Please select an XML data file from which to read data.");
		toolSession.setAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS,
				FilePickerHelper.CARDINALITY_SINGLE);
	}
   
   /*
    * (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalContent#getImportResourceId()
    */
   public String getImportedResourceId() {
		String id = null;
		List refs = null;

		ToolSession toolSession = sessionManager.getCurrentToolSession();

		// check that there is an attachment
		if (toolSession.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null
				&& toolSession
						.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) {
			refs = (List) toolSession
					.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
			if (refs == null || refs.size() != 1) {
				return null;
			}
			toolSession
					.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
			toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
			Reference ref = (Reference) refs.get(0);
			id = ref.getId();
			// check that the resource is accessible
			if (checkResource(id)) {
				return id;
			}
		}
		return null;
	}

   /*
    * (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalContent#removeResource(java.lang.String)
    */
   public boolean removeResource(String resourceId) {
	   try {
		   contentHostingService.removeResource(resourceId);
		   return true;
	   }
	   catch(Exception e) {
		   return false;
	   }
   }
   
   /*
    * (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.EvalExternalLogic#setSessionActive()
    */
   public void setSessionActive() {
	Session session = sessionManager.getCurrentSession();
	if(ADMIN_USER_ID.equals(session.getUserId())) {
			session.setActive();
		}
   }
   
   /*
    * (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.EvalExternalLogic#setSessionUserIdAdmin()
    */
   public boolean setSessionUserIdAdmin(String uid) {
	   //TODO protect this call
	   Session session = sessionManager.getCurrentSession();
	   if (session != null && uid != null) {
		   // if uid is in the admin group
		   if(isUserAdmin(uid)) {
			   //set the session user to admin
			   //session.setUserId(userDirectoryService.ADMIN_ID);
			   session.setUserId(uid);
			   return true;
			}
		}
		return false;
	}
   
   /*
    * (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.EvalExternalLogic#clearBindings()
    */
   public void clearBindings() {
	   ThreadLocalManager.clear();
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.EvalExternalLogic#cleanupUserStrings(java.lang.String)
    */
   @SuppressWarnings("deprecation")
   public String cleanupUserStrings(String userSubmittedString) {
      // clean up the string using Sakai text format (should stop XSS)
      // CANNOT CHANGE THIS TO STRINGBUILDER OR 2.4.x and below will fail -AZ
      return FormattedText.processFormattedText(userSubmittedString, new StringBuffer());
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalTextUtils#makePlainTextFromHTML(java.lang.String)
    */
   public String makePlainTextFromHTML(String html) {
      return FormattedText.convertFormattedTextToPlaintext(html);
   }


   // JOBS related pass-through methods

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalScheduler#createScheduledJob(java.util.Date, java.lang.String)
    */
   public String createScheduledJob(Date executionDate, Long evaluationId, String jobType) {
      String jobKey = EvalScheduledJob.encodeContextId(evaluationId, jobType);
      return scheduledInvocationManager.createDelayedInvocation(
            timeService.newTime(executionDate.getTime()), 
            SCHEDULER_SPRING_BEAN_ID, jobKey);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalScheduler#deleteScheduledJob(java.lang.String)
    */
   public void deleteScheduledJob(String jobID) {
      scheduledInvocationManager.deleteDelayedInvocation(jobID);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.ExternalScheduler#findScheduledJobs(java.lang.String)
    */
   public EvalScheduledJob[] findScheduledJobs(Long evaluationId, String jobType) {
      String jobKey = EvalScheduledJob.encodeContextId(evaluationId, jobType);
      DelayedInvocation[] invocations = scheduledInvocationManager.findDelayedInvocations(SCHEDULER_SPRING_BEAN_ID, jobKey);
      EvalScheduledJob[] jobs = new EvalScheduledJob[0];
      if (invocations != null) {
         jobs = new EvalScheduledJob[invocations.length];
         for (int i = 0; i < invocations.length; i++) {
            DelayedInvocation inv = invocations[i];
            jobs[i] = new EvalScheduledJob(inv.uuid, inv.date, inv.componentId, inv.contextId);
         }
      }
      return jobs;
   }
   /**
    * Get email settings used with single email option
    * 
    * @param from
    * @param deliveryOption
    * @param logEmailRecipients
    * @param batch
    * @param wait
    * @param modulo
    */
   private Object[] getSingleEmailSettings() {
	   Object[] emailSettings = new Object[6];
		emailSettings[0] = (String) settings.get(EvalSettings.FROM_EMAIL_ADDRESS);
	    if (emailSettings[0] == null) {
	         throw new IllegalStateException("Could not get a from email address from system settings or the evaluation");
	    }
	    emailSettings[1] = (String) settings.get(EvalSettings.EMAIL_DELIVERY_OPTION);
	    if (emailSettings[1] == null) {
	         throw new IllegalStateException("Could not get the delivery option from system settings or the evaluation");
	    }
	    emailSettings[2] = (Boolean) settings.get(EvalSettings.LOG_EMAIL_RECIPIENTS);
	    if (emailSettings[2] == null) {
	         throw new IllegalStateException("Could not get logging of email recipients from system settings or the evaluation");
	    }
	    emailSettings[3] = (Integer) settings.get(EvalSettings.EMAIL_BATCH_SIZE);
	    if (emailSettings[3] == null) {
	         throw new IllegalStateException("Could not get batch size system settings or the evaluation");
	    }
	    emailSettings[4] = (Integer) settings.get(EvalSettings.EMAIL_WAIT_INTERVAL);
	    if (emailSettings[4] == null) {
	         throw new IllegalStateException("Could not get a wait value from system settings or the evaluation");
	    }
	    emailSettings[5] = (Integer) settings.get(EvalSettings.LOG_PROGRESS_EVERY);
	    if (emailSettings[5] == null) {
	         throw new IllegalStateException("Could not get a logging interval from system settings or the evaluation");
	    }
	    return emailSettings;
   }
   
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalExternalLogic#getTaskStatusUrl()
	 */
	public String getTaskStatusUrl() {
		return serverConfigurationService.getString("taskstatus");
	}
	
	public String getTaskStatusContainer(String params) {
		if(params == null) throw new IllegalArgumentException(this + ".getTaskStatusContainer argument is null.");
		String serverUrl = getTaskStatusUrl();
		if(serverUrl != null && serverUrl.endsWith("taskstatus")) {
			serverUrl += "/" + params;
			TSSResponseApi r = taskStatus.getTSSItem(serverUrl, "text/xml");
			// 200 for a successful get.
			if(r != null && r.getStatus() == 200) {
				if(log.isDebugEnabled()) log.debug("r.getBody() " + r.getBody());
				return r.getBody();
			}
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalExternalLogic#createTaskStream(java.lang.String)
	 */
	public String newTaskStatusStream(String streamTag) {
		if(streamTag == null) throw new IllegalArgumentException(this + ".createTaskStream argument is null.");
		
		String serverUrl = getTaskStatusUrl();
		if(serverUrl != null && serverUrl.endsWith("taskstatus")) {
			// create a new stream with a recognized tag
			TSSResponseApi r = taskStatus.newTSSStream(serverUrl, streamTag);

			// 201 for a successful update.
			if(r != null && r.getStatus() == 201) {
				return r.retrieveNewContentUrl();
			}
		}
		return null;
	}

   /*
    * (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.EvalExternalLogic#addTaskStreamEntry(String, String, TaskStatusStandardValues, String)
    */
	public String newTaskStatusEntry(String streamUrl, String entryTag, TaskStatusStandardValues status,
			String payload) {
		if(streamUrl == null || status == null) {
			throw new IllegalArgumentException(this + ".addTaskStreamEntry argument(s) null.");
		}
		TSSResponseApi r = null;
		if(payload == null) {
			//r = taskStatus.newTSSEntry(streamUrl, status, entryTag);
			r = taskStatus.newTSSEntry(streamUrl, status.toString(), entryTag);
		}
		else {
			//r = taskStatus.newTSSEntry(streamUrl, status, entryTag, payload);
			r = taskStatus.newTSSEntry(streamUrl, status.toString(), entryTag, payload);
		}
		// 201 for a successful update.
		if(r != null && r.getStatus() == 201) {
			return r.retrieveNewContentUrl();
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalExternalLogic#getTaskStreamCount(java.lang.String)
	 */
	public String getTaskStreamCount(String params) {
		if(params == null) throw new IllegalArgumentException(this + ".getTaskStreamCount argument is null.");
		String serverUrl = getTaskStatusUrl() + "/" + params;
		TSSResponseApi r = taskStatus.getTSSItem(serverUrl, "text/xml");
		// 200 for a successful get.
		if(r != null && r.getStatus() == 200) {
			return r.getBody();
		}
		return null;
	}
}

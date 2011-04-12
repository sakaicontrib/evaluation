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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
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
import java.util.UUID;
import java.util.Vector;
import java.util.Map.Entry;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.cluster.api.ClusterService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.entity.AssignGroupEntityProvider;
import org.sakaiproject.evaluation.logic.entity.ConfigEntityProvider;
import org.sakaiproject.evaluation.logic.entity.EvaluationEntityProvider;
import org.sakaiproject.evaluation.logic.entity.ItemEntityProvider;
import org.sakaiproject.evaluation.logic.entity.TemplateEntityProvider;
import org.sakaiproject.evaluation.logic.entity.TemplateItemEntityProvider;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalScheduledJob;
import org.sakaiproject.evaluation.logic.model.EvalUser;
//import org.sakaiproject.evaluation.logic.scheduling.GroupMembershipSync;
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
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
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

    private static final String SAKAI_SITE_TYPE = SiteService.SITE_SUBTYPE;
    private static final String SAKAI_GROUP_TYPE = SiteService.GROUP_SUBTYPE;

    private static final String ANON_USER_ATTRIBUTE = "AnonUserAttribute";
    private static final String ANON_USER_PREFIX = "Anon_User_";

    private static final String ADMIN_USER_ID = "admin";
    private static final String SITE_TERM = "term";

    /**
     * Add presedence:bulk to mark emails as a type of bulk mail
     * This allows some email systems to deal with it correctly,
     * e.g. they won't send OOO replies / vacation messages 
     */
    private static final String EMAIL_BULK_FLAG = "Precedence: bulk";
    private List<String> emailHeaders;

    /**
     * This must match the id of the bean which implements {@link EvalScheduledInvocation}
     */
    protected final String SCHEDULER_SPRING_BEAN_ID = "org.sakaiproject.evaluation.logic.externals.EvalScheduledInvocation";

    private AuthzGroupService authzGroupService;
    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
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

    private SiteService siteService;
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private ToolManager toolManager;
    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    private UserDirectoryService userDirectoryService;
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    private ContentHostingService contentHostingService;
    public void setContentHostingService(ContentHostingService service) {
        this.contentHostingService = service;
    }

    protected ScheduledInvocationManager scheduledInvocationManager;
    public void setScheduledInvocationManager(ScheduledInvocationManager scheduledInvocationManager) {
        this.scheduledInvocationManager = scheduledInvocationManager;
    }

    protected TimeService timeService;
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }
    
    protected ClusterService clusterService;
    public void setClusterService(ClusterService clusterService) {
    	this.clusterService = clusterService;
    }
    
    protected ComponentManager componentManager;
    public void setComponentManager(ComponentManager componentManager) {
    	this.componentManager = componentManager;
    }

    public void init() {
        log.debug("init, register security perms");

        // register Sakai permissions for this tool
        registerPermissions();

        // email bulk headers - http://bugs.sakaiproject.org/jira/browse/EVALSYS-620
        emailHeaders = new Vector<String>();
        emailHeaders.add(EMAIL_BULK_FLAG);
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
                UUID userUUId = UUID.randomUUID();
                userId = ANON_USER_PREFIX + userUUId;
                session.setAttribute(ANON_USER_ATTRIBUTE, userId);
            }
        }
        return userId;
    }

    public String getAdminUserId() {
        // TODO make this pull from a property or something
        return ADMIN_USER_ID;
    }
    
    public List<EvalUser> getSakaiAdmins() {
		
    	List<String> groupsList = new ArrayList<String>(1);
		groupsList.add("/site/!admin");
    	Set<String> userIdSet = authzGroupService.getUsersIsAllowed("site.upd", groupsList);
    	
    	Map<String, EvalUser> sakaiAdminMap = this.getEvalUsersByIds(userIdSet.toArray(new String[userIdSet.size()]));
    	List<EvalUser> sakaiAdminList = new ArrayList<EvalUser>(sakaiAdminMap.values());
    	
    	return sakaiAdminList;
    	
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

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalUsers#getUserId(java.lang.String)
     */
    public String getUserId(String username) {
        String userId = null;
        try {
            userId = userDirectoryService.getUserId(username);
        } catch(UserNotDefinedException ex) {
            log.debug("Could not get userId from username: " + username);
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
        } else {
            try {
                User sakaiUser = userDirectoryService.getUser(userId);
                user = new EvalUser(userId, EvalConstants.USER_TYPE_EXTERNAL,
                        sakaiUser.getEmail(), sakaiUser.getEid(), sakaiUser.getDisplayName(), sakaiUser.getSortName(), sakaiUser.getDisplayId());
            } catch(UserNotDefinedException ex) {
                log.debug("Sakai could not get user from userId: " + userId, ex);
            }
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
                    sakaiUser.getEmail(), sakaiUser.getEid(), sakaiUser.getDisplayName(), sakaiUser.getSortName(), sakaiUser.getDisplayId());
        }
        return user;
    }



    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalUsers#getEvalUsersByIds(java.lang.String[])
     */
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
            for (Entry<String, User> entry : sakaiUsers.entrySet()) {
                String userId = entry.getKey();
                User sakaiUser = entry.getValue();
                EvalUser user = new EvalUser(sakaiUser.getId(), EvalConstants.USER_TYPE_EXTERNAL,
                        sakaiUser.getEmail(), sakaiUser.getEid(), sakaiUser.getDisplayName(), sakaiUser.getSortName(), sakaiUser.getDisplayId());
                users.put(userId, user);
            }
        }
        return users;
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
     * @see org.sakaiproject.evaluation.logic.externals.EvalExternalLogic#isUserSakaiAdmin(java.lang.String)
     */
    public boolean isUserSakaiAdmin(String userId) {
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
                // first we try to go straight to the siteService which is fastest
                if (evalGroupId.contains("/group/")) {
                    Group group = siteService.findGroup(evalGroupId);
                    c = new EvalGroup( evalGroupId, group.getTitle(), 
                            getContextType(SAKAI_GROUP_TYPE) );
                } else if (evalGroupId.startsWith("/site/")) {
                    String siteId = evalGroupId.substring(6);
                    try {
                        Site site = siteService.getSite(siteId);
                        c = new EvalGroup( evalGroupId, site.getTitle(), 
                                getContextType(SAKAI_SITE_TYPE) );
                    } catch (IdUnusedException e) {
                        c = null;
                    }
                }
                if (c == null) {
                    // next try getting from entity system
                    Object entity = entityBroker.fetchEntity(evalGroupId);
                    if (entity instanceof Site) {
                        Site site = (Site) entity;
                        c = new EvalGroup( evalGroupId, site.getTitle(), 
                                getContextType(SAKAI_SITE_TYPE) );
                    } else if (entity instanceof Group) {
                        Group group = (Group) entity;
                        c = new EvalGroup( evalGroupId, group.getTitle(), 
                                getContextType(SAKAI_GROUP_TYPE) );
                    }
                }
            } catch (Exception e) {
                // invalid site reference
                log.debug("Could not get sakai site from evalGroupId:" + evalGroupId, e);
                c = null;
            }
        }

        if (c == null) {
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
    public List<EvalGroup> getEvalGroupsForUser(String userId, String permission) {
        log.debug("userId: " + userId + ", permission: " + permission);

        return getEvalGroups(userId, permission, false, null);
    }
    
    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#getEvalGroupsForUser(java.lang.String, java.lang.String)
     */
    public List<EvalGroup> getFilteredEvalGroupsForUser(String userId, String permission, String currentSiteId) {
    	log.debug("userId: " + userId + ", permission: " + permission + ", current site: " + currentSiteId);

        return getEvalGroups(userId, permission, true, currentSiteId);
    }
    
    @SuppressWarnings("unchecked")
	private List<EvalGroup>getEvalGroups(String userId, String permission, boolean filterSites, String currentSiteId) {
    	List<EvalGroup> l = new ArrayList<EvalGroup>();

        // get the groups from Sakai
        Set<String> authzGroupIds = 
            authzGroupService.getAuthzGroupsIsAllowed(userId, permission, null);
        
        Site currentSite = null;
        if ( filterSites && currentSiteId != null ){
	        try {
				currentSite = siteService.getSite(currentSiteId);
			} catch (IdUnusedException e1) {
				// invalid site Id returned
	            throw new RuntimeException("Could not get site from siteId:" + currentSiteId);
			}
        }
        long currentSiteTerm = 0l;
        boolean isCurrentSiteTermDefined = true;
        if ( currentSite != null && currentSite.getProperties() != null ){
			try {
				currentSiteTerm = currentSite.getProperties().getLongProperty(SITE_TERM);
			} catch (EntityPropertyNotDefinedException e) {
				isCurrentSiteTermDefined = false;
			} catch (EntityPropertyTypeException e) {
				isCurrentSiteTermDefined = false;
			}
        }else{
        	isCurrentSiteTermDefined = false;
        }
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
                            if (filterSites && currentSite != null && currentSite.getType() != null){
                            	//only process sites that have the same type as the current one, if type is not stipulated simply add all sites
                            	if ( currentSite.getType().equals(site.getType()) ){
                            		//We only check terms if current site has term defined, otherwise just add this site to the list
                            		if ( isCurrentSiteTermDefined && site.getProperties() !=null){
		                                long siteTerm = 0l;
										try {
											siteTerm = site.getProperties().getLongProperty(SITE_TERM);
										} catch (EntityPropertyNotDefinedException e) {} catch (EntityPropertyTypeException e) {}
										//add this site to list only if it has the same term as the current site
		                                if ( currentSiteTerm == siteTerm ){
		                                	l.add(new EvalGroup(r.getReference(), site.getTitle(), getContextType(r.getType())));
		                                }
			                        }else{
			                        	l.add(new EvalGroup(r.getReference(), site.getTitle(), getContextType(r.getType())));
			                        }
                                }
		                    }else{
		                    	l.add(new EvalGroup(r.getReference(), site.getTitle(),  getContextType(r.getType())));
                          }
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

        if (l.isEmpty()) log.debug("Empty list of groups for user:" + userId + ", permission: " + permission);
        return l;
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
            if (isUserSakaiAdmin(userId)) {
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

    /* (non-Javadoc)
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
                    log.debug("blank or null to address in list ("+i+"): " + ArrayUtils.arrayToString(to));
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
     * @return an array of all email addresses that were sent to
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
                    log.debug("Invalid to address (" + email + "), skipping it, error:("+e+")...");
                    if (log.isDebugEnabled()) {
                    	e.printStackTrace();
                    }
                    continue;
                } else {
                    // die here since we were unable to find this user at all
                    throw new IllegalArgumentException("Invalid to address: " + email + ", cannot send emails", e);
                } 
            }
        }
        replyTo[0] = fromAddress;
        InternetAddress[] toAddresses = listAddresses.toArray(new InternetAddress[listAddresses.size()]);
        // headers are set globally and used for all emails going out (see top of this file)
        // added to ensure non-blank TO header: http://bugs.sakaiproject.org/jira/browse/EVALSYS-724
        emailService.sendMail(fromAddress, toAddresses, subject, message, 
                new InternetAddress[]{fromAddress}, replyTo, this.emailHeaders);

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



    // ENTITIES


    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#getServerUrl()
     */
    public String getServerUrl() {
        return serverConfigurationService.getPortalUrl();
    }
    
	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.ExternalEntities#getServerId()
	 */
	public String getServerId() {
		return serverConfigurationService.getServerId();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.ExternalEntities#getServers()
	 */
	public List<String> getServers() {
		List<String> servers = new ArrayList<String>();
        if(clusterService == null) {
        	servers.add(this.getServerId());
        } else {
	        List serverIds = clusterService.getServers();
	        if(serverIds == null || serverIds.isEmpty()) {
	        	servers.add(this.getServerId());
	        } else {
	        	servers.addAll((List<String>) serverIds);
	        }
        }
		return servers;
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
    public static String translatePermission(String permission) {
        if (EvalConstants.PERM_TAKE_EVALUATION.equals(permission)) {
            return EvalGroupsProvider.PERM_TAKE_EVALUATION;
        } else if (EvalConstants.PERM_BE_EVALUATED.equals(permission)) {
            return EvalGroupsProvider.PERM_BE_EVALUATED;
        } else if (EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission)) {
            return EvalGroupsProvider.PERM_ASSIGN_EVALUATION;
        } else if (EvalConstants.PERM_ASSISTANT_ROLE.equals(permission)) {
            return EvalGroupsProvider.PERM_TA_ROLE;
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

	@Override
	public String scheduleCronJob(Class jobClass, Map<String, String> dataMap) {
		
		String jobFullName = null;
		SchedulerManager scheduleManager = getBean(SchedulerManager.class);
		CronTrigger trigger = null;
		
		String triggerName = (String) dataMap.remove(EvalConstants.CRON_SCHEDULER_TRIGGER_NAME);
		String triggerGroup = (String) dataMap.remove(EvalConstants.CRON_SCHEDULER_TRIGGER_GROUP);
		String jobName = (String) dataMap.remove(EvalConstants.CRON_SCHEDULER_JOB_NAME);
		String jobGroup = (String) dataMap.remove(EvalConstants.CRON_SCHEDULER_JOB_GROUP);
		
		String cronExpression = (String) dataMap.remove(EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION);
		
		try {
			trigger = new CronTrigger(triggerName, triggerGroup, jobName, jobGroup, cronExpression);
			if(log.isDebugEnabled()) {
				log.debug("Created trigger: " + trigger.getCronExpression());
			}
		} catch(ParseException e) {
			log.warn("SchedulerException in scheduleCronJob()", e);
		}
		
		if(trigger != null) {
			// create job
			JobDataMap jobDataMap = new JobDataMap();
			if(dataMap != null) {
				for(String key : dataMap.keySet()) {
					jobDataMap.put(key, dataMap.get(key));
				}
			}
			
			JobDetail jobDetail = new JobDetail();
			jobDetail.setName(jobName);
			jobDetail.setGroup(jobGroup);		
			jobDetail.setJobDataMap(jobDataMap);
			jobDetail.setJobClass(jobClass);
			Scheduler scheduler = scheduleManager.getScheduler();
			if(scheduler == null) {
				log.warn("Unable to access scheduler", new Throwable());
			} else {
				try {
					Date date = scheduler.scheduleJob(jobDetail, trigger);
					if(log.isDebugEnabled()) {
						log.debug("Scheduled cron job: " + trigger.getCronExpression() + " " + date);
					}
					jobFullName =  jobDetail.getFullName();
				} catch(SchedulerException e) {
					log.warn("SchedulerException in scheduleCronJob()", e);
				}
			}
		}
		return jobFullName;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.ExternalScheduler#scheduleCronJob(java.lang.String, java.util.Map)
	 */
	public String scheduleCronJob(String jobClassBeanId,
			Map<String, String> dataMap) {
		String fullJobName = null;
		Object jobClass = this.componentManager.get(jobClassBeanId);
		if(jobClass == null) {
			throw new IllegalArgumentException(jobClassBeanId + " could not be found");
		} else {
			fullJobName = this.scheduleCronJob(jobClass.getClass(), dataMap);
		}
		return fullJobName;
	}

	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.ExternalScheduler#getCronJobs(java.lang.String)
	 */
	public Map<String,Map<String, String>> getCronJobs(String jobGroup) {
		Map<String,Map<String, String>> cronJobs = new HashMap<String,Map<String, String>>();
		SchedulerManager schedulerManager = getBean(SchedulerManager.class);
		Scheduler scheduler = schedulerManager.getScheduler();
		if(scheduler == null) {
			log.warn("Unable to access scheduler", new Throwable());
		} else {
			try {
				String[] jobNames = scheduler.getJobNames(jobGroup);
				for(String jobName : jobNames) {
					try {
						JobDetail job = scheduler.getJobDetail(jobName, jobGroup);
						JobDataMap jobDataMap = job.getJobDataMap();
						Trigger[] triggers = scheduler.getTriggersOfJob(jobName, jobGroup);
						for(Trigger trigger : triggers) {
							Map<String, String> map = new HashMap<String, String>();
							map.put(EvalConstants.CRON_SCHEDULER_JOB_NAME, jobName);
							map.put(EvalConstants.CRON_SCHEDULER_JOB_GROUP, jobGroup);
							
							map.put(EvalConstants.CRON_SCHEDULER_TRIGGER_NAME, trigger.getName());
							map.put(EvalConstants.CRON_SCHEDULER_TRIGGER_GROUP, trigger.getGroup());
							if(trigger instanceof CronTrigger) {
								map.put(EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION, ((CronTrigger) trigger).getCronExpression());
							}
							
							for(String propName : (Set<String>) jobDataMap.keySet()) {
								if(jobDataMap.containsKey(propName)) {
									map.put(propName, jobDataMap.getString(propName));
								}
							}
							
							cronJobs.put(trigger.getFullName(), map);
						}
					} catch(SchedulerException e) {
						log.warn("SchedulerException processing one trigger in getCronJobs", e);					
					}
				}
			} catch(SchedulerException e) {
				log.warn("SchedulerException processing one job in getCronJobs", e);
			}
		}
		return cronJobs;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.ExternalScheduler#deleteCronJob(java.lang.String, java.lang.String)
	 */
	public boolean deleteCronJob(String jobName, String jobGroup) {

		boolean success = false;
		SchedulerManager scheduleManager = getBean(SchedulerManager.class);
		Scheduler scheduler = scheduleManager.getScheduler();
		if(scheduler == null) {
			log.warn("Unable to access scheduler", new Throwable());
		} else {
			try {
				success = scheduler.deleteJob(jobName, jobGroup);
			} catch(SchedulerException e) {
				log.warn("SchedulerException in scheduleCronJob()", e);
			}
		}
		return success;
	}

    public String getContentCollectionId(String siteId) {
        String ret = contentHostingService.getSiteCollection(siteId);
        return ret;
    }
    
	public boolean isEvalGroupPublished(String evalGroupId) {
		//unless the site is specifically flagged as unpublished, assume it is published.
		boolean isEvalGroupPublished = true; 
		if( evalGroupId != null){
			try{
				Site site = siteService.getSite(evalGroupId.replaceAll("/site/", ""));
				isEvalGroupPublished = site.isPublished();
			}catch(IdUnusedException e){
				log.debug(e);
			} 
		}
		return isEvalGroupPublished;
	}

}

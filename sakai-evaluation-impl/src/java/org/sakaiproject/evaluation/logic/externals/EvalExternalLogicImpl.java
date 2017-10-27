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
package org.sakaiproject.evaluation.logic.externals;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.cluster.api.ClusterService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.coursemanagement.impl.provider.SectionRoleResolver;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.search.Order;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
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
import org.sakaiproject.evaluation.logic.model.HierarchyNodeRule;
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
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.evaluation.dao.EvalHierarchyRuleSupport;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;


/**
 * This class handles the Sakai based implementation of the external logic interface<br/>
 * This is sort of the provider for the evaluation system though it should be broken up
 * if it is going to be used like that,
 * This is a BOTTOM level service and should depend on no other eval services (only those in Sakai)
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalExternalLogicImpl implements EvalExternalLogic {

    private static final Log LOG = LogFactory.getLog(EvalExternalLogicImpl.class);

    private static final String SAKAI_SITE_TYPE = SiteService.SITE_SUBTYPE;
    private static final String SAKAI_GROUP_TYPE = SiteService.GROUP_SUBTYPE;
    private static final String SAKAI_SITE_TYPE_FULL = SiteService.APPLICATION_ID;

    private static final String SAKAI_SECTION_TYPE = "section";

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

    protected ClusterService clusterService;
    public void setClusterService(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    protected CourseManagementService courseManagementService;
    public void setCourseManagementService( CourseManagementService courseManagementService ) {
        this.courseManagementService = courseManagementService;
    }
    
    protected SectionRoleResolver sectionRoleResolver;
    public void setSectionRoleResolver( SectionRoleResolver sectionRoleResolver ) {
        this.sectionRoleResolver = sectionRoleResolver;
    }

    private EvalHierarchyRuleSupport evalHierarchyRuleLogic;
    public void setEvalHierarchyRuleLogic( EvalHierarchyRuleSupport evalHierarchyRuleLogic ) {
        this.evalHierarchyRuleLogic = evalHierarchyRuleLogic;
    }

    public void init() {
        LOG.debug("init, register security perms");

        // register Sakai permissions for this tool
        registerPermissions();

        // email bulk headers - http://bugs.sakaiproject.org/jira/browse/EVALSYS-620
        emailHeaders = new ArrayList<>();
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

        List<String> groupsList = new ArrayList<>(1);
        groupsList.add("/site/!admin");
        Set<String> userIdSet = authzGroupService.getUsersIsAllowed("site.upd", groupsList);

        Map<String, EvalUser> sakaiAdminMap = this.getEvalUsersByIds(userIdSet.toArray(new String[userIdSet.size()]));
        List<EvalUser> sakaiAdminList = new ArrayList<>(sakaiAdminMap.values());

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
            LOG.debug("Could not get userId from username: " + username);
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
                LOG.warn("Sakai could not get username from userId: " + userId, ex);
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
                LOG.debug("Sakai could not get user from userId: " + userId, ex);
            }
        }
        return user;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalUsers#getEvalUserByEmail(java.lang.String)
     */
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
        Map<String, EvalUser> users = new HashMap<>();
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
        Map<String, User> sakaiUsers = new HashMap<>(); // fill this with users
        for( String userId : userIds )
        {
            User user = null;
            try
            {
                user = userDirectoryService.getUser( userId );
            }
            catch( UserNotDefinedException e )
            {
                LOG.debug( "Cannot find user object by id:" + userId );
                try
                {
                    user = userDirectoryService.getUserByEid( userId );
                }
                catch( UserNotDefinedException e1 )
                {
                    LOG.debug( "Cannot find user object by eid:" + userId );
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
        LOG.debug("Checking is eval super admin for: " + userId);
        return securityService.isSuperUser(userId);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalUsers#getUserLocale(java.lang.String)
     */
    public Locale getUserLocale(String userId) {
        LOG.debug("userId: " + userId);
        //log.warn("can only get the locale for the current user right now...");
        // TODO - this sucks because there is no way to get the locale for anything but the
        // current user.... terrible -AZ
        return new ResourceLoader().getLocale();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#makeEvalGroupObjectsForSectionAwareness(java.lang.String)
     */
    public List<EvalGroup> makeEvalGroupObjectsForSectionAwareness( String evalGroupId )
    {
        if( evalGroupId == null )
        {
            throw new IllegalArgumentException( "evalGroupId cannot be null" );
        }

        if( !evalGroupId.startsWith( EvalConstants.GROUP_ID_SITE_PREFIX ) )
        {
            throw new IllegalArgumentException( "cannot determine sections of groupId='" + evalGroupId + "' (must be a site)" );
        }

        List<EvalGroup> groups = new ArrayList<>();
        try
        {
            String realmID = siteService.siteReference( evalGroupId.replace( EvalConstants.GROUP_ID_SITE_PREFIX, "" ) );
            Set<String> sectionIds = authzGroupService.getProviderIds( realmID );
            for( String sectionID : sectionIds )
            {
                try
                {
                    Section section = courseManagementService.getSection( sectionID );
                    EvalGroup group = new EvalGroup( evalGroupId + EvalConstants.GROUP_ID_SECTION_PREFIX + sectionID, 
                            section.getTitle(), getContextType( SAKAI_SECTION_TYPE ) );
                    groups.add( group );
                }
                catch( IdNotFoundException ex ) { LOG.debug( "Could not find section with ID = " + sectionID, ex ); }
            }
        }
        catch( Exception ex ) { LOG.debug( ex.getMessage() ); }


        return groups;
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
                if (evalGroupId.contains(EvalConstants.GROUP_ID_GROUP_PREFIX)) {
                    Group group = siteService.findGroup(evalGroupId);
                    c = new EvalGroup( evalGroupId, group.getTitle(), 
                            getContextType(SAKAI_GROUP_TYPE) );
                }
                
                // If the group id starts with '/site/' and contains '/section/', then we need to make a section type EvalGroup object
                else if( evalGroupId.startsWith( EvalConstants.GROUP_ID_SITE_PREFIX ) && evalGroupId.contains( EvalConstants.GROUP_ID_SECTION_PREFIX ) )
                {
                    try
                    {
                        // Get the section IDs for the parent site
                        String siteID = evalGroupId.substring( evalGroupId.indexOf( EvalConstants.GROUP_ID_SITE_PREFIX ) + 6, evalGroupId.indexOf( EvalConstants.GROUP_ID_SECTION_PREFIX ) );
                        String sectionID = evalGroupId.substring( evalGroupId.indexOf( EvalConstants.GROUP_ID_SECTION_PREFIX ) + 9 );
                        String realmID = siteService.siteReference( siteID );
                        Set<String> sectionIds = authzGroupService.getProviderIds( realmID );

                        // Loop through the section IDs, if one matches the section ID contained in the evalGroupID, create an EvalGroup object for it
                        for( String secID : sectionIds )
                        {
                            if( secID.equalsIgnoreCase( sectionID ) )
                            {
                                c = new EvalGroup( evalGroupId, courseManagementService.getSection( secID ).getTitle(), getContextType( SAKAI_SECTION_TYPE ) );
                            }
                        }
                    }
                    catch( Exception ex ) { c = null; }
                }
                else if (evalGroupId.startsWith(EvalConstants.GROUP_ID_SITE_PREFIX)) {
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
                LOG.debug("Could not get sakai site from evalGroupId:" + evalGroupId, e);
                c = null;
            }
        }

        if (c == null) {
            LOG.debug("Could not get group from evalGroupId:" + evalGroupId);
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
        String location;
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
    public int countEvalGroupsForUser(String userId, String permission) {
        LOG.debug("userId: " + userId + ", permission: " + permission);

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
        LOG.debug("userId: " + userId + ", permission: " + permission);

        return getEvalGroups(userId, permission, false, null);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#getEvalGroupsForUser(java.lang.String, java.lang.String)
     */
    public List<EvalGroup> getFilteredEvalGroupsForUser(String userId, String permission, String currentSiteId) {
        LOG.debug("userId: " + userId + ", permission: " + permission + ", current site: " + currentSiteId);

        return getEvalGroups(userId, permission, true, currentSiteId);
    }

    private List<EvalGroup>getEvalGroups(String userId, String permission, boolean filterSites, String currentSiteId) {
        List<EvalGroup> l = new ArrayList<>();

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
            } catch (EntityPropertyNotDefinedException | EntityPropertyTypeException e) {
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
                            boolean addSite = false;
                            if (filterSites && currentSite != null && currentSite.getType() != null) {
                                // only process sites that have the same type as the current one, if
                                // type is not stipulated simply add all sites
                                if (currentSite.getType().equals(site.getType())) {
                                    // We only check terms if current site has term defined,
                                    // otherwise just add this site to the list
                                    if (isCurrentSiteTermDefined && site.getProperties() != null) {
                                        long siteTerm = 0l;
                                        try {
                                            siteTerm = site.getProperties().getLongProperty(SITE_TERM);
                                        } catch (EntityPropertyNotDefinedException | EntityPropertyTypeException e) {
                                            // IGNORE
                                        }
                                        // add this site to list only if it has the same term as the current site
                                        if (currentSiteTerm == siteTerm) {
                                            addSite = true;
                                        }
                                    } else {
                                        addSite = true;
                                    }
                                }
                            } else {
                                addSite = true;
                            }
                            if (addSite) {
                                l.add(new EvalGroup(r.getReference(), site.getTitle(), getContextType(r.getType())));
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
                            LOG.info("Could not get Sakai group from group id:" + groupId);
                        }
                        else {
                            l.add(new EvalGroup(r.getReference(), group.getTitle(), getContextType(r.getType())));
                        }
                    }
                }
            }
        }

        if (l.isEmpty()) {
            LOG.debug("Empty list of groups for user:" + userId + ", permission: " + permission);
        }
        return l;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#countUserIdsForEvalGroup(java.lang.String, java.lang.String, java.lang.Boolean)
     */
    public int countUserIdsForEvalGroup( String evalGroupID, String permission, Boolean sectionAware )
    {
        return getUserIdsForEvalGroup( evalGroupID, permission, sectionAware ).size();
    }

    /**
     * Utility class to parse out section, site and group IDs into their own separate variables.
     */
    private class ParsedEvalGroupID
    {
        // Member variables
        private String siteID = "";
        private String sectionID = "";
        private String groupID = "";

        /**
         * Default constructor
         * @param evalGroupID the group ID to be parsed
         */
        public ParsedEvalGroupID( String evalGroupID )
        {
            // Evaluation group IDs will always be in one of the following formats:
            // "/site/<siteID>"
            // "/site/<siteID>/section/<sectionID>"
            // "/site/<siteID>/group/<groupID>"
            evalGroupID = evalGroupID.replace( EvalConstants.GROUP_ID_SITE_PREFIX, "" );

            String[] pieces = evalGroupID.split( EvalConstants.GROUP_ID_SECTION_PREFIX );
            if( pieces.length == 2 )
            {
                siteID = pieces[0];
                sectionID = pieces[1];
            }
            else
            {
                pieces = evalGroupID.split( EvalConstants.GROUP_ID_GROUP_PREFIX );
                siteID = pieces[0];
                if( pieces.length == 2 )
                {
                    groupID = pieces[1];
                }
            }
        }

        // Getters
        public boolean  hasSection()        { return !StringUtils.isBlank( sectionID ); }
        public boolean  hasGroup()          { return !StringUtils.isBlank( groupID ); }
        public String   getSiteID()         { return siteID; }
        public String   getSectionID()      { return sectionID; }
        public String   getGroupID()        { return groupID; }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#getUserIdsForEvalGroup(java.lang.String, java.lang.String, java.lang.Boolean)
     */
    public Set<String> getUserIdsForEvalGroup( String evalGroupID, String permission, Boolean sectionAware )
    {
        // Parse out the section and site IDs from the group ID
        ParsedEvalGroupID groupID = new ParsedEvalGroupID( evalGroupID );

        // If it's not section aware...
        Set<String> userIDs = new HashSet<>();
        if( BooleanUtils.isFalse(sectionAware) )
        {
            // Get the list normally
            List<String> azGroups = new ArrayList<>();
            String azGroup = EvalConstants.GROUP_ID_SITE_PREFIX + groupID.getSiteID();
            if( groupID.hasGroup() )
            {
                azGroup += EvalConstants.GROUP_ID_GROUP_PREFIX + groupID.getGroupID();
            }
            azGroups.add( azGroup );
            userIDs.addAll( authzGroupService.getUsersIsAllowed( permission, azGroups ) );
            if( userIDs.contains( ADMIN_USER_ID ) )
            {
                userIDs.remove( ADMIN_USER_ID );
            }
        }

        // Otherwise, it's section aware but we only need to run the following if the sectin prefix is present in the evalGroupID
        else if( groupID.hasSection() )
        {
            try
            {
                // Get all roles for the site
                Site site = siteService.getSite( groupID.getSiteID() );
                Set<Role> siteRoles = site.getRoles();
                List<String> siteRolesWithPerm = new ArrayList<>( siteRoles.size() );

                // Determine which roles have the given permission
                for( Role role : siteRoles )
                {
                    if( role.getAllowedFunctions().contains( permission ) )
                    {
                        siteRolesWithPerm.add( role.getId() );
                    }
                }

                // Get the section and the user role map for the section
                Section section = courseManagementService.getSection( groupID.getSectionID() );
                Map<String, String> userRoleMap = sectionRoleResolver.getUserRoles( courseManagementService, section );

                // Loop through the user role map; if the user's section role is in the list of site roles with the permission, add the user to the list
                for( Entry<String, String> userRoleEntry : userRoleMap.entrySet() )
                {
                    if( siteRolesWithPerm.contains( userRoleEntry.getValue() ) )
                    {
                        String userEID;
                        try { userEID = userDirectoryService.getUserId( userRoleEntry.getKey() ); }
                        catch( UserNotDefinedException e )
                        { 
                            LOG.info( "Cant find userID for user = " + userRoleEntry.getKey(), e );
                            continue;
                        }
                        userIDs.add( userEID );
                    }
                }
            }
            catch( IdUnusedException ex ) { LOG.warn( "Could not find site with ID = " + groupID.getSiteID(), ex ); }
            catch( IdNotFoundException ex ) { LOG.warn( "Could not find section with ID = " + groupID.getSectionID(), ex ); }
        }

        // Return the user IDs
        return userIDs;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#isUserAllowedInEvalGroup(java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean isUserAllowedInEvalGroup(String userId, String permission, String evalGroupId) {

        /* NOTE: false checks end up being really costly and should probably be cached
         */

        if (evalGroupId == null) {
            // special check for the admin user
            return isUserSakaiAdmin(userId);
        }

        // try checking Sakai
        String reference = evalGroupId;
        return securityService.unlock(userId, permission, reference);
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

        List<String> toEmails = new ArrayList<>();
        for (int i = 0; i < to.length; i++) {
            String email = to[i];
            if (email == null || email.equals("")) {
                if (deferExceptions) {
                    exceptionTracker += "blank or null to address in list ("+i+") :: ";
                    LOG.debug("blank or null to address in list ("+i+"): " + ArrayUtils.arrayToString(to));
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
        List<InternetAddress> listAddresses = new ArrayList<>();
        for (int i = 0; i < toEmails.size(); i++) {
            String email = toEmails.get(i);
            try {
                InternetAddress toAddress = new InternetAddress(email);
                listAddresses.add(toAddress);
            } catch (AddressException e) {
                if (deferExceptions) {
                    exceptionTracker += e.getMessage() + " :: ";
                    LOG.debug("Invalid to address (" + email + "), skipping it, error:("+e+")...");
                    if (LOG.isDebugEnabled()) {
                        LOG.warn( e );
                    }
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<String> getServers() {
        List<String> servers = new ArrayList<>();
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
            LOG.debug("Entity event: " + eventName + " for " + ref);
            entityBroker.fireEvent(eventName, ref);
        }
    }

    public void registerEntityEvent(String eventName, Class<? extends Serializable> entityClass, String entityId) {
        String ref = getEntityReference(entityClass, entityId);
        if (ref != null) {
            LOG.debug("Entity event: " + eventName + " for " + ref);
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
        String id;
        try {
            Class<? extends Serializable> elementClass = entity.getClass();
            Method getIdMethod = elementClass.getMethod("getId", new Class[] {});
            Long realId = (Long) getIdMethod.invoke(entity, (Object[]) null);
            id = realId.toString();
            return getEntityReference(elementClass, id);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOG.warn("Failed to get id from entity object", e);
            return null;
        }
    }

    protected String getEntityReference(Class<? extends Serializable> entityClass, String entityId) {
        String prefix;
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
        functionManager.registerFunction(EvalConstants.PERM_VIEW_RESPONDERS);
        functionManager.registerFunction(EvalConstants.PERM_ADMIN_READONLY);
    }

    /**
     * Returns the appropriate internal site type identifier based on the given string
     * 
     * @param sakaiType a type identifier used in Sakai (should be a constant from Sakai)
     * @return a CONTEXT_TYPE constant from {@link EvalConstants}
     */
    protected String getContextType(String sakaiType) {
        if (SAKAI_SITE_TYPE.equals(sakaiType) || SAKAI_SITE_TYPE_FULL.equals(sakaiType)) {
            return EvalConstants.GROUP_TYPE_SITE;
        } else if (SAKAI_GROUP_TYPE.equals(sakaiType)) {
            return EvalConstants.GROUP_TYPE_GROUP;
        }
        else if( sakaiType.equals( SAKAI_SECTION_TYPE ) )
        {
            return EvalConstants.GROUP_TYPE_SECTION;
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
        } else if (EvalConstants.PERM_VIEW_RESPONDERS.equals(permission)) {
            return EvalGroupsProvider.PERM_VIEW_RESPONDERS;
        } else if (EvalConstants.PERM_ADMIN_READONLY.equals(permission)) {
            return EvalGroupsProvider.PERM_ADMIN_READONLY;
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
                    boolean bool = ((Boolean) defaultValue);
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
        	if (StringUtils.isNotBlank(abspath) && !"null".equals(abspath)) {
        		ContentResource contentResource = contentHostingService.getResource(abspath);
        		return contentResource.getContent();
        	}
        } catch (PermissionException | IdUnusedException | TypeException | ServerOverloadException e) {
            if (LOG.isDebugEnabled()) {
            	LOG.debug("Cannot byte array for Content Hosting File " + abspath,e);
            }
            else {
            	LOG.info("Cannot byte array for Content Hosting File " + abspath);
            }
            return null;
        }
        return null;
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

	public List<String> searchForEvalGroupIds(String searchString, String order, int startResult, int maxResults) {
		//for now support sakai sites only TODO:// support hierarchy and adhoc groups. Ordering?
		List<String> sakaiSites = new ArrayList<String>(); //keep site ref 
		List<Site> searchResults = siteService.getSites(SiteService.SelectionType.ANY, null, searchString, null, SiteService.SortType.TITLE_ASC, new PagingPosition(1, maxResults));
		for ( Object rawSite : searchResults){
			Site site = (Site) rawSite;
			sakaiSites.add(site.getReference());
		}
		return sakaiSites;
	}

    // JOBS related pass-through methods

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalScheduler#createScheduledJob(java.util.Date, java.lang.String)
     */
    public String createScheduledJob(Date executionDate, Long evaluationId, String jobType) {
        String jobKey = EvalScheduledJob.encodeContextId(evaluationId, jobType);
        return scheduledInvocationManager.createDelayedInvocation(
                executionDate.toInstant(), 
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

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalScheduler#scheduleCronJob(java.lang.Class, java.util.Map)
     */
    @Override
    public String scheduleCronJob(Class<? extends Job> jobClass, Map<String, String> dataMap) {

        String jobFullName = null;
        SchedulerManager scheduleManager = getBean(SchedulerManager.class);
        CronTrigger trigger;

        String triggerName = (String) dataMap.remove(EvalConstants.CRON_SCHEDULER_TRIGGER_NAME);
        String triggerGroup = (String) dataMap.remove(EvalConstants.CRON_SCHEDULER_TRIGGER_GROUP);
        String jobName = (String) dataMap.remove(EvalConstants.CRON_SCHEDULER_JOB_NAME);
        String jobGroup = (String) dataMap.remove(EvalConstants.CRON_SCHEDULER_JOB_GROUP);

        String cronExpression = (String) dataMap.remove(EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION);

        trigger = TriggerBuilder.newTrigger()
				.withIdentity(triggerName, triggerGroup)
				.forJob(jobName, jobGroup)
				.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
				.build();
		if(LOG.isDebugEnabled()) {
		    LOG.debug("Created trigger: " + trigger.getCronExpression());
		}

        if(trigger != null) {
            // create job
            JobDataMap jobDataMap = new JobDataMap();
            for(String key : dataMap.keySet()) {
                jobDataMap.put(key, dataMap.get(key));
            }

            JobDetail jobDetail = JobBuilder.newJob(jobClass)
            		.withIdentity(jobName, jobGroup)
            		.usingJobData(jobDataMap)
            		.build();
            Scheduler scheduler = scheduleManager.getScheduler();
            if(scheduler == null) {
                LOG.warn("Unable to access scheduler", new Throwable());
            } else {
                try {
                    Date date = scheduler.scheduleJob(jobDetail, trigger);
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("Scheduled cron job: " + trigger.getCronExpression() + " " + date);
                    }
                    jobFullName =  jobDetail.getKey().toString();
                } catch(SchedulerException e) {
                    LOG.warn("SchedulerException in scheduleCronJob()", e);
                }
            }
        }
        return jobFullName;
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalScheduler#scheduleCronJob(java.lang.String, java.util.Map)
     */
    @Override
    public String scheduleCronJob(String jobClassBeanId, Map<String, String> dataMap) {
        String fullJobName = null;
        Job jobClass = (Job) ComponentManager.get(jobClassBeanId);
        if (jobClass == null) {
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
    @SuppressWarnings("unchecked")
    public Map<String,Map<String, String>> getCronJobs(String jobGroup) {
        Map<String,Map<String, String>> cronJobs = new HashMap<>();
        SchedulerManager schedulerManager = getBean(SchedulerManager.class);
        Scheduler scheduler = schedulerManager.getScheduler();
        if (scheduler == null) {
            LOG.warn("Unable to access scheduler", new Throwable());
        } else {
            try {
                Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroup));
                for(JobKey jobKey : jobKeys) {
                    try {
                        JobDetail job = scheduler.getJobDetail(jobKey);
                        JobDataMap jobDataMap = job.getJobDataMap();
                        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                        for(Trigger trigger : triggers) {
                        	TriggerKey triggerKey = trigger.getKey();
                            Map<String, String> map = new HashMap<>();
                            map.put(EvalConstants.CRON_SCHEDULER_JOB_NAME, jobKey.getName());
                            map.put(EvalConstants.CRON_SCHEDULER_JOB_GROUP, jobKey.getGroup());

                            map.put(EvalConstants.CRON_SCHEDULER_TRIGGER_NAME, triggerKey.getName());
                            map.put(EvalConstants.CRON_SCHEDULER_TRIGGER_GROUP, triggerKey.getGroup());
                            if(trigger instanceof CronTrigger) {
                                map.put(EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION, ((CronTrigger) trigger).getCronExpression());
                            }

                            for(String propName : (Set<String>) jobDataMap.keySet()) {
                                if(jobDataMap.containsKey(propName)) {
                                    map.put(propName, jobDataMap.getString(propName));
                                }
                            }

                            cronJobs.put(triggerKey.toString(), map);
                        }
                    } catch(SchedulerException e) {
                        LOG.warn("SchedulerException processing one trigger in getCronJobs", e);					
                    }
                }
            } catch(SchedulerException e) {
                LOG.warn("SchedulerException processing one job in getCronJobs", e);
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
            LOG.warn("Unable to access scheduler", new Throwable());
        } else {
            try {
                success = scheduler.deleteJob(JobKey.jobKey(jobName, jobGroup));
            } catch(SchedulerException e) {
                LOG.warn("SchedulerException in scheduleCronJob()", e);
            }
        }
        return success;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalContent#getContentCollectionId(java.lang.String)
     */
    public String getContentCollectionId(String siteId) {
        String ret = contentHostingService.getSiteCollection(siteId);
        return ret;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#isEvalGroupPublished(java.lang.String)
     */
    public boolean isEvalGroupPublished(String evalGroupId) {
        //unless the site is specifically flagged as unpublished, assume it is published.
        boolean isEvalGroupPublished = true; 
        if( evalGroupId != null){
            try{
                Site site = siteService.getSite(evalGroupId.replaceAll(EvalConstants.GROUP_ID_SITE_PREFIX, ""));
                isEvalGroupPublished = site.isPublished();
            }catch(IdUnusedException e){
                LOG.debug(e);
            } 
        }
        return isEvalGroupPublished;
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalUsers#getMyWorkspaceDashboard(java.lang.String)
     */
    public String getMyWorkspaceDashboard(String userId) {
        String url = null;
        try {
            String toolPage = null;
            //userId is Sakai id
            if(userId == null || userId.length() == 0) {
                LOG.error("getMyWorkspaceUrl(String userId) userId is null or empty.");
            } else {
                String myWorkspaceId = siteService.getUserSiteId(userId);
                if(myWorkspaceId != null && ! myWorkspaceId.trim().equals("")) {
                    Site myWorkspace = siteService.getSite(myWorkspaceId);
                    if(myWorkspace != null) {
                        // find page with eval tool
                        List<SitePage> pages = myWorkspace.getPages();
                        for( SitePage page : pages )
                        {
                            List<ToolConfiguration> tools = page.getTools();
                            for( ToolConfiguration tc : tools )
                            {
                                if (tc.getToolId().equals("sakai.rsf.evaluation")) {
                                    toolPage = page.getId();
                                    break;
                                }
                            }
                        }
                    }
                }
                if(toolPage != null && ! toolPage.trim().equals("")) {
                    // e.g., https://testctools.ds.itd.umich.edu/portal/site/~37d8035e-54b3-425c-bcb5-961e881d2afe/page/866dd4e6-0323-43a1-807c-9522bb3167b7
                    url = getServerUrl() + EvalConstants.GROUP_ID_SITE_PREFIX + myWorkspaceId + "/page/" + toolPage;
                }
            }
        } catch (Exception e) {
            LOG.warn("getMyWorkspaceUrl(String userId) '" + userId + "' " + e);
        }
        if(url == null) {
            url = getServerUrl();
        }
        return url;
    }

    public void setSessionTimeout(int seconds) {
        Session session = sessionManager.getCurrentSession();
		
        if (session !=null) {
            session.setMaxInactiveInterval(seconds);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalSecurity#isUserReadonlyAdmin(java.lang.String)
     */
    public boolean isUserReadonlyAdmin(String userId) {
    	return securityService.unlock(userId, EvalConstants.PERM_ADMIN_READONLY, this.getCurrentEvalGroup());
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#isRuleAlreadyAssignedToNode(java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    public boolean isRuleAlreadyAssignedToNode( String ruleText, String qualifierSelection, String optionSelection, Long nodeID )
    {
        return evalHierarchyRuleLogic.isRuleAlreadyAssignedToNode( ruleText, qualifierSelection, optionSelection, nodeID );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#assignNodeRule(java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    public void assignNodeRule( String ruleText, String qualifier, String option, Long nodeID )
    {
        evalHierarchyRuleLogic.assignNodeRule( ruleText, qualifier, option, nodeID );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#removeNodeRule(java.lang.Long)
     */
    public void removeNodeRule( Long ruleID )
    {
        evalHierarchyRuleLogic.removeNodeRule( ruleID );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#removeAllRulesForNode(java.lang.Long)
     */
    public void removeAllRulesForNode( Long nodeID )
    {
        evalHierarchyRuleLogic.removeAllRulesForNode( nodeID );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#updateNodeRule(java.lang.Long, java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    public void updateNodeRule( Long ruleID, String ruleText, String qualifier, String option, Long nodeID )
    {
        evalHierarchyRuleLogic.updateNodeRule( ruleID, ruleText, qualifier, option, nodeID );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#getRulesByNodeID(java.lang.Long)
     */
    public List<HierarchyNodeRule> getRulesByNodeID( Long nodeID )
    {
        return evalHierarchyRuleLogic.getRulesByNodeID( nodeID );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#getRuleByID(java.lang.Long)
     */
    public HierarchyNodeRule getRuleByID( Long ruleID )
    {
        return evalHierarchyRuleLogic.getRuleByID( ruleID );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#getAllRules()
     */
    public List<HierarchyNodeRule> getAllRules()
    {
        return evalHierarchyRuleLogic.getAllRules();
    }

}

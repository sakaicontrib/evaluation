/**
 * Copyright 2012 Unicon (R) Licensed under the
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
package org.sakaiproject.evaluation.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.providers.EvalGroupsProvider;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;


/**
 * Groups Provider implementation for the UCB evals system courses and memberships
 * 
 * @author Aaron Zeckoski (azeckoski @ vt.edu)
 */
public class UCBEvalGroupsProvider implements EvalGroupsProvider, ApplicationContextAware {

    private static Log log = LogFactory.getLog(UCBEvalGroupsProvider.class);

    ApplicationContext applicationContext;
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private UCBEvalGroupsProviderDao dao;
    public void setDao(UCBEvalGroupsProviderDao dao) {
        this.dao = dao;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic common) {
        this.commonLogic = common;
    }

    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private ExternalHierarchyLogic hierarchyLogic;
    public void setHierarchyLogic(ExternalHierarchyLogic hierarchyLogic) {
        this.hierarchyLogic = hierarchyLogic;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    private EvalEmailsLogic emails;
    public void setEmails(EvalEmailsLogic emails) {
        this.emails = emails;
    }

    private EvalJobLogic evalJobLogic;
    public void setEvalJobLogic(EvalJobLogic evalJobLogic) {
        this.evalJobLogic = evalJobLogic;
    }

    private EvalBeanUtils evalBeanUtils;
    public void setEvalBeanUtils(EvalBeanUtils evalBeanUtils) {
       this.evalBeanUtils = evalBeanUtils;
    }

    public void init() {
        if (log.isDebugEnabled()) log.debug("EvalGroupsProvider.init()");
        try {
            // AZ - now we need to do some serious spring gymnastics to get our service into the main Sakai AC
            // get the main sakai AC (it will be the parent of our AC)
            ApplicationContext sakaiAC = applicationContext.getParent();
            if (sakaiAC != null && sakaiAC instanceof ConfigurableApplicationContext) {
                // only ConfigurableApplicationContext - or higher - can register singletons
                Object currentGP = ComponentManager.get(UCBEvalGroupsProvider.class.getName());
                // check if something is already registered
                if (currentGP != null) {
                    log.info("Found existing "+UCBEvalGroupsProvider.class.getName()+" in the ComponentManager: "+currentGP);
                    // attempt to unregister the existing bean (otherwise the register call will fail)
                    try {
                        // only DefaultListableBeanFactory - or higher - can unregister singletons
                        DefaultListableBeanFactory dlbf = (DefaultListableBeanFactory) sakaiAC.getAutowireCapableBeanFactory();
                        dlbf.destroySingleton(UCBEvalGroupsProvider.class.getName());
                        log.info("Removed existing "+UCBEvalGroupsProvider.class.getName()+" from the ComponentManager");
                    } catch (Exception e) {
                        log.warn("FAILED attempted removal of EvalGroupsProvider bean: "+e);
                    }
                }
                // register this EP with the sakai AC
                ((ConfigurableApplicationContext)sakaiAC).getBeanFactory().registerSingleton(UCBEvalGroupsProvider.class.getName(), this);
            }
            // now verify if we are good to go
            if (ComponentManager.get(UCBEvalGroupsProvider.class.getName()) != null) {
                log.info("Found "+UCBEvalGroupsProvider.class.getName()+" in the ComponentManager");
            } else {
                log.warn("FAILED to insert and lookup "+UCBEvalGroupsProvider.class.getName()+" in the Sakai ComponentManager, groups resolution and lookups will fail to work");
            }

            // FINALLY register this groups provider with the evals code
            commonLogic.registerEvalGroupsProvider(this);
        } catch (Exception ex) {
            log.warn("EvalGroupsProvider.init(): "+ex, ex);
        }

        log.info("Found "+dao.getCoursesCount()+" courses with "+dao.getMembersCount()+" members ("+dao.getInstructorsCount()+" instructors) and "+dao.getCrosslistCount()+" crosslisting records");

        // load up the data into a faster cache
        
    }

    public void destroy() {
        if (log.isDebugEnabled()) log.debug("EvalGroupsProvider.shutdown()");
        // purge the caches
        if (groupsById != null) {
            groupsById.clear();
        }
        if (usersByGroupId != null) {
            usersByGroupId.clear();
        }
        if (groupsByUser != null) {
            groupsByUser.clear();
        }
        // unregister the provider
        if (commonLogic != null) {
            commonLogic.registerEvalGroupsProvider(null);
        }
        // kill the repeating timer
        if (timer != null) {
            timer.cancel();
        }
    }

    Timer timer = null;
    protected void initiateUpdateCacheTimer() {
        // timer repeats every 12 hours
        final long repeatInterval = 1000l * 60l * 60l * 12l;
        // start up a timer after 10 secs + random(60 secs)
        long startDelay =  (1000 * 10) + (1000 * new Random().nextInt(60));

        TimerTask runStateUpdateTask = new TimerTask() {
            @Override
            public void run() {
                reloadCacheData();
            }
        };

        // now we need to obtain a lock and then run the task if we have it
        timer = new Timer(true);
        log.info("Initializing the repeating timer task for evaluation groups cache update, first run in " + (startDelay/1000) + " seconds " +
                "and subsequent runs will happen every " + (repeatInterval/1000) + " seconds after that");
        timer.schedule(runStateUpdateTask, startDelay, repeatInterval);
    }

    /**
     * Loads the data into the caches,
     * this should actually load the data into other cache maps and then replace the current ones
     * once it is almost complete
     */
    public void reloadCacheData() {
        int coursesCount = dao.getCoursesCount();
        int membersCount = dao.getMembersCount();
        int instructorsCount = dao.getInstructorsCount();
        int crossListCount = dao.getCrosslistCount();
        log.info("Starting the cache data reload for the UCB eval groups provider, "+coursesCount+" courses with "+membersCount+" members ("+instructorsCount+" instructors) and "+crossListCount+" crosslisting records");

        // groups by group id (evalGroupId -> EvalGroup)
        Map<String, EvalGroup> groupsById = new HashMap<String, EvalGroup>(coursesCount);
        // users by group id (evalGroupId -> [userId])
        Map<String, Set<String>> usersByGroupId = new HashMap<String, Set<String>>(membersCount);
        // groups by user (userId -> {perm -> [evalGroupId]})
        Map<String, Map<String, Set<String>>> groupsByUser = new HashMap<String, Map<String, Set<String>>>(coursesCount);

        // TODO load the cache data into the 3 cache maps

        List<Map<String, Object>> courses = dao.getCourses();
        for (Map<String, Object> course : courses) {
            // TODO
        }

        // replace existing cache maps
        this.groupsById = groupsById;
        this.usersByGroupId = usersByGroupId;
        this.groupsByUser = groupsByUser;
        log.info("Completed the cache data reload for the UCB eval groups provider: "+groupsById.size()+" groups, "+usersByGroupId.size()+" users, "+groupsByUser.size()+" groupsByUser");
    }

    // groups by group id (evalGroupId -> EvalGroup)
    Map<String, EvalGroup> groupsById = new HashMap<String, EvalGroup>(0);
    // users by group id (evalGroupId -> [userId])
    Map<String, Set<String>> usersByGroupId = new HashMap<String, Set<String>>(0);
    // groups by user (userId -> {perm -> [evalGroupId]})
    Map<String, Map<String, Set<String>>> groupsByUser = new HashMap<String, Map<String, Set<String>>>(0);

    // PROVIDER METHODS

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#getUserIdsForEvalGroups(java.lang.String[], java.lang.String)
     */
    public Set<String> getUserIdsForEvalGroups(String[] groupIds, String permission) {
        Set<String> userIds = new HashSet<String>(0);
        if (groupIds != null && permission != null) {
            for (String evalGroupId : groupIds) {
                Set<String> users = usersByGroupId.get(evalGroupId);
                if (users != null) {
                    userIds.addAll(users);
                }
            }
        }
        return userIds;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#countUserIdsForEvalGroups(java.lang.String[], java.lang.String)
     */
    public int countUserIdsForEvalGroups(String[] groupIds, String permission) {
        return getUserIdsForEvalGroups(groupIds, permission).size();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#getEvalGroupsForUser(java.lang.String, java.lang.String)
     */
    public List<EvalGroup> getEvalGroupsForUser(String userId, String permission) {
        List<EvalGroup> evalGroups = new ArrayList<EvalGroup>();
        if (userId != null && permission != null) {
            Map<String, Set<String>> permGroupIds = groupsByUser.get(userId);
            if (permGroupIds != null) {
                Set<String> groupIds = permGroupIds.get(permission);
                if (groupIds != null) {
                    for (String groupId : groupIds) {
                        EvalGroup group = groupsById.get(groupId);
                        if (group != null) {
                            evalGroups.add(group);
                        }
                    }
                }
            }
        }
        return evalGroups;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#countEvalGroupsForUser(java.lang.String, java.lang.String)
     */
    public int countEvalGroupsForUser(String userId, String permission) {
        return getEvalGroupsForUser(userId, permission).size();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#getGroupByGroupId(java.lang.String)
     */
    public EvalGroup getGroupByGroupId(String groupId) {
        EvalGroup group = null;
        if (groupId != null) {
            group = groupsById.get(groupId);
        }
        return group;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#isUserAllowedInGroup(java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean isUserAllowedInGroup(String userId, String permission, String groupId) {
        boolean allowed = false;
        if (userId != null && permission != null && groupId != null) {
            Map<String, Set<String>> permGroupIds = groupsByUser.get(userId);
            if (permGroupIds != null) {
                Set<String> groupIds = permGroupIds.get(permission);
                if (groupIds != null) {
                    if (groupIds.contains(groupId)) {
                        allowed = true;
                    }
                }
            }
        }
        return allowed;
    }

}

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

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.providers.EvalGroupsProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;


/**
 * Groups Provider implementation for the UCB evals system courses and memberships
 * 
 * @author Aaron Zeckoski (azeckoski @ vt.edu)
 */
public class UCBEvalGroupsProvider implements EvalGroupsProvider {

    private static Log log = LogFactory.getLog(UCBEvalGroupsProvider.class);

    ApplicationContext applicationContext;
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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
                Object currentKEP = ComponentManager.get(UCBEvalGroupsProvider.class.getName());
                // check if something is already registered
                if (currentKEP != null) {
                    log.info("Found existing "+UCBEvalGroupsProvider.class.getName()+" in the ComponentManager: "+currentKEP);
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
        } catch (Exception ex) {
            log.warn("EvalGroupsProvider.init(): "+ex, ex);
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#getUserIdsForEvalGroups(java.lang.String[], java.lang.String)
     */
    public Set<String> getUserIdsForEvalGroups(String[] groupIds, String permission) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#countUserIdsForEvalGroups(java.lang.String[], java.lang.String)
     */
    public int countUserIdsForEvalGroups(String[] groupIds, String permission) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#getEvalGroupsForUser(java.lang.String, java.lang.String)
     */
    public List<EvalGroup> getEvalGroupsForUser(String userId, String permission) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#countEvalGroupsForUser(java.lang.String, java.lang.String)
     */
    public int countEvalGroupsForUser(String userId, String permission) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#getGroupByGroupId(java.lang.String)
     */
    public EvalGroup getGroupByGroupId(String groupId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#isUserAllowedInGroup(java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean isUserAllowedInGroup(String userId, String permission, String groupId) {
        // TODO Auto-generated method stub
        return false;
    }

}

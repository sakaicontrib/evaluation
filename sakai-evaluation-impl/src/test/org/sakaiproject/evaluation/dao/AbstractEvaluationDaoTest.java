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
package org.sakaiproject.evaluation.dao;

import org.sakaiproject.evaluation.dao.EvaluationAdminSupportDao;
import org.sakaiproject.evaluation.dao.EvaluationAssignmentDao;
import org.sakaiproject.evaluation.dao.EvaluationAuthoringDao;
import org.sakaiproject.evaluation.dao.EvaluationConsolidatedEmailDao;
import org.sakaiproject.evaluation.dao.EvaluationDaoBase;
import org.sakaiproject.evaluation.dao.EvaluationEmailTemplateDao;
import org.sakaiproject.evaluation.dao.EvaluationLockDao;
import org.sakaiproject.evaluation.dao.EvaluationQueryDao;
import org.sakaiproject.evaluation.dao.EvaluationResponseDao;
import org.sakaiproject.evaluation.dao.EvaluationSettingsDao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.model.EvalAdmin;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalConfig;
import org.sakaiproject.evaluation.model.EvalEmailProcessingData;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalGroupNodes;
import org.sakaiproject.evaluation.model.EvalHierarchyRule;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestDataImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import lombok.extern.slf4j.Slf4j;


/**
 * Shared Spring fixture for evaluation DAO tests.
 */
@ContextConfiguration(locations={
		"/hibernate-test.xml",
		"classpath:org/sakaiproject/evaluation/spring-hibernate.xml"})
public abstract class AbstractEvaluationDaoTest extends AbstractTransactionalJUnit4SpringContextTests {

    protected EvaluationDaoBase persistence;
    protected EvaluationSettingsDao settingsDao;
    protected EvaluationEmailTemplateDao emailTemplateDao;
    protected EvaluationAuthoringDao authoringDao;
    protected EvaluationAdminSupportDao adminSupportDao;
    protected EvaluationResponseDao responseDao;
    protected EvaluationAssignmentDao assignmentDao;
    protected EvaluationQueryDao queryDao;
    protected EvaluationLockDao lockDao;
    protected EvaluationConsolidatedEmailDao consolidatedEmailDao;

    protected EvalTestDataLoad etdl;

    protected EvalScale scaleLocked;
    protected EvalItem itemLocked;
    protected EvalItem itemUnlocked;
    protected EvalEvaluation evalUnLocked;

    protected static final long MILLISECONDS_PER_DAY = 24L * 60L * 60L * 1000L;

    // run this before each test starts
    @Before
    public void onSetUpBeforeTransaction() throws Exception {
        // load the spring created dao class bean from the Spring Application Context
        loadDaoPorts();

        // check the preloaded data
        Assert.assertTrue("Error preloading data", persistence.countAll(EvalScale.class) > 0);

        // check the preloaded test data
        Assert.assertTrue("Error preloading test data", persistence.countAll(EvalEvaluation.class) > 0);

        PreloadTestDataImpl ptd = (PreloadTestDataImpl) applicationContext.getBean("org.sakaiproject.evaluation.test.PreloadTestData");
        if (ptd == null) {
            throw new NullPointerException("PreloadTestDataImpl could not be retrieved from spring context");
        }

        // get test objects
        etdl = ptd.getEtdl();

        // preload additional data if desired
        List<String> optionsA = new ArrayList<String>( Arrays.asList("Male", "Female", "Unknown"));
        scaleLocked = new EvalScale(EvalTestDataLoad.ADMIN_USER_ID, "Scale Alpha", EvalConstants.SCALE_MODE_SCALE, 
                EvalConstants.SHARING_PRIVATE, EvalTestDataLoad.NOT_EXPERT, "description", 
                EvalConstants.SCALE_IDEAL_NONE, optionsA, EvalTestDataLoad.LOCKED);
        persistence.save( scaleLocked );

        itemLocked = new EvalItem(EvalTestDataLoad.MAINT_USER_ID, "Header type locked", EvalConstants.SHARING_PRIVATE, 
                EvalConstants.ITEM_TYPE_HEADER, EvalTestDataLoad.NOT_EXPERT);
        itemLocked.setLocked(EvalTestDataLoad.LOCKED);
        persistence.save( itemLocked );

        itemUnlocked = new EvalItem(EvalTestDataLoad.MAINT_USER_ID, "Header type locked", EvalConstants.SHARING_PRIVATE, 
                EvalConstants.ITEM_TYPE_HEADER, EvalTestDataLoad.NOT_EXPERT);
        itemUnlocked.setScale(etdl.scale2);
        itemUnlocked.setScaleDisplaySetting( EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL );
        itemUnlocked.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
        itemUnlocked.setLocked(EvalTestDataLoad.UNLOCKED);
        persistence.save( itemUnlocked );

        evalUnLocked = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, EvalTestDataLoad.MAINT_USER_ID, "Eval active not taken", null, 
                etdl.yesterday, etdl.tomorrow, etdl.tomorrow, etdl.threeDaysFuture, false, null,
                false, null, 
                EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.SHARING_VISIBLE, EvalConstants.INSTRUCTOR_OPT_IN, 1, null, null, null, null,
                etdl.templatePublicUnused, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                EvalTestDataLoad.UNLOCKED, EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null, null);

        persistence.save( evalUnLocked );

    }

    protected void loadDaoPorts() {
        persistence = applicationContext.getBean("org.sakaiproject.evaluation.dao.EvaluationDaoBase", EvaluationDaoBase.class);
        settingsDao = applicationContext.getBean("org.sakaiproject.evaluation.dao.EvaluationSettingsDao", EvaluationSettingsDao.class);
        emailTemplateDao = applicationContext.getBean("org.sakaiproject.evaluation.dao.EvaluationEmailTemplateDao", EvaluationEmailTemplateDao.class);
        authoringDao = applicationContext.getBean("org.sakaiproject.evaluation.dao.EvaluationAuthoringDao", EvaluationAuthoringDao.class);
        adminSupportDao = applicationContext.getBean("org.sakaiproject.evaluation.dao.EvaluationAdminSupportDao", EvaluationAdminSupportDao.class);
        responseDao = applicationContext.getBean("org.sakaiproject.evaluation.dao.EvaluationResponseDao", EvaluationResponseDao.class);
        assignmentDao = applicationContext.getBean("org.sakaiproject.evaluation.dao.EvaluationAssignmentDao", EvaluationAssignmentDao.class);
        queryDao = applicationContext.getBean("org.sakaiproject.evaluation.dao.EvaluationQueryDao", EvaluationQueryDao.class);
        lockDao = applicationContext.getBean("org.sakaiproject.evaluation.dao.EvaluationLockDao", EvaluationLockDao.class);
        consolidatedEmailDao = applicationContext.getBean("org.sakaiproject.evaluation.dao.EvaluationConsolidatedEmailDao", EvaluationConsolidatedEmailDao.class);
        if (persistence == null) {
            throw new NullPointerException("DAO could not be retrieved from spring context");
        }
    }

    protected Session currentSession() {
        SessionFactory sessionFactory = applicationContext.getBean(
                "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory", SessionFactory.class);
        return sessionFactory.getCurrentSession();
    }

    protected <T> T loadUninitializedProxy(Class<T> type, Serializable id) {
        Session session = currentSession();
        session.flush();
        session.clear();
        return session.load(type, id);
    }

}

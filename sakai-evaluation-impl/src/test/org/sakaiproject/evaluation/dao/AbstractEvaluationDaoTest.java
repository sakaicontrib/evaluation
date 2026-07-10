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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.BaseTestEvalLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;


/**
 * Shared Spring fixture for evaluation DAO tests.
 */
public abstract class AbstractEvaluationDaoTest extends BaseTestEvalLogic {

    protected EvalScale scaleLocked;
    protected EvalItem itemLocked;
    protected EvalItem itemUnlocked;
    protected EvalEvaluation evalUnLocked;

    protected static final long MILLISECONDS_PER_DAY = 24L * 60L * 60L * 1000L;

    // run this before each test starts
    @Before
    @Override
    public void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();

        // check the preloaded data
        Assert.assertTrue("Error preloading data", persistence.countAll(EvalScale.class) > 0);

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

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

package org.sakaiproject.evaluation.logic.test;

import junit.framework.Assert;

import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.impl.scheduling.EvalJobLogicImpl;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestData;
import org.springframework.test.AbstractTransactionalSpringContextTests;

public class EvalJobLogicImplTest extends AbstractTransactionalSpringContextTests {
	
	protected EvalJobLogicImpl jobLogic;

	private EvaluationDao evaluationDao;
	private EvalTestDataLoad etdl;

	@Override
	protected String[] getConfigLocations() {
		// point to the needed spring config files, must be on the classpath
		// (add component/src/webapp/WEB-INF to the build path in Eclipse),
		// they also need to be referenced in the project.xml file
		return new String[] {"hibernate-test.xml", "spring-hibernate.xml"};
	}
	
	// run this before each test starts
	protected void onSetUpBeforeTransaction() throws Exception {
		// load the spring created dao class bean from the Spring Application Context
		evaluationDao = (EvaluationDao) applicationContext.getBean("org.sakaiproject.evaluation.dao.EvaluationDao");
		if (evaluationDao == null) {
			throw new NullPointerException("EvaluationDao could not be retrieved from spring evalGroupId");
		}

		// check the preloaded data
		Assert.assertTrue("Error preloading data", evaluationDao.countAll(EvalScale.class) > 0);

		// check the preloaded test data
		Assert.assertTrue("Error preloading test data", evaluationDao.countAll(EvalEvaluation.class) > 0);

		PreloadTestData ptd = (PreloadTestData) applicationContext.getBean("org.sakaiproject.evaluation.test.PreloadTestData");
		if (ptd == null) {
			throw new NullPointerException("PreloadTestData could not be retrieved from spring evalGroupId");
		}

		// get test objects
		etdl = ptd.getEtdl();
		
		//create and setup the object to be tested
		jobLogic = new EvalJobLogicImpl();
	}
	
	// run this before each test starts and as part of the transaction
	protected void onSetUpInTransaction() {
		// preload additional data if desired
		
	}
	
	/**
	 * Test method for {@link EvalJobLogicImpl#isValidJobType(String)}
	 */
	public void testIsValidJobType() {
		//each valid type returns true
		Assert.assertTrue( jobLogic.isValidJobType(EvalConstants.JOB_TYPE_ACTIVE));
		Assert.assertTrue( jobLogic.isValidJobType(EvalConstants.JOB_TYPE_CLOSED));
		Assert.assertTrue( jobLogic.isValidJobType(EvalConstants.JOB_TYPE_CREATED));
		Assert.assertTrue( jobLogic.isValidJobType(EvalConstants.JOB_TYPE_DUE));
		Assert.assertTrue( jobLogic.isValidJobType(EvalConstants.JOB_TYPE_REMINDER));
		Assert.assertTrue( jobLogic.isValidJobType(EvalConstants.JOB_TYPE_VIEWABLE));
		Assert.assertTrue( jobLogic.isValidJobType(EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS));
		Assert.assertTrue( jobLogic.isValidJobType(EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS));
		//invalid or "" type returns false
		Assert.assertFalse( jobLogic.isValidJobType(EvalTestDataLoad.INVALID_CONSTANT_STRING));
		Assert.assertFalse( jobLogic.isValidJobType(new String("")));
		//null type retuns false
		Assert.assertFalse( jobLogic.isValidJobType(null));
	}
	
	/**
	 *  Test method for {@link EvalJobLogicImpl#isJobTypeScheduled(Long, String)}
	 *
	 */
	public void testIsJobTypeScheduled() {
		//invalid id, valid job type returns false
		Assert.assertFalse( jobLogic.isJobTypeScheduled(EvalTestDataLoad.INVALID_LONG_ID,EvalConstants.JOB_TYPE_ACTIVE));
		//invalid id, invalid job type returns false
		Assert.assertFalse( jobLogic.isJobTypeScheduled(EvalTestDataLoad.INVALID_LONG_ID,EvalTestDataLoad.INVALID_CONSTANT_STRING));
		//null id, valid job type returns false
		Assert.assertFalse( jobLogic.isJobTypeScheduled(null,EvalConstants.JOB_TYPE_ACTIVE));
		
		//TODO:
		//valid id, invalid job type returns false
		//valid id, empty String job type returns false
		//null id, null job type returns false
		//valid id, null job type returns false
		//no scheduled job returns false
		//1 sheduled job returns true
		//multiple scheduled jobs returns true
	}
	
	/**
	 *  Test method for {@link EvalJobLogicImpl#getReminderTime(EvalEvaluation)}
	 *
	 */
	public void testGetReminderTime() {
		//first reminder interval after start date in the futute (i.e., 15 minutes from now)
		//return 0 if no reminder should be scheduled
		//return long value of timeService.newTime(reminderTime)
		long reminderTime;
		
		//starts tomorrow, MAIN_USER_ID owns, templatePublic, no responses, no assigned groups, EVAL_CATEGORY1
		EvalEvaluation testEval = etdl.evaluationNew;
		
		//TODO:
		//valid reminder days 2-7
		//reduce due date
		//extend due date
	}
}
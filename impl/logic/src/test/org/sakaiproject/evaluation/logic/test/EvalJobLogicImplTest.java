/**
 * $Id: EvalJobLogicImplTest.java 1000 Dec 26, 2006 10:07:31 AM rwellis $
 * $URL: https://source.sakaiproject.org/contrib $
 * EvalJobLogicImplTest.java - evaluation - Oct 26, 2007 10:07:31 AM - rwellis
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

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

/**
 * FIXME test the rest of the methods -AZ
 * 
 * @author Dick Ellis (rwellis@umich.edu)
 */
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
		Assert.assertTrue( EvalJobLogicImpl.isValidJobType(EvalConstants.JOB_TYPE_ACTIVE));
		Assert.assertTrue( EvalJobLogicImpl.isValidJobType(EvalConstants.JOB_TYPE_CLOSED));
		Assert.assertTrue( EvalJobLogicImpl.isValidJobType(EvalConstants.JOB_TYPE_CREATED));
		Assert.assertTrue( EvalJobLogicImpl.isValidJobType(EvalConstants.JOB_TYPE_DUE));
		Assert.assertTrue( EvalJobLogicImpl.isValidJobType(EvalConstants.JOB_TYPE_REMINDER));
		Assert.assertTrue( EvalJobLogicImpl.isValidJobType(EvalConstants.JOB_TYPE_VIEWABLE));
		Assert.assertTrue( EvalJobLogicImpl.isValidJobType(EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS));
		Assert.assertTrue( EvalJobLogicImpl.isValidJobType(EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS));
		//invalid or "" type returns false
		Assert.assertFalse( EvalJobLogicImpl.isValidJobType(EvalTestDataLoad.INVALID_CONSTANT_STRING));
		Assert.assertFalse( EvalJobLogicImpl.isValidJobType(new String("")));
		//null type retuns false
		Assert.assertFalse( EvalJobLogicImpl.isValidJobType(null));
	}

}
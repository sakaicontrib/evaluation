/**
 * $Id$
 * $URL$
 * EvalSettingsImplTest.java - evaluation - Dec 25, 2006 10:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic;

import java.util.Date;

import junit.framework.Assert;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.EvalSettingsImpl;
import org.sakaiproject.evaluation.model.EvalConfig;


/**
 * Test case for EvaluationSettingsImpl
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalSettingsImplTest extends BaseTestEvalLogic {

	private EvalSettingsImpl evalSettings;

	private EvalConfig config1;
	private EvalConfig config3;

	private final String TEST_NAME1 = "testName";
	private final String TEST_CONSTANT1 = TEST_NAME1 + ":java.lang.String";
	private final String TEST_VALUE1 = "test value one";

	private final String TEST_NAME2 = "testName2";
	private final String TEST_VALUE2 = "test value two";

	private final String TEST_NAME3 = "testName3";
	private final String TEST_CONSTANT3 = TEST_NAME3 + ":java.lang.String";
	private final String TEST_VALUE3 = "test value three";

	private final String INVALID_CONSTANT = "XXXXXXXXXXXX" + ":java.lang.String";

	@Override
	protected String[] getConfigLocations() {
		// point to the needed spring config files, must be on the classpath
		// (add component/src/webapp/WEB-INF to the build path in Eclipse),
		// they also need to be referenced in the project.xml file
		return new String[] {"hibernate-test.xml", "spring-hibernate.xml"};
	}

	// run this before each test starts
	protected void onSetUpBeforeTransaction() throws Exception {
	   super.onSetUpBeforeTransaction();

		// load up any other needed spring beans

		// setup the mock objects if needed

		// create and setup the object to be tested
		evalSettings = new EvalSettingsImpl();
		evalSettings.setEvaluationDao(evaluationDao);

	}

	// run this before each test starts and as part of the transaction
	protected void onSetUpInTransaction() {
		// create test objects
		config1 = new EvalConfig(new Date(), TEST_NAME1, TEST_VALUE1);
		config3 = new EvalConfig(new Date(), TEST_NAME3, TEST_VALUE3);

		// preload additional data if desired
		evaluationDao.save(config1);
		evaluationDao.save(config3);
		
	}

	/**
	 * ADD unit tests below here, use testMethod as the name of the unit test,
	 * Note that if a method is overloaded you should include the arguments in the
	 * test name like so: testMethodClassInt (for method(Class, int);
	 */



	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.EvalSettingsImpl#get(java.lang.String)}.
	 */
	public void testGet() {
		// get a real value using a constant
		String s = (String) evalSettings.get(EvalSettings.FROM_EMAIL_ADDRESS);
		Assert.assertNotNull(s);
		Assert.assertTrue(s.length() > 0);

		// get the test value
		s = (String) evalSettings.get(TEST_CONSTANT1);
		Assert.assertNotNull(s);
		Assert.assertEquals(s, TEST_VALUE1);

		s = (String) evalSettings.get(TEST_CONSTANT3);
		Assert.assertNotNull(s);
		Assert.assertEquals(s, TEST_VALUE3);

		// get the test value (optional String only method)
		s = (String) evalSettings.get(TEST_NAME1);
		Assert.assertNotNull(s);
		Assert.assertEquals(s, TEST_VALUE1);

		// attempt to get the wrong object
		try {
			Boolean b = (Boolean) evalSettings.get(TEST_CONSTANT1);
			b.booleanValue();
			Assert.fail("Should have thrown a cast exception");
		} catch (ClassCastException e) {
			Assert.assertNotNull(e);
		}

		// attempt to get a non-existent item
		s = (String) evalSettings.get(INVALID_CONSTANT);
		Assert.assertNull(s); // non-existent items return a null

		// attempt to get an empty string
		try {
			s = (String) evalSettings.get("");
			Assert.fail("Should have thrown an illegal argument exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// attempt to get a null
		try {
			s = (String) evalSettings.get(null);
			Assert.fail("Should have thrown an illegal argument exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.EvalSettingsImpl#set(java.lang.String, java.lang.Object)}.
	 */
	public void testSet() {
		// set a real value using a constant
		Assert.assertEquals(true, evalSettings.set(EvalSettings.FROM_EMAIL_ADDRESS, TEST_VALUE1));
		String s = (String) evalSettings.get(EvalSettings.FROM_EMAIL_ADDRESS);
		Assert.assertNotNull(s);
		Assert.assertEquals(s, TEST_VALUE1);

		// set the test value to a new value
		Assert.assertEquals(true, evalSettings.set(TEST_CONSTANT1, TEST_VALUE2));
		s = (String) evalSettings.get(TEST_CONSTANT1);
		Assert.assertNotNull(s);
		Assert.assertEquals(s, TEST_VALUE2);

		// set test value using optional string method
		Assert.assertEquals(true, evalSettings.set(TEST_NAME2, TEST_VALUE1));
		s = (String) evalSettings.get(TEST_NAME2); // use optional string method to retrieve
		Assert.assertNotNull(s);
		Assert.assertEquals(s, TEST_VALUE1);		

		// test clearing the test value
		Assert.assertEquals(true, evalSettings.set(TEST_CONSTANT3, null));
		s = (String) evalSettings.get(TEST_CONSTANT3);
		Assert.assertNull(s);

		// NOT CURRENTLY SUPPORTED
//		// now attempt to set an invalid constant
//		try {
//			Assert.assertFalse( evalSettings.set(INVALID_CONSTANT, TEST_VALUE1) );
//			Assert.fail("Should have thrown an illegal argument exception");
//		} catch (IllegalArgumentException e) {
//			Assert.assertNotNull(e);
//		}

		// now attempt to set an invalid value
		try {
			evalSettings.set(TEST_CONSTANT1, Boolean.TRUE);
			Assert.fail("Should have thrown an illegal argument exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

	}

}

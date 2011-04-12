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

	// run this before each test starts
	protected void onSetUpBeforeTransaction() throws Exception {
	   super.onSetUpBeforeTransaction();

      // load up any other needed spring beans

		// setup the mock objects if needed

		// create and setup the object to be tested
		evalSettings = new EvalSettingsImpl();
		evalSettings.setDao(evaluationDao);
		evalSettings.setExternalLogic(externalLogic);

	}

	// run this before each test starts and as part of the transaction
	protected void onSetUpInTransaction() {
		// create test objects
		config1 = new EvalConfig(TEST_NAME1, TEST_VALUE1);
		config3 = new EvalConfig(TEST_NAME3, TEST_VALUE3);

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
		assertNotNull(s);
		assertTrue(s.length() > 0);

		// get the test value
		s = (String) evalSettings.get(TEST_CONSTANT1);
		assertNotNull(s);
		assertEquals(s, TEST_VALUE1);

		s = (String) evalSettings.get(TEST_CONSTANT3);
		assertNotNull(s);
		assertEquals(s, TEST_VALUE3);

		// get the test value (optional String only method)
		s = (String) evalSettings.get(TEST_NAME1);
		assertNotNull(s);
		assertEquals(s, TEST_VALUE1);

		// attempt to get the wrong object
		try {
			Boolean b = (Boolean) evalSettings.get(TEST_CONSTANT1);
			b.booleanValue();
			fail("Should have thrown a cast exception");
		} catch (ClassCastException e) {
			assertNotNull(e);
		}

		// attempt to get a non-existent item
		s = (String) evalSettings.get(INVALID_CONSTANT);
		assertNull(s); // non-existent items return a null

		// attempt to get an empty string
		try {
			s = (String) evalSettings.get("");
			fail("Should have thrown an illegal argument exception");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}

		// attempt to get a null
		try {
			s = (String) evalSettings.get(null);
			fail("Should have thrown an illegal argument exception");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.EvalSettingsImpl#set(java.lang.String, java.lang.Object)}.
	 */
	public void testSet() {
		// set a real value using a constant
		assertEquals(true, evalSettings.set(EvalSettings.FROM_EMAIL_ADDRESS, TEST_VALUE1));
		String s = (String) evalSettings.get(EvalSettings.FROM_EMAIL_ADDRESS);
		assertNotNull(s);
		assertEquals(s, TEST_VALUE1);

		// set the test value to a new value
		assertEquals(true, evalSettings.set(TEST_CONSTANT1, TEST_VALUE2));
		s = (String) evalSettings.get(TEST_CONSTANT1);
		assertNotNull(s);
		assertEquals(s, TEST_VALUE2);

		// set test value using optional string method
		assertEquals(true, evalSettings.set(TEST_NAME2, TEST_VALUE1));
		s = (String) evalSettings.get(TEST_NAME2); // use optional string method to retrieve
		assertNotNull(s);
		assertEquals(s, TEST_VALUE1);		

		// test clearing the test value
		assertEquals(true, evalSettings.set(TEST_CONSTANT3, null));
		s = (String) evalSettings.get(TEST_CONSTANT3);
		assertNull(s);

		// NOT CURRENTLY SUPPORTED
//		// now attempt to set an invalid constant
//		try {
//			assertFalse( evalSettings.set(INVALID_CONSTANT, TEST_VALUE1) );
//			fail("Should have thrown an illegal argument exception");
//		} catch (IllegalArgumentException e) {
//			assertNotNull(e);
//		}

		// now attempt to set an invalid value
		try {
			evalSettings.set(TEST_CONSTANT1, Boolean.TRUE);
			fail("Should have thrown an illegal argument exception");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}

	}

}

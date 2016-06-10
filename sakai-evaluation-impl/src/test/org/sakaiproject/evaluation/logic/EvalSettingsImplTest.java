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
package org.sakaiproject.evaluation.logic;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
	@Before
	public void onSetUpBeforeTransaction() throws Exception {
	   super.onSetUpBeforeTransaction();

      // load up any other needed spring beans

		// setup the mock objects if needed

		// create and setup the object to be tested
		evalSettings = new EvalSettingsImpl();
		evalSettings.setDao(evaluationDao);
		evalSettings.setExternalLogic(externalLogic);

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
	@Test
	public void testGet() {
		// get a real value using a constant
		String s = (String) evalSettings.get(EvalSettings.FROM_EMAIL_ADDRESS);
		Assert.assertNotNull(s);
		Assert.assertTrue(s.length() > 0);

		// get the test value
		s = (String) evalSettings.get(TEST_CONSTANT1);
		Assert.assertNotNull(s);
		Assert.assertEquals(TEST_VALUE1, s);

		s = (String) evalSettings.get(TEST_CONSTANT3);
		Assert.assertNotNull(s);
		Assert.assertEquals(TEST_VALUE3, s);

		// get the test value (optional String only method)
		s = (String) evalSettings.get(TEST_NAME1);
		Assert.assertNotNull(s);
		Assert.assertEquals(TEST_VALUE1, s);

		// attempt to get the wrong object
		try {
			Boolean b = (Boolean) evalSettings.get(TEST_CONSTANT1);
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
	@Test
	public void testSet() {
		// set a real value using a constant
		Assert.assertEquals(true, evalSettings.set(EvalSettings.FROM_EMAIL_ADDRESS, TEST_VALUE1));
		String s = (String) evalSettings.get(EvalSettings.FROM_EMAIL_ADDRESS);
		Assert.assertNotNull(s);
		Assert.assertEquals(TEST_VALUE1, s);

		// set the test value to a new value
		Assert.assertEquals(true, evalSettings.set(TEST_CONSTANT1, TEST_VALUE2));
		s = (String) evalSettings.get(TEST_CONSTANT1);
		Assert.assertNotNull(s);
		Assert.assertEquals(TEST_VALUE2, s);

		// set test value using optional string method
		Assert.assertEquals(true, evalSettings.set(TEST_NAME2, TEST_VALUE1));
		s = (String) evalSettings.get(TEST_NAME2); // use optional string method to retrieve
		Assert.assertNotNull(s);
		Assert.assertEquals(TEST_VALUE1, s);

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

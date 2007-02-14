/******************************************************************************
 * EvalExternalLogicStubTest.java - created by aaronz@vt.edu on Dec 26, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.test;

import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.sakaiproject.evaluation.logic.model.Context;
import org.sakaiproject.evaluation.logic.test.stubs.EvalExternalLogicStub;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;


/**
 * Test class for EvalExternalLogicStub<br/>
 * This is actually critical because this makes sure that the stub class passes all the
 * same tests that the real class has to pass
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalExternalLogicStubTest extends TestCase {

	protected EvalExternalLogicStub external;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		// create and setup the object to be tested
		external = new EvalExternalLogicStub();
	}


	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#getCurrentUserId()}.
	 */
	public void testGetCurrentUserId() {

		String userId = external.getCurrentUserId();
		Assert.assertNotNull(userId);
		Assert.assertEquals(EvalTestDataLoad.USER_ID, userId);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#getUserUsername(java.lang.String)}.
	 */
	public void testGetUserUsername() {

		String username = external.getUserUsername(EvalTestDataLoad.USER_ID);
		Assert.assertNotNull(username);
		Assert.assertEquals(EvalTestDataLoad.USER_NAME, username);

		username = external.getUserUsername(EvalTestDataLoad.MAINT_USER_ID);
		Assert.assertNotNull(username);
		Assert.assertEquals(EvalTestDataLoad.MAINT_USER_NAME, username);

		username = external.getUserUsername(EvalTestDataLoad.ADMIN_USER_ID);
		Assert.assertNotNull(username);
		Assert.assertEquals(EvalTestDataLoad.ADMIN_USER_NAME, username);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#getUserDisplayName(java.lang.String)}.
	 */
	public void testGetUserDisplayName() {

		String username = external.getUserDisplayName(EvalTestDataLoad.USER_ID);
		Assert.assertNotNull(username);
		Assert.assertEquals(EvalTestDataLoad.USER_DISPLAY, username);

		username = external.getUserDisplayName(EvalTestDataLoad.MAINT_USER_ID);
		Assert.assertNotNull(username);
		Assert.assertEquals(EvalTestDataLoad.MAINT_USER_DISPLAY, username);

		username = external.getUserDisplayName(EvalTestDataLoad.ADMIN_USER_ID);
		Assert.assertNotNull(username);
		Assert.assertEquals(EvalTestDataLoad.ADMIN_USER_DISPLAY, username);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#isUserAdmin(java.lang.String)}.
	 */
	public void testIsUserAdmin() {

		Assert.assertTrue( external.isUserAdmin(EvalTestDataLoad.ADMIN_USER_ID) );
		Assert.assertFalse( external.isUserAdmin(EvalTestDataLoad.MAINT_USER_ID) );
		Assert.assertFalse( external.isUserAdmin(EvalTestDataLoad.USER_ID) );

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#getCurrentContext()}.
	 */
	public void testGetCurrentContext() {

		String context = external.getCurrentContext();
		Assert.assertNotNull(context);
		Assert.assertEquals(EvalTestDataLoad.CONTEXT1, context);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#getDisplayTitle(java.lang.String)}.
	 */
	public void testGetDisplayTitle() {

		String title = external.getDisplayTitle(EvalTestDataLoad.CONTEXT1);
		Assert.assertNotNull(title);
		Assert.assertEquals(EvalTestDataLoad.CONTEXT1_TITLE, title);

		title = external.getDisplayTitle(EvalTestDataLoad.CONTEXT2);
		Assert.assertNotNull(title);
		Assert.assertEquals(EvalTestDataLoad.CONTEXT2_TITLE, title);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#countContextsForUser(java.lang.String, java.lang.String)}.
	 */
	public void testCountContextsForUser() {

		int count = external.countContextsForUser(EvalTestDataLoad.USER_ID, EvalConstants.PERM_TAKE_EVALUATION);
		Assert.assertEquals(2, count);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#getContextsForUser(java.lang.String, java.lang.String)}.
	 */
	public void testGetContextsForUser() {

		List l = external.getContextsForUser(EvalTestDataLoad.USER_ID, EvalConstants.PERM_TAKE_EVALUATION);
		Assert.assertNotNull(l);
		Assert.assertEquals(2, l.size());

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#getUserIdsForContext(java.lang.String, java.lang.String)}.
	 */
	public void testGetUserIdsForContext() {

		Set s = external.getUserIdsForContext(EvalTestDataLoad.CONTEXT1, EvalConstants.PERM_WRITE_TEMPLATE);
		Assert.assertNotNull(s);
		Assert.assertEquals(1, s.size());
		Assert.assertTrue(s.contains(EvalTestDataLoad.MAINT_USER_ID));
		Assert.assertTrue(! s.contains(EvalTestDataLoad.USER_ID));

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#countUserIdsForContext(java.lang.String, java.lang.String)}.
	 */
	public void testCountUserIdsForContext() {

		int count = external.countUserIdsForContext(EvalTestDataLoad.CONTEXT1, EvalConstants.PERM_WRITE_TEMPLATE);
		Assert.assertEquals(1, count);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#makeContextObject(java.lang.String)}.
	 */
	public void testMakeContextObject() {

		Context c = external.makeContextObject(EvalTestDataLoad.CONTEXT1);
		Assert.assertNotNull(c);
		Assert.assertEquals(EvalTestDataLoad.CONTEXT1_TITLE, c.title);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#isUserAllowedInContext(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	public void testIsUserAllowedInContext() {

		Assert.assertTrue( external.isUserAllowedInContext(
				EvalTestDataLoad.ADMIN_USER_ID, 
				EvalConstants.PERM_WRITE_TEMPLATE, 
				EvalTestDataLoad.CONTEXT1) );
		Assert.assertTrue( external.isUserAllowedInContext(
				EvalTestDataLoad.MAINT_USER_ID, 
				EvalConstants.PERM_ASSIGN_EVALUATION, 
				EvalTestDataLoad.CONTEXT1) );
		Assert.assertTrue( external.isUserAllowedInContext(
				EvalTestDataLoad.USER_ID, 
				EvalConstants.PERM_TAKE_EVALUATION, 
				EvalTestDataLoad.CONTEXT1) );

		Assert.assertFalse( external.isUserAllowedInContext(
				EvalTestDataLoad.MAINT_USER_ID, 
				EvalConstants.PERM_TAKE_EVALUATION, 
				EvalTestDataLoad.CONTEXT1) );
		Assert.assertFalse( external.isUserAllowedInContext(
				EvalTestDataLoad.USER_ID, 
				EvalConstants.PERM_BE_EVALUATED, 
				EvalTestDataLoad.CONTEXT1) );

	}

}

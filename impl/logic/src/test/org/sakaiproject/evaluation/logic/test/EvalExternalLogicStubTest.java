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

import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.test.mocks.EvalExternalLogicStub;
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
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#getCurrentEvalGroup()}.
	 */
	public void testGetCurrentEvalGroup() {

		String context = external.getCurrentEvalGroup();
		Assert.assertNotNull(context);
		Assert.assertEquals(EvalTestDataLoad.SITE1_REF, context);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#getDisplayTitle(java.lang.String)}.
	 */
	public void testGetDisplayTitle() {

		String title = external.getDisplayTitle(EvalTestDataLoad.SITE1_REF);
		Assert.assertNotNull(title);
		Assert.assertEquals(EvalTestDataLoad.SITE1_TITLE, title);

		title = external.getDisplayTitle(EvalTestDataLoad.SITE2_REF);
		Assert.assertNotNull(title);
		Assert.assertEquals(EvalTestDataLoad.SITE2_TITLE, title);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#countEvalGroupsForUser(java.lang.String, java.lang.String)}.
	 */
	public void testCountEvalGroupsForUser() {

		int count = external.countEvalGroupsForUser(EvalTestDataLoad.USER_ID, EvalConstants.PERM_TAKE_EVALUATION);
		Assert.assertEquals(2, count);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#getEvalGroupsForUser(java.lang.String, java.lang.String)}.
	 */
	public void testGetEvalGroupsForUser() {

		List l = external.getEvalGroupsForUser(EvalTestDataLoad.USER_ID, EvalConstants.PERM_TAKE_EVALUATION);
		Assert.assertNotNull(l);
		Assert.assertEquals(2, l.size());

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#getUserIdsForEvalGroup(java.lang.String, java.lang.String)}.
	 */
	public void testGetUserIdsForEvalGroup() {

		Set s = external.getUserIdsForEvalGroup(EvalTestDataLoad.SITE1_REF, EvalConstants.PERM_WRITE_TEMPLATE);
		Assert.assertNotNull(s);
		Assert.assertEquals(1, s.size());
		Assert.assertTrue(s.contains(EvalTestDataLoad.MAINT_USER_ID));
		Assert.assertTrue(! s.contains(EvalTestDataLoad.USER_ID));

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#countUserIdsForEvalGroup(java.lang.String, java.lang.String)}.
	 */
	public void testCountUserIdsForEvalGroup() {

		int count = external.countUserIdsForEvalGroup(EvalTestDataLoad.SITE1_REF, EvalConstants.PERM_WRITE_TEMPLATE);
		Assert.assertEquals(1, count);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#makeEvalGroupObject(java.lang.String)}.
	 */
	public void testMakeEvalGroupObject() {

		EvalGroup c = external.makeEvalGroupObject(EvalTestDataLoad.SITE1_REF);
		Assert.assertNotNull(c);
		Assert.assertEquals(EvalTestDataLoad.SITE1_TITLE, c.title);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicStub#isUserAllowedInEvalGroup(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	public void testIsUserAllowedInEvalGroup() {

		Assert.assertTrue( external.isUserAllowedInEvalGroup(
				EvalTestDataLoad.ADMIN_USER_ID, 
				EvalConstants.PERM_WRITE_TEMPLATE, 
				EvalTestDataLoad.SITE1_REF) );
		Assert.assertTrue( external.isUserAllowedInEvalGroup(
				EvalTestDataLoad.MAINT_USER_ID, 
				EvalConstants.PERM_ASSIGN_EVALUATION, 
				EvalTestDataLoad.SITE1_REF) );
		Assert.assertTrue( external.isUserAllowedInEvalGroup(
				EvalTestDataLoad.USER_ID, 
				EvalConstants.PERM_TAKE_EVALUATION, 
				EvalTestDataLoad.SITE1_REF) );

		Assert.assertFalse( external.isUserAllowedInEvalGroup(
				EvalTestDataLoad.MAINT_USER_ID, 
				EvalConstants.PERM_TAKE_EVALUATION, 
				EvalTestDataLoad.SITE1_REF) );
		Assert.assertFalse( external.isUserAllowedInEvalGroup(
				EvalTestDataLoad.USER_ID, 
				EvalConstants.PERM_BE_EVALUATED, 
				EvalTestDataLoad.SITE1_REF) );

	}

}

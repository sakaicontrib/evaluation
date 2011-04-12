/**
 * $Id$
 * $URL$
 * EvalExternalLogicStubTest.java - evaluation - Dec 26, 2006 10:07:31 AM - azeckoski
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

import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.mocks.MockEvalExternalLogic;


/**
 * Test class for MockEvalExternalLogic<br/>
 * This is actually critical because this makes sure that the stub class passes all the
 * same tests that the real class has to pass
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalExternalLogicStubTest extends TestCase {

	protected MockEvalExternalLogic external;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		// create and setup the object to be tested
		external = new MockEvalExternalLogic();
	}


	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.MockEvalExternalLogic#getCurrentUserId()}.
	 */
	public void testGetCurrentUserId() {

		String userId = external.getCurrentUserId();
		Assert.assertNotNull(userId);
		Assert.assertEquals(EvalTestDataLoad.USER_ID, userId);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.MockEvalExternalLogic#getUserUsername(java.lang.String)}.
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
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.MockEvalExternalLogic#getUserDisplayName(java.lang.String)}.
	 */
	public void testGetEvalUserById() {

		EvalUser user = external.getEvalUserById(EvalTestDataLoad.USER_ID);
		Assert.assertNotNull(user);
		Assert.assertEquals(EvalTestDataLoad.USER_DISPLAY, user.displayName);

		user = external.getEvalUserById(EvalTestDataLoad.MAINT_USER_ID);
		Assert.assertNotNull(user);
		Assert.assertEquals(EvalTestDataLoad.MAINT_USER_DISPLAY, user.displayName);

		user = external.getEvalUserById(EvalTestDataLoad.ADMIN_USER_ID);
		Assert.assertNotNull(user);
		Assert.assertEquals(EvalTestDataLoad.ADMIN_USER_DISPLAY, user.displayName);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.MockEvalExternalLogic#isUserAdmin(java.lang.String)}.
	 */
	public void testIsUserAdmin() {

		Assert.assertTrue( external.isUserSakaiAdmin(EvalTestDataLoad.ADMIN_USER_ID) );
		Assert.assertFalse( external.isUserSakaiAdmin(EvalTestDataLoad.MAINT_USER_ID) );
		Assert.assertFalse( external.isUserSakaiAdmin(EvalTestDataLoad.USER_ID) );

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.MockEvalExternalLogic#getCurrentEvalGroup()}.
	 */
	public void testGetCurrentEvalGroup() {

		String context = external.getCurrentEvalGroup();
		Assert.assertNotNull(context);
		Assert.assertEquals(EvalTestDataLoad.SITE1_REF, context);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.MockEvalExternalLogic#getDisplayTitle(java.lang.String)}.
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
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.MockEvalExternalLogic#countEvalGroupsForUser(java.lang.String, java.lang.String)}.
	 */
	public void testCountEvalGroupsForUser() {

		int count = external.countEvalGroupsForUser(EvalTestDataLoad.USER_ID, EvalConstants.PERM_TAKE_EVALUATION);
		Assert.assertEquals(2, count);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.MockEvalExternalLogic#getEvalGroupsForUser(java.lang.String, java.lang.String)}.
	 */
	public void testGetEvalGroupsForUser() {

		List<EvalGroup> l = external.getEvalGroupsForUser(EvalTestDataLoad.USER_ID, EvalConstants.PERM_TAKE_EVALUATION);
		Assert.assertNotNull(l);
		Assert.assertEquals(2, l.size());

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.MockEvalExternalLogic#getUserIdsForEvalGroup(java.lang.String, java.lang.String)}.
	 */
	public void testGetUserIdsForEvalGroup() {

		Set<String> s = external.getUserIdsForEvalGroup(EvalTestDataLoad.SITE1_REF, EvalConstants.PERM_WRITE_TEMPLATE);
		Assert.assertNotNull(s);
		Assert.assertEquals(1, s.size());
		Assert.assertTrue(s.contains(EvalTestDataLoad.MAINT_USER_ID));
		Assert.assertTrue(! s.contains(EvalTestDataLoad.USER_ID));

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.MockEvalExternalLogic#countUserIdsForEvalGroup(java.lang.String, java.lang.String)}.
	 */
	public void testCountUserIdsForEvalGroup() {

		int count = external.countUserIdsForEvalGroup(EvalTestDataLoad.SITE1_REF, EvalConstants.PERM_WRITE_TEMPLATE);
		Assert.assertEquals(1, count);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.MockEvalExternalLogic#makeEvalGroupObject(java.lang.String)}.
	 */
	public void testMakeEvalGroupObject() {

		EvalGroup c = external.makeEvalGroupObject(EvalTestDataLoad.SITE1_REF);
		Assert.assertNotNull(c);
		Assert.assertEquals(EvalTestDataLoad.SITE1_TITLE, c.title);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.MockEvalExternalLogic#isUserAllowedInEvalGroup(java.lang.String, java.lang.String, java.lang.String)}.
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

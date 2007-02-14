/******************************************************************************
 * EvalExternalLogicImplTest.java - created by aaronz@vt.edu on Dec 26, 2006
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.easymock.MockControl;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.evaluation.logic.impl.EvalExternalLogicImpl;
import org.sakaiproject.evaluation.logic.model.Context;
import org.sakaiproject.evaluation.logic.test.stubs.TestPlacement;
import org.sakaiproject.evaluation.logic.test.stubs.TestReference;
import org.sakaiproject.evaluation.logic.test.stubs.TestSite;
import org.sakaiproject.evaluation.logic.test.stubs.TestUser;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.test.AbstractTransactionalSpringContextTests;


/**
 * Test class for EvalExternalLogicImpl<br/>
 * The value of this test class is fairly debateable since everything in here is mocked
 * and if the mocks happen to be wrong then the tests are invalid<br/>
 * There is still value in that the tests show developers using the class under test
 * how it should be used so maybe it is worth it<br/> 
 * For now I will keep this in the code but it may be removed if it proves to be useless -AZ
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalExternalLogicImplTest extends AbstractTransactionalSpringContextTests {

	protected EvalExternalLogicImpl external;


	private AuthzGroupService authzGroupService;
	private MockControl authzGroupServiceControl;
	private EmailService emailService;
	private MockControl emailServiceControl;
	private EntityManager entityManager;
	private MockControl entityManagerControl;
	private SecurityService securityService;
	private MockControl securityServiceControl;
	private SessionManager sessionManager;
	private MockControl sessionManagerControl;
	private SiteService siteService;
	private MockControl siteServiceControl;
	private ToolManager toolManager;
	private MockControl toolManagerControl;
	private UserDirectoryService userDirectoryService;
	private MockControl userDirectoryServiceControl;

	protected String[] getConfigLocations() {
		// point to the needed spring config files, must be on the classpath
		// (add component/src/webapp/WEB-INF to the build path in Eclipse),
		// they also need to be referenced in the project.xml file
		return new String[] {"hibernate-test.xml", "spring-hibernate.xml"};
	}

	// run this before each test starts
	protected void onSetUpBeforeTransaction() throws Exception {
		// setup the mock objects
		authzGroupServiceControl = MockControl.createControl(AuthzGroupService.class);
		authzGroupService = (AuthzGroupService) authzGroupServiceControl.getMock();
		emailServiceControl = MockControl.createControl(EmailService.class);
		emailService = (EmailService) emailServiceControl.getMock();
		entityManagerControl = MockControl.createControl(EntityManager.class);
		entityManager = (EntityManager) entityManagerControl.getMock();
		securityServiceControl = MockControl.createControl(SecurityService.class);
		securityService = (SecurityService) securityServiceControl.getMock();
		sessionManagerControl = MockControl.createControl(SessionManager.class);
		sessionManager = (SessionManager) sessionManagerControl.getMock();
		siteServiceControl = MockControl.createControl(SiteService.class);
		siteService = (SiteService) siteServiceControl.getMock();
		toolManagerControl = MockControl.createControl(ToolManager.class);
		toolManager = (ToolManager) toolManagerControl.getMock();
		userDirectoryServiceControl = MockControl.createControl(UserDirectoryService.class);
		userDirectoryService = (UserDirectoryService) userDirectoryServiceControl.getMock();

		// create and setup the object to be tested
		external = new EvalExternalLogicImpl();
		external.setAuthzGroupService(authzGroupService);
		external.setEmailService(emailService);
		external.setEntityManager(entityManager);
		external.setSecurityService(securityService);
		external.setSessionManager(sessionManager);
		external.setSiteService(siteService);
		external.setToolManager(toolManager);
		external.setUserDirectoryService(userDirectoryService);

		// can set up the default mock object returns here if desired
		// Note: Still need to activate them in the test methods though

		// no defaults for authzGroupService

		entityManager.newReference(EvalTestDataLoad.AUTHZGROUP1A_ID);
		entityManagerControl.setReturnValue(
				new TestReference(EvalTestDataLoad.CONTEXT1, SiteService.APPLICATION_ID), 
				MockControl.ZERO_OR_MORE);
		entityManager.newReference(EvalTestDataLoad.AUTHZGROUP1B_ID);
		entityManagerControl.setReturnValue(
				new TestReference(EvalTestDataLoad.CONTEXT1, SiteService.GROUP_SUBTYPE), 
				MockControl.ZERO_OR_MORE);
		entityManager.newReference(EvalTestDataLoad.AUTHZGROUP2A_ID);
		entityManagerControl.setReturnValue(
				new TestReference(EvalTestDataLoad.CONTEXT2, SiteService.APPLICATION_ID), 
				MockControl.ZERO_OR_MORE);

		securityService.isSuperUser(EvalTestDataLoad.USER_ID); // normal user
		securityServiceControl.setReturnValue(false, MockControl.ZERO_OR_MORE); // return for above param
		securityService.isSuperUser(EvalTestDataLoad.MAINT_USER_ID); // maintain user
		securityServiceControl.setReturnValue(false, MockControl.ZERO_OR_MORE); // return for above param
		securityService.isSuperUser(EvalTestDataLoad.ADMIN_USER_ID); // admin user
		securityServiceControl.setReturnValue(true, MockControl.ZERO_OR_MORE); // return for above param
		securityService.isSuperUser(EvalTestDataLoad.INVALID_USER_ID); // invalid user
		securityServiceControl.setReturnValue(false, MockControl.ZERO_OR_MORE); // return for above param

		sessionManager.getCurrentSessionUserId(); // expect this to be called
		sessionManagerControl.setDefaultReturnValue(EvalTestDataLoad.USER_ID);

		siteService.siteReference(EvalTestDataLoad.SITE_ID); // expect this to be called
		siteServiceControl.setReturnValue(EvalTestDataLoad.SITE_REF, MockControl.ZERO_OR_MORE);
		siteService.siteReference(EvalTestDataLoad.SITE2_ID); // expect this to be called
		siteServiceControl.setReturnValue(EvalTestDataLoad.SITE2_REF, MockControl.ZERO_OR_MORE);
		siteService.siteReference(EvalTestDataLoad.CONTEXT1); // expect this to be called
		siteServiceControl.setReturnValue(EvalTestDataLoad.SITE_REF, MockControl.ZERO_OR_MORE);
		siteService.siteReference(EvalTestDataLoad.CONTEXT2); // expect this to be called
		siteServiceControl.setReturnValue(EvalTestDataLoad.SITE2_REF, MockControl.ZERO_OR_MORE);
		try {
			siteService.getSite(EvalTestDataLoad.CONTEXT1); // expect this to be called
			siteServiceControl.setReturnValue(new TestSite(EvalTestDataLoad.CONTEXT1, EvalTestDataLoad.CONTEXT1_TITLE), MockControl.ZERO_OR_MORE);
			siteService.getSite(EvalTestDataLoad.CONTEXT2); // expect this to be called
			siteServiceControl.setReturnValue(new TestSite(EvalTestDataLoad.CONTEXT1, EvalTestDataLoad.CONTEXT2_TITLE), MockControl.ZERO_OR_MORE);
		} catch (IdUnusedException e) {
			// just added try-catch because we have to in order to compile
			throw new IllegalStateException("Could not create siteService test object");
		}

		toolManager.getCurrentPlacement(); // expect this to be called
		toolManagerControl.setDefaultReturnValue(new TestPlacement(EvalTestDataLoad.SITE_ID));

		try {
			userDirectoryService.getUserEid(EvalTestDataLoad.ADMIN_USER_ID); // expect this to be called
			userDirectoryServiceControl.setReturnValue(EvalTestDataLoad.ADMIN_USER_NAME, MockControl.ZERO_OR_MORE);
			userDirectoryService.getUserEid(EvalTestDataLoad.MAINT_USER_ID); // expect this to be called
			userDirectoryServiceControl.setReturnValue(EvalTestDataLoad.MAINT_USER_NAME, MockControl.ZERO_OR_MORE);
			userDirectoryService.getUserEid(EvalTestDataLoad.USER_ID); // expect this to be called
			userDirectoryServiceControl.setReturnValue(EvalTestDataLoad.USER_NAME, MockControl.ZERO_OR_MORE);

			userDirectoryService.getUser(EvalTestDataLoad.ADMIN_USER_ID); // expect this to be called
			userDirectoryServiceControl.setReturnValue( 
					new TestUser(EvalTestDataLoad.ADMIN_USER_ID, EvalTestDataLoad.ADMIN_USER_NAME, EvalTestDataLoad.ADMIN_USER_DISPLAY), 
					MockControl.ZERO_OR_MORE);
			userDirectoryService.getUser(EvalTestDataLoad.MAINT_USER_ID); // expect this to be called
			userDirectoryServiceControl.setReturnValue( 
					new TestUser(EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.MAINT_USER_NAME, EvalTestDataLoad.MAINT_USER_DISPLAY), 
					MockControl.ZERO_OR_MORE);
			userDirectoryService.getUser(EvalTestDataLoad.USER_ID); // expect this to be called
			userDirectoryServiceControl.setReturnValue( 
					new TestUser(EvalTestDataLoad.USER_ID, EvalTestDataLoad.USER_NAME, EvalTestDataLoad.USER_DISPLAY), 
					MockControl.ZERO_OR_MORE);
		} catch (UserNotDefinedException e) {
			// just added try-catch because we have to in order to compile
			throw new IllegalStateException("Could not create userDirectoryService test object");
		}
	}

	// run this before each test starts and as part of the transaction
	protected void onSetUpInTransaction() {
		// preload additional data if desired
		
	}


	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicImpl#getCurrentUserId()}.
	 */
	public void testGetCurrentUserId() {
		// activate the mock objects
		sessionManagerControl.replay();

		// mock objects needed here
		String userId = external.getCurrentUserId();
		Assert.assertNotNull(userId);
		Assert.assertEquals(EvalTestDataLoad.USER_ID, userId);

		// verify the mock objects were used
		sessionManagerControl.verify();
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicImpl#getUserUsername(java.lang.String)}.
	 */
	public void testGetUserUsername() {
		// activate the mock objects
		userDirectoryServiceControl.replay();

		// mock objects needed here
		String username = external.getUserUsername(EvalTestDataLoad.USER_ID);
		Assert.assertNotNull(username);
		Assert.assertEquals(EvalTestDataLoad.USER_NAME, username);

		username = external.getUserUsername(EvalTestDataLoad.MAINT_USER_ID);
		Assert.assertNotNull(username);
		Assert.assertEquals(EvalTestDataLoad.MAINT_USER_NAME, username);

		username = external.getUserUsername(EvalTestDataLoad.ADMIN_USER_ID);
		Assert.assertNotNull(username);
		Assert.assertEquals(EvalTestDataLoad.ADMIN_USER_NAME, username);

		// verify the mock objects were used
		userDirectoryServiceControl.verify();
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicImpl#getUserDisplayName(java.lang.String)}.
	 */
	public void testGetUserDisplayName() {
		// set up mock objects with return values

		// activate the mock objects
		userDirectoryServiceControl.replay();

		// mock objects needed here
		String username = external.getUserDisplayName(EvalTestDataLoad.USER_ID);
		Assert.assertNotNull(username);
		Assert.assertEquals(EvalTestDataLoad.USER_DISPLAY, username);

		username = external.getUserDisplayName(EvalTestDataLoad.MAINT_USER_ID);
		Assert.assertNotNull(username);
		Assert.assertEquals(EvalTestDataLoad.MAINT_USER_DISPLAY, username);

		username = external.getUserDisplayName(EvalTestDataLoad.ADMIN_USER_ID);
		Assert.assertNotNull(username);
		Assert.assertEquals(EvalTestDataLoad.ADMIN_USER_DISPLAY, username);

		// verify the mock objects were used
		userDirectoryServiceControl.verify();
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicImpl#isUserAdmin(java.lang.String)}.
	 */
	public void testIsUserAdmin() {
		// set up mock objects with return values

		// activate the mock objects
		securityServiceControl.replay();

		// mock objects needed here
		Assert.assertTrue( external.isUserAdmin(EvalTestDataLoad.ADMIN_USER_ID) );
		Assert.assertFalse( external.isUserAdmin(EvalTestDataLoad.MAINT_USER_ID) );
		Assert.assertFalse( external.isUserAdmin(EvalTestDataLoad.USER_ID) );

		// verify the mock objects were used
		securityServiceControl.verify();
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicImpl#getCurrentContext()}.
	 */
	public void testGetCurrentContext() {
		// set up this mock object
		toolManager.getCurrentPlacement(); // expect this to be called
		toolManagerControl.setReturnValue(new TestPlacement(EvalTestDataLoad.CONTEXT1)); // return this

		// activate the mock object
		toolManagerControl.replay();

		// mock object is needed here
		String context = external.getCurrentContext();
		Assert.assertNotNull(context);
		Assert.assertEquals(EvalTestDataLoad.CONTEXT1, context);

		// verify the mock object was used
		toolManagerControl.verify();
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicImpl#getDisplayTitle(java.lang.String)}.
	 */
	public void testGetDisplayTitle() {
		// set up mock objects with return values

		// activate the mock objects
		siteServiceControl.replay();

		// mock objects needed here
		String title = external.getDisplayTitle(EvalTestDataLoad.CONTEXT1);
		Assert.assertNotNull(title);
		Assert.assertEquals(EvalTestDataLoad.CONTEXT1_TITLE, title);

		title = external.getDisplayTitle(EvalTestDataLoad.CONTEXT2);
		Assert.assertNotNull(title);
		Assert.assertEquals(EvalTestDataLoad.CONTEXT2_TITLE, title);

		// verify the mock objects were used
		siteServiceControl.verify();
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicImpl#countContextsForUser(java.lang.String, java.lang.String)}.
	 */
	public void testCountContextsForUser() {
		// set up mock objects with return values
		authzGroupService.getAuthzGroupsIsAllowed(EvalTestDataLoad.USER_ID, EvalConstants.PERM_TAKE_EVALUATION, null);
		// have to use the always matcher here since the 2nd argument is a Set
		authzGroupServiceControl.setDefaultMatcher(MockControl.ALWAYS_MATCHER);
		Set stuff = new HashSet();
		stuff.add(EvalTestDataLoad.AUTHZGROUP1A_ID);
		stuff.add(EvalTestDataLoad.AUTHZGROUP2A_ID);
		authzGroupServiceControl.setReturnValue(stuff);

		// activate the mock objects
		authzGroupServiceControl.replay();
		entityManagerControl.replay();

		// mock objects needed here
		int count = external.countContextsForUser(EvalTestDataLoad.USER_ID, EvalConstants.PERM_TAKE_EVALUATION);
		Assert.assertEquals(2, count);

		// verify the mock objects were used
		authzGroupServiceControl.verify();
		entityManagerControl.verify();
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicImpl#getContextsForUser(java.lang.String, java.lang.String)}.
	 */
	public void testGetContextsForUser() {
		// set up mock objects with return values
		authzGroupService.getAuthzGroupsIsAllowed(EvalTestDataLoad.USER_ID, EvalConstants.PERM_TAKE_EVALUATION, null);
		// have to use the always matcher here since the 2nd argument is a Set
		authzGroupServiceControl.setDefaultMatcher(MockControl.ALWAYS_MATCHER);
		Set stuff = new HashSet();
		stuff.add(EvalTestDataLoad.AUTHZGROUP1A_ID);
		stuff.add(EvalTestDataLoad.AUTHZGROUP2A_ID);
		authzGroupServiceControl.setReturnValue(stuff);

		// activate the mock objects
		authzGroupServiceControl.replay();
		entityManagerControl.replay();
		siteServiceControl.replay();

		// mock objects needed here
		List l = external.getContextsForUser(EvalTestDataLoad.USER_ID, EvalConstants.PERM_TAKE_EVALUATION);
		Assert.assertNotNull(l);
		Assert.assertEquals(2, l.size());

		// verify the mock objects were used
		authzGroupServiceControl.verify();
		entityManagerControl.verify();
		siteServiceControl.verify();
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicImpl#getUserIdsForContext(java.lang.String, java.lang.String)}.
	 */
	public void testGetUserIdsForContext() {
		// set up mock objects with return values
		authzGroupService.getUsersIsAllowed(EvalConstants.PERM_WRITE_TEMPLATE, null);
		// have to use the always matcher here since the 2nd argument is a Set
		authzGroupServiceControl.setDefaultMatcher(MockControl.ALWAYS_MATCHER);
		Set users = new HashSet();
		users.add(EvalTestDataLoad.MAINT_USER_ID);
		authzGroupServiceControl.setReturnValue(users);

		// activate the mock objects
		authzGroupServiceControl.replay();
		siteServiceControl.replay();

		// mock objects needed here
		Set s = external.getUserIdsForContext(EvalTestDataLoad.CONTEXT1, EvalConstants.PERM_WRITE_TEMPLATE);
		Assert.assertNotNull(s);
		Assert.assertEquals(1, s.size());
		Assert.assertTrue(s.contains(EvalTestDataLoad.MAINT_USER_ID));
		Assert.assertTrue(! s.contains(EvalTestDataLoad.USER_ID));

		// verify the mock objects were used
		authzGroupServiceControl.verify();
		siteServiceControl.verify();
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicImpl#getUserIdsForContext(java.lang.String, java.lang.String)}.
	 */
	public void testCountUserIdsForContext() {
		// set up mock objects with return values
		authzGroupService.getUsersIsAllowed(EvalConstants.PERM_WRITE_TEMPLATE, null);
		// have to use the always matcher here since the 2nd argument is a Set
		authzGroupServiceControl.setDefaultMatcher(MockControl.ALWAYS_MATCHER);
		Set users = new HashSet();
		users.add(EvalTestDataLoad.MAINT_USER_ID);
		authzGroupServiceControl.setReturnValue(users);

		// activate the mock objects
		authzGroupServiceControl.replay();
		siteServiceControl.replay();

		// mock objects needed here
		int count = external.countUserIdsForContext(EvalTestDataLoad.CONTEXT1, EvalConstants.PERM_WRITE_TEMPLATE);
		Assert.assertEquals(1, count);

		// verify the mock objects were used
		authzGroupServiceControl.verify();
		siteServiceControl.verify();
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicImpl#makeContextObject(java.lang.String)}.
	 */
	public void testMakeContextObject() {
		// set up mock objects with return values

		// activate the mock objects
		siteServiceControl.replay();

		// mock objects needed here
		Context c = external.makeContextObject(EvalTestDataLoad.CONTEXT1);
		Assert.assertNotNull(c);
		Assert.assertEquals(EvalTestDataLoad.CONTEXT1_TITLE, c.title);

		// verify the mock objects were used
		siteServiceControl.verify();
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicImpl#isUserAllowedInContext(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	public void testIsUserAllowedInContext() {
		// set up mock objects with return values
		securityService.unlock(EvalTestDataLoad.ADMIN_USER_ID, 
				EvalConstants.PERM_WRITE_TEMPLATE, EvalTestDataLoad.SITE_REF);
		securityServiceControl.setReturnValue(true, MockControl.ZERO_OR_MORE);

		securityService.unlock(EvalTestDataLoad.MAINT_USER_ID, 
				EvalConstants.PERM_ASSIGN_EVALUATION, EvalTestDataLoad.SITE_REF);
		securityServiceControl.setReturnValue(true, MockControl.ZERO_OR_MORE);
		securityService.unlock(EvalTestDataLoad.MAINT_USER_ID, 
				EvalConstants.PERM_TAKE_EVALUATION, EvalTestDataLoad.SITE_REF);
		securityServiceControl.setReturnValue(false, MockControl.ZERO_OR_MORE);

		securityService.unlock(EvalTestDataLoad.USER_ID, 
				EvalConstants.PERM_TAKE_EVALUATION, EvalTestDataLoad.SITE_REF);
		securityServiceControl.setReturnValue(true, MockControl.ZERO_OR_MORE);
		securityService.unlock(EvalTestDataLoad.USER_ID, 
				EvalConstants.PERM_BE_EVALUATED, EvalTestDataLoad.SITE_REF);
		securityServiceControl.setReturnValue(false, MockControl.ZERO_OR_MORE);

		// activate the mock objects
		securityServiceControl.replay();
		siteServiceControl.replay();

		// mock objects needed here
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

		// verify the mock objects were used
		securityServiceControl.verify();
		siteServiceControl.verify();
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExternalLogicImpl#isUserAllowedInContext(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	public void testSendEmails() {
		// cannot test this for now since easymock is making my life hard -AZ

//		userDirectoryService.getUsers( null );
//		userDirectoryServiceControl.setDefaultMatcher(MockControl.ALWAYS_MATCHER); // since this is a collection
//		List l = new ArrayList();
//		userDirectoryServiceControl.setReturnValue(l, MockControl.ZERO_OR_MORE);

//		emailService.sendMail(from, to, subject, content, headerTo, replyTo, additionalHeaders);
//		emailServiceControl.setDefaultMatcher(MockControl.ALWAYS_MATCHER); // since this is a collection
//		emailServiceControl.setReturnValue(true, MockControl.ZERO_OR_MORE);

		// activate the mock objects
//		userDirectoryServiceControl.replay();
//		emailServiceControl.replay();

//		// make sure this throws an illegal argument exception
//		try {
//			external.sendEmails("azeckoski@gmail.com", new String[] {"azeckoski@gmail.com"}, "test subject", "test message");
//			Assert.fail("Should have thrown exception");
//		} catch (IllegalArgumentException e) {
//			Assert.assertNotNull(e);
//		}
//
//		// check that null values cause exception
//		try {
//			external.sendEmails(null, new String[] {"azeckoski@gmail.com"}, "subject", "message");
//			Assert.fail("Should have thrown exception");
//		} catch (NullPointerException e) {
//			Assert.assertNotNull(e);
//		}
//
//		try {
//			external.sendEmails("aaronz@vt.edu", new String[] {"azeckoski@gmail.com"}, "subject", null);
//			Assert.fail("Should have thrown exception");
//		} catch (NullPointerException e) {
//			Assert.assertNotNull(e);
//		}

		// verify the mock objects were used
//		userDirectoryServiceControl.verify();
//		emailServiceControl.verify();
	}
}

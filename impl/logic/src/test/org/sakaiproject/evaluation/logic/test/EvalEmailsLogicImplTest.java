/******************************************************************************
 * EvalEmailsLogicImplTest.java - created by aaronz@vt.edu on Dec 29, 2006
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

import java.util.Date;

import junit.framework.Assert;

import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.impl.EvalEmailsLogicImpl;
import org.sakaiproject.evaluation.logic.impl.EvalEvaluationsLogicImpl;
import org.sakaiproject.evaluation.logic.test.stubs.EvalExternalLogicStub;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestData;
import org.springframework.test.AbstractTransactionalSpringContextTests;


/**
 * EvalEmailsLogicImpl test class
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalEmailsLogicImplTest extends AbstractTransactionalSpringContextTests {

	protected EvalEmailsLogicImpl emailTemplates;
	protected EvalEvaluationsLogicImpl evaluations;

	private EvaluationDao evaluationDao;
	private EvalTestDataLoad etdl;

	protected String[] getConfigLocations() {
		// point to the needed spring config files, must be on the classpath
		// (add component/src/webapp/WEB-INF to the build path in Eclipse),
		// they also need to be referenced in the project.xml file
		return new String[] {"hibernate-test.xml", "spring-hibernate.xml", "logic-support.xml"};
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

		// load up any other needed spring beans
		EvalSettings settings = (EvalSettings) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalSettings");
		if (settings == null) {
			throw new NullPointerException("EvalSettings could not be retrieved from spring evalGroupId");
		}

		// setup the mock objects if needed

		// create and setup the object to be tested
		emailTemplates = new EvalEmailsLogicImpl();
		emailTemplates.setDao(evaluationDao);
		emailTemplates.setExternalLogic( new EvalExternalLogicStub() );

	}

	// run this before each test starts and as part of the transaction
	protected void onSetUpInTransaction() {
		// preload additional data if desired
		
	}

	/**
	 * ADD unit tests below here, use testMethod as the name of the unit test,
	 * Note that if a method is overloaded you should include the arguments in the
	 * test name like so: testMethodClassInt (for method(Class, int);
	 */

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEmailsLogicImpl#saveEmailTemplate(org.sakaiproject.evaluation.model.EvalEmailTemplate, java.lang.String)}.
	 */
	public void testSaveEmailTemplate() {
		// test valid new saves
		emailTemplates.saveEmailTemplate( new EvalEmailTemplate( new Date(),
				EvalTestDataLoad.MAINT_USER_ID, "a message"), 
				EvalTestDataLoad.MAINT_USER_ID);
		emailTemplates.saveEmailTemplate( new EvalEmailTemplate( new Date(),
				EvalTestDataLoad.ADMIN_USER_ID, "another message"), 
				EvalTestDataLoad.ADMIN_USER_ID);

		// test saving new always nulls out the defaultType
		// the defaultType cannot be changed when saving
		// (default templates can only be set in the preloaded data for now)
		EvalEmailTemplate testTemplate = new EvalEmailTemplate( new Date(),
				EvalTestDataLoad.ADMIN_USER_ID, "a message", 
				EvalConstants.EMAIL_TEMPLATE_DEFAULT_AVAILABLE);
		emailTemplates.saveEmailTemplate( testTemplate, EvalTestDataLoad.ADMIN_USER_ID);
		Assert.assertNotNull( testTemplate.getId() );
		Assert.assertNull( testTemplate.getDefaultType() );

		// test invalid update to default template
		EvalEmailTemplate defaultTemplate = 
			emailTemplates.getDefaultEmailTemplate( EvalConstants.EMAIL_TEMPLATE_AVAILABLE );
		try {
			defaultTemplate.setMessage("new message for default");
			emailTemplates.saveEmailTemplate( defaultTemplate, 
					EvalTestDataLoad.ADMIN_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

		// test valid updates
		etdl.emailTemplate1.setMessage("new message 1");
		emailTemplates.saveEmailTemplate( etdl.emailTemplate1, EvalTestDataLoad.ADMIN_USER_ID);

		etdl.emailTemplate2.setMessage("new message 2");
		emailTemplates.saveEmailTemplate( etdl.emailTemplate2, EvalTestDataLoad.MAINT_USER_ID);

		// test user not has permission to update
		try {
			etdl.emailTemplate1.setMessage("new message 1");
			emailTemplates.saveEmailTemplate( etdl.emailTemplate1, 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}
		
		try {
			etdl.emailTemplate2.setMessage("new message 2");
			emailTemplates.saveEmailTemplate( etdl.emailTemplate2, 
					EvalTestDataLoad.USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

		// test associated eval is not in queue (is active) so cannot update
		try {
			etdl.emailTemplate3.setMessage("new message 3");
			emailTemplates.saveEmailTemplate( etdl.emailTemplate3, 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEmailsLogicImpl#getDefaultEmailTemplate(int)}.
	 */
	public void testGetDefaultEmailTemplate() {
		EvalEmailTemplate emailTemplate = null;

		// test getting the templates
		emailTemplate = emailTemplates.getDefaultEmailTemplate( 
				EvalConstants.EMAIL_TEMPLATE_CREATED );
		Assert.assertNotNull(emailTemplate);
		Assert.assertEquals( EvalConstants.EMAIL_TEMPLATE_DEFAULT_CREATED, 
				emailTemplate.getDefaultType() );
		Assert.assertEquals( EvalConstants.EMAIL_CREATED_DEFAULT_TEXT,
				emailTemplate.getMessage() );
		
		emailTemplate = emailTemplates.getDefaultEmailTemplate( 
				EvalConstants.EMAIL_TEMPLATE_AVAILABLE );
		Assert.assertNotNull(emailTemplate);
		Assert.assertEquals( EvalConstants.EMAIL_TEMPLATE_DEFAULT_AVAILABLE, 
				emailTemplate.getDefaultType() );
		Assert.assertEquals( EvalConstants.EMAIL_AVAILABLE_DEFAULT_TEXT,
				emailTemplate.getMessage() );
		
		emailTemplate = emailTemplates.getDefaultEmailTemplate( 
				EvalConstants.EMAIL_TEMPLATE_AVAILABLE_OPT_IN );
		Assert.assertNotNull(emailTemplate);
		Assert.assertEquals( EvalConstants.EMAIL_TEMPLATE_DEFAULT_AVAILABLE_OPT_IN, 
				emailTemplate.getDefaultType() );
		Assert.assertEquals( EvalConstants.EMAIL_AVAILABLE_OPT_IN_TEXT,
				emailTemplate.getMessage() );
		
		emailTemplate = emailTemplates.getDefaultEmailTemplate( 
				EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE );
		Assert.assertNotNull(emailTemplate);
		Assert.assertEquals( EvalConstants.EMAIL_TEMPLATE_DEFAULT_CONSOLIDATED_AVAILABLE, 
				emailTemplate.getDefaultType() );
		Assert.assertEquals( EvalConstants.EMAIL_CONSOLIDATED_AVAILABLE_DEFAULT_TEXT,
				emailTemplate.getMessage() );

		emailTemplate = emailTemplates.getDefaultEmailTemplate( 
				EvalConstants.EMAIL_TEMPLATE_REMINDER );
		Assert.assertNotNull(emailTemplate);
		Assert.assertEquals( EvalConstants.EMAIL_TEMPLATE_DEFAULT_REMINDER, 
				emailTemplate.getDefaultType() );
		Assert.assertEquals( EvalConstants.EMAIL_REMINDER_DEFAULT_TEXT,
				emailTemplate.getMessage() );
		
		emailTemplate = emailTemplates.getDefaultEmailTemplate( 
				EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER );
		Assert.assertNotNull(emailTemplate);
		Assert.assertEquals( EvalConstants.EMAIL_TEMPLATE_DEFAULT_CONSOLIDATED_REMINDER, 
				emailTemplate.getDefaultType() );
		Assert.assertEquals( EvalConstants.EMAIL_CONSOLIDATED_REMINDER_DEFAULT_TEXT,
				emailTemplate.getMessage() );
		
		emailTemplate = emailTemplates.getDefaultEmailTemplate( 
				EvalConstants.EMAIL_TEMPLATE_RESULTS );
		Assert.assertNotNull(emailTemplate);
		Assert.assertEquals( EvalConstants.EMAIL_TEMPLATE_DEFAULT_RESULTS, 
				emailTemplate.getDefaultType() );
		Assert.assertEquals( EvalConstants.EMAIL_RESULTS_DEFAULT_TEXT,
				emailTemplate.getMessage() );
		
		emailTemplate = emailTemplates.getDefaultEmailTemplate( 
				EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_SUBJECT );
		Assert.assertNotNull(emailTemplate);
		Assert.assertEquals( EvalConstants.EMAIL_TEMPLATE_DEFAULT_CONSOLIDATED_SUBJECT, 
				emailTemplate.getDefaultType() );
		Assert.assertEquals( EvalConstants.EMAIL_CONSOLIDATED_SUBJECT_DEFAULT_TEXT,
				emailTemplate.getMessage() );
		
		Assert.assertNotNull(EvalConstants.EMAIL_CREATED_DEFAULT_TEXT_FOOTER);
		Assert.assertNotNull(EvalConstants.EMAIL_CREATED_ADD_ITEMS_TEXT);
		Assert.assertNotNull(EvalConstants.EMAIL_CREATED_OPT_IN_TEXT);
		Assert.assertNotNull(EvalConstants.EMAIL_CREATED_OPT_OUT_TEXT);
		
		// test invalid constant causes failure
		try {
			emailTemplate = emailTemplates.getDefaultEmailTemplate( EvalTestDataLoad.INVALID_CONSTANT_STRING );
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

	}


    public void testGetEmailTemplate() {
        EvalEmailTemplate emailTemplate = null;

        // test getting the templates
        emailTemplate = emailTemplates.getEmailTemplate(etdl.evaluationActive.getId(), 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE );
        Assert.assertNotNull(emailTemplate);
        Assert.assertEquals( EvalConstants.EMAIL_AVAILABLE_DEFAULT_TEXT,
                emailTemplate.getMessage() );

        emailTemplate = emailTemplates.getEmailTemplate(etdl.evaluationActive.getId(), 
                EvalConstants.EMAIL_TEMPLATE_REMINDER );
        Assert.assertNotNull(emailTemplate);
        Assert.assertEquals( "Email Template 3", emailTemplate.getMessage() );

        // test invalid constant causes failure
        try {
            emailTemplate = emailTemplates.getEmailTemplate( EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.INVALID_CONSTANT_STRING );
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

    }


	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEmailsLogicImpl#canControlEmailTemplate(java.lang.String, java.lang.Long, int)}.
	 */
	public void testCanControlEmailTemplateStringLongInt() {
		// test valid email template control perms when none assigned
		Assert.assertTrue( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNewAdmin.getId(), 
				EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );
		Assert.assertTrue( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNewAdmin.getId(), 
				EvalConstants.EMAIL_TEMPLATE_REMINDER) );

		// user does not have perm for this eval
		Assert.assertFalse( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNewAdmin.getId(), 
				EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );
		Assert.assertFalse( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.USER_ID, etdl.evaluationNewAdmin.getId(), 
				EvalConstants.EMAIL_TEMPLATE_REMINDER) );

		// test when template has some assigned already
		Assert.assertTrue( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNew.getId(), 
				EvalConstants.EMAIL_TEMPLATE_REMINDER) );
		Assert.assertTrue( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNew.getId(), 
				EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );

		// test admin overrides perms
		Assert.assertTrue( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNew.getId(), 
				EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );

		// test not has permission
		Assert.assertFalse( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNewAdmin.getId(), 
				EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );
		Assert.assertFalse( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNew.getId(), 
				EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );
		Assert.assertFalse( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.USER_ID, etdl.evaluationNew.getId(), 
				EvalConstants.EMAIL_TEMPLATE_REMINDER) );

		// test cannot when evaluation is running (active+)
		Assert.assertFalse( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationActive.getId(), 
				EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );
		Assert.assertFalse( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationActive.getId(), 
				EvalConstants.EMAIL_TEMPLATE_REMINDER) );

		// check admin cannot override for running evals
		Assert.assertFalse( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationClosed.getId(), 
				EvalConstants.EMAIL_TEMPLATE_REMINDER) );
		Assert.assertFalse( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationClosed.getId(), 
				EvalConstants.EMAIL_TEMPLATE_REMINDER) );

		// check invalid evaluation id causes failure
		try {
			emailTemplates.canControlEmailTemplate(
					EvalTestDataLoad.ADMIN_USER_ID, 
					EvalTestDataLoad.INVALID_LONG_ID, 
					EvalConstants.EMAIL_TEMPLATE_REMINDER);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEmailsLogicImpl#canControlEmailTemplate(java.lang.String, java.lang.Long, java.lang.Long)}.
	 */
	public void testCanControlEmailTemplateStringLongLong() {
		// test valid email template control perms when none assigned
		Assert.assertTrue( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNew.getId(), 
				etdl.emailTemplate1.getId()) );
		Assert.assertTrue( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNew.getId(), 
				etdl.emailTemplate2.getId()) );
		Assert.assertTrue( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNew.getId(), 
				etdl.emailTemplate2.getId()) );

		// test not has permissions
		Assert.assertFalse( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNew.getId(), 
				etdl.emailTemplate1.getId()) );

		// test valid but active eval not allowed
		Assert.assertFalse( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationActive.getId(), 
				etdl.emailTemplate3.getId()) );

		// make sure admin cannot override for active eval
		Assert.assertFalse( emailTemplates.canControlEmailTemplate(
				EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationActive.getId(), 
				etdl.emailTemplate3.getId()) );

		// check invalid evaluation id causes failure
		try {
			emailTemplates.canControlEmailTemplate(
					EvalTestDataLoad.ADMIN_USER_ID, 
					EvalTestDataLoad.INVALID_LONG_ID, 
					etdl.emailTemplate1.getId() );
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

		// check invalid email template id causes failure
		try {
			emailTemplates.canControlEmailTemplate(
					EvalTestDataLoad.ADMIN_USER_ID, 
					etdl.evaluationNew.getId(), 
					EvalTestDataLoad.INVALID_LONG_ID );
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

		// check non-matching evaluation and template causes failure
		try {
			emailTemplates.canControlEmailTemplate(
					EvalTestDataLoad.ADMIN_USER_ID, 
					etdl.evaluationNew.getId(), 
					etdl.emailTemplate3.getId() );
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEmailsLogicImpl#sendEvalAvailableNotifications(java.lang.Long, boolean)}.
	 */
	public void testSendEvalAvailableNotifications() {
		//emailTemplates.sendEvalAvailableNotifications(etdl.evaluationActive.getId(), false);
		// TODO fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEmailsLogicImpl#sendEvalCreatedNotifications(java.lang.Long, boolean)}.
	 */
	public void testSendEvalCreatedNotifications() {
		//emailTemplates.sendEvalCreatedNotifications(etdl.evaluationActive.getId(), false);
		// TODO fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEmailsLogicImpl#sendEvalReminderNotifications(java.lang.Long, java.lang.String)}.
	 */
	public void testSendEvalReminderNotifications() {
		//emailTemplates.sendEvalReminderNotifications(etdl.evaluationActive.getId(), EvalConstants.EMAIL_INCLUDE_NONTAKERS);
		// TODO fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEmailsLogicImpl#sendEvalResultsNotifications(java.lang.Long, boolean, boolean)}.
	 */
	public void testSendEvalResultsNotifications() {
		//emailTemplates.sendEvalResultsNotifications(etdl.evaluationActive.getId(), false, false);
		// TODO fail("Not yet implemented");
	}

}

/**
 * $Id$
 * $URL$
 * EvalEmailsLogicImplTest.java - evaluation - Dec 29, 2006 10:07:31 AM - azeckoski
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

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.mocks.MockEvalExternalLogic;


/**
 * EvalEmailsLogicImpl test class
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalEmailsLogicImplTest extends BaseTestEvalLogic {

	protected EvalEmailsLogicImpl emailsLogic;
	private EvalSettings settings;
	private MockEvalExternalLogic externalLogicMock;

	// run this before each test starts
	protected void onSetUpBeforeTransaction() throws Exception {
	   super.onSetUpBeforeTransaction();

		// load up any other needed spring beans
		settings = (EvalSettings) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalSettings");
		if (settings == null) {
			throw new NullPointerException("EvalSettings could not be retrieved from spring evalGroupId");
		}

      EvalEvaluationService evaluationService = (EvalEvaluationService) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalEvaluationService");
      if (evaluationService == null) {
         throw new NullPointerException("EvalEvaluationService could not be retrieved from spring context");
      }

      externalLogicMock = (MockEvalExternalLogic) externalLogic;
      
		// create and setup the object to be tested
		emailsLogic = new EvalEmailsLogicImpl();
		emailsLogic.setCommonLogic(commonLogic);
		emailsLogic.setEvaluationService(evaluationService);
		emailsLogic.setSettings(settings);

	}

	/**
	 * ADD unit tests below here, use testMethod as the name of the unit test,
	 * Note that if a method is overloaded you should include the arguments in the
	 * test name like so: testMethodClassInt (for method(Class, int);
	 */


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#getFromEmailOrFail(org.sakaiproject.evaluation.model.EvalEvaluation)}.
    */
   public void testGetFromEmailOrFail() {
      String fromEmail = null;

      fromEmail = emailsLogic.getFromEmailOrFail(etdl.evaluationActive);
      assertNotNull(fromEmail);
      assertEquals(EvalTestDataLoad.EVAL_FROM_EMAIL, fromEmail);

      fromEmail = emailsLogic.getFromEmailOrFail(etdl.evaluationNew);
      assertNotNull(fromEmail);
      assertEquals("helpdesk@institution.edu", fromEmail);

      // should not throw exception unless no from email can be found
      settings.set(EvalSettings.FROM_EMAIL_ADDRESS, null);
      try {
         emailsLogic.getFromEmailOrFail(etdl.evaluationNew);
         fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         assertNotNull(e);
         //fail("Exception: " + e.getMessage()); // see why failing
      }

      settings.set(EvalSettings.FROM_EMAIL_ADDRESS, "helpdesk@email.com");
   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#getEmailTemplateOrFail(java.lang.String, java.lang.Long)}.
    */
   public void testGetEmailTemplateOrFail() {
      EvalEmailTemplate template = null;

      // test getting templates from evals
      template = emailsLogic.getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_AVAILABLE, etdl.evaluationNew.getId());
      assertNotNull(template);
      assertEquals(etdl.emailTemplate1.getId(), template.getId());

      template = emailsLogic.getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_REMINDER, etdl.evaluationActive.getId());
      assertNotNull(template);
      assertEquals(etdl.emailTemplate3.getId(), template.getId());

      // test getting templates without evals
      template = emailsLogic.getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_AVAILABLE, null);
      assertNotNull(template);
      assertEquals(EvalConstants.EMAIL_TEMPLATE_AVAILABLE, template.getDefaultType());

      // test getting non-eval template from evals
      template = emailsLogic.getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_CREATED, etdl.evaluationActive.getId());
      assertNotNull(template);
      assertEquals(EvalConstants.EMAIL_TEMPLATE_CREATED, template.getDefaultType());

      // exception if eval id is invalid
      try {
         emailsLogic.getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_AVAILABLE, EvalTestDataLoad.INVALID_LONG_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
         //fail("Exception: " + e.getMessage()); // see why failing
      }
      
      // exception if invalid template type
      try {
         emailsLogic.getEmailTemplateOrFail("XXXXXXXXXXXXX", etdl.evaluationActive.getId());
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
         //fail("Exception: " + e.getMessage()); // see why failing
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#sendEvalCreatedNotifications(java.lang.Long, boolean)}.
    */
   public void testSendEvalCreatedNotifications() {
      String[] sentEmails = null;

      // test send to just the evaluatees
      externalLogicMock.resetEmailsSentCounter();
      sentEmails = emailsLogic.sendEvalCreatedNotifications(etdl.evaluationViewable.getId(), false);
      assertNotNull(sentEmails);
      assertEquals(1, sentEmails.length);
      assertEquals(1, externalLogicMock.getNumEmailsSent());

      // test send to evaluatees and owner
      externalLogicMock.resetEmailsSentCounter();
      sentEmails = emailsLogic.sendEvalCreatedNotifications(etdl.evaluationViewable.getId(), true);
      assertNotNull(sentEmails);
      assertEquals(2, sentEmails.length);
      assertEquals(2, externalLogicMock.getNumEmailsSent());

      // test that invalid evaluation id causes failure
      try {
         emailsLogic.sendEvalCreatedNotifications(EvalTestDataLoad.INVALID_LONG_ID, false);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
         //fail("Exception: " + e.getMessage()); // see why failing
      }
   }

   /**
	 * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#sendEvalAvailableNotifications(java.lang.Long, boolean)}.
	 */
	public void testSendEvalAvailableNotifications() {
      String[] sentEmails = null;

      // test send to just the evaluators
      externalLogicMock.resetEmailsSentCounter();
      sentEmails = emailsLogic.sendEvalAvailableNotifications(etdl.evaluationNewAdmin.getId(), false);
		assertNotNull(sentEmails);
      assertEquals(2, sentEmails.length);
      assertEquals(2, externalLogicMock.getNumEmailsSent());

      // test send to evaluators and evaluatees
      externalLogicMock.resetEmailsSentCounter();
      sentEmails = emailsLogic.sendEvalAvailableNotifications(etdl.evaluationNewAdmin.getId(), true);
      assertNotNull(sentEmails);
      assertEquals(3, sentEmails.length);
      assertEquals(3, externalLogicMock.getNumEmailsSent());

      // test that invalid evaluation id causes failure
      try {
         emailsLogic.sendEvalAvailableNotifications(EvalTestDataLoad.INVALID_LONG_ID, false);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
         //fail("Exception: " + e.getMessage()); // see why failing
      }
	}

	// FIXME add remaining tests here

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#sendEvalAvailableGroupNotification(java.lang.Long, java.lang.String)}.
    */
   public void testSendEvalAvailableGroupNotification() {
      // TODO fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#sendEvalReminderNotifications(java.lang.Long, java.lang.String)}.
    */
   public void testSendEvalReminderNotifications() {
   // TODO fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#sendEvalResultsNotifications(java.lang.Long, boolean, boolean, java.lang.String)}.
    */
   public void testSendEvalResultsNotifications() {
   // TODO fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#makeEmailMessage(java.lang.String, String, org.sakaiproject.evaluation.model.EvalEvaluation, org.sakaiproject.evaluation.logic.model.EvalGroup, java.util.Map)}.
    */
   public void testMakeEmailMessage() {
   // TODO fail("Not yet implemented");
   }

}

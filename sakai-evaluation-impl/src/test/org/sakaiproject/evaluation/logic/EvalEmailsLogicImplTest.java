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
import org.junit.Ignore;
import org.junit.Test;
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
	@Before
	public void onSetUpBeforeTransaction() throws Exception {
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
	@Test
   public void testGetFromEmailOrFail() {
      String fromEmail;

      fromEmail = emailsLogic.getFromEmailOrFail(etdl.evaluationActive);
      Assert.assertNotNull(fromEmail);
      Assert.assertEquals(EvalTestDataLoad.EVAL_FROM_EMAIL, fromEmail);

      fromEmail = emailsLogic.getFromEmailOrFail(etdl.evaluationNew);
      Assert.assertNotNull(fromEmail);
      Assert.assertEquals("helpdesk@institution.edu", fromEmail);

      // should not throw exception unless no from email can be found
      settings.set(EvalSettings.FROM_EMAIL_ADDRESS, null);
      try {
         emailsLogic.getFromEmailOrFail(etdl.evaluationNew);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
         //Assert.fail("Exception: " + e.getMessage()); // see why Assert.failing
      }

      settings.set(EvalSettings.FROM_EMAIL_ADDRESS, "helpdesk@email.com");
   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#getEmailTemplateOrFail(java.lang.String, java.lang.Long)}.
    */
	@Test
   public void testGetEmailTemplateOrFail() {
      EvalEmailTemplate template;

      // test getting templates from evals
      template = emailsLogic.getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_AVAILABLE, etdl.evaluationNew.getId());
      Assert.assertNotNull(template);
      Assert.assertEquals(etdl.emailTemplate1.getId(), template.getId());

      template = emailsLogic.getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_REMINDER, etdl.evaluationActive.getId());
      Assert.assertNotNull(template);
      Assert.assertEquals(etdl.emailTemplate3.getId(), template.getId());

      template = emailsLogic.getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_SUBMITTED, etdl.evaluationActive.getId());
      Assert.assertNotNull(template);
      Assert.assertEquals(etdl.emailTemplate6.getId(), template.getId());

      // test getting templates without evals
      template = emailsLogic.getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_AVAILABLE, null);
      Assert.assertNotNull(template);
      Assert.assertEquals(EvalConstants.EMAIL_TEMPLATE_AVAILABLE, template.getDefaultType());

      // test getting non-eval template from evals
      template = emailsLogic.getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_CREATED, etdl.evaluationActive.getId());
      Assert.assertNotNull(template);
      Assert.assertEquals(EvalConstants.EMAIL_TEMPLATE_CREATED, template.getDefaultType());

      // exception if eval id is invalid
      try {
         emailsLogic.getEmailTemplateOrFail(EvalConstants.EMAIL_TEMPLATE_AVAILABLE, EvalTestDataLoad.INVALID_LONG_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
         //Assert.fail("Exception: " + e.getMessage()); // see why Assert.failing
      }
      
      // exception if invalid template type
      try {
         emailsLogic.getEmailTemplateOrFail("XXXXXXXXXXXXX", etdl.evaluationActive.getId());
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
         //Assert.fail("Exception: " + e.getMessage()); // see why Assert.failing
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#sendEvalCreatedNotifications(java.lang.Long, boolean)}.
    */
	@Ignore
	@Test
   public void testSendEvalCreatedNotifications() {
      String[] sentEmails;

      // test send to just the evaluatees
      externalLogicMock.resetEmailsSentCounter();
      sentEmails = emailsLogic.sendEvalCreatedNotifications(etdl.evaluationViewable.getId(), false);
      Assert.assertNotNull(sentEmails);
      Assert.assertEquals(1, sentEmails.length);
      Assert.assertEquals(1, externalLogicMock.getNumEmailsSent());

      // test send to evaluatees and owner
      externalLogicMock.resetEmailsSentCounter();
      sentEmails = emailsLogic.sendEvalCreatedNotifications(etdl.evaluationViewable.getId(), true);
      Assert.assertNotNull(sentEmails);
      Assert.assertEquals(2, sentEmails.length);
      Assert.assertEquals(2, externalLogicMock.getNumEmailsSent());

      // test that invalid evaluation id causes Assert.failure
      try {
         emailsLogic.sendEvalCreatedNotifications(EvalTestDataLoad.INVALID_LONG_ID, false);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
         //Assert.fail("Exception: " + e.getMessage()); // see why Assert.failing
      }
   }

   /**
	 * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#sendEvalAvailableNotifications(java.lang.Long, boolean)}.
	 */
	@Ignore
	@Test
	public void testSendEvalAvailableNotifications() {
      String[] sentEmails;

      // test send to just the evaluators
      externalLogicMock.resetEmailsSentCounter();
      sentEmails = emailsLogic.sendEvalAvailableNotifications(etdl.evaluationNewAdmin.getId(), false);
		Assert.assertNotNull(sentEmails);
      Assert.assertEquals(2, sentEmails.length);
      Assert.assertEquals(2, externalLogicMock.getNumEmailsSent());

      // test send to evaluators and evaluatees
      externalLogicMock.resetEmailsSentCounter();
      sentEmails = emailsLogic.sendEvalAvailableNotifications(etdl.evaluationNewAdmin.getId(), true);
      Assert.assertNotNull(sentEmails);
      Assert.assertEquals(3, sentEmails.length);
      Assert.assertEquals(3, externalLogicMock.getNumEmailsSent());

      // test that invalid evaluation id causes Assert.failure
      try {
         emailsLogic.sendEvalAvailableNotifications(EvalTestDataLoad.INVALID_LONG_ID, false);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
         //Assert.fail("Exception: " + e.getMessage()); // see why Assert.failing
      }
	}

	// FIXME add remaining tests here

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#sendEvalAvailableGroupNotification(java.lang.Long, java.lang.String)}.
    */
	@Test
   public void testSendEvalAvailableGroupNotification() {
      // TODO Assert.fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#sendEvalReminderNotifications(java.lang.Long, java.lang.String)}.
    */
	@Test
   public void testSendEvalReminderNotifications() {
   // TODO Assert.fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#sendEvalResultsNotifications(java.lang.Long, boolean, boolean, java.lang.String)}.
    */
	@Test
   public void testSendEvalResultsNotifications() {
   // TODO Assert.fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#makeEmailMessage(java.lang.String, String, org.sakaiproject.evaluation.model.EvalEvaluation, org.sakaiproject.evaluation.logic.model.EvalGroup)}.
    */
	@Test
   public void testMakeEmailMessage() {
   // TODO Assert.fail("Not yet implemented");
   }

}

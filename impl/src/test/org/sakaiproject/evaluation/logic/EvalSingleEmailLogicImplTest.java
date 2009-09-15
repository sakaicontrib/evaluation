package org.sakaiproject.evaluation.logic;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.constant.EvalEmailConstants;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.mocks.MockEvalExternalLogic;

public class EvalSingleEmailLogicImplTest  extends BaseTestEvalLogic {
	
	protected EvalSingleEmailLogicImpl emailLogic;
	private EvalSettings settings;
	private MockEvalExternalLogic externalLogicMock;
	private EvalEvaluationService evaluationService;
	
	// run this before each test starts
	protected void onSetUpBeforeTransaction() throws Exception {
	   super.onSetUpBeforeTransaction();

		// load up any other needed spring beans
		settings = (EvalSettings) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalSettings");
		if (settings == null) {
			throw new NullPointerException("EvalSettings could not be retrieved from spring evalGroupId");
		}

      evaluationService = (EvalEvaluationService) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalEvaluationService");
      if (evaluationService == null) {
         throw new NullPointerException("EvalEvaluationService could not be retrieved from spring context");
      }

      	externalLogicMock = (MockEvalExternalLogic) externalLogic;
      
		// create and setup the object to be tested
		emailLogic = new EvalSingleEmailLogicImpl();
		emailLogic.setCommonLogic(commonLogic);
		emailLogic.setEvaluationService(evaluationService);
		emailLogic.setSettings(settings);
		emailLogic.setDao(evaluationDao);
	}
	
	public void testIsSingleEmailEnabled() {
		// test ENABLED
		settings.set(EvalSettings.EMAIL_SEND_QUEUED_REPEAT_INTERVAL, new Integer(5));
		settings.set(EvalSettings.EMAIL_SEND_QUEUED_START_INTERVAL, new Integer(2));
		settings.set(EvalSettings.EMAIL_SEND_QUEUED_ENABLED, new Boolean(true));
		assertTrue(emailLogic.isSingleEmailEnabled());
		// test NOT ENABLED due to EMAIL_SEND_QUEUED_REPEAT_INTERVAL setting
		settings.set(EvalSettings.EMAIL_SEND_QUEUED_REPEAT_INTERVAL, new Integer(0));
		settings.set(EvalSettings.EMAIL_SEND_QUEUED_START_INTERVAL, new Integer(2));
		settings.set(EvalSettings.EMAIL_SEND_QUEUED_ENABLED, new Boolean(true));
		assertFalse(emailLogic.isSingleEmailEnabled());
		// test NOT ENABLED due to EMAIL_SEND_QUEUED_START_INTERVAL setting
		settings.set(EvalSettings.EMAIL_SEND_QUEUED_START_INTERVAL, new Integer(0));
		settings.set(EvalSettings.EMAIL_SEND_QUEUED_REPEAT_INTERVAL, new Integer(5));
		settings.set(EvalSettings.EMAIL_SEND_QUEUED_ENABLED, new Boolean(true));
		assertFalse(emailLogic.isSingleEmailEnabled());
		// test NOT ENABLED due to EMAIL_SEND_QUEUED_ENABLED setting
		settings.set(EvalSettings.EMAIL_SEND_QUEUED_ENABLED, new Boolean(false));
		settings.set(EvalSettings.EMAIL_SEND_QUEUED_REPEAT_INTERVAL, new Integer(5));
		settings.set(EvalSettings.EMAIL_SEND_QUEUED_START_INTERVAL, new Integer(2));
		assertFalse(emailLogic.isSingleEmailEnabled());
		// test NOT ENABLED due to EMAIL_SEND_QUEUED_REPEAT_INTERVAL, EMAIL_SEND_QUEUED_START_INTERVAL, EMAIL_SEND_QUEUED_ENABLED  settings
		settings.set(EvalSettings.EMAIL_SEND_QUEUED_REPEAT_INTERVAL, new Integer(0));
		settings.set(EvalSettings.EMAIL_SEND_QUEUED_START_INTERVAL, new Integer(0));
		settings.set(EvalSettings.EMAIL_SEND_QUEUED_ENABLED, new Boolean(false));
		assertFalse(emailLogic.isSingleEmailEnabled());
	}
	
	public void testIsSingleEmailRequired() {
		// test EMAIL_DELIVERY_NONE and LOG_EMAIL_RECIPIENTS false
		settings.set(EvalSettings.EMAIL_DELIVERY_OPTION, EvalConstants.EMAIL_DELIVERY_NONE);
		settings.set(EvalSettings.LOG_EMAIL_RECIPIENTS, new Boolean(false));
		assertFalse(emailLogic.isSingleEmailRequired());
		// test EMAIL_DELIVERY_NONE and LOG_EMAIL_RECIPIENTS true
		settings.set(EvalSettings.EMAIL_DELIVERY_OPTION, EvalConstants.EMAIL_DELIVERY_NONE);
		settings.set(EvalSettings.LOG_EMAIL_RECIPIENTS, new Boolean(true));
		assertTrue(emailLogic.isSingleEmailRequired());
		// test EMAIL_DELIVERY_LOG and LOG_EMAIL_RECIPIENTS false
		settings.set(EvalSettings.EMAIL_DELIVERY_OPTION, EvalConstants.EMAIL_DELIVERY_LOG);
		settings.set(EvalSettings.LOG_EMAIL_RECIPIENTS, new Boolean(false));
		assertTrue(emailLogic.isSingleEmailRequired());
		// test EMAIL_DELIVERY_LOG and LOG_EMAIL_RECIPIENTS true
		settings.set(EvalSettings.EMAIL_DELIVERY_OPTION, EvalConstants.EMAIL_DELIVERY_LOG);
		settings.set(EvalSettings.LOG_EMAIL_RECIPIENTS, new Boolean(true));
		assertTrue(emailLogic.isSingleEmailRequired());
		// test EMAIL_DELIVERY_SEND and LOG_EMAIL_RECIPIENTS false
		settings.set(EvalSettings.EMAIL_DELIVERY_OPTION, EvalConstants.EMAIL_DELIVERY_SEND);
		settings.set(EvalSettings.LOG_EMAIL_RECIPIENTS, new Boolean(false));
		assertTrue(emailLogic.isSingleEmailRequired());
		// test EMAIL_DELIVERY_SENT and LOG_EMAIL_RECIPIENTS true
		settings.set(EvalSettings.EMAIL_DELIVERY_OPTION, EvalConstants.EMAIL_DELIVERY_SEND);
		settings.set(EvalSettings.LOG_EMAIL_RECIPIENTS, new Boolean(true));
		assertTrue(emailLogic.isSingleEmailRequired());
	} 
	
	public void testIsLockHeld() {
		// test EMAIL_LOCK
		String lockId = EvalConstants.EMAIL_LOCK_PREFIX + 1;
		String lockType = EvalConstants.EMAIL_LOCK_PREFIX;
		assertFalse(emailLogic.isLockHeld(lockType, "executerId"));
		evaluationDao.obtainLock(lockId, "executerId", 100000l);
		assertTrue(emailLogic.isLockHeld(lockType, "executerId"));
		evaluationDao.releaseLock(lockId, "executerId");
		assertFalse(emailLogic.isLockHeld(lockType, "executerId"));
		// test GROUP_LOCK
		lockId = EvalConstants.GROUP_LOCK_PREFIX + 1;
		lockType = EvalConstants.GROUP_LOCK_PREFIX;
		assertFalse(emailLogic.isLockHeld(lockType, "executerId"));
		evaluationDao.obtainLock(lockId, "executerId", 100000l);
		assertTrue(emailLogic.isLockHeld(lockType, "executerId"));
		evaluationDao.releaseLock(lockId, "executerId");
		assertFalse(emailLogic.isLockHeld(lockType, "executerId"));
	}
	
	public void testGetLockNameSuffix() {
		// sets = EMAIL_LOCKS_SIZE 0, 10, 25, 50, 100, 250, 500, 750, 1000
		// start = 0 ... EMAIL_LOCKS_SIZE-1
		// test EMAIL_LOCKS_SIZE 0
		settings.set(EvalSettings.EMAIL_LOCKS_SIZE, new Integer(0));
		int sets = ((Integer)settings.get(EvalSettings.EMAIL_LOCKS_SIZE)).intValue();
		int start = 0;
		Integer suffix = emailLogic.getLockNameSuffix(start, sets);
		assertEquals(0, suffix.intValue());
		// test EMAIL_LOCKS_SIZE 10
		settings.set(EvalSettings.EMAIL_LOCKS_SIZE, new Integer(10));
		sets = ((Integer)settings.get(EvalSettings.EMAIL_LOCKS_SIZE)).intValue();
		start = 0;
		suffix = emailLogic.getLockNameSuffix(start, sets);
		assertEquals(0, suffix.intValue());
		settings.set(EvalSettings.EMAIL_LOCKS_SIZE, new Integer(10));
		sets = ((Integer)settings.get(EvalSettings.EMAIL_LOCKS_SIZE)).intValue();
		start = 9;
		suffix = emailLogic.getLockNameSuffix(start, sets);
		assertEquals(9, suffix.intValue());
		// test EMAIL_LOCKS_SIZE 1000
		settings.set(EvalSettings.EMAIL_LOCKS_SIZE, new Integer(1000));
		sets = ((Integer)settings.get(EvalSettings.EMAIL_LOCKS_SIZE)).intValue();
		start = 0;
		suffix = emailLogic.getLockNameSuffix(start, sets);
		assertEquals(0, suffix.intValue());
		settings.set(EvalSettings.EMAIL_LOCKS_SIZE, new Integer(1000));
		sets = ((Integer)settings.get(EvalSettings.EMAIL_LOCKS_SIZE)).intValue();
		start = 500;
		suffix = emailLogic.getLockNameSuffix(start, sets);
		assertEquals(500, suffix.intValue());
		settings.set(EvalSettings.EMAIL_LOCKS_SIZE, new Integer(1000));
		sets = ((Integer)settings.get(EvalSettings.EMAIL_LOCKS_SIZE)).intValue();
		start = 999;
		suffix = emailLogic.getLockNameSuffix(start, sets);
		assertEquals(999, suffix.intValue());
	}
	
	public void testSaveEmail() {
		EvalEmailTemplate emailTemplate = new EvalEmailTemplate(etdl.MAINT_USER_ID, EvalConstants.EMAIL_TEMPLATE_AVAILABLE, 
				EvalEmailConstants.SINGLE_EMAIL_AVAILABLE_DEFAULT_SUBJECT, EvalEmailConstants.SINGLE_EMAIL_AVAILABLE_DEFAULT_TEXT);
		evaluationDao.save(emailTemplate);
		EvalUser user = externalLogicMock.getEvalUserById(etdl.USER_ID);
		String url = user.displayName + "'s MyWorkspace URL";
		String systemUrl = "localhost:8080/portal/...";
		String earliest = "Sept 1, 2009";
		String emailLock = EvalConstants.EMAIL_LOCK_PREFIX + 1;
		String toolTitle = "Teaching Questionnaires";
		// test that duplicates are caught before unique constraint violation
		emailLogic.saveEmail(user, url, systemUrl, earliest, emailTemplate, emailLock, toolTitle);
		emailLogic.saveEmail(user, url, systemUrl, earliest, emailTemplate, emailLock, toolTitle);
		emailLogic.saveEmail(user, url, systemUrl, earliest, emailTemplate, emailLock, toolTitle);
		// 5 are preloaded
		assertEquals(6, evaluationDao.countQueuedEmail(Boolean.FALSE));
		user = externalLogicMock.getEvalUserById(etdl.ADMIN_USER_ID);
		emailLogic.saveEmail(user, url, systemUrl, earliest, emailTemplate, emailLock, toolTitle);
		assertEquals(7, evaluationDao.countQueuedEmail(Boolean.FALSE));
		user = externalLogicMock.getEvalUserById(etdl.MAINT_USER_ID);
		emailLogic.saveEmail(user, url, systemUrl, earliest, emailTemplate, emailLock, toolTitle);
		user = externalLogicMock.getEvalUserById(etdl.STUDENT_USER_ID);
		emailLogic.saveEmail(user, url, systemUrl, earliest, emailTemplate, emailLock, toolTitle);
		// test countQueuedEmail
		assertEquals(9, evaluationDao.countQueuedEmail(Boolean.FALSE));
		assertEquals(0, evaluationDao.countQueuedEmail(Boolean.TRUE));
		assertEquals(9, evaluationDao.countQueuedEmail(null));
	}
}

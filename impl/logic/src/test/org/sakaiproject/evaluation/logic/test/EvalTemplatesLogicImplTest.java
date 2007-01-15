/******************************************************************************
 * EvalTemplatesLogicImplTest.java - created by aaronz@vt.edu on Dec 31, 2006
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
import java.util.List;

import junit.framework.Assert;

import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl;
import org.sakaiproject.evaluation.logic.test.stubs.EvalExternalLogicStub;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestData;
import org.springframework.test.AbstractTransactionalSpringContextTests;


/**
 * Test class for EvalTemplatesLogicImpl
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalTemplatesLogicImplTest extends AbstractTransactionalSpringContextTests {

	protected EvalTemplatesLogicImpl templates;

	private EvaluationDao evaluationDao;
	private EvalTestDataLoad etdl;

	protected String[] getConfigLocations() {
		// point to the needed spring config files, must be on the classpath
		// (add component/src/webapp/WEB-INF to the build path in Eclipse),
		// they also need to be referenced in the project.xml file
		return new String[] {"hibernate-test.xml", "spring-hibernate.xml"};
	}

	// run this before each test starts
	protected void onSetUpBeforeTransaction() throws Exception {
		// load the spring created dao class bean from the Spring Application Context
		evaluationDao = (EvaluationDao) applicationContext.getBean("org.sakaiproject.evaluation.dao.EvaluationDao");
		if (evaluationDao == null) {
			throw new NullPointerException("EvaluationDao could not be retrieved from spring context");
		}

		// check the preloaded data
		Assert.assertTrue("Error preloading data", evaluationDao.countAll(EvalScale.class) > 0);

		// check the preloaded test data
		Assert.assertTrue("Error preloading test data", evaluationDao.countAll(EvalEvaluation.class) > 0);

		PreloadTestData ptd = (PreloadTestData) applicationContext.getBean("org.sakaiproject.evaluation.test.PreloadTestData");
		if (ptd == null) {
			throw new NullPointerException("PreloadTestData could not be retrieved from spring context");
		}

		// get test objects
		etdl = ptd.getEtdl();

		// load up any other needed spring beans

		// setup the mock objects if needed

		// create and setup the object to be tested
		templates = new EvalTemplatesLogicImpl();
		templates.setDao(evaluationDao);
		templates.setExternalLogic( new EvalExternalLogicStub() );

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
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#getTemplateById(java.lang.Long)}.
	 */
	public void testGetTemplateById() {
		EvalTemplate template = null;

		// test getting valid templates by id
		template = templates.getTemplateById( etdl.templateAdmin.getId() );
		Assert.assertNotNull(template);
		Assert.assertEquals(etdl.templateAdmin.getId(), template.getId());

		template = templates.getTemplateById( etdl.templatePublic.getId() );
		Assert.assertNotNull(template);
		Assert.assertEquals(etdl.templatePublic.getId(), template.getId());

		// test get eval by invalid id returns null
		template = templates.getTemplateById( EvalTestDataLoad.INVALID_LONG_ID );
		Assert.assertNull(template);
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#saveTemplate(org.sakaiproject.evaluation.model.EvalTemplate, java.lang.String)}.
	 */
	public void testSaveTemplate() {
		String test_title = "test template title";
		// test saving a valid template
		templates.saveTemplate( new EvalTemplate( new Date(), 
				EvalTestDataLoad.MAINT_USER_ID, test_title, 
				EvalConstants.SHARING_PRIVATE, 
				EvalTestDataLoad.NOT_EXPERT), 
				EvalTestDataLoad.MAINT_USER_ID);

		// test saving valid template locked
		templates.saveTemplate( new EvalTemplate( new Date(), 
				EvalTestDataLoad.ADMIN_USER_ID, "admin test template", 
				"desc", EvalConstants.SHARING_PRIVATE, EvalTestDataLoad.EXPERT, 
				"expert desc", null, null, EvalTestDataLoad.LOCKED), 
				EvalTestDataLoad.ADMIN_USER_ID);

		// test user without perms cannot create template
		try {
			templates.saveTemplate( new EvalTemplate( new Date(), 
					EvalTestDataLoad.USER_ID, "user test title 1", 
					EvalConstants.SHARING_PRIVATE, 
					EvalTestDataLoad.NOT_EXPERT), 
					EvalTestDataLoad.USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (SecurityException e) {
			Assert.assertNotNull(e);
		}

		// fetch templates to work with (for editing tests)
		EvalTemplate testTemplate1 = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, 
				etdl.templatePublicUnused.getId());
		EvalTemplate testTemplate2 = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, 
				etdl.templateUnused.getId());
		EvalTemplate testTemplate3 = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, 
				etdl.templatePublic.getId());
		EvalTemplate testTemplate4 = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, 
				etdl.templateUserUnused.getId());

		// test editing unlocked template
		testTemplate2.setDescription("something maint user new");
		templates.saveTemplate( testTemplate2, 
				EvalTestDataLoad.MAINT_USER_ID);

		// TODO - CANNOT RUN THIS TEST FOR NOW BECAUSE OF HIBERNATE
//		// test that LOCKED cannot be changed to FALSE on existing template
//		try {
//			testTemplate3.setLocked(Boolean.FALSE);
//			templates.saveTemplate( testTemplate3, 
//					EvalTestDataLoad.ADMIN_USER_ID);
//			Assert.fail("Should have thrown exception");
//		} catch (RuntimeException e) {
//			Assert.assertNotNull(e);
//			Assert.fail(e.getMessage()); // see why failing
//		}

		// test editing LOCKED template fails
		try {
			testTemplate3.setExpert(Boolean.FALSE);
			templates.saveTemplate( testTemplate3, 
					EvalTestDataLoad.ADMIN_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalStateException e) {
			Assert.assertNotNull(e);
		}

		// test admin can edit any template
		testTemplate1.setDescription("something admin new");
		templates.saveTemplate( testTemplate1, 
				EvalTestDataLoad.ADMIN_USER_ID);

		// test that editing unowned template causes permission failure
		try {
			testTemplate4.setDescription("something maint new");
			templates.saveTemplate( testTemplate4, 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (SecurityException e) {
			Assert.assertNotNull(e);
		}

		// test that setting sharing to PUBLIC as non-admin fails
		try {
			testTemplate1.setSharing(EvalConstants.SHARING_PUBLIC);
			templates.saveTemplate( testTemplate1, 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// test admin can set sharing to public
		testTemplate1.setSharing(EvalConstants.SHARING_PUBLIC);
		templates.saveTemplate( testTemplate1, 
				EvalTestDataLoad.ADMIN_USER_ID);

		// TODO - test cannot save template with no associated items

		// TODO - test saving template saves all associated items at same time

		// test cannot save 2 templates with same title
		try {
			templates.saveTemplate( new EvalTemplate( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, test_title, 
					EvalConstants.SHARING_PRIVATE, 
					EvalTestDataLoad.NOT_EXPERT), 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#deleteTemplate(java.lang.Long, java.lang.String)}.
	 */
	public void testDeleteTemplate() {
		// test removing template without permissions fails
		try {
			templates.deleteTemplate(etdl.templatePublicUnused.getId(), 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

		try {
			templates.deleteTemplate(etdl.templateUnused.getId(), 
					EvalTestDataLoad.USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

		// test removing locked template fails
		try {
			templates.deleteTemplate(etdl.templatePublic.getId(), 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

		try {
			templates.deleteTemplate(etdl.templateAdmin.getId(), 
					EvalTestDataLoad.ADMIN_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

		// test cannot remove expert template
		try {
			templates.deleteTemplate(etdl.templateUserUnused.getId(), 
					EvalTestDataLoad.ADMIN_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

		// test removing unused template OK
		templates.deleteTemplate(etdl.templateUnused.getId(), 
				EvalTestDataLoad.MAINT_USER_ID);
		Assert.assertNull( templates.getTemplateById(etdl.templateUnused.getId()) );

		templates.deleteTemplate(etdl.templatePublicUnused.getId(), 
				EvalTestDataLoad.ADMIN_USER_ID);
		Assert.assertNull( templates.getTemplateById(etdl.templatePublicUnused.getId()) );

		// test removing invalid template id fails
		try {
			templates.deleteTemplate(EvalTestDataLoad.INVALID_LONG_ID, 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#getTemplatesForUser(String, String)}.
	 */
	public void testGetTemplatesForUser() {
		List l = null;
		List ids = null;
		// NOTE: No preloaded public templates to take into account right now
		
		// test getting all templates for admin user (should include all templates)
		l = templates.getTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, null);
		Assert.assertNotNull(l);
		Assert.assertEquals(6, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templateAdmin.getId() ));
		Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
		Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));

		// test getting all templates for maint user
		l = templates.getTemplatesForUser(EvalTestDataLoad.MAINT_USER_ID, null);
		Assert.assertNotNull(l);
		Assert.assertEquals(3, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));
		Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
		Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));

		// test getting all templates for user
		l = templates.getTemplatesForUser(EvalTestDataLoad.USER_ID, null);
		Assert.assertNotNull(l);
		Assert.assertEquals(4, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
		Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));

		// test using SHARING_OWNER same as null (getting all templates)
		l = templates.getTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.SHARING_OWNER);
		Assert.assertNotNull(l);
		Assert.assertEquals(6, l.size());

		// test getting private templates for admin (admin should see all private)
		l = templates.getTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.SHARING_PRIVATE);
		Assert.assertNotNull(l);
		Assert.assertEquals(4, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templateAdmin.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));

		// test getting private templates for maint user
		l = templates.getTemplatesForUser(EvalTestDataLoad.MAINT_USER_ID, EvalConstants.SHARING_PRIVATE);
		Assert.assertNotNull(l);
		Assert.assertEquals(1, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));

		// test getting private templates for user
		l = templates.getTemplatesForUser(EvalTestDataLoad.USER_ID, EvalConstants.SHARING_PRIVATE);
		Assert.assertNotNull(l);
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));

		// test getting public templates only (normal user should see all)
		l = templates.getTemplatesForUser(EvalTestDataLoad.USER_ID, EvalConstants.SHARING_PUBLIC);
		Assert.assertNotNull(l);
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
		Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));

		// test getting invalid constant causes failure
		try {
			l = templates.getTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, 
					EvalTestDataLoad.INVALID_CONSTANT_STRING);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

	}


	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#canControlTemplate(java.lang.String, java.lang.Long)}.
	 */
	public void testCanControlTemplate() {
		// test can control owned templates
		Assert.assertTrue( templates.canControlTemplate( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.templatePublicUnused.getId() ) );
		Assert.assertTrue( templates.canControlTemplate( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.templateUnused.getId() ) );

		// test admin user can override perms
		Assert.assertTrue( templates.canControlTemplate( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.templateUnused.getId() ) );

		// test cannot control unowned templates
		Assert.assertFalse( templates.canControlTemplate( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.templatePublicUnused.getId() ) );
		Assert.assertFalse( templates.canControlTemplate( EvalTestDataLoad.USER_ID, 
				etdl.templateUnused.getId() ) );

		// test cannot control locked templates
		Assert.assertFalse( templates.canControlTemplate( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.templatePublic.getId() ) );
		Assert.assertFalse( templates.canControlTemplate( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.templateAdmin.getId() ) );

		// test invalid template id causes failure
		try {
			templates.canControlTemplate( EvalTestDataLoad.MAINT_USER_ID, 
					EvalTestDataLoad.INVALID_LONG_ID );
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#canCreateTemplate(java.lang.String)}.
	 */
	public void testCanCreateTemplate() {
		// test admin can create templates
		Assert.assertTrue( templates.canCreateTemplate(EvalTestDataLoad.ADMIN_USER_ID) );

		// test maint user can create templates (user with special perms)
		Assert.assertTrue( templates.canCreateTemplate(EvalTestDataLoad.MAINT_USER_ID) );

		// test normal user cannot create templates
		Assert.assertFalse( templates.canCreateTemplate(EvalTestDataLoad.USER_ID) );

		// test invalid user cannot create templates
		Assert.assertFalse( templates.canCreateTemplate(EvalTestDataLoad.INVALID_USER_ID) );

	}

}

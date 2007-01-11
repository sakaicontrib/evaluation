/******************************************************************************
 * EvalItemsLogicImplTest.java - created by aaronz@vt.edu
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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl;
import org.sakaiproject.evaluation.logic.test.stubs.EvalExternalLogicStub;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestData;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * Test class for EvalItemsLogicImpl
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalItemsLogicImplTest extends AbstractTransactionalSpringContextTests {

	protected EvalItemsLogicImpl items;

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
		items = new EvalItemsLogicImpl();
		items.setDao(evaluationDao);
		items.setExternalLogic( new EvalExternalLogicStub() );

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


	public void testPreloadedData() {
		// this test is just making sure that hibernate is actually linking the items
		// to the templates the way we think it is
		List l = null;
		List ids = null;

		Assert.assertEquals(10, evaluationDao.countAll(EvalTemplateItem.class) );

		EvalTemplate template = (EvalTemplate) 
			evaluationDao.findById(EvalTemplate.class, etdl.templateAdmin.getId());

		// No longer supporting this type of linkage between templates and items
//		Set items = template.getItems();
//		Assert.assertNotNull( items );
//		Assert.assertEquals(3, items.size());

		Set tItems = template.getTemplateItems();
		Assert.assertNotNull( tItems );
		Assert.assertEquals(3, tItems.size());
		ids = EvalTestDataLoad.makeIdList(tItems);
		Assert.assertTrue(ids.contains( etdl.templateItem2A.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateItem3A.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateItem5A.getId() ));
		
		EvalItem item = (EvalItem) evaluationDao.findById(EvalItem.class, etdl.item1.getId());
		Set itItems = item.getTemplateItems();
		Assert.assertNotNull( itItems );
		Assert.assertEquals(2, itItems.size());
		ids = EvalTestDataLoad.makeIdList(itItems);
		Assert.assertTrue(ids.contains( etdl.templateItem1P.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateItem1User.getId() ));

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getItemById(java.lang.Long)}.
	 */
	public void testGetItemById() {
		EvalItem item = null;

		// test getting valid items by id
		item = items.getItemById( etdl.item1.getId() );
		Assert.assertNotNull(item);
		Assert.assertEquals(etdl.item1.getId(), item.getId());

		item = items.getItemById( etdl.item5.getId() );
		Assert.assertNotNull(item);
		Assert.assertEquals(etdl.item5.getId(), item.getId());

		// test get eval by invalid id returns null
		item = items.getItemById( EvalTestDataLoad.INVALID_LONG_ID );
		Assert.assertNull(item);
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#saveItem(org.sakaiproject.evaluation.model.EvalItem, java.lang.String)}.
	 */
	public void testSaveItem() {
//		 TODO fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#deleteItem(java.lang.Long, java.lang.String)}.
	 */
	public void testDeleteItem() {
//		 TODO fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getItemsForUser(java.lang.String, java.lang.String)}.
	 */
	public void testGetItemsForUser() {
//		 TODO fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getItemsForTemplate(java.lang.Long)}.
	 */
	public void testGetItemsForTemplate() {
//		 TODO fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getTemplateItemById(java.lang.Long)}.
	 */
	public void testGetTemplateItemById() {
		EvalTemplateItem templateItem = null;

		// test getting valid templateItems by id
		templateItem = items.getTemplateItemById( etdl.templateItem1P.getId() );
		Assert.assertNotNull(templateItem);
		Assert.assertEquals(etdl.templateItem1P.getId(), templateItem.getId());

		templateItem = items.getTemplateItemById( etdl.templateItem1User.getId() );
		Assert.assertNotNull(templateItem);
		Assert.assertEquals(etdl.templateItem1User.getId(), templateItem.getId());

		// test get eval by invalid id returns null
		templateItem = items.getTemplateItemById( EvalTestDataLoad.INVALID_LONG_ID );
		Assert.assertNull(templateItem);
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#saveTemplateItem(org.sakaiproject.evaluation.model.EvalTemplateItem, java.lang.String)}.
	 */
	public void testSaveTemplateItem() {
//		 TODO fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#deleteTemplateItem(java.lang.Long, java.lang.String)}.
	 */
	public void testDeleteTemplateItem() {
//		 TODO fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getTemplateItemsForTemplate(java.lang.Long)}.
	 */
	public void testGetTemplateItemsForTemplate() {
//		 TODO fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#canControlItem(java.lang.String, java.lang.Long)}.
	 */
	public void testCanControlItem() {
//		 TODO fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#canControlTemplateItem(java.lang.String, java.lang.Long)}.
	 */
	public void testCanControlTemplateItem() {
//		 TODO fail("Not yet implemented");
	}

}

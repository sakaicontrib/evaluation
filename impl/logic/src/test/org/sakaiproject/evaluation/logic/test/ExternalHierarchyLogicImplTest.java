/**
 * $Id: ExternalHierarchyLogicImplTest.java 1000 Dec 25, 2006 10:07:31 AM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * ExternalHierarchyLogicImplTest.java - evaluation - Sep 7, 2007 11:06:38 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.test;

import junit.framework.Assert;

import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.impl.ExternalHierarchyLogicImpl;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.test.PreloadTestData;
import org.springframework.test.AbstractTransactionalSpringContextTests;


/**
 * Test cases for the evaluation hierarchy service,
 * trying to decide how I can write these without remaking the entire hierarchy service,
 * I can't think of a way so I think this will have to use the testrunner probably
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ExternalHierarchyLogicImplTest extends AbstractTransactionalSpringContextTests {

   protected ExternalHierarchyLogicImpl hierarchyLogicImpl;

   private EvaluationDao evaluationDao;
//   private EvalTestDataLoad etdl;

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
//      etdl = ptd.getEtdl();

      // load up any other needed spring beans
      EvalSettings settings = (EvalSettings) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalSettings");
      if (settings == null) {
         throw new NullPointerException("EvalSettings could not be retrieved from spring evalGroupId");
      }

      // setup the mock objects if needed

      // create and setup the object to be tested
      hierarchyLogicImpl = new ExternalHierarchyLogicImpl();
      hierarchyLogicImpl.setDao(evaluationDao);
      //hierarchyLogicImpl.setHierarchyService(hierarchyService);

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
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.ExternalHierarchyLogicImpl#init()}.
    */
   public void testInit() {
//      fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.ExternalHierarchyLogicImpl#getRootLevelNode()}.
    */
   public void testGetRootLevelNode() {
//      fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.ExternalHierarchyLogicImpl#getNodeById(java.lang.String)}.
    */
   public void testGetNodeById() {
//      fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.ExternalHierarchyLogicImpl#addNode(java.lang.String)}.
    */
   public void testAddNode() {
//      fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.ExternalHierarchyLogicImpl#removeNode(java.lang.String)}.
    */
   public void testRemoveNode() {
//      fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.ExternalHierarchyLogicImpl#updateNodeData(java.lang.String, java.lang.String, java.lang.String)}.
    */
   public void testUpdateNodeData() {
//      fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.ExternalHierarchyLogicImpl#getChildNodes(java.lang.String, boolean)}.
    */
   public void testGetChildNodes() {
//      fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.ExternalHierarchyLogicImpl#setEvalGroupsForNode(java.lang.String, java.util.Set)}.
    */
   public void testSetEvalGroupsForNode() {
// TODO      fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.ExternalHierarchyLogicImpl#getEvalGroupsForNode(java.lang.String)}.
    */
   public void testGetEvalGroupsForNode() {
//    TODO      fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.ExternalHierarchyLogicImpl#countEvalGroupsForNodes(java.lang.String[])}.
    */
   public void testCountEvalGroupsForNodes() {
//    TODO      fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.ExternalHierarchyLogicImpl#getNodesAboveEvalGroup(java.lang.String)}.
    */
   public void testGetNodesAboveEvalGroup() {
// TODO     fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.ExternalHierarchyLogicImpl#assignUserNodePerm(java.lang.String, java.lang.String, java.lang.String)}.
    */
   public void testAssignUserNodePerm() {
//      fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.ExternalHierarchyLogicImpl#checkUserNodePerm(java.lang.String, java.lang.String, java.lang.String)}.
    */
   public void testCheckUserNodePerm() {
//      fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.ExternalHierarchyLogicImpl#getNodesForUserPerm(java.lang.String, java.lang.String)}.
    */
   public void testGetNodesForUserPerm() {
//      fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.ExternalHierarchyLogicImpl#getUserIdsForNodesPerm(java.lang.String[], java.lang.String)}.
    */
   public void testGetUserIdsForNodesPerm() {
//      fail("Not yet implemented");
   }

}

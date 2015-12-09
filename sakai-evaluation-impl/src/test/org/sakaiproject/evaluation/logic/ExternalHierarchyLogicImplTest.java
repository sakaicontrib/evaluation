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

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogicImpl;


/**
 * Test cases for the evaluation hierarchy service,
 * TODO trying to decide how I can write these without remaking the entire hierarchy service,
 * I can't think of a way so I think this will have to use the testrunner probably
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ExternalHierarchyLogicImplTest extends BaseTestEvalLogic {

   protected ExternalHierarchyLogicImpl hierarchyLogicImpl;
   // run this before each test starts
   @Before
   public void onSetUpBeforeTransaction() throws Exception {
      super.onSetUpBeforeTransaction();

      // load up any other needed spring beans

      // setup the mock objects if needed

      // create and setup the object to be tested
      hierarchyLogicImpl = new ExternalHierarchyLogicImpl();
      hierarchyLogicImpl.setDao(evaluationDao);
      //hierarchyLogicImpl.setHierarchyService(hierarchyService);

   }

   /**
    * ADD unit tests below here, use testMethod as the name of the unit test,
    * Note that if a method is overloaded you should include the arguments in the
    * test name like so: testMethodClassInt (for method(Class, int);
    */


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogicImpl#init()}.
    */
   @Test
   public void testInit() {
//      Assert.fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogicImpl#getRootLevelNode()}.
    */
   @Test
   public void testGetRootLevelNode() {
//      Assert.fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogicImpl#getNodeById(java.lang.String)}.
    */
   @Test
   public void testGetNodeById() {
//      Assert.fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogicImpl#addNode(java.lang.String)}.
    */
   @Test
   public void testAddNode() {
//      Assert.fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogicImpl#removeNode(java.lang.String)}.
    */
   @Test
   public void testRemoveNode() {
//      Assert.fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogicImpl#updateNodeData(java.lang.String, java.lang.String, java.lang.String)}.
    */
   @Test
   public void testUpdateNodeData() {
//      Assert.fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogicImpl#getChildNodes(java.lang.String, boolean)}.
    */
   @Test
   public void testGetChildNodes() {
//      Assert.fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogicImpl#setEvalGroupsForNode(java.lang.String, java.util.Set)}.
    */
   @Test
   public void testSetEvalGroupsForNode() {
// TODO      Assert.fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogicImpl#getEvalGroupsForNode(java.lang.String)}.
    */
   @Test
   public void testGetEvalGroupsForNode() {
//    TODO      Assert.fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogicImpl#countEvalGroupsForNodes(java.lang.String[])}.
    */
   @Test
   public void testCountEvalGroupsForNodes() {
//    TODO      Assert.fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogicImpl#getNodesAboveEvalGroup(java.lang.String)}.
    */
   @Test
   public void testGetNodesAboveEvalGroup() {
// TODO     Assert.fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogicImpl#assignUserNodePerm(java.lang.String, java.lang.String, java.lang.String)}.
    */
   @Test
   public void testAssignUserNodePerm() {
//      Assert.fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogicImpl#checkUserNodePerm(java.lang.String, java.lang.String, java.lang.String)}.
    */
   @Test
   public void testCheckUserNodePerm() {
//      Assert.fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogicImpl#getNodesForUserPerm(java.lang.String, java.lang.String)}.
    */
   @Test
   public void testGetNodesForUserPerm() {
//      Assert.fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogicImpl#getUserIdsForNodesPerm(java.lang.String[], java.lang.String)}.
    */
   @Test
   public void testGetUserIdsForNodesPerm() {
//      Assert.fail("Not yet implemented");
   }

}

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
package org.sakaiproject.evaluation.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;

import junit.framework.TestCase;


/**
 * Tests the template items data structure to make sure everything is working
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TemplateItemDataListTest extends TestCase {

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemDataList#TemplateItemDataList(java.util.List, java.util.List, java.util.Map, List)}.
    */
   public void testTemplateItemDataList() {
      EvalTestDataLoad etdl = new EvalTestDataLoad(null);

      List<EvalTemplateItem> testList = new ArrayList<>();
      List<DataTemplateItem> flatList;
      TemplateItemDataList tidl;

      // test empty TI list fails
      try {
         tidl = new TemplateItemDataList(testList, null, null, null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // test that we can produce a valid one
      etdl.templateItem2A.setId(102l);
      etdl.templateItem3A.setId(103l);
      etdl.templateItem5A.setId(105l);
      testList.add(etdl.templateItem2A); // course
      testList.add(etdl.templateItem3A); // course
      testList.add(etdl.templateItem5A); // instructor

      tidl = new TemplateItemDataList(testList, null, null, null);
      assertNotNull(tidl);
      assertEquals(3, tidl.getTemplateItemsCount());
      assertEquals(3, tidl.getNonChildItemsCount());
      assertEquals(1, tidl.getTemplateItemGroupsCount());
      assertEquals(EvalConstants.ITEM_CATEGORY_COURSE, tidl.getTemplateItemGroups().get(0).associateType);

      // test the flattened data list
      flatList = tidl.getFlatListOfDataTemplateItems(false);
      assertNotNull(flatList);
      assertEquals(2, flatList.size());
      assertEquals(etdl.templateItem2A, flatList.get(0).templateItem);
      assertTrue(flatList.get(0).isFirstInAssociated);
      assertEquals(etdl.templateItem3A, flatList.get(1).templateItem);
      assertFalse(flatList.get(1).isFirstInAssociated);

      // now add in some associates
      Map<String, List<String>> associates = new HashMap<>();
      List<String> associateIds = new ArrayList<>();
      associateIds.add(EvalTestDataLoad.MAINT_USER_ID);
      associates.put(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, associateIds);

      tidl = new TemplateItemDataList(testList, null, associates, null);
      assertNotNull(tidl);
      assertEquals(3, tidl.getTemplateItemsCount());
      assertEquals(3, tidl.getNonChildItemsCount());
      assertEquals(2, tidl.getTemplateItemGroupsCount());
      assertEquals(EvalConstants.ITEM_CATEGORY_COURSE, tidl.getTemplateItemGroups().get(0).associateType);
      assertEquals(2, tidl.getTemplateItemGroups().get(0).getTemplateItemsCount());
      assertEquals(2, tidl.getTemplateItemGroups().get(0).getDataTemplateItems(false).size());
      assertEquals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, tidl.getTemplateItemGroups().get(1).associateType);
      assertEquals(1, tidl.getTemplateItemGroups().get(1).getTemplateItemsCount());
      assertEquals(1, tidl.getTemplateItemGroups().get(1).getDataTemplateItems(false).size());
      assertEquals(EvalTestDataLoad.MAINT_USER_ID, tidl.getTemplateItemGroups().get(1).associateId);
      assertEquals(2, tidl.getAssociateTypes().size());
      assertEquals(EvalConstants.ITEM_CATEGORY_COURSE, tidl.getAssociateTypes().get(0));
      assertEquals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, tidl.getAssociateTypes().get(1));

      // test the flattened data list
      flatList = tidl.getFlatListOfDataTemplateItems(false);
      assertNotNull(flatList);
      assertEquals(3, flatList.size());
      assertEquals(etdl.templateItem2A, flatList.get(0).templateItem);
      assertTrue(flatList.get(0).isFirstInAssociated);
      assertEquals(etdl.templateItem3A, flatList.get(1).templateItem);
      assertFalse(flatList.get(1).isFirstInAssociated);
      assertEquals(etdl.templateItem5A, flatList.get(2).templateItem);
      assertTrue(flatList.get(2).isFirstInAssociated);

      // now test multiple associates
      associateIds.add(EvalTestDataLoad.ADMIN_USER_ID);
      associates.put(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, associateIds);

      tidl = new TemplateItemDataList(testList, null, associates, null);
      assertNotNull(tidl);
      assertEquals(3, tidl.getTemplateItemsCount());
      assertEquals(3, tidl.getNonChildItemsCount());
      assertEquals(3, tidl.getTemplateItemGroupsCount());
      assertEquals(EvalConstants.ITEM_CATEGORY_COURSE, tidl.getTemplateItemGroups().get(0).associateType);
      assertEquals(2, tidl.getTemplateItemGroups().get(0).getTemplateItemsCount());
      assertEquals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, tidl.getTemplateItemGroups().get(1).associateType);
      assertEquals(1, tidl.getTemplateItemGroups().get(1).getTemplateItemsCount());
      assertEquals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, tidl.getTemplateItemGroups().get(2).associateType);
      assertEquals(1, tidl.getTemplateItemGroups().get(2).getTemplateItemsCount());
      assertEquals(2, tidl.getAssociateTypes().size());
      assertEquals(EvalConstants.ITEM_CATEGORY_COURSE, tidl.getAssociateTypes().get(0));
      assertEquals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, tidl.getAssociateTypes().get(1));

      // test the flattened data list
      flatList = tidl.getFlatListOfDataTemplateItems(false);
      assertNotNull(flatList);
      assertEquals(4, flatList.size());
      assertEquals(etdl.templateItem2A, flatList.get(0).templateItem);
      assertTrue(flatList.get(0).isFirstInAssociated);
      assertEquals(etdl.templateItem3A, flatList.get(1).templateItem);
      assertFalse(flatList.get(1).isFirstInAssociated);
      assertEquals(etdl.templateItem5A, flatList.get(2).templateItem);
      assertTrue(flatList.get(2).isFirstInAssociated);
      assertEquals(etdl.templateItem5A, flatList.get(3).templateItem);
      assertTrue(flatList.get(3).isFirstInAssociated);

      // now test adding in some hierarchy nodes
      List<EvalHierarchyNode> nodes = new ArrayList<>();
      nodes.add( new EvalHierarchyNode("node1", "node title", "description") );

      associateIds.clear();
      associateIds.add(EvalTestDataLoad.MAINT_USER_ID);
      associates.put(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, associateIds);

      tidl = new TemplateItemDataList(testList, nodes, associates, null);
      assertNotNull(tidl);
      assertEquals(3, tidl.getTemplateItemsCount());
      assertEquals(3, tidl.getNonChildItemsCount());
      assertEquals(2, tidl.getTemplateItemGroupsCount());
      assertEquals(EvalConstants.ITEM_CATEGORY_COURSE, tidl.getTemplateItemGroups().get(0).associateType);
      assertEquals(2, tidl.getTemplateItemGroups().get(0).getTemplateItemsCount());
      assertEquals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, tidl.getTemplateItemGroups().get(1).associateType);
      assertEquals(1, tidl.getTemplateItemGroups().get(1).getTemplateItemsCount());
      assertEquals(2, tidl.getAssociateTypes().size());
      assertEquals(EvalConstants.ITEM_CATEGORY_COURSE, tidl.getAssociateTypes().get(0));
      assertEquals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, tidl.getAssociateTypes().get(1));
      // node1 is not used so we will only get the top level nodes back
      assertEquals(1, tidl.getTemplateItemGroups().get(0).hierarchyNodeGroups.size());
      assertNull(tidl.getTemplateItemGroups().get(0).hierarchyNodeGroups.get(0).node);
      assertEquals(2, tidl.getTemplateItemGroups().get(0).getDataTemplateItems(false).size());
      assertEquals(1, tidl.getTemplateItemGroups().get(1).hierarchyNodeGroups.size());
      assertNull(tidl.getTemplateItemGroups().get(1).hierarchyNodeGroups.get(0).node);
      assertEquals(1, tidl.getTemplateItemGroups().get(1).getDataTemplateItems(false).size());

      // test the flattened data list
      flatList = tidl.getFlatListOfDataTemplateItems(false);
      assertNotNull(flatList);
      assertEquals(3, flatList.size());
      assertEquals(etdl.templateItem2A, flatList.get(0).templateItem);
      assertTrue(flatList.get(0).isFirstInAssociated);
      assertEquals(etdl.templateItem3A, flatList.get(1).templateItem);
      assertFalse(flatList.get(1).isFirstInAssociated);
      assertEquals(etdl.templateItem5A, flatList.get(2).templateItem);
      assertTrue(flatList.get(2).isFirstInAssociated);

      // TODO add in test data for TIs associated with nodes at some point

      // TODO add in tests for answers mapping

   }

}

/**
 * $Id$
 * $URL$
 * TemplateItemUtilsTest.java - evaluation - Mar 27, 2008 10:30:50 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;

import junit.framework.TestCase;


/**
 * Testing all the utilities used for template items
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TemplateItemUtilsTest extends TestCase {

    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#getTemplateItemType(org.sakaiproject.evaluation.model.EvalTemplateItem)}.
     */
    public void testGetTemplateItemType() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);
        String itemType = null;

        itemType = TemplateItemUtils.getTemplateItemType(etdl.templateItem1P);
        assertNotNull(itemType);
        assertEquals(EvalConstants.ITEM_TYPE_SCALED, itemType);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#makeTemplateItemsList(java.util.Collection)}.
     */
    public void testMakeTemplateItemsList() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);
        Set<EvalTemplateItem> collection = new HashSet<EvalTemplateItem>();
        List<EvalTemplateItem> list = null;

        list = TemplateItemUtils.makeTemplateItemsList(collection);
        assertNotNull(list);
        assertEquals(0, list.size());

        collection.add(etdl.templateItem1P);
        collection.add(etdl.templateItem2B);
        collection.add(etdl.templateItem3PU);

        list = TemplateItemUtils.makeTemplateItemsList(collection);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertTrue( list.contains(etdl.templateItem1P) );
        assertTrue( list.contains(etdl.templateItem2B) );
        assertTrue( list.contains(etdl.templateItem3PU) );
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#makeTemplateItemsIdsArray(java.util.Collection)}.
     */
    public void testMakeTemplateItemsIdsArray() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);
        Set<EvalTemplateItem> collection = new HashSet<EvalTemplateItem>();
        Long[] array = null;

        array = TemplateItemUtils.makeTemplateItemsIdsArray(collection);
        assertNotNull(array);
        assertEquals(0, array.length);

        collection.add(etdl.templateItem1P);
        collection.add(etdl.templateItem2B);
        collection.add(etdl.templateItem3PU);

        array = TemplateItemUtils.makeTemplateItemsIdsArray(collection);
        assertNotNull(array);
        assertEquals(3, array.length);
    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#checkTemplateItemsCategoryExists(java.lang.String, java.util.List)}.
     */
    public void testCheckTemplateItemsCategoryExists() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);
        List<EvalTemplateItem> itemList = new ArrayList<EvalTemplateItem>();

        itemList.add(etdl.templateItem2A);
        itemList.add(etdl.templateItem3A);
        itemList.add(etdl.templateItem5A);

        assertTrue( TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_COURSE, itemList) );
        assertFalse( TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_ENVIRONMENT, itemList) );
        assertTrue( TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, itemList) );
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#getCategoryTemplateItems(java.lang.String, java.util.List)}.
     */
    public void testGetCategoryTemplateItems() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);
        List<EvalTemplateItem> itemList = new ArrayList<EvalTemplateItem>();

        itemList.add(etdl.templateItem2A);
        itemList.add(etdl.templateItem3A);
        itemList.add(etdl.templateItem5A);

        List<EvalTemplateItem> list = null;
        list = TemplateItemUtils.getCategoryTemplateItems(EvalConstants.ITEM_CATEGORY_COURSE, itemList);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertTrue( list.contains(etdl.templateItem2A) );
        assertTrue( list.contains(etdl.templateItem3A) );

        list = TemplateItemUtils.getCategoryTemplateItems(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, itemList);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertTrue( list.contains(etdl.templateItem5A) );

        list = TemplateItemUtils.getCategoryTemplateItems(EvalConstants.ITEM_CATEGORY_ENVIRONMENT, itemList);
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#orderTemplateItems(java.util.List, boolean)}.
     */
    public void testOrderTemplateItems() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);
        List<EvalTemplateItem> itemList = new ArrayList<EvalTemplateItem>();

        itemList.add(etdl.templateItem2A);
        itemList.add(etdl.templateItem5A);
        itemList.add(etdl.templateItem3A);

        List<EvalTemplateItem> list = null;
        list = TemplateItemUtils.orderTemplateItems(itemList, false);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(etdl.templateItem2A, list.get(0));
        assertEquals(etdl.templateItem3A, list.get(1));
        assertEquals(etdl.templateItem5A, list.get(2));

        // check if this corrects invalid orders of items
        // check for orders with gaps in them (like a deletion)
        etdl.templateItem1U.setDisplayOrder(2);
        etdl.templateItem3U.setDisplayOrder(4);
        etdl.templateItem5U.setDisplayOrder(5);

        itemList.clear();
        itemList.add(etdl.templateItem1U);
        itemList.add(etdl.templateItem3U);
        itemList.add(etdl.templateItem5U);

        list = TemplateItemUtils.orderTemplateItems(itemList, true);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(etdl.templateItem1U, list.get(0));
        assertEquals(etdl.templateItem3U, list.get(1));
        assertEquals(etdl.templateItem5U, list.get(2));
        assertEquals(new Integer(1), etdl.templateItem1U.getDisplayOrder());
        assertEquals(new Integer(2), etdl.templateItem3U.getDisplayOrder());
        assertEquals(new Integer(3), etdl.templateItem5U.getDisplayOrder());

        // check for orders which are all the same
        etdl.templateItem1U.setDisplayOrder(2);
        etdl.templateItem3U.setDisplayOrder(2);
        etdl.templateItem5U.setDisplayOrder(2);

        itemList.clear();
        itemList.add(etdl.templateItem1U);
        itemList.add(etdl.templateItem3U);
        itemList.add(etdl.templateItem5U);

        list = TemplateItemUtils.orderTemplateItems(itemList, true);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(etdl.templateItem1U, list.get(0));
        assertEquals(etdl.templateItem3U, list.get(1));
        assertEquals(etdl.templateItem5U, list.get(2));
        assertEquals(new Integer(1), etdl.templateItem1U.getDisplayOrder());
        assertEquals(new Integer(2), etdl.templateItem3U.getDisplayOrder());
        assertEquals(new Integer(3), etdl.templateItem5U.getDisplayOrder());

        // check for orders which are reversed
        etdl.templateItem1U.setDisplayOrder(1);
        etdl.templateItem3U.setDisplayOrder(2);
        etdl.templateItem5U.setDisplayOrder(3);

        itemList.clear();
        itemList.add(etdl.templateItem5U);
        itemList.add(etdl.templateItem3U);
        itemList.add(etdl.templateItem1U);

        list = TemplateItemUtils.orderTemplateItems(itemList, true);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(etdl.templateItem1U, list.get(0));
        assertEquals(etdl.templateItem3U, list.get(1));
        assertEquals(etdl.templateItem5U, list.get(2));
        assertEquals(new Integer(1), etdl.templateItem1U.getDisplayOrder());
        assertEquals(new Integer(2), etdl.templateItem3U.getDisplayOrder());
        assertEquals(new Integer(3), etdl.templateItem5U.getDisplayOrder());

        // TODO check for block ordering

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#isAnswerable(org.sakaiproject.evaluation.model.EvalTemplateItem)}.
     */
    public void testIsAnswerable() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);

        assertTrue( TemplateItemUtils.isAnswerable(etdl.templateItem1U) );
        assertTrue( TemplateItemUtils.isAnswerable(etdl.templateItem2B) );
        assertTrue( TemplateItemUtils.isAnswerable(etdl.templateItem3A) );
        assertTrue( TemplateItemUtils.isAnswerable(etdl.templateItem5U) );
        assertTrue( TemplateItemUtils.isAnswerable(etdl.templateItem6UU) );
        assertFalse( TemplateItemUtils.isAnswerable(etdl.templateItem9B) );
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#getAnswerableTemplateItems(java.util.List)}.
     */
    public void testGetAnswerableTemplateItems() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);
        List<EvalTemplateItem> itemList = new ArrayList<EvalTemplateItem>();

        // need to trick this into thinking it works
        etdl.templateItem9B.setId( new Long(0) );

        itemList.add(etdl.templateItem2B);
        itemList.add(etdl.templateItem3B);
        itemList.add(etdl.templateItem9B);

        List<EvalTemplateItem> list = null;
        list = TemplateItemUtils.getAnswerableTemplateItems(itemList);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(etdl.templateItem2B, list.get(0));
        assertEquals(etdl.templateItem3B, list.get(1));
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#isRequireable(org.sakaiproject.evaluation.model.EvalTemplateItem)}.
     */
    public void testIsRequired() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);

        assertTrue( TemplateItemUtils.isRequireable(etdl.templateItem1U) );
        assertTrue( TemplateItemUtils.isRequireable(etdl.templateItem2B) );
        assertTrue( TemplateItemUtils.isRequireable(etdl.templateItem3A) );
        assertFalse( TemplateItemUtils.isRequireable(etdl.templateItem5U) );
        assertFalse( TemplateItemUtils.isRequireable(etdl.templateItem6UU) );
        assertFalse( TemplateItemUtils.isRequireable(etdl.templateItem9B) );
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#getRequireableTemplateItems(java.util.List)}.
     */
    public void testGetRequiredTemplateItems() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);
        List<EvalTemplateItem> itemList = new ArrayList<EvalTemplateItem>();

        itemList.add(etdl.templateItem1U);
        itemList.add(etdl.templateItem3U);
        itemList.add(etdl.templateItem5U);

        List<EvalTemplateItem> list = null;
        list = TemplateItemUtils.getRequireableTemplateItems(itemList);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(etdl.templateItem1U, list.get(0));
        assertEquals(etdl.templateItem3U, list.get(1));
    }

    public void testIsCompulsory() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);

        assertTrue( TemplateItemUtils.isCompulsory(etdl.templateItem1U) );
        assertFalse( TemplateItemUtils.isCompulsory(etdl.templateItem2B) );
        assertFalse( TemplateItemUtils.isCompulsory(etdl.templateItem3A) );
        assertFalse( TemplateItemUtils.isCompulsory(etdl.templateItem5U) );
        assertFalse( TemplateItemUtils.isCompulsory(etdl.templateItem6UU) );
        assertFalse( TemplateItemUtils.isCompulsory(etdl.templateItem9B) );
    }

    public void testGetCompulsoryTemplateItems() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);
        List<EvalTemplateItem> itemList = new ArrayList<EvalTemplateItem>();

        itemList.add(etdl.templateItem1U);
        itemList.add(etdl.templateItem3U);
        itemList.add(etdl.templateItem5U);

        List<EvalTemplateItem> list = null;
        list = TemplateItemUtils.getCompulsoryTemplateItems(itemList);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(etdl.templateItem1U, list.get(0));
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#isBlockParent(org.sakaiproject.evaluation.model.EvalTemplateItem)}.
     */
    public void testIsBlockParent() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);

        assertFalse( TemplateItemUtils.isBlockParent(etdl.templateItem1U) );
        assertFalse( TemplateItemUtils.isBlockParent(etdl.templateItem2B) );
        assertFalse( TemplateItemUtils.isBlockParent(etdl.templateItem3A) );
        assertFalse( TemplateItemUtils.isBlockParent(etdl.templateItem5U) );
        assertFalse( TemplateItemUtils.isBlockParent(etdl.templateItem6UU) );
        assertTrue( TemplateItemUtils.isBlockParent(etdl.templateItem9B) );
    }

    public void testIsBlockChild() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);

        // need to trick this into thinking it works
        etdl.templateItem9B.setId( new Long(0) );
        etdl.templateItem2B.setBlockId( new Long(0) );
        etdl.templateItem3B.setBlockId( new Long(0) );

        assertFalse( TemplateItemUtils.isBlockChild(etdl.templateItem1U) );
        assertTrue( TemplateItemUtils.isBlockChild(etdl.templateItem2B) );
        assertTrue( TemplateItemUtils.isBlockChild(etdl.templateItem3B) );
        assertFalse( TemplateItemUtils.isBlockChild(etdl.templateItem5U) );
        assertFalse( TemplateItemUtils.isBlockChild(etdl.templateItem6UU) );
        assertFalse( TemplateItemUtils.isBlockChild(etdl.templateItem9B) );
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#getNonChildItems(java.util.List)}.
     */
    public void testGetNonChildItems() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);
        List<EvalTemplateItem> itemList = new ArrayList<EvalTemplateItem>();

        itemList.add(etdl.templateItem2B);
        itemList.add(etdl.templateItem3B);
        itemList.add(etdl.templateItem9B);

        List<EvalTemplateItem> list = null;
        list = TemplateItemUtils.getNonChildItems(itemList);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(etdl.templateItem9B, list.get(0));
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#getChildItems(java.util.List, java.lang.Long)}.
     */
    public void testGetChildItems() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);
        List<EvalTemplateItem> itemList = new ArrayList<EvalTemplateItem>();

        // need to trick this into thinking it works
        etdl.templateItem9B.setId( new Long(0) );
        etdl.templateItem2B.setBlockId( new Long(0) );
        etdl.templateItem3B.setBlockId( new Long(0) );

        itemList.add(etdl.templateItem2B);
        itemList.add(etdl.templateItem3B);
        itemList.add(etdl.templateItem9B);

        List<EvalTemplateItem> list = null;
        list = TemplateItemUtils.getChildItems(itemList, new Long(0));
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(etdl.templateItem2B, list.get(0));
        assertEquals(etdl.templateItem3B, list.get(1));
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#getNodeItems(java.util.List, java.lang.String)}.
     */
    public void testGetNodeItems() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);
        List<EvalTemplateItem> itemList = new ArrayList<EvalTemplateItem>();

        itemList.add(etdl.templateItem1U);
        itemList.add(etdl.templateItem3U);
        itemList.add(etdl.templateItem5U);

        List<EvalTemplateItem> list = null;
        list = TemplateItemUtils.getNodeItems(itemList, null);
        assertNotNull(list);
        assertEquals(3, list.size());

        // TODO cannot test getting node items since none of the items are assigned to nodes

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#makeTemplateItemAnswerKey(java.lang.Long, java.lang.String, java.lang.String)}.
     */
    public void testMakeTemplateItemAnswerKey() {
        String key = null;

        key = TemplateItemUtils.makeTemplateItemAnswerKey(new Long(10), null, null);
        assertNotNull(key);
        assertEquals("10_null_null", key);

        key = TemplateItemUtils.makeTemplateItemAnswerKey(new Long(5), "instructor", "aaronz");
        assertNotNull(key);
        assertEquals("5_instructor_aaronz", key);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#makeTemplateItem(org.sakaiproject.evaluation.model.EvalItem)}.
     */
    public void testMakeTemplateItem() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);
        EvalTemplateItem newTI = null;

        newTI = TemplateItemUtils.makeTemplateItem(etdl.item1);
        assertNotNull(newTI);
        assertEquals(etdl.item1, newTI.getItem());
        assertEquals(etdl.item1.getScaleDisplaySetting(), newTI.getScaleDisplaySetting());
        assertEquals(etdl.item1.getUsesNA(), newTI.getUsesNA());
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#validateItemByClassification(org.sakaiproject.evaluation.model.EvalItem)}.
     */
    public void testValidateItemByClassification() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);
        TemplateItemUtils.validateItemByClassification(etdl.item1);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.utils.TemplateItemUtils#validateTemplateItemByClassification(org.sakaiproject.evaluation.model.EvalTemplateItem)}.
     */
    public void testValidateTemplateItemByClassification() {
        EvalTestDataLoad etdl = new EvalTestDataLoad(null);
        TemplateItemUtils.validateTemplateItemByClassification(etdl.templateItem1U);
    }

}

/**
 * $Id$
 * $URL$
 * ArrayUtilsTest.java - evaluation - Feb 12, 2008 9:48:36 AM - azeckoski
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
import java.util.List;

import junit.framework.TestCase;

import org.sakaiproject.evaluation.utils.ArrayUtils;


/**
 * Testing out the array utils
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ArrayUtilsTest extends TestCase {

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.ArrayUtils#removeDuplicates(java.util.List)}.
    */
   public void testRemoveDuplicates() {
      List<String> l = null;

      // positive
      List<String> testDups = new ArrayList<String>();
      testDups.add("aaron");
      testDups.add("zeckoski");
      testDups.add("duplicates");
      testDups.add("aaron");
      testDups.add("zeckoski");

      l = ArrayUtils.removeDuplicates(testDups);
      assertNotNull(l);
      assertEquals(3, l.size());
      assertTrue(l.contains("aaron"));
      assertTrue(l.contains("zeckoski"));
      assertTrue(l.contains("duplicates"));

      // even more dups
      testDups = new ArrayList<String>();
      testDups.add("aaron");
      testDups.add("zeckoski");
      testDups.add("duplicates");
      testDups.add("aaron");
      testDups.add("zeckoski");
      testDups.add("duplicates");
      testDups.add("aaron");
      testDups.add("zeckoski");

      l = ArrayUtils.removeDuplicates(testDups);
      assertNotNull(l);
      assertEquals(3, l.size());
      assertTrue(l.contains("aaron"));
      assertTrue(l.contains("zeckoski"));
      assertTrue(l.contains("duplicates"));

      // negative
      List<String> testNoDups = new ArrayList<String>();
      testNoDups.add("aaron");
      testNoDups.add("zeckoski");
      testNoDups.add("no_duplicates");

      l = ArrayUtils.removeDuplicates(testNoDups);
      assertNotNull(l);
      assertEquals(3, l.size());
      assertTrue(l.contains("aaron"));
      assertTrue(l.contains("zeckoski"));
      assertTrue(l.contains("no_duplicates"));

      // exception
      try {
         l = ArrayUtils.removeDuplicates(null);
         fail("Should have thrown exception");
      } catch (NullPointerException e) {
         assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.ArrayUtils#appendArray(java.lang.String[], java.lang.String)}.
    */
   public void testAppendArrayStringArrayString() {
      String[] strings = null;

      strings = new String[] { "test1", "test2" };
      assertEquals(2, strings.length);
      strings = ArrayUtils.appendArray(strings, "test3");
      assertEquals(3, strings.length);
      assertEquals("test3", strings[2]);

      strings = new String[] { };
      assertEquals(0, strings.length);
      strings = ArrayUtils.appendArray(strings, "testAZ");
      assertEquals(1, strings.length);
      assertEquals("testAZ", strings[0]);

      // exception
      try {
         ArrayUtils.appendArray(null, "blah");
         fail("Should have thrown exception");
      } catch (NullPointerException e) {
         assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.ArrayUtils#appendArray(java.lang.Object[], java.lang.Object)}.
    */
   public void testAppendArrayObjectArrayObject() {
      Object[] objects = null;

      objects = new Object[] { "test1", "test2" };
      assertEquals(2, objects.length);
      objects = ArrayUtils.appendArray(objects, "test3");
      assertEquals(3, objects.length);
      assertEquals("test3", objects[2]);

      objects = new Object[] { };
      assertEquals(0, objects.length);
      objects = ArrayUtils.appendArray(objects, "testAZ");
      assertEquals(1, objects.length);
      assertEquals("testAZ", objects[0]);

      // exception
      try {
         ArrayUtils.appendArray(null, "blah");
         fail("Should have thrown exception");
      } catch (NullPointerException e) {
         assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.ArrayUtils#appendArray(int[], int)}.
    */
   public void testAppendArrayIntArrayInt() {
      int[] ints = null;

      ints = new int[] { 1, 2 };
      assertEquals(2, ints.length);
      ints = ArrayUtils.appendArray(ints, 3);
      assertEquals(3, ints.length);
      assertEquals(3, ints[2]);

      ints = new int[] { };
      assertEquals(0, ints.length);
      ints = ArrayUtils.appendArray(ints, 5);
      assertEquals(1, ints.length);
      assertEquals(5, ints[0]);

      // exception
      try {
         ArrayUtils.appendArray(null, 10);
         fail("Should have thrown exception");
      } catch (NullPointerException e) {
         assertNotNull(e);
      }
   }

   public void testAppendArrays() {
      String[] a1 = new String[] {"aaa", "bbb", "ccc"};
      String[] a2 = new String[] {"ddd", "eee"};

      String[] result = null;

      result = ArrayUtils.appendArrays(a1, a2);
      assertNotNull(result);
      assertEquals(5, result.length);
      assertEquals("aaa", result[0]);
      assertEquals("bbb", result[1]);
      assertEquals("ccc", result[2]);
      assertEquals("ddd", result[3]);
      assertEquals("eee", result[4]);

      result = ArrayUtils.appendArrays(a1, new String[] {});
      assertNotNull(result);
      assertEquals(a1.length, result.length);

      result = ArrayUtils.appendArrays(a2, new String[] {});
      assertNotNull(result);
      assertEquals(a2.length, result.length);

      result = ArrayUtils.appendArrays(new String[] {}, new String[] {});
      assertNotNull(result);
      assertEquals(0, result.length);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.ArrayUtils#prependArray(java.lang.String[], java.lang.String)}.
    */
   public void testPrependArrayStringArrayString() {
      String[] strings = null;

      strings = new String[] { "test1", "test2" };
      assertEquals(2, strings.length);
      strings = ArrayUtils.prependArray(strings, "test0");
      assertEquals(3, strings.length);
      assertEquals("test0", strings[0]);

      strings = new String[] { };
      assertEquals(0, strings.length);
      strings = ArrayUtils.prependArray(strings, "testAZ");
      assertEquals(1, strings.length);
      assertEquals("testAZ", strings[0]);

      // exception
      try {
         ArrayUtils.prependArray(null, "blah");
         fail("Should have thrown exception");
      } catch (NullPointerException e) {
         assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.ArrayUtils#prependArray(java.lang.Object[], java.lang.Object)}.
    */
   public void testPrependArrayObjectArrayObject() {
      Object[] objects = null;

      objects = new Object[] { "test1", "test2" };
      assertEquals(2, objects.length);
      objects = ArrayUtils.prependArray(objects, "test0");
      assertEquals(3, objects.length);
      assertEquals("test0", objects[0]);

      objects = new Object[] { };
      assertEquals(0, objects.length);
      objects = ArrayUtils.prependArray(objects, "testAZ");
      assertEquals(1, objects.length);
      assertEquals("testAZ", objects[0]);

      // exception
      try {
         ArrayUtils.prependArray(null, "blah");
         fail("Should have thrown exception");
      } catch (NullPointerException e) {
         assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.ArrayUtils#prependArray(int[], int)}.
    */
   public void testPrependArrayIntArrayInt() {
      int[] ints = null;

      ints = new int[] { 1, 2 };
      assertEquals(2, ints.length);
      ints = ArrayUtils.prependArray(ints, 0);
      assertEquals(3, ints.length);
      assertEquals(0, ints[0]);

      ints = new int[] { };
      assertEquals(0, ints.length);
      ints = ArrayUtils.prependArray(ints, 5);
      assertEquals(1, ints.length);
      assertEquals(5, ints[0]);

      // exception
      try {
         ArrayUtils.prependArray(null, 10);
         fail("Should have thrown exception");
      } catch (NullPointerException e) {
         assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.ArrayUtils#arrayToString(java.lang.Object[])}.
    */
   public void testArrayToString() {
      String result = null;

      String[] strings = new String[] { "Aaron", "Testing", "Arrays" };
      assertEquals(3, strings.length);
      result = ArrayUtils.arrayToString(strings);
      assertNotNull(result);
      assertEquals("Aaron,Testing,Arrays", result);

      Object[] empty = new String[] { };
      assertEquals(0, empty.length);
      result = ArrayUtils.arrayToString(empty);
      assertNotNull(result);
      assertEquals("", result);

      result = ArrayUtils.arrayToString(null);
      assertNotNull(result);
      assertEquals("", result);      
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.ArrayUtils#listToIntArray(java.util.List)}.
    */
   public void testListToIntArray() {
      int[] ints = null;

      List<Number> numList = new ArrayList<Number>();
      numList.add(new Integer(1));
      numList.add(new Integer(2));

      ints = ArrayUtils.listToIntArray(numList);
      assertNotNull(ints);
      assertEquals(2, ints.length);
      assertEquals(1, ints[0]);
      assertEquals(2, ints[1]);

      List<Number> emptyList = new ArrayList<Number>();
      ints = ArrayUtils.listToIntArray(emptyList);
      assertNotNull(ints);
      assertEquals(0, ints.length);
   }

}

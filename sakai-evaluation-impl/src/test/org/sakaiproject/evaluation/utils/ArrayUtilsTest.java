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
import java.util.List;

import junit.framework.TestCase;

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
      List<String> l;

      // positive
      List<String> testDups = new ArrayList<>();
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
      testDups = new ArrayList<>();
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
      List<String> testNoDups = new ArrayList<>();
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
         ArrayUtils.removeDuplicates(null);
         fail("Should have thrown exception");
      } catch (NullPointerException e) {
         assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.ArrayUtils#appendArray(java.lang.String[], java.lang.String)}.
    */
   public void testAppendArrayStringArrayString() {
      String[] strings;

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
      Object[] objects;

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
      int[] ints;

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

      String[] result;

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
      String[] strings;

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
      Object[] objects;

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
      int[] ints;

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
      String result;

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
      int[] ints;

      List<Number> numList = new ArrayList<>();
      numList.add(1);
      numList.add(2);

      ints = ArrayUtils.listToIntArray(numList);
      assertNotNull(ints);
      assertEquals(2, ints.length);
      assertEquals(1, ints[0]);
      assertEquals(2, ints[1]);

      List<Number> emptyList = new ArrayList<>();
      ints = ArrayUtils.listToIntArray(emptyList);
      assertNotNull(ints);
      assertEquals(0, ints.length);
   }

}

/**
 * $Id$
 * $URL$
 * EvalUtilsTest.java - evaluation - Feb 5, 2008 2:42:41 PM - azeckoski
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.utils.EvalUtils;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;


/**
 * Testing out the {@link EvalUtils} utilities
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalUtilsTest extends TestCase {

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.utils.EvalUtils#getEvaluationState(org.sakaiproject.evaluation.model.EvalEvaluation)}.
    */
   public void testGetEvaluationState() {
      String state = null;
      EvalTestDataLoad etdl = new EvalTestDataLoad();

      // positive
      state = EvalUtils.getEvaluationState(etdl.evaluationActive);
      assertEquals(EvalConstants.EVALUATION_STATE_ACTIVE, state);

      state = EvalUtils.getEvaluationState(etdl.evaluationActiveUntaken);
      assertEquals(EvalConstants.EVALUATION_STATE_ACTIVE, state);

      state = EvalUtils.getEvaluationState(etdl.evaluationClosed);
      assertEquals(EvalConstants.EVALUATION_STATE_CLOSED, state);

      state = EvalUtils.getEvaluationState(etdl.evaluationNew);
      assertEquals(EvalConstants.EVALUATION_STATE_INQUEUE, state);

      state = EvalUtils.getEvaluationState(etdl.evaluationViewable);
      assertEquals(EvalConstants.EVALUATION_STATE_VIEWABLE, state);

      // negative
      state = EvalUtils.getEvaluationState( new EvalEvaluation() );
      assertEquals(EvalConstants.EVALUATION_STATE_UNKNOWN, state);

      // no exceptions thrown
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.utils.EvalUtils#checkSharingConstant(java.lang.String)}.
    */
   public void testCheckSharingConstant() {
      // positive
      assertTrue( EvalUtils.checkSharingConstant(EvalConstants.SHARING_OWNER) );
      assertTrue( EvalUtils.checkSharingConstant(EvalConstants.SHARING_PRIVATE) );
      assertTrue( EvalUtils.checkSharingConstant(EvalConstants.SHARING_PUBLIC) );
      assertTrue( EvalUtils.checkSharingConstant(EvalConstants.SHARING_SHARED) );
      assertTrue( EvalUtils.checkSharingConstant(EvalConstants.SHARING_VISIBLE) );

      // negative
      assertFalse( EvalUtils.checkSharingConstant("INVALID") );
      assertFalse( EvalUtils.checkSharingConstant("") );
      assertFalse( EvalUtils.checkSharingConstant(null) );
      
      // no exceptions thrown
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.utils.EvalUtils#removeDuplicates(java.util.List)}.
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

      l = EvalUtils.removeDuplicates(testDups);
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

      l = EvalUtils.removeDuplicates(testDups);
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

      l = EvalUtils.removeDuplicates(testNoDups);
      assertNotNull(l);
      assertEquals(3, l.size());
      assertTrue(l.contains("aaron"));
      assertTrue(l.contains("zeckoski"));
      assertTrue(l.contains("no_duplicates"));

      // exception
      try {
         l = EvalUtils.removeDuplicates(null);
         fail("Should have thrown exception");
      } catch (NullPointerException e) {
         assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.utils.EvalUtils#getGroupsInCommon(java.util.List, java.util.List)}.
    */
   public void testGetGroupsInCommon() {
      EvalGroup[] groups = null;
      List<EvalGroup> evalGroups = null;
      List<EvalAssignGroup> assignGroups = null;
      EvalTestDataLoad etdl = new EvalTestDataLoad();

      // test all empty stuff
      evalGroups = new ArrayList<EvalGroup>();
      assignGroups = new ArrayList<EvalAssignGroup>();
      groups = EvalUtils.getGroupsInCommon(evalGroups, assignGroups);
      assertNotNull(groups);
      assertEquals(0, groups.length);

      // test all unique
      evalGroups = new ArrayList<EvalGroup>();
      evalGroups.add( new EvalGroup("az", "AZ group", EvalConstants.GROUP_TYPE_PROVIDED) );
      assignGroups = new ArrayList<EvalAssignGroup>();
      assignGroups.add(etdl.assign1);
      assignGroups.add(etdl.assign4);
      groups = EvalUtils.getGroupsInCommon(evalGroups, assignGroups);
      assertNotNull(groups);
      assertEquals(0, groups.length);

      // test all the same
      evalGroups = new ArrayList<EvalGroup>();
      evalGroups.add( new EvalGroup(EvalTestDataLoad.SITE1_REF, "AZ group", EvalConstants.GROUP_TYPE_PROVIDED) );
      assignGroups = new ArrayList<EvalAssignGroup>();
      assignGroups.add(etdl.assign1);
      assignGroups.add(etdl.assign2);
      groups = EvalUtils.getGroupsInCommon(evalGroups, assignGroups);
      assertNotNull(groups);
      assertEquals(1, groups.length);

      // test 2 groups of 2 the same
      evalGroups = new ArrayList<EvalGroup>();
      evalGroups.add( new EvalGroup(EvalTestDataLoad.SITE1_REF, "AZ group", EvalConstants.GROUP_TYPE_PROVIDED) );
      evalGroups.add( new EvalGroup(EvalTestDataLoad.SITE2_REF, "AZ group", EvalConstants.GROUP_TYPE_PROVIDED) );
      assignGroups = new ArrayList<EvalAssignGroup>();
      assignGroups.add(etdl.assign1);
      assignGroups.add(etdl.assign4);
      groups = EvalUtils.getGroupsInCommon(evalGroups, assignGroups);
      assertNotNull(groups);
      assertEquals(2, groups.length);

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.utils.EvalUtils#makeUniqueIdentifier(int)}.
    */
   public void testMakeUniqueIdentifier() {
      String id = null;
      HashSet<String> uniqueIds = new HashSet<String>(); 

      id = EvalUtils.makeUniqueIdentifier(5);
      assertNotNull(id);
      assertTrue(5 >= id.length());

      id = EvalUtils.makeUniqueIdentifier(10);
      assertNotNull(id);
      assertTrue(10 >= id.length());

      id = EvalUtils.makeUniqueIdentifier(18);
      assertNotNull(id);
      assertTrue(18 >= id.length());

      for (int i = 0; i < 10000; i++) {
         uniqueIds.add(EvalUtils.makeUniqueIdentifier(10));
      }
      assertEquals(10000, uniqueIds.size());
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.utils.EvalUtils#getAnswersMapByTempItemAndAssociated(org.sakaiproject.evaluation.model.EvalResponse)}.
    */
   public void testGetAnswersMapByTempItemAndAssociated() {
      //Map<String, EvalAnswer> answersMap = null;
      //EvalTestDataLoad etdl = new EvalTestDataLoad();
      
      //answersMap = EvalUtils.getAnswersMapByTempItemAndAssociated(etdl.response1);

      // TODO - cannot test this right now as it depends on hibernate semantics

      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.utils.EvalUtils#encodeMultipleAnswers(int[])}.
    */
   public void testEncodeMultipleAnswers() {
      String encoded = null;
      String S = EvalUtils.SEPARATOR;

      // positive
      encoded = EvalUtils.encodeMultipleAnswers( new int[] {0, 5, 2} );
      assertNotNull(encoded);
      assertEquals(S+"0"+S+"2"+S+"5"+S, encoded);

      encoded = EvalUtils.encodeMultipleAnswers( new int[] {5, 4, 3, 2, 1} );
      assertNotNull(encoded);
      assertEquals(S+"1"+S+"2"+S+"3"+S+"4"+S+"5"+S, encoded);

      // negative
      encoded = EvalUtils.encodeMultipleAnswers( new int[] {} );
      assertNull(encoded);

      encoded = EvalUtils.encodeMultipleAnswers( null );
      assertNull(encoded);

      // does not throw any exceptions
      
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.utils.EvalUtils#decodeMultipleAnswers(java.lang.String)}.
    */
   public void testDecodeMultipleAnswers() {
      int[] decoded = null;
      String S = EvalUtils.SEPARATOR;

      // positive
      decoded = EvalUtils.decodeMultipleAnswers(S+"1"+S+"4"+S+"7"+S);
      assertNotNull(decoded);
      assertEquals(1, decoded[0]);
      assertEquals(4, decoded[1]);
      assertEquals(7, decoded[2]);

      decoded = EvalUtils.decodeMultipleAnswers(S+"3"+S+"5"+S+"1"+S+"9"+S+"7"+S);
      assertNotNull(decoded);
      assertEquals(1, decoded[0]);
      assertEquals(3, decoded[1]);
      assertEquals(5, decoded[2]);
      assertEquals(7, decoded[3]);
      assertEquals(9, decoded[4]);

      // negative
      decoded = EvalUtils.decodeMultipleAnswers("");
      assertNotNull(decoded);
      assertEquals(0, decoded.length);

      decoded = EvalUtils.decodeMultipleAnswers(null);
      assertNotNull(decoded);
      assertEquals(0, decoded.length);

      decoded = EvalUtils.decodeMultipleAnswers(S+S);
      assertNotNull(decoded);
      assertEquals(0, decoded.length);

   }

}

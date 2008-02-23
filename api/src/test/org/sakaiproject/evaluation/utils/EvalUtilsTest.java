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

package org.sakaiproject.evaluation.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.utils.EvalUtils;


/**
 * Testing out the {@link EvalUtils} utilities
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalUtilsTest extends TestCase {

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.EvalUtils#getEvaluationState(org.sakaiproject.evaluation.model.EvalEvaluation)}.
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
    * Test method for {@link org.sakaiproject.evaluation.utils.EvalUtils#validateSharingConstant(java.lang.String)}.
    */
   public void testCheckSharingConstant() {
      // positive
      assertTrue( EvalUtils.validateSharingConstant(EvalConstants.SHARING_OWNER) );
      assertTrue( EvalUtils.validateSharingConstant(EvalConstants.SHARING_PRIVATE) );
      assertTrue( EvalUtils.validateSharingConstant(EvalConstants.SHARING_PUBLIC) );
      assertTrue( EvalUtils.validateSharingConstant(EvalConstants.SHARING_SHARED) );
      assertTrue( EvalUtils.validateSharingConstant(EvalConstants.SHARING_VISIBLE) );

      // negative

      // exception
      try {
         EvalUtils.validateSharingConstant("INVALID");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
      try {
         EvalUtils.validateSharingConstant("");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
      try {
         EvalUtils.validateSharingConstant(null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
   }

   public void testCheckIncludeConstant() {
      // positive
      assertTrue( EvalUtils.validateEmailIncludeConstant(EvalConstants.EVAL_INCLUDE_ALL) );
      assertTrue( EvalUtils.validateEmailIncludeConstant(EvalConstants.EVAL_INCLUDE_ALL) );
      assertTrue( EvalUtils.validateEmailIncludeConstant(EvalConstants.EVAL_INCLUDE_ALL) );

      // exception
      try {
         EvalUtils.validateEmailIncludeConstant("INVALID");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
      try {
         EvalUtils.validateEmailIncludeConstant("");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
      try {
         EvalUtils.validateEmailIncludeConstant(null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
   }



   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.EvalUtils#updateDueStopDates(org.sakaiproject.evaluation.model.EvalEvaluation, int)}.
    */
   public void testUpdateDueStopDates() {
      Date dueDate = null;

      Date now = new Date();
      long nowTime = new Date().getTime();
      long hour = 1000 * 60 * 60;
      Date nowPlus2 = new Date(nowTime + hour * 2);
      Date nowPlus3 = new Date(nowTime + hour * 3);

      EvalEvaluation eval = new EvalEvaluation(new Date(), "aaronz", "title",
            now, nowPlus2, nowPlus2, nowPlus3, 
            EvalConstants.EVALUATION_STATE_ACTIVE, 0, null);

      // test that no change happens if the times are within the range
      assertEquals(eval.getDueDate(), nowPlus2);
      assertEquals(eval.getStopDate(), nowPlus2);
      assertEquals(eval.getViewDate(), nowPlus3);
      dueDate = EvalUtils.updateDueStopDates(eval, 1);
      assertEquals(dueDate, nowPlus2);
      assertEquals(eval.getDueDate(), nowPlus2);
      assertEquals(eval.getStopDate(), nowPlus2);
      assertEquals(eval.getViewDate(), nowPlus3);

      // test that no change happens if the times are at the range limit
      assertEquals(eval.getDueDate(), nowPlus2);
      assertEquals(eval.getStopDate(), nowPlus2);
      assertEquals(eval.getViewDate(), nowPlus3);
      dueDate = EvalUtils.updateDueStopDates(eval, 2);
      assertEquals(dueDate, nowPlus2);
      assertEquals(eval.getDueDate(), nowPlus2);
      assertEquals(eval.getStopDate(), nowPlus2);
      assertEquals(eval.getViewDate(), nowPlus3);

      // test that change happens if the times are beyond the limit
      assertEquals(eval.getDueDate(), nowPlus2);
      assertEquals(eval.getStopDate(), nowPlus2);
      assertEquals(eval.getViewDate(), nowPlus3);
      dueDate = EvalUtils.updateDueStopDates(eval, 3);
      assertEquals(dueDate, nowPlus3);
      assertEquals(eval.getDueDate(), nowPlus3);
      assertEquals(eval.getStopDate(), nowPlus3);
      assertFalse(eval.getViewDate().equals(nowPlus3));
      assertTrue(eval.getViewDate().after(eval.getStopDate()));

      // test that change happens if the times are way beyond
      assertEquals(eval.getDueDate(), nowPlus3);
      assertEquals(eval.getStopDate(), nowPlus3);
      dueDate = EvalUtils.updateDueStopDates(eval, 24);
      assertEquals(dueDate, new Date(nowTime + hour * 24));
      assertEquals(eval.getDueDate(), new Date(nowTime + hour * 24));
      assertEquals(eval.getStopDate(), new Date(nowTime + hour * 24));
      assertTrue(eval.getViewDate().after(eval.getStopDate()));

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.EvalUtils#getEndOfDayDate(java.util.Date)}.
    */
   public void testGetEndOfDayDate() {
      Date endOfDay = null;
      Date testDay = null;
      Calendar cal = new GregorianCalendar();

      // test that time moves to the end of the day
      cal.set(2000, 10, 29, 10, 01, 10);
      testDay = cal.getTime();

      endOfDay = EvalUtils.getEndOfDayDate(testDay);
      assertNotNull(endOfDay);
      assertTrue(testDay.before(endOfDay));
      cal.setTime(endOfDay);
      assertEquals(23, cal.get(Calendar.HOUR_OF_DAY));
      assertEquals(59, cal.get(Calendar.MINUTE));
      assertEquals(59, cal.get(Calendar.SECOND));

      cal.clear();

      // test that if it is already the end of the day it is not changed
      cal.set(2000, 10, 29, 23, 59, 59);
      testDay = cal.getTime();

      endOfDay = EvalUtils.getEndOfDayDate(testDay);
      assertNotNull(endOfDay);
      assertEquals(endOfDay, testDay);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.EvalUtils#getHoursDifference(java.util.Date, java.util.Date)}.
    */
   public void testGetHoursDifference() {
      Date startTime = new Date();
      Date endTime = new Date();
      int difference = 0;

      // test same dates
      difference = EvalUtils.getHoursDifference(startTime, endTime);
      assertEquals(0, difference);

      endTime = new Date( startTime.getTime() + (1000 * 60 * 60 * 5) );
      difference = EvalUtils.getHoursDifference(startTime, endTime);
      assertEquals(5, difference);

      difference = EvalUtils.getHoursDifference(endTime, startTime);
      assertEquals(-5, difference);

      // check that it rounds correctly
      endTime = new Date( startTime.getTime() + (1000 * 60 * 60 * 5) + (1000 * 60 * 30) );
      difference = EvalUtils.getHoursDifference(startTime, endTime);
      assertEquals(5, difference);
   }




   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.EvalUtils#getGroupsInCommon(java.util.List, java.util.List)}.
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
    * Test method for {@link org.sakaiproject.evaluation.utils.EvalUtils#makeUniqueIdentifier(int)}.
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
    * Test method for {@link org.sakaiproject.evaluation.utils.EvalUtils#getAnswersMapByTempItemAndAssociated(org.sakaiproject.evaluation.model.EvalResponse)}.
    */
   public void testGetAnswersMapByTempItemAndAssociated() {
      //Map<String, EvalAnswer> answersMap = null;
      //EvalTestDataLoad etdl = new EvalTestDataLoad();

      //answersMap = EvalUtils.getAnswersMapByTempItemAndAssociated(etdl.response1);

      // TODO - cannot test this right now as it depends on hibernate semantics

      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.EvalUtils#encodeMultipleAnswers(Integer[])}.
    */
   public void testEncodeMultipleAnswers() {
      String encoded = null;
      String S = EvalUtils.SEPARATOR;

      // positive
      encoded = EvalUtils.encodeMultipleAnswers( new Integer[] {0, 5, 2} );
      assertNotNull(encoded);
      assertEquals(S+"0"+S+"2"+S+"5"+S, encoded);

      encoded = EvalUtils.encodeMultipleAnswers( new Integer[] {5, 4, 3, 2, 1} );
      assertNotNull(encoded);
      assertEquals(S+"1"+S+"2"+S+"3"+S+"4"+S+"5"+S, encoded);

      // negative
      encoded = EvalUtils.encodeMultipleAnswers( new Integer[] {} );
      assertNull(encoded);

      encoded = EvalUtils.encodeMultipleAnswers( null );
      assertNull(encoded);

      // does not throw any exceptions

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.EvalUtils#decodeMultipleAnswers(java.lang.String)}.
    */
   public void testDecodeMultipleAnswers() {
      Integer[] decoded = null;
      String S = EvalUtils.SEPARATOR;

      // positive
      decoded = EvalUtils.decodeMultipleAnswers(S+"1"+S+"4"+S+"7"+S);
      assertNotNull(decoded);
      assertEquals(1, decoded[0].intValue());
      assertEquals(4, decoded[1].intValue());
      assertEquals(7, decoded[2].intValue());

      decoded = EvalUtils.decodeMultipleAnswers(S+"3"+S+"5"+S+"1"+S+"9"+S+"7"+S);
      assertNotNull(decoded);
      assertEquals(1, decoded[0].intValue());
      assertEquals(3, decoded[1].intValue());
      assertEquals(5, decoded[2].intValue());
      assertEquals(7, decoded[3].intValue());
      assertEquals(9, decoded[4].intValue());

      // do a really simple one
      decoded = EvalUtils.decodeMultipleAnswers(S+"9"+S);
      assertNotNull(decoded);
      assertEquals(9, decoded[0].intValue());

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

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.EvalUtils#encodeAnswerNA(org.sakaiproject.evaluation.model.EvalAnswer)}.
    */
   public void testEncodeAnswerNA() {
      EvalAnswer applicableAnswer = new EvalAnswer(null, null, null, null, "text", new Integer(3), null, null);
      EvalAnswer naAnswer = new EvalAnswer(null, null, null, null, "text", EvalConstants.NA_VALUE, null, null);
      naAnswer.setMultiAnswerCode("multiCode");

      applicableAnswer.NA = false;
      naAnswer.NA = true;

      assertFalse( EvalUtils.encodeAnswerNA(applicableAnswer) );
      assertEquals(false, applicableAnswer.NA);
      assertEquals(new Integer(3), applicableAnswer.getNumeric());
      assertEquals("text", applicableAnswer.getText());

      assertTrue( EvalUtils.encodeAnswerNA(naAnswer) );
      assertEquals(true, naAnswer.NA);
      assertEquals(EvalConstants.NA_VALUE, naAnswer.getNumeric());
      assertNull(naAnswer.getText());
      assertNull(naAnswer.getMultiAnswerCode());

      try {
         EvalUtils.encodeAnswerNA(null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.EvalUtils#decodeAnswerNA(org.sakaiproject.evaluation.model.EvalAnswer)}.
    */
   public void testDecodeAnswerNA() {
      EvalAnswer applicableAnswer = new EvalAnswer(null, null, null, null, "text", new Integer(3), null, null);
      EvalAnswer naAnswer = new EvalAnswer(null, null, null, null, "text", EvalConstants.NA_VALUE, null, null);
      naAnswer.setMultiAnswerCode("multiCode");

      assertFalse( EvalUtils.decodeAnswerNA(applicableAnswer) );
      assertEquals(false, applicableAnswer.NA);
      assertEquals(new Integer(3), applicableAnswer.getNumeric());
      assertEquals("text", applicableAnswer.getText());

      assertTrue( EvalUtils.decodeAnswerNA(naAnswer) );
      assertEquals(true, naAnswer.NA);
      assertEquals(EvalConstants.NA_VALUE, naAnswer.getNumeric());
      assertEquals("text", naAnswer.getText());
      assertEquals("multiCode", naAnswer.getMultiAnswerCode());

      try {
         EvalUtils.decodeAnswerNA(null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
   }

}

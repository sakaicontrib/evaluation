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

/**
 * Testing out the {@link EvalUtils} utilities
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalUtilsTest extends TestCase {

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.EvalUtils#getEvaluationState(org.sakaiproject.evaluation.model.EvalEvaluation, boolean)}.
    */
   public void testGetEvaluationState() {
      String state;
      EvalTestDataLoad etdl = new EvalTestDataLoad(null);

      // positive
      etdl.evaluationNew.setId( new Long(1) );
      state = EvalUtils.getEvaluationState(etdl.evaluationNew, false);
      assertEquals(EvalConstants.EVALUATION_STATE_INQUEUE, state);

      // test special has no effect on a date determined state
      state = EvalUtils.getEvaluationState(etdl.evaluationNew, true);
      assertEquals(EvalConstants.EVALUATION_STATE_INQUEUE, state);

      etdl.evaluationActive.setId( new Long(2) );
      state = EvalUtils.getEvaluationState(etdl.evaluationActive, false);
      assertEquals(EvalConstants.EVALUATION_STATE_ACTIVE, state);

      etdl.evaluationActiveUntaken.setId( new Long(3) );
      state = EvalUtils.getEvaluationState(etdl.evaluationActiveUntaken, false);
      assertEquals(EvalConstants.EVALUATION_STATE_ACTIVE, state);

      etdl.evaluationClosed.setId( new Long(4) );
      state = EvalUtils.getEvaluationState(etdl.evaluationClosed, false);
      assertEquals(EvalConstants.EVALUATION_STATE_CLOSED, state);

      etdl.evaluationViewable.setId( new Long(5) );
      state = EvalUtils.getEvaluationState(etdl.evaluationViewable, false);
      assertEquals(EvalConstants.EVALUATION_STATE_VIEWABLE, state);

      // negative (null start date) and saved (should not even be possible)
      EvalEvaluation invalidEval = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, 
            "aaronz", "testing null dates", null, "XXXXXXXX", EvalConstants.SHARING_PRIVATE, 0, null);
      invalidEval.setId( new Long(6) );
      state = EvalUtils.getEvaluationState( invalidEval, false );
      assertEquals(EvalConstants.EVALUATION_STATE_UNKNOWN, state);

      // test the cases where a lot of the dates are unset (testing various nulls)
      EvalEvaluation datesEval = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, 
            "aaronz", "testing null dates", etdl.tomorrow, null, EvalConstants.SHARING_PRIVATE, 0, null);

      // new evals are always partial state
      state = EvalUtils.getEvaluationState(datesEval, false);
      assertEquals(EvalConstants.EVALUATION_STATE_PARTIAL, state);

      // test ignoring the special states
      state = EvalUtils.getEvaluationState(datesEval, true);
      assertEquals(EvalConstants.EVALUATION_STATE_INQUEUE, state);

      // set the id so this eval does not look new
      datesEval.setId( new Long(99999) );

      // only the start date is set and in the future
      state = EvalUtils.getEvaluationState(datesEval, false);
      assertEquals(EvalConstants.EVALUATION_STATE_INQUEUE, state);

      // only the start date is set and way in the past
      datesEval.setStartDate(etdl.fifteenDaysAgo);
      state = EvalUtils.getEvaluationState(datesEval, false);
      assertEquals(EvalConstants.EVALUATION_STATE_ACTIVE, state);

      // only the start date (past) and due date (future) are set
      datesEval.setDueDate(etdl.tomorrow);
      state = EvalUtils.getEvaluationState(datesEval, false);
      assertEquals(EvalConstants.EVALUATION_STATE_ACTIVE, state);

      // only the start date (past) and due date (past) are set
      datesEval.setDueDate(etdl.yesterday);
      state = EvalUtils.getEvaluationState(datesEval, false);
      assertEquals(EvalConstants.EVALUATION_STATE_VIEWABLE, state);

      // only the start date (past) and due date (past) and stop date (future) are set
      datesEval.setDueDate(etdl.fourDaysAgo);
      datesEval.setStopDate(etdl.tomorrow);
      state = EvalUtils.getEvaluationState(datesEval, false);
      assertEquals(EvalConstants.EVALUATION_STATE_GRACEPERIOD, state);

      // only the start date (past) and due date (past) and stop date (past) are set
      datesEval.setStopDate(etdl.threeDaysAgo);
      state = EvalUtils.getEvaluationState(datesEval, false);
      assertEquals(EvalConstants.EVALUATION_STATE_VIEWABLE, state);

      // all dates set (view date in future)
      datesEval.setViewDate(etdl.tomorrow);
      state = EvalUtils.getEvaluationState(datesEval, false);
      assertEquals(EvalConstants.EVALUATION_STATE_CLOSED, state);

      // all dates set (view date in past)
      datesEval.setViewDate(etdl.yesterday);
      state = EvalUtils.getEvaluationState(datesEval, false);
      assertEquals(EvalConstants.EVALUATION_STATE_VIEWABLE, state);

      // all dates EXCEPT stop date set (view date in future)
      datesEval.setStopDate(null);
      datesEval.setViewDate(etdl.tomorrow);
      state = EvalUtils.getEvaluationState(datesEval, false);
      assertEquals(EvalConstants.EVALUATION_STATE_CLOSED, state);

      // all dates EXCEPT stop date set (view date in past)
      datesEval.setStopDate(null);
      datesEval.setViewDate(etdl.yesterday);
      state = EvalUtils.getEvaluationState(datesEval, false);
      assertEquals(EvalConstants.EVALUATION_STATE_VIEWABLE, state);

      // no exceptions thrown
   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.EvalUtils#checkStateAfter(java.lang.String, java.lang.String, boolean)}.
    */
   public void testCheckStateAfter() {
      // check that same works
      assertTrue( EvalUtils.checkStateAfter(EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.EVALUATION_STATE_INQUEUE, true) );
      assertTrue( EvalUtils.checkStateAfter(EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.EVALUATION_STATE_PARTIAL, true) );

      assertTrue( EvalUtils.checkStateAfter(EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.EVALUATION_STATE_PARTIAL, false) );
      assertTrue( EvalUtils.checkStateAfter(EvalConstants.EVALUATION_STATE_CLOSED, EvalConstants.EVALUATION_STATE_ACTIVE, false) );
      assertTrue( EvalUtils.checkStateAfter(EvalConstants.EVALUATION_STATE_VIEWABLE, EvalConstants.EVALUATION_STATE_CLOSED, false) );

      // now check the false cases
      assertFalse( EvalUtils.checkStateAfter(EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.EVALUATION_STATE_ACTIVE, false) );

      assertFalse( EvalUtils.checkStateAfter(EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.EVALUATION_STATE_ACTIVE, false) );
      assertFalse( EvalUtils.checkStateAfter(EvalConstants.EVALUATION_STATE_GRACEPERIOD, EvalConstants.EVALUATION_STATE_CLOSED, false) );
      assertFalse( EvalUtils.checkStateAfter(EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.EVALUATION_STATE_VIEWABLE, false) );
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.EvalUtils#checkStateBefore(java.lang.String, java.lang.String, boolean)}.
    */
   public void testCheckStateBefore() {
      // check that same works
      assertTrue( EvalUtils.checkStateBefore(EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.EVALUATION_STATE_ACTIVE, true) );
      assertTrue( EvalUtils.checkStateBefore(EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.EVALUATION_STATE_ACTIVE, true) );

      assertTrue( EvalUtils.checkStateBefore(EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.EVALUATION_STATE_ACTIVE, false) );
      assertTrue( EvalUtils.checkStateBefore(EvalConstants.EVALUATION_STATE_GRACEPERIOD, EvalConstants.EVALUATION_STATE_CLOSED, false) );
      assertTrue( EvalUtils.checkStateBefore(EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.EVALUATION_STATE_VIEWABLE, false) );

      // now check the false cases
      assertFalse( EvalUtils.checkStateBefore(EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.EVALUATION_STATE_INQUEUE, false) );

      assertFalse( EvalUtils.checkStateBefore(EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.EVALUATION_STATE_PARTIAL, false) );
      assertFalse( EvalUtils.checkStateBefore(EvalConstants.EVALUATION_STATE_CLOSED, EvalConstants.EVALUATION_STATE_ACTIVE, false) );
      assertFalse( EvalUtils.checkStateBefore(EvalConstants.EVALUATION_STATE_VIEWABLE, EvalConstants.EVALUATION_STATE_CLOSED, false) );
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
      Date dueDate;

      Date now = new Date();
      long nowTime = new Date().getTime();
      long hour = 1000 * 60 * 60;
      Date nowPlus2 = new Date(nowTime + hour * 2);
      Date nowPlus3 = new Date(nowTime + hour * 3);

      EvalEvaluation eval = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, "aaronz", "title",
            now, nowPlus2, nowPlus2, nowPlus3, 
            EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.SHARING_VISIBLE, 0, null);

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
      Date endOfDay;
      Date testDay;
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
      int difference;

      // test same dates
      difference = EvalUtils.getHoursDifference(startTime, endTime);
      assertEquals(0, difference);

      endTime = new Date( startTime.getTime() + (1000 * 60 * 60 * 5) );
      difference = EvalUtils.getHoursDifference(startTime, endTime);
      assertEquals(5, difference);

      difference = EvalUtils.getHoursDifference(endTime, startTime);
      assertEquals(-5, difference);

      endTime = new Date( startTime.getTime() + (1000l * 60l * 60l * 50l) );
      difference = EvalUtils.getHoursDifference(startTime, endTime);
      assertEquals(50, difference);

      endTime = new Date( startTime.getTime() + (1000l * 60l * 60l * 500l) );
      difference = EvalUtils.getHoursDifference(startTime, endTime);
      assertEquals(500, difference);

      endTime = new Date( startTime.getTime() + (1000l * 60l * 60l * 5000l) );
      difference = EvalUtils.getHoursDifference(startTime, endTime);
      assertEquals(5000, difference);

      endTime = new Date( startTime.getTime() + (1000l * 60l * 60l * 50000l) );
      difference = EvalUtils.getHoursDifference(startTime, endTime);
      assertEquals(50000, difference);

      // check that it rounds correctly
      endTime = new Date( startTime.getTime() + (1000 * 60 * 60 * 5) + (1000 * 60 * 30) );
      difference = EvalUtils.getHoursDifference(startTime, endTime);
      assertEquals(5, difference);
   }



   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.EvalUtils#getGroupsInCommon(java.util.List, java.util.List)}.
    */
   public void testGetGroupsInCommon() {
      EvalGroup[] groups;
      List<EvalGroup> evalGroups;
      List<EvalAssignGroup> assignGroups;
      EvalTestDataLoad etdl = new EvalTestDataLoad(null);

      // test all empty stuff
      evalGroups = new ArrayList<>();
      assignGroups = new ArrayList<>();
      groups = EvalUtils.getGroupsInCommon(evalGroups, assignGroups);
      assertNotNull(groups);
      assertEquals(0, groups.length);

      // test all unique
      evalGroups = new ArrayList<>();
      evalGroups.add( new EvalGroup("az", "AZ group", EvalConstants.GROUP_TYPE_PROVIDED) );
      assignGroups = new ArrayList<>();
      assignGroups.add(etdl.assign1);
      assignGroups.add(etdl.assign4);
      groups = EvalUtils.getGroupsInCommon(evalGroups, assignGroups);
      assertNotNull(groups);
      assertEquals(0, groups.length);

      // test all the same
      evalGroups = new ArrayList<>();
      evalGroups.add( new EvalGroup(EvalTestDataLoad.SITE1_REF, "AZ group", EvalConstants.GROUP_TYPE_PROVIDED) );
      assignGroups = new ArrayList<>();
      assignGroups.add(etdl.assign1);
      assignGroups.add(etdl.assign2);
      groups = EvalUtils.getGroupsInCommon(evalGroups, assignGroups);
      assertNotNull(groups);
      assertEquals(1, groups.length);

      // test 2 groups of 2 the same
      evalGroups = new ArrayList<>();
      evalGroups.add( new EvalGroup(EvalTestDataLoad.SITE1_REF, "AZ group", EvalConstants.GROUP_TYPE_PROVIDED) );
      evalGroups.add( new EvalGroup(EvalTestDataLoad.SITE2_REF, "AZ group", EvalConstants.GROUP_TYPE_PROVIDED) );
      assignGroups = new ArrayList<>();
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
      String id;
      HashSet<String> uniqueIds = new HashSet<>(); 

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
      String encoded;
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
      Integer[] decoded;
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
      EvalAnswer applicableAnswer = new EvalAnswer(null, null, null, null, null, "text", 3, null, null);
      EvalAnswer naAnswer = new EvalAnswer(null, null, null, null, null, "text", EvalConstants.NA_VALUE, null, null);
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
      EvalAnswer applicableAnswer = new EvalAnswer(null, null, null, null, null, "text", 3, null, null);
      EvalAnswer naAnswer = new EvalAnswer(null, null, null, null, null, "text", EvalConstants.NA_VALUE, null, null);
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

   /**
    * Test method for {@link org.sakaiproject.evaluation.utils.EvalUtils#makeResponseRateStringFromCounts(int, int)}.
    */
   public void testMakeResponseRateStringFromCounts() {
      assertNotNull( EvalUtils.makeResponseRateStringFromCounts(0, 10) );
      assertNotNull( EvalUtils.makeResponseRateStringFromCounts(10, 0) );
      assertNotNull( EvalUtils.makeResponseRateStringFromCounts(0, 0) );
      assertNotNull( EvalUtils.makeResponseRateStringFromCounts(20, 20) );
   }

   public void testMakeMaxLengthString() {
      String result;
      String test = "this is a string";

      result = EvalUtils.makeMaxLengthString(test, 100);
      assertNotNull(result);
      assertEquals(test, result);

      result = EvalUtils.makeMaxLengthString(test, 10);
      assertNotNull(result);
      assertEquals("this is ...", result);

      // test this leaves the string alone
      result = EvalUtils.makeMaxLengthString(test, 0);
      assertNotNull(result);
      assertEquals(test, result);

      // check null is ok
      result = EvalUtils.makeMaxLengthString(null, 100);
      assertNull(result);
   }

   public void testIsValidEmail() {
      assertTrue( EvalUtils.isValidEmail("aaronz@vt.edu") );
      assertTrue( EvalUtils.isValidEmail("aaron@caret.cam.ac.uk") );
      assertTrue( EvalUtils.isValidEmail("Aaron.Zeckoski@vt.edu") );
      assertTrue( EvalUtils.isValidEmail("aaron@long.and.really.log.domain.info") );

      assertFalse( EvalUtils.isValidEmail(null) );
      assertFalse( EvalUtils.isValidEmail("") );
      assertFalse( EvalUtils.isValidEmail("not an email") );
      assertFalse( EvalUtils.isValidEmail("not@email") );
   }

   public void testCleanupHtmlPtags() {
      String original;
      String cleanup;

      // check the trim ends cases
      original = "test with one return\n <p>&nbsp;</p> ";
      cleanup = EvalUtils.cleanupHtmlPtags(original);
      assertNotNull(cleanup);
      assertEquals("test with one return", cleanup);

      original = "test with two returns\n <p>&nbsp;</p>\n <p>&nbsp;</p>";
      cleanup = EvalUtils.cleanupHtmlPtags(original);
      assertNotNull(cleanup);
      assertEquals("test with two returns", cleanup);

      original = "test with multiple returns\n <p>&nbsp;</p> <p>&nbsp;</p>\n   <p>&nbsp;</p><p>&nbsp;</p>\n";
      cleanup = EvalUtils.cleanupHtmlPtags(original);
      assertNotNull(cleanup);
      assertEquals("test with multiple returns", cleanup);

      // test the trim surrounding cases
      original = "<p>test trimming extra surrounding</p>";
      cleanup = EvalUtils.cleanupHtmlPtags(original);
      assertNotNull(cleanup);
      assertEquals("test trimming extra surrounding", cleanup);

      original = "<p> test trimming extra surrounding</p>  ";
      cleanup = EvalUtils.cleanupHtmlPtags(original);
      assertNotNull(cleanup);
      assertEquals("test trimming extra surrounding", cleanup);

      original = "<p>line one</p>\n<p>line two</p>";
      cleanup = EvalUtils.cleanupHtmlPtags(original);
      assertNotNull(cleanup);
      assertEquals(original, cleanup);

      original = "<p> test not trimming</p>    <p>extra surrounding </p>";
      cleanup = EvalUtils.cleanupHtmlPtags(original);
      assertNotNull(cleanup);
      assertEquals(original, cleanup);

      // test both at once
      original = "<p>test with two returns</p> <p>&nbsp;</p> <p>&nbsp;</p>";
      cleanup = EvalUtils.cleanupHtmlPtags(original);
      assertNotNull(cleanup);
      assertEquals("test with two returns", cleanup);

      // check that strings that should not change do not
      original = "no p tags to cleanup";
      cleanup = EvalUtils.cleanupHtmlPtags(original);
      assertNotNull(cleanup);
      assertEquals(original, cleanup);

      original = "no p tags to cleanup, <p>they are all in the middle</p> so ok";
      cleanup = EvalUtils.cleanupHtmlPtags(original);
      assertNotNull(cleanup);
      assertEquals(original, cleanup);

      original = "<p> at the beginning</p> but not at the end";
      cleanup = EvalUtils.cleanupHtmlPtags(original);
      assertNotNull(cleanup);
      assertEquals(original, cleanup);

      original = "nothing at the beginning, <p>at the end</p>";
      cleanup = EvalUtils.cleanupHtmlPtags(original);
      assertNotNull(cleanup);
      assertEquals(original, cleanup);

      // check null and empty are ok
      original = null;
      cleanup = EvalUtils.cleanupHtmlPtags(original);
      assertNull(cleanup);

      original = "";
      cleanup = EvalUtils.cleanupHtmlPtags(original);
      assertNotNull(cleanup);
      assertEquals(original, cleanup);

      original = "   ";
      cleanup = EvalUtils.cleanupHtmlPtags(original);
      assertNotNull(cleanup);
      assertEquals(original, cleanup);
   }

}

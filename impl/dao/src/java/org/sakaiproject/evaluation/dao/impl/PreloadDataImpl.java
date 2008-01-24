/******************************************************************************
 * PreloadDataImpl.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * Antranig Basman (antranig@caret.cam.ac.uk)
 *****************************************************************************/

package org.sakaiproject.evaluation.dao.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.utils.SettingsLogicUtils;
import org.sakaiproject.evaluation.model.EvalConfig;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * This checks and preloads any data that is needed for the evaluation app
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class PreloadDataImpl {

   private static Log log = LogFactory.getLog(PreloadDataImpl.class);

   private static final String ADMIN_OWNER = "admin";

   private EvaluationDao evaluationDao;
   public void setEvaluationDao(EvaluationDao evaluationDao) {
      this.evaluationDao = evaluationDao;
   }


   // a few things we will need in the various other parts
   private EvalScale agreeDisagree;


   public void init() {
      try {
         preloadEvalConfig();
         preloadEmailTemplate();
         preloadScales();
         preloadExpertItems();
      } catch (Exception e) {
         // Better to log here since Sakai Spring reporting is a bit hit-or-miss
         log.error("Error preloading data for Evaluation", e);
      }
   }

   /**
    * Preload the default system configuration settings<br/> <b>Note:</b> If
    * you attempt to save a null value here in the preload it will cause this to
    * fail, just comment out or do not include the setting you want to "save" as
    * null to have the effect without causing a failure
    */
   @SuppressWarnings("unchecked")
   public void preloadEvalConfig() {
      // check if there are any EvalConfig items present
      int count = evaluationDao.countAll(EvalConfig.class);
      if (count == 0) {
         // Default Instructor system settings
         saveConfig(EvalSettings.INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS, true);
         saveConfig(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, true);
         saveConfig(EvalSettings.INSTRUCTOR_ALLOWED_EMAIL_STUDENTS, true);
         // leave this out to use the setting in the evaluation
         //saveConfig(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE, EvalConstants.INSTRUCTOR_OPT_OUT);

         saveConfig(EvalSettings.INSTRUCTOR_ADD_ITEMS_NUMBER, 5);

         // Default Student settings
         saveConfig(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED, true);
         saveConfig(EvalSettings.STUDENT_MODIFY_RESPONSES, false);
         saveConfig(EvalSettings.STUDENT_VIEW_RESULTS, false);

         // Default Admin settings
         saveConfig(EvalSettings.ADMIN_ADD_ITEMS_NUMBER, 5);
         saveConfig(EvalSettings.ADMIN_VIEW_BELOW_RESULTS, false);
         saveConfig(EvalSettings.ADMIN_VIEW_INSTRUCTOR_ADDED_RESULTS, false);

         // default hierarchy settings
         saveConfig(EvalSettings.DISPLAY_HIERARCHY_OPTIONS, false);
         saveConfig(EvalSettings.DISPLAY_HIERARCHY_HEADERS, false);

         // Default general settings
         saveConfig(EvalSettings.FROM_EMAIL_ADDRESS, "helpdesk@institution.edu");
         saveConfig(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS, 5);
         saveConfig(EvalSettings.NOT_AVAILABLE_ALLOWED, true);
         saveConfig(EvalSettings.ITEMS_ALLOWED_IN_QUESTION_BLOCK, 10);
         saveConfig(EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY, EvalConstants.SHARING_OWNER);
         saveConfig(EvalSettings.USE_EXPERT_TEMPLATES, true);
         saveConfig(EvalSettings.USE_EXPERT_ITEMS, true);
         saveConfig(EvalSettings.REQUIRE_COMMENTS_BLOCK, true);
         saveConfig(EvalSettings.EVAL_RECENTLY_CLOSED_DAYS, 10);
         saveConfig(EvalSettings.ITEM_USE_RESULTS_SHARING, false);

         // default is configurable (unset)
         //saveConfig(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY, false);
         saveConfig(EvalSettings.EVAL_USE_STOP_DATE, false);
         saveConfig(EvalSettings.EVAL_USE_SAME_VIEW_DATES, true);
         saveConfig(EvalSettings.EVAL_MIN_TIME_DIFF_BETWEEN_START_DUE, 4);

         log.info("Preloaded " + evaluationDao.countAll(EvalConfig.class) + " evaluation system EvalConfig items");
      }
   }

   private void saveConfig(String key, boolean value) {
      saveConfig(key, value ? "true" : "false");
   }

   private void saveConfig(String key, int value) {
      saveConfig(key, Integer.toString(value));
   }

   private void saveConfig(String key, String value) {
      evaluationDao.save(new EvalConfig(new Date(), SettingsLogicUtils.getName(key), value));
   }

   /**
    * Preload the default email template
    */
   public void preloadEmailTemplate() {

      // check if there are any emailTemplates present
      int count = evaluationDao.countAll(EvalEmailTemplate.class);
      if (count == 0) {
         evaluationDao.save(new EvalEmailTemplate(new Date(), ADMIN_OWNER,
               EvalConstants.EMAIL_CREATED_DEFAULT_TEXT, EvalConstants.EMAIL_TEMPLATE_DEFAULT_CREATED));
         evaluationDao.save(new EvalEmailTemplate(new Date(), ADMIN_OWNER,
               EvalConstants.EMAIL_AVAILABLE_DEFAULT_TEXT, EvalConstants.EMAIL_TEMPLATE_DEFAULT_AVAILABLE));
         evaluationDao.save(new EvalEmailTemplate(new Date(), ADMIN_OWNER,
               EvalConstants.EMAIL_AVAILABLE_OPT_IN_TEXT, EvalConstants.EMAIL_TEMPLATE_DEFAULT_AVAILABLE_OPT_IN));
         evaluationDao.save(new EvalEmailTemplate(new Date(), ADMIN_OWNER,
               EvalConstants.EMAIL_REMINDER_DEFAULT_TEXT, EvalConstants.EMAIL_TEMPLATE_DEFAULT_REMINDER));
         evaluationDao.save(new EvalEmailTemplate(new Date(), ADMIN_OWNER,
               EvalConstants.EMAIL_RESULTS_DEFAULT_TEXT, EvalConstants.EMAIL_TEMPLATE_DEFAULT_RESULTS));

         log.info("Preloaded " + evaluationDao.countAll(EvalEmailTemplate.class) + " evaluation EmailTemplates");
      }
   }


   /**
    * Preload the default expert built scales into the database
    */
   public void preloadScales() {

      // check if there are any scales present
      int count = evaluationDao.countAll(EvalScale.class);
      if (count == 0) {
         // NOTE: If you change the number of scales here (14 currently),
         // you will need to update the test in EvalScalesLogicImplTest also

         // initial expert scales
         agreeDisagree = saveScale("Agree disagree scale", EvalConstants.SCALE_IDEAL_HIGH,
               new String[] { "Strongly disagree", "Disagree", "Uncertain", "Agree", "Strongly agree" });
         saveScale("Disagree agree scale", EvalConstants.SCALE_IDEAL_HIGH,
               new String[] { "Strongly agree", "Agree", "Uncertain", "Disagree", "Strongly disagree" });
         saveScale("Frequency scale", EvalConstants.SCALE_IDEAL_NONE,
               new String[] { "Hardly ever", "Occasionally", "Sometimes", "Frequently", "Always" });
         saveScale("Relative rating scale", EvalConstants.SCALE_IDEAL_HIGH, 
               new String[] { "Poor", "Fair", "Good", "Excellent" });
         saveScale("Averages scale", EvalConstants.SCALE_IDEAL_NONE,
               new String[] { "Less than Average", "Average", "More than Average" });
         saveScale("Effectiveness scale", EvalConstants.SCALE_IDEAL_HIGH,
               new String[] { "Not effective", "Somewhat effective", 
               "Moderately effective", "Effective", "Very effective" });
         saveScale("Adequacy scale", EvalConstants.SCALE_IDEAL_HIGH,
               new String[] { "Unsatisfactory", "Inadequate", "Adequate", "Good", "Excellent" });
         saveScale("Relationships scale", EvalConstants.SCALE_IDEAL_NONE,
               new String[] { "Much less", "Less", "Some", "More", "Much more" });
         saveScale("Low high scale", EvalConstants.SCALE_IDEAL_NONE,
               new String[] { "Very low", "High", "Moderately high", "High", "Very high" });
         saveScale("Correctness scale", EvalConstants.SCALE_IDEAL_HIGH,
               new String[] { "No", "Somewhat", "Mostly", "Yes" });

         // measurement scales
         saveScale("Speed scale", EvalConstants.SCALE_IDEAL_MID,
               new String[] { "Too slow", "Okay", "Too fast" });
         saveScale("Size scale", EvalConstants.SCALE_IDEAL_MID,
               new String[] { "Too small", "Okay", "Too large" });
         saveScale("Length scale", EvalConstants.SCALE_IDEAL_MID,
               new String[] { "Too short", "Okay", "Too long" });

         // initial demographic scales
         saveScale("Gender scale", EvalConstants.SCALE_IDEAL_NONE, 
               new String[] { "Female", "Male" });
         saveScale("Grade (A-F) scale", EvalConstants.SCALE_IDEAL_NONE, 
               new String[] { "F", "D", "C", "B", "A", "Pass" });

         // Commented out VT specific scales -AZ
//       saveScale("Student year scale", EvalConstants.SCALE_IDEAL_NONE, 
//       new String[] { "Fresh", "Soph", "Junior", "Senior", "Master", "Doctoral" });
//       saveScale("Business major scale", EvalConstants.SCALE_IDEAL_NONE, 
//       new String[] { "MGT", "MSCI", "MKTG", "FIN", "ACIS", "ECON", "OTHER" });
//       saveScale("Business student yr scale", EvalConstants.SCALE_IDEAL_NONE,
//       new String[] { "Freshman", "Sophomore", "Junior", "Senior",	"Graduate" });
//       saveScale("Class requirements scale", EvalConstants.SCALE_IDEAL_NONE, 
//       new String[] { "Req. in Major", "Req. out of Major",
//       "Elective filling Req.", "Free Elec. in Major", "Free Elec. out of Major" });

         log.info("Preloaded " + evaluationDao.countAll(EvalScale.class) + " evaluation scales");
      }
   }

   /**
    * @param title
    * @param ideal
    * @param options
    * @return a persisted {@link EvalScale}
    */
   private EvalScale saveScale(String title, String ideal, String[] options) {
      EvalScale scale = new EvalScale(new Date(), ADMIN_OWNER, title, EvalConstants.SCALE_MODE_SCALE, 
            EvalConstants.SHARING_PUBLIC, Boolean.TRUE,
            "", ideal, options, Boolean.FALSE);
      evaluationDao.save(scale);
      return scale;
   }


   /**
    * Preload the default expert built items into the database
    */
   public void preloadExpertItems() {

      // check if there are any items present
      int count = evaluationDao.countAll(EvalItem.class);
      if (count == 0) {
         // NOTE: If you change the number of items here
         // you will need to update the test in EvalItemsLogicImplTest also

         // create expert items
         Set<EvalItem> itemSet;

         // student development
         EvalItemGroup newCategory = saveCategoryGroup("Student Development", "Determine how student development is perceived", null);

         itemSet = new HashSet<EvalItem>();
         itemSet.add( saveScaledExpertItem("I learned a good deal of factual material in this course", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         itemSet.add( saveScaledExpertItem("I gained a good understanding of principals and concepts in this field", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         itemSet.add( saveScaledExpertItem("I developed the a working knowledge of this field", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         saveObjectiveGroup("Knowledge", "", itemSet, newCategory);

         itemSet = new HashSet<EvalItem>();
         itemSet.add( saveScaledExpertItem("I participated actively in group discussions", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         itemSet.add( saveScaledExpertItem("I developed leadership skills within this group", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         itemSet.add( saveScaledExpertItem("I developed new friendships within this group", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         saveObjectiveGroup("Participation", "", itemSet, newCategory);

         itemSet = new HashSet<EvalItem>();
         itemSet.add( saveScaledExpertItem("I gained a better understanding of myself through this course", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         itemSet.add( saveScaledExpertItem("I developed a greater sense of personal responsibility", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         itemSet.add( saveScaledExpertItem("I increased my awareness of my own interests and talents", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         saveObjectiveGroup("Self-concept", "", itemSet, newCategory);

         itemSet = new HashSet<EvalItem>();
         itemSet.add( saveScaledExpertItem("Group activities contributed significantly to my learning", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         itemSet.add( saveScaledExpertItem("Collaborative group activities helped me learn the materials", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         itemSet.add( saveScaledExpertItem("Working with others in the group helpded me learn more effectively", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         saveObjectiveGroup("Interaction", "", itemSet, newCategory);


         // instructor effectiveness
         newCategory = saveCategoryGroup("Instructor Effectiveness", "Determine the perceived effectiveness of the instructor", null);

         itemSet = new HashSet<EvalItem>();
         itemSet.add( saveScaledExpertItem("The instructor explained material clearly and understandably", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
         itemSet.add( saveScaledExpertItem("The instructor handled questions well", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
         itemSet.add( saveScaledExpertItem("The instructor appeared to have a thorough knowledge of the subject and field", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
         itemSet.add( saveScaledExpertItem("The instructor taught in a manner that served my needs", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
         saveObjectiveGroup("Skill", "", itemSet, newCategory);

         itemSet = new HashSet<EvalItem>();
         itemSet.add( saveScaledExpertItem("The instructor was friendly", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
         itemSet.add( saveScaledExpertItem("The instructor was permissive and flexible", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
         itemSet.add( saveScaledExpertItem("The instructor treated students with respect", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
         saveObjectiveGroup("Climate", "", itemSet, newCategory);

         itemSet = new HashSet<EvalItem>();
         itemSet.add( saveScaledExpertItem("The instructor suggested specific ways students could improve", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
         itemSet.add( saveScaledExpertItem("The instructor gave positive feedback when students did especially well", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
         itemSet.add( saveScaledExpertItem("The instructor kept students informed of their progress", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
         saveObjectiveGroup("Feedback", "", itemSet, newCategory);


         itemSet = new HashSet<EvalItem>();
         itemSet.add( saveScaledExpertItem("Examinations covered the important aspects of the course", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         itemSet.add( saveScaledExpertItem("Exams were creative and required original thought", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         itemSet.add( saveScaledExpertItem("Exams were reasonable in length and difficulty", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         itemSet.add( saveScaledExpertItem("Examination items were clearly worded", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         itemSet.add( saveScaledExpertItem("Exam length was appropriate for the time alloted", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );

         saveCategoryGroup("Exams", "Measure the perception of examinations", itemSet);

         itemSet = new HashSet<EvalItem>();
         itemSet.add( saveScaledExpertItem("Assignments were interesting and stimulating", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         itemSet.add( saveScaledExpertItem("Assignments made students think", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         itemSet.add( saveScaledExpertItem("Assignments required a reasonable amount of time and effort", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         itemSet.add( saveScaledExpertItem("Assignments were relevant to what was presented", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
         itemSet.add( saveScaledExpertItem("Assignments were graded fairly", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );

         saveCategoryGroup("Assignments", "Measure the perception of out of class assignments", itemSet);


         log.info("Preloaded " + evaluationDao.countAll(EvalItem.class) + " evaluation items");
         log.info("Preloaded " + evaluationDao.countAll(EvalItemGroup.class) + " evaluation item groups");
      }

   }

   private EvalItem saveScaledExpertItem(String text, String description, String expertDescription, EvalScale scale, String category) {
      EvalItem item = new EvalItem(new Date(), ADMIN_OWNER, 
            text, description, EvalConstants.SHARING_PUBLIC, EvalConstants.ITEM_TYPE_SCALED,
            Boolean.TRUE, expertDescription, scale, null, Boolean.FALSE, null, 
            EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED, category, Boolean.FALSE);
      evaluationDao.save(item);
      return item;
   }

   private EvalItemGroup saveObjectiveGroup(String title, String description, Set<EvalItem> items, EvalItemGroup parentGroup) {
      EvalItemGroup group = new EvalItemGroup(new Date(), ADMIN_OWNER, EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE,
            title, description, Boolean.TRUE, parentGroup, items);
      evaluationDao.save( group );
      return group;
   }

   private EvalItemGroup saveCategoryGroup(String title, String description, Set<EvalItem> items) {
      EvalItemGroup group = new EvalItemGroup(new Date(), ADMIN_OWNER, EvalConstants.ITEM_GROUP_TYPE_CATEGORY,
            title, description, Boolean.TRUE, null, items);
      evaluationDao.save( group );
      return group;
   }

}

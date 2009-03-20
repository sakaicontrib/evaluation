/**
 * $Id$
 * $URL$
 * PreloadDataImpl.java - evaluation - Aug 21, 2006 12:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.dao;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.constant.EvalEmailConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalConfig;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.utils.SettingsLogicUtils;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;

/**
 * This checks and preloads any data that is needed for the evaluation app,
 * this should be executed with locks for cluster compatibility
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class PreloadDataImpl {

    private static Log log = LogFactory.getLog(PreloadDataImpl.class);

    private EvaluationDao dao;
    public void setDao(EvaluationDao evaluationDao) {
        this.dao = evaluationDao;
    }

    private EvalExternalLogic externalLogic;
    public void setExternalLogic(EvalExternalLogic externalLogic) {
        this.externalLogic = externalLogic;
    }

    public void preload() {
        // run the methods that will preload the data
        preloadEvalConfig();
        preloadEmailTemplate();
        preloadScales();
        preloadExpertItems();
    }

    /**
     * This checks to see if the critical preloaded data is loaded or not,
     * this currently includes system config, email templates, and scales
     * 
     * @return true if all critical data is loaded, false otherwise
     */
    public boolean checkCriticalDataPreloaded() {
        boolean preloaded = true;
        int configCount = dao.countAll(EvalConfig.class);
        if (configCount <= 0) {
            preloaded = false;
        } else {
            int emailTemplateCount = dao.countAll(EvalEmailTemplate.class);
            if (emailTemplateCount <= 0) {
                preloaded = false;
            } else {
                int scaleCount = dao.countAll(EvalScale.class);
                if (scaleCount <= 0) {
                    preloaded = false;
                }
            }
        }
        return preloaded;
    }

    // a few things we will need in the various other parts
    private String ADMIN_OWNER = "admin";
    private EvalScale agreeDisagree;

    /**
     * Preload the default system configuration settings<br/> <b>Note:</b> If
     * you attempt to save a null value here in the preload it will cause this to
     * fail, just comment out or do not include the setting you want to "save" as
     * null to have the effect without causing a failure
     */
    public void preloadEvalConfig() {
        // check if there are any EvalConfig items present
        int count = dao.countAll(EvalConfig.class);
        if (count == 0) {
            // Default Instructor system settings
            saveConfig(EvalSettings.INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS, true);
            saveConfig(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, true);
            saveConfig(EvalSettings.INSTRUCTOR_ALLOWED_EMAIL_STUDENTS, true);
            // NOTE: leave this out to default to use the setting in the evaluation
            saveConfig(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE, EvalConstants.INSTRUCTOR_REQUIRED);

            saveConfig(EvalSettings.INSTRUCTOR_ADD_ITEMS_NUMBER, 5);

            // Default Student settings
            saveConfig(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED, true);
            saveConfig(EvalSettings.STUDENT_MODIFY_RESPONSES, false);
            saveConfig(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, false);

            // Default Admin settings
            saveConfig(EvalSettings.ADMIN_ADD_ITEMS_NUMBER, 5);
            saveConfig(EvalSettings.ADMIN_VIEW_BELOW_RESULTS, false);
            saveConfig(EvalSettings.ADMIN_VIEW_INSTRUCTOR_ADDED_RESULTS, false);

            // default hierarchy settings
            saveConfig(EvalSettings.DISPLAY_HIERARCHY_OPTIONS, false);
            saveConfig(EvalSettings.DISPLAY_HIERARCHY_HEADERS, false);

            // Default general settings
            String helpdeskEmail = externalLogic.getConfigurationSetting("support.email", "helpdesk@institution.edu");
            saveConfig(EvalSettings.FROM_EMAIL_ADDRESS, helpdeskEmail);
            saveConfig(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS, 3);
            saveConfig(EvalSettings.ENABLE_NOT_AVAILABLE, true);
            saveConfig(EvalSettings.ITEMS_ALLOWED_IN_QUESTION_BLOCK, 10);
            saveConfig(EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY, EvalConstants.SHARING_OWNER);
            saveConfig(EvalSettings.USE_EXPERT_TEMPLATES, true);
            saveConfig(EvalSettings.USE_EXPERT_ITEMS, true);
            saveConfig(EvalSettings.REQUIRE_COMMENTS_BLOCK, false);
            saveConfig(EvalSettings.EVAL_RECENTLY_CLOSED_DAYS, 10);

            saveConfig(EvalSettings.ENABLE_SUMMARY_SITES_BOX, false);
            saveConfig(EvalSettings.ENABLE_EVAL_CATEGORIES, false);
            saveConfig(EvalSettings.ENABLE_EVAL_RESPONSE_REMOVAL, false);
            saveConfig(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY, false);
            saveConfig(EvalSettings.EVAL_USE_DATE_TIME, false);
            saveConfig(EvalSettings.EVAL_USE_STOP_DATE, false);
            saveConfig(EvalSettings.EVAL_USE_VIEW_DATE, false);
            saveConfig(EvalSettings.EVAL_USE_SAME_VIEW_DATES, true);
            saveConfig(EvalSettings.EVAL_MIN_TIME_DIFF_BETWEEN_START_DUE, 8);
            saveConfig(EvalSettings.ENABLE_EVAL_EARLY_CLOSE, true);
            saveConfig(EvalSettings.ENABLE_EVAL_REOPEN, true);

            // REPORTING
            saveConfig(EvalSettings.ENABLE_CSV_REPORT_EXPORT, true);
            saveConfig(EvalSettings.ENABLE_LIST_OF_TAKERS_EXPORT, true);
            saveConfig(EvalSettings.ENABLE_PDF_REPORT_BANNER, false);
            saveConfig(EvalSettings.ENABLE_PDF_REPORT_EXPORT, false);
            saveConfig(EvalSettings.ENABLE_XLS_REPORT_EXPORT, true);

            // INSTITUTIONAL SPECIFIC
            saveConfig(EvalSettings.ITEM_USE_RESULTS_SHARING, false);
            saveConfig(EvalSettings.ENABLE_IMPORTING, false);
            saveConfig(EvalSettings.ENABLE_ADHOC_GROUPS, true);
            saveConfig(EvalSettings.ENABLE_ADHOC_USERS, true);
            saveConfig(EvalSettings.ENABLE_ITEM_COMMENTS, true);
            saveConfig(EvalSettings.DISABLE_ITEM_BANK, false);
            saveConfig(EvalSettings.DISABLE_QUESTION_BLOCKS, false);

            // Default email settings
            saveConfig(EvalSettings.SINGLE_EMAIL_REMINDER_DAYS, 0);
            saveConfig(EvalSettings.EMAIL_BATCH_SIZE, 0);
            saveConfig(EvalSettings.EMAIL_WAIT_INTERVAL, 0);
            saveConfig(EvalSettings.EMAIL_DELIVERY_OPTION, EvalConstants.EMAIL_DELIVERY_DEFAULT);
            saveConfig(EvalSettings.LOG_EMAIL_RECIPIENTS, false);
            saveConfig(EvalSettings.ENABLE_SINGLE_EMAIL_PER_STUDENT, false);
            saveConfig(EvalSettings.DEFAULT_EMAIL_REMINDER_FREQUENCY, 0);
            saveConfig(EvalSettings.EVALUATION_TIME_TO_WAIT_SECS, 300);

            // Default batch performance metrics settings
            saveConfig(EvalSettings.LOG_PROGRESS_EVERY, 0);

            log.info("Preloaded " + dao.countAll(EvalConfig.class) + " evaluation system EvalConfig items");
        }
    }

    private void saveConfig(String key, boolean value) {
        saveConfig(key, value ? "true" : "false");
    }

    private void saveConfig(String key, int value) {
        saveConfig(key, Integer.toString(value));
    }

    private void saveConfig(String key, String value) {
        dao.save(new EvalConfig(SettingsLogicUtils.getName(key), value));
    }

    /**
     * Preload the default email template
     */
    public void preloadEmailTemplate() {

        // check if there are any emailTemplates present
        long count = dao.countBySearch(EvalEmailTemplate.class,
                new Search( new Restriction("defaultType", "", Restriction.NOT_NULL) ) );
        if (count == 0) {
            dao.save(new EvalEmailTemplate(ADMIN_OWNER, EvalConstants.EMAIL_TEMPLATE_CREATED, EvalEmailConstants.EMAIL_CREATED_DEFAULT_SUBJECT,
                    EvalEmailConstants.EMAIL_CREATED_DEFAULT_TEXT, EvalConstants.EMAIL_TEMPLATE_CREATED));
            dao.save(new EvalEmailTemplate(ADMIN_OWNER, EvalConstants.EMAIL_TEMPLATE_AVAILABLE, EvalEmailConstants.EMAIL_AVAILABLE_DEFAULT_SUBJECT,
                    EvalEmailConstants.EMAIL_AVAILABLE_DEFAULT_TEXT, EvalConstants.EMAIL_TEMPLATE_AVAILABLE));
            dao.save(new EvalEmailTemplate(ADMIN_OWNER, EvalConstants.EMAIL_TEMPLATE_AVAILABLE_OPT_IN, EvalEmailConstants.EMAIL_AVAILABLE_OPT_IN_SUBJECT,
                    EvalEmailConstants.EMAIL_AVAILABLE_OPT_IN_TEXT, EvalConstants.EMAIL_TEMPLATE_AVAILABLE_OPT_IN));
            dao.save(new EvalEmailTemplate(ADMIN_OWNER, EvalConstants.EMAIL_TEMPLATE_REMINDER, EvalEmailConstants.EMAIL_REMINDER_DEFAULT_SUBJECT,
                    EvalEmailConstants.EMAIL_REMINDER_DEFAULT_TEXT, EvalConstants.EMAIL_TEMPLATE_REMINDER));
            dao.save(new EvalEmailTemplate(ADMIN_OWNER, EvalConstants.EMAIL_TEMPLATE_RESULTS, EvalEmailConstants.EMAIL_RESULTS_DEFAULT_SUBJECT,
                    EvalEmailConstants.EMAIL_RESULTS_DEFAULT_TEXT, EvalConstants.EMAIL_TEMPLATE_RESULTS));
            //one email per user
            dao.save(new EvalEmailTemplate(ADMIN_OWNER,EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE, 
                    EvalEmailConstants.EMAIL_CONSOLIDATED_AVAILABLE_DEFAULT_SUBJECT,EvalEmailConstants.EMAIL_CONSOLIDATED_AVAILABLE_DEFAULT_TEXT, 
                    EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE));
            dao.save(new EvalEmailTemplate(ADMIN_OWNER,EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER, 
                    EvalEmailConstants.EMAIL_CONSOLIDATED_REMINDER_DEFAULT_SUBJECT,EvalEmailConstants.EMAIL_CONSOLIDATED_REMINDER_DEFAULT_TEXT, 
                    EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER));

            log.info("Preloaded " + dao.countAll(EvalEmailTemplate.class) + " evaluation EmailTemplates");
        }
    }


    /**
     * Preload the default expert built scales into the database
     */
    public void preloadScales() {

        // check if there are any scales present
        int count = dao.countAll(EvalScale.class);
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

            log.info("Preloaded " + dao.countAll(EvalScale.class) + " evaluation scales");
        }
    }

    /**
     * @param title
     * @param ideal
     * @param options
     * @return a persisted {@link EvalScale}
     */
    private EvalScale saveScale(String title, String ideal, String[] options) {
        EvalScale scale = new EvalScale(ADMIN_OWNER, title, EvalConstants.SCALE_MODE_SCALE, EvalConstants.SHARING_PUBLIC, 
                Boolean.TRUE, "",
                ideal, options, Boolean.FALSE);
        dao.save(scale);
        return scale;
    }


    /**
     * Preload the default expert built items into the database
     */
    public void preloadExpertItems() {

        // check if there are any items present
        int count = dao.countAll(EvalItem.class);
        if (count == 0) {
            // NOTE: If you change the number of items here
            // you will need to update the test in the logic tests

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


            log.info("Preloaded " + dao.countAll(EvalItem.class) + " evaluation items");
            log.info("Preloaded " + dao.countAll(EvalItemGroup.class) + " evaluation item groups");
        }

    }

    private EvalItem saveScaledExpertItem(String text, String description, String expertDescription, EvalScale scale, String category) {
        EvalItem item = new EvalItem(ADMIN_OWNER, text, 
                description, EvalConstants.SHARING_PUBLIC, EvalConstants.ITEM_TYPE_SCALED, Boolean.TRUE,
                expertDescription, scale, null, Boolean.FALSE, false, false, 
                null, EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED, category, Boolean.FALSE);
        dao.save(item);
        return item;
    }

    private EvalItemGroup saveObjectiveGroup(String title, String description, Set<EvalItem> items, EvalItemGroup parentGroup) {
        EvalItemGroup group = new EvalItemGroup(ADMIN_OWNER, EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE, title,
                description, Boolean.TRUE, parentGroup, items);
        dao.save( group );
        return group;
    }

    private EvalItemGroup saveCategoryGroup(String title, String description, Set<EvalItem> items) {
        EvalItemGroup group = new EvalItemGroup(ADMIN_OWNER, EvalConstants.ITEM_GROUP_TYPE_CATEGORY, title,
                description, Boolean.TRUE, null, items);
        dao.save( group );
        return group;
    }

}

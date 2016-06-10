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
package org.sakaiproject.evaluation.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    private static final Log LOG = LogFactory.getLog(PreloadDataImpl.class);

    private EvaluationDao dao;
    public void setDao(EvaluationDao evaluationDao) {
        this.dao = evaluationDao;
    }

    private EvalExternalLogic externalLogic;
    public void setExternalLogic(EvalExternalLogic externalLogic) {
        this.externalLogic = externalLogic;
    }

	/*
	 * List to hold the default email templates. Gets populated with default templates in method {@link #populateEmailTemplates()}
	 */
    List<EvalEmailTemplate> defaultEmailTempates = new ArrayList<>();
    
	/*
	 * List to hold the default configuration settings. Gets populated with default templates in method {@link #populateEvalConfig()}
	 */
    Map<String, Object> evalConfigMap = new HashMap<>();

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
        populateEvalConfig();
        long configDBCount = dao.countAll(EvalConfig.class);
        if (configDBCount <= 0) {
        	//No config settings found in DB
            preloaded = false;
        } else {
        	//there are some config settings saved, lets check if these include all settings in the evalConfigMap
        	configDBCount = countDefaultEvalConfigInDB();
        	if(configDBCount < evalConfigMap.size()){
        		//Some (not all) settings in evalConfigMap are in the DB
        		preloaded = false;
        	}else{         
        		//Get defined email templates for defaultEmailTempates list. Does not load data into DB
	        	populateEmailTemplates();
	            long emailTemplateCount = dao.countBySearch(EvalEmailTemplate.class,
	                    new Search( new Restriction("defaultType", "", Restriction.NOT_NULL) ) );
	            if (emailTemplateCount < defaultEmailTempates.size()) {
	            	//Either there are no templates loaded or some (not all) templates in defaultEmailTempates are in the DB
	                preloaded = false;
	            } else {
	                int scaleCount = dao.countAll(EvalScale.class);
	                if (scaleCount <= 0) {
	                    preloaded = false;
	                }
	            }
        	}
        }
        return preloaded;
    }

    // a few things we will need in the various other parts
    private static final String ADMIN_OWNER = "admin";
    private EvalScale agreeDisagree;

    /**
     * Preload the default system configuration settings<br/> <b>Note:</b> If
     * you attempt to save a null value here in the preload it will cause this to
     * fail, just comment out or do not include the setting you want to "save" as
     * null to have the effect without causing a failure
     */
	public void preloadEvalConfig() {
        // check if there are any EvalConfig items present in the defaults map
    	if( evalConfigMap.isEmpty()){
    		populateEvalConfig();
    	}
        List<EvalConfig> configInDB = dao.findAll(EvalConfig.class);
        
        //convert DB configurations into maps with the Config Name as key
        Map<String, String> configInDBMap = new HashMap<>();
        for ( EvalConfig config : configInDB){
        	configInDBMap.put(config.getName(), config.getValue());
        }
        
        //Now lets check and make sure that each default config in this file is actually in the DB. Load those that are loaded.
        int countNewConfigs = 0;
        Iterator<Map.Entry<String, Object>> evalConfigMapIt = evalConfigMap.entrySet().iterator();
        while (evalConfigMapIt.hasNext()) {
            Map.Entry<String, Object> configuration = evalConfigMapIt.next();
            String configName = configuration.getKey();
            Object configValue = configuration.getValue();
            
            if(! configInDBMap.containsKey(SettingsLogicUtils.getName(configName))){
            	//this default configuration is not in the DB, lets add it
            	if( configValue != null){
	            	if ( configValue.getClass().equals(Integer.class) || configValue.getClass().equals(String.class)){
	            		saveConfig(configName, configValue.toString());
	            	}else if ( configValue.getClass().equals(Boolean.class) ){
	            		saveConfig(configName, Boolean.parseBoolean(configValue.toString()) ? "true" : "false");
	            	}
            	}
            	countNewConfigs++;
            }
        }

        if( countNewConfigs > 0){
        	LOG.info("Preloaded " + countNewConfigs + " evaluation system EvalConfig items");
        }
    }

    private void saveConfig(String key, String value) {
        dao.save(new EvalConfig(SettingsLogicUtils.getName(key), value));
    }

    /**
     * Preload the default email template
     */
    public void preloadEmailTemplate() {
    	if ( defaultEmailTempates.isEmpty()){
    		populateEmailTemplates();
    	}
        // check if there are any emailTemplates present in the DB
    	List<EvalEmailTemplate> currentDefaultsList = dao.findBySearch(EvalEmailTemplate.class,
                new Search( new Restriction("defaultType", "", Restriction.NOT_NULL) ) );
    	
        // convert to map with defaultType as key
    	Map<String, EvalEmailTemplate> currentDefaultsMap = new HashMap<>();
    	for(EvalEmailTemplate emailTemplate : currentDefaultsList){
    		currentDefaultsMap.put(emailTemplate.getDefaultType(), emailTemplate);
    	}
    	
    	//Now lets check and make sure that each default template in this file is actually in the DB. Load those that are loaded.
        int count = 0;
        if (count < defaultEmailTempates.size()) {
        	for(EvalEmailTemplate emailTemplate : defaultEmailTempates){
        		if(! currentDefaultsMap.containsKey(emailTemplate.getDefaultType())){
        			//this default template is not in the DB, lets add it
        			dao.save(emailTemplate);
        			count++;
        		}
        	}
  
            LOG.info("Preloaded " + count + " evaluation EmailTemplates");
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

            LOG.info("Preloaded " + dao.countAll(EvalScale.class) + " evaluation scales");
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

            itemSet = new HashSet<>();
            itemSet.add( saveScaledExpertItem("I learned a good deal of factual material in this course", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            itemSet.add( saveScaledExpertItem("I gained a good understanding of principals and concepts in this field", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            itemSet.add( saveScaledExpertItem("I developed the a working knowledge of this field", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            saveObjectiveGroup("Knowledge", "", itemSet, newCategory);

            itemSet = new HashSet<>();
            itemSet.add( saveScaledExpertItem("I participated actively in group discussions", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            itemSet.add( saveScaledExpertItem("I developed leadership skills within this group", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            itemSet.add( saveScaledExpertItem("I developed new friendships within this group", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            saveObjectiveGroup("Participation", "", itemSet, newCategory);

            itemSet = new HashSet<>();
            itemSet.add( saveScaledExpertItem("I gained a better understanding of myself through this course", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            itemSet.add( saveScaledExpertItem("I developed a greater sense of personal responsibility", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            itemSet.add( saveScaledExpertItem("I increased my awareness of my own interests and talents", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            saveObjectiveGroup("Self-concept", "", itemSet, newCategory);

            itemSet = new HashSet<>();
            itemSet.add( saveScaledExpertItem("Group activities contributed significantly to my learning", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            itemSet.add( saveScaledExpertItem("Collaborative group activities helped me learn the materials", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            itemSet.add( saveScaledExpertItem("Working with others in the group helpded me learn more effectively", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            saveObjectiveGroup("Interaction", "", itemSet, newCategory);


            // instructor effectiveness
            newCategory = saveCategoryGroup("Instructor Effectiveness", "Determine the perceived effectiveness of the instructor", null);

            itemSet = new HashSet<>();
            itemSet.add( saveScaledExpertItem("The instructor explained material clearly and understandably", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
            itemSet.add( saveScaledExpertItem("The instructor handled questions well", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
            itemSet.add( saveScaledExpertItem("The instructor appeared to have a thorough knowledge of the subject and field", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
            itemSet.add( saveScaledExpertItem("The instructor taught in a manner that served my needs", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
            saveObjectiveGroup("Skill", "", itemSet, newCategory);

            itemSet = new HashSet<>();
            itemSet.add( saveScaledExpertItem("The instructor was friendly", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
            itemSet.add( saveScaledExpertItem("The instructor was permissive and flexible", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
            itemSet.add( saveScaledExpertItem("The instructor treated students with respect", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
            saveObjectiveGroup("Climate", "", itemSet, newCategory);

            itemSet = new HashSet<>();
            itemSet.add( saveScaledExpertItem("The instructor suggested specific ways students could improve", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
            itemSet.add( saveScaledExpertItem("The instructor gave positive feedback when students did especially well", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
            itemSet.add( saveScaledExpertItem("The instructor kept students informed of their progress", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_INSTRUCTOR) );
            saveObjectiveGroup("Feedback", "", itemSet, newCategory);


            itemSet = new HashSet<>();
            itemSet.add( saveScaledExpertItem("Examinations covered the important aspects of the course", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            itemSet.add( saveScaledExpertItem("Exams were creative and required original thought", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            itemSet.add( saveScaledExpertItem("Exams were reasonable in length and difficulty", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            itemSet.add( saveScaledExpertItem("Examination items were clearly worded", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            itemSet.add( saveScaledExpertItem("Exam length was appropriate for the time alloted", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );

            saveCategoryGroup("Exams", "Measure the perception of examinations", itemSet);

            itemSet = new HashSet<>();
            itemSet.add( saveScaledExpertItem("Assignments were interesting and stimulating", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            itemSet.add( saveScaledExpertItem("Assignments made students think", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            itemSet.add( saveScaledExpertItem("Assignments required a reasonable amount of time and effort", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            itemSet.add( saveScaledExpertItem("Assignments were relevant to what was presented", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );
            itemSet.add( saveScaledExpertItem("Assignments were graded fairly", null, null, agreeDisagree, EvalConstants.ITEM_CATEGORY_COURSE) );

            saveCategoryGroup("Assignments", "Measure the perception of out of class assignments", itemSet);

            // general catch all
            saveCategoryGroup(EvalConstants.EXPERT_ITEM_CATEGORY_TITLE, "General use items", null);
            
            LOG.info("Preloaded " + dao.countAll(EvalItem.class) + " evaluation items");
            LOG.info("Preloaded " + dao.countAll(EvalItemGroup.class) + " evaluation item groups");
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
    
    /**
     * Default email templates are defined here
     */
    private void populateEmailTemplates(){
    	defaultEmailTempates.clear();
    	defaultEmailTempates.add(new EvalEmailTemplate(ADMIN_OWNER, EvalConstants.EMAIL_TEMPLATE_CREATED, EvalEmailConstants.EMAIL_CREATED_DEFAULT_SUBJECT,
                EvalEmailConstants.EMAIL_CREATED_DEFAULT_TEXT, EvalConstants.EMAIL_TEMPLATE_CREATED));
    	defaultEmailTempates.add(new EvalEmailTemplate(ADMIN_OWNER, EvalConstants.EMAIL_TEMPLATE_AVAILABLE, EvalEmailConstants.EMAIL_AVAILABLE_DEFAULT_SUBJECT,
                EvalEmailConstants.EMAIL_AVAILABLE_DEFAULT_TEXT, EvalConstants.EMAIL_TEMPLATE_AVAILABLE));
    	defaultEmailTempates.add(new EvalEmailTemplate(ADMIN_OWNER, EvalConstants.EMAIL_TEMPLATE_AVAILABLE_EVALUATEE, EvalEmailConstants.EMAIL_AVAILABLE_EVALUATEE_DEFAULT_SUBJECT,
                EvalEmailConstants.EMAIL_AVAILABLE_EVALUATEE_DEFAULT_TEXT, EvalConstants.EMAIL_TEMPLATE_AVAILABLE_EVALUATEE));
    	defaultEmailTempates.add(new EvalEmailTemplate(ADMIN_OWNER, EvalConstants.EMAIL_TEMPLATE_AVAILABLE_OPT_IN, EvalEmailConstants.EMAIL_AVAILABLE_OPT_IN_SUBJECT,
                EvalEmailConstants.EMAIL_AVAILABLE_OPT_IN_TEXT, EvalConstants.EMAIL_TEMPLATE_AVAILABLE_OPT_IN));
    	defaultEmailTempates.add(new EvalEmailTemplate(ADMIN_OWNER, EvalConstants.EMAIL_TEMPLATE_REMINDER, EvalEmailConstants.EMAIL_REMINDER_DEFAULT_SUBJECT,
                EvalEmailConstants.EMAIL_REMINDER_DEFAULT_TEXT, EvalConstants.EMAIL_TEMPLATE_REMINDER));
    	defaultEmailTempates.add(new EvalEmailTemplate(ADMIN_OWNER, EvalConstants.EMAIL_TEMPLATE_RESULTS, EvalEmailConstants.EMAIL_RESULTS_DEFAULT_SUBJECT,
                EvalEmailConstants.EMAIL_RESULTS_DEFAULT_TEXT, EvalConstants.EMAIL_TEMPLATE_RESULTS));
        //one email per user
    	defaultEmailTempates.add(new EvalEmailTemplate(ADMIN_OWNER,EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE, 
                EvalEmailConstants.EMAIL_CONSOLIDATED_AVAILABLE_DEFAULT_SUBJECT,EvalEmailConstants.EMAIL_CONSOLIDATED_AVAILABLE_DEFAULT_TEXT, 
                EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE));
    	defaultEmailTempates.add(new EvalEmailTemplate(ADMIN_OWNER,EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER, 
                EvalEmailConstants.EMAIL_CONSOLIDATED_REMINDER_DEFAULT_SUBJECT,EvalEmailConstants.EMAIL_CONSOLIDATED_REMINDER_DEFAULT_TEXT, 
                EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER));
    	//job completion email
    	defaultEmailTempates.add(new EvalEmailTemplate(ADMIN_OWNER, EvalConstants.EMAIL_TEMPLATE_JOB_COMPLETED, EvalEmailConstants.EMAIL_JOB_COMPLETED_DEFAULT_SUBJECT,
                EvalEmailConstants.EMAIL_JOB_COMPLETED_DEFAULT_TEXT, EvalConstants.EMAIL_TEMPLATE_JOB_COMPLETED));
    	//submission confirmation template
    	defaultEmailTempates.add(new EvalEmailTemplate(ADMIN_OWNER,EvalConstants.EMAIL_TEMPLATE_SUBMITTED, 
    			EvalEmailConstants.EMAIL_SUBMITTED_DEFAULT_SUBJECT,EvalEmailConstants.EMAIL_SUBMITTED_DEFAULT_TEXT, 
    			EvalConstants.EMAIL_TEMPLATE_SUBMITTED));
    }
    
    /**
     * Default configuration settings are defined here
     */
    private void populateEvalConfig(){
    	evalConfigMap.clear();
    	
    	// Default Instructor system settings
    	evalConfigMap.put(EvalSettings.INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS, true);
        evalConfigMap.put(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, true);
        evalConfigMap.put(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_ALL_RESULTS, null);
        evalConfigMap.put(EvalSettings.INSTRUCTOR_ALLOWED_EMAIL_STUDENTS, true);
        // NOTE: leave this out to default to use the setting in the evaluation
        evalConfigMap.put(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE, EvalConstants.INSTRUCTOR_REQUIRED);

        evalConfigMap.put(EvalSettings.INSTRUCTOR_ADD_ITEMS_NUMBER, 5);

        evalConfigMap.put(EvalSettings.EVAL_MIN_LIST_LENGTH, 2);
        evalConfigMap.put(EvalSettings.EVAL_MAX_LIST_LENGTH, 40);

        // Default Student settings
        evalConfigMap.put(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED, true);
        evalConfigMap.put(EvalSettings.STUDENT_MODIFY_RESPONSES, false);
        evalConfigMap.put(EvalSettings.STUDENT_SAVE_WITHOUT_SUBMIT, false);
        evalConfigMap.put(EvalSettings.STUDENT_CANCEL_ALLOWED, false);
        evalConfigMap.put(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, false);

        // Default Admin settings
        evalConfigMap.put(EvalSettings.ENABLE_SAKAI_ADMIN_ACCESS, true);
        evalConfigMap.put(EvalSettings.ADMIN_ADD_ITEMS_NUMBER, 5);
        evalConfigMap.put(EvalSettings.ADMIN_VIEW_BELOW_RESULTS, false);
        evalConfigMap.put(EvalSettings.ADMIN_VIEW_INSTRUCTOR_ADDED_RESULTS, false);

        // default hierarchy settings
        evalConfigMap.put(EvalSettings.DISPLAY_HIERARCHY_OPTIONS, false);
        evalConfigMap.put(EvalSettings.DISPLAY_HIERARCHY_HEADERS, false);

        // Default general settings
        String helpdeskEmail = externalLogic.getConfigurationSetting("support.email", "helpdesk@institution.edu");
        evalConfigMap.put(EvalSettings.FROM_EMAIL_ADDRESS, helpdeskEmail);
        
        evalConfigMap.put(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS, 3);
        evalConfigMap.put(EvalSettings.ENABLE_NOT_AVAILABLE, true);
        evalConfigMap.put(EvalSettings.ITEMS_ALLOWED_IN_QUESTION_BLOCK, 10);
        evalConfigMap.put(EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY, EvalConstants.SHARING_OWNER);
        evalConfigMap.put(EvalSettings.ENABLE_TEMPLATE_COPYING, true);
        evalConfigMap.put(EvalSettings.USE_EXPERT_TEMPLATES, true);
        evalConfigMap.put(EvalSettings.USE_EXPERT_ITEMS, true);
        evalConfigMap.put(EvalSettings.REQUIRE_COMMENTS_BLOCK, false);
        evalConfigMap.put(EvalSettings.EVAL_RECENTLY_CLOSED_DAYS, 10);
        evalConfigMap.put(EvalSettings.EVAL_EVALUATEE_RECENTLY_CLOSED_DAYS, 10);

        evalConfigMap.put(EvalSettings.ENABLE_SUMMARY_SITES_BOX, false);
        evalConfigMap.put(EvalSettings.ENABLE_EVAL_CATEGORIES, false);
        evalConfigMap.put(EvalSettings.ENABLE_EVAL_TERM_IDS, false);
        evalConfigMap.put(EvalSettings.ENABLE_EVAL_RESPONSE_REMOVAL, false);
        evalConfigMap.put(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY, false);
        evalConfigMap.put(EvalSettings.EVAL_USE_DATE_TIME, false);
        evalConfigMap.put(EvalSettings.EVAL_USE_STOP_DATE, false);
        evalConfigMap.put(EvalSettings.EVAL_USE_VIEW_DATE, false);
        evalConfigMap.put(EvalSettings.EVAL_USE_SAME_VIEW_DATES, true);
        evalConfigMap.put(EvalSettings.EVAL_MIN_TIME_DIFF_BETWEEN_START_DUE, 8);
        evalConfigMap.put(EvalSettings.ENABLE_EVAL_EARLY_CLOSE, true);
        evalConfigMap.put(EvalSettings.ENABLE_EVAL_REOPEN, true);
        evalConfigMap.put(EvalSettings.ENABLE_MY_TOPLINKS, true);
        evalConfigMap.put(EvalSettings.ENABLE_ADMINISTRATING_BOX, true);
        evalConfigMap.put(EvalSettings.ALLOW_ALL_SITE_ROLES_TO_RESPOND, false);
        evalConfigMap.put(EvalSettings.ENABLE_GROUP_SPECIFIC_PREVIEW, false);

        // REPORTING
        evalConfigMap.put(EvalSettings.ENABLE_CSV_REPORT_EXPORT, true);
        evalConfigMap.put(EvalSettings.ENABLE_LIST_OF_TAKERS_EXPORT, true);
        evalConfigMap.put(EvalSettings.ENABLE_PDF_REPORT_BANNER, false);
        evalConfigMap.put(EvalSettings.ENABLE_PDF_REPORT_EXPORT, false);
        evalConfigMap.put(EvalSettings.ENABLE_XLS_REPORT_EXPORT, true);

        // INSTITUTIONAL SPECIFIC
        evalConfigMap.put(EvalSettings.ITEM_USE_RESULTS_SHARING, false);
        evalConfigMap.put(EvalSettings.ENABLE_IMPORTING, false);
        evalConfigMap.put(EvalSettings.ENABLE_PROVIDER_SYNC, false);
        evalConfigMap.put(EvalSettings.ENABLE_ADHOC_GROUPS, true);
        evalConfigMap.put(EvalSettings.ENABLE_ADHOC_USERS, true);
        evalConfigMap.put(EvalSettings.ENABLE_ITEM_COMMENTS, true);
        evalConfigMap.put(EvalSettings.DISABLE_ITEM_BANK, false);
        evalConfigMap.put(EvalSettings.DISABLE_QUESTION_BLOCKS, false);
        evalConfigMap.put(EvalSettings.ENABLE_FILTER_ASSIGNABLE_GROUPS, false);

        // Default email settings
        evalConfigMap.put(EvalSettings.SINGLE_EMAIL_REMINDER_DAYS, 0);
        evalConfigMap.put(EvalSettings.EMAIL_BATCH_SIZE, 0);
        evalConfigMap.put(EvalSettings.EMAIL_WAIT_INTERVAL, 0);
        evalConfigMap.put(EvalSettings.EMAIL_DELIVERY_OPTION, EvalConstants.EMAIL_DELIVERY_DEFAULT);
        evalConfigMap.put(EvalSettings.LOG_EMAIL_RECIPIENTS, false);
        evalConfigMap.put(EvalSettings.ENABLE_SINGLE_EMAIL_PER_STUDENT, false);
        evalConfigMap.put(EvalSettings.DEFAULT_EMAIL_REMINDER_FREQUENCY, 0);
        evalConfigMap.put(EvalSettings.EVALUATION_TIME_TO_WAIT_SECS, 300);
        evalConfigMap.put(EvalSettings.ALLOW_EVALSPECIFIC_TOGGLE_EMAIL_NOTIFICATION, false);
        evalConfigMap.put(EvalSettings.ENABLE_JOB_COMPLETION_EMAIL, false);
        evalConfigMap.put(EvalSettings.ENABLE_REMINDER_STATUS, false);
        evalConfigMap.put(EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_TIME, 1);
        evalConfigMap.put(EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_MINUTES, 10);
        evalConfigMap.put(EvalSettings.CONSOLIDATED_EMAIL_NOTIFY_AVAILABLE, true);

        // Default batch performance metrics settings
        evalConfigMap.put(EvalSettings.LOG_PROGRESS_EVERY, 0);
        
        // Default settings for scheduling sync with Group Provider
        evalConfigMap.put(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_GROUP_SAVE, true);
        evalConfigMap.put(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_GROUP_UPDATE, false);
        evalConfigMap.put(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_STATE_CHANGE, false);
        evalConfigMap.put(EvalSettings.SYNC_UNASSIGNED_GROUPS_ON_STARTUP, false);
	
    }
    
    /**
     * Gets the number of configurations in the database that are defined as preloadable configs
     */
    private long countDefaultEvalConfigInDB(){
    	long defaultConfigCount;
    	if (evalConfigMap.isEmpty()){
    		populateEvalConfig();
    	}
    	
    	//get default configs and trim their class definer
    	String[] configNames = evalConfigMap.keySet().toArray( new String[evalConfigMap.keySet().size()]);
    	for (int i = 0; i < configNames.length; i++){
    		configNames[i] = SettingsLogicUtils.getName(configNames[i]);
    	}
    	
    	Search search = new Search(new Restriction("name", configNames));
    	defaultConfigCount = dao.countBySearch(EvalConfig.class, search);
    	return defaultConfigCount;
    }

}

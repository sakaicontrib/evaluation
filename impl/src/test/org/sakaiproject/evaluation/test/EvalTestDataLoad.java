/**
 * EvalTestDataLoad.java - evaluation - Dec 25, 2006 10:07:31 AM - azeckoski
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.test;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalGroupNodes;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.test.mocks.MockEvalExternalLogic;
import org.sakaiproject.genericdao.api.GenericDao;


/**
 * This class holds a bunch of items used to prepopulate the database and then
 * do testing, it also handles initilization of the objects and saving
 * (Note for developers - do not modify this without permission from the author)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalTestDataLoad {

    // constants
    public final static String USER_NAME = "aaronz";
    public final static String USER_ID = "user-11111111";
    public final static String USER_DISPLAY = "Aaron Zeckoski";
    public final static String ADMIN_USER_ID = "admin";
    public final static String ADMIN_USER_NAME = "admin";
    public final static String ADMIN_USER_DISPLAY = "Administrator";
    public final static String MAINT_USER_ID = "main-22222222";
    public final static String MAINT_USER_NAME = "maintainer";
    public final static String MAINT_USER_DISPLAY = "Maint User";
    public final static String STUDENT_USER_ID = "student-12121212";
    public final static String INVALID_USER_ID = "invalid-XXXX";

    public final static String AUTHZGROUP1A_ID = "authzg-aaaaaaaa";
    public final static String AUTHZGROUP1B_ID = "authzg-bbbbbbbb";
    public final static String AUTHZGROUP2A_ID = "authzg-cccccccc";

    public final static Set<String> AUTHZGROUPSET1 = new HashSet<String>();
    public final static Set<String> AUTHZGROUPSET2 = new HashSet<String>();

    public final static String SITE1_CONTEXT = "siteC1";
    public final static String SITE1_REF = "/sites/ref-111111";
    public final static String SITE1_TITLE = "Site1 title";
    public final static String SITE2_CONTEXT = "siteC2";
    public final static String SITE2_REF = "/sites/ref-222222";
    public final static String SITE2_TITLE = "Site2 title";
    public final static String SITE3_REF = "/sites/ref-333333";

    public final static String ITEM_TEXT = "What do you think about this course?";
    public final static String ITEM_SCALE_CLASSIFICATION = EvalConstants.ITEM_TYPE_SCALED;

    public final static Boolean EXPERT = Boolean.TRUE;
    public final static Boolean NOT_EXPERT = Boolean.FALSE;

    public final static Boolean LOCKED = Boolean.TRUE;
    public final static Boolean UNLOCKED = Boolean.FALSE;

    public final static String ANSWER_TEXT = "text answer";
    public final static Integer ANSWER_SCALED_ONE = new Integer(1);
    public final static Integer ANSWER_SCALED_TWO = new Integer(2);
    public final static Integer ANSWER_SCALED_THREE = new Integer(3);

    public final static String EMAIL_MESSAGE = "This is a big long email message";

    public final static Long INVALID_LONG_ID = Long.valueOf(99999999);
    public final static String INVALID_STRING_EID = "XXXXXXX_XXXXXXXX";
    public final static String INVALID_CONTEXT = "XXXXXXXXXX";
    public final static String INVALID_CONSTANT_STRING = "XXXXXXX_XXXXXXXX";
    public final static int INVALID_CONSTANT_INT = -10;

    public final static Set<?> EMPTY_SET = new HashSet<Object>();
    public final static List<?> EMPTY_LIST = new ArrayList<Object>();
    public final static Map<?, ?> EMPTY_MAP = new HashMap<Object, Object>();
    public final static String[] EMPTY_STRING_ARRAY = new String[0];

    public final static String EVAL_CATEGORY_1 = "category one";
    public final static String EVAL_CATEGORY_2 = "category two";

    public final static String EVAL_FROM_EMAIL = "admin@eval.testing.com";

    /**
     * Used by {@link #templateItem6UU}, {@link #templateItem2A}, {@link #templateUnused}, and {@link #item4}
     */
    public final static String AUTO_USE_TAG = "official";


    // SCALES
    /**
     * Scale used in all scaled test items, LOCKED, ADMIN_USER_ID owner, public
     */
    public EvalScale scale1;
    /**
     * Scale not used in any items, UNLOCKED, MAINT_USER_ID owner, private
     */
    public EvalScale scale2;
    /**
     * Scale not used in any items, UNLOCKED, MAINT_USER_ID owner, private
     */
    public EvalScale scale3;
    /**
     * Scale not used in any items, UNLOCKED, ADMIN_USER_ID owner, private, EXPERT
     */
    public EvalScale scale4;
    /**
     * Scale used with imported items, UNLOCKED, ADMIN_USER_ID owner, public
     */
    public EvalScale scaleEid;



    // ITEMS
    /**
     * Item that is used in {@link #templateUser} and {@link #templatePublic} and {@link #templateUnused}, expert, 
     * locked, scaled, ADMIN_USER_ID owns, public
     */
    public EvalItem item1;
    /**
     * Item that is used in {@link #templateAdmin}, expert,
     * locked, scaled, MAINT_USER_ID owns, public
     */
    public EvalItem item2;
    /**
     * Item that is used in {@link #templateAdmin} and {@link #templateUnused} and {@link #templatePublicUnused}, 
     * locked, scaled, MAINT_USER_ID owns, private
     */
    public EvalItem item3;
    /**
     * Item that is not used in any template, scaled, unlocked, MAINT_USER_ID owns, private, {@value #AUTO_USE_TAG}
     */
    public EvalItem item4;
    /**
     * Item that is used in {@link #templateAdmin} and {@link #templateUser} 
     * and {@link #templateUnused}, textual, locked, MAINT_USER_ID owns, private
     */
    public EvalItem item5;
    /**
     * Item that is used in {@link #templateUserUnused}, textual, unlocked, MAINT_USER_ID owns, private, expert
     */
    public EvalItem item6;
    /**
     * Item that is unused, textual, unlocked, ADMIN_USER_ID owns, private
     */
    public EvalItem item7;
    /**
     * Item that is unused, header, unlocked, MAINT_USER_ID owns, private
     */
    public EvalItem item8;
    /**
     * Item that is a parent block item, unlocked, ADMIN_USER_ID owns, private, Compulsory
     */
    public EvalItem item9;
    /**
     * Item that is used in {@link #templateAdminComplex}, MAINT_USER_ID owns, private (used in various template items)
     */
    public EvalItem item10;
    /**
     * Item that is not used in any template, scaled, unlocked, MAINT_USER_ID owns, private
     */
    public EvalItem item11;

    /**
     * Item that is used in template with eid not null, scaled, unlocked, ADMIN_USER_ID owns, public
     */
    public EvalItem item1Eid;

    /**
     * Item that is used in template with eid not null, scaled, unlocked, ADMIN_USER_ID owns, public
     */
    public EvalItem item2Eid;

    /**
     * Item that is used in template with eid not null, scaled, unlocked, ADMIN_USER_ID owns, public
     */
    public EvalItem item3Eid;

    // TEMPLATE ITEMS
    public EvalTemplateItem templateItem1User;
    public EvalTemplateItem templateItem1P;
    public EvalTemplateItem templateItem1U;
    public EvalTemplateItem templateItem2A;
    public EvalTemplateItem templateItem3A;
    public EvalTemplateItem templateItem3U;
    public EvalTemplateItem templateItem3PU;
    public EvalTemplateItem templateItem5A;
    public EvalTemplateItem templateItem5User;
    public EvalTemplateItem templateItem5U;
    public EvalTemplateItem templateItem6UU;
    public EvalTemplateItem templateItem2B;
    public EvalTemplateItem templateItem3B;
    public EvalTemplateItem templateItem9B;
    public EvalTemplateItem templateItem10AC1;
    public EvalTemplateItem templateItem10AC2;
    public EvalTemplateItem templateItem10AC3;
    public EvalTemplateItem templateItem1Eid;
    public EvalTemplateItem templateItem2Eid;
    public EvalTemplateItem templateItem3Eid;


    // TEMPLATES
    //public EvalTemplate templateShared;
    //public EvalTemplate templateVisible;
    /**
     * Template used by admin, private, ADMIN_USER_ID owns, locked
     * <br/>Uses {@link #item2} and {@link #item3} and {@link #item5}
     */
    public EvalTemplate templateAdmin;
    /**
     * Public template not used by anyone, public, ADMIN_USER_ID owns, unlocked
     * <br/>Uses {@link #item3}
     */
    public EvalTemplate templatePublicUnused;
    /**
     * Expert template used for all, public, MAINT_USER_ID owns, locked
     * <br/>Uses {@link #item1}
     */
    public EvalTemplate templatePublic;
    /**
     * Template that is not being used, private, MAINT_USER_ID owns, unlocked
     * <br/>Uses {@link #item1}, {@link #item3} and {@link #item5}, {@link #AUTO_USE_TAG}
     */
    public EvalTemplate templateUnused;
    /**
     * Template used by user, private, USER_ID owns, locked
     * <br/>Uses {@link #item1} and {@link #item5}
     */
    public EvalTemplate templateUser;
    /**
     * Template not being used, private, USER_ID owns, unlocked, expert
     * <br/>Uses {@link #item6}
     */
    public EvalTemplate templateUserUnused;
    /**
     * Template not being used, private, ADMIN_USER_ID owns, unlocked, not expert
     * <br/>Uses NO items
     */
    public EvalTemplate templateAdminNoItems;
    /**
     * Template not being used, private, ADMIN_USER_ID owns, unlocked, not expert
     * <br/>1 block item with 2 child items {@link #item2} and {@link #item3} and parent {@link #item9}
     */
    public EvalTemplate templateAdminBlock;
    /**
     * Added type template in {@link #evaluationClosed} , private, ADMIN_USER_ID owns, unlocked, not expert
     * <br/>Includes various hierarchy items:<br/>
     * 1 instructor item {@link #templateItem10AC1} <br/>
     * 2 group items {@link #templateItem10AC2} and {@link #templateItem10AC3}
     */
    public EvalTemplate templateAdminComplex;

    /**
     * Template used by admin, public, ADMIN_USER_ID owns, unlocked
     * <br/>Uses {@link #item1Eid} and {@link #item2Eid} and {@link #item3Eid}
     */
    public EvalTemplate templateEid;

    // EVALUATIONS
    /**
     * Evaluation not started yet (starts tomorrow), MAINT_USER_ID owns, templatePublic, NO responses, No ACs, EVAL_CATEGORY_1
     */
    public EvalEvaluation evaluationNew;
    /**
     * Evaluation not started yet (starts tomorrow), ADMIN_USER_ID owns, templateAdmin, NO responses, 2 ACs, EVAL_CATEGORY_1
     */
    public EvalEvaluation evaluationNewAdmin;
    /**
     * Evaluation Active (ends today), viewable tomorrow, MAINT_USER_ID owns, templateUser, 1 response, 1 AC
     */
    public EvalEvaluation evaluationActive;
    /**
     * Evaluation Active (ends tomorrow), viewable 3 days, MAINT_USER_ID owns, templatePublic, NO responses, 1 AC,
     * NO AUTH CONTROL (thus group and permissions checks will go through automatically)
     */
    public EvalEvaluation evaluationActiveUntaken;
    /**
     * Evaluation Complete (ended yesterday, viewable tomorrow), ADMIN_USER_ID owns, templateAdmin, NO responses, 1 AC, recently closed
     */
    public EvalEvaluation evaluationClosedUntaken;
    /**
     * Evaluation Complete (ended yesterday, viewable tomorrow), ADMIN_USER_ID owns, templateAdmin/templateAdminComplex, 2 responses, 2 ACs, recently closed, EVAL_CATEGORY_2
     */
    public EvalEvaluation evaluationClosed;
    /**
     * Evaluation complete (20 days ago) and viewable (15 days ago), ADMIN_USER_ID owns, templateUser, 2 responses, 1 AC, not recently closed
     */
    public EvalEvaluation evaluationViewable;
    /**
     * Evaluation provided by an EvalGroupsProvider implementation (evaluationActive + eid set)
     */
    public EvalEvaluation evaluationProvided;
    /**
     * Evaluation which is only partially saved and has not been completely created
     */
    public EvalEvaluation evaluationPartial;
    /**
     * Evaluation which has been deleted
     */
    public EvalEvaluation evaluationDeleted;


    // EMAIL TEMPLATES

    /**
     * Email Template: owned by admin, used in {@link #evaluationNew} as available
     */
    public EvalEmailTemplate emailTemplate1;
    /**
     * Email Template: owned by maint user, used in {@link #evaluationNew} as reminder
     */
    public EvalEmailTemplate emailTemplate2;
    /**
     * Email Template: owned by maint user, used in {@link #evaluationActive} as reminder
     */
    public EvalEmailTemplate emailTemplate3;

    // ASSIGNMENTS
    /**
     * Group Assignment: MAINT_USER_ID, SITE1_REF, {@link #evaluationActive}
     */
    public EvalAssignGroup assign1;
    /**
     * Group Assignment: MAINT_USER_ID, SITE1_REF, {@link #evaluationActiveUntaken}
     */
    public EvalAssignGroup assign2;
    /**
     * Group Assignment: ADMIN_USER_ID, SITE1_REF, {@link #evaluationClosed}
     */
    public EvalAssignGroup assign3;
    /**
     * Group Assignment: MAINT_USER_ID, SITE2_REF, {@link #evaluationClosed}
     */
    public EvalAssignGroup assign4;
    /**
     * Group Assignment: ADMIN_USER_ID, SITE2_REF, {@link #evaluationViewable}
     */
    public EvalAssignGroup assign5;
    /**
     * Group Assignment: MAINT_USER_ID, SITE1_REF, {@link #evaluationNewAdmin},
     * NOT APPROVED YET
     */
    public EvalAssignGroup assign6;
    /**
     * Group Assignment: ADMIN_USER_ID, SITE2_REF, {@link #evaluationNewAdmin}
     */
    public EvalAssignGroup assign7;
    /**
     * Group Assignment: MAINT_USER_ID, SITE1_REF, {@link #evaluationPartial}
     */
    public EvalAssignGroup assign8;
    /**
     * Group Assignment: MAINT_USER_ID, SITE1_REF, {@link #evaluationDeleted}
     */
    public EvalAssignGroup assign9;
    /**
     * Group Assignment: MAINT_USER_ID, SITE1_REF, {@link #evaluationClosedUntaken}
     */
    public EvalAssignGroup assign10;
    /**
     * Group Assignment: ADMIN_USER_ID, SITE2_REF, {@link #evaluationNewAdmin} + eid
     */
    public EvalAssignGroup assignGroupProvided;

    /**
     * Hierarchy Assignment: MAINT_USER_ID, SITE1_REF, {@link #evaluationActive}
     */
    public EvalAssignHierarchy assignHier1;


    // RESPONSES
    /**
     * USER_ID, SITE1_REF, evaluationActive
     */
    public EvalResponse response1;
    /**
     * USER_ID, SITE1_REF, evaluationClosed
     */
    public EvalResponse response2;
    /**
     * STUDENT_USER_ID, SITE2_REF, evaluationClosed
     */
    public EvalResponse response3;
    /**
     * USER_ID, SITE2_REF, evaluationViewable
     */
    public EvalResponse response4;
    /**
     * STUDENT_USER_ID, SITE2_REF, evaluationViewable
     */
    public EvalResponse response5;
    /**
     * USER_ID, SITE2_REF, evaluationClosed
     */
    public EvalResponse response6;

    // ANSWERS
    public EvalAnswer answer1_1P;
    public EvalAnswer answer2_2A;
    public EvalAnswer answer2_5A;
    public EvalAnswer answer3_2A;
    public EvalAnswer answer4_1User;
    public EvalAnswer answer4_5User;
    public EvalAnswer answer5_5User;

    // ITEM GROUPS
    /**
     * Category with 2 objectives, {@link #objectiveA1} and {@link #objectiveA2} and no items, expert
     */
    public EvalItemGroup categoryA;
    /**
     * Category with no objectives and 1 item {@link #item1}, expert
     */
    public EvalItemGroup categoryB;
    /**
     * Category with no items (empty), expert
     */
    public EvalItemGroup categoryC;
    /**
     * Category with no items (empty), not expert
     */
    public EvalItemGroup categoryD;
    /**
     * Objective with 2 items, {@link #item2} and {@link #item6}, expert
     */
    public EvalItemGroup objectiveA1;
    /**
     * Objerctive with no items (empty), expert
     */
    public EvalItemGroup objectiveA2;

    // ADHOC stuff

    /**
     * owned by {@link #ADMIN_USER_ID}, user1@institution.edu,
     * in group1 and group2
     */
    public EvalAdhocUser user1;
    /**
     * owned by {@link #MAINT_USER_ID}, user2@institution.edu,
     * in NO groups
     */
    public EvalAdhocUser user2;
    /**
     * owned by {@link #MAINT_USER_ID}, user3@institution.edu,
     * in group2
     */
    public EvalAdhocUser user3;

    /**
     * Owned by admin, contains STUDENT_USER_ID, user1
     */
    public EvalAdhocGroup group1;
    /**
     * Owned by maint, contains USER_ID, user1, user3
     */
    public EvalAdhocGroup group2;


    // some date objects
    public Date twentyDaysAgo;
    public Date fifteenDaysAgo;
    public Date fourDaysAgo;
    public Date threeDaysAgo;
    public Date yesterday;
    public Date today = new Date();
    public Date tomorrow;
    public Date threeDaysFuture;
    public Date fourDaysFuture;

    // for testing nodes
    public static final String NODE_ID1 = "node1111111";
    public static final String NODE_ID2 = "node1111111";
    public EvalGroupNodes egn1 = new EvalGroupNodes(new Date(), NODE_ID1,
            new String[] {SITE1_REF, SITE2_REF});
    public EvalGroupNodes egn2 = new EvalGroupNodes(new Date(), NODE_ID2,
            new String[] {SITE3_REF});

    /**
     * For testing permissions
     */
    private MockEvalExternalLogic externalLogic = new MockEvalExternalLogic();

    /**
     * Construct the test data loader
     * 
     * @param dao a real dao which can insert data in the database,
     * if null then a fake one will be created (the data will not be truly accessible)
     */
    public EvalTestDataLoad(GenericDao dao) {
        AUTHZGROUPSET1.add(AUTHZGROUP1A_ID);
        AUTHZGROUPSET1.add(AUTHZGROUP1B_ID);
        AUTHZGROUPSET2.add(AUTHZGROUP2A_ID);

        if (dao == null) {
            dao = new FakeGenericDao();
        }
        initializeAndSave(dao);      
    }

    /**
     * initialize all the objects in this data load pea
     * (this will make sure all the public properties are not null)
     */
    public void initializeAndSave(GenericDao dao) {
        String[] options1 = {"Bad", "Average", "Good"};
        scale1 = new EvalScale(ADMIN_USER_ID, "Scale 1", EvalConstants.SCALE_MODE_SCALE, 
                EvalConstants.SHARING_PUBLIC, NOT_EXPERT, "description", 
                EvalConstants.SCALE_IDEAL_HIGH, 
                options1, LOCKED);

        String[] options2 = {"Poor", "Average", "Good", "Excellent"};
        scale2 = new EvalScale(MAINT_USER_ID, "Scale 2", EvalConstants.SCALE_MODE_SCALE, 
                EvalConstants.SHARING_PRIVATE, NOT_EXPERT, "description", 
                EvalConstants.SCALE_IDEAL_HIGH, 
                options2, UNLOCKED);

        String[] options3 = {"Male", "Female", "Unknown"};
        scale3 = new EvalScale(MAINT_USER_ID, "Scale 3", EvalConstants.SCALE_MODE_SCALE, 
                EvalConstants.SHARING_PRIVATE, NOT_EXPERT, "description", 
                EvalConstants.SCALE_IDEAL_NONE, 
                options3, UNLOCKED);

        scale4 = new EvalScale(ADMIN_USER_ID, "Scale 4", EvalConstants.SCALE_MODE_SCALE, 
                EvalConstants.SHARING_PRIVATE, EXPERT, "description", 
                EvalConstants.SCALE_IDEAL_NONE, 
                options3, UNLOCKED);

        String[] optionsEid = {"Strongly Agree", "Agree", "Neutral", "Disagree", "Strongly Disagree"};
        scaleEid = new EvalScale(ADMIN_USER_ID, "Scale Eid", EvalConstants.SCALE_MODE_SCALE,
                EvalConstants.SHARING_PUBLIC, NOT_EXPERT, "description",
                EvalConstants.SCALE_IDEAL_LOW,
                optionsEid, UNLOCKED);
        scaleEid.setEid("test-scale-1");

        // SAVE
        dao.save(scale1);
        dao.save(scale2);
        dao.save(scale3);
        dao.save(scale4);

        dao.save(scaleEid);

        item1 = new EvalItem(ADMIN_USER_ID, ITEM_TEXT, EvalConstants.SHARING_PUBLIC, 
                EvalConstants.ITEM_TYPE_SCALED, EXPERT);
        item1.setScale(scale1);
        item1.setUsesNA(true);
        item1.setScaleDisplaySetting( EvalConstants.ITEM_SCALE_DISPLAY_COMPACT );
        item1.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
        item1.setLocked(LOCKED);
        item2 = new EvalItem(MAINT_USER_ID, ITEM_TEXT, EvalConstants.SHARING_PUBLIC, 
                EvalConstants.ITEM_TYPE_SCALED, EXPERT);
        item2.setScale(scale1);
        item2.setScaleDisplaySetting( EvalConstants.ITEM_SCALE_DISPLAY_FULL );
        item2.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
        item2.setLocked(LOCKED);
        item3 = new EvalItem(MAINT_USER_ID, ITEM_TEXT, EvalConstants.SHARING_PRIVATE, 
                EvalConstants.ITEM_TYPE_SCALED, NOT_EXPERT);
        item3.setScale(scale1);
        item3.setScaleDisplaySetting( EvalConstants.ITEM_SCALE_DISPLAY_STEPPED );
        item3.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
        item3.setLocked(LOCKED);
        item4 = new EvalItem(MAINT_USER_ID, ITEM_TEXT, EvalConstants.SHARING_PRIVATE, 
                EvalConstants.ITEM_TYPE_SCALED, NOT_EXPERT);
        item4.setScale(scale1);
        item4.setScaleDisplaySetting( EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL );
        item4.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
        item4.setLocked(UNLOCKED);
        item4.setAutoUseTag(AUTO_USE_TAG);
        item5 = new EvalItem(MAINT_USER_ID, "Textual locked", EvalConstants.SHARING_PRIVATE, 
                EvalConstants.ITEM_TYPE_TEXT, NOT_EXPERT);
        item5.setDisplayRows( new Integer(2) );
        item5.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
        item5.setLocked(LOCKED);
        item6 = new EvalItem(MAINT_USER_ID, "Textual unlocked", EvalConstants.SHARING_PRIVATE, 
                EvalConstants.ITEM_TYPE_TEXT, EXPERT);
        item6.setDisplayRows( new Integer(3) );
        item6.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
        item6.setLocked(UNLOCKED);
        item7 = new EvalItem(ADMIN_USER_ID, "Textual unlocked", EvalConstants.SHARING_PRIVATE, 
                EvalConstants.ITEM_TYPE_TEXT, NOT_EXPERT);
        item7.setDisplayRows( new Integer(4) );
        item7.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
        item7.setLocked(UNLOCKED);
        item8 = new EvalItem(MAINT_USER_ID, "Header unlocked", EvalConstants.SHARING_PRIVATE, 
                EvalConstants.ITEM_TYPE_HEADER, NOT_EXPERT);
        item8.setLocked(UNLOCKED);
        item9 = new EvalItem(ADMIN_USER_ID, ITEM_TEXT, EvalConstants.SHARING_PRIVATE, 
                EvalConstants.ITEM_TYPE_BLOCK_PARENT, NOT_EXPERT);
        item9.setScale(scale1);
        item9.setScaleDisplaySetting( EvalConstants.ITEM_SCALE_DISPLAY_STEPPED );
        item9.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
        item9.setLocked(UNLOCKED);
        item9.setCompulsory(true);
        item10 = new EvalItem(MAINT_USER_ID, "Textual instructor added", EvalConstants.SHARING_PRIVATE, 
                EvalConstants.ITEM_TYPE_TEXT, NOT_EXPERT);
        item10.setDisplayRows( new Integer(4) );
        item10.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
        item10.setLocked(UNLOCKED);
        item11 = new EvalItem(MAINT_USER_ID, ITEM_TEXT, EvalConstants.SHARING_PRIVATE, 
                EvalConstants.ITEM_TYPE_SCALED, NOT_EXPERT);
        item11.setScale(scale2);
        item11.setScaleDisplaySetting( EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL );
        item11.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
        item11.setLocked(UNLOCKED);

        item1Eid = new EvalItem(ADMIN_USER_ID, ITEM_TEXT, EvalConstants.SHARING_PUBLIC, 
                EvalConstants.ITEM_TYPE_SCALED, NOT_EXPERT);
        item1Eid.setScale(scaleEid);
        item1Eid.setScaleDisplaySetting( EvalConstants.ITEM_SCALE_DISPLAY_STEPPED );
        item1Eid.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
        item1Eid.setLocked(UNLOCKED);
        item1Eid.setEid("test-item-1");

        item2Eid = new EvalItem(ADMIN_USER_ID, ITEM_TEXT, EvalConstants.SHARING_PUBLIC, 
                EvalConstants.ITEM_TYPE_SCALED, NOT_EXPERT);
        item2Eid.setScale(scaleEid);
        item2Eid.setScaleDisplaySetting( EvalConstants.ITEM_SCALE_DISPLAY_STEPPED );
        item2Eid.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
        item2Eid.setLocked(UNLOCKED);
        item2Eid.setEid("test-item-2");

        item3Eid = new EvalItem(ADMIN_USER_ID, ITEM_TEXT, EvalConstants.SHARING_PUBLIC, 
                EvalConstants.ITEM_TYPE_SCALED, NOT_EXPERT);
        item3Eid.setScale(scaleEid);
        item3Eid.setScaleDisplaySetting( EvalConstants.ITEM_SCALE_DISPLAY_STEPPED );
        item3Eid.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
        item3Eid.setLocked(UNLOCKED);
        item3Eid.setEid("test-item-3");

        // SAVE items  (NOTE: resaving further down)
        dao.save(item1);
        dao.save(item2);
        dao.save(item3);
        dao.save(item4);
        dao.save(item5);
        dao.save(item6);
        dao.save(item7);
        dao.save(item8);
        dao.save(item9);
        dao.save(item10);
        dao.save(item11);

        dao.save(item1Eid);
        dao.save(item2Eid);
        dao.save(item3Eid);


        //templateShared = new EvalTemplate(new Date(), ADMIN_USER_ID, "Template shared", EvalConstants.SHARING_SHARED, UNLOCKED, NOT_EXPERT);
        //templateVisible = new EvalTemplate(new Date(), ADMIN_USER_ID, "Template visible", EvalConstants.SHARING_VISIBLE, UNLOCKED, NOT_EXPERT);
        templateAdmin = new EvalTemplate(ADMIN_USER_ID, EvalConstants.TEMPLATE_TYPE_STANDARD, 
                "Template admin", "description", 
                EvalConstants.SHARING_PRIVATE, NOT_EXPERT, "expert desc", 
                null, LOCKED, false);
        templateAdminNoItems = new EvalTemplate(ADMIN_USER_ID, EvalConstants.TEMPLATE_TYPE_STANDARD, 
                "Template admin no items", "description", 
                EvalConstants.SHARING_PRIVATE, NOT_EXPERT, "not expert desc", 
                null, UNLOCKED, false);
        templatePublicUnused = new EvalTemplate(ADMIN_USER_ID, EvalConstants.TEMPLATE_TYPE_STANDARD, 
                "Template unused public", "description", 
                EvalConstants.SHARING_PUBLIC, NOT_EXPERT, "expert desc", 
                null, UNLOCKED, false);
        templatePublic = new EvalTemplate(MAINT_USER_ID, EvalConstants.TEMPLATE_TYPE_STANDARD, 
                "Template maint public", "description", 
                EvalConstants.SHARING_PUBLIC, EXPERT, "expert desc", 
                null, LOCKED, false);
        templateUnused = new EvalTemplate(MAINT_USER_ID, EvalConstants.TEMPLATE_TYPE_STANDARD, 
                "Template maint unused", "description", 
                EvalConstants.SHARING_PRIVATE, NOT_EXPERT, "expert desc", 
                null, UNLOCKED, false);
        templateUnused.setAutoUseTag(AUTO_USE_TAG);
        templateUser = new EvalTemplate(USER_ID, EvalConstants.TEMPLATE_TYPE_STANDARD, 
                "Template user", "description", 
                EvalConstants.SHARING_PRIVATE, NOT_EXPERT, "expert desc", 
                null, LOCKED, false);
        templateUserUnused = new EvalTemplate(USER_ID, EvalConstants.TEMPLATE_TYPE_STANDARD, 
                "Template user unused", "description", 
                EvalConstants.SHARING_PRIVATE, EXPERT, "expert desc", 
                null, UNLOCKED, false);
        templateAdminBlock = new EvalTemplate(ADMIN_USER_ID, EvalConstants.TEMPLATE_TYPE_STANDARD, 
                "Template admin with block", "description", 
                EvalConstants.SHARING_PRIVATE, NOT_EXPERT, "expert desc", 
                null, UNLOCKED, false);
        templateAdminComplex = new EvalTemplate(ADMIN_USER_ID, EvalConstants.TEMPLATE_TYPE_ADDED, 
                "Template admin added", "description", 
                EvalConstants.SHARING_PRIVATE, NOT_EXPERT, "expert desc", 
                null, UNLOCKED, false);

        templateEid = new EvalTemplate(ADMIN_USER_ID, EvalConstants.TEMPLATE_TYPE_STANDARD, 
                "Template Eid", "description", 
                EvalConstants.SHARING_PUBLIC, NOT_EXPERT, "expert desc",
                null, UNLOCKED, false);
        templateEid.setEid("test-template-1");

        // SAVE template (NOTE: resaving further down)
        //dao.save(templateShared);
        //dao.save(templateVisible);
        dao.save(templateAdmin);
        dao.save(templateAdminNoItems);
        dao.save(templatePublicUnused);
        dao.save(templatePublic);
        dao.save(templateUnused);
        dao.save(templateUser);
        dao.save(templateUserUnused);
        dao.save(templateAdminBlock);
        dao.save(templateAdminComplex);

        dao.save(templateEid);


        // assign items to templates
        templateItem1User = new EvalTemplateItem( USER_ID, templateUser, 
                item1, new Integer(1), EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE, null,
                EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, Boolean.FALSE, false, false, null, null, null);
        templateItem1P = new EvalTemplateItem( MAINT_USER_ID, templatePublic, 
                item1, new Integer(1), EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE, null,
                EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, Boolean.FALSE, false, false, null, null, null);
        templateItem1P.setCompulsory(false);
        templateItem1U = new EvalTemplateItem( MAINT_USER_ID, templateUnused, 
                item1, new Integer(1), EvalConstants.ITEM_CATEGORY_INSTRUCTOR, EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE, null,
                EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.FALSE, false, Boolean.TRUE, null, null, null); // compulsory
        templateItem2A = new EvalTemplateItem( ADMIN_USER_ID, templateAdmin, 
                item2, new Integer(1), EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE, null,
                EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.FALSE, false, false, null, null, null);
        templateItem2A.setAutoUseTag(AUTO_USE_TAG);
        templateItem3A = new EvalTemplateItem( ADMIN_USER_ID, templateAdmin, 
                item3, new Integer(2), EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE, null,
                EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL, Boolean.FALSE, false, false, null, null, null);
        templateItem3U = new EvalTemplateItem( MAINT_USER_ID, templateUnused, 
                item3, new Integer(2), EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE, null,
                EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.FALSE, false, false, null, null, null);
        templateItem3PU = new EvalTemplateItem( ADMIN_USER_ID, templatePublicUnused, 
                item3, new Integer(1), EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE, null,
                EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.FALSE, false, false, null, null, null);
        templateItem5A = new EvalTemplateItem( ADMIN_USER_ID, templateAdmin, 
                item5, new Integer(3), EvalConstants.ITEM_CATEGORY_INSTRUCTOR, EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE, new Integer(3),
                null, Boolean.FALSE, false, false, null, null, null);
        templateItem5User = new EvalTemplateItem( USER_ID, templateUser, 
                item5, new Integer(2), EvalConstants.ITEM_CATEGORY_INSTRUCTOR, EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE, new Integer(2),
                null, Boolean.FALSE, false, false, null, null, null);
        templateItem5U = new EvalTemplateItem( MAINT_USER_ID, templateUnused, 
                item5, new Integer(3), EvalConstants.ITEM_CATEGORY_INSTRUCTOR, EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE, new Integer(3),
                null, Boolean.FALSE, false, false, null, null, null);
        templateItem6UU = new EvalTemplateItem( USER_ID, templateUserUnused, 
                item6, new Integer(3), EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE, new Integer(4),
                null, Boolean.FALSE, false, false, null, null, null);
        templateItem6UU.setAutoUseTag(AUTO_USE_TAG);
        // items block
        templateItem9B = new EvalTemplateItem( ADMIN_USER_ID, templateAdminBlock, 
                item9, new Integer(1), EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE, null,
                EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, Boolean.FALSE, false, false, Boolean.TRUE, null, null);
        templateItem2B = new EvalTemplateItem( ADMIN_USER_ID, templateAdminBlock, 
                item2, new Integer(1), EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE, null,
                EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, Boolean.FALSE, false, false, Boolean.FALSE, templateItem9B.getId(), null );
        templateItem3B = new EvalTemplateItem( ADMIN_USER_ID, templateAdminBlock, 
                item3, new Integer(2), EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE, null,
                EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, Boolean.FALSE, false, false, Boolean.FALSE, templateItem9B.getId(), null );
        // added items
        templateItem10AC1 = new EvalTemplateItem( MAINT_USER_ID, templateAdminComplex, 
                item10, new Integer(1), EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_INSTRUCTOR,
                MAINT_USER_ID, new Integer(2),
                null, Boolean.FALSE, false, false, null, null, null);
        templateItem10AC2 = new EvalTemplateItem( MAINT_USER_ID, templateAdminComplex, 
                item10, new Integer(1), EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_GROUP,
                SITE1_REF, new Integer(2),
                null, Boolean.FALSE, false, false, null, null, null);
        templateItem10AC3 = new EvalTemplateItem( MAINT_USER_ID, templateAdminComplex, 
                item10, new Integer(1), EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_GROUP,
                SITE2_REF, new Integer(2),
                null, Boolean.FALSE, false, false, null, null, null);

        templateItem1Eid = new EvalTemplateItem( ADMIN_USER_ID, templateEid, 
                item1Eid, new Integer(1), EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE, null,
                EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, Boolean.FALSE, false, false, null, null, null);
        templateItem1Eid.setEid("test-templateitem-1");

        templateItem2Eid = new EvalTemplateItem( ADMIN_USER_ID, templateEid, 
                item2Eid, new Integer(2), EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE, null,
                EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, Boolean.FALSE, false, false, null, null, null);
        templateItem2Eid.setEid("test-templateitem-2");

        templateItem3Eid = new EvalTemplateItem( ADMIN_USER_ID, templateEid, 
                item3Eid, new Integer(3), EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE, null,
                EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, Boolean.FALSE, false, false, null, null, null);
        templateItem3Eid.setEid("test-templateitem-3");

        // save template items
        dao.save(templateItem1User);
        dao.save(templateItem1P);
        dao.save(templateItem1U);
        dao.save(templateItem2A);
        dao.save(templateItem3A);
        dao.save(templateItem3PU);
        dao.save(templateItem3U);
        dao.save(templateItem5A);
        dao.save(templateItem5U);
        dao.save(templateItem5User);
        dao.save(templateItem6UU);
        dao.save(templateItem9B);

        dao.save(templateItem1Eid);
        dao.save(templateItem2Eid);
        dao.save(templateItem3Eid);

        // set block id
        templateItem2B.setBlockId( templateItem9B.getId() );
        dao.save(templateItem2B);
        templateItem3B.setBlockId( templateItem9B.getId() );
        dao.save(templateItem3B);
        // added items
        dao.save(templateItem10AC1);
        dao.save(templateItem10AC2);
        dao.save(templateItem10AC3);


        // associate the templates with the link
        templateAdmin.setTemplateItems( new HashSet<EvalTemplateItem>() );
        templateAdmin.getTemplateItems().add( templateItem2A );
        templateAdmin.getTemplateItems().add( templateItem3A );
        templateAdmin.getTemplateItems().add( templateItem5A );

        templatePublicUnused.setTemplateItems( new HashSet<EvalTemplateItem>() );
        templatePublicUnused.getTemplateItems().add( templateItem3PU );

        templatePublic.setTemplateItems( new HashSet<EvalTemplateItem>() );
        templatePublic.getTemplateItems().add( templateItem1P );

        templateUnused.setTemplateItems( new HashSet<EvalTemplateItem>() );
        templateUnused.getTemplateItems().add( templateItem1U );
        templateUnused.getTemplateItems().add( templateItem3U );
        templateUnused.getTemplateItems().add( templateItem5U );

        templateUser.setTemplateItems( new HashSet<EvalTemplateItem>() );
        templateUser.getTemplateItems().add( templateItem1User );
        templateUser.getTemplateItems().add( templateItem5User );

        templateUserUnused.setTemplateItems( new HashSet<EvalTemplateItem>() );
        templateUserUnused.getTemplateItems().add( templateItem6UU );

        templateAdminBlock.setTemplateItems( new HashSet<EvalTemplateItem>() );
        templateAdminBlock.getTemplateItems().add( templateItem9B );
        templateAdminBlock.getTemplateItems().add( templateItem2B );
        templateAdminBlock.getTemplateItems().add( templateItem3B );

        templateAdminComplex.setTemplateItems( new HashSet<EvalTemplateItem>() );
        templateAdminComplex.getTemplateItems().add( templateItem10AC1 );
        templateAdminComplex.getTemplateItems().add( templateItem10AC2 );
        templateAdminComplex.getTemplateItems().add( templateItem10AC3 );


        templateEid.setTemplateItems( new HashSet<EvalTemplateItem>() );
        templateEid.getTemplateItems().add( templateItem1Eid );
        templateEid.getTemplateItems().add( templateItem2Eid );
        templateEid.getTemplateItems().add( templateItem3Eid);

        // reSAVE templates
        dao.update(templateAdmin);
        dao.update(templateAdminNoItems);
        dao.update(templatePublicUnused);
        dao.update(templatePublic);
        dao.update(templateUnused);
        dao.update(templateUser);
        dao.update(templateUserUnused);
        dao.update(templateAdminBlock);
        dao.update(templateAdminComplex);

        dao.update(templateEid);


        // associate the items with the link
        item1.setTemplateItems( new HashSet<EvalTemplateItem>() );
        item1.getTemplateItems().add( templateItem1P );
        item1.getTemplateItems().add( templateItem1User );
        item1.getTemplateItems().add( templateItem1U );

        item2.setTemplateItems( new HashSet<EvalTemplateItem>() );
        item2.getTemplateItems().add( templateItem2A );
        item2.getTemplateItems().add( templateItem2B );

        item3.setTemplateItems( new HashSet<EvalTemplateItem>() );
        item3.getTemplateItems().add( templateItem3A );
        item3.getTemplateItems().add( templateItem3PU );
        item3.getTemplateItems().add( templateItem3U );
        item3.getTemplateItems().add( templateItem3B );

        item4.setTemplateItems( new HashSet<EvalTemplateItem>() );

        item5.setTemplateItems( new HashSet<EvalTemplateItem>() );
        item5.getTemplateItems().add( templateItem5A );
        item5.getTemplateItems().add( templateItem5U );
        item5.getTemplateItems().add( templateItem5User );

        item6.setTemplateItems( new HashSet<EvalTemplateItem>() );
        item6.getTemplateItems().add( templateItem6UU );

        item9.setTemplateItems( new HashSet<EvalTemplateItem>() );
        item9.getTemplateItems().add( templateItem9B );

        item10.setTemplateItems( new HashSet<EvalTemplateItem>() );
        item10.getTemplateItems().add( templateItem10AC1 );
        item10.getTemplateItems().add( templateItem10AC2 );
        item10.getTemplateItems().add( templateItem10AC3 );


        item1Eid.setTemplateItems( new HashSet<EvalTemplateItem>() );
        item1Eid.getTemplateItems().add( templateItem1Eid );

        item2Eid.setTemplateItems( new HashSet<EvalTemplateItem>() );
        item2Eid.getTemplateItems().add( templateItem2Eid );

        item3Eid.setTemplateItems( new HashSet<EvalTemplateItem>() );
        item3Eid.getTemplateItems().add( templateItem3Eid);

        // reSAVE items
        dao.update(item1);
        dao.update(item2);
        dao.update(item3);
        dao.update(item4);
        dao.update(item5);
        dao.update(item6);
        dao.update(item7);
        dao.update(item8);
        dao.update(item9);
        dao.update(item10);
        dao.update(item11);

        dao.update(item1Eid);
        dao.update(item2Eid);
        dao.update(item3Eid);


        // init the evaluation times
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.HOUR, 2); // put today slightly in the future
        today = calendar.getTime();
        calendar.add(Calendar.DATE, -4);
        fourDaysAgo = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        threeDaysAgo = calendar.getTime();
        calendar.add(Calendar.DATE, 2);
        yesterday = calendar.getTime();
        calendar.add(Calendar.DATE, 2);
        tomorrow = calendar.getTime();
        calendar.add(Calendar.DATE, 2);
        threeDaysFuture = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        fourDaysFuture = calendar.getTime();
        calendar.setTime(today);
        calendar.add(Calendar.DATE, -15);
        fifteenDaysAgo = calendar.getTime();
        calendar.add(Calendar.DATE, -5);
        twentyDaysAgo = calendar.getTime();

        // init evaluations
        evaluationPartial = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, MAINT_USER_ID, "Eval partial", null, 
                tomorrow, null, null, null, false, null, false, null, 
                EvalConstants.EVALUATION_STATE_PARTIAL, EvalConstants.SHARING_PRIVATE, EvalConstants.INSTRUCTOR_REQUIRED, new Integer(0), null, null, null, null,
                templatePublic, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                UNLOCKED, EvalConstants.EVALUATION_AUTHCONTROL_NONE, null, null);
        // Evaluation not started yet (starts tomorrow)
        evaluationNew = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, MAINT_USER_ID, "Eval new", null, 
                tomorrow, threeDaysFuture, threeDaysFuture, fourDaysFuture, false, null, false, null, 
                EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.SHARING_VISIBLE, EvalConstants.INSTRUCTOR_OPT_IN, new Integer(1), null, null, null, null,
                templatePublic, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                UNLOCKED, EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null, null);
        // Evaluation not started yet (starts tomorrow), ADMIN
        evaluationNewAdmin = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, ADMIN_USER_ID, "Eval admin", null, 
                tomorrow, threeDaysFuture, threeDaysFuture, fourDaysFuture,  false, null, false, null, 
                EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.SHARING_VISIBLE, EvalConstants.INSTRUCTOR_OPT_IN, new Integer(1), null, null, null, null,
                templateAdmin, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                UNLOCKED, EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, EVAL_CATEGORY_1, null);
        // Evaluation Active (ends today), viewable tomorrow
        evaluationActive = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, MAINT_USER_ID, "Eval active", null, 
                yesterday, today, today, tomorrow, false, null, false, null, 
                EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.SHARING_VISIBLE, EvalConstants.INSTRUCTOR_OPT_IN, new Integer(1), EVAL_FROM_EMAIL, null, null, null,
                templateUser, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                LOCKED, EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null, null);
        //Evaluation Provided (has eid set, not null)
        evaluationProvided = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, MAINT_USER_ID, "Eval provided", null, 
                yesterday, today, today, tomorrow, false, null, false, null, 
                EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.SHARING_VISIBLE, EvalConstants.INSTRUCTOR_REQUIRED, new Integer(1), null, null, null, null,
                templateUser, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                LOCKED, EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null, null);
        evaluationProvided.setEid("test-eid");

        // Evaluation Active (open until tomorrow), immediate viewing
        evaluationActiveUntaken = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, MAINT_USER_ID, "Eval active not taken", null, 
                yesterday, tomorrow, null, null, false, null, false, null, 
                EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.SHARING_VISIBLE, EvalConstants.INSTRUCTOR_REQUIRED, new Integer(1), EVAL_FROM_EMAIL, null, null, null,
                templatePublic, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                UNLOCKED, EvalConstants.EVALUATION_AUTHCONTROL_NONE, EVAL_CATEGORY_1, null);
        // evaluation in the DUE state
        //    evaluationDueUntaken = new EvalEvaluation(new Date(), MAINT_USER_ID, "Eval due not taken", null, 
        //    threeDaysAgo, yesterday, tomorrow, threeDaysFuture, null, null,
        //    EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.INSTRUCTOR_OPT_IN, 
        //    new Integer(1), null, null, null, null, templatePublic, null,
        //    Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, UNLOCKED);
        // Evaluation Complete (ended yesterday, viewable tomorrow), recent close
        evaluationClosed = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, ADMIN_USER_ID, "Eval closed", null, 
                threeDaysAgo, yesterday, yesterday, tomorrow, true, null, true, null, 
                EvalConstants.EVALUATION_STATE_CLOSED, EvalConstants.SHARING_VISIBLE, EvalConstants.INSTRUCTOR_OPT_IN, new Integer(2), null, null, null, null,
                templateAdmin, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                LOCKED, EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, EVAL_CATEGORY_2, null);
        // Evaluation Complete (ended yesterday, viewable tomorrow), recent close
        evaluationClosedUntaken = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, ADMIN_USER_ID, "Eval closed untaken", null, 
                threeDaysAgo, yesterday, yesterday, tomorrow, true, null, true, null, 
                EvalConstants.EVALUATION_STATE_CLOSED, EvalConstants.SHARING_VISIBLE, EvalConstants.INSTRUCTOR_REQUIRED, new Integer(0), null, null, null, null,
                templateAdmin, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                UNLOCKED, EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null, null);
        // evaluation complete (20 days ago) and viewable (15 ago), also viewable to instructors but not students (til tomorrow)
        evaluationViewable = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, ADMIN_USER_ID, "Eval viewable", null, 
                twentyDaysAgo, twentyDaysAgo, twentyDaysAgo, fifteenDaysAgo, true, tomorrow, true, null, 
                EvalConstants.EVALUATION_STATE_VIEWABLE, EvalConstants.SHARING_VISIBLE, EvalConstants.INSTRUCTOR_OPT_IN, new Integer(2), null, null, null, null,
                templateUser, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                LOCKED, EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null, null);

        evaluationDeleted = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, MAINT_USER_ID, "Eval deleted", null, 
                fifteenDaysAgo, fourDaysAgo, null, null, false, null, false, null, 
                EvalConstants.EVALUATION_STATE_DELETED, EvalConstants.SHARING_PUBLIC, EvalConstants.INSTRUCTOR_REQUIRED, new Integer(0), null, null, null, null,
                templateUser, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                UNLOCKED, EvalConstants.EVALUATION_AUTHCONTROL_NONE, null, null);

        // email templates
        emailTemplate1 = new EvalEmailTemplate(ADMIN_USER_ID, EvalConstants.EMAIL_TEMPLATE_AVAILABLE, "Email Subject 1", "Email Template 1");
        evaluationNew.setAvailableEmailTemplate(emailTemplate1);
        emailTemplate2 = new EvalEmailTemplate(MAINT_USER_ID, EvalConstants.EMAIL_TEMPLATE_REMINDER, "Email Subject 2", "Email Template 2"); 
        evaluationNew.setReminderEmailTemplate(emailTemplate2);
        emailTemplate3 = new EvalEmailTemplate(MAINT_USER_ID, EvalConstants.EMAIL_TEMPLATE_REMINDER, "Email Subject 3", "Email Template 3"); 
        evaluationActive.setReminderEmailTemplate(emailTemplate3);

        dao.save(emailTemplate1);
        dao.save(emailTemplate2);
        dao.save(emailTemplate3);

        dao.save(evaluationPartial);
        dao.save(evaluationNew);
        dao.save(evaluationNewAdmin);
        dao.save(evaluationActive);
        dao.save(evaluationActiveUntaken);
        dao.save(evaluationClosedUntaken);
        dao.save(evaluationClosed);
        dao.save(evaluationViewable);
        dao.save(evaluationProvided);
        dao.save(evaluationDeleted);


        // evalGroupId assignments
        assign1 = new EvalAssignGroup( MAINT_USER_ID, SITE1_REF, EvalConstants.GROUP_TYPE_SITE, 
                evaluationActive, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        assign2 = new EvalAssignGroup( MAINT_USER_ID, SITE1_REF, EvalConstants.GROUP_TYPE_SITE, 
                evaluationActiveUntaken, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        assign3 = new EvalAssignGroup( MAINT_USER_ID, SITE1_REF, EvalConstants.GROUP_TYPE_SITE, 
                evaluationClosed, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        assign4 = new EvalAssignGroup( MAINT_USER_ID, SITE2_REF, EvalConstants.GROUP_TYPE_SITE, 
                evaluationClosed, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE);
        assign5 = new EvalAssignGroup( ADMIN_USER_ID, SITE2_REF, EvalConstants.GROUP_TYPE_SITE, 
                evaluationViewable, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
        assign6 = new EvalAssignGroup( ADMIN_USER_ID, SITE1_REF, EvalConstants.GROUP_TYPE_SITE, 
                evaluationNewAdmin, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE);
        assign7 = new EvalAssignGroup( ADMIN_USER_ID, SITE2_REF, EvalConstants.GROUP_TYPE_SITE, 
                evaluationNewAdmin, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        assign8 = new EvalAssignGroup( MAINT_USER_ID, SITE1_REF, EvalConstants.GROUP_TYPE_SITE, 
                evaluationPartial, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        assign9 = new EvalAssignGroup( MAINT_USER_ID, SITE1_REF, EvalConstants.GROUP_TYPE_SITE, 
                evaluationDeleted, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        assign10 = new EvalAssignGroup( MAINT_USER_ID, SITE1_REF, EvalConstants.GROUP_TYPE_SITE, 
                evaluationClosedUntaken, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        // Dick, you cannot assign 2 groups to an eval with the same evalGroupId... I have fixed this by making up a fake id -AZ
        assignGroupProvided = new EvalAssignGroup( ADMIN_USER_ID, "AZ-new-ref", EvalConstants.GROUP_TYPE_SITE, 
                evaluationNewAdmin, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        assignGroupProvided.setEid("test-eid");

        assignHier1 = new EvalAssignHierarchy( MAINT_USER_ID, "1", evaluationActive, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, null, null);


        dao.save(assign1);
        dao.save(assign2);
        dao.save(assign3);
        dao.save(assign4);
        dao.save(assign5);
        dao.save(assign6);
        dao.save(assign7);
        dao.save(assign8);
        dao.save(assign9);
        dao.save(assign10);
        dao.save(assignGroupProvided);

        dao.save(assignHier1);

        // create user assignments
        int counter = 0;
        counter += makeUserAssigns(dao, assign1);
        counter += makeUserAssigns(dao, assign2);
        counter += makeUserAssigns(dao, assign3);
        counter += makeUserAssigns(dao, assign4);
        counter += makeUserAssigns(dao, assign5);
        counter += makeUserAssigns(dao, assign6);
        counter += makeUserAssigns(dao, assign7);
        counter += makeUserAssigns(dao, assign8);
        counter += makeUserAssigns(dao, assign9);
        counter += makeUserAssigns(dao, assign10);
        if (counter != 23) {
            throw new IllegalStateException("Invalid user assignments creation, 23 != " + counter);
        }

        // now init response data for the evaluationSetupService
        response1 = new EvalResponse(USER_ID, SITE1_REF, evaluationActive, 
                new Date(), today, null);
        response2 = new EvalResponse(USER_ID, SITE1_REF, evaluationClosed, 
                new Date(), today, null);
        response3 = new EvalResponse(STUDENT_USER_ID, SITE2_REF, evaluationClosed, 
                new Date(), today, null);
        response4 = new EvalResponse(USER_ID, SITE2_REF, evaluationViewable, 
                new Date(), today, null);
        response5 = new EvalResponse(STUDENT_USER_ID, SITE2_REF, evaluationViewable, 
                new Date(), today, null);
        response6 = new EvalResponse(USER_ID, SITE2_REF, evaluationClosed, 
                new Date(), today, null);

        // SAVE responses  (NOTE: resaving further down)
        dao.save(response1);
        dao.save(response2);
        dao.save(response3);
        dao.save(response4);
        dao.save(response5);
        dao.save(response6);


        answer1_1P = new EvalAnswer(response1, templateItem1P, item1, null, null, null, ANSWER_SCALED_ONE, null, null);

        answer2_2A = new EvalAnswer(response2, templateItem2A, item2, null, null, null, ANSWER_SCALED_ONE, null, null);
        answer2_5A = new EvalAnswer(response2, templateItem5A, item5, MAINT_USER_ID, EvalConstants.ITEM_CATEGORY_INSTRUCTOR, ANSWER_TEXT, null, null, null);

        answer3_2A = new EvalAnswer(response3, templateItem2A, item2, null, null, null, ANSWER_SCALED_TWO, null, null);
        // left the text answer blank

        answer4_1User = new EvalAnswer(response4, templateItem1User, item1, null, null, null, ANSWER_SCALED_THREE, null, null);
        answer4_5User = new EvalAnswer(response4, templateItem5User, item5, MAINT_USER_ID, EvalConstants.ITEM_CATEGORY_INSTRUCTOR, ANSWER_TEXT, null, null, null);

        answer5_5User = new EvalAnswer(response5, templateItem5User, item5, MAINT_USER_ID, EvalConstants.ITEM_CATEGORY_INSTRUCTOR, null, ANSWER_SCALED_TWO, null, null);
        // left the text answer blank


        dao.save(answer1_1P);
        dao.save(answer2_2A);
        dao.save(answer2_5A);
        dao.save(answer3_2A);
        dao.save(answer4_1User);
        dao.save(answer4_5User);
        dao.save(answer5_5User);


        // associate the answers
        Set<EvalAnswer> answers = new HashSet<EvalAnswer>();
        answers.add(answer1_1P);
        response1.setAnswers(answers);

        answers = new HashSet<EvalAnswer>();
        answers.add(answer2_2A);
        answers.add(answer2_5A);
        response2.setAnswers(answers);

        answers = new HashSet<EvalAnswer>();
        answers.add(answer3_2A);
        response3.setAnswers(answers);

        answers = new HashSet<EvalAnswer>();
        answers.add(answer4_1User);
        answers.add(answer4_5User);
        response4.setAnswers(answers);

        answers = new HashSet<EvalAnswer>();
        answers.add(answer5_5User);
        response5.setAnswers(answers);

        answers = new HashSet<EvalAnswer>();
        response6.setAnswers(answers); // left all answers blank

        // reSAVE responses
        dao.update(response1);
        dao.update(response2);
        dao.update(response3);
        dao.update(response4);
        dao.update(response5);
        dao.update(response6);


        // catgories and objectives
        categoryA = new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.ITEM_GROUP_TYPE_CATEGORY, "A",
                "description", Boolean.TRUE, null, null);
        Set<EvalItem> itemsB = new HashSet<EvalItem>();
        itemsB.add( item1 );
        categoryB = new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.ITEM_GROUP_TYPE_CATEGORY, "B",
                "description", Boolean.TRUE, null, itemsB);
        categoryC = new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.ITEM_GROUP_TYPE_CATEGORY, "C",
                "description", Boolean.TRUE, null, null);
        categoryD = new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.ITEM_GROUP_TYPE_CATEGORY, "D",
                "description", Boolean.FALSE, null, null);

        dao.save(categoryA);
        dao.save(categoryB);
        dao.save(categoryC);
        dao.save(categoryD);


        Set<EvalItem> itemsA1 = new HashSet<EvalItem>();
        itemsA1.add( item2 );
        itemsA1.add( item6 );
        objectiveA1 = new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE, "A1",
                "description", Boolean.TRUE, categoryA, itemsA1);
        objectiveA2 = new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE, "A2",
                "description", Boolean.TRUE, categoryA, null);

        dao.save(objectiveA1);
        dao.save(objectiveA2);

        // can save nodes anytime
        dao.save(egn1);
        dao.save(egn2);


        // ADHOC groups and users

        user1 = new EvalAdhocUser(ADMIN_USER_ID, "user1@institution.edu", "user1", "User One", EvalAdhocUser.TYPE_EVALUATOR);
        user2 = new EvalAdhocUser(MAINT_USER_ID, "user2@institution.edu", "user2", "User Two", EvalAdhocUser.TYPE_EVALUATOR);
        user3 = new EvalAdhocUser(MAINT_USER_ID, "user3@institution.edu", "user3", "User Three", EvalAdhocUser.TYPE_EVALUATOR);


        dao.save(user1);
        dao.save(user2);
        dao.save(user3);

        group1 = new EvalAdhocGroup(ADMIN_USER_ID, "group 1", new String[] { STUDENT_USER_ID, user1.getUserId() }, null);
        group2 = new EvalAdhocGroup(MAINT_USER_ID, "group 2", new String[] { USER_ID, user1.getUserId(), user3.getUserId() }, null);

        // need to do this because the ids are null still otherwise
        group1.setParticipantIds( new String[] { STUDENT_USER_ID, user1.getUserId() } );
        group2.setParticipantIds( new String[] { USER_ID, user1.getUserId(), user3.getUserId() } );

        dao.save(group1);
        dao.save(group2);

    }

    private int makeUserAssigns(GenericDao dao, EvalAssignGroup assignGroup) {
        List<String> userIds = new ArrayList<String>();
        List<String> userTypes = new ArrayList<String>();
        // make the list of users and types from the perms
        Set<String> instIds = externalLogic.getUserIdsForEvalGroup(assignGroup.getEvalGroupId(), EvalConstants.PERM_BE_EVALUATED);
        for (String userId : instIds) {
            userIds.add(userId);
            userTypes.add(EvalAssignUser.TYPE_EVALUATEE);
        }
        Set<String> assistantIds = externalLogic.getUserIdsForEvalGroup(assignGroup.getEvalGroupId(), EvalConstants.PERM_ASSISTANT_ROLE);
        for (String userId : assistantIds) {
            userIds.add(userId);
            userTypes.add(EvalAssignUser.TYPE_ASSISTANT);
        }
        Set<String> takerIds = externalLogic.getUserIdsForEvalGroup(assignGroup.getEvalGroupId(), EvalConstants.PERM_TAKE_EVALUATION);
        for (String userId : takerIds) {
            userIds.add(userId);
            userTypes.add(EvalAssignUser.TYPE_EVALUATOR);
        }
        String owner = assignGroup.getOwner();
        for (int i = 0; i < userIds.size(); i++) {
            String userId = userIds.get(i);
            String type = userTypes.get(i);
            EvalAssignUser userAssign = new EvalAssignUser(userId, assignGroup.getEvalGroupId(), owner, type, EvalAssignUser.STATUS_LINKED);
            userAssign.setEvaluation(assignGroup.getEvaluation());
            userAssign.setAssignGroupId(assignGroup.getId());
            dao.save(userAssign);
            userAssign.getId();
        }
        return userIds.size();
    }

    /**
     * Take a collection of persistent objects and turn it into a list of the unique ids<br/>
     * Objects in collection must have a Long getId() method<br/>
     * Uses some fun reflection to figure out the IDs
     * 
     * @param c a collection of persistent objects
     * @return a list of IDs (Long)
     */
    public static List<Long> makeIdList(Collection<?> c) {
        List<Long> l = new ArrayList<Long>();
        for (Iterator<?> iter = c.iterator(); iter.hasNext();) {
            Serializable element = (Serializable) iter.next();
            Long id = null;
            try {
                Class<?> elementClass = element.getClass();
                Method getIdMethod = elementClass.getMethod("getId", new Class[] {});
                id = (Long) getIdMethod.invoke(element, (Object[])null);
                l.add(id);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Failed to make id list from collection",e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to make id list from collection",e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Failed to make id list from collection",e);
            }
        }
        return l;
    }

    /**
     * A fake generic dao which will stand in for a real one in the case of saving stuff
     * 
     * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
     */
    @SuppressWarnings("unchecked")
    public class FakeGenericDao implements GenericDao {

        int idCounter = 0;

        public Object findById(Class entityClass, Serializable id) {
            throw new UnsupportedOperationException();
        }

        public String getIdProperty(Class entityClass) {
            return "id";
        }

        public List getPersistentClasses() {
            throw new UnsupportedOperationException();
        }

        public void invokeTransactionalAccess(Runnable toinvoke) {
            throw new UnsupportedOperationException();
        }

        public void setPersistentClasses(List classes) {
            throw new UnsupportedOperationException();
        }

        public void create(Object object) {
            throw new UnsupportedOperationException();
        }

        public void delete(Object object) {
            throw new UnsupportedOperationException();
        }

        public boolean delete(Class entityClass, Serializable id) {
            throw new UnsupportedOperationException();
        }

        public void save(Object object) {
            Class<?> elementClass = object.getClass();
            try {
                Method getIdMethod = elementClass.getMethod("getId", new Class[] {});
                Method setIdMethod = elementClass.getMethod("setId", new Class[] {Long.class});
                Long id = (Long) getIdMethod.invoke(object, (Object[])null);
                if (id == null) {
                    id = new Long(idCounter++);
                    setIdMethod.invoke(object, new Object[] { id });
                }
            } catch (SecurityException e) {
                throw new RuntimeException("Failed to get id methods from object",e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Failed to get id methods from object",e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Failed to get id methods from object",e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to get id methods from object",e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Failed to get id methods from object",e);
            }
        }

        public void update(Object object) {
            // OK
        }

    };

}

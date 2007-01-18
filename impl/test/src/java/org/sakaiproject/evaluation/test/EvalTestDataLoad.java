/******************************************************************************
 * EvalTestDataLoad.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

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

import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignContext;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;


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

	public final static Set AUTHZGROUPSET1 = new HashSet();
	public final static Set AUTHZGROUPSET2 = new HashSet();

	public final static String SITE_ID = "site-1111111";
	public final static String SITE_REF = "siteref-1111111";
	public final static String SITE2_ID = "site-22222222";
	public final static String SITE2_REF = "siteref-22222222";

	public final static String CONTEXT1 = "testContext1";
	public final static String CONTEXT1_TITLE = "C1 title";
	public final static String CONTEXT2 = "testContext2";
	public final static String CONTEXT2_TITLE = "C2 title";
	public final static String CONTEXT3 = "testContext3";

	public final static String ITEM_TEXT = "What do you think about this course?";
	public final static String ITEM_SCALE_CLASSIFICATION = EvalConstants.ITEM_TYPE_SCALED;

	public final static Boolean EXPERT = Boolean.TRUE;
	public final static Boolean NOT_EXPERT = Boolean.FALSE;

	public final static Boolean LOCKED = Boolean.TRUE;
	public final static Boolean UNLOCKED = Boolean.FALSE;

	public final static String ANSWER_TEXT = "text answer";
	public final static Integer ANSWER_SCALED_ONE = Integer.valueOf(1);
	public final static Integer ANSWER_SCALED_TWO = Integer.valueOf(2);
	public final static Integer ANSWER_SCALED_THREE = Integer.valueOf(3);

	public final static String EMAIL_MESSAGE = "This is a big long email message";
	
	public final static Long INVALID_LONG_ID = Long.valueOf(99999999);
	public final static String INVALID_CONTEXT = "XXXXXXXXXX";
	public final static String INVALID_CONSTANT_STRING = "XXXXXXX_XXXXXXXX";
	public final static int INVALID_CONSTANT_INT = -10;

	public final static Set EMPTY_SET = new HashSet();
	public final static List EMPTY_LIST = new ArrayList();
	public final static Map EMPTY_MAP = new HashMap();

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
	 * Scale not used in any items, UNLOCKED, ADMIN_USER_ID owner, private
	 */
	public EvalScale scale3;
	/**
	 * Scale not used in any items, UNLOCKED, ADMIN_USER_ID owner, private, EXPERT
	 */
	public EvalScale scale4;

	// ITEMS
	/**
	 * Item that is used in {@link #templateUser} and {@link #templatePublic}, expert, 
	 * locked, scaled, ADMIN_USER_ID owns, public
	 */
	public EvalItem item1;
	/**
	 * Item that is used in {@link #templateAdmin}, 
	 * locked, scaled, MAINT_USER_ID owns, public
	 */
	public EvalItem item2;
	/**
	 * Item that is used in {@link #templateAdmin} and {@link #templateUnused} and {@link #templatePublicUnused}, 
	 * locked, scaled, MAINT_USER_ID owns, private
	 */
	public EvalItem item3;
	/**
	 * Item that is not used in any template, scaled, unlocked, MAINT_USER_ID owns, private
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

	// TEMPLATE ITEMS
	public EvalTemplateItem templateItem1User;
	public EvalTemplateItem templateItem1P;
	public EvalTemplateItem templateItem2A;
	public EvalTemplateItem templateItem3A;
	public EvalTemplateItem templateItem3U;
	public EvalTemplateItem templateItem3PU;
	public EvalTemplateItem templateItem5A;
	public EvalTemplateItem templateItem5User;
	public EvalTemplateItem templateItem5U;
	public EvalTemplateItem templateItem6UU;


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
	 * <br/>Uses {@link #item3} and {@link #item5}
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

	// EVALUATIONS
	/**
	 * Evaluation not started yet (starts tomorrow), MAINT_USER_ID owns, templatePublic, NO responses, No ACs
	 */
	public EvalEvaluation evaluationNew;
	/**
	 * Evaluation not started yet (starts tomorrow), ADMIN_USER_ID owns, templateAdmin, NO responses, 2 ACs
	 */
	public EvalEvaluation evaluationNewAdmin;
	/**
	 * Evaluation Active (ends today), viewable tomorrow, MAINT_USER_ID owns, templatePublic, 1 response, 1 AC
	 */
	public EvalEvaluation evaluationActive;
	/**
	 * Evaluation Active (ends tomorrow), viewable 3 days, MAINT_USER_ID owns, templatePublic, NO responses, 1 AC
	 */
	public EvalEvaluation evaluationActiveUntaken;
	/**
	 * Evaluation Complete (ended yesterday, viewable tomorrow), ADMIN_USER_ID owns, templateAdmin, 2 responses, 2 ACs, recently closed
	 */
	public EvalEvaluation evaluationClosed;
	/**
	 * Evaluation complete (20 days ago) and viewable (15 days ago), ADMIN_USER_ID owns, templateUser, 2 responses, 1 AC, not recently closed
	 */
	public EvalEvaluation evaluationViewable;

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
	 * Context Assignment: MAINT_USER_ID, CONTEXT1, {@link #evaluationActive}
	 */
	public EvalAssignContext assign1;
	/**
	 * Context Assignment: MAINT_USER_ID, CONTEXT1, {@link #evaluationActiveUntaken}
	 */
	public EvalAssignContext assign2;
	/**
	 * Context Assignment: ADMIN_USER_ID, CONTEXT1, {@link #evaluationClosed}
	 */
	public EvalAssignContext assign3;
	/**
	 * Context Assignment: MAINT_USER_ID, CONTEXT2, {@link #evaluationClosed}
	 */
	public EvalAssignContext assign4;
	/**
	 * Context Assignment: ADMIN_USER_ID, CONTEXT2, {@link #evaluationViewable}
	 */
	public EvalAssignContext assign5;
	/**
	 * Context Assignment: MAINT_USER_ID, CONTEXT1, {@link #evaluationNewAdmin}
	 */
	public EvalAssignContext assign6;
	/**
	 * Context Assignment: ADMIN_USER_ID, CONTEXT2, {@link #evaluationNewAdmin}
	 */
	public EvalAssignContext assign7;

	// RESPONSES
	/**
	 * USER_ID, CONTEXT1, evaluationActive
	 */
	public EvalResponse response1;
	/**
	 * USER_ID, CONTEXT1, evaluationClosed
	 */
	public EvalResponse response2;
	/**
	 * STUDENT_USER_ID, CONTEXT2, evaluationClosed
	 */
	public EvalResponse response3;
	/**
	 * USER_ID, CONTEXT2, evaluationViewable
	 */
	public EvalResponse response4;
	/**
	 * STUDENT_USER_ID, CONTEXT2, evaluationViewable
	 */
	public EvalResponse response5;
	/**
	 * USER_ID, CONTEXT2, evaluationClosed
	 */
	public EvalResponse response6;

	// ANSWERS
	public EvalAnswer answer1_1;
	public EvalAnswer answer2_2;
	public EvalAnswer answer2_5;
	public EvalAnswer answer3_2;
	public EvalAnswer answer4_1;
	public EvalAnswer answer4_5;
	public EvalAnswer answer5_1;

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


	public EvalTestDataLoad() {
		initialize();

		AUTHZGROUPSET1.add(AUTHZGROUP1A_ID);
		AUTHZGROUPSET1.add(AUTHZGROUP1B_ID);
		AUTHZGROUPSET2.add(AUTHZGROUP2A_ID);
	}

	/**
	 * initialize all the objects in this data load pea
	 * (this will make sure all the public properties are not null)
	 */
	public void initialize() {
		String[] options1 = {"Bad", "Average", "Good"};
		scale1 = new EvalScale(new Date(), ADMIN_USER_ID, "Scale 1", 
				EvalConstants.SHARING_PUBLIC, NOT_EXPERT, 
				"description", 
				EvalConstants.SCALE_IDEAL_HIGH, options1, LOCKED);

		String[] options2 = {"Poor", "Average", "Good", "Excellent"};
		scale2 = new EvalScale(new Date(), MAINT_USER_ID, "Scale 2", 
				EvalConstants.SHARING_PRIVATE, NOT_EXPERT, 
				"description", 
				EvalConstants.SCALE_IDEAL_HIGH, options2, UNLOCKED);

		String[] options3 = {"Male", "Female", "Unknown"};
		scale3 = new EvalScale(new Date(), ADMIN_USER_ID, "Scale 3", 
				EvalConstants.SHARING_PRIVATE, NOT_EXPERT, 
				"description", 
				EvalConstants.SCALE_IDEAL_NONE, options3, UNLOCKED);

		scale4 = new EvalScale(new Date(), ADMIN_USER_ID, "Scale 4", 
				EvalConstants.SHARING_PRIVATE, EXPERT, 
				"description", 
				EvalConstants.SCALE_IDEAL_NONE, options3, UNLOCKED);

		item1 = new EvalItem(new Date(), ADMIN_USER_ID, ITEM_TEXT, 
				EvalConstants.SHARING_PUBLIC, EvalConstants.ITEM_TYPE_SCALED, EXPERT);
		item1.setScale(scale1);
		item1.setScaleDisplaySetting( EvalConstants.ITEM_SCALE_DISPLAY_COMPACT );
		item1.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
		item1.setLocked(LOCKED);
		item2 = new EvalItem(new Date(), MAINT_USER_ID, ITEM_TEXT, 
				EvalConstants.SHARING_PUBLIC, EvalConstants.ITEM_TYPE_SCALED, NOT_EXPERT);
		item2.setScale(scale1);
		item2.setScaleDisplaySetting( EvalConstants.ITEM_SCALE_DISPLAY_FULL );
		item2.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
		item2.setLocked(LOCKED);
		item3 = new EvalItem(new Date(), MAINT_USER_ID, ITEM_TEXT, 
				EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_SCALED, NOT_EXPERT);
		item3.setScale(scale1);
		item3.setScaleDisplaySetting( EvalConstants.ITEM_SCALE_DISPLAY_STEPPED );
		item3.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
		item3.setLocked(LOCKED);
		item4 = new EvalItem(new Date(), MAINT_USER_ID, ITEM_TEXT, 
				EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_SCALED, NOT_EXPERT);
		item4.setScale(scale1);
		item4.setScaleDisplaySetting( EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL );
		item4.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
		item4.setLocked(UNLOCKED);
		item5 = new EvalItem(new Date(), MAINT_USER_ID, "Textual locked", 
				EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_TEXT, NOT_EXPERT);
		item5.setDisplayRows( new Integer(2) );
		item5.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
		item5.setLocked(LOCKED);
		item6 = new EvalItem(new Date(), MAINT_USER_ID, "Textual unlocked", 
				EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_TEXT, EXPERT);
		item6.setDisplayRows( new Integer(3) );
		item6.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
		item6.setLocked(UNLOCKED);
		item7 = new EvalItem(new Date(), ADMIN_USER_ID, "Textual unlocked", 
				EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_TEXT, NOT_EXPERT);
		item7.setDisplayRows( new Integer(4) );
		item7.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
		item7.setLocked(UNLOCKED);
		item8 = new EvalItem(new Date(), MAINT_USER_ID, "Header unlocked", 
				EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_HEADER, NOT_EXPERT);
		item8.setLocked(UNLOCKED);

		//templateShared = new EvalTemplate(new Date(), ADMIN_USER_ID, "Template shared", EvalConstants.SHARING_SHARED, UNLOCKED, NOT_EXPERT);
		//templateVisible = new EvalTemplate(new Date(), ADMIN_USER_ID, "Template visible", EvalConstants.SHARING_VISIBLE, UNLOCKED, NOT_EXPERT);
		templateAdmin = new EvalTemplate(new Date(), ADMIN_USER_ID, "Template admin", 
				"description", EvalConstants.SHARING_PRIVATE, NOT_EXPERT, 
				"expert desc", null, LOCKED);
		templateAdminNoItems = new EvalTemplate(new Date(), ADMIN_USER_ID, "Template admin no items", 
				"description", EvalConstants.SHARING_PRIVATE, NOT_EXPERT, 
				"not expert desc", null, UNLOCKED);
		templatePublicUnused = new EvalTemplate(new Date(), ADMIN_USER_ID, "Template unused public", 
				"description", EvalConstants.SHARING_PUBLIC, NOT_EXPERT, 
				"expert desc", null, UNLOCKED);
		templatePublic = new EvalTemplate(new Date(), MAINT_USER_ID, "Template maint public", 
				"description", EvalConstants.SHARING_PUBLIC, EXPERT, 
				"expert desc", null, LOCKED);
		templateUnused = new EvalTemplate(new Date(), MAINT_USER_ID, "Template maint unused", 
				"description", EvalConstants.SHARING_PRIVATE, NOT_EXPERT, 
				"expert desc", null, UNLOCKED);
		templateUser = new EvalTemplate(new Date(), USER_ID, "Template user", 
				"description", EvalConstants.SHARING_PRIVATE, NOT_EXPERT, 
				"expert desc", null, LOCKED);
		templateUserUnused = new EvalTemplate(new Date(), USER_ID, "Template user unused", 
				"description", EvalConstants.SHARING_PRIVATE, EXPERT, 
				"expert desc", null, UNLOCKED);

		// assign items to templates
		templateItem1User = new EvalTemplateItem( new Date(), USER_ID, 
				templateUser, item1, new Integer(1), EvalConstants.ITEM_CATEGORY_COURSE,
				null, EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, Boolean.FALSE, null, null);
		templateItem1P = new EvalTemplateItem( new Date(), MAINT_USER_ID, 
				templatePublic, item1, new Integer(1), EvalConstants.ITEM_CATEGORY_COURSE,
				null, EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, Boolean.FALSE, null, null);
		templateItem2A = new EvalTemplateItem( new Date(), ADMIN_USER_ID, 
				templateAdmin, item2, new Integer(1), EvalConstants.ITEM_CATEGORY_COURSE,
				null, EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.FALSE, null, null);
		templateItem3A = new EvalTemplateItem( new Date(), ADMIN_USER_ID, 
				templateAdmin, item3, new Integer(2), EvalConstants.ITEM_CATEGORY_COURSE,
				null, EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL, Boolean.FALSE, null, null);
		templateItem3U = new EvalTemplateItem( new Date(), MAINT_USER_ID, 
				templateUnused, item3, new Integer(1), EvalConstants.ITEM_CATEGORY_COURSE,
				null, EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.FALSE, null, null);
		templateItem3PU = new EvalTemplateItem( new Date(), ADMIN_USER_ID, 
				templatePublicUnused, item3, new Integer(1), EvalConstants.ITEM_CATEGORY_COURSE,
				null, EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.FALSE, null, null);
		templateItem5A = new EvalTemplateItem( new Date(), ADMIN_USER_ID, 
				templateAdmin, item5, new Integer(3), EvalConstants.ITEM_CATEGORY_INSTRUCTOR,
				new Integer(3), null, Boolean.FALSE, null, null);
		templateItem5User = new EvalTemplateItem( new Date(), USER_ID, 
				templateUser, item5, new Integer(2), EvalConstants.ITEM_CATEGORY_INSTRUCTOR,
				new Integer(2), null, Boolean.FALSE, null, null);
		templateItem5U = new EvalTemplateItem( new Date(), MAINT_USER_ID, 
				templateUnused, item5, new Integer(2), EvalConstants.ITEM_CATEGORY_INSTRUCTOR,
				new Integer(3), null, Boolean.FALSE, null, null);
		templateItem6UU = new EvalTemplateItem( new Date(), USER_ID, 
				templateUserUnused, item6, new Integer(3), EvalConstants.ITEM_CATEGORY_COURSE,
				new Integer(4), null, Boolean.FALSE, null, null);

		// associate the templates with the link
		templateAdmin.setTemplateItems( new HashSet() );
		templateAdmin.getTemplateItems().add( templateItem2A );
		templateAdmin.getTemplateItems().add( templateItem3A );
		templateAdmin.getTemplateItems().add( templateItem5A );

		templatePublicUnused.setTemplateItems( new HashSet() );
		templatePublicUnused.getTemplateItems().add( templateItem3PU );

		templatePublic.setTemplateItems( new HashSet() );
		templatePublic.getTemplateItems().add( templateItem1P );

		templateUnused.setTemplateItems( new HashSet() );
		templateUnused.getTemplateItems().add( templateItem3U );
		templateUnused.getTemplateItems().add( templateItem5U );

		templateUser.setTemplateItems( new HashSet() );
		templateUser.getTemplateItems().add( templateItem1User );
		templateUser.getTemplateItems().add( templateItem5User );

		templateUserUnused.setTemplateItems( new HashSet() );
		templateUserUnused.getTemplateItems().add( templateItem6UU );

		// associate the items with the link
		item1.setTemplateItems( new HashSet() );
		item1.getTemplateItems().add( templateItem1P );
		item1.getTemplateItems().add( templateItem1User );

		item2.setTemplateItems( new HashSet() );
		item2.getTemplateItems().add( templateItem2A );

		item3.setTemplateItems( new HashSet() );
		item3.getTemplateItems().add( templateItem3A );
		item3.getTemplateItems().add( templateItem3PU );
		item3.getTemplateItems().add( templateItem3U );

		item4.setTemplateItems( new HashSet() );

		item5.setTemplateItems( new HashSet() );
		item5.getTemplateItems().add( templateItem5A );
		item5.getTemplateItems().add( templateItem5U );
		item5.getTemplateItems().add( templateItem5User );

		item6.setTemplateItems( new HashSet() );
		item6.getTemplateItems().add( templateItem6UU );


		// TODO - change these to no longer use the old linking method
//		Set s = new HashSet();
//		s.add(templateUser);
//		s.add(templatePublic);
//		item1.setTemplates(s);
//		s.remove(templateUser);
//		s.add(templateAdmin);
//		item2.setTemplates(s);
//		s = new HashSet();
//		s.add(templateUnused);
//		s.add(templatePublicUnused);
//		item3.setTemplates(s);
//		s = new HashSet();
//		s.add(templateAdmin);
//		s.add(templateUser);
//		s.add(templateUnused);
//		item5.setTemplates(s);
//		s = new HashSet();
//		s.add(templateUserUnused);
//		item6.setTemplates(s);

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
		// Evaluation not started yet (starts tomorrow)
		evaluationNew = new EvalEvaluation(new Date(), MAINT_USER_ID, "Eval new", null, 
				tomorrow, threeDaysFuture, threeDaysFuture, fourDaysFuture, null, null,
				EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.INSTRUCTOR_OPT_IN, 
				Integer.valueOf(1), null, null, null, null, templatePublic, null,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, UNLOCKED);
		// Evaluation not started yet (starts tomorrow), ADMIN
		evaluationNewAdmin = new EvalEvaluation(new Date(), ADMIN_USER_ID, "Eval admin", null, 
				tomorrow, threeDaysFuture, threeDaysFuture, fourDaysFuture,  null, null,
				EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.INSTRUCTOR_OPT_IN, 
				Integer.valueOf(1), null, null, null, null, templateAdmin, null,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, UNLOCKED);
		// Evaluation Active (ends today), viewable tomorrow
		evaluationActive = new EvalEvaluation(new Date(), MAINT_USER_ID, "Eval active", null, 
				yesterday, today, today, tomorrow, null, null,
				EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.INSTRUCTOR_OPT_IN, 
				Integer.valueOf(1), null, null, null, null, templatePublic, null,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, LOCKED);
		// Evaluation Active (ends tomorrow), viewable 3 days
		evaluationActiveUntaken = new EvalEvaluation(new Date(), MAINT_USER_ID, "Eval active not taken", null, 
				yesterday, tomorrow, tomorrow, threeDaysFuture, null, null,
				EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.INSTRUCTOR_OPT_IN, 
				Integer.valueOf(1), null, null, null, null, templatePublic, null,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, UNLOCKED);
		// TODO - add in an evaluation in the DUE state?
		// Evaluation Complete (ended yesterday, viewable tomorrow), recent close
		evaluationClosed = new EvalEvaluation(new Date(), ADMIN_USER_ID, "Eval closed", null, 
				threeDaysAgo, yesterday, yesterday, tomorrow, null, null,
				EvalConstants.EVALUATION_STATE_CLOSED, EvalConstants.INSTRUCTOR_OPT_IN, 
				Integer.valueOf(2), null, null, null, null, templateAdmin, null,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, LOCKED);
		// evaluation complete (3 days ago) and viewable (yesterday)
		evaluationViewable = new EvalEvaluation(new Date(), ADMIN_USER_ID, "Eval viewable", null, 
				twentyDaysAgo, twentyDaysAgo, twentyDaysAgo, fifteenDaysAgo, null, null,
				EvalConstants.EVALUATION_STATE_VIEWABLE, EvalConstants.INSTRUCTOR_OPT_IN, 
				Integer.valueOf(2), null, null, null, null, templateUser, null,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, LOCKED);

		// email templates
		emailTemplate1 = new EvalEmailTemplate(new Date(), ADMIN_USER_ID, "Email Template 1");
		evaluationNew.setAvailableEmailTemplate(emailTemplate1);
		emailTemplate2 = new EvalEmailTemplate(new Date(), MAINT_USER_ID, "Email Template 2"); 
		evaluationNew.setReminderEmailTemplate(emailTemplate2);
		emailTemplate3 = new EvalEmailTemplate(new Date(), MAINT_USER_ID, "Email Template 3"); 
		evaluationActive.setReminderEmailTemplate(emailTemplate3);

		// context assignments
		assign1 = new EvalAssignContext(new Date(), MAINT_USER_ID, CONTEXT1, Boolean.FALSE, 
				Boolean.TRUE, Boolean.FALSE, evaluationActive);
		assign2 = new EvalAssignContext(new Date(), MAINT_USER_ID, CONTEXT1, Boolean.FALSE, 
				Boolean.TRUE, Boolean.FALSE, evaluationActiveUntaken);
		assign3 = new EvalAssignContext(new Date(), ADMIN_USER_ID, CONTEXT1, Boolean.FALSE, 
				Boolean.TRUE, Boolean.FALSE, evaluationClosed);
		assign4 = new EvalAssignContext(new Date(), MAINT_USER_ID, CONTEXT2, Boolean.FALSE, 
				Boolean.TRUE, Boolean.FALSE, evaluationClosed);
		assign5 = new EvalAssignContext(new Date(), ADMIN_USER_ID, CONTEXT2, Boolean.FALSE, 
				Boolean.TRUE, Boolean.FALSE, evaluationViewable);
		assign6 = new EvalAssignContext(new Date(), MAINT_USER_ID, CONTEXT1, Boolean.FALSE, 
				Boolean.TRUE, Boolean.FALSE, evaluationNewAdmin);
		assign7 = new EvalAssignContext(new Date(), ADMIN_USER_ID, CONTEXT2, Boolean.FALSE, 
				Boolean.TRUE, Boolean.FALSE, evaluationNewAdmin);

		// now init response data for the evaluations
		response1 = new EvalResponse(new Date(), USER_ID, CONTEXT1, 
				new Date(), today, null, evaluationActive);
		response2 = new EvalResponse(new Date(), USER_ID, CONTEXT1, 
				new Date(), today, null, evaluationClosed);
		response3 = new EvalResponse(new Date(), STUDENT_USER_ID, CONTEXT2, 
				new Date(), today, null, evaluationClosed);
		response4 = new EvalResponse(new Date(), USER_ID, CONTEXT2, 
				new Date(), today, null, evaluationViewable);
		response5 = new EvalResponse(new Date(), STUDENT_USER_ID, CONTEXT2, 
				new Date(), today, null, evaluationViewable);
		response6 = new EvalResponse(new Date(), USER_ID, CONTEXT2, 
				new Date(), today, null, evaluationClosed);

		answer1_1 = new EvalAnswer(new Date(), item1, response1, null, ANSWER_SCALED_ONE);

		answer2_2 = new EvalAnswer(new Date(), item2, response2, null, ANSWER_SCALED_ONE);
		answer2_5 = new EvalAnswer(new Date(), item5, response2, ANSWER_TEXT, null);

		answer3_2 = new EvalAnswer(new Date(), item2, response3, null, ANSWER_SCALED_TWO);
		// left the text answer blank

		answer4_1 = new EvalAnswer(new Date(), item1, response4, null, ANSWER_SCALED_THREE);
		answer4_5 = new EvalAnswer(new Date(), item5, response4, ANSWER_TEXT, null);

		answer5_1 = new EvalAnswer(new Date(), item1, response5, null, ANSWER_SCALED_TWO);
		// left the text answer blank

		// associate the answers
		Set answers = new HashSet();
		answers.add(answer1_1);
		response1.setAnswers(answers);

		answers = new HashSet();
		answers.add(answer2_2);
		answers.add(answer2_5);
		response2.setAnswers(answers);

		answers = new HashSet();
		answers.add(answer3_2);
		response3.setAnswers(answers);

		answers = new HashSet();
		answers.add(answer4_1);
		answers.add(answer4_5);
		response4.setAnswers(answers);

		answers = new HashSet();
		answers.add(answer5_1);
		response5.setAnswers(answers);

		answers = new HashSet();
		response6.setAnswers(answers); // left all answers blank

	}

	/**
	 * Store all of the persistent objects in this pea
	 * @param dao A DAO with a save method which takes a persistent object as an argument<br/>
	 * Example: dao.save(templateUser);
	 */
	public void saveAll(EvaluationDao dao) {
		dao.save(scale1);
		dao.save(scale2);
		dao.save(scale3);

		//dao.save(templateShared);
		//dao.save(templateVisible);
		dao.save(templateAdmin);
		dao.save(templateAdminNoItems);
		dao.save(templatePublicUnused);
		dao.save(templatePublic);
		dao.save(templateUnused);
		dao.save(templateUser);
		dao.save(templateUserUnused);

		dao.save(item1);
		dao.save(item2);
		dao.save(item3);
		dao.save(item4);
		dao.save(item5);
		dao.save(item6);
		dao.save(item7);
		dao.save(item8);

		dao.save(templateItem1User);
		dao.save(templateItem1P);
		dao.save(templateItem2A);
		dao.save(templateItem3A);
		dao.save(templateItem3PU);
		dao.save(templateItem3U);
		dao.save(templateItem5A);
		dao.save(templateItem5U);
		dao.save(templateItem5User);
		dao.save(templateItem6UU);

		dao.save(emailTemplate1);
		dao.save(emailTemplate2);
		dao.save(emailTemplate3);

		dao.save(evaluationNew);
		dao.save(evaluationNewAdmin);
		dao.save(evaluationActive);
		dao.save(evaluationActiveUntaken);
		dao.save(evaluationClosed);
		dao.save(evaluationViewable);

		dao.save(assign1);
		dao.save(assign2);
		dao.save(assign3);
		dao.save(assign4);
		dao.save(assign5);
		dao.save(assign6);
		dao.save(assign7);

		dao.save(response1);
		dao.save(response2);
		dao.save(response3);
		dao.save(response4);
		dao.save(response5);
		dao.save(response6);

		dao.save(answer1_1);
		dao.save(answer2_2);
		dao.save(answer2_5);
		dao.save(answer3_2);
		dao.save(answer4_1);
		dao.save(answer4_5);
		dao.save(answer5_1);
	}

	/**
	 * Take a collection of persistent objects and turn it into a list of the unique ids<br/>
	 * Objects in collection must have a Long getId() method<br/>
	 * Uses some fun reflection to figure out the IDs
	 * 
	 * @param c a collection of persistent objects
	 * @return a list of IDs (Long)
	 */
	public static List makeIdList(Collection c) {
		List l = new ArrayList();
		for (Iterator iter = c.iterator(); iter.hasNext();) {
			Serializable element = (Serializable) iter.next();
			Long id = null;
			try {
				Class elementClass = element.getClass();
				Method getIdMethod = elementClass.getMethod("getId", new Class[] {});
				id = (Long) getIdMethod.invoke(element, null);
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

}

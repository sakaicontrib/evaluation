/**
 * $Id: EvaluationDateUtil.java 1000 Nov 15, 2006 12:07:31 AM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * EvaluationDateUtil.java - evaluation - Nov 15, 2006 12:07:31 AM - kahuja
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalEvaluation;

/**
 * Utility class for date manipulations.  
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationDateUtil {

	private static Log log = LogFactory.getLog(EvaluationDateUtil.class);

	/**
	 * Ensures that there is minimum number of hours difference between
	 * the start date of the evaluation and due date of the evaluation.
	 * This minimum value is picked from system setting.
	 * 
	 * @param eval {@link EvalEvaluation} object that contains both start and due date.
	 * @param minHoursLong the minimum number of hours between the startdate and the duedate,
     * usually would come from a system setting {@link EvalSettings#EVAL_MIN_TIME_DIFF_BETWEEN_START_DUE}
	 */
	public static void updateDueDate(EvalEvaluation eval, int minHoursLong) {

		/*
		 * Find the difference between view date and due date so that after 
		 * due date is updated, add the same difference to view date again 
		 * (precision is miliseconds). Also casting to 'int' is fine as the 
		 * difference between view and due dates is not expected to be very 
		 * large.
		 */  
		int diffBetViewDue = (int) (eval.getViewDate().getTime() - eval.getDueDate().getTime());

		/*
		 * If the difference between start date and due date is less than
		 * the minimum value set as system settings, then update the due date
		 * to reflect this minimum time difference. After that update the view
		 * date also.
		 * 
		 * Note: Arguments to getHoursDifference() method should have due date as 
		 *       first date and start date as second date.
		 */
		if (getHoursDifference(eval.getDueDate(), eval.getStartDate()) < minHoursLong) {

			// Update due date
			Calendar calendarDue = new GregorianCalendar();
			calendarDue.setTime(eval.getStartDate());
			calendarDue.add(Calendar.HOUR_OF_DAY, minHoursLong);
			log.info("Fixing eval (" + eval.getId() + ") due date from " + eval.getDueDate() + " to " + calendarDue.getTime());
			eval.setDueDate(calendarDue.getTime());

			// Update stop date if needed
			if (eval.getStopDate().before(eval.getDueDate())) {
				Calendar calendarView = new GregorianCalendar();
				calendarView.setTime(eval.getStopDate());
				calendarView.add(Calendar.MILLISECOND, diffBetViewDue);
				log.info("Fixing the stop date from " + eval.getStopDate() + " to " + calendarView.getTime());
				eval.setStopDate(calendarView.getTime());
			}

			// Update view date if needed
			if (eval.getViewDate().before(eval.getStopDate())) {
				Calendar calendarView = new GregorianCalendar();
				calendarView.setTime(eval.getViewDate());
				calendarView.add(Calendar.MILLISECOND, diffBetViewDue);
				log.info("Fixing the view date from " + eval.getViewDate() + " to " + calendarView.getTime());
				eval.setViewDate(calendarView.getTime());
			}
		}
	}

	/**
	 * Set the time portion to the end of the day instead (23:59), this is to avoid confusion for users
	 * when setting the evaluations to end on a certain date and having them end in the first minute of the day instead of
	 * at the end of the day
	 * Note: This may lead to a nasty bug if anyone ever attempts to explicitly set the time for the stop and due dates
	 * 
	 * @param d a {@link java.util.Date}
	 * @return a {@link java.util.Date} which has the time portion set to the end of the day or the original Date
	 */
	public static Date getEndOfDayDate(Date d) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(d);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		log.info("Setting a date to the end of the day from " + d + " to " + cal.getTime());
		return cal.getTime();
	}

	/**
	 * Check if the time portion of a date is set to midnight and return true if it is
	 * 
	 * @param d a {@link Date} object
	 * @return true if time is midnight (00:00:00), false otherwise
	 */
	public static boolean isTimeMidnight(Date d) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(d);
		if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.SECOND) == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Find the number of hours between 2 dates
	 * 
	 * @param date1
	 * @param date2
	 * @return number of hours (can be negative, will round)
	 */
	public static int getHoursDifference(Date date1, Date date2) {
		long millisecondsDifference = date1.getTime() - date2.getTime();
		return (int) millisecondsDifference / (60*60*1000);
	}

}

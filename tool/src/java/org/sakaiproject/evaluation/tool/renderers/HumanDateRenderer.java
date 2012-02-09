/**
 * HumanDateRenderer.java - evaluation - Oct 29, 2007 2:59:12 PM - mgillian
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Michael Gillian (mgillian@unicon.net)
 */
package org.sakaiproject.evaluation.tool.renderers;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.sakaiproject.evaluation.logic.EvalSettings;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;

public class HumanDateRenderer {
    private long MILLIS_PER_DAY = 24*60*60*1000;
    private DateFormat df;
    
    private Locale locale;
    public void setLocale(Locale locale) {
       this.locale = locale;
    }
    
    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }
    
    public void init() {
        boolean useDateTime = (Boolean) settings.get(EvalSettings.EVAL_USE_DATE_TIME);
        if (useDateTime) {
        	df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        } else {
        	df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        }
    }
    
    /**
     * renderDate updates the passed field to display a human-readable date,
     * along with a tooltip of the actual date, based on a date passed
     * to the function.
     * <p>Human-readable in this context means using words instead of a date/time
     * format.  For example, if the renderDate happens to be today, the field
     * would be rendered as "Today", along with a tooltip of today's date 
     * (for example, "Dec 20, 2001")
     * <p>Date format is determined by the locale, and the human text
     * is controlled by properties file and supports internationalization.
     * @param parent RSF container holding the field
     * @param fieldName Field to be rendered
     * @param renderDate Date to be rendered in a human readable format
     */
    public void renderDate(UIContainer parent, String fieldName, Date renderDate) {
        // default render values
        String message = "humandate.date";
        String[] parameters = { df.format(renderDate) };
        
        long diffInDays = calculateDateDiff(renderDate);        
        if (diffInDays > 7) {
            message = "humandate.date";
        } else if (diffInDays == 7) {
            message = "humandate.inweek";
        } else if (diffInDays > 1) {
            message = "humandate.indays";
            parameters[0] = Long.toString(Math.abs(diffInDays));
        } else if (diffInDays == 1) {
            message = "humandate.tomorrow";
        } else if (diffInDays == 0) {
            message = "humandate.today";
        } else if (diffInDays == -1) {
            message = "humandate.yesterday";
        } else if (diffInDays > -7) {
            message = "humandate.daysago";
            parameters[0] = Long.toString(Math.abs(diffInDays));
        } else if (diffInDays == -7) {
            message = "humandate.weekago";
        } else {
            message = "humandate.date";
        }
        
        UIMessage output = UIMessage.make(parent, fieldName, message, parameters);
        output.decorate(new UITooltipDecorator( UIOutput.make(df.format(renderDate)) ));
        if ("humandate.today".equals(message)) {
            output.decorate(new UIStyleDecorator( "urgentStyle" ));
        }
    }
    
    /**
     * calculateDateDiff returns the number of days between the passed date and 
     * the current date.  Dates are normalized to 23:59:59, so that only year/month/day
     * affect the date calculation
     * <p>For example, if the date being passed in is January 15, 2001 3:30:15pm,
     * and the current date is January 12, 2001 9:45:18am, the date diff would be 3
     * days - January 15, 2001 23:59:59 - January 12, 2001 23:59:59
     * <p>For example, if the date being pass in is January 15, 2001 3:30:15pm, 
     * and the current date is January 18, 2001 9:45:18am, the date diff would be
     * -3 days - January 15, 2001 23:59:59 - January 18, 2001 23:59:59
     * @param _date date being compared
     * @return number of days.
     */
    private long calculateDateDiff(Date _date) {        
        Calendar trimDate = Calendar.getInstance();
        trimDate.setTime(_date);
        setEndOfDay(trimDate);
        
        Calendar curDate = Calendar.getInstance();
        setEndOfDay(curDate);

        long diffInMillis = trimDate.getTimeInMillis() - curDate.getTimeInMillis();
        return diffInMillis / MILLIS_PER_DAY;
    }
    
    /**
     * setEndOfDay changes the hours, minutes, and seconds to be at the end of 
     * the day - 23:59:59
     * @param cal
     */
    private void setEndOfDay(Calendar cal ) {
        cal.set(Calendar.HOUR, 11);
        cal.set(Calendar.AM_PM, Calendar.PM);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);        
    }
}

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
package org.sakaiproject.evaluation.tool.renderers;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;

public class HumanDateRenderer {
    private static final long MILLIS_PER_DAY = 24*60*60*1000;
    private DateFormat df;
    private DateFormat tf;
    private boolean useDateTime = false;

    private Locale locale;
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }

    public MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator messageLocator) {
        this.messageLocator = messageLocator;
    }

    public void init() {
        useDateTime = (Boolean) settings.get(EvalSettings.EVAL_USE_DATE_TIME);
        tf = DateFormat.getTimeInstance(DateFormat.SHORT, locale); // e.g. 3:30pm
        if (useDateTime) {
            df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
        } else {
            df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        }
    }

    public int maxTitle = 50;
    /**
     * Special method for consistent rendering of eval titles 
     * (which are constructed using a combination of the eval and group info)
     * @param eval the evaluation
     * @param group the related group
     * @return the string title
     */
    public String renderEvalTitle(EvalEvaluation eval, EvalGroup group) {
        String evalTitle = "";
        String evalState = "";
        String evalTerm = "";
        if (eval != null) {
            evalTitle = eval.getTitle();
            if (eval.getTermId() != null) {
                evalTerm = eval.getTermId();
            }
            evalState = EvalUtils.getEvaluationState(eval, true);
        }
        String groupTitle = "";
        String groupType = "";
        if (group != null) {
            groupTitle = group.title;
            groupType = group.type;
        }
        String title = messageLocator.getMessage("eval.display.title", new Object[] {evalTitle, groupTitle, evalState, evalTerm, groupType}) + " ";
        return EvalUtils.makeMaxLengthString(title, maxTitle);
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
     * @param container RSF container holding the field
     * @param fieldName Field to be rendered
     * @param renderDate Date to be rendered in a human readable format
     */
    public void renderDate(UIContainer container, String fieldName, Date renderDate) {
        // if date is less than WARNING_DAYS in the future, add emphasis
        final int WARNING_DAYS = 2;

        // default render values
        String messagePrefix = "humandate.";
        if (useDateTime) {
            messagePrefix = "humandatetime.";
        }
        String message = messagePrefix+"date";
        String[] parameters = { df.format(renderDate), tf.format(renderDate) };

        long diffInDays = calculateDateDiff(renderDate);
        if (diffInDays > 7) {
            // use default
            //message = messagePrefix+"date";
        } else if (diffInDays == 7) {
            message = messagePrefix+"inweek";
        } else if (diffInDays > 1) {
            message = messagePrefix+"indays";
            parameters[0] = Long.toString(Math.abs(diffInDays));
        } else if (diffInDays == 1) {
            message = messagePrefix+"tomorrow";
        } else if (diffInDays == 0) {
            message = messagePrefix+"today";
        } else if (diffInDays == -1) {
            message = messagePrefix+"yesterday";
        } else if (diffInDays > -7) {
            message = messagePrefix+"daysago";
            parameters[0] = Long.toString(Math.abs(diffInDays));
        } else if (diffInDays == -7) {
            message = messagePrefix+"weekago";
        } else {
            // diffInDays > 7, etc.
            // use default
            //message = messagePrefix+"date";
        }

        UIMessage output = UIMessage.make(container, fieldName, message, parameters);
        output.decorate(new UITooltipDecorator( UIOutput.make(df.format(renderDate)) ));
        if (diffInDays >= 0 && diffInDays <= WARNING_DAYS) {
            output.decorate(new UIStyleDecorator( "urgentStyle" ));
        }
        String unixtime = String.valueOf(renderDate.getTime());
        output.decorate(new UIFreeAttributeDecorator("data-time", unixtime ));
        //container.decorate(new UIFreeAttributeDecorator("data-timesort", unixtime ));
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

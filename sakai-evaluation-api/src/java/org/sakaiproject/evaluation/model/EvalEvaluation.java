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
package org.sakaiproject.evaluation.model;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalReminderStatus;
import org.sakaiproject.evaluation.utils.EvalUtils;

/**
 * Defines an evaluation itself (this is an instance which can be taken by evaluators)
 */
public class EvalEvaluation implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    // Fields

    private Long id;

    private String eid;

    private Date lastModified;

    /**
     * Defines the type of evaluation: use {@link EvalConstants} EVALUATION_TYPE_*
     */
    private String type;

    private String owner;

    private String title;

    private String instructions;

    /**
     * This is the date at which the evaluation becomes active, the users can take the evaluation
     * after this<br/>
     * This MUST be set, see {@link #customStartDate}
     */
    private Date startDate;

    /**
     * This is the ending date for the evaluation<br/>
     * if this is null then there is no due date and the evaluation is open until manually closed<br/>
     * Affected by the {@link #useDueDate} setting<br/>
     * <b>NOTE:</b> a null value here means that the evaluation is open until manually closed
     */
    private Date dueDate;

    /**
     * this defines the grace period for completing the evaluation after the due date, if this is
     * null then there is no grace period<br/>
     * Affected by the {@link #useStopDate} setting<br/>
     * <b>NOTE:</b> a null value here means that there is no grace period
     */
    private Date stopDate;

    /**
     * The overall view date for this evaluation, Note that this trumps the students and instructors
     * dates and they must be set to be after this one or they will have no effect<br/>
     * Affected by the {@link #useViewDate} setting<br/>
     * <b>NOTE:</b> a null value here means that the results are immediately viewable after the
     * evaluation closes<br/>
     * TODO - how to handle view date that is before the start date?
     */
    private Date viewDate;

    /**
     * if this is true then students can view results for this evaluation, This will cause the
     * {@link #studentsDate} to be nulled out when the evaluation is saved if set to false
     */
    private boolean studentViewResults;
    /**
     * if this is true then instructors can view results for this evaluation, This will cause the
     * {@link #instructorsDate} to be nulled out out when the evaluation is saved if set to false
     */
    private boolean instructorViewResults;

    private Boolean instructorViewAllResults;

    /**
     * An encoded string which indicates the stored selection settings for this evaluation,
     * null indicates that there are no selection settings stored and to use {@link #SELECTION_OPTION_ALL} 
     * (the default which indicates no selections),
     * this is passed down to all assign groups for this evaluation and should be checked there <br/>
     * <b>WARNING:</b> getting and setting this value directly should not be done,
     * use the {@link #setSelectionOption(String, String)} and {@link #getSelectionOptions()} methods
     */
    protected String selectionSettings;

    /**
     * if {@link #studentViewResults} is true and this is null then students can view results as
     * soon as the evaluation is closed, otherwise they can view results after this date
     */
    private Date studentsDate;

    /**
     * if {@link #instructorViewResults} is true and this is null then non-owner instructors can
     * view results as soon as the evaluation is closed, otherwise they can view results after this
     * date
     */
    private Date instructorsDate;

    /**
     * You should use the methods which check the state of the evaluation rather than looking at
     * this field directly in most cases<br/>
     * see {@link EvalEvaluationService#updateEvaluationState(Long)} and
     * {@link EvalEvaluationService#returnAndFixEvalState(EvalEvaluation, boolean)}
     * and {@link EvalUtils#getEvaluationState(EvalEvaluation, boolean)}
     */
    private String state;

    private String instructorOpt;

    /**
     * the number of days between reminder emails, 0 or null means emails are disabled,
     * -1 means send them out 24 hours before the close
     */
    private Integer reminderDays;

    /**
     * This will be the email address that notification emails should be sent from for this
     * evaluation
     */
    private String reminderFromEmail;

    /**
     * This is the status of the current reminder sending,
     * use the {@link #getCurrentReminderStatus()} method to get the info out of this field
     * and the {@link #setCurrentReminderStatus(EvalReminderStatus)} to set the field
     */
    private String reminderStatus;

    /**
     * This is the term code for this evaluation (I assume?)
     */
    private String termId;

    /**
     * if this is set then use the template here, if null then use the default
     * {@link EvalConstants#EMAIL_TEMPLATE_AVAILABLE}
     */
    private EvalEmailTemplate availableEmailTemplate;

    /**
     * if this is set then use the template here, if null then use the default
     * {@link EvalConstants#EMAIL_TEMPLATE_AVAILABLE}<br/>
     */
    private EvalEmailTemplate reminderEmailTemplate;

    /**
     * if this is set then use the template here, if null then use the default
     * {@link EvalConstants#EMAIL_TEMPLATE_SUBMITTED}<br/>
     */
    private EvalEmailTemplate submissionConfirmationEmailTemplate;

    /**
     * This is the template associated with this evaluation, this should always be a copy of an
     * existing template once the evaluation is created (no longer in the partial state)
     */
    private EvalTemplate template;

    private Set<EvalResponse> responses = new HashSet<>(0);

    /**
     * Defines the sharing setting for the results of this evaluation, Uses the sharing constants:
     * e.g. {@link EvalConstants#SHARING_VISIBLE}<br/>
     * <b>NOTE:</b> private means only the owner and superadmin can see the results, public means
     * anyone can see the results, visible is the owner plus all admins and colleagues (default)
     */
    private String resultsSharing;

    /**
     * Should never be null,
     * if true then blank responses are allowed,
     * if false then all answers must be completed
     */
    private Boolean blankResponsesAllowed;

    /**
     * Should never be null,
     * if true then responses may be changed during active eval period
     */
    private Boolean modifyResponsesAllowed;

    /**
     * if true then all roles will be included in the list of evaluators
     */
    private Boolean allRolesParticipate;    

    private Boolean unregisteredAllowed;

    private Boolean availableEmailSent = false;

    private Boolean locked;

    private String authControl;

    private String evalCategory;

    /** 
     * A flag to toggle sending mass email on evaluation active state
     */
    private Boolean sendAvailableNotifications;

    /**
     * If this is not null then we will load in all templates/templateItems/items with the related
     * linking autoUseTag when the evaluation is created
     */
    private String autoUseTag;

    /**
     * Controls whether the autoUseTag does anything and where the autoUse items are inserted, if
     * this is null then autoUse is disabled and no items are inserted, otherwise the constants
     * determine which location to insert the autoUse data, for example:
     * {@link EvalConstants#EVALUATION_AUTOUSE_INSERTION_BEFORE}
     */
    private String autoUseInsertion;

    /**
     * Controls whether or not the evaluation will be section aware. If this is set to true,
     * evaluator's will only evaluate evaluatees that are in their section/roster in the
     * site/group. Also, when creating the evaluation and releasing to HierarchyNodes, the
     * creator will see a list of selectable sections that match the node rules, rather than
     * a list of selectable sites if they elected not to make the evaluation section aware.
     */
    private Boolean sectionAwareness;

    // NON_PERSISTENT

    /**
     * Non-persistent field:<br/>
     * If this is set to false then the evaluation start date will be set to now, otherwise the
     * currently set start date will be used<br/>
     * <b>NOTE:</b> this should only be set when an evaluation is not yet saved This is ignored if
     * it is null<br/>
     */
    public Boolean customStartDate;
    /**
     * Non-persistent field:<br/>
     * If this is set to false then the evaluation due date will be set null and the evaluation will
     * be open forever, otherwise the currently set due date will be used<br/>
     * This is ignored if it is null<br/>
     * <b>NOTE:</b> this will override any setting for {@link #useStopDate} and {@link #useViewDate}
     */
    public Boolean useDueDate;
    /**
     * Non-persistent field:<br/>
     * If this is set to false then the evaluation stop date will be set to whatever the due date is
     * set to (effectively meaning there is no grace period at all), otherwise the currently set
     * stop date will be used<br/>
     * This is ignored if it is null<br/>
     */
    public Boolean useStopDate;
    /**
     * Non-persistent field:<br/>
     * If this is set to false then the evaluation view date will be set to whatever the due date is
     * set to (effectively meaning the results are immediately viewable), otherwise the currently
     * set view date will be used<br/>
     * This is ignored if it is null<br/>
     */
    public Boolean useViewDate;
    /**
     * Non-persistent field:<br/>
     * If this is set to false then the evaluation only uses the DATE portion of Date objects,
     * if true then the DATE and TIME portions are used<br/>
     * <b>NOTE:</b> This is ignored if it is null<br/>
     */
    public Boolean useDateTimes;

    /**
     * Optional field never used by EVALSYS code. May be used to mark records for bulk actions. 
     * For example, an import operation might set a value that will be persisted and can be used 
     * later to select records for export, deletion, etc.  Maximum length is 80 characters.
     */
    public String localSelector;

    /**
     * This is an optional listing of assigned eval groups for this evaluation,
     * if this is non-null and has been set then the groups contained are relevant to service method used
     * to retrieve the evaluations, typically this would include the groups assigned to this eval which
     * are accessible to the current user
     */
    protected List<EvalAssignGroup> evalAssignGroups;

    /**
     * This is an optional listing of eval groups for this evaluation,
     * if this is non-null and has been set then the groups contained are relevant to service method used
     * to retrieve the evaluations, typically this would include the groups assigned to this eval which
     * are accessible to the current user
     */
    protected List<EvalGroup> evalGroups;

    // Constructors

    /** default constructor */
    public EvalEvaluation() {
    }

    /** COPY constructor - this MUST be updated if fields are added to this object
     * @param eval 
     **/
    public EvalEvaluation(EvalEvaluation eval) {
        // construct evaluation from another one
        this.id = eval.id;
        this.eid = eval.eid;
        this.lastModified = copy(eval.lastModified);
        this.type = eval.type;
        this.owner = eval.owner;
        this.title = eval.title;
        this.instructions = eval.instructions;
        this.startDate = copy(eval.startDate);
        this.dueDate = copy(eval.dueDate);
        this.stopDate = copy(eval.stopDate);
        this.viewDate = copy(eval.viewDate);
        this.studentViewResults = eval.studentViewResults;
        this.instructorViewResults = eval.instructorViewResults;
        this.instructorViewAllResults = eval.instructorViewAllResults;
        this.selectionSettings = eval.selectionSettings;
        this.studentsDate = copy(eval.studentsDate);
        this.instructorsDate = copy(eval.instructorsDate);
        this.state = eval.state;
        this.instructorOpt = eval.instructorOpt;
        this.reminderDays = eval.reminderDays;
        this.reminderFromEmail = eval.reminderFromEmail;
        this.reminderStatus = eval.reminderStatus;
        this.termId = eval.termId;
        this.availableEmailTemplate = eval.availableEmailTemplate;
        this.reminderEmailTemplate = eval.reminderEmailTemplate;
        this.submissionConfirmationEmailTemplate = eval.submissionConfirmationEmailTemplate; 
        this.template = eval.template;
        this.responses = eval.responses;
        this.resultsSharing = eval.resultsSharing;
        this.blankResponsesAllowed = eval.blankResponsesAllowed;
        this.modifyResponsesAllowed = eval.modifyResponsesAllowed;
        this.allRolesParticipate = eval.allRolesParticipate;
        this.unregisteredAllowed = eval.unregisteredAllowed;
        this.availableEmailSent = eval.availableEmailSent;
        this.locked = eval.locked;
        this.authControl = eval.authControl;
        this.evalCategory = eval.evalCategory;
        this.sendAvailableNotifications = eval.sendAvailableNotifications;
        this.autoUseTag = eval.autoUseTag;
        this.autoUseInsertion = eval.autoUseInsertion;
        this.sectionAwareness = eval.sectionAwareness;
        // NON_PERSISTENT
        this.customStartDate = eval.customStartDate;
        this.useDueDate = eval.useDueDate;
        this.useStopDate = eval.useStopDate;
        this.useViewDate = eval.useViewDate;
        this.localSelector = eval.localSelector;
        this.evalAssignGroups = eval.evalAssignGroups;
        this.evalGroups = eval.evalGroups;
    }

    /**
     * minimal constructor
     * @param type
     * @param owner
     * @param title
     * @param startDate
     * @param state
     * @param resultsSharing
     * @param reminderDays
     * @param template
     */
    public EvalEvaluation(String type, String owner, String title, Date startDate, String state,
            String resultsSharing, Integer reminderDays, EvalTemplate template) {
        this(type, owner, title, null, startDate, null, null, null, false, null, true, null, state, resultsSharing, null, reminderDays, null, null, null, null, template, null, null, null, null, Boolean.FALSE, null, null, null);
    }

    /**
     * general use constructor
     * @param type
     * @param owner
     * @param title
     * @param dueDate
     * @param startDate
     * @param stopDate
     * @param viewDate
     * @param state
     * @param resultsSharing
     * @param reminderDays
     * @param template
     */
    public EvalEvaluation(String type, String owner, String title, Date startDate, Date dueDate,
            Date stopDate, Date viewDate, String state, String resultsSharing,
            Integer reminderDays, EvalTemplate template) {
        this(type, owner, title, null, startDate, dueDate, stopDate, viewDate, false, null, true, null, state, resultsSharing, null, reminderDays, null, null, null, null, template, null, null, null, null, Boolean.FALSE, null, null, null);
    }

    public EvalEvaluation(String type, String owner, String title, String instructions,
            Date startDate, Date dueDate, Date stopDate, Date viewDate, boolean studentViewResults,
            Date studentsDate, boolean instructorViewResults, Date instructorsDate, String state,
            String resultsSharing, String instructorOpt, Integer reminderDays,
            String reminderFromEmail, String termId, EvalEmailTemplate availableEmailTemplate,
            EvalEmailTemplate reminderEmailTemplate, EvalTemplate template,
            Set<EvalResponse> responses, Boolean blankResponsesAllowed,
            Boolean modifyResponsesAllowed, Boolean unregisteredAllowed, Boolean locked,
            String authControl, String evalCategory, String selectionSettings) {

        this(type, owner, title, instructions, startDate, dueDate, stopDate, viewDate, studentViewResults, studentsDate, instructorViewResults, Boolean.TRUE, instructorsDate, state,
                resultsSharing, instructorOpt, reminderDays, reminderFromEmail, termId, availableEmailTemplate, reminderEmailTemplate, null, template,
                responses, blankResponsesAllowed, modifyResponsesAllowed, unregisteredAllowed, locked,
                authControl, evalCategory, selectionSettings, Boolean.TRUE);
    }

    /**
     * full constructor without email flag
     * @param type
     * @param owner
     * @param title
     * @param instructions
     * @param startDate
     * @param dueDate
     * @param stopDate
     * @param viewDate
     * @param studentViewResults
     * @param studentsDate
     * @param instructorViewResults
     * @param instructorViewAllResults
     * @param instructorsDate
     * @param state
     * @param resultsSharing
     * @param instructorOpt
     * @param reminderDays
     * @param reminderFromEmail
     * @param termId
     * @param availableEmailTemplate
     * @param reminderEmailTemplate
     * @param template
     * @param responses
     * @param blankResponsesAllowed
     * @param modifyResponsesAllowed
     * @param unregisteredAllowed
     * @param locked
     * @param authControl
     * @param evalCategory
     * @param selectionSettings 
     */
    public EvalEvaluation(String type, String owner, String title, String instructions,
            Date startDate, Date dueDate, Date stopDate, Date viewDate, Boolean studentViewResults,
            Date studentsDate, Boolean instructorViewResults, Boolean instructorViewAllResults, Date instructorsDate, String state,
            String resultsSharing, String instructorOpt, Integer reminderDays,
            String reminderFromEmail, String termId, EvalEmailTemplate availableEmailTemplate,
            EvalEmailTemplate reminderEmailTemplate, EvalEmailTemplate submissionConfirmationEmailTemplate, EvalTemplate template,
            Set<EvalResponse> responses, Boolean blankResponsesAllowed,
            Boolean modifyResponsesAllowed, Boolean unregisteredAllowed, Boolean locked, 
            String authControl, String evalCategory, String selectionSettings) {

        this(type, owner, title, instructions, startDate, dueDate, stopDate, viewDate, studentViewResults, studentsDate, instructorViewResults, instructorViewAllResults, instructorsDate, state,
                resultsSharing, instructorOpt, reminderDays, reminderFromEmail, termId, availableEmailTemplate, reminderEmailTemplate, submissionConfirmationEmailTemplate, template,
                responses, blankResponsesAllowed, modifyResponsesAllowed, unregisteredAllowed, locked, authControl,
                evalCategory, selectionSettings, Boolean.TRUE);
    }

    /**
     * full constructor without all rolls can participate
     * @param type
     * @param owner
     * @param title
     * @param instructions
     * @param startDate
     * @param dueDate
     * @param stopDate
     * @param viewDate
     * @param studentViewResults
     * @param studentsDate
     * @param instructorViewResults
     * @param instructorViewAllResults
     * @param instructorsDate
     * @param state
     * @param resultsSharing
     * @param instructorOpt
     * @param reminderDays
     * @param reminderFromEmail
     * @param termId
     * @param availableEmailTemplate
     * @param reminderEmailTemplate
     * @param template
     * @param responses
     * @param blankResponsesAllowed
     * @param modifyResponsesAllowed
     * @param unregisteredAllowed
     * @param locked
     * @param authControl
     * @param evalCategory
     * @param selectionSettings
     * @param emailOpenNotification 
     */
    public EvalEvaluation(String type, String owner, String title, String instructions,
            Date startDate, Date dueDate, Date stopDate, Date viewDate, Boolean studentViewResults,
            Date studentsDate, Boolean instructorViewResults, Boolean instructorViewAllResults, Date instructorsDate, String state,
            String resultsSharing, String instructorOpt, Integer reminderDays,
            String reminderFromEmail, String termId, EvalEmailTemplate availableEmailTemplate,
            EvalEmailTemplate reminderEmailTemplate, EvalEmailTemplate submissionConfirmationEmailTemplate, EvalTemplate template,
            Set<EvalResponse> responses, Boolean blankResponsesAllowed, Boolean modifyResponsesAllowed, 
            Boolean unregisteredAllowed, Boolean locked, String authControl,
            String evalCategory, String selectionSettings, Boolean emailOpenNotification){

        this(type, owner, title, instructions, startDate, dueDate, stopDate, viewDate, studentViewResults, studentsDate, instructorViewResults, instructorViewAllResults, instructorsDate, state,
                resultsSharing, instructorOpt, reminderDays, reminderFromEmail, termId, availableEmailTemplate, reminderEmailTemplate, submissionConfirmationEmailTemplate, template,
                responses, blankResponsesAllowed, modifyResponsesAllowed, unregisteredAllowed, Boolean.FALSE ,locked, authControl,
                evalCategory, selectionSettings, Boolean.TRUE, Boolean.FALSE);
    }

    /**
     * full constructor with sectionAware
     * @param type
     * @param owner
     * @param title
     * @param instructions
     * @param startDate
     * @param dueDate
     * @param stopDate
     * @param viewDate
     * @param studentViewResults
     * @param studentsDate
     * @param instructorViewResults
     * @param instructorViewAllResults
     * @param instructorsDate
     * @param state
     * @param resultsSharing
     * @param instructorOpt
     * @param reminderDays
     * @param reminderFromEmail
     * @param termId
     * @param availableEmailTemplate
     * @param reminderEmailTemplate
     * @param template
     * @param responses
     * @param blankResponsesAllowed
     * @param modifyResponsesAllowed
     * @param unregisteredAllowed
     * @param locked
     * @param authControl
     * @param evalCategory
     * @param selectionSettings
     * @param emailOpenNotification
     * @param sectionAwareness 
     */
    public EvalEvaluation( String type, String owner, String title, String instructions,
            Date startDate, Date dueDate, Date stopDate, Date viewDate, Boolean studentViewResults,
            Date studentsDate, Boolean instructorViewResults, Boolean instructorViewAllResults, Date instructorsDate, String state,
            String resultsSharing, String instructorOpt, Integer reminderDays,
            String reminderFromEmail, String termId, EvalEmailTemplate availableEmailTemplate,
            EvalEmailTemplate reminderEmailTemplate, EvalTemplate template,
            Set<EvalResponse> responses, Boolean blankResponsesAllowed, Boolean modifyResponsesAllowed, 
            Boolean unregisteredAllowed, Boolean locked, String authControl,
            String evalCategory, String selectionSettings, Boolean emailOpenNotification, Boolean sectionAwareness )
    {
        this( type, owner, title, instructions, startDate, dueDate, stopDate, viewDate, studentViewResults, studentsDate, instructorViewResults, instructorViewAllResults, instructorsDate, state,
                resultsSharing, instructorOpt, reminderDays, reminderFromEmail, termId, availableEmailTemplate, reminderEmailTemplate, null, template,
                responses, blankResponsesAllowed, modifyResponsesAllowed, unregisteredAllowed, Boolean.FALSE ,locked, authControl,
                evalCategory, selectionSettings, Boolean.TRUE, sectionAwareness );
    }

    /**
     * full constructor
     * @param type
     * @param owner
     * @param title
     * @param instructions
     * @param startDate
     * @param dueDate
     * @param stopDate
     * @param viewDate
     * @param studentViewResults
     * @param studentsDate
     * @param instructorViewResults
     * @param instructorViewAllResults
     * @param instructorsDate
     * @param state
     * @param resultsSharing
     * @param instructorOpt
     * @param reminderDays
     * @param reminderFromEmail
     * @param termId
     * @param availableEmailTemplate
     * @param reminderEmailTemplate
     * @param submissionConfirmationEmailTemplate
     * @param template
     * @param responses
     * @param blankResponsesAllowed
     * @param modifyResponsesAllowed
     * @param unregisteredAllowed
     * @param allRolesParticipate
     * @param locked
     * @param authControl
     * @param evalCategory
     * @param selectionSettings
     * @param emailOpenNotification
     * @param sectionAwareness 
     */
    public EvalEvaluation(String type, String owner, String title, String instructions,
            Date startDate, Date dueDate, Date stopDate, Date viewDate, Boolean studentViewResults,
            Date studentsDate, Boolean instructorViewResults, Boolean instructorViewAllResults, Date instructorsDate, String state,
            String resultsSharing, String instructorOpt, Integer reminderDays,
            String reminderFromEmail, String termId, EvalEmailTemplate availableEmailTemplate,
            EvalEmailTemplate reminderEmailTemplate, EvalEmailTemplate submissionConfirmationEmailTemplate, EvalTemplate template,
            Set<EvalResponse> responses, Boolean blankResponsesAllowed, Boolean modifyResponsesAllowed, 
            Boolean unregisteredAllowed, Boolean allRolesParticipate,  Boolean locked, String authControl,
            String evalCategory, String selectionSettings, Boolean emailOpenNotification, Boolean sectionAwareness) {

        this.lastModified = new Date();
        this.type = type;
        this.owner = owner;
        this.title = title;
        this.instructions = instructions;
        this.startDate = startDate;
        this.stopDate = stopDate;
        this.dueDate = dueDate;
        this.viewDate = viewDate;
        this.studentViewResults = studentViewResults;
        this.studentsDate = studentsDate;
        this.instructorViewResults = instructorViewResults;
        this.instructorViewAllResults = instructorViewAllResults;
        this.instructorsDate = instructorsDate;
        this.state = state;
        this.instructorOpt = instructorOpt;
        this.reminderDays = reminderDays;
        this.reminderFromEmail = reminderFromEmail;
        this.termId = termId;
        this.availableEmailTemplate = availableEmailTemplate;
        this.reminderEmailTemplate = reminderEmailTemplate;
        this.submissionConfirmationEmailTemplate = submissionConfirmationEmailTemplate; 
        this.template = template;
        this.responses = responses;
        this.resultsSharing = resultsSharing;
        this.blankResponsesAllowed = blankResponsesAllowed;
        this.modifyResponsesAllowed = modifyResponsesAllowed;
        this.allRolesParticipate = allRolesParticipate;
        this.unregisteredAllowed = unregisteredAllowed;
        this.locked = locked;
        this.authControl = authControl;
        this.evalCategory = evalCategory;
        this.selectionSettings = selectionSettings;
        this.sendAvailableNotifications = emailOpenNotification;
        this.sectionAwareness = sectionAwareness;
    }

    /**
     * @return a copy of this object
     */
    public EvalEvaluation copy() {
        return new EvalEvaluation(this);
    }

    private Date copy(Date d) {
        Date copy = null;
        if (d != null) {
            copy = new Date(d.getTime());
        }
        return copy;
    }

    @Override
    public String toString() {
        return "eval: [" + this.type + "] " + this.title + " (" + this.id + ") state=" + this.state
                + " ,start=" + this.startDate + ", due=" + this.dueDate;
    };


    /**
     * @return the due date in a way which ensures it is not null even if the field is
     * @see EvalEvaluation#getSafeEvalDate(EvalEvaluation, String)
     */
    public Date getSafeDueDate() {
        return getSafeEvalDate(this, EvalConstants.EVALUATION_STATE_CLOSED);
    }

    /**
     * @return the stop date in a way which ensures it is not null even if the field is
     * @see EvalEvaluation#getSafeEvalDate(EvalEvaluation, String)
     */
    public Date getSafeStopDate() {
        return getSafeEvalDate(this, EvalConstants.EVALUATION_STATE_GRACEPERIOD);
    }

    /**
     * @return the stop date in a way which ensures it is not null even if the field is
     * @see EvalEvaluation#getSafeEvalDate(EvalEvaluation, String)
     */
    public Date getSafeViewDate() {
        return getSafeEvalDate(this, EvalConstants.EVALUATION_STATE_VIEWABLE);
    }

    /**
     * Gets a date to display to the user depending on the evaluation state requested, 
     * guarantees to return a date even if the dates are null
     * 
     * @param eval an evaluation
     * @param evalState the state which you want to get the date for,
     * e.g. EVALUATION_STATE_VIEWABLE would get the view date and EVALUATION_STATE_CLOSED would get the due date
     * @return a derived date which can be used for display
     */
    public static Date getSafeEvalDate(EvalEvaluation eval, String evalState) {
        if (eval == null) {
            throw new IllegalArgumentException("evaluation must not be null");
        }
        if (eval.getStartDate() == null) {
            throw new IllegalStateException("This eval ("+eval+") is not safe to pull dates from as the start date is not set yet");
        }
        Date date = null;
        if (EvalConstants.EVALUATION_STATE_VIEWABLE.equals(evalState)) {
            if (eval.getViewDate() != null) {
                date = eval.getViewDate();
            } else {
                // defaults to stop date
                evalState = EvalConstants.EVALUATION_STATE_GRACEPERIOD;
            }
        }
        if (EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(evalState)) {
            if (eval.getStopDate() != null) {
                date = eval.getStopDate();
            } else {
                // defaults to due date
                evalState = EvalConstants.EVALUATION_STATE_CLOSED;
            }
        }
        if (EvalConstants.EVALUATION_STATE_CLOSED.equals(evalState)) {
            if (eval.getDueDate() != null) {
                date = eval.getDueDate();
            } else {
                // make up a due date that is 7 days in the future when there is none
                Date now = new Date();
                if (now.after(eval.getStartDate())) {
                    date = new Date(now.getTime() + (1000 * 60 * 60 * 24 * 7));
                } else {
                    date = new Date(eval.getStartDate().getTime() + (1000 * 60 * 60 * 24 * 7));
                }
            }
        }
        if (date == null) {
            // default to using the start date
            date = eval.getStartDate();
        }
        return date;
    }

    /**
     * Get the selection settings out of this assign group (decoded),
     * this is a map of selection type constants like 
     * {@link EvalAssignGroup#SELECTION_TYPE_INSTRUCTOR} or {@link EvalAssignGroup#SELECTION_TYPE_ASSISTANT}
     * to selection option constant like {@link #SELECTION_OPTION_ONE}, 
     * this will be empty of no selection options are set for this assign group,
     * otherwise this will contain the selection options which are set only 
     * (not set should be assumed to mean the default: {@link EvalAssignGroup#SELECTION_OPTION_ALL}) <br/>
     * use the {@link EvalUtils#getSelectionSetting(String, EvalAssignGroup, EvalEvaluation)} method to make comparison easier and more standard
     * 
     * @return the selections as type constant => option constant (e.g. {@link EvalAssignGroup#SELECTION_TYPE_INSTRUCTOR} => {@link EvalAssignGroup#SELECTION_OPTION_ONE}) if any are set
     */
    public Map<String, String> getSelectionOptions() {
        return EvalAssignGroup.decodeSelectionSettings(this.selectionSettings);
    }

    /**
     * Sets the selections to store for a specific category type:
     * {@link EvalAssignGroup#SELECTION_TYPE_INSTRUCTOR} or {@link EvalAssignGroup#SELECTION_TYPE_ASSISTANT}
     * Can also clear the values for a type
     * 
     * @param selectionType the type constant to store selections for:
     * {@link EvalAssignGroup#SELECTION_TYPE_INSTRUCTOR} or {@link EvalAssignGroup#SELECTION_TYPE_ASSISTANT}
     * @param selectionOption one of the constants like {@link #SELECTION_OPTION_ONE},
     * indicates the selection option to use for this selection type,
     * if set to null or {@link #SELECTION_OPTION_ALL} then this selection type will be removed from the stored options
     */
    public void setSelectionOption(String selectionType, String selectionOption) {
        Map<String, String> selections = getSelectionOptions();
        EvalAssignGroup.handleSelectionOption(selectionType, selectionOption, selections);
        setSelectionSettings( EvalAssignGroup.encodeSelectionSettings(selections) );
    }

    // REMINDER STATUS METHODS

    /**
     * This will return the current reminder status based on the coded value in the evaluation
     * @return 
     */
    public EvalReminderStatus getCurrentReminderStatus() {
        EvalReminderStatus rs;
        try {
            rs = new EvalReminderStatus(this.reminderStatus);
        } catch (IllegalArgumentException e) {
            rs = null;
        }
        return rs;
    }

    /**
     * Set this to null in order to clear the current reminder status
     * and to indicate that the reminders processing is complete
     * @param status
     */
    public void setCurrentReminderStatus(EvalReminderStatus status) {
        if (status == null) {
            this.reminderStatus = null;
        } else {
            this.reminderStatus = status.toString();
        }
    }


    // GETTERS and SETTERS

    public String getAuthControl() {
        return authControl;
    }

    public void setAuthControl(String authControl) {
        this.authControl = authControl;
    }

    public Boolean getAvailableEmailSent() {
        return availableEmailSent;
    }

    public EvalEmailTemplate getAvailableEmailTemplate() {
        return availableEmailTemplate;
    }

    public void setAvailableEmailSent(Boolean availableEmailSent) {
        this.availableEmailSent = availableEmailSent;
    }

    public void setAvailableEmailTemplate(EvalEmailTemplate availableEmailTemplate) {
        this.availableEmailTemplate = availableEmailTemplate;
    }

    public Boolean getBlankResponsesAllowed() {
        return blankResponsesAllowed;
    }

    public void setBlankResponsesAllowed(Boolean blankResponsesAllowed) {
        this.blankResponsesAllowed = blankResponsesAllowed;
    }

    public String getEvalCategory() {
        return evalCategory;
    }

    public void setEvalCategory(String evalCategory) {
        this.evalCategory = evalCategory;
    }

    public String getEid() {
        return this.eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getInstructorOpt() {
        return instructorOpt;
    }

    public void setInstructorOpt(String instructorOpt) {
        this.instructorOpt = instructorOpt;
    }

    public Date getInstructorsDate() {
        return instructorsDate;
    }

    public void setInstructorsDate(Date instructorsDate) {
        this.instructorsDate = instructorsDate;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Boolean getModifyResponsesAllowed() {
        return modifyResponsesAllowed;
    }

    public void setModifyResponsesAllowed(Boolean modifyResponsesAllowed) {
        this.modifyResponsesAllowed = modifyResponsesAllowed;
    }

    public Boolean getAllRolesParticipate() {
        return allRolesParticipate;
    }

    public void setAllRolesParticipate(Boolean allRolesParticipate) {
        this.allRolesParticipate = allRolesParticipate;
    }  

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @return the reminder days as a number (no NPE),
     * this will be a 0 if there are no reminders set,
     * -1 indicates 24 hours before the eval closes
     * > 0 indicates the number of days between sending each reminder
     * @see #reminderDays
     */
    public int getReminderDaysInt() {
        return this.getReminderDays() == null ? 0 : this.getReminderDays();
    }

    /**
     * @return 
     * @see #reminderDays
     */
    public Integer getReminderDays() {
        return reminderDays;
    }

    /**
     * @param reminderDays
     * @see #reminderDays
     */
    public void setReminderDays(Integer reminderDays) {
        this.reminderDays = reminderDays;
    }

    public EvalEmailTemplate getReminderEmailTemplate() {
        return reminderEmailTemplate;
    }

    public void setReminderEmailTemplate(EvalEmailTemplate reminderEmailTemplate) {
        this.reminderEmailTemplate = reminderEmailTemplate;
    }

    public String getReminderFromEmail() {
        return reminderFromEmail;
    }

    public void setReminderFromEmail(String reminderFromEmail) {
        this.reminderFromEmail = reminderFromEmail;
    }

    public Set<EvalResponse> getResponses() {
        return responses;
    }

    public void setResponses(Set<EvalResponse> responses) {
        this.responses = responses;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        if (startDate != null && !EvalUtils.safeBool(this.useDateTimes, true)) {
            // force the date to the end of the day when times use is disabled
            startDate = EvalUtils.getEndOfDayDate(startDate);
        }
        this.startDate = startDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void forceDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setDueDate(Date dueDate) {
        if (dueDate != null && !EvalUtils.safeBool(this.useDateTimes, true)) {
            // force the date to the end of the day when times use is disabled
            dueDate = EvalUtils.getEndOfDayDate(dueDate);
        }
        this.dueDate = dueDate;
    }

    public Date getStopDate() {
        return stopDate;
    }

    public void forceStopDate(Date stopDate) {
        this.stopDate = stopDate;
    }

    public void setStopDate(Date stopDate) {
        if (stopDate != null && !EvalUtils.safeBool(this.useDateTimes, true)) {
            // force the date to the end of the day when times use is disabled
            stopDate = EvalUtils.getEndOfDayDate(stopDate);
        }
        this.stopDate = stopDate;
    }

    public Date getViewDate() {
        return viewDate;
    }

    public void setViewDate(Date viewDate) {
        if (viewDate != null && !EvalUtils.safeBool(this.useDateTimes, true)) {
            // force the date to the end of the day when times use is disabled
            viewDate = EvalUtils.getEndOfDayDate(viewDate);
        }
        this.viewDate = viewDate;
    }

    public Date getStudentsDate() {
        return studentsDate;
    }

    public void setStudentsDate(Date studentsDate) {
        this.studentsDate = studentsDate;
    }

    public EvalTemplate getTemplate() {
        return template;
    }

    public void setTemplate(EvalTemplate template) {
        this.template = template;
    }

    public String getTermId() {
        return termId;
    }

    public void setTermId(String termId) {
        this.termId = termId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getUnregisteredAllowed() {
        return unregisteredAllowed;
    }

    public void setUnregisteredAllowed(Boolean unregisteredAllowed) {
        this.unregisteredAllowed = unregisteredAllowed;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResultsSharing() {
        return resultsSharing;
    }

    public void setResultsSharing(String resultsPrivacy) {
        this.resultsSharing = resultsPrivacy;
    }

    public String getAutoUseTag() {
        return autoUseTag;
    }

    public void setAutoUseTag(String autoUseTag) {
        this.autoUseTag = autoUseTag;
    }

    public String getAutoUseInsertion() {
        return autoUseInsertion;
    }

    public void setAutoUseInsertion(String autoUseInsertion) {
        this.autoUseInsertion = autoUseInsertion;
    }

    public boolean getStudentViewResults() {
        return studentViewResults;
    }

    public void setStudentViewResults(boolean studentViewResults) {
        this.studentViewResults = studentViewResults;
    }

    public boolean getInstructorViewResults() {
        return instructorViewResults;
    }

    public void setInstructorViewResults(boolean instructorViewResults) {
        this.instructorViewResults = instructorViewResults;
    }

    public Boolean getInstructorViewAllResults() {
        return instructorViewAllResults;
    }

    public void setInstructorViewAllResults(Boolean instructorViewAllResults) {
        this.instructorViewAllResults = instructorViewAllResults;
    }

    public String getSelectionSettings() {
        return selectionSettings;
    }

    public void setSelectionSettings(String selectionSettings) {
        selectionSettings = EvalAssignGroup.validateSelectionSettings(selectionSettings);
        this.selectionSettings = selectionSettings;
    }

    public EvalEmailTemplate getSubmissionConfirmationEmailTemplate() {
        return submissionConfirmationEmailTemplate;
    }

    public void setSubmissionConfirmationEmailTemplate(
            EvalEmailTemplate submissionConfirmationEmailTemplate) {
        this.submissionConfirmationEmailTemplate = submissionConfirmationEmailTemplate;
    }

    /**
     * NON_PERSISTENT list of assign groups for this eval, may be limited by user
     * @return the evalAssignGroups, will be NULL if the groups were not populated, will be empty if populated but none are set
     */
    public List<EvalAssignGroup> getEvalAssignGroups() {
        return evalAssignGroups;
    }

    public void setEvalAssignGroups(List<EvalAssignGroup> evalAssignGroups) {
        this.evalAssignGroups = evalAssignGroups;
    }

    /**
     * NON_PERSISTENT list of assign groups for this eval, may be limited by user
     * @return the evalAssignGroups, will be NULL if the groups were not populated, will be empty if populated but none are set
     */
    public List<EvalGroup> getEvalGroups() {
        return evalGroups;
    }

    public void setEvalGroups(List<EvalGroup> evalGroups) {
        this.evalGroups = evalGroups;
    }

    public Boolean getSendAvailableNotifications() {
        return sendAvailableNotifications;
    }

    public void setSendAvailableNotifications(Boolean sendAvailableNotifications) {
        this.sendAvailableNotifications = sendAvailableNotifications;
    }

    public String getReminderStatus() {
        return reminderStatus;
    }

    public void setReminderStatus(String reminderStatus) {
        this.reminderStatus = reminderStatus;
    }

    /**
     * @return the localSelector
     */
    public String getLocalSelector() {
        return localSelector;
    }

    /**
     * @param localSelector the localSelector to set
     */
    public void setLocalSelector(String localSelector) {
        this.localSelector = localSelector;
    }

    /**
     * @return the sectionAware
     */
    public Boolean getSectionAwareness()
    {
        return this.sectionAwareness;
    }

    /**
     * @param sectionAwareness the sectionAware to set
     */
    public void setSectionAwareness( Boolean sectionAwareness )
    {
        this.sectionAwareness = sectionAwareness;
    }
}

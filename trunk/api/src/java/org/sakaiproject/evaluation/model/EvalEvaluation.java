
package org.sakaiproject.evaluation.model;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.model.EvalReminderStatus;
import org.sakaiproject.evaluation.utils.EvalUtils;

/**
 * Defines an evaluation itself (this is an instance which can be taken by evaluators)
 */
public class EvalEvaluation implements java.io.Serializable {

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
     * This is the template associated with this evaluation, this should always be a copy of an
     * existing template once the evaluation is created (no longer in the partial state)
     */
    private EvalTemplate template;

    private Set<EvalResponse> responses = new HashSet<EvalResponse>(0);

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

    private Boolean unregisteredAllowed;

    private Boolean availableEmailSent = new Boolean(false);
    
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
	* Optional field never used by EVALSYS code. May be used to mark records for bulk actions. 
	* For example, an import operation might set a value that will be persisted and can be used 
	* later to select records for export, deletion, etc.  Maximum length is 80 characters.
	*/
   public String localSelector;

    /**
     * This is an optional listing of eval groups for this evaluation,
     * if this is non-null and has been set then the groups contained are relevant to service method used
     * to retrieve the evaluations, typically this would include the groups assigned to this eval which
     * are accessible to the current user
     */
    protected List<EvalAssignGroup> evalAssignGroups;

    // Constructors

    /** default constructor */
    public EvalEvaluation() {
    }

    /**
     * minimal constructor
     */
    public EvalEvaluation(String type, String owner, String title, Date startDate, String state,
            String resultsSharing, Integer reminderDays, EvalTemplate template) {
        this(type, owner, title, null, startDate, null, null, null, false, null, true, null, state, resultsSharing, null, reminderDays, null, null, null, null, template, null, null, null, null, Boolean.FALSE, null, null, null);
    }

    /**
     * general use constructor
     */
    public EvalEvaluation(String type, String owner, String title, Date startDate, Date dueDate,
            Date stopDate, Date viewDate, String state, String resultsSharing,
            Integer reminderDays, EvalTemplate template) {
        this(type, owner, title, null, startDate, dueDate, stopDate, viewDate, false, null, true, null, state, resultsSharing, null, reminderDays, null, null, null, null, template, null, null, null, null, Boolean.FALSE, null, null, null);
    }

    /**
     * full constructor without email flag
     */
    public EvalEvaluation(String type, String owner, String title, String instructions,
            Date startDate, Date dueDate, Date stopDate, Date viewDate, boolean studentViewResults,
            Date studentsDate, boolean instructorViewResults, Date instructorsDate, String state,
            String resultsSharing, String instructorOpt, Integer reminderDays,
            String reminderFromEmail, String termId, EvalEmailTemplate availableEmailTemplate,
            EvalEmailTemplate reminderEmailTemplate, EvalTemplate template,
            Set<EvalResponse> responses, Boolean blankResponsesAllowed,
            Boolean modifyResponsesAllowed, Boolean unregisteredAllowed, Boolean locked,
            String authControl, String evalCategory, String selectionSettings) {
    	
    	this(type, owner, title, instructions, startDate, dueDate, stopDate, viewDate, studentViewResults, studentsDate, instructorViewResults, instructorsDate, state,
                resultsSharing, instructorOpt, reminderDays, reminderFromEmail, termId, availableEmailTemplate, reminderEmailTemplate, template,
                responses, blankResponsesAllowed, modifyResponsesAllowed, unregisteredAllowed, locked,
                authControl, evalCategory, selectionSettings, Boolean.TRUE);
    }
    
    /**
     * full constructor
     */
    public EvalEvaluation(String type, String owner, String title, String instructions,
            Date startDate, Date dueDate, Date stopDate, Date viewDate, boolean studentViewResults,
            Date studentsDate, boolean instructorViewResults, Date instructorsDate, String state,
            String resultsSharing, String instructorOpt, Integer reminderDays,
            String reminderFromEmail, String termId, EvalEmailTemplate availableEmailTemplate,
            EvalEmailTemplate reminderEmailTemplate, EvalTemplate template,
            Set<EvalResponse> responses, Boolean blankResponsesAllowed,
            Boolean modifyResponsesAllowed, Boolean unregisteredAllowed, Boolean locked,
            String authControl, String evalCategory, String selectionSettings, Boolean sendAvailableNotifications) {
    	
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
        this.instructorsDate = instructorsDate;
        this.state = state;
        this.instructorOpt = instructorOpt;
        this.reminderDays = reminderDays;
        this.reminderFromEmail = reminderFromEmail;
        this.termId = termId;
        this.availableEmailTemplate = availableEmailTemplate;
        this.reminderEmailTemplate = reminderEmailTemplate;
        this.template = template;
        this.responses = responses;
        this.resultsSharing = resultsSharing;
        this.blankResponsesAllowed = blankResponsesAllowed;
        this.modifyResponsesAllowed = modifyResponsesAllowed;
        this.unregisteredAllowed = unregisteredAllowed;
        this.locked = locked;
        this.authControl = authControl;
        this.evalCategory = evalCategory;
        this.selectionSettings = selectionSettings;
    	this.sendAvailableNotifications = sendAvailableNotifications;
    	if(this.availableEmailSent == null) {
    		this.availableEmailSent = new Boolean(false);
    	}
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

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
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
     * @see #reminderDays
     */
    public Integer getReminderDays() {
        return reminderDays;
    }

    /**
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getStopDate() {
        return stopDate;
    }

    public void setStopDate(Date stopDate) {
        this.stopDate = stopDate;
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

    public Date getViewDate() {
        return viewDate;
    }

    public void setViewDate(Date viewDate) {
        this.viewDate = viewDate;
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

    public String getSelectionSettings() {
        return selectionSettings;
    }

    public void setSelectionSettings(String selectionSettings) {
        selectionSettings = EvalAssignGroup.validateSelectionSettings(selectionSettings);
        this.selectionSettings = selectionSettings;
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
	
}


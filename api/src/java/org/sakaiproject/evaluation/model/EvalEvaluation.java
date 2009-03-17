
package org.sakaiproject.evaluation.model;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;

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
     * The key which determines which way to render instructors items in this evaluation (can be
     * overridden by specific group assignments which will inherit this setting value by default),
     * use the {@link EvalAssignHierarchy} SELECTION_* constants like
     * {@link EvalAssignHierarchy#SELECTION_ONE}, default is null (equivalent to all)
     */
    protected String instructorSelection;
    /**
     * The key which determines which way to render assistants items in this evaluation (can be
     * overridden by specific group assignments which will inherit this setting value by default),
     * use the {@link EvalAssignHierarchy} SELECTION_* constants like
     * {@link EvalAssignHierarchy#SELECTION_ONE}, default is null (equivalent to all)
     */
    protected String assistantSelection;

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
     */
    private String state;

    private String instructorOpt;

    /**
     * the number of days between reminder emails, 0 means emails are disabled
     */
    private Integer reminderDays;

    /**
     * This will be the email address that notification emails should be sent from for this
     * evaluation
     */
    private String reminderFromEmail;

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

    private Boolean locked;

    private String authControl;

    private String evalCategory;

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
        if (this.lastModified == null) {
            this.lastModified = new Date();
        }
        this.type = type;
        this.owner = owner;
        this.title = title;
        this.startDate = startDate;
        this.state = state;
        this.resultsSharing = resultsSharing;
        this.reminderDays = reminderDays;
        this.template = template;
    }

    /**
     * general use constructor
     */
    public EvalEvaluation(String type, String owner, String title, Date startDate, Date dueDate,
            Date stopDate, Date viewDate, String state, String resultsSharing,
            Integer reminderDays, EvalTemplate template) {
        if (this.lastModified == null) {
            this.lastModified = new Date();
        }
        this.type = type;
        this.owner = owner;
        this.title = title;
        this.startDate = startDate;
        this.stopDate = stopDate;
        this.dueDate = dueDate;
        this.viewDate = viewDate;
        this.state = state;
        this.resultsSharing = resultsSharing;
        this.reminderDays = reminderDays;
        this.template = template;
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
            String authControl, String evalCategory, String instructorSelection, String assistantSelection) {
        if (this.lastModified == null) {
            this.lastModified = new Date();
        }
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
        this.instructorSelection = instructorSelection;
        this.assistantSelection = assistantSelection;
    }

    @Override
    public String toString() {
        return "eval: [" + this.type + "] " + this.title + " (" + this.id + ") state=" + this.state
                + " ,start=" + this.startDate + ", due=" + this.dueDate;
    };

    public String getAuthControl() {
        return authControl;
    }

    public void setAuthControl(String authControl) {
        this.authControl = authControl;
    }

    public EvalEmailTemplate getAvailableEmailTemplate() {
        return availableEmailTemplate;
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

    public Integer getReminderDays() {
        return reminderDays;
    }

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

    public String getInstructorSelection() {
        return instructorSelection;
    }

    public void setInstructorSelection(String instructorSelection) {
        this.instructorSelection = instructorSelection;
    }

    public String getAssistantSelection() {
        return assistantSelection;
    }

    public void setAssistantSelection(String assistantSelection) {
        this.assistantSelection = assistantSelection;
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
    
}

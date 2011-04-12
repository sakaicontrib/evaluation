/**
 * $Id$
 * $URL$
 * SetupEvalBean.java - evaluation - Mar 18, 2008 4:38:20 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.exceptions.BlankRequiredFieldException;
import org.sakaiproject.evaluation.logic.exceptions.InvalidDatesException;
import org.sakaiproject.evaluation.logic.exceptions.InvalidEvalCategoryException;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.locators.AssignGroupSelectionSettings;
import org.sakaiproject.evaluation.tool.locators.EmailTemplateWBL;
import org.sakaiproject.evaluation.tool.locators.EvaluationBeanLocator;
import org.sakaiproject.evaluation.tool.locators.SelectedEvaluationUsersLocator;
import org.sakaiproject.evaluation.utils.ArrayUtils;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

/**
 * This action bean helps with the evaluation setup process where needed, this is a pea
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class SetupEvalBean {

	private static Log log = LogFactory.getLog(SetupEvalBean.class);

	private final String EVENT_EVAL_REOPENED = "eval.evaluation.reopened";

	/**
	 * This should be set to true while we are creating an evaluation
	 */
	public boolean creatingEval = false;
	/**
	 * This should be set to the evalId we are currently working with
	 */
	public Long evaluationId;
	/**
	 * This should be set to the templateId we are assigning to the evaluation
	 */
	public Long templateId;
	/**
	 * This should be set to the emailTemplateId we are assigning to the
	 * evaluation
	 */
	public Long emailTemplateId;
	/**
	 * This is set to the type of email template when resetting the evaluation
	 * to use default templates
	 */
	public String emailTemplateType;
	/**
	 * Set to true if we are reopening this evaluation
	 */
	public boolean reOpening = false;

    /** 
     * Whether to navigate to administrate_search view after saving settings
     */
    public boolean returnToSearchResults = false;
    
    /** 
     * The value of searchString needed by AdministrateSearchProducer.fillCommponents() 
     * to restore administrate_search view after saving settings
     */
    public String adminSearchString = null;

    /** 
     * The value of page needed by AdministrateSearchProducer.fillCommponents() 
     * to restore administrate_search view after saving settings
     */ 
    public int adminSearchPage = 0;
        
	/**
	 * the selected groups ids to bind to this evaluation when creating it
	 */
	public String[] selectedGroupIDs = new String[] {};
	/**
	 * the selected hierarchy nodes to bind to this evaluation when creating it
	 */
	public String[] selectedHierarchyNodeIDs = new String[] {};
    
    /**
	 * the selected option (eg. for TAs and Instructors) in this evaluation. see
	 * {@link EvalEvaluation.selectionSettings}
	 */
	public Map<String, String> selectionOptions = new HashMap<String, String>();
	/**
	 * selection value to populate {@link SetupEvalBean.selectionOptions}
	 */
	public String selectionInstructors, selectionAssistants;

	private EvalEvaluationService evaluationService;
	public void setEvaluationService(EvalEvaluationService evaluationService) {
		this.evaluationService = evaluationService;
	}

	private EvalCommonLogic commonLogic;
	public void setCommonLogic(EvalCommonLogic commonLogic) {
		this.commonLogic = commonLogic;
	}

	private ExternalHierarchyLogic hierarchyLogic;
	public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
		this.hierarchyLogic = logic;
	}

	private EvalEvaluationSetupService evaluationSetupService;
	public void setEvaluationSetupService(
			EvalEvaluationSetupService evaluationSetupService) {
		this.evaluationSetupService = evaluationSetupService;
	}

	private EvalAuthoringService authoringService;
	public void setAuthoringService(EvalAuthoringService authoringService) {
		this.authoringService = authoringService;
	}

	private EvaluationBeanLocator evaluationBeanLocator;
	public void setEvaluationBeanLocator(
			EvaluationBeanLocator evaluationBeanLocator) {
		this.evaluationBeanLocator = evaluationBeanLocator;
	}

	private EmailTemplateWBL emailTemplateWBL;
	public void setEmailTemplateWBL(EmailTemplateWBL emailTemplateWBL) {
		this.emailTemplateWBL = emailTemplateWBL;
	}

	private TargettedMessageList messages;
	public void setMessages(TargettedMessageList messages) {
		this.messages = messages;
	}

	private Locale locale;
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	private SelectedEvaluationUsersLocator selectedEvaluationUsersLocator;
	public void setSelectedEvalautionUsersLocator(SelectedEvaluationUsersLocator selectedEvaluationUsersLocator) {
		this.selectedEvaluationUsersLocator = selectedEvaluationUsersLocator;
	}
	
	private AssignGroupSelectionSettings assignGroupSelectionSettings;
	public void setAssignGroupSelectionSettings(
			AssignGroupSelectionSettings assignGroupSelectionSettings) {
		this.assignGroupSelectionSettings = assignGroupSelectionSettings;
	}
	
	private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }

	DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);

	/**
	 * sets he locale on the date formatter correctly
	 */
	public void init() {
		df = DateFormat.getDateInstance(DateFormat.LONG, locale);
	}

	// Action bindings

	/**
	 * Handles removal action from the remove eval view
	 */
	public String removeEvalAction() {
		if (evaluationId == null) {
			throw new IllegalArgumentException("evaluationId cannot be null");
		}
		EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
		evaluationSetupService.deleteEvaluation(evaluationId, commonLogic
				.getCurrentUserId());
		messages.addMessage(new TargettedMessage(
				"controlevaluations.delete.user.message", new Object[] { eval
						.getTitle() }, TargettedMessage.SEVERITY_INFO));
		return "success";
	}

	/**
	 * Handles close eval action from control evaluations view
	 */
	public String closeEvalAction() {
		if (evaluationId == null) {
			throw new IllegalArgumentException("evaluationId cannot be null");
		}
		EvalEvaluation eval = evaluationSetupService.closeEvaluation(
				evaluationId, commonLogic.getCurrentUserId());
		messages.addMessage(new TargettedMessage(
				"controlevaluations.closed.user.message", new Object[] { eval
						.getTitle() }, TargettedMessage.SEVERITY_INFO));
		return "success";
	}

	/**
	 * Handles reopening evaluation action (from eval settings view)
	 */
	public String reopenEvalAction() {
		if (evaluationId == null) {
			throw new IllegalArgumentException("evaluationId cannot be null");
		}
		EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
		// TODO reopen action
		// evaluationSetupService.deleteEvaluation(evaluationId,
		// commonLogic.getCurrentUserId());
		messages.addMessage(new TargettedMessage(
				"controlevaluations.reopen.user.message", new Object[] { eval
						.getTitle() }, TargettedMessage.SEVERITY_INFO));
		return "success";
	}

	/**
	 * Handles saving and assigning email templates to an evaluation, can just
	 * assign the email template if the emailTemplateId is set or will save and
	 * assign the one in the locator
	 */
    public String saveAndAssignEmailTemplate() {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId and emailTemplateId cannot be null");
        }

        EvalEmailTemplate emailTemplate = null;
        if (emailTemplateId == null) {
            // get it from the locator
            emailTemplateWBL.saveAll();
            emailTemplate = emailTemplateWBL.getCurrentEmailTemplate();
        } else {
            // just load up and assign the template and do not save it
            emailTemplate = evaluationService.getEmailTemplate(emailTemplateId);
        }

        // assign to the evaluation
        evaluationSetupService.assignEmailTemplate(emailTemplate.getId(), evaluationId, null,
                commonLogic.getCurrentUserId());

        // user message
        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        messages.addMessage(new TargettedMessage("controlemailtemplates.template.assigned.message",
                        new Object[] { emailTemplate.getType(), emailTemplate.getSubject(),
                                eval.getTitle() }, TargettedMessage.SEVERITY_INFO));
        return "successAssign";
    }


	/**
	 * Handles resetting the evaluation to use the default template
	 */
	public String resetToDefaultEmailTemplate() {
		if (evaluationId == null || emailTemplateType == null) {
			throw new IllegalArgumentException(
					"evaluationId and emailTemplateType cannot be null");
		}

		// reset to default email template
		evaluationSetupService.assignEmailTemplate(null, evaluationId,
				emailTemplateType, commonLogic.getCurrentUserId());

		return "successReset";
	}

	// NOTE: these are the simple navigation methods
	// 4 steps to create an evaluation: 1) Create -> 2) Settings -> 3) Assign ->
	// 4) Confirm/Save

	/**
	 * Completed the initial creation page where the template is chosen
	 */
	public String completeCreateAction() {
		// set the template on the evaluation in the bean locator
		if (templateId == null) {
			throw new IllegalStateException(
					"The templateId is null, it must be set so it can be assigned to the new evaluation");
		}
		EvalEvaluation eval = evaluationBeanLocator.getCurrentEval();
		if (eval == null) {
			throw new IllegalStateException(
					"The evaluation cannot be retrieved from the bean locator, critical failure");
		}
		EvalTemplate template = authoringService.getTemplateById(templateId);
		eval.setTemplate(template);

		// save the new evaluation
		try {
			setSelectionOptions();
			evaluationBeanLocator.saveAll();
		} catch (BlankRequiredFieldException e) {
			messages.addMessage(new TargettedMessage(e.messageKey,
					new Object[] { e.fieldName },
					TargettedMessage.SEVERITY_ERROR));
			throw new RuntimeException(e); // should not be needed but it is
		}
		return "evalSettings";
	}

	/**
	 * Updated or initially set the evaluation settings
	 */
	public String completeSettingsAction() {
		// set the template on the evaluation in the bean locator if not null
		if (templateId != null) {
			EvalEvaluation eval = evaluationBeanLocator.getCurrentEval();
			if (eval != null) {
				EvalTemplate template = authoringService
						.getTemplateById(templateId);
				eval.setTemplate(template);
			}
		}

		try {
			setSelectionOptions();
			evaluationBeanLocator.saveAll();
		} catch (BlankRequiredFieldException e) {
            messages.addMessage(new TargettedMessage(e.messageKey, new Object[] { e.fieldName },
                    TargettedMessage.SEVERITY_ERROR));
            throw new RuntimeException(e); // should not be needed but it is
        } catch (InvalidDatesException e) {
            messages.addMessage(new TargettedMessage(e.messageKey, new Object[] {},
                    TargettedMessage.SEVERITY_ERROR));
            throw new RuntimeException(e); // should not be needed but it is
        } catch (InvalidEvalCategoryException e) {
        	messages.addMessage(new TargettedMessage(e.messageKey, new Object[] {},
                    TargettedMessage.SEVERITY_ERROR));
            throw new RuntimeException(e); // should not be needed but it is
        }

		EvalEvaluation eval = evaluationBeanLocator.getCurrentEval();
		String destination = "controlEvals";
		if(this.returnToSearchResults) {
        	destination = "adminSearch";
        } else if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(eval.getState())) {
			destination = "evalAssign";
		} else {
			if (reOpening) {
				// we are reopening the evaluation
				commonLogic.registerEntityEvent(EVENT_EVAL_REOPENED, eval);
				messages.addMessage(new TargettedMessage(
						"controlevaluations.reopen.user.message",
						new Object[] { eval.getTitle() },
						TargettedMessage.SEVERITY_INFO));
			} else {
				messages.addMessage(new TargettedMessage(
						"evalsettings.updated.message", new Object[] { eval
								.getTitle() }, TargettedMessage.SEVERITY_INFO));
			}
		}
		return destination;
	}

    // NOTE: There is no action for the 3) assign step because that one just passes the data
    // straight to the confirm view

	// TODO - how do we handle removing assignments? (Currently not supported)

	/**
	 * Complete the creation process for an evaluation (view all the current
	 * settings and assignments and create eval/assignments), this will save the
	 * node/group assignments that were submitted and reconcile this list with
	 * the previous set of nodes/groups and remove any that are now missing
	 * before adding the new ones
	 */
	public String completeConfirmAction() {
		if (evaluationId == null) {
			throw new IllegalArgumentException(
					"evaluationId and emailTemplateId cannot be null");
		}

		// make sure that the submitted nodes are valid and populate the nodes
		// list
		Set<EvalHierarchyNode> nodes = null;
		if (selectedHierarchyNodeIDs.length > 0) {
			nodes = hierarchyLogic.getNodesByIds(selectedHierarchyNodeIDs);
			if (nodes.size() != selectedHierarchyNodeIDs.length) {
				throw new IllegalArgumentException(
						"Invalid set of hierarchy node ids submitted which "
								+ "includes node Ids which are not in the hierarchy: "
								+ ArrayUtils.arrayToString(selectedHierarchyNodeIDs));
			}
		} else {
			nodes = new HashSet<EvalHierarchyNode>();
		}

		// at least 1 node or group must be selected
		if (selectedGroupIDs.length == 0 && nodes.isEmpty()) {
			messages.addMessage(new TargettedMessage(
					"assigneval.invalid.selection", new Object[] {},
					TargettedMessage.SEVERITY_ERROR));
			return "fail";
		}

		EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
		if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(eval.getState())) {
			// save eval and assign groups

			// save the new evaluation state (moving from partial), set to true
			// should fix up the state automatically
			evaluationSetupService.saveEvaluation(eval, commonLogic
					.getCurrentUserId(), true);

			// NOTE - this allows the evaluation to be saved with zero assign
			// groups if this fails

			// save all the assignments (hierarchy and group)
			List<EvalAssignHierarchy> assignedHierList = evaluationSetupService
					.setEvalAssignments(evaluationId, selectedHierarchyNodeIDs,
							selectedGroupIDs, false);

			// failsafe check (to make sure we are not creating an eval with no
			// assigned groups)
			if (assignedHierList.isEmpty()) {
				evaluationSetupService.deleteEvaluation(evaluationId,
						commonLogic.getCurrentUserId());
				throw new IllegalStateException(
						"Invalid evaluation created with no assignments! Destroying evaluation: "
								+ evaluationId);
			}

			messages.addMessage(new TargettedMessage(
					"controlevaluations.create.user.message", new Object[] {
							eval.getTitle(), df.format(eval.getStartDate()) },
					TargettedMessage.SEVERITY_INFO));
		} else {
			// just assigning groups
			if (EvalUtils.checkStateAfter(eval.getState(),
					EvalConstants.EVALUATION_STATE_ACTIVE, false)) {
				throw new IllegalStateException(
						"User cannot update evaluation assignments after an evaluation is active and complete");
			}

			// make sure we cannot remove assignments for active evals
			boolean append = false;
			if (EvalUtils.checkStateAfter(eval.getState(),
					EvalConstants.EVALUATION_STATE_INQUEUE, false)) {
				append = true; // can only append after active
			}

			// save all the assignments (hierarchy and group)
			evaluationSetupService.setEvalAssignments(evaluationId,
					selectedHierarchyNodeIDs, selectedGroupIDs, append);
		}
		
		//Work with selection options
		if( ((Boolean)settings.get(EvalSettings.ENABLE_INSTRUCTOR_ASSISTANT_SELECTION)).booleanValue() ){
			Map<Long, List<EvalAssignGroup>> evalAssignGroupMap = evaluationService.getAssignGroupsForEvals(new Long[] {evaluationId}, true, false);
			List<EvalAssignGroup> evalAssignGroups = evalAssignGroupMap.get(evaluationId);
			
			// Query DB only once to get all EvalAssignUsers
			List<EvalAssignUser> evalUsers = evaluationService
				.getParticipantsForEval(evaluationId, null, null, null, EvalEvaluationService.STATUS_ANY, null, null);
			
			for (EvalAssignGroup assignGroup : evalAssignGroups) {
				String currentGroupId = assignGroup.getEvalGroupId();
				// Save Assistant/Instructor selections now. EVALSYS-618
				String[] deselectedInstructors = selectedEvaluationUsersLocator.getDeselectedInstructors(currentGroupId);
				String[] deselectedAssistants = selectedEvaluationUsersLocator.getDeselectedAssistants(currentGroupId);
				String[] orderingInstructors = selectedEvaluationUsersLocator.getOrderingForInstructors(currentGroupId);
				String[] orderingAssistants = selectedEvaluationUsersLocator.getOrderingForAssistants(currentGroupId);
				updateEvalAssignUsers(deselectedInstructors, orderingInstructors, EvalAssignUser.TYPE_EVALUATEE, currentGroupId, evalUsers);
				updateEvalAssignUsers(deselectedAssistants, orderingAssistants, EvalAssignUser.TYPE_ASSISTANT,currentGroupId, evalUsers);
				// set selection settings for assign group
				String settingInstructor = assignGroupSelectionSettings.getInstructorSetting(currentGroupId);
				String settingAssistant = assignGroupSelectionSettings.getAssistantSetting(currentGroupId);
				if( settingInstructor == null || "".equals(settingInstructor)){
					assignGroup.setSelectionOption(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR, EvalAssignGroup.SELECTION_OPTION_MULTIPLE );
				}else{
					assignGroup.setSelectionOption(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR, settingInstructor );
				}
				if( settingAssistant == null || "".equals(settingAssistant)){
					assignGroup.setSelectionOption(EvalAssignGroup.SELECTION_TYPE_ASSISTANT, EvalAssignGroup.SELECTION_OPTION_MULTIPLE );
				}else{
					assignGroup.setSelectionOption(EvalAssignGroup.SELECTION_TYPE_ASSISTANT, settingAssistant );
				}
				//Save selection settings
				evaluationSetupService.saveAssignGroup(assignGroup, commonLogic.getCurrentUserId());
			}
		}
		
		return "controlEvals";
	}

	
	// NOTE: these are the support methods

	/**
	 * Turn a boolean selection map into an array of the keys
	 * 
	 * @param booleanSelectionMap
	 *            a map of string -> boolean (from RSF bound booleans)
	 * @return an array of the keys where boolean is true
	 */
	public static String[] makeArrayFromBooleanMap(Map<String, Boolean> booleanSelectionMap) {
        List<String> hierNodeIdList = new ArrayList<String>();
        for (Entry<String, Boolean> entry: booleanSelectionMap.entrySet()) {
            if ( EvalUtils.safeBool(entry.getValue()) ) {
                hierNodeIdList.add(entry.getKey());
            }
        }
        String[] selectedHierarchyNodeIds = hierNodeIdList.toArray(new String[] {});
        return selectedHierarchyNodeIds;
    }

	/**
	 * Create {@link Map} object to inject into Eval and set Selection Options
	 * eg.for {@link EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR} and
	 * {@link EvalAssignGroup.SELECTION_TYPE_ASSISTANT}
	 * 
	 * @return selectionOptions {@link Map}
	 */
	private Map<String, String> setSelectionOptions() {
		selectionOptions.put(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR,
				selectionInstructors);
		selectionOptions.put(EvalAssignGroup.SELECTION_TYPE_ASSISTANT,
				selectionAssistants);
		return selectionOptions;
	}

	/**
	 * Remove this {@link List} of userIds from being assigned to current
	 * evaluation
	 * 
	 * @param deselected
	 * @param ordering 
	 * @param type either {@link EvalAssignUser.TYPE_EVALUATEE} or {@link EvalAssignUser.TYPE_ASSISTANT}
	 * @param currentGroupId 
	 * @param evalUsers 
	 */
	private void updateEvalAssignUsers(String[] deselected, String[] ordering, String type, String currentGroupId, List<EvalAssignUser> evalUsers) {
		if (deselected != null && deselected.length > 0){
			List<String> deselectedList = Arrays.asList(deselected);
			List<String> orderingList = Arrays.asList(ordering);
			for (EvalAssignUser user : evalUsers) {
				// only update users for this group with this permission type
				String userId = user.getUserId();
				if(currentGroupId.equals( user.getEvalGroupId().toString() ) && type.equals( user.getType()) ){
					if (deselectedList.contains( userId )) {
						user.setStatus(EvalAssignUser.STATUS_REMOVED);
					}else{
						user.setStatus(EvalAssignUser.STATUS_LINKED);
					}
					// set users' selection order
					if (orderingList.contains( userId )){
						int listOrder = orderingList.indexOf(userId) + 1;
						user.setListOrder( listOrder );
					}
				}
			}
		}
	}	
}

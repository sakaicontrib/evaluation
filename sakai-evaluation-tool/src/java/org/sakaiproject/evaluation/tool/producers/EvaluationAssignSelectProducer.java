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
/**********************************************************************************
 * @author lovemorenalube
 **********************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class EvaluationAssignSelectProducer extends EvalCommonProducer implements ViewParamsReporter{
	
	public static final String VIEW_ID = "evaluation_assign_select";

	public String getViewID() {
		return VIEW_ID;
	}
	private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic bean) {
        this.commonLogic = bean;
    }
    private EvalEvaluationService evaluationService; 
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }
    
    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }
  
	public void fill(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		String groupTitle, evalGroupId, selectType;  //Hold values passed via URL. selectType refers to what type of role is being show eg. Instructor or Assistant
		Long evalId;
		EvalViewParameters evalParameters;
		
		boolean isInstructor, isAssistant;
		
		UIForm form = UIForm.make(tofill, "form");
		String actionBean = "setupEvalBean.";
		String actionBeanVariable = actionBean;
		//Deal with EvalGroups
		 if(viewparams != null && viewparams instanceof EvalViewParameters){
			evalParameters = (EvalViewParameters) viewparams;
			evalId = evalParameters.evaluationId;
			evalGroupId = evalParameters.evalGroupId;
			groupTitle = commonLogic.getDisplayTitle(evalGroupId);
			selectType = evalParameters.evalCategory;
			isInstructor = EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR.equals(selectType);
			isAssistant = EvalAssignGroup.SELECTION_TYPE_ASSISTANT.equals(selectType);

			/**
			 * This is the evaluation we are working with on this page,
			 * this should ONLY be read from, do not change any of these fields
			 */
			EvalEvaluation evaluation = evaluationService.getEvaluationById(evalId);

			Set<String> users;
			if(isInstructor){
				users = commonLogic.getUserIdsForEvalGroup(evalGroupId, EvalConstants.PERM_BE_EVALUATED, evaluation.getSectionAwareness());
			}else if(isAssistant){
				users = commonLogic.getUserIdsForEvalGroup(evalGroupId, EvalConstants.PERM_ASSISTANT_ROLE, evaluation.getSectionAwareness());
			}else{
				throw new InvalidParameterException("Cannot handle this selection type: "+selectType);
			}
			
			//Get users
			List<EvalUser> evalUsers = commonLogic.getEvalUsersByIds(users.toArray(new String[users.size()]));
			//Sort the users list by displayName
			Collections.sort(evalUsers, new EvalUser.SortNameComparator());
			
			for(EvalUser evalUser : evalUsers){
				UIBranchContainer row = UIBranchContainer.make(form, "item-row:");
				UIBoundBoolean bb = UIBoundBoolean.make(row, "row-select", Boolean.TRUE);
				bb.decorators = new DecoratorList( new UIIDStrategyDecorator(evalUser.userId) );
	            UIOutput.make(row, "row-number", evalUser.username); 
	            UIOutput.make(row, "row-name", evalUser.sortName);
	        }
			
        String currentEvalState = evaluationService.returnAndFixEvalState(evaluation, true);
        
        //do a check for the Header
		UIMessage.make(tofill, "title", (isInstructor ? "assignselect.instructors.page.header" : "assignselect.tas.page.header"),
				new Object[] {groupTitle, evaluation.getTitle()});
		
		 // EVALUATION INSTRUCTOR/TA SELECTION
		UIMessage.make(form, "select-instructions", isInstructor ? "assignselect.instructions.instructors" : "assignselect.instructions.assistants" );
        if((Boolean) settings.get(EvalSettings.ENABLE_INSTRUCTOR_ASSISTANT_SELECTION)){
        	UIBranchContainer selectFieldSet = UIBranchContainer.make(form, "selectInstructorTA:");
	        String[] selectValues = new String[] {
	                    EvalAssignGroup.SELECTION_OPTION_ALL,
	                    EvalAssignGroup.SELECTION_OPTION_ONE, 
	                    EvalAssignGroup.SELECTION_OPTION_MULTIPLE};    
	         // radio buttons for the INSTRUCTOR selection options
	         if(isInstructor){
	            String savedSettingInstructor = getEvalSelectionSettings(evaluation, EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR, currentEvalState);
	            //if this is a new eval, default setting is {@link EvalAssignGroup.SELECTION_OPTION_MULTIPLE} EVALSYS-903
	            if(EvalUtils.checkStateBefore(currentEvalState, EvalConstants.EVALUATION_STATE_PARTIAL, true)){
	            	savedSettingInstructor = EvalAssignGroup.SELECTION_OPTION_MULTIPLE;
	            }
	            UISelect selectInstructors = UISelect.make(selectFieldSet, "selectionRadioInstructors", 
	                    selectValues, 
	                    new String[] {"evalsettings.selection.instructor.all",
	                    "evalsettings.selection.instructor.one",
	            "evalsettings.selection.instructor.many"},
	            actionBean + "selectionInstructors", savedSettingInstructor).setMessageKeys();
	            String selectInstructorsId = selectInstructors.getFullID();
	            for (int i = 0; i < selectValues.length; ++i) {
	                UIBranchContainer radiobranch = UIBranchContainer.make(selectFieldSet, "selectInstructorsChoice:", i + "");
	                UISelectChoice choice = UISelectChoice.make(radiobranch, "radioValue", selectInstructorsId, i);
	                UISelectLabel.make(radiobranch, "radioLabel", selectInstructorsId, i)
	                .decorate( new UILabelTargetDecorator(choice) );
	            }
        	}
            // radio buttons for the TA selection options
	        if(isAssistant){
	            String savedAssistantInstructor = getEvalSelectionSettings(evaluation, EvalAssignGroup.SELECTION_TYPE_ASSISTANT, currentEvalState);
	            UISelect selectTAs = UISelect.make(selectFieldSet, "selectionRadioTAs", 
	                    selectValues, 
	                    new String[] {"evalsettings.selection.ta.all","evalsettings.selection.ta.one","evalsettings.selection.ta.many"},
	                    actionBean + "selectionAssistants", savedAssistantInstructor).setMessageKeys();
	            String selectTAsId = selectTAs.getFullID();
	            for (int i = 0; i < selectValues.length; ++i) {
	                UIBranchContainer radiobranch = UIBranchContainer.make(selectFieldSet, "selectTAsChoice:", i + "");
	                UISelectChoice choice = UISelectChoice.make(radiobranch, "radioValue", selectTAsId, i);
	                UISelectLabel.make(radiobranch, "radioLabel", selectTAsId, i)
	                .decorate( new UILabelTargetDecorator(choice) );}
	        }
        }
		
		if(isInstructor){
			UIMessage.make(form, "instruction", "assignselect.instructions.instructor");
		}else if (isAssistant){
			UIMessage.make(form, "instruction", "assignselect.instructions.assistant");
		}
		UIMessage.make(form, "col-number", "assignselect.table.numbers");
		UIMessage.make(form, "col-name", "assignselect.table.names");
		 
		UIMessage.make(form, "save-item-action","assignselect.form.save" );
		UIMessage.make(form, "cancel-button", "general.cancel.button");
		
		 }else{
			throw new IllegalArgumentException("Params passed are not proper.");
		}
	}

	public ViewParameters getViewParameters() {
		// TODO Auto-generated method stub
		return new EvalViewParameters();
	}
	
	private String getEvalSelectionSettings(EvalEvaluation evaluation, String selectionTypeInstructor, String currentEvalState){
		String selection = EvalUtils.getSelectionSetting(selectionTypeInstructor, null, evaluation);

		//if this is a new eval, default setting is {@link EvalAssignGroup.SELECTION_OPTION_MULTIPLE} EVALSYS-903
        if(EvalUtils.checkStateBefore(currentEvalState, EvalConstants.EVALUATION_STATE_PARTIAL, true)){
        	selection = EvalAssignGroup.SELECTION_OPTION_MULTIPLE;
        }
        return selection;
	}
}

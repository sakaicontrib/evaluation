/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2012 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the
"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Michael Gillian (mgillian@unicon.net)
 **********************************************************************************/
package org.sakaiproject.evaluation.tool.renderers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.producers.ControlEvaluationsProducer;
import org.sakaiproject.evaluation.tool.producers.EvaluationRespondersProducer;
import org.sakaiproject.evaluation.tool.producers.EvaluationSettingsProducer;
import org.sakaiproject.evaluation.tool.producers.PreviewEvalProducer;
import org.sakaiproject.evaluation.tool.producers.ReportChooseGroupsProducer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * AdminBoxRender renders the list of Evaluations that the user administers. 
 * @author mgillian
 */
//public class AdminBoxRenderer implements ItemRenderer {
public class AdminBoxRenderer {
	
    private DateFormat df;
    
    private Locale locale;
    public void setLocale(Locale locale) {
       this.locale = locale;
    }    
    
    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }
    
    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }
    
    private EvalEvaluationSetupService evaluationSetupService;
    public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
        this.evaluationSetupService = evaluationSetupService;
    }
    
    private EvalDeliveryService deliveryService;
    public void setDeliveryService(EvalDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }
    
    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }
    
    private HumanDateRenderer humanDateRenderer;
    public void setHumanDateRenderer(HumanDateRenderer humanDateRenderer) {
        this.humanDateRenderer = humanDateRenderer;
    }
    
    public void init() {
        df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);        
    }
    
	public static final String COMPONENT_ID = "evalAdminBox:";
	
	public UIJointContainer renderItem(UIContainer parent, String ID) {
        UIJointContainer container = new UIJointContainer(parent, ID, COMPONENT_ID);
		renderBox(container);
		return container;
	}
	
	private void renderBox(UIContainer tofill) {
        String currentUserId = commonLogic.getCurrentUserId();		
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
        boolean beginEvaluation = evaluationService.canBeginEvaluation(currentUserId);
        Boolean instViewResults = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
        if (instViewResults == null) {
            instViewResults = true;
        } // if configurable then we will assume some are probably shared
        
        List<EvalEvaluation> evals = evaluationSetupService.getVisibleEvaluationsForUser(currentUserId, true, instViewResults, false);

        // If the person is an admin, then just point new evals to existing
        // object. If the person is not an admin then only show owned evals +
        // not-owned evals that are available for viewing results.
        List<EvalEvaluation> newEvals = evals;
        if (instViewResults && !userAdmin) {
            newEvals = new ArrayList<EvalEvaluation>();
            for (EvalEvaluation evaluation : evals) {
                // Add the owned evals
                if (currentUserId.equals(evaluation.getOwner())) {
                    newEvals.add(evaluation);
                } else {
                    // From the not-owned evals show those that are available
                    // for viewing results
                    String forcedViewableState = commonLogic.calculateViewability(evaluation.getState());
					if (EvalUtils.checkStateAfter(forcedViewableState, EvalConstants.EVALUATION_STATE_VIEWABLE, true)) {
                        newEvals.add(evaluation);
                    }
                }
            }
        }

        if (!newEvals.isEmpty()) {
            // sort evaluations by due date
            Collections.sort(evals, new Comparator<EvalEvaluation>() {
                public int compare(EvalEvaluation eval1, EvalEvaluation eval2) {
                    return (eval1.getDueDate().compareTo(eval2.getDueDate()));
                }                
            });
            
            UIBranchContainer evalAdminBC = UIBranchContainer.make(tofill, "evalAdminBoxContents:");
            // Temporary fix for http://www.caret.cam.ac.uk/jira/browse/CTL-583
            // (need to send them to the eval control page eventually) -AZ
            if (beginEvaluation) {
                UIInternalLink.make(evalAdminBC, "evaladmin-title-link", UIMessage.make("summary.evaluations.admin"), new SimpleViewParameters(
                        ControlEvaluationsProducer.VIEW_ID));
            } else {
                UIMessage.make(evalAdminBC, "evaladmin-title", "summary.evaluations.admin");
            }
            UIForm evalAdminForm = UIForm.make(evalAdminBC, "evalAdminForm");

    		// get the eval groups
    		Long[] evalIds = new Long[newEvals.size()];
    		int i = 0;
    		for(EvalEvaluation eval : newEvals) {
    			evalIds[i++] = eval.getId();
    		}
    		Map<Long, List<EvalGroup>> evalGroups = evaluationService.getEvalGroupsForEval(evalIds, false, null);
    		
            for (Iterator<EvalEvaluation> iter = newEvals.iterator(); iter.hasNext();) {
                EvalEvaluation eval = (EvalEvaluation) iter.next();

                String evalState = evaluationService.returnAndFixEvalState(eval, true);
                evalState = commonLogic.calculateViewability(evalState);

                // 1) if a evaluation is queued, title link go to EditSettings
                // page with populated data 
                // 2) if a evaluation is active, title link go to EditSettings 
                // page with populated data but start date should be disabled 
                // 3) if a evaluation is closed, title link go to previewEval 
                // page with populated data
				List<EvalGroup> groups = evalGroups.get(eval.getId());
				for(EvalGroup group : groups) {
	                UIBranchContainer evalrow = UIBranchContainer.make(evalAdminForm, "evalAdminList:", eval.getId().toString());
	                UIOutput.make(evalrow, "evalAdminStartDate", df.format(eval.getStartDate()));
	                humanDateRenderer.renderDate(evalrow, "evalAdminDueDate", eval.getDueDate());

					String title = EvalUtils.makeMaxLengthString(eval.getTitle() + " " + group.title, 50);
                    String[] groupIds = {group.evalGroupId};
                    int responsesCount = deliveryService.countResponses(eval.getId(), null, true);
                    int enrollmentsCount = evaluationService.countParticipantsForEval(eval.getId(), groupIds);
                    String responseString = EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount);
                    if (EvalUtils.checkStateAfter(evalState, EvalConstants.EVALUATION_STATE_CLOSED, true)) {
                        UIInternalLink.make(evalrow, "evalAdminTitleLink_preview", title, new EvalViewParameters(
                                PreviewEvalProducer.VIEW_ID, eval.getId(), eval.getTemplate().getId()));
                        UIInternalLink.make(evalrow, "evalAdminResponseLink", 
                                UIMessage.make("controlevaluations.eval.responses.inline", new Object[] { responseString }),
//                                new EvalViewParameters( EvaluationRespondersProducer.VIEW_ID, eval.getId() ) );
                        		new ReportParameters(ReportChooseGroupsProducer.VIEW_ID, eval.getId() ));
                    } else {
                        UIInternalLink.make(evalrow, "evalAdminTitleLink_edit", title, new EvalViewParameters(
                                EvaluationSettingsProducer.VIEW_ID, eval.getId()));
                        UIOutput.make(evalrow, "evalAdminResponse", responseString);
                    }
				}
            }
        }
	}
}

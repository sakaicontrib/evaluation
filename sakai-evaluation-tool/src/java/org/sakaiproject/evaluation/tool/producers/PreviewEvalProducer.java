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
package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.HierarchyNodeGroup;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.TemplateItemGroup;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * View for previewing a template or evaluation
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class PreviewEvalProducer extends EvalCommonProducer implements ViewParamsReporter {

    public static final String VIEW_ID = "preview_eval";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    private ItemRenderer itemRenderer;
    public void setItemRenderer(ItemRenderer itemRenderer) {
        this.itemRenderer = itemRenderer;
    }

    private MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator messageLocator) {
        this.messageLocator = messageLocator;
    }

    private ExternalHierarchyLogic hierarchyLogic;
    public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
        this.hierarchyLogic = logic;
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }

    private HttpServletResponse httpServletResponse;
    public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    int displayNumber = 1; //  determines the number to display next to each item

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // force the headers to expire this - http://jira.sakaiproject.org/jira/browse/EVALSYS-621
        RenderingUtils.setNoCacheHeaders(httpServletResponse);

        String currentUserId = commonLogic.getCurrentUserId();

        EvalViewParameters previewEvalViewParams = (EvalViewParameters)viewparams;
        if (previewEvalViewParams.evaluationId == null && 
                previewEvalViewParams.templateId == null) {
            throw new IllegalArgumentException("Must specify template id or evaluation id, both cannot be null");
        }

        Long evaluationId = previewEvalViewParams.evaluationId;
        Long templateId = previewEvalViewParams.templateId;
        String evalGroupId;
        EvalEvaluation eval;

        if (! previewEvalViewParams.external) {
            UIInternalLink.make(tofill, "summary-link", 
                    UIMessage.make("summary.page.title"), 
                    new SimpleViewParameters(SummaryProducer.VIEW_ID));
        }

        if (evaluationId == null) {
            // previewing a template
            UIMessage.make(tofill, "preview-title", "previeweval.template.title");
            // load up the template
            EvalTemplate template = authoringService.getTemplateById(templateId);
            // create a fake evaluation
            eval = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, currentUserId, 
                    messageLocator.getMessage("previeweval.evaluation.title.default"), 
                    new Date(), new Date(), new Date(), new Date(), EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.SHARING_VISIBLE, 1, template);
            eval.setInstructions(messageLocator.getMessage("previeweval.instructions.default"));
        } else {
            // previewing an evaluation
            UIMessage.make(tofill, "preview-title", "previeweval.evaluation.title");
            UIMessage.make(tofill, "preview-title-prefix", "previeweval.evaluation.title.prefix");
            // load the real evaluation and template
            eval = evaluationService.getEvaluationById(evaluationId);
            templateId = eval.getTemplate().getId();
            //template = authoringService.getTemplateById(templateId);
        }
        
        EvalAssignGroup group = null;
        String groupDisplayTitle = null;
        Boolean useGroupSpecificPreview = (Boolean) evalSettings.get(EvalSettings.ENABLE_GROUP_SPECIFIC_PREVIEW);
        if (useGroupSpecificPreview && evaluationId != null) {
        	int groupCount = this.evaluationService.countEvaluationGroups(evaluationId, true);
        	if (groupCount == 0) {
        		useGroupSpecificPreview = false;
        	} else if(groupCount == 1) {
        		Map<Long, List<EvalAssignGroup>> groupMap = this.evaluationService.getAssignGroupsForEvals(new Long[]{evaluationId}, false, false);
        		List<EvalAssignGroup> groups = groupMap.get(evaluationId);
        		if(groups == null || groups.isEmpty()){
        			useGroupSpecificPreview = false;
        		} else {
        			group = groups.get(0);
        			evalGroupId = group.getEvalGroupId();
        			if(evalGroupId == null) {
        				// the group is unreliable and the default group-title will be used
        				group = null;
        				useGroupSpecificPreview = false;
        			} else {
        				groupDisplayTitle = commonLogic.getDisplayTitle(evalGroupId);
        				if(groupDisplayTitle == null) {
            				// the group is unreliable, but the group-id can be used in place of a group title
            				group = null;
            				groupDisplayTitle = evalGroupId;
            				useGroupSpecificPreview = false;
        				}
        			}
        		}
        	} else {
        		// TODO: provide mechanism to select group to preview
        		useGroupSpecificPreview = false;
        	}
        }
    	
        UIMessage.make(tofill, "eval-title-header", "takeeval.eval.title.header");
        UIOutput.make(tofill, "evalTitle", eval.getTitle());

        UIBranchContainer groupTitle = UIBranchContainer.make(tofill, "show-group-title:");
        UIMessage.make(groupTitle, "group-title-header", "takeeval.group.title.header");
        if (groupDisplayTitle == null) {
            UIMessage.make(groupTitle, "group-title", "previeweval.course.title.default");
            group = null;
        } else {
            UIOutput.make(groupTitle, "group-title", groupDisplayTitle);
        }
        
        // show instructions if not null
        if (eval.getInstructions() != null) {
            UIBranchContainer instructions = UIBranchContainer.make(tofill, "show-eval-instructions:");
            UIMessage.make(instructions, "eval-instructions-header", "takeeval.instructions.header");	
            UIVerbatim.make(instructions, "eval-instructions", eval.getInstructions());
        }

        TemplateItemDataList tidl = null;
        if (group == null) {
            // get all items for this template
            List<EvalTemplateItem> allItems = 
            	authoringService.getTemplateItemsForTemplate(templateId, new String[] {}, new String[] {}, new String[] {});
            if (allItems.isEmpty()) {
            	// do nothing
            } else {
                // Get the sorted list of all nodes for this set of template items
                List<EvalHierarchyNode> hierarchyNodes = TemplateItemDataList.makeEvalNodesList(allItems, hierarchyLogic);

                // make up 2 fake instructors for this evaluation (to show the instructor added items)
                List<String> instructors = new ArrayList<>();
                instructors.add("fake1");
                instructors.add("fake2");

                // make the TI data structure
                Map<String, List<String>> associates = new HashMap<>();
                associates.put(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, instructors);
                
                // add in the TA list if it is enabled
                Boolean taEnabled = (Boolean) evalSettings.get(EvalSettings.ENABLE_ASSISTANT_CATEGORY);
                if (taEnabled.booleanValue()) {
                    List<String> teachingAssistants = new ArrayList<>();
                    teachingAssistants.add("fake1");
                    teachingAssistants.add("fake2");
                    associates.put(EvalConstants.ITEM_CATEGORY_ASSISTANT, teachingAssistants);
                }

                tidl = new TemplateItemDataList(allItems, hierarchyNodes, associates, null);
            }
        } else {
        	tidl = new TemplateItemDataList(evaluationId, group.getEvalGroupId(),
                    evaluationService, authoringService, hierarchyLogic, null);
        }

        if (tidl == null) {
            // this is an empty template so just display some ugly message
            UIMessage.make(tofill, "noItemsToShow", "no.list.items");   
        } else {

            int countAssistants = 0;
            int countInstructors = 0;
            // loop through the TIGs and handle each associated category
            for (TemplateItemGroup tig : tidl.getTemplateItemGroups()) {
                UIBranchContainer categorySectionBranch = UIBranchContainer.make(tofill, "categorySection:");
                // handle printing the category header
                if (EvalConstants.ITEM_CATEGORY_COURSE.equals(tig.associateType) && !((Boolean)evalSettings.get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY))) {
                    UIMessage.make(categorySectionBranch, "categoryHeader", "takeeval.group.questions.header");
                } else {
                	String evaluateeName = null;
                	String messageKey = null;
                	if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(tig.associateType)) {
                		if(group == null) {
                			evaluateeName = tig.associateId.equals("fake2") ? messageLocator.getMessage("previeweval.instructor.2") : messageLocator.getMessage("previeweval.instructor.1");
                		} else {
                			EvalUser user = commonLogic.getEvalUserById(tig.associateId);
                			evaluateeName = user.displayName;
                		}
                		messageKey = "takeeval.instructor.questions.header";
	                    countInstructors ++;
	                } else if (EvalConstants.ITEM_CATEGORY_ASSISTANT.equals(tig.associateType)) {
	                	if(group == null) {
                			evaluateeName = tig.associateId.equals("fake2") ? messageLocator.getMessage("previeweval.ta.2") : messageLocator.getMessage("previeweval.ta.1");
	                	} else {
                			EvalUser user = commonLogic.getEvalUserById(tig.associateId);
                			evaluateeName = user.displayName;
	                	}
	                    messageKey = "takeeval.assistant.questions.header";
	                    countAssistants ++;
	                }
                    UIMessage.make(categorySectionBranch, "categoryHeader", 
                            messageKey, new Object[] { evaluateeName });
                }

                // loop through the hierarchy node groups
                for (HierarchyNodeGroup hng : tig.hierarchyNodeGroups) {
                    // render a node title
                    if (hng.node != null) {
                        // Showing the section title is system configurable via the administrate view
                        Boolean showHierSectionTitle = (Boolean) evalSettings.get(EvalSettings.DISPLAY_HIERARCHY_HEADERS);
                        if (showHierSectionTitle) {
                            UIBranchContainer nodeTitleBranch = UIBranchContainer.make(categorySectionBranch, "itemrow:nodeSection");
                            UIOutput.make(nodeTitleBranch, "nodeTitle", hng.node.title);
                        }
                    }

                    List<DataTemplateItem> dtis = hng.getDataTemplateItems(false);
                    for (int i = 0; i < dtis.size(); i++) {
                        DataTemplateItem dti = dtis.get(i);
                        UIBranchContainer nodeItemsBranch = UIBranchContainer.make(categorySectionBranch, "itemrow:templateItem");
                        if (i % 2 != 0) {
                            nodeItemsBranch.decorate( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
                        }
                        renderItemPrep(nodeItemsBranch, dti, eval);
                    }
                }
            }

            //explain groups if using any groups in this template
            if ( countAssistants > 0 || countInstructors > 0 ){
            	UIOutput.make(tofill, "show-eval-instructions-groups");
            }

            if (! previewEvalViewParams.external) {
                // only show back button when navigating inside the tool
                UIOutput.make(tofill, "show-back-button");
            }
        }

    }

    /**
     * Prepare to render an item, this handles blocks correctly
     * 
     * @param parent the parent container
     * @param dti the wrapped template item we will render
     */
    private void renderItemPrep(UIBranchContainer parent, DataTemplateItem dti, EvalEvaluation eval) {
        int displayIncrement = 0; // stores the increment in the display number
        EvalTemplateItem templateItem = dti.templateItem;
        if (! TemplateItemUtils.isAnswerable(templateItem)) {
            // nothing to bind for unanswerable items unless it is a block parent
            if ( dti.blockChildItems != null ) {
                // Handle the BLOCK PARENT special case - block item being rendered
                displayIncrement = dti.blockChildItems.size();
            }
        } else {
            // non-block and answerable items
            displayIncrement++;
        }

        // render the item
        itemRenderer.renderItem(parent, "renderedItem:", null, templateItem, displayNumber, true, 
                RenderingUtils.makeRenderProps(dti, eval, null, null) );

        // increment the display number
        displayNumber += displayIncrement;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new EvalViewParameters();
    }

}

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.utils.ArrayUtils;

import uk.org.ponder.arrayutil.MapUtil;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIDisabledDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;

/**
 * This handles the rendering of scaled type items
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class BlockRenderer implements ItemRenderer {

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    /**
     * This identifies the template component associated with this renderer
     */
    public static final String COMPONENT_ID = "render-block-item:";


    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#renderItem(uk.org.ponder.rsf.components.UIContainer, java.lang.String, org.sakaiproject.evaluation.model.EvalTemplateItem, int, boolean)
     */
    @SuppressWarnings("unchecked")
    public UIJointContainer renderItem(UIContainer parent, String ID, String[] bindings, EvalTemplateItem templateItem, int displayNumber, boolean disabled, Map<String, Object> renderProperties) {

        // check to make sure we are only dealing with block parents
        if (templateItem.getBlockParent() == null) {
            throw new IllegalArgumentException("Block renderer can only work for block items, this templateItem ("+templateItem.getId()+") has a null block parent"); 
        } else if (! templateItem.getBlockParent() ||
                templateItem.getBlockId() != null) {
            throw new IllegalArgumentException("Block renderer can only work for block parents, this templateItem ("+templateItem.getId()+") is a block child");
        }

        List<EvalTemplateItem> childList = templateItem.childTemplateItems;
        if (childList == null || childList.isEmpty()) {
            // get the list of children the slow way if we have to
            childList = authoringService.getBlockChildTemplateItemsForBlockParent(templateItem.getId(), false);
        }
        // check that the child count matches the bindings count
        if ( ! disabled && (childList.size() != bindings.length) ) {
            throw new IllegalArgumentException("The bindings array ("+bindings.length+") must match the size of the block child count ("+childList.size()+")");
        }

        UIJointContainer container = new UIJointContainer(parent, ID, COMPONENT_ID);

        if (displayNumber <= 0) displayNumber = 0;
        String initValue = null;
        if (bindings[0] == null) initValue = "";

        EvalScale scale = templateItem.getItem().getScale();
        String[] scaleOptions = scale.getOptions();
        int optionCount = scaleOptions.length;

        // handle NA
        boolean usesNA = templateItem.getUsesNA();

        String scaleValues[] = new String[optionCount];
        String scaleLabels[] = new String[optionCount];

        String scaleDisplaySetting = templateItem.getScaleDisplaySetting();

        
        ///////////////
        // matrix block
        ///////////////
        if (templateItem.getScaleDisplaySetting().equals(EvalConstants.ITEM_SCALE_DISPLAY_MATRIX) ||
        		templateItem.getScaleDisplaySetting().equals(EvalConstants.ITEM_SCALE_DISPLAY_MATRIX_COLORED)) {
            
            for (int count = 1; count <= optionCount; count++) {
                scaleValues[optionCount - count] = Integer.toString(optionCount - count);
                scaleLabels[optionCount - count] = scaleOptions[count-1];
            }
            
            if (usesNA) {
                scaleValues = ArrayUtils.appendArray(scaleValues, EvalConstants.NA_VALUE.toString());
                scaleLabels = ArrayUtils.appendArray(scaleLabels, "");
            }
            
            UIBranchContainer matrixGroup = UIBranchContainer.make(container, "matrixGroupDisplay:");
            
            if (usesNA) {
            	matrixGroup.decorate( new UIStyleDecorator("use-na") );
            	UIMessage.make(matrixGroup,"response-scale-label-na", "viewitem.na.desc");
            }
            
            // display header labels
            List<String> headerLabels = RenderingUtils.getMatrixLabels(scaleOptions);
            UIOutput.make(matrixGroup, "label-start", headerLabels.get(0));
            UIOutput.make(matrixGroup, "label-end", headerLabels.get(1));
            if (headerLabels.size() == 3) {
            	UIOutput.make(matrixGroup, "label-middle", headerLabels.get(2));
            }            
            
           	UIOutput.make(matrixGroup,"label-na", "NA");
        	UIVerbatim.make(matrixGroup, "matrixGroupTitle", templateItem.getItem().getItemText());
        	
        	// display number labels
        	for (int i = 0; i < optionCount; i++) {
        	    UIOutput.make(matrixGroup, "response-scale-label:", (i + 1) + "");
        	}
            
        	// iterate through each question in the block
            for (int j = 0; j < childList.size(); j++) {
                
            	// build the question row container and apply decorations
	            UIBranchContainer matrix = UIBranchContainer.make(matrixGroup, "matrixDisplay:", j+"");
	            if (usesNA) {
	            	matrix.decorate( new UIStyleDecorator("use-na") );
	            }
	            
                // get the child item
                EvalTemplateItem childTemplateItem = (EvalTemplateItem) childList.get(j);
                EvalItem childItem = childTemplateItem.getItem();
                
	            Map<String, Object> childRenderProperties = (Map<String, Object>) renderProperties.get("child-" + childTemplateItem.getId());
	            if (childRenderProperties.containsKey(ItemRenderer.EVAL_PROP_RENDER_INVALID)) {
	                matrix.decorate( new UIStyleDecorator("validFail") ); // must match the existing CSS class
	            } else if ( childRenderProperties.containsKey(ItemRenderer.EVAL_PROP_ANSWER_REQUIRED) ) {
	                matrix.decorate( new UIStyleDecorator("compulsory") ); // must match the existing CSS class
	            }	            
	            
	            // display question text
	            UIOutput.make(matrix, "itemNum", Integer.toString(displayNumber + j) ); //$NON-NLS-2$
	            UIVerbatim.make(matrix, "itemText", childItem.getItemText());
            
	            UIBranchContainer rowBranch = UIBranchContainer.make(matrix, "response-list:");
	            	
                // Bind the answers to a list of answers in evaluation bean (if enabled)
                String childBinding = null;
                if (! disabled && bindings != null) {
                    childBinding = bindings[j];
                }
                UISelect childRadios = UISelect.make(rowBranch, "childRadio", scaleValues, scaleLabels, childBinding, initValue);
                String selectID = childRadios.getFullID();
	            
	            if (disabled) {
	                childRadios.selection.willinput = false;
	                childRadios.selection.fossilize = false;
	            }
	
	            int scaleLength = scaleValues.length;
	            int limit = usesNA ? scaleLength - 1: scaleLength;  // skip the NA value at the end
	            
	            for (int k = 0; k < limit; ++k) {
                    UIBranchContainer radioBranchSecond = UIBranchContainer.make(rowBranch, "scaleOption:", k+"");
                    UISelectChoice.make(radioBranchSecond, "radioValue", selectID, k);
                    // scaleLabels are in reverse order, indexed from (end - 1) to 0.  If usesNA, 
                    // an empty label is appended; ignore that one too 
                    int labelIndex = scaleLabels.length - k - (usesNA ? 2 : 1);
                    UIVerbatim.make(radioBranchSecond,  "radioValueLabel", scaleLabels[labelIndex]);
	            }
	            
	            // display the N/A radio button always; use CSS to hide if not needed (via the "use-na" class (above)
	            UIBranchContainer labelContainer = UIBranchContainer.make(rowBranch,  "na-input-label:");
                UISelectChoice naChoice = UISelectChoice.make(labelContainer, "na-input", selectID, scaleLength - 1);
                if (!usesNA) {
                	naChoice.decorate( new UIDisabledDecorator());
                }
                UIMessage.make(rowBranch, "radioValueLabelNa", "viewitem.na.desc");
            }
        	
        ////////////////
        // stepped block
        ////////////////
        } else {
        	
            UIBranchContainer blockStepped = UIBranchContainer.make(container, "blockStepped:");
            blockStepped.decorate( new UIStyleDecorator("options-"+optionCount) );

            // setup simple variables to make code more clear
            boolean colored = EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED.equals(scaleDisplaySetting) || 
            		EvalConstants.ITEM_SCALE_DISPLAY_MATRIX_COLORED.equals(scaleDisplaySetting);

            for (int count = 1; count <= optionCount; count++) {
                scaleValues[optionCount - count] = Integer.toString(optionCount - count);
                scaleLabels[optionCount - count] = scaleOptions[count-1];
            }

            // handle ideal coloring
            String idealImage = "";
            if (colored) {
                String ideal = scale.getIdeal();
                // Get the scale ideal value (none, low, mid, high )
                if (ideal == null || "*NULL*".equals(ideal)) {
                    // When no ideal is specified then just plain blue for both start and end
                    idealImage = EvalToolConstants.COLORED_IMAGE_URLS[0];
                } else if (EvalConstants.SCALE_IDEAL_LOW.equals(ideal)) {
                    idealImage = EvalToolConstants.COLORED_IMAGE_URLS[1];
                } else if (EvalConstants.SCALE_IDEAL_MID.equals(ideal)) {
                    idealImage = EvalToolConstants.COLORED_IMAGE_URLS[2];
                } else if (EvalConstants.SCALE_IDEAL_HIGH.equals(ideal)) {
                    idealImage = EvalToolConstants.COLORED_IMAGE_URLS[3];
                } else if (EvalConstants.SCALE_IDEAL_OUTSIDE.equals(ideal)) {
                    idealImage = EvalToolConstants.COLORED_IMAGE_URLS[4];
                } else {
                    // use no decorators
                }
            }

            // Radio Buttons
            UISelect radioLabel = UISelect.make(blockStepped, "radioLabel", scaleValues, scaleLabels, null, false);
            String selectIDLabel = radioLabel.getFullID();

            if (usesNA) {
                scaleValues = ArrayUtils.appendArray(scaleValues, EvalConstants.NA_VALUE.toString());
                scaleLabels = ArrayUtils.appendArray(scaleLabels, "");
                UIMessage.make(blockStepped, "na-desc", "viewitem.na.desc");
            }

            // render the stepped labels and images
            int scaleLength = scaleValues.length;
            int limit = usesNA ? scaleLength - 1: scaleLength;  // skip the NA value at the end
            
	            for (int j = 0; j < limit; ++j) {
	                UIBranchContainer rowBranch = UIBranchContainer.make(blockStepped, "blockRowBranch:", j+"");

	                // put in the block header text (only once)
	                if (j == 0) {
	                    UIVerbatim headerText = UIVerbatim.make(rowBranch, "itemText", templateItem.getItem().getItemText());
	                    headerText.decorators =
	                        new DecoratorList(new UIFreeAttributeDecorator( MapUtil.make("rowspan", (optionCount + 1) + "") ));
	                    // add render markers if they are set for this block parent
	                    if ( renderProperties.containsKey(ItemRenderer.EVAL_PROP_RENDER_INVALID) ) {
	                        rowBranch.decorate( new UIStyleDecorator("validFail") ); // must match the existing CSS class
	                    } else if ( renderProperties.containsKey(ItemRenderer.EVAL_PROP_ANSWER_REQUIRED) ) {
	                        rowBranch.decorate( new UIStyleDecorator("compulsory") ); // must match the existing CSS class
	                    }
	                }
	
	                // Actual label
	                UISelectLabel.make(rowBranch, "topLabel", selectIDLabel, j);
	
	                // Corner Image
	                UILink.make(rowBranch, "cornerImage", EvalToolConstants.STEPPED_IMAGE_URLS[0]);
	
	                // This branch container is created to help in creating the middle images after the LABEL
	                for (int k = 0; k < j; ++k) {
	                    UIBranchContainer afterTopLabelBranch = UIBranchContainer.make(rowBranch, "blockAfterTopLabelBranch:", k+"");
	                    UILink.make(afterTopLabelBranch, "middleImage", EvalToolConstants.STEPPED_IMAGE_URLS[1]);	
	                }
	
	                // the down arrow images
	                UIBranchContainer bottomLabelBranch = UIBranchContainer.make(blockStepped, "blockBottomLabelBranch:", j+"");
	                UILink.make(bottomLabelBranch, "bottomImage", EvalToolConstants.STEPPED_IMAGE_URLS[2]);
            }

            // the child items rendering loop
            for (int j = 0; j < childList.size(); j++) {

                // get the child item
                EvalTemplateItem childTemplateItem = (EvalTemplateItem) childList.get(j);
                EvalItem childItem = childTemplateItem.getItem();

                // get mapping props for the child
                Map<String, Object> childRenderProps = (Map<String, Object>) renderProperties.get("child-"+childTemplateItem.getId());
                if (childRenderProps == null) {
                    childRenderProps = new HashMap<>(0);
                }

                // For the radio buttons
                UIBranchContainer childRow = UIBranchContainer.make(blockStepped, "childRow:", j+"" );
                if ( childRenderProps.containsKey(ItemRenderer.EVAL_PROP_RENDER_INVALID) ) {
                    childRow.decorate( new UIStyleDecorator("validFail") ); // must match the existing CSS class
                } else if ( childRenderProps.containsKey(ItemRenderer.EVAL_PROP_ANSWER_REQUIRED) ) {
                    childRow.decorate( new UIStyleDecorator("compulsory") ); // must match the existing CSS class
                }
                if (colored) {
                    UILink.make(childRow, "idealImage", idealImage);
                }

                // put in the item information (number and text)
                UIOutput.make(childRow, "childNum", Integer.toString(displayNumber + j) );
                UIVerbatim.make(childRow, "childText", childItem.getItemText());

                // Bind the answers to a list of answers in evaluation bean (if enabled)
                String childBinding = null;
                if (! disabled && bindings != null) {
                    childBinding = bindings[j];
                }
                UISelect childRadios = UISelect.make(childRow, "childRadio", scaleValues, scaleLabels, childBinding, initValue);
                String selectID = childRadios.getFullID();

                if (disabled) {
                    childRadios.selection.willinput = false;
                    childRadios.selection.fossilize = false;
                }

                if (usesNA) {
                	UIBranchContainer na = UIBranchContainer.make(childRow, "na-parent:");
                    UISelectChoice.make(childRow, "na-input", selectID, scaleLength - 1);
                }

                // render child radio choices
                for (int k = 0; k < limit; ++k) {
                    if (colored) {
                        UIBranchContainer radioBranchFirst = UIBranchContainer.make(childRow, "scaleOptionColored:", k+"");
                        UISelectChoice.make(radioBranchFirst, "radioValueColored", selectID, k);
                        // this is confusing but this is now the one underneath
                        UIBranchContainer radioBranchSecond = UIBranchContainer.make(childRow, "scaleOption:", k+"");
                        UIOutput.make(radioBranchSecond, "radioValue");
                    } else {
                        UIBranchContainer radioBranchSecond = UIBranchContainer.make(childRow, "scaleOption:", k+"");
                        UISelectChoice.make(radioBranchSecond, "radioValue", selectID, k);
                    }
                }

            }
        }

        return container;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#getRenderType()
     */
    public String getRenderType() {
        return EvalConstants.ITEM_TYPE_BLOCK_PARENT;
    }

}

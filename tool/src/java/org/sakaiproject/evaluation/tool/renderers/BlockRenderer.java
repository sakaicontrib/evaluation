/**
 * BlockRenderer.java - evaluation - Oct 29, 2007 2:59:12 PM - azeckoski
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.renderers;

import static org.sakaiproject.evaluation.utils.TemplateItemUtils.*;

import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
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
    public UIJointContainer renderItem(UIContainer parent, String ID, String[] bindings, EvalTemplateItem templateItem, int displayNumber, boolean disabled, Map<String, String> evalProperties) {

        // check to make sure we are only dealing with block parents
        if (templateItem.getBlockParent() == null) {
            throw new IllegalArgumentException("Block renderer can only work for block items, this templateItem ("+templateItem.getId()+") has a null block parent"); 
        } else if (! templateItem.getBlockParent().booleanValue() ||
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
        boolean usesNA = templateItem.getUsesNA().booleanValue();

        String scaleValues[] = new String[optionCount];
        String scaleLabels[] = new String[optionCount];

        String scaleDisplaySetting = templateItem.getScaleDisplaySetting();

        if (templateItem.getScaleDisplaySetting().equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED) ||
                templateItem.getScaleDisplaySetting().equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED) ) {

            UIBranchContainer blockStepped = UIBranchContainer.make(container, "blockStepped:");

            // setup simple variables to make code more clear
            boolean colored = EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED.equals(scaleDisplaySetting);

            for (int count = 1; count <= optionCount; count++) {
                scaleValues[optionCount - count] = Integer.toString(optionCount - count);
                scaleLabels[optionCount - count] = scaleOptions[count-1];
            }

            // handle ideal coloring
            String idealImage = "";
            if (colored) {
                String ideal = scale.getIdeal();
                // Get the scale ideal value (none, low, mid, high )
                if (ideal == null) {
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

            boolean evalAnswerReqired = evalProperties.containsKey(ItemRenderer.EVAL_PROP_ANSWER_REQUIRED) ? Boolean.valueOf(evalProperties.get(ItemRenderer.EVAL_PROP_ANSWER_REQUIRED)) : false;

            // the child items rendering loop
            for (int j = 0; j < childList.size(); j++) {

                // get the child item
                EvalTemplateItem childTemplateItem = (EvalTemplateItem) childList.get(j);
                EvalItem childItem = childTemplateItem.getItem();

                if(childTemplateItem.isCompulsory() == null) childTemplateItem.setCompulsory(false);

                // For the radio buttons
                UIBranchContainer childRow = UIBranchContainer.make(blockStepped, "childRow:", j+"" );
                if (evalProperties.containsKey(ItemRenderer.EVAL_PROP_RENDER_INVALID)) {
                    childRow.decorate( new UIStyleDecorator("validFail") ); // must match the existing CSS class
                } else if (safeBool(childTemplateItem.isCompulsory())  && ! evalAnswerReqired) {
                    childRow.decorate( new UIStyleDecorator("compulsory") ); // must match the existing CSS class
                }
                if (colored) {
                    UILink.make(childRow, "idealImage", idealImage);
                }

                // put in the item information (number and text)
                UIOutput.make(childRow, "childNum", new Integer(displayNumber + j).toString() );
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

/******************************************************************************
 * BlockRenderer.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.renderers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;

/**
 * This handles the rendering of scaled type items
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class BlockRenderer implements ItemRenderer {

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	private EvalItemsLogic itemsLogic;
	public void setItemsLogic( EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

	/**
	 * This identifies the template component associated with this renderer
	 */
	public static final String COMPONENT_ID = "render-block-item:";


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#renderItem(uk.org.ponder.rsf.components.UIContainer, java.lang.String, org.sakaiproject.evaluation.model.EvalTemplateItem, int, boolean)
	 */
	public UIJointContainer renderItem(UIContainer parent, String ID, String[] bindings, EvalTemplateItem templateItem, int displayNumber, boolean disabled) {

		// check to make sure we are only dealing with block parents
		if (templateItem.getBlockParent() == null) {
			throw new IllegalArgumentException("Block renderer can only work for block items, this templateItem ("+templateItem.getId()+") has a null block parent"); 
		} else if (! templateItem.getBlockParent().booleanValue() ||
				templateItem.getBlockId() != null) {
			throw new IllegalArgumentException("Block renderer can only work for block parents, this templateItem ("+templateItem.getId()+") is a block child");
		}

		// check that the child count matches the bindings count
		List childList = itemsLogic.getBlockChildTemplateItemsForBlockParent(templateItem.getId(), false);
		if ( !disabled && (childList.size() != bindings.length) ) {
			throw new IllegalArgumentException("The bindings array ("+bindings.length+") must match the size of the block child count ("+childList.size()+")");
		}

		UIJointContainer container = new UIJointContainer(parent, ID, COMPONENT_ID);

		if (displayNumber <= 0) displayNumber = 0;
		String initValue = null;
		if (bindings[0] == null) initValue = "";

		EvalScale scale = templateItem.getItem().getScale();
		String[] scaleOptions = scale.getOptions();
		int optionCount = scaleOptions.length;
		String scaleValues[] = new String[optionCount];
		String scaleLabels[] = new String[optionCount];

		String scaleDisplaySetting = templateItem.getScaleDisplaySetting();

		if (templateItem.getScaleDisplaySetting().equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED) ||
				templateItem.getScaleDisplaySetting().equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED) ) {

			UIBranchContainer blockStepped = UIBranchContainer.make(container, "blockStepped:"); //$NON-NLS-1$

			// setup simple variables to make code more clear
			boolean colored = EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED.equals(scaleDisplaySetting);

			handleNA(blockStepped, templateItem.getUsesNA().booleanValue());

			for (int count = 1; count <= optionCount; count++) {
				scaleValues[optionCount - count] = Integer.toString(optionCount - count);
				scaleLabels[optionCount - count] = scaleOptions[count-1];
			}
//System.out.println("BLOCK-RENDER: scaleValues: " + ArrayUtil.toString(scaleValues));
//System.out.println("BLOCK-RENDER: scaleLabels: " + ArrayUtil.toString(scaleLabels));

			// handle ideal coloring
			String idealImage = ""; //$NON-NLS-1$
			if (colored) {
				String ideal = scale.getIdeal();
				// Get the scale ideal value (none, low, mid, high )
				if (ideal == null) {
					// When no ideal is specified then just plain blue for both start and end
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[0];
				} else if (EvalConstants.SCALE_IDEAL_LOW.equals(ideal)) {
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[1];
				} else if (EvalConstants.SCALE_IDEAL_MID.equals(ideal)) {
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[2];
				} else if (EvalConstants.SCALE_IDEAL_HIGH.equals(ideal)) {
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[3];
				} else if (EvalConstants.SCALE_IDEAL_OUTSIDE.equals(ideal)) {
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[4];
				} else {
					// use no decorators
				}
			}

			// Radio Buttons
			UISelect radioLabel = UISelect.make(blockStepped, 
					"radioLabel", scaleValues, scaleLabels, null, false); //$NON-NLS-1$
			String selectIDLabel = radioLabel.getFullID();

			// the scale rendering loop
			for (int j = 0; j < scaleValues.length; ++j) {
				UIBranchContainer rowBranch = UIBranchContainer.make(
						blockStepped, "blockRowBranch:", Integer.toString(j)); //$NON-NLS-1$

				// put in the block header text (only once)
				if (j == 0) {
					UIVerbatim headerText = UIVerbatim.make(rowBranch, "itemText", templateItem.getItem().getItemText()); //$NON-NLS-1$
					Map map = new HashMap();
					map.put("rowspan", (optionCount + 1) + "");
					headerText.decorators =
						new DecoratorList(new UIFreeAttributeDecorator(map));
				}

				// Actual label
				UISelectLabel.make(rowBranch, "topLabel", selectIDLabel, j); //$NON-NLS-1$

				// Corner Image
				UILink.make(rowBranch, "cornerImage", EvaluationConstant.STEPPED_IMAGE_URLS[0]);

				// This branch container is created to help in creating the middle images after the LABEL
				for (int k = 0; k < j; ++k) {
					UIBranchContainer afterTopLabelBranch = UIBranchContainer.make(rowBranch, "blockAfterTopLabelBranch:", Integer.toString(k)); //$NON-NLS-1$
					UILink.make(afterTopLabelBranch, "middleImage", EvaluationConstant.STEPPED_IMAGE_URLS[1]); //$NON-NLS-1$	
				}

				// the down arrow images
				UIBranchContainer bottomLabelBranch = UIBranchContainer.make(blockStepped, "blockBottomLabelBranch:", Integer.toString(j)); //$NON-NLS-1$
				UILink.make(bottomLabelBranch, "bottomImage", EvaluationConstant.STEPPED_IMAGE_URLS[2]); //$NON-NLS-1$
			}
		
			// the child items rendering loop
			for (int j = 0; j < childList.size(); j++) {

				// get the child item
				EvalTemplateItem childTemplateItem = (EvalTemplateItem) childList.get(j);
				EvalItem childItem = childTemplateItem.getItem();

				// For the radio buttons
				UIBranchContainer childRow = UIBranchContainer.make(blockStepped, "childRow:", new Integer(j).toString() ); //$NON-NLS-1$
				if (colored) {
					UILink.make(childRow, "idealImage", idealImage); //$NON-NLS-1$
				}

				// put in the item information (number and text)
				UIOutput.make(childRow, "childNum", new Integer(displayNumber + j).toString() ); //$NON-NLS-1$
				UIOutput.make(childRow, "childText", childItem.getItemText()); //$NON-NLS-1$

				// Bind the answers to a list of answers in evaluation bean (if enabled)
				String childBinding = null;
				if (!disabled && bindings != null) {
					childBinding = bindings[j];
				}
				UISelect childRadios = UISelect.make(childRow, "dummyRadio",
						scaleValues, scaleLabels, childBinding, initValue);
				String selectID = childRadios.getFullID();
				
				if (disabled) {
					childRadios.selection.willinput = false;
					childRadios.selection.fossilize = false;
				}

				for (int k = 0; k < scaleValues.length; ++k) {
					if (colored) {
						UIBranchContainer radioBranchFirst = 
							UIBranchContainer.make(childRow, "scaleOptionsFake:", Integer.toString(k)); //$NON-NLS-1$
						UISelectChoice.make(radioBranchFirst,
								"dummyRadioValueFake", selectID, k); //$NON-NLS-1$
					}

					UIBranchContainer radioBranchSecond = 
						UIBranchContainer.make(childRow, "scaleOptionsReal:", Integer.toString(k)); //$NON-NLS-1$
					UISelectChoice.make(radioBranchSecond,
							"dummyRadioValueReal", selectID, k); //$NON-NLS-1$
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


	private void handleNA(UIContainer container, boolean useNA) {
		if (useNA) {
			UIBranchContainer radiobranch3 = UIBranchContainer.make(container, "showNA:"); //$NON-NLS-1$
			UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
			UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

}

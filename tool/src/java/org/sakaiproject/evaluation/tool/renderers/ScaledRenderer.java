/******************************************************************************
 * ScaledRenderer.java - created by aaronz@vt.edu
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

import java.awt.Color;

import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.utils.ScaledUtils;

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
import uk.org.ponder.rsf.components.decorators.UIColourDecorator;

/**
 * This handles the rendering of scaled type items
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ScaledRenderer implements ItemRenderer {

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	/**
	 * This identifies the template component associated with this renderer
	 */
	public static final String COMPONENT_ID = "render-scaled-item:";

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#renderItem(uk.org.ponder.rsf.components.UIContainer, java.lang.String, org.sakaiproject.evaluation.model.EvalTemplateItem, int, boolean)
	 */
	public UIJointContainer renderItem(UIContainer parent, String ID, String[] bindings, EvalTemplateItem templateItem, int displayNumber, boolean disabled) {
		UIJointContainer container = new UIJointContainer(parent, ID, COMPONENT_ID);

		if (displayNumber <= 0) displayNumber = 0;
		String initValue = null;
		if (bindings == null) initValue = "";

		EvalScale scale = templateItem.getItem().getScale();
		String[] scaleOptions = scale.getOptions();
		int optionCount = scaleOptions.length;
		String scaleValues[] = new String[optionCount];
		String scaleLabels[] = new String[optionCount];

		String scaleDisplaySetting = templateItem.getScaleDisplaySetting();

		if (EvalConstants.ITEM_SCALE_DISPLAY_COMPACT.equals(scaleDisplaySetting) ||
				EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED.equals(scaleDisplaySetting)) {

			UIBranchContainer compact = UIBranchContainer.make(container, "compactDisplay:"); //$NON-NLS-1$

			// setup simple variables to make code more clear
			boolean colored = EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED.equals(scaleDisplaySetting);

			String compactDisplayStart = scaleOptions[0];		
			String compactDisplayEnd = scaleOptions[optionCount - 1];

			for (int count = 0; count < optionCount; count++) {
				scaleValues[count] = new Integer(count).toString();
				scaleLabels[count] = " ";
			}

			UIOutput.make(compact, "itemNum", displayNumber+"" ); //$NON-NLS-1$ //$NON-NLS-2$
			UIVerbatim.make(compact, "itemText", templateItem.getItem().getItemText()); //$NON-NLS-1$

			// Compact start and end label containers
			UIBranchContainer compactStartContainer = UIBranchContainer.make(compact, "compactStartContainer:"); //$NON-NLS-1$
			UIBranchContainer compactEndContainer = UIBranchContainer.make(compact, "compactEndContainer:"); //$NON-NLS-1$

			// Compact start and end labels
			UIOutput.make(compactStartContainer, "compactDisplayStart", compactDisplayStart);
			UIOutput.make(compactEndContainer, "compactDisplayEnd", compactDisplayEnd);

			if (colored) {
				compactStartContainer.decorators = 
					new DecoratorList(new UIColourDecorator(null, ScaledUtils.getStartColor(scale)));
				compactEndContainer.decorators = 
					new DecoratorList(new UIColourDecorator(null, ScaledUtils.getEndColor(scale)));
			}

			// For the radio buttons
			UIBranchContainer compactRadioContainer = UIBranchContainer.make(compact, "compactRadioContainer:"); //$NON-NLS-1$
			if (colored) {
				UILink.make(compactRadioContainer, "idealImage", ScaledUtils.getIdealImageURL(scale)); //$NON-NLS-1$
			}

			UISelect radios = UISelect.make(compactRadioContainer, 
					"dummyRadio", scaleValues, scaleLabels, bindings[0], initValue);
			String selectID = radios.getFullID();
			
			if (disabled) {
				radios.selection.willinput = false;
				radios.selection.fossilize = false;
			}

			for (int j = 0; j < scaleValues.length; ++j) {
				if (colored) {
					UIBranchContainer radioBranchFirst = UIBranchContainer.make(compactRadioContainer, 
							"scaleOptionsFake:", new Integer(j).toString()); //$NON-NLS-1$
					UISelectChoice.make(radioBranchFirst,
							"dummyRadioValueFake", selectID, j); //$NON-NLS-1$
				}

				UIBranchContainer radioBranchSecond = UIBranchContainer.make(compactRadioContainer, 
						"scaleOptionsReal:", new Integer(j).toString()); //$NON-NLS-1$
				UISelectChoice.make(radioBranchSecond,
						"dummyRadioValueReal", selectID, j); //$NON-NLS-1$
			}

			handleNA(compact, templateItem.getUsesNA().booleanValue());

		} else if (EvalConstants.ITEM_SCALE_DISPLAY_FULL.equals(scaleDisplaySetting) || 
				EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED.equals(scaleDisplaySetting) ||
				EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL.equals(scaleDisplaySetting)) {

			UIBranchContainer fullFirst = UIBranchContainer.make(container, "fullType:"); //$NON-NLS-1$

			for (int count = 0; count < optionCount; count++) {
				scaleValues[count] = new Integer(count).toString();
				scaleLabels[count] = scaleOptions[count];	
			}

			UIOutput.make(fullFirst, "itemNum", displayNumber+"" ); //$NON-NLS-1$ //$NON-NLS-2$
			UIVerbatim.make(fullFirst, "itemText", templateItem.getItem().getItemText()); //$NON-NLS-1$

			handleNA(fullFirst, templateItem.getUsesNA().booleanValue());

			// display next row
			UIBranchContainer radiobranchFullRow = UIBranchContainer.make(container, "nextrow:", displayNumber+""); //$NON-NLS-1$
			// Setting the row background color for even numbered rows.
			if (displayNumber % 2 == 1)
				radiobranchFullRow.decorators = new DecoratorList(
						new UIColourDecorator(null, Color.decode(EvaluationConstant.LIGHT_GRAY_COLOR)));

			if ( EvalConstants.ITEM_SCALE_DISPLAY_FULL.equals(scaleDisplaySetting) ) {
				UIBranchContainer full = UIBranchContainer.make(radiobranchFullRow, "fullDisplay:"); //$NON-NLS-1$
				
				// Radio Buttons
				UISelect radios = UISelect.make(full, "dummyRadio", scaleValues,scaleLabels, bindings[0], null);				
				String selectID = radios.getFullID();

				if (disabled) {
					radios.selection.willinput = false;
					radios.selection.fossilize = false;
				}

				for (int j = 0; j < scaleValues.length; ++j) {
					UIBranchContainer radiobranchNested = UIBranchContainer
							.make(full, "scaleOptions:", Integer.toString(j));
					UISelectChoice.make(radiobranchNested,
							"dummyRadioValue", selectID, j); //$NON-NLS-1$
					UISelectLabel.make(radiobranchNested,
							"dummyRadioLabel", selectID, j); //$NON-NLS-1$
				}
			} else if ( EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL.equals(scaleDisplaySetting) ) {
				UIBranchContainer vertical = UIBranchContainer.make(
						radiobranchFullRow, "verticalDisplay:"); //$NON-NLS-1$
				
				UISelect radios = UISelect.make(vertical, "dummyRadio", scaleValues, scaleLabels, bindings[0], initValue);
				String selectID = radios.getFullID();

				if (disabled) {
					radios.selection.willinput = false;
					radios.selection.fossilize = false;
				}

				for (int j = 0; j < scaleValues.length; ++j) {
					UIBranchContainer radiobranchInside = UIBranchContainer
							.make(vertical, "scaleOptions:", Integer.toString(j));
					UISelectChoice.make(radiobranchInside,
							"dummyRadioValue", selectID, j); //$NON-NLS-1$
					UISelectLabel.make(radiobranchInside,
							"dummyRadioLabel", selectID, j); //$NON-NLS-1$
				}
			} else if ( EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED.equals(scaleDisplaySetting) ) {
				UIBranchContainer fullColored = UIBranchContainer.make(radiobranchFullRow,"fullDisplayColored:"); //$NON-NLS-1$

				UILink.make(fullColored, "idealImage", ScaledUtils.getIdealImageURL(scale)); //$NON-NLS-1$

				UISelect radios = UISelect.make(fullColored, "dummyRadio", scaleValues, scaleLabels, bindings[0], initValue);
				String selectID = radios.getFullID();

				if (disabled) {
					radios.selection.willinput = false;
					radios.selection.fossilize = false;
				}

				for (int j = 0; j < scaleValues.length; ++j) {
					UIBranchContainer radiobranchInside = UIBranchContainer
							.make(fullColored, "scaleOptions:", Integer.toString(j));
					UISelectChoice.make(radiobranchInside,
							"dummyRadioValue", selectID, j); //$NON-NLS-1$
					UISelectLabel.make(radiobranchInside,
							"dummyRadioLabel", selectID, j); //$NON-NLS-1$
				}
			}

		} else if (EvalConstants.ITEM_SCALE_DISPLAY_STEPPED.equals(scaleDisplaySetting) ) {
			UIBranchContainer stepped = UIBranchContainer.make(container, "steppedDisplay:"); //$NON-NLS-1$

			UIOutput.make(stepped, "itemNum", displayNumber+"" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			UIOutput.make(stepped, "itemText", templateItem.getItem().getItemText()); //$NON-NLS-1$

			handleNA(stepped, templateItem.getUsesNA().booleanValue());

			for (int count = 1; count <= optionCount; count++) {
				scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
				scaleLabels[optionCount - count] = scaleOptions[count-1];
			}
			
			UISelect radios = UISelect.make(stepped, "dummyRadio", scaleValues, scaleLabels, bindings[0], initValue); 
			String selectID = radios.getFullID();

			if (disabled) {
				radios.selection.willinput = false;
				radios.selection.fossilize = false;
			}

			for (int j = 0; j < scaleValues.length; ++j) {
				UIBranchContainer radioTopLabelBranch = UIBranchContainer
						.make(stepped, "scaleTopLabelOptions:", Integer.toString(j));
				UISelectLabel.make(radioTopLabelBranch,
						"dummyTopRadioLabel", selectID, j); //$NON-NLS-1$
				UILink.make(radioTopLabelBranch, "cornerImage", //$NON-NLS-1$
						EvaluationConstant.STEPPED_IMAGE_URLS[0]);

				// This branch container is created to help in creating the
				// middle images after the LABEL
				for (int k = 0; k < j; ++k) {
					UIBranchContainer radioTopLabelAfterBranch = UIBranchContainer
							.make(radioTopLabelBranch,
									"scaleTopLabelAfterOptions:", Integer.toString(k));
					UILink.make(radioTopLabelAfterBranch, "middleImage",
							EvaluationConstant.STEPPED_IMAGE_URLS[1]);
				}

				UIBranchContainer radioBottomLabelBranch = UIBranchContainer
						.make(stepped, "scaleBottomLabelOptions:", Integer.toString(j));
				UILink.make(radioBottomLabelBranch, "bottomImage",EvaluationConstant.STEPPED_IMAGE_URLS[2]);

				UIBranchContainer radioValueBranch = UIBranchContainer
						.make(stepped, "scaleValueOptions:", Integer.toString(j));
				UISelectChoice.make(radioValueBranch, "dummyRadioValue",selectID, j);
			}

		} else if ( EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED.equals(scaleDisplaySetting) ) {
			UIBranchContainer steppedColored = UIBranchContainer.make(container, "steppedDisplayColored:"); //$NON-NLS-1$

			UIOutput.make(steppedColored, "itemNum", displayNumber+"" ); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(steppedColored, "itemText", templateItem.getItem().getItemText()); //$NON-NLS-1$

			handleNA(steppedColored, templateItem.getUsesNA().booleanValue());

			UILink.make(steppedColored,  "idealImage", ScaledUtils.getIdealImageURL(scale)); //$NON-NLS-1$
			
			for (int count = 1; count <= optionCount; count++) {
				scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
				scaleLabels[optionCount - count] = scaleOptions[count-1];
			}

			UISelect radios = UISelect.make(steppedColored, "dummyRadio", scaleValues, scaleLabels, bindings[0], initValue); 
			String selectID = radios.getFullID();

			if (disabled) {
				radios.selection.willinput = false;
				radios.selection.fossilize = false;
			}

			for (int j = 0; j < scaleValues.length; ++j) {
				UIBranchContainer rowBranch = UIBranchContainer.make(
						steppedColored, "rowBranch:", Integer.toString(j)); //$NON-NLS-1$
				// Actual label
				UISelectLabel.make(rowBranch, "topLabel", selectID, j); //$NON-NLS-1$

				// Corner Image
				UILink.make(rowBranch, "cornerImage", //$NON-NLS-1$
						EvaluationConstant.STEPPED_IMAGE_URLS[0]);

				// This branch container is created to help in creating the
				// middle images after the LABEL
				for (int k = 0; k < j; ++k) {
					UIBranchContainer afterTopLabelBranch = UIBranchContainer
							.make(rowBranch, "afterTopLabelBranch:",Integer.toString(k));
					UILink.make(afterTopLabelBranch, "middleImage",
							EvaluationConstant.STEPPED_IMAGE_URLS[1]);
				}

				UIBranchContainer bottomLabelBranch = UIBranchContainer
						.make(steppedColored, "bottomLabelBranch:", Integer.toString(j));
				UILink.make(bottomLabelBranch, "bottomImage", //$NON-NLS-1$
						EvaluationConstant.STEPPED_IMAGE_URLS[2]);

				UIBranchContainer radioBranchFirst = UIBranchContainer
						.make(steppedColored, "scaleOptionsFirst:", Integer.toString(j));
				UISelectChoice.make(radioBranchFirst,
						"dummyRadioValueFirst", selectID, j); //$NON-NLS-1$

				UIBranchContainer radioBranchSecond = UIBranchContainer
						.make(steppedColored, "scaleOptionsSecond:",Integer.toString(j));
				UISelectChoice.make(radioBranchSecond,
						"dummyRadioValueSecond", selectID, j); //$NON-NLS-1$
			}
		}
		return container;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#getRenderType()
	 */
	public String getRenderType() {
		return EvalConstants.ITEM_TYPE_SCALED;
	}


	private void handleNA(UIContainer container, boolean useNA) {
		if (useNA) {
			UIBranchContainer radiobranch3 = UIBranchContainer.make(container, "showNA:"); //$NON-NLS-1$
			UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
			UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}

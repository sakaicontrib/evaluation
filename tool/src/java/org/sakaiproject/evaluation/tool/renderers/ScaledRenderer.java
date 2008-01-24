/**
 * ScaledRenderer.java - evaluation - Oct 29, 2007 2:59:12 PM - azeckoski
 * $URL: https://source.sakaiproject.org/contrib $
 * $Id: MultipleChoice.java 1000 Jan 21, 2008 2:59:12 PM azeckoski $
 **************************************************************************
 * Copyright (c) 2008 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.renderers;

import org.sakaiproject.evaluation.logic.utils.ArrayUtils;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.utils.ScaledUtils;

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
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;

/**
 * This handles the rendering of scaled type items
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ScaledRenderer implements ItemRenderer {

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
		if (bindings[0] == null) initValue = "";

		EvalScale scale = templateItem.getItem().getScale();
		String[] scaleOptions = scale.getOptions();
		int optionCount = scaleOptions.length;
		String scaleValues[] = new String[optionCount];
		String scaleLabels[] = new String[optionCount];

		String scaleDisplaySetting = templateItem.getScaleDisplaySetting();
		boolean usesNA = templateItem.getUsesNA().booleanValue();

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
					new DecoratorList( new UIStyleDecorator("compactDisplayStart") );// must match the existing CSS class
					//new DecoratorList(new UIColourDecorator(null, ScaledUtils.getStartColor(scale)));
				compactEndContainer.decorators =
					new DecoratorList( new UIStyleDecorator("compactDisplayEnd") );// must match the existing CSS class
					//new DecoratorList(new UIColourDecorator(null, ScaledUtils.getEndColor(scale)));
			}

			// For the radio buttons
			UIBranchContainer compactRadioContainer = UIBranchContainer.make(compact, "compactRadioContainer:"); //$NON-NLS-1$
			if (colored) {
				UILink.make(compactRadioContainer, "idealImage", ScaledUtils.getIdealImageURL(scale)); //$NON-NLS-1$
			}

			if (usesNA) {
				scaleValues = ArrayUtils.appendArray(scaleValues, EvaluationConstant.NA_VALUE);
				scaleLabels = ArrayUtils.appendArray(scaleLabels, "");
			}

			UISelect radios = UISelect.make(compactRadioContainer, "dummyRadio", scaleValues, scaleLabels, bindings[0], initValue);
			String selectID = radios.getFullID();
			
			if (disabled) {
				radios.selection.willinput = false;
				radios.selection.fossilize = false;
			}

			int scaleLength = scaleValues.length;
			int limit = usesNA? scaleLength - 1: scaleLength;  // skip the NA value at the end
			for (int j = 0; j < limit; ++j) {
				if (colored) {
					UIBranchContainer radioBranchFirst = UIBranchContainer.make(compactRadioContainer, 
							"scaleOptionsFake:", j+""); //$NON-NLS-1$
					UISelectChoice.make(radioBranchFirst,
							"dummyRadioValueFake", selectID, j); //$NON-NLS-1$
				}

				UIBranchContainer radioBranchSecond = UIBranchContainer.make(compactRadioContainer, 
						"scaleOptionsReal:", j+""); //$NON-NLS-1$
				UISelectChoice.make(radioBranchSecond,
						"dummyRadioValueReal", selectID, j); //$NON-NLS-1$
			}

			if (usesNA) {
				UIBranchContainer radiobranch3 = UIBranchContainer.make(compact, "showNA:"); //$NON-NLS-1$
				radiobranch3.decorators = new DecoratorList( new UIStyleDecorator("na") );// must match the existing CSS class
				UISelectChoice choice = UISelectChoice.make(radiobranch3, "na-input", selectID, scaleLength - 1); //$NON-NLS-1$
				UILabelTargetDecorator.targetLabel(
						UIMessage.make(radiobranch3, "na-desc", "viewitem.na.desc"), choice);
			}

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

			// display next row
			UIBranchContainer radiobranchFullRow = UIBranchContainer.make(container, "nextrow:", displayNumber+""); //$NON-NLS-1$

			String containerId;
			if ( EvalConstants.ITEM_SCALE_DISPLAY_FULL.equals(scaleDisplaySetting) ) {
				containerId = "fullDisplay:";
			} else if ( EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL.equals(scaleDisplaySetting) ) {
				containerId = "verticalDisplay:";
			} else if ( EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED.equals(scaleDisplaySetting) ) {
				containerId = "fullDisplayColored:";
			} else {
				throw new RuntimeException("Invalid scaleDisplaySetting (this should not be possible): " + scaleDisplaySetting);
			}

			UIBranchContainer displayContainer = UIBranchContainer.make(radiobranchFullRow, containerId); //$NON-NLS-1$

			if ( EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED.equals(scaleDisplaySetting) ) {
				UILink.make(displayContainer, "idealImage", ScaledUtils.getIdealImageURL(scale)); //$NON-NLS-1$
			}

			if (usesNA) {
				scaleValues = ArrayUtils.appendArray(scaleValues, EvaluationConstant.NA_VALUE);
				scaleLabels = ArrayUtils.appendArray(scaleLabels, "");
			}

			UISelect radios = UISelect.make(displayContainer, "dummyRadio", scaleValues, scaleLabels, bindings[0], initValue);
			String selectID = radios.getFullID();

			if (disabled) {
				radios.selection.willinput = false;
				radios.selection.fossilize = false;
			}

			int scaleLength = scaleValues.length;
			int limit = usesNA? scaleLength - 1: scaleLength;  // skip the NA value at the end
			for (int j = 0; j < limit; ++j) {
				UIBranchContainer radiobranchNested = UIBranchContainer
						.make(displayContainer, "scaleOptions:", j+"");
				UISelectChoice choice = UISelectChoice.make(radiobranchNested, "dummyRadioValue", selectID, j); //$NON-NLS-1$
				UILabelTargetDecorator.targetLabel(
						UISelectLabel.make(radiobranchNested, "dummyRadioLabel", selectID, j),
						choice);
			}

			if (usesNA) {
				UIBranchContainer radiobranch3 = UIBranchContainer.make(displayContainer, "showNA:"); //$NON-NLS-1$
				radiobranch3.decorators = new DecoratorList( new UIStyleDecorator("na") );// must match the existing CSS class				
				UISelectChoice choice = UISelectChoice.make(radiobranch3, "na-input", selectID, scaleLength - 1); //$NON-NLS-1$
				UILabelTargetDecorator.targetLabel(
						UIMessage.make(radiobranch3, "na-desc", "viewitem.na.desc"),
						choice);
			}


		} else if (EvalConstants.ITEM_SCALE_DISPLAY_STEPPED.equals(scaleDisplaySetting) ||
				EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED.equals(scaleDisplaySetting) ) {
			UIBranchContainer stepped = UIBranchContainer.make(container, "steppedDisplay:"); //$NON-NLS-1$

			// setup simple variables to make code more clear
			boolean colored = EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED.equals(scaleDisplaySetting);

			UIOutput.make(stepped, "itemNum", displayNumber+"" ); //$NON-NLS-1$ //$NON-NLS-2$
			UIVerbatim.make(stepped, "itemText", templateItem.getItem().getItemText()); //$NON-NLS-1$

			UIBranchContainer coloredBranch = null;
			if (colored) {
				coloredBranch = UIBranchContainer.make(stepped, "coloredChoicesBranch:");
				UILink.make(coloredBranch, "idealImage", ScaledUtils.getIdealImageURL(scale)); //$NON-NLS-1$
			}

			for (int count = 1; count <= optionCount; count++) {
				scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
				scaleLabels[optionCount - count] = scaleOptions[count-1];
			}

			if (usesNA) {
				scaleValues = ArrayUtils.appendArray(scaleValues, EvaluationConstant.NA_VALUE);
				scaleLabels = ArrayUtils.appendArray(scaleLabels, "");
			}

			UISelect radios = UISelect.make(stepped, "dummyRadio", scaleValues, scaleLabels, bindings[0], initValue); 
			String selectID = radios.getFullID();

			if (disabled) {
				radios.selection.willinput = false;
				radios.selection.fossilize = false;
			}

			int scaleLength = scaleValues.length;
			int limit = usesNA? scaleLength - 1: scaleLength;  // skip the NA value at the end
			UISelectLabel[] labels = new UISelectLabel[limit];
			UISelectChoice[] choices = new UISelectChoice[limit];
			for (int j = 0; j < limit; ++j) {
				UIBranchContainer rowBranch = UIBranchContainer.make(stepped, "rowBranch:", j+""); //$NON-NLS-1$

				// Actual label
				labels[limit-j-1] = UISelectLabel.make(rowBranch, "topLabel", selectID, j); //$NON-NLS-1$

				// Corner Image
				UILink.make(rowBranch, "cornerImage", EvaluationConstant.STEPPED_IMAGE_URLS[0]);

				// create middle images after the item label
				for (int k = 0; k < j; ++k) {
					UIBranchContainer afterTopLabelBranch = UIBranchContainer.make(rowBranch, "afterTopLabelBranch:", k+"");
					UILink.make(afterTopLabelBranch, "middleImage",	EvaluationConstant.STEPPED_IMAGE_URLS[1]);
				}

				// create bottom (down arrow) image
				UIBranchContainer bottomLabelBranch = UIBranchContainer.make(stepped, "bottomLabelBranch:", j+"");
				UILink.make(bottomLabelBranch, "bottomImage", EvaluationConstant.STEPPED_IMAGE_URLS[2]);

				if (colored) {
					UIBranchContainer radioBranchFirst = UIBranchContainer.make(coloredBranch, "scaleOptionsFirst:", j+"");
					choices[j] = UISelectChoice.make(radioBranchFirst, "dummyRadioValueFirst", selectID, j); //$NON-NLS-1$
				}

				UIBranchContainer radioBranchSecond = UIBranchContainer.make(stepped, "scaleOptionsSecond:", j+"");
				UISelectChoice choice = UISelectChoice.make(radioBranchSecond, "dummyRadioValueSecond", selectID, j); //$NON-NLS-1$
				if (!colored) {
					choices[j] = choice;
				}
			}

			for (int i = 0; i < choices.length; i++) {
				UILabelTargetDecorator.targetLabel(labels[i], choices[i]);
			}

			if (usesNA) {
				UISelectChoice choice = UISelectChoice.make(stepped, "na-input", selectID, scaleLength - 1); //$NON-NLS-1$
				UILabelTargetDecorator.targetLabel(
						UIMessage.make(container, "na-desc", "viewitem.na.desc"),
						choice);
			}

      } else {
         throw new IllegalStateException("Unknown scaleDisplaySetting ("+scaleDisplaySetting+") for " + getRenderType());
      }
		return container;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#getRenderType()
	 */
	public String getRenderType() {
		return EvalConstants.ITEM_TYPE_SCALED;
	}

}

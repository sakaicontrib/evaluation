/**
 * MultipleAnswerRenderer.java - evaluation - Jan 21, 2008 2:59:12 PM - azeckoski
 * $URL$
 * $Id$
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

import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;

/**
 * This handles the rendering of multiple answer type items
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class MultipleAnswerRenderer implements ItemRenderer {

	/**
	 * This identifies the template component associated with this renderer
	 */
	public static final String COMPONENT_ID = "render-multipleanswer-item:";

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#renderItem(uk.org.ponder.rsf.components.UIContainer, java.lang.String, org.sakaiproject.evaluation.model.EvalTemplateItem, int, boolean)
	 */
	public UIJointContainer renderItem(UIContainer parent, String ID, String[] bindings, EvalTemplateItem templateItem, int displayNumber, boolean disabled) {
		UIJointContainer container = new UIJointContainer(parent, ID, COMPONENT_ID);

		if (displayNumber <= 0) displayNumber = 0;
      Boolean initValue = null;
      if (bindings[0] == null) initValue = Boolean.FALSE;

      String naBinding = null;
      if (bindings.length > 1) {
         naBinding = bindings[1];
      }
      Boolean naInit = null;
      if (naBinding == null) naInit = Boolean.FALSE;

		EvalScale scale = templateItem.getItem().getScale();
		String[] scaleOptions = scale.getOptions();
		int optionCount = scaleOptions.length;

		String scaleDisplaySetting = templateItem.getScaleDisplaySetting();
		boolean usesNA = templateItem.getUsesNA().booleanValue();

		if (EvalConstants.ITEM_SCALE_DISPLAY_FULL.equals(scaleDisplaySetting) || 
			EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL.equals(scaleDisplaySetting)) {

			UIBranchContainer fullFirst = UIBranchContainer.make(container, "fullType:");

			UIOutput.make(fullFirst, "itemNum", displayNumber+"" );
			UIVerbatim.make(fullFirst, "itemText", templateItem.getItem().getItemText());

			// display next row
			UIBranchContainer fullRow = UIBranchContainer.make(container, "nextrow:", displayNumber+"");

			String containerId;
			if ( EvalConstants.ITEM_SCALE_DISPLAY_FULL.equals(scaleDisplaySetting) ) {
				containerId = "fullDisplay:";
			} else if ( EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL.equals(scaleDisplaySetting) ) {
				containerId = "verticalDisplay:";
			} else {
				throw new RuntimeException("Invalid scaleDisplaySetting (this should not be possible): " + scaleDisplaySetting);
			}

			UIBranchContainer displayContainer = UIBranchContainer.make(fullRow, containerId);

         for (int j = 0; j < optionCount; ++j) {
            UIBranchContainer scaleOption = UIBranchContainer.make(displayContainer, "scaleOptions:", j+"");
            // e.g. binding: answer.5.multipleAnswers.1
            UIBoundBoolean checkbox = UIBoundBoolean.make(scaleOption, "multipleAnswerValue", bindings[0] + "." + j, initValue);
            UILabelTargetDecorator.targetLabel(UIOutput.make(scaleOption, "multipleAnswerLabel", scaleOptions[j]), checkbox);
            if (disabled) {
               checkbox.willinput = false;
               checkbox.fossilize = false;
            }
         }

			if (usesNA) {
				UIBranchContainer branchNA = UIBranchContainer.make(container, "showNA:");
				branchNA.decorators = new DecoratorList( new UIStyleDecorator("na") );// must match the existing CSS class
            UIBoundBoolean checkbox = UIBoundBoolean.make(branchNA, "itemNA", naBinding, naInit);
            UILabelTargetDecorator.targetLabel(UIMessage.make(branchNA, "na-desc", "viewitem.na.desc"), checkbox);
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
		return EvalConstants.ITEM_TYPE_MULTIPLEANSWER;
	}

}

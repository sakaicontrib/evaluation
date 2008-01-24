/**
 * AddItemControlRenderer.java - evaluation - Oct 29, 2007 2:59:12 PM - azeckoski
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

import org.sakaiproject.evaluation.tool.viewparams.TemplateItemViewParameters;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewStateHandler;

/**
 * This handles the rendering of add item controls
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class AddItemControlRenderer {

	/**
	 * This identifies the template component associated with this renderer
	 */
	public static final String COMPONENT_ID = "render_add_item_control:";

	private ViewStateHandler viewStateHandler;
	public void setViewStateHandler(ViewStateHandler viewStateHandler) {
		this.viewStateHandler = viewStateHandler;
	}

	public UIJointContainer renderControl(UIContainer parent, String ID, String[] viewIDs, String[] labels, UIMessage addButtonLabel, Long templateId) {

		UIJointContainer container = new UIJointContainer(parent, ID, COMPONENT_ID);

		String[] values = convertViews(viewIDs, templateId);

		UIForm form = UIForm.make(container, "add-item-form");
		UISelect.make(form, "add-item-classification", values, labels, values[0], false).setMessageKeys();
		UICommand.make(form, "add-item-button", addButtonLabel);

		return container;
	}

	private ViewParameters deriveTarget(String viewID, Long templateId) {
		return new TemplateItemViewParameters(viewID, templateId, null);
	}

	private String[] convertViews(String[] viewIDs, Long templateId) {
		String[] togo = new String[viewIDs.length];
		for (int i = 0; i < viewIDs.length; ++i) {
			togo[i] = viewStateHandler.getFullURL(deriveTarget(viewIDs[i], templateId));
		}
		return togo;
	}

}

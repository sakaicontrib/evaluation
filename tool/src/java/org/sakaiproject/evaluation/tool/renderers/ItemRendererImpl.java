/**
 * ItemRendererImpl.java - evaluation - Oct 29, 2007 2:59:12 PM - azeckoski
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.logic.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;

/**
 * The main implementation for the ItemRenderer class which allows the presentation programmers
 * to simply inject and use a single class for all rendering of items
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ItemRendererImpl implements ItemRenderer {

	private Map<String, ItemRenderer> renderImpls = new HashMap<String, ItemRenderer>();
	public void setRenderTypes(List<ItemRenderer> types) {
		for (Iterator<ItemRenderer> iter = types.iterator(); iter.hasNext();) {
			ItemRenderer ir = iter.next();
			renderImpls.put(ir.getRenderType(), ir);
		}
	}

	public void init() {
		// just check that the renderImpls are inited
		if (renderImpls.size() <= 0) {
			throw new IllegalStateException("The renderTypes must be set before this class can be used");
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#renderItem(uk.org.ponder.rsf.components.UIContainer, java.lang.String, org.sakaiproject.evaluation.model.EvalTemplateItem, int, boolean)
	 */
	public UIJointContainer renderItem(UIContainer parent, String ID, String[] bindings, EvalTemplateItem templateItem, int displayNumber, boolean disabled) {
		// do a quick check to make sure stuff is ok
		if (templateItem == null) {
			throw new IllegalArgumentException("templateItem cannot be null");
		}
		if (templateItem.getItem() == null) {
			throw new IllegalArgumentException("item (from templateItem.getItem()) cannot be null");
		}

		if (bindings == null || 
				(bindings != null && bindings.length == 0) ) {
			bindings = new String[] {null};
		}

		// figure out the type of item and then call the appropriate renderer
		String itemTypeConstant = TemplateItemUtils.getTemplateItemType(templateItem);
		ItemRenderer renderer = (ItemRenderer) renderImpls.get( itemTypeConstant );
		if (renderer == null) {
			throw new IllegalStateException("No renderer available for this item type: " + itemTypeConstant);
		}
		return renderer.renderItem(parent, ID, bindings, templateItem, displayNumber, disabled);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#getRenderType()
	 */
	public String getRenderType() {
		// this handles no specific type so return null
		return null;
	}

}

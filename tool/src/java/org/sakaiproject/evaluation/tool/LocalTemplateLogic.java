/*
 * Created on 23 Jan 2007
 */
package org.sakaiproject.evaluation.tool;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/*
 * A "Local DAO" to focus dependencies and centralise fetching logic for the
 * Template views.
 */

public class LocalTemplateLogic {
	private EvalItemsLogic itemsLogic;

	public void setItemsLogic(EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

	private EvalTemplatesLogic templatesLogic;

	public void setTemplatesLogic(EvalTemplatesLogic templatesLogic) {
		this.templatesLogic = templatesLogic;
	}

	private EvalExternalLogic external;

	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

	public EvalTemplate fetchTemplate(Long templateId) {
		return templatesLogic.getTemplateById(templateId);
	}

	public EvalTemplateItem fetchTemplateItem(Long itemId) {
		return itemsLogic.getTemplateItemById(itemId);
	}

	public List fetchTemplateItems(Long templateId) {
		if (templateId == null) {
			return new ArrayList();
		}
		else {
			return itemsLogic.getTemplateItemsForTemplate(templateId, 
					external.getCurrentUserId(), null);
		}
	}

	public void saveTemplate(EvalTemplate tosave) {
		templatesLogic.saveTemplate(tosave, external.getCurrentUserId());
	}
	public void saveItem(EvalItem tosave) {
		itemsLogic.saveItem(tosave, external.getCurrentUserId());
	}

	public void saveTemplateItem(EvalTemplateItem tosave) {
		itemsLogic.saveTemplateItem(tosave, external.getCurrentUserId());
	}

	public void deleteTemplateItem(Long id) {
		itemsLogic.deleteTemplateItem(id, external.getCurrentUserId());
	}

	public EvalTemplate newTemplate() {
		EvalTemplate currTemplate = new EvalTemplate(new Date(), 
				external.getCurrentUserId(), EvalConstants.TEMPLATE_TYPE_STANDARD, 
				null, "private", Boolean.FALSE);
		currTemplate.setDescription(""); // Note- somehow gives DataIntegrityViolation if null
		return currTemplate;
	}

	public EvalTemplateItem newTemplateItem() {
		String level = EvalConstants.HIERARCHY_LEVEL_TOP;
		String nodeId = EvalConstants.HIERARCHY_NODE_ID_TOP;

		// TODO - this should respect the current level the user is at

		EvalItem newItem = new EvalItem(new Date(), external.getCurrentUserId(), "", "",
				"", new Boolean(false));
		EvalTemplateItem newTemplateItem = new EvalTemplateItem( new Date(), 
				external.getCurrentUserId(), null, newItem, null, 
				EvaluationConstant.ITEM_CATEGORY_VALUES[0], level, nodeId);
		newTemplateItem.setUsesNA(new Boolean(false));
		return newTemplateItem;
	}
}

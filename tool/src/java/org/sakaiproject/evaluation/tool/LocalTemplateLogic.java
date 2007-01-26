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
    return itemsLogic.getTemplateItemsForTemplate(templateId, external
        .getCurrentUserId());
    }
  }
  
  public void saveTemplate(EvalTemplate tosave) {
    templatesLogic.saveTemplate(tosave, external.getCurrentUserId());
  }
  
  public void saveTemplateItem(EvalTemplateItem tosave) {
    itemsLogic.saveTemplateItem(tosave, external.getCurrentUserId());
  }
  
  public EvalTemplate newTemplate() {
    EvalTemplate currTemplate = new EvalTemplate(new Date(), external.getCurrentUserId(),
        null, "private", Boolean.FALSE);
    currTemplate.setDescription(""); // TODO - somehow gives DataIntegrityViolation if null
    return currTemplate;
  }
  
  public EvalTemplateItem newTemplateItem() {
    EvalItem newItem = new EvalItem(new Date(), external.getCurrentUserId(), "", "",
        "", new Boolean(false));
    return new EvalTemplateItem(new Date(), external.getCurrentUserId(),
        null, newItem, null, "");
  }
}

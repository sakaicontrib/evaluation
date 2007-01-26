/******************************************************************************
 * TemplateBBean.java - created by whumphri@vt.edu on Jan 16, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Antranig Basman
 * Will Humphries (whumphri@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

/**
 * This request-scope bean handles template creation and modification.
 * 
 * @author Antranig Basman
 * @author will Humphries (whumphri@vt.edu)
 */

public class TemplateBBean {
  /*
   * VARIABLE DECLARATIONS
   */
  private static Log log = LogFactory.getLog(TemplateBean.class);

  private TemplateBeanLocator templateBeanLocator;

  public void setTemplateBeanLocator(TemplateBeanLocator templateBeanLocator) {
    this.templateBeanLocator = templateBeanLocator;
  }

  private TemplateItemBeanLocator templateItemBeanLocator;

  public void setTemplateItemBeanLocator(
      TemplateItemBeanLocator templateItemBeanLocator) {
    this.templateItemBeanLocator = templateItemBeanLocator;
  }

  private LocalTemplateLogic localTemplateLogic;

  public void setLocalTemplateLogic(LocalTemplateLogic localTemplateLogic) {
    this.localTemplateLogic = localTemplateLogic;
  }

  /**
   * If the template is not saved, button will show text "continue and add
   * question" method binding to the "continue and add question" button on
   * template_title_description.html replaces TemplateBean.createTemplateAction,
   * but template is added to db here.
   */
  public String createTemplateAction() {
    templateBeanLocator.saveAll();
    return "success";
  }

  /**
   * If the template is already stored, button will show text "Save" method
   * binding to the "Save" button on template_title_description.html replaces
   * TemplateBean.saveTemplate()
   */
  public String updateTemplateTitleDesc() {
    templateBeanLocator.saveAll();
    return "success";
  }

  public Long templateId;

  private void emit(EvalTemplateItem toemit, int outindex) {
    toemit.setDisplayOrder(new Integer(outindex));
    localTemplateLogic.saveTemplateItem(toemit);
  }

  // NB - this implementation depends on Hibernate reference equality
  // semantics!!
  // Guarantees output sequence is consecutive without duplicates, and will
  // prefer honoring user sequence requests so long as they are not inconsistent.
  public void saveReorder() {
    Map delivered = templateItemBeanLocator.getDeliveredBeans();
    List ordered = localTemplateLogic.fetchTemplateItems(templateId);
    for (int i = 1; i <= ordered.size();) {
      EvalTemplateItem item = (EvalTemplateItem) ordered.get(i - 1);
      int itnum = item.getDisplayOrder().intValue();
      if (i < ordered.size()) {
        EvalTemplateItem next = (EvalTemplateItem) ordered.get(i);
        int nextnum = next.getDisplayOrder().intValue();
        if (itnum == nextnum) {
          if (delivered.containsValue(item) ^ (itnum == i)) {
            emit(next, i++); emit(item, i++); continue;
          }
          else {
            emit(item, i++); emit(next, i++); continue;
          }
        }
      }
      emit(item, i++);
    }
  }
  
}
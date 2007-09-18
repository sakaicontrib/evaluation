/*
 * Created on 18 Feb 2007
 */
package org.sakaiproject.evaluation.tool.wrapper;

import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.locators.TemplateBeanLocator;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;

import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
  * An interceptor for the outgoing URLs from action cycles that applies the
 * IDs from freshly saved entities to the templateId field in an outgoing
 * EvalViewParameters. 
 * @author Antranig Basman (amb26@ponder.org.uk)
 *
 */

public class EvalActionResultInterceptor implements ActionResultInterceptor {

  private TemplateBeanLocator templateBeanLocator;

  public void setTemplateBeanLocator(TemplateBeanLocator templateBeanLocator) {
    this.templateBeanLocator = templateBeanLocator;
  }
  // Standard semantics - if there was an incoming ID, outgoing will be the same
  // if incoming is blank, and OTP bean contains a new entity, outgoing will be
  // the new entity.
  
  public void interceptActionResult(ARIResult result, ViewParameters incoming,
      Object actionReturn) {
    if (incoming instanceof TemplateViewParameters
        && result.resultingView instanceof TemplateViewParameters) {
      TemplateViewParameters ineval = (TemplateViewParameters) incoming;
      TemplateViewParameters outgoing = (TemplateViewParameters) result.resultingView;
      if (ineval.templateId != null) {
        outgoing.templateId = ineval.templateId;
      }
      else {
        EvalTemplate template = (EvalTemplate) templateBeanLocator
            .locateBean(TemplateBeanLocator.NEW_1);
        if (template != null) {
          outgoing.templateId = template.getId();
        }
      }
    }
  }

}

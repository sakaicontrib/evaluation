/*
 * Created on 24 Jan 2007
 */
package org.sakaiproject.evaluation.tool.wrapper;

import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.TemplateBeanLocator;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;
import org.sakaiproject.evaluation.tool.producers.TemplateModifyProducer;
import org.sakaiproject.evaluation.tool.producers.TemplateProducer;

import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.viewstate.AnyViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.util.RunnableInvoker;

public class EntityRewriteWrapper implements RunnableInvoker {

  private ARIResult ariresult;
  private ViewParameters incoming;

  public void setARIResult(ARIResult ariresult) {
    this.ariresult = ariresult;
  }
  public void setViewParameters(ViewParameters incoming) {
    this.incoming = incoming;
  }
  
  private TemplateBeanLocator templateBeanLocator;

  public void setTemplateBeanLocator(TemplateBeanLocator templateBeanLocator) {
    this.templateBeanLocator = templateBeanLocator;
    }
  // Returns true for those states which are part of the "new" transition system
  // Standard semantics - if there was an incoming ID, outgoing will be the same
  // if incoming is blank, and OTP bean contains a new entity, outgoing will be the
  // new entity.
  private void rewriteOutgoing(EvalViewParameters outgoing) {
    if (incoming.viewID.equals(TemplateModifyProducer.VIEW_ID)
        || incoming.viewID.equals(TemplateProducer.VIEW_ID)) {
      EvalViewParameters ineval = (EvalViewParameters) incoming;
      if (ineval.templateId != null) {
        outgoing.templateId = ineval.templateId;
      }
      else {
        EvalTemplate template = (EvalTemplate) templateBeanLocator.locateBean(TemplateBeanLocator.NEW_1);
        if (template != null) {
          outgoing.templateId = template.getId();
        }
      }
    }
  }
  
  public void invokeRunnable(Runnable toinvoke) {
    toinvoke.run();
    AnyViewParameters outgoing = ariresult.getResultingView();
    
    if (outgoing instanceof EvalViewParameters) {
      EvalViewParameters eval = (EvalViewParameters) outgoing;
        rewriteOutgoing(eval);
      }
    }
  
}

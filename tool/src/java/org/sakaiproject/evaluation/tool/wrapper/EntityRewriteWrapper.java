/*
 * Created on 24 Jan 2007
 */
package org.sakaiproject.evaluation.tool.wrapper;

import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.TemplateBeanLocator;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;
import org.sakaiproject.evaluation.tool.producers.ModifyEssayProducer;
import org.sakaiproject.evaluation.tool.producers.ModifyHeaderProducer;
import org.sakaiproject.evaluation.tool.producers.ModifyScaledProducer;
import org.sakaiproject.evaluation.tool.producers.RemoveQuestionProducer;
import org.sakaiproject.evaluation.tool.producers.TemplateModifyProducer;
import org.sakaiproject.evaluation.tool.producers.TemplateProducer;

import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.request.EarlyRequestParser;
import uk.org.ponder.rsf.viewstate.AnyViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.util.RunnableInvoker;

/** An interceptor for the outgoing URLs from action cycles that applies the
 * IDs from freshly saved entities to the templateId field in an outgoing
 * EvalViewParameters. This is a temporary measure to get the
 * application working, in as close as possible to its final form, until
 * RSF is upgraded to allow "local ARIs" or some equivalent system.
 */

public class EntityRewriteWrapper implements RunnableInvoker {

  private ARIResult ariresult;
  private ViewParameters incoming;
  private String requesttype;

  public void setARIResult(ARIResult ariresult) {
    this.ariresult = ariresult;
  }

  public void setViewParameters(ViewParameters incoming) {
    this.incoming = incoming;
  }

  public void setRequestType(String requesttype) {
    this.requesttype = requesttype;
  }

  private TemplateBeanLocator templateBeanLocator;

  public void setTemplateBeanLocator(TemplateBeanLocator templateBeanLocator) {
    this.templateBeanLocator = templateBeanLocator;
  }

  // Acts only on those states which are part of the "new" transition system.
  // Standard semantics - if there was an incoming ID, outgoing will be the same
  // if incoming is blank, and OTP bean contains a new entity, outgoing will be
  // the new entity.
  private void rewriteOutgoing(EvalViewParameters outgoing) {
    if (incoming.viewID.equals(TemplateModifyProducer.VIEW_ID)
        || incoming.viewID.equals(TemplateProducer.VIEW_ID)
        || incoming.viewID.equals(ModifyScaledProducer.VIEW_ID)
        || incoming.viewID.equals(ModifyHeaderProducer.VIEW_ID)
        || incoming.viewID.equals(ModifyEssayProducer.VIEW_ID)
        || incoming.viewID.equals(RemoveQuestionProducer.VIEW_ID)) {
      EvalViewParameters ineval = (EvalViewParameters) incoming;
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

  public void invokeRunnable(Runnable toinvoke) {
    toinvoke.run();
    if (requesttype.equals(EarlyRequestParser.ACTION_REQUEST)) {
      AnyViewParameters outgoing = ariresult.getResultingView();

      if (outgoing instanceof EvalViewParameters) {
        EvalViewParameters eval = (EvalViewParameters) outgoing;
        rewriteOutgoing(eval);
      }
    }
  }

}

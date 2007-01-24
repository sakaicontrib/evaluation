package org.sakaiproject.evaluation.tool;

import org.sakaiproject.evaluation.tool.params.EvalViewParameters;
import org.sakaiproject.evaluation.tool.params.TemplateItemViewParameters;

import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterpreter;
import uk.org.ponder.rsf.viewstate.ViewParameters;

// TODO: This entire bean to be destroyed, integrate "new" views into 
// EntityRewriteWrapper system
public class EvalARI implements ActionResultInterpreter {

  private ItemsBean itemsBean;

  public void setItemsBean(ItemsBean itemsBean) {
    this.itemsBean = itemsBean;
  }

  public ARIResult interpretActionResult(ViewParameters incoming, Object result) {
    // Avoid acting for views where not required.
    if (!(result instanceof String))
      return null;
    ARIResult togo = new ARIResult();
    String s = (String) result;
    // TODO: fold templateItmeProducer into the Wrapper system!
    // From ItemsBean via templateItemProducer
    if (s.substring(0, 15).equals("item-created:::")) {
      TemplateItemViewParameters templateItemViewParams = (TemplateItemViewParameters) incoming;
      togo.resultingview = new EvalViewParameters(s.substring(15),
          itemsBean.templateId);
    }
    else
      return null;

    togo.propagatebeans = ARIResult.FLOW_END;
    return togo;
  }

}
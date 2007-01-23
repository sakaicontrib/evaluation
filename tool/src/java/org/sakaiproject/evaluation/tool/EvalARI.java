package org.sakaiproject.evaluation.tool;

import org.sakaiproject.evaluation.tool.params.EvalViewParameters;
import org.sakaiproject.evaluation.tool.params.TemplateItemViewParameters;

import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterpreter;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class EvalARI implements ActionResultInterpreter {
  
	private TemplateBBean templateBBean;
	
	public void setTemplateBBean(TemplateBBean templateBBean) {
		this.templateBBean = templateBBean;
	}
	
	private ItemsBean itemsBean;
	public void setItemsBean(ItemsBean itemsBean) {
		this.itemsBean = itemsBean;
	}
	
public ARIResult interpretActionResult(ViewParameters incoming, Object result) {
    // clearly not one of our views
    if (!(result instanceof String)) return null;
    ARIResult togo = new ARIResult();
    String s = (String)result;
    if(s.substring(0,11).equals("new-item:::")){
    	System.out.println("New item, bbean tid:"+templateBBean.templateId);
    	EvalViewParameters evalViewParams=(EvalViewParameters)incoming;
    	togo.resultingview=new TemplateItemViewParameters(s.substring(11), itemsBean.templateId, null, evalViewParams.viewID);
    }
    else if(s.substring(0,11).equals("mod-item:::")){
    	EvalViewParameters evalViewParams=(EvalViewParameters)incoming;
    	togo.resultingview=new TemplateItemViewParameters(s.substring(11), itemsBean.templateItem.getTemplate().getId(), itemsBean.templateItem.getId(), evalViewParams.viewID);
    }
    else if(s.substring(0,15).equals("item-created:::")){
    	TemplateItemViewParameters templateItemViewParams=(TemplateItemViewParameters)incoming;
    	togo.resultingview=new EvalViewParameters(s.substring(15), itemsBean.templateId, templateItemViewParams.viewID);
    }
    else if(s.substring(0,23).equals("intercept-append-tId:::")){
    	System.out.println("We found our desired result, destination will be"+s.substring(23));
    	EvalViewParameters evalViewParams = (EvalViewParameters)incoming;
        togo.resultingview=new EvalViewParameters(s.substring(23),templateBBean.templateId, evalViewParams.viewID);
    	
    }

    else togo.resultingview=new SimpleViewParameters((String)result);

    togo.propagatebeans = ARIResult.FLOW_END;
    return togo;
  }


}
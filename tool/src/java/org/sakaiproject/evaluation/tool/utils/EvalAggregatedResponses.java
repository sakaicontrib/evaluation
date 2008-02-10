package org.sakaiproject.evaluation.tool.utils;

import java.util.List;

import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

/* 
 * This pea holds the aggregated responses that are returned from the 
 * ResponseAggregatorUtil.
 * 
 * The first three pea members are included for the sake of convenience when 
 * iterating over this object when constructing response output. This is so 
 * reports can be generated easily using only this object as the data source.
 */
public class EvalAggregatedResponses {
   public EvalEvaluation evaluation;
   public EvalTemplate template;
   public String[] groupIds;
   
   public List<EvalItem> allEvalItems;
   public List<EvalTemplateItem> allEvalTemplateItems;
   public List<String> topRow;
   public List<List<String>> responseRows;
   public int numOfResponses;
   
   /*
    * Default constructor, in case this ever needs to be created by Spring for
    * some reason.
    */
   public EvalAggregatedResponses() {}
   
   public EvalAggregatedResponses(EvalEvaluation evaluation, String[] groupIds,
         List<EvalItem> allEvalItems, List<EvalTemplateItem> allEvalTemplateItems,
         List<String> topRow, List<List<String>> responseRows, int numOfResponses) {
      this.evaluation = evaluation;
      this.template = evaluation.getTemplate();
      this.groupIds = groupIds;
      this.allEvalItems = allEvalItems;
      this.allEvalTemplateItems = allEvalTemplateItems;
      this.topRow = topRow;
      this.responseRows = responseRows;
      this.numOfResponses = numOfResponses;
   }
}

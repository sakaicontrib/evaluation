package org.sakaiproject.evaluation.tool.reporting;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.tool.producers.ReportChooseGroupsProducer;
import org.sakaiproject.evaluation.tool.producers.ReportsViewingProducer;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;

import uk.org.ponder.rsf.viewstate.AnyViewParameters;
import uk.org.ponder.rsf.viewstate.RedirectViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsInterceptor;
import uk.org.ponder.rsf.viewstate.support.ViewParamsInterceptorManager;

public class ReportViewParamsInterceptor implements ViewParamsInterceptor {

   private CurrentViewableReports curViewableReports;
   public void setCurrentViewableReports(CurrentViewableReports cur) {
      this.curViewableReports = cur;
   }
   
   private ViewParamsInterceptorManager viewParamsInterceptorManager;
   public void setViewParamsInterceptorManager(ViewParamsInterceptorManager manager) {
      this.viewParamsInterceptorManager = manager;
   }
   
   /* FIXME TODO sgithens 2008-02-24 5:06PM Central Time
    * This injecting of the VPIM is a temporary thing because I'm in a coffee 
    * shop and don't know what the appropriate parent definition to use is.
    */
   public void init() {
      List togo = new ArrayList();
      togo.add(this);
      viewParamsInterceptorManager.setInterceptors(togo);
   }

   public AnyViewParameters adjustViewParameters(ViewParameters incoming) {
      AnyViewParameters togo = incoming;
      
      if (ReportChooseGroupsProducer.VIEW_ID.equals(incoming.viewID)) {
         ReportParameters params = (ReportParameters) incoming;
         curViewableReports.populate(params.evaluationId);
         if (curViewableReports.getViewableGroupIDs().length <= 1) {
            ReportParameters viewReports = (ReportParameters) params.copyBase();
            viewReports.viewID = ReportsViewingProducer.VIEW_ID;
            togo = new RedirectViewParameters(viewReports);
         }
      }
      
      return togo;
   }

}

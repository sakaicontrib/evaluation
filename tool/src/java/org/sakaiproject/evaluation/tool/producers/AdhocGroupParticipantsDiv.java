package org.sakaiproject.evaluation.tool.producers;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.content.ContentTypeInfoRegistry;
import uk.org.ponder.rsf.content.ContentTypeReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class AdhocGroupParticipantsDiv implements ViewComponentProducer//,
{
//ContentTypeReporter {
    public static final String VIEW_ID = "adhoc_group_participants_div";
    
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        // TODO Auto-generated method stub
    }

    public String getViewID() {
        return VIEW_ID;
    }

 //   public String getContentType() {
 //       return ContentTypeInfoRegistry.HTML_FRAGMENT;
  //  }

}

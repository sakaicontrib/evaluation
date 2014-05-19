package org.sakaiproject.evaluation.tool.producers;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class MessagesProducer implements ViewComponentProducer {
	
	public static String VIEW_ID = "messages";
    public String getViewID() {
        return VIEW_ID;
    }

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		

	}


}

package org.sakaiproject.evaluation.tool.producers;

import org.sakaiproject.evaluation.tool.viewparams.AdhocGroupParams;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class ModifyAdhocGroupProducer implements ViewComponentProducer, ViewParamsReporter {
    public static final String VIEW_ID = "modify_adhoc_group";

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        
    }

    public String getViewID() {
        return VIEW_ID;
    }

    public ViewParameters getViewParameters() {
        return new AdhocGroupParams();
    }

}

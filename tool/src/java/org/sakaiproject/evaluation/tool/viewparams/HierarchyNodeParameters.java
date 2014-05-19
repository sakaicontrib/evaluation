package org.sakaiproject.evaluation.tool.viewparams;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class HierarchyNodeParameters extends SimpleViewParameters {
    public String nodeId;
    
    public HierarchyNodeParameters() {
    }
    
    public HierarchyNodeParameters(String viewID, String nodeId) {
        this.viewID = viewID;
        this.nodeId = nodeId;
    }
}

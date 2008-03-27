package org.sakaiproject.evaluation.tool.viewparams;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class AdhocGroupParams extends SimpleViewParameters {
    public Long adhocGroupId;
    
    public AdhocGroupParams() {};
    
    public AdhocGroupParams(String viewid, Long adhocGroupId) {
        this.viewID = viewid;
        this.adhocGroupId = adhocGroupId;
    }
}

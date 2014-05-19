package org.sakaiproject.evaluation.tool.producers;

import uk.org.ponder.json.support.JSONProvider;
import uk.org.ponder.rsf.content.ContentTypeInfoRegistry;
import uk.org.ponder.rsf.view.DataView;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * This page does not seem to actually do anything?
 * Probably should be deleted
 * TODO delete this producer
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class UserAvailableEvalsDataView implements DataView {
    public static final String VIEW_ID = "available_evals";
    private JSONProvider jsonProvider;

    public Object getData(ViewParameters viewparams) {
        String helloworld = "Hello world";
        return jsonProvider.toString(helloworld);
    }

    public String getContentType() {
        return ContentTypeInfoRegistry.JSON;
    }

    public String getViewID() {
        return VIEW_ID;
    }

    public void setJsonProvider(JSONProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
    }

}

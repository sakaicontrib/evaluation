/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
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

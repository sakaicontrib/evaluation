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
package org.sakaiproject.evaluation.tool.wrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Global safety net to ensure RSF never attempts to render a null view.
 * If a producer forgets to set result.resultingView, we fall back to the
 * incoming view parameters so the user stays on the same screen.
 */
public class DefaultViewInterceptor implements ActionResultInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultViewInterceptor.class);

    @Override
    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        if (incoming == null) {
            return;
        }

        if (result.resultingView == null) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Resulting view object was null (incoming view '{}'); defaulting back to incoming parameters.", incoming.viewID);
            }
            result.resultingView = incoming;
            return;
        }

        if (result.resultingView instanceof ViewParameters) {
            ViewParameters outgoing = (ViewParameters) result.resultingView;
            if (outgoing.viewID == null) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Resulting view ID was null (incoming view '{}'); copying incoming view ID.", incoming.viewID);
                }
                outgoing.viewID = incoming.viewID;
            }
        }
    }
}

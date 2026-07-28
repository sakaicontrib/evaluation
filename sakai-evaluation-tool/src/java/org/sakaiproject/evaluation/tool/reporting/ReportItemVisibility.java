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
package org.sakaiproject.evaluation.tool.reporting;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalEvaluation;

/**
 * Shared visibility rules for report views and exporters when instructors may
 * only see their own associated items.
 */
public final class ReportItemVisibility {

    private ReportItemVisibility() {
    }

    /**
     * Whether the viewer may see report items for this associate (instructor, TA, or course).
     *
     * @param evaluation the evaluation being reported
     * @param viewerIsAdmin whether the viewer is a system admin
     * @param viewerUserId internal user id of the viewer
     * @param associateType template item category (course/instructor/assistant)
     * @param associateUserId associate user id, or null when unset
     * @return true when the viewer may see this associate's items
     */
    public static boolean isVisibleToViewer(EvalEvaluation evaluation, boolean viewerIsAdmin,
            String viewerUserId, String associateType, String associateUserId) {
        if (evaluation.isInstructorViewAllResultsEnabled()
                || viewerIsAdmin
                || viewerUserId.equals(evaluation.getOwner())) {
            return true;
        }
        if (EvalConstants.ITEM_CATEGORY_COURSE.equals(associateType)) {
            return true;
        }
        if (associateUserId == null) {
            return true;
        }
        return viewerUserId.equals(associateUserId);
    }
}

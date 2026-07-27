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

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalEvaluation;

/**
 * Unit tests for {@link ReportItemVisibility}.
 */
public class ReportItemVisibilityTest {

    @Test
    public void isVisibleToViewer_hidesOtherInstructorWhenViewAllDisabled() {
        EvalEvaluation eval = evaluation(false);
        Assert.assertFalse(ReportItemVisibility.isVisibleToViewer(
                eval, false, "viewer", EvalConstants.ITEM_CATEGORY_INSTRUCTOR, "other-instructor"));
    }

    @Test
    public void isVisibleToViewer_showsCourseAndOwnItems() {
        EvalEvaluation eval = evaluation(false);
        Assert.assertTrue(ReportItemVisibility.isVisibleToViewer(
                eval, false, "viewer", EvalConstants.ITEM_CATEGORY_COURSE, "anything"));
        Assert.assertTrue(ReportItemVisibility.isVisibleToViewer(
                eval, false, "viewer", EvalConstants.ITEM_CATEGORY_INSTRUCTOR, "viewer"));
        Assert.assertTrue(ReportItemVisibility.isVisibleToViewer(
                eval, false, "viewer", EvalConstants.ITEM_CATEGORY_INSTRUCTOR, null));
    }

    @Test
    public void isVisibleToViewer_viewAllAdminOrOwnerSeeAll() {
        Assert.assertTrue(ReportItemVisibility.isVisibleToViewer(
                evaluation(true), false, "viewer", EvalConstants.ITEM_CATEGORY_INSTRUCTOR, "other"));
        Assert.assertTrue(ReportItemVisibility.isVisibleToViewer(
                evaluation(false), true, "viewer", EvalConstants.ITEM_CATEGORY_INSTRUCTOR, "other"));
        Assert.assertTrue(ReportItemVisibility.isVisibleToViewer(
                evaluation(false), false, "owner", EvalConstants.ITEM_CATEGORY_INSTRUCTOR, "other"));
    }

    @Test
    public void isVisibleToViewer_treatsNullViewAllAsFalse() {
        EvalEvaluation eval = evaluation(false);
        eval.setInstructorViewAllResults(null);
        Assert.assertFalse(eval.isInstructorViewAllResultsEnabled());
        Assert.assertFalse(ReportItemVisibility.isVisibleToViewer(
                eval, false, "viewer", EvalConstants.ITEM_CATEGORY_INSTRUCTOR, "other"));
    }

    private EvalEvaluation evaluation(boolean instructorViewAllResults) {
        EvalEvaluation eval = new EvalEvaluation();
        eval.setOwner("owner");
        eval.setInstructorViewAllResults(instructorViewAllResults);
        return eval;
    }
}

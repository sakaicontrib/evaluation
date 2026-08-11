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
package org.sakaiproject.evaluation.logic;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;

/**
 * Tests for {@link EvalCommonLogicImpl#countUserIdsForEvalGroups(java.util.Collection, String, Boolean)},
 * the bulk lookup added to replace calling countUserIdsForEvalGroup() once per group
 * (EVALSYS-1614 preprod perf investigation: the per-group loop took 60+ seconds for a user
 * with hundreds of assignable groups).
 */
public class EvalCommonLogicImplTest extends BaseTestEvalLogic {

    @Test
    public void testCountUserIdsForEvalGroupsMatchesPerGroupCalls() {
        Map<String, Integer> counts = commonLogic.countUserIdsForEvalGroups(
                Arrays.asList(EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF, EvalTestDataLoad.SITE7_REF),
                EvalConstants.PERM_TAKE_EVALUATION, Boolean.FALSE);

        Assert.assertNotNull(counts);
        Assert.assertEquals(3, counts.size());
        Assert.assertEquals(
                Integer.valueOf(commonLogic.countUserIdsForEvalGroup(EvalTestDataLoad.SITE1_REF, EvalConstants.PERM_TAKE_EVALUATION, Boolean.FALSE)),
                counts.get(EvalTestDataLoad.SITE1_REF));
        Assert.assertEquals(
                Integer.valueOf(commonLogic.countUserIdsForEvalGroup(EvalTestDataLoad.SITE2_REF, EvalConstants.PERM_TAKE_EVALUATION, Boolean.FALSE)),
                counts.get(EvalTestDataLoad.SITE2_REF));
        Assert.assertEquals(
                Integer.valueOf(commonLogic.countUserIdsForEvalGroup(EvalTestDataLoad.SITE7_REF, EvalConstants.PERM_TAKE_EVALUATION, Boolean.FALSE)),
                counts.get(EvalTestDataLoad.SITE7_REF));

        // known values from MockEvalExternalLogic.getUserIdsForEvalGroup(String, String)
        Assert.assertEquals(Integer.valueOf(1), counts.get(EvalTestDataLoad.SITE1_REF)); // USER_ID
        Assert.assertEquals(Integer.valueOf(2), counts.get(EvalTestDataLoad.SITE2_REF)); // USER_ID + STUDENT_USER_ID
        Assert.assertEquals(Integer.valueOf(1), counts.get(EvalTestDataLoad.SITE7_REF)); // USER_ID_5
    }

    @Test
    public void testCountUserIdsForEvalGroupsStillHasEntryWhenCountIsZero() {
        // SITE2_REF has no users with PERM_WRITE_TEMPLATE - the map must still contain the key with 0,
        // not omit it (the controller relies on getOrDefault(id, 0), but a missing key would be a silent bug elsewhere)
        Map<String, Integer> counts = commonLogic.countUserIdsForEvalGroups(
                Collections.singletonList(EvalTestDataLoad.SITE2_REF), EvalConstants.PERM_WRITE_TEMPLATE, Boolean.FALSE);

        Assert.assertEquals(1, counts.size());
        Assert.assertTrue(counts.containsKey(EvalTestDataLoad.SITE2_REF));
        Assert.assertEquals(Integer.valueOf(0), counts.get(EvalTestDataLoad.SITE2_REF));
    }

    @Test
    public void testCountUserIdsForEvalGroupsEmptyInput() {
        Map<String, Integer> counts = commonLogic.countUserIdsForEvalGroups(
                Collections.emptyList(), EvalConstants.PERM_TAKE_EVALUATION, Boolean.FALSE);

        Assert.assertNotNull(counts);
        Assert.assertTrue(counts.isEmpty());
    }

    @Test
    public void testCountUserIdsForEvalGroupsFallsBackToAdhocGroup() {
        // Adhoc groups are not known to the external logic, so the external bulk lookup returns 0 for
        // them; countUserIdsForEvalGroups() must fall back to the per-group resolution (same as
        // countUserIdsForEvalGroup()) instead of leaving them at 0.
        String adhocGroupId = etdl.group1.getEvalGroupId();

        int expected = commonLogic.countUserIdsForEvalGroup(adhocGroupId, EvalConstants.PERM_TAKE_EVALUATION, Boolean.FALSE);
        Assert.assertEquals(2, expected); // group1 participants: STUDENT_USER_ID, user1

        Map<String, Integer> counts = commonLogic.countUserIdsForEvalGroups(
                Arrays.asList(EvalTestDataLoad.SITE1_REF, adhocGroupId),
                EvalConstants.PERM_TAKE_EVALUATION, Boolean.FALSE);

        Assert.assertEquals(2, counts.size());
        Assert.assertEquals(Integer.valueOf(1), counts.get(EvalTestDataLoad.SITE1_REF));
        Assert.assertEquals(Integer.valueOf(expected), counts.get(adhocGroupId));
    }

}
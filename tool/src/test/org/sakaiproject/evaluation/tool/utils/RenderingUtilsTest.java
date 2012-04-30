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
package org.sakaiproject.evaluation.tool.utils;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils.AnswersMean;


/**
 * Testing for the render utils
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class RenderingUtilsTest {

    /**
     * Test method for {@link org.sakaiproject.evaluation.tool.utils.RenderingUtils#calculateMean(int[])}.
     */
    @Test
    public void testCalculateMean() {
        AnswersMean am = null;

        am = RenderingUtils.calculateMean(new int[] {1,1,1,1,1,0});
        assertNotNull(am);
        assertEquals(5, am.answersCount);
        assertEquals(3.0, am.mean, 0.0);
        assertNotNull(am.meanText);

        am = RenderingUtils.calculateMean(new int[] {5,4,3,2,1,0});
        assertNotNull(am);
        assertEquals(15, am.answersCount);
        assertEquals(2.33, am.mean, 0.1);
        assertNotNull(am.meanText);

        am = RenderingUtils.calculateMean(new int[0]);
        assertNotNull(am);
        assertEquals(0, am.answersCount);
        assertEquals(0.0, am.mean, 0.0);
        assertNotNull(am.meanText);

        try {
            am = RenderingUtils.calculateMean(null);
            fail("should have died");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

}

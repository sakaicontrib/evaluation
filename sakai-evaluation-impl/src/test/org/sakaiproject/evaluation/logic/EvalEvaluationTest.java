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

import org.sakaiproject.evaluation.logic.model.EvalReminderStatus;
import org.sakaiproject.evaluation.model.EvalEvaluation;

import junit.framework.TestCase;


/**
 * Testing reminder status handling or other evaluation methods
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EvalEvaluationTest extends TestCase {

    /**
     * Test method for {@link org.sakaiproject.evaluation.model.EvalEvaluation#getReminderStatus()}.
     */
    public void testReminderStatus() {
        EvalReminderStatus ers = null;

        ers = new EvalReminderStatus("50:5:abcde12345");
        assertNotNull(ers);
        assertEquals(50, ers.totalNum);
        assertEquals(5, ers.currentNum);
        assertEquals("abcde12345", ers.currentEvalGroupId);

        EvalEvaluation eval = new EvalEvaluation();
        ers = eval.getCurrentReminderStatus();
        assertNull(ers);

        eval.setReminderStatus(null);
        ers = eval.getCurrentReminderStatus();
        assertNull(ers);
        assertNull(eval.getReminderStatus());

        eval.setReminderStatus("50:5:abcde12345");
        ers = eval.getCurrentReminderStatus();
        assertNotNull(ers);
        assertEquals(50, ers.totalNum);
        assertEquals(5, ers.currentNum);
        assertEquals("abcde12345", ers.currentEvalGroupId);
        assertEquals("50:5:abcde12345", eval.getReminderStatus());
    }

}

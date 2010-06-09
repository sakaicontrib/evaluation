/**
 * $Id: EvalEvaluationTest.java 1000 Jun 9, 2010 11:33:12 AM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * EvalEvaluationTest.java - evaluation - Jun 9, 2010 11:33:12 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
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

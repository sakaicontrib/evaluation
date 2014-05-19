/**
 * $Id$
 * $URL$
 * RenderingUtilsTest.java - evaluation - Apr 3, 2009 12:01:18 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
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

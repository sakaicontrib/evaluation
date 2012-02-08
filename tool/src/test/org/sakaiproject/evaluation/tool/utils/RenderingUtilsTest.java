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

import java.util.List;
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
    
    @Test
    public void testGetMatrixLabels() {
    	String[] scaleOptions0 = {"a", "b"};
    	List<String> labels0 = RenderingUtils.getMatrixLabels(scaleOptions0);
    	assertEquals(2, labels0.size());
    	assertEquals("a", labels0.get(0));
    	assertEquals("b", labels0.get(1));
    	
    	String[] scaleOptions1 = {"a", "b", "c"};
    	List<String> labels1 = RenderingUtils.getMatrixLabels(scaleOptions1);
    	assertEquals(2, labels1.size());
    	assertEquals("a", labels1.get(0));
    	assertEquals("c", labels1.get(1));
    	
    	String[] scaleOptions2 = {"a", "b", "c", "d"};
    	List<String> labels2 = RenderingUtils.getMatrixLabels(scaleOptions2);
    	assertEquals(2, labels2.size());
    	assertEquals("a", labels2.get(0));
    	assertEquals("d", labels2.get(1));
    	
    	String[] scaleOptions3 = {"a", "b", "c", "d", "e"};
    	List<String> labels3 = RenderingUtils.getMatrixLabels(scaleOptions3);
    	assertEquals(3, labels3.size());
    	assertEquals("a", labels3.get(0));
    	assertEquals("e", labels3.get(1));
    	assertEquals("c", labels3.get(2));
    	
    	String[] scaleOptions4 = {"a", "b", "c", "d", "e", "f"};
    	List<String> labels4 = RenderingUtils.getMatrixLabels(scaleOptions4);
    	assertEquals(3, labels4.size());
    	assertEquals("a", labels4.get(0));
    	assertEquals("f", labels4.get(1));
    	assertEquals("c", labels4.get(2));
    	
    	String[] scaleOptions5 = {"a", "b", "c", "d", "e", "f", "g"};
    	List<String> labels5 = RenderingUtils.getMatrixLabels(scaleOptions5);
    	assertEquals(3, labels5.size());
    	assertEquals("a", labels5.get(0));
    	assertEquals("g", labels5.get(1));
    	assertEquals("d", labels5.get(2));
    }

}

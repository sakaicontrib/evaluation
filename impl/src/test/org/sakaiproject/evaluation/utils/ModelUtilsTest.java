/**
 * $Id$
 * $URL$
 * ModelUtilsTest.java - evaluation - Mar 21, 2009 11:58:44 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.utils;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalResponse;


/**
 * Testing the utility methods in the model
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ModelUtilsTest {

    /**
     * Test method for {@link org.sakaiproject.evaluation.model.EvalResponse#encodeSelections(java.util.Map)}.
     */
    @Test
    public void testEncodeSelections() {
        Map<String, String[]> m = null;

        String s = EvalResponse.encodeSelections(m);
        assertNull(s);
        
        m = new LinkedHashMap<String, String[]>();
        m.put("aaronz", new String[] {"A", "B", "C"});
        s = EvalResponse.encodeSelections(m);
        assertNotNull(s);
        assertEquals("{aaronz[A,B,C]}", s);

        m.put("instructor", new String[] {"becky", "minerva"});
        s = EvalResponse.encodeSelections(m);
        assertNotNull(s);
        assertEquals("{aaronz[A,B,C]}{instructor[becky,minerva]}", s);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.model.EvalResponse#decodeSelections(java.lang.String)}.
     */
    @Test
    public void testDecodeSelections() {
        String s1 = "";
        String s2 = "{aaronz[A,B,C]}";
        String s3 = "{aaronz[A,B,C]}{instructor[becky,minerva]}";

        Map<String, String[]> m = EvalResponse.decodeSelections(s1);
        assertNotNull(m);
        assertEquals(0, m.size());

        m = EvalResponse.decodeSelections(s2);
        assertNotNull(m);
        assertEquals(1, m.size());
        assertArrayEquals(new String[] {"A", "B", "C"}, m.get("aaronz"));

        m = EvalResponse.decodeSelections(s3);
        assertNotNull(m);
        assertEquals(2, m.size());
        assertArrayEquals(new String[] {"A", "B", "C"}, m.get("aaronz"));
        assertArrayEquals(new String[] {"becky", "minerva"}, m.get("instructor"));
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.model.EvalResponse#getSelections()}.
     */
    @Test
    public void testGetSelections() {
        EvalResponse response = new EvalResponse();
        Map<String, String[]> m = response.getSelections();
        assertNotNull(m);
        assertEquals(0, m.size());

        response.setSelectionsCode("{aaronz[A,B,C]}");
        m = response.getSelections();
        assertNotNull(m);
        assertEquals(1, m.size());
        assertArrayEquals(new String[] {"A", "B", "C"}, m.get("aaronz"));
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.model.EvalResponse#setSelections(java.lang.String, java.lang.String[])}.
     */
    @Test
    public void testSetSelections() {
        EvalResponse response = new EvalResponse();
        response.setSelections(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, new String[] {"aaronz","becky"});
        Map<String, String[]> m = response.getSelections();
        assertNotNull(m);
        assertEquals(1, m.size());
        assertArrayEquals(new String[] {"aaronz","becky"}, m.get(EvalConstants.ITEM_CATEGORY_INSTRUCTOR));

        response.setSelections(EvalConstants.ITEM_CATEGORY_ASSISTANT, new String[] {"minerva"});
        m = response.getSelections();
        assertNotNull(m);
        assertEquals(2, m.size());
        assertArrayEquals(new String[] {"aaronz","becky"}, m.get(EvalConstants.ITEM_CATEGORY_INSTRUCTOR));
        assertArrayEquals(new String[] {"minerva"}, m.get(EvalConstants.ITEM_CATEGORY_ASSISTANT));

        response.setSelections(EvalConstants.ITEM_CATEGORY_ASSISTANT, null);
        m = response.getSelections();
        assertNotNull(m);
        assertEquals(1, m.size());
        assertArrayEquals(new String[] {"aaronz","becky"}, m.get(EvalConstants.ITEM_CATEGORY_INSTRUCTOR));

        response.setSelections(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, null);
        m = response.getSelections();
        assertNotNull(m);
        assertEquals(0, m.size());        
    }

}

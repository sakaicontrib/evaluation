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
import org.sakaiproject.evaluation.model.EvalAssignGroup;
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
        m.put(EvalAssignGroup.SELECTION_TYPE_ASSISTANT, new String[] {"A", "B", "C"});
        s = EvalResponse.encodeSelections(m);
        assertNotNull(s);
        assertEquals("{"+EvalAssignGroup.SELECTION_TYPE_ASSISTANT+"[A,B,C]}", s);

        m.put(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR, new String[] {"becky", "minerva"});
        s = EvalResponse.encodeSelections(m);
        assertNotNull(s);
        assertEquals("{"+EvalAssignGroup.SELECTION_TYPE_ASSISTANT+"[A,B,C]}{"+EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR+"[becky,minerva]}", s);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.model.EvalResponse#decodeSelections(java.lang.String)}.
     */
    @Test
    public void testDecodeSelections() {
        String s1 = "";
        String s2 = "{"+EvalAssignGroup.SELECTION_TYPE_ASSISTANT+"[A,B,C]}";
        String s3 = "{"+EvalAssignGroup.SELECTION_TYPE_ASSISTANT+"[A,B,C]}{"+EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR+"[becky,minerva]}";

        Map<String, String[]> m = EvalResponse.decodeSelections(s1);
        assertNotNull(m);
        assertEquals(0, m.size());

        m = EvalResponse.decodeSelections(s2);
        assertNotNull(m);
        assertEquals(1, m.size());
        assertArrayEquals(new String[] {"A", "B", "C"}, m.get(EvalAssignGroup.SELECTION_TYPE_ASSISTANT));

        m = EvalResponse.decodeSelections(s3);
        assertNotNull(m);
        assertEquals(2, m.size());
        assertArrayEquals(new String[] {"A", "B", "C"}, m.get(EvalAssignGroup.SELECTION_TYPE_ASSISTANT));
        assertArrayEquals(new String[] {"becky", "minerva"}, m.get(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR));

        try {
            m = EvalResponse.decodeSelections("XXXX");
            fail("should have failed");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
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

        response.setSelectionsCode("{"+EvalAssignGroup.SELECTION_TYPE_ASSISTANT+"[A,B,C]}");
        m = response.getSelections();
        assertNotNull(m);
        assertEquals(1, m.size());
        assertArrayEquals(new String[] {"A", "B", "C"}, m.get(EvalAssignGroup.SELECTION_TYPE_ASSISTANT));
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.model.EvalResponse#setSelections(java.lang.String, java.lang.String[])}.
     */
    @Test
    public void testSetSelections() {
        EvalResponse response = new EvalResponse();
        response.setSelections(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR, new String[] {"aaronz","becky"});
        Map<String, String[]> m = response.getSelections();
        assertNotNull(m);
        assertEquals(1, m.size());
        assertArrayEquals(new String[] {"aaronz","becky"}, m.get(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR));

        response.setSelections(EvalAssignGroup.SELECTION_TYPE_ASSISTANT, new String[] {"minerva"});
        m = response.getSelections();
        assertNotNull(m);
        assertEquals(2, m.size());
        assertArrayEquals(new String[] {"aaronz","becky"}, m.get(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR));
        assertArrayEquals(new String[] {"minerva"}, m.get(EvalAssignGroup.SELECTION_TYPE_ASSISTANT));

        response.setSelections(EvalAssignGroup.SELECTION_TYPE_ASSISTANT, null);
        m = response.getSelections();
        assertNotNull(m);
        assertEquals(1, m.size());
        assertArrayEquals(new String[] {"aaronz","becky"}, m.get(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR));

        response.setSelections(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR, null);
        m = response.getSelections();
        assertNotNull(m);
        assertEquals(0, m.size());        
    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.model.EvalAssignGroup#validateSelectionOption(java.lang.String)}.
     */
    @Test
    public void testValidateSelectionOption() {
        EvalAssignGroup.validateSelectionOption(EvalAssignGroup.SELECTION_OPTION_MULTIPLE);
        EvalAssignGroup.validateSelectionOption(EvalAssignGroup.SELECTION_OPTION_ONE);
        try {
            EvalAssignGroup.validateSelectionOption("XXXX");
            fail("should have died");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.model.EvalAssignGroup#validateSelectionType(java.lang.String)}.
     */
    @Test
    public void testValidateSelectionType() {
        EvalAssignGroup.validateSelectionType(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR);
        EvalAssignGroup.validateSelectionType(EvalAssignGroup.SELECTION_TYPE_ASSISTANT);
        try {
            EvalAssignGroup.validateSelectionType("XXXXX");
            fail("should have died");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.model.EvalAssignGroup#encodeSelectionSettings(java.util.Map)}.
     */
    @Test
    public void testEncodeSelectionSettings() {
        Map<String, String> m = null;
        String s1 = "{"+EvalAssignGroup.SELECTION_TYPE_ASSISTANT+":"+EvalAssignGroup.SELECTION_OPTION_MULTIPLE+"}";
        String s2 = "{"+EvalAssignGroup.SELECTION_TYPE_ASSISTANT+":"+EvalAssignGroup.SELECTION_OPTION_MULTIPLE+"}{"+EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR+":"+EvalAssignGroup.SELECTION_OPTION_ONE+"}";

        String s = EvalAssignGroup.encodeSelectionSettings(m);
        assertNull(s);
        
        m = new LinkedHashMap<String, String>();
        m.put(EvalAssignGroup.SELECTION_TYPE_ASSISTANT, EvalAssignGroup.SELECTION_OPTION_MULTIPLE);
        s = EvalAssignGroup.encodeSelectionSettings(m);
        assertNotNull(s);
        assertEquals(s1, s);

        m.put(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR, EvalAssignGroup.SELECTION_OPTION_ONE);
        s = EvalAssignGroup.encodeSelectionSettings(m);
        assertNotNull(s);
        assertEquals(s2, s);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.model.EvalAssignGroup#decodeSelectionSettings(java.lang.String)}.
     */
    @Test
    public void testDecodeSelectionSettings() {
        Map<String, String> m = null;
        String s1 = "{"+EvalAssignGroup.SELECTION_TYPE_ASSISTANT+":"+EvalAssignGroup.SELECTION_OPTION_MULTIPLE+"}";
        String s2 = "{"+EvalAssignGroup.SELECTION_TYPE_ASSISTANT+":"+EvalAssignGroup.SELECTION_OPTION_MULTIPLE+"}{"+EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR+":"+EvalAssignGroup.SELECTION_OPTION_ONE+"}";

        m = EvalAssignGroup.decodeSelectionSettings("");
        assertNotNull(m);
        assertEquals(0, m.size());

        m = EvalAssignGroup.decodeSelectionSettings(s1);
        assertNotNull(m);
        assertEquals(1, m.size());
        assertEquals(EvalAssignGroup.SELECTION_OPTION_MULTIPLE, m.get(EvalAssignGroup.SELECTION_TYPE_ASSISTANT));
        
        m = EvalAssignGroup.decodeSelectionSettings(s2);
        assertNotNull(m);
        assertEquals(2, m.size());
        assertEquals(EvalAssignGroup.SELECTION_OPTION_MULTIPLE, m.get(EvalAssignGroup.SELECTION_TYPE_ASSISTANT));
        assertEquals(EvalAssignGroup.SELECTION_OPTION_ONE, m.get(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR));

        try {
            m = EvalAssignGroup.decodeSelectionSettings("XXXX");
            fail("should have failed");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.model.EvalAssignGroup#getSelectionOptions()}.
     */
    @Test
    public void testGetSelectionOptions() {
        Map<String, String> m = null;
        EvalAssignGroup eag = new EvalAssignGroup();
        String s1 = "{"+EvalAssignGroup.SELECTION_TYPE_ASSISTANT+":"+EvalAssignGroup.SELECTION_OPTION_MULTIPLE+"}";
        String s2 = "{"+EvalAssignGroup.SELECTION_TYPE_ASSISTANT+":"+EvalAssignGroup.SELECTION_OPTION_MULTIPLE+"}{"+EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR+":"+EvalAssignGroup.SELECTION_OPTION_ONE+"}";

        eag.setSelectionSettings(null);
        m = eag.getSelectionOptions();
        assertNotNull(m);
        assertEquals(0, m.size());

        eag.setSelectionSettings(s1);
        m = eag.getSelectionOptions();
        assertNotNull(m);
        assertEquals(1, m.size());
        assertEquals(EvalAssignGroup.SELECTION_OPTION_MULTIPLE, m.get(EvalAssignGroup.SELECTION_TYPE_ASSISTANT));
        
        eag.setSelectionSettings(s2);
        m = eag.getSelectionOptions();
        assertNotNull(m);
        assertEquals(2, m.size());
        assertEquals(EvalAssignGroup.SELECTION_OPTION_MULTIPLE, m.get(EvalAssignGroup.SELECTION_TYPE_ASSISTANT));
        assertEquals(EvalAssignGroup.SELECTION_OPTION_ONE, m.get(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR));
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.model.EvalAssignGroup#setSelectionOption(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testSetSelectionOption() {
        Map<String, String> m = null;
        EvalAssignGroup eag = new EvalAssignGroup();

        m = eag.getSelectionOptions();
        assertNotNull(m);
        assertEquals(0, m.size());

        eag.setSelectionOption(EvalAssignGroup.SELECTION_TYPE_ASSISTANT, EvalAssignGroup.SELECTION_OPTION_MULTIPLE);
        m = eag.getSelectionOptions();
        assertNotNull(m);
        assertEquals(1, m.size());
        assertEquals(EvalAssignGroup.SELECTION_OPTION_MULTIPLE, m.get(EvalAssignGroup.SELECTION_TYPE_ASSISTANT));
        
        eag.setSelectionOption(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR, EvalAssignGroup.SELECTION_OPTION_ONE);
        m = eag.getSelectionOptions();
        assertNotNull(m);
        assertEquals(2, m.size());
        assertEquals(EvalAssignGroup.SELECTION_OPTION_MULTIPLE, m.get(EvalAssignGroup.SELECTION_TYPE_ASSISTANT));
        assertEquals(EvalAssignGroup.SELECTION_OPTION_ONE, m.get(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR));

        eag.setSelectionOption(EvalAssignGroup.SELECTION_TYPE_ASSISTANT, EvalAssignGroup.SELECTION_OPTION_ONE);
        m = eag.getSelectionOptions();
        assertNotNull(m);
        assertEquals(2, m.size());
        assertEquals(EvalAssignGroup.SELECTION_OPTION_ONE, m.get(EvalAssignGroup.SELECTION_TYPE_ASSISTANT));
        assertEquals(EvalAssignGroup.SELECTION_OPTION_ONE, m.get(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR));

        eag.setSelectionOption(EvalAssignGroup.SELECTION_TYPE_ASSISTANT, null);
        m = eag.getSelectionOptions();
        assertNotNull(m);
        assertEquals(1, m.size());
        assertEquals(EvalAssignGroup.SELECTION_OPTION_ONE, m.get(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR));

        eag.setSelectionOption(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR, null);
        m = eag.getSelectionOptions();
        assertNotNull(m);
        assertEquals(0, m.size());

        // http://jira.sakaiproject.org/jira/browse/EVALSYS-685
        eag.setSelectionOption(EvalAssignGroup.SELECTION_TYPE_ASSISTANT, EvalAssignGroup.SELECTION_OPTION_MULTIPLE);
        eag.setSelectionOption(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR, EvalAssignGroup.SELECTION_OPTION_ALL);
        m = eag.getSelectionOptions();
        assertNotNull(m);
        assertEquals(1, m.size());
        assertEquals(EvalAssignGroup.SELECTION_OPTION_MULTIPLE, m.get(EvalAssignGroup.SELECTION_TYPE_ASSISTANT));

        eag.setSelectionOption(EvalAssignGroup.SELECTION_TYPE_ASSISTANT, EvalAssignGroup.SELECTION_OPTION_ALL);
        m = eag.getSelectionOptions();
        assertNotNull(m);
        assertEquals(0, m.size());
    }

}

/**
 * SettingsWBL.java - evaluation - 2008 Oct 21, 2007 11:35:56 AM - antranig
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.locators;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.utils.SettingsLogicUtils;

import uk.org.ponder.beanutil.WriteableBeanLocator;
import uk.org.ponder.conversion.GeneralLeafParser;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

/**
 * Obviates the need for a backing bean for administrative functionality 
 * (this is a WritableBeanLocator). 
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Kapil Ahuja (kahuja@caret.cam.ac.uk)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class SettingsWBL implements WriteableBeanLocator {
    public static final String NEW_PREFIX = "new";
    public static String NEW_1 = NEW_PREFIX +"1";

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }

    private GeneralLeafParser leafParser;
    public void setLeafParser(GeneralLeafParser leafParser) {
        this.leafParser = leafParser;
    }

    private TargettedMessageList messages;
    public void setMessages(TargettedMessageList messages) {
        this.messages = messages;
    }

    /**
     * Simply tells the user that remove functionality is not 
     * supported (this is done by throwing an exception).
     * 
     * @param beanname -  Name of the property that has to be removed    
     * @return throws a new UnsupportedOperationException exception
     */
    public boolean remove(String beanname) {
        throw new UnsupportedOperationException("Removal not supported from SettingsWBL");
    }

    /**
     * Sets the data from producer to EvalSettings (database). 
     * 
     * @param beanname -  Name of the property to be set    
     * @param toset -  Value of the property to be set    
     */
    public void set(String beanname, Object toset) {
        /*
         * Note: If the value of field is null it means that this particular value should be
         * deleted from the table. 
         */ 
        if ( isTernaryBoolean(beanname) ) {
            if ( ((String)toset).equals(EvalToolConstants.ADMIN_BOOLEAN_CONFIGURABLE) ) 
                toset = null;
            else if ( ((String)toset).equals(EvalToolConstants.ADMIN_BOOLEAN_YES) ) 
                toset = Boolean.TRUE;
            else if ( ((String)toset).equals(EvalToolConstants.ADMIN_BOOLEAN_NO) ) 
                toset = Boolean.FALSE;
            else {
                throw new IllegalStateException("Invalid value for this ternary boolean: " + toset);
            }
        } else {
            /*
             *  The UI has already converted Booleans.
             *  This is primarily to catch Integers and Strings. 
             */
            if (toset instanceof String) {
                if ( EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE.equals(beanname) && 
                        ((String)toset).equals(EvalToolConstants.ADMIN_BOOLEAN_CONFIGURABLE) ) {
                    // special handling for 4 part select
                    toset = null;
                } else {
                    Class<?> proptype = SettingsLogicUtils.getTypeClass(beanname);
                    toset = leafParser.parse(proptype, (String) toset);
                }
            }
        }
        evalSettings.set(beanname, toset);
    }

    /**
     * Gets the data from EvalSettings (database) and returns to producer. 
     * 
     * @param path -  Name of the property whose value has to be fetched 
     * 				  from database
     * @return Value of the property obtained from database
     */
    public Object locateBean(String path) {
        Object toget = evalSettings.get(path);
        /*
         * Fields inside isFieldToBeParsed are not directly mapped to those in database.
         * Thus parsing them.  
         */
        if ( isTernaryBoolean(path) ) {
            if (toget == null)
                toget = EvalToolConstants.ADMIN_BOOLEAN_CONFIGURABLE;
            else if (toget instanceof Boolean) {
                if ( ((Boolean)toget).booleanValue() ) {
                    toget = EvalToolConstants.ADMIN_BOOLEAN_YES;
                } else { 
                    toget = EvalToolConstants.ADMIN_BOOLEAN_NO;
                }
            } else {
                throw new IllegalStateException("Invalid value for this ternary boolean: " + toget);
            }
        }

        if (toget == null) {
            // this workaround is here for the DARApplier issue of handling nulls, should be fixed in 0.7.3
            toget = "";
        }
        return toget;
    }

    /**
     * Checks to see if a field is a ternary Boolean, that is to say
     * it can store true, false, and "configurable"
     * @param path
     * @return true is this is ternary
     */
    private boolean isTernaryBoolean(String path) {
        boolean isTernary = false;
        for (int i = 0; i < EvalSettings.TERNARY_BOOLEAN_SETTINGS.length; i++) {
            if (EvalSettings.TERNARY_BOOLEAN_SETTINGS[i].equals(path)) {
                isTernary = true;
                break;
            }
        }
        return isTernary;
    }

    /**
     * Clears the config settings cache
     * @return simple string to indicate success
     */
    public String resetConfigCache() {
        evalSettings.resetCache(null);
        messages.addMessage(new TargettedMessage("administrate.reset.message",
                null, TargettedMessage.SEVERITY_INFO));
        return "success";
    }

}

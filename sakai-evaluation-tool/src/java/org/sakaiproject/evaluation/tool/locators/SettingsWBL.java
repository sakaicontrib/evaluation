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

    protected EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }

    protected GeneralLeafParser leafParser;
    public void setLeafParser(GeneralLeafParser leafParser) {
        this.leafParser = leafParser;
    }

    protected TargettedMessageList messages;
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
                throw new IllegalStateException("Invalid value for " + beanname + " and this ternary boolean: " + toset);
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
                } 
               
                else {
                    Class<?> proptype = SettingsLogicUtils.getTypeClass(beanname);
                    toset = leafParser.parse(proptype, (String) toset);
                }
            }
            else if (toset instanceof Integer) {
                if (EvalSettings.EVAL_MIN_LIST_LENGTH.equals(beanname)) {
                    Integer evalMax = (Integer) evalSettings.get(EvalSettings.EVAL_MAX_LIST_LENGTH);
                    if (evalMax == null)
                        evalMax = 40;

                    int evalMin = (Integer) toset;
                    if (evalMin < 1)
                        toset = 1;
                    else if (evalMin > evalMax)
                        toset = evalMax;
                }
                else if (EvalSettings.EVAL_MAX_LIST_LENGTH.equals(beanname)) {
                    Integer evalMin = (Integer) evalSettings.get(EvalSettings.EVAL_MIN_LIST_LENGTH);
                    if (evalMin == null)
                        evalMin = 1;

                    int evalMax = (Integer) toset;
                    if (evalMax < evalMin) 
                        toset = evalMin;
                    
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
                if ( ((Boolean)toget) ) {
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
        for( String TERNARY_BOOLEAN_SETTINGS : EvalSettings.TERNARY_BOOLEAN_SETTINGS )
        {
            if( TERNARY_BOOLEAN_SETTINGS.equals( path ) )
            {
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

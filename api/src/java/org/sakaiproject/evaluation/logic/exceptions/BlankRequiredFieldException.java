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
package org.sakaiproject.evaluation.logic.exceptions;


/**
 * This exception indicates that a required field is blank
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class BlankRequiredFieldException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * This is the name of property/field which is blank
     */
    public String fieldName;
    /**
     * This is the message key used to tell the user what field they must fill in,
     * {0} should be the fieldname
     */
    public String messageKey = "general.blank.required.field.user.message";

    public BlankRequiredFieldException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    public BlankRequiredFieldException(String message, String fieldName, String messageKey) {
        super(message);
        this.fieldName = fieldName;
        this.messageKey = messageKey;
    }

}

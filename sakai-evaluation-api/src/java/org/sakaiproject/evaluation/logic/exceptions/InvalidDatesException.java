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
 * Thrown when the dates for an evaluation are invalid in some way
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class InvalidDatesException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * The date field which is invalid
     */
    public String dateField;

    public String messageKey = "evalsettings.invalid.dates";

    public InvalidDatesException(String message, String dateField) {
        super(message);
        this.dateField = dateField;
    }

    public InvalidDatesException(String message, String dateField, String messageKey) {
        super(message);
        this.dateField = dateField;
        this.messageKey = messageKey;
    }   

}

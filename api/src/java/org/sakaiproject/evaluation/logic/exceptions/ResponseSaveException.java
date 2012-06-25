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
 * Exception thrown if something fails while attempting to save a user response
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ResponseSaveException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public static String TYPE_MISSING_REQUIRED_ANSWERS = "missingRequiredAnswers";
    public static String TYPE_CANNOT_TAKE_EVAL = "userCannotTakeEval";
    public static String TYPE_BLANK_RESPONSE = "blankResponse";
    public static String TYPE_CANNOT_SAVE = "cannotSave";

    public String type;
    public String[] missingItemAnswerKeys;

    public ResponseSaveException(String message, String type) {
        this(message, type, null);
    }

    /**
     * Constructor for error where we are missing required answers
     * 
     * @param message the message
     * @param missingItemAnswerKeys an array of itemAnswerKeys
     */
    public ResponseSaveException(String message, String[] missingItemAnswerKeys) {
        this(message, null, missingItemAnswerKeys);
    }

    /**
     * Constructor to use when we are missing required keys
     * 
     * @param message
     * @param type (OPTIONAL) if null then {@link #TYPE_MISSING_REQUIRED_ANSWERS} is used
     * @param missingItemAnswerKeys an array of itemAnswerKeys
     */
    public ResponseSaveException(String message, String type, String[] missingItemAnswerKeys) {
        super(message);
        if (type == null || "".equals(type)) {
            type = TYPE_MISSING_REQUIRED_ANSWERS;
        }
        this.type = type;
        this.missingItemAnswerKeys = missingItemAnswerKeys;
    }

}

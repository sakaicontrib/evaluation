/**
 * $Id$
 * $URL$
 * ResponseSaveException.java - evaluation - Mar 14, 2008 8:48:49 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.exceptions;


/**
 * Exception thrown if something fails while attempting to save a user response
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ResponseSaveException extends RuntimeException {

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

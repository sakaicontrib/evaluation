/**
 * $Id$
 * $URL$
 * InvalidDatesException.java - evaluation - Mar 20, 2008 3:48:38 PM - azeckoski
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

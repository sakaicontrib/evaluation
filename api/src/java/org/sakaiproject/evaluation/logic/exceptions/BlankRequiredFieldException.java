/**
 * $Id$
 * $URL$
 * BlankRequiredFieldException.java - evaluation - Mar 20, 2008 2:09:33 PM - azeckoski
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

/**
 * $Id: EvalEmailMessage.java 1000 Jun 10, 2010 12:26:47 PM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * EvalEmailMessage.java - evaluation - Jun 10, 2010 12:26:47 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.model;


/**
 * This is just a holder for data
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EvalEmailMessage {

    public String subjectTemplate;
    public String messageTemplate;
    public String subject;
    public String message;

    public EvalEmailMessage(String subjectTemplate, String messageTemplate, String subject,
            String message) {
        this.subjectTemplate = subjectTemplate;
        this.messageTemplate = messageTemplate;
        this.subject = subject;
        this.message = message;
    }

}

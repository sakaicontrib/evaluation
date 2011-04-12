/**
 * $Id$
 * $URL$
 * ExternalEmail.java - evaluation - May 12, 2008 10:40:37 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.externals;

/**
 * Handles emailing
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ExternalEmail {

    // EMAIL

    /**
     * Send emails to a set of email addresses (can send to a single address
     * by specifying an array with one item only)
     * NOTE: Use {@link #sendEmailsToUsers(String, String[], String, String, boolean)} if you know who the users are
     * 
     * @param from the email address this email appears to come from
     * @param to the email address(es) this message should be sent to
     * @param subject the message subject
     * @param message the message to send
     * @param deferExceptions if true, then exceptions are logged and then thrown after sending as many emails as possible,
     * if false then exceptions are thrown immediately
     * @return an array of email addresses that this message was sent to
     * @throws IllegalArgumentException if necessary params are not included
     */
    public String[] sendEmailsToAddresses(String from, String[] to, String subject, String message, boolean deferExceptions);

}

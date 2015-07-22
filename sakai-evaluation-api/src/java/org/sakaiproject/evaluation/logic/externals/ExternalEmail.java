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

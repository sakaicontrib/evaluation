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

import java.io.Serializable;

/**
 * A message which is meant to be seen by a user,
 * this is formed from an I18n message key and optional data
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class UserMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Indicates that this is an informational message
     */
    public static final int SEVERITY_INFO = 0;
    /**
     * Indicates that this is an error message
     */
    public static final int SEVERITY_ERROR = 1;

    /**
     * A list of possible I18n message keys, in descending order of specificity<br/> 
     * This is the same semantics as Spring's "MessageSourceResolvable" system
     */
    private String[] messageKeys;
    /**
     * data to fill in the message fields (i.e. the {0} in the message itself), 
     * leave this null or empty array if there are none
     */
    private Object[] args = null;
    /**
     * The associated exception if there is one
     */
    private Exception exception;
    /**
     * a constant to indicate if this message is informational or error
     */
    private int severity = SEVERITY_ERROR;

    public UserMessage() {
    }

    /**
     * Create an error message for a user, 
     * this is the simplest constructor
     * 
     * @param messageKey an I18n message key from a properties file
     */
    public UserMessage(String messageKey) {
        updateMessageKey(messageKey);
    }

    /**
     * Create an error message for a user
     * 
     * @param messageKey an I18n message key from a properties file
     * @param exception the exception which is associated with this error message
     */
    public UserMessage(String messageKey, Exception exception) {
        updateMessageKey(messageKey);
        this.exception = exception;
    }

    /**
     * Create an error message for a user with variable data
     * 
     * @param messageKey an I18n message key from a properties file
     * @param args the data to fill in the arguments in the message (e.g. {0})
     */
    public UserMessage(String messageKey, Object[] args) {
        updateMessageKey(messageKey);
        this.args = args;
    }

    /**
     * Create an error message for a user with variable data
     * 
     * @param messageKey an I18n message key from a properties file
     * @param args the data to fill in the arguments in the message (e.g. {0})
     * @param exception the exception which is associated with this error message
     */
    public UserMessage(String messageKey, Object[] args, Exception exception) {
        updateMessageKey(messageKey);
        this.args = args;
        this.exception = exception;
    }

    /**
     * Create an informational or other type of message for a user with variable data
     * 
     * @param messageKey an I18n message key from a properties file
     * @param args the data to fill in the arguments in the message (e.g. {0})
     * @param severity a SEVERITY_* constant to indicate if this message is informational or error
     */
    public UserMessage(String messageKey, Object[] args, int severity) {
        updateMessageKey(messageKey);
        this.args = args;
        this.severity = severity;
    }



    /**
     * Reset the array of message keys to contain a single key
     * 
     * @param messagecode a message key to make the solitary key in the array
     */
    public void updateMessageKey(String messagecode) {
        messageKeys = new String[] { messagecode };
    }

    /**
     * Get the message key
     * 
     * @return the most detailed message key from the array of keys
     */
    public String getMessageKey() {
        return messageKeys == null ? null
                : messageKeys[messageKeys.length - 1];
    }


    /*
     * GETTERS AND SETTERS
     */

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public int getSeverity() {
        return severity;
    }

    /**
     * Set this message to be informational or an error message
     * 
     * @param severity a SEVERITY_* constant to indicate if this message is informational or error
     */
    public void setSeverity(int severity) {
        if (SEVERITY_ERROR == severity ||
                SEVERITY_INFO == severity) {
            this.severity = severity;
        }
    }

    public String[] getMessageKeys() {
        return messageKeys;
    }

    public void setMessageKeys(String[] messageKeys) {
        this.messageKeys = messageKeys;
    }

}


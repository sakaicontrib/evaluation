/**
 * $Id$
 * $URL$
 * EvalGroup.java - evaluation - Dec 24, 2006 12:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.model;

import java.io.Serializable;
import java.util.Comparator;

import org.sakaiproject.evaluation.constant.EvalConstants;


/**
 * This pea represents an evalUser (could be internal or external),
 * this is a user in the evaluation system
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalUser implements Serializable {

    public static final String USER_TYPE_UNKNOWN = EvalConstants.USER_TYPE_UNKNOWN;
    public static final String USER_TYPE_ANONYMOUS = EvalConstants.USER_TYPE_ANONYMOUS;
    public static final String USER_TYPE_EXTERNAL = EvalConstants.USER_TYPE_EXTERNAL;
    public static final String USER_TYPE_INTERNAL = EvalConstants.USER_TYPE_INTERNAL;
    public static final String USER_TYPE_INVALID = EvalConstants.USER_TYPE_INVALID;

    /**
     * The string which is the unique identifier for this user
     */
    public String userId;
    /**
     * The string which is the username (eid) for this user
     * or default text "------" if it cannot be found
     */
    public String username;
    /**
     * The email address for this user if they have one,
     * null if they do not have an email address
     */
    public String email;
    /**
     * The displayable name of this user
     * or default text "--------" if it cannot be found
     */
    public String displayName;
    /**
     * The sort name of this user
     * or defaults to username if it cannot be found
     */
    public String sortName;
    /**
     * The type of this user (use the USER_TYPE constants in {@link EvalConstants})
     */
    public String type = EvalConstants.USER_TYPE_UNKNOWN;

    /**
     * Empty Constructor
     */
    public EvalUser() {}

    /**
     * Minimal constructor
     * 
     * @param userId the internal user id (not username)
     * @param type the type of this user (use the USER_TYPE constants in {@link EvalConstants})
     * @param email email address for this user if they have one
     */
    public EvalUser(String userId, String type, String email) {
        this(userId, type, email, null, null, null);
    }

    /**
     * @param userId the internal user id (not username)
     * @param type the type of this user (use the USER_TYPE constants in {@link EvalConstants})
     * @param email email address for this user if they have one
     * @param username the login name (eid) for the user or default text "------" if it cannot be found
     * @param displayName the user display name or default text "--------" if it cannot be found
     */
    public EvalUser(String userId, String type, String email, String username, String displayName) {
        this(userId, type, email, username, displayName, null);
    }

    /**
     * Full constructor
     * 
     * @param userId the internal user id (not username)
     * @param type the type of this user (use the USER_TYPE constants in {@link EvalConstants})
     * @param email email address for this user if they have one
     * @param username the login name (eid) for the user or default text "------" if it cannot be found
     * @param displayName the user display name or default text "--------" if it cannot be found
     * @param sortName the name to use when sorting users or defaults to username if none set
     */
    public EvalUser(String userId, String type, String email, String username, String displayName, String sortName) {
        if (userId == null || "".equals(userId)) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (type == null || "".equals(type)) {
            throw new IllegalArgumentException("type cannot be null");
        }
        this.userId = userId;
        this.type = type;
        this.email = email;
        if (username != null && ! "".equals(username)) {
            this.username = username;
        } else {
            this.username = "------";
        }
        if (displayName != null && ! "".equals(displayName)) {
            this.displayName = displayName;
        } else {
            this.displayName = "--------";
        }
        if (sortName != null && ! "".equals(sortName)) {
            this.sortName = sortName;
        } else {
            this.sortName = username != null ? username : userId;
        }
    }

    @Override
    public String toString() {
        return this.userId + ":" + this.username + ":" + this.email;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EvalUser other = (EvalUser) obj;
        if (userId == null) {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }

    public static class SortNameComparator implements Comparator<EvalUser> {
        public static final long serialVersionUID = 31L;
        public int compare(EvalUser o1, EvalUser o2) {
            return o1.sortName.compareTo(o2.sortName);
        }
    }

}

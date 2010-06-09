/**
 * $Id: ReminderStatus.java 1000 Jun 9, 2010 11:44:08 AM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * ReminderStatus.java - evaluation - Jun 9, 2010 11:44:08 AM - azeckoski
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

import org.sakaiproject.evaluation.utils.EvalUtils;

/**
 * This is just a cute little class to help with the handling of the reminder status coded data
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EvalReminderStatus {
    public int totalNum;
    public int getTotalNum() {
        return totalNum;
    }
    public int currentNum;
    public int getCurrentNum() {
        return currentNum;
    }
    public String currentEvalGroupId;
    public String getCurrentEvalGroupId() {
        return currentEvalGroupId;
    }
    public EvalReminderStatus(int totalNum, int currentNum, String currentEvalGroupId) {
        if (totalNum <= 0) {
            throw new IllegalArgumentException("totalNum must be > 0");
        }
        if (currentNum <= 0) {
            throw new IllegalArgumentException("currentNum must be > 0");
        }
        if (EvalUtils.isBlank(currentEvalGroupId)) {
            throw new IllegalArgumentException("currentEvalGroupId must be set");
        }
        this.totalNum = totalNum;
        this.currentNum = currentNum;
        this.currentEvalGroupId = currentEvalGroupId;
    }
    public EvalReminderStatus(String reminderStatusCoded) {
        if (EvalUtils.isBlank(reminderStatusCoded)) {
            throw new IllegalArgumentException("reminderStatusCoded must be set to something");
        }
        try {
            int pos1 = reminderStatusCoded.indexOf(':');
            if (pos1 > 0) {
                int pos2 = reminderStatusCoded.indexOf(':', pos1+1);
                if (pos2 > 0) {
                    this.totalNum = Integer.valueOf(reminderStatusCoded.substring(0, pos1));
                    this.currentNum = Integer.valueOf(reminderStatusCoded.substring(pos1+1, pos2));
                    this.currentEvalGroupId = reminderStatusCoded.substring(pos2+1);
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid reminderStatusCoded string: "+reminderStatusCoded+":: "+ e, e);
        }
    }
    @Override
    public String toString() {
        return totalNum+":"+currentNum+":"+currentEvalGroupId;
    }
}

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

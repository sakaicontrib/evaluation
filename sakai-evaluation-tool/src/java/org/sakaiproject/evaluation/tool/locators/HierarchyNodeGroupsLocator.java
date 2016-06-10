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
package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;

import uk.org.ponder.beanutil.BeanLocator;

/*
 * This is used to set whether various Groups are assigned to a Node.  In
 * reality it's just for backing a page of UIBoundBooleans to set the groups.
 * 
 * Example EL Path:
 * 
 *     hierNodeGroupsLocator.12345.mygroup
 *     
 *  Will return a boolean depending on whether that group is selected.
 */
public class HierarchyNodeGroupsLocator implements BeanLocator {
    public static final String NEW_PREFIX = "new";
    public static String NEW_1 = NEW_PREFIX +"1";

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private ExternalHierarchyLogic hierarchyLogic;
    public void setHierarchyLogic(ExternalHierarchyLogic hierarchyLogic) {
        this.hierarchyLogic = hierarchyLogic;
    }

    public Map<String, Map<String, Boolean>> delivered = new HashMap<>(); 

    public Object locateBean(String name) {
        checkSecurity();

        Map<String, Boolean> togo = delivered.get(name);
        if (togo == null) {
            // FIXME Should this really use the hardcoded "admin" user id?
            List<EvalGroup> evalGroups = commonLogic.getEvalGroupsForUser(commonLogic.getCurrentUserId(), EvalConstants.PERM_BE_EVALUATED);
            Set<String> assignedGroupIds = hierarchyLogic.getEvalGroupsForNode(name);
            Map<String, Boolean> assignedGroups = new HashMap<>();
            //for (EvalGroup group: evalGroups) {
            //    if (assignedGroupIds.contains(group.evalGroupId)) {
            //        assignedGroups.put(group.evalGroupId, Boolean.TRUE);
            //    }
            //    else {
            //        assignedGroups.put(group.evalGroupId, Boolean.FALSE);
            //    }
            //}
            // instead of above, we're going to add all hierarchy provided groups, regardless of if user is enrolled.
            // then, add the rest of user's groups as possible option with false
            for (String assignedGroupId : assignedGroupIds) {
              assignedGroups.put(assignedGroupId, Boolean.TRUE);
            }
            for (EvalGroup group: evalGroups) {
              if (!assignedGroupIds.contains(group.evalGroupId)) {
                assignedGroups.put(group.evalGroupId, Boolean.FALSE);
              }
            }

            togo = assignedGroups;
            delivered.put(name, togo);
        }
        return togo;
    }

    public void saveAll() {
        for( String key : delivered.keySet() )
        {
            Map<String, Boolean> groupbools = delivered.get(key);
            assignGroups(key, groupbools);
        }
    }

    private void assignGroups(String nodeid, Map<String, Boolean> groupbools) {
        Set<String> assignedGroup = new HashSet<>();
        for (Entry<String, Boolean> entry : groupbools.entrySet()) {
            String groupid = entry.getKey();
            Boolean assigned = entry.getValue();
            if (assigned == true) {
                assignedGroup.add(groupid);
            }
        }
        hierarchyLogic.setEvalGroupsForNode(nodeid, assignedGroup);
    }

    /*
     * Currently only administrators can use this functionality.
     */
    private void checkSecurity() {
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        if (!userAdmin) {
            // Security check and denial
            throw new SecurityException("Non-admin users may not access this locator");
        }
    }
}

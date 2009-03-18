/**
 * HierarchyNodeGroupsLocator.java - evaluation - Oct 29, 2007 11:35:56 AM - sgithens
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public Map<String, Map<String, Boolean>> delivered = new HashMap<String, Map<String, Boolean>>(); 

    public Object locateBean(String name) {
        checkSecurity();

        Map<String, Boolean> togo = delivered.get(name);
        if (togo == null) {
            // FIXME Should this really use the hardcoded "admin" user id?
            List<EvalGroup> evalGroups = commonLogic.getEvalGroupsForUser("admin", EvalConstants.PERM_BE_EVALUATED);
            Set<String> assignedGroupIds = hierarchyLogic.getEvalGroupsForNode(name);
            Map<String, Boolean> assignedGroups = new HashMap<String, Boolean>();
            for (EvalGroup group: evalGroups) {
                if (assignedGroupIds.contains(group.evalGroupId)) {
                    assignedGroups.put(group.evalGroupId, new Boolean(true));
                }
                else {
                    assignedGroups.put(group.evalGroupId, new Boolean(false));
                }
            }
            togo = assignedGroups;
            delivered.put(name, togo);
        }
        return togo;
    }

    public void saveAll() {
        for (Iterator<String> i = delivered.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            Map<String, Boolean> groupbools = delivered.get(key);
            assignGroups(key, groupbools);
        }
    }

    private void assignGroups(String nodeid, Map<String, Boolean> groupbools) {
        Set<String> assignedGroup = new HashSet<String>();
        for (Iterator<String> i = groupbools.keySet().iterator(); i.hasNext();) {
            String groupid = (String) i.next();
            Boolean assigned = (Boolean) groupbools.get(groupid);
            if (assigned.booleanValue() == true) {
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

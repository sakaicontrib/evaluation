/**
 * HierarchyNodeLocator.java - evaluation - Oct 29, 2007 11:35:56 AM - sgithens
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
import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;

import uk.org.ponder.beanutil.WriteableBeanLocator;

/*
 * Bean Locator for nodes we are operating upon from
 * ExternalHierarchyLogicImpl
 * 
 * This Locator will look up Nodes by their node id.
 * 
 * example:
 *   HierarchyNodeLocator.12345.title
 *   
 *   Should fetch the title for node with id 12345
 *   
 * If you are adding a new node, use the NEW_PREFIX followed
 * by the id of the Parent Node. For instance to add a child
 * to the node with id '34' use:
 * 
 *   HierarchyNodeLocator.NEW-34.title
 */
public class HierarchyNodeLocator implements WriteableBeanLocator {

    public static final String NEW_PREFIX = "new-";
    public static String NEW_1 = NEW_PREFIX + "1";

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private ExternalHierarchyLogic hierarchyLogic;

    public void setHierarchyLogic(ExternalHierarchyLogic hierarchyLogic) {
        this.hierarchyLogic = hierarchyLogic;
    }

    public Map<String, EvalHierarchyNode> delivered = new HashMap<String, EvalHierarchyNode>();

    public Object locateBean(String name) {
        checkSecurity();

        EvalHierarchyNode togo = delivered.get(name);
        if (togo == null) {
            if (name.startsWith(NEW_PREFIX)) {
                togo = new EvalHierarchyNode();
            } else {
                togo = hierarchyLogic.getNodeById(name);
            }
            delivered.put(name, togo);
        }
        return togo;
    }

    public void saveAll() {
        checkSecurity();

        for (Iterator<String> it = delivered.keySet().iterator(); it.hasNext();) {
            String key = it.next();
            EvalHierarchyNode node = (EvalHierarchyNode) delivered.get(key);
            if (key.startsWith(HierarchyNodeLocator.NEW_PREFIX)) {
                String[] parts = key.split("-");
                EvalHierarchyNode newNode = hierarchyLogic.addNode(parts[1]);
                hierarchyLogic.updateNodeData(newNode.id, node.title, node.description);
            }
            else {
                hierarchyLogic.updateNodeData(node.id, node.title, node.description);
            }
        }
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

    public boolean remove(String id) {
        checkSecurity();
        hierarchyLogic.removeNode(id);
        return true;
    }

    /* Not going to use this for now.
     * 
     */
    public void set(String arg0, Object arg1) {
        checkSecurity();
        // TODO Auto-generated method stub

    }
}

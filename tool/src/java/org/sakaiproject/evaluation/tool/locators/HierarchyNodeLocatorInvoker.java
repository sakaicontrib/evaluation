/**
 * HierarchyNodeLocatorInvoker.java - evaluation - Oct 29, 2007 11:35:56 AM - sgithens
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */
   
package org.sakaiproject.evaluation.tool.locators;

import java.util.Iterator;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;

/*
 * Quick hack to get around RSF Bug.
 */
public class HierarchyNodeLocatorInvoker {
    private EvalExternalLogic external;
    public void setExternal(EvalExternalLogic external) {
       this.external = external;
    }

    private ExternalHierarchyLogic hierarchyLogic;
    public void setHierarchyLogic(ExternalHierarchyLogic hierarchyLogic) {
       this.hierarchyLogic = hierarchyLogic;
    }
    private HierarchyNodeLocator loc;
    public void setLoc(HierarchyNodeLocator loc) {
        this.loc = loc;
    }
    
    public void saveAll() {
        checkSecurity();
        
        for (Iterator it = loc.delivered.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            EvalHierarchyNode node = (EvalHierarchyNode) loc.delivered.get(key);
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
        String currentUserId = external.getCurrentUserId();
        boolean userAdmin = external.isUserAdmin(currentUserId);

        if (!userAdmin) {
            // Security check and denial
            throw new SecurityException("Non-admin users may not access this locator");
        }
    }
    
}

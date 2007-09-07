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
            hierarchyLogic.updateNodeData(node.id, node.title, node.description);
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

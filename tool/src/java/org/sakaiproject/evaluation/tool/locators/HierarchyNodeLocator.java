package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.Map;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import java.util.Iterator;
import uk.org.ponder.beanutil.BeanLocator;

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
public class HierarchyNodeLocator implements BeanLocator {
    public static final String NEW_PREFIX = "new-";
    //public static String NEW_1 = NEW_PREFIX +"1";
    
    private EvalExternalLogic external;
    public void setExternal(EvalExternalLogic external) {
       this.external = external;
    }

    private ExternalHierarchyLogic hierarchyLogic;
    public void setHierarchyLogic(ExternalHierarchyLogic hierarchyLogic) {
       this.hierarchyLogic = hierarchyLogic;
    }
    
    public Map delivered = new HashMap();
    
    public Object locateBean(String name) {
        checkSecurity();
        
        Object togo = delivered.get(name);
        if (togo == null) {
            if (name.startsWith(NEW_PREFIX)) {
                //TODO Not sure how to EL that Set<String> yet... SWG
                String[] parts = name.split("-");
                EvalHierarchyNode node = new EvalHierarchyNode();
                togo = node; //hierarchyLogic.addNode(parts[1]);
            }
            else {
                togo = hierarchyLogic.getNodeById(name);
            }
            delivered.put(name, togo);
        } 
        return togo;
    }
    
    /*
    public void saveAll() {
        checkSecurity();
        
        for (Iterator it = delivered.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            EvalHierarchyNode node = (EvalHierarchyNode) delivered.get(key);
            hierarchyLogic.updateNodeData(node.id, node.title, node.description);
        }
    } */
    
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

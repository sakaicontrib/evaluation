package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.Map;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;

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
 */
public class HierarchyNodeLocator implements BeanLocator {
    public static final String NEW_PREFIX = "new";
    public static String NEW_1 = NEW_PREFIX +"1";
    
    private EvalExternalLogic external;
    public void setExternal(EvalExternalLogic external) {
       this.external = external;
    }

    private ExternalHierarchyLogic hierarchyLogic;
    public void setHierarchyLogic(ExternalHierarchyLogic hierarchyLogic) {
       this.hierarchyLogic = hierarchyLogic;
    }
    
    private Map delivered = new HashMap();
    
    public Object locateBean(String name) {
        Object togo = delivered.get(name);
        if (togo == null) {
            if (name.startsWith(NEW_PREFIX)) {
                //TODO Not sure how to EL that Set<String> yet... SWG
            }
            else {
                togo = hierarchyLogic.getNodeById(name);
            }
            delivered.put(name, togo);
        } 
        return togo;
    }
}

package org.sakaiproject.evaluation.tool.locators;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class HierarchyNodeGroupsLocatorInvoker {
    private EvalExternalLogic external;
    public void setExternal(EvalExternalLogic external) {
       this.external = external;
    }

    private ExternalHierarchyLogic hierarchyLogic;
    public void setHierarchyLogic(ExternalHierarchyLogic hierarchyLogic) {
       this.hierarchyLogic = hierarchyLogic;
    }
    
    private HierarchyNodeLocator hierloc;
    public void setHierloc(HierarchyNodeLocator hierloc) {
        this.hierloc = hierloc;
    }
    
    public void saveAll() {
        for (Iterator i = hierloc.delivered.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            Map groupbools = (Map) hierloc.delivered.get(key);
            assignGroups(key,groupbools);
        }
    }
    
    private void assignGroups(String nodeid, Map groupbools) {
        Set<String> assignedGroup = new HashSet<String>();
        for (Iterator i = groupbools.keySet().iterator(); i.hasNext();) {
            String groupid = (String) i.next();
            Boolean assigned = (Boolean) groupbools.get(groupid);
            if (assigned.booleanValue() == true) {
                assignedGroup.add(groupid);
            }
        }
        hierarchyLogic.setEvalGroupsForNode(nodeid, assignedGroup);
    }
}

package org.sakaiproject.evaluation.logic.impl.providers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.logic.model.HierarchyNode;
import org.sakaiproject.evaluation.logic.providers.EvalHierarchyProvider;

/*
 * Dummy implementation of EvalHierarchyProvider to test the Hierarchy
 * Administration User Interface.  It's understood that eventually, it
 * will be using an API with Read/Write functionality, but that the read
 * method signatures will be the same as EvalHierarchyProvider.
 * 
 * This data is based on the mockups here:
 * https://courseware.vt.edu/users/aaronz/CourseEval/Redesign/Wireframe/HierarchyControl.html
 */
public class DummyEvalHierarchyProviderImpl implements EvalHierarchyProvider {
    
    private Map nodesById = new HashMap();
    private DummyNodeImpl root;
    
    public void init() {
        root = new DummyNodeImpl("root", "Institution Level", "Institution", 0, false);
        DummyNodeImpl schoolBus = new DummyNodeImpl("schoolBus", "School of Business", "School", 1, true);
        DummyNodeImpl schoolSci = new DummyNodeImpl("schoolSci", "School of Science", "School", 1, false);
        DummyNodeImpl deptMath = new DummyNodeImpl("deptMath", "Department of Math", "Department", 2, true);
        DummyNodeImpl deptBio = new DummyNodeImpl("deptBio", "Department of Biology", "Department", 2, true);
        DummyNodeImpl deptChem = new DummyNodeImpl("deptChem", "Department of Chemistry", "Department", 2, true);
        DummyNodeImpl schoolEng = new DummyNodeImpl("schoolEng", "School of Engineering", "School", 1, true);
        
        nodesById.put(root.id, root);
        nodesById.put(schoolBus.id, schoolBus );
        nodesById.put(schoolSci.id, schoolSci );
        nodesById.put(deptMath.id, deptMath);
        nodesById.put(deptBio.id, deptBio);
        nodesById.put(deptChem.id, deptChem);
        nodesById.put(schoolEng.id, schoolEng);
        
        relate(root,schoolBus);
        relate(root,schoolSci);
            relate(schoolSci,deptMath);
            relate(schoolSci,deptBio);
            relate(schoolSci,deptChem);
        relate(root,schoolEng);
    }
    
    private void relate(DummyNodeImpl parent, DummyNodeImpl child) {
        parent.children.add(child);
    }

    public Set getChildNodes(String nodeId) {
        DummyNodeImpl node = (DummyNodeImpl) nodesById.get(nodeId);
        if (node != null)
            return node.children;
        else
            return null;
    }

    public HierarchyNode getParentNode(String nodeId) {
        DummyNodeImpl node = (DummyNodeImpl) nodesById.get(nodeId);
        if (node != null)
            return node.parent;
        else
            return null;
    }

    public HierarchyNode getRootLevelNode() {
        return root;
    }

    /*
     * Don't need to implement the nodes below for testing the Hierarchy Admin
     * User Interface.
     */
    public String[] getUserIdsForNodesPerm(String[] nodeIds, String hierarchyPermConstant) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public Set getNodesForUserPerm(String userId, String hierarchyPermConstant) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public List getNodesAboveEvalGroup(String evalGroupId) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public boolean checkUserNodePerm(String userId, String nodeId, String hierarchyPermConstant) {
        return false;
    }
    
    public Set getEvalGroupsForNode(String nodeId) {
        // TODO Auto-generated method stub
        return null;
    }

}

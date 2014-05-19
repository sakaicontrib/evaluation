package org.sakaiproject.evaluation.tool.viewparams;

public class ModifyHierarchyNodeParameters extends HierarchyNodeParameters {
    /* If adding child is true, we are adding a subnode to this node,
     * rather than modifying the node itself.
     */
    public Boolean addingChild;
    
    public ModifyHierarchyNodeParameters() {
    }
    
    public ModifyHierarchyNodeParameters(String viewID, String nodeId, boolean addingchild) {
        this.viewID = viewID;
        this.nodeId = nodeId;
        this.addingChild = addingchild;
    }
}
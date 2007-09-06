package org.sakaiproject.evaluation.logic.impl.providers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.logic.model.HierarchyNode;

/* Quick and dirty node impl for the test Hierarchy Provider.
 * Only needed for testing while building the GUI.
 */
public class DummyNodeImpl extends HierarchyNode {
    /*
     * This is null if it's the root node.
     */
    public HierarchyNode parent;
    
    public Set /* <HierarchyNode> */ children = new HashSet();
    
    /* I can't remember how to automatically inherit a constructor in java at the moment... */
    public DummyNodeImpl(String id, String name, String type, int level, boolean leaf) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.level = level;
        this.leaf = leaf;
    }
}

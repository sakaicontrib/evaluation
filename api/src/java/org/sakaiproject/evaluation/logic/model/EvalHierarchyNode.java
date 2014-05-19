/**
 * $Id$
 * $URL$
 * EvalHierarchyNode.java - evaluation - Aug 20, 2007 12:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.model;

import java.util.Set;

/**
 * This pea represents a node in a hierarchy 
 * (in academics a department or college would probably be represented by a node),
 * this is basically a copy of the node from hierarchy
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalHierarchyNode implements Comparable<EvalHierarchyNode> {

    /**
     * The unique id for this hierarchy node
     */
    public String id;
    /**
     * the title of this node
     */
    public String title;
    /**
     * the description for this node 
     */
    public String description;
    /**
     * a set of all direct parents for this node,
     * the ids of parent nodes that touch this node directly
     */
    public Set<String> directParentNodeIds;
    /**
     * a set of all direct children for this node,
     * the ids of child nodes that touch this node directly
     */
    public Set<String> directChildNodeIds;
    /**
     * a set of all parents for this node
     */
    public Set<String> parentNodeIds;
    /**
     * a set of all children for this node
     */
    public Set<String> childNodeIds;



    /**
     * Empty constructor
     */
    public EvalHierarchyNode() {}

    /**
     * Convenience constructor for testing
     */
    public EvalHierarchyNode(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }


    /*
     * overrides for various internal methods
     */

    @Override
    public boolean equals(Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof EvalHierarchyNode)) return false;
        else {
            EvalHierarchyNode castObj = (EvalHierarchyNode) obj;
            if (null == this.id || null == castObj.id) return false;
            else return (
                    this.id.equals(castObj.id)
            );
        }
    }

    @Override
    public int hashCode() {
        if (null == this.id) return super.hashCode();
        String hashStr = this.getClass().getName() + ":" + this.id.hashCode();
        return hashStr.hashCode();
    }

    @Override
    public String toString() {
        return "id:" + this.id + ";title:" + this.title + ";parents:" + this.parentNodeIds.size() + ";children:" + this.childNodeIds.size();
    }

    public int compareTo(EvalHierarchyNode o) {
        if (o == null || o.id == null || this.id == null) {
            return 0;
        }
        return this.id.compareTo(o.id);
    }

}

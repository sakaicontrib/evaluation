/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
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
     * @param id
     * @param title
     * @param description
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

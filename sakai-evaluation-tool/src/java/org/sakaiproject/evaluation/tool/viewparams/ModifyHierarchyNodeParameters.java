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
package org.sakaiproject.evaluation.tool.viewparams;

public class ModifyHierarchyNodeParameters extends HierarchyNodeParameters {
    /* If adding child is true, we are adding a subnode to this node,
     * rather than modifying the node itself.
     */
    public Boolean addingChild;
    
    public ModifyHierarchyNodeParameters() {
    }
    
    public ModifyHierarchyNodeParameters(String viewID, String nodeId, boolean addingchild, String[] expanded) {
        this.viewID = viewID;
        this.nodeId = nodeId;
        this.addingChild = addingchild;
        this.expanded = expanded;
    }
}
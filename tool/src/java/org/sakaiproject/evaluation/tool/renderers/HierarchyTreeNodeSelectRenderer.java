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
package org.sakaiproject.evaluation.tool.renderers;

import java.util.List;
import java.util.Set;
import java.util.Arrays;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.tool.producers.ControlHierarchyProducer;
import org.sakaiproject.evaluation.tool.producers.EvaluationAssignProducer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.HierarchyNodeParameters;

import uk.org.ponder.arrayutil.MapUtil;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;

/**
 * At the moment, this is strictly for rendering the tree of hierarchy nodes
 * ,with potential evalgroup leaves, in the assign page of the create evaluation
 * wizard.  This is going in it's own class for clarity because it requires
 * several recursive methods.
 * 
 * This class should not be used as a singleton, it has instance fields.
 * 
 * @author Steven Githens
 */
public class HierarchyTreeNodeSelectRenderer {

    private ExternalHierarchyLogic hierarchyLogic;
    public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
       this.hierarchyLogic = logic;
    }
    
    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
       this.commonLogic = commonLogic;
    }
    
    String groupsSelectID; 
    String hierNodesSelectID;
    List<String> evalGroupLabels; 
    List<String> evalGroupValues;
    List<String> hierNodeLabels; 
    List<String> hierNodeValues;
    
    
   /**
    * This is the main entry point for rendering the hierarchy with selectable
    * checkboxes.  The parent container should be inside whatever form is being
    * used. The groups and nodes SelectID's are the getFullID's that were generated
    * from the UISelect. We're doing a GET form here so there are no EL Bindings
    * for these checkboxes.  The Lists of things are used for storing the option
    * label/value at each point.  These are live references that are passed in 
    * and used after this method invocation to update the UISelect.optionslist
    * and UISelect.optionnames for the Hierarchy Nodes and Group Nodes.
    * 
    * From a visual perspective, the Hierarchy and Group Nodes are interspersed
    * together throughout the page, but we keep them seperate with the two 
    * UISelects.
    * 
    * @param parent
    * @param clientID
    * @param groupsSelectID
    * @param hierNodesSelectID
    * @param evalGroupLabels
    * @param evalGroupValues
    * @param hierNodeLabels
    * @param hierNodeValues
    */
   public void renderSelectHierarchyNodesTree(UIContainer parent, String clientID, 
            String groupsSelectID, String hierNodesSelectID,
            List<String> evalGroupLabels, List<String> evalGroupValues,
            List<String> hierNodeLabels, List<String> hierNodeValues, EvalViewParameters evalViewParams, Set<String> accessNodeIds, Set<String> parentNodeIds) {
       this.groupsSelectID = groupsSelectID;
       this.hierNodesSelectID = hierNodesSelectID;
       this.evalGroupLabels = evalGroupLabels;
       this.evalGroupValues = evalGroupValues;
       this.hierNodeLabels = hierNodeLabels;
       this.hierNodeValues = hierNodeValues;
        
       UIJointContainer joint = new UIJointContainer(parent, clientID, "hierarchy_table_treeview:");

       UIMessage.make(joint, "node-select-header", "controlhierarchy.table.selectnode.header");
       UIMessage.make(joint, "hierarchy-header", "controlhierarchy.table.hierarchy.header");

       EvalHierarchyNode root = hierarchyLogic.getRootLevelNode();

       renderSelectHierarchyNode(joint, root, 0, evalViewParams, accessNodeIds, parentNodeIds);
    }
    

   /**
    * Performs the recursive rendering logic for a single hierarchy node.
    * 
    * @param tofill
    * @param node
    * @param level
    */
   private void renderSelectHierarchyNode(UIContainer tofill, EvalHierarchyNode node, int level, EvalViewParameters evalViewParams, Set<String> accessNodeIds, Set<String> parentNodeIds) {
	   //a null "accessNodeIds varaible means the user is admin
       if(parentNodeIds == null || parentNodeIds.contains(node.id) || accessNodeIds.contains(node.id)){
    	   boolean expanded = renderRow(tofill, "hierarchy-level-row:", level, node, evalViewParams, accessNodeIds);
    	   Set<String> groupIDs = hierarchyLogic.getEvalGroupsForNode(node.id);
    	   for (String groupID: groupIDs) {
    		   EvalGroup evalGroupObj = commonLogic.makeEvalGroupObject(groupID);
    		   renderRow(tofill, "hierarchy-level-row:", level+1, evalGroupObj, evalViewParams, accessNodeIds);
    	   }
    	   if(expanded){
    		   for (String childNodeID: node.directChildNodeIds) {
    			   EvalHierarchyNode childHierNode = hierarchyLogic.getNodeById(childNodeID);
    			   renderSelectHierarchyNode(tofill, childHierNode, level+1, evalViewParams, accessNodeIds, parentNodeIds);
    		   }
    	   }
       }
    }
    
   /**
    * Renders a single row, which could either be a hierarchy node or eval group
    * with a checkbox.
    *
    * @param parent
    * @param clientID
    * @param level
    * @param toRender
    */
   private boolean renderRow(UIContainer parent, String clientID, int level, Object toRender, EvalViewParameters evalViewParams, Set<String> accessNodeIds) {
	   boolean expanded = false;
        UIBranchContainer tableRow = UIBranchContainer.make(parent, "hierarchy-level-row:");
        
        UIOutput.make(tableRow, "node-select-cell");
        
        String title = "";
        if (toRender instanceof EvalHierarchyNode) {
            EvalHierarchyNode evalHierNode = (EvalHierarchyNode) toRender;
            title = evalHierNode.title;
          
            hierNodeLabels.add(title);
            hierNodeValues.add(evalHierNode.id);
            if(accessNodeIds == null || accessNodeIds.contains(evalHierNode.id)){
            	UISelectChoice.make(tableRow, "select-checkbox",
            			hierNodesSelectID, hierNodeLabels.size()-1);
            }
            
            String[] original = null;
            String[] expandedParamArr = new String[1];
            String[] collapseParamArr = new String[]{};
            int collapseI = 0;
            if(evalViewParams.expanded != null && evalViewParams.expanded.length > 0){
            	original = Arrays.copyOf(evalViewParams.expanded, evalViewParams.expanded.length);
            	collapseParamArr = new String[evalViewParams.expanded.length];
            	for(int i = 0; i < evalViewParams.expanded.length; i++){
            		if(evalViewParams != null && evalViewParams.expanded[i] != null && evalHierNode != null
            				&& evalViewParams.expanded[i].equals(evalHierNode.id)){
            			expanded = true;
            		}else if(evalViewParams != null && evalViewParams.expanded[i] != null){
            			collapseParamArr[collapseI] = evalViewParams.expanded[i];
            			collapseI++;
            		}
            	}
            }
            
            if(!expanded){
            	int newLength = evalViewParams.expanded != null ? evalViewParams.expanded.length + 1 : 1;
            	//add this node to the parameter so that if the link is clicked, we know that it is expanded
            	if(newLength > 1){
            		expandedParamArr = Arrays.copyOf(evalViewParams.expanded, newLength);
            	}
            	expandedParamArr[newLength - 1] = evalHierNode.id;
            	UIOutput.make(tableRow, "node-collapsed");
            	evalViewParams.expanded = expandedParamArr;
            }else{
            	evalViewParams.expanded = collapseParamArr;
            	UIOutput.make(tableRow, "node-expanded");
            }
            UIInternalLink.make(tableRow, "node-name-link", title, evalViewParams);
            //now reset this for the next loop
            evalViewParams.expanded = original;
        }
        else if (toRender instanceof EvalGroup) {
            EvalGroup evalGroup = (EvalGroup) toRender;
            title = evalGroup.title;
            
            evalGroupLabels.add(title);
            evalGroupValues.add(evalGroup.evalGroupId);
            
            UISelectChoice choice = UISelectChoice.make(tableRow, "select-checkbox",
                groupsSelectID, evalGroupLabels.size()-1);
            UIOutput name = UIOutput.make(tableRow, "node-name", title);
        }
        else {
            title = "";
        }
        UIOutput name = UIOutput.make(tableRow, "node-name-indent");
        name.decorate(new UIFreeAttributeDecorator( MapUtil.make("style", "text-indent:" + (level*2) + "em") ));
        
        return expanded;
    }
}

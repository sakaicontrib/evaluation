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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;

import uk.org.ponder.arrayutil.MapUtil;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.decorators.UICSSDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;

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
    private boolean sectionAware;

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
    * @param evalViewParams
    * @param accessNodeIds
    * @param parentNodeIds
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
       
       if(evalViewParams.nodeClicked != null){
    	   try{
    		   //make sure we have something that is truely a node id
    		   Integer.parseInt(evalViewParams.nodeClicked);
    		   boolean expand = true;
    		   String[] collapseArr = null;
    		   String[] expandArr = null;
    		   if(evalViewParams.expanded != null && evalViewParams.expanded.length > 0){
    			   int c = 0;
    			   if(evalViewParams.expanded.length == 1){
    				   collapseArr = null;
    			   }else{
    				   collapseArr = Arrays.copyOf(evalViewParams.expanded, evalViewParams.expanded.length - 1);
    			   }
    			   
    			   for( String expanded : evalViewParams.expanded ){
    				   if(expanded.equals(evalViewParams.nodeClicked)){
    					   expand = false;
    				   }else{
    					   if(collapseArr != null && c < collapseArr.length){
    						   collapseArr[c] = expanded;
    						   c++;
    					   }
    				   }
    			   }
    		   }else{
    			   expandArr = new String[]{evalViewParams.nodeClicked};
    		   }
    		   if(expand){
    			   if(expandArr == null){
    				   expandArr = Arrays.copyOf(evalViewParams.expanded, evalViewParams.expanded.length + 1);
    				   expandArr[expandArr.length-1] = evalViewParams.nodeClicked;
    			   }
    			   evalViewParams.expanded = expandArr;
    		   }else{
    			   evalViewParams.expanded = collapseArr;
    		   }
    	   }catch (Exception e) {
    	   }
       }
       evalViewParams.nodeClicked = null;
       //Setup a list of the selected nodes and groups.  If we rendered one, it will get removed from the list
	   //so we will be left with the selected nodes or groups that were "collapsed" and are hidden but still
	   //selected
	   List<String> selectedNodes = new ArrayList<>();
	   Collections.addAll(selectedNodes, evalViewParams.selectedHierarchyNodeIDs);
	   List<String> selectedGroups = new ArrayList<>();
	   Collections.addAll(selectedGroups, evalViewParams.selectedGroupIDs);
	   renderSelectHierarchyNode(joint, root, 0, evalViewParams, accessNodeIds, parentNodeIds, selectedNodes, selectedGroups);
	   //go through the left over selected groups and nodes and render hidden rows in the table so that we won't lose
	   //them when other node links are clicked
	   for(String nodeId : selectedNodes){
		   //this is just a placeholder for the id, so no need to get the real node
		   EvalHierarchyNode node = new EvalHierarchyNode(nodeId, nodeId, nodeId);
		   renderRow(joint, "hierarchy-level-row:", 0, node, evalViewParams, accessNodeIds, new HashSet<>(),true);
	   }
	   for(String groupId : selectedGroups){
		   //this is just a placeholder for the id, so no need to get the real group
		   EvalGroup evalGroup = new EvalGroup();
		   evalGroup.title = groupId;
		   evalGroup.evalGroupId = groupId;
		   renderRow(joint, "hierarchy-level-row:", 0, evalGroup, evalViewParams, accessNodeIds, new HashSet<>(), true);
	   }
	   
    }

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
     * @param evalViewParams
     * @param accessNodeIds
     * @param parentNodeIds
     * @param sectionAware
     */
    public void renderSelectHierarchyNodesTree( UIContainer parent, String clientID, String groupsSelectID, String hierNodesSelectID,
             List<String> evalGroupLabels, List<String> evalGroupValues, List<String> hierNodeLabels, List<String> hierNodeValues, 
             EvalViewParameters evalViewParams, Set<String> accessNodeIds, Set<String> parentNodeIds, boolean sectionAware )
    {
        this.sectionAware = sectionAware;
        renderSelectHierarchyNodesTree( parent, clientID, groupsSelectID, hierNodesSelectID, evalGroupLabels, evalGroupValues, hierNodeLabels,
                hierNodeValues, evalViewParams, accessNodeIds, parentNodeIds );
    }

    /**
     * Performs the recursive rendering logic for a single hierarchy node.
     * 
     * @param tofill
     * @param node
     * @param level
     */
    private void renderSelectHierarchyNode(UIContainer tofill, EvalHierarchyNode node, int level, EvalViewParameters evalViewParams, Set<String> accessNodeIds, Set<String> parentNodeIds,
           List<String> selectedNodes, List<String> selectedGroups) {

        //a null "accessNodeIds varaible means the user is admin
        if(parentNodeIds == null || parentNodeIds.contains(node.id) || accessNodeIds.contains(node.id)){
            boolean expanded = renderRow(tofill, "hierarchy-level-row:", level, node, evalViewParams, accessNodeIds, null, false);
            selectedNodes.remove(""+node.id);
            if(expanded){
                Set<String> groupIDs = hierarchyLogic.getEvalGroupsForNode(node.id);
                for( String groupID : groupIDs )
                {
                    Set<String> currentNodeParents = node.parentNodeIds;
                    currentNodeParents.add( node.id );
                    selectedGroups.remove( groupID );

                    if( !sectionAware )
                    {
                        EvalGroup evalGroup = commonLogic.makeEvalGroupObject( groupID );
                        renderRow( tofill, "hierarchy-level-row:", level + 1, evalGroup, evalViewParams, accessNodeIds, currentNodeParents, false );
                    }
                    else
                    {
                        // Get the eval groups (child sections) under this group ID (parent site), and render a row for each
                        List<EvalGroup> evalGroups = commonLogic.makeEvalGroupObjectsForSectionAwareness( groupID );
                        for( EvalGroup evalGroup : evalGroups )
                        {
                            renderRow( tofill, "hierarchy-level-row:", level + 1, evalGroup, evalViewParams, accessNodeIds, currentNodeParents, false );
                        }
                    }
                }

                for (EvalHierarchyNode childHierNode: hierarchyLogic.getChildNodes(node.id, true)) {
                    renderSelectHierarchyNode(tofill, childHierNode, level+1, evalViewParams, accessNodeIds, parentNodeIds, selectedNodes, selectedGroups);
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
   private boolean renderRow(UIContainer parent, String clientID, int level, Object toRender, EvalViewParameters evalViewParams, Set<String> accessNodeIds, Set<String> currentNodeParents, boolean hideRow) {
	   boolean expanded = false;
	   
	    UIBranchContainer tableRow = UIBranchContainer.make(parent, "hierarchy-level-row:");
		
		if(hideRow){
			Map<String, String> cssHide = new HashMap<>();
			cssHide.put("display", "none");
			tableRow.decorate(new UICSSDecorator(cssHide));
		}
        
        UIOutput.make(tableRow, "node-select-cell");
        
        String title;
        if (toRender instanceof EvalHierarchyNode) {
            EvalHierarchyNode evalHierNode = (EvalHierarchyNode) toRender;
            title = evalHierNode.title;
          
            hierNodeLabels.add(title);
            hierNodeValues.add(evalHierNode.id);
            if(accessNodeIds == null || accessNodeIds.contains(evalHierNode.id)){
            	UISelectChoice checkbox = UISelectChoice.make(tableRow, "select-checkbox-hide",
            			hierNodesSelectID, hierNodeLabels.size()-1);
            	boolean disabled = false;
            	if(evalHierNode.parentNodeIds != null){
					for(String parentId : evalHierNode.parentNodeIds){
						checkbox.decorate(new UIStyleDecorator("parentNode" + parentId));
						//see if any parents are selected, if so, then disable the checkbox
						if(!disabled && evalViewParams.selectedHierarchyNodeIDs != null){
							for( String selectedHierarchyNodeID : evalViewParams.selectedHierarchyNodeIDs ){
								if(parentId.equals(selectedHierarchyNodeID)){
									disabled = true;
									break;
								}
            				}
            			}
            		}
            	}
            	checkbox.decorate(new UIFreeAttributeDecorator("nodeId", evalHierNode.id));
            	if(disabled){
            		checkbox.decorate(new UIFreeAttributeDecorator("disabled", "disabled"));
            	}
            }
            
            if(evalViewParams.expanded != null){
            	for( String expanded1 : evalViewParams.expanded ){
            		if(expanded1.equals(evalHierNode.id)){
            			expanded = true;
            			break;
            		}
            	}
            }

            if(!expanded){
            	UIOutput.make(tableRow, "node-collapsed");
            }else{
            	UIOutput.make(tableRow, "node-expanded");
            }
            UILink nodeLink = UILink.make(tableRow, "node-name-link-submit", title, "");
            nodeLink.decorate(new UIFreeAttributeDecorator("nodeId", evalHierNode.id));
        }
        else if (toRender instanceof EvalGroup) {
            EvalGroup evalGroup = (EvalGroup) toRender;
            title = evalGroup.title;
            
            evalGroupLabels.add(title);
            evalGroupValues.add(evalGroup.evalGroupId);
            
            UISelectChoice choice = UISelectChoice.make(tableRow, "select-checkbox",
                groupsSelectID, evalGroupLabels.size()-1);
            boolean disabled = false;
        	for(String parentId : currentNodeParents){
        		choice.decorate(new UIStyleDecorator("parentNode" + parentId));
        		//see if any parents are selected, if so, then disable the checkbox
        		if(!disabled && evalViewParams.selectedHierarchyNodeIDs != null){
        			for( String selectedHierarchyNodeID : evalViewParams.selectedHierarchyNodeIDs ){
        				if(parentId.equals(selectedHierarchyNodeID)){
        					disabled = true;
        					break;
        				}
        			}
        		}
        	}
        	if(disabled){
        		choice.decorate(new UIFreeAttributeDecorator("disabled", "disabled"));
        	}
            UIOutput.make(tableRow, "node-name", title);
        }

        UIOutput name = UIOutput.make(tableRow, "node-name-indent");
        name.decorate(new UIFreeAttributeDecorator( MapUtil.make("style", "text-indent:" + (level*2) + "em") ));
        
        return expanded;
    }
}

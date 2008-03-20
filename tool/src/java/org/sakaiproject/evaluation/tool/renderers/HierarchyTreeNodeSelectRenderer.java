package org.sakaiproject.evaluation.tool.renderers;

import java.util.Set;

import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;

import uk.org.ponder.arrayutil.MapUtil;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;

/**
 * At the moment, this is strictly for rendering the tree of hierarchy nodes
 * ,with potential evalgroup leaves, in the assign page of the create evaluation
 * wizard.  This is going in it's own class for clarity because it requires
 * several recursive methods.
 */
public class HierarchyTreeNodeSelectRenderer {
    private ExternalHierarchyLogic hierarchyLogic;
    public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
       this.hierarchyLogic = logic;
    }
    
    private EvalExternalLogic externalLogic;
    public void setExternalLogic(EvalExternalLogic externalLogic) {
       this.externalLogic = externalLogic;
    }
    
    /**
     * @param parent
     * @param clientID
     * @param elbinding
     */
    public void renderSelectHierarchyNodesTree(UIContainer parent, String clientID, String elbinding, String groupElBinding) {
       UIJointContainer joint = new UIJointContainer(parent, clientID, "hierarchy_table_treeview:");

       UIMessage.make(joint, "node-select-header", "controlhierarchy.table.selectnode.header");
       UIMessage.make(joint, "hierarchy-header", "controlhierarchy.table.heirarchy.header");

       EvalHierarchyNode root = hierarchyLogic.getRootLevelNode();

       //UISelect siteCheckboxes = UISelect.makeMultiple(joint, "selectColumnCheckboxes", new String[] {}, elbinding, null);
       //String selectID = siteCheckboxes.getFullID();
       //List<String> checkboxValues = new ArrayList<String>();

       renderSelectHierarchyNode(joint, root, 0);

       //String[] ids = checkboxValues.toArray(new String[]{});
       //siteCheckboxes.optionlist = siteCheckboxes.optionnames = UIOutputMany.make(ids);
    }
    
    /**
     * @param tofill
     * @param node
     * @param level
     * @param selectID
     * @param checkboxValues
     */
    private void renderSelectHierarchyNode(UIContainer tofill, EvalHierarchyNode node, int level ) {
       renderRow(tofill, "hierarchy-level-row:", level, node);
       
       Set<String> groupIDs = hierarchyLogic.getEvalGroupsForNode(node.id);
       for (String groupID: groupIDs) {
           EvalGroup evalGroupObj = externalLogic.makeEvalGroupObject(groupID);
           renderRow(tofill, "hierarchy-level-row:", level+1, evalGroupObj);
       }
       
       for (String childNodeID: node.directChildNodeIds) {
           EvalHierarchyNode childHierNode = hierarchyLogic.getNodeById(childNodeID);
           renderSelectHierarchyNode(tofill, childHierNode, level+1);
       }
       
       /* 
       String title = node.title != null ? node.title : "Null Title?";
       UIBranchContainer tableRow = UIBranchContainer.make(tofill, "hierarchy-level-row:");
       UIOutput.make(tableRow, "node-select-cell");
       UISelectChoice.make(tableRow, "select-checkbox", selectID, checkboxValues.size());
       checkboxValues.add(node.id);
       UIOutput name = UIOutput.make(tableRow, "node-name", title);
       name.decorate(new UIFreeAttributeDecorator( MapUtil.make("style", "text-indent:" + (level*2) + "em") ));

       Set<String> groupIDs = hierarchyLogic.getEvalGroupsForNode(node.id);
       for (String groupID: groupIDs) {
           renderSelectHierarchyGroup(tofill, groupID, level+1, evalGroupIDs, groupClientID);
       }

       for (String childId : node.directChildNodeIds) {
          renderSelectHierarchyNode(tofill, hierarchyLogic.getNodeById(childId), level+1, selectID, checkboxValues, evalGroupIDs, groupElBinding, groupClientID);
       } */
    }
    
    public void renderRow(UIContainer parent, String clientID, int level, Object toRender) {
        UIBranchContainer tableRow = UIBranchContainer.make(parent, "hierarchy-level-row:");
        
        UIOutput.make(tableRow, "node-select-cell");
        
        String title = "";
        if (toRender instanceof EvalHierarchyNode) {
            EvalHierarchyNode evalHierNode = (EvalHierarchyNode) toRender;
            title = evalHierNode.title;
            UIBoundBoolean.make(tableRow, "select-checkbox", "evaluationBean.selectedEvalHierarchyNodeIDsMap."+evalHierNode.id);
        }
        else if (toRender instanceof EvalGroup) {
            EvalGroup evalGroup = (EvalGroup) toRender;
            title = evalGroup.title;
            UIBoundBoolean.make(tableRow, "select-checkbox", "evaluationBean.selectedEvalGroupIDsMap."+evalGroup.evalGroupId);
        }
        else {
            title = "";
        }
        UIOutput name = UIOutput.make(tableRow, "node-name", title);
        name.decorate(new UIFreeAttributeDecorator( MapUtil.make("style", "text-indent:" + (level*2) + "em") ));
    }
}
package org.sakaiproject.evaluation.tool.renderers;

import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;

import uk.org.ponder.arrayutil.MapUtil;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;

/*
 * Render a specific row in the Hierarchy.
 */
public class HierarchyRowRenderer {

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
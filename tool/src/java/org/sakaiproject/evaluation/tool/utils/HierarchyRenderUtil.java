package org.sakaiproject.evaluation.tool.utils;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UISelect;

/* Should be replaced by either an Evolver or a Real Producer of some
 * sort.
 * 
 * Renders the drop down Hiearchy Node Selector on the Views for Modifying
 * template items.
 */
public class HierarchyRenderUtil {
    private ExternalHierarchyLogic hierarchyLogic;
    public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
        this.hierarchyLogic = logic;
    }
    
    public void makeHierSelect(UIForm form, String selectID, String elBinding) {
        List<String> hierSelectValues = new ArrayList<String>();
        List<String> hierSelectLabels = new ArrayList<String>();
        populateHierSelectLists(hierSelectValues, hierSelectLabels, 0, null);
        UISelect.make(form, selectID,
                hierSelectValues.toArray(new String[]{}), 
                hierSelectLabels.toArray(new String[]{}),
                elBinding, null);
    }
    
    private void populateHierSelectLists(List<String> values, List<String> labels, int level, EvalHierarchyNode node) {
        if (values.size() == 0) {
            values.add(EvalConstants.HIERARCHY_NODE_ID_NONE);
            labels.add("Top Level");
            populateHierSelectLists(values, labels, 0, null);
        }
        else {
            if (level == 0 && node == null) {
                EvalHierarchyNode root = hierarchyLogic.getRootLevelNode();
                for (String childId: root.childNodeIds) {
                    populateHierSelectLists(values, labels, 0, hierarchyLogic.getNodeById(childId));
                }
            }
            else {
                values.add(node.id);
                String label = node.title;
                for (int i = 0; i < level; i++) {
                    label = "." + label;
                }
                labels.add(label);
                for (String childId: node.childNodeIds) {
                    populateHierSelectLists(values, labels, level+2, hierarchyLogic.getNodeById(childId));
                }
            }
        }
    }
}

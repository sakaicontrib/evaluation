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
package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;

import uk.org.ponder.beanutil.WriteableBeanLocator;

/*
 * Bean Locator for nodes we are operating upon from
 * ExternalHierarchyLogicImpl
 * 
 * This Locator will look up Nodes by their node id.
 * 
 * example:
 *   HierarchyNodeLocator.12345.title
 *   
 *   Should fetch the title for node with id 12345
 *   
 * If you are adding a new node, use the NEW_PREFIX followed
 * by the id of the Parent Node. For instance to add a child
 * to the node with id '34' use:
 * 
 *   HierarchyNodeLocator.NEW-34.title
 */
public class HierarchyNodeLocator implements WriteableBeanLocator {

    public static final String NEW_PREFIX = "new-";
    public static String NEW_1 = NEW_PREFIX + "1";

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private ExternalHierarchyLogic hierarchyLogic;

    public void setHierarchyLogic(ExternalHierarchyLogic hierarchyLogic) {
        this.hierarchyLogic = hierarchyLogic;
    }

    public Map<String, EvalHierarchyNode> delivered = new HashMap<>();

    public Object locateBean(String name) {
        checkSecurity();

        EvalHierarchyNode togo = delivered.get(name);
        if (togo == null) {
            if (name.startsWith(NEW_PREFIX)) {
                togo = new EvalHierarchyNode();
            } else {
                togo = hierarchyLogic.getNodeById(name);
            }
            delivered.put(name, togo);
        }
        return togo;
    }

    public void saveAll() {
        checkSecurity();

        for( String key : delivered.keySet() )
        {
            EvalHierarchyNode node = (EvalHierarchyNode) delivered.get(key);
            if (key.startsWith(HierarchyNodeLocator.NEW_PREFIX)) {
                String[] parts = key.split("-");
                EvalHierarchyNode newNode = hierarchyLogic.addNode(parts[1]);
                hierarchyLogic.updateNodeData(newNode.id, node.title, node.description);
            }
            else {
                hierarchyLogic.updateNodeData(node.id, node.title, node.description);
            }
        }
    }

    /*
     * Currently only administrators can use this functionality.
     */
    private void checkSecurity() {
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        if (!userAdmin) {
            // Security check and denial
            throw new SecurityException("Non-admin users may not access this locator");
        }
    }

    public boolean remove(String id) {
        checkSecurity();
        hierarchyLogic.removeNode(id);
        return true;
    }

    /* Not going to use this for now.
     * 
     */
    public void set(String arg0, Object arg1) {
        checkSecurity();
        // TODO Auto-generated method stub

    }
}

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
package org.sakaiproject.evaluation.logic.externals;

import java.util.List;

import org.sakaiproject.evaluation.logic.model.HierarchyNodeRule;

/**
 * This allows for the hierarchy data to contain node rules
 */
public interface ExternalHierarchyRules
{
    /**
     * Get all rules for the given node ID
     * 
     * @param nodeID the ID of the node in question
     * @return a List of Entry<String, String>, where the first String in the entry is the rule text, and the second is the option selection
     */
    public List<HierarchyNodeRule> getRulesByNodeID( Long nodeID );

    /**
     * Get a specific rule by the given rule ID
     * 
     * @param ruleID the ID of the rule to retrieve
     * @return the HierarchyNodeRule asked for
     */
    public HierarchyNodeRule getRuleByID( Long ruleID );

    /**
     * Get all rules in the DB
     * 
     * @return a List of HierarchyNodeRule
     */
    public List<HierarchyNodeRule> getAllRules();

    /**
     * Determine if the given rule is already applied to the given node
     * 
     * @param ruleText - the rule text without qualifier applied
     * @param qualifierSelection - the qualifier to be applied
     * @param optionSelection - the option selection (site or section title)
     * @param nodeID - the node id to check
     * @return true/false if this rule already exists for the given node
     */
    public boolean isRuleAlreadyAssignedToNode( String ruleText, String qualifierSelection, String optionSelection, Long nodeID );

    /**
     * Assign the rule to the given node
     * 
     * @param ruleText - the text of the rule
     * @param qualifier - rule qualifier (is, contains, ends with, starts with)
     * @param option - the rule option (site or section title)
     * @param nodeID - the node this rule should be assigned to
     */
    public void assignNodeRule( String ruleText, String qualifier, String option, Long nodeID );

    /**
     * Remove the given rule from the system
     * 
     * @param ruleID - the ID of the rule to be removed
     */
    public void removeNodeRule( Long ruleID );

    /**
     * Remove all rules for the give node ID.
     * 
     * @param nodeID the ID of the node to remove all rules for
     */
    public void removeAllRulesForNode( Long nodeID );

    /**
     * Update the give rule for the given node
     * 
     * @param ruleID - the ID of the rule to be updated
     * @param ruleText - the new rule text
     * @param qualifier - the new rule qualifier (is, contains, ends with, starts with)
     * @param option - the new rule option (site or section title)
     * @param nodeID - the ID of the node this rule is assigned to
     */
    public void updateNodeRule( Long ruleID, String ruleText, String qualifier, String option, Long nodeID );
}

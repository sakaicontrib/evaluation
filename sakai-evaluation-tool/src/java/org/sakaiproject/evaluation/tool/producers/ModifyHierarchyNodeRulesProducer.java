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
package org.sakaiproject.evaluation.tool.producers;

import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.logic.model.HierarchyNodeRule;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.HierarchyNodeParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * This producer is responsible for instantiating the Modify Node Rules (Criteria) UI.
 * 
 * @author bjones86
 */
public class ModifyHierarchyNodeRulesProducer extends EvalCommonProducer implements ViewParamsReporter
{
    private static final Log LOG = LogFactory.getLog( ModifyHierarchyNodeRulesProducer.class );
    public static final String VIEW_ID = "modify_hierarchy_node_rules";
    public String getViewID() { return VIEW_ID; }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic( EvalCommonLogic commonLogic )
    {
        this.commonLogic = commonLogic;
    }

    private ExternalHierarchyLogic hierarchyLogic;
    public void setHierarchyLogic( ExternalHierarchyLogic hierarchyLogic )
    {
        this.hierarchyLogic = hierarchyLogic;
    }

    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer( NavBarRenderer navBarRenderer )
    {
        this.navBarRenderer = navBarRenderer;
    }

    /**
     * Instantiate the Modify Node Rules (Criteria) UI
     * 
     * @param toFill - parent container
     * @param viewParams - parameters passed from the previous page
     * @param checker
     */
    public void fill( UIContainer toFill, ViewParameters viewParams, ComponentChecker checker )
    {
        // Make sure it's an admin user
        String currentUserID = commonLogic.getCurrentUserId();
        boolean isAdmin = commonLogic.isUserAdmin( currentUserID );
        if( !isAdmin )
        {
            throw new SecurityException( "Non-admin users may not access this locator." );
        }

        // Render the navigation bar
        navBarRenderer.makeNavBar( toFill, NavBarRenderer.NAV_ELEMENT, this.getViewID() );

        // Get the parameters
        HierarchyNodeParameters params = (HierarchyNodeParameters) viewParams;
        EvalHierarchyNode evalNode = hierarchyLogic.getNodeById( params.nodeId );

        // Render the page titles, instructions, top menu links and bread crumbs
        UIInternalLink.make( toFill, "hierarchy-toplink", UIMessage.make( "controlhierarchy.breadcrumb.title" ),
                new SimpleViewParameters( ControlHierarchyProducer.VIEW_ID ) );
        UIMessage.make( toFill, "page-title", "modifynoderules.breadcrumb.title" );
        UIMessage.make( toFill, "node-info", "modifynoderules.node.info", new Object[] { evalNode.title, evalNode.description } );
        UIMessage.make( toFill, "instructions1", "modifynoderules.instructions.1" );
        UIMessage.make( toFill, "instructions2", "modifynoderules.instructions.2" );

        // Define the action bean prefix, create the form
        String actionBean = "hierarchyBean.";
        UIForm rulesForm = UIForm.make( toFill, "rules-form" );

        // Render the cancel buttons and return links
        UICommand.make( rulesForm, "cancel-changes-button1", UIMessage.make( "modifynoderules.cancel.changes.button" ) );
        UICommand.make( rulesForm, "cancel-changes-button2", UIMessage.make( "modifynoderules.cancel.changes.button" ) );
        UIInternalLink.make( rulesForm, "return-link1", UIMessage.make( "controlhierarchy.return.link" ),
                new HierarchyNodeParameters( ControlHierarchyProducer.VIEW_ID, null, params.expanded ) );
        UIInternalLink.make( rulesForm, "return-link2", UIMessage.make( "controlhierarchy.return.link" ),
                new HierarchyNodeParameters( ControlHierarchyProducer.VIEW_ID, null, params.expanded ) );

        // Render the table headers and the hidden node-id element
        UIInput.make( rulesForm, "node-id", actionBean + "nodeId", evalNode.id );
        UIMessage.make( rulesForm, "rules-header", "modifynoderules.rule.header" );
        UIMessage.make( rulesForm, "actions-header", "modifynoderules.actions.header" );

        // Render the new rule container
        UIBranchContainer newRuleBranch = UIBranchContainer.make( rulesForm, "new-rule:" );
        UISelect.make( newRuleBranch, "new-rule-option-selection", EvalToolConstants.HIERARCHY_RULE_OPTION_VALUES,
                EvalToolConstants.HIERARCHY_RULE_OPTION_LABELS, actionBean + "newOptionSelection" ).setMessageKeys();
        UISelect.make( newRuleBranch, "new-rule-qualifier-selection", EvalToolConstants.HIERARCHY_RULE_QUALIFIER_VALUES,
                EvalToolConstants.HIERARCHY_RULE_QUALIFIER_LABELS, actionBean + "newQualifierSelection" ).setMessageKeys();
        UIInput.make( newRuleBranch, "rule-text", actionBean + "newRuleText" );
        UICommand.make( newRuleBranch, "add-rule-button", UIMessage.make( "modifynoderules.add.rule.button" ), actionBean + "addRule" );

        // Render the existing rules
        List<HierarchyNodeRule> existingNodeRules;
        try { existingNodeRules = hierarchyLogic.getRulesByNodeID( Long.parseLong( evalNode.id ) ); }
        catch( Exception ex )
        {
            LOG.warn( "Can't fetch hierarchy node ID = " + evalNode.id, ex );
            existingNodeRules = Collections.emptyList();
        }
        for( HierarchyNodeRule existingRule : existingNodeRules )
        {
            // Parse the rule index to a string, determine the qualifier, determine raw rule text, parse ruleID to string
            String qualifier = hierarchyLogic.determineQualifierFromRuleText( existingRule.getRule() );
            String rawRuleText = hierarchyLogic.removeQualifierFromRuleText( existingRule.getRule() );
            String ruleID = Long.toString( existingRule.getId() );

            // Create the branch for this rule, add the option, qualifier and rule text
            UIBranchContainer existingRuleBranch = UIBranchContainer.make( rulesForm, "rule:", ruleID );
            UISelect.make( existingRuleBranch, "existing-rule-option-selection", EvalToolConstants.HIERARCHY_RULE_OPTION_VALUES, 
                    EvalToolConstants.HIERARCHY_RULE_OPTION_LABELS, actionBean + "existingOptionSelections." + ruleID, existingRule.getOption() ).setMessageKeys();
            UISelect.make( existingRuleBranch, "existing-rule-qualifier-selection", EvalToolConstants.HIERARCHY_RULE_QUALIFIER_VALUES,
                    EvalToolConstants.HIERARCHY_RULE_QUALIFIER_LABELS, actionBean + "existingQualifierSelections." + ruleID, qualifier ).setMessageKeys();
            UIInput.make( existingRuleBranch, "existing-rule-text", actionBean + "existingRuleTexts." + ruleID, rawRuleText );

            // Render the action buttons
            UICommand btnSave = UICommand.make( existingRuleBranch, "save-changes-button", UIMessage.make( "modifynoderules.save.changes.button" ), actionBean + "saveRule" );
            UICommand btnRemove = UICommand.make( existingRuleBranch, "remove-rule-button", UIMessage.make( "modifynoderules.remove.rule.button" ), actionBean + "removeRule" );

            // Add the ELBinding to tell the backing bean which rule these buttons correspond to
            btnSave.parameters.add( new UIELBinding( actionBean + "ruleID", ruleID ) );
            btnRemove.parameters.add( new UIELBinding( actionBean + "ruleID", ruleID ) );
        }
    }

    public ViewParameters getViewParameters()
    {
        return new HierarchyNodeParameters();
    }
}

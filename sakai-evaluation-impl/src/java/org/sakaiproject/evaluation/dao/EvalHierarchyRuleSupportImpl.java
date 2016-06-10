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
package org.sakaiproject.evaluation.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.model.HierarchyNodeRule;
import org.sakaiproject.evaluation.model.EvalHierarchyRule;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;

/**
 * This class implements all DAO functionality for Hierarchy Rules.
 */
public class EvalHierarchyRuleSupportImpl implements EvalHierarchyRuleSupport
{
    private static final Log LOG = LogFactory.getLog( EvalHierarchyRuleSupportImpl.class );

    private EvaluationDao dao;
    public void setDao( EvaluationDao dao )
    {
        this.dao = dao;
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#isRuleAlreadyAssignedToNode(java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    public boolean isRuleAlreadyAssignedToNode( String ruleText, String qualifierSelection, String optionSelection, Long nodeID )
    {
        if( LOG.isDebugEnabled() )
        {
            LOG.debug( "isRuleAlreadyAssignedToNode( " + ruleText + ", " + qualifierSelection + ", " + optionSelection + ", " + nodeID + " )" );
        }

        if( StringUtils.isBlank( ruleText ) || StringUtils.isBlank( qualifierSelection ) || StringUtils.isBlank( optionSelection ) )
        {
            throw new IllegalArgumentException( "Invalid arguments for isRuleAlreadyAssignedToNode(), no arguments can be null or blank: " +
                "ruleText=" + ruleText + ", qualifierSelection=" + qualifierSelection + ", optionSelection=" + optionSelection + ", nodeID=" + nodeID );
        }

        // Check to see if this rule is already in place for the given node
        String finalRuleText = applyQualifierToRuleText( ruleText, qualifierSelection );
        List<HierarchyNodeRule> existingRules = getRulesByNodeID( nodeID );
        return existingRules.stream().anyMatch( (rule) -> ( finalRuleText.equals( rule.getRule() ) && optionSelection.equals( rule.getOption() ) ) );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#assignNodeRule(java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    public void assignNodeRule( String ruleText, String qualifier, String option, Long nodeID )
    {
        if( LOG.isDebugEnabled() )
        {
            LOG.debug( "assignNodeRule( " + ruleText + ", " + qualifier + ", " + option + ", " + nodeID + " )" );
        }

        if( StringUtils.isBlank( ruleText ) || StringUtils.isBlank( qualifier ) || StringUtils.isBlank( option ) )
        {
            throw new IllegalArgumentException( "Invalid arguments for assignNodeRule(), no arguments can be null or blank: " +
                "ruleText=" + ruleText + ", qualifier=" + qualifier + ", option=" + option + ", nodeID=" + nodeID );
        }

        // Generate the final rule text from the original rule text and the qualifier
        String finalRuleText = applyQualifierToRuleText( ruleText, qualifier );

        EvalHierarchyRule rule = new EvalHierarchyRule( nodeID, finalRuleText, option );
        dao.create( rule );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#removeNodeRule(java.lang.Long)
     */
    public void removeNodeRule( Long ruleID )
    {
        if( LOG.isDebugEnabled() )
        {
            LOG.debug( "removeNodeRule( " + ruleID + " )" );
        }

        EvalHierarchyRule rule = getByID( ruleID );
        if( rule == null )
        {
            throw new IllegalArgumentException( "Can't find hierarchy node rule: " + "ruleID=" + ruleID );
        }

        dao.delete( rule );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#removeAllRulesForNode(java.lang.Long)
     */
    public void removeAllRulesForNode( Long nodeID )
    {
        if( LOG.isDebugEnabled() )
        {
            LOG.debug( "removeAllRulesForNode( " + nodeID + " )" );
        }

        Set<EvalHierarchyRule> rules = new HashSet( getAllByNodeID( nodeID ) );
        if( !rules.isEmpty() )
        {
            dao.deleteSet( rules );
        }
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#updateNodeRule(java.lang.Long, java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    public void updateNodeRule( Long ruleID, String ruleText, String qualifier, String option, Long nodeID )
    {
        if( LOG.isDebugEnabled() )
        {
            LOG.debug( "updateNodeRule( " + ruleID + ", " + ruleText + ", " + qualifier + ", " + option + ", " + nodeID + " )" );
        }

        if( StringUtils.isBlank( ruleText ) || StringUtils.isBlank( qualifier ) || StringUtils.isBlank( option ) )
        {
            throw new IllegalArgumentException( "Invalid arguments for updateNodeRule(), no arguments can be null or blank: " +
                "ruleID=" + ruleID + ", ruleText=" + ruleText + ", qualifier=" + qualifier + ", option=" + option + ", nodeID=" + nodeID );
        }

        // Generate the final rule text from the original rule text and the qualifier
        String finalRuleText = applyQualifierToRuleText( ruleText, qualifier );

        EvalHierarchyRule rule = getByID( ruleID );
        if( rule != null )
        {
            rule.setRule( finalRuleText );
            rule.setOpt( option );
            rule.setNodeID( nodeID );
            dao.update( rule );
        }
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#getRulesByNodeID(java.lang.Long)
     */
    public List<HierarchyNodeRule> getRulesByNodeID( Long nodeID )
    {
        if( LOG.isDebugEnabled() )
        {
            LOG.debug( "getRulesByNodeID( " + nodeID + " )" );
        }

        List<EvalHierarchyRule> rules = getAllByNodeID( nodeID );
        return convertRules( rules );
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#getRuleByID(java.lang.Long)
     */
    public HierarchyNodeRule getRuleByID( Long ruleID )
    {
        if( LOG.isDebugEnabled() )
        {
            LOG.debug( "getRuleByID( " + ruleID + " )" );
        }

        EvalHierarchyRule rule = getByID( ruleID );
        if( rule == null )
        {
            return null;
        }
        else
        {
            return new HierarchyNodeRule( rule );
        }
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.external.ExternalHierarchyRules#getAllRules()
     */
    public List<HierarchyNodeRule> getAllRules()
    {
        if( LOG.isDebugEnabled() )
        {
            LOG.debug( "getAllRules()" );
        }

        List<EvalHierarchyRule> rules = dao.findAll( EvalHierarchyRule.class );
        return convertRules( rules );
    }

    /**
     * Utility method to get a rule by an ID. Created to reduce code duplication.
     * 
     * @param ruleID the ID of the rule to be retrieved
     * @return the EvalHierarchyRule retrieved
     */
    private EvalHierarchyRule getByID( Long ruleID )
    {
        if( LOG.isDebugEnabled() )
        {
            LOG.debug( "getByID( " + ruleID + " )" );
        }

        Search searchObj = new Search();
        searchObj.addRestriction( new Restriction( "id", ruleID ) );
        List<EvalHierarchyRule> rules = dao.findBySearch( EvalHierarchyRule.class, searchObj );

        if( rules == null || rules.isEmpty() )
        {
            return null;
        }
        else
        {
            return rules.get( 0 );
        }
    }

    /**
     * Utility method to get all rules by a node ID. Created to reduce code duplication.
     * 
     * @param nodeID the ID of the node to retrieve all rules for
     * @return a list of EvalHierarchyRules for the given node ID
     */
    private List<EvalHierarchyRule> getAllByNodeID( Long nodeID )
    {
        if( LOG.isDebugEnabled() )
        {
            LOG.debug( "getAllByNodeID( " + nodeID + " )" );
        }

        Search searchObj = new Search();
        searchObj.addRestriction( new Restriction( "nodeID", nodeID ) );
        List<EvalHierarchyRule> rules = dao.findBySearch( EvalHierarchyRule.class, searchObj );
        if( rules == null )
        {
            return Collections.emptyList();
        }
        else
        {
            return rules;
        }
    }

    /**
     * Utility method to take a list of EvalHierarchyRule objects from Hibernate,
     * and return a list of HierarchyNodeRule objects for use.
     * 
     * @param list the list of EvalHierarchyRule objects to be converted
     * @return a list of HierarchyNodeRule objects for use elsewhere
     */
    private List<HierarchyNodeRule> convertRules( List<EvalHierarchyRule> list )
    {
        List<HierarchyNodeRule> retVal = new ArrayList<>();
        list.stream().forEach( (rule) -> { retVal.add( new HierarchyNodeRule( rule ) ); } );
        return retVal;
    }

    /**
     * Utility method to take the raw rule text and apply the given qualifier
     * 
     * @param ruleText - the original rule text
     * @param qualifier - the qualifier to apply to the rule text
     * @return the final (qualified) rule text
     */
    private String applyQualifierToRuleText( String ruleText, String qualifier )
    {
        String finalRuleText = "";
        if( null != qualifier )
        {
            switch( qualifier )
            {
                case EvalConstants.HIERARCHY_QUALIFIER_CONTAINS:
                    finalRuleText = "%" + ruleText + "%";
                    break;
                case EvalConstants.HIERARCHY_QUALIFIER_ENDS_WITH:
                    finalRuleText = "%" + ruleText;
                    break;
                case EvalConstants.HIERARCHY_QUALIFIER_STARTS_WITH:
                    finalRuleText = ruleText + "%";
                    break;
                default:
                    finalRuleText = ruleText;
                    break;
            }
        }

        return finalRuleText;
    }
}

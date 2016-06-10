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
package org.sakaiproject.evaluation.logic.model;

import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.model.EvalHierarchyRule;

/**
 * This represents a single hierarchy rule in the system.
 */
public class HierarchyNodeRule implements Serializable
{
    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog( HierarchyNodeRule.class );

    private Long id;        // unique identifier
    private Long nodeID;    // the node this rule belongs to
    private String rule;    // the actual rule text (with qualifiers applied)
    private String option;  // the 'option' for the rule (site or section)

    // Default constructor needed for reflection
    public HierarchyNodeRule() {}

    public HierarchyNodeRule( Long nodeID, String rule, String option )
    {
        if( nodeID == null || StringUtils.isBlank( rule ) || StringUtils.isBlank( option ) )
        {
            IllegalArgumentException iae = new IllegalArgumentException( "None of the inputs can be null or blank: nodeID=" + nodeID + ", rule=" + rule 
                                                                         + ", option=" + option );
            LOG.warn( iae );
            throw iae;
        }

        this.id = null; // Default to null
        this.nodeID = nodeID;
        this.rule = rule;
        this.option = option;
    }

    public HierarchyNodeRule( EvalHierarchyRule rule )
    {
        if( rule == null || StringUtils.isBlank( rule.getRule() ) || StringUtils.isBlank( rule.getOpt() ) )
        {
            IllegalArgumentException iae = new IllegalArgumentException( "None of the inputs can be null or blank: EvalHierarchyRule=" + rule );
            LOG.warn( iae );
            throw iae;
        }

        this.id = rule.getId();
        this.nodeID = rule.getNodeID();
        this.rule = rule.getRule();
        this.option = rule.getOpt();
    }

    // Getters
    public Long     getId()     { return id; }
    public Long     getNodeID() { return nodeID; }
    public String   getRule()   { return rule; } 
    public String   getOption() { return option; }

    // Setters
    public void setId       (Long id)       { this.id = id; }
    public void setNodeID   (Long node)     { this.nodeID = node; }
    public void setRule     (String rule)   { this.rule = rule; }
    public void setOption   (String option) { this.option = option; }

    @Override
    public boolean equals( Object obj )
    {
        if( null == obj )
        {
            return false;
        }
        if( !( obj instanceof HierarchyNodeRule ) )
        {
            return false;
        }
        else
        {
            HierarchyNodeRule castObj = (HierarchyNodeRule) obj;
            boolean eq = (this.id == null ? castObj.id == null : this.id.equals( castObj.id ))
                    && (this.nodeID == null ? false : this.nodeID.equals( castObj.nodeID ))
                    && (this.rule == null ? false : this.rule.equals( castObj.rule ))
                    && (this.option == null ? false : this.option.equals( castObj.option ));
            return eq;
        }
    }

    @Override
    public int hashCode()
    {
        String hashStr = this.getClass().getName() + ":" + this.id + ":" + this.nodeID + ":" + this.rule + ":" + this.option;
        return hashStr.hashCode();
    }
}

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
package org.sakaiproject.evaluation.model;

import java.io.Serializable;

public class EvalHierarchyRule implements Serializable
{
    protected Long      id;
    protected Long      nodeID;
    protected String    rule;
    protected String    opt;

    // Default constructor
    public EvalHierarchyRule() {};

    // Parameterized constructor
    public EvalHierarchyRule( Long nodeID, String rule, String opt )
    {
        this.nodeID = nodeID;
        this.rule   = rule;
        this.opt    = opt;
    }

    // Getters
    public Long     getId()         { return this.id; }
    public Long     getNodeID()     { return this.nodeID; }
    public String   getRule()       { return this.rule; }
    public String   getOpt()        { return this.opt; }

    // Setters
    public void setId       ( Long id )     { this.id = id; }
    public void setNodeID   ( Long node )   { this.nodeID = node; }
    public void setRule     ( String rule ) { this.rule = rule; }
    public void setOpt      ( String opt )  { this.opt = opt; }
}

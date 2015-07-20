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

import org.sakaiproject.evaluation.constant.EvalConstants;


/**
 * This pea represents an evalGroup (often a site or group),
 * the eval group is a collection of users
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * The evalGroup ID string which is the unique identifier for this
	 */
	public String evalGroupId;
	/**
	 * The displayable title of this evalGroup
	 */
	public String title = "Unknown";
	/**
	 * The type of this evalGroup (use the GROUP_TYPE constants in {@link EvalConstants})
	 */
	public String type = EvalConstants.GROUP_TYPE_UNKNOWN;

	/**
	 * Empty Constructor
	 */
	public EvalGroup() {}

	/**
	 * Full Constructor
	 * 
	 * @param evalGroupId unique id for this evalGroup
	 * @param title the display title of this group
	 * @param type the type of this group
	 * (use the GROUP_TYPE constants in {@link EvalConstants})
	 */
	public EvalGroup(String evalGroupId, String title, String type) {
		this.evalGroupId = evalGroupId;
		this.title = title;
		this.type = type;
	}
    
    @Override
    public String toString() {
        return this.evalGroupId + ":title=" + this.title + ":type=" + this.type;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((evalGroupId == null) ? 0 : evalGroupId.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EvalGroup other = (EvalGroup) obj;
        if (evalGroupId == null) {
            if (other.evalGroupId != null)
                return false;
        } else if (!evalGroupId.equals(other.evalGroupId))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}

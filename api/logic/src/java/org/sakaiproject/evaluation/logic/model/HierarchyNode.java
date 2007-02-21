/******************************************************************************
 * HierarchyNode.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.model;

/**
 * This pea represents a node in a hierarchy (in academics a department or college would probably
 * be represented by a node)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class HierarchyNode {

	/**
	 * A unique id for this hierarchy node
	 */
	public String id;
	/**
	 * The title of this hierarchy node (may be null)<br/>
	 * e.g. Department of Computer Science
	 */
	public String title;
	/**
	 * The type of this hierarchy node (may be null)<br/>
	 * e.g. Department<br/>
	 * This will probably generally correspond to the level of the node but does not have to
	 */
	public String type;
	/**
	 * the level of this node (how many steps from the root node level),
	 * the root node would have a level of 0, a child of the root node would have
	 * a level of 1, etc.
	 */
	public int level = 0;
	/**
	 * true if this node has no children, false otherwise
	 */
	public boolean leaf = false;

	/**
	 * Empty constructor
	 */
	public HierarchyNode() {}

	/**
	 * Full constructor
	 * 
	 * @param id unique id for this hierarchy node
	 * @param title title of this hierarchy node
	 * @param type the classification for this hierarchy node (dept, etc.)
	 * @param level the level of this node (number of nodes in path from this to the root node)
	 * @param leaf true if this node has no children
	 */
	public HierarchyNode(String id, String title, String type, int level, boolean leaf) {
		this.id = id;
		this.title = title;
		this.type = type;
		this.level = level;
		this.leaf = leaf;
	}

	
}

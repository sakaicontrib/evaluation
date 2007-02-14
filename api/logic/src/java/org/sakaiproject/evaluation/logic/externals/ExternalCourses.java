/******************************************************************************
 * ExternalCourses.java - created by aaronz@vt.edu
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

package org.sakaiproject.evaluation.logic.externals;

import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.logic.model.Context;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * This interface provides methods to get external courses and enrollments into the
 * evaluation system<br/>,
 * This interface can and should be implemented and then spring loaded to allow
 * an institution to bring in external courses without needing to load them all
 * into Sakai or a similar system<br/>
 * <b>Note</b>: Specifically this allows us to reference courses and enrollments which
 * are not stored in something like Sakai
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface ExternalCourses {

	/**
	 * Get a list of all user ids that can take an evaluation or be evaluated in
	 * course(s) identified by a unique identifier (context)
	 * 
	 * @param contexts courses identified by unique strings
	 * @param permission a permission string PERM constant from {@link EvalConstants},
	 * <b>Note</b>: only take evaluation and be evaluated are supported
	 * @return a Set of Strings which represent the user Ids of all users in the course with that permission
	 */
	public Set getUserIdsForCourses(String[] contexts, String permission);

	/**
	 * Get a list of all user ids that can take an evaluation or be evaluated in
	 * course(s) identified by a unique identifier (context),
	 * <b>Note</b>: This is simply here for the sake of efficiency
	 * 
	 * @param contexts courses identified by unique strings
	 * @param permission a permission string PERM constant from {@link EvalConstants},
	 * <b>Note</b>: only take evaluation and be evaluated are supported
	 * @return the count of the users
	 */
	public int countUserIdsForCourses(String[] contexts, String permission);

	/**
	 * Get a list of all courses that a user can take an evaluation or be evaluated in,
	 * courses are identified by a unique identifier (context)
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string PERM constant from {@link EvalConstants},
	 * <b>Note</b>: only take evaluation and be evaluated are supported
	 * @return a List of {@link Context} objects
	 */
	public List getCoursesForUser(String userId, String permission);

	/**
	 * Get a count of all courses that a user can take an evaluation or be evaluated in,
	 * courses are identified by a unique identifier (context),
	 * <b>Note</b>: This is simply here for the sake of efficiency
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string PERM constant from {@link EvalConstants},
	 * <b>Note</b>: only take evaluation and be evaluated are supported
	 * @return the count of the courses
	 */
	public int countCoursesForUser(String userId, String permission);

	/**
	 * Return a Context object which represents a collection of users (a course 
	 * in this case) based on the unique id (context string)
	 * 
	 * @param context the unique id of an external course
	 * @return a Context object or null if not found
	 */
	public Context getContextByCourseId(String context);

}

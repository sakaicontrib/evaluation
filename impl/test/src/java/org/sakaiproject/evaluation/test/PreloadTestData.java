/******************************************************************************
 * PreloadDataImpl.java - created by aaronz@vt.edu
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

package org.sakaiproject.evaluation.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;


/**
 * This preloads data needed for testing<br/>
 * Do not load this data into a live or production database<br/>
 * Load this after the normal preload<br/>
 * Add the following (or something like it) to a spring beans def file:<br/>
 * <pre>
	<!-- create a test data preloading bean -->
	<bean id="org.sakaiproject.evaluation.test.PreloadTestData"
		class="org.sakaiproject.evaluation.test.PreloadTestData"
		init-method="init">
		<property name="evaluationDao"
			ref="org.sakaiproject.evaluation.dao.EvaluationDao" />
		<property name="pdl"
			ref="org.sakaiproject.evaluation.dao.PreloadData" />
	</bean>
 * </pre>
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class PreloadTestData {

	private static Log log = LogFactory.getLog(PreloadTestData.class);

	private EvaluationDao evaluationDao;
	public void setEvaluationDao(EvaluationDao evaluationDao) {
		this.evaluationDao = evaluationDao;
	}

	private EvalTestDataLoad etdl;
	/**
	 * @return the test data loading class with copies of all saved objects
	 */
	public EvalTestDataLoad getEtdl() {
		return etdl;
	}

	/**
	 * This is here because we need the empty DB data preload to run before this
	 * class and by making it a dependecy we ensure that is the case,
	 * Should be injecting a PreloadDataImpl here -AZ
	 */
	private Object pdl;
	public void setPdl(Object pdl) {
		this.pdl = pdl;
	}

	public void init() {
		log.info("INIT");
		if (pdl == null) {
			throw new NullPointerException("PreloadDataImpl must be loaded before this class");
		}
		preloadDB();
	}

	/**
	 * Preload the data
	 */
	public void preloadDB(){
		log.info("preloading DB...");
		etdl = new EvalTestDataLoad();
		etdl.saveAll(evaluationDao);
	}
}

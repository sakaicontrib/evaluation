/**
 * MockEvalExternalLogic.java - evaluation - Dec 25, 2006 10:07:31 AM - azeckoski
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.test.mocks;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.taskstream.client.TaskStatusStandardValues;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalScheduledJob;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.utils.EvalUtils;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


/**
 * This is a mock class for testing purposes, it will allow us to test all the classes
 * that depend on it since it has way to many external dependencies to make it worth
 * it to mock them all up<br/>
 * <br/>
 * It is emulating the following system state:<br/>
 * 4 users: ADMIN_USER_ID (super admin), MAINT_USER_ID, USER_ID, STUDENT_USER_ID
 * 2 sites:<br/>
 * 1) CONTEXT1/SITE_ID (Site) -
 * USER_ID can take eval, MAINT_USER_ID can admin evals and be evaluated (cannot take)<br/>
 * 2) CONTEXT2/SITE2_ID (Group) -
 * USER_ID and STUDENT_USER_ID can take eval, MAINT_USER_ID can be evaluated but can not admin (cannot take)<br/>
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class MockEvalExternalLogic implements EvalExternalLogic {
	
	public String getTaskStatusUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTaskStreamCount(String params) {
		StringBuilder xml = new StringBuilder();	
		xml.append("<taskStreamContainer>\n");
		xml.append("\t<streamCount>4</streamCount>\n");
		xml.append("\t<streams>\n");
		xml.append("\t</streams>\n");
		xml.append("</taskStreamContainer>\n");
		return xml.toString();
	}

	public String getTaskStatusContainer(String params) {
		
		StringBuilder xml = new StringBuilder();
		
		xml.append("<taskStreamContainer>");
		xml.append("\t<streamCount>3</streamCount>");
		xml.append("\t<streams>");
		xml.append("\n");
		xml.append("<taskStatusStream>\n");
		xml.append("<taskStatusStreamID>TSS20100408150833568</taskStatusStreamID>\n");
		xml.append("<created>Apr 8, 2010 3:08:33 PM</created>\n");
		xml.append("<time>Apr 8, 2010 3:11:08 PM</time>\n");
		xml.append("<status>FINISHED</status>\n");
		xml.append("<streamTag>Import</streamTag>\n");
		xml.append("<entryCount>4</entryCount>\n");
		xml.append("<statusEntries>\n");
		xml.append("\n");
		xml.append("<taskStatusEntry>\n");
		xml.append("<taskStatusStreamID>TSS20100408150833568</taskStatusStreamID>\n");
		xml.append("<taskStatusEntryID>TSE20100408150833570</taskStatusEntryID>\n");
		xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100408150833568/TSE20100408150833570/TSE20100408150833570</taskStatusEntryURL>\n");
		xml.append("<status>CREATED</status>\n");
		xml.append("<created>Apr 8, 2010 3:08:33 PM</created>\n");
		xml.append("<entryTag>no value</entryTag>\n");
		xml.append("<payload>no value</payload>\n");
		xml.append("</taskStatusEntry>\n");
		xml.append("\n");
		xml.append("<taskStatusEntry>\n");
		xml.append("<taskStatusStreamID>TSS20100408150833568</taskStatusStreamID>\n");
		xml.append("<taskStatusEntryID>TSE20100408150951240</taskStatusEntryID>\n");
		xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100408150833568/TSE20100408150951240/TSE20100408150951240</taskStatusEntryURL>\n");
		xml.append("<status>RUNNING</status>\n");
		xml.append("<created>Apr 8, 2010 3:09:51 PM</created>\n");
		xml.append("<entryTag>filename</entryTag>\n");
		xml.append("<payload>file1.txt</payload>\n");
		xml.append("</taskStatusEntry>\n");
		xml.append("\n");
		xml.append("<taskStatusEntry>\n");
		xml.append("<taskStatusStreamID>TSS20100408150833568</taskStatusStreamID>\n");
		xml.append("<taskStatusEntryID>TSE20100408151032081</taskStatusEntryID>\n");
		xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100408150833568/TSE20100408151032081/TSE20100408151032081</taskStatusEntryURL>\n");
		xml.append("<status>RUNNING</status>\n");
		xml.append("<created>Apr 8, 2010 3:10:32 PM</created>\n");
		xml.append("<entryTag>error</entryTag>\n");
		xml.append("<payload>my dumb error</payload>\n");
		xml.append("</taskStatusEntry>\n");
		xml.append("\n");
		xml.append("<taskStatusEntry>");
		xml.append("<taskStatusStreamID>TSS20100408150833568</taskStatusStreamID>\n");
		xml.append("<taskStatusEntryID>TSE20100408151108574</taskStatusEntryID>\n");
		xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100408150833568/TSE20100408151108574/TSE20100408151108574</taskStatusEntryURL>\n");
		xml.append("<status>FINISHED</status>\n");
		xml.append("<created>Apr 8, 2010 3:11:08 PM</created>\n");
		xml.append("<entryTag>withError</entryTag>\n");
		xml.append("<payload>file1.txt</payload>\n");
		xml.append("</taskStatusEntry>\n");
		xml.append("\n");
		xml.append("</statusEntries>\n");
		xml.append("</taskStatusStream>\n");
		xml.append("\n");
		xml.append("<taskStatusStream>\n");
		xml.append("<taskStatusStreamID>TSS20100408151132355</taskStatusStreamID>\n");
		xml.append("<created>Apr 8, 2010 3:11:32 PM</created>\n");
		xml.append("<time>Apr 8, 2010 3:12:48 PM</time>\n");
		xml.append("<status>FINISHED</status>\n");
		xml.append("<streamTag>Import</streamTag>\n");
		xml.append("<entryCount>3</entryCount>\n");
		xml.append("<statusEntries>\n");
		xml.append("\n");
		xml.append("<taskStatusEntry>\n");
		xml.append("<taskStatusStreamID>TSS20100408151132355</taskStatusStreamID>\n");
		xml.append("<taskStatusEntryID>TSE20100408151132356</taskStatusEntryID>\n");
		xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100408151132355/TSE20100408151132356/TSE20100408151132356</taskStatusEntryURL>\n");
		xml.append("<status>CREATED</status>\n");
		xml.append("<created>Apr 8, 2010 3:11:32 PM</created>\n");
		xml.append("<entryTag>no value</entryTag>\n");
		xml.append("<payload>no value</payload>\n");
		xml.append("</taskStatusEntry>\n");
		xml.append("\n");
		xml.append("<taskStatusEntry>\n");
		xml.append("<taskStatusStreamID>TSS20100408151132355</taskStatusStreamID>\n");
		xml.append("<taskStatusEntryID>TSE20100408151225382</taskStatusEntryID>\n");
		xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100408151132355/TSE20100408151225382/TSE20100408151225382</taskStatusEntryURL>\n");
		xml.append("<status>RUNNING</status>\n");
		xml.append("<created>Apr 8, 2010 3:12:25 PM</created>\n");
		xml.append("<entryTag>filename</entryTag>\n");
		xml.append("<payload>file2.txt</payload>\n");
		xml.append("</taskStatusEntry>\n");
		xml.append("\n");
		xml.append("<taskStatusEntry>\n");
		xml.append("<taskStatusStreamID>TSS20100408151132355</taskStatusStreamID>\n");
		xml.append("<taskStatusEntryID>TSE20100408151248499</taskStatusEntryID>\n");
		xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100408151132355/TSE20100408151248499/TSE20100408151248499</taskStatusEntryURL>\n");
		xml.append("<status>FINISHED</status>\n");
		xml.append("<created>Apr 8, 2010 3:12:48 PM</created>\n");
		xml.append("<entryTag>withoutError</entryTag>\n");
		xml.append("<payload>file2.txt</payload>\n");
		xml.append("</taskStatusEntry>\n");
		xml.append("\n");
		xml.append("</statusEntries>\n");
		xml.append("</taskStatusStream>\n");
		xml.append("\n");
		xml.append("<taskStatusStream>\n");
		xml.append("<taskStatusStreamID>TSS20100408151315568</taskStatusStreamID>\n");
		xml.append("<created>Apr 8, 2010 3:13:15 PM</created>\n");
		xml.append("<time>Apr 8, 2010 3:15:28 PM</time>\n");
		xml.append("<status>FINISHED</status>\n");
		xml.append("<streamTag>Import</streamTag>\n");
		xml.append("<entryCount>7</entryCount>\n");
		xml.append("<statusEntries>\n");
		xml.append("\n");
		xml.append("<taskStatusEntry>");
		xml.append("<taskStatusStreamID>TSS20100408151315568</taskStatusStreamID>\n");
		xml.append("<taskStatusEntryID>TSE20100408151315569</taskStatusEntryID>\n");
		xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100408151315568/TSE20100408151315569/TSE20100408151315569</taskStatusEntryURL>\n");
		xml.append("<status>CREATED</status>\n");
		xml.append("<created>Apr 8, 2010 3:13:15 PM</created>\n");
		xml.append("<entryTag>no value</entryTag>\n");
		xml.append("<payload>no value</payload>\n");
		xml.append("</taskStatusEntry>\n");
		xml.append("\n");
		xml.append("<taskStatusEntry>\n");
		xml.append("<taskStatusStreamID>TSS20100408151315568</taskStatusStreamID>\n");
		xml.append("<taskStatusEntryID>TSE20100408151355969</taskStatusEntryID>\n");
		xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100408151315568/TSE20100408151355969/TSE20100408151355969</taskStatusEntryURL>\n");
		xml.append("<status>RUNNING</status>\n");
		xml.append("<created>Apr 8, 2010 3:13:55 PM</created>\n");
		xml.append("<entryTag>filename</entryTag>\n");
		xml.append("<payload>file3.txt</payload>\n");
		xml.append("</taskStatusEntry>\n");
		xml.append("\n");
		xml.append("<taskStatusEntry>\n");
		xml.append("<taskStatusStreamID>TSS20100408151315568</taskStatusStreamID>\n");
		xml.append("<taskStatusEntryID>TSE20100408151424297</taskStatusEntryID>\n");
		xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100408151315568/TSE20100408151424297/TSE20100408151424297</taskStatusEntryURL>\n");
		xml.append("<status>RUNNING</status>\n");
		xml.append("<created>Apr 8, 2010 3:14:24 PM</created>\n");
		xml.append("<entryTag>error</entryTag>\n");
		xml.append("<payload>NullPointerError</payload>\n");
		xml.append("</taskStatusEntry>\n");
		xml.append("\n");
		xml.append("<taskStatusEntry>\n");
		xml.append("<taskStatusStreamID>TSS20100408151315568</taskStatusStreamID>\n");
		xml.append("<taskStatusEntryID>TSE20100408151440876</taskStatusEntryID>\n");
		xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100408151315568/TSE20100408151440876/TSE20100408151440876</taskStatusEntryURL>\n");
		xml.append("<status>RUNNING</status>\n");
		xml.append("<created>Apr 8, 2010 3:14:40 PM</created>\n");
		xml.append("<entryTag>error</entryTag>\n");
		xml.append("<payload>IllegalArgumentException</payload>\n");
		xml.append("</taskStatusEntry>\n");
		xml.append("\n");
		xml.append("<taskStatusEntry>\n");
		xml.append("<taskStatusStreamID>TSS20100408151315568</taskStatusStreamID>\n");
		xml.append("<taskStatusEntryID>TSE20100408151457486</taskStatusEntryID>\n");
		xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100408151315568/TSE20100408151457486/TSE20100408151457486</taskStatusEntryURL>\n");
		xml.append("<status>RUNNING</status>\n");
		xml.append("<created>Apr 8, 2010 3:14:57 PM</created>\n");
		xml.append("<entryTag>error</entryTag>\n");
		xml.append("<payload>IllegalArgumentException</payload>\n");
		xml.append("</taskStatusEntry>\n");
		xml.append("\n");
		xml.append("<taskStatusEntry>\n");
		xml.append("<taskStatusStreamID>TSS20100408151315568</taskStatusStreamID>\n");
		xml.append("<taskStatusEntryID>TSE20100408151502835</taskStatusEntryID>\n");
		xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100408151315568/TSE20100408151502835/TSE20100408151502835</taskStatusEntryURL>\n");
		xml.append("<status>RUNNING</status>\n");
		xml.append("<created>Apr 8, 2010 3:15:02 PM</created>\n");
		xml.append("<entryTag>error</entryTag>\n");
		xml.append("<payload>IllegalArgumentException</payload>\n");
		xml.append("</taskStatusEntry>\n");
		xml.append("\n");
		xml.append("<taskStatusEntry>\n");
		xml.append("<taskStatusStreamID>TSS20100408151315568</taskStatusStreamID>\n");
		xml.append("<taskStatusEntryID>TSE20100408151528564</taskStatusEntryID>\n");
		xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100408151315568/TSE20100408151528564/TSE20100408151528564</taskStatusEntryURL>\n");
		xml.append("<status>FINISHED</status>\n");	
		xml.append("<created>Apr 8, 2010 3:15:28 PM</created>\n");	
		xml.append("<entryTag>withError</entryTag>\n");		
		xml.append("<payload>file3.txt</payload>\n");	
		xml.append("</taskStatusEntry>\n");
		xml.append("\n");		
		xml.append("</statusEntries>\n");		
		xml.append("</taskStatusStream>\n");
		xml.append("\n");		
		xml.append("</streams>\n");		
		xml.append("</taskStreamContainer>\n");
		return xml.toString();
	}
		
		
		
		/*
		 * TODO update model object EvalTaskStatusContainer re status
		 * 0.0.16 the single "status" parameter is replaced by 2: "entryStatus" and "streamStatus". - search
		 * .TQ.Container model object EvalTaskStatusContainer has the single "status" parameter - returned
		
		
		xml.append("<taskStreamContainer>\n");
		xml.append("<streamCount>4</streamCount>\n");
		xml.append("<streams>\n");
		xml.append("\n\n");
		// stream 1
		xml.append("<taskStatusStream>\n");
			xml.append("<taskStatusStreamID>TSS20100328083232938</taskStatusStreamID>\n");
			xml.append("<created>Mar 28, 2010 8:32:32 AM</created>\n");
			xml.append("<time>Mar 28, 2010 8:35:32 AM</time>\n");
			xml.append("<status>FINISHED</status>\n");
			xml.append("<streamTag>Import</streamTag>\n");
			xml.append("<entryCount>3</entryCount>\n");
			xml.append("<statusEntries>\n");
				xml.append("\n\n");
				// entry 1
				xml.append("<taskStatusEntry>\n");
					xml.append("<taskStatusStreamID>TSS20100328083232938</taskStatusStreamID>\n");
					xml.append("<taskStatusEntryID>TSE20100328083232942</taskStatusEntryID>\n");
					xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100328083232938/TSE20100328083232942/TSE20100328083232942</taskStatusEntryURL>\n");
					xml.append("<status>CREATED</status>\n");
					xml.append("<created>Mar 28, 2010 8:32:32 AM</created>\n");
					xml.append("<entryTag>no value</entryTag>\n");
					xml.append("<payload>no value</payload>\n");
				xml.append("</taskStatusEntry>\n");
				xml.append("\n\n");
				// entry 2
				xml.append("<taskStatusEntry>\n");
					xml.append("<taskStatusStreamID>TSS20100328083232938</taskStatusStreamID>\n");
					xml.append("<taskStatusEntryID>TSE20100328083409815</taskStatusEntryID>\n");
					xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100328083232938/TSE20100328083409815/TSE20100328083409815</taskStatusEntryURL>\n");
					xml.append("<status>RUNNING</status>\n");
					xml.append("<created>Mar 28, 2010 8:34:09 AM</created>\n");
					xml.append("<entryTag>error</entryTag>\n");
					xml.append("<payload>NullPointerException</payload>\n");
					xml.append("</taskStatusEntry>\n");
				xml.append("\n\n");
				// entry 3
				xml.append("<taskStatusEntry>\n");
					xml.append("<taskStatusStreamID>TSS20100328083232938</taskStatusStreamID>\n");
					xml.append("<taskStatusEntryID>TSE20100328083532348</taskStatusEntryID>\n");
					xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100328083232938/TSE20100328083532348/TSE20100328083532348</taskStatusEntryURL>\n");
					xml.append("<status>FINISHED</status>\n");
					xml.append("<created>Mar 28, 2010 8:35:32 AM</created>\n");
					xml.append("<entryTag>withError</entryTag>\n");
					xml.append("<payload>TEST</payload>\n");
				xml.append("</taskStatusEntry>\n");
			xml.append(" </statusEntries>\n");
		xml.append("</taskStatusStream>\n");
		xml.append("\n\n");
		// stream 2
		xml.append("<taskStatusStream>\n");
				xml.append("<taskStatusStreamID>TSS20100328083601015</taskStatusStreamID>\n");
				xml.append("<created>Mar 28, 2010 8:36:01 AM</created>\n");
				xml.append("<time>Mar 28, 2010 8:36:01 AM</time>\n");
				xml.append("<status>CREATED</status>\n");
				xml.append("<streamTag>Import</streamTag>\n");
				xml.append("<entryCount>1</entryCount>\n");
				xml.append("<statusEntries>\n");
					xml.append("\n\n");
					// entry 1
					xml.append("<taskStatusEntry>\n");
						xml.append("<taskStatusStreamID>TSS20100328083601015</taskStatusStreamID>\n");
						// expected:<TSS20100328083[601015]> but was:<TSS20100328083[232938]>
						xml.append("<taskStatusEntryID>TSE20100328083601016</taskStatusEntryID>\n");
						xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100328083601015/TSE20100328083601016/TSE20100328083601016</taskStatusEntryURL>\n");
						xml.append("<status>CREATED</status>\n");
						xml.append("<created>Mar 28, 2010 8:36:01 AM</created>\n");
						xml.append("<entryTag>no value</entryTag>\n");
						xml.append("<payload>no value</payload>\n");
					xml.append("</taskStatusEntry>\n");
			xml.append("</statusEntries>\n");
		xml.append("</taskStatusStream>\n");
		xml.append("\n\n");
		// stream 3
		xml.append("<taskStatusStream>\n");
			xml.append("<taskStatusStreamID>TSS20100328083620593</taskStatusStreamID>\n");
			xml.append("<created>Mar 28, 2010 8:36:20 AM</created>\n");
			xml.append("<time>Mar 28, 2010 8:36:20 AM</time>\n");
			xml.append("<status>CREATED</status>\n");
			xml.append("<streamTag>Import</streamTag>\n");
			xml.append("<entryCount>1</entryCount>\n");
			xml.append("<statusEntries>\n");
				xml.append("\n\n");
				xml.append("<taskStatusEntry>\n");
					xml.append("<taskStatusStreamID>TSS20100328083620593</taskStatusStreamID>\n");
					xml.append("<taskStatusEntryID>TSE20100328083620594</taskStatusEntryID>\n");
					xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100328083620593/TSE20100328083620594/TSE20100328083620594</taskStatusEntryURL>\n");
					xml.append("<status>CREATED</status>\n");
					xml.append("<created>Mar 28, 2010 8:36:20 AM</created>\n");
					xml.append("<entryTag>no value</entryTag>\n");
					xml.append("<payload>no value</payload>\n");
				xml.append("</taskStatusEntry>\n");
			xml.append("</statusEntries>\n");
		xml.append("</taskStatusStream>\n");
		// stream 2
		xml.append("<taskStatusStream>\n");
			xml.append("<taskStatusStreamID>TSS20100328083655087</taskStatusStreamID>\n");
			xml.append("<taskStatusEntryID>TSE20100328083655088</taskStatusEntryID>\n");
			xml.append("<created>Mar 28, 2010 8:36:55 AM</created>\n");
			xml.append("<time>Mar 28, 2010 8:36:55 AM</time>\n");
			xml.append("<status>CREATED</status>\n");
			xml.append("<streamTag>Import</streamTag>\n");
			xml.append("<entryCount>1</entryCount>\n");
			xml.append("<statusEntries>\n");
				xml.append("<taskStatusEntry>\n");
					xml.append("<taskStatusStreamID>TSS20100328083655087</taskStatusStreamID>\n");
					xml.append("<taskStatusEntryID>TSE20100328083655088</taskStatusEntryID>\n");
					xml.append("<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100328083655087/TSE20100328083655088/TSE20100328083655088</taskStatusEntryURL>\n");
					xml.append("<status>CREATED</status>\n");
					xml.append("<created>Mar 28, 2010 8:36:55 AM</created>\n");
					xml.append("<entryTag>no value</entryTag>\n");
					xml.append("<payload>no value</payload>\n");
				xml.append("</taskStatusEntry>\n");
			xml.append("</statusEntries>\n");
		xml.append("</taskStatusStream>\n");
		xml.append(" </streams>\n");
		xml.append("</taskStreamContainer>\n");
		 */


	public String getTaskStatus(String params) {
		//TODO check value of params to return correct XML

		StringBuilder xml = new StringBuilder();
		xml.append("<taskStreamContainer>\n");
		xml.append("\t<streamCount>1</streamCount>\n");
		xml.append("\t<streams>\n");
		xml.append("\t\t<stream>\n");
		xml.append("\t\t\t<taskStatusStreamURL>http://localhost:8666/taskstatus/TSS20100322093828540</taskStatusStreamURL>\n");
		xml.append("\t\t\t<streamLength>4</streamLength>\n");
		xml.append("\t\t\t<streamTag>Import</streamTag>\n");
		xml.append("\t\t</stream>\n");
		xml.append("\t</streams>\n");
		xml.append("</taskStreamContainer>\n");
		
		/*
		 * 	<taskStreamContainer>Ã
			  	<streamCount>1</streamCount>Ã
				 <streams>Ã
					<taskStatusStream>
						<taskStatusStreamID>TSS20100325133341590</taskStatusStreamID>
			  			<created>Mar 25, 2010 1:33:41 PM</created>
			  			<time>Mar 25, 2010 1:33:41 PM</time>
			  			<status>CREATED</status>
			  	 		<streamTag></streamTag>
			  	   		<entryCount>1</entryCount>
				 		<statusEntries>
							<taskStatusEntry>
								<taskStatusStreamID>TSS20100325133341590</taskStatusStreamID>
								<taskStatusEntryID>TSE20100325133341596</taskStatusEntryID>
								<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100325133341590/TSE20100325133341596/TSE20100325133341596</taskStatusEntryURL>
			  					<status>CREATED</status>
			 					<created>Mar 25, 2010 1:33:41 PM</created>
			  					<entryTag>no value</entryTag>
			  					<payload>no value</payload>
							</taskStatusEntry>
			    		</statusEntries>
					</taskStatusStream>
			    </streams>
			</taskStreamContainer>
		 */
		
		return xml.toString();
	}


	public boolean updateTaskStatus(String params) {
		return true;
	}
	
	public String newTaskStatusEntry(
			String streamUrl,
			String entryTag,
			String status,
			String payload) {
		// TODO Auto-generated method stub
		return null;
	}

	public String newTaskStatusStream(String streamTag) {
		// TODO Auto-generated method stub
		return null;
	}
	



	/**
	 * Note: Admin has all perms in all sites
	 * 2 sites:<br/>
	 * 1) CONTEXT1/SITE_ID -
	 * USER_ID can take eval, MAINT_USER_ID can admin evals and be evaluated (cannot take)<br/>
	 * 2) CONTEXT2/SITE2_ID -
	 * USER_ID and STUDENT_USER_ID can take eval, MAINT_USER_ID can be evaluated but can not admin (cannot take)<br/>
	 */
	public int countEvalGroupsForUser(String userId, String permission) {
		if ( EvalTestDataLoad.ADMIN_USER_ID.equals(userId) ) {
			return 2;
		} else if ( EvalTestDataLoad.MAINT_USER_ID.equals(userId) ) {
			if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
				return 1;
			} else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
				return 2;
			} else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				return 0;				
			} else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
				return 1;
			} else {
				return 0;				
			}
		} else if ( EvalTestDataLoad.USER_ID.equals(userId) ) {
			if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
				return 0;
			} else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
				return 0;
			} else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				return 2;
			} else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
				return 0;
			} else {
				return 0;
			}
		} else if ( EvalTestDataLoad.STUDENT_USER_ID.equals(userId) ) {
			if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
				return 0;
			} else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
				return 0;
			} else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				return 1;
			} else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
				return 0;
			} else {
				return 0;
			}
		} else {
			// do nothing
		}
		return 0;
	}

	/**
	 * Note: Admin has all perms in all sites
	 * 2 sites:<br/>
	 * 1) CONTEXT1/SITE_ID -
	 * USER_ID can take eval, MAINT_USER_ID can admin evals and be evaluated (cannot take)<br/>
	 * 2) CONTEXT2/SITE2_ID -
	 * USER_ID and STUDENT_USER_ID can take eval, MAINT_USER_ID can be evaluated but can not admin (cannot take)<br/>
	 */
	public List<EvalGroup> getEvalGroupsForUser(String userId, String permission) {
		List<EvalGroup> l = new ArrayList<EvalGroup>();
		if ( EvalTestDataLoad.ADMIN_USER_ID.equals(userId) ) {
			l.add( makeEvalGroupObject(EvalTestDataLoad.SITE1_REF) );
			l.add( makeEvalGroupObject(EvalTestDataLoad.SITE2_REF) );
		} else if ( EvalTestDataLoad.MAINT_USER_ID.equals(userId) ) {
			if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
				l.add( makeEvalGroupObject(EvalTestDataLoad.SITE1_REF) );
			} else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
				l.add( makeEvalGroupObject(EvalTestDataLoad.SITE1_REF) );
				l.add( makeEvalGroupObject(EvalTestDataLoad.SITE2_REF) );
			} else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				// nothing
			} else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
				l.add( makeEvalGroupObject(EvalTestDataLoad.SITE1_REF) );
			} else {
				// nothing
			}
		} else if ( EvalTestDataLoad.USER_ID.equals(userId) ) {
			if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
				// nothing
			} else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
				// nothing
			} else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				l.add( makeEvalGroupObject(EvalTestDataLoad.SITE1_REF) );
				l.add( makeEvalGroupObject(EvalTestDataLoad.SITE2_REF) );
			} else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
				// nothing
			} else {
				// nothing
			}
		} else if ( EvalTestDataLoad.STUDENT_USER_ID.equals(userId) ) {
			if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
				// nothing
			} else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
				// nothing
			} else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				l.add( makeEvalGroupObject(EvalTestDataLoad.SITE2_REF) );
			} else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
				// nothing
			} else {
				// nothing
			}
		} else {
			// do nothing
		}
		return l;
	}
	
	public Map<String, List<EvalGroup>> getEvalGroupsForUser(String userId) {
		Map<String, List<EvalGroup>> map = new HashMap<String, List<EvalGroup>>();
		List<EvalGroup> take = new ArrayList();
		take.add( makeEvalGroupObject(EvalTestDataLoad.SITE1_REF) );
		map.put(EvalConstants.PERM_TAKE_EVALUATION, take);
		List<EvalGroup> be = new ArrayList();
		be.add( makeEvalGroupObject(EvalTestDataLoad.SITE2_REF) );
		map.put(EvalConstants.PERM_BE_EVALUATED, be);
		return map;
	}

	/**
	 * Note: Admin has all perms in all sites
	 * 2 sites:<br/>
	 * 1) CONTEXT1/SITE_ID -
	 * USER_ID can take eval, MAINT_USER_ID can admin evals and be evaluated (cannot take)<br/>
	 * 2) CONTEXT2/SITE2_ID -
	 * USER_ID and STUDENT_USER_ID can take eval, MAINT_USER_ID can be evaluated but can not admin (cannot take)<br/>
	 */
	public Set<String> getUserIdsForEvalGroup(String context, String permission) {
		Set<String> s = new HashSet<String>();
		// Maybe should add the admin user here? -AZ
		if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
			if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
				s.add(EvalTestDataLoad.MAINT_USER_ID);
			} else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
				s.add(EvalTestDataLoad.MAINT_USER_ID);
			} else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				s.add(EvalTestDataLoad.USER_ID);
			} else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
				s.add(EvalTestDataLoad.MAINT_USER_ID);
			} else {
				// nothing
			}
		} else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
			if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
				// nothing
			} else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
				s.add(EvalTestDataLoad.MAINT_USER_ID);
			} else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				s.add(EvalTestDataLoad.USER_ID);
				s.add(EvalTestDataLoad.STUDENT_USER_ID);
			} else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
				// nothing
			} else {
				// nothing
			}
		} else {
			// do nothing
		}
		return s;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.ExternalContexts#countUserIdsForContext(java.lang.String, java.lang.String)
	 */
	public int countUserIdsForEvalGroup(String context, String permission) {
		// just use the other stub method
		return getUserIdsForEvalGroup(context, permission).size();
	}

	/**
	 * Note: Admin has all perms in all sites
	 * 2 sites:<br/>
	 * 1) CONTEXT1/SITE_ID -
	 * USER_ID can take eval, MAINT_USER_ID can admin evals and be evaluated (cannot take)<br/>
	 * 2) CONTEXT2/SITE2_ID -
	 * USER_ID and STUDENT_USER_ID can take eval, MAINT_USER_ID can be evaluated but can not admin (cannot take)<br/>
	 */
	public boolean isUserAllowedInEvalGroup(String userId, String permission, String context) {
		if ( EvalTestDataLoad.ADMIN_USER_ID.equals(userId) ) {
			return true;
		} else if ( EvalTestDataLoad.MAINT_USER_ID.equals(userId) ) {
			if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
				if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
					return true;
				} else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
					return false;					
				}
			} else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
				if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
					return true;
				} else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
					return true;					
				}
			} else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
					return false;
				} else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
					return false;					
				}
			} else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
				if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
					return true;
				} else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
					return false;					
				}
			}
		} else if ( EvalTestDataLoad.USER_ID.equals(userId) ) {
			if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
				if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
					return false;
				} else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
					return false;					
				}
			} else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
				if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
					return false;
				} else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
					return false;					
				}
			} else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
					return true;
				} else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
					return true;					
				}
			} else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
				if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
					return false;
				} else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
					return false;					
				}
			}
		} else if ( EvalTestDataLoad.STUDENT_USER_ID.equals(userId) ) {
			if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
				if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
					return false;
				} else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
					return false;					
				}
			} else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
				if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
					return false;
				} else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
					return false;					
				}
			} else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
					return false;
				} else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
					return true;					
				}
			} else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
				if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
					return false;
				} else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
					return false;					
				}
			}
		} else {
			// do nothing
		}
		return false;
	}

	/**
	 * Always assume the current evalGroupId is CONTEXT1
	 */
	public String getCurrentEvalGroup() {
		return currentGroupId;
	}

	/**
	 * Always assume the current user is USER_ID
	 */
	public String getCurrentUserId() {
		return currentUserId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.ExternalUsers#isUserAnonymous(java.lang.String)
	 */
	public boolean isUserAnonymous(String userId) {
		if (userId.equals(EvalTestDataLoad.USER_ID) 
		      ||	userId.equals(EvalTestDataLoad.MAINT_USER_ID) 
		      ||	userId.equals(EvalTestDataLoad.ADMIN_USER_ID)
				|| userId.equals(EvalTestDataLoad.STUDENT_USER_ID)) {
			return false;
		}
		return true;
	}

   public EvalUser getEvalUserById(String userId) {
      EvalUser user = new EvalUser(userId, EvalConstants.USER_TYPE_INVALID, null);
      if ( EvalTestDataLoad.ADMIN_USER_ID.equals(userId) ) {
         user = new EvalUser(userId, EvalConstants.USER_TYPE_INTERNAL, userId + "@institution.edu",
               EvalTestDataLoad.ADMIN_USER_NAME, EvalTestDataLoad.ADMIN_USER_DISPLAY);
      } else if ( EvalTestDataLoad.MAINT_USER_ID.equals(userId) ) {
         user = new EvalUser(userId, EvalConstants.USER_TYPE_EXTERNAL, userId + "@institution.edu",
               EvalTestDataLoad.MAINT_USER_NAME, EvalTestDataLoad.MAINT_USER_DISPLAY);
      } else if ( EvalTestDataLoad.USER_ID.equals(userId) ) {
         user = new EvalUser(userId, EvalConstants.USER_TYPE_EXTERNAL, userId + "@institution.edu",
               EvalTestDataLoad.USER_NAME, EvalTestDataLoad.USER_DISPLAY);
      } else if ( EvalTestDataLoad.STUDENT_USER_ID.equals(userId) ) {
         user = new EvalUser(userId, EvalConstants.USER_TYPE_EXTERNAL, userId + "@institution.edu",
               EvalTestDataLoad.STUDENT_USER_ID, EvalTestDataLoad.STUDENT_USER_ID + "name");
      }
      return user;
   }

   public EvalUser getEvalUserByEmail(String email) {
      String userId = email; // userId + "@institution.edu"
      int position = userId.indexOf('@');
      if (position != -1) {
         userId = userId.substring(0, position);
      }
      EvalUser user = getEvalUserById(userId);
      return user;
   }

	/**
	 * Return usernames from the data load class
	 */
	public String getUserUsername(String userId) {
		if ( EvalTestDataLoad.ADMIN_USER_ID.equals(userId) ) {
			return EvalTestDataLoad.ADMIN_USER_NAME;
		} else if ( EvalTestDataLoad.MAINT_USER_ID.equals(userId) ) {
			return EvalTestDataLoad.MAINT_USER_NAME;			
		} else if ( EvalTestDataLoad.USER_ID.equals(userId) ) {
			return EvalTestDataLoad.USER_NAME;			
		}
		return "------";
	}
	
	public String getEvalToolTitle() {
		return "Evaluation System";
	}

    public String getUserId(String username) {
        if ( EvalTestDataLoad.ADMIN_USER_NAME.equals(username) ) {
            return EvalTestDataLoad.ADMIN_USER_ID;
        } else if ( EvalTestDataLoad.MAINT_USER_NAME.equals(username) ) {
            return EvalTestDataLoad.MAINT_USER_ID;            
        } else if ( EvalTestDataLoad.USER_NAME.equals(username) ) {
            return EvalTestDataLoad.USER_ID;          
        }
        return null;
    }

	/**
	 * only true for ADMIN_USER_ID
	 */
	public boolean isUserAdmin(String userId) {
		if ( EvalTestDataLoad.ADMIN_USER_ID.equals(userId) ) {
			return true;
		}
		return false;
	}

	public Locale getUserLocale(String userId) {
		return Locale.US;
	}

   /**
    * Return titles from the data load class
    */
   public String getDisplayTitle(String evalGroupId) {
      if ( EvalTestDataLoad.SITE1_REF.equals(evalGroupId) ) {
         return EvalTestDataLoad.SITE1_TITLE;
      } else if ( EvalTestDataLoad.SITE2_REF.equals(evalGroupId) ) {
         return EvalTestDataLoad.SITE2_TITLE;         
      }
      return "--------";
   }

	/**
	 * Return Context objects based on data from the data load class
	 * CONTEXT1 = Site, CONTEXT2 = Group
	 */
	public EvalGroup makeEvalGroupObject(String context) {
		EvalGroup c = new EvalGroup(context, null, EvalConstants.GROUP_TYPE_UNKNOWN);
		if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
			c.title = EvalTestDataLoad.SITE1_TITLE;
			c.type = EvalConstants.GROUP_TYPE_SITE;
		} else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
			c.title = EvalTestDataLoad.SITE2_TITLE;			
			c.type = EvalConstants.GROUP_TYPE_GROUP;
		}
		return c;
	}

	public String[] sendEmailsToUsers(String from, String[] to, String subject, String message, boolean deferExceptions) {
		if (from == null || to == null || subject == null || message == null) {
			throw new NullPointerException("All params are required (none can be null)");
		}
      emailsSentCounter += to.length;
      return to;
	}

   public String[] sendEmailsToAddresses(String from, String[] to, String subject, String message,
         boolean deferExceptions) {
      if (from == null || to == null || subject == null || message == null) {
         throw new NullPointerException("All params are required (none can be null)");
      }
      emailsSentCounter += to.length;
      return to;
   }

	public String getServerUrl() {
		return "http://localhost:8080/portal/";
	}

	public String getEntityURL(Serializable evaluationEntity) {
		return "http://localhost:8080/access/eval-evaluation/123/";
	}

	public String getEntityURL(String entityPrefix, String entityId) {
		return getEntityURL(null);
	}

	public void registerEntityEvent(String eventName, Serializable evaluationEntity) {
		// pretending it worked
	}

   public void registerEntityEvent(String eventName, Class<? extends Serializable> entityClass, String entityId) {
      // pretending it worked
   }



   @SuppressWarnings("unchecked")
   public <T> T getConfigurationSetting(String settingName, T defaultValue) {
      T returnValue = defaultValue;
      if (defaultValue == null) {
         returnValue = (T) (settingName + ":NULL");
      }
      return returnValue;
   }

   public byte[] getFileContent(String abspath) {
      return new byte[] {'H','E','L','L','O',' ','E','V','A','L','U','A','T','I','O','N' };
   }

   public String cleanupUserStrings(String userSubmittedString) {
      return EvalUtils.cleanupHtmlPtags(userSubmittedString);
   }

   public String makePlainTextFromHTML(String html) {
      return html;
   }



   // FOR scheduling
   // the methods below should return some fake jobs which will correctly correspond to the jobs one would expect to find
   // in the system based on the current test dats (i.e. maybe 8 or so jobs should exist based on the current test data)
   
   public String createScheduledJob(Date executionDate, Long evaluationId, String jobType) {
      // TODO - make these return some fake data for testing - Dick should do this when he writes tests for this code
      throw new NotImplementedException();
   }

   public void deleteScheduledJob(String jobID) {
      // TODO - make these return some fake data for testing - Dick should do this when he writes tests for this code
      throw new NotImplementedException();
   }

   public EvalScheduledJob[] findScheduledJobs(Long evaluationId, String jobType) {
      // TODO - make these return some fake data for testing - Dick should do this when he writes tests for this code
      throw new NotImplementedException();
   }


	// testing methods

   private int emailsSentCounter = 0;
   /**
    * TESTING method:
    * Provides a way to determine the number of emails sent via this mock service since the service started up
    * or the counter was reset, reset the counter using {@link #resetEmailsSentCounter()}
    * @return
    */
   public int getNumEmailsSent() {
      return emailsSentCounter;
   }
   /**
    * TESTING method:
    * Resets the emails sent test counter to 0
    */
   public void resetEmailsSentCounter() {
      emailsSentCounter = 0;
   }

   private String currentUserId = EvalTestDataLoad.USER_ID;
   public void setCurrentUserId(String userId) {
      currentUserId = userId;
   }

   private String currentGroupId = EvalTestDataLoad.SITE1_REF;
   public void setCurrentGroupId(String evalGroupId) {
      currentGroupId = evalGroupId;
   }

   public <T> T getBean(Class<T> type) {
      // TODO Auto-generated method stub
      return null;
   }

   public Map<String, EvalUser> getEvalUsersByIds(String[] userIds) {
      Map<String, EvalUser> users = new HashMap<String, EvalUser>();
      for (String userId : userIds) {
         users.put(userId, getEvalUserById(userId) );
      }
      return users;
   }
	
	public String getMyWorkspaceUrl(String userId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void clearBindings() {
		// TODO Auto-generated method stub
		
	}
	
	public void setSessionActive() {
		// TODO Auto-generated method stub
		
	}
	
	public boolean setSessionUserIdAdmin(String uid) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean checkResource(String resourceId) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public String getImportedResourceId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public InputStream getStreamContent(String resourceId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean removeResource(String resourceId) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void setImportedResourceAttributes() {
		// TODO Auto-generated method stub
	}
	
	public String getSectionTitle(String providerId) {
		return null;
	}
	
	public EvalUser getEvalUserByEid(String eid) {
		return null;
	}

}

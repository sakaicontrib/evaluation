/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.evaluation.logic.impl.scheduling;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.logic.externals.EvalExport;
import org.sakaiproject.evaluation.logic.externals.EvalExportLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;


/**
 * Deprecated after U-M Pilot I, replaced by Oracle DBLINK in Pilot II
 * @author rwellis
 *
 */
public class EvalExportImpl implements EvalExport {
	
	/*TODO access ContentServices through external logic
	  	private EvalExportJob evalExportJob;
		public void setEvalExportJob(EvalExportJob evalExportJob) {
			this.evalExportJob = evalExportJob;
		}
	*/
	
	private static Log log = LogFactory.getLog(EvalExportImpl.class);
	
	private int numPersisted;
	
	private Calendar cal;
	private SimpleDateFormat formatter;

	private ContentHostingService contentHostingService;
	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}
	
	private EvaluationDao dao;
	public void setDao(EvaluationDao dao) {
		this.dao = dao;
	}
	
	private EvalEmailsLogic emails;
	public void setEmails(EvalEmailsLogic emails) {
		this.emails = emails;
	}
	
	//EvalExternalLogic.isAdmin(Sakai_id)  EvalExternalLogic.sendEmails()
	
	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	
	private EvalExportLogic evalExportLogic;
	public void setEvalExportLogic(EvalExportLogic evalExportLogic) {
		this.evalExportLogic = evalExportLogic;
	}
	
	private EvalEvaluationsLogic evalEvaluationsLogic;
	public void setEvalEvaluationsLogic(EvalEvaluationsLogic evalEvaluationsLogic) {
		this.evalEvaluationsLogic = evalEvaluationsLogic;
	}
	
	private EvalResponsesLogic evalResponsesLogic;
	public void setEvalResponsesLogic(EvalResponsesLogic evalResponsesLogic) {
		this.evalResponsesLogic = evalResponsesLogic;
	}
	
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	private ToolManager toolManager;
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}
	
	private ThreadLocalManager threadLocalManager;
	public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
		this.threadLocalManager = threadLocalManager;
	}
	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
	
	//open tags
	public static final String EVAL_DATA = "<EVAL_DATA>\n";
	public static final String EVAL_GROUPS_NODE = "<EVAL_GROUPS>\n";
	public static final String EVAL_GROUP_NODE = "<EVAL_GROUP>\n";
	public static final String EVAL_GROUP_ID_NODE = "<EVAL_GROUP_ID>";
	public static final String EVAL_INSTRUCTOR_NODE = "<EVAL_INSTRUCTOR>";
	public static final String EVAL_EVALUATIONS_NODE = "<EVAL_EVALUATIONS>\n";
	public static final String EVAL_EVALUATION_NODE  = "<EVAL_EVALUATION>\n";
	public static final String EVAL_EVALUATION_EID_NODE  = "<EVAL_EVALUATION_EID>";
	public static final String EVAL_RESPONSES_NODE = "<EVAL_RESPONSES>\n";
	public static final String EVAL_RESPONSE_NODE = "<EVAL_RESPONSE>\n";
	public static final String EVAL_ITEMS_NODE = "<EVAL_ITEMS>\n";
	public static final String EVAL_ITEM_NODE = "<EVAL_ITEM>\n";
	public static final String EVAL_ITEM_EID_NODE = "<EVAL_ITEM_EID>";
	public static final String EVAL_ITEM_TYPE_NODE = "<EVAL_ITEM_TYPE>";
	public static final String EVAL_SCALE_OPTION_NODE = "<EVAL_SCALE_OPTION>";
	public static final String EVAL_SCALE_OPTIONS_NODE = "<EVAL_SCALE_OPTIONS>";
	public static final String EVAL_ANSWER_NODE = "<EVAL_ANSWER>";
	public static final String EVAL_ITEM_TEXT_NODE = "<EVAL_ITEM_TEXT>";
	//close tags
	public static final String CLOSE_EVAL_DATA = "</EVAL_DATA>\n";
	public static final String CLOSE_EVAL_GROUPS_NODE = "</EVAL_GROUPS>\n";
	public static final String CLOSE_EVAL_GROUP_NODE = "</EVAL_GROUP>\n";
	public static final String CLOSE_EVAL_GROUP_ID_NODE = "</EVAL_GROUP_ID>\n";
	public static final String CLOSE_EVAL_INSTRUCTOR_NODE = "</EVAL_INSTRUCTOR>\n";
	public static final String CLOSE_EVAL_EVALUATIONS_NODE = "</EVAL_EVALUATIONS>\n";
	public static final String CLOSE_EVAL_EVALUATION_NODE  = "</EVAL_EVALUATION>\n";
	public static final String CLOSE_EVAL_EVALUATION_EID_NODE  = "</EVAL_EVALUATION_EID>\n";
	public static final String CLOSE_EVAL_RESPONSES_NODE = "</EVAL_RESPONSES>\n";
	public static final String CLOSE_EVAL_RESPONSE_NODE = "</EVAL_RESPONSE>\n";
	public static final String CLOSE_EVAL_ITEMS_NODE = "</EVAL_ITEMS>\n";
	public static final String CLOSE_EVAL_ITEM_NODE = "</EVAL_ITEM>\n";
	public static final String CLOSE_EVAL_ITEM_EID_NODE = "</EVAL_ITEM_EID>\n";
	public static final String CLOSE_EVAL_ITEM_TYPE_NODE = "</EVAL_ITEM_TYPE>\n";
	public static final String CLOSE_EVAL_SCALE_OPTION_NODE = "</EVAL_SCALE_OPTION>\n";
	public static final String CLOSE_EVAL_SCALE_OPTIONS_NODE = "</EVAL_SCALE_OPTIONS>\n";
	public static final String CLOSE_EVAL_ANSWER_NODE = "</EVAL_ANSWER>\n";
	public static final String CLOSE_EVAL_ITEM_TEXT_NODE = "</EVAL_ITEM_TEXT>\n";

	public void init() {
		formatter = new SimpleDateFormat("MMMM dd yyyy hh mm ss aa");
		cal = Calendar.getInstance();
    }
	
	public void evalExportImpl() {
	}
	
	/**
	 * Write responsess for all eval groups to ContentResiurce
	 */
	public List<String> writeResponses() {
		List<String> messages = new ArrayList<String>();
		try
		{
			String mySiteId = getSiteId();
			String resourceTitle = "Raw Data " + getTime();
			byte[] content = writeGroups();
			writeContent(mySiteId, content, "text/plain", resourceTitle);
		}
		catch(Exception e) {
			log.error("writeResponses(): " + e);
		}
		evalExportLogic.setLock(false);
		return messages;
	}
	
	private byte[] writeGroups() {
		StringWriter sw = new StringWriter();
		StringBuffer buf = sw.getBuffer();
		try{
			buildGroups(buf);
		}
		catch(Exception e) {
			log.error("writeGroups(): " + e);
		}
		return buf.toString().getBytes();
	}
	
	private void buildGroups(StringBuffer buf) {
		EvalAssignGroup assignGroup = null;
		EvalEvaluation evaluation = null;
		EvalResponse evalResponse = null;
		Set<EvalAnswer> answers = null;
		EvalAnswer answer = null;
		EvalItem item = null;
		String groupId = null;
		String evalEid = null;
		String itemEid = null;
		String numericAnswer = null;
		numPersisted = 0;

		//write root
		buf.append(EVAL_DATA);
		
		//get all groups
		List<EvalAssignGroup> evalAssignGroups = dao.getEvalAssignGroups();
		
		//write groups node
		buf.append(EVAL_GROUPS_NODE);
		for(Iterator<EvalAssignGroup> i = evalAssignGroups.iterator(); i.hasNext();) {

			try{
				//ping session to keep it alive
				numPersisted++;
				if(numPersisted % 100 == 0) {
					Session session = sessionManager.getCurrentSession();
					session.setActive();
					if(log.isInfoEnabled())
						log.info("Groups written to XML: " + (new Integer(numPersisted)).toString());
				}
				
				//get a group
				assignGroup = i.next();
				groupId = assignGroup.getEvalGroupId();
				
				//TODO late opt-out of two courses of pilot by Peter Washabaugh, need UI to exclude specific sections
				if((groupId.equals("2007,2,A,ENGR,100,700") || groupId.equals("2007,2,A,ENGR,100,900"))) 
						continue;
				
				//write group node
				buf.append(EVAL_GROUP_NODE);
		
				//write group id
				buf.append(EVAL_GROUP_ID_NODE);
				buf.append(groupId);
				//close group id
				buf.append(CLOSE_EVAL_GROUP_ID_NODE);
				
				//get all evaluations for a group
				List<EvalEvaluation> evaluations = evalEvaluationsLogic.getEvaluationsByGroupId(groupId);
				
				//write evaluations node
				buf.append(EVAL_EVALUATIONS_NODE);
				for(Iterator<EvalEvaluation> j = evaluations.iterator(); j.hasNext();) {
	
					//write evaluation node
					buf.append(EVAL_EVALUATION_NODE);
						
					try {
						evaluation = j.next();
					
						//write evaluation eid
						buf.append(EVAL_EVALUATION_EID_NODE);
						evalEid = evaluation.getEid();
						buf.append(evalEid);
						//close evaluation eid
						buf.append(CLOSE_EVAL_EVALUATION_EID_NODE);
						
						//get all responses to this evaluation
						buf.append(EVAL_RESPONSES_NODE);
						Set<EvalResponse> responses = evaluation.getResponses();
						for(Iterator<EvalResponse> k = responses.iterator(); k.hasNext();) {

								//write response node
								buf.append(EVAL_RESPONSE_NODE);
								try{
									evalResponse = k.next();
								
									//get all answers
									answers = evalResponse.getAnswers();
									
									//write items node
									buf.append(EVAL_ITEMS_NODE);
									for (Iterator <EvalAnswer> l = answers.iterator(); l.hasNext();) {
										answer = l.next();
										item = answer.getItem();
										
										//write item node
										buf.append(EVAL_ITEM_NODE);
										
										//write item eid
										buf.append(EVAL_ITEM_EID_NODE);
										itemEid = item.getEid();
										buf.append(itemEid);
										buf.append(CLOSE_EVAL_ITEM_EID_NODE);
										
										//write answer
										numericAnswer = answer.getNumeric().toString();
										buf.append(EVAL_ANSWER_NODE);
										buf.append(numericAnswer);
										buf.append(CLOSE_EVAL_ANSWER_NODE);
										
										//close item node
										buf.append(CLOSE_EVAL_ITEM_NODE);
									}//EvalAnswer
										
									//close items node
									buf.append(CLOSE_EVAL_ITEMS_NODE);
									//close response node
									buf.append(CLOSE_EVAL_RESPONSE_NODE);
								}
								catch(Exception e) {
									//skip bad response
									buf.append(CLOSE_EVAL_RESPONSE_NODE);
									log.error("group '" + groupId + "' skip bad response: " + e);
									continue;
								}
							}//EvalResponse

							//close all responses to this evaluation
							buf.append(CLOSE_EVAL_RESPONSES_NODE);
							//close evaluation node
							buf.append(CLOSE_EVAL_EVALUATION_NODE);
						}
						catch(Exception e) {
							//skip bad evaluation
							buf.append(CLOSE_EVAL_EVALUATION_NODE);
							log.error("group '" + groupId + "' skip bad evaluation: " + e);
							continue;
						}
					}//EvalEvaluation

					//close evaluations node
					buf.append(CLOSE_EVAL_EVALUATIONS_NODE);
					//close group node
					buf.append(CLOSE_EVAL_GROUP_NODE);
				}
				catch(Exception e) {
					//skip bad group
					buf.append(CLOSE_EVAL_GROUP_NODE);
					log.error("group '" + groupId + "' skip bad group: " + e);
					continue;
				}
	
			}//EvalAssignGroup

			//close groups node
			buf.append(CLOSE_EVAL_GROUPS_NODE);
			//close root
			buf.append(CLOSE_EVAL_DATA);
			
			if(log.isInfoEnabled())
				log.info("Final count of Groups written to XML: " + (new Integer(numPersisted)).toString());
	}
	
	/**
	* Create a static copy of the evaluation data under Resources
	*/
	public boolean writeContent(String siteUuid, byte[] content, String contentType, String resourceTitle)
	{
		if(siteUuid == null || content == null || contentType == null ||  resourceTitle == null)
			return false;
		
		boolean retVal = false;
		try
		{
			//TODO append date + time to resourceTitle
			WriteContent s = new WriteContent(siteUuid, content, contentType, resourceTitle);
			Thread t = new Thread(s);
			t.start();
			retVal = true;
		}
		catch(Exception e)
		{
			retVal = false;
		}
		return retVal;
	}
	
	/** 
	 * Start a session to create content. 
	 * 
	 * */
	protected class WriteContent implements Runnable
	{
		byte[] content = null;
		String contentType = null;
		String resourceTitle = null;
		String siteUuid = null;
		
		public void init(){}
		public void start(){}
		
		WriteContent(String siteUuid, byte[] content, String contentType, String resourceTitle)
		{
			this.siteUuid = siteUuid;
			this.content = content;
			this.contentType = contentType;
			this.resourceTitle = resourceTitle;
		}

		public void run()
		{
		    try
			{
				// set the current user to admin
				Session s = sessionManager.getCurrentSession();
				if (s != null)
				{
					s.setUserId(UserDirectoryService.ADMIN_ID);
				}
				else
				{
					//Log.warn("chef", this + ".run() - Session is null, cannot set user id to ADMIN_ID user");
				}

				if(siteUuid != null)
				{
					ContentResourceEdit edit = null;
					String collectionId = null;
					if(content != null)
					{
						//check Resources collection is accessible
						collectionId = contentHostingService.getSiteCollection(siteUuid);
						try
						{
							contentHostingService.checkCollection(collectionId);
							
							//save the content in Resources
							try
							{
								edit = contentHostingService.addResource(collectionId + resourceTitle);
								edit.setContent(content);
								edit.setContentType(contentType);
								edit.setContentLength(content.length);
								ResourcePropertiesEdit props = edit.getPropertiesEdit();
								props.addProperty(ResourceProperties.PROP_DISPLAY_NAME,resourceTitle);
								props.addProperty(ResourceProperties.PROP_DESCRIPTION, resourceTitle);
								contentHostingService.commitResource(edit);
							}
							catch(Exception e)
							{
								if(edit != null && edit.isActiveEdit())
								{
									try
									{
										contentHostingService.removeResource(edit);
									}
									catch(Exception ee)
									{
										if(log.isWarnEnabled())
											log.warn(this + ".run removeResource " + e);
									}
								}
								if(log.isWarnEnabled())
									log.warn(this + ".run addResource " + collectionId + resourceTitle + " " + e);
							}
						}
						catch (Exception e)
						{
							if(log.isWarnEnabled())
								log.warn(this + ".run checkCollection " + collectionId + " " + e);
						}
					}
				}
		    }
		    finally
			{
				//clear any current bindings
				threadLocalManager.clear();
			}
		}
	}
	
		/**
		 * Get the site id of current user's site
		 * 
		 * @return site id
		 */
		private String getSiteId() {
			String id = null;
			try{
				id = ((Site)siteService.getSite(toolManager.getCurrentPlacement().getContext())).getId();
			}
			catch(Exception e){
				log.error("getSiteId(): " + e);
			}
			return id;
		}
		
		/**
		 * A utility method to create a user-readable time
		 * 
		 * @return the current time String
		 */
		protected String getTime()
		{
			long millis = System.currentTimeMillis();
			cal.setTimeInMillis(millis);
			String now = formatter.format(cal.getTime());
			return now;
		}
}

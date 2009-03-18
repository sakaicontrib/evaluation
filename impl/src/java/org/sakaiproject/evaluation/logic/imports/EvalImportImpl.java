/**********************************************************************************
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

package org.sakaiproject.evaluation.logic.imports;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.imports.EvalImport;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Process an XML ContentResource and save or update the evaluation data contained in it
 * 
 * @author rwellis
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 * FIXME rwellis, please use generics in all collections in this class -AZ
 */
public class EvalImportImpl implements EvalImport {
	
	private static final Log log = LogFactory.getLog(EvalImportImpl.class);

	private final String SEPARATOR = "/";    // max-32:12345678901234567890123456789012
	private final String EVENT_SCALE_SAVE =           "eval.scale.import";
	private final String EVENT_SCALE_UPDATE =         "eval.scale.import.update";
	private final String EVENT_ITEM_SAVE =            "eval.item.import";
	private final String EVENT_ITEM_UPDATE =          "eval.item.import.update";
	private final String EVENT_TEMPLATE_SAVE =        "eval.template.import";
	private final String EVENT_TEMPLATE_UPDATE =      "eval.templateitem.import.update";
	private final String EVENT_TEMPLATEITEM_SAVE =    "eval.templateitem.import";
	private final String EVENT_TEMPLATEITEM_UPDATE =  "eval.template.import.update";
	private final String EVENT_EVALUATION_SAVE =      "eval.evaluation.import";
	private final String EVENT_EVALUATION_UPDATE =    "eval.evaluation.import.update";
	private final String EVENT_ASSIGNGROUP_SAVE =     "eval.assigngroup.import";
	private final String EVENT_ASSIGNGROUP_UPDATE =   "eval.assigngroup.import.update";
	
	//Spring injection
	private ContentHostingService contentHostingService;
	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}
	private EvalEvaluationSetupService evaluationSetupService;
	public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
		this.evaluationSetupService = evaluationSetupService;
	}
   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }
	private EvalEvaluationSetupService evalEvaluationsLogic;
	public void setEvalEvaluationsLogic(EvalEvaluationSetupService evalEvaluationsLogic) {
		this.evalEvaluationsLogic = evalEvaluationsLogic;
	}

	private EvalCommonLogic commonLogic;
   public void setCommonLogic(EvalCommonLogic commonLogic) {
      this.commonLogic = commonLogic;
   }

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
   }
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
		
    private Calendar cal;
	private SimpleDateFormat formatter;
	private String currentUserId;
	private int numPersisted;
	
	// error messages during processing to surface to UI 
	// TODO collecting parameter pattern
	private List<String> messages = new ArrayList<String>();
	
	public void init() {
		
	}
	public EvalImportImpl() {
		currentUserId = null;
		numPersisted = 0;
		formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
		cal = Calendar.getInstance();
	}
	
	/**
	 * Parse and save or update evaluation data found in an XML ContentResource
	 * 
	 * @param id The Reference id of the ContentResource
	 * @return String error message(s)
	 */
	public List<String> process(String id, String userId) {
		if(id == null || userId == null) {
			messages.add("There was a problem: parameter id and/or userId was null.");
			//TODO add to audit trail
		}
		currentUserId = userId;
		ContentResource resource = null;
		Document doc = null;
		InputStream in = null;
		
		//TODO getTime() of start and add to audit trail
		try {
			contentHostingService.checkResource(id);
			
			//object types not in the XML file will just be ignored during the load
			resource = contentHostingService.getResource(id);
			in = resource.streamContent();
			doc = new SAXBuilder().build(in);

			//save in this order for db referential integrity
			saveOrUpdateScales(doc);
			saveOrUpdateItems(doc);
			saveOrUpdateTemplates(doc);
			saveOrUpdateTemplateItems(doc);
			saveOrUpdateEvaluations(doc);
			saveOrUpdateAssignGroups(doc);
		}
		catch (JDOMException jde) {
			log.error("There was a problem parsing the XML data. " + jde);
			messages.add("There was a problem parsing the XML data. " + jde);
			//TODO add to audit trail

		} catch (Exception e) {
			log.error("There was a problem loading the XML data. " + e);
			messages.add("There was a problem loading the XML data. " + e);
			//TODO add to audit trail
		}
		finally {
			// close the input stream
			if(in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
					log.error("Unable to close input stream. " + id + " " + ioe);
					messages.add("Unable to close input stream. " + id + " " + ioe);
					//TODO add to audit trail
				}
			}
			//remove the FilePickerHelper attachment that was created
			if(id != null) {
				try
				{
					contentHostingService.removeResource(id);
				}
				catch(Exception e) {
					log.warn("There was a problem deleting the FilePickerHelper attachment that was created '" + id + "' " + e);
					messages.add("There was a problem deleting the FilePickerHelper attachment that was created '" + id + "' " + e);
					//TODO add to audit trail
				}
			}
		}
		//TODO getTime() of finish and add to audit trail
		return messages;
	}
	
	/**
	 * Save new or update existing EvalScales
	 * 
	 * @param doc the Document created from the XML ContentResource
	 */
	protected void saveOrUpdateScales(Document doc) {
		String eid = null;
		String event = null;
		EvalScale scale = null;
		int scalesSaved = 0;
		try {
			XPath docsPath = XPath.newInstance("/EVAL_DATA/EVAL_SCALES/EVAL_SCALE");
			List scales = docsPath.selectNodes(doc);
			if(log.isInfoEnabled())
				log.info(scales.size() + " scales in XML document");
			//TODO add to audit trail
			
			for(Iterator iter = scales.iterator(); iter.hasNext();) {
				try {
					Element element = (Element)iter.next();
					eid = element.getChildText("EID");
					scale = authoringService.getScaleByEid(eid);
					if(scale == null) {
						//create new
						scale = newScale(element);
						event = EVENT_SCALE_SAVE;
					}
					else {
						//update existing
						setScaleProperties(element, scale);
						event = EVENT_SCALE_UPDATE;
					}
					
					//save or update
					authoringService.saveScale(scale, currentUserId);
					commonLogic.registerEntityEvent(event, scale);
					scalesSaved++;
					
					//ping session to keep it alive
					numPersisted++;
					if(numPersisted % 100 == 0) {
						Session session = sessionManager.getCurrentSession();
						session.setActive();
					}
				}
				catch(Exception e) {
					log.warn("EvalScale with eid '" + eid + "' was not saved/updated in the database " + e);
					messages.add("EvalScale with eid '" + eid + "' was not saved/updated in the database " + e);
					//TODO add to audit trail
					continue;
				}
			}
			if(log.isInfoEnabled())
				log.info(new String(getTime()) + " " + new Integer(scalesSaved) + " EvalScales saved/updated");
			//TODO add to audit trail
		}
		catch (JDOMException jde) {
			log.error("There was an error parsing the XML data. " + jde);
			messages.add("There was an error parsing the XML data. " + jde);
			//TODO add to audit trail
		}
		catch(Exception e) {
			log.error("There was a problem loading EvalScales. " + e);
			messages.add("There was a problem loading EvalScales. " + e);
			//TODO add to audit trail
		}
	}
	
	/**
	 * Save new or update existing EvalItems
	 * 
	 * @param doc the Document created from the XML ContentResource
	 */
	protected void saveOrUpdateItems(Document doc) {
		String eid = null;
		String event = null;
		EvalItem item = null;
		int itemsSaved = 0;
		
		//TODO getTime() of start and add to audit trail
		try {
			XPath docsPath = XPath.newInstance("/EVAL_DATA/EVAL_ITEMS/EVAL_ITEM");
			List items = docsPath.selectNodes(doc);
			if(log.isInfoEnabled())
				log.info(items.size() + " items in XML document");
			//TODO add to audit trail
			
			for(Iterator iter = items.iterator(); iter.hasNext();) {
				try {
					Element element = (Element)iter.next();
					eid = element.getChildText("EID");
					item = authoringService.getItemByEid(eid);
					if(item == null) {
						//create new
						item = newItem(element);
						event = EVENT_ITEM_SAVE;
					}
					else {
						//update existing
						setItemProperties(element, item);
						event = EVENT_ITEM_UPDATE;
					}
					
					//save or update
					authoringService.saveItem(item, currentUserId);
					commonLogic.registerEntityEvent(event, item);
					itemsSaved++;
					
					//ping session to keep it alive
					numPersisted++;
					if(numPersisted % 100 == 0) {
						Session session = sessionManager.getCurrentSession();
						session.setActive();
					}
				}
				catch(Exception e) {
					log.warn("EvalItem with eid '" + eid + "' was not saved/updated in the database " + e);
					messages.add("EvalItem with eid '" + eid + "' was not saved/updated in the database " + e);
					//TODO add to audit trail
					continue;
				}
			}
			if(log.isInfoEnabled())
				log.info(new String(getTime()) + " " + new Integer(itemsSaved) + " EvalItems saved/updated");
				//TODO add to audit trail
		}
		catch (JDOMException jde) {
			log.error("There was an error parsing the XML data. " + jde);
			messages.add("There was an error parsing the XML data. " + jde);
			//TODO add to audit trail
		}
		catch(Exception e) {
			log.error("There was a problem loading EvalItems. " + e);
			messages.add("There was a problem loading EvalItems. " + e);
			//TODO add to audit trail
		}
		//TODO getTime() of finish and add to audit trail
	}
	
	/**
	 * Save new or update existing EvalTemplates
	 * 
	 * @param doc the Document created from the XML ContentResource
	 */
	protected void saveOrUpdateTemplates(Document doc) {
		String eid = null;
		String event = null;
		EvalTemplate template = null;
		int templatesSaved = 0;
		
		//TODO getTime() of start and add to audit trail
		try {
			XPath docsPath = XPath.newInstance("/EVAL_DATA/EVAL_TEMPLATES/EVAL_TEMPLATE");
			/*
			 * Use {@link #canCreateTemplate(String)} or {@link #canControlTemplate(String, Long)}
			 * to check if user can save template and avoid exceptions
			 */
			List items = docsPath.selectNodes(doc);
			if(log.isInfoEnabled())
				log.info(items.size() + " templates in XML document");
			//TODO add to audit trail
			
			for(Iterator iter = items.iterator(); iter.hasNext();) {
				try {
					Element element = (Element)iter.next();
					eid = element.getChildText("EID");
					template = authoringService.getTemplateByEid(eid);
					if(template == null) {
						//create new
						template = newTemplate(element);
						event = EVENT_TEMPLATE_SAVE;
					}
					else {
						//update existing
						setTemplateProperties(element, template);
						event = EVENT_TEMPLATE_UPDATE;
					}
					
					authoringService.saveTemplate(template, currentUserId);
					commonLogic.registerEntityEvent(event, template);
					templatesSaved++;
					
					//ping session to keep it alive
					numPersisted++;
					if(numPersisted % 100 == 0) {
						Session session = sessionManager.getCurrentSession();
						session.setActive();
					}
				}
				catch(Exception e) {
					log.warn("EvalTemplate with eid '" + eid + "' was not saved/updated in the database " + e);
					messages.add("EvalTemplate with eid '" + eid + "' was not saved/updated in the database " + e);
					//TODO add to audit trail
					continue;
				}
			}
			if(log.isInfoEnabled())
				log.info(new String(getTime()) + " " + new Integer(templatesSaved) + " EvalTemplates saved/updated");
			//TODO add to audit trail
		} 
		catch (JDOMException jde) {
			log.error("There was an error parsing the XML data. " + jde);
			messages.add("There was an error parsing the XML data. " + jde);
			//TODO add to audit trail
		}
		catch(Exception e) {
			log.error("There was a problem loading EvalTemplates. " + e);
			messages.add("There was a problem loading EvalTemplates. " + e);
			//TODO add to audit trail
		}
		//TODO getTime() of finish and add to audit trail
	}
	
	/**
	 * Save new or update existing EvalTemplateItems
	 * 
	 * @param doc the Document created from the XML ContentResource
	 */
	protected void saveOrUpdateTemplateItems(Document doc) {
		String eid = null;
		String event = null;
		int templateItemsSaved = 0;
		try {
			XPath docsPath = XPath.newInstance("/EVAL_DATA/EVAL_TEMPLATEITEMS/EVAL_TEMPLATEITEM");
			List items = docsPath.selectNodes(doc);
			if(log.isInfoEnabled())
				log.info(items.size() + " template items in XML document");
			//TODO add to audit trail
			
			for(Iterator iter = items.iterator(); iter.hasNext();) {
				try {
					Element element = (Element)iter.next();
					eid = element.getChildText("EID");
					String templateEid = element.getChildText("TEMPLATE_EID");
					EvalTemplate template = authoringService.getTemplateByEid(templateEid);
					EvalTemplateItem templateItem = authoringService.getTemplateItemByEid(eid);
					if(template != null && templateItem == null) {
						//create new
						templateItem = newTemplateItem(element);
						event = EVENT_TEMPLATEITEM_SAVE;
					}
					else {
						//update existing
						setTemplateItemProperties(templateItem, element);
						event = EVENT_TEMPLATEITEM_UPDATE;
					}
					authoringService.saveTemplateItem(templateItem, currentUserId);
					commonLogic.registerEntityEvent(event, templateItem);
					templateItemsSaved++;
					
					//ping session to keep it alive
					numPersisted++;
					if(numPersisted % 100 == 0) {
						Session session = sessionManager.getCurrentSession();
						session.setActive();
					}
				}
				catch(Exception e) {
					log.warn(e);
					if(eid != null)
						log.warn("EvalTemplateItem with eid '" + eid + "' was not saved/updated in the database " + e);
					messages.add("EvalTemplateItem with eid '" + eid + "' was not saved/updated in the database " + e);
					//TODO add to audit trail
					continue;
				}
			}
			if(log.isInfoEnabled())
				log.info(new String(getTime()) + " " + new Integer(templateItemsSaved) + " EvalTemplateItems saved/updated");
		} 
		catch (JDOMException jde) {
			log.error("There was an error parsing the XML data. " + jde);
			messages.add("There was an error parsing the XML data. " + jde);
			//TODO add to audit trail
		}
		catch(Exception e) {
			log.error("There was a problem loading EvalTemplateItems. " + e);
			messages.add("There was a problem loading EvalTemplateItems. " + e);
			//TODO add to audit trail
		}
	}
	
	/**
	 * Save new or update existing EvalEvaluations
	 * 
	 * @param doc the Document created from the XML ContentResource
	 */
	private void saveOrUpdateEvaluations(Document doc) {
		String eid = null;
		String event = null;
		EvalEvaluation evaluation = null;
		int evaluationsSaved = 0;

		//TODO getTime() of start and add to audit trail
		try {
			XPath docsPath = XPath.newInstance("/EVAL_DATA/EVAL_EVALUATIONS/EVAL_EVALUATION");
			/*
			 * Use {@link #canCreateTemplate(String)} or {@link #canControlTemplate(String, Long)}
			 * to check if user can save template and avoid exceptions
			 */
			List evals = docsPath.selectNodes(doc);
			if(log.isInfoEnabled())
				log.info(evals.size() + " evaluationSetupService in XML document");
			//TODO add to audit trail
			
			for(Iterator iter = evals.iterator(); iter.hasNext();) {
				try {
					Element element = (Element)iter.next();
					eid = element.getChildText("EID");
					evaluation = evaluationService.getEvaluationByEid(eid);
					if(evaluation == null) {
						//create new
						evaluation = newEvaluation(element);
						event = EVENT_EVALUATION_SAVE;
					}
					else
					{
						//update existing
						setEvaluationProperties(element, evaluation);
						event = EVENT_EVALUATION_UPDATE;
					}
					
					//save or update
					evalEvaluationsLogic.saveEvaluation(evaluation, currentUserId, false);
					commonLogic.registerEntityEvent(event, evaluation);
					evaluationsSaved++;
					
					//ping session to keep it alive
					numPersisted++;
					if(numPersisted % 100 == 0) {
						Session session = sessionManager.getCurrentSession();
						session.setActive(); 
					}
				}
				catch (Exception e) {
					log.warn("EvalEvaluation with eid '" + eid + "' was not saved/updated in the database " + e);
					messages.add("EvalEvaluation with eid '" + eid + "' was not saved/updated in the database " + e);
					//TODO add to audit trail
					continue;
				}
			}
			if(log.isInfoEnabled()) {
				log.info(new String(getTime()) + " " + new Integer(evaluationsSaved) + " EvalEvaluations saved/updated");
			}
			//TODO add to audit trail
		}
		catch (JDOMException jde) {
			log.error("There was an error parsing the XML data. " + jde);
			messages.add("There was an error parsing the XML data. " + jde);
			//TODO add to audit trail
		}
		catch(Exception e) {
			log.error("There was a problem loading EvalEvaluations. " + e);
			messages.add("There was a problem loading EvalEvaluations. " + e);
			//TODO add to audit trail
		}
	}
	
	/**
	 * Save new or update existing EvalAssignGroups
	 * 
	 * @param doc the Document created from the XML ContentResource
	 */
	private void saveOrUpdateAssignGroups(Document doc) {
		//TODO getTime() of start and add to audit trail
		
		String eid = null;
		String event = null;
		int assignGroupsSaved = 0;
		EvalAssignGroup evalAssignGroup;
		try {
			XPath docsPath = XPath.newInstance("/EVAL_DATA/EVAL_ASSIGN_GROUPS/EVAL_ASSIGN_GROUP");
			/*
			 * Use {@link #canCreateTemplate(String)} or {@link #canControlTemplate(String, Long)}
			 * to check if user can save template and avoid exceptions
			 */
			List evalAssignGroups = docsPath.selectNodes(doc);
			if(log.isInfoEnabled())
				log.info(evalAssignGroups .size() + " EvalAssignGroups in XML document");
			//TODO add to audit trail
			
			for(Iterator iter = evalAssignGroups .iterator(); iter.hasNext();) {
				try {
					Element element = (Element)iter.next();
					eid = element.getChildText("EID");
					evalAssignGroup = evaluationService.getAssignGroupByEid(eid);
					
					//TODO remove: testing
//					String evalGroupId = element.getChildText("PROVIDER_ID");
//					Set userIds = commonLogic.getUserIdsForEvalGroup(evalGroupId, EvalConstants.PERM_TAKE_EVALUATION);
//					userIds = commonLogic.getUserIdsForEvalGroup(evalGroupId, EvalConstants.PERM_BE_EVALUATED);
					//TODO remove: testing
					
					if(evalAssignGroup == null) {
						//create new
						evalAssignGroup = newAssignGroup(element);
						event = EVENT_ASSIGNGROUP_SAVE;
					}
					else
					{
						//update existing
						setAssignGroupProperties(element, evalAssignGroup);
						event = EVENT_ASSIGNGROUP_UPDATE;
					}
					
					//save or update
					evaluationSetupService.saveAssignGroup(evalAssignGroup, currentUserId);
					commonLogic.registerEntityEvent(event, evalAssignGroup);
					assignGroupsSaved++;
					
					//ping session to keep it alive
					numPersisted++;
					if(numPersisted % 100 == 0) {
						Session session = sessionManager.getCurrentSession();
						session.setActive(); 
					}
				}
				catch(Exception e) {
					log.warn("EvalAssignGroup with eid '" + eid + "' was not saved/updated in the database " + e);
					messages.add("EvalAssignGroup with eid '" + eid + "' was not saved/updated in the database " + e);
					//TODO add to audit trail
					continue;
				}
				
			}
			if(log.isInfoEnabled()) {
				log.info(new String(getTime()) + " " + new Integer(assignGroupsSaved) + " EvalAssignGroups saved/updated");
			}
			//TODO add to audit trail
		}
		catch (JDOMException jde) {
			log.error("There was an error parsing the XML data. " + jde);
			messages.add("There was an error parsing the XML data. " + jde);
			//TODO add to audit trail
		}
		catch(Exception e) {
			log.error("There was a problem loading EvalAssignGroups. " + e);
			messages.add("There was a problem loading EvalAssignGroups. " + e);
			//TODO add to audit trail
		}
	}
	
	/**
	 * Create a new EvalTemplate item with properties from XML Element data
	 * 
	 * @param element the Element
	 * @return the EvalTemplate
	 */
	private EvalTemplate newTemplate(Element element) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			Boolean locked = element.getChildText("LOCKED").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			Boolean expert = element.getChildText("EXPERT").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			String owner = element.getChildText("OWNER");
			String type = element.getChildText("TYPE");
			String title = element.getChildText("TITLE");
			String description = element.getChildText("DESCR");
			String sharing = element.getChildText("SHARING");
			String expertDescription = element.getChildText("EXPERTDESCR");
			Set<EvalTemplateItem> templateItems = new HashSet<EvalTemplateItem>(0);
			EvalTemplate template = new EvalTemplate(new Date(), owner, type, title, description,  sharing, expert,
					expertDescription, templateItems, locked, false);
			template.setEid(eid);
			return template;
		}
		catch(Exception e) {
			throw new RuntimeException("newTemplate() eid '" + eid + "' " + e);
		}
	}

	/**
	 * Set EvalTemplate properties from XML Element data
	 * 
	 * @param element the Element
	 * @param template the EvalTemplate
	 */
	private void setTemplateProperties(Element element, EvalTemplate template) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			template.setOwner(element.getChildText("OWNER"));
			template.setType(element.getChildText("TYPE"));
			template.setTitle(element.getChildText("TITLE"));
			template.setDescription(element.getChildText("DESCR"));
			template.setSharing(element.getChildText("SHARING"));
			Boolean locked = element.getChildText("LOCKED").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			template.setLocked(locked);
			Boolean expert = element.getChildText("EXPERT").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			template.setExpert(expert);
		}
		catch(Exception e) {
			throw new RuntimeException("setTemplateProperties() eid '" + eid + "' " + e);
		}
	}
	
	/**
	 * Create a new EvalTemplateItem item with properties from XML Element data
	 * 
	 * @param element the Element
	 * @return the EvalTemplateItem
	 */
	private EvalTemplateItem newTemplateItem(Element element) {
		String eid = null;
		Integer displayOrder = null;
		try{
			eid = element.getChildText("EID");
			String owner = element.getChildText("OWNER");
			String resultsSharing = element.getChildText("RESULTS_SHARING");
			String level = element.getChildText("HIERARCHY_LEVEL");
			String nodeId = element.getChildText("HIERARCHY_NODE_ID");
			String displayOrderString = (String)element.getChildText("DISPLAY_ORDER");
			if(displayOrderString != null && !displayOrderString.trim().equals("")){
				try {
					displayOrder = Integer.parseInt((String)element.getChildText("DISPLAY_ORDER"));
				}
				catch(NumberFormatException e) {
					log.warn("There was a problem with DISPLAY_ORDER involving EvalTemplateItem with eid '" + eid + "'. " + e);
					messages.add("There was a problem with DISPLAY_ORDER involving EvalTemplateItem with eid '" + eid + "'. " + e);
					//TODO add to audit trail
				}
			}
		
		Boolean usesNA = new Boolean(Boolean.FALSE);
		if(element.getChildText("USES_NA").trim().equals("1"))
			usesNA=Boolean.TRUE;
		else
			usesNA=Boolean.FALSE;
		
			Long blockId = null;
			if((String)element.getChildText("BLOCK_ID") != null && !((String)element.getChildText("BLOCK_ID")).trim().equals("")){
				try {
					blockId = new Long(Long.parseLong((String)element.getChildText("BLOCK_ID")));
				}
				catch(NumberFormatException e) {
					log.warn("There was a problem with BLOCK_ID involving EvalTemplateItem with eid '" + eid + "'. " + e);
					messages.add("There was a problem with BLOCK_ID involving EvalTemplateItem with eid '" + eid + "'. " + e);
					//TODO add to audit trail
				}
			}
			Boolean blockParent = null;
			if((String)element.getChildText("BLOCK_PARENT") != null && !((String)element.getChildText("BLOCK_PARENT")).trim().equals("")){
				if(((String)element.getChildText("BLOCK_PARENT")).trim().equals("1"))
					blockParent = new Boolean(Boolean.TRUE);
			}
			String itemEid = element.getChildText("ITEM_EID");
			EvalItem item = authoringService.getItemByEid(itemEid);
			String templateEid = element.getChildText("TEMPLATE_EID");
			EvalTemplate template = authoringService.getTemplateByEid(templateEid);
			
			Integer displayRows = null;
			EvalScale scale = null;
			String scaleDisplaySetting = null;
			String itemCategory = item.getCategory();
			
			//if not Essay type question
			if(!item.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)){
				scaleDisplaySetting = (String)element.getChildText("SCALE_DISPLAY_SETTING");
				
				//set scale from EvalItem
				if(item != null)
				{
					scale = item.getScale();
					if(scale == null) {
						log.warn("EvalScale is null for EvalTemplateItem with eid '" + eid + "' for EvalTemplate '" + template.getTitle());
						messages.add("EvalScale is null for EvalTemplateItem with eid '" + eid + "' for EvalTemplate '" + template.getTitle());
						//TODO add to audit trail
					}
				}
				else {
					log.warn("item is null for templateItem with eid '" + eid + "' for template '" + template.getTitle());
					messages.add("EvalItem is null for EvalTemplateItem with eid '" + eid + "' for EvalTemplate '" + template.getTitle());
					//TODO add to audit trail
				}
				String displayRowsString = element.getChildText("DISPLAY_ROWS");
				if(displayRowsString != null && !displayRowsString.trim().equals("")){
					try {
						displayRows = Integer.parseInt(element.getChildText("DISPLAY_ROWS"));
					}
					catch(NumberFormatException e) {
						log.warn("There was a problem with DISPLAY_ROWS involving EvalTemplateItem with eid '" + eid + "'. " + e);
						messages.add("There was a problem with DISPLAY_ROWS involving EvalTemplateItem with eid '" + eid + "'. " + e);
						//TODO add to audit trail
					}
				}
			}
			//new templateItem
			EvalTemplateItem evalTemplateItem = 
				new EvalTemplateItem(new Date(), owner,	
						template, item, displayOrder, itemCategory, level, nodeId,
						displayRows, scaleDisplaySetting, usesNA, false, blockParent,
						blockId, resultsSharing);
			evalTemplateItem.setEid(eid);
			return evalTemplateItem;
		}
		catch(Exception e) {
			throw new RuntimeException("newTemplateItem eid '" + eid + "' " + e);
		}
	}
	
	/**
	 * Set EvalTemplateItem properties from XML Element data
	 * 
	 * @param element the Element
	 * @param templateItem the EvalTemplateItem
	 */
	private void setTemplateItemProperties(EvalTemplateItem evalTemplateItem, Element element) {
		String eid = null;
		try {
			eid = evalTemplateItem.getEid();
			evalTemplateItem.setOwner(new String(element.getChildText("OWNER")));
			evalTemplateItem.setResultsSharing(new String(element.getChildText("RESULTS_SHARING")));
			evalTemplateItem.setHierarchyLevel(element.getChildText("HIERARCHY_LEVEL"));
			evalTemplateItem.setHierarchyNodeId(new String(element.getChildText("HIERARCHY_NODE_ID")));
			Integer displayOrder = null;
			String displayOrderString = (String)element.getChildText("DISPLAY_ORDER");
			if(displayOrderString != null && !displayOrderString.trim().equals("")){
				try {
					displayOrder = Integer.parseInt((String)element.getChildText("DISPLAY_ORDER"));
				}
				catch(NumberFormatException e) {
					log.warn("There was a problem with DISPLAY_ORDER involving EvalTemplateItem with eid '" + eid + "'. " + e);
					messages.add("There was a problem with DISPLAY_ORDER involving EvalTemplateItem with eid '" + eid + "'. " + e);
					//TODO add to audit trail
				}
			}
			evalTemplateItem.setDisplayOrder(displayOrder);
			
			Boolean usesNA = new Boolean(Boolean.FALSE);
			if(element.getChildText("USES_NA").trim().equals("1"))
				usesNA=Boolean.TRUE;
			else
				usesNA=Boolean.FALSE;
			evalTemplateItem.setUsesNA(usesNA);
			
			Long blockId = null;
			if((String)element.getChildText("BLOCK_ID") != null && !((String)element.getChildText("BLOCK_ID")).trim().equals("")){
				try {
					blockId = new Long(Long.parseLong((String)element.getChildText("BLOCK_ID")));
				}
				catch(NumberFormatException e) {
					log.warn("There was a problem with BLOCK_ID involving EvalTemplateItem with eid '" + eid + "'. " + e);
					messages.add("There was a problem with BLOCK_ID involving EvalTemplateItem with eid '" + eid + "'. " + e);
					//TODO add to audit trail
				}
			}
			evalTemplateItem.setBlockId(blockId);
			
			Boolean blockParent = null;
			if((String)element.getChildText("BLOCK_PARENT") != null && !((String)element.getChildText("BLOCK_PARENT")).trim().equals("")){
				if(((String)element.getChildText("BLOCK_PARENT")).trim().equals("1"))
					blockParent = new Boolean(Boolean.TRUE);
			}
			evalTemplateItem.setBlockParent(blockParent);
			
			String itemEid = element.getChildText("ITEM_EID");
			EvalItem item = authoringService.getItemByEid(itemEid);
			evalTemplateItem.setItem(item);
			
			String templateEid = element.getChildText("TEMPLATE_EID");
			EvalTemplate template = authoringService.getTemplateByEid(templateEid);
			evalTemplateItem.setTemplate(template);
		
			String itemCategory = item.getCategory();
			evalTemplateItem.setCategory(itemCategory);
			
			Integer displayRows = null;
			String scaleDisplaySetting = null;
			//if not Essay type question
			if(!item.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)){
				scaleDisplaySetting = (String)element.getChildText("SCALE_DISPLAY_SETTING");
				evalTemplateItem.setScaleDisplaySetting(scaleDisplaySetting);
				
				String displayRowsString = element.getChildText("DISPLAY_ROWS");
				if(displayRowsString != null && !displayRowsString.trim().equals("")){
					try {
						displayRows = Integer.parseInt(element.getChildText("DISPLAY_ROWS"));
						evalTemplateItem.setDisplayRows(displayRows);
					}
					catch(NumberFormatException e) {
						log.warn("There was a problem with DISPLAY_ROWS involving EvalTemplateItem with eid '" + eid + "'. " + e);
						messages.add("There was a problem with DISPLAY_ROWS involving EvalTemplateItem with eid '" + eid + "'. " + e);
						//TODO add to audit trail
					}
				}
			}
		}
		catch(Exception e) {
			throw new RuntimeException("setTemplateItem eid '" + eid + "' " + e);
		}
	}
	
	/**
	 * Create a new EvalScale item with properties from XML Element data
	 * 
	 * @param element the Element
	 * @return the EvalScale item
	 */
	private EvalScale newScale(Element element) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			String title = element.getChildText("TITLE");
			String [] choices = null;
			Boolean locked = element.getChildText("LOCKED").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			Boolean expert = element.getChildText("EXPERT").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
	
			//set options
			HashMap<Integer, String> order = new HashMap<Integer, String>();
			Element evalScaleOptions = element.getChild("EVAL_SCALE_OPTIONS");
			List options = evalScaleOptions.getChildren("EVAL_SCALE_OPTION");
			if(options != null && !options.isEmpty()) {
				choices = new String[options.size()];
				for(Iterator i = options.iterator(); i.hasNext();) {
					Element e = (Element)i.next();
					Integer key = Integer.parseInt(e.getChildText("SCALE_OPTION_INDEX"));
					String value = e.getChildText("SCALE_OPTION");
					order.put(key, value);
				}
				for (int i = 0; i < choices.length; i++) {
					choices[i] = (String)order.get(new Integer(i));
				}
			}
			else {
				if(log.isWarnEnabled())
					log.warn("No options were found for EvalScale with eid '" + eid + "' " + title);
				messages.add("No options were found for EvalScale with eid '" + eid + "' " + title);
				//TODO add to audit trail
			}
			String owner = element.getChildText("OWNER");
			String sharing = element.getChildText("SHARING");
			String expertDescription = element.getChildText("EXPERT_DESCRIPTION");
			String ideal = element.getChildText("IDEAL");
			
			//new scale
			EvalScale scale = new EvalScale(new Date(), owner, title, EvalConstants.SCALE_MODE_SCALE, sharing, expert, expertDescription, ideal, choices,locked);	
			scale.setEid(eid);
			return scale;
		}
		catch(Exception e) {
			throw new RuntimeException("newScale() eid '" + eid + "' " + e);
		}
	}
	
	/**
	 * Set EvalScale properties from XML Element data
	 * 
	 * @param element the Element
	 * @param scale the EvalScale
	 */
	private void setScaleProperties(Element element, EvalScale scale) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			scale.setOwner(element.getChildText("OWNER"));
			scale.setTitle(element.getChildText("TITLE"));
			scale.setSharing(element.getChildText("SHARING"));
			if(element.getChildText("EXPERT").trim().equals("1"))
				scale.setExpert(new Boolean(Boolean.TRUE));
			else
				scale.setExpert(new Boolean(Boolean.FALSE));
			scale.setExpertDescription(element.getChildText("EXPERT_DESCRIPTION"));
			scale.setIdeal(element.getChildText("IDEAL"));
			if(element.getChildText("LOCKED").trim().equals("1"))
				scale.setLocked(new Boolean(Boolean.TRUE));
			else
				scale.setLocked(new Boolean(Boolean.FALSE));
			//set options
			HashMap<Integer, String> order = new HashMap<Integer, String>();
			Element evalScaleOptions = element.getChild("EVAL_SCALE_OPTIONS");
			List options = evalScaleOptions.getChildren("EVAL_SCALE_OPTION");
			if(options != null && !options.isEmpty()) {
				String [] choices = new String[options.size()];
				for(Iterator iter = options.iterator(); iter.hasNext();) {
					Element e = (Element)iter.next();
					Integer key = Integer.parseInt(e.getChildText("SCALE_OPTION_INDEX"));
					String value = e.getChildText("SCALE_OPTION");
					order.put(key, value);
				}
				for (int i = 0; i < choices.length; i++) {
					choices[i] = (String)order.get(new Integer(i));
				}
				scale.setOptions(choices);
			}
			else {
				if(log.isWarnEnabled())
					log.warn("No options were found for EvalScale with eid '" + scale.getEid() + "' " + scale.getTitle());
				messages.add("No options were found for EvalScale with eid '" + scale.getEid() + "' " + scale.getTitle());
				//TODO add to audit trail
			}
		}
		catch(Exception e) {
			throw new RuntimeException("setScaleProperties() eid '" + eid + "' " + e);
		}
	}
	
	/**
	 * Create a new EvalItem item with properties from XML Element data
	 * 
	 * @param element the Element
	 * @return the EvalItem item
	 */
	private EvalItem newItem(Element element) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			EvalScale scale;
			String scaleDisplaySetting;
			Integer displayRows = null;
			Boolean locked = element.getChildText("LOCKED").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			Boolean expert = element.getChildText("EXPERT").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			Boolean usesNA = element.getChildText("USES_NA").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			String displayRowsString = element.getChildText("DISPLAY_ROWS");
			if(displayRowsString != null && !displayRowsString.trim().equals("")){
				try {
					displayRows = Integer.parseInt(element.getChildText("DISPLAY_ROWS"));
				}
				catch(NumberFormatException e) {
					log.warn("There was a problem with DISPLAY_ROWS involving EvalItem with eid '" + eid + "'. " + e);
					messages.add("There was a problem with DISPLAY_ROWS involving EvalItem with eid '" + eid + "'. " + e);
					//TODO add to audit trail
				}
			}
			
			//if Essay type question do not set scaleDisplaySetting & scale
			scaleDisplaySetting = null;
			scale = null;
			if(!((String)element.getChildText("CLASSIFICATION")).equals(EvalConstants.ITEM_TYPE_TEXT)) {
				
				scaleDisplaySetting = (String)element.getChildText("SCALE_DISPLAY_SETTING");
				//set scale
				String scaleEid = element.getChildText("SCALE_EID");
				if(scaleEid != null && scaleEid.trim().length() != 0)
				{
					scale = authoringService.getScaleByEid(scaleEid);
					if(scale == null) {
						log.warn("EvalScale is null for EvalItem with eid '" + eid + "' " + element.getChildText("ITEM_TEXT"));
						messages.add("EvalScale is null for EvalItem with eid '" + eid + "' " + element.getChildText("ITEM_TEXT"));
						//TODO add to audit trail
					}
				}
				else {
					log.warn("Could not get EvalScale by eid for EvalItem with eid '" + eid + "' " + element.getChildText("ITEM_TEXT"));
					messages.add("Could not get EvalScale by eid for EvalItem with eid '" + eid + "' " + element.getChildText("ITEM_TEXT"));
					//TODO add to audit trail
				}
			}
			String itemText = element.getChildText("ITEM_TEXT");
			String description = element.getChildText("DESCRIPTION");
			String sharing = element.getChildText("SHARING");
			String classification = element.getChildText("CLASSIFICATION");
			String expertDescription = element.getChildText("EXPERT_DESCRIPTION");
			String category = element.getChildText("CATEGORY");
			Set<EvalTemplateItem> templateItems = new HashSet<EvalTemplateItem>(0);
	
			//new item
			EvalItem item = new EvalItem(new Date(), currentUserId, itemText, description, sharing, classification, expert,
					expertDescription, scale, templateItems, usesNA, false, displayRows, scaleDisplaySetting, 
					category, locked);
			
			item.setEid(eid);
			return item;
		}
		catch(Exception e) {
			throw new RuntimeException("newItem() eid '" + eid + "' " + e);
		}
	}
	
	/**
	 * Set EvalItem properties from XML Element data
	 * 
	 * @param element the Element
	 * @param item the EvalItem
	 */
	private void setItemProperties(Element element, EvalItem item) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			item.setOwner(element.getChildText("OWNER"));
			item.setItemText(element.getChildText("ITEM_TEXT"));
			item.setDescription(element.getChildText("DESCRIPTION"));
			item.setSharing(element.getChildText("SHARING"));
			item.setClassification(element.getChildText("CLASSIFICATION"));
	
			Boolean locked = element.getChildText("LOCKED").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			item.setLocked(locked);
			Boolean expert = element.getChildText("EXPERT").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			item.setExpert(expert);
			Boolean usesNA = element.getChildText("USES_NA").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			item.setUsesNA(usesNA);
			
			String displayRows = element.getChildText("DISPLAY_ROWS");
			if(displayRows != null && !displayRows.trim().equals("")){
				try {
					item.setDisplayRows(new Integer(Integer.parseInt(element.getChildText("DISPLAY_ROWS"))));
				}
				catch(NumberFormatException e) {
					log.warn("There was a problem with DISPLAY_ROWS involving EvalItem with eid '" + item.getEid() + "'. " + e);
					messages.add("There was a problem with DISPLAY_ROWS involving EvalItem with eid '" + item.getEid() + "'. " + e);
					//TODO add to audit trail
				}
			}
			item.setCategory(element.getChildText("CATEGORY"));
			if(element.getChildText("LOCKED").trim().equals("1"))
				item.setLocked(new Boolean(Boolean.TRUE));
			else
				item.setLocked(new Boolean(Boolean.FALSE));
			
			//if not Essay type question
			if(!item.getCategory().equals(EvalConstants.ITEM_TYPE_TEXT)) {
				
				item.setScaleDisplaySetting(element.getChildText("SCALE_DISPLAY_SETTING"));
				//set scale
				String scaleEid = element.getChildText("SCALE_EID");
				if(scaleEid != null && scaleEid.trim().length() != 0)
				{
					EvalScale scale = authoringService.getScaleByEid(scaleEid);
					if(scale != null) {
						item.setScale(scale);
					}
					else {
						log.warn("Could not get EvalScale with eid '" + scaleEid + "' for EvalItem with eid '" + item.getEid() + "' " + item.getItemText());
						messages.add("Could not get EvalScale with eid '" + scaleEid + "' for EvalItem with eid '" + item.getEid() + "' " + item.getItemText());
						//TODO add to audit trail
					}
				}
				else {
					log.warn("Could not get EvalScale by eid for EvalItem with eid '" + item.getEid() + "' " + item.getItemText());
					messages.add("Could not get EvalScale by eid for EvalItem with eid '" + item.getEid() + "' " + item.getItemText());
					//TODO add to audit trail
				}
			}
		}
		catch(Exception e) {
			throw new RuntimeException("setItemProperties() eid '" + eid + "' " + e);
		}
	}
	
	/**
	 * Create a new EvalAssignGroup item with properties from XML Element data
	 * 
	 * @param element the Element
	 * @return the EvalAssignGroup item
	 */
	private EvalAssignGroup newAssignGroup(Element element) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			if(eid == null || "".equals(eid)) {
				log.warn("EvalAssignGroup was not saved/updated in the database, because eid was missing.");
				messages.add("EvalAsignGroup was not saved/updated in the database, because eid was missing.");
				//TODO add to audit trail
			}
			String providerId = element.getChildText("PROVIDER_ID");
			if(providerId == null || "".equals(providerId)) {
				log.warn("EvalAssignGroup with eid '" + eid + "' was not saved/updated in the database, because provider id was missing.");
				messages.add("EvalAssignGroup with eid '" + eid + "' was not saved/updated in the database, because provider id was missing.");
				//TODO add to audit trail
			}
			String owner = element.getChildText("OWNER");
			String groupType = element.getChildText("GROUP_TYPE");
			String evalEid = element.getChildText("EVAL_EVALUATION_EID");
			EvalEvaluation evaluation = evaluationService.getEvaluationByEid(evalEid);
			Boolean instructorApproval = element.getChildText("INSTRUCTOR_APPROVAL").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			Boolean instructorsViewResults = element.getChildText("INSTRUCTOR_VIEW_RESULTS").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			Boolean studentsViewResults = element.getChildText("STUDENT_VIEW_RESULTS").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			
			//new EvalAssignGroup
			EvalAssignGroup evalAssignGroup = new EvalAssignGroup(owner, providerId, groupType,
					instructorApproval, instructorsViewResults, studentsViewResults, evaluation);
			evalAssignGroup.setEid(eid);
			return evalAssignGroup;
		}
		catch(Exception e) {
			throw new RuntimeException("newAssignGroup() eid '" + eid + "' " + e);
		}
	}
	
	/**
	 * Set EvalAssignGroup properties from XML Element data
	 * 
	 * @param element the Element
	 * @param evalAssignGroup the EvalAssignEvaluation item
	 */
	private void setAssignGroupProperties(Element element, EvalAssignGroup evalAssignGroup) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			evalAssignGroup.setEid(eid);
			Boolean instructorApproval = element.getChildText("INSTRUCTOR_APPROVAL").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			Boolean instructorsViewResults = element.getChildText("INSTRUCTOR_VIEW_RESULTS").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			Boolean studentsViewResults = element.getChildText("STUDENT_VIEW_RESULTS").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			EvalEvaluation evaluation = evaluationService.getEvaluationByEid(element.getChildText("EVALUATION_EID"));
			evalAssignGroup.setEvaluation(evaluation);
			evalAssignGroup.setEvalGroupType(new String(element.getChildText(element.getChildText("GROUP_TYPE"))));
			evalAssignGroup.setEvalGroupId(new String(element.getChildText(element.getChildText("PROVIDER_ID"))));
			evalAssignGroup.setInstructorApproval(instructorApproval);
			evalAssignGroup.setInstructorsViewResults(instructorsViewResults);
			evalAssignGroup.setLastModified(new Date());
			evalAssignGroup.setOwner(new String(element.getChildText("OWNER")));
			evalAssignGroup.setStudentsViewResults(studentsViewResults);
		}
		catch(Exception e) {
			throw new RuntimeException("setAssignGroupProperties() eid '" + eid + "' " + e);
		}
	}
	
	/**
	 * Create a new EvalEvaluation item with properties from XML Element data
	 * 
	 * @param element the Element
	 * @return the EvalEvaluation item
	 */
	private EvalEvaluation newEvaluation(Element element) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			String state = null;
			EvalTemplate addedTemplate = null;
			eid = element.getChildText("EID");
			if(eid == null || "".equals(eid)) {
				log.warn("EvalEvaluation was not saved/updated in the database, because eid was missing.");
				messages.add("EvalEvaluation was not saved/updated in the database, because eid was missing.");
				//TODO add to audit trail
			}
			/*
			String providerId = element.getChildText("PROVIDER_ID");
			if(providerId == null || "".equals(providerId)) {
				log.warn("EvalEvaluation with eid '" + eid + "' was not saved/updated in the database, because provider id was missing.");
				messages.add("EvalEvaluation with eid '" + eid + "' was not saved/updated in the database, because provider id was missing.");
				//TODO add to audit trail
			}
			*/
			String title = element.getChildText("TITLE");
			String owner = element.getChildText("OWNER");
			Date startDate = getDate(element.getChildText("START_DATE"));
			Date dueDate = getDate(element.getChildText("DUE_DATE"));
			Date stopDate = getDate(element.getChildText("STOP_DATE"));
			Date viewDate = getDate(element.getChildText("VIEW_DATE"));
			Date studentsDate = getDate(element.getChildText("STUDENTS_DATE"));
			Date instructorsDate = getDate(element.getChildText("INSTRUCTORS_DATE"));
			EvalEmailTemplate availableEmailTemplate = evaluationService.getDefaultEmailTemplate(element.getChildText("AVAILABLE_EMAIL_TEMPLATE"));
			EvalEmailTemplate reminderEmailTemplate = evaluationService.getDefaultEmailTemplate(element.getChildText("REMINDER_EMAIL_TEMPLATE"));
			EvalTemplate template = authoringService.getTemplateByEid(element.getChildText("TEMPLATE_EID"));
			String instructions = element.getChildText("INSTRUCTIONS");
			if( instructions == null || instructions.trim().equals("")){
				instructions = null;
			}
			String instructorOpt = element.getChildText("INSTRUCTOR_OPT");
			if( instructorOpt == null || instructorOpt.trim().equals("")){
				instructorOpt = null;
			}
			Integer reminderDays = 0;
			String reminderDaysString = element.getChildText("REMINDER_DAYS").trim();
			if(reminderDaysString != null && !reminderDaysString.trim().equals("")){
				try {
					reminderDays = Integer.parseInt(reminderDaysString);
				}
				catch(NumberFormatException e) {
					log.warn("There was a problem with REMINDER_DAYS involving EvalEvaluation with eid '" + eid + "'. " + e);
					messages.add("There was a problem with REMINDER_DAYS involving EvalEvaluation with eid '" + eid + "'. " + e);
					//TODO add to audit trail
				}
			}
			String reminderFromEmail = element.getChildText("REMINDER_FROM_EMAIL");
			String termId = element.getChildText("TERM_ID");
			if( termId == null || termId.trim().equals("")){
				termId = null;
			}
			String authControl = element.getChildText("AUTH_CONTROL");
			String evalCategory = element.getChildText("EVAL_CATEGORY");
			
			Boolean resultsPrivate = element.getChildText("RESULTS_PRIVATE").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			Boolean blankResponsesAllowed = element.getChildText("BLANK_RESPONSES_ALLOWED").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			Boolean modifyResponsesAllowed = element.getChildText("MODIFY_RESPONSES_ALLOWED").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			Boolean locked = element.getChildText("LOCKED").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			Boolean unregisteredAllowed = element.getChildText("UNREGISTERED_ALLOWED").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			
			String resultsPrivacy = EvalConstants.SHARING_VISIBLE;
			if (resultsPrivate) {
			   resultsPrivacy = EvalConstants.SHARING_PRIVATE;
			}

			//new evaluation
			EvalEvaluation evaluation = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, owner, title, instructions, startDate, dueDate,
				stopDate, viewDate, false, studentsDate, false, instructorsDate, state,
				EvalConstants.SHARING_VISIBLE, instructorOpt, reminderDays, reminderFromEmail, termId, availableEmailTemplate,
				reminderEmailTemplate, template, new HashSet(0), blankResponsesAllowed, modifyResponsesAllowed,
				unregisteredAllowed, locked, authControl, evalCategory, null, null);
			evaluation.setEid(eid);
			return evaluation;
		}
		catch(Exception e) {
			throw new RuntimeException("newEvaluation() eid '" + eid + "' " + e);
		}
	}
	
	/**
	 * Set EvalEvaluation properties from XML Element data
	 * 
	 * @param element the Element
	 * @param evaluation the EvalEvaluation
	 */
	private void setEvaluationProperties(Element element, EvalEvaluation evaluation) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			if(eid == null || "".equals(eid)) {
				log.warn("EvalEvaluation was not saved/updated in the database, because eid was missing.");
				messages.add("EvalEvaluation was not saved/updated in the database, because eid was missing.");
				throw new IllegalArgumentException("Eid missing for EvalEvaluation");
				//TODO add to audit trail
			}
			/*
			String providerId = element.getChildText("PROVIDER_ID");
			if(providerId == null || "".equals(providerId)) {
				log.warn("EvalEvaluation with eid '" + eid + "' was not saved/updated in the database, because provider id was missing.");
				messages.add("EvalEvaluation with eid '" + eid + "' was not saved/updated in the database, because provider id was missing.");
				throw new IllegalArgumentException("Provider Id missing for EvalEvaluation with eid '" + eid + "'");
				//TODO add to audit trail
			}
			*/
			evaluation.setTitle(new String(element.getChildText("TITLE")));
			evaluation.setOwner(new String(element.getChildText("OWNER")));
			evaluation.setStartDate(getDate(element.getChildText("START_DATE")));
			evaluation.setDueDate(getDate(element.getChildText("DUE_DATE")));
			evaluation.setStopDate(getDate(element.getChildText("STOP_DATE")));
			evaluation.setViewDate(getDate(element.getChildText("VIEW_DATE")));
			evaluation.setStudentsDate(getDate(element.getChildText("STUDENTS_DATE")));
			evaluation.setInstructorsDate(getDate(element.getChildText("INSTRUCTORS_DATE")));
			
			Boolean resultsPrivate = element.getChildText("RESULTS_PRIVATE").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
         String resultsPrivacy = EvalConstants.SHARING_VISIBLE;
         if (resultsPrivate) {
            resultsPrivacy = EvalConstants.SHARING_PRIVATE;
         }
			Boolean blankResponsesAllowed = element.getChildText("BLANK_RESPONSES_ALLOWED").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			Boolean modifyResponsesAllowed = element.getChildText("MODIFY_RESPONSES_ALLOWED").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			Boolean locked = element.getChildText("LOCKED").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			Boolean unregisteredAllowed = element.getChildText("UNREGISTERED_ALLOWED").trim().equals("1") ? new Boolean(Boolean.TRUE) : new Boolean(Boolean.FALSE);
			evaluation.setResultsSharing(resultsPrivacy);
			evaluation.setBlankResponsesAllowed(blankResponsesAllowed);
			evaluation.setModifyResponsesAllowed(modifyResponsesAllowed);
			evaluation.setUnregisteredAllowed(unregisteredAllowed);
			evaluation.setLocked(locked);
			
			evaluation.setAvailableEmailTemplate(evaluationService.getDefaultEmailTemplate(element.getChildText("AVAILABLE_EMAIL_TEMPLATE")));
			evaluation.setReminderEmailTemplate(evaluationService.getDefaultEmailTemplate(element.getChildText("REMINDER_EMAIL_TEMPLATE")));
			evaluation.setTemplate(authoringService.getTemplateByEid(element.getChildText("TEMPLATE_EID")));
			
			String instructions = element.getChildText("INSTRUCTIONS");
			if( instructions == null || instructions.trim().equals("")){
				instructions = null;
			}
			evaluation.setInstructions(instructions);
			
			String instructorOpt = element.getChildText("INSTRUCTOR_OPT");
			if( instructorOpt == null || instructorOpt.trim().equals("")){
				instructorOpt = null;
			}
			evaluation.setInstructorOpt(instructorOpt);
			
			Integer reminderDays = 0;
			String reminderDaysString = element.getChildText("REMINDER_DAYS").trim();
			if(reminderDaysString != null && !reminderDaysString.trim().equals("")){
				try {
					reminderDays = Integer.parseInt(reminderDaysString);
				}
				catch(NumberFormatException e) {
					log.warn("There was a problem with REMINDER_DAYS involving EvalEvaluation with eid '" + eid + "'. " + e);
					messages.add("There was a problem with REMINDER_DAYS involving EvalEvaluation with eid '" + eid + "'. " + e);
					//TODO add to audit trail
				}
			}
			evaluation.setReminderDays(reminderDays);
			evaluation.setReminderFromEmail(new String(element.getChildText("REMINDER_FROM_EMAIL")));
			String termId = element.getChildText("TERM_ID");
			if( termId == null || termId.trim().equals("")){
				termId = null;
			}
			evaluation.setTermId(termId);
			evaluation.setAuthControl(new String(element.getChildText("AUTH_CONTROL")));
			evaluation.setEvalCategory(new String(element.getChildText("EVAL_CATEGORY")));
		}
		catch(Exception e) {
			throw new RuntimeException("setEvaluationProperties() eid '" + eid + "' " + e);
		}
	}
	
	/**
	 * A utility method to get a Date from a formatted String
	 * @param dateString a date String in "yyyy-MM-dd hh:mm:ss" format e.g., 2007-10-03 04:00:00
	 * @return the corresponding Date
	 */
	private Date getDate(String dateString) {
		
		if(dateString == null || "".equals(dateString)) {
			return null;
		}
		//e.g., 2007-10-03 04:00:00
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try {
			return df.parse(dateString);
		} catch (ParseException pe) {
			log.warn("Invalid date: " + dateString);
			return null;
		}
	}

	/**
	 * A utility method to create a user-readable time
	 * 
	 * @return the current time String
	 */
	protected String getTime()
	{
		String now = null;
		long millis = System.currentTimeMillis();
		cal.setTimeInMillis(millis);
		now = formatter.format(cal.getTime());
		return now;
	}
}

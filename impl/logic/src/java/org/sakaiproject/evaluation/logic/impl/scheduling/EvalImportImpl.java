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

package org.sakaiproject.evaluation.logic.impl.scheduling;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
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
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.evaluation.logic.externals.EvalImport;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.tool.api.Session;

/**
 * Process an XML ContentResource and save or update the evaluation data
 * 
 * @author rwellis
 *
 */
public class EvalImportImpl implements EvalImport {
	
	private static final Log log = LogFactory.getLog(EvalImportImpl.class);
	
	private org.sakaiproject.content.api.ContentHostingService contentHostingService = 
		(org.sakaiproject.content.api.ContentHostingService) ComponentManager.get(org.sakaiproject.content.api.ContentHostingService.class);
	private org.sakaiproject.tool.api.SessionManager sessionManager = 
		(org.sakaiproject.tool.api.SessionManager) ComponentManager.get(org.sakaiproject.tool.api.SessionManager.class);
	private org.sakaiproject.evaluation.logic.EvalItemsLogic evalItemsLogic = 
		(org.sakaiproject.evaluation.logic.EvalItemsLogic) ComponentManager.get(org.sakaiproject.evaluation.logic.EvalItemsLogic.class);
	private org.sakaiproject.evaluation.logic.EvalScalesLogic evalScalesLogic =
		(org.sakaiproject.evaluation.logic.EvalScalesLogic) ComponentManager.get(org.sakaiproject.evaluation.logic.EvalScalesLogic.class);
	private org.sakaiproject.evaluation.logic.EvalTemplatesLogic evalTemplatesLogic = 
		(org.sakaiproject.evaluation.logic.EvalTemplatesLogic) ComponentManager.get(org.sakaiproject.evaluation.logic.EvalTemplatesLogic.class);
	private org.sakaiproject.evaluation.logic.EvalExternalLogic evalExternalLogic = 
		(org.sakaiproject.evaluation.logic.EvalExternalLogic)ComponentManager.get(org.sakaiproject.evaluation.logic.EvalExternalLogic.class);
	
	private final String EVENT_TEMPLATE_CREATE = "eval.template.added";
	private final String EVENT_TEMPLATE_UPDATE = "eval.template.updated";
	private Calendar cal = Calendar.getInstance();
	private SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
	private String currentUserId = null;
	private int numPersisted = 0;
	
	//error messages during processing to surface to UI
	private List messages = new ArrayList();
	
	public void init() {
		
	}
	public EvalImportImpl() {
		
	}
	
	/**
	 * Parse and save or update evaluation data found in an XML ContentResource
	 * 
	 * @param id The Reference id of the ContentResource
	 * @return String error message(s)
	 */
	public List process(String id, String userId) {
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
		EvalScale scale = null;
		String [] choices = null;
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
					//If there's an existing item with this eid update the item, else create a new item with this eid
					scale = evalScalesLogic.getScaleByEid(eid);
					if(scale == null) {
						//if there isn't an item with this eid, create one
						Boolean locked = new Boolean(Boolean.FALSE);
						Boolean expert = new Boolean(Boolean.FALSE);
						if(element.getChildText("LOCKED").trim().equals("1"))
							locked = Boolean.TRUE;
						else
							locked = Boolean.FALSE;
						if(element.getChildText("EXPERT").trim().equals("1"))
							expert = Boolean.TRUE;
						else
							expert = Boolean.FALSE;
						
						//set options
						Hashtable order = new Hashtable();
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
								log.warn("No options were found for EvalScale with eid '" + scale.getEid() + "' " + scale.getTitle());
							messages.add("No options were found for EvalScale with eid '" + scale.getEid() + "' " + scale.getTitle());
							//TODO add to audit trail
						}
						String owner = element.getChildText("OWNER");
						String title = element.getChildText("TITLE");
						String sharing = element.getChildText("SHARING");
						String expertDescription = element.getChildText("EXPERT_DESCRIPTION");
						String ideal = element.getChildText("IDEAL");
						
						//new scale
						scale = new EvalScale(new Date(), owner, title, sharing, expert, expertDescription, ideal, choices,locked);	
						scale.setEid(eid);
					}
					else {
						//if there is a template with this eid, update it
						setScaleProperties(element, scale);
					}
					//save or update scale
					evalScalesLogic.saveScale(scale, currentUserId);
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
		String scaleDisplaySetting = null;
		String displayRowsString = null;
		Integer displayRows = null;
		EvalItem item = null;
		EvalScale scale = null;
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
					
					//If there's an existing item with this eid update the item, else create a new item with this eid
					item = evalItemsLogic.getItemByEid(eid);
					if(item == null) {
						Boolean locked = new Boolean(Boolean.FALSE);
						Boolean expert = new Boolean(Boolean.FALSE);
						Boolean usesNA = new Boolean(Boolean.FALSE);
						if(element.getChildText("LOCKED").trim().equals("1"))
							locked = Boolean.TRUE;
						else
							locked = Boolean.FALSE;
						if(element.getChildText("EXPERT").trim().equals("1"))
							expert = Boolean.TRUE;
						else
							expert = Boolean.FALSE;
						if(element.getChildText("USES_NA").trim().equals("1"))
							usesNA=Boolean.TRUE;
						else
							usesNA=Boolean.FALSE;
						
						displayRows = null;
						displayRowsString = element.getChildText("DISPLAY_ROWS");
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
								scale = evalScalesLogic.getScaleByEid(scaleEid);
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
						Set templateItems = new HashSet(0);

						//new item
						item = new EvalItem(new Date(), currentUserId, itemText, description, sharing, classification, expert,
								expertDescription, scale, templateItems, usesNA, displayRows, scaleDisplaySetting, category, 
								locked);
						
						item.setEid(eid);
					}
					else {
						//if there is a template with this eid, update it
						setItemProperties(element, item);
					}
					
					//save or update item
					evalItemsLogic.saveItem(item, currentUserId);
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
					template = evalTemplatesLogic.getTemplateByEid(eid);
					if(template == null) {
						Boolean locked = new Boolean(Boolean.FALSE);
						Boolean expert = new Boolean(Boolean.FALSE);
						if(element.getChildText("LOCKED").trim().equals("1"))
							locked = Boolean.TRUE;
						else
							locked = Boolean.FALSE;
						if(element.getChildText("EXPERT").trim().equals("1"))
							expert = Boolean.TRUE;
						else
							expert = Boolean.FALSE;
						
						String owner = element.getChildText("OWNER");
						String type = element.getChildText("TYPE");
						String title = element.getChildText("TITLE");
						String description = element.getChildText("DESCR");
						String sharing = element.getChildText("SHARING");
						String expertDescription = element.getChildText("EXPERTDESCR");
						Set templateItems = new HashSet(0);
						
						//new template
						template = new EvalTemplate(new Date(), owner, type, title, description,  sharing, expert,
								expertDescription, templateItems, locked);
						template.setEid(eid);
						//TODO fix NPE evalExternalLogic.registerEntityEvent(EVENT_TEMPLATE_CREATE, template);
					}
					else {
						//update template
						setTemplateProperties(element, template);
						//TODO fix NPE evalExternalLogic.registerEntityEvent(EVENT_TEMPLATE_UPDATE, template);
					}
					
					evalTemplatesLogic.saveTemplate(template, currentUserId);
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
	 * Set EvalTemplate properties from XML Element data
	 * 
	 * @param element the Element
	 * @param template the EvalTemplate
	 */
	private void setTemplateProperties(Element element, EvalTemplate template) {
		template.setOwner(element.getChildText("OWNER"));
		template.setType(element.getChildText("TYPE"));
		template.setTitle(element.getChildText("TITLE"));
		template.setDescription(element.getChildText("DESCR"));
		template.setSharing(element.getChildText("SHARING"));
		if(element.getChildText("EXPERT").trim().equals("1"))
			template.setExpert(new Boolean(Boolean.TRUE));
		else
			template.setExpert(new Boolean(Boolean.FALSE));
		template.setExpertDescription(element.getChildText("EXPERTDESCR"));
		if(element.getChildText("LOCKED").trim().equals("1"))
			template.setLocked(new Boolean(Boolean.TRUE));
		else
			template.setLocked(new Boolean(Boolean.FALSE));
	}
	
	/**
	 * Set EvalTemplateItem properties from XML Element data
	 * 
	 * @param element the Element
	 * @param templateItem the TemplateItem
	 */
	private void setTemplateItemProperties(Element element, EvalTemplateItem templateItem) {
		EvalItem item = templateItem.getItem();
		templateItem.setUsesNA(item.getUsesNA());
		templateItem.setScaleDisplaySetting(item.getScaleDisplaySetting());
		templateItem.setDisplayRows(item.getDisplayRows());
		templateItem.setOwner(element.getChildText("OWNER"));
		templateItem.setResultsSharing(element.getChildText("RESULTS_SHARING"));
		String blockId = element.getChildText("BLOCK_ID");
		if(blockId != null && !blockId.trim().equals("")){
			try {
				templateItem.setBlockId(new Long(Long.parseLong(blockId)));
			}
			catch(NumberFormatException e) {
				log.warn("There was a problem with BLOCK_ID involving EvalTemplateItem with eid '" + templateItem.getEid() + "'. " + e);
				messages.add("There was a problem with BLOCK_ID involving EvalTemplateItem with eid '" + templateItem.getEid() + "'. " + e);
				//TODO add to audit trail
			}
		}
		String blockParent = element.getChildText("BLOCK_PARENT");
		if(blockParent != null && !blockParent.trim().equals("")){
			if(blockParent.trim().equals("1"))
				templateItem.setBlockParent(new Boolean(Boolean.TRUE));
			else
				templateItem.setBlockParent(new Boolean(Boolean.FALSE));
		}
		String displayOrder = element.getChildText("DISPLAY_ORDER");
		if(displayOrder != null && !displayOrder.trim().equals("")){
			try {
				templateItem.setDisplayOrder(new Integer(Integer.parseInt(element.getChildText("DISPLAY_ORDER"))));
			}
			catch(NumberFormatException e) {
				log.warn("There was a problem with DISPLAY_ORDER involving EvalTemplateItem with eid '" + templateItem.getEid() + "'. " + e);
				messages.add("There was a problem with DISPLAY_ORDER involving EvalTemplateItem with eid '" + templateItem.getEid() + "'. " + e);
				//TODO add to audit trail
			}
		}
		templateItem.setItemCategory(element.getChildText("ITEM_CATEGORY"));
	}
	
	/**
	 * Set EvalScale properties from XML Element data
	 * 
	 * @param element the Element
	 * @param scale the EvalScale
	 */
	private void setScaleProperties(Element element, EvalScale scale) {
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
		Hashtable order = new Hashtable();
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
	
	/**
	 * Set EvalItem properties from XML Element data
	 * 
	 * @param element the Element
	 * @param item the EvalItem
	 */
	private void setItemProperties(Element element, EvalItem item) {
		item.setOwner(element.getChildText("OWNER"));
		item.setItemText(element.getChildText("ITEM_TEXT"));
		item.setDescription(element.getChildText("DESCRIPTION"));
		item.setSharing(element.getChildText("SHARING"));
		item.setClassification(element.getChildText("CLASSIFICATION"));
		if(element.getChildText("EXPERT").trim().equals("1"))
			item.setExpert(new Boolean(Boolean.TRUE));
		else
			item.setExpert(new Boolean(Boolean.FALSE));
		item.setExpertDescription(element.getChildText("EXPERT_DESCRIPTION"));

		if(element.getChildText("USES_NA").trim().equals("1"))
			item.setUsesNA(new Boolean(Boolean.TRUE));
		else
			item.setUsesNA(new Boolean(Boolean.FALSE));
		
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
			String eid = element.getChildText("SCALE_EID");
			if(eid != null && eid.trim().length() != 0)
			{
				EvalScale scale = evalScalesLogic.getScaleByEid(eid);
				if(scale != null) {
					item.setScale(scale);
				}
				else {
					log.warn("Could not get EvalScale with eid '" + eid + "' for EvalItem with eid '" + item.getEid() + "' " + item.getItemText());
					messages.add("Could not get EvalScale with eid '" + eid + "' for EvalItem with eid '" + item.getEid() + "' " + item.getItemText());
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
	
	/**
	 * Save new or update existing EvalTemplateItems
	 * 
	 * @param doc the Document created from the XML ContentResource
	 */
	protected void saveOrUpdateTemplateItems(Document doc) {
		String eid = null;
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
					EvalTemplate template = evalTemplatesLogic.getTemplateByEid(templateEid);
					String owner = element.getChildText("OWNER");
					String resultsSharing = element.getChildText("RESULTS_SHARING");
					String level = element.getChildText("HIERARCHY_LEVEL");
					String nodeId = element.getChildText("HIERARCHY_NODE_ID");
					Integer displayOrder = null;
					
					//If no existing templateItem with this eid create a new templateItem with this eid
					EvalTemplateItem templateItem = evalItemsLogic.getTemplateItemByEid(eid);
					if(template != null && templateItem == null) {
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
						EvalItem item = evalItemsLogic.getItemByEid(itemEid);
						
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
						templateItem = 
							new EvalTemplateItem(new Date(), owner,	
									template, item, displayOrder, itemCategory, level, nodeId,
									displayRows, scaleDisplaySetting, usesNA, blockParent, blockId,
									resultsSharing);
						templateItem.setEid(eid);
						evalItemsLogic.saveTemplateItem(templateItem, currentUserId);
						templateItemsSaved++;
						
						//ping session to keep it alive
						numPersisted++;
						if(numPersisted % 100 == 0) {
							Session session = sessionManager.getCurrentSession();
							session.setActive();
						}
					}
					else {
						/*
						    TODO update template item?
						*/
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

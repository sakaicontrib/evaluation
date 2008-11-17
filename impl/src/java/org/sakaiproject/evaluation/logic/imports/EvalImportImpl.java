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

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.imports.EvalImport;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

/**
 * Process an XML ContentResource and save or update the evaluation data
 * contained in it
 * 
 * @author rwellis
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalImportImpl implements EvalImport {

	private static Log log = LogFactory.getLog(EvalImportImpl.class);
	private static Log metric = LogFactory.getLog("metrics."
			+ EvalImportImpl.class.getName());

	private static final String SIMPLEDATEFORMAT = "yyyy-MM-dd hh:mm aa"; // e.g. 15 min. after midnight <START_DATE>2008-10-20 12:15 AM</START_DATE>
	private static final String NOT_SAVED = "was not saved/updated in the database. Continuing with next. ";
	private static final String LOAD_ERR = "There was a problem loading ";
	private static final String PARSE_ERR = "There was an error parsing the XML data.";
	
	private EvalAuthoringService authoringService;
	public void setAuthoringService(EvalAuthoringService authoringService) {
		this.authoringService = authoringService;
	}
	
	private EvalCommonLogic commonLogic;
	public void setCommonLogic(EvalCommonLogic commonLogic) {
		this.commonLogic = commonLogic;
	}
	
	private EvaluationDao dao;
	public void setDao(EvaluationDao dao) {
		this.dao = dao;
	}
	
	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	
	private EvalEvaluationService evaluationService;
	public void setEvaluationService(EvalEvaluationService evaluationService) {
		this.evaluationService = evaluationService;
	}

	private EvalEvaluationSetupService evaluationSetupService;
	public void setEvaluationSetupService(
			EvalEvaluationSetupService evaluationSetupService) {
		this.evaluationSetupService = evaluationSetupService;
	}

	private Calendar cal;
	private SimpleDateFormat formatter;
	
	public void init() {
	}

	public EvalImportImpl() {
		formatter = new SimpleDateFormat(SIMPLEDATEFORMAT);
		cal = Calendar.getInstance();
	}
	
	/**
	 * Internal method</ br>
	 * Get the Element data contained in an XML Document based on an XPath expression
	 * 
	 * @param doc
	 * @param path
	 * @return
	 * @throws JDOMException
	 */
	private List<Object> getElementsOfDoc(Document doc, String path) throws JDOMException {
		List<Object> elements = new ArrayList();
		if (path != null) {
			try {
				XPath docsPath = XPath.newInstance(path);
				elements = docsPath.selectNodes(doc);
			}
				catch (JDOMException jde) {
					log.error("There was an error parsing the XML data using the path ' " + path + "' " + jde);
					throw new JDOMException(jde.getMessage());
			} 
				catch (Exception e) {
				log.error("There was a problem in getElementsFromDoc(String path) using the path '" + "' " + e);
				throw new RuntimeException(e.getMessage());
			}
		}
		return elements;
	}

	/**
	 * Parse and save or update evaluation data found in an XML ContentResource</ br>
	 * Saved in this order for db referential integrity.
	 * 
	 * @param id
	 *            The Reference id of the ContentResource
	 * @return String error message(s)
	 */
	public List<String> process(String id, String userId) {
		List<Long> skip = new ArrayList<Long>();
		List<String> messages = new ArrayList<String>();
		if (id == null || userId == null) {
			throw new IllegalArgumentException(
					"EvalImportImpl process() parameter(s) null.");
		}
		Document doc = null;
		InputStream in = null;
		try {

			// Note: object types not included in the XML file are skipped
			in = externalLogic.getStreamContent(id);
			doc = new SAXBuilder().build(in);
			
			saveOrUpdateScales(doc, messages,userId);
			
			saveOrUpdateItems(doc, messages,userId);

			//update template also replaces its template items
			saveOrUpdateTemplates(doc, messages,userId, skip);
			
			//skip template items treated in template revisions
			saveOrUpdateTemplateItems(doc, messages,userId, skip);

			saveOrUpdateEmailTemplates(doc, messages,userId);

			saveOrUpdateEvaluations(doc, messages,userId);

			saveOrUpdateAssignGroups(doc, messages,userId);
			
			if(log.isInfoEnabled())
				log.info("Importing of data is done.");
			
		} catch (JDOMException jde) {
			publishException("error","There was a problem parsing the XML data.", jde, messages);
		} catch (Exception e) {
			publishException("error","There was a problem loading the XML data.", e, messages);
		} finally {
			// close the input stream
			if (in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
					publishException("error","Unable to close input stream. " + id, ioe, messages);
				}
			}
			// remove the FilePickerHelper attachment that was created
			if (id != null) {
				if(! externalLogic.removeResource(id)) {
					publishException("warn","There was a problem deleting the FilePickerHelper attachment that was created '"
							+ id + "' ", null, messages);
				}
			}
		}
		return messages;
	}

	/**
	 * Save new or update existing EvalScales
	 * 
	 * @param doc
	 *            the Document created from the XML ContentResource
	 * @param messages
	 *            a collecting parameter pattern list of error messages
	 */
	protected void saveOrUpdateScales(Document doc, List<String> messages, String userId) {
		String eid = null;
		EvalScale scale = null;
		int scalesSaved = 0, scalesUpdated = 0;
		long start, end;
		float seconds;
		List<Object> elements = null;
		if(doc == null || userId == null)
			throw new RuntimeException("parameter(s) passed to saveOrUpdateScales null");
		start = System.currentTimeMillis();
		try {
			elements = getElementsOfDoc(doc, "/EVAL_DATA/EVAL_SCALES/EVAL_SCALE");
			// metrics
			logElementsFound("EvalScale", elements.size());
			for(Object o: elements) {
				try {
					Element element = (Element)o;
					eid = element.getChildText("EID");
					scale = authoringService.getScaleByEid(eid);
					if (scale == null) {
						// create new
						scale = newScale(element, messages);
						scalesSaved++;
						if((scalesSaved % 100) == 0) {
							externalLogic.setSessionActive();
						}
					} else {
						// update existing
						setScaleProperties(element, scale, messages);
						scalesUpdated++;
						if((scalesUpdated % 100) == 0) {
							externalLogic.setSessionActive();
						}
					}
					authoringService.saveScale(scale, userId);
				} catch (Exception e) {
					publishException("warn","EvalScale with eid '" + eid + "'" + NOT_SAVED, e, messages);
					continue;
				}
			}
			end = System.currentTimeMillis();
			seconds = (end - start) / 1000;
			// metrics
			logElapsedTime("EvalScale", scalesSaved, scalesUpdated, seconds);
		} catch (JDOMException jde) {
			publishException("error", PARSE_ERR, jde, messages);
		} catch (Exception e) {
			publishException("error", LOAD_ERR + "EvalScales.", e, messages);
		}
	}

	/**
	 * Save new or update existing EvalItems
	 * 
	 * @param doc
	 *            the Document created from the XML ContentResource
	 * @param messages
	 *            a collecting parameter pattern list of error messages
	 */
	protected void saveOrUpdateItems(Document doc, List<String> messages, String userId) {
		String eid = null;
		String classification = null;
		EvalItem item = null;
		int itemsSaved = 0, itemsUpdated = 0;
		long start, end;
		float seconds;
		List<Object> elements = null;
		if(doc == null || userId == null)
			throw new RuntimeException("parameter(s) passed to saveOrUpdateItems null");
		start = System.currentTimeMillis();
		try {
			elements = getElementsOfDoc(doc, "/EVAL_DATA/EVAL_ITEMS/EVAL_ITEM");
			// metrics
			logElementsFound("EvalItem", elements.size());
			for(Object o: elements) {
				try {
					Element element = (Element)o;
					eid = element.getChildText("EID");
					item = authoringService.getItemByEid(eid);
					if (item == null) {
						// create new
						item = newItem(element, messages, userId);
						authoringService.saveItem(item, userId);
						itemsSaved++;
						if((itemsSaved % 100) == 0) {
							externalLogic.setSessionActive();
						}
					} else {
						// update existing
						updateItemProperties(element, item, messages);
						authoringService.saveItem(item, userId);
						itemsUpdated++;

						//update copies
						List<Long> itemIds = authoringService.getIdsOfCopiesOfItem(item.getId());
						if(!itemIds.isEmpty()) {
							for(Long itemId : itemIds ) {
								item = authoringService.getItemById(itemId);
								updateItemProperties(element, item, messages);
								authoringService.saveItem(item, userId);
								itemsUpdated++;
							}
						}
						if((itemsUpdated % 100) == 0) {
							externalLogic.setSessionActive();
						}
					}

				} catch (Exception e) {
					publishException("warn","EvalItem with eid '" + eid + "'" + NOT_SAVED, e, messages);
					continue;
				}
			}
			end = System.currentTimeMillis();
			seconds = (end - start) / 1000;
			// metrics
			logElapsedTime("EvalItem", itemsSaved, itemsUpdated, seconds);
		} catch (JDOMException jde) {
			publishException("error", PARSE_ERR, jde, messages);
		} catch (Exception e) {
			publishException("error", LOAD_ERR + "EvalItems.", e, messages);
		}
	}

	/**
	 * Internal method</ br>
	 * Log an issue and add it to messages returned
	 * 
	 * @param level, String "warn" or "error"
	 * @param comment, String description of the problem in layman's terms
	 * @param e, the exception or null if other problem
	 * @param messages
	 */
	private void publishException(String level, String comment, Exception e, List<String> messages) {
		if("warn".equalsIgnoreCase(level))
			if(log.isWarnEnabled())
				if(e != null)
					log.warn(comment + ": " + e);
				else
					log.warn(comment);
		if("error".equalsIgnoreCase(level))
			if(log.isErrorEnabled())
				if(e != null)
					log.error(comment + ": " + e);
				else
					log.error(comment);
		messages.add(comment + ": " + e);
	}

	/**
	 * Save new or update existing EvalTemplates. </ br>
	 * If new, save new template and template items.
	 * If existing, check whether in use or not. If in use, update copies of template, template items, items.
	 * 
	 * @param doc
	 *            the Document created from the XML ContentResource
	 * @param messages
	 *            a collecting parameter pattern list of error messages
	 */
	protected void saveOrUpdateTemplates(Document doc, List<String> messages, String userId, List<Long> idsToSkip) {
		if(doc == null || userId == null)
			throw new IllegalArgumentException("Parameter(s) to saveOrUpdateTemplates() null.");
		
		List<Object> elements = null;
		List<Long> templatesIds = null;
		List<EvalTemplateItem> allTemplateItems = null;
		EvalTemplate template = null;
		Long[] newTemplateItemIds = new Long[]{};
		Set<EvalTemplateItem> templateItemSet = new HashSet<EvalTemplateItem>();
		String eid = null;
		int templatesSaved = 0, templatesUpdated = 0, templateItemsSaved = 0;
		long start, end;
		float seconds;
		
		start = System.currentTimeMillis();
		try {
			elements = getElementsOfDoc(doc, "/EVAL_DATA/EVAL_TEMPLATES/EVAL_TEMPLATE");
			// metrics
			logElementsFound("EvalTemplate", elements.size());
			for(Object o: elements) {
				try {
					Element element = (Element)o;
					eid = element.getChildText("EID");
					template = authoringService.getTemplateByEid(eid);
					if (template == null) {
						// create new
						if(authoringService.canCreateTemplate(userId)) {
							template = newTemplate(element, messages);
							authoringService.saveTemplate(template, userId);
							templatesSaved++;
							if((templatesSaved % 100)  == 0 ) {
								externalLogic.setSessionActive();
							}
							//get the associated template items
							allTemplateItems = newTemplateItems(doc, element);
							for(EvalTemplateItem ti : allTemplateItems) {
								//save template items and update linkages
								authoringService.saveTemplateItem(ti, userId);
								idsToSkip.add(ti.getId());
							}
							templateItemsSaved = templateItemsSaved + allTemplateItems.size();
						}
						else {
							log.warn("user with id '" + userId + "' cannot create EvalTemplate with eid '" + eid + "'.");
						}
					} else {
						// update existing
						if(authoringService.canModifyTemplate(userId, template.getId())) {
							//first update the original
							setTemplateProperties(element, template);
							authoringService.saveTemplate(template, userId);
							templatesUpdated++;
							allTemplateItems = authoringService.getTemplateItemsForTemplate(template.getId(),
									new String[] {},new String[] {}, new String[] {});
							for(EvalTemplateItem ti : allTemplateItems) {
								// remove the templateItem and update all linkages
								authoringService.deleteTemplateItem(ti.getId(), userId);
							}
							//save the contained XML template items with the updated template
							allTemplateItems.clear();
							//allTemplateItems contains the original template's new template items
							allTemplateItems = newTemplateItems(doc, element);
							for(EvalTemplateItem ti : allTemplateItems) {
								//save template items and update linkages
								authoringService.saveTemplateItem(ti, userId);
								idsToSkip.add(ti.getId());
							}
							templateItemsSaved = templateItemsSaved + allTemplateItems.size();
							
							//if the template is being used, there are also copies of template, item, template item
							//TODO isUsedTemplate found no EvalEvaluations because the template_fk was a copy of the original template id
							if(authoringService.isUsedTemplateCopyOf(template.getId())) {
								//copies of the template
								templatesIds = authoringService.getIdsOfCopiesOfTemplate(template.getId());
								List toDelete = new ArrayList();
								for(Long templateId:templatesIds) {
									if (authoringService.canModifyTemplate(externalLogic.getCurrentUserId(), templateId)) {
										template = authoringService.getTemplateById(templateId);
										setTemplateProperties(element, template);
										authoringService.saveTemplate(template, userId);
										templatesUpdated++;
										templateItemSet = template.getTemplateItems();
										toDelete.clear();
										for(EvalTemplateItem ti : templateItemSet) {
											if(authoringService.canControlTemplateItem(externalLogic.getCurrentUserId(), ti.getId())) {
												toDelete.add(new Long(ti.getId()));
											}
										}
										Long delete;
										for(Object id:toDelete) {
											delete = (Long)id;
											// remove the templateItem and update all linkages
											authoringService.deleteTemplateItem(delete, externalLogic.getCurrentUserId());
										}
										int index = allTemplateItems.size();
										Long templateItemIds[] = new Long[index];
										for(int i = 0; i < index; i++) {
											templateItemIds[i] = allTemplateItems.get(i).getId();
										}
										
										//save and link copies of template items and items to copy of template
										newTemplateItemIds = authoringService.copyTemplateItems(templateItemIds, template.getOwner(), true, template.getId(), true);
										templateItemsSaved = templateItemsSaved + newTemplateItemIds.length;
									}
									else {
										log.warn("user with id '" + userId + "' cannot modify EvalTemplate with eid '" + eid + "'.");
									}
								}
							}
							if((templatesUpdated % 100)  == 0 ) {
								externalLogic.setSessionActive();
							}
						}
					}
				}catch (Exception e) {
					publishException("warn","EvalTemplate with eid '" + eid + "'" + NOT_SAVED, e, messages);
					continue;
				}
			}
			end = System.currentTimeMillis();
			seconds = (end - start) / 1000;
			// metrics
			logElapsedTime("EvalTemplate", templatesSaved, templatesUpdated, seconds);
			logElapsedTime("EvalTemplateItem", templateItemsSaved, 0, seconds);
		} catch (JDOMException jde) {
			publishException("error", PARSE_ERR, jde, messages);

		} catch (Exception e) {
			publishException("error", LOAD_ERR + "EvalTemplates.", e, messages);
		}
	}

	/**
	 * Internal method</ br>
	 * 
	 * log the elapsed time in seconds to save/update objects
	 * 
	 * @param object
	 * @param total
	 * @param seconds
	 */
	private void logElapsedTime(String name, int saved, int updated, float seconds) {
		if(saved > 0 || updated > 0) {
			if (metric.isInfoEnabled()) {
				String message = "Metric";
				if(saved != 0) {
					message = message + " saving " + new Integer(saved);
					if(saved != 1) {
						message = message + " " + name + "s ";
					}
					else {
						message = message + " " + name + " ";
					}
				}
				if(updated != 0) {
					message = message + " updating " + new Integer(updated);
					if(updated != 1) {
						message = message + " " + name + "s ";
					}
					else {
						message = message + " " + name + " ";
					}
				}
				metric.info(message + "took " + seconds + " seconds.");
			}
		}
	}

	/**
	 * Get the EvalTemplateItems associated with the EvalTemplate contained in the Element
	 * 
	 * @param doc
	 * @param element
	 * @return 
	 */
	private List<EvalTemplateItem> newTemplateItems(Document doc, Element element) {
		if(element == null) {
			log.error("Element parameter to saveTemplateItems was null.");
			throw new RuntimeException("Element parameter to saveTemplateItems was null.");
		}
		String eid = element.getChildText("EID");
		List<EvalTemplateItem> evalTemplateItems = new ArrayList<EvalTemplateItem>();
		List<String> messages = new ArrayList();
		try {
			EvalTemplate template = authoringService.getTemplateByEid(eid);
			List<Object> elements = new ArrayList();
			elements = getElementsOfDoc(doc, "/EVAL_DATA/EVAL_TEMPLATEITEMS/EVAL_TEMPLATEITEM[TEMPLATE_EID=" + eid + "]");
			logElementsFound("EvalTemplateItem", elements.size());
			Element el = null;
			EvalTemplateItem templateItem = null;
			for (int i = 0; i < elements.size(); i++) {
				el = (Element) elements.get(i);
				templateItem = newTemplateItem(el, messages);
				//authoringService.saveTemplateItem(templateItem, externalLogic.getCurrentUserId());
				evalTemplateItems.add(templateItem);
			}
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
		//TODO messages
		return evalTemplateItems;
	}

	/**
	 * Save new or update existing EvalEmailTemplate
	 * 
	 * @param doc
	 *            the Document created from the XML ContentResource
	 * @param messages
	 *            a collecting parameter pattern list of error messages
	 */
	protected void saveOrUpdateEmailTemplates(Document doc,
			List<String> messages, String userId) {
		String eid = null, subject = null, message = null, type = null, defaultType = null;
		int emailTemplatesSaved = 0, emailTemplatesUpdated = 0;
		long start, end;
		float seconds;
		List<Object> elements = null;
		if(doc == null || userId == null)
			throw new RuntimeException("parameter(s) passed to saveOrUpdateEmailTemplates null");
		start = System.currentTimeMillis();
		try {
			elements = getElementsOfDoc(doc, "/EVAL_DATA/EVAL_EMAIL_TEMPLATES/EVAL_EMAIL_TEMPLATE");
			// metrics
			logElementsFound("EvalEmailTemplate", elements.size());
			for(Object o: elements) {
				try {
					Element element = (Element)o;
					eid = element.getChildText("EID");
					EvalEmailTemplate template = evaluationService
							.getEmailTemplateByEid(eid);
					if (template == null) {
						template = newEmailTemplate(element, messages);
						emailTemplatesSaved++;
						if((emailTemplatesSaved % 100) == 0) {
							externalLogic.setSessionActive();
						}
					} else {
						// update existing
						//TODO evaluationService.canControlEmailTemplate(userId, evaluationId, emailTemplateId)
						setEmailTemplateProperties(element, template);
						emailTemplatesUpdated++;
						if((emailTemplatesUpdated % 100) == 0) {
							externalLogic.setSessionActive();
						}
					}
					evaluationSetupService.saveEmailTemplate(template,
							userId);
				} catch (Exception e) {
					publishException("warn","EvalEmailTemplate with eid '" + eid + "'" + NOT_SAVED, e, messages);
					continue;
				}
			}
			end = System.currentTimeMillis();
			seconds = (end - start) / 1000;
			// metrics
			logElapsedTime("EvalEmailTemplate", emailTemplatesSaved, emailTemplatesUpdated, seconds);
		} catch (JDOMException jde) {
			publishException("error", PARSE_ERR, jde, messages);
		} catch (Exception e) {
			publishException("error", LOAD_ERR + "EvalEmailTemplates.", e, messages);
		}
	}

	/**
	 * Save new or update existing EvalTemplateItems
	 * (Deprecated by saveTemplateItems)
	 * 
	 * @param doc
	 *            the Document created from the XML ContentResource
	 * @param messages
	 *            a collecting parameter pattern list of error messages
	 */
	protected void saveOrUpdateTemplateItems(Document doc, List<String> messages, String userId, List<Long> idsToSkip) {
		String eid = null;
		int templateItemsSaved = 0, templateItemsUpdated = 0;
		long start, end;
		float seconds;
		List<Object> elements = null;
		if(doc == null || userId == null)
			throw new RuntimeException("parameter(s) passed to saveOrUpdateTemplateItems null");
		start = System.currentTimeMillis();
		try {
			elements = getElementsOfDoc(doc, "/EVAL_DATA/EVAL_TEMPLATEITEMS/EVAL_TEMPLATEITEM");
			// metrics
			logElementsFound("EvalTemplateItem", elements.size());
			for(Object o: elements) {
				try {
					Element element = (Element)o;
					eid = element.getChildText("EID");
					String templateEid = element.getChildText("TEMPLATE_EID");
					EvalTemplate template = authoringService
							.getTemplateByEid(templateEid);
					EvalTemplateItem templateItem = authoringService
							.getTemplateItemByEid(eid);
					if (template != null && templateItem == null) {
						// create new
						templateItem = newTemplateItem(element, messages);
						templateItemsSaved++;
						if((templateItemsSaved % 100) == 0) {
							externalLogic.setSessionActive();
						}
					} else {
						// update existing
						if(idsToSkip.contains(templateItem.getId()))
							continue;
						setTemplateItemProperties(templateItem, element, messages);
						templateItemsUpdated++;
						if((templateItemsUpdated % 100) == 0) {
							externalLogic.setSessionActive();
						}
					}
					authoringService.saveTemplateItem(templateItem,
							userId);
				} catch (Exception e) {
					publishException("warn","EvalTemplateItem with eid '" + eid + "'" + NOT_SAVED, e, messages);
					continue;
				}
			}
			end = System.currentTimeMillis();
			seconds = (end - start) / 1000;
			// metrics
			logElapsedTime("EvalTemplateItem", templateItemsSaved, templateItemsUpdated, seconds);
		} catch (JDOMException jde) {
			publishException("error", PARSE_ERR, jde, messages);
		} catch (Exception e) {
			publishException("error", LOAD_ERR + "EvaltemplateItems.", e, messages);
		}
	}

	/**
	 * Save new or update existing EvalEvaluations
	 * 
	 * @param doc
	 *            the Document created from the XML ContentResource
	 * @param messages
	 *            a collecting parameter pattern list of error messages
	 */
	private void saveOrUpdateEvaluations(Document doc, List<String> messages, String userId) {
		String eid = null;
		EvalEvaluation evaluation = null;
		int evaluationsSaved = 0, evaluationsUpdated = 0;
		long start, end;
		float seconds;
		List<Object> elements = null;
		if(doc == null || userId == null)
			throw new RuntimeException("parameter(s) passed to saveOrUpdateEvaluations null");
		start = System.currentTimeMillis();
		try {
			elements = getElementsOfDoc(doc, "/EVAL_DATA/EVAL_EVALUATIONS/EVAL_EVALUATION");
			// metrics
			logElementsFound("EvalEvaluation", elements.size());
			for(Object o: elements) {
				try {
					Element element = (Element)o;
					eid = element.getChildText("EID");
					evaluation = evaluationService.getEvaluationByEid(eid);
					if (evaluation == null) {
						// create new
						evaluation = newEvaluation(element, messages);
						evaluationSetupService.saveEvaluation(evaluation,
								userId, true);
						evaluationsSaved++;
						if((evaluationsSaved % 100) == 0) {
							externalLogic.setSessionActive();
						}
					} else {
						// update existing
						setEvaluationProperties(element, evaluation, messages);
						//save updates state based on current date settings
						evaluationSetupService.saveEvaluation(evaluation,
								userId, false);
						evaluationsUpdated++;
						if((evaluationsUpdated % 100) == 0) {
							externalLogic.setSessionActive();
						}
					}
				} catch (Exception e) {
					publishException("warn","EvalEvaluation with eid '" + eid + "'" + NOT_SAVED, e, messages);
					continue;
				}
			}
			end = System.currentTimeMillis();
			seconds = (end - start) / 1000;
			// metrics
			logElapsedTime("EvalEvaluation", evaluationsSaved, evaluationsUpdated, seconds);
		} catch (JDOMException jde) {
			publishException("error", PARSE_ERR, jde, messages);
		} catch (Exception e) {
			publishException("error", LOAD_ERR + "EvalEvaluations.", e, messages);
		}
	}

	/**
	 * Internal method</br>
	 * If the metric logger for this class is info-enabled,
	 * log the number of a type of element found in an XML document
	 * 
	 * @param object
	 * @param number
	 */
	private void logElementsFound(String object, int number) {
		if (metric.isInfoEnabled()) {
			String message = "Metric " + new Integer(number) + " " + object;
			if(number != 1) {
				metric.info(message + "s in XML.");
			}
			else {
				metric.info(message + " in XML.");
			}
		}
	}

	/**
	 * Save new or update existing EvalAssignGroups
	 * 
	 * @param doc
	 *            the Document created from the XML ContentResource
	 * @param messages
	 *            a collecting parameter pattern list of error messages
	 */
	private void saveOrUpdateAssignGroups(Document doc, List<String> messages, String userId) {
		String eid = null, evalEid = null;
		int assignGroupsSaved = 0, assignGroupsUpdated = 0;
		long start, end;
		float seconds;
		EvalAssignGroup evalAssignGroup = null;
		EvalEvaluation evalEvaluation = null;
		List<Object> elements = null;
		if(doc == null || userId == null)
			throw new RuntimeException("parameter(s) passed to saveOrUpdateAssignGroups null");
		start = System.currentTimeMillis();
		try {
			elements = getElementsOfDoc(doc, "/EVAL_DATA/EVAL_ASSIGN_GROUPS/EVAL_ASSIGN_GROUP");
			// metrics
			logElementsFound("EvalAssignGroup", elements.size());
			for(Object o: elements) {
				try {
					Element element = (Element)o;
					eid = element.getChildText("EID");
					evalEid = element.getChildText("EVAL_EVALUATION_EID");
					evalEvaluation = evaluationService.getEvaluationByEid(evalEid);
					if(evalEvaluation != null) {
						if(evaluationService.canCreateAssignEval(userId, evalEvaluation.getId())) {
							evalAssignGroup = evaluationService
									.getAssignGroupByEid(eid);
							if (evalAssignGroup == null) {
								// create new
								evalAssignGroup = newAssignGroup(element, messages);
								assignGroupsSaved++;
								if((assignGroupsSaved % 100) == 0) {
									externalLogic.setSessionActive();
								}
							} else {
								// update existing
								setAssignGroupProperties(element, evalAssignGroup);
								assignGroupsUpdated++;
								if(assignGroupsUpdated % 100 == 0) {
									externalLogic.setSessionActive();
								}
							}
							evaluationSetupService.saveAssignGroup(evalAssignGroup,
									userId);
						}
						else {
							log.warn("user with id '" + userId + "' cannot create/modify EvalAssignGroup with eid '" + eid + "'.");
						}
					}
					else {
						log.warn("EvalEvaluation assigned to EvalAsssignGroup with eid " + eid +  
								" and EvalEvaluation eid " + evalEid + "is null.");
					}	
				} catch (Exception e) {
					publishException("warn","EvalAssignGroup with eid '" + eid + "'" + NOT_SAVED, e, messages);
					continue;
				}
			}
			end = System.currentTimeMillis();
			seconds = (end - start) / 1000;
			// metrics
			logElapsedTime("EvalAssignGroup", assignGroupsSaved, assignGroupsUpdated, seconds);
		} catch (JDOMException jde) {
			publishException("error", PARSE_ERR, jde, messages);
		} catch (Exception e) {
			publishException("error", LOAD_ERR + "EvalAssignGroups.", e, messages);
		}
	}

	/**
	 * Create a new EvalTemplate item with properties from XML Element data
	 * 
	 * @param element
	 *            the Element
	 * @return the EvalTemplate
	 */
	private EvalTemplate newTemplate(Element element, List<String> messages) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			Boolean locked = element.getChildText("LOCKED").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			Boolean expert = element.getChildText("EXPERT").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			String owner = element.getChildText("OWNER");
			String type = element.getChildText("TYPE");
			String title = element.getChildText("TITLE");
			String description = element.getChildText("DESCR");
			String sharing = element.getChildText("SHARING");
			String expertDescription = element.getChildText("EXPERTDESCR");
			Set<EvalTemplateItem> templateItems = new HashSet<EvalTemplateItem>(
					0);
			// create new
			EvalTemplate template = new EvalTemplate(new Date(), owner, type,
					title, description, sharing, expert, expertDescription,
					templateItems, locked, false);
			template.setEid(eid);
			return template;
		} catch (Exception e) {
			throw new RuntimeException("newTemplate() eid '" + eid + "' " + e);
		}
	}

	/**
	 * Create a new EvalEmailTemplate item with properties from XML Element data
	 * 
	 * @param element
	 *            the Element
	 * @return the EvalEmailTemplate
	 */
	private EvalEmailTemplate newEmailTemplate(Element element, List<String> messages) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			String subject = element.getChildText("SUBJECT");
			String message = element.getChildText("MESSAGE");
			String type = element.getChildText("TEMPLATE_TYPE");
			String defaultType = element.getChildText("DEFAULTTYPE");
			// create new
			EvalEmailTemplate template = new EvalEmailTemplate(
					EvalExternalLogic.ADMIN_USER_ID, type, subject, message,
					defaultType);
			template.setEid(eid);
			return template;
		} catch (Exception e) {
			throw new RuntimeException("newEmailTemplate() eid '" + eid + "' "
					+ e);
		}
	}

	/**
	 * Set EvalTemplate properties from XML Element data
	 * 
	 * @param element
	 *            the Element
	 * @param template
	 *            the EvalTemplate
	 */
	private void setTemplateProperties(Element element, EvalTemplate template) {
		try {
			template.setOwner(element.getChildText("OWNER"));
			template.setType(element.getChildText("TYPE"));
			template.setTitle(element.getChildText("TITLE"));
			template.setDescription(element.getChildText("DESCR"));
			template.setSharing(element.getChildText("SHARING"));
			Boolean locked = element.getChildText("LOCKED").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			template.setLocked(locked);
			Boolean expert = element.getChildText("EXPERT").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			template.setExpert(expert);
		} catch (Exception e) {
			throw new RuntimeException("setTemplateProperties() eid '" + element.getChildText("EID")
					+ "' " + e);
		}
	}

	/**
	 * Set EvalEmailTemplate properties from XML Element data
	 * 
	 * @param element
	 *            the Element
	 * @param template
	 *            the EvalEmailTemplate
	 */
	private void setEmailTemplateProperties(Element element,
			EvalEmailTemplate template) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			template.setOwner(EvalExternalLogic.ADMIN_USER_ID);
			template.setMessage(element.getChildText("MESSAGE"));
			template.setSubject(element.getChildText("SUBJECT"));
			template.setType(element.getChildText("TEMPLATE_TYPE"));
			template.setDefaultType(element.getChildText("DEFAULTTYPE"));
		} catch (Exception e) {
			throw new RuntimeException("setEmailTemplateProperties() for eid '"
					+ eid + "' " + e);
		}
	}

	/**
	 * Create a new EvalTemplateItem item with properties from XML Element data
	 * 
	 * @param element
	 *            the Element
	 * @return the EvalTemplateItem
	 */
	private EvalTemplateItem newTemplateItem(Element element, List<String> messages) {
		String eid = null;
		Integer displayOrder = null;
		try {
			eid = element.getChildText("EID");
			String owner = element.getChildText("OWNER");
			String resultsSharing = element.getChildText("RESULTS_SHARING");
			String level = element.getChildText("HIERARCHY_LEVEL");
			String nodeId = element.getChildText("HIERARCHY_NODE_ID");
			String displayOrderString = (String) element
					.getChildText("DISPLAY_ORDER");
			if (displayOrderString != null
					&& !displayOrderString.trim().equals("")) {
				try {
					displayOrder = Integer.parseInt((String) element
							.getChildText("DISPLAY_ORDER"));
				} catch (NumberFormatException e) {
					publishException("warn","There was a problem with DISPLAY_ORDER involving EvalTemplateItem with eid '"
							+ eid + "'. ", e, messages);
				}
			}

			Boolean usesNA = new Boolean(Boolean.FALSE);
			if (element.getChildText("USES_NA").trim().equals("1"))
				usesNA = Boolean.TRUE;
			else
				usesNA = Boolean.FALSE;

			Long blockId = null;
			if ((String) element.getChildText("BLOCK_ID") != null
					&& !((String) element.getChildText("BLOCK_ID")).trim()
							.equals("")) {
				try {
					blockId = new Long(Long.parseLong((String) element
							.getChildText("BLOCK_ID")));
				} catch (NumberFormatException e) {
					publishException("warn","There was a problem with BLOCK_ID involving EvalTemplateItem with eid '"
							+ eid + "'. ", e, messages);
				}
			}
			Boolean blockParent = null;
			if ((String) element.getChildText("BLOCK_PARENT") != null
					&& !((String) element.getChildText("BLOCK_PARENT")).trim()
							.equals("")) {
				if (((String) element.getChildText("BLOCK_PARENT")).trim()
						.equals("1"))
					blockParent = new Boolean(Boolean.TRUE);
			}
			String itemEid = element.getChildText("ITEM_EID");
			EvalItem item = authoringService.getItemByEid(itemEid);
			String templateEid = element.getChildText("TEMPLATE_EID");
			EvalTemplate template = authoringService
					.getTemplateByEid(templateEid);

			Integer displayRows = null;
			EvalScale scale = null;
			String scaleDisplaySetting = null;
			String itemCategory = item.getCategory();

			// if not Essay type question
			if (!item.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) {
				scaleDisplaySetting = (String) element
						.getChildText("SCALE_DISPLAY_SETTING");
				// set scale from EvalItem
				if (item != null) {
					scale = item.getScale();
					if (scale == null) {
						publishException("warn","EvalScale is null for EvalTemplateItem with eid '"
								+ eid + "' for EvalTemplate '" + template.getTitle(), null, messages);
					}
				} else {
					publishException("warn","item is null for templateItem with eid '" + eid
							+ "' for template '" + template.getTitle(), null, messages);
				}
				String displayRowsString = element.getChildText("DISPLAY_ROWS");
				if (displayRowsString != null
						&& !displayRowsString.trim().equals("")) {
					try {
						displayRows = Integer.parseInt(element
								.getChildText("DISPLAY_ROWS"));
					} catch (NumberFormatException e) {
						publishException("warn","There was a problem with DISPLAY_ROWS involving EvalTemplateItem with eid '"
								+ eid + "'. ", e, messages);
					}
				}
			}
			// create new
			EvalTemplateItem evalTemplateItem = new EvalTemplateItem(
					new Date(), owner, template, item, displayOrder,
					itemCategory, level, nodeId, displayRows,
					scaleDisplaySetting, usesNA, false, blockParent, blockId,
					resultsSharing);
			evalTemplateItem.setEid(eid);
			return evalTemplateItem;
		} catch (Exception e) {
			throw new RuntimeException("newTemplateItem eid '" + eid + "' " + e);
		}
	}

	/**
	 * Set EvalTemplateItem properties from XML Element data
	 * 
	 * @param element
	 *            the Element
	 * @param templateItem
	 *            the EvalTemplateItem
	 */
	private void setTemplateItemProperties(EvalTemplateItem evalTemplateItem,
			Element element, List<String> messages) {
		String eid = null;
		try {
			eid = evalTemplateItem.getEid();
			evalTemplateItem
					.setOwner(new String(element.getChildText("OWNER")));
			evalTemplateItem.setResultsSharing(new String(element
					.getChildText("RESULTS_SHARING")));
			evalTemplateItem.setHierarchyLevel(element
					.getChildText("HIERARCHY_LEVEL"));
			evalTemplateItem.setHierarchyNodeId(new String(element
					.getChildText("HIERARCHY_NODE_ID")));
			Integer displayOrder = null;
			String displayOrderString = (String) element
					.getChildText("DISPLAY_ORDER");
			if (displayOrderString != null
					&& !displayOrderString.trim().equals("")) {
				try {
					displayOrder = Integer.parseInt((String) element
							.getChildText("DISPLAY_ORDER"));
				} catch (NumberFormatException e) {
					publishException("warn","There was a problem with DISPLAY_ROWS involving EvalTemplateItem with eid '"
							+ eid + "'. ", e, messages);
				}
			}
			evalTemplateItem.setDisplayOrder(displayOrder);
			Boolean usesNA = new Boolean(Boolean.FALSE);
			if (element.getChildText("USES_NA").trim().equals("1"))
				usesNA = Boolean.TRUE;
			else
				usesNA = Boolean.FALSE;
			evalTemplateItem.setUsesNA(usesNA);
			Long blockId = null;
			if ((String) element.getChildText("BLOCK_ID") != null
					&& !((String) element.getChildText("BLOCK_ID")).trim()
							.equals("")) {
				try {
					blockId = new Long(Long.parseLong((String) element
							.getChildText("BLOCK_ID")));
				} catch (NumberFormatException e) {
					publishException("warn","There was a problem with BLOCK_ID involving EvalTemplateItem with eid '"
							+ eid + "'. ", e, messages);
				}
			}
			evalTemplateItem.setBlockId(blockId);

			Boolean blockParent = null;
			if ((String) element.getChildText("BLOCK_PARENT") != null
					&& !((String) element.getChildText("BLOCK_PARENT")).trim()
							.equals("")) {
				if (((String) element.getChildText("BLOCK_PARENT")).trim()
						.equals("1"))
					blockParent = new Boolean(Boolean.TRUE);
			}
			evalTemplateItem.setBlockParent(blockParent);

			String itemEid = element.getChildText("ITEM_EID");
			EvalItem item = authoringService.getItemByEid(itemEid);
			evalTemplateItem.setItem(item);

			String templateEid = element.getChildText("TEMPLATE_EID");
			EvalTemplate template = authoringService
					.getTemplateByEid(templateEid);
			evalTemplateItem.setTemplate(template);

			String itemCategory = item.getCategory();
			evalTemplateItem.setCategory(itemCategory);

			Integer displayRows = null;
			String scaleDisplaySetting = null;
			// if not Essay type question
			if (!item.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) {
				scaleDisplaySetting = (String) element
						.getChildText("SCALE_DISPLAY_SETTING");
				evalTemplateItem.setScaleDisplaySetting(scaleDisplaySetting);

				String displayRowsString = element.getChildText("DISPLAY_ROWS");
				if (displayRowsString != null
						&& !displayRowsString.trim().equals("")) {
					try {
						displayRows = Integer.parseInt(element
								.getChildText("DISPLAY_ROWS"));
						evalTemplateItem.setDisplayRows(displayRows);
					} catch (NumberFormatException e) {
						publishException("warn","There was a problem with DISPLAY_ROWS involving EvalTemplateItem with eid '"
								+ eid + "'. ", e, messages);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("setTemplateItem eid '" + eid + "' " + e);
		}
	}

	/**
	 * Create a new EvalScale item with properties from XML Element data
	 * 
	 * @param element
	 *            the Element
	 * @return the EvalScale item
	 */
	private EvalScale newScale(Element element, List<String> messages) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			String title = element.getChildText("TITLE");
			String[] choices = null;
			Boolean locked = element.getChildText("LOCKED").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			Boolean expert = element.getChildText("EXPERT").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);

			// set options
			HashMap<Integer, String> order = new HashMap<Integer, String>();
			Element evalScaleOptions = element.getChild("EVAL_SCALE_OPTIONS");
			List options = evalScaleOptions.getChildren("EVAL_SCALE_OPTION");
			if (options != null && !options.isEmpty()) {
				choices = new String[options.size()];
				for (Iterator i = options.iterator(); i.hasNext();) {
					Element e = (Element) i.next();
					Integer key = Integer.parseInt(e
							.getChildText("SCALE_OPTION_INDEX"));
					String value = e.getChildText("SCALE_OPTION");
					order.put(key, value);
				}
				for (int i = 0; i < choices.length; i++) {
					choices[i] = (String) order.get(new Integer(i));
				}
			} else {
				publishException("warn","No options were found for EvalScale with eid '"
						+ eid + "' " + title, null, messages);
			}
			String owner = element.getChildText("OWNER");
			String sharing = element.getChildText("SHARING");
			String expertDescription = element
					.getChildText("EXPERT_DESCRIPTION");
			String ideal = element.getChildText("IDEAL");

			// create new
			EvalScale scale = new EvalScale(new Date(), owner, title,
					EvalConstants.SCALE_MODE_SCALE, sharing, expert,
					expertDescription, ideal, choices, locked);
			scale.setEid(eid);
			return scale;
		} catch (Exception e) {
			throw new RuntimeException("newScale() eid '" + eid + "' " + e);
		}
	}

	/**
	 * Set EvalScale properties from XML Element data
	 * 
	 * @param element
	 *            the Element
	 * @param scale
	 *            the EvalScale
	 */
	private void setScaleProperties(Element element, EvalScale scale, List<String> messages) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			scale.setOwner(element.getChildText("OWNER"));
			scale.setTitle(element.getChildText("TITLE"));
			scale.setSharing(element.getChildText("SHARING"));
			if (element.getChildText("EXPERT").trim().equals("1"))
				scale.setExpert(new Boolean(Boolean.TRUE));
			else
				scale.setExpert(new Boolean(Boolean.FALSE));
			scale.setExpertDescription(element
					.getChildText("EXPERT_DESCRIPTION"));
			scale.setIdeal(element.getChildText("IDEAL"));
			if (element.getChildText("LOCKED").trim().equals("1"))
				scale.setLocked(new Boolean(Boolean.TRUE));
			else
				scale.setLocked(new Boolean(Boolean.FALSE));
			// set options
			HashMap<Integer, String> order = new HashMap<Integer, String>();
			Element evalScaleOptions = element.getChild("EVAL_SCALE_OPTIONS");
			List<Element> options = evalScaleOptions.getChildren("EVAL_SCALE_OPTION");
			if (options != null && !options.isEmpty()) {
				String[] choices = new String[options.size()];
				for(Element e: options) {
					Integer key = Integer.parseInt(e
							.getChildText("SCALE_OPTION_INDEX"));
					String value = e.getChildText("SCALE_OPTION");
					order.put(key, value);
				}
				for (int i = 0; i < choices.length; i++) {
					choices[i] = (String) order.get(new Integer(i));
				}
				scale.setOptions(choices);
			} else {
				publishException("warn","No options were found for EvalScale with eid '"
						+ scale.getEid() + "' " + scale.getTitle(), null, messages);
			}
		} catch (Exception e) {
			throw new RuntimeException("setScaleProperties() eid '" + eid
					+ "' " + e);
		}
	}

	/**
	 * Create a new EvalItem item with properties from XML Element data
	 * 
	 * @param element
	 *            the Element
	 * @return the EvalItem item
	 */
	private EvalItem newItem(Element element, List<String> messages, String userId) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			EvalScale scale;
			String scaleDisplaySetting;
			Integer displayRows = null;
			Boolean locked = element.getChildText("LOCKED").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			Boolean expert = element.getChildText("EXPERT").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			Boolean usesNA = element.getChildText("USES_NA").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			String displayRowsString = element.getChildText("DISPLAY_ROWS");
			if (displayRowsString != null
					&& !displayRowsString.trim().equals("")) {
				try {
					displayRows = Integer.parseInt(element
							.getChildText("DISPLAY_ROWS"));
				} catch (NumberFormatException e) {
					publishException("warn","There was a problem with DISPLAY_ROWS involving EvalItem with eid '"
							+ eid + "'. ", e, messages);
				}
			}

			// if Essay type question do not set scaleDisplaySetting & scale
			scaleDisplaySetting = null;
			scale = null;
			if (!((String) element.getChildText("CLASSIFICATION"))
					.equals(EvalConstants.ITEM_TYPE_TEXT)) {

				scaleDisplaySetting = (String) element
						.getChildText("SCALE_DISPLAY_SETTING");
				// set scale
				String scaleEid = element.getChildText("SCALE_EID");
				if (scaleEid != null && scaleEid.trim().length() != 0) {
					scale = authoringService.getScaleByEid(scaleEid);
					if (scale == null) {
						publishException("warn","EvalScale is null for EvalItem with eid '"
								+ eid + "' "
								+ element.getChildText("ITEM_TEXT"), null, messages);
					}
				} else {
					publishException("warn","Could not get EvalScale by eid for EvalItem with eid '"
							+ eid + "' "
							+ element.getChildText("ITEM_TEXT"), null, messages);
				}
			}
			String itemText = element.getChildText("ITEM_TEXT");
			String description = element.getChildText("DESCRIPTION");
			String sharing = element.getChildText("SHARING");
			String classification = element.getChildText("CLASSIFICATION");
			String expertDescription = element
					.getChildText("EXPERT_DESCRIPTION");
			String category = element.getChildText("CATEGORY");
			Set<EvalTemplateItem> templateItems = new HashSet<EvalTemplateItem>(
					0);

			// create new
			EvalItem item = new EvalItem(new Date(), userId, itemText,
					description, sharing, classification, expert,
					expertDescription, scale, templateItems, usesNA, false,
					displayRows, scaleDisplaySetting, category, locked);

			item.setEid(eid);
			return item;
		} catch (Exception e) {
			throw new RuntimeException("newItem() eid '" + eid + "' " + e);
		}
	}

	/**
	 * Set EvalItem properties from XML Element data
	 * 
	 * @param element
	 *            the Element
	 * @param item
	 *            the EvalItem
	 */
	private void setItemProperties(Element element, EvalItem item, List<String> messages) {
		try {
			item.setOwner(element.getChildText("OWNER"));
			item.setItemText(element.getChildText("ITEM_TEXT"));
			item.setDescription(element.getChildText("DESCRIPTION"));
			item.setSharing(element.getChildText("SHARING"));
			item.setClassification(element.getChildText("CLASSIFICATION"));
			Boolean locked = element.getChildText("LOCKED").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			item.setLocked(locked);
			Boolean expert = element.getChildText("EXPERT").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			item.setExpert(expert);
			Boolean usesNA = element.getChildText("USES_NA").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			item.setUsesNA(usesNA);
			item.setCategory(element.getChildText("CATEGORY"));
			if (element.getChildText("LOCKED").trim().equals("1"))
				item.setLocked(new Boolean(Boolean.TRUE));
			else
				item.setLocked(new Boolean(Boolean.FALSE));

			// if Scaled type question
			if (!item.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) {
				item.setDisplayRows(null);
				item.setScaleDisplaySetting(element
						.getChildText("SCALE_DISPLAY_SETTING"));
				// set scale
				String scaleEid = element.getChildText("SCALE_EID");
				if (scaleEid != null && scaleEid.trim().length() != 0) {
					EvalScale scale = authoringService.getScaleByEid(scaleEid);
					if (scale != null) {
						item.setScale(scale);
					} else {
						publishException("warn","Could not get EvalScale with eid '"
								+ scaleEid + "' for EvalItem with eid '"
								+ item.getEid() + "' " + item.getItemText(), null, messages);
					}
				} else {
					publishException("warn","Could not get EvalScale by eid for EvalItem with eid '"
							+ item.getEid() + "' " + item.getItemText(), null, messages);
				}
			}
			// if Essay type question
			if (item.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) {
				item.setScale(null);
				item.setScaleDisplaySetting(null);
				String displayRows = element.getChildText("DISPLAY_ROWS");
				if (displayRows != null && !displayRows.trim().equals("")) {
					try {
						item.setDisplayRows(new Integer(Integer.parseInt(element
								.getChildText("DISPLAY_ROWS"))));
					} catch (NumberFormatException e) {
						publishException("warn","There was a problem with DISPLAY_ROWS involving EvalItem with eid '"
								+ item.getEid() + "'. ", e, messages);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("setItemProperties() '" + item.toString() + "' "
					+ e);
		}
	}
	
	private void updateItemProperties(Element element, EvalItem item, List<String> messages) {
		try {
			String classification = item.getClassification();
			//update item properties from XML
			setItemProperties(element, item, messages);
			
			//CT-731 a change in item classification (Scaled, Essay) requires coordinated change in template item
			if(element.getChildText("CLASSIFICATION") == null)
				throw new RuntimeException("CLASSIFICATION is null.");
			
			if(!classification.equals(element.getChildText("CLASSIFICATION"))) {
				List<EvalTemplateItem> templateItems = authoringService. getTemplateItemsUsingItem(item.getId());
				if(EvalConstants.ITEM_TYPE_TEXT.equals(element.getChildText("CLASSIFICATION"))) {
					//"Scaled" -> "Essay"
					for(EvalTemplateItem ti: templateItems) {
						ti.setScaleDisplaySetting(null);
						ti.setDisplayRows(item.getDisplayRows());
						ti.setUsesNA(item.getUsesNA());
						authoringService.saveTemplateItem(ti, externalLogic.getCurrentUserId());
					}
				}
				else if (EvalConstants.ITEM_TYPE_SCALED.equals(element.getChildText("CLASSIFICATION"))) {
					//"Essay" -> "Scaled"
					for(EvalTemplateItem ti: templateItems) {
						ti.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_FULL);
						ti.setDisplayRows(null);
						ti.setUsesNA(item.getUsesNA());
						authoringService.saveTemplateItem(ti, externalLogic.getCurrentUserId());
					}
				}
				else {
					if(log.isWarnEnabled())
						log.warn("CLASSIFICATION change type is not implemented.");
				}
			}
		}
		catch(Exception e) {
			throw new RuntimeException("updateItemProperties() '" + item.toString() + "' "
					+ e);
		}
	}

	/**
	 * Create a new EvalAssignGroup item with properties from XML Element data
	 * 
	 * @param element
	 *            the Element
	 * @return the EvalAssignGroup item
	 */
	private EvalAssignGroup newAssignGroup(Element element, List<String> messages) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			if (eid == null || "".equals(eid)) {
				publishException("warn","EvalAssignGroup with eid '" + eid
						+ "' was not saved/updated in the database, because eid was missing.",null,messages);
				throw new RuntimeException("EvalAssignGroup with eid '" + eid + "': eid missing.");
			}
			String providerId = element.getChildText("PROVIDER_ID");
			if (providerId == null || "".equals(providerId)) {
				publishException("warn","EvalAssignGroup with eid '" + eid
						+ "' was not saved/updated in the database, because provider id was missing.",null,messages);
				throw new RuntimeException("EvalAssignGroup with eid '" + eid + "': provider id missing.");
			}
			String owner = element.getChildText("OWNER");
			String groupType = element.getChildText("GROUP_TYPE");
			String evalEid = element.getChildText("EVAL_EVALUATION_EID");
			EvalEvaluation evaluation = evaluationService
					.getEvaluationByEid(evalEid);
			Boolean instructorApproval = element.getChildText(
					"INSTRUCTOR_APPROVAL").trim().equals("1") ? new Boolean(
					Boolean.TRUE) : new Boolean(Boolean.FALSE);
			Boolean instructorsViewResults = element.getChildText(
					"INSTRUCTOR_VIEW_RESULTS").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			Boolean studentsViewResults = element.getChildText(
					"STUDENT_VIEW_RESULTS").trim().equals("1") ? new Boolean(
					Boolean.TRUE) : new Boolean(Boolean.FALSE);
			// create new
			EvalAssignGroup evalAssignGroup = new EvalAssignGroup(owner,
					providerId, groupType, instructorApproval,
					instructorsViewResults, studentsViewResults, evaluation);
			evalAssignGroup.setEid(eid);
			return evalAssignGroup;
		} catch (Exception e) {
			throw new RuntimeException("newAssignGroup() eid '" + eid + "' "
					+ e);
		}
	}

	/**
	 * Set EvalAssignGroup properties from XML Element data
	 * 
	 * @param element
	 *            the Element
	 * @param evalAssignGroup
	 *            the EvalAssignEvaluation item
	 */
	private void setAssignGroupProperties(Element element,
			EvalAssignGroup evalAssignGroup) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			evalAssignGroup.setEid(eid);
			Boolean instructorApproval = element.getChildText(
					"INSTRUCTOR_APPROVAL").trim().equals("1") ? new Boolean(
					Boolean.TRUE) : new Boolean(Boolean.FALSE);
			Boolean instructorsViewResults = element.getChildText(
					"INSTRUCTOR_VIEW_RESULTS").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			Boolean studentsViewResults = element.getChildText(
					"STUDENT_VIEW_RESULTS").trim().equals("1") ? new Boolean(
					Boolean.TRUE) : new Boolean(Boolean.FALSE);
			EvalEvaluation evaluation = evaluationService
					.getEvaluationByEid(element.getChildText("EVAL_EVALUATION_EID"));
			evalAssignGroup.setEvaluation(evaluation);
			String groupType = element.getChildText("GROUP_TYPE");
			evalAssignGroup.setEvalGroupType(groupType);
			String providerId = element.getChildText("PROVIDER_ID");
			evalAssignGroup.setEvalGroupId(providerId);
			evalAssignGroup.setInstructorApproval(instructorApproval);
			evalAssignGroup.setInstructorsViewResults(instructorsViewResults);
			evalAssignGroup.setLastModified(new Date());
			String owner = element.getChildText("OWNER");
			evalAssignGroup.setOwner(owner);
			evalAssignGroup.setStudentsViewResults(studentsViewResults);
		} catch (Exception e) {
			throw new RuntimeException("setAssignGroupProperties() eid '" + eid
					+ "' " + e);
		}
	}

	/**
	 * Create a new EvalEvaluation item with properties from XML Element data
	 * 
	 * @param element
	 *            the Element
	 * @return the EvalEvaluation item
	 */
	private EvalEvaluation newEvaluation(Element element, List<String> messages) {
		String eid = null;
		try {
			eid = element.getChildText("EID");
			String state = EvalConstants.EVALUATION_STATE_PARTIAL;
			EvalTemplate addedTemplate = null;
			eid = element.getChildText("EID");
			if (eid == null || "".equals(eid)) {
				log
						.warn("EvalEvaluation was not saved/updated in the database, because eid was missing.");
				messages
						.add("EvalEvaluation was not saved/updated in the database, because eid was missing.");
			}
			String title = element.getChildText("TITLE");
			String owner = element.getChildText("OWNER");
			Date startDate = getDate(element.getChildText("START_DATE"));
			Date dueDate = getDate(element.getChildText("DUE_DATE"));
			Date stopDate = getDate(element.getChildText("STOP_DATE"));
			Date viewDate = getDate(element.getChildText("VIEW_DATE"));
			Date studentsDate = getDate(element.getChildText("STUDENTS_DATE"));
			Date instructorsDate = getDate(element
					.getChildText("INSTRUCTORS_DATE"));
			// (UM) use single email templates as Available, Reminder
			/*
			EvalEmailTemplate availableEmailTemplate = evaluationService
					.getDefaultEmailTemplate(element
							.getChildText("AVAILABLE_EMAIL_TEMPLATE"));
			EvalEmailTemplate reminderEmailTemplate = evaluationService
					.getDefaultEmailTemplate(element
							.getChildText("REMINDER_EMAIL_TEMPLATE"));
			*/
			EvalEmailTemplate availableEmailTemplate = evaluationService
			.getEmailTemplateByEid(element
					.getChildText("AVAILABLE_EMAIL_TEMPLATE_EID"));
			EvalEmailTemplate reminderEmailTemplate = evaluationService
			.getEmailTemplateByEid(element
					.getChildText("REMINDER_EMAIL_TEMPLATE_EID"));
			
			EvalTemplate template = authoringService.getTemplateByEid(element
					.getChildText("TEMPLATE_EID"));
			String instructions = element.getChildText("INSTRUCTIONS");
			if (instructions == null || instructions.trim().equals("")) {
				instructions = null;
			}
			String instructorOpt = element.getChildText("INSTRUCTOR_OPT");
			if (instructorOpt == null || instructorOpt.trim().equals("")) {
				instructorOpt = null;
			}
			Integer reminderDays = 0;
			String reminderDaysString = element.getChildText("REMINDER_DAYS")
					.trim();
			if (reminderDaysString != null
					&& !reminderDaysString.trim().equals("")) {
				try {
					reminderDays = Integer.parseInt(reminderDaysString);
				} catch (NumberFormatException e) {
					publishException("warn","There was a problem with REMINDER_DAYS involving EvalEvaluation with eid '"
							+ eid + "'. ", e, messages);
				}
			}
			String reminderFromEmail = element
					.getChildText("REMINDER_FROM_EMAIL");
			String termId = element.getChildText("TERM_ID");
			if (termId == null || termId.trim().equals("")) {
				termId = null;
			}
			String authControl = element.getChildText("AUTH_CONTROL");
			String evalCategory = element.getChildText("EVAL_CATEGORY");

			Boolean resultsPrivate = element.getChildText("RESULTS_PRIVATE")
					.trim().equals("1") ? new Boolean(Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			Boolean blankResponsesAllowed = element.getChildText(
					"BLANK_RESPONSES_ALLOWED").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			Boolean modifyResponsesAllowed = element.getChildText(
					"MODIFY_RESPONSES_ALLOWED").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			Boolean locked = element.getChildText("LOCKED").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			Boolean unregisteredAllowed = element.getChildText(
					"UNREGISTERED_ALLOWED").trim().equals("1") ? new Boolean(
					Boolean.TRUE) : new Boolean(Boolean.FALSE);

			String resultsPrivacy = EvalConstants.SHARING_VISIBLE;
			if (resultsPrivate) {
				resultsPrivacy = EvalConstants.SHARING_PRIVATE;
			}

			// create new
			EvalEvaluation evaluation = new EvalEvaluation(
					EvalConstants.EVALUATION_TYPE_EVALUATION, owner, title,
					instructions, startDate, dueDate, stopDate, viewDate,
					false, studentsDate, false, instructorsDate, state,
					EvalConstants.SHARING_VISIBLE, instructorOpt, reminderDays,
					reminderFromEmail, termId, availableEmailTemplate,
					reminderEmailTemplate, template, new HashSet(0),
					blankResponsesAllowed, modifyResponsesAllowed,
					unregisteredAllowed, locked, authControl, evalCategory);
			evaluation.setEid(eid);
			evaluation.setAvailableEmailSent(Boolean.FALSE);
			return evaluation;
		} catch (Exception e) {
			throw new RuntimeException("newEvaluation() eid '" + eid + "' " + e);
		}
	}

	/**
	 * Set EvalEvaluation properties from XML Element data
	 * Template is excluded from the properties being set
	 * 
	 * @param element
	 *            the Element
	 * @param evaluation
	 *            the EvalEvaluation
	 */
	private void setEvaluationProperties(Element element,
			EvalEvaluation evaluation, List<String> messages) {
 		String eid = null;
		try {
			eid = element.getChildText("EID");
			if (eid == null || "".equals(eid)) {
				log
						.warn("EvalEvaluation was not saved/updated in the database, because eid was missing.");
				messages
						.add("EvalEvaluation was not saved/updated in the database, because eid was missing.");
				throw new IllegalArgumentException(
						"Eid missing for EvalEvaluation");
			}
			evaluation.setTitle(new String(element.getChildText("TITLE")));
			evaluation.setOwner(new String(element.getChildText("OWNER")));
			evaluation
					.setStartDate(getDate(element.getChildText("START_DATE")));
			evaluation.setDueDate(getDate(element.getChildText("DUE_DATE")));
			evaluation.setStopDate(getDate(element.getChildText("STOP_DATE")));
			evaluation.setViewDate(getDate(element.getChildText("VIEW_DATE")));
			evaluation.setStudentsDate(getDate(element
					.getChildText("STUDENTS_DATE")));
			evaluation.setInstructorsDate(getDate(element
					.getChildText("INSTRUCTORS_DATE")));

			Boolean resultsPrivate = element.getChildText("RESULTS_PRIVATE")
					.trim().equals("1") ? new Boolean(Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			String resultsPrivacy = EvalConstants.SHARING_VISIBLE;
			if (resultsPrivate) {
				resultsPrivacy = EvalConstants.SHARING_PRIVATE;
			}
			Boolean blankResponsesAllowed = element.getChildText(
					"BLANK_RESPONSES_ALLOWED").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			Boolean modifyResponsesAllowed = element.getChildText(
					"MODIFY_RESPONSES_ALLOWED").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			Boolean locked = element.getChildText("LOCKED").trim().equals("1") ? new Boolean(
					Boolean.TRUE)
					: new Boolean(Boolean.FALSE);
			Boolean unregisteredAllowed = element.getChildText(
					"UNREGISTERED_ALLOWED").trim().equals("1") ? new Boolean(
					Boolean.TRUE) : new Boolean(Boolean.FALSE);
			evaluation.setResultsSharing(resultsPrivacy);
			evaluation.setBlankResponsesAllowed(blankResponsesAllowed);
			evaluation.setModifyResponsesAllowed(modifyResponsesAllowed);
			evaluation.setUnregisteredAllowed(unregisteredAllowed);
			evaluation.setLocked(locked);
			EvalEmailTemplate availableEmailTemplate = evaluationService
			.getEmailTemplateByEid(element
					.getChildText("AVAILABLE_EMAIL_TEMPLATE_EID"));
			evaluation.setAvailableEmailTemplate(availableEmailTemplate);
			EvalEmailTemplate reminderEmailTemplate = evaluationService
			.getEmailTemplateByEid(element
					.getChildText("REMINDER_EMAIL_TEMPLATE_EID"));
			evaluation.setReminderEmailTemplate(reminderEmailTemplate);
			/*
			 * CT-682
			 * Evaluation template is a copy of the original so do not
			 * reset it to the original or check for getCopyOf() will fail later
			 * evaluation.setTemplate(authoringService.getTemplateByEid(element
		     * .getChildText("TEMPLATE_EID")));
			*/
			String instructions = element.getChildText("INSTRUCTIONS");
			if (instructions == null || instructions.trim().equals("")) {
				instructions = null;
			}
			evaluation.setInstructions(instructions);

			String instructorOpt = element.getChildText("INSTRUCTOR_OPT");
			if (instructorOpt == null || instructorOpt.trim().equals("")) {
				instructorOpt = null;
			}
			evaluation.setInstructorOpt(instructorOpt);

			Integer reminderDays = 0;
			String reminderDaysString = element.getChildText("REMINDER_DAYS")
					.trim();
			if (reminderDaysString != null
					&& !reminderDaysString.trim().equals("")) {
				try {
					reminderDays = Integer.parseInt(reminderDaysString);
				} catch (NumberFormatException e) {
					publishException("warn","There was a problem with REMINDER_DAYS involving EvalEvaluation with eid '"
							+ eid + "'. ", e, messages);
				}
			}
			evaluation.setReminderDays(reminderDays);
			evaluation.setReminderFromEmail(new String(element
					.getChildText("REMINDER_FROM_EMAIL")));
			String termId = element.getChildText("TERM_ID");
			if (termId == null || termId.trim().equals("")) {
				termId = null;
			}
			evaluation.setTermId(termId);
			evaluation.setAuthControl(new String(element
					.getChildText("AUTH_CONTROL")));
			evaluation.setEvalCategory(new String(element
					.getChildText("EVAL_CATEGORY")));
		} catch (Exception e) {
			throw new RuntimeException("setEvaluationProperties() eid '" + eid
					+ "' " + e);
		}
	}

	/**
	 * A utility method to get a Date from a formatted String
	 * @param dateString a date String
	 * @return the corresponding Date
	 */
	private Date getDate(String dateString) {

		if (dateString == null || "".equals(dateString)) {
			return null;
		}
		SimpleDateFormat df = new SimpleDateFormat(SIMPLEDATEFORMAT);
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
	protected String getTime() {
		String now = null;
		long millis = System.currentTimeMillis();
		cal.setTimeInMillis(millis);
		now = formatter.format(cal.getTime());
		return now;
	}
}

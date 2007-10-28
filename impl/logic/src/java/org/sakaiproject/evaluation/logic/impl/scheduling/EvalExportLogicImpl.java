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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.externals.EvalExport;
import org.sakaiproject.evaluation.logic.externals.EvalExportLogic;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.SessionManager;

public class EvalExportLogicImpl implements EvalExportLogic {
	
	private static final Log log = LogFactory.getLog(EvalExportLogicImpl.class);
	
	private boolean locked;
	
	private Map<String,String> instructorMap;
	
	private EvalEvaluationsLogic evalEvaluationsLogic;
	public void setEvalEvaluationsLogic(EvalEvaluationsLogic evalEvaluationsLogic) {
		this.evalEvaluationsLogic = evalEvaluationsLogic;
	}

	private EvalExport evalExport;
	public void setEvalExport(EvalExport evalExport) {
		this.evalExport = evalExport;
	}
	
	private EvalJobLogic evalJobLogic;
	public void setEvalJobLogic(EvalJobLogic evalJobLogic) {
		this.evalJobLogic = evalJobLogic;
	}
	
	private IdManager idManager;
	public void setIdManager(IdManager idManager) {
		this.idManager = idManager;
	}
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void init() {
		locked = false;
		loadInstructorMap();
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalExportLogic#emailAllResponses()
	 */
	public List<String> emailAllResponses() {
		List<String> messages = new ArrayList();
		if(!locked) {
			locked = true;
			evalJobLogic.scheduleResponsesEmail(instructorMap);
		}
		return messages;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalExportLogic#writeAllResponses()
	 */
	public List<String> writeAllResponses() {
		List<String> messages = new ArrayList();
		if(!locked) {
			locked = true;
			messages = evalExport.writeResponses();
		}
		return messages;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalExportLogic#getLock()
	 */
	public boolean getLock() {
		return locked;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalExportLogic#isLocked()
	 */
	public boolean isLocked() {
		return locked;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalExportLogic#setLock(boolean)
	 */
	public void setLock(boolean lock) {
		String value = (new Boolean(lock)).toString();
		if(log.isInfoEnabled())
			log.info("EvalExportLogic#setLock(" + value + ")");
		this.locked = lock;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalExportLogic#getInstructorByEvaluationEid(java.lang.String)
	 */
	public String getInstructorByEvaluationEid(String evalEid) {
		String instructorEid = null;
		try {
			instructorEid = (String)instructorMap.get(evalEid);
		}
		catch(Exception e) {
			if(log.isWarnEnabled())
				log.warn("getInstructorByEvaluationEid() evalEid '" + evalEid + "': " + e);
		}
		return instructorEid;
	}
	
	/**
	 * Get the responses content for inclusion in email to instructors
	 * 
	 * @param the identity of the evaluation
	 * @return the responses
	 */
	public String getFormattedResponsesByEvaluationId(Long id) {

		String title = null;
		String instructor = null;
		String[] parts = new String[]{};
		String itemText = null;
		EvalTemplateItem templateItem = null;
		Map.Entry entry = null;
		EvalResponse evalResponse = null;
		
		//TODO Note: only submitted answers are output,should iterate through templateItems and get answer to item
		Set<EvalAnswer> answers = new HashSet<EvalAnswer>();
		
		EvalAnswer answer = null;
		EvalItem item = null;
		EvalItem orderItem = null;
		String answerString = null;
		Integer numResponses;
		int currentResponse = 0;
		int currentItem = 0;
		int position = 0;
		int index = 0;
		
		String lineEnd = "\n";
		String separator = "\n=====================\n";
		
		EvalEvaluation evaluation = evalEvaluationsLogic.getEvaluationById(id);
		StringBuffer buf = new StringBuffer();
		//Evaluation Title and Instructor
		title = evaluation.getTitle().trim();
		buf.append(title);
		buf.append(lineEnd);
		//TODO term code expansion
		buf.append("\nFall 2007");
		buf.append(lineEnd);
		
		//TODO this needs to be generalized before merging into trunk
		//Instructor
		buf.append("Instructor: ");
		parts = title.split(" ");
		if(parts[3] != null)
			instructor = parts[3];
		buf.append(instructor);
		buf.append(lineEnd);
		buf.append(lineEnd);
		//LEGEND
		buf.append("LEGEND\n");
		buf.append("\tSA - Strongly Agree\n");
		buf.append("\tA - Agree \n");
		buf.append("\tN - Neutral\n");
		buf.append("\tD - Disagree\n");
		buf.append("\tSD - Strongly Disagree\n");
		buf.append("\tNA - Does Not Apply\n");
		
		//display order of template items for this evaluation
		Map mapOrder = Collections.synchronizedMap(new TreeMap());
		Set<EvalTemplateItem> templateItems = evaluation.getTemplate().getTemplateItems();
		int arraySize = templateItems.size();
		for(Iterator<EvalTemplateItem> i = templateItems.iterator();i.hasNext();) {
			templateItem = i.next();
			mapOrder.put(templateItem.getDisplayOrder(),templateItem.getItem());
		}

		//get all responses to this evaluation
		Set<EvalResponse> responses = evaluation.getResponses();
		if(responses.isEmpty())
			if(log.isInfoEnabled())
				log.info("There are no responses to evaluation '" + evaluation.getTitle() + "' id " + evaluation.getId().toString());
		numResponses = new Integer(responses.size());
		currentResponse = 0;
		
		//for each response
		for(Iterator<EvalResponse> k = responses.iterator(); k.hasNext();) {
			try {
				evalResponse = k.next();
				currentResponse++;
				//EVALUATION
				buf.append(separator);
				buf.append("EVALUATION " + (new Integer(currentResponse)).toString() + " of " + numResponses.toString());
				buf.append(separator);
				
				//get all answers in a response
				answers = evalResponse.getAnswers();
				//a position corresponding to each template item, though answers may be fewer than template items
				EvalAnswer[] answerArray = new EvalAnswer[arraySize];

				//go through the answers putting them in display order
				for (Iterator <EvalAnswer> l = answers.iterator(); l.hasNext();) {
					answer = l.next();
					//get the item associated with the answer
					item = answer.getItem();
					//go through the display order starting at 1
					for(Iterator<Map.Entry> order = mapOrder.entrySet().iterator();order.hasNext();) {
						//Integer display order key = EvalItem value
						entry = order.next();
						orderItem = (EvalItem)entry.getValue();
						position = ((Integer)entry.getKey()).intValue();
						if(orderItem.getId() == item.getId()) {
							index = position - 1;
							answerArray[index] = answer;
						}
					}
				}//ordering
			
				currentItem = 0;
				//go through the ordered answers in array order (if no answer submitted skip null)
				EvalAnswer evalAnswer = null;
				EvalItem evalItem = null;
				for(int n = 0; n < answerArray.length; n++) {
					try {
						if(answerArray[n] == null)
							continue;
						evalAnswer = answerArray[n];
						evalItem = evalAnswer.getItem();
						//write item number
						currentItem++;
						buf.append((new Integer(currentItem)).toString() + ". ");
						itemText = evalItem.getItemText();
						
						//write answer
						if(evalItem.getClassification().equals(EvalConstants.ITEM_TYPE_SCALED)) {
							//response followed by question
							int numericResponse = evalAnswer.getNumeric();
							if(numericResponse == -1)
								answerString = "(NA)";
							else if(numericResponse == 0)
								answerString = "(SA)";
							else if(numericResponse == 1)
								answerString = "(A)";
							else if(numericResponse == 2)
								answerString = "(N)";
							else if(numericResponse == 3)
								answerString = "(D)";
							else if(numericResponse == 4)
								answerString = "(SD)";
							buf.append("\t" + answerString + "  ");
							buf.append(itemText + "\n");
						}
						else if(evalItem.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) {
							buf.append("\t" + itemText + "\n");
							answerString = evalAnswer.getText();
							buf.append("\t" + answerString + "\n");
						}
					}
					catch(Exception e) {
						if(log.isInfoEnabled())
							log.info("getFormattedResponsesByEvaluationId(): skipping array position " + e);
						continue;
					}
				}//go through answers in order
			}
			catch(Exception e) {
				if(log.isErrorEnabled())
					log.error("getFormattedResponsesByEvaluationId(): skipping response " + e);
			}
		}//iterate through responses
		return buf.toString();
	}
	
	private void loadInstructorMap() {
		//a quick, dirty kludge to be able to associate an evalaution (by eid) with an instructor (by eid)
		//TODO replace with an external logic api to associate instructor with evaluation
		instructorMap = new HashMap<String,String>();
		instructorMap.put("2","BALPAY");
		instructorMap.put("3","BALPAY");
		instructorMap.put("4","MARINSTE");
		instructorMap.put("5","MARINSTE");
		instructorMap.put("6","DKW");
		instructorMap.put("7","DKW");
		instructorMap.put("8","BIELAJEW");
		instructorMap.put("9","ATZMON");
		instructorMap.put("10","MIKEHART");
		instructorMap.put("11","BALPAY");
		instructorMap.put("12","JCL");
		instructorMap.put("13","JEFOSTER");
		instructorMap.put("14","MIKLOS");
		instructorMap.put("15","MIKLOS");
		instructorMap.put("16","MIKLOS");
		instructorMap.put("17","FLEMINGR");
		instructorMap.put("18","HEZHONG");
		instructorMap.put("19","TERJIMAN");
		instructorMap.put("20","ANDSTE");
		instructorMap.put("21","GSW");
		instructorMap.put("22","EDLARSEN");
		instructorMap.put("23","YYLAU");
		instructorMap.put("24","WRUTH");
		instructorMap.put("25","QWEI");
		instructorMap.put("26","LMWANG");
		instructorMap.put("27","RONGILG");
		instructorMap.put("28","KMKR");
		instructorMap.put("29","MDORF");
		instructorMap.put("30","MDORF");
		instructorMap.put("31","ERICVW");
		instructorMap.put("32","ALANUOFM");
		instructorMap.put("33","GAROH");
		instructorMap.put("34","MACHTY");
		instructorMap.put("35","BKWASELO");
		instructorMap.put("36","PRJORDAN");
		instructorMap.put("37","PETTIE");
		instructorMap.put("38","PETTIE");
		instructorMap.put("39","GOWTHAM");
		instructorMap.put("40","SKUTTY");
		instructorMap.put("41","WEBERJS");
		instructorMap.put("42","GOWTHAM");
		instructorMap.put("43","SKUTTY");
		instructorMap.put("44","WEBERJS");
		instructorMap.put("45","FREDTY");
		instructorMap.put("46","JPHILLI");
		instructorMap.put("47","PERRINC");
		instructorMap.put("48","ADAMBA");
		instructorMap.put("49","EMENOLLY");
		instructorMap.put("50","PCHOWDHR");
		instructorMap.put("51","PCHOWDHR");
		instructorMap.put("52","PUPUKID");
		instructorMap.put("53","EMENOLLY");
		instructorMap.put("54","JODALYST");
		instructorMap.put("55","PUPUKID");
		instructorMap.put("56","PUPUKID");
		instructorMap.put("57","ADAMBA");
		instructorMap.put("58","PERRINC");
		instructorMap.put("59","GHW");
		instructorMap.put("60","GRIZZLE");
		instructorMap.put("61","QUANN");
		instructorMap.put("62","QUANN");
		instructorMap.put("63","QUANN");
		instructorMap.put("64","SHIHCHUN");
		instructorMap.put("65","SHIHCHUN");
		instructorMap.put("66","AWLOK");
		instructorMap.put("67","SHIHCHUN");
		instructorMap.put("68","AWLOK");
		instructorMap.put("69","AWLOK");
		instructorMap.put("70","AGRBIC");
		instructorMap.put("71","MENGHUNG");
		instructorMap.put("72","SCR");
		instructorMap.put("73","YURIY");
		instructorMap.put("74","YURIY");
		instructorMap.put("75","YURIY");
		instructorMap.put("76","YURIY");
		instructorMap.put("77","YURIY");
		instructorMap.put("78","CHESNEYD");
		instructorMap.put("79","CHESNEYD");
		instructorMap.put("80","MATSMITH");
		instructorMap.put("81","TFROB");
		instructorMap.put("82","SMJOSHUA");
		instructorMap.put("83","EYLIU");
		instructorMap.put("84","EYLIU");
		instructorMap.put("85","NURRENBS");
		instructorMap.put("86","NICQ");
		instructorMap.put("87","JLIPMAN");
		instructorMap.put("88","JLIPMAN");
		instructorMap.put("89","ANUCH");
		instructorMap.put("90","JRSHUST");
		instructorMap.put("91","MPRADEEP");
		instructorMap.put("92","ANUCH");
		instructorMap.put("93","DUNGJADE");
		instructorMap.put("94","MPRADEEP");
		instructorMap.put("95","DUNGJADE");
		instructorMap.put("96","SWOLCHOK");
		instructorMap.put("97","SUGIH");
		instructorMap.put("98","CDAGLI");
		instructorMap.put("99","KIRBYB");
		instructorMap.put("100","CDAGLI");
		instructorMap.put("101","KIRBYB");
		instructorMap.put("102","MORGANA");
		instructorMap.put("103","ROIENTAN");
		instructorMap.put("104","CHARCHIC");
		instructorMap.put("105","CHARCHIC");
		instructorMap.put("106","MAHARBIZ");
		instructorMap.put("107","RACHIT");
		instructorMap.put("108","GANAGO");
		instructorMap.put("109","CHOIP");
		instructorMap.put("110","BIRKHOLZ");
		instructorMap.put("111","BGMULL");
		instructorMap.put("112","CSYANG");
		instructorMap.put("113","CHOIP");
		instructorMap.put("114","CHOIP");
		instructorMap.put("115","CHOIP");
		instructorMap.put("116","CHOIP");
		instructorMap.put("117","CSYANG");
		instructorMap.put("118","CSYANG");
		instructorMap.put("119","CSYANG");
		instructorMap.put("120","CSYANG");
		instructorMap.put("121","BGMULL");
		instructorMap.put("122","BGMULL");
		instructorMap.put("123","BGMULL");
		instructorMap.put("124","BGMULL");
		instructorMap.put("125","BIRKHOLZ");
		instructorMap.put("126","PEICHENG");
		instructorMap.put("127","MINWKIM");
		instructorMap.put("128","MINWKIM");
		instructorMap.put("129","ULABY");
		instructorMap.put("130","JONWILSO");
		instructorMap.put("131","JONWILSO");
		instructorMap.put("132","JONWILSO");
		instructorMap.put("133","KYPROS");
		instructorMap.put("134","KYPROS");
		instructorMap.put("135","DANZ");
		instructorMap.put("136","SHILPAN");
		instructorMap.put("137","SHILPAN");
		instructorMap.put("138","MATSMITH");
		instructorMap.put("139","MATSMITH");
		instructorMap.put("140","MELAGNEW");
		instructorMap.put("141","MELAGNEW");
		instructorMap.put("142","SPLAZA");
		instructorMap.put("143","DISHUMAN");
		instructorMap.put("144","MORNICK");
		instructorMap.put("145","ZHUXINEN");
		instructorMap.put("146","MORNICK");
		instructorMap.put("147","DANIAL");
		instructorMap.put("148","DANIAL");
		instructorMap.put("149","JAGREGOR");
		instructorMap.put("150","DATTOLI");
		instructorMap.put("151","TERREB");
		instructorMap.put("152","TERREB");
		instructorMap.put("153","TERREB");
		instructorMap.put("154","TERREB");
		instructorMap.put("155","TERREB");
		instructorMap.put("156","JCKAO");
		instructorMap.put("157","HKHSIAO");
		instructorMap.put("158","NOHJOONK");
		instructorMap.put("159","WANGCW");
		instructorMap.put("160","WANGCW");
		instructorMap.put("161","SHINUNG");
		instructorMap.put("162","KJARVIND");
		instructorMap.put("163","JWSCHMO");
		instructorMap.put("164","KJARVIND");
		instructorMap.put("165","RODERJEF");
		instructorMap.put("166","IKOUNTAN");
		instructorMap.put("167","SMITA");
		instructorMap.put("168","KJC");
		instructorMap.put("169","BWESTER");
		instructorMap.put("170","BWESTER");
		instructorMap.put("171","JDSORG");
		instructorMap.put("172","XINJU");
		instructorMap.put("173","BDWOLFE");
		instructorMap.put("174","BDWOLFE");
		instructorMap.put("175","NGORSKI");
		instructorMap.put("176","SVANDANA");
		instructorMap.put("177","SVANDANA");
		instructorMap.put("179","POULAKAS");
		instructorMap.put("180","POULAKAS");
		instructorMap.put("181","POULAKAS");
		instructorMap.put("183","VALE");
		instructorMap.put("184","DON");
		instructorMap.put("185","DON");
		instructorMap.put("186","VALE");
		instructorMap.put("187","BREHOB");
		instructorMap.put("188","MARTINJS");
		instructorMap.put("189","KIERAS");
		instructorMap.put("190","PETAR");
		instructorMap.put("191","AMIRM");
		instructorMap.put("192","WENTZLOF");
		instructorMap.put("193","NAJAFI");
		instructorMap.put("194","WLUEE");
		instructorMap.put("195","GUO");
		instructorMap.put("196","DMCS");
		instructorMap.put("197","TCARMON");
		instructorMap.put("198","WAKIN");
		instructorMap.put("199","METZGER");
		instructorMap.put("200","STARK");
		instructorMap.put("201","SMM");
		instructorMap.put("202","JFR");
		instructorMap.put("203","TWENISCH");
		instructorMap.put("204","MARIOS");
		instructorMap.put("205","BNOBLE");
		instructorMap.put("206","JIGNESH");
		instructorMap.put("207","GUSKOV");
		instructorMap.put("208","BAVEJA");
		instructorMap.put("209","LAIRD");
		instructorMap.put("210","FARNAM");
		instructorMap.put("211","SOLOWAY");
		instructorMap.put("212","STEPHANE");
		instructorMap.put("213","MNI");
		instructorMap.put("214","SOLOWAY");
		instructorMap.put("215","KJC");
		instructorMap.put("216","NEUHOFF");
		instructorMap.put("217","KANICKI");
		instructorMap.put("218","BASUA");
		instructorMap.put("219","PKB");
		instructorMap.put("220","SARABAND");
		instructorMap.put("221","SARABAND");
		instructorMap.put("222","TNORRIS");
		instructorMap.put("223","TNORRIS");
		instructorMap.put("224","ARRAYS");
		instructorMap.put("225","ARRAYS");
		instructorMap.put("226","ARRAYS");
		instructorMap.put("227","DST");
		instructorMap.put("228","DST");
		instructorMap.put("229","DURFEE");
		instructorMap.put("230","CLAYSCOT");
		instructorMap.put("231","SAVARI");
		instructorMap.put("232","PRADHANV");
		instructorMap.put("233","WINICK");
		instructorMap.put("234","TENEKET");
		instructorMap.put("235","DDV");
		instructorMap.put("236","DDV");
		instructorMap.put("237","DDV");
		instructorMap.put("238","TNM");
		instructorMap.put("239","KGSHIN");
		instructorMap.put("240","SHIYY");
		instructorMap.put("241","JHAYES");
		instructorMap.put("242","MAHLKE");
		instructorMap.put("243","JIGNESH");
		instructorMap.put("244","QSTOUT");
		instructorMap.put("245","APRAKASH");
		instructorMap.put("246","ZMAO");
		instructorMap.put("247","WELLMAN");
		instructorMap.put("248","ALMANTAS");
		instructorMap.put("249","MMOGHADD");
		instructorMap.put("250","ANASTAS");
		instructorMap.put("251","HERO");
		instructorMap.put("252","SCR");
		instructorMap.put("253","LINDERMA");
		instructorMap.put("254","SUZIQ");
		instructorMap.put("255","MDREWSKI");
		instructorMap.put("256","PSAVAGE");
		instructorMap.put("257","MDREWSKI");
		instructorMap.put("258","PSAVAGE");
		instructorMap.put("259","LOLAA");
		instructorMap.put("260","JASONHZY");
		instructorMap.put("261","JASONHZY");
		instructorMap.put("262","PHAPANIN");
		instructorMap.put("263","DUSTINC");
		instructorMap.put("264","DUSTINC");
		instructorMap.put("265","LAHANN");
		instructorMap.put("266","PABLO");
		instructorMap.put("267","DVHOEWYK");
		instructorMap.put("268","MJSOLO");
		instructorMap.put("269","FELUIS");
		instructorMap.put("270","LSHEREDA");
		instructorMap.put("271","PJHYANG");
		instructorMap.put("272","LANGELIE");
		instructorMap.put("273","PABLO");
		instructorMap.put("274","DVHOEWYK");
		instructorMap.put("275","GULARI");
		instructorMap.put("277","SAADETAL");
		instructorMap.put("278","ASOPHOCL");
		instructorMap.put("279","SAADETAL");
		instructorMap.put("280","PWOOLF");
		instructorMap.put("281","EWIS");
		instructorMap.put("282","BMBARKEL");
		instructorMap.put("283","SCHWANK");
		instructorMap.put("284","SFOGLER");
		instructorMap.put("285","HYWANG");
		instructorMap.put("286","HYWANG");
		instructorMap.put("287","RZIFF");
		instructorMap.put("288","LINIC");
		instructorMap.put("289","LINIC");
		instructorMap.put("290","WILDD");
		instructorMap.put("291","SMONTGOM");
		instructorMap.put("292","SMONTGOM");
		instructorMap.put("293","RZIFF");
		instructorMap.put("294","RLMICH");
		instructorMap.put("295","CMLASTO");
		instructorMap.put("296","FORD");
		instructorMap.put("297","CTENNEY");
		instructorMap.put("298","AMICHALA");
		instructorMap.put("299","PMSP");
		instructorMap.put("300","YUNSH");
		instructorMap.put("301","ACOTEL");
		instructorMap.put("302","MOLZ");
		instructorMap.put("303","VCLI");
		instructorMap.put("304","KDJOO");
		instructorMap.put("305","ELTAWIL");
		instructorMap.put("306","KAPILK");
		instructorMap.put("307","ELTAWIL");
		instructorMap.put("308","CREECHC");
		instructorMap.put("309","JIMLEWIS");
		instructorMap.put("310","NDK");
		instructorMap.put("311","AVERYD");
		instructorMap.put("312","AVERYD");
		instructorMap.put("313","HKHOURY");
		instructorMap.put("314","PHOTIOS");
		instructorMap.put("315","JONGWONL");
		instructorMap.put("316","RUGREEN");
		instructorMap.put("317","TMOLSON");
		instructorMap.put("318","JERLYNCH");
		instructorMap.put("319","GJPM");
		instructorMap.put("320","JWIGHT");
		instructorMap.put("321","SJWRIGHT");
		instructorMap.put("322","EVERETT");
		instructorMap.put("323","VKAMAT");
		instructorMap.put("324","PHOTIOS");
		instructorMap.put("325","PHOTIOS");
		instructorMap.put("326","EVERETT");
		instructorMap.put("327","BRILAKIS");
		instructorMap.put("328","RUGREEN");
		instructorMap.put("329","ROMANH");
		instructorMap.put("330","WHANSEN");
		instructorMap.put("331","RLMICH");
		instructorMap.put("332","DMBERRY");
		instructorMap.put("333","RASKIN");
		instructorMap.put("334","VKAMAT");
		instructorMap.put("335","GDHERRIN");
		instructorMap.put("336","GDHERRIN");
		instructorMap.put("337","JZHONG");
		instructorMap.put("338","JZHONG");
		instructorMap.put("339","JZHONG");
		instructorMap.put("340","JZHONG");
		instructorMap.put("341","RRINDLER");
		instructorMap.put("342","RRINDLER");
		instructorMap.put("343","RRINDLER");
		instructorMap.put("344","RRINDLER");
		instructorMap.put("345","RRINDLER");
		instructorMap.put("346","RRINDLER");
		instructorMap.put("347","JZHONG");
		instructorMap.put("348","JZHONG");
		instructorMap.put("349","JZHONG");
		instructorMap.put("350","JZHONG");
		instructorMap.put("351","RSAIGAL");
		instructorMap.put("352","YUZHOU");
		instructorMap.put("353","YUZHOU");
		instructorMap.put("354","YUZHOU");
		instructorMap.put("355","ANKITAGR");
		instructorMap.put("356","ANKITAGR");
		instructorMap.put("357","ANKITAGR");
		instructorMap.put("358","MARTINBJ");
		instructorMap.put("359","BARRYKAN");
		instructorMap.put("360","BARRYKAN");
		instructorMap.put("361","SIDITIS");
		instructorMap.put("362","SIDITIS");
		instructorMap.put("363","SIDITIS");
		instructorMap.put("364","BGOODSEL");
		instructorMap.put("365","MGOD");
		instructorMap.put("366","MGOD");
		instructorMap.put("367","MGOD");
		instructorMap.put("368","SNSAAB");
		instructorMap.put("369","SNSAAB");
		instructorMap.put("370","RSANTER");
		instructorMap.put("371","KLUDWIG");
		instructorMap.put("372","PSPICER");
		instructorMap.put("373","CWOOLLEY");
		instructorMap.put("374","CWOOLLEY");
		instructorMap.put("375","CWOOLLEY");
		instructorMap.put("376","PAGREEN");
		instructorMap.put("377","VANOYEN");
		instructorMap.put("378","VANOYEN");
		instructorMap.put("379","YABOZER");
		instructorMap.put("380","YABOZER");
		instructorMap.put("381","BABICH");
		instructorMap.put("382","BABICH");
		instructorMap.put("383","PHAMMETT");
		instructorMap.put("384","PHAMMETT");
		instructorMap.put("385","TJA");
		instructorMap.put("386","TJA");
		instructorMap.put("387","JLIUZZ");
		instructorMap.put("388","JLIUZZ");
		instructorMap.put("389","JLIUZZ");
		instructorMap.put("390","OMERT");
		instructorMap.put("391","SHERVIN");
		instructorMap.put("392","SKAMILAR");
		instructorMap.put("393","SKAMILAR");
		instructorMap.put("394","SHERVIN");
		instructorMap.put("395","SHERVIN");
		instructorMap.put("396","RJCOFFEY");
		instructorMap.put("397","MURTY");
		instructorMap.put("398","MURTY");
		instructorMap.put("399","MURTY");
		instructorMap.put("400","MEPELMAN");
		instructorMap.put("401","MEPELMAN");
		instructorMap.put("402","RSAIGAL");
		instructorMap.put("403","RSAIGAL");
		instructorMap.put("404","RLSMITH");
		instructorMap.put("405","XCHAO");
		instructorMap.put("406","MARTINBJ");
		instructorMap.put("407","MARTINBJ");
		instructorMap.put("408","SARTER");
		instructorMap.put("409","WMKEYSER");
		instructorMap.put("410","WMKEYSER");
		instructorMap.put("411","AYDING");
		instructorMap.put("412","AYDING");
		instructorMap.put("413","SJDESIGN");
		instructorMap.put("414","KEPPO");
		instructorMap.put("415","KEPPO");
		instructorMap.put("416","VNN");
		instructorMap.put("417","VNN");
		instructorMap.put("418","LILZ");
		instructorMap.put("419","LILZ");
		instructorMap.put("420","LILZ");
		instructorMap.put("421","SHIHANG");
		instructorMap.put("422","SHIHANG");
		instructorMap.put("423","VANOYEN");
		instructorMap.put("424","SERCAN");
		instructorMap.put("425","MURTY");
		instructorMap.put("426","WMKEYSER");
		instructorMap.put("427","TJA");
		instructorMap.put("428","SEIFORD");
		instructorMap.put("429","JASHAW");
		instructorMap.put("430","RASTA");
		instructorMap.put("431","KABAMBA");
		instructorMap.put("432","TIMSMITH");
		instructorMap.put("433","PETEGUS");
		instructorMap.put("434","HEIKELB");
		instructorMap.put("435","HEIKELB");
		instructorMap.put("436","HEIKELB");
		instructorMap.put("437","PETEGUS");
		instructorMap.put("438","PETEGUS");
		instructorMap.put("439","NICK");
		instructorMap.put("440","VEERAS");
		instructorMap.put("441","LPB");
		instructorMap.put("442","WDAHM");
		instructorMap.put("443","ANOUCK");
		instructorMap.put("444","NHM");
		instructorMap.put("445","SAAG");
		instructorMap.put("446","KABAMBA");
		instructorMap.put("447","SAAG");
		instructorMap.put("448","WOOSEOK");
		instructorMap.put("449","WOOSEOK");
		instructorMap.put("450","WOOSEOK");
		instructorMap.put("451","DRACOC");
		instructorMap.put("452","DRACOC");
		instructorMap.put("453","DRACOC");
		instructorMap.put("454","POWELL");
		instructorMap.put("455","CESNIK");
		instructorMap.put("456","EMATKINS");
		instructorMap.put("457","JASHAW");
		instructorMap.put("458","DCW");
		instructorMap.put("459","BRAM");
		instructorMap.put("460","PHILROE");
		instructorMap.put("461","IAINBOYD");
		instructorMap.put("462","JAMESFD");
		instructorMap.put("463","JAMESFD");
		instructorMap.put("464","SCHEERES");
		instructorMap.put("465","SCHEERES");
		instructorMap.put("466","PERETZF");
		instructorMap.put("467","DSBAERO");
		instructorMap.put("468","PERETZF");
		instructorMap.put("469","WDAHM");
		instructorMap.put("470","NHM");
		instructorMap.put("471","MSWOOL");
		instructorMap.put("472","MSWOOL");
		instructorMap.put("473","MSWOOL");
		instructorMap.put("474","RIDLEY");
		instructorMap.put("475","RIDLEY");
		instructorMap.put("476","RIDLEY");
		instructorMap.put("477","JKEELER");
		instructorMap.put("478","JKEELER");
		instructorMap.put("479","JKEELER");
		instructorMap.put("480","JKEELER");
		instructorMap.put("482","TAMAS");
		instructorMap.put("483","SGREGER");
		instructorMap.put("484","ALSTEINE");
		instructorMap.put("485","ALSTEINE");
		instructorMap.put("486","RUFF");
		instructorMap.put("487","RUFF");
		instructorMap.put("488","XIANGLEI");
		instructorMap.put("490","TAMAS");
		instructorMap.put("491","RBROOD");
		instructorMap.put("492","PENNER");
		instructorMap.put("493","PENNER");
		instructorMap.put("494","NRENNO");
		instructorMap.put("495","MARSIK");
		instructorMap.put("496","LIEMOHN");
		instructorMap.put("497","CJABLONO");
		instructorMap.put("498","CJABLONO");
		instructorMap.put("499","TAMAS");
		instructorMap.put("500","TAMAS");
		instructorMap.put("501","MCARROLL");
		instructorMap.put("502","MCARROLL");
		instructorMap.put("503","ANAGY");
		instructorMap.put("504","ANAGY");
		instructorMap.put("505","RPDRAKE");
		instructorMap.put("506","LAFISK");
		instructorMap.put("507","LAFISK");
		instructorMap.put("508","THOMASZ");
		instructorMap.put("509","THOMASZ");
		instructorMap.put("510","THOMASZ");
		instructorMap.put("511","NATAND");
		instructorMap.put("512","JPBOYD");
		instructorMap.put("513","NRENNO");
		instructorMap.put("514","CHIPM");
		instructorMap.put("515","CAIN");
		instructorMap.put("516","MIMAYER");
		instructorMap.put("517","JOEBULL");
		instructorMap.put("518","JINSANG");
		instructorMap.put("519","JINSANG");
		instructorMap.put("520","JINSANG");
		instructorMap.put("521","GROTBERG");
		instructorMap.put("522","SCOTTHO");
		instructorMap.put("523","SCOTTHO");
		instructorMap.put("524","KANGKIM");
		instructorMap.put("525","KANGKIM");
		instructorMap.put("526","KANGKIM");
		instructorMap.put("527","KANGKIM");
		instructorMap.put("528","KANGKIM");
		instructorMap.put("529","KANGKIM");
		instructorMap.put("530","KANGKIM");
		instructorMap.put("531","KANGKIM");
		instructorMap.put("532","DNOLL");
		instructorMap.put("533","DNOLL");
		instructorMap.put("534","FESSLER");
		instructorMap.put("535","FESSLER");
		instructorMap.put("536","AILEENHS");
		instructorMap.put("537","MYCEK");
		instructorMap.put("538","AJHUNT");
		instructorMap.put("539","TAKAYAMA");
		instructorMap.put("540","AILEENHS");
		instructorMap.put("541","SIENKO");
		instructorMap.put("542","MELSAYED");
		instructorMap.put("543","WYNARSKY");
		instructorMap.put("544","ESGIRSCH");
		instructorMap.put("545","WYNARSKY");
		instructorMap.put("546","WYNARSKY");
		instructorMap.put("547","ESGIRSCH");
		instructorMap.put("548","ESGIRSCH");
		instructorMap.put("549","DAIDA");
		instructorMap.put("550","ESHILD");
		instructorMap.put("551","ESHILD");
		instructorMap.put("552","ESHILD");
		instructorMap.put("553","KALFANO");
		instructorMap.put("554","KALFANO");
		instructorMap.put("555","DKLEINKE");
		instructorMap.put("556","PAULKO");
		instructorMap.put("557","ROOTSR");
		instructorMap.put("558","PBKHAN");
		instructorMap.put("559","SJWRIGHT");
		instructorMap.put("560","ROOTSR");
		instructorMap.put("561","PBKHAN");
		instructorMap.put("562","PBKHAN");
		instructorMap.put("563","SINGH");
		instructorMap.put("564","TEBOWDEN");
		instructorMap.put("565","TEBOWDEN");
		instructorMap.put("566","TEBOWDEN");
		instructorMap.put("567","TEBOWDEN");
		instructorMap.put("568","SHOPE");
		instructorMap.put("569","MIMIADAM");
		instructorMap.put("570","MIMIADAM");
		instructorMap.put("571","ROBSUL");
		instructorMap.put("572","MIMIADAM");
		instructorMap.put("573","ROBSUL");
		instructorMap.put("574","MIMIADAM");
		instructorMap.put("575","ROBSUL");
		instructorMap.put("576","MIMIADAM");
		instructorMap.put("577","ROBSUL");
		instructorMap.put("578","LTT");
		instructorMap.put("579","KALFANO");
		instructorMap.put("580","PAULKO");
		instructorMap.put("581","PAULKO");
		instructorMap.put("582","KALFANO");
		instructorMap.put("583","KALFANO");
		instructorMap.put("584","PETE");
		instructorMap.put("585","LOLSEN");
		instructorMap.put("586","ROOTSR");
		instructorMap.put("587","LOLSEN");
		instructorMap.put("588","ROOTSR");
		instructorMap.put("589","LOLSEN");
		instructorMap.put("590","ROOTSR");
		instructorMap.put("591","LOLSEN");
		instructorMap.put("592","ROOTSR");
		instructorMap.put("593","LOLSEN");
		instructorMap.put("594","BREHOB");
		instructorMap.put("595","RCJ");
		instructorMap.put("596","HUGOSHI");
		instructorMap.put("597","RCJ");
		instructorMap.put("598","HUGOSHI");
		instructorMap.put("599","RCJ");
		instructorMap.put("600","NGALLAHE");
		instructorMap.put("601","HUGOSHI");
		instructorMap.put("602","PETE");
		instructorMap.put("603","LOLSEN");
		instructorMap.put("604","PETE");
		instructorMap.put("605","LOLSEN");
		instructorMap.put("606","PETE");
		instructorMap.put("607","LOLSEN");
		instructorMap.put("608","PETE");
		instructorMap.put("609","LOLSEN");
		instructorMap.put("610","DAIDA");
		instructorMap.put("611","YVOROBEY");
		instructorMap.put("612","YVOROBEY");
		instructorMap.put("613","TAKACN");
		instructorMap.put("614","TAKACN");
		instructorMap.put("615","YVOROBEY");
		instructorMap.put("616","TAKACN");
		instructorMap.put("617","BIELAJEW");
		instructorMap.put("618","ACHU");
		instructorMap.put("619","RMARINIE");
		instructorMap.put("620","RMARINIE");
		instructorMap.put("621","ACHU");
		instructorMap.put("622","RMARINIE");
		instructorMap.put("623","ACHU");
		instructorMap.put("624","MLAPP");
		instructorMap.put("625","MLAPP");
		instructorMap.put("626","MLAPP");
		instructorMap.put("627","BIELAJEW");
		instructorMap.put("628","CMORRISZ");
		instructorMap.put("629","CCLUM");
		instructorMap.put("630","CMORRISZ");
		instructorMap.put("631","CCLUM");
		instructorMap.put("632","CCLUM");
		instructorMap.put("633","CMORRISZ");
		instructorMap.put("634","JRINGENB");
		instructorMap.put("635","CHADJO");
		instructorMap.put("636","LUCASMIC");
		instructorMap.put("637","LUCASMIC");
		instructorMap.put("638","CHADJO");
		instructorMap.put("639","LUCASMIC");
		instructorMap.put("640","CHADJO");
		instructorMap.put("641","HODGESM");
		instructorMap.put("642","DGKARR");
		instructorMap.put("643","JOANNAMM");
		instructorMap.put("644","SGREGER");
		instructorMap.put("645","ADRIAENS");
		instructorMap.put("646","FALEY");
		instructorMap.put("647","ADRIAENS");
		instructorMap.put("648","JOANNAMM");
		instructorMap.put("649","WYNARSKY");
		instructorMap.put("650","WYNARSKY");
		instructorMap.put("651","CTNELSON");
		instructorMap.put("652","AMRITHA");
		instructorMap.put("653","CTNELSON");
		instructorMap.put("654","KIMMIN");
		instructorMap.put("655","SMY");
		instructorMap.put("656","FEF");
		instructorMap.put("657","SMY");
		instructorMap.put("658","RSGOLD");
		instructorMap.put("659","PETERJON");
		instructorMap.put("660","JONNYMAD");
		instructorMap.put("661","BAECJ");
		instructorMap.put("662","OBOLTON");
		instructorMap.put("663","KTHORN");
		instructorMap.put("664","MILTY");
		instructorMap.put("665","MILTY");
		instructorMap.put("666","MILTY");
		instructorMap.put("667","FEF");
		instructorMap.put("668","FEF");
		instructorMap.put("669","FEF");
		instructorMap.put("670","FEF");
		instructorMap.put("671","FEF");
		instructorMap.put("672","FEF");
		instructorMap.put("673","FEF");
		instructorMap.put("674","FEF");
		instructorMap.put("675","AKG");
		instructorMap.put("676","MSHTEIN");
		instructorMap.put("677","MSHTEIN");
		instructorMap.put("678","RER");
		instructorMap.put("679","RER");
		instructorMap.put("680","RER");
		instructorMap.put("681","AVDV");
		instructorMap.put("682","TALSDAD");
		instructorMap.put("683","TALSDAD");
		instructorMap.put("684","MFALK");
		instructorMap.put("685","PANX");
		instructorMap.put("686","JONESJWA");
		instructorMap.put("687","JONESJWA");
		instructorMap.put("688","MFALK");
		instructorMap.put("689","KIEFFER");
		instructorMap.put("690","ROOTSR");
		instructorMap.put("691","FWARD");
		instructorMap.put("692","JACKFISH");
		instructorMap.put("693","PJN");
		instructorMap.put("694","ESHILD");
		instructorMap.put("695","FWARD");
		instructorMap.put("696","WALZAH");
		instructorMap.put("697","MLIND");
		instructorMap.put("698","MLIND");
		instructorMap.put("699","MLIND");
		instructorMap.put("700","FWARD");
		instructorMap.put("701","PJN");
		instructorMap.put("702","FWARD");
		instructorMap.put("703","RCJ");
		instructorMap.put("704","LOLSEN");
		instructorMap.put("705","LOLSEN");
		instructorMap.put("726","PYP");
		instructorMap.put("727","DMALEN");
		instructorMap.put("728","KOTA");
		instructorMap.put("729","RBECK");
		instructorMap.put("730","NICKVL");
		instructorMap.put("731","EUSTICE");
		instructorMap.put("732","JEFFCOOK");
		instructorMap.put("733","KJMAKI");
		instructorMap.put("734","DGKARR");
		instructorMap.put("735","DGKARR");
		instructorMap.put("736","PERLIN");
		instructorMap.put("737","PERLIN");
		instructorMap.put("738","PERLIN");
		instructorMap.put("739","NICKVL");
		instructorMap.put("740","NICKVL");
		instructorMap.put("741","PERLIN");
		instructorMap.put("742","MICHAELB");
		instructorMap.put("743","JINGSUN");
		instructorMap.put("744","TROESCH");
		instructorMap.put("745","DJSINGER");
		instructorMap.put("746","DJSINGER");
		instructorMap.put("747","TASSOS");
		instructorMap.put("748","TASSOS");
		instructorMap.put("749","SHOPE");
		instructorMap.put("750","KOTOV");
		instructorMap.put("751","MORAVEJI");
		instructorMap.put("752","LEESEUNG");
		instructorMap.put("753","ZEPPELIN");
		instructorMap.put("754","LARDAN");
		instructorMap.put("755","MORAVEJI");
		instructorMap.put("756","LEESEUNG");
		instructorMap.put("757","ZEPPELIN");
		instructorMap.put("758","JBARBER");
		instructorMap.put("759","CLAUS");
		instructorMap.put("760","VSICK");
		instructorMap.put("761","CAR");
		instructorMap.put("762","STEIN");
		instructorMap.put("763","SAAG");
		instructorMap.put("764","DUTTA");
		instructorMap.put("765","YEOIL");
		instructorMap.put("766","YEOIL");
		instructorMap.put("767","MEHTACR");
		instructorMap.put("768","MEHTACR");
		instructorMap.put("769","VINODA");
		instructorMap.put("770","VINODA");
		instructorMap.put("771","HULBERT");
		instructorMap.put("772","JBARBER");
		instructorMap.put("773","RAA");
		instructorMap.put("774","KATSUO");
		instructorMap.put("775","KAVIANY");
		instructorMap.put("776","KAVIANY");
		instructorMap.put("777","CLAUS");
		instructorMap.put("778","SHIHA");
		instructorMap.put("779","SHIHA");
		instructorMap.put("780","DANIJOHN");
		instructorMap.put("781","SHIHA");
		instructorMap.put("782","CHAZZ");
		instructorMap.put("783","ARTKUO");
		instructorMap.put("784","OLDHAM");
		instructorMap.put("785","WEILU");
		instructorMap.put("786","THOULESS");
		instructorMap.put("787","LOLSEN");
		instructorMap.put("788","PRAMODR");
		instructorMap.put("789","GROSH");
		instructorMap.put("790","PTEINI");
		instructorMap.put("791","JENSMN");
		instructorMap.put("792","PTEINI");
		instructorMap.put("793","JENSMN");
		instructorMap.put("794","JENSMN");
		instructorMap.put("795","PTEINI");
		instructorMap.put("796","CAR");
		instructorMap.put("797","MEYHOFER");
		instructorMap.put("798","DEPCIKC");
		instructorMap.put("799","ASSANIS");
		instructorMap.put("800","NCP");
		instructorMap.put("801","KAZU");
		instructorMap.put("802","YKOREN");
		instructorMap.put("803","KAZU");
		instructorMap.put("804","EPUREANU");
		instructorMap.put("805","JWO");
		instructorMap.put("806","KATSUO");
		instructorMap.put("807","DMALEN");
		instructorMap.put("808","DMALEN");
		instructorMap.put("809","KOTA");
		instructorMap.put("810","KOTA");
		instructorMap.put("811","DMALEN");
		instructorMap.put("812","DMALEN");
		instructorMap.put("813","KOTA");
		instructorMap.put("814","KOTA");
		instructorMap.put("815","PYP");
		instructorMap.put("816","MOUSSEAU");
		instructorMap.put("817","TILBURY");
		instructorMap.put("818","TJGORDON");
		instructorMap.put("819","MAZUMDER");
		instructorMap.put("820","PIPE");
		instructorMap.put("821","AMSASTRY");
		instructorMap.put("822","DONGKYUN");
		instructorMap.put("823","YIFENG");
		instructorMap.put("824","DONGKYUN");
		instructorMap.put("825","SERGE");
		instructorMap.put("826","YIFENG");
		instructorMap.put("827","SERGE");
		instructorMap.put("828","DONGKYUN");
		instructorMap.put("829","YIFENG");
		instructorMap.put("830","SERGE");
		instructorMap.put("831","MSWOOL");
		instructorMap.put("832","VSICK");
		instructorMap.put("833","CAR");
		instructorMap.put("836","HULBERT");
		instructorMap.put("837","KRISHNA");
		instructorMap.put("838","KIKUCHI");
		instructorMap.put("839","KIKUCHI");
		instructorMap.put("840","KIKUCHI");
		instructorMap.put("841","DMALEN");
		instructorMap.put("842","DMALEN");
		instructorMap.put("843","DMALEN");
		instructorMap.put("844","DRD");
		instructorMap.put("845","HGIM");
		instructorMap.put("846","HGIM");
		instructorMap.put("847","CECCIO");
		instructorMap.put("848","AWTAR");
		instructorMap.put("849","AWTAR");
		instructorMap.put("850","CHRONIS");
		instructorMap.put("851","CHRONIS");
		instructorMap.put("852","DIBREI");
		instructorMap.put("853","DIBREI");
		instructorMap.put("854","ULSOY");
		instructorMap.put("855","ULSOY");
		instructorMap.put("856","ANNASTEF");
		instructorMap.put("857","JWO");
		instructorMap.put("858","JWO");
		instructorMap.put("859","KAZU");
		instructorMap.put("860","KAZU");
		instructorMap.put("861","DUTTA");
		instructorMap.put("862","DUTTA");
		instructorMap.put("863","ASIBU");
		instructorMap.put("864","ASIBU");
		instructorMap.put("865","YKOREN");
		instructorMap.put("866","YKOREN");
		instructorMap.put("867","YKOREN");
		instructorMap.put("868","YKOREN");
		instructorMap.put("869","AVIOLI");
		instructorMap.put("870","SIENKO");
		instructorMap.put("871","AJOHNH");
		instructorMap.put("872","AMSASTRY");
		instructorMap.put("873","AMSASTRY");
		instructorMap.put("874","PIPE");
		instructorMap.put("875","VIKRAMG");
		instructorMap.put("876","MHASKINS");
		instructorMap.put("877","LYNNW");
		instructorMap.put("878","MHASKINS");
		instructorMap.put("879","PAFORD");
	}
	
	/*
	 * Note that as of Spring 2.0 and Quartz 1.5, the preferred way to apply dependency injection to Job instances 
	 * is via a JobFactory:  that is, to specify SpringBeanJobFactory as Quartz JobFactory (typically via 
	 * SchedulerFactoryBean.setJobFactory(org.quartz.spi.JobFactory) SchedulerFactoryBean's "jobFactory" property}). 
	 * This allows to implement dependency-injected Quartz Jobs without a dependency on Spring base classes.
	 */
}

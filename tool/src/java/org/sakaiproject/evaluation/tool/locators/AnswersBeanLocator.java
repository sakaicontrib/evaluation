/**
 * AnswersBeanLocator.java - evaluation - Feb 02, 2007 11:35:56 AM - whumphri
 * $URL: https://source.sakaiproject.org/contrib $
 * $Id: Locator.java 11234 Jan 21, 2008 11:35:56 AM azeckoski $
 **************************************************************************
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.tool.LocalResponsesLogic;

import uk.org.ponder.beanutil.BeanLocator;

/**
 * This is the OTP bean used to locate Answers. The OTP path key is 
 * actually the ID of the enclosed ITEM, not that of the Answer itself!
 * 
 * @author Will Humphries (whumphri@vt.edu)
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class AnswersBeanLocator implements BeanLocator {
   public static final String NEW_PREFIX = "new";
   public static String NEW_1 = NEW_PREFIX +"1";

   private Map<String, EvalAnswer> delivered = new HashMap<String, EvalAnswer>();

   private EvalResponse parent;

   private LocalResponsesLogic localResponsesLogic;

   public AnswersBeanLocator(EvalResponse parent, LocalResponsesLogic localResponsesLogic) {
      this.parent = parent;
      this.localResponsesLogic = localResponsesLogic;
      loadMap(parent.getAnswers());
   }

   public Object locateBean(String path) {
      EvalAnswer togo = delivered.get(path);
      // no answer has been created for this item-response pairing, so we'll make
      // one. we don't use the new prefix, because the producer has no way of knowing
      // if an answer has been created for the given item, even if a response exists.
      if (togo == null) {
         if(path.startsWith(NEW_PREFIX)) togo = localResponsesLogic.newAnswer(parent);
         parent.getAnswers().add(togo);
         delivered.put(path, togo);
      }
      return togo;
   }

   /**
    * loads a {@link Map} with the answers provided. The key used to access an answer
    * will be of the form <responseNum>.<answerId>.<field>
    * 
    * @param answers - Set of answers
    */
   public void loadMap(Set<EvalAnswer> answers) {
      for (Iterator<EvalAnswer> it = answers.iterator(); it.hasNext();) {
         EvalAnswer answer = it.next();
         delivered.put(answer.getId().toString(), answer);
      }
   }

}
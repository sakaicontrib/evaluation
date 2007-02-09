/******************************************************************************
 * AnswersBeanLocator.java - created by whumphri@vt.edu on Feb 02, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Will Humphries (whumphri@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalResponse;

import uk.org.ponder.beanutil.BeanLocator;

/**
 * This is the OTP bean used to locate Answers. The OTP path key is 
 * actually the ID of the enclosed ITEM, not that of the Answer itself!
 * 
 * @author
 */

public class AnswersBeanLocator implements BeanLocator {
  public static final String NEW_PREFIX = "new";

  private Map delivered = new HashMap();

  private EvalResponse parent;

  private LocalResponsesLogic localResponsesLogic;

  public AnswersBeanLocator(EvalResponse parent,
      LocalResponsesLogic localResponsesLogic) {
    this.parent = parent;
    this.localResponsesLogic = localResponsesLogic;
    loadMap(parent.getAnswers());
  }

  public Object locateBean(String path) {
    Object togo = delivered.get(path);
    // no answer has been created for this item-response pairing, so we'll make
    // one. we don't use the new prefix, because the producer has no way of knowing
    // if an answer has been created for the given item, even if a response exists.
    if (togo == null) {
      Long itemId = Long.valueOf(path);
      togo = localResponsesLogic.newAnswer(parent, itemId);
      parent.getAnswers().add(togo);
      delivered.put(path, togo);
    }
    return togo;
  }

  /**
   * loads a HashMap with the answers provided. The key used to access an answer
   * will be of the form <responseNum>.<itemId>.<field>
   * 
   * @param answers - HashSet of answers
   */
  public void loadMap(Set answers) {
    for (Iterator it = answers.iterator(); it.hasNext();) {
      EvalAnswer answer = (EvalAnswer) it.next();
      delivered.put(answer.getItem().getId().toString(), answer);
    }
  }

}
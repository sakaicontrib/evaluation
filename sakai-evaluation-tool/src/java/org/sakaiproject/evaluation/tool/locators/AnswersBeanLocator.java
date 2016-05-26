/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.tool.LocalResponsesLogic;
import org.sakaiproject.evaluation.utils.EvalUtils;

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

    private Map<String, EvalAnswer> delivered = new HashMap<>();

    private EvalResponse response;

    private LocalResponsesLogic localResponsesLogic;

    public AnswersBeanLocator(EvalResponse response, LocalResponsesLogic localResponsesLogic) {
        if (response == null) {
            throw new IllegalArgumentException("response cannot be null to create an AnswersBeanLocator");
        }
        this.response = response;
        this.localResponsesLogic = localResponsesLogic;
        loadMap(response.getAnswers());
    }

    public Object locateBean(String path) {
        EvalAnswer togo = delivered.get(path);
        // no answer has been created for this item-response pairing, so we'll make
        // one. we don't use the new prefix, because the producer has no way of knowing
        // if an answer has been created for the given item, even if a response exists.
        if (togo == null) {
            if (path.startsWith(NEW_PREFIX)) {
                togo = localResponsesLogic.newAnswer(response);
            }
            response.getAnswers().add(togo);
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
        if (answers == null) {
            throw new IllegalStateException("answers set in the response ("+response+") is null, this should never happen");
        }
        for (EvalAnswer answer : answers) {
            // decode the various parts of this answer
            EvalUtils.decodeAnswerNA(answer);
            answer.multipleAnswers = EvalUtils.decodeMultipleAnswers(answer.getMultiAnswerCode());
            // put the answer into the map
            delivered.put(answer.getId().toString(), answer);
        }
    }

}
/**
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.evaluation.logic.scheduling;

import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalLockManager;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;

import lombok.extern.slf4j.Slf4j;

/**
 * Job to delete evaluations for a term
 *
 */
@Slf4j
public class DeleteEvaluationsImpl implements DeleteEvaluations {

	
    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalEvaluationSetupService evaluationSetupService;
    public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
        this.evaluationSetupService = evaluationSetupService;
    }
    
    private EvalExternalLogic externalLogic;
    public void setExternalLogic(EvalExternalLogic externalLogic) {
        this.externalLogic = externalLogic;
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings settings) {
        this.evalSettings = settings;
    }
	protected EvalLockManager lockManager;
	public void setEvalLockManager(EvalLockManager lockManager) {
		this.lockManager = lockManager;
	}

    /*
     * (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.debug("DeleteEvaluations.execute()");
        String termId = context.getMergedJobDataMap().getString("term.id");
        List<EvalEvaluation> evaluations = evaluationService.getEvaluationsByTermId(termId);
        log.info("Found "+ evaluations.size() + " evaluations to delete matching " + termId);
        for (EvalEvaluation evaluation: evaluations) {
        	//Set admin as the id, I don't think there's any way to get this from the job scheduler
        	log.info("Deleting evaluation id " + evaluation.getId());
        	evaluationSetupService.deleteEvaluation(evaluation.getId(), "admin");
        }
	}
	
	public void init() {
		log.debug("init()");
	}

}

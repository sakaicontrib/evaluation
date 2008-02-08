package org.sakaiproject.evaluation.tool.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/* This utility class contains methods for discovering information about an 
 * EvalEvaluation object such as total enrollments, and some helpful methods
 * for creating moderately complex strings representing this information.
 * 
 * This was born out of a need to do these routines in multiple places, and was
 * being copy and pasted.
 * 
 * This class requires injection, so it should be injected via Spring as well.
 */
public class EvaluationCalcUtility {
   private EvalExternalLogic externalLogic;
   private EvalEvaluationService evaluationService;
   private EvalDeliveryService deliveryService;
   
   /**
    * Gets the total count of enrollments for an evaluation
    * 
    * @param evaluationId
    * @return total number of users with take eval perms in this evaluation
    */
   public int getTotalEnrollmentsForEval(Long evaluationId) {
      int totalEnrollments = 0;
      Map<Long, List<EvalAssignGroup>> evalAssignGroups = evaluationService.getEvaluationAssignGroups(new Long[] {evaluationId}, true);
      List<EvalAssignGroup> groups = evalAssignGroups.get(evaluationId);
      for (int i=0; i<groups.size(); i++) {
         EvalAssignGroup eac = (EvalAssignGroup) groups.get(i);
         String context = eac.getEvalGroupId();
         Set<String> userIds = externalLogic.getUserIdsForEvalGroup(context, EvalConstants.PERM_TAKE_EVALUATION);
         totalEnrollments = totalEnrollments + userIds.size();
      }
      return totalEnrollments;
   }
   

   /*
    * Get the Evaluation Response Rate as a human readable string. This is typically
    * used for getting the response rate of a <em>closed</em> evaluation. This includes
    * the percentage. The string will typically look something like 11% ( 3 / 98 )
    * 
    * Originally from ConrolEvaluationsProducer
    * 
    * @param evaluation The EvalEvaluation object to fetch the response rate from.
    * @return Human readable string with participant response rate.
    */
   public String getParticipantResults(EvalEvaluation evaluation) {
      int countResponses = deliveryService.countResponses(evaluation.getId(), null, true);
      int countEnrollments = getTotalEnrollmentsForEval(evaluation.getId());
      long percentage = 0;
      if (countEnrollments > 0) {
         percentage = Math.round(  (((float)countResponses) / (float)countEnrollments) * 100.0 );
         return percentage + "%  ( " + countResponses + " / " + countEnrollments + " )";
      } else {
         return countResponses + "";
      }
   }

   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }
   
   public void setDeliveryService(EvalDeliveryService deliveryService) {
      this.deliveryService = deliveryService;
   }
}

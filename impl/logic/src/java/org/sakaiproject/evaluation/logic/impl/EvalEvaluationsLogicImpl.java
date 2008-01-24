/******************************************************************************
 * EvalEvaluationsLogicImpl.java - created by aaronz@vt.edu on Dec 26, 2006
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.logic.impl.interceptors.EvaluationModificationRegistry;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.utils.EvalUtils;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * Implementation for EvalEvaluationsLogic
 * (Note for developers - do not modify this without permission from the author)<br/>
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalEvaluationsLogicImpl implements EvalEvaluationsLogic {

   private static Log log = LogFactory.getLog(EvalEvaluationsLogicImpl.class);

   private final String EVENT_EVAL_CREATE = "eval.evaluation.created";
   private final String EVENT_EVAL_UPDATE = "eval.evaluation.updated";
   private final String EVENT_EVAL_STATE_CHANGE = "eval.evaluation.state.change";
   private final String EVENT_EVAL_DELETE = "eval.evaluation.deleted";

   private EvaluationDao dao;
   public void setDao(EvaluationDao dao) {
      this.dao = dao;
   }

   private EvalExternalLogic external;
   public void setExternalLogic(EvalExternalLogic external) {
      this.external = external;
   }

   private EvalJobLogic evalJobLogic;
   public void setEvalJobLogic(EvalJobLogic evalJobLogic) {
      this.evalJobLogic = evalJobLogic;
   }

   private EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }


   // INIT method
   public void init() {
      log.debug("Init");
   }


   /**
    * Non-API method used from interceptor<br/>
    * Semantics - throws exception on forbidden operation<br/>
    * Do NOT use this method outside the logic layer or even outside the interceptor!
    * 
    * @param evaluation an evaluation persistent object
    * @param method a method being called on the evaluation persistent object
    */
   public void modifyEvaluation(EvalEvaluation evaluation, String method) {
      // we only care to do these checks when object is modified (set is called)
      if (method.startsWith("set")) {
         String state = EvalUtils.getEvaluationState(evaluation);        
         // strip off the "set" part and fix case at beginning to get the name of the property we are setting
         String property = Character.toLowerCase(method.charAt(3)) + method.substring(4);
         // check if this property can be changed while evaluation is in this state
         if (!EvaluationModificationRegistry.isPermittedModification(state, property)) {
            throw new IllegalArgumentException("Cannot change state of evaluation with " +
                  method + " when it is in state " + state);
         }

         // check the user control permissions
         // This check is more expensive than the others so we do those first -AZ
         String userId = external.getCurrentUserId();
         if (! canUserControlEvaluation(userId, evaluation) ) {
            throw new SecurityException("User ("+userId+") attempted to update existing evaluation ("+evaluation.getId()+") without permissions");
         }
      }
   }

   // EVALUATIONS

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#saveEvaluation(edu.vt.sakai.evaluation.model.EvalEvaluation, java.lang.String)
    */
   public void saveEvaluation(EvalEvaluation evaluation, String userId) {
      log.debug("evalId: " + evaluation.getId() + ",userId: " + userId);

      boolean newEvaluation = false;

      // set the date modified
      evaluation.setLastModified( new Date() );

      // test date ordering first
      if (evaluation.getStartDate().compareTo(evaluation.getDueDate()) >= 0 ) {
         throw new IllegalArgumentException(
               "due date (" + evaluation.getDueDate() +
               ") must occur after start date (" + 
               evaluation.getStartDate() + "), can occur on the same date but not at the same time");
      } else if (evaluation.getDueDate().compareTo(evaluation.getStopDate()) > 0 ) {
         throw new IllegalArgumentException(
               "stop date (" + evaluation.getStopDate() +
               ") must occur on or after due date (" + 
               evaluation.getDueDate() + "), can be identical");
      } else if (evaluation.getViewDate().compareTo(evaluation.getStopDate()) <= 0 ) {
         throw new IllegalArgumentException(
               "view date (" + evaluation.getViewDate() +
               ") must occur after stop date (" + 
               evaluation.getStopDate() + "), can occur on the same date but not at the same time");
      }

      // now perform checks depending on whether this is new or existing
      Calendar calendar = GregorianCalendar.getInstance();
      calendar.add(Calendar.MINUTE, -15); // put today a bit in the past (15 minutes)
      Date today = calendar.getTime();
      if (evaluation.getId() == null) { // creating new evaluation

         newEvaluation = true;

         // test if new evaluation occurs in the past
         if (evaluation.getStartDate().before(today)) {
            throw new IllegalArgumentException(
                  "start date (" + evaluation.getStartDate() +
            ") cannot occur in the past for new evaluations");
         } else if (evaluation.getDueDate().before(today)) {
            throw new IllegalArgumentException(
                  "due date (" + evaluation.getDueDate() +
            ") cannot occur in the past for new evaluations");
         } else if (evaluation.getStopDate().before(today)) {
            throw new IllegalArgumentException(
                  "stop date (" + evaluation.getStopDate() +
            ") cannot occur in the past for new evaluations");
         }

         // make sure the state is set correctly
         fixReturnEvalState(evaluation, false);

         // check user permissions (uses public method)
         if (! canBeginEvaluation(userId) ) {
            throw new SecurityException("User ("+userId+") attempted to create evaluation without permissions");
         }

      } else { // updating existing evaluation

         // make sure the state is set correctly
         fixReturnEvalState(evaluation, false);

         if (! canUserControlEvaluation(userId, evaluation) ) {
            throw new SecurityException("User ("+userId+") attempted to update existing evaluation ("+evaluation.getId()+") without permissions");
         }

         // All other checks have been moved to interceptor
      }

      // make sure we are not using a blank template here
      if (evaluation.getTemplate() == null ||
            evaluation.getTemplate().getTemplateItems() == null ||
            evaluation.getTemplate().getTemplateItems().size() <= 0) {
         throw new IllegalArgumentException("Evaluations must include a template and the template must have at least one item in it");
      } else if (! EvalConstants.TEMPLATE_TYPE_STANDARD.equals(evaluation.getTemplate().getType())) {
         throw new IllegalArgumentException("Evaluations cannot use templates of type: " + evaluation.getTemplate().getType() + 
         " as the primary template");			
      }

      // check the added template for type
      if (evaluation.getAddedTemplate() != null &&
            ! EvalConstants.TEMPLATE_TYPE_ADDED.equals(evaluation.getTemplate().getType()) ) {
         throw new IllegalArgumentException("Evaluations cannot use templates of type: " + evaluation.getTemplate().getType() + 
               " as the added template, must be " + EvalConstants.TEMPLATE_TYPE_ADDED);			
      }

      // fill in any default values and nulls here
      if (evaluation.getLocked() == null) {
         evaluation.setLocked( Boolean.FALSE );
      }
      if (evaluation.getResultsPrivate() == null) {
         evaluation.setResultsPrivate( Boolean.FALSE );
      }
      if (evaluation.getAuthControl() == null) {
         evaluation.setAuthControl( EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ );
      }

      if (evaluation.getEvalCategory() != null && evaluation.getEvalCategory().length() <= 0) {
         evaluation.setEvalCategory(null);
      }

      // system setting checks for things like allowing users to modify responses
      Boolean systemModifyResponses = (Boolean) settings.get( EvalSettings.STUDENT_MODIFY_RESPONSES );
      if ( systemModifyResponses == null ) {
         if ( evaluation.getModifyResponsesAllowed() == null ) {
            evaluation.setModifyResponsesAllowed( Boolean.FALSE );
         }
      } else {
         evaluation.setBlankResponsesAllowed( systemModifyResponses );
      }
      Boolean systemBlankResponses = (Boolean) settings.get( EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED );
      if ( systemBlankResponses == null ) {
         if (evaluation.getBlankResponsesAllowed() == null) {
            evaluation.setBlankResponsesAllowed( Boolean.FALSE );
         }
      } else {
         evaluation.setBlankResponsesAllowed( systemBlankResponses );
      }
      String systemInstructorOpt = (String) settings.get( EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE );
      if ( systemInstructorOpt == null ) {
         if (evaluation.getInstructorOpt() == null) {
            evaluation.setInstructorOpt( EvalConstants.INSTRUCTOR_OPT_OUT );
         }
      } else {
         evaluation.setInstructorOpt( systemInstructorOpt );
      }

      dao.save(evaluation);
      log.info("User ("+userId+") saved evaluation ("+evaluation.getId()+"), title: " + evaluation.getTitle());

      if (newEvaluation) {
         external.registerEntityEvent(EVENT_EVAL_CREATE, evaluation);
         // call logic to manage Quartz scheduled jobs
         evalJobLogic.processNewEvaluation(evaluation);
      } else {
         external.registerEntityEvent(EVENT_EVAL_UPDATE, evaluation);
         // call logic to manage Quartz scheduled jobs
         evalJobLogic.processEvaluationChange(evaluation);
      }

      // effectively we are locking the evaluation when a user replies to it, otherwise the chain can be changed
      if (evaluation.getLocked().booleanValue()) {
         // lock evaluation and associated template
         log.info("Locking evaluation ("+evaluation.getId()+") and associated template ("+evaluation.getTemplate().getId()+")");
         dao.lockEvaluation(evaluation);			
      }

   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#deleteEvaluation(java.lang.Long, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public void deleteEvaluation(Long evaluationId, String userId) {
      log.debug("evalId: " + evaluationId + ",userId: " + userId);
      EvalEvaluation evaluation = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
      if (evaluation == null) {
         log.warn("Cannot find evaluation to delete with id: " + evaluationId);
         return;
      }

      if ( canUserRemoveEval(userId, evaluation) ) {

         //remove all scheduled job invocations
         evalJobLogic.removeScheduledInvocations(evaluationId);

         Set[] entitySets = new HashSet[4];
         // remove associated AssignGroups
         List<EvalAssignGroup> acs = dao.findByProperties(EvalAssignGroup.class, 
               new String[] {"evaluation.id"}, 
               new Object[] {evaluationId});
         Set<EvalAssignGroup> assignGroupSet = new HashSet<EvalAssignGroup>(acs);
         entitySets[0] = assignGroupSet;

         // remove associated assigned hierarchy nodes
         List<EvalAssignHierarchy> ahs = dao.findByProperties(EvalAssignHierarchy.class, 
               new String[] {"evaluation.id"}, 
               new Object[] {evaluationId});
         Set<EvalAssignHierarchy> assignHierSet = new HashSet<EvalAssignHierarchy>(ahs);
         entitySets[1] = assignHierSet;

         // remove associated unused email templates
         Set<EvalEmailTemplate> emailSet = new HashSet<EvalEmailTemplate>();
         entitySets[2] = emailSet;
         if (evaluation.getAvailableEmailTemplate() != null) {
            if (evaluation.getAvailableEmailTemplate().getDefaultType() == null) {
               // only remove non-default templates
               Long emailTemplateId = evaluation.getAvailableEmailTemplate().getId();
               if ( dao.countByProperties(EvalEvaluation.class, 
                     new String[] {"availableEmailTemplate.id"}, 
                     new Object[] {emailTemplateId}) <= 1 ) {
                  // template was only used in this evaluation
                  emailSet.add( evaluation.getAvailableEmailTemplate() );
               }
            }
         }
         if (evaluation.getReminderEmailTemplate() != null) {
            if (evaluation.getReminderEmailTemplate().getDefaultType() == null) {
               Long emailTemplateId = evaluation.getReminderEmailTemplate().getId();
               if ( dao.countByProperties(EvalEvaluation.class, 
                     new String[] {"reminderEmailTemplate.id"}, 
                     new Object[] {emailTemplateId}) <= 1 ) {
                  // template was only used in this evaluation
                  emailSet.add( evaluation.getReminderEmailTemplate() );
               }
            }
         }

         // add eval to a set to be removed
         Set evalSet = new HashSet();
         entitySets[3] = evalSet;
         evalSet.add(evaluation);

         // unlock associated template
         log.info("Unlocking associated template ("+evaluation.getTemplate().getId()+") for eval ("+evaluation.getId()+")");
         dao.lockTemplate(evaluation.getTemplate(), Boolean.FALSE);

         // fire the evaluation deleted event
         external.registerEntityEvent(EVENT_EVAL_DELETE, evaluation);

         // remove the evaluation and related data in one transaction
         dao.deleteMixedSet(entitySets);
         //dao.delete(eval);

         log.info("User ("+userId+") removed evaluation ("+evaluationId+"), title: " + evaluation.getTitle());
         return;
      }

      // should not get here so die if we do
      throw new RuntimeException("User ("+userId+") could NOT delete evaluation ("+evaluationId+")");
   }
   
   /*
    * (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationsLogic#getEvaluationByEid(java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public EvalEvaluation getEvaluationByEid(String eid) {
      List<EvalEvaluation> evalEvaluations = new ArrayList<EvalEvaluation>();
      EvalEvaluation evalEvaluation = null;
      if(eid != null) {
         evalEvaluations = dao.findByProperties(EvalEvaluation.class, new String[] {"eid"}, new Object[] {eid});
         if(!evalEvaluations.isEmpty())
            evalEvaluation = (EvalEvaluation)evalEvaluations.get(0);
      }
      return evalEvaluation;
   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#getEvaluationById(java.lang.Long)
    */
   public EvalEvaluation getEvaluationById(Long evaluationId) {
      log.debug("evalId: " + evaluationId);
//    TODO - Interceptor strategy is hopeless, removing for now -AZ
//    EvalEvaluation togo = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
//    return wrapEvaluationProxy(togo);
      return (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationsLogic#getEvaluationsByTemplateId(java.lang.Long)
    */
   @SuppressWarnings("unchecked")
   public List<EvalEvaluation> getEvaluationsByTemplateId(Long templateId) {
      log.debug("templateId: " + templateId);
      EvalTemplate template = (EvalTemplate) dao.findById(EvalTemplate.class, templateId);
      if (template == null) {
         throw new IllegalArgumentException("Cannot find template with id: " + templateId);
      }
      return dao.findByProperties(EvalEvaluation.class,
            new String[] {"template.id"},  new Object[] {templateId});
   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#countEvaluationsByTemplateId(java.lang.Long)
    */
   public int countEvaluationsByTemplateId(Long templateId) {
      log.debug("templateId: " + templateId);
      EvalTemplate template = (EvalTemplate) dao.findById(EvalTemplate.class, templateId);
      if (template == null) {
         throw new IllegalArgumentException("Cannot find template with id: " + templateId);
      }
      return dao.countByProperties(EvalEvaluation.class,
            new String[] {"template.id"},  new Object[] {templateId});
   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#getVisibleEvaluationsForUser(java.lang.String, boolean, boolean)
    */
   @SuppressWarnings("unchecked")
   public List<EvalEvaluation> getVisibleEvaluationsForUser(String userId, boolean recentOnly, boolean showNotOwned) {
      List<EvalEvaluation> l = new ArrayList<EvalEvaluation>();
      if (recentOnly) {
         // only get recently closed evals 
         // check system setting to get "recent" value
         Integer recentlyClosedDays = (Integer) settings.get(EvalSettings.EVAL_RECENTLY_CLOSED_DAYS);
         if (recentlyClosedDays == null) { recentlyClosedDays = 10; }
         Calendar calendar = GregorianCalendar.getInstance();
         calendar.add(Calendar.DATE, -1 * recentlyClosedDays.intValue());
         Date recent = calendar.getTime();

         if (external.isUserAdmin(userId)) {
            l = dao.findByProperties(EvalEvaluation.class,
                  new String[] {"stopDate"}, 
                  new Object[] {recent}, 
                  new int[] {EvaluationDao.GREATER});
         } else {

            // Get the owned + not-owned evaluations i.e. where the 
            // user has PERM_BE_EVALUATED permissions.
            if (showNotOwned) {
               getEvalsWhereBeEvaluated(userId, recentOnly, l, recent);
            }
            // Get the evaluations owned by the user
            else {
               l = dao.findByProperties(EvalEvaluation.class,
                     new String[] {"owner", "stopDate"}, 
                     new Object[] {userId, recent}, 
                     new int[] {EvaluationDao.EQUALS, EvaluationDao.GREATER});
            }
         }
      } else {
         // don't worry about when they closed
         if (external.isUserAdmin(userId)) {
            // NOTE: this will probably be too slow -AZ
            l = dao.findAll(EvalEvaluation.class);
         } else {

            // Get the owned + not-owned evaluations i.e. where the 
            // user has PERM_BE_EVALUATED permissions.
            if (showNotOwned) {
               getEvalsWhereBeEvaluated(userId, recentOnly, l, null);
            }
            // get all evaluations created (owned) by this user
            else {
               l = dao.findByProperties(EvalEvaluation.class,
                     new String[] {"owner"}, new Object[] {userId});
            }
         }
      }
      return l;
   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#getEvaluationsForUser(java.lang.String, boolean, boolean)
    */
   @SuppressWarnings("unchecked")
   public List<EvalEvaluation> getEvaluationsForUser(String userId, boolean activeOnly, boolean untakenOnly) {
      List<EvalGroup> takeGroups = external.getEvalGroupsForUser(userId, EvalConstants.PERM_TAKE_EVALUATION);

      String[] evalGroupIds = new String[takeGroups.size()];
      for (int i=0; i<takeGroups.size(); i++) {
         EvalGroup c = (EvalGroup) takeGroups.get(i);
         evalGroupIds[i] = c.evalGroupId;
      }

      // get the evaluations
      Set<EvalEvaluation> s = dao.getEvaluationsByEvalGroups( evalGroupIds, activeOnly, false, false );

      if (untakenOnly) {
         // filter out the evaluations this user already took

         // create an array of the evaluation ids
         Long[] evalIds = new Long[s.size()];
         int j = 0;
         for (Iterator<EvalEvaluation> it = s.iterator(); it.hasNext(); j++) {
            EvalEvaluation eval = it.next();
            evalIds[j] = (Long) eval.getId();
         }

         // now get the responses for all the returned evals
         List<EvalResponse> l = dao.findByProperties(EvalResponse.class, 
               new String[] {"owner", "evaluation.id"}, 
               new Object[] {userId, evalIds});

         // Iterate through and remove the evals this user already took
         for (int i = 0; i < l.size(); i++) {
            EvalResponse er = (EvalResponse) l.get(i);
            s.remove( er.getEvaluation() );
         }
      }

      // stuff the remaining set into a list
      return new ArrayList<EvalEvaluation>(s);
   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#countEvaluationContexts(java.lang.Long)
    */
   public int countEvaluationGroups(Long evaluationId) {
      log.debug("evalId: " + evaluationId);
      return dao.countByProperties(EvalAssignGroup.class, 
            new String[] {"evaluation.id"}, 
            new Object[] {evaluationId});
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationsLogic#getEvaluationAssignGroups(java.lang.Long[], boolean)
    */
   @SuppressWarnings("unchecked")
   public Map<Long, List<EvalAssignGroup>> getEvaluationAssignGroups(Long[] evaluationIds, boolean includeUnApproved) {
      log.debug("evalIds: " + evaluationIds + ", includeUnApproved=" + includeUnApproved);
      Map<Long, List<EvalAssignGroup>> evals = new TreeMap<Long, List<EvalAssignGroup>>();

      // create the inner lists
      for (int i=0; i<evaluationIds.length; i++) {
         List<EvalAssignGroup> innerList = new ArrayList<EvalAssignGroup>();
         evals.put(evaluationIds[i], innerList);
      }

      // get all the evalGroupIds for the given eval ids in one storage call
      List<EvalAssignGroup> l = new ArrayList<EvalAssignGroup>();
      if (includeUnApproved) {
         l = dao.findByProperties(EvalAssignGroup.class,
               new String[] {"evaluation.id"}, 
               new Object[] {evaluationIds} );
      } else {
         // only include those that are approved
         l = dao.findByProperties(EvalAssignGroup.class,
               new String[] {"evaluation.id", "instructorApproval"}, 
               new Object[] {evaluationIds, Boolean.TRUE} );
      }
      for (int i=0; i<l.size(); i++) {
         EvalAssignGroup eac = (EvalAssignGroup) l.get(i);

         // put stuff in inner list
         Long evalId = eac.getEvaluation().getId();
         List<EvalAssignGroup> innerList = evals.get(evalId);
         innerList.add( eac );
      }
      return evals;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationsLogic#getEvaluationGroups(java.lang.Long[], boolean)
    */
   public Map<Long, List<EvalGroup>> getEvaluationGroups(Long[] evaluationIds, boolean includeUnApproved) {
      log.debug("evalIds: " + evaluationIds + ", includeUnApproved=" + includeUnApproved);
      Map<Long, List<EvalGroup>> evals = new TreeMap<Long, List<EvalGroup>>();
      // replace each assign group with an EvalGroup
      Map<Long, List<EvalAssignGroup>> evalAGs = getEvaluationAssignGroups(evaluationIds, includeUnApproved);
      for (Iterator<Long> iter = evalAGs.keySet().iterator(); iter.hasNext();) {
         Long evalId = (Long) iter.next();
         List<EvalAssignGroup> innerList = evalAGs.get(evalId);
         List<EvalGroup> newList = new ArrayList<EvalGroup>();
         for (int i=0; i<innerList.size(); i++) {
            EvalAssignGroup eac = (EvalAssignGroup) innerList.get(i);
            newList.add( external.makeEvalGroupObject( eac.getEvalGroupId() ) );
         }
         evals.put(evalId, newList);
      }
      return evals;
   }

   // PERMISSIONS

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#canBeginEvaluation(java.lang.String)
    */
   public boolean canBeginEvaluation(String userId) {
      log.debug("Checking begin eval for: " + userId);
      boolean isAdmin = external.isUserAdmin(userId);
      if ( isAdmin && (dao.countAll(EvalTemplate.class) > 0) ) {
         // admin can access all templates and create an evaluation if 
         // there is at least one template
         return true;
      }
      if ( external.countEvalGroupsForUser(userId, EvalConstants.PERM_ASSIGN_EVALUATION) > 0 ) {
         log.debug("User has permission to assign evaluation in at least one site");

         /*
          * TODO - Hierarchy
          * visible and shared sharing methods are meant to work by relating the hierarchy level of 
          * the owner with the sharing setting in the template, however, that was when 
          * we assumed there would only be one level per user. That is no longer anything 
          * we have control over (since we depend on data that comes from another API) 
          * so we will have to add in a table which will track the hierarchy levels and
          * link them to the template. This will be a very simple but necessary table.
          */

         /*
          * If the person is not an admin (super or any kind, currently we just have super admin) 
          * then system settings should be checked whether they can create templates 
          * or not. This is because if they cannot create templates then they cannot start 
          * evaluations also - kahuja.
          * 
          * TODO - this check needs to be more robust at some point
          * currently we are ignoring shared and visible templates - aaronz.
          */
         if ( ((Boolean)settings.get(EvalSettings.INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS)).booleanValue() && 
               dao.countVisibleTemplates(userId, new String[] {EvalConstants.SHARING_PUBLIC}, false) > 0 ) {
            return true;
         }
      }
      return false;
   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#getEvaluationState(java.lang.Long)
    */
   public String updateEvaluationState(Long evaluationId) {
      log.debug("evalId: " + evaluationId);
      // get evaluation
      EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
      if (eval == null) {
         throw new IllegalArgumentException("Cannot find evaluation with id: " + evaluationId);
      }

      // fix the state of this eval if needed, save it, and return the state constant 
      return fixReturnEvalState(eval, true);
   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#canRemoveEvaluation(java.lang.String, java.lang.Long)
    */
   public boolean canRemoveEvaluation(String userId, Long evaluationId) {
      log.debug("evalId: " + evaluationId + ",userId: " + userId);
      EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
      if (eval == null) {
         throw new IllegalArgumentException("Cannot find evaluation with id: " + evaluationId);
      }

      try {
         return canUserRemoveEval(userId, eval);
      } catch (RuntimeException e) {
         return false;
      }
   }

   /* (non-Javadoc)
    * @see edu.vt.sakai.evaluation.logic.EvalEvaluationsLogic#canTakeEvaluation(java.lang.String, java.lang.Long, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public boolean canTakeEvaluation(String userId, Long evaluationId, String evalGroupId) {
      log.debug("evalId: " + evaluationId + ", userId: " + userId + ", evalGroupId: " + evalGroupId);

      // grab the evaluation itself first
      EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
      if (eval == null) {
         throw new IllegalArgumentException("Cannot find evaluation with id: " + evaluationId);
      }

      // check the evaluation state
      String state = EvalUtils.getEvaluationState(eval);
      if ( ! EvalConstants.EVALUATION_STATE_ACTIVE.equals(state) &&
            ! EvalConstants.EVALUATION_STATE_DUE.equals(state) ) {
         log.info("User (" + userId + ") cannot take evaluation (" + evaluationId + ") when eval state is: " + state);
         return false;
      }

      if (evalGroupId != null) {
         // check that the evalGroupId is valid for this evaluation
         List<EvalAssignGroup> ags = dao.findByProperties(EvalAssignGroup.class, 
               new String[] {"evaluation.id", "evalGroupId"}, 
               new Object[] {evaluationId, evalGroupId});
         if (ags.size() <= 0) {
            log.info("User (" + userId + ") cannot take evaluation (" + evaluationId + ") in this evalGroupId (" + evalGroupId + "), not assigned");
            return false;
         } else {
            // make sure instructor approval is true
            EvalAssignGroup eac = (EvalAssignGroup) ags.get(0);
            if (! eac.getInstructorApproval().booleanValue() ) {
               log.info("User (" + userId + ") cannot take evaluation (" + evaluationId + ") in this evalGroupId (" + evalGroupId + "), instructor has not approved");
               return false;
            }
         }
      } else {
         // no groupId is supplied so do a simpler check
         log.info("No evalGroupId supplied, doing abbreviated check for evalId=" + evaluationId + ", userId=" + userId);
         if ( external.isUserAdmin(userId) ) {
            // admin trumps being in a group
            return true;
         }

         // make sure at least one group is valid for this eval
         int count = dao.countByProperties(EvalAssignGroup.class, 
               new String[] {"evaluation.id", "instructorApproval"}, 
               new Object[] {evaluationId, Boolean.TRUE});
         if (count <= 0) {
            // no valid groups
            return false;
         }
      }

      if ( EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(eval.getAuthControl()) ) {
         // if this is anonymous then group membership does not matter
         return true;
      } else if ( EvalConstants.EVALUATION_AUTHCONTROL_KEY.equals(eval.getAuthControl()) ) {
         // if this uses a key then only the key matters
         // TODO add key check
         return false;
      } else if ( EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ.equals(eval.getAuthControl()) ) {
         if (evalGroupId == null) {
            // if no groupId is supplied then simply check to see if the user is in any of the groups assigned,
            // hopefully this is faster than checking if the user has the right permission in every group -AZ
            List<EvalGroup> userEvalGroups = external.getEvalGroupsForUser(userId, EvalConstants.PERM_TAKE_EVALUATION);
            if (userEvalGroups.size() > 0) {
               // only try to do this check if there is at least one userEvalGroup
               String[] evalGroupIds = new String[userEvalGroups.size()];
               for (int i=0; i<userEvalGroups.size(); i++) {
                  EvalGroup group = (EvalGroup) userEvalGroups.get(i);
                  evalGroupIds[i] = group.evalGroupId;
               }

               int count = dao.countByProperties(EvalAssignGroup.class, 
                     new String[] {"evaluation.id", "instructorApproval", "evalGroupId"}, 
                     new Object[] {evaluationId, Boolean.TRUE, evalGroupIds});
               if (count > 0) {
                  // ok if at least one group is approved and in the set of groups this user can take evals in for this eval id
                  return true;
               }
            }
         } else {
            // check the user permissions
            if ( ! external.isUserAdmin(userId) && 
                  ! external.isUserAllowedInEvalGroup(userId, EvalConstants.PERM_TAKE_EVALUATION, evalGroupId) ) {
               log.info("User (" + userId + ") cannot take evaluation (" + evaluationId + ") without permission");
               return false;
            }

            // check if the user already took this evaluation
            int evalResponsesForUser = dao.countByProperties(EvalResponse.class, 
                  new String[] {"owner", "evaluation.id", "evalGroupId"}, 
                  new Object[] {userId, evaluationId, evalGroupId});
            if (evalResponsesForUser > 0) {
               // check if persistent object is the one that already exists
               List l = dao.findByProperties(EvalResponse.class, 
                     new String[] {"owner", "evaluation.id", "evalGroupId"}, 
                     new Object[] {userId, evaluationId, evalGroupId});
               EvalResponse response = (EvalResponse) l.get(0);
               if (response.getId() == null && l.size() == 1) {
                  // all is ok, the "existing" response is a hibernate persistent object
                  // WARNING: this is a bit of a hack though
               } else {
                  // user already has a response saved for this evaluation and evalGroupId
                  if (eval.getModifyResponsesAllowed() == null || 
                        eval.getModifyResponsesAllowed().booleanValue() == false) {
                     // user cannot modify existing responses
                     log.info("User (" + userId + ") cannot take evaluation (" + evaluationId + ") again, already taken");
                     return false;
                  }
               }
            }
            return true;
         }
      }
      return false;
   }


   // CATEGORIES

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationsLogic#getEvalCategories(java.lang.String)
    */
   public String[] getEvalCategories(String userId) {
      log.debug("userId: " + userId );

      // return all current categories or only return categories created by this user if not null
      List<String> l = dao.getEvalCategories(userId);
      return (String[]) l.toArray(new String[] {});
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalEvaluationsLogic#getEvaluationsByCategory(java.lang.String, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public List<EvalEvaluation> getEvaluationsByCategory(String evalCategory, String userId) {
      log.debug("evalCategory: " + evalCategory + ", userId: " + userId );

      if (evalCategory == null || evalCategory.equals("")) {
         throw new IllegalArgumentException("evalCategory cannot be blank or null");
      }

      List<EvalEvaluation> evals = new ArrayList<EvalEvaluation>();
      if (userId == null) {
         // get all evals for a category
         evals = dao.findByProperties(EvalEvaluation.class,
               new String[] {"evalCategory"}, 
               new Object[] {evalCategory}, 
               new int[] {EvaluationDao.EQUALS},
               new String[] {"startDate"});
      } else {
         // get all evals for a specific user for a category
         List takeGroups = external.getEvalGroupsForUser(userId, EvalConstants.PERM_TAKE_EVALUATION);

         String[] evalGroupIds = new String[takeGroups.size()];
         for (int i=0; i<takeGroups.size(); i++) {
            EvalGroup c = (EvalGroup) takeGroups.get(i);
            evalGroupIds[i] = c.evalGroupId;
         }

         // this sucks for efficiency -AZ
         Set s = dao.getEvaluationsByEvalGroups( evalGroupIds, true, false, true ); // only get active for users
         for (Iterator iter = s.iterator(); iter.hasNext();) {
            EvalEvaluation eval = (EvalEvaluation) iter.next();
            if ( evalCategory.equals(eval.getEvalCategory()) ) {
               evals.add(eval);
            }
         }
      }
      return evals;
   }


   // INTERNAL METHODS

   /**
    * Check if a user can control an evaluation
    * @param userId
    * @param eval
    * @return true if they can, false otherwise
    */
   protected boolean canUserControlEvaluation(String userId, EvalEvaluation eval) {
      if ( external.isUserAdmin(userId) ) {
         return true;
      } else if ( eval.getOwner().equals(userId) ) {
         return true;
      }
      return false;
   }

   /**
    * Check if user can remove this evaluation
    * @param userId
    * @param eval
    * @return truse if they can, false otherwise
    */
   protected boolean canUserRemoveEval(String userId, EvalEvaluation eval) {
      // if eval id is invalid then just log it
      if (eval == null) {
         log.warn("Cannot find evaluation to delete");
         return false;
      }

      // check user control permissions
      if (! canUserControlEvaluation(userId, eval) ) {
         log.warn("User ("+userId+") attempted to remove evaluation ("+eval.getId()+") without permissions");
         throw new SecurityException("User ("+userId+") attempted to remove evaluation ("+eval.getId()+") without permissions");
      }

      // cannot remove evaluations unless there are no responses (not locked)
      if ( eval.getLocked().booleanValue() ) {
         log.warn("Cannot remove an evaluation ("+eval.getId()+") which is locked");
         throw new IllegalStateException("Cannot remove an evaluation ("+eval.getId()+") which is locked");
      }

      return true;
   }

   /**
    * Fixes the state of an evaluation (if needed) and saves it,
    * will not save state for a new evaluation
    * 
    * @param eval
    * @param saveState if true, save the fixed eval state, else do not save
    * @return the state constant string
    */
   protected String fixReturnEvalState(EvalEvaluation eval, boolean saveState) {
      String state = EvalUtils.getEvaluationState(eval);
      // check state against stored state
      if (EvalConstants.EVALUATION_STATE_UNKNOWN.equals(state)) {
         log.warn("Evaluation ("+eval.getTitle()+") in UNKNOWN state");
      } else {
         // compare state and set if not equal
         if (! state.equals(eval.getState()) ) {
            eval.setState(state);
            if ( (eval.getId() != null) && saveState) {
               external.registerEntityEvent(EVENT_EVAL_STATE_CHANGE, eval);
               dao.save(eval);
            }
         }
      }
      return state;
   }


   /**
    * Wrap the persistent object so that the interceptor can track it
    * 
    * @param togo
    * @return
    */
// TODO - Interceptor strategy is hopeless, removing for now -AZ
// private EvalEvaluation wrapEvaluationProxy(EvalEvaluation togo) {
// if (togo != null && togo.getId() != null) {
// ProxyFactoryBean pfb = new ProxyFactoryBean();
// pfb.setProxyTargetClass(true);
// pfb.setTargetSource(new SingletonTargetSource(togo));
// pfb.addAdvice(new EvaluationInterceptor(this));
// return (EvalEvaluation) pfb.getObject();
// }
// else return togo;
// }

   /**
    * Get both owned and not-owned evaluation for the given user.
    * 
    * @param userId the internal user id (not username).
    * @param recentOnly if true return recently closed evaluations only
    * (still returns all active and in queue evaluations), if false return all closed evaluations.
    * @param evalsToReturn list of owned and not-owned evaluations. 
    * @param recent date for comparison when looking for recently closed evaluations.
    */
   @SuppressWarnings("unchecked")
   private void getEvalsWhereBeEvaluated(String userId, boolean recentOnly, List<EvalEvaluation> evalsToReturn, Date recent) {

      // Get the list of EvalGroup where user has "eval.be.evaluated" permission.
      List<EvalGroup> evaluatedContexts = external.getEvalGroupsForUser(userId, EvalConstants.PERM_BE_EVALUATED);
      if (evaluatedContexts.size() > 0) {

         String[] evalGroupsIds = new String[evaluatedContexts.size()];
         for (int i = 0; i < evaluatedContexts.size(); i++) {
            EvalGroup c = (EvalGroup) evaluatedContexts.get(i);
            evalGroupsIds [i] = c.evalGroupId;
         }

         // Using the list of EvalGroups, get the corresponding list of EvalAssignGroup.
         List<EvalAssignGroup> assignGroupList = dao.findByProperties(EvalAssignGroup.class,
               new String[] {"evalGroupId"}, 
               new Object[] {evalGroupsIds}, 
               new int[] {EvaluationDao.EQUALS});

         // Iterate through list of EvalAssignGroup and get the EvalEvaluation.
         for (int i = 0; i < assignGroupList.size(); i++) {

            EvalAssignGroup assignGroup = (EvalAssignGroup) assignGroupList.get(i);
            EvalEvaluation eval = assignGroup.getEvaluation();

            /*
             * If only recent evaluations have to be fetched, then check for
             * stop date else just add to the existing list of evaluations.
             */ 
            if (recentOnly) {
               if ((eval.getStopDate()).after(recent)) {
                  evalsToReturn.add(eval);
               } else {
                  // Do nothing
               }
            } else {
               evalsToReturn.add(eval);
            }

         } // end of for
      } // end of if
   } // end of method

}

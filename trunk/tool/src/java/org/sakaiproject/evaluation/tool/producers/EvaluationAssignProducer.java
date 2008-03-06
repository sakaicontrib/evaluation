/******************************************************************************
 * EvaluationAssignProducer.java - created by kahuja@vt.edu on Oct 05, 2006
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.tool.EvaluationBean;
import org.sakaiproject.evaluation.tool.utils.HierarchyRenderUtil;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * assign an evaluation to courses 
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Rui Feng (fengr@vt.edu)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationAssignProducer implements ViewComponentProducer, NavigationCaseReporter {

   public static final String VIEW_ID = "evaluation_assign";
   public String getViewID() {
      return VIEW_ID;
   }

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private EvaluationBean evaluationBean;
   public void setEvaluationBean(EvaluationBean evaluationBean) {
      this.evaluationBean = evaluationBean;
   }

   private EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }

   private HierarchyRenderUtil hierUtil;
   public void setHierarchyRenderUtil(HierarchyRenderUtil util) {
      hierUtil = util;
   }
   
   private ExternalHierarchyLogic hierarchyLogic;
   public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
      this.hierarchyLogic = logic;
   }


   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      UIMessage.make(tofill, "page-title", "assigneval.page.title");

      UIInternalLink.make(tofill, "summary-link", UIMessage.make("summary.page.title"),
            new SimpleViewParameters(SummaryProducer.VIEW_ID));	

      UIMessage.make(tofill, "create-eval-title", "starteval.page.title");
      UIMessage.make(tofill, "eval-settings-title", "evalsettings.page.title");

      UIMessage.make(tofill, "assign-eval-edit-page-title", "assigneval.assign.page.title", new Object[] {evaluationBean.eval.getTitle()});
      UIMessage.make(tofill, "assign-eval-instructions", "assigneval.assign.instructions", new Object[] {evaluationBean.eval.getTitle()});

      UIForm form = UIForm.make(tofill, "eval-assign-form");

      UIMessage.make(form, "name-header", "assigneval.name.header");
      UIMessage.make(form, "select-header", "assigneval.select.header");		

      List<EvalGroup> evalGroups = externalLogic.getEvalGroupsForUser(externalLogic.getCurrentUserId(), EvalConstants.PERM_BE_EVALUATED);
      if (evalGroups.size() > 0) {
         String[] ids = new String[evalGroups.size()];
         String[] labels = new String[evalGroups.size()];
         for (int i=0; i < evalGroups.size(); i++) {
            EvalGroup c = (EvalGroup) evalGroups.get(i);
            ids[i] = c.evalGroupId;
            labels[i] = c.title;
         }

         //UISelect siteCheckboxes = UISelect.makeMultiple(form, "siteCheckboxes", ids, "#{evaluationBean.selectedEvalGroupIds}", null);
         //String selectID = siteCheckboxes.getFullID();

         Set<String> evalGroupIDs = new HashSet<String>();
         /* Display the table for selecting hierarchy nodes */
         Boolean showHierarchy = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
         if (showHierarchy.booleanValue() == true) {
            UIMessage.make(form, "assign-hierarchy-title", "assigneval.page.hier.title");
            hierUtil.renderSelectHierarchyNodesTree(form, "hierarchy-tree-select:", "", "" );
         }
         
         String[] nonAssignedEvalGroupIDs = getEvalGroupIDsNotAssignedInHierarchy().toArray(new String[] {});
         //for (int i=0; i < ids.length; i++){
         for (int i = 0; i < nonAssignedEvalGroupIDs.length; i++) {
            UIBranchContainer checkboxRow = UIBranchContainer.make(form, "sites:", i+"");
            if (i % 2 == 0) {
               checkboxRow.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
            }
            //UISelectChoice checkbox = UISelectChoice.make(checkboxRow, "siteId", selectID, i);
            UIBoundBoolean checkbox = UIBoundBoolean.make(checkboxRow, "siteId", "evaluationBean.selectedEvalGroupIDsMap."+nonAssignedEvalGroupIDs[i]);
            //TODO  we shouldn't need to keep fetching this from the service API
            UIOutput title = UIOutput.make(checkboxRow, "siteTitle", externalLogic.getDisplayTitle(nonAssignedEvalGroupIDs[i]));
            UILabelTargetDecorator.targetLabel(title, checkbox); // make title a label for checkbox
         }
      }

      /*
       * TODO: If more than one course is selected and you come back to this page from confirm page,
       * then without changing the selection you again go to confirm page, you get a null pointer
       * that is created by RSF as:
       * 
       * 	"Error flattening value[Ljava.lang.String;@944d4a into class [Ljava.lang.String;
       * 	...
       *  java.lang.NullPointerException
       * 		at uk.org.ponder.arrayutil.ArrayUtil.lexicalCompare(ArrayUtil.java:205)
       *  	at uk.org.ponder.rsf.uitype.StringArrayUIType.valueUnchanged(StringArrayUIType.java:23)
       *  ..."
       */

      UICommand.make(form, "cancel-button", UIMessage.make("general.cancel.button"), "#{evaluationBean.cancelAssignAction}");
      UICommand.make(form, "editSettings", UIMessage.make("assigneval.edit.settings.button"), "#{evaluationBean.backToSettingsAction}");
      UICommand.make(form, "confirmAssignCourses", UIMessage.make("assigneval.save.assigned.button"), "#{evaluationBean.confirmAssignCoursesAction}");
   }
   
   public Set<String> getEvalGroupIDsNotAssignedInHierarchy() {
       
       // 1. All the Evaluation Group IDs
       Set<String> evalGroupIDs = new HashSet<String>();
       List<EvalGroup> evalGroups = externalLogic.getEvalGroupsForUser(externalLogic.getCurrentUserId(), EvalConstants.PERM_BE_EVALUATED);
       for (EvalGroup evalGroup: evalGroups) {
           evalGroupIDs.add(evalGroup.evalGroupId);
       }
       
       // 2. All the Evaluation Group IDs that are assigned to Hierarchy Nodes
       Set<String> hierAssignedGroupIDs = new HashSet<String>();
       
       EvalHierarchyNode rootNode = hierarchyLogic.getRootLevelNode();
       String[] rootNodeChildren = rootNode.childNodeIds.toArray(new String[] {});
       if (rootNodeChildren.length > 0) {
          Map<String,Set<String>> assignedGroups = hierarchyLogic.getEvalGroupsForNodes(rootNodeChildren);
          
          for (String key: assignedGroups.keySet()) {
              hierAssignedGroupIDs.addAll(assignedGroups.get(key));
          }
          
          // 3. Remove all EvalGroup IDs that have been assigned to 
          evalGroupIDs.removeAll(hierAssignedGroupIDs);
       }
       
       return evalGroupIDs;
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
    */
   @SuppressWarnings("unchecked")
   public List reportNavigationCases() {
      List i = new ArrayList();
      i.add(new NavigationCase(SummaryProducer.VIEW_ID, new SimpleViewParameters(SummaryProducer.VIEW_ID)));
      i.add(new NavigationCase(EvaluationSettingsProducer.VIEW_ID, new SimpleViewParameters(EvaluationSettingsProducer.VIEW_ID)));
      i.add(new NavigationCase(EvaluationAssignConfirmProducer.VIEW_ID, new SimpleViewParameters(EvaluationAssignConfirmProducer.VIEW_ID)));
      return i;
   }


}
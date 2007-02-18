/******************************************************************************
 * TemplateProducer.java - created by fengr@vt.edu on Aug 21, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 * Antranig Basman (antranig@caret.cam.ac.uk)
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.TemplateBeanLocator;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Page for start creating a template
 * 
 * @author: Rui Feng (fengr@vt.edu)
 */

public class TemplateProducer implements ViewComponentProducer,
    ViewParamsReporter, NavigationCaseReporter {

  public static final String VIEW_ID = "template_title_desc";

  public String getViewID() {
    return VIEW_ID;
  }

  private MessageLocator messageLocator;

  public void setMessageLocator(MessageLocator messageLocator) {
    this.messageLocator = messageLocator;
  }

  private EvalSettings settings;

  public void setSettings(EvalSettings settings) {
    this.settings = settings;
  }

  private EvalExternalLogic externalLogic;

  public void setExternalLogic(EvalExternalLogic externalLogic) {
    this.externalLogic = externalLogic;
  }

  private TextInputEvolver richTextEvolver;

  public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
    this.richTextEvolver = richTextEvolver;
  }

  /*
   * 1) accessing this page trough "Create Template" link -- 2) accessing
   * through "Modify Template Title/Description" link on ModifyTemplate page
   * 2-1) no template been save in DAO 2-2) existing template in DAO
   * 
   */
  public void fillComponents(UIContainer tofill, ViewParameters viewparams,
      ComponentChecker checker) {
    EvalViewParameters evalViewParams = (EvalViewParameters) viewparams;

    String templateOTPBinding = null;
    if (evalViewParams.templateId != null) {
      templateOTPBinding = "templateBeanLocator." + evalViewParams.templateId;
    }
    else {
      templateOTPBinding = "templateBeanLocator." + TemplateBeanLocator.NEW_1;
    }
    String templateOTP = templateOTPBinding + ".";

    UIOutput.make(tofill, "template-title-desc-title", messageLocator
        .getMessage("modifytemplatetitledesc.page.title")); //$NON-NLS-1$ //$NON-NLS-2$

    UIInternalLink.make(tofill,
        "summary-toplink", messageLocator.getMessage("summary.page.title"), //$NON-NLS-1$ //$NON-NLS-2$
        new SimpleViewParameters(SummaryProducer.VIEW_ID));

    UIForm form = UIForm.make(tofill, "basic-form"); //$NON-NLS-1$
    UIOutput.make(form, "title-header", messageLocator
        .getMessage("modifytemplatetitledesc.title.header")); //$NON-NLS-1$ //$NON-NLS-2$
    UIOutput.make(form, "description-header", messageLocator
        .getMessage("modifytemplatetitledesc.description.header")); //$NON-NLS-1$ //$NON-NLS-2$
    UIOutput.make(form, "description-note", messageLocator
        .getMessage("modifytemplatetitledesc.description.note")); //$NON-NLS-1$ //$NON-NLS-2$
    UICommand.make(form, "addContinue", messageLocator
        .getMessage("modifytemplatetitledesc.save.button"),
        "#{templateBBean.updateTemplateTitleDesc}");
    UIInput.make(form, "title", templateOTP + "title");
    UIInput descinput = UIInput.make(form, "description:", templateOTP
        + "description");
    richTextEvolver.evolveTextInput(descinput);

    /*
     * (Non-javadoc) If "EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY" is set
     * EvalConstants.SHARING_OWNER, then it means that owner can decide what
     * sharing settings to chose. In other words, it means that show the
     * sharing/visibility dropdown. Else just show the label for what has been
     * set in system setting (admin setting) i.e.
     * "EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY".
     */
    String sharingkey = null;
    String sharingValue = (String) settings
        .get(EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY);
    if (sharingValue.equals(EvalConstants.SHARING_OWNER)) {
      /*
       * Dropdown values are visible only for admins. For instructors
       * (non-admin) we just show the private label
       */
      if (externalLogic.isUserAdmin(externalLogic.getCurrentUserId())) {

        UIBranchContainer showSharingOptions = UIBranchContainer.make(form,
            "showSharingOptions:"); //$NON-NLS-1$
        // Commented as visible and shared are not used as of now - kahuja.
        // messageLocator.getMessage("modifytemplatetitledesc.sharing.visible")
        // //$NON-NLS-1$
        // messageLocator.getMessage("modifytemplatetitledesc.sharing.shared")
        // //$NON-NLS-1$
        String[] sharingList = {
            messageLocator
                .getMessage("modifytemplatetitledesc.sharing.private"), //$NON-NLS-1$
            messageLocator.getMessage("modifytemplatetitledesc.sharing.public") //$NON-NLS-1$
        };
        UISelect.make(showSharingOptions, "sharing",
            EvaluationConstant.MODIFIER_VALUES, sharingList, templateOTP
                + "sharing", null);
      }
      else {
        sharingkey = "modifytemplatetitledesc.sharing.private";
        form.parameters.add(new UIELBinding(
            templateOTP + "sharing", EvalConstants.SHARING_PRIVATE)); //$NON-NLS-1$
      }
    }
    else {
      if ((EvalConstants.SHARING_PRIVATE).equals(sharingValue))
        sharingkey = "modifytemplatetitledesc.sharing.private";
      else if ((EvalConstants.SHARING_PUBLIC).equals(sharingValue))
        sharingkey = "modifytemplatetitledesc.sharing.public"; //$NON-NLS-1$

      // Doing the binding of this sharing value so that it can be saved in the
      // database
      form.parameters.add(new UIELBinding(
          templateOTP + "sharing", sharingValue)); //$NON-NLS-1$
    }

    if (sharingkey != null) {
      /*
       * Displaying the sharing label private. Doing the binding of sharing
       * value private.
       */
      UIBranchContainer showSharingLabel = UIBranchContainer.make(form,
          "showSharingLabel:"); //$NON-NLS-1$
      UIOutput.make(showSharingLabel,
          "sharingValueToDisplay", messageLocator.getMessage(sharingkey)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    UIOutput.make(form, "cancel-button", messageLocator
        .getMessage("general.cancel.button"));
  }

  public ViewParameters getViewParameters() {
    return new EvalViewParameters();
  }

  public List reportNavigationCases() {
    List togo = new ArrayList();
    togo.add(new NavigationCase("success", new EvalViewParameters(
        TemplateModifyProducer.VIEW_ID, null)));
    return togo;
  }

}

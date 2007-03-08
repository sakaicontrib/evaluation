/******************************************************************************
 * EvaluationStartProducer.java - created by kahuja@vt.edu on Oct 05, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.params.TemplateViewParameters;
import org.sakaiproject.evaluation.tool.params.PreviewEvalParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UITextDimensionsDecorator;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
/**
 * Start a Evaluation page
 * 
 * @author:Kapil Ahuja (kahuja@vt.edu)
 * @author: Rui Feng (fengr@vt.edu)
 */

public class EvaluationStartProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {
	
	public static final String VIEW_ID = "evaluation_start"; //$NON-NLS-1$
		
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}
	
	private EvalTemplatesLogic templatesLogic;
	public void setTemplatesLogic(EvalTemplatesLogic templatesLogic) {
		this.templatesLogic = templatesLogic;
	}
	public String getViewID() {
		return VIEW_ID;
	}
	

	private TextInputEvolver richTextEvolver;

	public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
		this.richTextEvolver = richTextEvolver;
	}
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"), new SimpleViewParameters(SummaryProducer.VIEW_ID));			 //$NON-NLS-1$ //$NON-NLS-2$
		
		UIMessage.make(tofill, "start-eval-title", "starteval.page.title"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(tofill, "start-eval-header", "starteval.header"); //$NON-NLS-1$ //$NON-NLS-2$
		
		TemplateViewParameters evalViewParams = (TemplateViewParameters) viewparams;

		UIForm form = UIForm.make(tofill, "basic-form"); //$NON-NLS-1$
		
		UIMessage.make(form, "title-header","starteval.title.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(form, "instructions-header", "starteval.instructions.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(form, "instructions-desc", "starteval.instructions.desc"); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIInput.make(form, "title", "#{evaluationBean.eval.title}"); //$NON-NLS-1$ //$NON-NLS-2$
		UIInput instructions = UIInput.make(form, "instructions:", "#{evaluationBean.eval.instructions}"); //$NON-NLS-1$ //$NON-NLS-2$
		instructions.decorators = new DecoratorList(new UITextDimensionsDecorator(60, 4));
		richTextEvolver.evolveTextInput(instructions);

		// Code to make bottom table containing the list of templates when coming from Summary or Edit Settings page.
		if ( evalViewParams.templateId == null ) {

			//List templateList = evaluationBean.getTemplatesToDisplay();
			List templateList = templatesLogic.getTemplatesForUser(external.getCurrentUserId(), null, false);
			if(templateList !=null && templateList.size()>0){
				
				UIBranchContainer chooseTemplate = UIBranchContainer.make(form, "chooseTemplate:"); //$NON-NLS-1$

				//Preparing the string array of template titles and corresponding id's 
				String[] values = new String[templateList.size()];
				String[] labels = new String[templateList.size()];
				String[] owners = new String[templateList.size()];

				for (int count = 0; count < templateList.size(); count++) {
					values[count] = ((EvalTemplate)(templateList.get(count))).getId().toString();
					labels[count] = ((EvalTemplate)(templateList.get(count))).getTitle();
					owners[count] = ((EvalTemplate)(templateList.get(count))).getOwner();
				}
				
				UIMessage.make(chooseTemplate, "choose-template-header", "starteval.choose.template.header"); //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(chooseTemplate, "choose-template-desc", "starteval.choose.template.desc"); //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(chooseTemplate, "template-title-header", "starteval.template.title.header"); //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(chooseTemplate, "template-owner-header", "starteval.template.ownder.header"); //$NON-NLS-1$ //$NON-NLS-2$
				UISelect radios = UISelect.make(chooseTemplate, "templateRadio", values, labels, "#{evaluationBean.templateId}",null); //$NON-NLS-1$ //$NON-NLS-2$

				radios.optionnames = UIOutputMany.make(labels);

			    String selectID = radios.getFullID();
			    for (int i = 0; i < values.length; ++i) {
					UIBranchContainer radiobranch = UIBranchContainer.make(chooseTemplate, "templateOptions:", Integer.toString(i)); //$NON-NLS-1$
					UISelectChoice.make(radiobranch, "radioValue", selectID, i); //$NON-NLS-1$
					UISelectLabel.make(radiobranch, "radioLabel", selectID, i); //$NON-NLS-1$

				//	UIOutput.make(radiobranch,"radioOwner", logic.getUserDisplayName( owners[i])); //$NON-NLS-1$
					UIOutput.make(radiobranch,"radioOwner", external.getUserDisplayName( owners[i]));
					UIInternalLink.make(radiobranch, "viewPreview_link", UIMessage.make("starteval.view.preview.link"),  //$NON-NLS-1$ //$NON-NLS-2$
							new PreviewEvalParameters(PreviewEvalProducer.VIEW_ID,null, new Long(values[i]),null, EvaluationStartProducer.VIEW_ID));
			    }
			}
	
		} else {	
			form.parameters.add( new UIELBinding("#{evaluationBean.templateId}", //$NON-NLS-1$
					evalViewParams.templateId) );
		}

		UIMessage.make(form, "cancel-button", "general.cancel.button");

		UICommand.make(form, "continueToSettings", UIMessage.make("starteval.continue.settings.link"), "#{evaluationBean.continueToSettingsAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	public List reportNavigationCases() {
		List i = new ArrayList();

		i.add(new NavigationCase(EvaluationSettingsProducer.VIEW_ID, new SimpleViewParameters(EvaluationSettingsProducer.VIEW_ID)));
		i.add(new NavigationCase(ModifyTemplateItemsProducer.VIEW_ID, new SimpleViewParameters(ModifyTemplateItemsProducer.VIEW_ID)));
		i.add(new NavigationCase(SummaryProducer.VIEW_ID, new SimpleViewParameters(SummaryProducer.VIEW_ID)));

		return i;
	}
	
	public ViewParameters getViewParameters() {
		return new TemplateViewParameters();
	}
}

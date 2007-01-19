/******************************************************************************
 * TakeEvalProducer.java - created by feng@vt.edu on Sept 18, 2006
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

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.ItemBlockUtils;
import org.sakaiproject.evaluation.tool.params.EvalTakeViewParameters;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIColourDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;


/**
 * This page is for a user with take evaluation permission to fill and submit the evaluation
 * 
 * @author: Rui Feng (fengr@vt.edu)
 * @author: Kapil Ahuja (kahuja@vt.edu)
 */

public class TakeEvalProducer implements ViewComponentProducer,
	ViewParamsReporter, NavigationCaseReporter {

	public static final String VIEW_ID = "take_eval"; //$NON-NLS-1$
	public String getViewID() {
		return VIEW_ID;
	}
	
	private EvalEvaluationsLogic evalsLogic;
	public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
		this.evalsLogic = evalsLogic;
	}
	
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}
	
	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}	
	
	public ViewParameters getViewParameters() {
		return new EvalTakeViewParameters(VIEW_ID, null, null);
	}
	
	//This variable is used for binding the items to a list in evaluationBean
	private int totalItemsAdded = 0;
	
	/**
	 * 
	 * @param bl=
	 *            true: test if there is any "Course" item
	 * @param bl=
	 *            false: test if there is any "Instructor" item
	 */
	private boolean findItemCategory(boolean bl, List itemList) {
		boolean rs = false;

		for (int j = 0; j < itemList.size(); j++) {
			EvalItem item1 = (EvalItem) itemList.get(j);
			String category = item1.getCategory();
			if (bl && category.equals(EvalConstants.ITEM_CATEGORY_COURSE)) { //"Course"
				rs = true;
				break;
			}

			if (bl == false && category.equals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR)) { //"Instructor"
				rs = true;
				break;
			}
		}

		return rs;
	}


	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		UIOutput.make(tofill, "take-eval-title", messageLocator.getMessage("takeeval.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleViewParameters(SummaryProducer.VIEW_ID));			
		
		UIOutput.make(tofill, "eval-title-header", messageLocator.getMessage("takeeval.eval.title.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "course-title-header", messageLocator.getMessage("takeeval.course.title.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "instructions-header", messageLocator.getMessage("takeeval.instructions.header"));	 //$NON-NLS-1$ //$NON-NLS-2$
		
		
		EvalTakeViewParameters evalTakeViewParams = (EvalTakeViewParameters) viewparams;
		EvalEvaluation eval = evalsLogic.getEvaluationById(evalTakeViewParams.evaluationId);
		if(eval !=null && evalTakeViewParams.context !=null){
			UIOutput.make(tofill, "evalTitle", eval.getTitle()); //$NON-NLS-1$
			//get course title: from sakaicontext to course title
			//String title = logic.getDisplayTitle(evalTakeViewParams.context);
			String title = external.getDisplayTitle(evalTakeViewParams.context); 
			
			UIOutput.make(tofill, "courseTitle", title); //$NON-NLS-1$
			EvalTemplate et = eval.getTemplate();
			if(et.getDescription() != null)
				UIOutput.make(tofill, "description", et.getDescription()); //$NON-NLS-1$
			else
				UIOutput.make(tofill, "description", messageLocator.getMessage("takeeval.description.filler")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		
		UIOutput.make(tofill, "evalInstruction", eval.getInstructions()); //$NON-NLS-1$
		
		
		UIForm form = UIForm.make(tofill, "evaluationForm"); //$NON-NLS-1$

		//Binding the EvalEvaluation object id to the EvalEvaluation object id in EvaluationBean.
		form.parameters.add( new UIELBinding("#{evaluationBean.eval.id}", eval.getId()) ); //$NON-NLS-1$
		
		//Binding the sakaiContext (aka course) to the sakaiContext in EvaluationBean.
		form.parameters.add( new UIELBinding("#{evaluationBean.sakaiContext}", evalTakeViewParams.context) ); //$NON-NLS-1$

		EvalTemplate template = eval.getTemplate();

		// get items(parent items, child items --need to set order
		//List childItems = new ArrayList(template.getItems());

		// TODO - changed to EMPTY ARRAY so it will COMPILE - AZ
		//List allItems = new ArrayList(template.getItems());
		List allItems = new ArrayList();
		
		//filter out the block child items, to get a list non-child items
		List ncItemsList = ItemBlockUtils.getNonChildItems(allItems);
		// We know that for an evaluation child items will not be empty so no check needed here
		//Collections.sort(allItems, new PreviewEvalProducer.EvaluationItemOrderComparator());
		Collections.sort(ncItemsList, new PreviewEvalProducer.EvaluationItemOrderComparator());
		
		// check if there is any "Course" items or "Instructor" items;
		UIBranchContainer courseSection = null;
		UIBranchContainer instructorSection = null;
		if (this.findItemCategory(true,ncItemsList)){
			courseSection = UIBranchContainer.make(form,
					"courseSection:"); //$NON-NLS-1$
			UIOutput.make(courseSection, "course-questions-header", messageLocator.getMessage("takeeval.course.questions.header")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (this.findItemCategory(false, ncItemsList)){
			instructorSection = UIBranchContainer.make(form,
					"instructorSection:"); //$NON-NLS-1$
			UIOutput.make(instructorSection, "instructor-questions-header", messageLocator.getMessage("takeeval.instructor.questions.header"));			 //$NON-NLS-1$ //$NON-NLS-2$
		}
		for (int i = 0; i <ncItemsList.size(); i++) {
			EvalItem item1 = (EvalItem) ncItemsList.get(i);

			String cat = item1.getCategory();
			UIBranchContainer radiobranch = null;
			
			if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_COURSE)) { //"Course"
				radiobranch = UIBranchContainer.make(courseSection,
						"itemrow:first", Integer.toString(i)); //$NON-NLS-1$
				
				if (i % 2 == 1)
					radiobranch.decorators = new DecoratorList(
							new UIColourDecorator(null,
									Color.decode(EvaluationConstant.LIGHT_GRAY_COLOR)));

				this.doFillComponent(item1, i, radiobranch,courseSection, form,allItems);
				
			} else if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR)) { //"Instructor"
				radiobranch = UIBranchContainer.make(instructorSection,
						"itemrow:first", Integer.toString(i)); //$NON-NLS-1$
				
				if (i % 2 == 1)
					radiobranch.decorators = new DecoratorList(
							new UIColourDecorator(null,
									Color.decode(EvaluationConstant.LIGHT_GRAY_COLOR)));
				
				this.doFillComponent(item1, i, radiobranch,instructorSection, form,allItems);
			}
		} // end of for loop
		
		UICommand.make(form, "submitEvaluation", messageLocator.getMessage("takeeval.submit.button"), "#{evaluationBean.submitEvaluation}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
	} // end of method

	private void doFillComponent(EvalItem myItem, int i,
			UIBranchContainer radiobranch, UIContainer tofill, UIContainer form,List itemsList) {

		if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_SCALED)) { //"Scaled/Survey"

			// Bind item id to list of items in evaluation bean.
			form.parameters.add( new UIELBinding
					("#{evaluationBean.listOfItems." + totalItemsAdded + "}",myItem.getId().toString()) );		
			
/*			ItemDisplay itemDisplay = new ItemDisplay(myItem);
			String values[] = itemDisplay.getScaleValues();
			String labels[] = itemDisplay.getScaleLabels();
*/
			EvalScale  scale =  myItem.getScale();
			String[] scaleOptions = scale.getOptions();
			int optionCount = scaleOptions.length;
			String scaleValues[] = new String[optionCount];
			String scaleLabels[] = new String[optionCount];
			
			String setting = myItem.getScaleDisplaySetting();
			Boolean useNA = myItem.getUsesNA();

			if (setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_COMPACT)) { //"Compact"
				UIBranchContainer compact = UIBranchContainer.make(radiobranch,
						"compactDisplay:"); //$NON-NLS-1$
				
				String compactDisplayStart = scaleOptions[0];		
				String compactDisplayEnd = scaleOptions[optionCount - 1];

				for (int count = 0; count < optionCount; count++) {
					
					scaleValues[count] = new Integer(count).toString();
					scaleLabels[count] = " ";
				}
				
				UIOutput.make(compact, "itemNum", (new Integer(i + 1)) //$NON-NLS-1$
						.toString());
				UIOutput.make(compact, "itemText", myItem.getItemText()); //$NON-NLS-1$
				UIOutput.make(compact, "compactDisplayStart", compactDisplayStart);
				UISelect radios = UISelect.make(compact, "dummyRadio", scaleValues,
						scaleLabels, "#{evaluationBean.listOfAnswers." + 
							totalItemsAdded + "}", null); //$NON-NLS-1$
				radios.optionnames = UIOutputMany.make(scaleLabels);

				String selectID = radios.getFullID();
				for (int j = 0; j < scaleValues.length; ++j) {
					UIBranchContainer rb = UIBranchContainer.make(compact,
							"scaleOptions:", Integer.toString(j)); //$NON-NLS-1$
					UISelectChoice.make(rb, "dummyRadioValue", selectID, j); //$NON-NLS-1$
				}
				UIOutput.make(compact, "compactDisplayEnd", compactDisplayEnd);

				if (useNA.booleanValue() == true) {
					UIBranchContainer radiobranch3 = UIBranchContainer.make(
							compact, "showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}

			} else if (setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED)) { //"Compact Colored"
				UIBranchContainer compactColored = UIBranchContainer.make(
						radiobranch, "compactDisplayColored:"); //$NON-NLS-1$

				UIOutput.make(compactColored, "itemNum", (new Integer(i + 1)).toString());
				UIOutput.make(compactColored, "itemText", myItem.getItemText()); //$NON-NLS-1$
				// Get the scale ideal value (none, low, mid, high )
				String ideal = scale.getIdeal();
				// Compact start and end label containers
				UIBranchContainer compactStartContainer = UIBranchContainer
						.make(compactColored, "compactStartContainer:"); //$NON-NLS-1$
				UIBranchContainer compactEndContainer = UIBranchContainer.make(
						compactColored, "compactEndContainer:"); //$NON-NLS-1$
				// Finding the colors if compact start and end labels
				Color startColor = null;
				Color endColor = null;

				// When no ideal is specified then just plain blue for both
				// start and end
				if (ideal == null) {
					startColor = Color.decode(EvaluationConstant.BLUE_COLOR);
					endColor = Color.decode(EvaluationConstant.BLUE_COLOR);
				} else if (ideal.equals("low")) { //$NON-NLS-1$
					startColor = Color.decode(EvaluationConstant.GREEN_COLOR);
					endColor = Color.decode(EvaluationConstant.RED_COLOR);
				} else if (ideal.equals("mid")) { //$NON-NLS-1$
					startColor = Color.decode(EvaluationConstant.RED_COLOR);
					endColor = Color.decode(EvaluationConstant.RED_COLOR);
				}
				// This for the case when high is the ideal
				else {
					startColor = Color.decode(EvaluationConstant.RED_COLOR);
					endColor = Color.decode(EvaluationConstant.GREEN_COLOR);
				}

				compactStartContainer.decorators = new DecoratorList(
						new UIColourDecorator(null, startColor));
				compactEndContainer.decorators = new DecoratorList(
						new UIColourDecorator(null, endColor));

				String compactDisplayStart = scaleOptions[0];		
				String compactDisplayEnd = scaleOptions[optionCount - 1];
				for (int count = 0; count < optionCount; count++) {
					
					scaleValues[count] = new Integer(count).toString();
					scaleLabels[count] = " ";
				}
				// Compact start and end actual labels
				UIOutput.make(compactStartContainer, "compactDisplayStart", compactDisplayStart);
				UIOutput.make(compactEndContainer, "compactDisplayEnd", compactDisplayEnd);

				// For the radio buttons
				UIBranchContainer compactRadioContainer = UIBranchContainer
						.make(compactColored, "compactRadioContainer:"); //$NON-NLS-1$

				String idealImage = ""; //$NON-NLS-1$
				if (ideal == null)
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[0];
				else if (ideal.equals("low")) //$NON-NLS-1$
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[1];
				else if (ideal.equals("mid")) //$NON-NLS-1$
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[2];
				else
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[3];

				UILink.make(compactRadioContainer, "idealImage", idealImage); //$NON-NLS-1$

				UISelect radios = UISelect.make(compactRadioContainer, 
						"dummyRadio", scaleValues, scaleLabels, "#{evaluationBean.listOfAnswers." + 
								totalItemsAdded + "}", null);
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				String selectID = radios.getFullID();
				for (int j = 0; j < scaleValues.length; ++j) {
					UIBranchContainer radioBranchFirst = UIBranchContainer
							.make(compactRadioContainer, "scaleOptionsFirst:",
									Integer.toString(j));
					UISelectChoice.make(radioBranchFirst,
							"dummyRadioValueFirst", selectID, j); //$NON-NLS-1$

					UIBranchContainer radioBranchSecond = UIBranchContainer
							.make(compactRadioContainer, "scaleOptionsSecond:",
									Integer.toString(j));
					UISelectChoice.make(radioBranchSecond,
							"dummyRadioValueSecond", selectID, j); //$NON-NLS-1$
				}

				if (useNA.booleanValue() == true) {
					UIBranchContainer radiobranch3 = UIBranchContainer.make(
							compactColored, "showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}

			} else if (setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_FULL) || setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED)
					|| setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL)) {// "Full","Full Colored","Vertical"
				
				for (int count = 0; count < optionCount; count++) {

					scaleValues[count] = new Integer(count).toString();
					scaleLabels[count] = scaleOptions[count];	
				}
				
				UIBranchContainer fullFirst = UIBranchContainer.make(
						radiobranch, "fullType:"); //$NON-NLS-1$
				UIOutput.make(fullFirst, "itemNum", (new Integer(i + 1)) //$NON-NLS-1$
						.toString());
				UIOutput.make(fullFirst, "itemText", myItem.getItemText()); //$NON-NLS-1$
				if (useNA.booleanValue() == true) {
					UIBranchContainer radiobranch3 = UIBranchContainer.make(
							fullFirst, "showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				// display next row
				UIBranchContainer radiobranchFullRow = UIBranchContainer.make(
						tofill, "itemrow:second", Integer.toString(i)); //$NON-NLS-1$
				// Setting the row background color for even numbered rows.
				if (i % 2 == 1)
					radiobranchFullRow.decorators = new DecoratorList(
							new UIColourDecorator(
									null,
									Color
											.decode(EvaluationConstant.LIGHT_GRAY_COLOR)));

				if (setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_FULL)) { //"Full"
					UIBranchContainer full = UIBranchContainer.make(
							radiobranchFullRow, "fullDisplay:"); //$NON-NLS-1$
					
					// Radio Buttons
					UISelect radios = UISelect.make(full, 
							"dummyRadio", scaleValues,scaleLabels,
								"#{evaluationBean.listOfAnswers." + 
									totalItemsAdded + "}", null);
					
					radios.optionnames = UIOutputMany.make(scaleLabels);
					String selectID = radios.getFullID();
					for (int j = 0; j < scaleValues.length; ++j) {
						UIBranchContainer radiobranchNested = UIBranchContainer
								.make(full, "scaleOptions:", Integer.toString(j));
						UISelectChoice.make(radiobranchNested,
								"dummyRadioValue", selectID, j); //$NON-NLS-1$
						UISelectLabel.make(radiobranchNested,
								"dummyRadioLabel", selectID, j); //$NON-NLS-1$
					}
				} else if (setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL)) { //"Vertical"
					UIBranchContainer vertical = UIBranchContainer.make(
							radiobranchFullRow, "verticalDisplay:"); //$NON-NLS-1$
					
					UISelect radios = UISelect.make(vertical, 
							"dummyRadio", scaleValues, scaleLabels,
								"#{evaluationBean.listOfAnswers." + 
									totalItemsAdded + "}", null);
					
					radios.optionnames = UIOutputMany.make(scaleLabels);
					String selectID = radios.getFullID();
					for (int j = 0; j < scaleValues.length; ++j) {
						UIBranchContainer radiobranchInside = UIBranchContainer
								.make(vertical, "scaleOptions:", Integer.toString(j));
						UISelectChoice.make(radiobranchInside,
								"dummyRadioValue", selectID, j); //$NON-NLS-1$
						UISelectLabel.make(radiobranchInside,
								"dummyRadioLabel", selectID, j); //$NON-NLS-1$
					}
				} else {// for "Full Colored"
					UIBranchContainer fullColored = UIBranchContainer.make(
							radiobranchFullRow,"fullDisplayColored:"); //$NON-NLS-1$
					// Get the scale ideal value (none, low, mid, high )
					String ideal = scale.getIdeal(); //myItem.getScale().getIdeal();

					// Set the ideal image
					String idealImage = ""; //$NON-NLS-1$
					if (ideal == null)
						idealImage = EvaluationConstant.COLORED_IMAGE_URLS[0];
					else if (ideal.equals("low")) //$NON-NLS-1$
						idealImage = EvaluationConstant.COLORED_IMAGE_URLS[1];
					else if (ideal.equals("mid")) //$NON-NLS-1$
						idealImage = EvaluationConstant.COLORED_IMAGE_URLS[2];
					else
						idealImage = EvaluationConstant.COLORED_IMAGE_URLS[3];

					UILink.make(fullColored, "idealImage", idealImage); //$NON-NLS-1$

					UISelect radios = UISelect.make(fullColored, 
							"dummyRadio",scaleValues, scaleLabels,"#{evaluationBean.listOfAnswers." + 
									totalItemsAdded + "}", null);

					radios.optionnames = UIOutputMany.make(scaleLabels);

					String selectID = radios.getFullID();
					for (int j = 0; j < scaleValues.length; ++j) {
						UIBranchContainer radiobranchInside = UIBranchContainer
								.make(fullColored, "scaleOptions:", Integer.toString(j));
						UISelectChoice.make(radiobranchInside,
								"dummyRadioValue", selectID, j); //$NON-NLS-1$
						UISelectLabel.make(radiobranchInside,
								"dummyRadioLabel", selectID, j); //$NON-NLS-1$
					}
				}

			} else if (setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED)) { //"Stepped"
				UIBranchContainer stepped = UIBranchContainer.make(radiobranch,
						"steppedDisplay:"); //$NON-NLS-1$

				UIOutput.make(stepped, "itemNum", (new Integer(i + 1)) //$NON-NLS-1$
						.toString());
				UIOutput.make(stepped, "itemText", myItem.getItemText()); //$NON-NLS-1$

				if (useNA.booleanValue() == true) {
					UIBranchContainer radiobranch3 = UIBranchContainer.make(
							stepped, "showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				for (int count = 1; count <= optionCount; count++) {
					scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
					scaleLabels[optionCount - count] = scaleOptions[count-1];
				}
				
				UISelect radios = UISelect.make(stepped, 
						"dummyRadio", scaleValues, scaleLabels,
							"#{evaluationBean.listOfAnswers." + 
								totalItemsAdded + "}", null); 
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				String selectID = radios.getFullID();

				for (int j = 0; j < scaleValues.length; ++j) {
					UIBranchContainer radioTopLabelBranch = UIBranchContainer
							.make(stepped, "scaleTopLabelOptions:", Integer.toString(j));
					UISelectLabel.make(radioTopLabelBranch,
							"dummyTopRadioLabel", selectID, j); //$NON-NLS-1$
					UILink.make(radioTopLabelBranch, "cornerImage", //$NON-NLS-1$
							EvaluationConstant.STEPPED_IMAGE_URLS[0]);

					// This branch container is created to help in creating the
					// middle images after the LABEL
					for (int k = 0; k < j; ++k) {
						UIBranchContainer radioTopLabelAfterBranch = UIBranchContainer
								.make(radioTopLabelBranch,
										"scaleTopLabelAfterOptions:", Integer.toString(k));
						UILink.make(radioTopLabelAfterBranch, "middleImage",
								EvaluationConstant.STEPPED_IMAGE_URLS[1]);
					}

					UIBranchContainer radioBottomLabelBranch = UIBranchContainer
							.make(stepped, "scaleBottomLabelOptions:", Integer.toString(j));
					UILink.make(radioBottomLabelBranch, "bottomImage",EvaluationConstant.STEPPED_IMAGE_URLS[2]);

					UIBranchContainer radioValueBranch = UIBranchContainer
							.make(stepped, "scaleValueOptions:", Integer.toString(j));
					UISelectChoice.make(radioValueBranch, "dummyRadioValue",selectID, j);
				}

			} else if (setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED)) { //"Stepped Colored"
				UIBranchContainer steppedColored = UIBranchContainer.make(
						radiobranch, "steppedDisplayColored:"); //$NON-NLS-1$

				UIOutput.make(steppedColored, "itemNum", (new Integer(i + 1)) //$NON-NLS-1$
						.toString());
				UIOutput.make(steppedColored, "itemText", myItem.getItemText()); //$NON-NLS-1$

				if (useNA.booleanValue() == true) {
					UIBranchContainer radiobranch3 = UIBranchContainer.make(
							steppedColored, "showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				// Get the scale ideal value (none, low, mid, high )
				String ideal = scale.getIdeal();//myItem.getScale().getIdeal();
				String idealImage = ""; //$NON-NLS-1$
				if (ideal == null)
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[0];
				else if (ideal.equals("low")) //$NON-NLS-1$
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[1];
				else if (ideal.equals("mid")) //$NON-NLS-1$
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[2];
				else
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[3];

				UILink.make(steppedColored, "idealImage", idealImage); //$NON-NLS-1$
				
				for (int count = 1; count <= optionCount; count++) {
					
					scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
					scaleLabels[optionCount - count] = scaleOptions[count-1];
				}
				
				UISelect radios = UISelect.make(steppedColored, 
						"dummyRadio", scaleValues, scaleLabels,
							"#{evaluationBean.listOfAnswers." + 
								totalItemsAdded + "}", null); 
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				String selectID = radios.getFullID();
				for (int j = 0; j < scaleValues.length; ++j) {
					UIBranchContainer rowBranch = UIBranchContainer.make(
							steppedColored, "rowBranch:", Integer.toString(j)); //$NON-NLS-1$
					// Actual label
					UISelectLabel.make(rowBranch, "topLabel", selectID, j); //$NON-NLS-1$

					// Corner Image
					UILink.make(rowBranch, "cornerImage", //$NON-NLS-1$
							EvaluationConstant.STEPPED_IMAGE_URLS[0]);

					// This branch container is created to help in creating the
					// middle images after the LABEL
					for (int k = 0; k < j; ++k) {
						UIBranchContainer afterTopLabelBranch = UIBranchContainer
								.make(rowBranch, "afterTopLabelBranch:",Integer.toString(k));
						UILink.make(afterTopLabelBranch, "middleImage",
								EvaluationConstant.STEPPED_IMAGE_URLS[1]);
					}

					UIBranchContainer bottomLabelBranch = UIBranchContainer
							.make(steppedColored, "bottomLabelBranch:", Integer.toString(j));
					UILink.make(bottomLabelBranch, "bottomImage", //$NON-NLS-1$
							EvaluationConstant.STEPPED_IMAGE_URLS[2]);

					UIBranchContainer radioBranchFirst = UIBranchContainer
							.make(steppedColored, "scaleOptionsFirst:", Integer.toString(j));
					UISelectChoice.make(radioBranchFirst,
							"dummyRadioValueFirst", selectID, j); //$NON-NLS-1$

					UIBranchContainer radioBranchSecond = UIBranchContainer
							.make(steppedColored, "scaleOptionsSecond:",Integer.toString(j));
					UISelectChoice.make(radioBranchSecond,
							"dummyRadioValueSecond", selectID, j); //$NON-NLS-1$
				}

			}
			/*
			 * increment the total items (related to binding of list of 
			 * items and answers in evaluation bean.
			 */ 
			totalItemsAdded++;

		// TODO - changed to ITEM_TYPE_SCALED so it will COMPILE - AZ
		} else if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_SCALED) 
				&& myItem.getScaleDisplaySetting().equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED)) { //"Question Block","Stepped"
			UIBranchContainer block = UIBranchContainer.make(radiobranch,
					"blockStepped:"); //$NON-NLS-1$
			UIOutput.make(block, "itemNum", (new Integer(i + 1)).toString()); //$NON-NLS-1$
			UIOutput.make(block, "itemText", myItem.getItemText()); //$NON-NLS-1$
			Boolean useNA = myItem.getUsesNA();
			if (useNA.booleanValue() == true) {
				UIBranchContainer radiobranch3 = UIBranchContainer.make(block,
						"showNA:"); //$NON-NLS-1$
				UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
				UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			// Radio Buttons
/*			ItemDisplay itemDisplay = new ItemDisplay(myItem);
			String values[] = itemDisplay.getScaleValues();
			String labels[] = itemDisplay.getScaleLabels();
*/
			EvalScale  scale = myItem.getScale();
			String[] scaleOptions = scale.getOptions();
			int optionCount = scaleOptions.length;
			String scaleValues[] = new String[optionCount];
			String scaleLabels[] = new String[optionCount];

			for (int count = 1; count <= optionCount; count++) {
				scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
				scaleLabels[optionCount - count] = scaleOptions[count-1];
			}
			
			
			UISelect radioLabel = UISelect.make(block, "radioLabel", scaleValues,scaleLabels, null, false);
			radioLabel.optionnames = UIOutputMany.make(scaleLabels);
			String selectIDLabel = radioLabel.getFullID();
			
			for (int j = 0; j < scaleValues.length; ++j) {
				UIBranchContainer radioTopLabelBranch = UIBranchContainer.make(
						block, "scaleTopLabelOptions:", Integer.toString(j)); //$NON-NLS-1$
				UISelectLabel.make(radioTopLabelBranch, "dummyTopRadioLabel", //$NON-NLS-1$
						selectIDLabel, j);
				UILink.make(radioTopLabelBranch, "cornerImage", //$NON-NLS-1$
						EvaluationConstant.STEPPED_IMAGE_URLS[0]);
				// This branch container is created to help in creating the
				// middle images after the LABEL
				for (int k = 0; k < j; ++k) {
					UIBranchContainer radioTopLabelAfterBranch = UIBranchContainer
							.make(radioTopLabelBranch,
									"scaleTopLabelAfterOptions:", Integer.toString(k));
					UILink.make(radioTopLabelAfterBranch, "middleImage",
							EvaluationConstant.STEPPED_IMAGE_URLS[1]);
				}
				UIBranchContainer radioBottomLabelBranch = UIBranchContainer
						.make(block, "scaleBottomLabelOptions:", Integer.toString(j));
				UILink.make(radioBottomLabelBranch, "bottomImage",
						EvaluationConstant.STEPPED_IMAGE_URLS[2]);
			}

			// get child block item text
			// TODO - changed to ALWAYS FALSE so it will COMPILE - AZ
			//if (myItem.getBlockParent().booleanValue() == true) {
			if ( false ) {
				Long parentID = myItem.getId();
				Integer blockID = new Integer(parentID.intValue());
				
			//	List childItems = logic.findItem(blockID);
				
				List childItems = ItemBlockUtils.getChildItmes(itemsList, blockID);
				if (childItems != null && childItems.size() > 0) {
					for (int j = 0; j < childItems.size(); j++) {
						UIBranchContainer queRow = UIBranchContainer.make(
								block, "queRow:", Integer.toString(j)); //$NON-NLS-1$

						// Get child item
						EvalItem child = (EvalItem) childItems.get(j);
						
						// Bind item id to list of items in evaluation bean.
						form.parameters.add( new UIELBinding
								("#{evaluationBean.listOfItems." +  //$NON-NLS-1$
										totalItemsAdded + "}",	 //$NON-NLS-1$
											child.getId().toString()) );		
						
						// Bind answer to list of answers in evaluation bean.
						UISelect radios = UISelect.make(queRow, "dummyRadio",scaleValues, scaleLabels, 
									"#{evaluationBean.listOfAnswers."+ totalItemsAdded + "}", null); //$NON-NLS-1$
						totalItemsAdded++;
						
						radios.optionnames = UIOutputMany.make(scaleLabels);
						String selectID = radios.getFullID();
						
						UIOutput.make(queRow, "queNo", Integer.toString(j + 1)); //$NON-NLS-1$
						UIOutput.make(queRow, "queText", child.getItemText()); //$NON-NLS-1$
						for (int k = 0; k < scaleValues.length; k++) {
							UIBranchContainer bc1 = UIBranchContainer.make(
									queRow, "scaleValueOptions:", Integer //$NON-NLS-1$
											.toString(k));
							UISelectChoice.make(bc1, "dummyRadioValue", //$NON-NLS-1$
									selectID, k);
						}
					}
				}// end of if

			} // end of get child block item

		// TODO - changed to ITEM_TYPE_SCALED so it will COMPILE - AZ
		} else if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_SCALED) 
				&& myItem.getScaleDisplaySetting().equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED)) { //"Question Block","Stepped Colored"

			UIBranchContainer blockSteppedColored = UIBranchContainer.make(
					radiobranch, "blockSteppedColored:"); //$NON-NLS-1$
			UIOutput.make(blockSteppedColored, "itemNum", (new Integer(i + 1)).toString());
			UIOutput
					.make(blockSteppedColored, "itemText", myItem.getItemText()); //$NON-NLS-1$
			Boolean useNA = myItem.getUsesNA();
			if (useNA.booleanValue() == true) {
				UIBranchContainer radiobranch3 = UIBranchContainer.make(
						blockSteppedColored, "showNA:"); //$NON-NLS-1$
				UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
				UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			EvalScale  scale = myItem.getScale();
			String[] scaleOptions = scale.getOptions();
			int optionCount = scaleOptions.length;
			String scaleValues[] = new String[optionCount];
			String scaleLabels[] = new String[optionCount];

			for (int count = 1; count <= optionCount; count++) {
				scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
				scaleLabels[optionCount - count] = scaleOptions[count-1];
			}
			
			// Get the scale ideal value (none, low, mid, high )
			String ideal = scale.getIdeal();//myItem.getScale().getIdeal();
			String idealImage = ""; //$NON-NLS-1$
			if (ideal == null)
				idealImage = EvaluationConstant.COLORED_IMAGE_URLS[0];
			else if (ideal.equals("low")) //$NON-NLS-1$
				idealImage = EvaluationConstant.COLORED_IMAGE_URLS[1];
			else if (ideal.equals("mid")) //$NON-NLS-1$
				idealImage = EvaluationConstant.COLORED_IMAGE_URLS[2];
			else
				idealImage = EvaluationConstant.COLORED_IMAGE_URLS[3];

			// Radio Buttons
	/*		ItemDisplay itemDisplay = new ItemDisplay(myItem);
			String values[] = itemDisplay.getScaleValues();
			String labels[] = itemDisplay.getScaleLabels();
*/
			UISelect radioLabel = UISelect.make(blockSteppedColored, 
					"radioLabel", scaleValues, scaleLabels, null, false); //$NON-NLS-1$
			radioLabel.optionnames = UIOutputMany.make(scaleLabels);
			String selectIDLabel = radioLabel.getFullID();
			
			for (int j = 0; j < scaleValues.length; ++j) {
				UIBranchContainer rowBranch = UIBranchContainer.make(
						blockSteppedColored, "rowBranch:", Integer.toString(j)); //$NON-NLS-1$
				// Actual label
				UISelectLabel.make(rowBranch, "topLabel", selectIDLabel, j); //$NON-NLS-1$
				// Corner Image
				UILink.make(rowBranch, "cornerImage",
						EvaluationConstant.STEPPED_IMAGE_URLS[0]);
				// This branch container is created to help in creating the
				// middle images after the LABEL
				for (int k = 0; k < j; ++k) {
					UIBranchContainer afterTopLabelBranch = UIBranchContainer
							.make(rowBranch, "afterTopLabelBranch:", Integer.toString(k));
					UILink.make(afterTopLabelBranch, "middleImage", //$NON-NLS-1$
							EvaluationConstant.STEPPED_IMAGE_URLS[1]);
				}

				UIBranchContainer bottomLabelBranch = UIBranchContainer.make(
						blockSteppedColored, "bottomLabelBranch:", Integer.toString(j));
				UILink.make(bottomLabelBranch, "bottomImage",
						EvaluationConstant.STEPPED_IMAGE_URLS[2]);

			}
		
			// get child block item text
			// TODO - changed to ALWAYS FALSE so it will COMPILE - AZ
			//if (myItem.getBlockParent().booleanValue() == true) {
			if ( false ) {
				Long parentID = myItem.getId();
				Integer blockID = new Integer(parentID.intValue());
				
				//List childItems = logic.findItem(blockID);
				List childItems = ItemBlockUtils.getChildItmes(itemsList, blockID);
				if (childItems != null && childItems.size() > 0) {
					for (int j = 0; j < childItems.size(); j++) {
						UIBranchContainer queRow = UIBranchContainer.make(
								blockSteppedColored, "queRow:", Integer //$NON-NLS-1$
										.toString(j));

						//get the child item
						EvalItem child = (EvalItem) childItems.get(j);
						
						// Bind item id to list of items in evaluation bean.
						form.parameters.add( new UIELBinding
								("#{evaluationBean.listOfItems."+  //$NON-NLS-1$
										totalItemsAdded + "}",	 //$NON-NLS-1$
											child.getId().toString()) );		
						
						// Bind the answers to list of answers in evaluation bean.
						UISelect radios = UISelect.make(queRow, "dummyRadio",  //$NON-NLS-1$
								scaleValues,	scaleLabels, 
									"#{evaluationBean.listOfAnswers."  //$NON-NLS-1$
										+ totalItemsAdded + "}", null); //$NON-NLS-1$
						totalItemsAdded++;

						radios.optionnames = UIOutputMany.make(scaleLabels);
						String selectID = radios.getFullID();
						
						UIOutput.make(queRow, "queNo", Integer.toString(j + 1)); //$NON-NLS-1$
						UIOutput.make(queRow, "queText", child.getItemText()); //$NON-NLS-1$
						UILink.make(queRow, "idealImage", idealImage); //$NON-NLS-1$
						for (int k = 0; k < scaleValues.length; k++) {
							UIBranchContainer radioBranchFirst = UIBranchContainer
									.make(queRow, "scaleOptionsFirst:", Integer //$NON-NLS-1$
											.toString(k));
							UISelectChoice.make(radioBranchFirst,
									"dummyRadioValueFirst", selectID, k); //$NON-NLS-1$

							UIBranchContainer radioBranchSecond = UIBranchContainer
									.make(queRow, "scaleOptionsSecond:", //$NON-NLS-1$
											Integer.toString(k));
							UISelectChoice.make(radioBranchSecond,
									"dummyRadioValueSecond", selectID, k); //$NON-NLS-1$
						}
					}
				}// end of if

			} // end of get child block item

		} else if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) { //"Short Answer/Essay"

			UIBranchContainer essay = UIBranchContainer.make(radiobranch,
					"essayType:"); //$NON-NLS-1$
			UIOutput.make(essay, "itemNum", (new Integer(i + 1)).toString()); //$NON-NLS-1$
			UIOutput.make(essay, "itemText", myItem.getItemText()); //$NON-NLS-1$
			Boolean useNA = myItem.getUsesNA();
			if (useNA.booleanValue() == true) {
				UIBranchContainer radiobranch3 = UIBranchContainer.make(essay,
						"showNA:"); //$NON-NLS-1$
				UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
				UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// Binding item id to list of items in evaluation bean.
			form.parameters.add( new UIELBinding
					("#{evaluationBean.listOfItems." + totalItemsAdded + "}",
							myItem.getId().toString()) );		
			
			//Binding answer to this item to list of answers in evaluation bean
			UIInput textarea = UIInput.make(essay, "essayBox",
					"#{evaluationBean.listOfAnswers." + totalItemsAdded + "}"); //$NON-NLS-1$ //$NON-NLS-2$
			totalItemsAdded++;
			
			Map attrmap = new HashMap();
			String rowNum = myItem.getDisplayRows().toString();
			attrmap.put("rows", rowNum); //$NON-NLS-1$
			textarea.decorators = new DecoratorList(
					new UIFreeAttributeDecorator(attrmap));

		} else if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_HEADER)) { //"Text Header"

			UIBranchContainer header = UIBranchContainer.make(radiobranch,
					"headerType:"); //$NON-NLS-1$
			UIOutput.make(header, "itemNum", (new Integer(i + 1)).toString()); //$NON-NLS-1$
			UIOutput.make(header, "itemText", myItem.getItemText()); //$NON-NLS-1$

		}
		
	} // end of method

	public List reportNavigationCases() {
		List i = new ArrayList();

		i.add(new NavigationCase(SummaryProducer.VIEW_ID, new SimpleViewParameters(SummaryProducer.VIEW_ID)));

		return i;
	}
}

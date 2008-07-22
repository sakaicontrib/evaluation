package org.sakaiproject.evaluation.tool.producers;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class ControlEmailProducer implements ViewComponentProducer {

	/**
	 * This is used for navigation within the system.
	 */
	public static final String VIEW_ID = "control_email";

	public String getViewID() {
		return VIEW_ID;
	}

	// Spring injection 
	private EvalCommonLogic commonLogic;

	public void setCommonLogic(EvalCommonLogic commonLogic) {
		this.commonLogic = commonLogic;
	}

	private EvalSettings evalSettings;
	public void setEvalSettings(EvalSettings evalSettings) {
		this.evalSettings = evalSettings;
	}
	
	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	
	private Locale locale;
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	private FormatAwareDateInputEvolver dateevolver;
	public void setDateEvolver(FormatAwareDateInputEvolver dateevolver) {
		this.dateevolver = dateevolver;
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {

		String currentUserId = commonLogic.getCurrentUserId();
		boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
		boolean useDateTime = true;
		
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
		DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale);

		if (!userAdmin) {
			// Security check and denial
			throw new SecurityException(
					"Non-admin users may not access this page");
		}
		
		/*
		 * top links here
		 */
		UIInternalLink.make(tofill, "summary-link", UIMessage
				.make("summary.page.title"), new SimpleViewParameters(
				SummaryProducer.VIEW_ID));
		UIInternalLink.make(tofill, "administrate-link", UIMessage
				.make("administrate.page.title"), new SimpleViewParameters(
				AdministrateProducer.VIEW_ID));
		UIInternalLink.make(tofill, "control-scales-link", UIMessage
				.make("controlscales.page.title"), new SimpleViewParameters(
				ControlScalesProducer.VIEW_ID));
		UIInternalLink.make(tofill, "control-templates-link", UIMessage
				.make("controltemplates.page.title"), new SimpleViewParameters(
				ControlTemplatesProducer.VIEW_ID));
		UIInternalLink.make(tofill, "control-items-link", UIMessage
				.make("controlitems.page.title"), new SimpleViewParameters(
				ControlItemsProducer.VIEW_ID));
		UIInternalLink.make(tofill, "control-evaluations-link", UIMessage
				.make("controlevaluations.page.title"),
				new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
		UIInternalLink
				.make(tofill, "control-emailtemplates-link", UIMessage
						.make("controlemailtemplates.page.title"),
						new SimpleViewParameters(
								ControlEmailTemplatesProducer.VIEW_ID));

		UIForm form = UIForm.make(tofill, "emailcontrol-form");
		
	     // use a date which is related to the current users locale
	     DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		
		/* one email per course evaluation (default) OR one email per student 
		 * having one or more evaluations for which no response has been submitted (UM) */
		UIMessage.make(form, "controlemail-instructions", "controlemail.instructions");
		AdministrateProducer.makeBoolean(form, "emailtype",
				EvalSettings.ENABLE_SINGLE_EMAIL);
		
		//settings re single email per student
		Boolean singleEmailEnabled = (Boolean) evalSettings.get(EvalSettings.ENABLE_SINGLE_EMAIL);
		if (singleEmailEnabled != null && singleEmailEnabled.booleanValue()) {
			UIBranchContainer oneemail = UIBranchContainer.make(form,
					"one-email-settings:");
			AdministrateProducer.makeSelect(oneemail, "log-progress-every",
					EvalToolConstants.PULLDOWN_BATCH_VALUES,
					EvalToolConstants.PULLDOWN_BATCH_VALUES,
					EvalSettings.EMAIL_BATCH_SIZE, false);
			AdministrateProducer.makeSelect(oneemail, "email-batch-size",
					EvalToolConstants.PULLDOWN_BATCH_VALUES,
					EvalToolConstants.PULLDOWN_BATCH_VALUES,
					EvalSettings.EMAIL_BATCH_SIZE, false);
			AdministrateProducer.makeSelect(oneemail, "wait-interval",
					EvalToolConstants.PULLDOWN_BATCH_VALUES,
					EvalToolConstants.PULLDOWN_BATCH_VALUES,
					EvalSettings.EMAIL_WAIT_INTERVAL, false);
			AdministrateProducer.makeSelect(oneemail, "send-reminders",
					EvalToolConstants.REMINDER_EMAIL_DAYS_VALUES,
					EvalToolConstants.REMINDER_EMAIL_DAYS_LABELS,
					EvalSettings.REMINDER_INTERVAL_DAYS, true);
			
			Date nextReminderDate = (Date)evalSettings.get(EvalSettings.NEXT_REMINDER_DATE);
			UIMessage.make(tofill, "current_date", "evalsettings.dates.current", 
					new Object[] { dateFormat.format(nextReminderDate), timeFormat.format(nextReminderDate) });
			
			//String evaluationOTP = "evaluationBeanLocator." + evalViewParams.evaluationId + ".";
			
			String binding = "settingsBean";

			/*
			 * 	<div rsf:id="next-reminder-settings:" class="shorttext">
					<span  rsf:id="showNextReminderDate:">
						<input type="text" id="reminder_date_dummy" name="reminder_date_dummy" value="MM/DD/YYYY" size="12" maxlength="10"/>
						<img src="../images/calendar.gif" />
					</span>
				</div>
			 */

			
			// Next Reminder Date
			UIBranchContainer showNextReminderDate = UIBranchContainer.make(form, "next-reminder-settings:");
			generateNextReminderDateSelector(showNextReminderDate, "nextReminderDate", binding, 
					nextReminderDate, useDateTime);
		}

		//dispose of email by sending to email system, log, or dev/null
		AdministrateProducer.makeSelect(form, "delivery-option",
				EvalToolConstants.EMAIL_DELIVERY_VALUES,
				EvalToolConstants.EMAIL_DELIVERY_LABELS,
				EvalSettings.EMAIL_DELIVERY_OPTION, false);

		//log email To: addresses
		AdministrateProducer.makeBoolean(form, "log-recipients",
				EvalSettings.LOG_EMAIL_RECIPIENTS);
		
	      // Save Settings button
	      UICommand.make(form, "saveSettings",UIMessage.make("administrate.save.settings.button"), null);  
	}
	
	/**
	 * Generates the date picker control for next reminder date
	 * 
	 * @param parent the parent container
	 * @param rsfId the rsf id of the checkbox
	 * @param binding the EL binding for this control value
	 * @param initValue null or an initial date value
	 * @param useDateTime
	 */
	private void generateNextReminderDateSelector(UIBranchContainer parent, String rsfId, String binding,
			Date initValue, boolean useDateTime) {
		
		/*
		 * UIInput datePicker = UIInput.make(parent, rsfId + ":", binding);
         if (useDateTime) {
            dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);         
         } else {
            dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_INPUT);        
         }
         dateevolver.evolveDateInput(datePicker, initValue);
		 */
		
		UIInput datePicker = UIInput.make(parent, rsfId + ":", binding);
		if (useDateTime) {
			dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);         
		} else {
			dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_INPUT);        
		}
		dateevolver.evolveDateInput(datePicker, initValue);
	}
	
	/* Missing UIOutput
	 *   private void generateDateSelector(UIBranchContainer parent, String rsfId, String binding, 
 Date initValue, String currentEvalState, String worksUntilState, boolean useDateTime) {
if ( EvalUtils.checkStateAfter(currentEvalState, worksUntilState, true) ) {
 String suffix = ".date";
 if (useDateTime) {
    suffix = ".time";
 }
 UIOutput.make(parent, rsfId + "_disabled", null, binding)
    .resolver = new ELReference("dateResolver." + suffix);
} else {
 UIInput datePicker = UIInput.make(parent, rsfId + ":", binding);
 if (useDateTime) {
    dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);         
 } else {
    dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_INPUT);        
 }
 dateevolver.evolveDateInput(datePicker, initValue);
}
}
	 */
}

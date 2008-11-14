package org.sakaiproject.evaluation.tool.producers;

import java.util.Date;
import java.util.Locale;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.tool.EvalToolConstants;

import uk.org.ponder.beanutil.PathUtil;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIVerbatim;
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
	
	// Used to prepare the path for WritableBeanLocator
	public static final String ADMIN_WBL = "settingsBean";

	public String getViewID() {
		return VIEW_ID;
	}
	// injection
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer,
	 *      uk.org.ponder.rsf.viewstate.ViewParameters,
	 *      uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {

		String currentUserId = commonLogic.getCurrentUserId();
		boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

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
		
		/*
		 * one email per course evaluation (default) OR one email per student
		 * having one or more evaluations for which no response has been
		 * submitted (UM)
		 */
		UIMessage.make(form, "controlemail-instructions",
				"controlemail.instructions");
		AdministrateProducer.makeBoolean(form, "emailtype",
				EvalSettings.ENABLE_SINGLE_EMAIL);
		UIBranchContainer oneemail = UIBranchContainer.make(form,
				"one-email-settings:");
		AdministrateProducer.makeSelect(oneemail, "log-progress-every",
				EvalToolConstants.PULLDOWN_BATCH_VALUES,
				EvalToolConstants.PULLDOWN_BATCH_VALUES,
				EvalSettings.LOG_PROGRESS_EVERY, false);
		//(Control send task polling interval)
		AdministrateProducer.makeSelect(oneemail, "check-queue-every",
				EvalToolConstants.PULLDOWN_INTEGER_VALUES,
				EvalToolConstants.PULLDOWN_INTEGER_VALUES,
				EvalSettings.EMAIL_SEND_QUEUED_REPEAT_INTERVAL, false);
		//(Control send task polling start delay)
		AdministrateProducer.makeSelect(oneemail, "check-queue-start",
				EvalToolConstants.PULLDOWN_INTEGER_VALUES,
				EvalToolConstants.PULLDOWN_INTEGER_VALUES,
				EvalSettings.EMAIL_SEND_QUEUED_START_INTERVAL, false);
		//(Throttle while sending)
		AdministrateProducer.makeSelect(oneemail, "email-batch-size",
				EvalToolConstants.PULLDOWN_BATCH_VALUES,
				EvalToolConstants.PULLDOWN_BATCH_VALUES,
				EvalSettings.EMAIL_BATCH_SIZE, false);
		//(Influences batch size)
		AdministrateProducer.makeSelect(oneemail, "email-locks-size",
				EvalToolConstants.PULLDOWN_BATCH_VALUES,
				EvalToolConstants.PULLDOWN_BATCH_VALUES,
				EvalSettings.EMAIL_LOCKS_SIZE, false);
		//(Throttle while sending)
		AdministrateProducer.makeSelect(oneemail, "wait-interval",
				EvalToolConstants.PULLDOWN_BATCH_VALUES,
				EvalToolConstants.PULLDOWN_BATCH_VALUES,
				EvalSettings.EMAIL_WAIT_INTERVAL, false);
		AdministrateProducer.makeSelect(oneemail, "send-reminders",
				EvalToolConstants.REMINDER_EMAIL_DAYS_VALUES,
				EvalToolConstants.REMINDER_EMAIL_DAYS_LABELS,
				EvalSettings.REMINDER_INTERVAL_DAYS, true);

		UIMessage.make(form, "next-reminder-date-instructions",
				"controlemail.next.reminder.date.instructions");
		UIVerbatim.make(tofill, "reminder_date_instruction", UIMessage
				.make("controlemail.next.reminder.date.instructions"));
		
		String reminderOTP = "settingsBean.";

		// next reminder date
		Date nextReminder = (Date) evalSettings
				.get(EvalSettings.NEXT_REMINDER_DATE);
		UIBranchContainer date = UIBranchContainer.make(form,
				"next-reminder-date:");
		generateReminderDateSelector(date, "show-next-reminder-date", true, nextReminder);

		// dispose of email by sending to email system, log, or dev/null
		AdministrateProducer.makeSelect(form, "delivery-option",
				EvalToolConstants.EMAIL_DELIVERY_VALUES,
				EvalToolConstants.EMAIL_DELIVERY_LABELS,
				EvalSettings.EMAIL_DELIVERY_OPTION, false);

		// log email To: addresses
		AdministrateProducer.makeBoolean(form, "log-recipients",
				EvalSettings.LOG_EMAIL_RECIPIENTS);

		// Save Settings button
		UICommand.make(form, "saveSettings", UIMessage
				.make("administrate.save.settings.button"), null);
	}// fillComponents

	/**
	 * Generates the date picker control for next reminder date
	 * 
	 * @param parent
	 *            the parent container
	 * @param rsfId
	 *            the rsf id of the evolver
	 * @param useDateTime
	 * 			  true to use both date and time, false to use date only
	 * @param initValue
	 *            null or an initial date value
	 */
	private void generateReminderDateSelector(UIBranchContainer parent, String rsfId,
			boolean useDateTime, Date initValue) {
		
		String binding = PathUtil.composePath(ADMIN_WBL, EvalSettings.NEXT_REMINDER_DATE);
		UIInput datePicker = UIInput.make(parent, rsfId + ":", binding);
		if (useDateTime) {
			dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);
		} else {
			dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_INPUT);
		}
		dateevolver.evolveDateInput(datePicker, initValue);
	}
}

package org.sakaiproject.evaluation.tool.producers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;

import uk.org.ponder.beanutil.PathUtil;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInitBlock;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class AdministrateEmailProducer implements ViewComponentProducer {

    /**
     * This is used for navigation within the system.
     */
    public static final String VIEW_ID = "administrate_email";
    public String getViewID() {
        return VIEW_ID;
    }

	private static final int REMINDER_FREQUENCY_LIMIT = 8;

	public static final String EMAIL_SETTINGS_WBL = "emailSettingsBean";
	
    // Spring injection 
    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }
    
    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
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

        if (!userAdmin) {
            // Security check and denial
            throw new SecurityException(
            "Non-admin users may not access this page");
        }

        navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

        Boolean useConsolidatedNotifications = (Boolean) evalSettings.get(EvalSettings.ENABLE_SINGLE_EMAIL_PER_STUDENT);
        if(useConsolidatedNotifications == null) {
        	useConsolidatedNotifications = new Boolean(false);
        }


        UIForm emailForm = UIForm.make(tofill, "emailcontrol-form");
        
		String[] options = new String[]{Boolean.toString(false), Boolean.toString(true)};
        String[] labels = new String[]{"controlemail.individual.emails", "controlemail.consolidated.emails"};
        UISelect emailTypeChoice = UISelect.make(emailForm, "email-type-choice", options, labels, 
        		PathUtil.composePath(EMAIL_SETTINGS_WBL, EvalSettings.ENABLE_SINGLE_EMAIL_PER_STUDENT),
        		Boolean.toString(useConsolidatedNotifications)).setMessageKeys();
        //AdministrateProducer.makeSelect(emailForm, "email-type-choice", options, labels, EvalSettings.ENABLE_SINGLE_EMAIL_PER_STUDENT, true);
        UISelectChoice.make(emailForm, "email-type-choice-individual", emailTypeChoice.getFullID(), 0);
        UISelectLabel.make(emailForm, "email-type-choice-individual-label", emailTypeChoice.getFullID(), 0);

        AdministrateProducer.makeSelect(emailForm, "default-reminders-frequency-selection",
                EvalToolConstants.REMINDER_EMAIL_DAYS_VALUES,
                EvalToolConstants.REMINDER_EMAIL_DAYS_LABELS,
                EMAIL_SETTINGS_WBL, EvalSettings.DEFAULT_EMAIL_REMINDER_FREQUENCY, true); 
        
		/* enable an email to be sent to the admin/helpdesk address when a email job has completed. */
        AdministrateProducer.makeBoolean(emailForm, "enable-job-completion-email", EMAIL_SETTINGS_WBL, EvalSettings.ENABLE_JOB_COMPLETION_EMAIL);
        
		/* enable the updating of reminder status while remiders are running. */
        AdministrateProducer.makeBoolean(emailForm, "enable-reminder-status", EMAIL_SETTINGS_WBL, EvalSettings.ENABLE_REMINDER_STATUS);       

        UIInput evalTimeToWaitSecs = AdministrateProducer.makeInput(emailForm, "eval-time-to-wait-secs", EMAIL_SETTINGS_WBL, EvalSettings.EVALUATION_TIME_TO_WAIT_SECS);

        //allow eval begin email notification - eval specific toggle 
        AdministrateProducer.makeBoolean(emailForm, "allow-eval-begin-email",
        		EMAIL_SETTINGS_WBL, EvalSettings.ALLOW_EVALSPECIFIC_TOGGLE_EMAIL_NOTIFICATION);
        
        // control options for consolidated emails
        
        String nextReminderStr = (String) evalSettings.get(EvalSettings.NEXT_REMINDER_DATE);
        
		Date nextReminder = null;
		if(nextReminderStr == null || nextReminderStr.trim().equals("")) {
			nextReminder = new Date();
		} else {
	        DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy"); //DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL);
			try {
				nextReminder = df.parse( nextReminderStr );
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				nextReminder = new Date();
			}
		}

        UISelectChoice.make(emailForm, "email-type-choice-consolidated", emailTypeChoice.getFullID(), 1);
        UISelectLabel.make(emailForm, "email-type-choice-consolidated-label", emailTypeChoice.getFullID(), 1);

		UIBranchContainer oneemail = UIBranchContainer.make(emailForm, "consolidated-email-settings:");
		
		AdministrateProducer.makeBoolean(oneemail, "consolidated-send-available", EMAIL_SETTINGS_WBL, EvalSettings.CONSOLIDATED_EMAIL_NOTIFY_AVAILABLE);

        AdministrateProducer.makeSelect(emailForm, "reminders-frequency-selection",
                EvalToolConstants.REMINDER_EMAIL_DAYS_VALUES,
                EvalToolConstants.REMINDER_EMAIL_DAYS_LABELS,
                EMAIL_SETTINGS_WBL, EvalSettings.SINGLE_EMAIL_REMINDER_DAYS, true); 
		
        AdministrateProducer.makeSelect(oneemail, "consolidated-job-start-time", 
        		EvalToolConstants.PULLDOWN_HOUR_VALUES, 
        		EvalToolConstants.PULLDOWN_HOUR_LABELS, 
        		EMAIL_SETTINGS_WBL, EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_TIME, true);
        
        AdministrateProducer.makeSelect(oneemail, "consolidated-job-start-time-minutes", 
        		EvalToolConstants.PULLDOWN_MINUTE_VALUES, 
        		EvalToolConstants.PULLDOWN_MINUTE_LABELS, 
        		EMAIL_SETTINGS_WBL, EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_MINUTES, true);

		UIBranchContainer nextReminderDiv = UIBranchContainer.make(oneemail, "nextReminderDateDiv:");
        generateDatePicker(nextReminderDiv,"nextReminderDate",EvalSettings.NEXT_REMINDER_DATE, nextReminder);
        
            AdministrateProducer.makeSelect(oneemail, "log-progress-every",
                    EvalToolConstants.PULLDOWN_BATCH_VALUES,
                    EvalToolConstants.PULLDOWN_BATCH_VALUES,
		          EMAIL_SETTINGS_WBL, EvalSettings.LOG_PROGRESS_EVERY, false);
            AdministrateProducer.makeSelect(oneemail, "email-batch-size",
                    EvalToolConstants.PULLDOWN_BATCH_VALUES,
                    EvalToolConstants.PULLDOWN_BATCH_VALUES,
		          EMAIL_SETTINGS_WBL, EvalSettings.EMAIL_BATCH_SIZE, false);
            AdministrateProducer.makeSelect(oneemail, "wait-interval",
                    EvalToolConstants.PULLDOWN_BATCH_VALUES,
                    EvalToolConstants.PULLDOWN_BATCH_VALUES,
		          EMAIL_SETTINGS_WBL, EvalSettings.EMAIL_WAIT_INTERVAL, false);
            AdministrateProducer.makeSelect(oneemail, "send-reminders",
                    EvalToolConstants.REMINDER_EMAIL_DAYS_VALUES,
                    EvalToolConstants.REMINDER_EMAIL_DAYS_LABELS,
				  EMAIL_SETTINGS_WBL, EvalSettings.SINGLE_EMAIL_REMINDER_DAYS, true);         

       // control the general email options
        AdministrateProducer.makeBoolean(emailForm, "general-use-admin-from-email", EMAIL_SETTINGS_WBL, EvalSettings.USE_ADMIN_AS_FROM_EMAIL);
        AdministrateProducer.makeInput(emailForm, "general-helpdesk-email", EMAIL_SETTINGS_WBL, EvalSettings.FROM_EMAIL_ADDRESS);

        
        //dispose of email by sending to email system, log, or dev/null
        AdministrateProducer.makeSelect(emailForm, "delivery-option",
                EvalToolConstants.EMAIL_DELIVERY_VALUES,
                EvalToolConstants.EMAIL_DELIVERY_LABELS,
                EMAIL_SETTINGS_WBL, EvalSettings.EMAIL_DELIVERY_OPTION, false);


//
//        //log email To: addresses
//        AdministrateProducer.makeBoolean(form, "log-recipients",
//                EvalSettings.LOG_EMAIL_RECIPIENTS);
//        
        // Save Settings button
        UICommand.make(emailForm, "saveSettings", UIMessage.make("administrate.save.settings.button"), PathUtil.buildPath(EMAIL_SETTINGS_WBL, "saveSettings"));
        
        // this fills in the javascript call
        UIInitBlock.make(tofill, "initEvalJS", "EvalSystem.addNumericOnly", 
                new Object[] { evalTimeToWaitSecs.getFullID(), "time-wait-errmsg"} );
    }
    
    
    /**
     * @param parent
     * @param rsfId
     * @param adminkey
     * @param initValue
     */
    private void generateDatePicker(UIBranchContainer parent, String rsfId, String adminkey, Date initValue) {
        UIInput datePicker = UIInput.make(parent, rsfId + ":", PathUtil.composePath(EMAIL_SETTINGS_WBL, adminkey));
        dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);
        dateevolver.evolveDateInput(datePicker, initValue);
}

}

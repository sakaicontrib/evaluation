package org.sakaiproject.evaluation.tool.producers;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInitBlock;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class AdministrateEmailProducer implements ViewComponentProducer {

    /**
     * This is used for navigation within the system.
     */
    public static final String VIEW_ID = "administrate_email";
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
    
    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
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

        UIForm form = UIForm.make(tofill, "emailcontrol-form");

        // control the general email options
        AdministrateProducer.makeInput(form, "general-helpdesk-email", EvalSettings.FROM_EMAIL_ADDRESS);
        AdministrateProducer.makeBoolean(form, "general-use-admin-from-email", EvalSettings.USE_ADMIN_AS_FROM_EMAIL);
        AdministrateProducer.makeSelect(form, "default-reminders-frequency",
                EvalToolConstants.REMINDER_EMAIL_DAYS_VALUES,
                EvalToolConstants.REMINDER_EMAIL_DAYS_LABELS,
                EvalSettings.DEFAULT_EMAIL_REMINDER_FREQUENCY, true); 
        UIInput evalTimeToWaitSecs = AdministrateProducer.makeInput(form, "eval-time-to-wait-secs", EvalSettings.EVALUATION_TIME_TO_WAIT_SECS);
        
		/* enable an email to be sent to the admin/helpdesk address when a email job has completed. */
        AdministrateProducer.makeBoolean(form, "enable-job-completion-email", EvalSettings.ENABLE_JOB_COMPLETION_EMAIL);
        
		/* enable the updating of reminder status while remiders are running. */
        AdministrateProducer.makeBoolean(form, "enable-reminder-status", EvalSettings.ENABLE_REMINDER_STATUS);       

        /* one email per course evaluation (default) OR one email per student 
         * having one or more evaluations for which no response has been submitted (UM) */
        AdministrateProducer.makeBoolean(form, "emailtype", EvalSettings.ENABLE_SINGLE_EMAIL_PER_STUDENT);

        //settings re one email per student
        Boolean student = (Boolean) evalSettings.get(EvalSettings.ENABLE_SINGLE_EMAIL_PER_STUDENT);
        if (student != null && student) {
            UIBranchContainer oneemail = UIBranchContainer.make(form, "one-email-settings:");
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
                    EvalSettings.SINGLE_EMAIL_REMINDER_DAYS, true); 
        }

        //dispose of email by sending to email system, log, or dev/null
        AdministrateProducer.makeSelect(form, "delivery-option",
                EvalToolConstants.EMAIL_DELIVERY_VALUES,
                EvalToolConstants.EMAIL_DELIVERY_LABELS,
                EvalSettings.EMAIL_DELIVERY_OPTION, false);

        //log email To: addresses
        AdministrateProducer.makeBoolean(form, "log-recipients",
                EvalSettings.LOG_EMAIL_RECIPIENTS);
        
        //allow eval begin email notification - eval specific toggle 
        AdministrateProducer.makeBoolean(form, "allow-eval-begin-email",
        		EvalSettings.ALLOW_EVALSPECIFIC_TOGGLE_EMAIL_NOTIFICATION);

        // Save Settings button
        UICommand.make(form, "saveSettings",UIMessage.make("administrate.save.settings.button"), null);


        // this fills in the javascript call
        UIInitBlock.make(tofill, "initEvalJS", "EvalSystem.addNumericOnly", 
                new Object[] { evalTimeToWaitSecs.getFullID(), "time-wait-errmsg"} );
    }
}

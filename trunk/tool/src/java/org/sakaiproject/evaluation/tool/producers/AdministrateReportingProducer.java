package org.sakaiproject.evaluation.tool.producers;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;

import uk.org.ponder.beanutil.PathUtil;
import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBoundList;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class AdministrateReportingProducer implements ViewComponentProducer {
    public static final String VIEW_ID = "administrate_reporting";
    
    private EvalExternalLogic external;
    public void setExternal(EvalExternalLogic external) {
        this.external = external;
    }
    
    public String getViewID() {
        return VIEW_ID;
    }

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        String currentUserId = external.getCurrentUserId();
        boolean userAdmin = external.isUserAdmin(currentUserId);

        if (!userAdmin) {
            // Security check and denial
            throw new SecurityException("Non-admin users may not access this page");
        }
        
        // Breadcrumbs
        UIInternalLink.make(tofill, "summary-link", UIMessage.make("summary.page.title"),
                new SimpleViewParameters(SummaryProducer.VIEW_ID));
        UIInternalLink.make(tofill, "administrate-link", UIMessage.make("administrate.page.title"),
                new SimpleViewParameters(AdministrateProducer.VIEW_ID));
        UIMessage.make(tofill, "page-title", "controlreporting.breadcrumb.title");
        
        UIForm form = UIForm.make(tofill, "settings-form");
        
        // Allow CSV Export
        makeBoolean(form, "allow-csv-export", EvalSettings.ENABLE_CSV_REPORT_EXPORT);
        UIMessage.make(form, "allow-csv-export-note", "controlreporting.enable.csv.label");
        
        // Allow XLS Export
        makeBoolean(form, "allow-xls-export", EvalSettings.ENABLE_XLS_REPORT_EXPORT);
        UIMessage.make(form, "allow-xls-export-note", "controlreporting.enable.xls.label");
        
        // Allow PDF Export
        makeBoolean(form, "allow-pdf-export", EvalSettings.ENABLE_PDF_REPORT_EXPORT);
        UIMessage.make(form, "allow-pdf-export-note", "controlreporting.enable.pdf.label");
        
        // Enable PDF Banner
        makeBoolean(form, "include-pdf-banner-image", EvalSettings.ENABLE_PDF_REPORT_BANNER);
        UIMessage.make(form, "include-pdf-banner-image-note", "controlreporting.enable.pdfbanner.label");
        
        // Set the location in Resources of the PDF Banner
        makeInput(form, "pdf-banner-image-location", EvalSettings.PDF_BANNER_IMAGE_LOCATION);
        UIMessage.make(form, "pdf-banner-image-location-note", "controlreporting.pdfbanner.location.label");
        
        UICommand.make(form, "saveSettings",UIMessage.make("controlreporting.save"), null);
    }

    
    
    // BAD BAD BAD BAD Duplicated from AdministrateProducer. Factor out after merging SAK Branch.
    
 // Used to prepare the path for WritableBeanLocator
    private String ADMIN_WBL = "settingsBean";
    
    /*
     * (non-Javadoc)
     * This method is used to render checkboxes.
     */
    private void makeBoolean(UIContainer parent, String ID, String adminkey) {
       // Must use "composePath" here since admin keys currently contain periods
       UIBoundBoolean.make(parent, ID, adminkey == null? null : PathUtil.composePath(ADMIN_WBL, adminkey)); 
    }

    /*
     * (non-Javadoc)
     * This is a common method used to render dropdowns (select boxes).
     */
    private void makeSelect(UIContainer parent, String ID, String[] values, String[] labels, String adminkey, boolean message) {
       UISelect selection = UISelect.make(parent, ID); 
       selection.selection = new UIInput();
       if (adminkey != null) {
          selection.selection.valuebinding = new ELReference(PathUtil.composePath(ADMIN_WBL, adminkey));
       }
       UIBoundList selectvalues = new UIBoundList();
       selectvalues.setValue(values);
       selection.optionlist = selectvalues;
       UIBoundList selectlabels = new UIBoundList();
       selectlabels.setValue(labels);
       selection.optionnames = selectlabels;   

       if (message)
          selection.setMessageKeys();
    }

    /*
     * (non-Javadoc)
     * This is a common method used to render text boxes.
     */
    private void makeInput(UIContainer parent, String ID, String adminkey) {
       UIInput.make(parent, ID, PathUtil.composePath(ADMIN_WBL, adminkey));
    }
}

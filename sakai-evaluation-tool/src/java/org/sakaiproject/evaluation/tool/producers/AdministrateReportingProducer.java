/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.tool.producers;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class AdministrateReportingProducer extends EvalCommonProducer {
    public static final String VIEW_ID = "administrate_reporting";
    
    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
        this.navBarRenderer = navBarRenderer;
    }

    public String getViewID() {
        return VIEW_ID;
    }

    public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        if (!userAdmin) {
            // Security check and denial
            throw new SecurityException("Non-admin users may not access this page");
        }

        navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

        // Breadcrumbs
        UIInternalLink.make(tofill, "summary-link", UIMessage.make("summary.page.title"),
                new SimpleViewParameters(SummaryProducer.VIEW_ID));
        UIInternalLink.make(tofill, "administrate-link", UIMessage.make("administrate.page.title"),
                new SimpleViewParameters(AdministrateProducer.VIEW_ID));

        UIMessage.make(tofill, "page-title", "controlreporting.breadcrumb.title");
        
        UIForm form = UIForm.make(tofill, "settings-form");
        
        // Allow CSV Export
        AdministrateProducer.makeBoolean(form, "allow-csv-export", AdministrateProducer.ADMIN_WBL, EvalSettings.ENABLE_CSV_REPORT_EXPORT);
        UIMessage.make(form, "allow-csv-export-note", "controlreporting.enable.csv.label");
        
        // Allow XLS Export
        AdministrateProducer.makeBoolean(form, "allow-xls-export", AdministrateProducer.ADMIN_WBL, EvalSettings.ENABLE_XLS_REPORT_EXPORT);
        UIMessage.make(form, "allow-xls-export-note", "controlreporting.enable.xls.label");
        
        // Allow PDF Export
        AdministrateProducer.makeBoolean(form, "allow-pdf-export", AdministrateProducer.ADMIN_WBL, EvalSettings.ENABLE_PDF_REPORT_EXPORT);
        UIMessage.make(form, "allow-pdf-export-note", "controlreporting.enable.pdf.label");
        
        // Enable PDF Banner
        AdministrateProducer.makeBoolean(form, "include-pdf-banner-image", AdministrateProducer.ADMIN_WBL, EvalSettings.ENABLE_PDF_REPORT_BANNER);
        UIMessage.make(form, "include-pdf-banner-image-note", "controlreporting.enable.pdfbanner.label");
        
        // Set the location in Resources of the PDF Banner
        AdministrateProducer.makeInput(form, "pdf-banner-image-location", AdministrateProducer.ADMIN_WBL, EvalSettings.PDF_BANNER_IMAGE_LOCATION);
        UIMessage.make(form, "pdf-banner-image-location-note", "controlreporting.pdfbanner.location.label");
        
        // Allow CSV Export
        AdministrateProducer.makeBoolean(form, "allow-list-of-takers-export", AdministrateProducer.ADMIN_WBL, EvalSettings.ENABLE_LIST_OF_TAKERS_EXPORT);
        UIMessage.make(form, "allow-list-of-takers-export-note", "controlreporting.enable.list.of.takers.label");

        
        UICommand.make(form, "saveSettings",UIMessage.make("controlreporting.save"), null);
    }
}

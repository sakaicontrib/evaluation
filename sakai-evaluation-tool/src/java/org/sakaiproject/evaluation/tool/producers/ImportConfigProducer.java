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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * ImportConfigProducer handles the server side operations to ready the import_config page.
 * @author chasegawa
 */
public class ImportConfigProducer extends EvalCommonProducer implements NavigationCaseReporter {
    /**
     * @see org.sakaiproject.evaluation.tool.producers.EvalCommonProducer#fill(uk.org.ponder.rsf.components.UIContainer,
     *      uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    @Override
    public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        boolean userAdmin = commonLogic.isUserAdmin(commonLogic.getCurrentUserId());
        if (!userAdmin) {
            throw new SecurityException("Non-admin users may not access this page");
        }

        navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

        UIForm uploadform = UIForm.make(tofill, "upload-form");
        UICommand.make(uploadform, "upload-button", UIMessage.make("importconfig.upload.button"), "propertiesFileParser.parse");
        setCurrentSettingsForDisplay(tofill);

        // Only add the commit button if the user has uploaded settings.
        if (!uploadedConfigValues.keySet().isEmpty()) {
            UIForm overwriteForm = UIForm.make(tofill, "overwrite-form");
            UICommand.make(overwriteForm, "overwrite-button", UIMessage.make("importconfig.overwrite.config.button"), "overwriteSettingsHandler.saveOverwriteSettings");
        }

        // Clean the uploaded settings out
        uploadedConfigValues = new HashMap<>();
    }

    public static final String VIEW_ID = "import_config";
    @Override
    public String getViewID() {
        return VIEW_ID;
    }

    /**
     * @return 
     * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List reportNavigationCases() {
        List i = new ArrayList();
        i.add(new NavigationCase("uploadSucces", new SimpleViewParameters(VIEW_ID)));
        i.add(new NavigationCase("uploadFailure", new SimpleViewParameters(AdministrateProducer.VIEW_ID)));
        i.add(new NavigationCase("overwriteSuccess", new SimpleViewParameters(AdministrateProducer.VIEW_ID)));
        return i;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    /**
     * Fill the current parameter names and values into the UIContainer.
     */
    private void setCurrentSettingsForDisplay(UIContainer tofill) {
        Field[] evalSettingFields = EvalSettings.class.getFields();
        Arrays.sort(evalSettingFields, (Field o1, Field o2) -> o1.getName().compareTo(o2.getName()));
        for (Field field : evalSettingFields) {
            // Ignore the arrays of String and just get the String constants
            if (String.class.equals(field.getType())) {
                // Create data for new <tr> element
                UIBranchContainer row = UIBranchContainer.make(tofill, "settings:");
                String propertyName = "";
                try {
                    propertyName = EvalSettings.class.getDeclaredField(field.getName()).get(String.class).toString();
                } catch (IllegalArgumentException | SecurityException | IllegalAccessException | NoSuchFieldException e) {
                }
                UIOutput.make(row, "propertyName", propertyName);
                Object settingValue = evalSettings.get(propertyName);
                UIOutput.make(row, "currentValue", null == settingValue ? "null" : settingValue.toString());
                String incomingValue = uploadedConfigValues.get(propertyName);
                UIOutput.make(row, "incomingValue", null == incomingValue ? "" : incomingValue);
            }
        }
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }

    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
        this.navBarRenderer = navBarRenderer;
    }

    private static HashMap<String, String> uploadedConfigValues = new HashMap<>();
    public void setUploadedConfigValues(HashMap<String, String> hashMap) {
        ImportConfigProducer.uploadedConfigValues = hashMap;
    }
}

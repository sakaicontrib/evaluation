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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.util.ResourceLoader;

import org.sakaiproject.rsf.helper.HelperViewParameters;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * AssignPermissionsProducer handles the server side operations to ready the permissions helper
 * 
 * @author chasegawa
 */
public class AssignPermissionsProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {
   /**
     * @param tofill
     * @param viewparams
     * @param checker
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer,
    *      uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
      ToolSession session = sessionManager.getCurrentToolSession();
      session.setAttribute(PermissionsHelper.TARGET_REF, site.getReference());
      session.setAttribute(PermissionsHelper.PREFIX, "eval.");

      ResourceLoader resourceLoader = new ResourceLoader("org.sakaiproject.evaluation.tool.bundle.permissions");
      HashMap<String, String> permissionsDescriptions = new HashMap<>();
      for (Object key : resourceLoader.keySet()) {
         permissionsDescriptions.put(key.toString(), (String) resourceLoader.get(key));
      }
      session.setAttribute("permissionDescriptions", permissionsDescriptions);

      UIOutput.make(tofill, HelperViewParameters.HELPER_ID, "sakai.permissions.helper");
      UICommand.make(tofill, HelperViewParameters.POST_HELPER_BINDING, "", null);
   }

   public static final String VIEW_ID = "assign_permissions";
   public String getViewID() {
      return VIEW_ID;
   }

   public ViewParameters getViewParameters() {
      return new HelperViewParameters();
   }

   /**
     * @return 
    * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
    */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public List reportNavigationCases() {
      ArrayList result = new ArrayList();
      result.add(new NavigationCase(null, new SimpleViewParameters(SummaryProducer.VIEW_ID)));
      return result;
   }

   private SessionManager sessionManager;
   public void setSessionManager(SessionManager sessionManager) {
      this.sessionManager = sessionManager;
   }

   private Site site;
   public void setSite(Site site) {
      this.site = site;
   }

}
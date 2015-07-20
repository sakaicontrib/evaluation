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
package org.sakaiproject.evaluation.tool.renderers;

import org.sakaiproject.evaluation.tool.viewparams.ItemViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateItemViewParameters;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewStateHandler;

/**
 * This handles the rendering of add item controls
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class AddItemControlRenderer {

   /**
    * This identifies the template component associated with this renderer
    */
   public static final String COMPONENT_ID = "render_add_item_control:";

   private ViewStateHandler viewStateHandler;
   public void setViewStateHandler(ViewStateHandler viewStateHandler) {
      this.viewStateHandler = viewStateHandler;
   }

   public UIJointContainer renderControl(UIContainer parent, String ID, ViewParameters[] viewParams, String[] labels, UIMessage addButtonLabel, Long templateId) {

      UIJointContainer container = new UIJointContainer(parent, ID, COMPONENT_ID);

      String[] values = convertVPs(viewParams, templateId);

      UIForm form = UIForm.make(container, "add-item-form");
      UISelect.make(form, "add-item-classification", values, labels, values[0], false).setMessageKeys();
      UICommand.make(form, "add-item-button", addButtonLabel);

      return container;
   }

   private String[] convertVPs(ViewParameters[] VPs, Long templateId) {
      String[] togo = new String[VPs.length];
      for (int i = 0; i < VPs.length; ++i) {
         if (VPs[i] instanceof ItemViewParameters) {
            ((ItemViewParameters) VPs[i]).templateId = templateId;
         } else if (VPs[i] instanceof TemplateItemViewParameters) {
            ((TemplateItemViewParameters) VPs[i]).templateId = templateId;            
         }
         togo[i] = viewStateHandler.getFullURL(VPs[i]);
      }
      return togo;
   }

//   private String[] convertViews(String[] viewIDs, Long templateId) {
//      String[] togo = new String[viewIDs.length];
//      for (int i = 0; i < viewIDs.length; ++i) {
//         togo[i] = viewStateHandler.getFullURL(deriveTarget(viewIDs[i], templateId));
//      }
//      return togo;
//   }
//
//   private ViewParameters deriveTarget(String viewID, Long templateId) {
//      return new TemplateItemViewParameters(viewID, templateId, null);
//   }

}

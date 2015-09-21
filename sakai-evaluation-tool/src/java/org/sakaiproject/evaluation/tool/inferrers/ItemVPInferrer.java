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
package org.sakaiproject.evaluation.tool.inferrers;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.evaluation.logic.entity.ItemEntityProvider;
import org.sakaiproject.evaluation.logic.entity.TemplateItemEntityProvider;
import org.sakaiproject.evaluation.tool.producers.PreviewItemProducer;
import org.sakaiproject.evaluation.tool.viewparams.ItemViewParameters;

import org.sakaiproject.rsf.entitybroker.EntityViewParamsInferrer;
import uk.org.ponder.rsf.viewstate.ViewParameters;


/**
 * Handles the viewing/previewing of items and template items
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net)
 */
public class ItemVPInferrer implements EntityViewParamsInferrer {

   /* (non-Javadoc)
    * @see org.sakaiproject.rsf.entitybroker.EntityViewParamsInferrer#getHandledPrefixes()
    */
   public String[] getHandledPrefixes() {
      return new String[] {ItemEntityProvider.ENTITY_PREFIX, TemplateItemEntityProvider.ENTITY_PREFIX};
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.rsf.entitybroker.EntityViewParamsInferrer#inferDefaultViewParameters(java.lang.String)
    */
   public ViewParameters inferDefaultViewParameters(String reference) {
      EntityReference ep = new EntityReference(reference);
      Long itemId = null;
      Long templateItemId = null;
      if (ItemEntityProvider.ENTITY_PREFIX.equals(ep.getPrefix())) {
         itemId = new Long(ep.getId()); 
      } else {
         templateItemId = new Long(ep.getId());
      }
      // MAYBE add in restriction for access to item preview later? -AZ
//    EvalItem item = authoringService.getTemplateById(itemId); 
//    if (EvalConstants.SHARING_PUBLIC.equals(item.getSharing()) ||
//          Boolean.TRUE.equals(item.getExpert())) {
//    } else {
//       authoringService.canControlItem(userId, item);
//    }
      ItemViewParameters vp = new ItemViewParameters(PreviewItemProducer.VIEW_ID, itemId, templateItemId);
      vp.external = true;
      return vp;
   }

}

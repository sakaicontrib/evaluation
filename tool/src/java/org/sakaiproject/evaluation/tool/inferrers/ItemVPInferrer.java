/**
 * $Id$
 * $URL$
 * ItemVPInferrer.java - evaluation - Jan 31, 2008 2:18:37 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.inferrers;

import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.evaluation.logic.entity.ItemEntityProvider;
import org.sakaiproject.evaluation.logic.entity.TemplateItemEntityProvider;
import org.sakaiproject.evaluation.tool.producers.PreviewItemProducer;
import org.sakaiproject.evaluation.tool.viewparams.ItemViewParameters;

import uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer;
import uk.org.ponder.rsf.viewstate.ViewParameters;


/**
 * Handles the viewing/previewing of items and template items
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ItemVPInferrer implements EntityViewParamsInferrer {

   /* (non-Javadoc)
    * @see uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer#getHandledPrefixes()
    */
   public String[] getHandledPrefixes() {
      return new String[] {ItemEntityProvider.ENTITY_PREFIX, TemplateItemEntityProvider.ENTITY_PREFIX};
   }

   /* (non-Javadoc)
    * @see uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer#inferDefaultViewParameters(java.lang.String)
    */
   public ViewParameters inferDefaultViewParameters(String reference) {
      IdEntityReference ep = new IdEntityReference(reference);
      Long itemId = null;
      Long templateItemId = null;
      if (ItemEntityProvider.ENTITY_PREFIX.equals(ep.prefix)) {
         itemId = new Long(ep.id); 
      } else {
         templateItemId = new Long(ep.id);
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

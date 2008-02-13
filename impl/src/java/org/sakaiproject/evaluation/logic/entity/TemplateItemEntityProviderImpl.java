/**
 * $Id$
 * $URL$
 * TemplateItemEntityProviderImpl.java - evaluation - Jan 31, 2008 2:16:46 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.entity;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.entity.TemplateItemEntityProvider;


/**
 * Implementation for the entity provider for template items (questions in a template)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TemplateItemEntityProviderImpl implements TemplateItemEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider {

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
   }


   public String getEntityPrefix() {
      return ENTITY_PREFIX;
   }

   public boolean entityExists(String id) {
      boolean exists = false;
      Long templateItemId;
      try {
         templateItemId = new Long(id);
         if (authoringService.getTemplateItemById(templateItemId) != null) {
            exists = true;
         }
      } catch (NumberFormatException e) {
         // invalid number so roll through to the false
         exists = false;
      }
      return exists;
   }

}

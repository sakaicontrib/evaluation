/**
 * $Id$
 * $URL$
 * ComparatorsUtils.java - evaluation - Sep 09, 2007 12:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.utils;

import java.util.Comparator;

import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

/**
 * Utilities for sorting templateItem objects
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ComparatorsUtils {

   /**
    * Sort evaluationSetupService by due date, then by title, then by id
    */
   public static class EvaluationDateTitleIdComparator implements Comparator<EvalEvaluation> {
      public int compare(EvalEvaluation eval0, EvalEvaluation eval1) {
         int comparison = (eval0).getDueDate().compareTo((eval1).getDueDate());
         if (comparison == 0) {
            comparison = (eval0).getTitle().compareTo((eval1).getTitle());
         }
         if (comparison == 0) {
            comparison = (eval0).getId().compareTo((eval1).getId());
         }         
         return comparison;
      }
   }

   /**
    * static class to sort EvalTemplateItem objects by DisplayOrder
    */
   public static class TemplateItemComparatorByOrder implements Comparator<EvalTemplateItem>  {
      public int compare(EvalTemplateItem eti0, EvalTemplateItem eti1) {
         return eti0.getDisplayOrder().compareTo( eti1.getDisplayOrder() );
      }
   }

   /**
    * static class to sort EvalTemplateItem objects by Id
    */
   public static class TemplateItemComparatorById implements Comparator<EvalTemplateItem>  {
      public int compare(EvalTemplateItem eti0, EvalTemplateItem eti1) {
         return eti0.getId().compareTo( eti1.getId() );
      }
   }

   /**
    * static class to sort EvalItem objects by Id
    */
   public static class ItemComparatorById implements Comparator<EvalItem> {
      public int compare(EvalItem item0, EvalItem item1) {
         return item0.getId().compareTo( item1.getId() );
      }
   }

   /**
    * static class to sort EvalTemplateItem objects by DisplayOrder
    */
   public static class ItemGroupComparatorByTitle implements Comparator<EvalItemGroup> {
      public int compare(EvalItemGroup ig0, EvalItemGroup ig1) {
         return ig0.getTitle().compareTo( ig1.getTitle() );
      }
   }

}

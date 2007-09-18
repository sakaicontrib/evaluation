/******************************************************************************
 * ComparatorsUtils.java - refactored - evaluation - 2007 Sep 9, 2007 12:12:20 PM - AZ
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.utils;

import java.util.Comparator;

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

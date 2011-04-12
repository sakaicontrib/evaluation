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

import java.io.Serializable;
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
     * static class to sort {@link EvalEvaluation} by due date, then by title, then by id
     */
    public static class EvaluationDateTitleIdComparator implements Comparator<EvalEvaluation>, Serializable {
        static private final long serialVersionUID = 31L;
        public int compare(EvalEvaluation eval0, EvalEvaluation eval1) {
            int comparison = 0;
            if (eval0.getDueDate() != null && eval1.getDueDate() != null) {
                comparison = eval0.getDueDate().compareTo(eval1.getDueDate());
            } else {
                comparison = eval0.getStartDate().compareTo(eval1.getStartDate());
            }
            if (comparison == 0) {
                comparison = eval0.getTitle().compareTo(eval1.getTitle());
            }
            if (comparison == 0) {
                comparison = eval0.getId().compareTo(eval1.getId());
            }         
            return comparison;
        }
    }

    /**
     * static class to sort {@link EvalTemplateItem} objects by DisplayOrder (and ID if needed)
     */
    public static class TemplateItemComparatorByOrder implements Comparator<EvalTemplateItem>, Serializable {
        static private final long serialVersionUID = 31L;
        public int compare(EvalTemplateItem eti0, EvalTemplateItem eti1) {
            int comparison = 0;
            comparison = eti0.getDisplayOrder().compareTo( eti1.getDisplayOrder() );
            if (comparison == 0
                    && eti0.getId() != null && eti1.getId() != null) {
                comparison = eti0.getId().compareTo(eti1.getId());
            }         
            return comparison;
        }
    }

    /**
     * static class to sort {@link EvalTemplateItem} objects by Id
     */
    public static class TemplateItemComparatorById implements Comparator<EvalTemplateItem>, Serializable {
        static private final long serialVersionUID = 31L;
        public int compare(EvalTemplateItem eti0, EvalTemplateItem eti1) {
            return eti0.getId().compareTo( eti1.getId() );
        }
    }

    /**
     * static class to sort {@link EvalItem} objects by Id
     */
    public static class ItemComparatorById implements Comparator<EvalItem>, Serializable {
        static private final long serialVersionUID = 31L;
        public int compare(EvalItem item0, EvalItem item1) {
            return item0.getId().compareTo( item1.getId() );
        }
    }

    /**
     * static class to sort {@link EvalItemGroup} objects by Title
     */
    public static class ItemGroupComparatorByTitle implements Comparator<EvalItemGroup>, Serializable {
        static private final long serialVersionUID = 31L;
        public int compare(EvalItemGroup ig0, EvalItemGroup ig1) {
            int comparison = 0;
            comparison = ig0.getTitle().compareTo( ig1.getTitle() );
            if (comparison == 0) {
                comparison = ig0.getId().compareTo(ig1.getId());
            }         
            return comparison;
        }
    }

    /**
     * static class to sort {@link EvalItem} objects by Id
     */
    public static class GroupComparatorByTitle implements Comparator<org.sakaiproject.evaluation.logic.model.EvalGroup>, Serializable {
        static private final long serialVersionUID = 31L;
        public int compare(org.sakaiproject.evaluation.logic.model.EvalGroup item0, org.sakaiproject.evaluation.logic.model.EvalGroup item1) {
            return item0.title.compareTo( item1.title );
        }
    }

}

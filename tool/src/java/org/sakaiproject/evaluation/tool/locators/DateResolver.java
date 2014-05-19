/**
 * $Id$
 * $URL$
 * DateResolver.java - evaluation - Feb 28, 2008 5:51:37 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.locators;

import java.text.DateFormat;

import uk.org.ponder.beanutil.BeanLocator;
import uk.org.ponder.beanutil.BeanResolver;
import uk.org.ponder.localeutil.LocaleGetter;


/**
 * This is a bean which processes 
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class DateResolver implements BeanLocator {

   private LocaleGetter localeGetter;
   public void setLocaleGetter(LocaleGetter localeGetter) {
      this.localeGetter = localeGetter;
   }

   public Object locateBean(final String name) {
      return new BeanResolver () {

         public String resolveBean(Object date) {
            DateFormat df = null;
            if (name.equals("time")) {
               df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, localeGetter.get());
            } else {
               df = DateFormat.getDateInstance(DateFormat.LONG, localeGetter.get());
            }
            return df.format(date);
         }
      };
   }

}

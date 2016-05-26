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
      return (BeanResolver) (Object date) ->
      {
          DateFormat df;
          if (name.equals("time")) {
              df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, localeGetter.get());
          } else {
              df = DateFormat.getDateInstance(DateFormat.LONG, localeGetter.get());
          }
          return df.format(date);
      };
   }

}

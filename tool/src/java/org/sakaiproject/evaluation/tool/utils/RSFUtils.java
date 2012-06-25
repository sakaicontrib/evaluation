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
package org.sakaiproject.evaluation.tool.utils;

import uk.org.ponder.rsf.components.UIBound;
import uk.org.ponder.rsf.components.UIComponent;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIDisabledDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;

/**
 * A set of low-level utilities for working with RSF.
 * Goal is that this file is perpetually empty, but since we are working with
 * public repo JARs for this project now there will in reality be some lag 
 * before they are folded into the framework.
 * 
 * @author Antranig Basman (amb26@ponder.org.uk)
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@SuppressWarnings("deprecation")
public class RSFUtils {
 
  public static void targetLabel(UIComponent label, UIComponent target) {
    label.decorators = new DecoratorList(new UILabelTargetDecorator(target));
  }

  /**
   * This method is used to make things appear disabled where ever needed,
   * should work correctly with most components
   * 
   * @param component any RSF component
   */
  public static void disableComponent(UIComponent component) {
     component.decorate( new UIDisabledDecorator(true) );
     if (component instanceof UISelect) {
        component = ((UISelect)component).selection;
     }
     if (component instanceof UIBound) {
        ((UIBound)component).fossilize = false;
        ((UIBound)component).willinput = false;
     }
  }

}

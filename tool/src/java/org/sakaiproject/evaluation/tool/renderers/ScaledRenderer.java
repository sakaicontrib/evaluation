/**
 * ScaledRenderer.java - evaluation - Oct 29, 2007 2:59:12 PM - azeckoski
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.renderers;

import static org.sakaiproject.evaluation.utils.TemplateItemUtils.*;

import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.utils.ScaledUtils;
import org.sakaiproject.evaluation.utils.ArrayUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;

/**
 * This handles the rendering of scaled type items
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@SuppressWarnings("deprecation")
public class ScaledRenderer implements ItemRenderer {

   /**
    * This identifies the template component associated with this renderer
    */
   public static final String COMPONENT_ID = "render-scaled-item:";

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#renderItem(uk.org.ponder.rsf.components.UIContainer, java.lang.String, org.sakaiproject.evaluation.model.EvalTemplateItem, int, boolean)
    */
   public UIJointContainer renderItem(UIContainer parent, String ID, String[] bindings, EvalTemplateItem templateItem, int displayNumber, boolean disabled, Map<String, String> evalProperties) {
      UIJointContainer container = new UIJointContainer(parent, ID, COMPONENT_ID);

      if (displayNumber <= 0) displayNumber = 0;
      String initValue = null;
      if (bindings[0] == null) initValue = "";

      EvalScale scale = templateItem.getItem().getScale();
      String[] scaleOptions = scale.getOptions();
      int optionCount = scaleOptions.length;
      String scaleValues[] = new String[optionCount];
      String scaleLabels[] = new String[optionCount];

      String scaleDisplaySetting = templateItem.getScaleDisplaySetting();
      boolean usesNA = templateItem.getUsesNA() == null ? false : templateItem.getUsesNA().booleanValue();
      boolean evalAnswerReqired = evalProperties.containsKey(ItemRenderer.EVAL_PROP_ANSWER_REQUIRED) ? Boolean.valueOf(evalProperties.get(ItemRenderer.EVAL_PROP_ANSWER_REQUIRED)) : false;
      
      if (EvalConstants.ITEM_SCALE_DISPLAY_COMPACT.equals(scaleDisplaySetting) ||
            EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED.equals(scaleDisplaySetting)) {

         UIBranchContainer compact = UIBranchContainer.make(container, "compactDisplay:");
         if (templateItem.renderOption) {
            compact.decorate( new UIStyleDecorator("validFail") ); // must match the existing CSS class
         } else if ( safeBool(templateItem.getIsCompulsory()) && ! evalAnswerReqired) {
        	 compact.decorate( new UIStyleDecorator("compulsory") ); // must match the existing CSS class
         }


         // setup simple variables to make code more clear
         boolean colored = EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED.equals(scaleDisplaySetting);

         String compactDisplayStart = scaleOptions[0];		
         String compactDisplayEnd = scaleOptions[optionCount - 1];

         for (int count = 0; count < optionCount; count++) {
            scaleValues[count] = new Integer(count).toString();
            scaleLabels[count] = " ";
         }

         UIOutput.make(compact, "itemNum", displayNumber+"" ); //$NON-NLS-2$
         UIVerbatim.make(compact, "itemText", templateItem.getItem().getItemText());

         // Compact start and end label containers
         UIBranchContainer compactStartContainer = UIBranchContainer.make(compact, "compactStartContainer:");
         UIBranchContainer compactEndContainer = UIBranchContainer.make(compact, "compactEndContainer:");

         // Compact start and end labels
         UIOutput.make(compactStartContainer, "compactDisplayStart", compactDisplayStart);
         UIOutput.make(compactEndContainer, "compactDisplayEnd", compactDisplayEnd);

         if (colored) {
            compactStartContainer.decorators =
               new DecoratorList( new UIStyleDecorator("compactDisplayStart") );// must match the existing CSS class
            //new DecoratorList(new UIColourDecorator(null, ScaledUtils.getStartColor(scale)));
            compactEndContainer.decorators =
               new DecoratorList( new UIStyleDecorator("compactDisplayEnd") );// must match the existing CSS class
            //new DecoratorList(new UIColourDecorator(null, ScaledUtils.getEndColor(scale)));
         }

         // For the radio buttons
         UIBranchContainer compactRadioContainer = UIBranchContainer.make(compact, "compactRadioContainer:");
         if (colored) {
            UILink.make(compactRadioContainer, "idealImage", ScaledUtils.getIdealImageURL(scale));
         }

         if (usesNA) {
            scaleValues = ArrayUtils.appendArray(scaleValues, EvalConstants.NA_VALUE.toString());
            scaleLabels = ArrayUtils.appendArray(scaleLabels, "");
         }

         UISelect radios = UISelect.make(compactRadioContainer, "dummyRadio", scaleValues, scaleLabels, bindings[0], initValue);
         String selectID = radios.getFullID();

         if (disabled) {
            radios.selection.willinput = false;
            radios.selection.fossilize = false;
         }

         int scaleLength = scaleValues.length;
         int limit = usesNA ? scaleLength - 1: scaleLength;  // skip the NA value at the end
         for (int j = 0; j < limit; ++j) {
            if (colored) {
               UIBranchContainer radioBranchFirst = UIBranchContainer.make(compactRadioContainer, "scaleOptionColored:", j+"");
               UISelectChoice.make(radioBranchFirst, "radioValueColored", selectID, j);
               // this is confusing but this is now the one underneath
               UIBranchContainer radioBranchSecond = UIBranchContainer.make(compactRadioContainer, "scaleOption:", j+"");
               UIOutput.make(radioBranchSecond, "radioValue");
            } else {
               UIBranchContainer radioBranchSecond = UIBranchContainer.make(compactRadioContainer,	"scaleOption:", j+"");
               UISelectChoice.make(radioBranchSecond, "radioValue", selectID, j);
            }
         }

         if (usesNA) {
            UIBranchContainer radiobranch3 = UIBranchContainer.make(compact, "showNA:");
            radiobranch3.decorators = new DecoratorList( new UIStyleDecorator("na") );// must match the existing CSS class
            UISelectChoice choice = UISelectChoice.make(radiobranch3, "na-input", selectID, scaleLength - 1);
            UIMessage.make(radiobranch3, "na-desc", "viewitem.na.desc").decorate( new UILabelTargetDecorator(choice));
         }

      } else if (EvalConstants.ITEM_SCALE_DISPLAY_FULL.equals(scaleDisplaySetting) || 
            EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED.equals(scaleDisplaySetting) ||
            EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL.equals(scaleDisplaySetting)) {

         UIBranchContainer fullFirst = UIBranchContainer.make(container, "fullType:");
         if (templateItem.renderOption) {
            fullFirst.decorate( new UIStyleDecorator("validFail") ); // must match the existing CSS class
         } else if ( safeBool(templateItem.getIsCompulsory()) && ! evalAnswerReqired) {
        	 fullFirst.decorate( new UIStyleDecorator("compulsory") ); // must match the existing CSS class
         }

         for (int count = 0; count < optionCount; count++) {
            scaleValues[count] = new Integer(count).toString();
            scaleLabels[count] = scaleOptions[count];	
         }

         UIOutput.make(fullFirst, "itemNum", displayNumber+"" ); //$NON-NLS-2$
         UIVerbatim.make(fullFirst, "itemText", templateItem.getItem().getItemText());

         // display next row
         UIBranchContainer radiobranchFullRow = UIBranchContainer.make(container, "nextrow:", displayNumber+"");

         String containerId;
         if ( EvalConstants.ITEM_SCALE_DISPLAY_FULL.equals(scaleDisplaySetting) ) {
            containerId = "fullDisplay:";
         } else if ( EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL.equals(scaleDisplaySetting) ) {
            containerId = "verticalDisplay:";
         } else if ( EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED.equals(scaleDisplaySetting) ) {
            containerId = "fullDisplayColored:";
         } else {
            throw new RuntimeException("Invalid scaleDisplaySetting (this should not be possible): " + scaleDisplaySetting);
         }

         UIBranchContainer displayContainer = UIBranchContainer.make(radiobranchFullRow, containerId);

         if ( EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED.equals(scaleDisplaySetting) ) {
            UILink.make(displayContainer, "idealImage", ScaledUtils.getIdealImageURL(scale));
         }

         if (usesNA) {
            scaleValues = ArrayUtils.appendArray(scaleValues, EvalConstants.NA_VALUE.toString());
            scaleLabels = ArrayUtils.appendArray(scaleLabels, "");
         }

         UISelect radios = UISelect.make(displayContainer, "dummyRadio", scaleValues, scaleLabels, bindings[0], initValue);
         String selectID = radios.getFullID();

         if (disabled) {
            radios.selection.willinput = false;
            radios.selection.fossilize = false;
         }

         int scaleLength = scaleValues.length;
         int limit = usesNA ? scaleLength - 1: scaleLength;  // skip the NA value at the end
         for (int j = 0; j < limit; ++j) {
            UIBranchContainer radiobranchNested = UIBranchContainer.make(displayContainer, "scaleOption:", j+"");
            UISelectChoice choice = UISelectChoice.make(radiobranchNested, "radioValue", selectID, j);
            UISelectLabel.make(radiobranchNested, "radioLabel", selectID, j).decorate( new UILabelTargetDecorator(choice));
         }

         if (usesNA) {
            UIBranchContainer radiobranch3 = UIBranchContainer.make(displayContainer, "showNA:");
            radiobranch3.decorators = new DecoratorList( new UIStyleDecorator("na") );// must match the existing CSS class				
            UISelectChoice choice = UISelectChoice.make(radiobranch3, "na-input", selectID, scaleLength - 1);
            UIMessage.make(radiobranch3, "na-desc", "viewitem.na.desc").decorate( new UILabelTargetDecorator(choice));
         }


      } else if (EvalConstants.ITEM_SCALE_DISPLAY_STEPPED.equals(scaleDisplaySetting) ||
            EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED.equals(scaleDisplaySetting) ) {

         UIBranchContainer stepped = UIBranchContainer.make(container, "steppedDisplay:");
         if (templateItem.renderOption) {
            stepped.decorate( new UIStyleDecorator("validFail") ); // must match the existing CSS class
         } else if ( safeBool(templateItem.getIsCompulsory()) && ! evalAnswerReqired) {
        	 stepped.decorate( new UIStyleDecorator("compulsory") ); // must match the existing CSS class
         }

         // setup simple variables to make code more clear
         boolean colored = EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED.equals(scaleDisplaySetting);

         UIOutput.make(stepped, "itemNum", displayNumber+"" ); //$NON-NLS-2$
         UIVerbatim.make(stepped, "itemText", templateItem.getItem().getItemText());

         UIBranchContainer coloredBranch = null;
         if (colored) {
            coloredBranch = UIBranchContainer.make(stepped, "coloredChoicesBranch:");
            UILink.make(coloredBranch, "idealImage", ScaledUtils.getIdealImageURL(scale));
         }

         for (int count = 1; count <= optionCount; count++) {
            scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
            scaleLabels[optionCount - count] = scaleOptions[count-1];
         }

         if (usesNA) {
            scaleValues = ArrayUtils.appendArray(scaleValues, EvalConstants.NA_VALUE.toString());
            scaleLabels = ArrayUtils.appendArray(scaleLabels, "");
         }

         UISelect radios = UISelect.make(stepped, "dummyRadio", scaleValues, scaleLabels, bindings[0], initValue); 
         String selectID = radios.getFullID();

         if (disabled) {
            radios.selection.willinput = false;
            radios.selection.fossilize = false;
         }

         int scaleLength = scaleValues.length;
         int limit = usesNA ? scaleLength - 1: scaleLength;  // skip the NA value at the end
         UISelectLabel[] labels = new UISelectLabel[limit];
         UISelectChoice[] choices = new UISelectChoice[limit];
         for (int j = 0; j < limit; ++j) {
            UIBranchContainer rowBranch = UIBranchContainer.make(stepped, "rowBranch:", j+"");

            // Actual label
            labels[limit-j-1] = UISelectLabel.make(rowBranch, "topLabel", selectID, j);

            // Corner Image
            UILink.make(rowBranch, "cornerImage", EvalToolConstants.STEPPED_IMAGE_URLS[0]);

            // create middle images after the item label
            for (int k = 0; k < j; ++k) {
               UIBranchContainer afterTopLabelBranch = UIBranchContainer.make(rowBranch, "afterTopLabelBranch:", k+"");
               UILink.make(afterTopLabelBranch, "middleImage",	EvalToolConstants.STEPPED_IMAGE_URLS[1]);
            }

            // create bottom (down arrow) image
            UIBranchContainer bottomLabelBranch = UIBranchContainer.make(stepped, "bottomLabelBranch:", j+"");
            UILink.make(bottomLabelBranch, "bottomImage", EvalToolConstants.STEPPED_IMAGE_URLS[2]);

            if (colored) {
               UIBranchContainer radioBranchFirst = UIBranchContainer.make(coloredBranch, "scaleOptionColored:", j+"");
               choices[j] = UISelectChoice.make(radioBranchFirst, "radioValueColored", selectID, j);
               // this is confusing but this is now the one underneath
               UIBranchContainer radioBranchSecond = UIBranchContainer.make(stepped, "scaleOption:", j+"");
               UIOutput.make(radioBranchSecond, "radioValue");
            } else {
               UIBranchContainer radioBranchSecond = UIBranchContainer.make(stepped, "scaleOption:", j+"");
               choices[j] = UISelectChoice.make(radioBranchSecond, "radioValue", selectID, j);
            }
         }

         for (int i = 0; i < choices.length; i++) {
            UILabelTargetDecorator.targetLabel(labels[i], choices[i]);
         }

         if (usesNA) {
            UISelectChoice choice = UISelectChoice.make(stepped, "na-input", selectID, scaleLength - 1);
            UIMessage.make(container, "na-desc", "viewitem.na.desc").decorate( new UILabelTargetDecorator(choice));
         }

      } else {
         throw new IllegalStateException("Unknown scaleDisplaySetting ("+scaleDisplaySetting+") for " + getRenderType());
      }

      // render the item comment
      ItemRendererImpl.renderCommentBlock(container, templateItem, bindings);

      return container;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#getRenderType()
    */
   public String getRenderType() {
      return EvalConstants.ITEM_TYPE_SCALED;
   }

}

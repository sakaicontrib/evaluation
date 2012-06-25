/*
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
// Eval Template Modify JS for Sorting
// @author lovemore.nalube@uct.ac.za

var evalTemplateSort = (function(){
    //Logger
    var log = evalTemplateUtils.debug;

    //Update the itemRow labels after a sort event
    var updateLabellingAfterSort = function(enableSaveButton){
        var list = $("#itemList > div.itemRow").not('.ui-sortable-helper').get(),
        enableBtn = ( typeof enableSaveButton === "undefined" || enableSaveButton === null) ? true : enableSaveButton;
        log.group("Updating Labelling After Sort");
        for (var i = 0; i < list.length; i++) {
            if (list[i].id) {
                setIndex(list[i].id, i);
                log.debug("Setting index for id %s from object %o", list[i].id, list[i]);
            }
        }

        if( enableBtn ){
            log.info("enabling save buttons");
            enableOrderButtons();
        }

        refreshSort();
        log.groupEnd();
    },

    updateDropDownMax = function(){
        //update all dropdowns to show new max EVALSYS-802
        var currentMaxDropDownOptions = $("#itemList > div.itemRow").not('.ui-sortable-helper').length,
        previousMaxDropDownOptions = $("select[id*=item-select-selection]:eq(0) > option").length;
        if(currentMaxDropDownOptions !== previousMaxDropDownOptions){
            $.each($("select[id*=item-select-selection]"), function(m, dropdown) {
                log.group("Trimming dropdown vals for %o", dropdown);
                //Only consider option items in the range to remove so as to avoid unneccessary looping
                for (var toRemove = previousMaxDropDownOptions; toRemove >= currentMaxDropDownOptions; toRemove--){
                    $(dropdown).find("option:eq(" + toRemove + ")").each(function() {
                           log.info("Removing %o with num: %i", this, toRemove);
                            $(this).remove();
                    });
                }
                log.groupEnd();
            });
        }
    };

    return {
        updateLabelling: updateLabellingAfterSort,
        updateDropDownMax: updateDropDownMax
    };
})($);

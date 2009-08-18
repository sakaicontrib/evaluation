// Eval Template Modify JS for Sorting
// @author lovemore.nalube@uct.ac.za

var evalTemplateSort = (function(){
    //Logger
    var log = evalTemplateUtils.debug;

    //Update the itemRow labels after a sort event
    var updateLabellingAfterSort = function(enableSaveButton){
        var list = $("#itemList > div.itemRow").not('.ui-sortable-helper').get(),
        currentMaxDropDownOptions = list.length,
        previousMaxDropDownOptions = $("select[id*=item-select-selection]:eq(0) > option").length,
        enableBtn = ( typeof enableSaveButton === "undefined" ) ? true : enableSaveButton;
        log.group("Updating Labelling After Sort");
        for (var i = 0; i < list.length; i++) {
            if (list[i].id) {
                setIndex(list[i].id, i);
                log.debug("Setting index for id %s from object %o", list[i].id, list[i]);
            }
        }

        if( enableBtn){
            log.info("enabling save buttons");
            enableOrderButtons();
        }
        //update all dropdowns to show new max EVALSYS-802
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

        refreshSort();
        log.groupEnd();
    };

    return {
        updateLabelling: updateLabellingAfterSort
    };
})($);

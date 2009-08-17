// Eval Template Modify JS for Sorting
// @author lovemore.nalube@uct.ac.za

var evalTemplateSort = (function(){
    //Logger
    var log = evalTemplateUtils.debug;

    //Update the itemRow labels after a sort event
    var updateLabellingAfterSort = function(enableSaveButton){
        var list = $("#itemList > div.itemRow").not('.ui-sortable-helper').get(),
        maxDropDownOptions = list.length,
        count = 0,
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
        $("select[id*=item-select-selection]").each(function(){
            count = 0;
            $(this).find("option").each(function(){
                if (count >= maxDropDownOptions){
                    $(this).remove();
                }
                count++;
            });
        });
        refreshSort();
        log.groupEnd();
    };

    return {
        updateLabelling: updateLabellingAfterSort
    };
})($);

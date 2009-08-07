// Eval Template Modify JS for re-ordering
// @author lovemore.nalube@uct.ac.za

var evalTemplateOrder = (function(){

    //Logger
    var log = evalTemplateUtils.debug;

    //initialise the reorder dropdowns
    var initDropDowns = function(){
      $("#itemList > div.itemRow").each(function(){

           $(this).find("select[id*=item-select-selection]:eq(0)").each(function(){
               var oldPosition =  this.options[this.selectedIndex].value;
                this.onchange = function(){
                    var newPosition = this.options[this.selectedIndex].value -1,
                    diff = newPosition - oldPosition,
                    moveUp = newPosition < oldPosition,
                    allRows = $("#itemList > div.itemRow").not('.ui-sortable-helper'),
                    thisRow = $(this).parents("div.itemRow"),
                    clone = thisRow.clone(true);
                    clone.hide();
                    log.info("Moving row %i from %i to position: %i", thisRow.find("input[name*=item-select-selection-fossil]").val(), oldPosition, newPosition + 1);
                    thisRow.fadeOut(0, function(){
                        if ( moveUp ){
                            if( (diff === 2 || diff === -2) && newPosition !== 0 ){      //if row to move to is immediatley above this row
                                clone.insertBefore(allRows.eq(oldPosition - 2));
                            }else if(newPosition > 0){
                                clone.insertAfter(allRows.eq(newPosition - 1));
                            }else{
                                clone.insertBefore(allRows.eq(0));  //if row to move is moving to the absolute top
                            }
                        }else{
                            clone.insertAfter(allRows.eq(newPosition));
                        }

                        clone.fadeIn(0, function(){
                                    evalTemplateSort.updateLabelling(false);
                                    initDropDowns();
                                    var order = [];
                                    $("#itemList > div").not('.ui-sortable-helper').each(function(){
                                        order.push($(this).find('a[templateitemid]').attr('templateitemid'));
                                    });
                                    var params = {
                                        orderedIds : order.toString()
                                    };

                                    evalTemplateData.item.saveOrder(evalTemplateUtils.pages.eb_save_order, params);
                                })
                                .effect("highlight", "normal");
                        thisRow.remove();
                    });
                };
           });
        });

    };

    return {
        initDropDowns : initDropDowns
    };
})($);

// Eval Template Modify JS for re-ordering
// @author lovemore.nalube@uct.ac.za

var evalTemplateOrder = (function(){

    //Logger
    var log = evalTemplateUtils.debug,

    //initialise the reorder dropdowns
    initDropDowns = function(changedRow){
        if ( typeof changedRow === "undefined" || changedRow === null){
            var rowList = $("#itemList > div.itemRow");
              log.group("initialising the reorder dropdowns in rows: %o", rowList);
              rowList.each(function(){
                   $(this).find("select[id*=item-select-selection]:eq(0)").each(function(){
                        privateInitDropDownForRow(this);
                   });
                });
              log.groupEnd();
        }else{
            changedRow.each(function(){
                privateInitDropDownForRow(this);
            });
        }
    },

    saveTopLevelTemplateOrdering = function(){
        var order = [];
        $("#itemList > div").not('.ui-sortable-helper').each(function(){
            order.push($(this).find('a[templateitemid]').attr('templateitemid'));
        });
        var params = {
            orderedIds : order.toString()
        };
        evalTemplateData.item.saveOrder(evalTemplateUtils.pages.eb_save_order, params);
    },

    initSaveGroupOrderControls = function(anyGroupedItemObject){
        log.info("Moved ordering for %o", anyGroupedItemObject);
        $(document).trigger('block.triggerChildrenSort', [anyGroupedItemObject.parents("div.itemTableBlock")]);
        evalTemplateOrder.saveGroupLevelTemplateOrdering(anyGroupedItemObject);
    },

    saveGroupLevelTemplateOrdering = function(anyGroupedItemObject){
        var order = [];
        anyGroupedItemObject.parents('.itemTableBlock').find('div.itemRowBlock').not('.ui-sortable-helper').each(function(){
            order.push($(this).find('a[templateitemid]').attr('templateitemid'));
        });
        var params = {
            orderedIds : order.toString()
        },
        fnBefore = function(){
            $(document).trigger('block.triggerChildrenSort', [$(this).parents("div.itemRow")]);
            anyGroupedItemObject.parents('.itemTableBlock').sortable('disable');
        },
        fnAfter = function(){
            //init dropdown controls
            evalTemplateOrder.initDropDowns();
            anyGroupedItemObject.parents('.itemTableBlock').sortable('enable');
            anyGroupedItemObject.parents('.itemRow').find('.itemBlockSave').fadeOut(0, function() {
                $(this).remove();
            });
        };
        evalTemplateData.item.saveOrder(evalTemplateUtils.pages.eb_save_order, params, fnBefore, fnAfter);
    },

    // populate or re-populate groupable item array
    initGroupableItems = function(){
        log.group("Init groupable Items list");
        log.debug("%i items WERE groupable.", evalTemplateUtils.vars.groupableItems.length);
        evalTemplateUtils.vars.groupableItems = [];
        $('div.itemList > div:visible').each(function() {
            var that = $(this);
            if (that.children('.itemLine3').length === 0) {
                var t = "",
                rowNumber;
                if (that.find('.itemText > span').eq(1).text() == ""){
                    t = that.find('.itemText > span').eq(0).html();
                }
                else{
                    t = that.find('.itemText > span').eq(1).html();
                }
                that.find("select:eq(0)").each(function(){
                    rowNumber = this.options[this.selectedIndex].value;
                });
                var groupableItem = {
                    text:   t,
                    type:   (that.find('.itemCheckbox > input').attr('id') ? that.find('.itemCheckbox > input').attr('id') : "000"),
                    itemId: that.find('a[templateitemid]').eq(0).attr('templateitemid'),
                    otp:    that.find('a[otp]').eq(0).attr('otp'),
                    rowId:  that.attr('id'),
                    rowNumber: rowNumber
                };
            evalTemplateUtils.vars.groupableItems.push(groupableItem);
            log.debug("Added %o to list.", groupableItem);
        }
    });
    log.info("%i items now groupable.", evalTemplateUtils.vars.groupableItems.length);
    log.groupEnd();
    },

    //Internal functions
    privateInitDropDownForRow = function(_this){
        var oldPosition =  _this.options[_this.selectedIndex].value,
        that = $(_this);
        //First unbind onChange event
        that.unbind("change");
        that.bind("change", function(){
            var newPosition = this.options[this.selectedIndex].value -1,
            diff = newPosition - oldPosition,
            moveUp = newPosition < oldPosition,
            allRows = $("#itemList > div.itemRow").not('.ui-sortable-helper'),
            thisRow = $(this).parents("div.itemRow"),
            clone = evalTemplateUtils.vars.isIE ? thisRow.clone(false) : thisRow.clone(true);
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
                            evalTemplateOrder.saveTopLevelTemplateOrdering();
                            //IE has issues with persisting events after cloning, so rebinding event handlers
                            if (evalTemplateUtils.vars.isIE){
                                evalTemplateLoaderEvents.bindRowEditPreviewIcons($(this));
                                //Unbind link events from row links
                                evalTemplateLoaderEvents.unBindDeleteIcons();
                                evalTemplateLoaderEvents.bindDeleteIcons();
                            }
                        })
                        .effect("highlight", "normal");
                thisRow.remove();
            });
        });
    };

    return {
        initDropDowns : initDropDowns,
        initSaveGroupOrderControls : initSaveGroupOrderControls,
        initGroupableItems : initGroupableItems,
        saveTopLevelTemplateOrdering : saveTopLevelTemplateOrdering,
        saveGroupLevelTemplateOrdering : saveGroupLevelTemplateOrdering
    };
})($);

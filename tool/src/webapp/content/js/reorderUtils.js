/**
 * Reordering utilities for the items drag and drop ordering page -AZ
 * Does not put stuff into the global namespace and uses only methods that are compatible with most browsers
 *
 * Update: Functions have been exposed to enable startSort() to be accessible by custom plugins - lovemore.nalube@uct.ac.za
 **/
var sortableIds = [];

function $it(id) {
    return document.getElementById(id);
}

function enableOrderButtons() {
    try {
        $("#revertOrderButton")
                .attr('disabled', false)
                // put original order into the revertOrder trigger
                .bind('click', function() {
                    disableOrderButtons();
                    for (var i = sortableIds.length - 1; i > 0; --i) {
                        var thisitem = $it(sortableIds[i]);
                        var previtem = $it(sortableIds[i - 1]);
                        previtem.parentNode.removeChild(previtem);
                        thisitem.parentNode.insertBefore(previtem, thisitem);
                        setIndex(thisitem.id, i);
                    }
                    setIndex(sortableIds[0], 0);
                });
        $("#saveReorderButton").attr('disabled', false);
        $("#orderInputs").attr("class","itemOperationsEnabled");
    }catch(e){}
}

function disableOrderButtons() {
    try {
        $("#revertOrderButton").attr('disabled', true);
        $("#saveReorderButton").attr('disabled', true);
        $("#orderInputs").attr("class","itemOperations");
    }catch(e){}
}

function buildSortableIds() {
    $("#itemList > div.itemRow").each(function(){
       sortableIds.push($(this).attr('id'));
    });
}

function setIndex(itemId, newindex) {
    if (newindex != null || newindex != "") {
        var changed = sortableIds[newindex] != itemId;
        var itemSelect = document.getElementById(itemId + "item-select-selection");
        itemSelect.selectedIndex = newindex;
        // NOTE: might need to disable the pulldowns once the user drags
        $it(itemId + "hidden-item-num").value = newindex;
    }
}

function startSort() {
    var log = evalTemplateUtils.debug,
    numHasRun = 0;
    $("#itemList").sortable({
        axis:         'y',
        cancel:     ':input, button, a, select, div.itemLine3',
        connectWith: ['.itemTableBlock'],
        cursor:     'move',
        delay:         '1',
        opacity:     '0.9',
        scroll:     true,
        update: function(){
            evalTemplateSort.updateLabelling();
        }
    });
    $(".itemTableBlock").each(function() {
        $(this).sortable({
            axis: 'y',
            cancel: ':input, button, a, select',
            containment: 'parent',
            cursor: 'move',
            over: function(e, ui) {
                if ($('#closeItemOperationsEnabled').length > 0) {
                    $('#closeItemOperationsEnabled').parent().remove();
                }
            },
            deactivate: function(e, ui) {
                //Ensure this only runs once when object is dropped into the group by tracking {@link numHasRun}
                if (numHasRun === 0){
                    if ((ui.sender.attr('id') != "itemTableBlock") && (ui.item.parent().attr('id') != "itemList")) {
                        var that = ui.item.find('.itemCheckbox > input').eq(0).attr('id');
                        if (that === null || typeof that === "undefined") {
                            $(document).trigger('block.rejectItem', [ui , "plain"]);
                            log.warn("RejectItem: Item is not the same type as the grouped items");
                        }else{
                        var target = ui.item.parents('.itemRow').eq(0).find('.itemCheckbox > input').eq(0).attr('id');
                        if (target === null || typeof target === "undefined"){
                            $(document).trigger('block.rejectItem', [ui , "plain"]);                            
                            log.warn("RejectItem: Target is is not the same type as the selected Item");
                        }else{
                        var targetVal = target.substring(target.indexOf('-') + 1, target.lastIndexOf('-'));
                        var thatVal = that.substring(that.indexOf('-') + 1, that.lastIndexOf('-'));
                        if (targetVal !== thatVal) {
                            $(document).trigger('block.rejectItem', [ui, "plain"]);
                        } else {
                            var confirmMsg = evalTemplateUtils.messageLocator('modifytemplate.group.add.existingitem.confirm');
                                if (confirm(confirmMsg)) {
                                    var parentObj = ui.item.parents("div.itemRow"),
                                    blockId = parentObj.find("input[name=template-item-id]").val(),
                                    itemId =  ui.item.find('a[templateitemid]').attr('templateitemid');
                                    log.info("Added group %o", parentObj);
                                    ui.item.attr('style', 'display:none');
                                    var shadow = ui.item.parent().find('.itemRowBlock').eq(0).clone(true);
                                    ui.item.parent().find('div[id="' + shadow.attr('id') + '"]').not(':lt(2)').remove();
                                    shadow.find('span').eq(1).html(ui.item.find('.itemText > span').eq(0).html());
                                    shadow.find('input').eq(0).val(itemId);
                                    shadow.find('a[templateitemid]').attr('templateitemid', ui.item.find('a[templateitemid]').attr('templateitemid'));
                                    shadow.find('a[otp]').attr('otp', ui.item.find('a[otp]').attr('otp'));
                                    shadow.insertAfter(ui.item);
                                    log.info("Added %o to group %o", itemId, parentObj);
                                    //Save new item to the group using already set EB post method
                                    var params = {
                                        blockid : blockId,
                                        additems: itemId
                                    },
                                    fnAfter = function(){
                                        evalTemplateSort.updateDropDownMax();
                                        //Save new group order
                                        evalTemplateOrder.saveGroupLevelTemplateOrdering(ui.item);
                                        //Save overall template ordering so the template knows that we've removed a few items and grouped them.
                                        evalTemplateOrder.saveTopLevelTemplateOrdering();
                                        $(document).trigger('block.triggerChildrenSort', [parentObj]);
                                    };
                                    evalTemplateData.item.saveOrder(evalTemplateUtils.pages.eb_block_edit, params, null, fnAfter);
                                }
                                else{
                                    $(document).trigger('block.rejectItem', [ui, "noAlert"]);
                                }
                        }
                      }
                    }
                    }
                }
                numHasRun ++;
            },
            delay: '1',
            items: 'div',
            opacity: '0.9',
            scroll: true,
            update: function(e, ui) {
                evalTemplateOrder.initSaveGroupOrderControls(ui.item);
            }
        });
    });
}
function refreshSort() {
    $("div.itemList").sortable('destroy');
    $("div.itemTableBlock").sortable('destroy');
    startSort();
}
$(document).bind('block.triggerChildrenSort', function(e, parentItem) {
    evalTemplateUtils.debug.info("block.triggerChildrenSort with patent item %o", parentItem);
    $.each(parentItem.find('div.itemRowBlock').not('.ui-sortable-helper'), function(i, _this){      
        $(_this).find('span.itemLabel').text(i + 1);
    });
    refreshSort();
});
$(document).bind('block.rejectItem', function(e, ui, option) {
    evalTemplateUtils.debug.group("rejecting to block this item: %o", ui.item);
    $(document).trigger('list.warning', [ui, option, 'block', evalTemplateUtils.messageLocator('modifytemplate.group.cannot.add.item')]);
    var shadow = evalTemplateUtils.vars.isIE ? ui.item.clone(false) : ui.item.clone(true);
    var shadowPlace = $('.itemList > div.itemRow').eq(ui.item.find('input').eq(0).val()).attr('id');
    ui.item.remove();
    shadow.insertBefore($('[id="' + shadowPlace + '"]'));
    shadow.removeAttr('style').effect('highlight', 1000);
    $('.itemList div[id="' + shadow.attr('id') + '"]').not(':lt(1)').remove();
    refreshSort();
    if (evalTemplateUtils.vars.isIE){
        evalTemplateLoaderEvents.bindRowEditPreviewIcons(shadow);
        //Unbind link events from row links
        evalTemplateLoaderEvents.unBindDeleteIcons();
        evalTemplateLoaderEvents.bindDeleteIcons();
    }
    //init dropdown controls for this row & also update labelling
    evalTemplateOrder.initDropDowns();
    evalTemplateSort.updateLabelling(false);
    evalTemplateUtils.debug.groupEnd();
});

$(document).bind('list.warning', function(e, ui, option, extra, err){
    var item = ui.item? ui.item : ui;
        if (option != "noAlert") {
       $("div.itemList").find('.itemOperationsEnabled').remove();
        var error = '<div class="itemOperationsEnabled">\
                        <img src="/library/image/sakai/cancelled.gif"/>\
                        <span class="instruction">'+ err +'</span> <a href="#" id="closeItemOperationsEnabled">close</a></div>\
                        ';
        if(extra == 'block')
            item.parents('.itemLine3').prepend(error);
        $('#closeItemOperationsEnabled').click(function() {
            $(this).parent().slideUp('normal', function() {
                $(this).remove()
            });
            return false
        });
    }
});

/**
 * Reordering utilities for the items drag and drop ordering page -AZ
 * Does not put stuff into the global namespace and uses only methods that are compatible with most browsers
 *
 * Update: Functions have been exposed to enable startSort() to be accessible by custom plugins - lovemore.nalube@uct.ac.za
 **/
var sortableIds = new Array();
$(document).ready(
        function () {
			startSort();
            $(document).trigger('activateControls.templateItems');
            buildSortableIds();

        }
        );

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
    $("#itemList").sortable({
        axis:         'y',
        cancel:     ':input, button, a, select, div.itemLine3',
        connectWith: ['.itemTableBlock'],
        cursor:     'move',
        delay:         '1',
        opacity:     '0.9',
        scroll:     true,
        update: function(data) {
            var list = $("#itemList > div").get();
            for (var i = 0; i < list.length - 1; i++) {
                if (list[i].id) {
                    setIndex(list[i].id, i);
                }
            }
            enableOrderButtons();
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
                if ((ui.sender.attr('id') != "itemTableBlock") && (ui.item.parent().attr('id') != "itemList")) {
                    var that = ui.item.find('.itemCheckbox > input').eq(0).attr('id');
                    if (that == null) {
                        $(document).trigger('block.rejectItem', [ui , "plain"]);
                    }
                    var target = ui.item.parents('.itemRow').eq(0).find('.itemCheckbox > input').eq(0).attr('id');
                    if (target == null)
                        return false;
                    var targetVal = target.substring(target.indexOf('-') + 1, target.lastIndexOf('-'));
                    var thatVal = that.substring(that.indexOf('-') + 1, that.lastIndexOf('-'));
                    if (targetVal != thatVal) {
                        $(document).trigger('block.rejectItem', [ui]);
                    } else {

                        var confirmMsg = '\
                                         <div class="" style="font-weight: bold;">Are you sure?</div><br />\
			                <div class="footer"> \
			                  <input type="button" name="blockChildConfirm" msg="yes" accesskey="s" value="Yes"/>  |  \
			                  <a href="#" name="blockChildConfirm" msg="no">Cancel</a>  \
			                </div> \
                                         ';

                        $.facebox(confirmMsg);

                        $('#facebox .header').eq(0).hide();
                        $('#facebox table').attr('width', 250);
                        $('#facebox .body').css('width', 250);
                        $('#facebox .body [name*=blockChildConfirm]').click(function() {
                            if ($(this).attr('msg') == 'yes') {
                                $.facebox('\
                                                  <div><img src="' + $.facebox.defaults.loadingImage + '"/>  Saving... \
			                </div> \
			                <div class="footer"> \
			                   Please do not close this window.  \
			                </div> ');
                                ui.item.attr('style', 'display:none');
                                var shadow = ui.item.parent().find('.itemRowBlock').eq(0).clone(true);
                                shadow.insertAfter(ui.item);
                                ui.item.parent().find('div[id="' + shadow.attr('id') + '"]').not(':lt(2)').remove();
                                shadow.find('span').eq(1).html(ui.item.find('.itemText > span').eq(0).html());
                                shadow.find('input').eq(0).val(ui.item.find('a[templateitemid]').attr('templateitemid'));
                                shadow.find('a[templateitemid]').attr('templateitemid', ui.item.find('a[templateitemid]').attr('templateitemid'));
                                shadow.find('a[otp]').attr('otp', ui.item.find('a[otp]').attr('otp'));
                                $(document).trigger('block.saveReorder', [ui]);
                                return false;

                            }
                            else if ($(this).attr('msg') == 'no') {
                                $(document).trigger('block.rejectItem', [ui, "noAlert"]);
                                $(document).trigger('close.facebox');
                                return false;
                            }

                        });
                    }

                }
                return false;
            },
            delay: '1',
            items: 'div',
            opacity: '0.9',
            scroll: true,
            update: function(e, ui) {
                $(document).trigger('block.triggerChildrenSort', [ui]);
                if (ui.item.parents('.itemTableBlock').find('.itemBlockSave').length == 0) {
                    var saveAction = '<a class="itemBlockSave highlight" href="#saveAction">Save new order for grouped items</a>';
                    $(saveAction).appendTo(ui.item.parents('.itemTableBlock').children('.instruction').eq(0));
                    ui.item.parents('.itemTableBlock').children('.instruction').eq(0).effect('highlight', 1500);
                    ui.item.parents('.itemTableBlock').find('.itemBlockSave').bind('click', function() {
                        $(document).trigger('block.triggerChildrenSort', [ui]);   
                        $(this).html('Saving... <img src="' + $.facebox.defaults.loadingImage + '"/>');
                        ui.item.parents('.itemTableBlock').sortable('disable');
                        $(document).trigger('block.saveReorder', [ui,"simple"]);
                        ui.item.parents('.itemTableBlock').sortable('enable');
                        return false;
                    });
                }


            }
        });
    });
}
function refreshSort() {
    $("div.itemList").sortable('destroy');
    $("div.itemTableBlock").sortable('destroy');
    startSort();
}
$(document).bind('block.triggerChildrenSort', function(e, ui) {
	if(!ui.item){
        var tempUi = ui;
        var ui = new Object()
          ui.item = tempUi;
    }
    var count = 1;
    ui.item.parents('.itemTableBlock').find('div.itemRowBlock').not('.ui-sortable-helper').each(function(){
		$(this).find('.itemLabel').text(count);
		count++;
	});
});
$(document).bind('block.rejectItem', function(e, ui, option) {
    $(document).trigger('list.warning', [ui, option, 'block', 'Sorry this item cannot be grouped here. It is not the same type as the grouped items.']);
    var shadow = ui.item.clone(true);
    var shadowPlace = $('.itemList > div.itemRow').eq(ui.item.find('input').eq(0).val()).attr('id');
    ui.item.remove();
    shadow.insertBefore($('[id="' + shadowPlace + '"]'));
    shadow.removeAttr('style').effect('highlight', 3500);
    $('.itemList div[id="' + shadow.attr('id') + '"]').not(':lt(1)').remove();
    refreshSort();
    var list = $(".itemList > div.itemRow").get();
    for (var i = 0; i < list.length; i++) {
        if (list[i].id) {
            setIndex(list[i].id, i);
        }
    }
});

$(document).bind('block.saveReorder', function(e, ui, type) {
    if(!ui.item){
        var tempUi = ui;
        var ui = new Object()
          ui.item = tempUi;
    }
	$(document).trigger('list.busy', true);
    var currentOrder = ui.item.parents('.itemRow').children('input[name=*hidden-item-num]').eq(0).val();
    $.get('/direct/eval-templateitem/' + ui.item.parents('.itemRow').children('.itemLine2').find('a[templateitemid]').attr('templateitemid') + '.xml',
            function(msg) {
                var itemText = $(msg).find('item > itemText').text();
                var usesNA = $(msg).find('usesNA').eq(1).text();
                var category = $(msg).find('category').eq(1).text();
                var idealColour =  $(msg).find('item > scaleDisplaySetting').text();
                var t = ui.item.parents('.itemRow').eq(0).children('.itemLine2').find('a[templateitemid]');

                $.ajax({
                    url: "remove_item",
                    data: 'templateItemId=' + t.attr('templateitemid') + '&templateId=' + t.attr('templateid') + '&command+link+parameters%26deletion-binding%3Dl%2523%257B' + t.attr('otp') + '%257D%26Submitting%2520control%3Dremove-item-command-link=Remove+Item',
                    type: "POST",
                    success: function() {
                        var params = 'item-text=' + itemText;
                        params += '&item-text-fossil=istring%23%7BtemplateItemWBL.new1.item.itemText%7D';
                        params += '&showItemCategory%3A%3Aitem-category-list-selection-fossil=istring%23%7BtemplateItemWBL.new1.category%7DCourse';
                        params += '$showItemCategory%3A%3Aitem-category-list-selection=' + category;
                        params += '&idealColor-fossil=iboolean%23%7BtemplateBBean.idealColor%7Dfalse';
                        params += '&idealColor=' + (idealColour == "Stepped" ? "false" : "true");
                        params += '&item_NA-fossil=iboolean%23%7BtemplateItemWBL.new1.usesNA%7Dfalse';
                        params += '&item_NA=' + usesNA;
                        var ordering = new Array();
                        ui.item.parents('.itemTableBlock').eq(0).children('.itemRowBlock').filter(':visible').each(function() {
                            var val = $(this).children('input[name=hidden-item-id]').eq(0).val();
                            params += '&hidden-item-id=' + val;
                            ordering.push(val);
                        });
                        params += '&templateItemIds=' + ordering.toString();
                        params += '&ordered-child-ids=' + ordering.toString();
                        params += '&ordered-child-ids-fossil=istring%23%7BtemplateBBean.orderedChildIds%7D';
                        params += '&templateId=' + t.attr('templateid');
                        params += '&command+link+parameters%26el-binding%3Dj%2523%257BtemplateBBean.blockId%257Dnew1%26el-binding%3Dj%2523%257BtemplateBBean.templateItemIds%257D' + ordering.toString() + '%26el-binding%3Dj%2523%257BtemplateBBean.originalDisplayOrder%257D2%26Submitting%2520control%3DsaveBlockAction%26Fast%2520track%2520action%3DtemplateBBean.saveBlockItemAction=Save+item';
                        $.ajax({
                            url: "modify_block",
                            data: params,
                            type: "POST",
                            success: function(d) {
                                var id = 'input[value=' + ui.item.find('input[name=hidden-item-id]').eq(0).val() + ']:hidden';
                                ui.item.parents('.itemRow').eq(0).html($(d).find(id).parents('.itemRow').eq(0).html());
                                $(document).trigger('list.triggerSort');
                                ui.item.parents('.itemRow').eq(0).children('input[name=*hidden-item-num]').eq(0).val(currentOrder);

                                $('form[name=modify_form_rows]').ajaxSubmit({
                                    success: function(d) {
                                        if ((type != null) && (type == "simple")) {
                                            $(document).trigger('activateControls.templateItems');
                                            ui.item.parents('.itemRow').effect('highlight', 1500);
                                            ui.item.parents('.itemRow').find('.itemBlockSave').fadeOut('fast', function() {
                                                $(this).remove();
                                            });
                                        } else {
                                            $('#itemList').html($(d).find('#itemList').html());
                                            $(document).trigger('list.triggerSort', [ui]);
                                            $(document).trigger('activateControls.templateItems');
                                            $(document).trigger('close.facebox');
                                            ui.item.parents('.itemTableBlock').effect('highlight', 3500);
                                        }
										$(document).trigger('list.busy', false);
                                    }
                                });
                            }
                        });
                    }
                });
});
});
$(document).bind('list.triggerSort', function() {
var c = 0;
$(".itemList > div.itemRow").filter(':visible').each(function() {
    $(this).find('select').eq(0).val(c + 1);
    $(this).find('input[name=*hidden-item-num]').eq(0).val(c);
    c++;
});
});

$(document).bind('list.warning', function(e, ui, option, extra, err){
	if(!ui.item){
        var tempUi = ui;
        var ui = new Object()
          ui.item = tempUi;
    }
	    if (option != "noAlert") {
		$('.itemOperationsEnabled').remove();
        var error = '<div class="itemOperationsEnabled">\
                        <img src="/library/image/sakai/cancelled.gif"/>\
                        <span class="instruction">'+ err +'</span> <a href="#" id="closeItemOperationsEnabled">close</a></div>\
                        ';
		if(extra == 'block')
        	ui.item.parents('.itemLine3').prepend(error);
        $('#closeItemOperationsEnabled').click(function() {
            $(this).parent().slideUp('normal', function() {
                $(this).remove()
            });
            return false
        });
    }
});

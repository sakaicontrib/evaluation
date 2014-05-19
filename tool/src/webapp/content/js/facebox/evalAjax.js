/**
 * @author lovemorenalube
 **/

$(document).bind('activateControls.templateItems', function() {
    var log = evalTemplateUtils.debug,
    lang = evalTemplateUtils.messageLocator;
    evalTemplateLoaderEvents.bindDeleteIcons();
    evalTemplateLoaderEvents.bindRowEditPreviewIcons();

    $('a.addItem[rel=faceboxAddGroupItems]').click(function() {
        //Unbind current reveal event to avoid fckEditor error
        $(document).unbind('reveal.facebox');
        var that = $(this),
        parentObj = that.parents("div.itemRow"),
        blockId = parentObj.find("input[name=template-item-id]").val();
        var noGroupableItems = true;
        //console.log(that.attr('class'));
        var myType = $(this).parents('.itemRow').find('.itemCheckbox > input').attr('id');

        if (evalTemplateUtils.vars.groupableItems.length > 0) {
            for (var i = 0; i < evalTemplateUtils.vars.groupableItems.length; i ++) {
                type = evalTemplateUtils.vars.groupableItems[i].type;
                if ( (myType.substring(myType.indexOf('-') + 1, myType.lastIndexOf('-'))) == (type.substring(type.indexOf('-') + 1, type.lastIndexOf('-'))) ) {
                    noGroupableItems = false;
                }
            }

        }
        if (noGroupableItems) {
            alert(lang('modifytemplate.group.no.itemstoadd'));
           } else {

            $.facebox('<div id="addGroupItemDiv"></div>');
            if ($('#facebox .titleHeader').length > 0){
                $('#facebox .titleHeader').remove();
            }
            $('<h2 style="font-weight: bold;" class="titleHeader">' + lang('modifytemplate.group.add.selection') + '').insertBefore('#facebox .close'); 
            $('<div class="itemAction"> <a id="addGroupItemSelectAll" title="'+lang('modifytemplate.group.add.select.all')+'" href="#">'+lang('modifytemplate.group.add.select.all')+'</a> | <a id="addGroupItemDeselectAll" title="'+lang('modifytemplate.group.add.select.none')+'" href="#">'+lang('modifytemplate.group.add.select.none')+'</a></div>').insertBefore('#addGroupItemDiv');
            $('<div class="act">' +
              '<input type="button" id="addGroupItemSave" class="active submit" accesskey="s" value="'+lang('modifytemplate.add.item.button')+'" disabled="disabled"/>' +
              '<input type="button" onclick="$(document).trigger(\'close.facebox\')" accesskey="x" value="'+lang('general.cancel.button')+'"/>' +
              '</div>').insertAfter('#addGroupItemDiv');
            //bind new control actions
            $('#addGroupItemSelectAll').bind('click', function() {
                $('#addGroupItemDiv input[type=checkbox]').each(function() {
                    $(this).attr('checked', 'checked');
                });
                $('#addGroupItemSave').removeAttr('disabled');
            });
            $('#addGroupItemDeselectAll').bind('click', function() {
                $('#addGroupItemDiv input[type=checkbox]').each(function() {
                    $(this).removeAttr('checked');
                });
                $('#addGroupItemSave').attr('disabled', 'disabled');
            });
            $('#addGroupItemSave').bind('click', function() {
                var img = '<img src="' + $.facebox.settings.loadingImage + '"/>';
                $(this).attr('disabled', 'disabled');
                $(this).next('input').attr('disabled', 'disabled');
                $(img).insertAfter($(this).next('input'));
                var addItems = [];
                $('#addGroupItemDiv > div').each(function() {
                    if ($(this).find('input[type=checkbox]').attr('checked') === true) {
                        var itemId = $(this).find("input[name=hidden-item-id]:hidden").val();
                        addItems.push(itemId);
                    }
                });

                log.info("selected %o", addItems.toString());
                //Save new item to the group using already set EB post method
                var params = {
                    blockid : blockId,
                    additems: addItems.toString()
                },
                fnAfter = function(){
                    $.get("modify_template_items?external=false&templateId=" + $("input[name=templateId]:hidden").val(),
                            null,
                            function(data){
                                $('#itemList').html($(data).find('#itemList').html());
                                $(document).trigger('activateControls.templateItems');
                                evalTemplateOrder.initGroupableItems();
                                $(document).trigger('close.facebox');
                            });
                };
                evalTemplateData.item.saveOrder(evalTemplateUtils.pages.eb_block_edit, params, null, fnAfter);
            });
            var clone = $(this).parent().find('.itemRowBlock').eq(0).clone(),
            selectBox = $('<input name="addGroupItemCheckbox" type="checkbox" title="'+ lang('modifytemplate.item.checkbox.title') +'" value="true"/>');
            //edit extra controls
            clone.find('.itemRight').hide();
            selectBox.insertBefore(clone.find('.itemLabel'));
            // Populate groupable items into the facebox
            if (evalTemplateUtils.vars.groupableItems.length > 0) {
                for (var num = 0; num < evalTemplateUtils.vars.groupableItems.length; num ++) {
                    var type = evalTemplateUtils.vars.groupableItems[num].type;
                    if ( (myType.substring(myType.indexOf('-') + 1, myType.lastIndexOf('-'))) == (type.substring(type.indexOf('-') + 1, type.lastIndexOf('-'))) ) {
                        var itemId = evalTemplateUtils.vars.groupableItems[num].itemId,
                        otp = evalTemplateUtils.vars.groupableItems[num].otp,
                        text = evalTemplateUtils.vars.groupableItems[num].text,
                        rowId = evalTemplateUtils.vars.groupableItems[num].rowId,
                        rowNumber = evalTemplateUtils.vars.groupableItems[num].rowNumber,
                        shadow = $(clone).clone();
                        shadow.find('.itemLabel').text(rowNumber).css("font-weight","bold");
                        shadow.css('cursor', 'auto');
                        shadow.attr('oldRowId', rowId);
                        shadow.find('span').eq(1).html(text);
                        shadow.find('input').eq(0).val(itemId);
                        shadow.find('a[templateitemid]').attr('templateitemid', itemId);
                        shadow.find('a[otp]').attr('otp', otp);
                        $('#addGroupItemDiv').append(shadow);
                    }
                }
                $('input[name=addGroupItemCheckbox]').bind('click', function() {
                    var c = 0;
                    $('input[name=addGroupItemCheckbox]').each(function() {
                        if ($(this).attr('checked')) {
                            c++;
                        }
                    });
                    if (c > 0) {
                        $('#addGroupItemSave').removeAttr('disabled');
                    } else {
                        $('#addGroupItemSave').attr('disabled', 'disabled');
                    }
                });
            }
        }

        return false;
    });

    $('a.addItem[rel=faceboxAddNewGroupItem]').click(function() {
        var url = "modify_item?external=false&itemClassification=Scaled&",
        postVars = {
            groupItemId : $(this).parents(".itemRow").find("input[name=template-item-id]:hidden").val(),
            templateId  : $("input[name=templateId]:hidden").val()
        };
        
        evalTemplateFacebox.addItem( url + $.param(postVars));
        return false;
    });

    var saveButton = document.getElementById("saveReorderButton");
    saveButton.onclick = function() {
        disableOrderButtons();
        buildSortableIds();

        var order = [];
        $("#itemList > div").not('.ui-sortable-helper').each(function(){
            order.push($(this).find('a[templateitemid]').attr('templateitemid'));
        });
        var params = {
            orderedIds : order.toString()
        };

        evalTemplateData.item.saveOrder(evalTemplateUtils.pages.eb_save_order, params);
        return false;
    };

    $('a[rel=childEdit]').childEdit();

    $('.blockExpandText').toggle(
            function() {
                if ($(this).parent().parent().parent().find('.itemLine3').is(':hidden')) {
                    $(this).click();
                    return false;
                }
                var text = lang('modifytemplate.group.show');
                $(this).parent().parent().parent().find('.itemLine3').slideToggle();
                $(this).text(text);
                evalTemplateUtils.frameGrow(0);
                // save closed state
                evalTemplateUtils.closedGroup.add( $(this).parents(".itemRow").attr('name') );
                return false;
            },
            function() {
                var text = lang('modifytemplate.group.hide');
                $(this).parent().parent().parent().find('.itemLine3').slideToggle();
                $(this).text(text);
                // remove closed state
                evalTemplateUtils.closedGroup.remove( $(this).parents(".itemRow").attr('name') );
                evalTemplateUtils.frameGrow(0);
                return false;
            }
            );
    $('.more').bind('click', function() {
        if ($(this).parent().parent().find('.itemText').eq(1).find('.blockExpandText').length === 0) {
            $(this).parent().parent().find('.itemText').eq(1).find('.blockExpandText').remove();
            $(this).parent().find('.blockExpandText').clone(true).insertAfter($(this).parent().parent().find('.itemText').eq(1).find('.less'));
        }
        $(this).parent().toggle();
        $(this).parent().parent().find('.itemText').eq(1).toggle();
        evalTemplateUtils.frameGrow(0);
        return false;
    });
    $('.less').bind('click', function() {
        $(this).parent().toggle();
        $(this).parent().parent().find('.itemText').eq(0).toggle();
        return false;
    });

    refreshSort();
    //initialise the reorder dropdowns
    evalTemplateOrder.initDropDowns();

    // populate or re-populate groupable item array
    evalTemplateOrder.initGroupableItems();

    updateControlItemsTotal();
});

(function($) {
    $.fn.blockChildControls = function() {
        // iterate and bind each matched element
        return this.each(function() {
            $(this).click(function() {
                blockChildControls($(this));
                return false;
            });
        });
    };
    function blockChildControls(that) {
        that.bind('click', function() {
            that.parent().parent().find('span').eq(1).click();
            return false;
        });
    }

})(jQuery);

function truncateTextDo(string, number) {
    var trunc = string.substring(0, (number === null) ? 150 : number);
    trunc = trunc.replace(/\w+$/, '');
    return trunc;
}

function truncateText() {
    $('.itemText').each(function() {
        var that = $(this);
        that.realText = '<h4 class="itemText"><span>' + that.text() + '</span><a class="less" href="#">less<\/a></h4>';
        if(that.realText.length > 150) {
            that.html('<span>' + truncateTextDo(that.text(), null) + '</span><a class="more" href="#">...more</a>');
            $(that.realText).insertAfter(that);
            that.parent().find('.itemText').eq(1).toggle();
        }
        else{
            that.html("<span>" + that.text() + "</span>");
        }
    });
}
$('.itemBlockRow').each(function() {
    var block = $(this);
    block.expandText = ' [<a class="blockExpandText" href="#"> Show child items </a>]';
    block.html(block.html() + block.expandText);
});

var options_choose_existing_items_html_search = {
    beforeSend: function() {
        $(".form_submit").attr("disabled", "disabled");
        if (!/\S/.test($(".form_search_box").val())) {
            $(".form_search_box").focus();
            $(".form_submit").removeAttr("disabled");
            return false;
        }
    },
    success: function(data) {
        $.facebox(data);
        $("a[rel*=facebox]").facebox();
    }
};

//Update the Control Items total in dom
function updateControlItemsTotal() {
    var tNum = $('#itemList > div.itemRow:visible').get().length;
    $('[id=level-header-number]').text(tNum);
    var dummyLink = $('span[id=begin-eval-dummylink]');
    var link = $('a[id=begin_eval_link]');
    var title = dummyLink.length !== 0 ? dummyLink.text() : link.text();
    var takeEvalLink = '<a id="begin_eval_link" title="' + title + '" href="evaluation_create?reOpening=false&amp;templateId=' + $('input[name*=templateId]').val() + '">' + title + '</a>';
    if (tNum === 0) {
        if (link.length !== 0) {
            link.replaceWith('<span id="begin-eval-dummylink">' + link.text() + '</span>');
        }
    } else {
        if (dummyLink.length !== 0) {
            dummyLink.replaceWith(takeEvalLink);
        }
    }
}


/* Script adapted from: www.jtricks.com
 * Version: 20070301
 * Latest version:
 * www.jtricks.com/javascript/window/box_alone.html
 */
// Moves the box object to be directly beneath an object.
(function($) {
    //
    // plugin definition
    //
    var that,
    log = evalTemplateUtils.debug;
    $.fn.itemRemove = function(options) {
        var settings = {
            ref:    '', //the RESTful unique identifier for this component
            id:        '' //the id passed
        };

        // iterate and bind each matched element
        return this.each(function() {
            $(this).click(function() {
                var opts = $.extend(settings, options);
                opts.id = eval(opts.id);
                var t = eval(opts.text);
                opts.text = (t !== null && t.length > 20) ? truncateTextDo(t, 20) + "..." : t;
                show_hide_box(this, opts);
                that = this;
                return false;
            });
        });
    };

    //
    // private functions
    //


    function move_box(an, box, options)
    {
        var cleft = -140;

        if ((options.itemType == "blockChild") && ($.browser.msie)) {
            cleft = -290;
        }

        var ctop = -25;
        var obj = an;

        while (obj.offsetParent)
        {
            cleft += obj.offsetLeft;
            ctop += obj.offsetTop;
            obj = obj.offsetParent;
        }

        box.style.left = cleft + 'px';

        ctop += an.offsetHeight + 8;

        // Handle Internet Explorer body margins,
        // which affect normal document, but not
        // absolute-positioned stuff.
        if (document.body.currentStyle &&
            document.body.currentStyle.marginTop)
        {
            ctop += parseInt(document.body.currentStyle.marginTop, 10);
        }

        box.style.top = ctop + 'px';
        $(box).fadeIn('fast');
    }

    // Hides other alone popup boxes that might be displayed
    function hide_other_alone(obj)
    {
        if (!document.getElementsByTagName){
            return;
        }
        var all_divs = document.body.getElementsByTagName("DIV");

        for (var i = 0; i < all_divs.length; i++)
        {
            if (all_divs.item(i).style.position != 'absolute' ||
                all_divs.item(i) == obj ||
                !all_divs.item(i).alonePopupBox)
            {
                continue;
            }

            all_divs.item(i).style.display = 'none';
        }
    }

    // Shows a box if it wasn't shown yet or is hidden
    // or hides it if it is currently shown
    function show_hide_box(an, options)
    {
        if (!options.itemType){
            options.itemType = 'none';
        }
        if ((options.itemType == "blockChild")){
            $('a[templateItemId=' + options.id + ']').parents('.itemRowBlock').eq(0).css('background', '#fff');
        }
        else{
            $('a[templateItemId=' + options.id + ']').parents('.itemRow').eq(0).css('background', '#fff');
        }

        var href = an.href,
        width = 150,
        // only apply to IE

        borderStyle = '1px solid #2266AA',
        boxdiv = document.getElementById(href);

        if (boxdiv !== null)
        {
            if (boxdiv.style.display == 'none') {
                hide_other_alone(boxdiv);
                // Show existing box, move it
                // if document changed layout
                move_box(an, boxdiv, options);
                boxdiv.style.display = 'block';
            }
            else {
                // Hide currently shown box.
                //boxdiv.style.display = 'none';
                $(boxdiv).fadeOut('fast');
                $(boxdiv).remove();
            }
            return false;
        }

        hide_other_alone(null);

        // Create box object through DOM
        boxdiv = document.createElement('div');

        // Assign id equalling to the document it will show
        boxdiv.setAttribute('id', href);

        // Add object identification variable
        boxdiv.alonePopupBox = 1;

        boxdiv.style.display = 'block';
        boxdiv.style.position = 'absolute';
        boxdiv.style.width = width + 'px';
        //boxdiv.style.height = height + 'px';
        boxdiv.style.border = borderStyle;
        // boxdiv.style.textAlign = 'right';
        boxdiv.style.padding = '4px';
        boxdiv.style.background = '#FFFFFF';
        $(boxdiv).addClass('act');
        $(boxdiv).hide();
        document.body.appendChild(boxdiv);

        //var actionText = evalTemplateUtils.messageLocator('general.command.delete');
        var actionText = options.itemType.search('block') === -1 ? evalTemplateUtils.messageLocator('general.command.delete') : evalTemplateUtils.messageLocator('modifytemplate.group.ungroup');
        if ( options.itemType == 'blockChild' && options.ref == "eval-templateitem" ){
           actionText = evalTemplateUtils.messageLocator('general.command.delete');
        }

        if (options.itemType == "blockChild") {
            $('a[templateItemId=' + options.id + ']').parents('.itemRowBlock').eq(0).css('background', '#ffc');
        }
        else{
            $('a[templateItemId=' + options.id + ']').parents('.itemRow').eq(0).css('background', '#ffc');
        }

        var content = '<div class="" style="font-weight: bold;">'+actionText+' item</div><div>"' + options.text + '"</div>' +
                      '<div class="" style="float: right;">' +
                      '<input type="button" value="'+actionText+'" accesskey="s" class="removeItemConfirmYes active"/>' +
                      '<input type="button" value="Cancel" accesskey="x" class="closeImage"/>' +
                      '</div>';
        $(boxdiv).html(content);
        $('.closeImage').click(function()
        {
            show_hide_box(an, options);
        });
        $('.removeItemConfirmYes').click(function() {
            log.group("Start removing template item");
            if (options.itemType === "blockChild") {
                if ($('#closeItemOperationsEnabled').length > 0) {
                    $('#closeItemOperationsEnabled').parent().remove();
                }
                if ($('a[templateItemId=' + options.id + ']').parents('.itemTableBlock').find('div.itemRowBlock').get().length <= 2) {
                    var error = '<div class="itemOperationsEnabled">' +
                                '<img src="/library/image/sakai/cancelled.gif"/>' +
                                '<span class="instruction"></span>'+evalTemplateUtils.messageLocator('modifytemplate.group.cannot.delete.item')+' <a href="#" id="closeItemOperationsEnabled">X</a></div>';
                    $(that).parents('.itemLine3').prepend(error).effect('highlight', 1000);
                    $('#closeItemOperationsEnabled').click(function() {
                        $(this).parent().slideUp('normal', function() {
                            $(this).remove();
                        });
                        return false;
                    });
                    return false;
                }
                if( options.ref === "eval-templateitem/unblock" ){
                $.ajax({
                    url: "/direct/" + options.ref,
                    data: { itemid: options.id },
                    type: "POST",
                    beforeSend: function() {
                        doBusy($(an));
                    },
                    success: function() {
                        $.get("modify_template_items?external=false&templateId=" + $("input[name=templateId]:hidden").val(), null, function(data){ options.itemType = "block"; finish(options, data); });
                    }
                });
                }else{
                    $.ajax({
                        url: "/direct/" + options.ref + "/" + options.id + "/delete",
                        type: "DELETE",
                        beforeSend: function() {
                            doBusy($(an));
                        },
                        success: function(data) {
                            finish(options, data);
                        }
                    });
                }
            }
            else if (options.ref === 'eval-templateitem') {
                var s = 'a[templateitemid=' + options.id + ']';
                var t = $(s);
                //AJAX request is to bean because of what it returns
                $.ajax({
                    url: "remove_item",
                    data: 'templateItemId=' + options.id + '&templateId=' + t.attr('templateid') + '&command+link+parameters%26deletion-binding%3Dl%2523%257B' + t.attr('otp') + '%257D%26Submitting%2520control%3Dremove-item-command-link=Remove+Item',
                    type: "POST",
                    beforeSend: function() {
                        doBusy(t);
                    },
                    success: function(data) {
                        t.parent().parent().parent().find('.selectReorder').removeAttr('disabled');
                        //alert(data);
                        finish(options, data);
                    }
                });
            }
            else {
                log.debug("EB action for deleting id: %i", options.id );
                $.ajax({
                    url: "/direct/" + options.ref + "/" + options.id + "/delete",
                    type: "DELETE",
                    success: function(data) {
                        //alert(data);
                        finish(options, data);
                    }
                });

                

            }
            show_hide_box(an, options);
            log.groupEnd();
        });

        move_box(an, boxdiv, options);

        // The script has successfully shown the box,
        // prevent hyperlink navigation.
        return false;
    }

    function doBusy( link ){
        link.hide();
        link.parent().find('a').slice(0, 2).hide();
        link.parent().append('<img src="/library/image/sakai/spinner.gif"/>');
        link.parent().parent().parent().find('.selectReorder').attr('disabled', 'disabled');
    }

    function finish(options, d) {
        log.group("Removed item of type %s with ref of %s", options.itemType, options.ref);
        if (options.itemType == 'block') {
            $('#itemList').html($(d).find('#itemList').html());
            $(document).trigger('activateControls.templateItems');
            evalTemplateOrder.initGroupableItems();
        }else
        if (options.itemType == 'blockChild') {
            $(that).parent().parent().effect('highlight', {}, 500).fadeOut(100, function() {
                var parentItem = $(this).parents("div.itemRow");
                $(this).remove();
                $(document).trigger('block.triggerChildrenSort', [parentItem]);
                updateControlItemsTotal();
                evalTemplateOrder.initGroupableItems();
            });
        }else
        if (options.ref == 'eval-templateitem') {
            $(that).parents("div.itemRow").effect('highlight', {}, 500).fadeOut(100, function() {
                $(this).remove();
                updateControlItemsTotal();
                evalTemplateSort.updateLabelling();
                evalTemplateSort.updateDropDownMax();
                //Updating labelling leaves Save Order active. Deactivate it now.
                disableOrderButtons();
                evalTemplateOrder.initGroupableItems();
            });
        }
        log.groupEnd();
        return false;
    }

})(jQuery);	

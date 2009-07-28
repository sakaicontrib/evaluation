/**
 * @author lovemorenalube
 **/

$(document).bind('activateControls.templateItems', function(e, opt) {
    var groupableItems = new Array();
    $('a[rel=remove]').itemRemove({
        ref:    'eval-templateitem',
        id:     '$(this).attr("templateitemid")',
        text:   '$(this).parents("div.itemLine2").eq(0).find("h4.itemText:visible").text()'
    });
    $('a[rel=childRemove]').itemRemove({
        ref:    'eval-templateitem',
        id:        '$(this).attr("templateitemid")',
        itemType: 'blockChild',
        text:   '$(this).parents("div.itemRowBlock").eq(0).find("span.text:visible").text()'
    });
    $('a[rel=unblock]').itemRemove({
        ref:    'eval-templateitem',
        id:        '$(this).attr("templateitemid")',
        itemType: 'block',
        text:   '$(this).parents("div.itemLine2").eq(0).find("h4.itemText:visible").text()'
    });

    $('a[rel=facebox]').facebox();
    $('a[rel=faceboxGrid]').faceboxGrid();
    $('a.addItem[rel=faceboxAddGroupItems]').click(function() {
        //Unbind current reveal event to avoid fckEditor error
        $(document).unbind('reveal.facebox');
        var that = $(this);
        var noGroupableItems = true;
        //console.log(that.attr('class'));
        var myType = $(this).parents('.itemRow').find('.itemCheckbox > input').attr('id');

        if (groupableItems.length > 0) {
            for (i in groupableItems) {
                var type = groupableItems[i].type;
                if (
                        (myType.substring(myType.indexOf('-') + 1, myType.lastIndexOf('-')))
                                ==
                        (type.substring(type.indexOf('-') + 1, type.lastIndexOf('-')))

                        ) {
                    noGroupableItems = false;
                }
            }

        }
        if (noGroupableItems) {
            $.facebox('There are no items you can add to this group.');      //TODO:i8N
            if ($('#facebox .titleHeader').length > 0)
                $('#facebox .titleHeader').remove();
            $('<h2 style="font-weight: bold;" class="titleHeader">Select existing items to add to a group</h2>').insertBefore('#facebox .close');   //TODO:i8N

        } else {

            $.facebox('<div id="addGroupItemDiv"></div>');
            if ($('#facebox .titleHeader').length > 0)
                $('#facebox .titleHeader').remove();
            $('<h2 style="font-weight: bold;" class="titleHeader">Select existing items to add to a group</h2>').insertBefore('#facebox .close'); //TODO:i8N
            $('<div class="itemAction"> <a id="addGroupItemSelectAll" title="Select all" href="#">Select all</a> | <a id="addGroupItemDeselectAll" title="Deselect all" href="#">Deselect all</a></div>').insertBefore('#addGroupItemDiv'); //TODO:i8N
            $('\
			<div class="act">\
				<input type="button" id="addGroupItemSave" class="active submit" accesskey="s" value="Add" disabled="disabled"/>\
				<input type="button" onclick="$(document).trigger(\'close.facebox\')" accesskey="x" value="Cancel"/> \
			</div>\
			').insertAfter('#addGroupItemDiv');    //TODO:i8N
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
                var selectedItems = new Array();
                $('#addGroupItemDiv > div').each(function() {
                    if ($(this).find('input[type=checkbox]').attr('checked') == true) {
                        $(this).find('.itemRight').show();
                        $(this).appendTo(that.parent());
                        selectedItems.push($(this).attr('oldRowId'));
                    }
                });
                for (i in groupableItems) {
                    for (l in selectedItems) {
                        if (groupableItems[i].rowId == selectedItems[l]) {
                            $('div[id=' + groupableItems[i].rowId + ']').remove();
                        }
                    }
                }
                $(document).trigger('block.triggerChildrenSort', [that]);
                $(document).trigger('list.triggerSort');
                //$(document).trigger('activateControls.templateItems');
                $.facebox('\
                                                  <div><img src="' + $.facebox.settings.loadingImage + '"/>  Saving... \
			                </div> \
			                <div class="footer"> \
			                   Please do not close this window.  \
			                </div> ');
                $(document).trigger('block.saveReorder', [that]);
                $(document).trigger('close.facebox');

            });
            var clone = $(this).parent().find('.itemRowBlock').eq(0).clone();
            //edit extra controls
            clone.find('.itemRight').remove();
            clone.find('.itemLabel').html('<input name="addGroupItemCheckbox" type="checkbox" title="Mark item to use in a group" value="true"/>');//TODO:i8N
            //TODO:Strip out unneeded . in string

            if (groupableItems.length > 0) {
                for (i in groupableItems) {
                    var type = groupableItems[i].type;
                    if (
                            (myType.substring(myType.indexOf('-') + 1, myType.lastIndexOf('-')))
                                    ==
                            (type.substring(type.indexOf('-') + 1, type.lastIndexOf('-')))

                            ) {
                        var itemId = groupableItems[i].itemId;
                        var otp = groupableItems[i].otp;
                        var text = groupableItems[i].text;
                        var rowId = groupableItems[i].rowId;
                        var shadow = $(clone).clone();
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

    });

    var saveButton = document.getElementById("saveReorderButton");
    saveButton.onclick = function() {
        disableOrderButtons();
        buildSortableIds();
        $(saveButton.form).ajaxSubmit();
        return false;
    };

    $('a[rel=childEdit]').childEdit();

    $('.blockExpandText').toggle(

            function() {
                if ($(this).parent().parent().parent().find('.itemLine3').is(':hidden')) {
                    $(this).click();
                    return false;
                }
                text = '[Show child items]';
                $(this).parent().parent().parent().find('.itemLine3').slideToggle();
                $(this).text(text);
                evalTemplateUtils.frameGrow(0);
                return false;
            },
            function() {
                text = '[Hide child items]';
                $(this).parent().parent().parent().find('.itemLine3').slideToggle();
                $(this).text(text);
                evalTemplateUtils.frameGrow(0);
                return false;
            }
            );
    $('.more').bind('click', function() {
        if ($(this).parent().parent().find('.itemText').eq(1).find('.blockExpandText').length == 0) {
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
    EvalSystem.decorateReorderSelects("", $("#itemList > div").get().length);
    // populate or re-populate groupable item array
    $('div.itemList > div:visible').each(function() {
        if ($(this).children('.itemLine3').length == 0) {
            var t = "";
            if ($(this).find('.itemText > span').eq(1).text() == "")
                t = $(this).find('.itemText > span').eq(0).html();
            else
                t = $(this).find('.itemText > span').eq(1).html();
            var object = {
                text:   t,
                type:   ($(this).find('.itemCheckbox > input').attr('id') ? $(this).find('.itemCheckbox > input').attr('id') : "000"),
                itemId: $(this).find('a[templateitemid]').eq(0).attr('templateitemid'),
                otp:    $(this).find('a[otp]').eq(0).attr('otp'),
                rowId:  $(this).attr('id')
            };
            groupableItems.push(object);
        }
    });
    updateControlItemsTotal();
});

(function($) {
    $.fn.blockChildControls = function() {
        var opts = $.extend({});
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
    trunc = string.substring(0, (number == null) ? 150 : number);
    trunc = trunc.replace(/\w+$/, '');
    return trunc;
}

function truncateText() {
    $('.itemText').each(function() {
        that = $(this);
        that.realText = '<h4 class="itemText"><span>' + that.text() + '</span><a class="less" href="#">less<\/a></h4>';
        if (that.realText.length > 150) {
            that.html('<span>' + truncateTextDo(that.text()) + '</span><a class="more" href="#">...more</a>');
            $(that.realText).insertAfter(that);
            that.parent().find('.itemText').eq(1).toggle();
        }
        else
            that.html("<span>" + that.text() + "</span>");
    });
}
$('.itemBlockRow').each(function() {
    block = $(this);
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
    var title = dummyLink.length != 0 ? dummyLink.text() : link.text();
    var takeEvalLink = '<a id="begin_eval_link" title="' + title + '" href="evaluation_create?reOpening=false&amp;templateId=' + $('input[name*=templateId]').val() + '">' + title + '</a>';
    if (tNum == 0) {
        if (link.length != 0) {
            link.replaceWith('<span id="begin-eval-dummylink">' + link.text() + '</span>');
        }
    } else {
        if (dummyLink.length != 0) {
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
    var that;
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
                opts.text = (t != null && t.length > 20) ? truncateTextDo(t, 20) + "..." : t;
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
            document.body.currentStyle['marginTop'])
        {
            ctop += parseInt(
                    document.body.currentStyle['marginTop']);
        }

        box.style.top = ctop + 'px';
        $(box).fadeIn('fast');
    }

    // Hides other alone popup boxes that might be displayed
    function hide_other_alone(obj)
    {
        if (!document.getElementsByTagName)
            return;

        var all_divs = document.body.getElementsByTagName("DIV");

        for (i = 0; i < all_divs.length; i++)
        {
            if (all_divs.item(i).style.position != 'absolute' ||
                all_divs.item(i) == obj ||
                !all_divs.item(i).alonePopupBox)
            {
                continue;
            }

            all_divs.item(i).style.display = 'none';
        }
        return;
    }

    // Shows a box if it wasn't shown yet or is hidden
    // or hides it if it is currently shown
    function show_hide_box(an, options)
    {
        if (!options.itemType)
            options.itemType = 'none';
        if ((options.itemType == "blockChild"))
            $('a[templateItemId=' + options.id + ']').parents('.itemRowBlock').eq(0).css('background', '#fff');
        else
            $('a[templateItemId=' + options.id + ']').parents('.itemRow').eq(0).css('background', '#fff');

        var href = an.href;
        var width = 150;
        // only apply to IE

        var height = 0;
        var borderStyle = '1px solid #2266AA';
        var boxdiv = document.getElementById(href);

        if (boxdiv != null)
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

        var offset = 0,
                actionText = options.itemType=='block'?'Ungroup':'Delete';
        if (options.itemType == "blockChild") {
            $('a[templateItemId=' + options.id + ']').parents('.itemRowBlock').eq(0).css('background', '#ffc');
        }
        else{
            $('a[templateItemId=' + options.id + ']').parents('.itemRow').eq(0).css('background', '#ffc');
        }

        var content = '\
                            <div class="" style="font-weight: bold;">'+actionText+' item</div><div>"' + options.text + '"</div>\
                            <div class="" style="float: right;">\
                            <input type="button" value="'+actionText+'" accesskey="s" class="removeItemConfirmYes active"/>\
                            <input type="button" value="Cancel" accesskey="x" class="closeImage"/>\
                            </div>\
                            ';
        $(boxdiv).html(content);
        $('.closeImage').click(function()
        {
            show_hide_box(an, options);
        });
        $('.removeItemConfirmYes').click(function() {
            if (options.itemType == "blockChild") {
                if ($('#closeItemOperationsEnabled').length > 0) {
                    $('#closeItemOperationsEnabled').parent().remove();
                }
                if ($('a[templateItemId=' + options.id + ']').parents('.itemTableBlock').find('div.itemRowBlock').get().length <= 2) {
                    var error = '<div class="itemOperationsEnabled">\
            <img src="/library/image/sakai/cancelled.gif"/>\
            <span class="instruction">Sorry, groups have to have at least TWO items in them.</span> <a href="#" id="closeItemOperationsEnabled">close</a></div>\
            ';
                    $(that).parents('.itemLine3').prepend(error).effect('highlight', 1500);
                    $('#closeItemOperationsEnabled').click(function() {
                        $(this).parent().slideUp('normal', function() {
                            $(this).remove()
                        });
                        return false
                    });
                    return false;
                }
                $.ajax({
                    url: "/direct/" + options.ref + "/" + options.id + "/delete",
                    type: "DELETE",
                    success: function(data) {
                        //alert(data);
                        finish(options, data);
                    }
                });
            }
            else if (options.ref == 'eval-templateitem') {
                var s = 'a[templateitemid=' + options.id + ']';
                var t = $(s);
                $.ajax({
                    url: "remove_item",
                    data: 'templateItemId=' + options.id + '&templateId=' + t.attr('templateid') + '&command+link+parameters%26deletion-binding%3Dl%2523%257B' + t.attr('otp') + '%257D%26Submitting%2520control%3Dremove-item-command-link=Remove+Item',
                    type: "POST",
                    beforeSend: function() {
                        //t.parent().parent().parent().parent().css('background', '#eee');
                        t.hide();
                        t.parent().find('a').slice(0, 2).hide();
                        t.parent().append('<img src="/library/image/sakai/spinner.gif"/>');
                        t.parent().parent().parent().find('.selectReorder').attr('disabled', 'disabled');
                    },
                    success: function(data) {
                        t.parent().parent().parent().find('.selectReorder').removeAttr('disabled');
                        //alert(data);
                        finish(options, data);
                    }
                });
            }
            else {
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
        });

        move_box(an, boxdiv, options);

        // The script has successfully shown the box,
        // prevent hyperlink navigation.
        return false;
    }

    function finish(options, d) {
        if (options.itemType == 'block') {
            $('#itemList').html($(d).find('#itemList').html());
            $(document).trigger('activateControls.templateItems');
            return false;
        }
        if (options.itemType == 'blockChild') {
            $(that).parent().parent().effect('highlight', {}, 1000).fadeOut(500, function() {
                $(document).trigger('block.triggerChildrenSort', [$(this)]);
                $(this).remove();
                updateControlItemsTotal();
            });
            return false;
        }
        if (options.ref == 'eval-templateitem') {
            $(that).parent().parent().parent().parent().effect('highlight', {}, 1000).fadeOut(500, function() {
                $(this).remove();
                updateControlItemsTotal();
                var list = $("#itemList > div").get();
                for (var i = 0; i < list.length; i++) {
                    if (list[i].id) {
                        setIndex(list[i].id, i);
                    }
                }
            });

            //do more stuff here
        }

        return false;
    }

})(jQuery);	

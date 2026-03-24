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
/**
 * Sakai Evaluation System project
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009, 2010, 2011, 2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * - Aaron Zeckoski (azeckoski)
 **********************************************************************************/

// @author lovemorenalube

$(document).bind('activateControls.templateItems', function() {
    var log = evalTemplateUtils.debug,
    lang = evalTemplateUtils.messageLocator;
    evalTemplateLoaderEvents.bindDeleteIcons();
    evalTemplateLoaderEvents.bindRowEditPreviewIcons();

    $("#saveReorderButton").click(function(){
        disableOrderButtons();
        buildSortableIds();

        var order = [];
        var $items = $("#itemList > div").not('.ui-sortable-helper');
        if ($items.length > 0) {
            // only run this when there are items to sort
            $items.each(function(){
                order.push($(this).find('input[name=template-item-id]:hidden').val());
            });
            var params = {
                orderedIds : order.toString()
            };
            evalTemplateData.item.saveOrder(evalTemplateUtils.pages.eb_save_order, params);
        }
        return false;
    });

    //bind grouping/block button
    $('#createBlockBtn').click(evalTemplateLoaderEvents.block.extractSelectedItems);
    //bind groupable checkboxes
    $('input[name$=block-checkbox]').click(evalTemplateLoaderEvents.block.countCheckBox);

    evalTemplateLoaderEvents.bindGroupParentTextControls();

    refreshSort();
    //initialise the reorder dropdowns
    evalTemplateOrder.initDropDowns();

    // populate or re-populate groupable item array
    evalTemplateOrder.initGroupableItems();

    updateControlItemsTotal();
});

function truncateTextDo(string, number) {
    var trunc = jQuery.trim(string);
    trunc = trunc.substring(0, (number === null) ? 150 : number);
    trunc = jQuery.trim(trunc);
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

    function getItemTarget(options) {
        if (options.itemType == "blockChild") {
            return $('div.itemRowBlock[id$=:itemRowBlock:'+options.id+':]');
        }
        return $('input[name=template-item-id][value='+options.id+']:hidden').parents('.itemRow');
    }

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
        var itemTarget = getItemTarget(options);
        itemTarget.removeClass('item-delete-target--active');

        var href = an.href,
        width = 150,
        // only apply to IE
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
        // boxdiv.style.textAlign = 'right';
        boxdiv.style.padding = '4px';
        $(boxdiv).addClass('act eval-template-popup');
        $(boxdiv).hide();
        document.body.appendChild(boxdiv);

        //var actionText = evalTemplateUtils.messageLocator('general.command.delete');
        var actionText = options.itemType.search('block') === -1 ? evalTemplateUtils.messageLocator('general.command.delete') : evalTemplateUtils.messageLocator('modifytemplate.group.ungroup');
        if ( options.itemType == 'blockChild' && options.ref == "eval-templateitem" ){
           actionText = evalTemplateUtils.messageLocator('general.command.delete');
        }

        itemTarget.addClass('item-delete-target--active');

        var content = '<div class="" style="font-weight: bold;">'+actionText+' item</div><div>"' + options.text + '"</div>' +
                      '<div class="" style="float: right;">' +
                      '<input type="button" value="'+actionText+'" accesskey="s" class="removeItemConfirmYes active"/> ' +
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
                if ($('div.itemRowBlock[id$=:itemRowBlock:'+options.id+':]').parents('.itemTableBlock').find('div.itemRowBlock').get().length <= 2) {
                    var error = '<div class="item-operations item-operations--enabled alert alert-warning d-flex flex-wrap align-items-center gap-2 py-2 px-3" role="alert">' +
                                '<span class="bi bi-x-circle" aria-hidden="true"></span>' +
                                '<span class="visually-hidden">Error</span> ' +
                                '<span class="instructionText"></span>' + evalTemplateUtils.messageLocator('modifytemplate.group.cannot.delete.item') + ' <a href="#" id="closeItemOperationsEnabled">x</a></div>';
                    $(that).parents('.itemLine3').prepend(error).effect('highlight', 1000);
                    $('#closeItemOperationsEnabled').click(function() {
                        $(this).parent().slideUp('normal', function() {
                            $(this).remove();
                        });
                        return false;
                    });
                    $('.closeImage').click();
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
                        window.location.reload(true);
                        //$.get("modify_template_items?external=false&templateId=" + $("input[name=templateId]:hidden").val(), null, function(data){ options.itemType = "block"; finish(options, data); });
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
        link.parents('.itemLine2').find('select.selectReorder').attr('disabled', 'disabled');
    }

    function finish(options, d) {
        log.group("Removed item of type %s with ref of %s", options.itemType, options.ref);
        if (options.itemType == 'block') {
            window.location.reload(true);
            /*$('#itemList').html($(d).find('#itemList').html());
            $(document).trigger('activateControls.templateItems');
            evalTemplateOrder.initGroupableItems();*/
        }else
        if (options.itemType == 'blockChild') {
            $(that).parents('.itemRowBlock').effect('highlight', {}, 500).fadeOut(100, function() {
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

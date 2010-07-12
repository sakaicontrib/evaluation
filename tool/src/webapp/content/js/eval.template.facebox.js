// Eval Template Facebox JS for extending the facebox functionality
// @author lovemore.nalube@uct.ac.za

var evalTemplateFacebox = (function() {

    //backup the facebox functions
    var origionalFBfunctions = {
        loading: $.facebox.loading,
        close: $.facebox.close,
        reveal: $.facebox.reveal
    };

    var initFunction = function() {
        //Override FB settings or Extend methods/actions

        $.facebox.settings.opacity = 0.1;
        $.facebox.settings.overlay = true;
        $.facebox.settings.loadingImage = '/library/image/sakai/spinner.gif';
        $.facebox.settings.closeImage = '/library/image/sakai/cross.png';

        $.facebox.settings.elementToUpdate = null; //this is to store the parent element of item being edited to update after an edit via ajax
        $.facebox.saveOrder = function(f) {
            $('#saveReorderButton').click();
            $.facebox({ajax:(f)});
        };
        $.facebox.settings.faceboxHtml = '<div id="facebox" style="display:none;">' +
                                         '<div class="popup"> ' +
                                         '<table width="700px"> ' +
                                         '<tbody> ' +
                                         '<tr>' +
                                         '<td class="tl"/><td class="b"/><td class="tr"/>' +
                                         '</tr>' +
                                         '<tr>' +
                                         '<td class="b"/>' +
                                         '<td class="body">' +
                                         '<div class="header breadCrumb" style="display:block">' +
                                         '<h2 id="titleHeader" class="titleHeader">&nbsp;</h2>' +
                                         '<a class="close" href="#" accesskey="x"><img class="close_image" title="close"/></a></div>' +
                                         '<div style="display: none" class="results"></div>' +
                                         '<div class="content">' +
                                         '</div>' +
                                         '</td>' +
                                         '<td class="b"/>' +
                                         '</tr>' +
                                         '<tr>' +
                                         '<td class="bl"/><td class="b"/><td class="br"/>' +
                                         '</tr>' +
                                         '</tbody>' +
                                         '</table>' +
                                         '</div>' +
                                         '</div>';

        $.facebox.loading = function() {
            //store original frame height
            var frameHeight = document.body.clientHeight;
            if (frameHeight) {
                evalTemplateUtils.frameSize = frameHeight;
            }
            origionalFBfunctions.loading();
            $('#saveReorderButton').click();
            $('#facebox').css({
                top:    evalTemplateUtils.frameScrollHeight,
                left:    $(window).width() / 2 - ($('#facebox table').width() / 2)
            }).show();
            $('#facebox_overlay').unbind('click');
        };

        $.facebox.reveal = function(data, klass) {
            $('#facebox .content').empty();
            $("#facebox .results").empty();
            var fbCssLeft = $('#facebox').css('left');
            origionalFBfunctions.reveal(data, klass);
            //restore left css value
            $('#facebox').css('left', fbCssLeft);
            evalTemplateUtils.frameGrow(450);
            //bind event handler for FB form buttons
            //bind the close button
            $('#facebox .close').unbind('click');
            $('#facebox .close').bind('click', function() {
                $.facebox.close();
            });
        };

        $.facebox.close = function() {
            evalTemplateUtils.frameShrink(0);
            origionalFBfunctions.close();
            evalTemplateUtils.debug.info("closing facebox");
            evalTemplateFacebox.fbResetClasses();
            $('#facebox table').attr('width', 700);
            $('#facebox .body').css('width', 660);
            $('#facebox .header').eq(0).show();
            $.facebox.settings.elementToUpdate = null;
            evalTemplateData.setCurrentRow(undefined);
            return false;
        };

        $.facebox.setHeader = function(page_type) {
            var pageTitle = "";
            if(page_type === evalTemplateUtils.pages.preview_item_page){
                pageTitle = evalTemplateUtils.messageLocator("previewitem.page.title");
            }else
            if(page_type === evalTemplateUtils.pages.modify_block_page){
                pageTitle = evalTemplateUtils.messageLocator("modifyblock-page-title");
            }else
            if(page_type === evalTemplateUtils.pages.modify_item_page){
                pageTitle = evalTemplateUtils.messageLocator("modifyitem.page.title");
            }else
            if(page_type === evalTemplateUtils.pages.modify_template_page){
                pageTitle = evalTemplateUtils.messageLocator("modifytemplatetitledesc.page.title");
            }else
            if(page_type === evalTemplateUtils.pages.choose_expert_page){
                pageTitle = evalTemplateUtils.messageLocator("expert.expert.items");
            }

            $("h2.pageHeaderOnPage").hide();

            if (pageTitle !== "" && $("#facebox .titleHeader").length > 0) {
                $("#facebox .titleHeader").text(pageTitle);
            }

            evalTemplateUtils.debug.info("Page title for %s is %s", page_type, pageTitle);
        };

        $.facebox.setBtn = function(obj, form) {
            var btn = '<input type="button" class="active" accesskey="s" />';
            $(btn).insertBefore($("#facebox .close"));
            $("#facebox .header .active").val(obj);
            $("#facebox .header .active").click(function() {
                $(form).trigger('click');
            });
        };

        $.facebox.showResponse = function(entityCat, id) {
            evalTemplateData.fillActionResponse(entityCat, id);
        };

    };

    return {
        init: function() {
            initFunction();
        },
        fbResetClasses: function() {
            $("#itemList").find(".editing").attr("class", "itemRow");
        },
        addItem: function(url) {
            evalTemplateUtils.debug.info("Trying to fetch: %s", url);
            //Unbind current reveal event
            $(document).unbind('reveal.facebox');
            //Check for after load type of event to call
            var pageType = undefined,
                    revealFunction;
            pageType = evalTemplateUtils.getPageType(url);
            if (typeof pageType !== "undefined") {
                //Bind new reveal event
                if (pageType === evalTemplateUtils.pages.modify_item_page) {
                    revealFunction = evalTemplateLoaderEvents.modify_item;
                } else if (pageType === evalTemplateUtils.pages.modify_template_page) {
                    revealFunction = evalTemplateLoaderEvents.modify_template;
                } else if (pageType === evalTemplateUtils.pages.choose_existing_page) {
                    //Redirect
                    window.location = url;
                    return false;
                } else if (pageType === evalTemplateUtils.pages.choose_expert_page){
                    revealFunction = evalTemplateLoaderEvents.choose_expert_category;
                }
                $(document).bind('reveal.facebox', function() {
                    if (typeof revealFunction !== "undefined") {
                        revealFunction();
                    }
                });
            }
            $.facebox({ajax: url});
        }
    };


})($);

/*This is a fix for the Interface Highlight compatibility bug [added by lovemore.nalube@uct.ac.za]
 * see http://groups.google.com/group/jquery-en/browse_thread/thread/d094a3f299055a99
 */
( function($) {
    $.dequeue = function(a, b) {
        return $(a).dequeue(b);
    };
})($);

/**
 links with the rel=faceboxGrid have pre-click events attached to them
 **/

(function($) {
    function setPreClick(that) {
        that.each(function() {
            $(this).bind('click', function(e) {
                //Save current iframe height position
                evalTemplateUtils.frameScrollHeight = e.pageY;
                //Unbind current reveal event
                $(document).unbind('reveal.facebox');
                //Check for after load type of event to call
                var pageType = undefined,
                        revealFunction;
                pageType = evalTemplateUtils.getPageType(this.href);
                if (typeof pageType !== "undefined") {
                    //Bind new reveal event
                    if (pageType === evalTemplateUtils.pages.modify_item_page) {
                        revealFunction = evalTemplateLoaderEvents.modify_item;
                    } else if (pageType === evalTemplateUtils.pages.modify_template_page) {
                        revealFunction = evalTemplateLoaderEvents.modify_template;
                    }else if (pageType === evalTemplateUtils.pages.modify_block_page) {
                        revealFunction = evalTemplateLoaderEvents.modify_block;
                    } else if (pageType === evalTemplateUtils.pages.preview_item_page){
                        revealFunction = evalTemplateLoaderEvents.preview_item;
                    }
                    $(document).bind('reveal.facebox', function() {
                        if (typeof revealFunction !== "undefined") {
                            revealFunction();
                        }
                    });
                }
                if ($(this).attr("rel") === "faceboxGrid" && pageType !== evalTemplateUtils.pages.modify_template_page) {
                    var itemRowBlock = $(this).parents('.itemRow');
                    evalTemplateData.setCurrentRow(itemRowBlock);
                    evalTemplateFacebox.fbResetClasses();
                    $(this).parent().parent().parent().parent().attr("class", "editing");
                    $.facebox.settings.elementToUpdate = itemRowBlock;
                }
                $.facebox({ajax: this.href});
                return false;
            });
        });
    }

    function setChildGroupClick(that) {
        that.each(function() {
            $(this).bind('click', function(e) {
                //Save current iframe height position
                evalTemplateUtils.frameScrollHeight = e.pageY;
                //Unbind current reveal event
                $(document).unbind('reveal.facebox');
                var itemRowBlock = $(this).parents('.itemRowBlock'),
                    params = {
                    templateItemId: itemRowBlock.find('input[name*=hidden-item-id]').val(),
                    templateId: $('input[name=templateId]').val(),
                    itemClassification: "Block",
                    groupItemId: -1
                },
                        url = 'modify_item?' + $.param(params);
                $.facebox.settings.elementToUpdate = "block";
                evalTemplateData.setCurrentRow(itemRowBlock);               

                $(document).bind('reveal.facebox', function() {
                    evalTemplateLoaderEvents.modify_item();
                });
                $.facebox({ ajax: url });
                return false;
            });
        });
    }

    $.fn.faceboxGrid = function() {
        setPreClick($(this));
    };

    $.fn.childEdit = function() {
        setChildGroupClick($(this));
    };
})($);
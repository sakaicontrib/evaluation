// Eval Template Modify JS for Server Ajax transactions
// @author lovemore.nalube@uct.ac.za

var evalTemplateData = (function() {
    //Private data

   function submitFCKform(form, textarea, target, btn) {
            evalTemplateUtils.debug.group("Starting Fn submitFCKform", [form, textarea, target, btn]);
        var rowId = $( $.facebox.defaults.objToUpdate ).attr("id");  //get the row Id of the currently selected eval template item
            evalTemplateUtils.debug.info("Selected row %d", rowId);
        var img = new Image();
        img.src = $.facebox.defaults.loadingImage;
        var templateItemId = $(form).find('input[@name*=templateItemId]').attr('value');
            evalTemplateUtils.debug.info("Saving item %i", templateItemId);
        var formAsArray = $(form).formToArray();
        var FCKeditorValue = null;
        try {
            if (FCKeditorAPI) {
                FCKeditorValue = FCKeditorAPI.GetInstance(textarea).GetHTML(); //Actual editor textarea value
            }
        }
        catch(e) {
                evalTemplateUtils.debug.error('Check if you have imported FCKeditor.js Error: FCKeditorAPI not found. ', e);
            //TODO: Update UI - tell user that saving will not be done.
            return false;
        }
        //Validate text
        if (FCKeditorValue === null || FCKeditorValue.length < 0) {
            alert('You must fill in the title.'); //TODO: i18n this & make it unobtrusive
            return false;
        }

        //iterate through returned formToArray elements and replace input value with editor value
        for (var i = 0; i < formAsArray.length; i++) {
            if ($(formAsArray[i]).attr('name') == textarea) {
                $(formAsArray[i]).attr('value', FCKeditorValue);
            }
        }
        $.ajax({
            type: 'POST',
            url: target,
            data: formAsArray,
            dataType: "html",
            beforeSend: function() {
                btn.parent().parent().find('input').each(function() {
                    $(this).attr('disabled', 'disabled');
                });
                FCKeditorAPI.GetInstance(textarea).disabled;
                btn.parent().append(img);

            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                return false;
            },
            success: function(d) {
                $(document).trigger('close.facebox');
                if (form == '#blockForm' || form == '#item-form') {
                    $('#itemList').html($(d).find('#itemList').html());
                    $(document).trigger('activateControls.templateItems');
                    return false;
                }
                $.ajax({
                    url: evalTemplateUtils.entity.getTemplateItemURL(templateItemId),
                    dataType: 'xml',
                    cache: false,
                    beforeSend: function() {
                        $(jQuery.facebox.defaults.objToUpdate).find('.itemText span').html(img);
                    },
                    success: function(msg) {
                        var realText = $(msg).find('itemText').text();
                        var shortText = null;
                        var that = $(jQuery.facebox.defaults.objToUpdate).find('.itemText').eq(0);

                        if (realText.length > 150) {
                            that.realText = '<h4 class="itemText"><span>' + realText + '</span><a class="less" href="#">less<\/a></h4>';
                            $('body').append('<div id="shortText" style="display:none;">' + truncateTextDo(realText) + '</div>');
                            $('body').append('<div id="realText" style="display:none;">' + realText + '</div>');
                            that.html('<span>' + $('#shortText').text() + '</span><a class="more" href="#">...more</a>');
                            that.parent().find('.itemText').eq(1).remove();
                            try {
                                that.hide();
                                $('<h4 class="itemText"><span>' + $('#realText').text() + '</span><a class="less" href="#">less<\/a></h4>').insertAfter(that);
                            } catch(e) {
                            }
                            $('.more').bind('click', function() {
                                $(this).parent().toggle();
                                $(this).parent().parent().find('.itemText').eq(1).toggle();
                                frameGrow();
                                return false;
                            });
                            $('.less').bind('click', function() {
                                $(this).parent().toggle();
                                $(this).parent().parent().find('.itemText').eq(0).toggle();
                                return false;
                            });
                        } else {
                            that.find('span').html(realText);
                        }
                        $(document).trigger('activateControls.templateItems');
                    }
                });
            }
        });
       evalTemplateUtils.debug.groupEnd();
    }

    //Public data
    return {
        ajaxDelete: function() {

        },
        ajaxGet: function() {

        },
        ajaxPost: function(){

        },
        //Specific methods
        postFCKform: function(form, textarea, target, btn){
            submitFCKform(form, textarea, target, btn);
        }
    };
})($);
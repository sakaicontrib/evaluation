// Eval Template Modify JS for Server Ajax transactions
// @author lovemore.nalube@uct.ac.za

var evalTemplateData = (function() {
    //Private data


    var _postFCKform = function(form, textarea, target, btn) {
        evalTemplateUtils.debug.group("Starting Fn submitFCKform", [form, textarea, target, btn]);
        evalTemplateUtils.debug.time("submitFCKform");
        var img = new Image(),
                templateItemId = $(form).find('input[@name*=templateItemId]').attr('value'),
                formAsArray = $(form).formToArray(),
                fckEditor = null,
                fckEditorValue = null;
        img.src = $.facebox.settings.loadingImage;
        evalTemplateUtils.debug.info("Saving item %i", templateItemId);
        try {
            if (typeof FCKeditorAPI !== "undefined" && textarea !== null) {
                fckEditor = FCKeditorAPI.GetInstance(textarea);
                fckEditorValue = fckEditor.GetHTML(); //Actual editor textarea value
                evalTemplateUtils.debug.info("User entered: %s ( from DOM object %o )", fckEditorValue, fckEditor);
            }
        }
        catch(e) {
            evalTemplateUtils.debug.error('Check if you have imported FCKeditor.js Error: FCKeditorAPI not found. ', e);
            //TODO: Update UI - tell user that saving will not be done.
            return false;
        }
        //Validate text
        if (fckEditorValue === null || fckEditorValue.length === 0) {
            alert( evalTemplateUtils.messageLocator("general.blank.required.field.user.message",
                                    evalTemplateUtils.messageLocator('modifytemplatetitledesc.title.header')));
            return false;
        }

        //iterate through returned formToArray elements and replace input value with editor value
        for (var i = 0; i < formAsArray.length; i++) {
            if ($(formAsArray[i]).attr('name') === textarea) {
                $(formAsArray[i]).attr('value', fckEditorValue);
            }
        }
        $.ajax({
            type: evalTemplateData.constants.rest_Post,
            url: target,
            data: formAsArray,
            dataType: "html",
            beforeSend: function() {
                //Disable the form in the 
                $('#facebox input').each(function() {
                    $(this).attr('disabled', 'disabled');
                });
                $("#facebox option").each(function(){
                    this.disabled = true;
                });
                try{ //if fckEditor is in source mode at this time, an exception could occur: EVALSYS-836
                fckEditor.EditorDocument.body.disabled = true;
                }catch(e){};
                btn.parent().append(img);
            },
            success: function(d) {
                var successDOM = $(d);
                //Check if there is a server-side error or an alert
                if(evalTemplateData.showRSFMessage(successDOM)){
                    //error happened. STOP and re-enable edit controls
                    //enable the form in the
                    $('#facebox input').each(function() {
                        $(this).removeAttr('disabled');
                    });
                    $("#facebox option").each(function(){
                        this.disabled = false;
                    });
                    try{ //if fckEditor is in source mode at this time, an exception could occur: EVALSYS-836
                        fckEditor.EditorDocument.body.disabled = false;
                    }catch(e){};
                    $("#facebox .act img").remove();
                return false;
                }

                $(document).trigger('close.facebox');
                if (form == '#blockForm' || form == '#item-form') {
                    $('#itemList').html(successDOM.find('#itemList').html());
                    $(document).trigger('activateControls.templateItems');
                    evalTemplateUtils.debug.warn(" Updated row %o", $.facebox.settings.elementToUpdate);

                    // restore closed group items
                    for ( var closedIndex = 0; closedIndex < evalTemplateUtils.closedGroup.get.length; closedIndex ++ ){
                        $("div.itemRow[name=" + evalTemplateUtils.closedGroup.get[closedIndex] + "]").find("a.blockExpandText").click();
                    }

                    return false;
                }
            }
        });
        evalTemplateUtils.debug.groupEnd();
        evalTemplateUtils.debug.timeEnd("submit");
        return true;
    },
    
    //fillActionResponse function called by $.facebox.showResponse
    _fillActionResponse = function (entityCat,_id, templateOwner){
      evalTemplateUtils.debug.group("starting fillActionResponse", this);
        var id = _id;
  	if (entityCat == "eval-item") {
        var par = $.facebox.settings.elementToUpdate;
		id = $(par).children(".itemLine2").children("[name=rowItemId]").html();
	}
	evalTemplateUtils.debug.info($.facebox.settings.elementToUpdate);
	var entityURL = "/direct/" + entityCat + "/" + id + ".xml";
		$.ajax({
		url: entityURL,
		cache: false,
		dataType: "xml",
	    success: function(xml){
            evalTemplateUtils.debug.info("Got cat %s and data %o", entityCat, xml);
			if (entityCat == "eval-item") {
				$(entityCat, xml).each(function(){
					if (par) {
						$(par).children(".itemLine2").children(".itemText").html($("itemText", this).text());
						$(par).highlight(250, "#eeeeee");
                        evalTemplateUtils.debug.info("%o ids not null ", par);
					}
				});
			}
			if (entityCat == "eval-template") {
				$(entityCat, xml).each(function(){
					if ($('form').length > 0) {
						var newRow = $('form tr:eq(1)').clone();
						newRow.find('td:eq(0) span:eq(0)').html($("title", this).text());
						var q = 'td:eq(0) span:eq(1) a[@href$='+newRow.attr("rowId")+']';
						alert(newRow.find(q).attr('href').substring(newRow.find(q).attr('href').lastIndexOf('/') + 1));
						newRow.find(q).attr('href',newRow.find(q).attr('href').replace(newRow.attr("rowId"),id));
						newRow.attr("rowId", id);
						newRow.find('td:eq(1)').html(templateOwner);
						var tempateDate = $("lastModified", this).text();
						newRow.find('td:eq(2)').html(tempateDate.getFullYear);
						newRow.find('td:eq(0) span:eq(0)').html($("title", this).text());

						newRow.prependTo($('form tbody'));
						}
				});
			}
			}
	     });
      evalTemplateUtils.debug.groupEnd();
  },

    _initAjaxSetUp = function(){
        $(document).ajaxError(function(event, XMLHttpRequest, ajaxOptions){
                 evalTemplateUtils.debug.error("Ajax request failed: %o", XMLHttpRequest);
                 alert( evalTemplateUtils.messageLocator('GeneralAjaxChannelError', ajaxOptions.url));
            }
        );
    };

    //Public data
    return {
        ajaxSetUp: _initAjaxSetUp,
        //Specific methods
        postFCKform: function(form, textarea, target, btn) {
            _postFCKform(form, textarea, target, btn);
        },
        fillActionResponse: function(entityCat,id, templateOwner){
             _fillActionResponse(entityCat,id, templateOwner);
        },
        item: {
            /** Save the order of template items or grouped items. Can be used to save order for anything in Evals
             * @param ebAction :the custom EB action to call. Is anyone of the {@link evalTemplateUtils.pages.eb_*}
             * @param params :An object of key-value parameter pairs
             * @param fnAfter :function to excecute on ajax success
             * @param fnBefore :function to run before post
             */
            saveOrder: function(ebAction, params, fnBefore, fnAfter){
                if ( typeof params !== undefined && typeof ebAction !== undefined ){
                    evalTemplateUtils.debug.info("Parameters use to set new block items order: %o", params);
                    $.ajax({
                        url: ebAction,
                        data: $.param(params),
                        type: evalTemplateData.constants.rest_Post,
                        beforeSend: function(){
                            if ( typeof fnBefore === "function"){
                                fnBefore();
                            }
                        },
                        success: function(data){
                            if ( typeof fnAfter === "function"){
                                fnAfter(data);
                            }
                        }
                    });
                }
            }
        },
        constants: {
            //REST Constants
            rest_Post : "POST",
            rest_Get : "GET",
            rest_Delete : "DELETE"
        },
        showRSFMessage: function(DOMresponse){
            //Possible RSF message tags
            var message, messageObject, isError = false, isInfo = false,
                messagesKeys = {
                    e: "rsf-messages::error-messages::",
                    c: "rsf-messages::confirm-messages::",  //Not utilising the confirm message yet.
                    i: "rsf-messages::info-messages::"
                };
            //Check if RSF DOM objects exist in response
            if (typeof DOMresponse == "object"){
                messageObject = DOMresponse.find("div[id=" + messagesKeys.e + "]").get(0);
                if(typeof messageObject == "undefined"){
                    messageObject = DOMresponse.find("div[id=" + messagesKeys.i + "]").get(0);
                    if(typeof messageObject == "undefined"){
                        messageObject = DOMresponse.find("div[id=" + messagesKeys.c + "]").get(0);
                    }else{
                        isInfo = true;
        }
                }else{
                    isError = true;
                }

               if(typeof messageObject != "undefined" || $(messageObject).length > 0){
                    message = $(messageObject).text();
                    if(isError){
                        //Error occurred
                        alert(message);
                        return true;
                    }else if(isInfo){
                        //User info. occurred
                        //alert(message);   //TODO: Uncomment this to enable alert on every successfull save.
                    }
                }
            }
            return false;
        }
    };
})($);
/**
 * @author lovemorenalube

	// Fix for FCKeditor value to be submitted with the form. Added by Lovemore Nalube, lovemore.nalube@uct.ac.za
function txtAreaFck(){
	this.UpdateEditorFormValue = function(){
		for ( i = 0; i < parent.frames.length; i++ )
			if ( parent.frames[i].FCK )
				parent.frames[i].FCK.UpdateLinkedField();
	}
	//alert(FCKeditorAPI.GetInstance('item-text:1:input').GetHTML());
}
 */
	var groupableItems;
	$(document).bind('activateControls.templates', function(){
		$(document).unbind('activateControls.templates');
		$('a[rel*=templateRemove]').itemRemove({
					ref:	'eval-template',
					id:		"$(this).parent().parent().attr('rowId')",
					row: 	"tr[rowId='+opts.id+']"
				});
		$('a[rel=facebox]').facebox();
	});	
	
	$(document).bind('activateControls.templateItems', function(e, opt){
		$('a[rel=remove]').itemRemove({
			ref:	'eval-templateitem',
			id:		'$(this).attr("templateitemid")'
		});
		$('a[rel=childRemove]').itemRemove({
			ref:	'eval-templateitem',
			id:		'$(this).attr("templateitemid")',
			itemType: 'blockChild'
		});
		$('a[rel=unblock]').itemRemove({
			ref:	'eval-templateitem',
			id:		'$(this).attr("templateitemid")',
			itemType: 'block'
		});
		$('a[rel=facebox]').facebox();
		$('a[rel=faceboxGrid]').facebox();
		$('a.addItem[rel=faceboxAddGroupItems]').click(function(){
			var that = $(this);
            var numGroupableItems = 0;
            var myType = $(this).parents('.itemRow').find('.itemCheckbox > input').attr('id');
			if(groupableItems.length > 0){
				for(i in groupableItems){
						var type = groupableItems[i].type;
						if(
							(myType.substring(myType.indexOf('-') + 1, myType.lastIndexOf('-')))
								==
							 (type.substring(type.indexOf('-') + 1, type.lastIndexOf('-')))

							){
                             numGroupableItems++;
                        } }

            }
            if(numGroupableItems == 0){
                $(document).trigger('list.warning', [that, '', 'block', 'There are no items you can add to this group.']);
            }   else{

			$.facebox('<div id="addGroupItemDiv"></div>');
            if($('#facebox .titleHeader').length > 0)
                  $('#facebox .titleHeader').remove();
			$('<h2 style="font-weight: bold;" class="titleHeader">Select existing items to add to a group</h2>').insertBefore('#facebox .close');
			$('<div class="itemAction"> <a id="addGroupItemSelectAll" title="Select all" href="#">Select all</a> | <a id="addGroupItemDeselectAll" title="Deselect all" href="#">Deselect all</a></div>').insertBefore('#addGroupItemDiv');
			$('\
			<div class="act">\
				<input type="button" id="addGroupItemSave" class="active submit" accesskey="s" value="Add" disabled="disabled"/>\
				<input type="button" onclick="$(document).trigger(\'close.facebox\')" accesskey="x" value="Cancel"/> \
			</div>\
			').insertAfter('#addGroupItemDiv');
			//bind new control actions
			$('#addGroupItemSelectAll').bind('click',function(){
				$('#addGroupItemDiv input[type=checkbox]').each(function(){
					$(this).attr('checked','checked');
				});
                $('#addGroupItemSave').removeAttr('disabled');
				return false;
			});
			$('#addGroupItemDeselectAll').bind('click',function(){
				$('#addGroupItemDiv input[type=checkbox]').each(function(){
					$(this).removeAttr('checked');
				});
                $('#addGroupItemSave').attr('disabled', 'disabled');
				return false;
			});
			$('#addGroupItemSave').bind('click', function(){
				var img = '<img src="' + $.facebox.settings.loadingImage + '"/>';
				$(this).attr('disabled', 'disabled');
				$(this).next('input').attr('disabled', 'disabled');
				$(img).insertAfter($(this).next('input'));
                var selectedItems = new Array();
				$('#addGroupItemDiv > div').each(function(){
					if($(this).find('input[type=checkbox]').attr('checked') == true){
						$(this).find('.itemRight').show();
						$(this).appendTo(that.parent());
                        selectedItems.push($(this).attr('oldRowId'));
					}
				});
                for(i in groupableItems){
                    for(l in selectedItems){
                        if(groupableItems[i].rowId == selectedItems[l]){
                            $('div[id='+groupableItems[i].rowId+']').remove();
                        }
                    }
                }
                $(document).trigger('block.triggerChildrenSort', [that]);
                $(document).trigger('list.triggerSort');
                $(document).trigger('activateControls.templateItems');
                $.facebox('\
                                                  <div><img src="' + $.facebox.settings.loadingImage + '"/>  Saving... \
			                </div> \
			                <div class="footer"> \
			                   Please do not close this window.  \
			                </div> ');
                $(document).trigger('block.saveReorder', [that]);
				//$(document).trigger('close.facebox');
				
			});
			var clone = $(this).parent().find('.itemRowBlock').eq(0).clone();
			//edit extra controls
			clone.find('.itemRight').hide();
			clone.find('.itemLabel').html('<input name="addGroupItemCheckbox" type="checkbox" title="Mark item to use in a group" value="true"/>');

			if(groupableItems.length > 0){
				for(i in groupableItems){
						var type = groupableItems[i].type;
						if(
							(myType.substring(myType.indexOf('-') + 1, myType.lastIndexOf('-'))) 
								==
							 (type.substring(type.indexOf('-') + 1, type.lastIndexOf('-')))
							
							){
						var itemId = groupableItems[i].itemId;
						var otp = groupableItems[i].otp;
						var text = groupableItems[i].text;
						var rowId = groupableItems[i].rowId;
						var shadow = $(clone).clone();
                        shadow.css('cursor','auto');
                        shadow.attr('oldRowId', rowId);
						shadow.find('span').eq(1).html(text);
			            shadow.find('input').eq(0).val(itemId);
			            shadow.find('a[templateitemid]').attr('templateitemid', itemId);
			            shadow.find('a[otp]').attr('otp', otp);
						$('#addGroupItemDiv').append(shadow);
						
					}
				}
                $('input[name=addGroupItemCheckbox]').bind('click', function(){
                    var c =0;
                    $('input[name=addGroupItemCheckbox]').each(function(){
                       if($(this).attr('checked') == true){
                           c++;
                       }
                    }) ;
                    if(c > 0 ){
                       $('#addGroupItemSave').removeAttr('disabled');
                    }else{
                        $('#addGroupItemSave').attr('disabled', 'disabled');
                    }
                });
			}
            }
			return false;
		});

		var saveButton = document.getElementById("saveReorderButton");
		if (saveButton) {
			saveButton.onclick = function(){
				disableOrderButtons();
				buildSortableIds();
				$(saveButton.form).ajaxSubmit();
				return false;
			};
		}
		
		$('a[rel=childEdit]').click(function(){
			var url = 'modify_item?templateItemId='+$(this).parent().parent().find('input[name*=hidden-item-id]').val()+'&templateId='+$('input[name*=templateId]').val()+'&itemClassification=Block';
			$.facebox({ ajax: url });
			$.facebox.settings.objToUpdate = "block";
			return false;
		});
		//$('a[rel=childEdit]').blockChildControls();
		//$('a[rel=childRemove]').bind('click', function(){
			//$.facebox('A confirmation message will appear and if true, appropriate DIV removed from the template and DOM.');
			
		//	return false;
		//});	
		$('.blockExpandText').toggle(

			function(){
                if($(this).parent().parent().parent().find('.itemLine3').is(':hidden')){
                    $(this).click();
                    return false;
                }
				text = '[Show child items]';
               $(this).parent().parent().parent().find('.itemLine3').slideToggle();
				$(this).text(text);
                frameGrow();
				return false;
			},
            function(){
				text = '[Hide child items]';
                $(this).parent().parent().parent().find('.itemLine3').slideToggle();
				$(this).text(text);
				frameGrow();
				return false;
			}
		);	
		$('.more').bind('click', function(){
            if($(this).parent().parent().find('.itemText').eq(1).find('.blockExpandText').length == 0){
                $(this).parent().parent().find('.itemText').eq(1).find('.blockExpandText').remove();
                $(this).parent().find('.blockExpandText').clone(true).insertAfter($(this).parent().parent().find('.itemText').eq(1).find('.less'));
            }
			$(this).parent().toggle();
			$(this).parent().parent().find('.itemText').eq(1).toggle();
			frameGrow();
            //if($(this).parent().parent().parent().find('.itemLine3').is(':hidden'))
             //   $(this).parent().find('.blockExpandText').text('[Show child items]');
			return false;
		});	
		$('.less').bind('click', function(){
			$(this).parent().toggle();
			$(this).parent().parent().find('.itemText').eq(0).toggle();
            //if($(this).parent().parent().parent().find('.itemLine3').is(':hidden'))
            //    $(this).parent().find('.blockExpandText').text('[Show child items]');
			return false;	
		});
		
		refreshSort();	
		if(opt && opt != 'skipSelection'){
		//initialise the reorder dropdowns
		var l = $("#itemList > div.itemRow").get()
		EvalSystem.decorateReorderSelects("", l.length);
        }
		$(document).trigger('list.populateGroupableItems');
	});

(function($){
$.fn.blockChildControls = function() {
 	var opts = $.extend({});
	// iterate and bind each matched element
	return this.each(function() {
	  $(this).click(function(){
	  	blockChildControls($(this));
		return false;
	  });
	});
  }; 
  function blockChildControls(that){
  	//var controls = '<span class="childControls"> [ <a href="#"> Edit</a> | <a href="#"> Delete</a> ]</span>';
	//that.append(controls);
 	that.bind('click', function(){
	//	that.find('.childControls').hide();	
		that.parent().parent().find('span').eq(1).click();
		return false;
	}); 
	//that.find('.childControls').find('a').eq(1).bind('click', function(){
	//	confirm('Are u sure, really sure??');
	//	return false;
	//}); 
	}
 
})(jQuery);


function submitForm(form, textarea, target, btn){
	var rowId = $(jQuery.facebox.settings.objToUpdate).attr("id");
	var thisRow = $(document).find("[id=" + rowId + "]");	
	var img = '<img src="/library/image/sakai/spinner.gif"/>';
	var templateItemId = $(form).find('input[@name*=templateItemId]').attr('value');
	var entityUrl = '/direct/eval-templateitem/' + templateItemId + '.xml';
	//console.log('templateItemId' + templateItemId);
	var d = $(form).formToArray();
	var fckVal = null;
	try{
		if (FCKeditorAPI) {
			fckVal =  FCKeditorAPI.GetInstance(textarea).GetHTML(); //Actual editor textarea value
		}
	}
	catch(e){
			alert('Check if you have imported FCKeditor.js \n Error: FCKeditorAPI not found. ');
			//console.log(e.description);
	}
			
	//iterate through returned formToArray elements and replace input value with editor value
	for (var i = 0; i < d.length; i++) {
		if ($(d[i]).attr('name') == textarea) {
			$(d[i]).attr('value',fckVal);
		}
	}
	//console.info(textarea);
	//console.info(fckVal);
	$.ajax({
		type: 'POST',
		url: target,
		data: d,
		dataType: "html",
		beforeSend: function(){	
		btn.parent().parent().find('input').each(function(){
			$(this).attr('disabled', 'disabled');
		});
		
		
		FCKeditorAPI.GetInstance(textarea).disabled;
		
		
		btn.parent().append(img);
		//$(thisRow).html('<div class="loading">Refreshing...<img src="/library/image/sakai/spinner.gif"/></div>');
		//$("#facebox .results").html('<div class="loading">Saving...<img src="/library/image/sakai/spinner.gif"/></div>');
		//console.log(target + "   -   "+ d);
		//return false;
		},
		error: function (XMLHttpRequest, textStatus, errorThrown) {
		  return false;
		},
		success: function(d){
			$(document).trigger('close.facebox');
			if(form == '#blockForm' || form == '#item-form'){
				$('#itemList').html($(d).find('#itemList').html());
				$(document).trigger('activateControls.templateItems');
				return false;
			}
				$.ajax({
					url: entityUrl,
					dataType: 'xml',
					cache: false,
					beforeSend: function(){
						$(jQuery.facebox.settings.objToUpdate).find('.itemText span').html(img);
					},
					success: function(msg){
						var realText = $(msg).find('itemText').text();
						var shortText = null;						
						var that = $(jQuery.facebox.settings.objToUpdate).find('.itemText').eq(0);
						
						if(realText.length > 150){
							that.realText = '<h4 class="itemText"><span>' +realText+'</span><a class="less" href="#">less<\/a></h4>';
											$('body').append('<div id="shortText" style="display:none;">'+ truncateTextDo(realText) +'</div>');
											$('body').append('<div id="realText" style="display:none;">'+ realText +'</div>');
											that.html('<span>'+$('#shortText').text()+'</span><a class="more" href="#">...more</a>');
											that.parent().find('.itemText').eq(1).remove();
											try {
												that.hide();
												$('<h4 class="itemText"><span>' +$('#realText').text()+'</span><a class="less" href="#">less<\/a></h4>').insertAfter(that);
											}catch(e){
												//console.log(e.description);
											}
								$('.more').bind('click', function(){
									$(this).parent().toggle();
									$(this).parent().parent().find('.itemText').eq(1).toggle();
									frameGrow();
									return false;
								});	
								$('.less').bind('click', function(){
									$(this).parent().toggle();
									$(this).parent().parent().find('.itemText').eq(0).toggle();
									return false;	
								});
							//console.log(shortText);
							
							
						}else{
							that.find('span').html(realText);
						}
						
					}
				});
			
		}
	});
	
	
}

function truncateTextDo(string){
	trunc = string.substring(0, 150);
	trunc = trunc.replace(/\w+$/, '');	
	return trunc;
}
		
function truncateText(){
	$('.itemText').each(function(){
		that = $(this);
		that.realText = '<h4 class="itemText"><span>' +that.text()+'</span><a class="less" href="#">less<\/a></h4>';
		if(that.realText.length > 150){
			that.html('<span>'+truncateTextDo(that.text())+'</span><a class="more" href="#">...more</a>');
			$(that.realText).insertAfter(that);
			that.parent().find('.itemText').eq(1).toggle();
			}
		else
			that.html("<span>"+that.text()+"</span>");
	});
}
$('.itemBlockRow').each(function(){
			block = $(this);
			block.expandText = ' [<a class="blockExpandText" href="#"> Show child items </a>]';
			block.html(block.html() + block.expandText);
});
	

	function frameGrow(){
		try {
			var frame = parent.document.getElementById(window.name);
			$(frame).height(parent.document.body.scrollHeight + 120);
		}catch(e){}
	
	}

var options_choose_existing_items_html_search = {
				beforeSend: function(){
				$(".form_submit").attr("disabled","disabled");
					if(!/\S/.test($(".form_search_box").val())){
						$(".form_search_box").focus();
						$(".form_submit").removeAttr("disabled");
						return false;
					} 
				},
				success: function(data){
					$.facebox(data);
					$("a[rel*=facebox]").facebox();
				}
			};
	
	
/* Script adapted from: www.jtricks.com
 * Version: 20070301
 * Latest version:
 * www.jtricks.com/javascript/window/box_alone.html
 */
// Moves the box object to be directly beneath an object.
(function($){
 //
  // plugin definition
  //
  var that;
  $.fn.itemRemove = function(options) {
  	var defaults = {
		ref:	'', //the RESTful unique identifier for this component
		id:		'' //the id passed
	};
	
	// iterate and bind each matched element
	return this.each(function() {
	  $(this).click(function(){
	  	var opts = $.extend(defaults, options);
		opts.id = eval(opts.id);
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
    var cleft = -100;

    if((options.itemType == "blockChild") && ($.browser.msie)){
	    cleft = -250;
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
    if(options.itemType != null){
    if(options.itemType == "blockChild")
         $('a[templateItemId='+options.id+']').parents('.itemRowBlock').eq(0).css('background','#fff') ;
     else
         $('a[templateItemId='+options.id+']').parents('.itemRow').eq(0).css('background','#fff') ;
    }
    var href = an.href;
    var width = 120;
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
	$(boxdiv).hide();
    document.body.appendChild(boxdiv);

    var offset = 0;    

  /* var content = '\
	<table width="100%" border="0" cellspacing="0" cellpadding="0">\
  	<tr>\
    <td class="header" colspan="2"><h1 class="">Are you sure?</h1>\
  	</tr>\
  	<tr><td align="center"><a href="#" id="removeConfirmYes">YES</a></td><td align="center">\
	<a accesskey="x" href="#cancel" class="closeImage">No</a>\
	</td>\
	</tr>\
	</table>\
	';*/
	
 var content = '\
<h4 style="font-weight: bold;" class="">Are you sure?</h4>\
<div class="" style="float: right;">\
					<input type="button" value="Yes" accesskey="s" class="removeItemConfirmYes"/>\
					<input type="button" value="Cancel" accesskey="x" class="closeImage"/>\
	</div>\
	';
	
    //$(boxdiv).html(content);
    if(options.itemType == "blockChild")
     $('a[templateItemId='+options.id+']').parents('.itemRowBlock').eq(0).css('background','#ffc') ;
    else
     $('a[templateItemId='+options.id+']').parents('.itemRow').eq(0).css('background','#ffc') ;
     $(boxdiv).html(content);
//    $.facebox(content);
//    $.facebox.settings.overlay = false;
//    $('#facebox .header').eq(0).remove();
//    $('#facebox table').attr('width', 250);
//    $('#facebox .body').css('width',250);

	
	$('.closeImage').click(function()
        {show_hide_box(an, options);});
	$('.removeItemConfirmYes').click(function(){
		if(options.itemType == "blockChild"){
			if($('#closeItemOperationsEnabled').length > 0){
                $('#closeItemOperationsEnabled').parent().remove();
             }
        if($('a[templateItemId='+options.id+']').parents('.itemTableBlock').find('div.itemRowBlock').get().length <= 2){
            var error = '<div class="itemOperationsEnabled">\
            <img src="/library/image/sakai/cancelled.gif"/>\
            <span class="instruction">Sorry, groups have to have at least TWO items in them.</span> <a href="#" id="closeItemOperationsEnabled">close</a></div>\
            ';
            $(that).parents('.itemLine3').prepend(error).effect('highlight', 1500);
            $('#closeItemOperationsEnabled').click(function(){$(this).parent().slideUp('normal', function(){$(this).remove()});return false});
            return false;
        }
            $.ajax({
				url: "/direct/" + options.ref + "/" + options.id + "/delete",
				type: "DELETE",
				success: function(data){
					//alert(data);
					finish(options, data);
				}
			});
		}
		else if (options.ref == 'eval-templateitem') {
			var s = 'a[templateitemid='+options.id+']';
			var t = $(s);
			$.ajax({
				url: "remove_item",
				data: 'templateItemId='+options.id+'&templateId='+t.attr('templateid')+'&command+link+parameters%26deletion-binding%3Dl%2523%257B'+t.attr('otp')+'%257D%26Submitting%2520control%3Dremove-item-command-link=Remove+Item',
				type: "POST",
				beforeSend: function(){
					//t.parent().parent().parent().parent().css('background', '#eee');
					t.hide();
					t.parent().find('a').slice(0,2).hide();
					t.parent().append('<img src="/library/image/sakai/spinner.gif"/>');
					t.parent().parent().parent().find('.selectReorder').attr('disabled','disabled');
				},
				success: function(data){
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
				success: function(data){
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

function finish(options, d){
	if(options.itemType == 'block'){
		$('#itemList').html($(d).find('#itemList').html());
		$(document).trigger('activateControls.templateItems');
		return false;
	}
	if(options.itemType == 'blockChild'){
		$(that).parent().parent().effect('highlight', {}, 1000).fadeOut(500, function(){
			$(document).trigger('block.triggerChildrenSort', [$(this)]);
            $(this).remove();
		});
		return false;
		//do more stuff here
	}	
	if(options.ref == 'eval-templateitem'){
		$(that).parent().parent().parent().parent().effect('highlight', {}, 1000).fadeOut(500, function(){
			$(this).remove();
			var list = $("#itemList > div").get();
			for (var i = 0; i < list.length; i++) {
				if (list[i].id) {
					setIndex(list[i].id, i);
				}
			}
		});
		
		//do more stuff here
	}
	
	if(options.ref == 'eval-template'){
		$('tr[rowId='+options.id+']').effect('highlight', {}, 1000).fadeOut('normal');
	}
    return false;
}

$(document).bind('list.populateGroupableItems', function(){
		groupableItems = new Array();
		groupableItems.length = 0;
		// populate or re-populate groupable item array
		$('div.itemList > div:visible').each(function(){
			if ($(this).children('.itemLine3').length == 0) {
                var t = "";
                if($(this).find('.itemText > span').eq(1).text() == "")
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
});
$(document).bind('list.busy', function(e, val){
		if (val == true) {
	var message = 'We are busy saving your work, please wait!';
if (navigator.userAgent.toLowerCase().indexOf("safari") != -1 && window != top) {
 top.pageOrderExitMessage = message;
 top.onbeforeunload = function() { return top.pageOrderExitMessage };
	}
else {
window.onbeforeunload = function() {
	 return message; 
};
}		
		}else{
			 if (navigator.userAgent.toLowerCase().indexOf("safari") != -1 && window != top) {
 top.onbeforeunload = function() { };
 }
 else {
 window.onbeforeunload = function() { };
  }
		}
	
});

})(jQuery);	

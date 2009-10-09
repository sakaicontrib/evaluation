// Eval Template Modify JS for onload functions
// @author lovemore.nalube@uct.ac.za

var evalTemplateLoaderEvents = (function($) {
    var _initDom = function(){
        startSort();
        $(document).trigger('activateControls.templateItems');
        buildSortableIds();
    },

    _modify_block = function(){
                var siteId = $('#site-id').text();
        SakaiProject.fckeditor.initializeEditor("item-text", siteId);
        $.facebox.setHeader($(".titleHeader"));

		var baseId = ""; // "blockPage::";

		$('#saveBlockAction').click(function() {
			//var list = $("#blockForm > div.itemRow").get();
			var childIdList = [];
			$('#blockForm input[name*=hidden-item-id]').each(function(){
				childIdList.push($(this).val());
			});
			$("#ordered-child-ids").val(childIdList.toString());
			evalTemplateData.postFCKform('#blockForm', 'item-text', 'modify_template', $('#saveBlockAction'));
		});

		function $it(id) {
            return document.getElementById(id);
        }

		function enableOrderButtons() {
			$it(baseId + "revertOrderButton").disabled = false;
			$it(baseId + "saveReorderButton").disabled = false;
		}

		function disableOrderButtons() {
			$it(baseId + "revertOrderButton").disabled = true;
			$it(baseId + "saveReorderButton").disabled = true;
		}

        //var sortableIds;
        function buildSortableIds() {
            var sortableIds = [];
            var domList = $("div.itemList > div").get();
			for (var i = 0; i < domList.length; i++) {
				sortableIds.push(domList[i].id);
			}
		}

		var reorderButtonsExist = false;
		var saveReorderButton = $it(baseId + "saveReorderButton");

		if (saveReorderButton !== null) {
			reorderButtonsExist = true;
			saveReorderButton.onclick = function() {
				disableOrderButtons();
				buildSortableIds();
				$(saveReorderButton.form).ajaxSubmit();
				return false;
			};
		}

		function setIndex(itemId, newindex) {
			$it(itemId + "item-num").innerHTML = (parseInt(newindex, 10) + 1);
		}

		function setRadioActions() {
			var radioList = $("div.blockText > input").get();
			var textAreaDiv = $it("item-text-div");
			if (radioList.length !== 0) {
				for (var i in radioList) {
					if (radioList[i].id) {
						radioList[i].onclick = function() {
							textAreaDiv.style.display = "none";
						};
					}
				}
				radioList[radioList.length - 1].onclick = function() {
					textAreaDiv.style.display = "block";
				};
				radioList[radioList.length - 1].checked = true;
			}
		}
		// put original order into the revertOrder trigger
		var revertOrderButton = $it(baseId + "revertOrderButton");
		if (revertOrderButton !== null) {
			revertOrderButton.onclick = function() {
				disableOrderButtons();
				for(var i = sortableIds.length - 1; i > 0; --i) {
					var thisitem = $it(sortableIds[i]);
					var previtem = $it(sortableIds[i - 1]);
					previtem.parentNode.removeChild(previtem);
					thisitem.parentNode.insertBefore(previtem, thisitem);
					setIndex(thisitem.id, i);
				}
				setIndex(sortableIds[0], 0);
			};
		}
		var saveBlockButton = $it(baseId + "saveBlockAction");
    },

    bindDeleteIcons = function(){
        $('a[rel=remove]').itemRemove({
            ref:    'eval-templateitem',
            id:     '$(this).attr("templateitemid")',
            text:   '$(this).parents("div.itemLine2").find("h4.itemText:visible").text()'
        });
        $('a[rel=childRemove]').itemRemove({
                    ref:    'eval-templateitem',
                    id:        '$(this).attr("templateitemid")',
                    itemType: 'blockChild',
                    text:   '$(this).parents("div.itemRowBlock").find("span.text:visible").text()'
                });
        $('a[rel=childUngroup]').itemRemove({
                    ref:    'eval-templateitem/unblock',
                    id:    '$(this).parents("div.itemRowBlock").find("input[name=hidden-item-id]").val()',
                    itemType: 'blockChild',
                    text:   '$(this).parents("div.itemRowBlock").find("span.text:visible").text()'
                });
        $('a[rel=unblock]').itemRemove({
            ref:    'eval-templateitem',
            id:        '$(this).attr("templateitemid")',
            itemType: 'block',
            text:   '$(this).parents("div.itemLine2").find("h4.itemText:visible").text()'
        });
    },

    unBindDeleteIcons = function(){
        $('a[rel=remove]').unbind("click");
        $('a[rel=childRemove]').unbind("click");
        $('a[rel=unblock]').unbind("click");
    },

    bindRowEditPreviewIcons = function(changedRow){
        if ( typeof changedRow === "undefined" || changedRow === null){
            $('a[rel=facebox]').facebox();
            $('a[rel=faceboxGrid]').faceboxGrid();
        }else{
            changedRow.find('a[rel=facebox]').facebox();
            changedRow.find('a[rel=faceboxGrid]').faceboxGrid();
        }
    },

    unBindRowEditPreviewIcons = function(changedRow){
        if ( typeof changedRow === "undefined" || changedRow === null){
            $('a[rel=facebox]').unbind("facebox");
            $('a[rel=faceboxGrid]').unbind("faceboxGrid");
        }else{
            changedRow.find('a[rel=facebox]').unbind("facebox");
            changedRow.find('a[rel=faceboxGrid]').unbind("faceboxGrid");
        }
    };

    return {
        onDOMReady: _initDom,
        //modify_item.html view
        modify_item: function() {
            var siteId = $('#site-id').text();
            SakaiProject.fckeditor.initializeEditor("item-text", siteId);
            if ($.facebox.settings.elementToUpdate == "block") {
                //$('#facebox .content .navPanel').hide();
                //$('#nonBlockSettings').hide();
                //$('#nonBlockSettings2').hide();
               // $('div[@id=show-item-scale::]').hide();
            }
            $.facebox.setHeader($(".portletBody .titleHeader"));
            if ($('.act .submit').attr('name').search(/templateBBean.saveItemAction/) == -1) {
                $('.act .submit').bind('click', function() {
                    evalTemplateData.postFCKform('#item-form', 'item-text', 'modify_item', $(this));
                });
            } else {
                var button = $('<input type="submit" class="active" accesskey="s"/>');
                button.attr('name', $('.act .submit').attr('name'))
                        .val($('.act .submit').val());
                $('.act .submit').replaceWith(button);
            }
        },
        //modify_block.html view
        modify_block: function(){
            _modify_block();
        },
        //modify_template.html view
        modify_template: function(){
            var title = "",
                description = "",
                 o = {
                    beforeSubmit: function() {
                        title = $('#basic-form input[@name=title]').val();
                        description = $('#basic-form textarea[@name=description]').val();
                        if (!title) {
                            alert( evalTemplateUtils.messageLocator("general.blank.required.field.user.message",
                                    evalTemplateUtils.messageLocator('modifytemplatetitledesc.title.header').toLowerCase())); //TODO: Make unobtrusive
                            return false;
                        }
                        $("#facebox .content").html('<img src="/library/image/sakai/spinner.gif"/>');
                    },
                    success: function() {
                        $('h3.viewNav > span#title').text(title);
                        $('span[id=description-switch::description]').text(description);
                        $(document).trigger('close.facebox');
                    }
                };
                $.facebox.setHeader($(".portletBody .titleHeader"));
                evalTemplateUtils.frameGrow(0);
                $('#basic-form').ajaxForm(o);
                $('input.cancelButtonLightbox').bind("click", function() {
                    $(document).trigger('close.facebox');
                });
        },
        bindDeleteIcons : bindDeleteIcons,
        unBindDeleteIcons : unBindDeleteIcons,
        bindRowEditPreviewIcons : bindRowEditPreviewIcons,
        unBindRowEditPreviewIcons : unBindRowEditPreviewIcons
    };
})($);
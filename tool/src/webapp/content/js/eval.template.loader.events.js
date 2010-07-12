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
        $.facebox.setHeader(evalTemplateUtils.pages.modify_block_page);

		var baseId = ""; // "blockPage::";

		$('#saveBlockAction').click(function() {
			var childIdList = [],
                reuseBlockText = $('.blockText input[type=radio][checked][value!=new1]').length > 0;
			$('#blockForm input[name*=hidden-item-id]').each(function(){
				childIdList.push($(this).val());
			});
			$("#ordered-child-ids").val(childIdList.toString());
            if (reuseBlockText){
			    evalTemplateData.postFCKform('#blockForm', false, 'modify_block', $('#saveBlockAction'));
                return false;
            }else{
                evalTemplateData.postFCKform('#blockForm', 'item-text', 'modify_block', $('#saveBlockAction'));
                return false;
            }
		});

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
    },

    bindDeleteIcons = function(row){
        var scope = (typeof row === 'undefined') ? document : row.get();
        $(scope).find('a[rel=remove]').itemRemove({
            ref:    'eval-templateitem',
            id:     'evalTemplateUtils.getTemplateItemId($(this))',
            text:   '$(this).parents("div.itemLine2").find("h4.itemText:visible").text()'
        });
        $(scope).find('a[rel=childRemove]').itemRemove({
                    ref:    'eval-templateitem',
                    id:        '$(this).parents("div.itemRowBlock").find("input[name=hidden-item-id]").val()',
                    itemType: 'blockChild',
                    text:   '$(this).parents("div.itemRowBlock").find("span.text").text()'
                });
        $(scope).find('a[rel=childUngroup]').itemRemove({
                    ref:    'eval-templateitem/unblock',
                    id:    '$(this).parents("div.itemRowBlock").find("input[name=hidden-item-id]").val()',
                    itemType: 'blockChild',
                    text:   '$(this).parents("div.itemRowBlock").find("span.text:visible").text()'
                });
        $(scope).find('a[rel=unblock]').itemRemove({
            ref:    'eval-templateitem',
            id:        'evalTemplateUtils.getTemplateItemId($(this))',
            itemType: 'block',
            text:   '$(this).parents("div.itemLine2").find("h4.itemText:visible").text()'
        });
    },

    unBindDeleteIcons = function(){
        $('a[rel=remove]').unbind("click");
        $('a[rel=childRemove]').unbind("click");
        $('a[rel=childUngroup]').unbind("click");
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
    },
        /**
         * this will bind these controls in the main item of a group: hide/show item && more/less item
          */
    bindGroupParentTextControls = function(row){
        var scope = (typeof row === 'undefined') ? document : row.get();
        $(scope).find('.blockExpandText').toggle(
            function() {
                if ($(this).parents('.itemRow').find('.itemLine3').is(':hidden')) {
                    $(this).click();
                    return false;
                }
                var text = evalTemplateUtils.messageLocator('modifytemplate.group.show');
                $(this).parents('.itemRow').find('.itemLine3').slideToggle();
                $(this).text(text);
                // save closed state
                evalTemplateUtils.closedGroup.add( $(this).parents(".itemRow").attr('name') );
                return false;
            },
            function() {
                var text = evalTemplateUtils.messageLocator('modifytemplate.group.hide');
                $(this).parents('.itemRow').find('.itemLine3').slideToggle();
                $(this).text(text);
                // remove closed state
                evalTemplateUtils.closedGroup.remove( $(this).parents(".itemRow").attr('name') );
                return false;
            });
        $(scope).find('.more').bind('click', function() {
            if ($(this).parents('.itemLine2').find('.itemText').eq(1).find('.blockExpandText').length === 0) {
                $(this).parents('.itemLine2').find('.itemText').eq(1).find('.blockExpandText').remove();
                $(this).parent().find('.blockExpandText').clone(true).insertAfter($(this).parents('.itemLine2').find('.itemText').eq(1).find('.less'));
            }
            $(this).parent().toggle();
            $(this).parents('.itemLine2').find('.itemText').eq(1).toggle();
            evalTemplateUtils.frameGrow(0);
            return false;
        });
        $(scope).find('.less').bind('click', function() {
            $(this).parent().toggle();
            $(this).parents('.itemLine2').find('.itemText').eq(0).toggle();
            return false;
        });
    };

    return {
        onDOMReady: _initDom,
        //modify_item.html view
        modify_item: function() {
            var siteId = $('#site-id').text();
            SakaiProject.fckeditor.initializeEditor("item-text", siteId);
            $.facebox.setHeader(evalTemplateUtils.pages.modify_item_page);
            if ($('.act .submit').attr('name').search(/templateBBean.saveTemplateItemToGroupAction/) !== -1) {
                //proper submit  skip validation - it is not important
            } else if ($('.act .submit').attr('name').search(/templateBBean.saveItemAction/) === -1) {
                $('.act .submit').bind('click', function() {
                    evalTemplateData.postFCKform('#item-form', 'item-text', 'modify_item', $(this));
                    return false;
                });
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
                $.facebox.setHeader(evalTemplateUtils.pages.modify_template_page);
                evalTemplateUtils.frameGrow(0);
                $('#basic-form').ajaxForm(o);
                $('input.cancelButtonLightbox').bind("click", function() {
                    $(document).trigger('close.facebox');
                });
        },
        //choose expert items view
        choose_expert_category: function(){
            $.facebox.setHeader(evalTemplateUtils.pages.choose_expert_page);
            $('a[rel*=facebox]').facebox();
        },
        preview_item: function(){
            if ($('.blockItemGroup').length > 0){
                evalsys.instrumentBlockItem();
            }
            if ($('.steppedItemGroup').length > 0){
                evalsys.instrumentSteppedItem();
            }
            if ($('.mult-choice-ans').length > 0){
                evalsys.instrumentMCMAItem();
            }
            if ($('.itemListEval').length > 0) {
                evalsys.instrumentScaleItem();
            }
            if ($('.fullDisplayHorizontal').length > 0) {
                evalsys.instrumentDisplayHorizontal();
            }
            $.facebox.setHeader(evalTemplateUtils.pages.preview_item_page);
            $("div.JSevalComment").evalComment();   //Bind comment boxes toggle link action
        },
        bindDeleteIcons : bindDeleteIcons,
        unBindDeleteIcons : unBindDeleteIcons,
        bindRowEditPreviewIcons : bindRowEditPreviewIcons,
        unBindRowEditPreviewIcons : unBindRowEditPreviewIcons,
        bindGroupParentTextControls : bindGroupParentTextControls
    };
})($);
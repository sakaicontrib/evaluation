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
                evalTemplateData.postFCKform('#blockForm', 'item-text::input', 'modify_block', $('#saveBlockAction'));
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
        // rel=childUngroup is NOT bound here: it navigates straight to /unblock_item,
        // a real Spring MVC confirmation page (see UnblockItemController), instead of
        // going through the legacy small popup + EntityBroker AJAX call below.
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
        $('a[rel=unblock]').unbind("click");
    },

    bindRowEditPreviewIcons = function(changedRow){
        return;
    },

    unBindRowEditPreviewIcons = function(changedRow){
        return;
    },
        /**
         * this will bind these controls in the main item of a group: hide/show item && more/less item
          */
    bindGroupParentTextControls = function(row){
        var scope = (typeof row === 'undefined') ? document : row.get();
        // jQuery >= 1.9 dropped the click-alternating form of .toggle(fnShow, fnHide),
        // so the show/hide state is driven from the current visibility of .itemLine3 instead.
        $(scope).find('.blockExpandText').on('click', function() {
            var groupName = $(this).parents('.itemRow').attr('name');
            var itemLine3 = $(this).parents('.itemRow').find('.itemLine3');
            var wasVisible = itemLine3.is(':visible');
            itemLine3.slideToggle();
            if (wasVisible) {
                $(this).text(evalTemplateUtils.messageLocator('modifytemplate.group.show'));
                evalTemplateUtils.closedGroup.add( groupName );
            } else {
                $(this).text(evalTemplateUtils.messageLocator('modifytemplate.group.hide'));
                evalTemplateUtils.closedGroup.remove( groupName );
            }
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
            if ($('.act .submit').attr('name').search(/templateBBean.saveTemplateItemToGroupAction/) !== -1) {
                //proper submit  skip validation - it is not important
            } else if ($('.act .submit').attr('name').search(/templateBBean.saveItemAction/) === -1) {
                $('.act .submit').bind('click', function() {
                    evalTemplateData.postFCKform('#item-form', 'item-text::input', 'modify_item', $(this));
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
            return;
        },
        //choose expert items view
        choose_expert_category: function(){
            return;
        },
        //modify expert item view
        modify_expert_item: function(){
            return;
        },
        //preview expert item view
        preview_expert_item: function(){
            return;
        },        
        //remove_expert_item view
        remove_expert_item: function(){
            return;
        },
        preview_item: function(){
            evalsys.instrumentItems("#item_container");
        },
        bindDeleteIcons : bindDeleteIcons,
        unBindDeleteIcons : unBindDeleteIcons,
        bindRowEditPreviewIcons : bindRowEditPreviewIcons,
        unBindRowEditPreviewIcons : unBindRowEditPreviewIcons,
        bindGroupParentTextControls : bindGroupParentTextControls,
        //functions for manipulating the blocks eg: clicking on 'create block items'
        block : {
            countCheckBox : function(){
                    //1)if total cout of checked checkbox > 2, enable "Create Block" button
                    //2) disable all boxes which are not using the same scale as the first one that is checked
                    //3)if no check box is checked, all the check box should be enabled
                    var checkBoxes_ALL = $("label.itemCheckbox input[type=checkbox]"),
                        checkBoxes_CHECKED = $("label.itemCheckbox input[type=checkbox]:checked"),
                        checkBoxes_ALL_array = checkBoxes_ALL.get(),
                        checkBoxes_CHECKED_array = checkBoxes_CHECKED.get(),
                        submitButton = document.getElementById("createBlockBtn"),
                        submitButtonBlock = document.getElementById("blockInputs");

                    var count_CheckBoxes_CHECKED = checkBoxes_CHECKED.length; //enforce at least 2 check box are checked

                    $(".itemLine2").removeClass('itemLine2--selected-scale');
                    if (count_CheckBoxes_CHECKED > 0){
                        for (var a=0; a< checkBoxes_CHECKED_array.length;a++) {
                            var target = $(checkBoxes_CHECKED_array[a]),
                                targetId = target.attr('id');
                                target.parents(".itemLine2:eq(0)").addClass('itemLine2--selected-scale');
                            var scaleId = targetId.substring(targetId.indexOf('-')+1, targetId.lastIndexOf('-'));
                            //disable other check box which has different scale id
                             for(var b=0;b<checkBoxes_ALL_array.length;b++){
                                 var other = $(checkBoxes_ALL_array[b]),
                                    otherId = other.attr('id');
                                 if(other !== target){
                                    var otherScaleId = otherId.substring(otherId.indexOf('-')+1, otherId.lastIndexOf('-'));
                                    if(otherScaleId !== scaleId){
                                        other.attr("disabled", true);
                                        other.parents("label").addClass("notselectable");
                                    } else {
                                        other.attr("disabled", false);
                                        other.parents("label").addClass("selectable");
                                    }
                                 }
                             }
                        }
                    }

                    //alert("scaleId= "+scaleId);
                    if(count_CheckBoxes_CHECKED === 0){ //no check box is checked, enable all the checkboxes
                        checkBoxes_ALL.attr("disabled", false);
                        checkBoxes_ALL.parents("label").removeClass("selectable");
                        checkBoxes_ALL.parents("label").removeClass("notselectable");
                        submitButton.disabled = true;
                        evalsys.setItemOperationsState(submitButtonBlock, false);
                    }else if(count_CheckBoxes_CHECKED < 2){
                        //alert("select less than 2 items,disable submit button");
                        submitButton.disabled = true;
                        evalsys.setItemOperationsState(submitButtonBlock, false);
                    }else{
                        //alert("select 2 or more items,enable submit button");
                        submitButton.disabled = false;
                        evalsys.setItemOperationsState(submitButtonBlock, true);
                    }
            },
            extractSelectedItems : function(e){
                var submitButton = this,
                    checkBoxes_CHECKED = $("label.itemCheckbox input[type=checkbox]:checked"),
                    checkBoxes_CHECKED_array = checkBoxes_CHECKED.get(),
                    destinationForm = submitButton.form;

                if (checkBoxes_CHECKED.length > 0 && destinationForm) {
                    var idList = [];
                    // look for checkboxes which have IDs of the form "block-12-11" and keep track of the ones that are checked.
                    for (var a=0;a<checkBoxes_CHECKED_array.length;a++) {
                        var target = $(checkBoxes_CHECKED_array[a]),
                            targetId = target.attr('id'),
                            scaleId = targetId.substr(targetId.lastIndexOf('-')+1);
                            idList.push(scaleId);
                    }

                    destinationForm.templateItemIds.value=idList.toString();

                    if (checkBoxes_CHECKED.length < 2) {
                        alert(evalTemplateUtils.messageLocator('modifytemplate.group.warn.minimum'));
                        return false;
                    }
                    $(destinationForm).trigger('submit');
                    return true;
                }
                return false;
            }
        }
    };
})($);

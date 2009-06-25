/**
 * Reordering utilities for the items drag and drop ordering page -AZ
 * Does not put stuff into the global namespace and uses only methods that are compatible with most browsers
 */
$(document).ready(
	function () {
        var siteId = $('#site-id').text();
        SakaiProject.fckeditor.initializeEditor("item-text", siteId);		
        evalTemplateUtils.frameGrow(0); //TODO: find a suitable height
		$.facebox.setHeader($(".titleHeader"));
		
		//$('#blockForm').ajaxForm();
		
		var baseId = ""; // "blockPage::";
		
		$('#saveBlockAction').click(function() {
			var list = $("#blockForm > div.itemRow").get();
			var childIdList = new Array();
			$('#blockForm input[name*=hidden-item-id]').each(function(){
				childIdList.push($(this).val());
			});	
			$("#ordered-child-ids").val(childIdList.toString());
			evalTemplateData.postFCKform('#blockForm', 'item-text', 'modify_block', $('#saveBlockAction'));
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
        	sortableIds = new Array();
			var domList = $("div.itemList > div").get();
			for (var i = 0; i < domList.length; i++) {
				sortableIds.push(domList[i].id);
				//var itemNum = $it(domList[i].id + "item-num");
				//$(itemNum).removeClass("orderChanged");  
				//var itemSelect = $it(domList[i].id + "item-select-selection");
				//$(itemSelect).removeClass("orderChanged");
			}
		}
		//buildSortableIds();
		
		var reorderButtonsExist = false;
		var saveReorderButton = $it(baseId + "saveReorderButton");
		
		if (saveReorderButton != null) {
			reorderButtonsExist = true;
			saveReorderButton.onclick = function() {
				disableOrderButtons();
				buildSortableIds();
				$(saveReorderButton.form).ajaxSubmit();
				return false;
			};
		}
		
		function setIndex(itemId, newindex) {
			$it(itemId + "item-num").innerHTML = (parseInt(newindex) + 1);
		}
		
		function setRadioActions() {
			
			var radioList = $("div.blockText > input").get();
			var textAreaDiv = $it("item-text-div");
			
			if (radioList.length != 0) {
				
				for (i in radioList) {
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
		
		//setRadioActions();
		
		// put original order into the revertOrder trigger
		var revertOrderButton = $it(baseId + "revertOrderButton");
		
		if (revertOrderButton != null) {
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
		
		
		/*$("div.itemTable").Sortable(
			{
				accept: 		"itemRow",
				activeclass: 	"sortableactive",
				axis:			"vertically",
				hoverclass: 	"sortablehover",
				helperclass: 	"sorthelper",
				onchange:		
					function(data) {
						var list = $("div.itemTable > div").get();
						for (i in list) {
							if (list[i].id) {
								itemId = list[i].id;
								setIndex(itemId, i);
							}
						}
						if (reorderButtonsExist)
							enableOrderButtons();
					},
				opacity: 		0.5,
				tolerance:		"intersect"
			}
		);*/
	}
);

/**
 * Reordering utilities for the items drag and drop ordering page -AZ
 * Does not put stuff into the global namespace and uses only methods that are compatible with most browsers
 */
$(document).ready(
	function () {
		function $it(id) {
        	return document.getElementById(id);
        }

		function enableOrderButtons() {
			document.getElementById("revertOrderButton").disabled = false;
			document.getElementById("saveReorderButton").disabled = false;
			document.getElementById("orderInputs").className ="itemOperationsEnabled";
		}
	
		function disableOrderButtons() {
			document.getElementById("revertOrderButton").disabled = true;
			document.getElementById("saveReorderButton").disabled = true;
			document.getElementById("orderInputs").className ="itemOperations";
		}

        var sortableIds;
        function buildSortableIds() {
        	sortableIds = new Array();
			var domList = $("div.itemList > div").get();
			for (var i = 0; i < domList.length; i++) {
				sortableIds.push(domList[i].id);
				//var itemNum = $it(domList[i].id + "item-num");
				//$(itemNum).removeClass("orderChanged");  
				var itemSelect = $it(domList[i].id + "item-select-selection");
				$(itemSelect).removeClass("orderChanged");
	
			}
		}
		buildSortableIds();

		var saveButton = document.getElementById("saveReorderButton");
		saveButton.onclick = function() {
			disableOrderButtons();
			buildSortableIds();
			$(saveButton.form).ajaxSubmit();
			return false;
		};

        function setIndex(itemId, newindex) {
            var changed = sortableIds[newindex] != itemId;
            //var itemNum = $it(itemId + "item-num");
            var itemSelect = document.getElementById(itemId + "item-select-selection");
			if (changed) {
				//$(itemNum).addClass("orderChanged");
				$(itemSelect).addClass("orderChanged");
			} else {
				//$(itemNum).removeClass("orderChanged");  
				$(itemSelect).removeClass("orderChanged");  
			}
        	//itemNum.innerHTML = (parseInt(newindex) + 1);
			itemSelect.selectedIndex = newindex;
			// NOTE: might need to disable the pulldowns once the user drags
			$it(itemId + "hidden-item-num").value = newindex;
		}

		// put original order into the revertOrder trigger
		$it("revertOrderButton").onclick = function() {
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

		$("div.itemList").Sortable(
			{
				accept: 		"itemRow",
				activeclass: 	"sortableactive",
				axis:			"vertically",
				hoverclass: 	"sortablehover",
				helperclass: 	"sorthelper",
				onchange:		function(data) {
					var list = $("div.itemList > div").get();
					for (i in list) {
						if (list[i].id) {
							itemId = list[i].id;
							setIndex(itemId, i);
						}
					}
					enableOrderButtons();
								},
				opacity: 		0.5,
				tolerance:		"intersect"
			}
		);
	}
);

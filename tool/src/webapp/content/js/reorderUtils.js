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
		}
	
		function disableOrderButtons() {
			document.getElementById("revertOrderButton").disabled = true;
			document.getElementById("saveReorderButton").disabled = true;
		}

        var sortableIds;
        function buildSortableIds() {
        	sortableIds = new Array();
			var domList = $("ol.itemList > li").get();
			for (var i = 0; i < domList.length; i++) {
				sortableIds.push(domList[i].id);
				var itemNum = $it(domList[i].id + "item-num");
				$(itemNum).removeClass("orderChanged");  
			}
		}
		buildSortableIds();

		var saveButton = $it("saveReorderButton");
		saveButton.onclick = function() {
			disableOrderButtons();
			buildSortableIds();
			$(saveButton.form).ajaxSubmit();
			return false;
		};

        function setIndex(itemId, newindex) {
            var changed = sortableIds[newindex] != itemId;
            var itemNum = $it(itemId + "item-num");
			if (changed) {
				$(itemNum).addClass("orderChanged");
			} else {
				$(itemNum).removeClass("orderChanged");  
			}
        	itemNum.innerHTML = (parseInt(newindex) + 1);
			$it(itemId + "hidden-item-num").value = newindex;
		}

		// put original order into the revertOrder trigger
		$it("revertOrderButton").onclick = function() {
			for(var i = sortableIds.length - 1; i > 0; --i) {
				var thisitem = $it(sortableIds[i]);
				var previtem = $it(sortableIds[i - 1]);
				previtem.parentNode.removeChild(previtem);
				thisitem.parentNode.insertBefore(previtem, thisitem);
				setIndex(thisitem.id, i);
			}
			setIndex(sortableIds[0], 0);
		};

		$("ol.itemList").Sortable(
			{
				accept: 		"itemRow",
				activeclass: 	"sortableactive",
				axis:			"vertically",
				hoverclass: 	"sortablehover",
				helperclass: 	"sorthelper",
				onchange:		function(data) {
					var list = $("ol.itemList > li").get();
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

		savedList = new Array();

		$("ol.itemList > li").each(
			function(item) { savedList.push(this); }
		);
	}
);
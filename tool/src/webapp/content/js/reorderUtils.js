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
	}
);

var EvalSystem = function() {
  function $it(elementID) {
    return document.getElementById(elementID);
  }

  return {
    getRelativeID: function (baseid, targetid) {
      colpos = baseid.lastIndexOf(':');
      return baseid.substring(0, colpos + 1) + targetid;
    },    

   // See http://www.pageresource.com/jscript/jdropbox.htm for general approach
    operateSelectLink: function(linkid, localselectid) {
      var selectid = EvalSystem.getRelativeID(linkid, localselectid);
      var selection = $it(selectid);
      var url = selection.options[selection.selectedIndex].value;
      // See http://www.quirksmode.org/js/iframe.html for discussion
      document.location.href = url;
    },

    decorateReorderSelects: function(namebase, count) {
      var buttonid = namebase + "hiddenBtn";
      var button = $it(buttonid);
      for (var i = 0; i < count; ++ i) {
        var selectid = namebase + "item-row::"+i+":item-select-selection";
        var selection = $it(selectid);
        selection.onchange = function() {
        	button.click();
        };
      }
    }
  }
}();
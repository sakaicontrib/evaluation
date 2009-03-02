/**
 * Reordering utilities for the items drag and drop ordering page -AZ
 * Does not put stuff into the global namespace and uses only methods that are compatible with most browsers
 */
$(document).ready(
	function () {
        var siteId = $('#site-id').text();
        SakaiProject.fckeditor.initializeEditor("item-text", siteId);		
		$.facebox.grow();
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
			submitForm('#blockForm', 'item-text', 'modify_block', $('#saveBlockAction'));
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

function submitF(){  
					//submitForm('#blockForm', 'item-text:1:input', 'modify_block');
				//var rowId = $(jQuery.facebox.settings.objToUpdate).attr("id");
				//var thisRow = $(document).find("[id=" + rowId + "]");	
				alert('sdaf');
				return false;
				var d = $('#blockForm').formToArray();
				var textarea = 'item-text:1:input';
				var fckVal = function(){
					if (FCKeditorAPI) {
						return FCKeditorAPI.GetInstance(textarea).GetHTML(); //Actual editor textarea value
					}
					else {
						alert('Check if you have imported FCKeditor.js \n Error: FCKeditorAPI not found. ');
						return false;
					}
				}	
				//iterate through returned formToArray elements and replace input value with editor value
				for (var i = 0; i < d.length; i++) {
					if ($(d[i]).attr('name') == textarea) {
						$(d[i]).attr('value',fckVal);
					}
				}
				$.ajax({
					type: 'POST',
					url: 'modify_block',
					data: d,
					dataType: "html",
					beforeSend: function(){	
					//$(thisRow).html('<div class="loading">Refreshing...<img src="/library/image/sakai/spinner.gif"/></div>');
					//$("#facebox .results").html('<div class="loading">Saving...<img src="/library/image/sakai/spinner.gif"/></div>');
					//console.log(target + "   -   "+ d);
					alert('yes!');
					},
					error: function(rq, status, e){
						alert(rq.responseText);
						alert(status);
						alert(e);
						
					},
					success: function(msg){
						return false;/*$(document).trigger('close.facebox');
						var messageInformation = $(msg).find(".messageInformation").parent().text();
						var newRowVal = $(msg).find("[id=" + rowId + "]").html();
						$(thisRow).html(newRowVal);
						$(thisRow).find(".itemType").append('<span class="highlight">' + messageInformation + '!</span>');
						if (target == "modify_item") {
							$(".itemTable").html($(msg).find("#itemTable").html());
						}
						$(thisRow).before('<a name="popUpTop"></a>');
						window.location.href = "#popUpTop"			
						$('a[name*=popUpTop]').remove();			
						$('a[rel*=facebox]').facebox();
						$('a[rel*=remove]').itemRemove();
						startSort();*/
						alert('yes!');
					}
				});
     			}

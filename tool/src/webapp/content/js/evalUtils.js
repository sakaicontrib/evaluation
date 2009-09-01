/**
 * The EvalSystem set of functions (keeps the namespace clean and tidy) -AZ
 * DEPENDS on jQuery 1.2 to operate
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman - dropbox methods at end
 */
var EvalSystem = function() {
	//var $ = $ || function() { throw "JQuery undefined"; };
	//var RSF = RSF || function() { throw "RSF JS undefined"; };
	function $it(elementID) {
		return document.getElementById(elementID);
	}

	/**
	 * Because JQuery uses this sort of CSS based selector system, if you have 
	 * wacky characters in your tag ID (such as RSF Colons) you have to escape 
	 * them. At the moment this just escapes the colons, should probably have a
	 * fuller (or automatic) jquery escaping mechanism.
	 */
	function escIdForJquery(value) {
		var escaped = (value !== null ? "#" + value.replace(/:/g, "\\:") : null);
		escaped = (escaped === "#" ? null : escaped);
		return escaped;
	}

	/**
	 * Creates an adhoc group delete function for a specific link,
	 * this expects to be called by an event handler
	 */
	function makeAdhocDeleteFunction(deleteLink, adhocArea) {
		return function(event) {
			var url = deleteLink.attr("href");
			$.ajax({
				url: url,
				type: "DELETE",
				dataType: "text",
				cache: false,
				success: function(data, msg) {
				adhocArea.fadeOut(500, function() {
					adhocArea.hide(0);
					adhocArea.remove();
				});
			},
			error: function(request, msg, errorThrown) {
				var status = request.status;
				// 404 just means it does not exist which is ok
				if (status !== "404") {
					alert( "Failure: " + status + " :" + msg + " :" + errorThrown + " :" + XMLHttpRequest );
				}
			}
			});
			return false;
		};
	}

	return {

		/**
		 *  A little bit of javascript to validate the Eval Assign Page. At the 
		 *  moment this is only for making sure users have selected at least one
		 *  group or hierarchy node or something.
		 *
		 * @errorDivId A Div with an already translated message we can light up.
		 * @submitButtonId The button that is doing the submitting.
		 */
		initEvalAssignValidation: function(formId, errorDivId, submitButtonId) {
			var form = $(escIdForJquery(formId));
			var errorDiv = $(escIdForJquery(errorDivId));
			var submitButton = $(escIdForJquery(submitButtonId));
	
			// Boolean to store whether the form should submit based on validation.
			var passes = false;
	
			form.submit(function() {
				if (!passes) {
					errorDiv.show();
					RSF.getDOMModifyFirer().fireEvent();
				}
				return passes;
			});
	
			var onAssignClick = function(event) {
				passes = false;
				var n = $(".evalgroupselect:checked").length;
				if (n > 0) {
					passes=true;
				}
			};
	
			submitButton.click( function(event) { onAssignClick(event); } );
		},
	
		/**
		 * Handle the setup of all adhoc group delete handlers
		 */
		initEvalAssignAdhocDelete: function(adhocAreaIds) {
			// get all adhoc areas (these will be hidden when the group is deleted)
			for (var i = 0; i < adhocAreaIds.length; i++) {
				var adhocAreaId = adhocAreaIds[i];
				var adhocArea = $(escIdForJquery(adhocAreaId));
				var deleteLink = $(escIdForJquery(adhocAreaId + "deleteGroupLink"));
				if (adhocArea.length > 0 && deleteLink.length > 0) {
					deleteLink.show(0); // only show the links which actually could be found
					var onAdhocDeleteClick = makeAdhocDeleteFunction(deleteLink, adhocArea);
					deleteLink.click( onAdhocDeleteClick );
				} else {
					// failed to find the id given, nothing really to do here
					//alert("Failed to find adhoc elements: " + adhocAreaId + " :" + adhocArea + " :" + deleteLink);
				}
			}
		},
	
		initEvalReportView: function (allCommentsId, allTextResponsesId) {
			// for now this is just enabling toggling of comments and responses
			// see the makeToggle method for description of arguments
			if (allCommentsId.length > 0) {
				$(escIdForJquery(allCommentsId)).show();
				EvalSystem.makeToggle(allCommentsId + "Show", allCommentsId + "Hide", null, "showcomments", false);
			}
			if (allTextResponsesId.length > 0) {
				$(escIdForJquery(allTextResponsesId)).show();
				EvalSystem.makeToggle(allTextResponsesId + "Show", allTextResponsesId + "Hide", null, "showtextresponses", false);
			}
		},
	
		/**
		 * Adds a limitation of numbers only to an input box (inputId),
		 * displays the numbers only warning in the message area (msgId)
		 */
		addNumericOnly: function(inputId, msgId) {
			var inputBox = $(escIdForJquery(inputId));
			var msgSpan = $(escIdForJquery(msgId));
			inputBox.keypress(function (e) {
				//if the letter is not digit then display error and don't type anything
				if( e.which !== 13 && e.which !== 8 && e.which !== 0 && (e.which < 48 || e.which > 57)) {
					//display error message
					msgSpan.html("Numbers Only").show().fadeOut(3000);
					return false;
				}
			});
		},
	
		/**
		 * This will hide an area (or a set areas that match the toggleClass)
		 * when the element with hideId is clicked (it will also be hidden),
		 * it will show the area(s) and the hideId element when the element with showId is clicked,
		 * and also hide the showId element
		 * showId: the id of the element that triggers the show when clicked
		 * hideId: the id of the element that triggers the hide when clicked
		 * areaId: the id of area (div probably) to show/hide, set null if not used
		 * toggleClass: a classname such that all elements with this class will be shown/hidden, set null if not used
		 * initialHide: if true then start things hidden, else start things shown (default if unspecified)
		 */
		makeToggle: function (showId, hideId, areaId, toggleClass, initialHide) {
			var showButton = $(escIdForJquery(showId));
			var hideButton = $(escIdForJquery(hideId));
			var area = $(escIdForJquery(areaId));
			var toggles = $(toggleClass === null ? null : "." + toggleClass);
	
			var togglearea = showButton.parent().parent();
	
			showButton.show();
	
			hideButton.show();
			showButton.parent().next().wrap("<a href=\"#\"></a>");
	
			var showAction = function(event) {
				showButton.hide();
				hideButton.show();
				area.show("normal");
				toggles.show("normal");
				RSF.getDOMModifyFirer().fireEvent();
			};
	
			var hideAction = function(event) {
				showButton.show();
				hideButton.hide();
				area.hide("normal");
				toggles.hide("normal");
				RSF.getDOMModifyFirer().fireEvent();
			};
	
			if (initialHide) {
				hideAction();
			} else {
				showAction();
			}
	
			var toggle = function(event) {
				showButton.toggle();
				hideButton.toggle();
				area.toggle("normal");
				toggles.toggle("normal");
				RSF.getDOMModifyFirer().fireEvent();
			};
	
			togglearea.click( function (event) { toggle(event); } );
	
			/*showButton.click( function (event) { showAction(event) });
	        hideButton.click( function (event) { hideAction(event) });*/
		},
	
		// this just makes it easier to add in more stuff later -AZ
		initEvalSettings: function (areaId, selectId, selectValue, reminderId) {
			// see the toggleAndDisableSetReminder method for description of arguments
			EvalSystem.toggleAndDisableSetReminder(areaId, selectId, selectValue, reminderId);
		},
	
		/**
		 * This will hide and disable the setReminder option area
		 * and force the setting to 0 reminders on the evaluation settings page 
		 * when the anonymous auth option is selected OR reveal the option area
		 * areaId: the id of area (div probably) to show/hide
		 * selectId: the id of the select that triggers the show/hide
		 * selectValue: the value of the select that causes the hide action (other cause show)
		 * reminderId: the id of the form control to set when hide action happens
		 */
		toggleAndDisableSetReminder: function (areaId, selectId, selectValue, reminderId) {
			var area = $("#"+areaId); //document.getElementById(areaId);
			var select = $("#"+selectId); //document.getElementById(selectId);
			var reminder = $("#"+reminderId); //document.getElementById(reminderId);
	
			var changeAction = function(event) {
				var selectedValue = event.target.value;
				if (selectedValue === selectValue) {
					// values are the same so we need to hide things
					// only take action if needed so we are not resetting the field constantly
					if ( area.is(':visible') ) {
						// area is currently visible so hide it
						area.hide("normal");
						// force reminder to the no reminders setting of 0
						reminder.val('0');
					}
				} else {
					// set to one of the non-hiding options
					if ( area.is(':hidden') ) {
						// area is currently hidden so reveal it
						area.show("slow");
					}
				}
			};
			// add the event for onchange to trigger the changeAction method and cause it to fire immediately
			select.change( function(event) { changeAction(event); }).change();
		},
	
		getRelativeID: function (baseid, targetid) {
			var colpos = baseid.lastIndexOf(':');
			return baseid.substring(0, colpos + 1) + targetid;
		},
	
		// See http://www.pageresource.com/jscript/jdropbox.htm for general approach
		operateSelectLink: function(linkid, localselectid) {
			var selectid = EvalSystem.getRelativeID(linkid, localselectid);
			var selection = $it(selectid);
			var url = selection.options[selection.selectedIndex].value;
            evalTemplateFacebox.addItem( url );
		}
	
		/*decorateReorderSelects: function(namebase, count) {
			var makeButtonClick = function(b) {
				return function() {
					b.click();
				};
			};
			var buttonid = namebase + "hiddenBtn";
			var button = $it(buttonid);
			for (var i = 0; i < count; ++ i) {
				var selectid = namebase + "item-row:"+i+":item-select-selection";
				var selection = $it(selectid);

				//selection.onchange = makeButtonClick(button);

               // selection.onchange = function(){
                 //   alert(44);
                //};
                $(selection).bind('onChange', function(){
                    alert(4434);
                });

                var order = [];
                $("#itemList > div").not('.ui-sortable-helper').each(function(){
                    order.push($(this).find('a[templateitemid]').attr('templateitemid'));
                });
                var params = {
                    orderedIds : order.toString()
                };

                evalTemplateData.item.saveOrder(evalTemplateUtils.pages.eb_save_order, params);
                
			}
		}
*/
	};
}();
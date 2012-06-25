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
/**
 * Sakai Evaluation System project
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009, 2010, 2011, 2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * - Aaron Zeckoski (azeckoski)
 **********************************************************************************/

/**
 * The EvalSystem set of functions (keeps the namespace clean and tidy) -AZ
 * DEPENDS on jQuery 1.2 to operate
 * @author Aaron Zeckoski (azeckoski)
 * @author Antranig Basman - dropbox methods at end
 */
var EvalSystem = function() {
	
	if (!jQuery) {
		throw "JQuery undefined";
	}
	
	if (!RSF) {
		throw "RSF JS undefined";
	}
	
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
		initEvalAssignValidation: function(formId, errorDivId, submitButtonId, anonymousAllowed) {
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
				
				// EVALSYS-987
				if (anonymousAllowed == "true") { 
					n++;
				}
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
		
		initModifyHierarchyNodePerms: function(userEidInputId, addUserButtonId, noUserEidErrorMsg, noPermsErrorMsg) {
			
			var userEidInput = $(escIdForJquery(userEidInputId));
			var addUserButton = $(escIdForJquery(addUserButtonId));
			var savePermsButtons = $('.saveButton input:submit');
			
			var isPermSelected = function(targetButton) {
				
				var userRow = $(targetButton).parents('tr.userPermsRow');
				var checkedPerms = $(':checked', userRow);
				
				if (checkedPerms.size() == 0)
					return false;
				
				return true;
				
			};
			
			var validateUserPerms = function(targetButton) {
				
				if (!isPermSelected(this)) {
					alert(noPermsErrorMsg);
					return false;
				}
				
				return true;
				
			};
			
			var validateNewUserFields = function() {
				
				if (userEidInput.val().trim().length == 0) {
					alert(noUserEidErrorMsg);
					return false;
				}
				
				if (!isPermSelected(this)) {
					alert(noPermsErrorMsg);
					return false;
				}

				return true;
				
			};
			
			addUserButton.click(validateNewUserFields);
			savePermsButtons.click(validateUserPerms);
			
		},
		
		initEvalAdminView: function() {
			
			var assignButton = $("input.assignButton");
			var userEidInput = $("input.userEidInput");
			
			var assignCallback = function() {
				
				if ($.trim(userEidInput.val()).length == 0) {
					alert("You must enter a valid user eid.");
					return false;
				}
					
				return true;
				
			};
			
			assignButton.click(assignCallback);
			
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
		
		instrumentMatrixItem: function() {
			/////////////////////////////////
			// Scales
			/////////////////////////////////

			// Runs methods for assigning widths to elements of the individual and grouped scale items.

			// Needs to fire as soon as the document is ready.

			// Compact Scale
			/////////////////////////////////
			// To achieve the inline display of the title and the content, both block elements are floated with css.
			// This is great except when the title is long enough to overlap the floated content.
			// In that scenario, the title is pushing the content to the next line.
			// To prevent that, will calculate and set the space available to the title.
			// Calculate itemWidth and contentWidth.
			// Then set: titleWidth = itemWidth - contentWidth.

			// Method for calculating and setting the title width.
			setScaleTitleWidth = function() {
				// Scope: define variables for this function.
				var scaleItems, itemWidth, contentWidth, titleWidth;

				// Init: set the variable values.
				scaleItems = $(".scale");
				itemWidth = null;
				contentWidth = null;
				titleWidth = null;

				// Apply: do stuff using the variables.
				// For each scale item, determine item and content width, then set title width.
				$.each(scaleItems, function(index, value) {
					var container = $(value);
					itemWidth = container.innerWidth();
					contentWidth = container.find(".content").outerWidth();
					titleWidth = itemWidth - contentWidth - 20;
					container.find(".title").attr("style", "width:" + titleWidth + "px;");
				});

				// Console for debugging.
				//console.debug("scaleItems", scaleItems, "itemWidth", itemWidth, "contentWidth", contentWidth, "titleWidth", titleWidth);
			};

			// Grouped Items
			/////////////////////////////////
			// Similar to Compact Scales above, grouped scale items have the overal scale descriptor (legend) inline with the group title.
			// Different than the individual scale item, however, in the item-group both elements are positioned absolutely via css.
			// Because of that setup, both elements need to be given widths to match the item-group content and not overlap.

			// Method for calculating and setting the title and legend widths of grouped items.
			setGroupedItemsWidths = function() {
				// Scope: define variables for this function.
				var itemGroups, groupWidth, scaleWidth, legendWidth, titleWidth;

				// Init: set the variable values.
				itemGroups = $(".item-group");
				groupWidth = null;
				scaleWidth = null;
				legendWidth = null;
				titleWidth = null;

				// Apply: do stuff using the variables.
				// For each scale item, determine item and content width, then set title width.
				$.each(itemGroups, function(index, value) {
					// Scope
					var container, scale, legend, title;
					// Init
					container = $(value);
					scale = container.find("> .content > fieldset > .item .content .response-list");
					legend = container.find("> .content > fieldset > legend");
					title = container.find("> .title");
					// Apply
					groupWidth = container.innerWidth();
					scaleWidth = scale.outerWidth();
					legendWidth = scaleWidth;
					titleWidth = groupWidth - legendWidth - 20;
					legend.attr("style", "width:" + legendWidth + "px;");
					title.attr("style", "width:" + titleWidth + "px;");
				});

				// Console for debugging.
				console.debug("itemGroups", itemGroups, "groupWidth", groupWidth, "scaleWidth", scaleWidth, "legendWidth", legendWidth, "titleWidth", titleWidth);
			};

			// Also needs to fire when the browser window is resized.
			$(window).resize(function() {
				setScaleTitleWidth();
				setGroupedItemsWidths();	
			});
			
			setScaleTitleWidth();
			setGroupedItemsWidths();

			/////////////////////////////////
			// Response List Behavior
			/////////////////////////////////
			// Natural form behavior: clicking the label checks/unchecks associated checkbox or radio button.
			// Orignally had the click event trigger on the wrapping label.
			// Discovered, however, that the event double-triggers on both the label and the input.
			// Because the click event on the label triggers a click event on the corresponding input.
			// This results in the function running twice.
			// Therefore, as it is coded now, the click event that triggers the function comes only from the input.
			// Which is also triggered when the label is clicked.
			// This results in the function properly executing once.
			// And allows for either the label or the input to be clicked in the UI.

			// mark initially selected inputs with "selected" class so that labels display correctly
			$('.response-list input:checked').parent("label").addClass('selected');
			
			// when inputs are clicked, add "selected" class so that labels display correctly
			var responseItems = $(".response-list input");	
			responseItems.click(function(event){
				var parentResponseList = $(event.target).parents(".response-list");
				var inputItems = $(".selected", parentResponseList);
				inputItems.removeClass("selected");
				$(event.target).parent("label").addClass("selected");
			});

		},
	
		// See http://www.pageresource.com/jscript/jdropbox.htm for general approach
		operateSelectLink: function(linkid, localselectid) {
			var selectid = EvalSystem.getRelativeID(linkid, localselectid);
			var selection = $it(selectid);
			var url = selection.options[selection.selectedIndex].value;
            evalTemplateFacebox.addItem( url );
		}
	};
}();
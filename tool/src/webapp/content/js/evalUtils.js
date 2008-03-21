/**
 * The EvalSystem set of functions (keeps the namespace clean and tidy) -AZ
 * DEPENDS on jQuery 1.2 to operate
 * @author Antranig Basman
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
var EvalSystem = function() {
  function $it(elementID) {
    return document.getElementById(elementID);
  }

  return {
  
  	initAssignAdhocGroupArea: function (saveButtonId,clearButtonId,
        groupNameInputId,emailInputId,emailListDivId,uvburl) {
        var groupNameInput = $("#"+groupNameInputId);
        var emailListInput = $("#"+emailInputId);
        var emailListDiv = $("#"+emailListDivId);
        var saveButton = $("#"+saveButtonId);
        var clearButton = $("#"+clearButtonId);
        
        var adhocGroupId = null;
        
        var saveUpdateEmailsAction = function(event) {
            //alert("Saving emails" + groupNameInput.val() + "\n" + emailListInput.val());
            //alert("The URL is: " + uvburl);
            //alert("Going:");
            updater();
        }
        
        var clearEmailsAction = function(event) {
            alert("Clearing emails");
        }
        
        var saveCallback = function(event) {
            alert("Done with Ajax");
        }
        
        //var updater = RSF.getAJAXUpdater([inputField], ajaxUrl, [elBinding], callback);
        var updater = RSF.getAJAXUpdater([], uvburl, [], saveCallback);
        
        saveButton.click( function (event) { saveUpdateEmailsAction(event) });
        clearButton.click( function (event) { clearEmailsAction(event) });
  	},
  
    /**
     * This will bind a checkbox to a some area (usually a div) so that clicking
     * the checkbox will hide/show the area.
     *
     * This is initially for the evaluation_assign page, where we are collapsing
     * and showing the different options for evaluation assignment.
     *
     * areaId: The ID of the area to show/hide
     * checkboxId: The ID of the checkbox to trigger the hide/show
     */
    hideAndShowRegionWithCheckbox: function (areaId, checkboxId) {
        var area = $("#"+areaId);
        var checkbox = $("#"+checkboxId);
        
        var changeAction = function(event) {
            var checkboxValue = event.target.checked;
            
            // If the checkbox becomes clicked and the area is hidden unhide it.
            // If the checkbox is unclicked and it's visible then hide it.
            if (checkboxValue && area.is(':hidden')) {
                area.show("slow");
            } 
            else if (!checkboxValue && area.is(':visible')) {
                area.hide("normal");
            }
            else {
                // Do Nothing
            }
        }
        
        checkbox.change( function (event) { changeAction(event) }).change();
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
            if (selectedValue == selectValue) {
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
        }
        // add the event for onchange to trigger the changeAction method and cause it to fire immediately
        select.change( function(event) { changeAction(event) }).change();
    },    
 
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
        var selectid = namebase + "item-row:"+i+":item-select-selection";
        var selection = $it(selectid);
        selection.onchange = function() {
        	button.click();
        };
      }
    }
 
  }
}();
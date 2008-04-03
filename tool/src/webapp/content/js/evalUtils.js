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
  
  /**
   * Because JQuery uses this sort of CSS based selector system, if you have 
   * wacky characters in your tag ID (such as RSF Colons) you have to escape 
   * them. At the moment this just escapes the colons, should probably have a
   * fuller (or automatic) jquery escaping mechanism.
   */
  function escIdForJquery(value) {
    return value != null ? "#" + value.replace(/:/g, "\\:") : null;
  }
  
  /**
   * NOTE: THis is moving to RSF TRunk. as soon as we upgrade to 0.7.3M3 we 
   *       should start using this from the RSF Namespace.
   *
   * This is the meat and potatos RSF UVB function I've always wanted,
   * designed for the UVB enthusiast who knows what they want. It takes
   * the UVB URL, the bindings/values to send over, the bindings you 
   * want back, and an optional action binding.
   *
   * @token A special token depicting this request. You can often put in whatever
   * you like, but this can be used to stop the dreaded double submission effects.
   * If a post with the same token is already being processed, subsequent ones
   * will be ignored.
   *
   * @uvburl The url. In practice this usually looks something like,
   *   http://server/myapp/faces/UVBview, though it's best to generate it with
   *   viewStateHandler.getFullURL(new SimpleViewParameters(UVBProducer.VIEW_ID))
   *
   * @inbindings This should be a standard object hash of bindings to values.
   *   ex. inbindings['mybean.value1'] = 'one';  
   *       inbindings['mybean.value2'] = 'two';
   *
   * @outbindings This should be an array of bindings you want back from the request.
   *   ex. outbindings[0] = 'mybean.value3'
   *       outbindings[1] = 'mybean.value4'
   *
   * @actionbinding This should a String with the actionbinding. Can be null if
   *   you don't want one.  ex. 'mybean.execute'
   *
   * @callback This should be a standard javascript object containing the usual
   * callback functions such as success.
   */
  function fireUVBRequest(token, uvburl, inbindings, outbindings, actionbinding, callback) {
    var queries = new Array();
    for (var i in inbindings) {
      queries.push(RSF.renderBinding(i,inbindings[i]));
    }
    if (actionbinding != null) {
      queries.push(RSF.renderActionBinding(actionbinding));
    }
    for (var i in outbindings) {
      queries.push(RSF.renderUVBQuery(outbindings[i]));
    }
    var body = queries.join("&");
    RSF.queueAJAXRequest(token,"POST",uvburl,body,callback);
  }

  return {
  
    /*
     *  This code is largely in exploration mode and obviously very broken
     *  still, random things commented out here and there. But shouldn't show up 
     *  at all unless you've enabled adhoc groups in the admin settings.
     *
     *  Evaluation will still compile, but this is not going to work until we
     *  move up to 0.7.3M2, but this functionality is disabled by default.
     */
  	initAssignAdhocGroupArea: function (saveButtonId,addMoreUsersButtonId,
        groupNameInputId,emailInputId,emailListDivId,uvburl,cloneMemberBranchId) {
        var groupNameInput = $(escIdForJquery(groupNameInputId));
        var emailListInput = $(escIdForJquery(emailInputId));
        var emailListDiv = $(escIdForJquery(emailListDivId));
        var saveButton = $(escIdForJquery(saveButtonId));
        var addMoreUsersButton = $(escIdForJquery(addMoreUsersButtonId));
        var cloneMemberBranch = $(escIdForJquery(cloneMemberBranchId));
        
        var adhocGroupId = null;
        
        var outbindings = ['adhocGroupsBean.adhocGroupId', 'adhocGroupsBean.acceptedInternalUsers',
          'adhocGroupsBean.acceptedAdhocUsers', 'adhocGroupsBean.rejectedUsers'];
   
        var saveUpdateEmailsAction = function(event) {
            var inbindings = new Object();
            var outbindings = new Array();
            if (adhocGroupId == null) {
                inbindings['adhocGroupsBean.adhocGroupTitle'] = groupNameInput.val();
                inbindings['adhocGroupsBean.newAdhocGroupUsers'] = emailListInput.val();
                fireUVBRequest('atoken', uvburl, inbindings, outbindings, 'adhocGroupsBean.addNewAdHocGroup', saveCallback);
            }
            else {
                inbindings['adhocGroupsBean.adhocGroupId'] = adhocGroupId;
                inbindings['adhocGroupsBean.newAdhocGroupUsers'] = emailListInput.val();
                fireUVBRequest('atoken', uvburl, inbindings, outbindings, 'adhocGroupsBean.addUsersToAdHocGroup', saveCallback);
            }
            emailListInput.val('');
        }
        
        var clearEmailsAction = function(event) {
            //alert("Clearing emails");
        }
        
        var updateParticipantDiv = {
            success: function(response) {
                //alert("What??" + response.responseXML);
            }
        }
        
        var saveCallback = {
            success: function(response) {
                var UVB = RSF.accumulateUVBResponse(response.responseXML);
                
                var adhocGroupId = UVB.EL["adhocGroupsBean.adhocGroupId"];
                var acceptedInternalUsers = UVB.EL["adhocGroupsBean.acceptedInternalUsers"];
                for (var i in acceptedInternalUsers) {
                    var nextMember = cloneMemberBranch.clone();
                    nextMember.find("td:eq(0)").text(acceptedInternalUsers[i]);
                    nextMember.show().insertAfter(cloneMemberBranch);
                }
                alert(UVB.EL["adhocGroupsBean.acceptedInternalUsers"]);
                alert(UVB.EL["adhocGroupsBean.acceptedAdhocUsers"]);
                alert(UVB.EL["adhocGroupsBean.rejectedUsers"]);
                
                saveButton.hide();
                addMoreUsersButton.show();
            }
        }
        
        saveButton.click( function (event) { saveUpdateEmailsAction(event) });
        addMoreUsersButton.click();
        //clearButton.click( function (event) { clearEmailsAction(event) });
  	},
    
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
        }
        
        submitButton.click( function(event) { onAssignClick(event) } );
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
        var toggles = $(toggleClass == null ? null : "." + toggleClass);

        showButton.show();
        hideButton.show();

        var showAction = function(event) {
            showButton.hide();
            hideButton.show();
            area.show("normal");
            toggles.show("normal");
            RSF.getDOMModifyFirer().fireEvent();
        }

        var hideAction = function(event) {
            showButton.show();
            hideButton.hide();
            area.hide("normal");
            toggles.hide("normal");
            RSF.getDOMModifyFirer().fireEvent();
        }

        if (initialHide) {
            hideAction();
        } else {
            showAction();
        }

        showButton.click( function (event) { showAction(event) });
        hideButton.click( function (event) { hideAction(event) });
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
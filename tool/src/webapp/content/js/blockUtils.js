function countCheckBox(){
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

    $(".itemLine2").removeAttr('style');   
    if (count_CheckBoxes_CHECKED > 0){
		for (var a in checkBoxes_CHECKED_array) {
			var target = $(checkBoxes_CHECKED_array[a]),
                targetId = target.attr('id');
                target.parents(".itemLine2:eq(0)").attr('style', 'background-color:#FFFFCC;');
                    var scaleId = targetId.substring(targetId.indexOf('-')+1, targetId.lastIndexOf('-'));
                    //disable other check box which has different scale id
                     for(var b in checkBoxes_ALL_array){
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
		submitButtonBlock.className="itemOperations";
	}else if(count_CheckBoxes_CHECKED < 2){
		//alert("select less than 2 items,disable submit button");
		submitButton.disabled = true;
		submitButtonBlock.className="itemOperations";
	}else{
        //alert("select 2 or more items,enable submit button");
		submitButton.disabled = false;
		submitButtonBlock.className="itemOperationsEnabled";
	}
	
}//end of function

function extractSelectedItems(submitButton) {
	var checkBoxes_CHECKED = $("label.itemCheckbox input[type=checkbox]:checked"),
        checkBoxes_CHECKED_array = checkBoxes_CHECKED.get(),
        destinationForm = submitButton.form;

	if (checkBoxes_CHECKED.length > 0 && destinationForm) {
		var idList = [];
		// look for checkboxes which have IDs of the form "block-12-11" and keep track of the ones that are checked.
        for (var a in checkBoxes_CHECKED_array) {
			var target = $(checkBoxes_CHECKED_array[a]),
                targetId = target.attr('id'),
                scaleId = targetId.substr(targetId.lastIndexOf('-')+1);
                idList.push(scaleId);
        }

        destinationForm.templateItemIds.value=idList.toString();

		if (checkBoxes_CHECKED.length < 2) {
            alert(evalTemplateUtils.messageLocator('modifytemplate.group.warn.minimum'));
		 	return false; 
		} else {
			$(submitButton).parent().ajaxSubmit({
				success: function(msg){
                    //Unbind current reveal event
                    $(document).unbind('reveal.facebox');
                    //Bind new reveal event
                    $(document).bind('reveal.facebox', function() {
                        evalTemplateLoaderEvents.modify_block();
                        $('#saveBlockAction')
                                .unbind("click")
                                .bind("click", function(){
                            			var childIdList = [],
                                        reuseBlockText = $('.blockText input[type=radio][checked][value!=new1]').length > 0;
                                        $('#blockForm input[name*=hidden-item-id]').each(function(){
                                            childIdList.push($(this).val());
                                        });
                                        $("#ordered-child-ids").val(childIdList.toString());
                                        return true;
                                    });

                    });
					$.facebox(msg);
				}
		    });
			return false;
		}
	}
	return false;
}
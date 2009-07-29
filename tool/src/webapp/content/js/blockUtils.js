function countCheckBox(){
	//1)if total cout of checked checkbox > 2, enable "Create Block" button
	//2) disable all boxes which are not using the same scale as the first one that is checked
	//3)if no check box is checked, all the check box should be enabled
	sourceForm = document.modify_form_rows;
	destinationForm = document.create_block_form;
	submitButton = document.getElementById("createBlockBtn");
	submitButtonBlock = document.getElementById("blockInputs");
	
	var count = 0; //enforce at least 2 check box are checked	
	var scaleId = "";
	
	if (sourceForm && destinationForm) {			
		// look for checkboxes which have IDs of the form "block-12-11" and keep track of the ones that are checked.
		for (var a=0; a < sourceForm.elements.length; a++) {
			var target = sourceForm.elements[a];
			if(target.type == "checkbox"){
				if(target.checked){
					count = count +1;
						$(target.parentNode.parentNode.parentNode).attr('style', 'background-color:#FFFFCC;');
						if(count == 1){
						scaleId = target.id.substring(target.id.indexOf('-')+1,target.id.lastIndexOf('-'));
						
						//disable other check box which has different scale id
						 for(var b=0; b < sourceForm.elements.length; b++){
							 var other = sourceForm.elements[b];
							 if(other != target && other.type == "checkbox"){
							 	var otherScaleId= other.id.substring(other.id.indexOf('-')+1,other.id.lastIndexOf('-'));
                                 //console.log(target.id.indexOf('-')+1);
                                 //alert("scaleId ="+scaleId);
							 	if(otherScaleId != scaleId)
								{
							 		other.disabled = true;
									other.parentNode.className='notselectable';  
									    
								}	
							 	else{
									other.disabled = false;	
									other.parentNode.className='selectable '; //UI hint of which checkboxes are selectable
								
								}
							 }
						 }
								 
					}
					
				}else{
					$(target.parentNode.parentNode.parentNode).removeAttr('style');
				}
			}
		}
	}
	//alert("scaleId= "+scaleId);
	if(count < 1){ //no check box is checked, enable all the checkbox
		for (var c=0; c < sourceForm.elements.length; c++){
			if (sourceForm.elements[c].type=="checkbox"){
					sourceForm.elements[c].disabled = false;
					sourceForm.elements[c].parentNode.className='itemCheckbox'; //UI hint of which checkboxes are selectable
					
			}		
				
		}
		submitButton.disabled = true;
		submitButtonBlock.className="itemOperations";
	}else if(count < 2){ 
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
	//sourceForm = document.getElementById("modifyFormRows");
	sourceForm = document.modify_form_rows;
	destinationForm = submitButton.form;
	var count = 0; //enforce at least 2 check box are checked
	
	if (sourceForm && destinationForm) {
		var idList = "";
		// look for checkboxes which have IDs of the form "block-12-11" and keep track of the ones that are checked.
		for (var a=0; a < sourceForm.elements.length; a++) {
			if (sourceForm.elements[a].type=="checkbox" && sourceForm.elements[a].checked) {
				if (idList.length > 0) { idList += ","; }
				idList += sourceForm.elements[a].id.substr(sourceForm.elements[a].id.lastIndexOf('-')+1);
				count = count + 1;
			}
		}

		destinationForm.templateItemIds.value=idList;
	
		//alert("ids="+destinationForm.templateItemIds.value);
		
		if (count < 2) {
		 	alert("you must select at least 2 items to create a group");
		 	return false; 
		} else {
			
			$(submitButton).parent().ajaxSubmit({
				success: function(msg){
                    //Unbind current reveal event
                    $(document).unbind('reveal.facebox');
                    //Bind new reveal event
                    $(document).bind('reveal.facebox', function() {
                        evalTemplateLoaderEvents.modify_block();
                    });
					$.facebox(msg);
				}
		});
			
			return false;
		}
	}

	return false;
}
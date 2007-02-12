//TODO:Javacsript can check if checked Ids hasv the same scale ID
function extractSelectedItems(submitButton) {
	sourceForm = document.getElementById("modifyFormRows");
	destinationForm = submitButton.form;
	var count = 0; //enforce at least 2 check box are checked
	
	if (sourceForm && destinationForm) {
		var idList = "";
		
		// look for checkboxes which have IDs of the form "block-12-11" and keep track of the ones that are checked.
		for (var a=0; a < sourceForm.elements.length; a++) {
			if (sourceForm.elements[a].type=="checkbox" &&
sourceForm.elements[a].checked) {
				if (idList.length > 0) { idList += ","; }
				idList += sourceForm.elements[a].id.substr(sourceForm.elements[a].id.lastIndexOf('-')+1);
				count = count + 1;
			}
		}
		destinationForm.templateItemIds.value=idList;
	
		//alert("ids="+destinationForm.templateItemIds.value);
		if(count < 2){
		 alert("you must select at least 2 items to create a block");
		 return false; 
		}
		else return true;
	}

	return false;
}

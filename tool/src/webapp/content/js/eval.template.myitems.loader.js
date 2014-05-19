// Eval My Items JS for page onload function
// @author lovemore.nalube@uct.ac.za


$(document).ready(function () {
    $('a[@rel=facebox]').faceboxGrid();
			$('form.inlineForm :submit').bind("click", function(){
                evalTemplateFacebox.addItem( $('form').attr("action") + "?" + $("form").formSerialize() );
                return false;
		    });
    //init Global Ajax Overide Options
    evalTemplateData.ajaxSetUp();
});

//trigger FB overrides
evalTemplateFacebox.init();

// Eval Template Modify JS for page onload function
// @author lovemore.nalube@uct.ac.za

$(document).ready(function () {
    startSort();
    $(document).trigger('activateControls.templateItems');
    buildSortableIds();
    //init Global Ajax Overide Options
    evalTemplateData.ajaxSetUp();

});

//trigger FB overrides
evalTemplateFacebox.init();

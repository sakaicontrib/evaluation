// Eval Template Modify JS for page onload function
// @author lovemore.nalube@uct.ac.za


$(document).ready(function () {
    startSort();
    $(document).trigger('activateControls.templateItems');
    buildSortableIds();
    //init Global Ajax Overide Options
    evalTemplateData.ajaxSetUp();
    
    // This will initialize the plugin
    // and show two dialog boxes: one with the text "Ol‡ World"
    // and other with the text "Good morning John!"
    /*jQuery.i18n({
        name:'messages',
        path:'/sakai-evaluation-tool/content/bundle/',
        callback: function() {
            alert(general.direct.link.title);
            alert(general.category.link.tip('John'));
        }
    });
    */
});

//trigger FB overrides
evalTemplateFacebox.init();

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
var evalsys = evalsys || {};

if (!jQuery) {
    throw "JQuery undefined";
}

/**
 * This will center any jquery object it is executed on,
 * if the container cannot be found then this will produce a failure alert
 *
 * @param options [OPTIONAL] specify the options to use when centering
 *      containerSelector - specify the selector for the container to center within, DEFAULT: window
 *      containerResize - if true then the container will be shrunk to fit in the window, DEFAULT: true
 *      elementSelector - specify the selector for element we are measuring the height and width of, DEFAULT: this
 *      elementResize - if true, the element will be shrunk to fit within the container, DEFAULT: true
 *      horizontal - center this horizontally, DEFAULT: true
 *      vertical - center this vertically, DEFAULT: true
 *      debug - enable debugging alerts, DEFAULT: false
 */
jQuery.fn.center = function (options) {
    var debug = false;
    var containerResize = true;
    var elementResize = true;
    var horizontal = true;
    var vertical = true;

    // SET OPTIONS
    if (typeof options === "undefined") { options = {}; }
    if ("debug" in options) {
        debug = (options.debug === true);
    }
    if ("containerResize" in options) {
        containerResize = (options.containerResize === false) ? false : true;
    }
    if ("elementResize" in options) {
        elementResize = (options.elementResize === false) ? false : true;
    }
    if ("horizontal" in options) {
        horizontal = (options.horizontal === false) ? false : true;
    }
    if ("vertical" in options) {
        vertical = (options.vertical === false) ? false : true;
    }
    var $container;
    if ("containerSelector" in options && typeof options.containerSelector === "string") {
        $container = jQuery(options.containerSelector);
    } else {
        $container = jQuery(window);
    }
    var $element;
    if ("elementSelector" in options && typeof options.elementSelector === "string") {
        $element = this.find(options.elementSelector);
        if ($element.length === 0) {
            // expand the search
            $element = this.find(options.elementSelector);
        }
        if ($element.length === 0) {
            // fall back to just using this
            if (debug) {
                alert('ERROR: invalid elementSelector ('+options.elementSelector+') for center (fail safe to using: this)');
            }
            $element = this;
        } else {
            $element = $element.first(); // only use the first one
        }
    } else {
        $element = this;
    }

    // PROCESS centering
    var topPos = 0;
    var leftPos = 0;
    if ($container.length && (horizontal || vertical)) {
        this.css("position","absolute"); // must switch the position to absolute first
        topPos = ( ( $container.height() - $element.height() ) / 2 ) + $container.scrollTop();
        // check to make sure the width is within the screen
        var screenWidth = $("body").innerWidth(); //$(document).width()
        if (containerResize && $container.width() > screenWidth) {
            $container.width(screenWidth); // resize container to fit within the screen
        }
        // check to make sure the element is within the container
        if (elementResize && $element.width() > $container.width()) {
            $element.width($container.width() - 20); // resize element to fit in the container
        }
        leftPos = ( ( $container.width() - $element.width() ) / 2 ) + $container.scrollLeft();
        if (debug) {
            alert("thing: "+$element.width()+"x"+$element.height()+" window: "+$container.width()+"x"+$container.height()+" scroll: "+$container.scrollLeft()+"x"+$container.scrollTop()+" -> "+leftPos+"x"+topPos);
        }
        if (vertical) {
            this.css("top", topPos + "px");
        }
        if (horizontal) {
            this.css("left", leftPos + "px");
        }
    } else {
        alert('ERROR: invalid containerSelector ('+options.containerSelector+') for center');
    }
    return this;
};


evalsys.instrumentBlockItem = function(){
    $('label.blockItemLabel,label.blockItemLabelNA').click(function(){
        var choiceGroup = $(this).parents('.choiceGroup');
        $(choiceGroup).find('label').removeClass('blockItemLabelSelected').removeClass('blockItemLabelSelectedNA');
        $(choiceGroup).find('.itemDoneCheck').addClass('itemDoneCheckShow');

        if ($(this).hasClass('blockItemLabel')) {
            $(this).addClass('blockItemLabelSelected');
        }
        else {
            $(this).addClass('blockItemLabelSelectedNA');
        }
    });

    $('.blockItemLabel,.blockItemLabelNA').each(function(){
        if ($(this).children('input:checked').length == 1) {
            $(this).parents('.choiceGroup').find('.itemDoneCheck').addClass('itemDoneCheckShow');
            if ($(this).hasClass('blockItemLabel')) {
                $(this).addClass('blockItemLabelSelected');
            }
            else {
                $(this).addClass('blockItemLabelSelectedNA');
            }

        }
    });

    $('.blockItemGroup').each(function(x){
        var headerCol = [];
        /*
         add to the headerCol array the header string values for the choices
         */
        $(this).find('.actualHeader').each(function(){
            headerCol.push($(this).text());
        });
        /*
         add to the headerCol the header string values for the NA choice if present
         */
        $(this).find('.actualHeaderNA').each(function(){
            headerCol.push($(this).text());
        });
        /*
         reverse the array
         */
        headerCol = headerCol.reverse();
        // console.log(headerCol)

        /*
         for each block, plug in the corresponding choice string value in
         a screen-reader-only label and in the title attribute of the input
         */
        $(this).find('.choiceGroup').each(function(){
            $(this).find('span.blockChoice').each(function(n){
                $(this).prepend(headerCol[n]);
                $(this).siblings('input').attr('title', headerCol[n]);
            });
        });
    });
};

evalsys.instrumentSteppedItem = function(){
    $('label.blockItemLabel,label.blockItemLabelNA').click(function(){
        var answerCell = $(this).parents('.answerCell');
        $(answerCell).find('label').removeClass('blockItemLabelSelected').removeClass('blockItemLabelSelectedNA');
        $(answerCell).find('.itemDoneCheck').addClass('itemDoneCheckShow');

        if ($(this).hasClass('blockItemLabel')) {
            $(this).addClass('blockItemLabelSelected');
        }
        else {
            $(this).addClass('blockItemLabelSelectedNA');
        }
    });
    $('.blockItemLabel,.blockItemLabelNA').each(function(){
        if ($(this).children('input:checked').length == 1) {
            $(this).parents('.answerCell').find('.itemDoneCheck').addClass('itemDoneCheckShow');
            if ($(this).hasClass('blockItemLabel')) {
                $(this).addClass('blockItemLabelSelected');
            }
            else {
                $(this).addClass('blockItemLabelSelectedNA');
            }

        }
    });


};

evalsys.instrumentMCMAItem = function(){
    /*
     apply "checked" class to any element with a checked child
     */
    $('.mult-choice-ans li.check').each(function(){
        if ($(this).find('input').attr('checked') === true) {
            $(this).addClass('checked');
        }
    });
    /*
     apply "checked" class to any NA element with a checked child
     */
    $('.mult-choice-ans li.na').each(function(){
        if ($(this).find('input').attr('checked') === true) {
            $(this).addClass('checkedNA');
        }
        /*
         hide the NA element if no input children
         */
        if ($(this).find('input').length === 0) {
            $(this).hide();
        }
    });


    $('.mult-choice-ans input').click(function(e){
        var className;
        var parentLi = $(this).parents('li:eq(0)');
        var parentUl = $(this).parents('ul:eq(0)');
        if ($(parentLi).hasClass('check')) {
            className = 'checked';
        }
        else {
            className = 'checkedNA';
        }
        /*
         multiple choices
         */
        if ($(this).attr('type') == 'radio') {
            $(parentUl).find('li').removeClass('checked').removeClass('checkedNA');
            $(parentLi).addClass(className);
        }
        /*
         multiple answers
         */
        else {
            if ($(parentLi).hasClass('check')) {
                $(parentLi).toggleClass('checked');
            }
            else {
                $(parentLi).toggleClass('checkedNA');
            }
            /*
             an NA element, uncheck all non-NA if selected
             */
            if ($(parentLi).hasClass('checkedNA')) {
                $(this).parents('.mult-choice-ans').children('li.check').find('input[type!="hidden"]').attr('checked', false);
                $(this).parents('.mult-choice-ans').find('li.check').removeClass('checked');
            }
            /*
             an non-NA element, uncheck all NA if selected
             */
            if ($(parentLi).hasClass('check') | $(this).parents('li:eq(0)').hasClass('checked')) {
                $(this).parents('.mult-choice-ans').children('li.na').find('input[type!="hidden"]').attr('checked', false);
                $(this).parents('.mult-choice-ans').find('li.na').removeClass('checkedNA');
            }
            else {
            }

        }
        e.stopPropagation();
    });

};

evalsys.instrumentScaleItem = function(){
    $('.scaleItemLabel').click(function(){
        $(this).parents('.itemScalePanel').find('label').removeClass('scaleItemLabelSelected');
        $(this).parents('li').find('.itemDoneCheck').addClass('itemDoneCheckShow');
        $(this).addClass('scaleItemLabelSelected');
    });
    $('.scaleItemLabelNA').click(function(){
        $(this).parents('.itemScalePanel').find('label').removeClass('scaleItemLabelSelected');
        $(this).parents('li').find('.itemDoneCheck').addClass('itemDoneCheckShow');
        $(this).addClass('scaleItemLabelSelected');
    });

    $('.scaleItemLabel').each(function(){
        if ($(this).children('input:checked').length == 1) {
            $(this).parents('li').find('.itemDoneCheck').addClass('itemDoneCheckShow');
            $(this).addClass('scaleItemLabelSelected');
        }
    });

};

evalsys.instrumentDisplayHorizontal = function(){
    $('.fullDisplayHorizontalScale').each(function(){
        $(this).find('input:checked').parent('span').addClass('labelSelected');
    });
    $('.fullDisplayHorizontalScale').find('input').click(function(){
        $(this).parents('table').find('span').removeClass('labelSelected');
        $(this).parent('span').addClass('labelSelected');
    });

};

evalsys.instrumentMatrixItem = function() {
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
            titleWidth = groupWidth - legendWidth - 50;
            legend.attr("style", "width:" + legendWidth + "px;");
            title.attr("style", "width:" + titleWidth + "px;");
        });

        // Console for debugging.
        //console.debug("itemGroups", itemGroups, "groupWidth", groupWidth, "scaleWidth", scaleWidth, "legendWidth", legendWidth, "titleWidth", titleWidth);
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
};

evalsys.instrumentEssayText = function() {
    autosize(document.querySelectorAll('textarea'));
};

evalsys.instrumentItems = function($container) {
    if (typeof $container === "undefined" || $container === null) {
        // default it to the entire body
        $container = $("body");
    } else if (typeof $container === "string") {
        // it's a selector so make a jquery object
        $container = $($container);
    }
    if ($container.find('.blockItemGroup').length > 0){
        evalsys.instrumentBlockItem();
    }
    if ($container.find('.steppedItemGroup').length > 0){
        evalsys.instrumentSteppedItem();
    }
    if ($container.find('.mult-choice-ans').length > 0){
        evalsys.instrumentMCMAItem();
    }
    if ($container.find('.itemListEval').length > 0) {
        evalsys.instrumentScaleItem();
    }
    if ($container.find('.fullDisplayHorizontal').length > 0) {
        evalsys.instrumentDisplayHorizontal();
    }
    if ($container.find('.matrix').length > 0) {
        evalsys.instrumentMatrixItem();
    }
    if ($container.find('.item.text').length > 0){
        evalsys.instrumentEssayText();
    }
    var $evalComments = $container.find("div.JSevalComment");
    if ($evalComments.length > 0) {
        if (typeof jQuery().evalComment === "undefined") {
            alert("programming error: toggleCommentBox.js is not loaded!");
        }
        $evalComments.evalComment(); //Bind comment boxes toggle link action
    }
};


//PAGE inits
evalsys.initSummary = function() {
    // click to show/toggle arrows
    var $beEvaluatedBox = jQuery(".beEvaluated");
    var $inProgressTable = $beEvaluatedBox.find(".evaluated_in_progress");
    if ($inProgressTable.length > 0) {
        $beEvaluatedBox.find(".inProgressEvals").click(function(){
            var $this = jQuery(this);
            var $table = $inProgressTable;
            if ($table.is(":visible")) {
                // hide it
                $this.removeClass("triangle-open").addClass("triangle-closed");
                $table.slideUp();
            } else {
                // show it
                $this.removeClass("triangle-closed").addClass("triangle-open");
                $table.slideDown();
            }
        });
    }
    var $closedTable = $beEvaluatedBox.find(".evaluated_closed");
    if ($closedTable.length > 0) {
        $beEvaluatedBox.find(".closedEvals").click(function(){
            var $this = jQuery(this);
            var $table = $closedTable;
            if ($table.is(":visible")) {
                // hide it
                $this.removeClass("triangle-open").addClass("triangle-closed");
                $table.slideUp();
            } else {
                // show it
                $this.removeClass("triangle-closed").addClass("triangle-open");
                $table.slideDown();
            }
        });
    }
    if (typeof jQuery().tablesorter === "undefined") {
        alert("programming error: jquery tablesorter is not loaded!");
    }
    // http://tablesorter.com/docs/
    jQuery.tablesorter.addParser({ 
        id: 'humanDate', // set a unique id
        is: function(s) { return false; },
        format: function(str,table,cell) { // return a normalized version which is easily sorted
            return $(cell).attr("data-time");
        }, 
        type: 'numeric' 
    });
    jQuery("TABLE.beEvaluatedInProgressTable").tablesorter({
        headers: { 0:{sorter:'text'}, 1:{sorter:'humanDate'}, 2:{sorter:'humanDate'}, 3:{sorter: false}, 4:{sorter: false} },
        sortList: [[2,0]]
    });
    jQuery("TABLE.beEvaluatedClosedTable").tablesorter({
        headers: { 0:{sorter:'text'}, 1:{sorter:'humanDate'}, 2:{sorter:'humanDate'}, 3:{sorter: false}, 4:{sorter: false} },
        sortList: [[2,1]]
    });
};

evalsys.initControlScales = function() {
    evalsys.initFacebox({verticalCenterOnClick: true, minWidth: 740});
    jQuery('a.preview_scale').facebox();
    /* $(".preview_scale").click(function(event) {
        //event.preventDefault();
        var previewUrl = $(this).attr("href");
        alert("preview scale: "+previewUrl);
    }); */
};

evalsys.initModifyScales = function() {
    var $textboxes = $("div.labelindnt input:text");
    $textboxes.attr("maxlength", "250"); // force the existing ones first
    $textboxes.bind("click", function(event){
        // each time the text box is clicked on
        $(this).attr("maxlength", "250"); // force the input text boxes to 250 chars or less
    });

    var $previewScaleLink = jQuery('a.preview_scale');
    var originalUrl = $previewScaleLink.attr("href"); // store the url so we can append to it later

    // NOTE: the order here matters (the order of the click events firing must be the one below first and THEN the facebox one)
    $previewScaleLink.click(function(event) {
        // Get the form elements from scale_ideal_container and scale_points_container
        var $form = jQuery("form.scale_modify_form");
        var formData = $form.find(".scale_points_container INPUT,.scale_ideal_container INPUT").serializeArray();
        // now we extract the data from the array and make it into a query string to append on the URL
        var scaleData = "";
        for (var i = 0; i < formData.length; i++) {
            var element = formData[i];
            if (element["name"] == "modify-scale-points:1:list-control") {
                if (i > 0) { scaleData += "&"; }
                scaleData += "points=" + element["value"];
            } else if (element["name"] == "scaleIdealRadio-selection") {
                if (i > 0) { scaleData += "&"; }
                scaleData += "ideal=" + element["value"];
            }
        }
        var previewUrl = originalUrl+"?"+encodeURI(scaleData);
        $previewScaleLink.attr("href", previewUrl);
    });

    evalsys.initFacebox({verticalCenterOnClick: false, minWidth: 740});
    $previewScaleLink.facebox();
};

evalsys.initPreviewScales = function() {
    // NOTE: this essentially loads in a lightbox so be careful
    if (typeof jQuery.accordian !== "undefined") {
        alert("programming error: jquery ui accordian is not loaded!");
    }
    var screenWidth = $("body").innerWidth(); //$(document).width()
    jQuery("#items_container").accordion({
        autoHeight: false,
        clearStyle: true,
        change: function(event, ui) {
            // force the CSS min and max midth
            ui.newContent.css({
                "min-width": "200px",
                "max-width": (screenWidth-40)+"px"
            });
            $('#facebox').center({elementSelector: "table.faceboxtable", vertical: false});
        },
    });
    evalsys.instrumentItems("div.preview-item");
};

evalsys.initPreviewItem = function(selector) {
    // NOTE: this essentially loads in a lightbox so be careful
    evalsys.instrumentItems(selector);
};


//SPECIAL inits
/**
 * @param options an object with options for this facebox
 */
evalsys.initFacebox = function(options) {
    if (!evalsys.faceboxinitialized) {
        // only run the facebox init one time
        if (typeof jQuery.facebox !== "undefined") {
            // Use portal-provided styling; avoid image-based close button
            jQuery.facebox.settings.closeImage = null;
            //jQuery.facebox.settings.opacity = 0.1;
            //jQuery.facebox.settings.overlay = true;
            //jQuery.facebox.settings.faceboxHtml = "some html";
            // DOCS: https://github.com/defunkt/facebox
            if (options && options.verticalCenterOnClick) {
                jQuery(document).bind('beforeReveal.facebox', function() {
                    // set the vertical position
                    var posY = jQuery.facebox.mousePosY;
                    //var $clickedOn = jQuery.facebox.clicked;
                    var $facebox = $('#facebox');
                    $facebox.css({
                        'top': posY+'px'
                    });
                    // set the min-width
                    if (options && options.minWidth > 0) {
                        $facebox.css({
                            'min-width': (options.minWidth+26) +'px'
                        });
                        $facebox.find('.body').css({
                            'min-width': (options.minWidth) +'px'
                        });
                    }
                });
            }
            jQuery(document).bind('reveal.facebox', function() {
                // set the width
                var $facebox = $('#facebox');
                var faceboxWidth = $facebox.find('table.faceboxtable').width();
                if (options && options.minWidth > 0) {
                    faceboxWidth = faceboxWidth < options.minWidth ? options.minWidth : faceboxWidth;
                }
                //alert("before widths: .body="+$('#facebox .body').width()+", .content="+$('#facebox .content').width()+", .popup="+$('#facebox .popup').width()+", .table="+$('#facebox table.faceboxtable').width()+" ");
                $facebox.css({
                    'width': (faceboxWidth+26) +'px'
                });
                $facebox.find('.body').css({
                    'width': (faceboxWidth) +'px'
                });
            });
            evalsys.faceboxinitialized = true;
        } else {
            alert("Programming error: no facebox is available!");
        }
    }
};

// Utility function to select/deselect all checkboxes of a given form
evalsys.toggleCheckboxes = function( formName, checkToggle )
{
    var elements = document[formName].getElementsByTagName( "input" );
    for( var i = 0; i < elements.length; i++ )
    {
        if( elements[i].type === "checkbox" )
        {
            elements[i].checked = checkToggle;
        }
    }
};

// Hack function to toggle new/old report format
evalsys.toggleReportFormat = function( formName )
{
    var elements = document[formName].getElementsByTagName( "input" );
    for( var i = 0; i < elements.length; i++ )
    {
        if( elements[i].type === "hidden" && elements[i].name === "el-binding" && elements[i].value.indexOf( "{reportExporterBean.newReportStyle}" ) !== -1 )
        {
            var newValue = "";
            var oldValue = elements[i].value.substring( elements[i].value.indexOf( "}" ) + 1 );
            if( oldValue.toLowerCase() === "true" )
            {
                newValue = "false";
            }
            else
            {
                newValue = "true";
            }
            elements[i].value = elements[i].value.substring( 0, elements[i].value.indexOf( "}" ) + 1 ) + newValue;
        }
    }
};

// Eval Template Modify JS for Utilities
// @author lovemore.nalube@uct.ac.za

var evalTemplateUtils = (function() {

    //Configurable private variables
    var canDebug = false,
            canDebugLevels = "info,debug,warn,error", //Comma delimitated set of the debug levels to show. Select from info,debug,warn,error
            entityTemplateItemURL = "/direct/eval-templateitem/:ID:.xml",
            messgeBundlePath = "/direct/eval-resources/message-bundle.json",

    // Dont configure these vars
            pagesLoadedByFBwithJs = [],
            messageBundle = {},
            closedGroups = [];     // keep track of groups a user closes: EVALSYS-825
   
    function resizeFrame(updown, height) {
        try {
            var thisHeight = typeof height === "undefined" ? 280 : Number(height) + 40,
                frame = parent.document.getElementById(window.name),
                clientH = "";

            if (frame) {
                if (updown === -1) {
                    clientH = document.body.clientHeight;
                }
                else {
                    clientH = document.body.clientHeight + thisHeight; //increasing the height
                }
                $(frame).height(clientH);
            }
        } catch(e) {
            evalTemplateUtils.debug.error("Frame resize did not work. Error: %o", e, e);
        }
    }

    // Format bundle path for user locale
    var loadMessageBundle = function(){
        $.ajax({
            url: messgeBundlePath,
            global: false,
            cache: true,
            dataType : "json",
            success: function(messageBundleJSON){
                messageBundle = messageBundleJSON.data;
            }
        });
    };

    //public data
    return {
        entity:{
            getTemplateItemURL: function(id) {
                return entityTemplateItemURL.replace(":ID:", id);
            }
        },
        //keep frame size before grow
        frameSize: 0,
        frameGrow: function(height) {
            resizeFrame(1, height);
        },
        frameShrink: function(height) {
            resizeFrame(-1, height);
        },
        debug: {
            info : function() {
            },
            debug : function() {
            },
            warn : function() {
            },
            error : function() {
            },
            group : function() {
            },
            groupEnd : function() {
            },
            time : function() {
            },
            timeEnd : function() {
            }
        },
        vars: {
            groupableItems : [],
            isIE : false
        },
        init: function() {
            // Debugging. Acts on Firebug console methods described at http://getfirebug.com/console.html
            if (canDebug && typeof console !== "undefined") {
                if (typeof console.info !== "undefined" && canDebugLevels.search(/info/i) !== -1) {
                    evalTemplateUtils.debug.info = console.info;
                }
                if (typeof console.debug !== "undefined" && canDebugLevels.search(/debug/i) !== -1) {
                    evalTemplateUtils.debug.debug = console.debug;
                }
                if (typeof console.warn !== "undefined" && canDebugLevels.search(/warn/i) !== -1) {
                    evalTemplateUtils.debug.warn = console.warn;
                }
                if (typeof console.error !== "undefined" && canDebugLevels.search(/error/i) !== -1) {
                    evalTemplateUtils.debug.error = console.error;
                }
                if (typeof console.group !== "undefined") {
                    evalTemplateUtils.debug.group = console.group;
                }
                if (typeof console.groupEnd !== "undefined") {
                    evalTemplateUtils.debug.groupEnd = console.groupEnd;
                }
                if (typeof console.time !== "undefined") {
                    evalTemplateUtils.debug.time = console.time;
                }
                if (typeof console.timeEnd !== "undefined") {
                    evalTemplateUtils.debug.timeEnd = console.timeEnd;
                }
            }
            //browser check
            evalTemplateUtils.vars.isIE = $.browser.msie;

            // Message Locale bundle loader
            loadMessageBundle();

        },
        pages: {
                modify_item_page: "modify_item",
                modify_template_page: "modify_template",
                modify_block_page: "modify_block",
                choose_existing_page: "choose_existing_items",
                eb_save_order: "/direct/eval-templateitem/template-items-reorder",
                eb_block_edit: "/direct/eval-templateitem/modify-block-items"
            },
        getPageType: function(url){
            evalTemplateUtils.debug.group("Getting the page type/name");
            var pageType = undefined,
                    i = 0,
                    regExp = null;
            pagesLoadedByFBwithJs = [ evalTemplateUtils.pages.modify_item_page,
                evalTemplateUtils.pages.modify_template_page, evalTemplateUtils.pages.modify_block_page,
                evalTemplateUtils.pages.choose_existing_page ];
            evalTemplateUtils.debug.debug("Pages supported are %s", pagesLoadedByFBwithJs.toString());
            for ( i in pagesLoadedByFBwithJs){
                if ( typeof pageType == "undefined" ){
                    regExp = new RegExp(pagesLoadedByFBwithJs[i]);
                    evalTemplateUtils.debug.debug("Checking url against page filter %s and regExp %o", pagesLoadedByFBwithJs[i], regExp);
                    pageType =  ( url.search(regExp) !== -1 ) ? pagesLoadedByFBwithJs[i] : undefined ;
                    evalTemplateUtils.debug.debug("Page type is %s", pageType);
                }
            }
            evalTemplateUtils.debug.info("Page type found as: %s", pageType);
            evalTemplateUtils.debug.groupEnd();
            return pageType;
        },
        //retrieve the message strings for key
        messageLocator: function(key, params){
            return fluid.messageLocator( messageBundle )([key], params);            
        },

        // Remember the groups a user closes: EVALSYS-825
        closedGroup : {
            add : function(groupId){
                if( groupId ){
                    if ( $.inArray(groupId, closedGroups) === -1){
                        closedGroups.push(groupId);
                        evalTemplateUtils.debug.info("Closed %s", groupId);
                    }
                }
                evalTemplateUtils.debug.warn("closedGroups %o", closedGroups);
            },
            remove : function(groupId){
                if( groupId && $.inArray(groupId, closedGroups) > -1){
                    var index;
                    for ( var i in closedGroups){
                        if ( closedGroups[i] === groupId ){
                            index = i;
                        }
                    }
                    closedGroups.splice(index, 1);
                    evalTemplateUtils.debug.info("Opened %s", groupId);
                }
                evalTemplateUtils.debug.warn("closedGroups %o", closedGroups);
            },
            get : closedGroups            
        }

        };


})($);

//init evalUtils
evalTemplateUtils.init();
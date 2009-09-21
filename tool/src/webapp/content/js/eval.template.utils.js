// Eval Template Modify JS for Utilities
// @author lovemore.nalube@uct.ac.za

var evalTemplateUtils = (function() {

    //Configurable private variables
    var canDebug = false,
            canDebugLevels = "info,debug,warn,error", //Comma delimitated set of the debug levels to show. Select from info,debug,warn,error
            entityTemplateItemURL = "/direct/eval-templateitem/:ID:.xml",
            messgeBundlePathDir = "/sakai-evaluation-tool/content/bundle/",

    // Dont configure these vars
            pagesLoadedByFBwithJs = [],
            messageBundle = {};
   
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
        var bundleExtension = ".properties",
        bundleName = "messages",
        path,
        lang = navigator.language /* Mozilla */ || navigator.userLanguage /* IE */;

        /** Ensure language code is in the format aa_AA. */
        lang = lang.toLowerCase();
        if(lang.length > 3) {
            lang = "_" + lang.substring(0, 3) + lang.substring(3).toUpperCase();
        }

        path = messgeBundlePathDir + bundleName + lang + bundleExtension;

        //Attempt to load this locale's aa_AA bundle if fail then resort to aa, then messages bundle
        // Try actual locale:
        $.ajax({
                url: path,
                global: false,
                success: function(messageBundleRaw, status){
                    messageBundle = fluid.parseJavaProperties(messageBundleRaw);
                },
                error: function(){

                    //try nearest bundle
                    path = messgeBundlePathDir + bundleName + lang.substring(0, 3) + bundleExtension;
                    $.ajax({
                        url: path,
                        global: false,
                        success: function(messageBundleRaw){
                            messageBundle = fluid.parseJavaProperties(messageBundleRaw);
                        },
                        error: function(){

                            //get default bundle
                            path = messgeBundlePathDir + bundleName + bundleExtension;
                            $.ajax({
                                url: path,
                                global: false,
                                success: function(messageBundleRaw){
                                    messageBundle = fluid.parseJavaProperties(messageBundleRaw);                                    
                                },
                                error: function(){
                                    evalTemplateUtils.debug.error("Can not load bundle file.");
                                    return false;
                                }

                            });
                            
                        }

                    });
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
            /*if(messageBundle === null){
                //try get bundle & wait for it to load. waiting is 1 second.
                loadMessageBundle();
                evalTemplateUtils.sleep(1000);
                if(messageBundle === null){
                    //wait a little longer
                    evalTemplateUtils.sleep(1000);
                }
            } */
            return fluid.messageLocator( messageBundle )([key], params);            
        }
        // Sleeper function
        /*sleep : function(numberMillis){
             var now = new Date(),
             exitTime = now.getTime() + numberMillis;
             while (true){
                now = new Date();
                if (now.getTime() > exitTime) {return;};
             }
            }*/
        };


})($);

//init evalUtils
evalTemplateUtils.init();
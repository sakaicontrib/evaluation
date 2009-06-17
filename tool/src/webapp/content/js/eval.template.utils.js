// Eval Template Modify JS for Utilities
// @author lovemore.nalube@uct.ac.za

var evalTemplateUtils = (function() {
    //private data

    //Configurable variables
    var canDebug = true,
            canDebugLevels = "info,debug,warn,error",
            entityTemplateItemURL = "/direct/eval-templateitem/:ID:.xml" ;

    //public data
    return {
        entity:{
            getTemplateItemURL: function(id) {
                return entityTemplateItemURL.replace(":ID:", id);
            }
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
        }
    };


})($);

//init evalUtils
evalTemplateUtils.init();
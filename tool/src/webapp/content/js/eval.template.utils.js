// Eval Template Modify JS for Utilities
// @author lovemore.nalube@uct.ac.za

var evalTemplateUtils = (function() {

    //Configurable variables
    var canDebug = true,
            canDebugLevels = "info,debug,warn,error", //Comma delimitated set of the debug levels to show. Select from info,debug,warn,error
            entityTemplateItemURL = "/direct/eval-templateitem/:ID:.xml" ;
    //private data

    function resizeFrame(updown, height) {
        var _height = typeof height == "undefined" ? 280 : Number(height) + 40,
                frame = parent.document.getElementById(window.name),
                clientH = "";
        try {
            if (frame) {
                if (updown === -1) {
                    clientH = document.body.clientHeight - _height;
                }
                else {
                    clientH = document.body.clientHeight + _height;
                }
                $(frame).height(clientH);
            }
        } catch(e) {
            evalTemplateUtils.debug.error("Frame resize did not work. Error: %o", e, e);
        }
    }

    //public data
    return {
        entity:{
            getTemplateItemURL: function(id) {
                return entityTemplateItemURL.replace(":ID:", id);
            }
        },
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
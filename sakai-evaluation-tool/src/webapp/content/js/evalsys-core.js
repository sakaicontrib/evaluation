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

evalsys.getHostFrame = function() {
    if (window === window.parent) {
        return null;
    }
    try {
        var frameEl = window.frameElement;
        if (frameEl && frameEl.tagName && frameEl.tagName.toLowerCase() === "iframe") {
            return frameEl;
        }
    } catch (e) {
        return null;
    }
    return null;
};

evalsys.resizeEmbeddedFrame = function(options) {
    var frame = evalsys.getHostFrame();
    if (!frame) {
        return false;
    }
    var extra = 0;
    var minHeight = 0;
    if (options && typeof options.extra === "number" && !isNaN(options.extra)) {
        extra = options.extra;
    }
    if (options && typeof options.minHeight === "number" && !isNaN(options.minHeight)) {
        minHeight = options.minHeight;
    }
    var body = document.body || { scrollHeight: 0, offsetHeight: 0, clientHeight: 0 };
    var docEl = document.documentElement || { scrollHeight: 0, offsetHeight: 0, clientHeight: 0 };
    var target = Math.max(
        minHeight,
        body.scrollHeight,
        body.offsetHeight,
        body.clientHeight,
        docEl.scrollHeight,
        docEl.offsetHeight,
        docEl.clientHeight
    );
    target = Math.max(0, target + extra);
    frame.style.height = target + "px";
    return true;
};

evalsys.runWhenReady = function(callback) {
    if (typeof callback !== "function") {
        return;
    }
    var execute = function() {
        callback();
    };
    if (document.readyState === "loading") {
        if (document.addEventListener) {
            var handler = function() {
                document.removeEventListener("DOMContentLoaded", handler);
                execute();
            };
            document.addEventListener("DOMContentLoaded", handler);
        } else if (document.attachEvent) {
            var ieHandler = function() {
                if (document.readyState === "complete") {
                    document.detachEvent("onreadystatechange", ieHandler);
                    execute();
                }
            };
            document.attachEvent("onreadystatechange", ieHandler);
        } else {
            window.setTimeout(execute, 0);
        }
    } else {
        execute();
    }
};

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



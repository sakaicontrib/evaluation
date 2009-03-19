/**
 * For the takeEval and preview views
 */
$(document).ready(function() {
    $('div[@rel=evalInstructorSelector]').evalSelector({type:0});
    $('div[@rel=evalTaSelector]').evalSelector({type:1});
});

(function($) {
    $.fn.evalSelector = function(opts) {
        var options = $.extend({}, $.fn.evalSelector.defaults, opts);
        init($(this), options);
    };
    /**
     * Public variables. Once init() has ran, do not reference directly to these, use variables.options instead.
     */
    $.fn.evalSelector.defaults = {
        css:{
            activeCheckbox: {background:'#eee'}
        },
        type: 1, //Type is for type of category we are handling. ie: 0 = instructor, 1 = ta
        debug: false,
        fields: ['input', 'select', 'textarea'] //Array of fields in the form
    }
    /**
     * Private methods and variables
     *
     * @param that Element to be attached to
     */

    var variables = {
        typeOfSelector:{
            one: false,
            multiple: false
        },
        questionsToShow: new Array(),
        questionsToHide: new Array(),
        get:{
            typeOfBranch:function() {
                log("Active code type is: " + variables.options.type);
                switch (variables.options.type) {
                    case 0: return "instructor";break;
                    case 1: return "ta";break;
                }
            } ,
            shownQuestions: function() {
                log("Getting questions for type: " + variables.get.typeOfBranch());
                var str = 'div.' + variables.get.typeOfBranch() + 'Branch:visible';
                var temp = new Array();
                $.each($(str).get(), function(i, item) {
                    temp.push($(item).attr("name"));
                    log("Search found: " + $(item).attr("name"));
                });
                log("Search found total: " + temp.length);
                return temp;
            },

            selectedBoxesArray:function(that) {
                var checked = new Array();
                that.find('input[@type=checkbox]').each(function() {
                    if (this.checked)checked.push(this);
                });
                log("Found " + checked.length + " checked boxes");
                return checked;
            } ,
            selectedBoxesBool:function(that) {
                return variables.get.selectedBoxesArray(that).length > 0
            }
        },
        set:{
            typeOfBranch:function(that) {
                var temp;
                if (that.attr('name').search(/instructor/i) != -1)
                    temp = 0;
                else if (that.attr('name').search(/tas/i) != -1)
                    temp = 1;
                variables.options.type = temp;
                log("Active type is: " + variables.get.typeOfBranch());

            }
        },
        options:null,
        foundArray: function() {
            var temp = new Array();
            $.each(variables.questionsToShow, function(s, shown) {
                log("Checking if " + shown + " is already showing in dom. Dom has:" + variables.get.shownQuestions().length + " visible.");
                $.each(variables.get.shownQuestions(), function(h, hidden) {
                    if (shown == hidden) {
                        temp.push(shown);
                        log("Found visible field: " + shown);
                    }
                });
            });
            return temp;
        },
        foundBool: function() {
            var temp = variables.foundArray();
            log("Found " + temp.length + " with IDs: " + temp.toString());
            return (temp.length > 0)
        },
        foundSplice:function(item) {
            if (variables.foundBool()) {
                $.each(variables.questionsToShow, function(s, show) {
                    if (show == item) {
                        variables.questionsToShow.splice(parseInt(s), 1);
                        log("Spliced: " + show + " from selected list.");
                        return true;
                    }
                });
            }
            return false;
        },
        isVisible:function(item) {
            var visible = false;
            if (variables.foundBool) {
                $.each(variables.get.shownQuestions(), function(i, found) {
                    if (item == found) {
                        visible = true;
                    }
                });
            }
            return visible;
        }
    }

    function init(that, options) {
        //copy options to this class
        variables.options = options;
        return that.each(function() {
            if ($(this).find('select').length > 0) {
                variables.typeOfSelector.one = true;
                log("Found a selector for: " + variables.get.typeOfBranch());
            } else {
                variables.typeOfSelector.multiple = true;
                log("Found a checkbox list for: " + variables.get.typeOfBranch());
            }
            initControls($(this));
            initClassVars();
        });
    }

    function initClassVars() {
        variables.questionsToHide = new Array();
        variables.questionsToShow = new Array();
    }

    function initControls(that) {
        if (variables.typeOfSelector.multiple) {
            //Working with checkboxes
            that.find('input[type=checkbox]').each(function() {
                var elem = $(this);
                var elemId = elem.attr('id');
                var elemLabel = elem.parent().find('label').text();
                log("Attaching event listener for: " + elemLabel + " with id: " + elemId);
                elem.bind('click', function() {
                    variables.set.typeOfBranch($(this));
                    if (this.checked) {
                        //Fn for Check
                        elem.parent().css('background', variables.options.css.activeCheckbox.background);
                        log("SELECTED: " + elemLabel + " with id: " + elemId);
                        if (!variables.isVisible(elemId)) {
                            variables.questionsToShow.push(elemId);
                            log("ADDED: " + elemLabel + " with id: " + elemId);
                            showQuestions();
                            initClassVars();
                        } else {
                            log("Person: " + elemLabel + " with id: " + elemId + " is already visible.");
                        }
                    }
                    else {
                        this.checked = true;
                        if (variables.isVisible(elemId) && confirmSelection(elemLabel)) {
                            elem.parent().css('background', '');
                            log("DESELECTED: " + elemLabel + " with id: " + elemId);
                            variables.questionsToHide.push(elemId);
                            variables.foundSplice(elemId);
                            log("DELETED: " + elemLabel + " with id: " + elemId);
                            showQuestions();
                            initClassVars();
                            this.checked = false;
                        }
                    }
                });
            });
        }

        that.find('input[@type=button]').bind('click', function() {
            var elem = that.find('select:eq(0)');
            variables.set.typeOfBranch(elem);
            if (variables.typeOfSelector.one) {
                //Working with dropdown
                var elemId = elem.val();
                var render = false;
                log("Selected guy with id: " + elemId);
                if (variables.get.shownQuestions().length == 0) {
                    render = true;
                }
                else if (variables.isVisible(elemId)) {
                    log("Person with id: " + elemId + " is already visible.");
                    return false;
                } else
                {
                    var temp = 'div[name=' + variables.get.shownQuestions()[0] + ']';
                    render = confirmSelection($(temp).find('legend').attr('title'));
                }
                if (render) {
                    variables.questionsToHide = variables.get.shownQuestions();
                    variables.questionsToShow = new Array();
                    variables.questionsToShow.push(elemId);
                    log("Added item " + elemId + " to array. Now Array has this number of elements: " + variables.questionsToShow.length);
                    showQuestions();
                    initClassVars();
                }
            }
        });
    }

    function showQuestions() {
        hideQuestions();
        log("Showing " + variables.questionsToShow.length + " items in dom.");
        $.each(variables.questionsToShow, function(i, item) {
            var tempFound = 0;
            $.each(variables.get.shownQuestions(), function(s, toSkip) {
                if (item == toSkip) {
                    tempFound++;
                }
            });
            if (tempFound == 0) {
                var str = 'div[name=' + item + ']';
                $(str).slideDown('normal', function() {
                    log("Revealing: " + item)
                });
            }
        });
    }

    function hideQuestions(all) {
        var temp = new Array();
        temp = (all && all == 1) ? variables.get.shownQuestions() : variables.questionsToHide;
        $.each(temp, function(i, item) {
            var str = 'div[@name=' + item + ']';
            $(str).slideUp('normal', function() {
                log("WARN: Hiding " + item + " in dom.");
                clearFieldsFor($(this));
            });
        });
    }

    function confirmSelection(name) {
        return confirm($('div#evalSelectWarn').text().replace(/XXX/, name));
    }

    function clearFieldsFor(item) {
        $.each(variables.options.fields, function(f, field) {
            log("Clearing all " + field.toLowerCase() + " fields.");
            item.find(field.toLowerCase()).each(function() {
                var t = this.type, tag = this.tagName.toLowerCase();
                log("Found type: " + t);
                log("Found tag: " + tag);
                if (t == 'text' || t == 'password' || tag == 'textarea')
                    this.value = '';
                else if (t == 'checkbox' || t == 'radio')
                    this.checked = false;
                else if (tag == 'select')
                        this.selectedIndex = -1;
            });
        });
    }

    // Debugging
    function log($obj) {
        if (variables.options.debug) {
            if (window.console && window.console.log) {
                window.console.log('INFO: ' + $obj);
            }
        }
    }
})($)
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
        debug: true
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
            shownQuestions: function() {
                log("Getting questions for type: " + variables.get.typeOfBranch());
                var str = '"div.' + variables.get.typeOfBranch() + 'Branch:visible"';
                return $(str).get();
            },
            typeOfBranch:function() {
                switch (variables.options.type) {
                    case 0: return "instructor";break;
                    case 1: return "ta";break;
                }
            } ,
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
        options:'',
        foundArray: function() {
            var f = new Array();
            $.each(variables.questionsToShow, function(s, show) {
                log("Checking if "+show+" is already showing in dom.");
                $.each(variables.questionsToHide, function(h, hide) {
                    if (show == hide) {
                        f.push(show);
                        log("Found visible field : " + show);
                    }
                });            
            });
            return f;
        },
        foundBool: function() {
            var temp = variables.foundArray();
            log("Found "+temp.length+" with IDs: "+ temp.toString());
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
        });
    }

    function initControls(that) {
        var elemId;
        if (variables.typeOfSelector.one) {
            //Working with dropdown
           that.find('select').change(function(){
               var elem = $(this)
               elemId =  elem.val();
               log("Selected guy with id: "+elemId);
               variables.questionsToShow = new Array();
               variables.questionsToShow.push(elemId)
               log("Added item "+elemId+" to array. Now Array has this number of elements: "+variables.questionsToShow.length);
           });
        }
        if (variables.typeOfSelector.multiple) {
            //Working with checkboxes
            that.find('input[@type=checkbox]').each(function() {
                var elem = $(this);
                elemId = elem.attr('id');
                log("Attaching event listener for: " + elem.parent().find('label').text() + " with id: " + elemId);
                elem.bind('click', function() {
                    if (this.checked) {
                        //Fn for Check
                        variables.questionsToShow.push(elemId);
                        elem.parent().css('background', variables.options.css.activeCheckbox.background);
                        log("SELECTED: " + elem.parent().find('label').text() + " with id: " + elemId);
                    }
                    else
                    {   //Fn for Uncheck
                        variables.questionsToHide.push(elemId);
                        variables.foundSplice(elemId);
                        elem.parent().css('background', '');
                        log("DESELECTED: " + elem.parent().find('label').text() + " with id: " + elemId);

                    }
                });
            });
        }
        
        that.find('input[@type=button]:eq(0)').bind('click', function() {
            if (variables.typeOfSelector.one) {
                if (variables.foundBool) {
                        render = confirmSelection();
                    } else {
                        render = true;
                    }
                    if (render) {
                        log("Showing " + variables.questionsToShow.length + " item in dom.");
                            showQuestions();

                        
                    }
            }
            if (variables.typeOfSelector.multiple) {
                if (variables.get.selectedBoxesBool(that)) {
                    var render = false;
                    if (variables.foundBool) {
                        render = confirmSelection();
                    } else {
                        render = true;
                    }
                    if (render) {
                        log("Showing " + variables.questionsToShow.length + " items in dom.");
                        $.each(variables.questionsToShow, function(i, item) {
                            showQuestions();
                        });
                    }
                    else {
                        log('No selection made');
                    }
                }
            }
        });
    }

    function showQuestions() {
        hideQuestions();
        $.each(variables.questionsToShow, function(i, item) {
            var str = 'div[name=' + item + ']';
            $(str).slideDown('normal', function() {
                log("Revealing: "+item)
            });
        });
    }
    function hideQuestions() {
        $.each(variables.questionsToHide, function(i, item) {
            var str = '"div[@name=' + item + ']"';
            $(str).slideUp('normal', function() {
                log(item + " HIDDEN")
            });
        });
    }

    function confirmSelection() {
        log("Found is: "+variables.foundBool());
        if (variables.foundBool()) {
            log("Found "+variables.foundArray().length+" with IDs: "+ variables.foundArray().toString());
            if (confirm("Are you sure :)")) {
                return true;
            }else{
                return false;
            }
        }
        return true;
    }

    // Debugging
    //
    function log($obj) {
        if (variables.options.debug) {
            if (window.console && window.console.log) {
                window.console.log('INFO: ' + $obj);
            }
            else {
                alert($obj);
            }
        }
    }


})(jQuery)
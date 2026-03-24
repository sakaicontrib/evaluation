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
/**
 * Sakai Evaluation System project
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009, 2010, 2011, 2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * - Aaron Zeckoski (azeckoski)
 **********************************************************************************/

// For the evalAssign view
$(document).ready(function() {
    $('a[rel=assignInstructorSelector]').assignSelector({type:0});
    $('a[rel=assignTaSelector]').assignSelector({type:1});
    $(':submit').bind('click', function() {
        //Validate group Selections
        var countChecked = $('form[id=eval-assign-form] input[type=checkbox]:checked').get().length;
        if (countChecked == null || countChecked == 0) {
            $('#error').fadeIn(0);
            return false;
        } else {
            $('#error').fadeOut(0);
        }
        return true;
    });
    //re-select previously selected groups
    $('tr.selectedGroup').find('input[type=checkbox]').each(function(){
    	this.checked = true;
        $(this).parents('tr.selectedGroup').find('a[rel=assignInstructorSelector]').fadeIn('fast');
        $(this).parents('tr.selectedGroup').find('a[rel=assignTaSelector]').fadeIn('fast');
    });
});

(function($) {
    $.fn.assignSelector = function(opts) {
        var options = $.extend({}, $.fn.assignSelector.defaults, opts);
        init($(this), options);
    };
    /**
     * Public variables. Once init() has ran, do not reference directly to these, use variables.options instead.
     */
    $.fn.assignSelector.defaults = {
        type: 1, //Type is for type of category we are handling. ie: 0 = instructor, 1 = ta
        debug: false
    };
    /**
     * Private methods and variables
     *
     */

    var variables = {
        option:null,
        get:{
            typeOfBranch:function() {
                switch (variables.options.type) {
                    case 0: return "instructor";break;
                    case 1: return "ta";break;
                }
            },
            siteId:false,
            inlinePanel:null
        },
        set:{
            typeOfBranch:function(that) {
                var temp;
                if (that.attr('rel').search(/instructor/i) != -1)
                    temp = 0;
                else if (that.attr('rel').search(/tas/i) != -1)
                    temp = 1;
                variables.options.type = temp;
                log("Active catergory type is: " + variables.get.typeOfBranch());
            },
            siteId:function(that) {
                variables.get.siteId = that.parents('tr:eq(0)').find('input:eq(0)').val();
                return !variables.get.siteId ? false : true;  //Do not simplify this. Should return true if siteId is anything other than false.
            }
        },
        selectedPeople:0,
        deselectedPeopleIds: new Array(),
        that:null,
        deselectedLog:new Array(),
        thatRowNumber:0 ,
        evalGroupId:null,
        groupCheckBox:null
    };

    function init(that, options) {
        //copy options to this class
        variables.options = options;
        return that.each(function() {
            initControls($(this));
            initClassVars();
        });
    }

    function openInlinePanel(that) {
        var row = that.parents('tr:eq(0)');
        var container = row.find('.assign-select-inline');
        if (!container.length) {
            return false;
        }
        $('.assign-select-inline').not(container).empty().hide();
        container.show();
        variables.that = that;
        variables.groupCheckBox = row.find('input[type=checkbox]');
        variables.set.typeOfBranch(that);
        variables.evalGroupId = (variables.groupCheckBox.val() || '').replace('/site/', '');
        var url = that.attr('href');
        if (!url) {
            return false;
        }
        if (container.data('loaded-url') === url && container.children().length) {
            initInlinePanel(container);
            return false;
        }
        container.html('<div class="instructionText">Loading...</div>');
        $.get(url, function(data) {
            var $wrap = $('<div>').html(data);
            var $fragment = $wrap.find('#assign-select-fragment');
            if ($fragment.length === 0) {
                $fragment = $wrap;
            }
            container.empty().append($fragment);
            container.data('loaded-url', url);
            initInlinePanel(container);
        });
        return false;
    }

    function closeInlinePanel() {
        if (variables.get.inlinePanel) {
            variables.get.inlinePanel.hide();
        }
    }

    function initInlinePanel(container) {
        log("Inline selector loaded, attaching listeners now...");
        variables.get.inlinePanel = container;
        // deselect boxes already deselected
        var field, regText = variables.evalGroupId + ".deselected" + (variables.options.type == 0 ? "Instructors" : "Assistants"),
        sRegExInput = new RegExp(regText);
        $('input[name=el-binding]').each(function() {
            if ($(this).val().search(sRegExInput) != -1) {
                field = $(this);
            }
        });
        if (field && field.val) {
            var deselected = field.val().replace("j#{selectedEvaluationUsersLocator." + regText + "}[", "").replace(/"/g,"").replace(/ /g,"").replace("]", "").split(",");
            if (deselected.length > 0) {
                container.find('input[type=checkbox]:checked').each(function() {
                    for (var i = 0; i < deselected.length; i++) {
                        if ( deselected[i] == $(this).attr('id')) {
                            this.checked = false;
                        }
                    }
                });
                handleCheckboxes(container.find('.selectTable tbody input[type=checkbox]').not(':checked'), 1);
            }
        }

        // check proper radiobox depending on selection option already saved in the DOM
        regText = variables.evalGroupId + (variables.options.type === 0 ? ".instructor" : ".assistant"),
        sRegExInput = new RegExp(regText);
        field = null;
        $('input[name=el-binding]').each(function() {
            if ($(this).val().search(sRegExInput) != -1) {
                field = $(this);
            }
        });
        if (field && field.val) {
            var selectionSaved = field.val().replace("j#{assignGroupSelectionSettings." + regText + "}", "");
            log("selectionSaved value: " + selectionSaved);
            if (selectionSaved.toString().length > 0) {
                container.find('input[type=radio]').each(function() {
                    if ( selectionSaved === $(this).val()) {
                        this.checked = true;
                    }
                });
            }
        }

        // set initial state of select all box
        if (container.find('.selectTable tbody input[type=checkbox]').not(':checked').length === 0) {
            container.find('.selectorControl').each(function() {
                this.checked = true;
            });
        }
        // Activate Mass (De)Selector controls
        container.find('.selectorControl').off('click').on('click', function() {
            if (this.checked) {
                container.find('.selectTable tbody input[type=checkbox]').not(':checked').each(function() {
                    this.checked = true;
                });
            } else {
                container.find('.selectTable tbody input[type=checkbox]:checked').each(function() {
                    this.checked = false;
                });
            }
        });
        // bind individual checkbox to toggle main selector
        container.find(".selectTable tbody input[type=checkbox]").off('click').on('click', function() {
            if (this.checked) {
                if (container.find('.selectTable tbody input[type=checkbox]').not(':checked').length === 0) {
                    container.find('.selectorControl').each(function() {
                        this.checked = true;
                    });
                }
            } else {
                container.find('.selectorControl').each(function() {
                    this.checked = false;
                });
            }
        });

        container.find('.assign-select-save').off('click').on('click', function() {
            log("Binding submit button value");
            handleFormSubmit(this);
            return false;
        });
        container.find('.assign-select-cancel').off('click').on('click', function() {
            closeInlinePanel();
            return false;
        });
        // Make list scrollable if height is more than 200px
        var tableHolder = container.find('.selectTable:eq(0)');
        tableHolder.css({
            'overflow': 'auto',
            'height': tableHolder.height() > 200 ? '205px' : (tableHolder.height() + 10) + "px"
        });
        log("Formatting table holder height. Set height to:" + tableHolder.height());
    }

    function initClassVars() {
        variables.selectedPeople = 0;
        variables.deselectedPeopleIds = new Array();
        variables.thatRowNumber = 0;
    }

    function initControls(that) {
        that.hide();
        that.on('click', function() {
            variables.thatRowNumber = that.parents('tr').attr('rel');
            log("Fetching URL: " + that.attr('href'));
            openInlinePanel(that);
            return false;
        });
        that.parents('tr').find('input[type=checkbox]').on('click', function() {
            if (this.checked) {
                that.fadeIn('fast');
            } else {
                that.fadeOut('fast');
                that.parents('tr').find('.assign-select-inline').empty().hide();
            }
        });

    }

    function handleCheckboxes(unChecked, where) {
        if (unChecked.length != 0) {
            if(where==2){//deselct everyone
                variables.deselectedPeopleIds = new Array();
            }else{
            unChecked.each(function() {
                var id = $(this).attr('id');
                variables.deselectedPeopleIds.push(id);
            });
            }
            var field;
            var regText = variables.evalGroupId + ".deselected" + (variables.options.type == 0 ? "Instructors" : "Assistants");
            var sRegExInput = new RegExp(regText);
            $('input[name=el-binding]').each(function() {
                if ($(this).val().search(sRegExInput) != -1) {
                    field = $(this);
                }
            });
            if (field != null) {
                field.val("j#{selectedEvaluationUsersLocator." + regText + "}[" + variables.deselectedPeopleIds.toString() + "]");
                log('Found - ' + unChecked.length + ' - deselected people and setting form value now. New val is:' + field.val());
                variables.deselectedPeopleIds = new Array();
                return true;
            } else {
                log("ERROR: Field param with part val:" + regText + " Not FOUND.");
            }

        }
        else {
            if (where == 1) {
                return false;
            }
            closeInlinePanel();
        }
        return false;
    }

    function handleRadioButton(selection){
         if (variables.evalGroupId !== null){
            var field, regText = variables.evalGroupId + (variables.options.type === 0 ? ".instructor" : ".assistant"),
            sRegExInput = new RegExp(regText);
            $('input[name=el-binding]').each(function() {
                if ($(this).val().search(sRegExInput) != -1) {
                    field = $(this);
                }
            });
            if (field !== null) {
                field.val("j#{assignGroupSelectionSettings." + regText + "}" + selection );
                log('Selection setting is: ' + selection);
                return true;
            } else {
                log("ERROR: Field param with part val:" + regText + " Not FOUND.");
            }
         }
    }

    function handleFormSubmit(_that) {
        log("Running pre-SET checks");
        var that = $(_that), temp = variables.get.inlinePanel.find('.selectTable tbody input[type=checkbox]').not(':checked'),
        tempChecked = variables.get.inlinePanel.find('.selectTable tbody input[type=checkbox]:checked'),
        selectionChosen = variables.get.inlinePanel.find('input[type=radio]:checked');
        variables.selectedPeople = tempChecked.length > 0 ? tempChecked.length : 0;
        if(temp.length>0){
            if (handleCheckboxes(temp, 0)) {
                //reset dom count on selected Instr/Ass
                var origionalSelected = variables.that.attr('class').replace("addItem total:", "");  //gets a String
                var text = variables.that.attr('title') + " (" + variables.selectedPeople + "/" + origionalSelected + ")";
                variables.that.text(text);
                closeInlinePanel();
            }
        }else{
              if (handleCheckboxes(tempChecked, 2)) {
                //reset dom count on selected Instr/Ass
                var origionalSelected2 = variables.that.attr('class').replace("addItem total:", "");  //gets a String
                var text2 = variables.that.attr('title') + " (" + variables.selectedPeople + "/" + origionalSelected2 + ")";
                variables.that.text(text2);
                closeInlinePanel();
            }
        }
        //save selection setting to DOM
        if (selectionChosen.length > 0){
            handleRadioButton(selectionChosen.val());
            handleListOrdering(tempChecked);
        }
        initClassVars();
        return true;

    }

    var handleListOrdering = function(selected){
        log("Handling ordering");
        var regText = variables.evalGroupId + ".ordering" + (variables.options.type === 0 ? "Instructors" : "Assistants"),
        sRegExInput = new RegExp(regText),
        selectedOrderedUserIds = [];
            $('input[name=el-binding]').each(function() {
                if ($(this).val().search(sRegExInput) != -1) {
                    field = $(this);
                }
            });
            if (field !== null) {
                // extract userIds from selected objects list
                for(var i=0; i < selected.length; i++){
                    selectedOrderedUserIds.push( $(selected[i]).attr("id") );
                }
                field.val("j#{selectedEvaluationUsersLocator." + regText + "}[" + selectedOrderedUserIds.toString() + "]");
                log('Found - ' + selected.length + ' - selected people and setting ordering form value now. New val is:' + field.val());
                return true;
            } else {
                log("ERROR: Field param with part val:" + regText + " Not FOUND.");
            }
    };

    // Debugging
    log = (($.fn.assignSelector.defaults.debug) && window.console) ? console.info : function(){return true;};

})($);

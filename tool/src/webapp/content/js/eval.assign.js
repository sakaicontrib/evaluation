/**
 * For the evalAssign view
 */
$(document).ready(function() {
    //trigger FB overrides 	
    evalAssignFacebox.init();
    $('a[rel=assignInstructorSelector]').assignSelector({type:0});
    $('a[rel=assignTaSelector]').assignSelector({type:1});
    $(':submit').bind('click', function() {
        //Validate group Selections
        var countChecked = $('form[id=eval-assign-form] input[@type=checkbox]:checked').get().length;
        if (countChecked == null || countChecked == 0) {
            $('#error').fadeIn(0);
            return false;
        } else {
            $('#error').fadeOut(0);
        }
        return true;
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
            documentFB:null
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
        initedafterRevealFacebox:0,
        that:null,
        deselectedLog:new Array(),
        thatRowNumber:0 ,
        evalGroupId:null,
        groupCheckBox:null,
        css: {
            enabled:{
                color: ""
            },
            disabled:{
                color: "#888"
            }
        }
    };

    function init(that, options) {
        //copy options to this class
        variables.options = options;
        $(document).bind('afterReveal.facebox', function() {
            variables.initedafterRevealFacebox++;
            log(variables.initedafterRevealFacebox);
                log("WARN: Lightbox loaded, attaching listerners now...");
                var _that = $('#facebox div.content:eq(0)');    //lightbox $document object
                variables.get.documentFB = _that ? _that : null;
                //Set the current evalGroupID
                var grpId = variables.get.documentFB.find("input[name=evalGroupId]").val();
                variables.evalGroupId = grpId == null ? '' : grpId.replace('/site/', '');
                //deselect boxes already deselected
                var field, regText = variables.evalGroupId + ".deselected" + (variables.options.type == 0 ? "Instructors" : "Assistants"),
                sRegExInput = new RegExp(regText);
                $('input[name=el-binding]').each(function() {
                    if ($(this).val().search(sRegExInput) != -1) {
                        field = $(this);
                    }
                });
                var deselected = field.val().replace("j#{selectedEvaluationUsersLocator." + regText + "}[", "").replace(/"/g,"").replace(/ /g,"").replace("]", "").split(",");
                if (deselected.length > 0) {
                    variables.get.documentFB.find('input[@type=checkbox]:checked').each(function() {
                        for (var i = 0; i < deselected.length; i++) {
                            if ( deselected[i] == $(this).attr('id')) {
                                this.checked = false;
                            }
                        }
                    });
                    handleCheckboxes(variables.get.documentFB.find('.selectTable tbody input[@type=checkbox]').not(':checked'), 1);
                }

                //check proper radiobox depending on selcetion option already saved in the DOM
                regText = variables.evalGroupId + (variables.options.type === 0 ? ".instructor" : ".assistant"),
                sRegExInput = new RegExp(regText);
                $('input[name=el-binding]').each(function() {
                    if ($(this).val().search(sRegExInput) != -1) {
                        field = $(this);
                    }
                });
                var selectionSaved = field.val().replace("j#{assignGroupSelectionSettings." + regText + "}", "");
                log("selectionSaved value: " + selectionSaved);
                if (selectionSaved.toString().length > 0) {
                    variables.get.documentFB.find('input[@type=radio]').each(function() {
                        if ( selectionSaved === $(this).val()) {
                            this.checked = true;
                        }

                    });
                }

                /**
                 *  Excecute this as soon as lightbox DON is loaded
                 */

                //set initial state of select all box
                if(variables.get.documentFB.find('.selectTable tbody input[@type=checkbox]').not(':checked').length === 0){
                   $('#selectorControl').each(function(){
                      this.checked = true;
                    });
                }
                $.facebox.setHeader($("#title"));
                $("#facebox .header .titleHeader").show();
                //Activate Mass (De)Selector controls
                $("#selectorControl").bind('click', function(){
                    if (this.checked) {
                        $('.selectTable tbody input[@type=checkbox]').not(':checked').each(function(){
                            this.checked = true;
                        });
                    }else{
                        $('.selectTable tbody input[@type=checkbox]:checked').each(function(){
                          this.checked = false;
                        });
                    }
                });
                //bind individual checkbox to toggle main selector
                $(".selectTable tbody input[@type=checkbox]").bind('click', function(){
                    if (this.checked) {
                        if($('.selectTable tbody input[@type=checkbox]').not(':checked').length === 0){
                           $('#selectorControl').each(function(){
                              this.checked = true;
                            });
                        }
                    }else{
                        $('#selectorControl').each(function(){
                          this.checked = false;
                        });
                    }
                });

                $('#facebox input[id=save-item-action]').bind('click', function() {
                    log("Binding submit button value");
                    handleFormSubmit(this);

                    return false;
                });
                //Make list scrollable if hieght is more than 200px
                var tableHolder = variables.get.documentFB.find('.selectTable:eq(0)');
                tableHolder
                        .css({
                    'overflow': 'auto',
                    'height': tableHolder.height() > 200 ? '205px' : (tableHolder.height() + 10) + "px"
                }) ;
                log("Formatting table holder hieght. Set height to:" + tableHolder.height());
        });
        $(document).bind('afterClose.facebox', function() {
            log("Running afterClose.facebox; initialising variables.initedafterRevealFacebox.");
            variables.initedafterRevealFacebox = 0;
        });
        return that.each(function() {
            initControls($(this));
            initClassVars();
        });
    }

    function initClassVars() {
        variables.selectedPeople = 0;
        variables.deselectedPeopleIds = new Array();
        variables.thatRowNumber = 0;
    }

    function initControls(that) {
        that.hide();
        variables.groupCheckBox = that.parents('tr').find('input[@type=checkbox]');
        that.bind('click', function() {
                    var _url = that.attr('href');
                    variables.that = that;
                    variables.thatRowNumber = that.parents('tr').attr('rel');
                    variables.set.typeOfBranch(that);
                    log("Fetching URL: " + _url);
                    $.facebox({ajax: _url});
                    return false;
                });
        variables.groupCheckBox.bind('click', function() {
            if (this.checked) {
                that.fadeIn('fast');
            } else {
                that.fadeOut('fast');

            }
        });
        that.parents('tr.selectedGroup').find('input[@type=checkbox]').each(function(){
            //$(this).trigger("click");
            this.checked = true;
            that.fadeIn('fast');
            
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
            $(document).trigger('close.facebox');
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
        var that = $(_that), temp = variables.get.documentFB.find('.selectTable tbody input[@type=checkbox]').not(':checked'),
        tempChecked = variables.get.documentFB.find('.selectTable tbody input[@type=checkbox]:checked'),
        selectionChosen = variables.get.documentFB.find('input[@type=radio]:checked');
        variables.selectedPeople = tempChecked.length > 0 ? tempChecked.length : 0;
        if(temp.length>0){
            if (handleCheckboxes(temp, 0)) {
                //reset dom count on selected Instr/Ass
                var origionalSelected = variables.that.attr('class').replace("addItem total:", "");  //gets a String
                var text = variables.that.attr('title') + " (" + variables.selectedPeople + "/" + origionalSelected + ")";
                variables.that.text(text);
                $(document).trigger('close.facebox');
            }
        }else{
              if (handleCheckboxes(tempChecked, 2)) {
                //reset dom count on selected Instr/Ass
                var origionalSelected2 = variables.that.attr('class').replace("addItem total:", "");  //gets a String
                var text2 = variables.that.attr('title') + " (" + variables.selectedPeople + "/" + origionalSelected2 + ")";
                variables.that.text(text2);
                $(document).trigger('close.facebox');
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

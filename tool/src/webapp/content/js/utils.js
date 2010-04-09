var sakai = sakai ||
{};
var utils = utils ||
{};
var evalsys = evalsys ||
{};

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
    $('.blockItemLabel,.blockItemLabelNA').hover(function(e){
	       var text = $(this).find('span').text();
	     $('#toolTip').text(text);
	        var width = $('#toolTip').width();
	        var pos = $(this).parents('.answerCell').find('.NACell').position();
	        $(this).find('input').attr('title', '');
	
	        $('#toolTip').css({
	            'top': pos.top + 1,
	            'left': pos.left - width - 25
	        });
	       $('#toolTip').show();
	}, function(e){
	      $('#toolTip').hide();
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

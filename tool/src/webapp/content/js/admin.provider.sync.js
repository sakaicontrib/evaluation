

$(document).ready(function(){
    var initialTab = $('span.initialTab').text();
    var $tabs = $("#tabs").tabs({ selected: initialTab });
    //$tabs.tabs('selected', initialTab);
    $('#addSync').click(function(e){
        e.preventDefault();
        $('#syncEdit').show('slow');
        $(this).hide();
        // TODO: resize
        setMainFrameHeight(window.id);
    })
    $('.triggerDelete').click(function(e){
		e.preventDefault();
		var fullJobName = $(this).closest('td').children('span.fullJobName').text();
		var triggerDeleteConfirm = $(this).closest('td').children('span.triggerDeleteConfirm').text();
		var decision = confirm(triggerDeleteConfirm);
		if(decision){
			$('.syncDeleteForm').find('input.syncDeleteItem').val(fullJobName);
			$('.syncDeleteForm').find('input.syncDeleteSubmit').click();
		}
		else{
			
		}
    })
    $('.triggerRevise').click(function(e){
    	e.preventDefault();
    	var fullJobName = $(this).closest('td').children('span.fullJobName').text();
    	var triggerCronExpression = $(this).closest('td').children('span.triggerCronExpression').text();
    	var triggerStateList = $(this).closest('td').children('span.triggerStateList').text();
    	$(this).closest('form').find('.formfullJobName').val(fullJobName);
    	$(this).closest('form').find('.formCronExpression').val(triggerCronExpression);
    	
    	$(this).closest('form').find('input:checkbox').each(function(index,element){
    		var stateName = $(element).siblings('span.formStateName').text();
    		$(element).attr('checked',(triggerStateList.indexOf(stateName) >= 0));
    	});
    	$(this).closest('form').find('.createSync').hide();
    	$(this).closest('form').find('.updateSync').show();
    	
        $('#syncEdit').show('slow');
        $('#addSync').hide();
        setMainFrameHeight(window.id);
    });
    $('.cancelSyncEdit').click(function(e){
    	e.preventDefault();
    	$(this).closest('form').find('.formCronExpression').val('');
    	$(this).closest('form').find('input:checkbox').attr('checked', false);
    	$(this).closest('form').find('.createSync').show();
    	$(this).closest('form').find('.updateSync').hide();
    	
    	$('#syncEdit').hide('slow');
    	$('#addSync').show();
    	// TODO: resize
    	setMainFrameHeight(window.id);
    });
    $('#tabs').bind('tabsselect', function(event, ui) {
    	$('.currentTab').val(ui.index);
    	setMainFrameHeight(window.id);
    });
});

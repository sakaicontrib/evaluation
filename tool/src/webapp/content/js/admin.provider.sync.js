var evalsysAdminSync = evalsysAdminSync || {};

evalsysAdminSync.resizeFrame = function(updown) {
	try {
		var clientH, frame = parent.document.getElementById(window.name);
		if (frame) {
			if (updown === 'shrink') {
				clientH = document.body.clientHeight;
			} else {
				clientH = document.body.clientHeight + 70;
			}
			$(frame).height(clientH);
		}
	} catch(e) {
	}
}


$(document).ready(function(){
	var initialTab = $('span.initialTab').text();
	var $tabs = $("#tabs").tabs({ selected: initialTab });
	//$tabs.tabs('selected', initialTab);
	$('#addSync').click(function(e){
		e.preventDefault();
		$('#syncEdit').show('slow', function(){
			evalsysAdminSync.resizeFrame('grow');
		});
		$(this).hide();
		// TODO: resize
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
		$(this).closest('form').find('.updateSync').show('slow', function(){
			evalsysAdminSync.resizeFrame('grow');
		});

		$('#syncEdit').show('slow', function(){
			evalsysAdminSync.resizeFrame('grow');
		});
		$('#addSync').hide();
	});
	$('.cancelSyncEdit').click(function(e){
		e.preventDefault();
		$(this).closest('form').find('.formCronExpression').val('');
		$(this).closest('form').find('input:checkbox').attr('checked', false);
		$(this).closest('form').find('.createSync').show('slow', function(){
			evalsysAdminSync.resizeFrame('grow');
		});
		$(this).closest('form').find('.updateSync').hide();
		$('#syncEdit').hide('slow', function(){
			$('#addSync').show();
			evalsysAdminSync.resizeFrame('grow');
		});
	});
	$('#tabs').bind('tabsselect', function(event, ui) {
		$('.currentTab').val(ui.index);
		$('#syncEdit').hide();
		$('#addSync').show();
	});
	$('#tabs').bind('tabsshow', function(event, ui) {
		evalsysAdminSync.resizeFrame('grow');
	});
});


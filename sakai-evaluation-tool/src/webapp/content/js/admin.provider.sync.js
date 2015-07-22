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


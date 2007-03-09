function addScaleOption() {

	var scaleOptionsPath = document.getElementById('scale-options-path');
	alert(scaleOptionsPath.value);
	
	var ni = document.getElementById('myNewSpan');
	var numi = document.getElementById('scale-options-num');
	
	var num = (document.getElementById('scale-options-num').value -1)+ 2;
	alert(num);

	numi.value = num;
	var newspan = document.createElement('span');
	var spanIdName = 'my'+num+'Span';
	newspan.setAttribute('id',spanIdName);
	newspan.innerHTML = '<input type=text name=scaleOptions::' + num + ':scale-option-label/><input type=hidden  name=scaleOptions::'+ num +':scale-option-label-fossil value=istring#{' + scaleOptionsPath.value + num + '}/> &nbsp; <input type=button value=Remove onclick=javascript:removeNewElement(' + spanIdName + ') /> <br/>';
	ni.appendChild(newspan);	
}

function removeNewElement(spanNum) {
	var s = document.getElementById('myNewSpan');
	var oldspan = document.getElementById(spanNum.id);
	s.removeChild(oldspan);
	updateScaleOptionCount();
}

function removeOldElement(spanNum) {
	var s = document.getElementById('myOldSpan');
	var oldspan = document.getElementById(spanNum.parentNode.id);
	s.removeChild(oldspan);
	updateScaleOptionCount();
}

function updateScaleOptionCount() {
	var numi = document.getElementById('scale-options-num');
	var num = (document.getElementById('scale-options-num').value -1);
	numi.value = num;
}
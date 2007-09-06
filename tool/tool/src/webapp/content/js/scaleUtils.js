function addScaleOption() {

	var scaleOptionsPath = document.getElementById('scale-options-path').value;
	alert(scaleOptionsPath);
	
	var nextOptionIndex = document.getElementById('scale-options-num').value; 
	alert(nextOptionIndex);
	
	var ni = document.getElementById('myNewSpan');
	var newspan = document.createElement('span');
	var spanIdName = 'my' + nextOptionIndex + 'Span';
	newspan.setAttribute('id',spanIdName);
	newspan.innerHTML = '<input type=text name=scaleOptions::' + nextOptionIndex + ':scale-option-label/><input type=hidden  name=scaleOptions::'+ nextOptionIndex +':scale-option-label-fossil value=istring#{' + scaleOptionsPath + nextOptionIndex + '}/> &nbsp; <input type=button value=Remove onclick=javascript:removeNewElement(' + spanIdName + ') /> <br/>';
	ni.appendChild(newspan);	
	
	var numi = document.getElementById('scale-options-num');
	var num = (document.getElementById('scale-options-num').value - 1) + 2;
	numi.value = num;
}

function removeNewElement(spanNum) {
	var s = document.getElementById('myNewSpan');
	var oldspan = document.getElementById(spanNum.id);
	s.removeChild(oldspan);
	decreaseScaleOptionCount();
}

function removeOldElement(spanNum) {
	var s = document.getElementById('myOldSpan');
	var oldspan = document.getElementById(spanNum.parentNode.id);
	s.removeChild(oldspan);
	decreaseScaleOptionCount();
}

function decreaseScaleOptionCount() {
	var numi = document.getElementById('scale-options-num');
	var num = (document.getElementById('scale-options-num').value -1);
	numi.value = num;
}
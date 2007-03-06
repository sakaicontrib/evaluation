function addScaleOption() {

	var ni = document.getElementById('myNewSpan');
	var numi = document.getElementById('theValue');
	var num = (document.getElementById('theValue').value -1)+ 2;
	numi.value = num;
	var newspan = document.createElement('span');
	var spanIdName = 'my'+num+'Span';
	newspan.setAttribute('id',spanIdName);
	newspan.innerHTML = '<input type=text/> &nbsp; <input type=button value=Remove onclick=javascript:removeNewElement(' + spanIdName + ') /> <br/>';
	ni.appendChild(newspan);	
}

function removeNewElement(spanNum) {
	var s = document.getElementById('myNewSpan');
	var oldspan = document.getElementById(spanNum.id);
	s.removeChild(oldspan);
}

function removeOldElement(spanNum) {
	var s = document.getElementById('myOldSpan');
	var oldspan = document.getElementById(spanNum.parentNode.id);
	s.removeChild(oldspan);
}
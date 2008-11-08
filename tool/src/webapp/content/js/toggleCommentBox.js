// Show/hide comment box in Evaluation tool
		function showComment(elem){
			getBox(elem).style.display = "";
			getBox(elem).focus();
			elem.parentNode.style.display = "none";
			elem.parentNode.parentNode.getElementsByTagName("div")[1].style.display = "";
			try{
				resizeFrame('grow');
			}
			catch(e){}
		}
		function hideComment(elem){
			if (getBox(elem).value){
				editComment(elem);
			}
			else{
			elem.parentNode.style.display = "none";
			elem.parentNode.parentNode.getElementsByTagName("div")[0].style.display = "";
			elem.parentNode.parentNode.getElementsByTagName("div")[0].setAttribute("class","addItem");
			elem.parentNode.parentNode.getElementsByTagName("div")[0].innerHTML = '<a class="commentLinks" href="#" onclick="showComment(this);return false;">Add a comment</a>';
			getBox(elem).style.display = "none";
			}
		}
		function getBox(elem){
			return (elem.parentNode.parentNode.getElementsByTagName("div")[2].firstChild);
		}
		function editComment(elem){
			var len = 70;
			  var trunc = getBox(elem).value;
			  if (trunc.length > len) {
				trunc = trunc.substring(0, len);
				trunc = trunc.replace(/\w+$/, '');
				trunc += '... <a class="commentLinks" href="#" ' +
				  'onclick="showComment(this);return false;">' +
				  'Edit comment.<\/a>';
				elem.parentNode.parentNode.getElementsByTagName("div")[0].style.display = "";
				elem.parentNode.parentNode.getElementsByTagName("div")[0].setAttribute("class", "editComment");
				elem.parentNode.parentNode.getElementsByTagName("div")[0].innerHTML = '<b>Comment: </b>' + trunc;
				elem.parentNode.style.display = "none";
				getBox(elem).style.display = "none";
			  }
			  else{
				elem.parentNode.parentNode.getElementsByTagName("div")[0].style.display = "";
				elem.parentNode.parentNode.getElementsByTagName("div")[0].removeAttribute("class");
				elem.parentNode.parentNode.getElementsByTagName("div")[0].setAttribute("class", "editComment");
				elem.parentNode.parentNode.getElementsByTagName("div")[0].innerHTML = '<b>Comment: </b>' + trunc + '. <a class="commentLinks" href="#" onclick="showComment(this);return false;">Edit comment.</a>';
				elem.parentNode.style.display = "none";
				getBox(elem).style.display = "none";
			  }
		}
function processCommentBoxes(){
	var commentLinks = new Array();
	commentLinks = getElementsByClass(document,"commentLinks","*");
	for(var i = 0; i < commentLinks.length; i++){
		var elem = commentLinks[i];
			if (getBox(elem).value){
			var len = 70;
			  var trunc = getBox(elem).value;
			  if (trunc.length > len) {
				trunc = trunc.substring(0, len);
				trunc = trunc.replace(/\w+$/, '');
				trunc += '... <a class="commentLinks" href="#" ' +
				  'onclick="showComment(this);return false;">' +
				  'Edit comment.<\/a>';
				elem.parentNode.parentNode.getElementsByTagName("div")[0].style.display = "";
				elem.parentNode.parentNode.getElementsByTagName("div")[0].setAttribute("class", "editComment");
				elem.parentNode.parentNode.getElementsByTagName("div")[0].innerHTML = '<b>Comment: </b>' + trunc;
			  }
			  else{
				elem.parentNode.parentNode.getElementsByTagName("div")[0].style.display = "";
				elem.parentNode.parentNode.getElementsByTagName("div")[0].removeAttribute("class");
				elem.parentNode.parentNode.getElementsByTagName("div")[0].setAttribute("class", "editComment");
				elem.parentNode.parentNode.getElementsByTagName("div")[0].innerHTML = '<b>Comment: </b>' + trunc + '. <a class="commentLinks" href="#" onclick="showComment(this);return false;">Edit comment.</a>';
			  }
			}
	}
}		
function getElementsByClass(node,searchClass,tag) {
var classElements = new Array();
var els = node.getElementsByTagName(tag); // use "*" for all elements
var elsLen = els.length;
var pattern = new RegExp("\\b"+searchClass+"\\b");
for (i = 0, j = 0; i < elsLen; i++) {
 if ( pattern.test(els[i].className) ) {
 classElements[j] = els[i];
 j++;
 }
}
return classElements;
}

//this function needs jquery 1.1.2 or later - it resizes the parent iframe without bringing the scroll to the top
function resizeFrame(updown)
{
var frame = parent.document.getElementById( window.name );

if( frame )
{
if(updown=='shrink')
{
var clientH = document.body.clientHeight - 30;
}
else
{
var clientH = document.body.clientHeight + 30;
}
$( frame ).height( clientH );
}
else
{
throw( "resizeFrame did not get the frame (using name=" + window.name + ")" );
}
} 
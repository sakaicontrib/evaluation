/* 
	 Javascript checks. Make sure that:
	* 1) All dates are in valid date format
	* 2) Start date cannot be before today.
	* 3) Due date cannot be before start date
	* 4) View date cannot be before or same as due date
	*TODO: need to validate email format
*/
	
	// Declaring valid date character, minimum year and maximum year
	var dtCh= "/";
	var minYear=1900;
	var maxYear=2100;
	

	function isInteger(s){
		var i;
    	for (i = 0; i< s.length; i++){   
       	 // Check that current character is number.
       	 var c = s.charAt(i);
        	if (((c < "0") || (c > "9"))) return false;
    	}
   		 // All characters are numbers.
   	 return true;
	}

	function stripCharsInBag(s, bag){
		var i;
    	var returnString = "";
    	// Search through string's characters one by one.
    	// If character is not in bag, append to returnString.
   		for (i = 0; i < s.length; i++){   
       	 	var c = s.charAt(i);
        	if (bag.indexOf(c) == -1) returnString += c;
   		}
   	 	return returnString;
	}

	function daysInFebruary (year){
		// February has 29 days in any year evenly divisible by four,
   		 // EXCEPT for centurial years which are not also divisible by 400.
   	 return (((year % 4 == 0) && ( (!(year % 100 == 0)) || (year % 400 == 0))) ? 29 : 28 );
	}
	
	function DaysArray(n) {
		for (var i = 1; i <= n; i++) {
			this[i] = 31;
			if (i==4 || i==6 || i==9 || i==11) {this[i] = 30;}
			if (i==2) {this[i] = 29;}
   	} 
   	return this;
	}

	function isDate(dtStr){	
		var daysInMonth = DaysArray(12);
		var pos1=dtStr.indexOf(dtCh);
		var pos2=dtStr.indexOf(dtCh,pos1+1);
		var strMonth=dtStr.substring(0,pos1);
		var strDay=dtStr.substring(pos1+1,pos2);
		var strYear=dtStr.substring(pos2+1);
		strYr=strYear;
		
		if (strDay.charAt(0)=="0" && strDay.length>1) 
			strDay=strDay.substring(1);
		if (strMonth.charAt(0)=="0" && strMonth.length>1) 
			strMonth=strMonth.substring(1);
		for (var i = 1; i <= 3; i++) {
			if (strYr.charAt(0)=="0" && strYr.length>1) 
				strYr=strYr.substring(1);
		}
		month=parseInt(strMonth);
		day=parseInt(strDay);
		year=parseInt(strYr);
		
		if (pos1==-1 || pos2==-1){
			alert("The date format should be : mm/dd/yyyy");
			return false;
		}
		
		if (strMonth.length<1 || month<1 || month>12){
			alert("Please enter a valid month");
			return false;
		}
		
		if (strDay.length<1 || day<1 || day>31 || (month==2 && day>daysInFebruary(year)) || day > daysInMonth[month]){
			alert("Please enter a valid day");
			return false;
		}
		
		if (strYear.length != 4 || year==0 || year<minYear || year>maxYear){
			alert("Please enter a valid 4 digit year between "+minYear+" and "+maxYear);
			return false;
		}
		
		if (dtStr.indexOf(dtCh,pos2+1)!=-1 || isInteger(stripCharsInBag(dtStr, dtCh))==false){
			alert("Please enter a valid date");
			return false;
		}
		
		return true;
	} //end of function	 
	
	function validateSettingsForm(){
	//TODO: need ot validate email pattern
		var dateStart = document.getElementById('startDate');
		if (isDate(dateStart.value)==false){
			dateStart.focus();
			return false;
		}
		
		var dateDue = document.getElementById('dueDate'); 
		if (isDate(dateDue.value)==false){
			dateDue.focus();
			return false;
		}
		
		var dateView = document.getElementById('viewDate'); 
		if (isDate(dateView.value)==false){
			dateView.focus();
			return false;
		}
		
		//var today = new Date();	
		
		var status = document.getElementById('evalStatus'); 
		//var today = new Date();	 
		var todayDummy = document.getElementById('dummyToday'); 
		//alert("yesterday =" + todayDummy.value );
		if(status.value != "active"){
		//disable validation of start date for active eval
			//if (Date.parse(dateStart.value) <  Date.parse(today.toString())){
			if (Date.parse(dateStart.value) <=  Date.parse(todayDummy.value)){
				alert("You cannot select start date to be before today. Please select a different start date and submit the form");
				return false;
			}
		}
		
		if (Date.parse(dateDue.value) < Date.parse(dateStart.value)) {
			alert("You cannot select due date before start date. Please select a different due date and submit the form");
			return false;
		}
		//View date check is for admins
		else if (Date.parse(dateView.value) <= Date.parse(dateDue.value)) {
			alert("You cannot select view results date same as due date or before due date. Please select a different view results date and submit the form");
			return false;
		}
		//Check for student and instructor view dates only if results private is not checked.
		else if ( !(document.getElementById('resultsPrivate').checked) ){
		 
			//Following check is for student view dates
			var viewResultsFlagStu = document.getElementById('showResultsToStudents:::studentViewResults');
			if (viewResultsFlagStu != null && viewResultsFlagStu.checked) {
				var dateViewStu = document.getElementById('showResultsToStudents:::showResultsToStuDate:::studentsDate');
				if (isDate(dateViewStu.value)==false){
					dateViewStu.focus();
					return false;
				}
				else if (Date.parse(dateViewStu.value) <= Date.parse(dateDue.value)) {
					alert("You cannot select view results date for students to be same as due date or before due date. Please select a different view results date for students and submit the form");
					dateViewStu.focus();
					return false;
				}else { 
					//Do nothing as there is a return true at the very end
				}
			}
			
			//Following check is for instructor view dates
			var viewResultsFlagInst = document.getElementById('showResultsToInst:::instructorViewResults');
			if (viewResultsFlagInst != null && viewResultsFlagInst.checked) {
				var dateViewInst = document.getElementById('showResultsToInst:::showResultsToInstDate:::instructorsDate');
				if (isDate(dateViewInst.value)==false){
					dateViewInst.focus();
					return false;
				}
				else if (Date.parse(dateViewInst.value) <= Date.parse(dateDue.value)) {
					alert("You cannot select view results date for instructors to be same as due date or before due date. Please select a different view results date for instructors and submit the form");
					dateViewInst.focus();
					return false;
				}else { 
					//Do nothing as there is a return true at the very end
				}
			}		
			return true;
		}
		else {
			return true;
		}
 	}
 	
 	function updateStuInstDates() {
		var dateView = document.getElementById('viewDate');
		var dateViewStu = document.getElementById('showResultsToStudents:::showResultsToStuDate:::studentsDate');
		var dateViewInst = document.getElementById('showResultsToInst:::showResultsToInstDate:::instructorsDate');
		
		if (dateViewStu != null) 
			dateViewStu.value = dateView.value;
			
		if (dateViewInst != null) 
			dateViewInst.value = dateView.value;
 	} 	
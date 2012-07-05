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
// Eval My Items JS for page onload function
// @author lovemore.nalube@uct.ac.za


$(document).ready(function () {
    $('a[rel=facebox]').faceboxGrid();
			$('form.inlineForm :submit').bind("click", function(){
                evalTemplateFacebox.addItem( $('form').attr("action") + "?" + $("form").formSerialize() );
                return false;
		    });
    //init Global Ajax Overide Options
    evalTemplateData.ajaxSetUp();
});

//trigger FB overrides
evalTemplateFacebox.init();

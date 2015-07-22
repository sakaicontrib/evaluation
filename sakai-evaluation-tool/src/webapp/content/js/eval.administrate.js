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
//used on the administration pages

// http://www.jshint.com/
/*jshint jquery:true, browser:true */
var evalsys = evalsys || {};

evalsys.initAdministrate = function() {
    "use strict";
    // Run this stuff when the administrate page is loaded

    // Handle disabling the recently closed input boxes when the related checkboxes are unchecked
    $('input.dash-enable-evaluatee-box-marker').change(function () {
        var $relatedInput = $('input.dash-evaluatee-closed-still-recent-marker');
        if ($(this).attr("checked")) {
            // box is checked
            $relatedInput.removeAttr('disabled'); // jquery 1.6+ - .prop('disabled', false);
            return;
        }
        // box is 'unchecked'
        $relatedInput.attr('disabled','disabled'); // jquery 1.6+ - .prop('disabled', true);
    }).change(); // call the on change immediately
    $('input.dash-enable-administrating-box-marker').change(function () {
        var $relatedInput = $('input.dash-eval-closed-still-recent-marker');
        if ($(this).attr("checked")) {
            // box is checked
            $relatedInput.removeAttr('disabled'); // jquery 1.6+ - .prop('disabled', false);
            return;
        }
        // box is 'unchecked'
        $relatedInput.attr('disabled','disabled'); // jquery 1.6+ - .prop('disabled', true);
    }).change(); // call the on change immediately

};

$(document).ready(function () {
    "use strict";
    // run this on page load complete
    evalsys.initAdministrate();
});

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
function setMainFrameHeightFixed(dokkument, outerid) {
	// run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	if (typeof window.name != "undefined" && outerid != window.name) return;

	var frame = parent.document.getElementById(outerid);
	if (frame) {
      var objToResize = (frame.style) ? frame.style : frame;
      var newHeight = RSF.computeDocumentHeight(dokkument);
      newHeight += 20; // double Gonzalo's hallowed fudge factor from Sakai headscripts
      objToResize.height = newHeight + "px";
      }
    }

// Very long function name, since this can't be safely put in the RSF namespace!
function addSakaiRSFDomModifyHook(frameID) {
  /* Could not get this to work (in sakai-10.x r86819)
  if (typeof(RSF) != "undefined" && RSF.getDOMModifyFirer) {
    var firer = RSF.getDOMModifyFirer();
    firer.addListener(
      function() {
        setMainFrameHeightFixed(document, frameID);
        setFocus(focus_path);
        });
    }
  }
  */
	MutationObserver = window.MutationObserver || window.WebKitMutationObserver;

	var observer = new MutationObserver(function(mutations, observer) {
        setMainFrameHeightFixed(document, frameID);
        setFocus(focus_path);
	});

	// define what element should be observed by the observer
	// and what types of mutations trigger the callback
	observer.observe(document, {
		subtree: true,
		childList: true
	});
	
	
  }

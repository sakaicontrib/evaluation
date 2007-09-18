/**
 * The EvalSystem set of functions, written by antranig (keeps the namespace clean and tidy) -AZ
 */
var EvalSystem = function() {
  function $it(elementID) {
    return document.getElementById(elementID);
  }

  return {
    getRelativeID: function (baseid, targetid) {
      colpos = baseid.lastIndexOf(':');
      return baseid.substring(0, colpos + 1) + targetid;
    },    

   // See http://www.pageresource.com/jscript/jdropbox.htm for general approach
    operateSelectLink: function(linkid, localselectid) {
      var selectid = EvalSystem.getRelativeID(linkid, localselectid);
      var selection = $it(selectid);
      var url = selection.options[selection.selectedIndex].value;
      // See http://www.quirksmode.org/js/iframe.html for discussion
      document.location.href = url;
    },

    decorateReorderSelects: function(namebase, count) {
      var buttonid = namebase + "hiddenBtn";
      var button = $it(buttonid);
      for (var i = 0; i < count; ++ i) {
        var selectid = namebase + "item-row::"+i+":item-select-selection";
        var selection = $it(selectid);
        selection.onchange = function() {
        	button.click();
        };
      }
    }
  }
}();
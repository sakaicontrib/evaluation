/*
 * Facebox (for jQuery) edited by Lovemore.nalube@uct.ac.za
 * - DO NOT UPGRADE THIS PLUGIN. THERE IS CUSTOMIZED CODE HERE
 * version: 1.2 (05/05/2008)
 * @requires jQuery v1.2 or later
 *
 * Examples at http://famspam.com/facebox/
 *
 * Licensed under the MIT:
 *   http://www.opensource.org/licenses/mit-license.php
 *
 * Copyright 2007, 2008 Chris Wanstrath [ chris@ozmm.org ]
 *
 */
(function($) {
  $.facebox = function(data, klass) {
    $.facebox.loading()

    if (data.ajax) fillFaceboxFromAjax(data.ajax)
    else if (data.image) fillFaceboxFromImage(data.image)
    else if (data.div) fillFaceboxFromHref(data.div)
    else if ($.isFunction(data)) data.call($)
    else $.facebox.reveal(data, klass)
  }

  /*
   * Public, $.facebox methods
   */

  $.extend($.facebox, {
    defaults: {
      opacity      : 0.1,
      overlay      : true,
      loadingImage : '/library/image/sakai/spinner.gif',
      closeImage   : '/library/image/sakai/cross.png',
      imageTypes   : [ 'png', 'jpg', 'jpeg', 'gif' ],
	  objToUpdate	: null, //this is to store the parent element of item being edited to update after an edit via ajax
	  confirmBox: function(f){
	  	return ('\
			                <div> You have reordered the Questions list. Would you like to save it? \
			                </div> \
			                <div class="footer"> \
			                  <input type="button" value="Save and Continue" onclick="jQuery.facebox.settings.saveOrder(\''+ f +'\');" class="active" accesskey="s" />  |  \
			                  <a href="#" onclick="jQuery.facebox({ajax:\''+ f +'\'})">Continue without saving</a>  \
			                </div> \
					');
	  },
	  saveOrder: function(f){
						$('#saveReorderButton').click(); 
						jQuery.facebox({ajax:(f)});
					  },
      faceboxHtml  : '\
    <div id="facebox" style="display:none;"> \
      <div class="popup"> \
        <table width="700px"> \
          <tbody> \
            <tr> \
              <td class="tl"/><td class="b"/><td class="tr"/> \
            </tr> \
            <tr> \
              <td class="b"/> \
              <td class="body"> \
              <div class="header breadCrumb" style="display:block"> \
			  <a class="close" href="#" accesskey="x"><img class="close_image" title="close"/></a></div> \
              	<div style="display: none" class="results"></div> \
				<div class="content"> \
                </div> \
              </td> \
              <td class="b"/> \
            </tr> \
            <tr> \
              <td class="bl"/><td class="b"/><td class="br"/> \
            </tr> \
          </tbody> \
        </table> \
      </div> \
    </div>'
    },

    loading: function() {
      init()
      if ($('#facebox .loading').length == 1) return true
      showOverlay()

		$('#saveReorderButton').click()
      $('#facebox .content').empty()
      $('#facebox .body').children().hide().end().
        append('<div class="loading">LOADING...<br /><br /><img src="'+$.facebox.defaults.loadingImage+'"/></div>')

      $('#facebox').css({
        top:	getPageScroll()[1] + (getPageHeight() / 500)
        //left:	385.5
      }).show()
	  $('#facebox').css('left', $(window).width() / 2 - ($('#facebox table').width() / 2)).show()

      $(document).bind('keydown.facebox', function(e) {
        if (e.keyCode == 27) $.facebox.close()
        return true
      })
      $(document).trigger('loading.facebox')
    },

    reveal: function(data, klass) {
		$('#facebox .content').empty()
      	$("#facebox .results").empty()
      $(document).trigger('beforeReveal.facebox')
      if (klass) $('#facebox .content').addClass(klass)
      $('#facebox .content').append(data)
      $('#facebox .loading').remove()
      $('#facebox .body').children().fadeIn('fast')
      //$('#facebox').css('left', $(window).width() / 2 - ($('#facebox table').width() / 2))
	  frameGrow('up');
      $(document).trigger('reveal.facebox').trigger('afterReveal.facebox')
	  $(document).trigger('resize.facebox')
    },

    close: function() {
      $(document).trigger('close.facebox')
      return false
    },
	
	grow: function(){frameGrow('up')},
	
	shrink: function(){frameShrink()},
	
	setHeader: function(obj){
		if($("#facebox .titleHeader"))
			$("#facebox .titleHeader").remove();
		$(obj).clone(true).insertBefore($("#facebox .header .close"));
		$(obj).remove();
		//alert(obj.html());
		//$("#facebox .header .active").val(obj);
		//$(obj).clone(true).insertBefore($("#facebox .close"));
		//$("#facebox .header .active").click(function(){
			//alert(form);
			//$(form).trigger('click');
			//return false;
		//});
		//$("#facebox .btn").replaceWith(obj.clone() +"rgsf");
	},
	
	setBtn: function(obj, form){
		var btn = '<input type="button" class="active" accesskey="s" />';
		$(btn).insertBefore($("#facebox .close"));
		$("#facebox .header .active").val(obj);
		//$(obj).clone(true).insertBefore($("#facebox .close"));
		$("#facebox .header .active").click(function(){
			//alert(form);
			$(form).trigger('click');
			//return false;
		});
		//$("#facebox .btn").replaceWith(obj.clone() +"rgsf");
	},
	
	showResponse: function(entityCat, id){
		fillActionResponse(entityCat, id);
	}
  })

  /*
   * Public, $.fn methods
   */

  $.fn.facebox = function(settings) {
    init(settings)
	
    function clickHandler() {
		
	resetClasses();
	
	if($(this).attr("rel") == "faceboxGrid"){
		$(this).parent().parent().parent().parent().attr("class","editing");
		$.facebox.defaults.objToUpdate = $(this).parent().parent().parent();
	}
		
      $.facebox.loading(true)

      // support for rel="facebox.inline_popup" syntax, to add a class
      // also supports deprecated "facebox[.inline_popup]" syntax
      var klass = this.rel.match(/facebox\[?\.(\w+)\]?/)
      if (klass) klass = klass[1]

	//if the save box is active, prompt for save or cancel
			//if(confirm("change has been made")){
				 //fillFaceboxFromHref(this.href, klass)
	     	//	 return false
			//}else
			//jQuery(document).trigger('close.facebox');
		if ($('#orderInputs').attr('class') == "itemOperationsEnabled"){
			jQuery.facebox($.facebox.defaults.confirmBox(this.href));
			//alert();
			return false;
		}else{
			fillFaceboxFromHref(this.href, klass)
	     	return false
		}

    }

    return this.click(clickHandler)
  }

  /*
   * Private methods  
   */

  // called one time to setup facebox on this page
 
  function init(settings) {
    

    if ($.facebox.defaults.inited) return true
    else $.facebox.defaults.inited = true

    $(document).trigger('init.facebox')
    makeCompatible()

    var imageTypes = $.facebox.defaults.imageTypes.join('|')
    $.facebox.defaults.imageTypesRegexp = new RegExp('\.' + imageTypes + '$', 'i')

    if (settings) $.extend($.facebox.defaults, settings)
    $('body').append($.facebox.defaults.faceboxHtml)

    var preload = [ new Image(), new Image() ]
    preload[0].src = $.facebox.defaults.closeImage
    preload[1].src = $.facebox.defaults.loadingImage

    $('#facebox').find('.b:first, .bl, .br, .tl, .tr').each(function() {
      preload.push(new Image())
      preload.slice(-1).src = $(this).css('background-image').replace(/url\((.+)\)/, '$1')
    })

    $('#facebox .close').click($.facebox.close)
    $('#facebox .close_image').attr('src', $.facebox.defaults.closeImage)
  }
  
  // getPageScroll() by quirksmode.com
  function getPageScroll() {
    var xScroll, yScroll;
    if (top.self.pageYOffset) {
      yScroll = top.self.pageYOffset;
      xScroll = top.self.pageXOffset;
    } else if (top.document.documentElement && top.document.documentElement.scrollTop) {	 // Explorer 6 Strict
      yScroll = top.document.documentElement.scrollTop;
      xScroll = top.document.documentElement.scrollLeft;
    } else if (top.document.body) {// all other Explorers
      yScroll = top.document.body.scrollTop;
      xScroll = top.document.body.scrollLeft;	
    }
    return new Array(xScroll,yScroll) 
  }

  // Adapted from getPageSize() by quirksmode.com
  function getPageHeight() {
    var windowHeight
    if (top.self.innerHeight) {	// all except Explorer
        windowHeight = top.self.innerHeight;
        //alert(windowHeight);
        //alert(parent.self.innerHeight);
    } else if (top.document.documentElement && top.document.documentElement.clientHeight) { // Explorer 6 Strict Mode
      windowHeight = top.document.documentElement.clientHeight;
    } else if (top.document.body) { // other Explorers
      windowHeight = top.document.body.clientHeight;
    }	
    return windowHeight
  }

  // Backwards compatibility
  function makeCompatible() {
    var $s = $.facebox.defaults

    $s.loadingImage = $s.loading_image || $s.loadingImage
    $s.closeImage = $s.close_image || $s.closeImage
    $s.imageTypes = $s.image_types || $s.imageTypes
    $s.faceboxHtml = $s.facebox_html || $s.faceboxHtml
  }

  // Figures out what you want to display and displays it
  // formats are:
  //     div: #id
  //   image: blah.extension
  //    ajax: anything else
  function fillFaceboxFromHref(href, klass) {
    // div
    if (href.match(/#/)) {
      var url    = window.location.href.split('#')[0]
      var target = href.replace(url,'')
      $.facebox.reveal($(target).clone().show(), klass)

    // image
    } else if (href.match($.facebox.defaults.imageTypesRegexp)) {
      fillFaceboxFromImage(href, klass)
    // ajax
    } else {
      fillFaceboxFromAjax(href, klass)
    }
  }

  function fillFaceboxFromImage(href, klass) {
    var image = new Image()
    image.onload = function() {
      $.facebox.reveal('<div class="image"><img src="' + image.src + '" /></div>', klass)
    }
    image.src = href
  }

  function fillFaceboxFromAjax(href, klass) {
    $.get(href, function(data) { $.facebox.reveal(data, klass) })
  }

  function skipOverlay() {
    return $.facebox.defaults.overlay == false || $.facebox.defaults.opacity === null
  }

  function showOverlay() {
    if (skipOverlay()) return

    if ($('facebox_overlay').length == 0) 
      $("body").append('<div id="facebox_overlay" class="facebox_hide"></div>')

    $('#facebox_overlay').hide().addClass("facebox_overlayBG")
      .css('opacity', $.facebox.defaults.opacity)
      //.click(function() { $(document).trigger('close.facebox') })
      .fadeIn('fast')
    return false
  }

  function hideOverlay() {
    if (skipOverlay()) return

    $('#facebox_overlay').fadeOut('fast', function(){
      $("#facebox_overlay").removeClass("facebox_overlayBG")
      $("#facebox_overlay").addClass("facebox_hide") 
      $("#facebox_overlay").remove()
    })
    
    return false
  }
  
  function fillActionResponse(entityCat,id, templateOwner){
  	if (entityCat == "eval-item") {
		var par = $.facebox.defaults.objToUpdate;
		var id = $(par).children(".itemLine2").children("[name=rowItemId]").html();
	}
		
	entityURL = "/direct/" + entityCat + "/" + id + ".xml";
		$.ajax({
		url: entityURL,
		cache: false,
		dataType: "xml",
	    success: function(xml){
			if (entityCat == "eval-item") {
				$(entityCat, xml).each(function(){
					if (par) {
						$(par).children(".itemLine2").children(".itemText").html($("itemText", this).text());
						$(par).highlight(250, "#eeeeee");
					}
				});
			}
			if (entityCat == "eval-template") {
				$(entityCat, xml).each(function(){
					if ($('form')) {
						var newRow = $('form tr:eq(1)').clone()//.prependTo($('form tbody'));
						newRow.find('td:eq(0) span:eq(0)').html($("title", this).text());
						var q = 'td:eq(0) span:eq(1) a[@href$='+newRow.attr("rowId")+']';
						alert(newRow.find(q).attr('href').substring(newRow.find(q).attr('href').lastIndexOf('/') + 1));
						newRow.find(q).attr('href',newRow.find(q).attr('href').replace(newRow.attr("rowId"),id));
						newRow.attr("rowId", id);
						newRow.find('td:eq(1)').html(templateOwner);
						var tempateDate = $("lastModified", this).text();
						newRow.find('td:eq(2)').html(tempateDate.getFullYear);
						newRow.find('td:eq(0) span:eq(0)').html($("title", this).text());
						
						newRow.prependTo($('form tbody'))
						}
				});
			}
			}
	     });
  }
  
  function resetClasses(){
	$("#itemList").find(".editing").attr("class","itemRow");
	if($("#facebox .titleHeader"))
			$("#facebox .titleHeader").remove();
  }
    
	function frameShrink(){
	  		frameGrow('shrink');
	}
	
    function frameGrow(updown)
    {
        var frame = parent.document.getElementById(window.name);
    try {
            if (frame)
            {
                if (updown == 'shrink')
                {
                    $(frame).height(document.body.clientHeight);
                }
                else
                {
                    $(frame).height(parent.document.body.scrollHeight);
                }
            }
        } catch(e) {
        }
    }
  /*
   * Bindings
   */

    $(document).bind('close.facebox', function() {
      $(document).unbind('keydown.facebox')
      $('#facebox').fadeOut(500, function() {
          frameShrink();
          resetClasses()
        $('#facebox .content').removeClass().addClass('content')
        hideOverlay()
        $('#facebox .loading').remove()
           $('#facebox .loading').remove()
        $('#facebox table').attr('width', 700);
        $('#facebox .body').css('width',660);
        $('#facebox .header').eq(0).show();
		$.facebox.defaults.objToUpdate = null;
      })
        $(document).trigger('afterClose.facebox');
        return false
    })


})(jQuery);

/*This is a fix for the Interface Highlight compatibility bug [added by lovemore.nalube@uct.ac.za]
 * see http://groups.google.com/group/jquery-en/browse_thread/thread/d094a3f299055a99
 */
( function( $ ) {
        $.dequeue = function( a , b ){
                return $(a).dequeue(b);
        };
 })( jQuery ); 

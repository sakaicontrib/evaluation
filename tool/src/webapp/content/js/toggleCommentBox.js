/**
 * Toggle comment box in Evaluation tool
 * @author lovemore.nalube@uct.ac.za
 */

(function($) {
    //Private variable and  functions

    function truncate(dom, str) {
        if (str.length > dom.truncateLength) {
            return ( ( str.substring(0, dom.truncateLength).replace(/\w+$/, '') ) + " ..."  );
        }
        return str;
    }

    //Deal with existing comments
    function hideComment(dom) {
        var truncatedComment = truncate(dom, dom.textarea.val());//Truncate the comment and update Dom
        dom.showLink.hide();
        dom.commentLabel.show();
        dom.editLink.show();
        dom.commentTruncated.text(truncatedComment).show(); //add truncated comment to DOM
    }


    function resizeFrame(updown) {
        try {
            var clientH, frame = parent.document.getElementById(window.name);
            if (frame) {
                if (updown === 'shrink') {
                    clientH = document.body.clientHeight;
                } else {
                    clientH = document.body.clientHeight + 70;
                }
                $(frame).height(clientH);
            }
        } catch(e) {
        }
    }

    function disable(dom) {
        if (dom.hideLink.css("display") !== "none") {
            dom.hideLink.trigger('click');
        }
        var commentLink = dom.commentHolder.find("a, span:not(.JSwarn)");
        commentLink
                .css({color:"#000", opacity:0.5})
                .unbind("click")
                .bind("click", function() {
            return false;
        });
        //Add event for tooltip
        commentLink.tipsy({gravity: 'w'});
        if (dom.textarea.val().length > 0) {
            //Warn user
            dom.warn.fadeIn("fast");
        }
    }

    function enable(dom) {
        dom.commentHolder.find("a, span:not(.JSwarn)")
                .css({color:null, opacity:1})
                .unbind("click")
                //Remove event for tooltip
                .unbind('mouseenter').unbind('mouseleave');
        
        //Hide warning
        dom.warn.fadeOut("fast");
        //Bind the click event for ADD comment control
        dom.showLink.bind('click', function() {
            dom.textarea.show();
            dom.showLink.hide();
            dom.hideLink.show();
            dom.warn.fadeOut("fast");
			resizeFrame('grow');
            dom.textarea.focus();
            return false;
        });
        //Bind the click event for EDIT comment control
        dom.editLink.bind('click', function() {
            dom.textarea.show();
            dom.commentLabel.hide();
            dom.commentTruncated.hide();
            dom.editLink.hide();
            dom.hideLink.show();
            dom.warn.fadeOut("fast");
            resizeFrame('grow');
            dom.textarea.focus();
            return false;
        });
        //Bind the click event for HIDE comment control
        dom.hideLink.bind('click', function() {
            if (dom.textarea.val().length > 0) {
                hideComment(dom);
            } else {
                dom.showLink.show(0, function(){
                    dom.showLink.css({display: "inline"});
                });
            }
            dom.textarea.hide();
            dom.hideLink.hide();
            resizeFrame('shrink');
            return false;
        });

    }

    function init(that) {
        that.each(function() {
            //populate init Fn-scope DOM variables
            var commentHolder = $(this), isSelectionMade = false,
                    dom = {
                        truncateLength: 70,   //Customise this if need be
                        commentHolder: commentHolder,
                        showLink: commentHolder.find("a.JSshow"),
                        editLink: commentHolder.find("a.JSedit"),
                        hideLink: commentHolder.find("a.JShide"),
                        textarea: commentHolder.find("textarea"),
                        commentTruncated: commentHolder.find("span.JScommentTruncated"),
                        commentLabel: commentHolder.find("span.JSlabel"),
                        //if li:eq(0) doesn't return an object, we are in an item preview
                        questionHolder: commentHolder.parents("li.evalItemTop:eq(0)").length === 0 ? commentHolder.parents("div.highlightPanel:eq(0)") : commentHolder.parents("li.evalItemTop:eq(0)"),
                        warn: commentHolder.find("span.JSwarn")
                    };
            //Set listeners for any question item response activity and toggle comment controls - EVALSYS-628

			dom.questionHolder.find("input").each(function() {
                if (this.checked) {
                    isSelectionMade = true;
                }

                $(this).bind("click", function() {
                    //Only items with checkboxes or radio buttions are supported.
					// console.log(dom.questionHolder.find("input:checked").length)
					  if (dom.questionHolder.find(":checked").length > 0) {
                        //Activate comment controls
                        enable(dom);
                    } else if (dom.questionHolder.find(":checked").length === 0) {
                        //De-activate comment controls
                        disable(dom);
                    }
                });
            });

            //Question has an answer (possibly a comment too), so activate comment controls
            if ( isSelectionMade && dom.textarea.val().length > 0 ) {
                enable(dom);
                dom.hideLink.trigger('click');
            } else if (isSelectionMade) {
                enable(dom);
            } else {
                disable(dom);
            }
        });
    }
    $.fn.evalComment = function() {
            init($(this));
        };

})(jQuery);
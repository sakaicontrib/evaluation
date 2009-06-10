/**
 * Toggle comment box in Evaluation tool
 * @author lovemore.nalube@uct.ac.za
 */ 

(function($) {
    $.fn.evalComment = function() {
        init($(this));
    };
    //Private variable and  functions
    var truncateLength = 70;   //Customise this if need be
    function init(that) {
        that.each(function() {
            //populate init Fn-scope DOM variables
            var commentHolder = $(this),
            dom = {
                commentHolder: commentHolder,
                showLink: commentHolder.find("a.JSshow"),
                editLink: commentHolder.find("a.JSedit"),
                hideLink: commentHolder.find("a.JShide"),
                textarea: commentHolder.find("textarea"),
                commentTruncated: commentHolder.find("span.JScommentTruncated"),
                commentLabel: commentHolder.find("span.JSlabel")
            };
            //Bind the click event for ADD comment control
            dom.showLink.bind('click', function() {
                dom.textarea.slideDown("fast");
                dom.showLink.slideUp("fast");
                dom.hideLink.slideDown("fast");
                resizeFrame('grow');
                dom.textarea.focus();
                return false;
            });
            //Bind the click event for EDIT comment control
            dom.editLink.bind('click', function() {
                dom.textarea.slideDown("fast");
                dom.commentLabel.slideUp("fast");
                dom.commentTruncated.slideUp("fast");
                dom.editLink.slideUp("fast");
                dom.hideLink.slideDown("fast");
                resizeFrame('grow');
                dom.textarea.focus();
                return false;
            });
            //Bind the click event for HIDE comment control
            dom.hideLink.bind('click', function() {
                if (dom.textarea.val().length > 0) {
                    hideComment(dom);
                } else {
                    dom.showLink.slideDown("fast");
                }
                dom.textarea.slideUp("fast");
                dom.hideLink.slideUp("fast");
                resizeFrame('shrink');
                return false;
            });
        });
    }
    //Deal with existing comments
    function hideComment(dom) {
        var truncatedComment = truncate(dom.textarea.val());//Truncate the comment and update Dom
        dom.showLink.hide();
        dom.commentLabel.fadeIn("fast");
        dom.editLink.fadeIn("fast");
        dom.commentTruncated.text(truncatedComment).fadeIn("fast"); //add truncated comment to DOM
    }
    function truncate(str) {
        if (str.length > truncateLength) {
            return ( ( str.substring(0, truncateLength).replace(/\w+$/, '') ) + " ..."  );
        }
        return str;
    }
    function resizeFrame(updown) {
        try {
            var clientH, frame = parent.document.getElementById(window.name);
            if (frame) {
                if (updown === 'shrink') {
                    clientH = document.body.clientHeight - 30;
                } else {
                    clientH = document.body.clientHeight + 30;
                }
                $(frame).height(clientH);
            }
        } catch(e) {}
    }
})($);
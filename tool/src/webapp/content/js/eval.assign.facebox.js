// Eval Template Facebox JS for extending the facebox functionality
// @author lovemore.nalube@uct.ac.za

var evalAssignFacebox = (function() {

    //backup the facebox functions
    var origionalFBfunctions = {
        loading: $.facebox.loading,
        close: $.facebox.close,
        reveal: $.facebox.reveal
    };

    var initFunction = function() {
        //Override FB settings or Extend methods/actions

        $.facebox.settings.opacity = 0.1;
        $.facebox.settings.overlay = true;
        $.facebox.settings.loadingImage = '/library/image/sakai/spinner.gif';
        $.facebox.settings.closeImage = '/library/image/sakai/cross.png';

        $.facebox.settings.faceboxHtml = '<div id="facebox" style="display:none;">' +
                                         '<div class="popup"> ' +
                                         '<table width="700px"> ' +
                                         '<tbody> ' +
                                         '<tr>' +
                                         '<td class="tl"/><td class="b"/><td class="tr"/>' +
                                         '</tr>' +
                                         '<tr>' +
                                         '<td class="b"/>' +
                                         '<td class="body">' +
                                         '<div class="header breadCrumb" style="display:block">' +
                                         '<a class="close" href="#" accesskey="x"><img class="close_image" title="close"/></a></div>' +
                                         '<div style="display: none" class="results"></div>' +
                                         '<div class="content">' +
                                         '</div>' +
                                         '</td>' +
                                         '<td class="b"/>' +
                                         '</tr>' +
                                         '<tr>' +
                                         '<td class="bl"/><td class="b"/><td class="br"/>' +
                                         '</tr>' +
                                         '</tbody>' +
                                         '</table>' +
                                         '</div>' +
                                         '</div>';

        $.facebox.loading = function() {
            origionalFBfunctions.loading();
            $('#saveReorderButton').click();
            $('#facebox').css({
                left:    $(window).width() / 2 - ($('#facebox table').width() / 2)
            }).show();
            $('#facebox_overlay').unbind('click');
        };

        $.facebox.reveal = function(data, klass) {
            $('#facebox .content').empty();
            $("#facebox .results").empty();
            var fbCssLeft = $('#facebox').css('left');
            origionalFBfunctions.reveal(data, klass);
            //restore left css value
            $('#facebox').css('left', fbCssLeft);
            //bind event handler for FB form buttons
            //bind the close button
            $('#facebox .close').unbind('click');
            $('#facebox .close').bind('click', function() {
                $.facebox.close();
            });
        };

        $.facebox.close = function() {
            origionalFBfunctions.close();
            $('#facebox table').attr('width', 700);
            $('#facebox .body').css('width', 660);
            $('#facebox .header').eq(0).show();
            return false;
        };

        $.facebox.setHeader = function(obj) {
            if ($("#facebox .titleHeader")) {
                $("#facebox .titleHeader").remove();
            }
            $(obj).clone(true).insertBefore($("#facebox .header .close"));
            $(obj).remove();
        };
    };


    return {
        init: function() {
            initFunction();
        }
    };


})($);
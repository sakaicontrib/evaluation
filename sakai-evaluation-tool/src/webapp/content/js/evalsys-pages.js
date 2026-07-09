evalsys.initSummary = function() {
    // click to show/toggle arrows
    var $beEvaluatedBox = jQuery(".beEvaluated");
    var $inProgressTable = $beEvaluatedBox.find(".evaluated_in_progress");
    if ($inProgressTable.length > 0) {
        $beEvaluatedBox.find(".inProgressEvals").click(function(){
            var $this = jQuery(this);
            var $table = $inProgressTable;
            if ($table.is(":visible")) {
                // hide it
                $this.removeClass("triangle-open").addClass("triangle-closed");
                $table.slideUp();
            } else {
                // show it
                $this.removeClass("triangle-closed").addClass("triangle-open");
                $table.slideDown();
            }
        });
    }
    var $closedTable = $beEvaluatedBox.find(".evaluated_closed");
    if ($closedTable.length > 0) {
        $beEvaluatedBox.find(".closedEvals").click(function(){
            var $this = jQuery(this);
            var $table = $closedTable;
            if ($table.is(":visible")) {
                // hide it
                $this.removeClass("triangle-open").addClass("triangle-closed");
                $table.slideUp();
            } else {
                // show it
                $this.removeClass("triangle-closed").addClass("triangle-open");
                $table.slideDown();
            }
        });
    }
    if (typeof jQuery().tablesorter === "undefined") {
        alert("programming error: jquery tablesorter is not loaded!");
    }
    // http://tablesorter.com/docs/
    jQuery.tablesorter.addParser({ 
        id: 'humanDate', // set a unique id
        is: function(s) { return false; },
        format: function(str,table,cell) { // return a normalized version which is easily sorted
            return $(cell).attr("data-time");
        }, 
        type: 'numeric' 
    });
    jQuery("TABLE.beEvaluatedInProgressTable").tablesorter({
        headers: { 0:{sorter:'text'}, 1:{sorter:'humanDate'}, 2:{sorter:'humanDate'}, 3:{sorter: false}, 4:{sorter: false} },
        sortList: [[2,0]]
    });
    jQuery("TABLE.beEvaluatedClosedTable").tablesorter({
        headers: { 0:{sorter:'text'}, 1:{sorter:'humanDate'}, 2:{sorter:'humanDate'}, 3:{sorter: false}, 4:{sorter: false} },
        sortList: [[2,1]]
    });
};

evalsys.initControlScales = function() {
    var modalEl = document.getElementById('scaleModal');
    var modal = new bootstrap.Modal(modalEl);
    var $body = jQuery('#scaleModalBody');

    modalEl.addEventListener('show.bs.modal', function() {
        evalsys.positionModalInViewport(modalEl);
    });

    jQuery(document).on('click', 'a.preview_scale', function(e) {
        e.preventDefault();
        $body.html('<p class="text-center py-3"><span class="spinner-border spinner-border-sm"></span></p>');
        modal.show();
        jQuery.ajax({
            url: this.href,
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            success: function(html) { $body.html(html); },
            error: function() { $body.html('<p class="text-danger">Error loading preview.</p>'); }
        });
    });

    modalEl.addEventListener('shown.bs.modal', function() {
        jQuery(modalEl).find('.modal-dialog').draggable({ handle: '.modal-header' });
    });

    modalEl.addEventListener('hidden.bs.modal', function() { $body.html(''); });
};

evalsys.initModifyScales = function() {
    var $textboxes = $("div.labelindnt input:text");
    $textboxes.attr("maxlength", "250"); // force the existing ones first
    $textboxes.bind("click", function(event){
        // each time the text box is clicked on
        $(this).attr("maxlength", "250"); // force the input text boxes to 250 chars or less
    });
};

evalsys.initPreviewScales = function() {
    var $container = jQuery("#items_container");
    var containerWidth = $container.closest('.modal-body').length
        ? $container.closest('.modal-body').innerWidth()
        : jQuery("body").innerWidth();

    $container.accordion({
        heightStyle: "content",
        activate: function(event, ui) {
            ui.newPanel.css({
                "min-width": "200px",
                "max-width": (containerWidth - 40) + "px"
            });
        }
    });
    evalsys.instrumentItems("div.preview-item");
};

// In Sakai, the tool iframe is full-height and the portal scrolls externally.
// Bootstrap's position:fixed is relative to the iframe viewport, which doesn't match
// the visible browser area when the user has scrolled. This helper adjusts the
// modal dialog's margin-top so it appears within the visible viewport.
evalsys.positionModalInViewport = function(modalEl) {
    try {
        if (window.parent && window.frameElement) {
            var parentScrollY = window.parent.scrollY || window.parent.pageYOffset || 0;
            var iframeOffsetTop = window.frameElement.getBoundingClientRect().top + parentScrollY;
            var parentViewH   = window.parent.innerHeight;
            var visibleTop    = parentScrollY - iframeOffsetTop;
            var marginTop     = Math.max(10, visibleTop + parentViewH * 0.05);
            jQuery(modalEl).find('.modal-dialog').css('margin-top', marginTop + 'px');
        }
    } catch(e) { /* cross-origin fallback: use Bootstrap default */ }
};

evalsys.initPreviewItem = function(selector) {
    // NOTE: this essentially loads in a lightbox so be careful
    evalsys.instrumentItems(selector);
};

evalsys.initControlItems = function() {
    evalsys.initFacebox({verticalCenterOnClick: true, minWidth: 740});
    jQuery('a.preview_item').facebox();

    // When facebox opens, if it contains the edit form, intercept its submit
    jQuery(document).bind('reveal.facebox', function() {
        var $form = jQuery('#facebox #item-form');
        if ($form.length === 0) { return; }
        $form.off('submit.faceboxEdit').on('submit.faceboxEdit', function(e) {
            e.preventDefault();
            var formData = $form.serialize();
            jQuery.ajax({
                url: $form.attr('action'),
                type: 'POST',
                data: formData,
                success: function() {
                    jQuery(document).trigger('close.facebox');
                    window.location.reload();
                }
            });
        });
    });

    jQuery('a.edit_item').facebox();
};

evalsys.initControlItemsModal = function() {
    var modalEl = document.getElementById('itemModal');
    var modal = new bootstrap.Modal(modalEl);
    var $body = jQuery('#itemModalBody');

    modalEl.addEventListener('show.bs.modal', function() {
        evalsys.positionModalInViewport(modalEl);
    });

    function loadIntoModal(url) {
        $body.html('<p class="text-center py-3"><span class="spinner-border spinner-border-sm"></span></p>');
        modal.show();
        jQuery.ajax({
            url: url,
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            success: function(html) {
                $body.html(html);
                var $form = $body.find('#item-form');
                if ($form.length === 0) { return; }
                $form.off('submit.modalEdit').on('submit.modalEdit', function(e) {
                    e.preventDefault();
                    jQuery.ajax({
                        url: $form.attr('action'),
                        type: 'POST',
                        data: $form.serialize(),
                        headers: { 'X-Requested-With': 'XMLHttpRequest' },
                        success: function() {
                            modal.hide();
                            window.location.reload();
                        },
                        error: function() {
                            alert('Error saving the item. Please try again.');
                        }
                    });
                });
            },
            error: function() {
                $body.html('<p class="text-danger">Error loading content.</p>');
            }
        });
    }

    jQuery(document).on('click', 'a.preview_item, a.edit_item', function(e) {
        e.preventDefault();
        loadIntoModal(this.href);
    });

    // Make modal draggable from the header using jQuery UI
    modalEl.addEventListener('shown.bs.modal', function() {
        jQuery(modalEl).find('.modal-dialog').draggable({
            handle: '.modal-header'
        });
    });

    // Clear modal body when hidden to avoid stale content
    modalEl.addEventListener('hidden.bs.modal', function() {
        $body.html('');
    });
};

evalsys.initModifyItem = function() {
    var modalEl = document.getElementById('previewItemModal');
    var modal = new bootstrap.Modal(modalEl);
    var $body = jQuery('#previewItemModalBody');
    var $previewLink = jQuery('a.preview_item');
    var originalUrl = $previewLink.attr('href');

    modalEl.addEventListener('show.bs.modal', function() {
        evalsys.positionModalInViewport(modalEl);
    });

    $previewLink.click(function(e) {
        e.preventDefault();
        // Build URL with current form values
        var textVal = '';
        if (typeof CKEDITOR !== 'undefined' && CKEDITOR.instances && CKEDITOR.instances['item-text']) {
            textVal = CKEDITOR.instances['item-text'].getData();
        } else {
            textVal = jQuery('#item-text').val() || '';
        }
        var scaleDisplay = jQuery('#scale-display-list').val() || jQuery('#choices-display-list').val() || '';
        var na = jQuery('#item-na').is(':checked') ? 'true' : 'false';
        var textLines = jQuery('#item-response-size-list').val() || '';
        var params = [];
        if (scaleDisplay) { params.push('scaleDisplay=' + encodeURIComponent(scaleDisplay)); }
        if (textVal)       { params.push('text=' + encodeURIComponent(textVal)); }
        params.push('na=' + na);
        if (textLines)     { params.push('textLines=' + encodeURIComponent(textLines)); }
        var url = originalUrl + (params.length > 0 ? '?' + params.join('&') : '');

        $body.html('<p class="text-center py-3"><span class="spinner-border spinner-border-sm"></span></p>');
        modal.show();
        jQuery.ajax({
            url: url,
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            success: function(html) { $body.html(html); },
            error: function() { $body.html('<p class="text-danger">Error loading preview.</p>'); }
        });
    });

    modalEl.addEventListener('hidden.bs.modal', function() { $body.html(''); });
};

evalsys.initTemplateItemsModal = function() {
    var modalEl = document.getElementById('templateItemModal');
    var modal = new bootstrap.Modal(modalEl);
    var $body = jQuery('#templateItemModalBody');

    modalEl.addEventListener('show.bs.modal', function() {
        evalsys.positionModalInViewport(modalEl);
    });

    function loadIntoModal(url) {
        $body.html('<p class="text-center py-3"><span class="spinner-border spinner-border-sm"></span></p>');
        modal.show();
        jQuery.ajax({
            url: url,
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            success: function(html) {
                $body.html(html);
                bindModalForm();
            },
            error: function() {
                $body.html('<p class="text-danger">Error loading content.</p>');
            }
        });
    }

    function bindModalForm() {
        var $form = $body.find('form').first();
        if ($form.length === 0) { return; }
        $form.off('submit.modalEdit').on('submit.modalEdit', function(e) {
            e.preventDefault();
            // Sync CKEditor instances (e.g. the block item text) into their
            // textareas before serializing, since they don't update on their
            // own when the AJAX submit below bypasses the native form submit.
            if (typeof CKEDITOR !== 'undefined') {
                jQuery.each(CKEDITOR.instances, function(name, instance) {
                    instance.updateElement();
                });
            }
            jQuery.ajax({
                url: $form.attr('action'),
                type: 'POST',
                data: $form.serialize(),
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                success: function(html) {
                    // If response has no form, save succeeded
                    if (jQuery(html).find('form').length === 0 && html.indexOf('<form') === -1) {
                        modal.hide();
                        window.location.reload();
                    } else {
                        $body.html(html);
                        bindModalForm();
                    }
                },
                error: function() {
                    alert('Error saving. Please try again.');
                }
            });
        });
    }

    jQuery(document).on('click', 'a[rel=faceboxGrid], a[rel=childEdit]', function(e) {
        e.preventDefault();
        loadIntoModal(this.href);
    });

    jQuery(document).on('submit', '#createBlockForm', function(e) {
        e.preventDefault();
        loadIntoModal(this.action + '?' + jQuery(this).serialize());
    });

    modalEl.addEventListener('shown.bs.modal', function() {
        jQuery(modalEl).find('.modal-dialog').draggable({ handle: '.modal-header' });
    });

    modalEl.addEventListener('hidden.bs.modal', function() { $body.html(''); });
};


//SPECIAL inits
/**
 * This sets up the facebox lightbox system
 */
evalsys.initFacebox = function(options) {
    if (!evalsys.faceboxinitialized) {
        // only run the facebox init one time
        if (typeof jQuery.facebox !== "undefined") {
            // Use portal-provided styling; avoid image-based close button
            jQuery.facebox.settings.closeImage = null;
            //jQuery.facebox.settings.opacity = 0.1;
            //jQuery.facebox.settings.overlay = true;
            //jQuery.facebox.settings.faceboxHtml = "some html";
            // DOCS: https://github.com/defunkt/facebox
            if (options && options.verticalCenterOnClick) {
                jQuery(document).bind('beforeReveal.facebox', function() {
                    // set the vertical position
                    var posY = jQuery.facebox.mousePosY;
                    var $facebox = $('#facebox');
                    $facebox.css({
                        'top': posY+'px'
                    });
                    // set the min-width
                    if (options && options.minWidth > 0) {
                        $facebox.css({
                            'min-width': (options.minWidth+26) +'px'
                        });
                        $facebox.find('.body').css({
                            'min-width': (options.minWidth) +'px'
                        });
                    }
                });
            }
            jQuery(document).bind('reveal.facebox', function() {
                // set the width
                var $facebox = $('#facebox');
                var faceboxWidth = $facebox.find('table.faceboxtable').width();
                if (options && options.minWidth > 0) {
                    faceboxWidth = faceboxWidth < options.minWidth ? options.minWidth : faceboxWidth;
                }
                $facebox.css({
                    'width': (faceboxWidth+26) +'px'
                });
                $facebox.find('.body').css({
                    'width': (faceboxWidth) +'px'
                });
            });
            evalsys.faceboxinitialized = true;
        } else {
            alert("Programming error: no facebox is available!");
        }
    }
};

// Utility function to select/deselect all checkboxes of a given form
evalsys.toggleCheckboxes = function( formName, checkToggle )
{
    var elements = document[formName].getElementsByTagName( "input" );
    for( var i = 0; i < elements.length; i++ )
    {
        if( elements[i].type === "checkbox" )
        {
            elements[i].checked = checkToggle;
        }
    }
};

// Hack function to toggle new/old report format
evalsys.toggleReportFormat = function( formName )
{
    var elements = document[formName].getElementsByTagName( "input" );
    for( var i = 0; i < elements.length; i++ )
    {
        if( elements[i].type === "hidden" && elements[i].name === "el-binding" && elements[i].value.indexOf( "{reportExporterBean.newReportStyle}" ) !== -1 )
        {
            var newValue = "";
            var oldValue = elements[i].value.substring( elements[i].value.indexOf( "}" ) + 1 );
            if( oldValue.toLowerCase() === "true" )
            {
                newValue = "false";
            }
            else
            {
                newValue = "true";
            }
            elements[i].value = elements[i].value.substring( 0, elements[i].value.indexOf( "}" ) + 1 ) + newValue;
        }
    }
};

evalsys.setItemOperationsState = function(target, enabled) {
    var $element = $(target);
    if (!$element || $element.length === 0) {
        return;
    }
    $element.addClass('item-operations alert d-flex flex-wrap align-items-center gap-2 py-2 px-3');
    $element.toggleClass('alert-warning', !!enabled);
    $element.toggleClass('alert-secondary', !enabled);
    $element.toggleClass('item-operations--enabled', !!enabled);
};

// Sticky table headers inside Sakai iframes.
// position:sticky doesn't work because the portal (parent window) scrolls,
// not the iframe. We listen to the parent's scroll event and apply translateY.
evalsys.initStickyHeaders = function() {
    var scrollParent;
    try {
        scrollParent = (window.parent && window.parent !== window) ? window.parent : window;
    } catch (e) {
        scrollParent = window;
    }

    scrollParent.addEventListener('scroll', function() {
        var iframeTop = 0;
        try {
            if (window.frameElement) {
                iframeTop = window.frameElement.getBoundingClientRect().top;
            }
        } catch (e) {}

        document.querySelectorAll('table.evalsysTable').forEach(function(table) {
            var thead = table.querySelector('thead');
            if (!thead) return;
            var tableRect = table.getBoundingClientRect();
            var theadHeight = thead.offsetHeight;
            var offset = -(iframeTop + tableRect.top);
            if (offset > 0 && offset < tableRect.height - theadHeight) {
                thead.style.transform = 'translateY(' + offset + 'px)';
            } else {
                thead.style.transform = '';
            }
        });
    });
};

/**
 * Block Matrix items show the start/middle/end scale text above their
 * own option column (.matrixLegendRow > th > .matrixLegendText). With long
 * scale text and many columns the start/middle/end labels can overlap each
 * other; shrink their font-size step by step until they no longer do.
 */
evalsys.fitMatrixLegendRows = function(scope) {
    var root = scope ? jQuery(scope) : jQuery(document);
    var MIN_FONT_SIZE = 8;
    var START_FONT_SIZE = 12;

    function overlaps(spans) {
        var rects = [];
        for (var i = 0; i < spans.length; i++) {
            rects.push(spans[i].getBoundingClientRect());
        }
        for (var a = 0; a < rects.length - 1; a++) {
            for (var b = a + 1; b < rects.length; b++) {
                if (rects[a].right > rects[b].left && rects[b].right > rects[a].left) {
                    return true;
                }
            }
        }
        return false;
    }

    root.find('.matrixLegendRow').each(function() {
        var spans = jQuery(this).find('.matrixLegendText').get();
        if (spans.length < 2) return;

        var fontSize = START_FONT_SIZE;
        jQuery(spans).css('font-size', fontSize + 'px');
        while (fontSize > MIN_FONT_SIZE && overlaps(spans)) {
            fontSize -= 1;
            jQuery(spans).css('font-size', fontSize + 'px');
        }
    });
};

jQuery(function() {
    evalsys.fitMatrixLegendRows();

    // preview_item loaded into the legacy jQuery facebox (control_items, modify_item)
    jQuery(document).bind('reveal.facebox', function() {
        evalsys.fitMatrixLegendRows('#facebox');
    });

    // preview_item loaded into the Bootstrap modal (modify_template_items)
    var templateItemModal = document.getElementById('templateItemModal');
    if (templateItemModal) {
        templateItemModal.addEventListener('shown.bs.modal', function() {
            evalsys.fitMatrixLegendRows(templateItemModal);
        });
    }
});

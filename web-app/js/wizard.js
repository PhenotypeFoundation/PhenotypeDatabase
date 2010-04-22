/**
 * wizard javascript functions
 *
 * @author  Jeroen Wesbeek
 * @since   20100115
 * @package wizard
 * @see     dbnp.studycapturing.WizardTagLib
 * @see     dbnp.studycapturing.WizardController
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
var warnOnRedirect = true;
$(document).ready(function() {
    // check if user is using Firefox 3.6 and warm the user
    // about the XMLHttpRequest bug that causes the wizard to break...
    re = /Firefox\/3\.6/gi;
    if (navigator.userAgent.match(re)) {
        // http://code.google.com/p/fbug/issues/detail?id=1899
        var wizard = $('div#wizard')
        if (wizard.find("#warning").length == 0) {
            wizard.html('<span id="warning" style="color:red;font-size:8px;">Firefox 3.6 contains <a href="http://code.google.com/p/fbug/issues/detail?id=2746" target="_new">a bug</a> in combination with Firebug\'s XMLHttpRequest spy which causes the wizard to not function anymore. Please make sure you have Firebug\'s XMLHttpRequest spy disabled or use Firefox 3.5.7 instead...</span>' + wizard.html())
        }
    }

    // attach Tooltips
    insertOnRedirectWarning();
    onWizardPage();
});

// runs when document is ready or a wizard action has been performs
// @see _wizard.gsp, _navigation.gsp, _subjects.gsp
function onWizardPage() {
    // GENERAL
    attachHelpTooltips();
    attachDatePickers();
    attachDateTimePickers();

    // handle and initialize table(s)
    attachTableEvents();
    handleWizardTable();
    new TableEditor().init('div.table', 'div.row', 'div.column');

    // initialize the ontology chooser
    new OntologyChooser().init();

    // handle term selects
    new SelectAddMore().init({
        rel     : 'term',
        url     : '/gscf/termEditor',
        vars    : 'ontology',
        label   : 'add more...',
        class   : 'addMore',
        onClose : function(scope) {
            refreshWebFlow();
        }
    });

    // handle template selects
    new SelectAddMore().init({
        rel     : 'template',
        url     : '/gscf/templateEditor',
        vars    : 'entity',
        label   : 'add / modify..',
        class   : 'modify',
        onClose : function(scope) {
            refreshWebFlow();
        }
    });

    // initialize accordeon(s)
    $("#accordion").accordion();
}

// insert a redirect confirmation dialogue to all anchors leading the
// user away from the wizard
function insertOnRedirectWarning() {
    // find all anchors that lie outside the wizard
    $('a').each(function() {
        var element = $(this)
        var re = /^#/gi;

        // bind to the anchor?
        if (!element.attr('href').match(/^#/gi) && !element.attr('href').match(/\/([^\/]+)\/wizard\/pages/gi)) {
            // bind a warning to the onclick event
            element.bind('click',function() {
                if (warnOnRedirect) {
                    return onDirectWarning();
                }
            });
        }
    })
}

function onDirectWarning() {
    return confirm('Warning: navigating away from the wizard causes loss of work and unsaved data. Are you sure you want to continue?');
}

// attach help tooltips
function attachHelpTooltips() {
    // attach help action on all wizard help icons
    $('div#wizard').find('div.helpIcon').each(function() {
        helpIcon = $(this)
        helpContent = helpIcon.parent().parent().find('div.helpContent')

        // handle special content
        var html = (helpContent.html()) ? helpContent.html() : '';
        if (html) {
            var specialContent = html.match(/\[([^:]+)\:([^\]]+)\]/)
            if (specialContent) {
                // replace content by calling a helper function
                eval(specialContent[1] + "('" + specialContent[2] + "',helpContent)");
            }

            // attach tooltip
            helpIcon.qtip({
                content: 'leftMiddle',
                position: {
                    corner: {
                        tooltip: 'leftMiddle',
                        target: 'rightMiddle'
                    }
                },
                style: {
                    border: {
                        width: 5,
                        radius: 10
                    },
                    padding: 10,
                    textAlign: 'center',
                    tip: true,
                    name: 'blue'
                },
                content: helpContent.html(),
                show: 'mouseover',
                hide: 'mouseout',
                api: {
                    beforeShow: function() {
                        // not used at this moment
                    }
                }
            })

            // remove helpcontent div as we don't need it anymore
            helpContent.remove();
        }
    });
}

// insert a youtube player in a certain element
function youtube(video, element) {
    // insert a div we will replace with a youtube player
    element.html("<div id='" + video + "'></div>")

    // insert youtube player
    var params = { allowScriptAccess: "always" };
    var atts = { id: 'myytplayer_' + video };
    swfobject.embedSWF("http://www.youtube.com/v/" + video + "?enablejsapi=1&playerapiid=ytplayer_" + video,
            video, "200", "150", "8", null, null, params, atts)
}

// when a youtube player is ready, play the video
function onYouTubePlayerReady(playerId) {
    ytplayer = document.getElementById("my" + playerId);
    ytplayer.playVideo();
}

// add datepickers to date fields
function attachDatePickers() {
    $('div#wizard').find("input[type=text][rel$='date']").each(function() {
        $(this).datepicker({
            changeMonth : true,
            changeYear  : true,
            dateFormat  : 'dd/mm/yy',
            altField    : '#' + $(this).attr('name') + 'Example',
            altFormat   : 'DD, d MM, yy'
        });
    })
}

// add datetimepickers to date fields
function attachDateTimePickers() {
    $('div#wizard').find("input[type=text][rel$='datetime']").each(function() {
        $(this).datepicker({
            changeMonth     : true,
            changeYear      : true,
            dateFormat      : 'dd/mm/yy',
            altField        : '#' + $(this).attr('name') + 'Example',
            altTimeField    : '#' + $(this).attr('name') + 'Example2',
            altFormat       : 'DD, d MM, yy',
            showTime        : true,
            time24h         : true
        });
    })
}

// attach subject events
function attachTableEvents() {
    $('div#wizard').find('div.row').each(function() {
        $(this).hover(
                function() {
                    $(this).addClass('highlight');
                },
                function() {
                    $(this).removeClass('highlight');
                }
                )
    });
}

// if the wizard page contains a table, the width of
// the header and the rows is automatically scaled to
// the cummalative width of the columns in the header
function handleWizardTable() {
    var that = this;
    var wizardTables = $("div#wizard").find('div.table');

    wizardTables.each(function() {
        var wizardTable = $(this);
        var sliderContainer = (wizardTable.next().attr('class') == 'sliderContainer') ? wizardTable.next() : null;
        var header = wizardTable.find('div.header')
        var width = 20;

        // calculate total width of elements in header
        header.children().each(function() {
            // calculate width per column
            var c = $(this);
            var columnWidth = c.width();
            columnWidth += parseInt(c.css("padding-left"), 10) + parseInt(c.css("padding-right"), 10);          // padding width
            columnWidth += parseInt(c.css("margin-left"), 10) + parseInt(c.css("margin-right"), 10);            // margin width
            columnWidth += parseInt(c.css("borderLeftWidth"), 10) + parseInt(c.css("borderRightWidth"), 10);    // border width
            width += columnWidth;
        });

        // resize the header
        header.css({ width: width + 'px' });

        // set table row width and assume column widths are
        // identical to those in the header (css!)
        wizardTable.find('div.row').each(function() {
            $(this).css({ width: width + 'px' });
        });

        // got a slider for this table?
        if (sliderContainer) {
            // handle slider
            if (header.width() < wizardTable.width()) {
                // no, so hide it
                sliderContainer.css({ 'display': 'none '});
            } else {
                sliderContainer.slider({
                    value   : 1,
                    min     : 1,
                    max     : header.width() - wizardTable.width(),
                    step    : 1,
                    slide: function(event, ui) {
                        wizardTable.find('div.header, div.row').css({ 'margin-left': ( 1 - ui.value ) + 'px' });
                    }
                });
            }
        }
    });
}
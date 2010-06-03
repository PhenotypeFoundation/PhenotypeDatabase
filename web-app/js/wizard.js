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
        var wizard = $('div#wizard');
        if (wizard.find("#warning").length === 0) {
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
        url     : baseUrl + '/termEditor',
        vars    : 'ontologies',
        label   : 'add more...',
        style   : 'addMore',
        onClose : function(scope) {
            refreshWebFlow();
        }
    });

    // handle template selects
    new SelectAddMore().init({
        rel     : 'template',
        url     : baseUrl + '/templateEditor',
        vars    : 'entity',
        label   : 'add / modify..',
        style   : 'modify',
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
        var element = $(this);
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
    });
}

function onDirectWarning() {
    return confirm('Warning: navigating away from the wizard causes loss of work and unsaved data. Are you sure you want to continue?');
}

// attach help tooltips
function attachHelpTooltips() {
    // attach help action on all wizard help icons
    $('div#wizard').find('div.helpIcon').each(function() {
        helpIcon = $(this);
        helpContent = helpIcon.parent().find('div.helpContent');
        if (!helpContent.html()) {
            helpContent = helpIcon.parent().parent().find('div.helpContent');
        }

        // handle special content
        var html = (helpContent.html()) ? helpContent.html() : '';
        if (html) {
            var specialContent = html.match(/\[([^:]+)\:([^\]]+)\]/);
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
            });

            // remove helpcontent div as we don't need it anymore
            helpContent.remove();
        }
    });
}

// insert a youtube player in a certain element
function youtube(video, element) {
    // insert a div we will replace with a youtube player
    element.html("<div id='" + video + "'></div>");

    // insert youtube player
    var params = { allowScriptAccess: "always" };
    var atts = { id: 'myytplayer_' + video };
    swfobject.embedSWF("http://www.youtube.com/v/" + video + "?enablejsapi=1&playerapiid=ytplayer_" + video,
            video, "200", "150", "8", null, null, params, atts);
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
            numberOfMonths: 3,
            showButtonPanel: true,
            changeMonth : true,
            changeYear  : true,
            dateFormat  : 'dd/mm/yy',
            altField    : '#' + $(this).attr('name') + 'Example',
            altFormat   : 'DD, d MM, yy'
        });
    });
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
    });
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
        );
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
        var header = wizardTable.find('div.header');
        var width = 20;
        var column = 0;
        var columns = [];

        // calculate total width of elements in header
        header.children().each(function() {
            // calculate width per column
            var c = $(this);
            var columnWidth     = c.width();
            var paddingWidth    = parseInt(c.css("padding-left"), 10) + parseInt(c.css("padding-right"), 10);
            var marginWidth     = parseInt(c.css("margin-left"), 10) + parseInt(c.css("margin-right"), 10);
            var borderWidth     = parseInt(c.css("borderLeftWidth"), 10) + parseInt(c.css("borderRightWidth"), 10);

            // add width...
            if (paddingWidth) columnWidth += paddingWidth;
            if (marginWidth) columnWidth += marginWidth;
            if (borderWidth) columnWidth += borderWidth;
            width += columnWidth;

            // remember column
            columns[ column ] = c.width();
            column++;
        });

        // resize the header
        header.css({ width: width + 'px' });

        // set table row width and assume column widths are
        // identical to those in the header (css!)
        wizardTable.find('div.row').each(function() {
            var row = $(this);
            var column = 0;
            row.children().each(function() {
                $(this).css({ width: columns[ column] + 'px' });
                column++;
            });
            row.css({ width: width + 'px' });
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

// Show example of parsed data next to RelTime fields
function showExampleReltime(inputfield) {
    var fieldName = inputfield.name;

    var successFunc = function(data, textStatus, request) {
        if( request.status == 200 ) {
            document.getElementById( fieldName + "Example" ).value = data;
        }
    };

    var errorFunc = function( request, textStatus, errorThrown ) {
        // On error, clear the example field
        document.getElementById( fieldName + "Example" ).value = "";
    };
        
    $.ajax({
        url     : baseUrl + '/wizard/ajaxParseRelTime?reltime=' + inputfield.value,
        success : successFunc,
        error   : errorFunc
    });
}

// Create a file upload field
function fileUploadField(field_id) {
	/* example 2 */
	new AjaxUpload('#upload_button_' + field_id, {
		//action: 'upload.php',
		action: baseUrl + '/file/upload', // I disabled uploads in this example for security reaaons
		data : {},
                name : field_id,
                autoSubmit: true,
		onChange : function(file, ext){
                    oldFile = $('#' + field_id).val();
                    if( oldFile != '' ) {
                        if( !confirm( 'The old file is deleted when uploading a new file. Do you want to continue?') ) {
                            return false;
                        }
                    }

                    this.setData({
                            'field':   field_id,
                            'oldFile': oldFile
                    });

                    // Give feedback to the user
                    $('#' + field_id + 'Example').html('Uploading ' + createFileHTML( file ));


		},
		onComplete : function(file, response){
                    if( response == "" ) {
                        $('#' + field_id).val( '' );
                        $('#' + field_id + 'Example').html('<span class="error">Error uploading ' + createFileHTML( file ) + '</span>' );
                    } else {
                        $('#' + field_id).val( response );
                        $('#' + field_id + 'Example').html('Uploaded ' + createFileHTML( file ) );
                    }
		}
	});
}

function createFileHTML( filename ) {
    return '<a target="_blank" href="' + baseUrl + '/file/get/' + filename + '">' + filename + '</a>';
}

function addPublication( element_id ) {
  /* Find publication ID and add to form */
  jQuery.ajax({
    type:"GET",
    url: baseUrl + "/publication/getID?" +  $("#" + element_id + "_form").serialize(),
    success: function(data,textStatus){
        var id = parseInt( data );

        // Put the ID in the array, but only if it does not yet exist
        var ids = getPublicationIds( element_id );

        if( $.inArray(id, ids ) == -1 ) {
            ids[ ids.length ] = id;
            $( '#' + element_id + '_ids' ).val( ids.join( ',' ) );

            // Show the title and a remove button
            showPublication( element_id, id, $("#" + element_id + "_form").find( '[name=publication-title]' ).val(), $("#" + element_id + "_form").find( '[name=publication-authorsList]' ).val(), ids.length - 1 );

            // Hide the 'none box'
            $( '#' + element_id + '_none' ).css( 'display', 'none' );
        }
    },
    error:function(XMLHttpRequest,textStatus,errorThrown){ alert( "Publication could not be added." ) }
  }); return false;
}

function removePublication( element_id, id ) {
    var ids = getPublicationIds( element_id );
    if( $.inArray(id, ids ) != -1 ) {
        // Remove the ID
        ids.splice($.inArray(id, ids ), 1);
        $( '#' + element_id + '_ids' ).val( ids.join( ',' ) );

        // Remove the title from the list
        var li = $( "#" + element_id + '_item_' + id );
        if( li ) {
            li.remove();
        }

        // Show the 'none box' if needed
        if( ids.length == 0 ) {
            $( '#' + element_id + '_none' ).css( 'display', 'inline' );
        }

    }
}

function getPublicationIds( element_id ) {
    var ids = $( '#' + element_id + '_ids' ).val();
    if( ids == "" ) {
        return new Array();
    } else {
        ids_array = ids.split( ',' );
        for( var i = 0; i < ids_array.length; i++ ) {
            ids_array[ i ] = parseInt( ids_array[ i ] );
        }

        return ids_array;
    }
}

function showPublication( element_id, id, title, authors, nr ) {
    var deletebutton = document.createElement( 'img' );
    deletebutton.setAttribute( 'class', 'famfamfam delete_button' );
    deletebutton.setAttribute( 'alt', 'remove this publication' );
    deletebutton.setAttribute( 'src', baseUrl + '/images/icons/famfamfam/delete.png' );
    deletebutton.setAttribute( 'onClick', 'removePublication( "' + element_id + '", ' + id + ' ); return false;' );
    deletebutton.setAttribute( 'style', 'cursor: pointer;' );

    var titleDiv = document.createElement( 'div' );
    titleDiv.setAttribute( 'class', 'title' );
    titleDiv.appendChild( document.createTextNode( title ) );

    var authorsDiv = document.createElement( 'div' );
    authorsDiv.setAttribute( 'class', 'authors' );
    authorsDiv.appendChild( document.createTextNode( authors ) );

    var li = document.createElement( 'li' );
    li.setAttribute( 'id', element_id + '_item_' + id );
    li.setAttribute( 'class', nr % 2 == 0 ? 'even' : 'odd' );
    li.appendChild( deletebutton );
    li.appendChild( titleDiv );
    li.appendChild( authorsDiv );

    $( '#' + element_id + '_list' ).append( li );
}
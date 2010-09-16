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
    new TableEditor().init({
        tableIdentifier : 'div.table',
        rowIdentifier   : 'div.row',
        columnIdentifier: 'div.column',
        headerIdentifier: 'div.header'
    });

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
        vars    : 'entity,ontologies',
        label   : 'add / modify..',
        style   : 'modify',
        onClose : function(scope) {
            refreshWebFlow();
        }
    });

    // Handle person selects
    new SelectAddMore().init({
        rel     : 'person',
        url     : baseUrl + '/person/list?dialog=true',
        vars    : 'person',
        label   : 'add / modify persons...',
        style   : 'modify',
        onClose : function(scope) {
            refreshWebFlow();
        }
    });

    // Handle personRole selects
    new SelectAddMore().init({
        rel     : 'role',
        url     : baseUrl + '/personRole/list?dialog=true',
        vars    : 'role',
        label   : 'add / modify roles...',
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
                sliderContainer.hide();
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


/*************************************************
 *
 * Functions for RelTime fields
 *
 ************************************************/

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

/*************************************************
 *
 * Functions for file upload fields
 *
 ************************************************/

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


/*************************************************
 *
 * Functions for adding publications to the study
 *
 ************************************************/

/**
 * Adds a publication to the study using javascript
 * N.B. The publication must be added in grails when the form is submitted
 */
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

/**
 * Removes a publication from the study using javascript
 * N.B. The deletion must be handled in grails when the form is submitted
 */
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

/**
 * Returns an array of publications IDs currently attached to the study
 * The array contains integers
 */
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

/**
 * Shows a publication on the screen
 */
function showPublication( element_id, id, title, authors, nr ) {
    var deletebutton = document.createElement( 'img' );
    deletebutton.className = 'famfamfam delete_button';
    deletebutton.setAttribute( 'alt', 'remove this publication' );
    deletebutton.setAttribute( 'src', baseUrl + '/images/icons/famfamfam/delete.png' );
    deletebutton.onclick = function() { removePublication(  element_id, id ); return false; };

    var titleDiv = document.createElement( 'div' );
    titleDiv.className = 'title' ;
    titleDiv.appendChild( document.createTextNode( title ) );

    var authorsDiv = document.createElement( 'div' );
    authorsDiv.className = 'authors';
    authorsDiv.appendChild( document.createTextNode( authors ) );

    var li = document.createElement( 'li' );
    li.setAttribute( 'id', element_id + '_item_' + id );
    li.className = nr % 2 == 0 ? 'even' : 'odd';
    li.appendChild( deletebutton );
    li.appendChild( titleDiv );
    li.appendChild( authorsDiv );

    $( '#' + element_id + '_list' ).append( li );
}

/**
 * Creates the dialog for searching a publication
 */
function createPublicationDialog( element_id ) {
    /* Because of the AJAX loading of this page, the dialog will be created
     * again, when the page is reloaded. This raises problems when reading the
     * values of the selected publication. For that reason we check whether the
     * dialog already exists
     */
    if( $( "." + element_id + "_publication_dialog" ).length == 0 ) {
        $("#" + element_id + "_dialog").dialog({
            title   : "Add publication",
            autoOpen: false,
            width   : 800,
            height  : 400,
            modal   : true,
            dialogClass : element_id + "_publication_dialog",
            position: "center",
            buttons : {
               Add  : function() { addPublication( element_id ); $(this).dialog("close"); },
               Close  : function() { $(this).dialog("close"); }
            },
            close   : function() {
                /* closeFunc(this); */
            }
        }).width(790).height(400);
    } else {
       /* If a dialog already exists, remove the new div */
       $("#" + element_id + "_dialog").remove();
    }
}

/**
 * Opens the dialog for searching a publication
 */
function openPublicationDialog( element_id ) {
    // Empty input field
    var field = $( '#' + element_id );
    field.autocomplete( 'close' );
    field.val( '' );

    // Show the dialog
    $( '#' + element_id + '_dialog' ).dialog( 'open' );
    field.focus();

    // Disable 'Add' button
    enableButton( '.' + element_id + '_publication_dialog', 'Add', false );
}

/**
 * Finds a button in a jquery dialog by name
 */
function getDialogButton( dialog_selector, button_name )
{
  var buttons = $( dialog_selector + ' .ui-dialog-buttonpane button' );
  for ( var i = 0; i < buttons.length; ++i )
  {
     var jButton = $( buttons[i] );
     if ( jButton.text() == button_name )
     {
         return jButton;
     }
  }

  return null;
}

/**
 * Enables or disables a button in a selected dialog
 */
function enableButton(dialog_selector, button_name, enable)
{
    var dlgButton = getDialogButton( dialog_selector, button_name );

    if( dlgButton ) {
        if (enable) {
            dlgButton.attr('disabled', '');
            dlgButton.removeClass('ui-state-disabled');
        } else {
            dlgButton.attr('disabled', 'disabled');
            dlgButton.addClass('ui-state-disabled');
        }
    }
}

/*************************************************
 *
 * Functions for adding contacts to the study
 *
 ************************************************/

/**
 * Adds a contact to the study using javascript
 * N.B. The contact must be added in grails when the form is submitted
 */
function addContact( element_id ) {
  // FInd person and role IDs
  var person_id = $( '#' + element_id + '_person' ).val();
  var role_id = $( '#' + element_id + '_role' ).val();

  var combination = person_id + '-' + role_id;

    // Put the ID in the array, but only if it does not yet exist
    var ids = getContactIds( element_id );
    if( $.inArray(combination, ids ) == -1 ) {
        ids[ ids.length ] = combination;
        $( '#' + element_id + '_ids' ).val( ids.join( ',' ) );
        
        // Show the title and a remove button
        showContact( element_id, combination, $("#" + element_id + "_person  :selected").text(), $("#" + element_id + "_role :selected").text(), ids.length - 1 );

        // Hide the 'none box'
        $( '#' + element_id + '_none' ).css( 'display', 'none' );
    }
}

/**
 * Removes a contact from the study using javascript
 * N.B. The deletion must be handled in grails when the form is submitted
 */
function removeContact( element_id, combination ) {
    var ids = getContactIds( element_id );
    if( $.inArray(combination, ids ) != -1 ) {
        // Remove the ID
        ids.splice($.inArray(combination, ids ), 1);
        $( '#' + element_id + '_ids' ).val( ids.join( ',' ) );

        // Remove the title from the list
        var li = $( "#" + element_id + '_item_' + combination );
        if( li ) {
            li.remove();
        }

        // Show the 'none box' if needed
        if( ids.length == 0 ) {
            $( '#' + element_id + '_none' ).css( 'display', 'inline' );
        }

    }
}

/**
 * Returns an array of studyperson IDs currently attached to the study.
 * The array contains string formatted like '[person_id]-[role_id]'
 */
function getContactIds( element_id ) {
    var ids = $( '#' + element_id + '_ids' ).val();
    if( ids == "" ) {
        return new Array();
    } else {
        ids_array = ids.split( ',' );

        return ids_array;
    }
}

/**
 * Shows a contact on the screen
 */
function showContact( element_id, id, fullName, role, nr ) {
    var deletebutton = document.createElement( 'img' );
    deletebutton.className = 'famfamfam delete_button';
    deletebutton.setAttribute( 'alt', 'remove this person' );
    deletebutton.setAttribute( 'src', baseUrl + '/images/icons/famfamfam/delete.png' );
    deletebutton.onclick = function() { removeContact(  element_id, id ); return false; };

    var titleDiv = document.createElement( 'div' );
    titleDiv.className = 'person' ;
    titleDiv.appendChild( document.createTextNode( fullName ) );

    var authorsDiv = document.createElement( 'div' );
    authorsDiv.className = 'role';
    authorsDiv.appendChild( document.createTextNode( role ) );
    
    var li = document.createElement( 'li' );
    li.setAttribute( 'id', element_id + '_item_' + id );
    li.className = nr % 2 == 0 ? 'even' : 'odd';
    li.appendChild( deletebutton );
    li.appendChild( titleDiv );
    li.appendChild( authorsDiv );

    $( '#' + element_id + '_list' ).append( li );
}


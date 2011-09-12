/**
 * This variable holds the currently displayed visualization
 */
var visualization = null;

$(document).ready(function() {
    $(".menu_item").mouseover(
        function() {
            $( this ).css("width","150px");
            $(this).find("div.formulier").show();
		}
    ).mouseout(
        function() {
            $( this ).find("div.formulier").hide();
            $( this ).css("width","88px");
		}
    );
    $("#menu_go").unbind('mouseover').unbind('mouseout');
});

/**
 * Retrieve new fields based on the study that the user has selected.
 */
function changeStudy() {
    $( "#menu_study" ).find("div.formulier").hide();

    if($( '#study option:selected' ).val()!="") {
        $( "#menu_study" ).find("img.spinner").show();
        $( "#menu_study" ).find("div.menu_item_info").html("<br />"+$( '#study option:selected' ).text());

        executeAjaxCall( "getFields", {
            "errorMessage": "An error occurred while retrieving variables from the server. Please try again or contact a system administrator.",
            "success": function( data, textStatus, jqXHR ) {
                // Remove all previous entries from the list
                $( '#rows, #columns' ).empty();

                if(data.infoMessage) {
                    showError(data.infoMessage,"warning");
                }

                clearStep(".menu_item");
                
                // Add all fields to the lists
                if( data.returnData ) {
                    var returnData = data.returnData;

                    var prevCat = "";
	                $.each( returnData, function( idx, field ) {
                        if(field.category!=prevCat) {
                            $( '#rows, #columns' )
                                    .append( $( "<option>" )
                                    .val( "" )
                                    .text( field.source+": "+field.category )
                                    .attr("disabled","disabled")
                                    .css("font-weight","bold")
                                    .css("color","white")
                                    .css("background-color","#333") );
                            prevCat = field.category;
                        }
	                    $( '#rows, #columns' ).append( $( "<option>" ).val( field.id ).text( field.name ) );
	                });
	                
	                $( "#menu_study" ).find("div.menu_item_info").html("<br />"+$( '#study option:selected' ).text());
	                $( "#menu_study" ).addClass("menu_item_done");
	                $( "#menu_row, #menu_column" ).addClass("menu_item_fill");
                }
            }
        },'menu_study');
    } else {
        $( '#rows, #columns' ).empty();
        clearStep(".menu_item");
        $( "#menu_study" ).addClass("menu_item_fill");
    }
}

/**
 * Retrieve the possible visualization types based on the fields that the user has selected.
 */
function changeFields(divid) {
    $( "#"+divid ).find("div.formulier").hide();

    var type = "rows";
    if(divid=="menu_column") type = "columns";

    if($( '#'+type+' option:selected' ).val()!="") {

        $( "#"+divid ).find("img.spinner").show();

        executeAjaxCall( "getVisualizationTypes", {
            "errorMessage": "An error occurred while retrieving visualization types from the server. Please try again or contact a system administrator.",
            "success": function( data, textStatus, jqXHR ) {
                // Remove all previous entries from the list
                $( '#types' ).empty();

                if(data.infoMessage!=null) {
                    showError(data.infoMessage,"warning");
                } else {

                    // Add all fields to the lists
                    var returnData = data.returnData;

                    $.each( returnData, function( idx, field ) {
                        $( '#types' ).append( $( "<option>" ).val( field.id ).text( field.name ) );
                    });

                    clearStep("#menu_vis");
                }

                clearStep("#"+divid);
                $( "#"+divid ).find("div.menu_item_info").html("<br />"+$( '#'+type+' option:selected' ).text());
                $( "#"+divid ).addClass("menu_item_done");

                if((!$( "#menu_vis" ).hasClass("menu_item_done")) &&
                        ($( "#menu_row" ).hasClass("menu_item_done") || divid=="menu_row") &&
                        ($( "#menu_column" ).hasClass("menu_item_done") || divid=="menu_column")
                        ) {
                    clearStep("#menu_vis");
                    $( "#menu_vis" ).addClass("menu_item_fill");
                    if($( '#types option' ).length==1) {
                        $( '#types :first-child' ).attr("selected","selected");
                        changeVis();
                    }
                }


            }
        },divid);
    } else {
        clearStep("#menu_vis, #"+divid);
        $( "#"+divid ).addClass("menu_item_fill");
    }
}

/**
 *
 */
function changeVis() {
    $( "#menu_vis" ).find("div.formulier").hide();
    if($( '#types option:selected' ).val()!="") {
        $( "#menu_vis" ).removeClass().addClass("menu_item menu_item_done");
        $( "#menu_vis" ).find("div.menu_item_info").html("<br />"+$( '#types option:selected' ).text());
    } else {
        $( "#menu_vis" ).find("div.menu_item_info").html("");
        $( "#menu_vis" ).removeClass().addClass("menu_item menu_item_fill");
    }
    if($("#autovis").attr("checked")=="checked") {
        visualize();
    }

}


/**
 * Create a visualization based on the parameters entered in the form
 * The data for the visualization is retrieved from the serverside getData method
 */ 
function visualize() {

    if(!$( "#menu_vis" ).hasClass("menu_item_done") ||
        !$( "#menu_row" ).hasClass("menu_item_done") ||
        !$( "#menu_row" ).hasClass("menu_item_done") ||
        !$( "#menu_row" ).hasClass("menu_item_done") ) {

        $( ".menu_item" ).not(".menu_item_done").removeClass().addClass("menu_item menu_item_warning");
    } else {

        $( "#menu_go" ).find("img.spinner").show();

        executeAjaxCall( "getData", {
            "errorMessage": "An error occurred while retrieving data from the server. Please try again or contact a system administrator.",
            "success": function( data, textStatus, jqXHR ) {
                // Remove old chart, if available
                if( visualization )
                    visualization.destroy();

                if(data.infoMessage!=null) {
                    showError(data.infoMessage,"warning");
                }
                // Handle erroneous data
                /*if( !checkCorrectData( data.returnData ) ) {
                    showError( "Unfortunately the server returned data in a format that we did not expect.", "error" );
                    return;
                }*/

                // Retrieve the datapoints from the json object
                var dataPoints = [];
                var series = [];


                var returnData = data.returnData;
                $.each(returnData.series, function(idx, element ) {
                    dataPoints[ dataPoints.length ] = element.y;
                    series[ series.length ] = { "label": element.name };
                });

                var xlabel = returnData[ "xaxis" ].unit=="" ? returnData[ "xaxis" ].title : returnData[ "xaxis" ].title + " (" + returnData[ "xaxis" ].unit + ")";
                var ylabel = returnData[ "yaxis" ].unit=="" ? returnData[ "yaxis" ].title : returnData[ "yaxis" ].title + " (" + returnData[ "yaxis" ].unit + ")";

                // TODO: create a chart based on the data that is sent by the user and the type of chart
                // chosen by the user
                visualization = $.jqplot('visualization', dataPoints, {
                    // Tell the plot to stack the bars.
                    stackSeries: true,
                    seriesDefaults:{
                        renderer:$.jqplot.BarRenderer,
                        rendererOptions: {
                                // Put a 30 pixel margin between bars.
                                barMargin: 30,
                                // Highlight bars when mouse button pressed.
                                // Disables default highlighting on mouse over.
                                highlightMouseDown: true
                        },
                        pointLabels: {show: true}
                    },
                    series: series,
                    axes: {
                        xaxis: {
                                renderer: $.jqplot.CategoryAxisRenderer,
                                ticks: returnData.x,
                                label: xlabel,
                                labelRenderer: $.jqplot.CanvasAxisLabelRenderer
                        },
                        yaxis: {
                            label: ylabel,
                            labelRenderer: $.jqplot.CanvasAxisLabelRenderer
                        }
                    }

                });

                $( "#visualization" ).show();
                $( "#menu_go" ).find("img.spinner").hide();
            }
        }, "menu_go");
    }
}

/**
 * Shows an error message in a proper way
 * @param message	String	Message to show
 */
function showError( message, strClass ) {
	$( '#message' ).html( message );
    $( '#message' ).removeClass().addClass(strClass);
	$( '#message' ).fadeIn();
    $(document).bind('click',function() {
        $( '#message' ).removeClass();
        $( '#message' ).html("");
        $(document).unbind('click');
    });
}


/**
 * Clears one or multiple steps
 * @param data
 */
function clearStep(strSelector) {
    $( strSelector ).removeClass().addClass("menu_item");
    $( strSelector ).find(".menu_item_info").html("");
    $( strSelector ).find("img.spinner").hide();
}

/** 
 * Checks whether the data in the getData call can be handled correctly
 * @param	JSON object to check
 * @return	boolean	True if the data is correctly formatted, false otherwise
 */
function checkCorrectData( data ) {
	/*
	Data expected:
	{
		"type": "barchart",
		"x": [ "Q1", "Q2", "Q3", "Q4" ],
		"xaxis": { "title": "quarter 2011", "unit": "" },
		"yaxis": { "title": "temperature", "unit": "degrees C" },
		"series": [
			{
				"name": "series name",
				"y": [ 5.1, 3.1, 20.6, 15.4 ],
				"error": [ 0.5, 0.2, 0.4, 0.5 ]
			},
		]
	}
	*/

	return ( "type" in data && "x" in data && "xaxis" in data && "yaxis" in data && "series" in data && $.isArray( data.series ) );
}

/**
 * Gathers data for the given request type from the form elements on the page
 * @param type	String	Can be 'getStudies', 'getFields', 'getVisualizationType' or 'getData'
 * @return Object		Object with the data to be sent to the server
 */
function gatherData( type ) {
	// For simplicity, we send the whole form to the server. In the
	// future this might be enhanced, based on the given type
	return $( 'form#visualizationForm' ).serialize();
}

/**
 * Executes an ajax call in a standardized way. Retrieves data to be sent with gatherData
 * The ajaxParameters map will be sent to the $.ajax call
 * @param action			Name of the action to execute. Is also given to the gatherData method
 * 							as a parameter and the url will be determined based on this parameter.
 * @param ajaxParameters	Hashmap with parameters that are sent to the $.ajax call. The entries
 *							url, data and dataType are set by this method. 
 *							An additional key 'errorMessage' can be given, with the message that will be
 *							shown if an error occurrs in this method. In that case, the 'error' method from
 *							the ajaxParameters method will be overwritten.
 * @see visualizationUrls
 * @see jQuery.ajax
 */
function executeAjaxCall( action, ajaxParameters, divid ) {
	var data = gatherData( action );

	// If no parameters are given, create an empty map
	if( !ajaxParameters ) 
		ajaxParameters = {}

	if( ajaxParameters[ "errorMessage" ] ) {
		var message = ajaxParameters[ "errorMessage" ];
		ajaxParameters[ "error" ] = function( jqXHR, textStatus, errorThrown ) {
			// An error occurred while retrieving fields from the server
			showError( "An error occurred while retrieving variables from the server. Please try again or contact a system administrator.<br />"+textStatus, "error" );
            $( "#"+divid ).removeClass().addClass('menu_item menu_item_error');
            $( "#"+divid ).find("img.spinner").hide();
		}

		// Remove the error message
		delete ajaxParameters[ "errorMessage" ];
	}
	
	// Retrieve a new list of fields from the controller
	// based on the study we chose
	$.ajax($.extend({
		url: visualizationUrls[ action ],
		data: data,
		dataType: "json"
	}, ajaxParameters ) );
}

/**
 * This variable holds the currently displayed visualization
 */
var visualization = null;
var visType = null;
var openForm = null;

$(document).ready(function() {

    $(".topmenu_item").click(
        function(event) {
            if(openForm!=null && this!=openForm) {
                $(openForm).children('.formulier').hide();
            }
            $(this).children('.formulier').toggle();
            openForm = this;
            return false;
		}
    );

    $(document).keyup(
        function(event) {
            if ( event.which == 27 && openForm!=null ) {
                $(openForm).children('.formulier').hide();
                openForm = null;
            }
        }
    );
    
    $(document).click(
        function() {
            if(openForm!=null) {
                $(openForm).children('.formulier').hide();
                openForm = null;
            }
        }
    );

    $(".formulier").click(function(event) {
        event.stopPropagation();
    });

    
});

/**
 * Retrieve new fields based on the study that the user has selected.
 */
function changeStudy() {


    $("#menu_study").children('.formulier').hide();
    openForm = null;

    $( '#rows, #columns, #types' ).empty();
    clearStep(".menu_item");

    if( visualization )
        visualization.destroy();

    if($( '#study option:selected' ).length>0) {
        $( "#menu_row, #menu_column" ).find("img.spinner").show();
        $( "#menu_study" ).find("span.topmenu_item_info").html($( '#study option:selected' ).text());

        executeAjaxCall( "getFields", {
            "errorMessage": "An error occurred while retrieving variables from the server. Please try again or contact a system administrator.",
            "success": function( data, textStatus, jqXHR ) {


                if(data.infoMessage) {
                    showError(data.infoMessage,"message_warning");
                }

                // Add all fields to the lists
                if( data.returnData && data.returnData.studyIds==$( '#study option:selected' ).val() ) {
                    var returnData = data.returnData.fields;

                    var prevCat = "";
                    var strOptions = "";
	                $.each( returnData, function( idx, field ) {
                        if(field.category!=prevCat) {
                            if(prevCat.length>0) strOptions += "</optgroup>";
                            strOptions += "<optgroup label='"+field.source+": "+field.category+"' onClick='return false;'>";
                            prevCat = field.category;
                        }
	                    strOptions += "<option value='"+field.id+"'>"+field.name+"</option>";
	                });
                    strOptions += "</optgroup>";
                    $( "#rows, #columns" ).html(strOptions);
	                
	                $( "#menu_study" ).find("span.topmenu_item_info").html($( '#study option:selected' ).text());
                    $( "#menu_row, #menu_column" ).find("img.spinner").hide();
	                $( "#menu_row, #menu_column" ).addClass("menu_item_fill");
                }
            }
        },'menu_study');
    }
}

/**
 * Retrieve the possible visualization types based on the fields that the user has selected.
 */
function changeFields(divid) {

    var type = "rows";
    if(divid=="menu_column") type = "columns";

    clearStep("#"+divid);
    $( "#"+divid ).addClass("menu_item_done");

    if($( '#rows option:selected' ).length>0 && $( '#columns option:selected' ).length>0) {

        $( "#menu_vis" ).find("img.spinner").show();

        executeAjaxCall( "getVisualizationTypes", {
            "errorMessage": "An error occurred while retrieving visualization types from the server. Please try again or contact a system administrator.",
            "success": function( data, textStatus, jqXHR ) {
                // Remove all previous entries from the list
                $( '#types' ).empty();

                if( data.infoMessage!=null ) {
                    showError(data.infoMessage,"message_warning");
                }

                if( data.returnData && data.returnData.rowIds==$( '#rows option:selected' ).val() && data.returnData.columnIds==$( '#columns option:selected' ).val() ) {
                    // Add all fields to the lists
                    var returnData = data.returnData.types;

                    $.each( returnData, function( idx, field ) {
                        $( '#types' ).append( $( "<option>" ).val( field.id ).text( field.name ) );
                        if( field.name==visType ) { $( '#types option:last' ).attr("selected","selected"); };
                    });
                }

                if( $( '#types option' ).length>0 ) {
                    clearStep("#menu_vis");
                    $( "#menu_vis" ).addClass("menu_item_fill");
                    if( visualization )
                        visualization.destroy();
                    
                    if($( '#types option' ).length==1) {
                        $( '#types :first-child' ).attr("selected","selected");
                    }
                    if($( '#types option:selected' ).length>0) {
                        changeVis();
                    }
                }

                $( "#menu_vis" ).find("img.spinner").hide();

            }
        },divid);
    }
}

/**
 *
 */
function changeVis() {

    if($( '#types option:selected' ).length>0) {
        $( "#menu_vis" ).removeClass().addClass("menu_item menu_item_done");
        visType = $( '#types option:selected' ).text();
    } else {
        $( "#menu_vis" ).removeClass().addClass("menu_item menu_item_fill");
    }
    $( "#menu_go" ).removeClass().addClass("menu_item");
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
        !$( "#menu_column" ).hasClass("menu_item_done")
       ) {
        $( ".menu_item" ).not(".menu_item_done").removeClass().addClass("menu_item menu_item_warning");
    } else {

        executeAjaxCall( "getData", {
            "errorMessage": "An error occurred while retrieving data from the server. Please try again or contact a system administrator.",
            "success": function( data, textStatus, jqXHR ) {
                // Remove old chart, if available
                if( visualization )
                    visualization.destroy();

                if(data.infoMessage!=null) {
                    showError(data.infoMessage,"message_warning");
                }
                
                // Handle erroneous data
                if( !checkCorrectData( data.returnData ) ) {
                    showError( ["Unfortunately the server returned data in a format that we did not expect."], "message_error" );
                    $( "#menu_go" ).find("img.spinner").hide();
                    return;
                }

                // Retrieve the datapoints from the json object
                var dataPoints = [];
                var series = [];

                var returnData = data.returnData;
                $.each(returnData.series, function(idx, element ) {
                	if( element.y && element.y.length > 0 ) {
	                    dataPoints[ dataPoints.length ] = element.y;
	                    series[ series.length ] = { "label": element.name };
                	}
                });

                // If no datapoints are found, return an error
                if( dataPoints.length == 0 ) {
                    showError( ["Unfortunately the server returned data without any measurements"], "message_error" );
                    $( "#menu_go" ).find("img.spinner").hide();
                    return;
                }
                
                var xlabel = returnData[ "xaxis" ].unit=="" ? returnData[ "xaxis" ].title : returnData[ "xaxis" ].title + " (" + returnData[ "xaxis" ].unit + ")";
                var ylabel = returnData[ "yaxis" ].unit=="" ? returnData[ "yaxis" ].title : returnData[ "yaxis" ].title + " (" + returnData[ "yaxis" ].unit + ")";

                // TODO: create a chart based on the data that is sent by the user and the type of chart
                // chosen by the user
                var plotOptions = null;
                
                switch( returnData.type ) {
                	case "horizontal_barchart":
                        plotOptions = {
                            // Tell the plot to stack the bars.
                            stackSeries: true,
                            seriesDefaults:{
                                renderer:$.jqplot.BarRenderer,
                                rendererOptions: {
                                    // Put a 30 pixel margin between bars.
                                    barMargin: 30,
                                    // Highlight bars when mouse button pressed.
                                    // Disables default highlighting on mouse over.
                                    highlightMouseDown: true,
                                    barDirection: 'horizontal'
                                }
                            },
                            highlighter: {
                                show: true,
                                sizeAdjust: 7.5,
                                tooltipAxes: "y"
                            },
                            series: series,
                            axes: {
                                xaxis: {
                                    //ticks: returnData.series[ 0 ].y,		// Use the x-axis of the first serie
                                    label: ylabel,
                                    formatString:'%.2f',
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer
                                },
                                yaxis: {
                                    renderer: $.jqplot.CategoryAxisRenderer,
                                    label: xlabel,
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer
                                }
                            }

                        };
                		break;
                	case "scatterplot":

                        series[0].showLine = false;
                        series[0].markerOptions = { "size": 7, "style":"filledCircle" };

                		plotOptions = {
                            stackSeries: true,
                            seriesDefaults:{
                                renderer:$.jqplot.LineRenderer
                            },
                            series: series,
                            highlighter: {
                                show: true,
                                sizeAdjust: 7.5,
                                tooltipAxes: "y"
                            },
                            axes: {
                                xaxis: {
                                    renderer: $.jqplot.CategoryAxisRenderer,
                                    ticks: returnData.series[ 0 ].x,	// Use the x-axis of the first serie
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                                    label: xlabel
                                },
                                yaxis: {
                                    label: ylabel,
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                                    formatString:'%.2f'
                                }
                            }
                        };
                	case "linechart":
                        plotOptions = {
                            stackSeries: true,
                            seriesDefaults:{
                                renderer:$.jqplot.LineRenderer
                            },
                            series: series,
                            highlighter: {
                                show: true,
                                sizeAdjust: 7.5,
                                tooltipAxes: "y"
                            },
                            axes: {
                                xaxis: {
                                    renderer: $.jqplot.CategoryAxisRenderer,
                                    ticks: returnData.series[ 0 ].x,	// Use the x-axis of the first serie
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                                    label: xlabel
                                },
                                yaxis: {
                                    label: ylabel,
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                                    formatString:'%.2f'
                                }
                            }
                        };                		
                		break;
                	case "barchart":
                        plotOptions = {
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
                                }
                            },
                            highlighter: {
                                show: true,
                                sizeAdjust: 7.5,
                                tooltipAxes: "y"
                            },
                            series: series,
                            axes: {
                                xaxis: {
                                    renderer: $.jqplot.CategoryAxisRenderer,
                                    ticks: returnData.series[ 0 ].x,		// Use the x-axis of the first serie
                                    label: xlabel,
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer
                                },
                                yaxis: {
                                    label: ylabel,
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                                    formatString:'%.2f'
                                }
                            }

                        };                		
                		break;
                	case "table":
                        // create table
                        var table = $("<table>").addClass("tablevis");

                        // create caption-row
                        var row = $("<tr>");
                        // create empty top-left-field
                        row.append("<td class='caption' colspan='2' rowspan='2'>&nbsp;</td>");
                        // create caption
                        row.append("<td class='caption' colspan='"+returnData.series[0].x.length+"'>"+xlabel+"</td>");
                        row.appendTo(table);

                        // create header-row
                        var row = $("<tr>");
                        // create headers
                        for(j=0; j<returnData.series[0].x.length; j++) {
                            row.append("<th>"+returnData.series[0].x[j]+"</th>");
                        }
                        row.appendTo(table);

                        // create data-rows
                        for(i=0; i<returnData.series[0].y.length; i++) {
                            var row = $("<tr>");
                            for(j=-1; j<returnData.series[0].x.length; j++) {
                                if(j<0) {
                                    if(i==0) {
                                        // create caption-column
                                        row.append("<td class='caption' rowspan='"+returnData.series[0].y.length+"'>"+ylabel+"</td>");
                                    }

                                    // create row-header
                                    row.append("<th>"+returnData.series[0].y[i]+"</th>");
                                } else {
                                    // row-data
                                    row.append("<td>"+returnData.series[0].data[j][i]+"</td>");
                                }
                            }
                            row.appendTo(table);
                        }

                        plotOptions = table;
                		break;
                }
                
                // If a chart has been created, show it
                if( plotOptions != null ) {
                    $( "#visualization" ).empty();
                    if(returnData.type=="table") {
                        $( "#visualization" ).html(plotOptions);
                    } else {
                        visualization = $.jqplot('visualization', dataPoints, plotOptions );
                    }
                    $( "#visualization" ).show();
                }

                $( "#menu_go" ).find("img.spinner").hide();
            }
        }, "menu_go");
    }
}

/**
 * Shows an error message in a proper way
 * @param messages	array of Strings
 * @param strClass  the Class the messages get
 */
function showError( messages, strClass ) {
    for (index in messages) {
        var newClose = $( "<div>" ).css("position","absolute").css("top","3px").css("right","10px").html("<a href='#' onclick='removeError(this); return false;'>x</a>");
	    $( '#message_container' ).prepend( $( "<div>" ).addClass("message_box "+strClass).html( messages[index] ).css("position","relative").fadeIn().append(newClose) );
    }
    $( '#message_counter' ).children(".topmenu_item_info").html($('.message_box').length);
}

function removeError(strSelector) {
    $( strSelector ).closest(".message_box").remove();
    $( '#message_counter' ).children(".topmenu_item_info").html($(".message_box").length);
    if($(".message_box").length==0) {
        $( '#message_counter' ).children(".formulier").toggle();
        openForm = null;
    }
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
		"xaxis": { "title": "quarter 2011", "unit": "" },
		"yaxis": { "title": "temperature", "unit": "degrees C" },
		"series": [
			{
				"name": "series name",
				"x": [ "Q1", "Q2", "Q3", "Q4" ],
				"y": [ 5.1, 3.1, 20.6, 15.4 ],
				"error": [ 0.5, 0.2, 0.4, 0.5 ]
			},
		]
	}
	*/

	return ( "type" in data && "xaxis" in data && "yaxis" in data && "series" in data && $.isArray( data.series ) );
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
			showError( ["An error occurred while retrieving variables from the server. Please try again or contact a system administrator.<br />"+textStatus], "message_error" );
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

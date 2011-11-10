/**
 * This variable holds the currently displayed visualization
 */
var visualization = null;
var visType = null;
var openForm = null;
var selectCache = new Array();
var selectVal = new Array();

jQuery.expr[':'].Contains = function(a, i, m) {
  return jQuery(a).text().toUpperCase().indexOf(m[3].toUpperCase()) >= 0;
};

$(document).ready(function() {

    toggleForm($("#menu_study"), "open");

    $(".topmenu_item").click(
        function(event) {
            if(this!=openForm) {
                toggleForm(this, "open");
            } else {
                toggleForm(this, "close");
            }
            return false;
		}
    );

    $(document).keyup(
        function(event) {
            if ( event.which == 27 ) {
                toggleForm(openForm, "close");
            }
        }
    );
    
    $(document).click(
        function() {
            toggleForm(openForm, "close");
        }
    );

    $(".formulier").click(function(event) {
        event.stopPropagation();
    });

    selectCache['study'] = $('#study').html();

    
});

/**
 * Retrieve new fields based on the study that the user has selected.
 */
function changeStudy() {

    toggleForm(openForm, "close");

    $( '#rows, #columns, #types, #visualization' ).empty();
    clearStep(".menu_item");

    if( visualization )
        visualization.destroy();

    if($( '#study' ).find( 'option:selected' ).length>0) {
        $( "#menu_row, #menu_column" ).find("img.spinner").show();
        $( "#menu_study" ).find(".topmenu_item_info").html($( '#study').find( 'option:selected' ).text());
        clearSearch("menu_column, #menu_row");

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
                    if(strOptions.length>0) {
                        strOptions += "</optgroup>";
                        $( "#rows, #columns" ).html(strOptions);
                        selectCache['rows'] = $('#rows').html();
                        selectCache['columns'] = $('#columns').html();
                    } else {
                        $("#visualization").html('<div style="padding: 30px">No fields could be found. This visualization prototype requires studies with samples.</div>');
                        selectCache['rows'] = null;
                        selectCache['columns'] = null;
                    }
	                
	                $( "#menu_study" ).find(".topmenu_item_info").html($( '#study').find( 'option:selected' ).text());
                    $( "#menu_row, #menu_column" ).find(".spinner").hide();
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

    clearStep("#"+divid);
    $( "#"+divid ).addClass("menu_item_done");

    if($( '#rows' ).find( 'option:selected' ).length>0 && $( '#columns' ).find( 'option:selected' ).length>0) {

        $( "#menu_vis" ).find(".spinner").show();

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
                        if( field.name==visType ) { $( '#types').find( 'option:last' ).attr("selected","selected"); };
                    });
                }

                if( $( '#types option' ).length>0 ) {
                    clearStep("#menu_vis");
                    $( "#menu_vis" ).addClass("menu_item_fill");
                    if( visualization )
                        visualization.destroy();
                    
                    if($( '#types' ).find( 'option' ).length==1) {
                        $( '#types :first-child' ).attr("selected","selected");
                    }
                    if($( '#types').find( 'option:selected' ).length>0) {
                        changeVis();
                    }
                }

                $( "#menu_vis" ).find(".spinner").hide();

            }
        },divid);
    }
}

/**
 *
 */
function changeVis() {

    if($( '#types' ).find( 'option:selected' ).length>0) {
        $( "#menu_vis" ).removeClass().addClass("menu_item menu_item_done");
        visType = $( '#types option:selected' ).text();
    } else {
        if( $( "#menu_row" ).hasClass("menu_item_done") && $( "#menu_column" ).hasClass("menu_item_done") ) {
            $( "#menu_vis" ).removeClass().addClass("menu_item menu_item_fill");
        }
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

    if($( "#menu_vis" ).hasClass("menu_item_done") &&
        $( "#menu_row" ).hasClass("menu_item_done") &&
        $( "#menu_column" ).hasClass("menu_item_done")
       ) {
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
                    $( "#menu_go" ).find(".spinner").hide();
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

                if($("#errorbars").attr("checked")=="checked" && returnData.series[ 0 ].error!=null) {
                    alert("Errorbars aren't implemented yet");
                }

                // If no datapoints are found, return an error
                if( dataPoints.length == 0 ) {
                    showError( ["Unfortunately the server returned data without any measurements"], "message_error" );
                    $( "#menu_go" ).find(".spinner").hide();
                    return;
                }
                
                var xlabel = returnData[ "xaxis" ].unit=="" ? returnData[ "xaxis" ].title : returnData[ "xaxis" ].title + " (" + returnData[ "xaxis" ].unit + ")";
                var ylabel = returnData[ "yaxis" ].unit=="" ? returnData[ "yaxis" ].title : returnData[ "yaxis" ].title + " (" + returnData[ "yaxis" ].unit + ")";

                // TODO: create a chart based on the data that is sent by the user and the type of chart
                // chosen by the user
                var plotOptions = null;

                var showDataValues = $("#showvalues").attr("checked")=="checked" ? true : false;
                var xangle = $("#anglelabels").attr("checked")=="checked" ? -45 : 0;

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
                                },
                                pointLabels: {show: showDataValues}
                            },
                            highlighter: {
                                show: !showDataValues,
                                sizeAdjust: 7.5,
                                tooltipAxes: "x"
                            },
                            series: series,
                            axes: {
                                xaxis: {
                                    label: ylabel,
                                    formatString:'%.2f',
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                                    tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                                    tickOptions: {
                                        angle: xangle
                                    },
                            		min: 0
                                },
                                yaxis: {
                                    renderer: $.jqplot.CategoryAxisRenderer,
                                    ticks: returnData.series[ 0 ].x,		// Use the x-axis of the first serie
                                    label: xlabel,
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer
                                }
                            },
                            axesDefaults: {
                                pad: 1.4
                            }

                        };
                		break;
                	case "scatterplot":

                        series[0].showLine = false;
                        series[0].markerOptions = { "size": 7, "style":"filledCircle" };

                		plotOptions = {
                            stackSeries: true,
                            seriesDefaults:{
                                renderer:$.jqplot.LineRenderer,
                                pointLabels: {show: showDataValues}
                            },
                            series: series,
                            highlighter: {
                                show: !showDataValues,
                                sizeAdjust: 7.5,
                                tooltipAxes: "y"
                            },
                            axes: {
                                xaxis: {
                                    renderer: $.jqplot.CategoryAxisRenderer,
                                    ticks: returnData.series[ 0 ].x,	// Use the x-axis of the first serie
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                                    label: xlabel,
                                    tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                                    tickOptions: {
                                        angle: xangle
                                    }
                                },
                                yaxis: {
                                    label: ylabel,
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                                    formatString:'%.2f'
                                }
                            },
                            axesDefaults: {
                                pad: 1.4
                            }
                        };
                	case "linechart":
                        plotOptions = {
                            stackSeries: true,
                            seriesDefaults:{
                                renderer:$.jqplot.LineRenderer,
                                pointLabels: {show: showDataValues}
                            },
                            series: series,
                            highlighter: {
                                show: !showDataValues,
                                sizeAdjust: 7.5,
                                tooltipAxes: "y"
                            },
                            axes: {
                                xaxis: {
                                    renderer: $.jqplot.CategoryAxisRenderer,
                                    ticks: returnData.series[ 0 ].x,	// Use the x-axis of the first serie
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                                    label: xlabel,
                                    tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                                    tickOptions: {
                                        angle: xangle
                                    }
                                },
                                yaxis: {
                                    label: ylabel,
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                                    formatString:'%.2f'
                                }
                            },
                            axesDefaults: {
                                pad: 1.4
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
                                },
                                pointLabels: {show: showDataValues}
                            },
                            highlighter: {
                                show: !showDataValues,
                                sizeAdjust: 7.5,
                                tooltipAxes: "y"
                            },
                            series: series,
                            axes: {
                                xaxis: {
                                    renderer: $.jqplot.CategoryAxisRenderer,
                                    ticks: returnData.series[ 0 ].x,		// Use the x-axis of the first serie
                                    label: xlabel,
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                                    tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                                    tickOptions: {
                                        angle: xangle
                                    }
                                },
                                yaxis: {
                                    label: ylabel,
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                                    formatString:'%.2f',
                                    min: 0
                                }
                            },
                            axesDefaults: {
                                pad: 1.4
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

                $( "#menu_go" ).find(".spinner").hide();
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
        toggleForm('#message_counter', 'close');
    }
}

function toggleForm(selector, action) {
    if( action=="close" || openForm !=null ) {
        $(openForm).children('.formulier').hide();
        $(openForm).removeClass("topmenu_item_selected");
        if($(openForm).attr('id')=='menu_study') {
            clearSearch('menu_study');
        }
        openForm = null;
    }
    if( action=="open" ) {
        $(selector).children('.formulier').show();
        $(selector).addClass("topmenu_item_selected");
        openForm = selector;
    }
}

function doSearch(menuId, selectId) {
    var searchVal = $('#'+menuId).find('.block_search').children('input').val();
    var currentVal = $('#'+selectId).find( 'option:selected' ).val();
    if(currentVal!==undefined) {
        selectVal[selectId] = currentVal;
    }
    if(selectVal[selectId]!=null) {
        currentVal = selectVal[selectId];
    }
    $('#'+selectId).html(selectCache[selectId]);
    $('#'+selectId).find('option[value="'+currentVal+'"]').attr('selected','selected');
    $('#'+selectId).find('option:not(:Contains("'+searchVal+'"))').remove();
}

function clearSearch(menuId, selectId) {
    
    $('#'+menuId).find('.block_search').children('input').val('');
    doSearch(menuId, selectId);
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
	return $( '#visualizationForm' ).serialize();
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
            $( "#"+divid ).removeClass().addClass('menu_item_error');
            if(divid!="menu_study") {
                $( "#"+divid ).addClass('menu_item');
            }
            $( "#"+divid ).find(".spinner").hide();
		}

		// Remove the error message
		delete ajaxParameters[ "errorMessage" ];
	}
	
	// Retrieve a new list of fields from the controller
	// based on the study we chose
	$.ajax($.extend({
		url: visualizationUrls[ action ],
        type: 'POST',
		data: data,
		dataType: "json"
	}, ajaxParameters ) );
}

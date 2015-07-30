/**
 * This variable holds the currently displayed visualization
 */
var visualization = null;
var currType = null;
var currAggr = null;

jQuery.expr[':'].Contains = function(a, i, m) {
  return jQuery(a).text().toUpperCase().indexOf(m[3].toUpperCase()) >= 0;
};

$(document).ready(function() {

    $( "#dialog_messages, #dialog_advanced_settings" ).dialog({
        autoOpen: false, // don't open on startup
        modal: true, // set dialogs as modal
        resizable: false, // don't allow resize
        show: "slide", // open effect is slide
        hide: "slide", // close effect is slide
        zIndex: 10002, // 1 higher than the login panel
        buttons: {
            Ok: function() {
                $( this ).dialog( "close" );
            }
        }
    });

    $( "#select_study" ).combobox();
    $( "#select_rows" ).combobox();
    $( "#select_columns" ).combobox();
    $( "#select_groups" ).combobox();

    $(".ui-autocomplete-input").click(function() {
        $( this ).blur();
        // pass empty string as value to search for, displaying all results
        $( this ).autocomplete( "search", "" );
        $( this ).focus();
    });

    $.jqplot.config.enablePlugins = true;

    var s1 = [2, -6, 7, -5];
    var ticks = ['a', 'b', 'c', 'd'];
    var combinedtest = [name='count', x=['a', 'b', 'c', 'd'], y=[2, -6, 7, -5]];
});

/**
 * Retrieve new fields based on the study that the user has selected.
 */
function changeStudy() {

    $( '#select_rows, #select_columns, #select_groups' ).empty();
    $( '#select_rows, #select_columns, #select_groups' ).next().val("");

    if( visualization )
        visualization.destroy();

    if($( '#select_study' ).find( 'option:selected' ).length>0) {
        $( "#select_rows, #select_columns, #select_groups" ).parents(".menu_header").find("img.spinner").show();

        executeAjaxCall( "getFields", 'menu_study', {
            "errorMessage": "An error occurred while retrieving variables from the server. Please try again or contact a system administrator.",
            "success": function( data, textStatus, jqXHR ) {

                if(data.infoMessage) {
                    showError(data.infoMessage,"message_warning");
                }

                // Add all fields to the lists
                if( data.returnData && data.returnData.studyIds==$( '#select_study option:selected' ).val() ) {
                    var returnData = data.returnData.fields;

                    var prevCat = "";
                    var strOptions = "<option value=''>Select One...</option>";
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
                        $( "#select_rows, #select_columns, #select_groups" ).html(strOptions);
                    } else {
                        $("#visualization").html('<div style="padding: 30px">No fields could be found. This visualization prototype requires studies with samples.</div>');
                    }
                    $( "#select_study" ).parents(".menu_header").find(".menu_header_count").switchClass("menu_fill", "menu_done", 1000);
                    $( "#select_rows, #select_columns, #select_groups" ).parents(".menu_header").find("img.spinner").hide();
	                $( "#select_rows, #select_columns, #select_groups" ).parents(".menu_header").find(".menu_header_count").addClass("menu_fill");
                }
            }
        });
    }
}

/**
 * Retrieve the possible visualization types based on the fields that the user has selected.
 */
function changeFields(selectid) {

    $( "#"+selectid ).parents(".menu_header").find(".menu_header_count").switchClass("menu_fill", "menu_done", 1000);

    if($( '#select_rows' ).find( 'option:selected' ).val().length>0 && $( '#select_columns' ).find( 'option:selected' ).val().length>0) {

        $( "#select_types, #select_aggregation" ).parents(".menu_header").find("img.spinner").show();

        executeAjaxCall( "getVisualizationTypes", selectid, {
            "errorMessage": "An error occurred while retrieving visualization types from the server. Please try again or contact a system administrator.",
            "success": function( data, textStatus, jqXHR ) {
                // Remove all previous entries from the list

                if( data.infoMessage!=null ) {
                    showError(data.infoMessage,"message_warning");
                }

                if( data.returnData && data.returnData.rowIds==$( '#select_rows option:selected' ).val() && data.returnData.columnIds==$( '#select_columns option:selected' ).val() ) {
                    // Add all fields to the lists
                    var returnDataTypes = data.returnData.types;
                    var returnDataAggregation = data.returnData.aggregations;

                    if(currAggr==null) {
                        currAggr = "average";
                    } else {
                        currAggr = $("#select_aggregation input:checked").val();
                    }
                    currType = $("#select_types input:checked").val();

                    $( "#select_types, #select_aggregation" )
                        .parents(".menu_header")
                        .find(".menu_header_count")
                        .addClass("menu_fill");

                    // Disable all aggregation- and visualizationoptions
                    $("#select_aggregation input, #select_types input").each(function(index) {
                        $(this).attr("disabled","disabled");
                    });

                    // Enable some visualizationoptions
                    $.each( returnDataTypes, function( idx, field ) {
                        $("#vis_"+field.id).attr("disabled",false);
                        if( field.id==currType || returnDataTypes.length==1 ) {
                            currType = field.id;
                            $("#vis_"+field.id).attr("checked","checked");
                            $( "#select_types" )
                                .parents(".menu_header")
                                .find(".menu_header_count")
                                .switchClass("menu_fill", "menu_done", 1000);
                        };
                    });

                    // Enable some aggregationoptions
                    $.each( returnDataAggregation, function( idx, field ) {
                        if(!field.disabled)
                            $("#aggr_"+field.id).attr("disabled",false);
                            if( field.id==currAggr || returnDataAggregation.length==1 ) {
                                currAggr = field.id;
                                $("#aggr_"+field.id).attr("checked","checked");
                                $( "#select_aggregation" )
                                    .parents(".menu_header")
                                    .find(".menu_header_count")
                                    .switchClass("menu_fill", "menu_done", 1000);
                            };
                    });

                    changeVis();
                }

                $( "#select_types, #select_aggregation" ).parents(".menu_header").find("img.spinner").hide();

            }
        });
    }
}

/**
 *
 */
function changeRadio(that) {

    if($(that).attr("name")=="aggregation") {
        $( "#select_aggregation" )
            .parents(".menu_header")
            .find(".menu_header_count")
            .switchClass("menu_fill", "menu_done", 1000);
        currAggr = $(that).val();

        if(currAggr!="none") {
            $("#vis_boxplot").attr("disabled","disabled");
            if(currType=="boxplot") {
                $("#vis_boxplot").attr("checked",false);
            }
        } else {
            $("#vis_boxplot").attr("disabled",false);
        }
    } else {
        $( "#select_types" )
            .parents(".menu_header")
            .find(".menu_header_count")
            .switchClass("menu_fill", "menu_done", 1000);
        currType = $(that).val();

        if($(that).val()=="boxplot") {
            $( "#select_aggregation input[value=none]" ).attr("checked","checked");
            $( "#select_aggregation" )
                .parents(".menu_header")
                .find(".menu_header_count")
                .switchClass("menu_fill", "menu_done", 1000);
            currAggr = "none";
        }
    }

    changeVis();

}

function changeVis() {
    if($("#autovis").attr("checked")=="checked") {
        visualize();
    }
}


/**
 * Create a visualization based on the parameters entered in the form
 * The data for the visualization is retrieved from the serverside getData method
 */ 
function visualize() {

    if($( "#select_rows" ).val() &&
        $( "#select_columns" ).val() &&
        $( "#select_types input:checked" ).length>0 &&
        $( "#select_aggregation input:checked" ).length>0
       ) {

        $( "#menu_go" ).find(".spinner").show();

        executeAjaxCall( "getData", "menu_go", {
            "errorMessage": "An error occurred while retrieving data from the server. Please try again or contact a system administrator.",
            "success": function( data, textStatus, jqXHR ) {
                // Remove old chart, if available
                if( visualization )
                    visualization.destroy();

                if(data.infoMessage!=null) {
                    showError(data.infoMessage,"message_warning");
                }

                // Handle erroneous data
                if( !checkCorrectData( data.returnData ) && data.returnData.type!="boxplot" ) {
                    showError( ["Unfortunately the server returned data in a format that we did not expect."], "message_error" );
                    return;
                }

                // Retrieve the datapoints from the json object
                var dataPoints = new Array();
                var series = [];
                
                //data = {"returnData":{"type":null,"xaxis":{"title":"Gender","unit":"","type":"categorical"},"yaxis":{"title":"Weight","unit":"kg","type":"numerical"},"groupaxis":{"title":null,"unit":null,"type":"numerical"},"series":[{"name":"count","x":["Male","Male","Female","Male","Male","Male","Male","Male","Female","Female","Female"],"y":[1,2,3,4,2,2,3,1,2,3,5]}]}}

                var returnData = data.returnData;

                $.each(returnData.series, function(idx, element ) {
                	if( element.y && element.y.length > 0 ) {
                        if(returnData.type=="horizontal_barchart") {
                            // The horizontal barchart needs special dataPoints
                            var newArr = new Array();
                            for(var i=0; i<element.y.length; i++) {
                                newArr[ newArr.length ] = new Array(element.y[i],i+1);
                            }
                            dataPoints[ dataPoints.length ] = newArr;
                        } else if(returnData.type=="boxplot")  {
                            var tempArr = element.y;
                            tempArr = [tempArr[0],tempArr[4],tempArr[1],tempArr[2],tempArr[3],tempArr[4],tempArr[5],tempArr[6],tempArr[7]];
                            dataPoints[ dataPoints.length ] = tempArr;
                        } else {
                            dataPoints[ dataPoints.length ] = element.y;
                        }
	                    series[ series.length ] = { "label": element.name };
                	}
                });

                // If no datapoints are found, return an error
                if( dataPoints.length == 0 ) {
                    showError( ["Unfortunately the server returned data without any measurements"], "message_error" );
                    $( "#menu_go" ).find(".spinner").hide();
                    return;
                }
                
                var xlabel = returnData[ "xaxis" ].unit=="" ? returnData[ "xaxis" ].title : returnData[ "xaxis" ].title + " (" + returnData[ "xaxis" ].unit + ")";
                var ylabel = returnData[ "yaxis" ].unit=="" ? returnData[ "yaxis" ].title : returnData[ "yaxis" ].title + " (" + returnData[ "yaxis" ].unit + ")";
                var grouplabel = "";
                if(returnData[ "groupaxis" ]!==undefined) {
                    grouplabel = returnData[ "groupaxis" ].unit=="" ? returnData[ "groupaxis" ].title : returnData[ "groupaxis" ].title + " (" + returnData[ "groupaxis" ].unit + ")";
                }

                // TODO: create a chart based on the data that is sent by the user and the type of chart
                // chosen by the user
                var plotOptions = null;

                var blnShowDataValues = $("#showvalues").attr("checked")=="checked";
                var blnShowLegend = returnData.series.length>1;
                var strLegendPlacement = $("#legendplacement").attr("checked")=="checked" && blnShowLegend ? "outsideGrid" : "insideGrid";
                var xangle = $("#anglelabels").attr("checked")=="checked" ? -45 : 0;

                switch( returnData.type ) {
                	case "scatterplot":

                        $.each(returnData.series, function(idx, element ) {
                            series[idx].showLine = false;
                            series[idx].markerOptions = { "size": 7, "style":"filledCircle" };
                        });
                        // No break, scatterplot gets the plotoptions of the linechart
                	case "linechart":
                        plotOptions = {
                            stackSeries: false,
                            seriesDefaults:{
                                renderer:$.jqplot.LineRenderer,
                                pointLabels: {show: blnShowDataValues}
                            },
                            series: series,
                            highlighter: {
                                show: !blnShowDataValues,
                                sizeAdjust: 7.5,
                                tooltipAxes: "y"
                            },
                            legend: {
                                show: blnShowLegend,
                                placement: strLegendPlacement
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
                    case "horizontal_barchart":
                        plotOptions = {
                            // Tell the plot to stack the bars.
                            stackSeries: false,
                            seriesDefaults:{
                                renderer:$.jqplot.BarRenderer,
                                rendererOptions: {
                                    // Put a 30 pixel margin between bars.
                                    barMargin: 30,
                                    // Highlight bars when mouse button pressed.
                                    // Disables default highlighting on mouse over.
                                    highlightMouseDown: true,
                                    barDirection: 'horizontal',
                                    highlightMouseDown: true,
                                    fillToZero: true
                                },
                                pointLabels: {show: blnShowDataValues}
                            },
                            highlighter: {
                                show: !blnShowDataValues,
                                sizeAdjust: 7.5,
                                tooltipAxes: "x"
                            },
                            legend: {
                                show: blnShowLegend,
                                placement: strLegendPlacement
                            },
                            series: series,
                            axes: {
                                xaxis: {
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                                    label: xlabel,
                                    
                                    formatString:'%.2f',
                                    min: 0,

                                    tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                                    tickOptions: {
                                        angle: xangle
                                    }

                                },
                                yaxis: {
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                                    label: ylabel,

                                    tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                                    ticks: returnData.series[ 0 ].x,		// Use the x-axis of the first serie

                                    renderer: $.jqplot.CategoryAxisRenderer
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
                            stackSeries: false,
                            seriesDefaults:{
                                renderer:$.jqplot.BarRenderer,
                                rendererOptions: {
                                    barMargin: 30,
                                    highlightMouseDown: true,
                                    fillToZero: true
                                },
                                pointLabels: {show: blnShowDataValues}
                            },
                            highlighter: {
                                show: !blnShowDataValues,
                                sizeAdjust: 7.5,
                                tooltipAxes: "y"
                            },
                            legend: {
                                show: blnShowLegend,
                                placement: strLegendPlacement
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
                                    formatString:'%.2f'
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
                        if(series.length==1) {
                            row.append("<td class='caption' colspan='2' rowspan='2'>&nbsp;</td>");
                        } else {
                            row.append("<td class='caption' colspan='2'>&nbsp;</td>");
                        }
                        // create caption
                        row.append("<td class='caption' colspan='"+returnData.series[0].x.length+"'>"+xlabel+"</td>");
                        row.appendTo(table);

                        // create header-row
                        var row = $("<tr>");
                        // create headers
                        if(series.length>1) {
                            row.append("<td class='caption'>"+grouplabel+"</td>");
                            row.append("<td class='caption'>"+ylabel+"</td>");
                        }
                        for(j=0; j<returnData.series[0].x.length; j++) {
                            row.append("<th>"+returnData.series[0].x[j]+"</th>");
                        }
                        row.appendTo(table);

                        for(k=0; k<returnData.series.length; k++) {
                        // create data-rows
                            for(i=0; i<returnData.series[0].y.length; i++) {
                                var row = $("<tr>");
                                for(j=-1; j<returnData.series[0].x.length; j++) {
                                    if(j<0) {
                                        if(i==0) {
                                            if(series.length==1) {
                                            // create caption-column
                                                row.append("<td class='caption' rowspan='"+returnData.series[0].y.length+"'>"+ylabel+"</td>");
                                            } else {
                                                row.append("<td class='caption' rowspan='"+returnData.series[0].y.length+"'>"+returnData.series[k].name+"</td>");
                                            }
                                        }

                                        // create row-header
                                        row.append("<th>"+returnData.series[k].y[i]+"</th>");
                                    } else {
                                        // row-data
                                        row.append("<td>"+returnData.series[k].data[j][i]+"</td>");
                                    }
                                }
                                row.appendTo(table);
                            }
                        }

                        plotOptions = table;
                		break;
                    case "boxplot":
                        /* code for canvasXpress
                        var arrSmps = new Array();
                        for(i=0; i<returnData.series[0].x.length; i++) {
                            arrSmps[i] = "Smp"+i;
                        }
                        plotOptions = {
                              x: {xdata: returnData.series[0].x},
                              y: {vars:  ['blabla'],
                                   smps:  arrSmps,
                                   desc:  [ylabel],
                                   data:  [returnData.series[0].y]
                                 }
                        };
                        var plotOptions2 = {
                            graphType: 'Boxplot',
                            graphOrientation: 'vertical',
                            showDataValues: true,
                            //title: 'Boxplots',
                            //maxTextSize: 5,
                            colorScheme: 'basic',
                            blockFactor: 1.5,
                            smpLabelRotate: -xangle,
                            legendBackgroundColor: false,
                            blockSeparationFactor: 2,
                            //showLegend: blnShowLegend
                            showLegend: false
                        };*/
                        dataPoints = [dataPoints];

                        plotOptions = {
                            series: [{
                                renderer: $.jqplot.BoxplotRenderer,
                                rendererOptions: {

                                }
                            }]/*,
                            seriesDefaults:{
                                pointLabels: {show: blnShowDataValues}
                            }*/,
                            highlighter: {
                                show: true,
                                sizeAdjust: 7.5,
                                showMarker: true,
                                tooltipAxes: 'y',
                                yvalues: 8,
                                formatString: '<table class="jqplot-highlighter dummy%s">' +
                                              '<tr><td>Maximum:</td><td>%s</td></tr>' +
                                              '<tr><td>Median + 1.5*IQR:</td><td>%s</td></tr>' +
                                              '<tr><td>Q3:</td><td>%s</td></tr>' +
                                              '<tr><td>Median:</td><td>%s</td></tr>' +
                                              '<tr><td>Q1:</td><td>%s</td></tr>' +
                                              '<tr><td>Median - 1.5*IQR:</td><td>%s</td></tr>' +
                                              '<tr><td>Minimum:</td><td>%s</td></tr>' +
                                              '</table>'
                            },
                            axesDefaults: {
                                pad: 1.4
                            },
                            axes: {
                                xaxis: {
                                    renderer: $.jqplot.CategoryAxisRenderer,
                                    label: xlabel,
                                    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                                    tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                                    tickOptions: {
                                        angle: xangle
                                    }
                                },
                                yaxis: {
                                    min: 0
                                }
                            }
                        };
                        break;
                }
                
                // If a chart has been created, show it
                if( plotOptions != null ) {

                    $( "#visualization" ).css("width",$( "#visualization_container" ).innerWidth()-2);
                    $( "#visualization" ).css("height",$( "#visualization_container" ).innerHeight()-2);

                    $( "#visualization" ).empty();
                    /*if(bx!==undefined) {
                        bx.destroy();
                    }*/
                    
                    if(returnData.type=="table") {
                        $( "#visualization" ).html(plotOptions);
                    } /* else if(returnData.type=="boxplot") {
                        $( "#visualization" ).html('<canvas id="boxplotcanvas"></canvas>');
                        $( "#boxplotcanvas" ).css("width",$( "#visualization" ).innerWidth()-2);
                        $( "#boxplotcanvas" ).css("height",$( "#visualization" ).innerHeight()-2);
                        var bx = new CanvasXpress('boxplotcanvas',plotOptions, plotOptions2);
                        bx.groupSamples(['xdata'], 'iqr');
                        bx.draw();
                    } */ else  {
                        // console.log(dataPoints);
                        visualization = $.jqplot('visualization', dataPoints, plotOptions );
                    }
                    $( "#visualization" ).show();
                }

                $( "#menu_go" ).find(".spinner").hide();
            }
        });
    }
}

/**
 * Shows an error message in a proper way
 * @param messages	array of Strings
 * @param strClass  the Class the messages get
 */
function showError( messages, strClass ) {
	// Add the message to the container
    for (index in messages) {
        var newClose = $( "<div>" ).css("position","absolute").css("top","3px").css("right","10px").html("<a href='#' onclick='removeError(this); return false;'>x</a>");
	    $( '#message_container' ).prepend( $( "<div>" ).addClass("message_box "+strClass).html( messages[index] ).css("position","relative").fadeIn().append(newClose) );
    }
    
    // Update the link to the messages
    var newMessage = "&nbsp;" + $('.message_box').length + " message";
    
    if($('.message_box').length!=1) {
        newMessage = newMessage + "s";
    }
    
    // Determine highlight color
    switch( strClass ) {
    	case "message_error": highlightColor = "#FDA5A5"; break;
    	case "message_warning": highlightColor = "#FDF9A5"; break;
    	default: highlightColor = "#A5DCFD"; break;
    }
    
    $( '#messages_link' ).html(newMessage).hide().show( 
    	"highlight", 
    	{ color: highlightColor }, 
    	1000 );
}

function removeError(strSelector) {
    $( strSelector ).closest(".message_box").remove();
    var newMessage = "&nbsp;" + $('.message_box').length + " message";
    if($('.message_box').length!=1) {
        newMessage = newMessage + "s";
    }
    $( '#messages_link' ).html(newMessage);
    if($(".message_box").length==0) {
        $('#dialog_messages').dialog('close');
    }
}

function clearSelect(that, stepNr) {

    var block = $(that).parents(".menu_header").find(".block_variable");
    $(block).children("input, select").val("");
    $(block).children("input").data( "ui-autocomplete" ).term = "";

    if(stepNr==1) {
        $(that).parents(".menu_item").children(".menu_header").each(function(index) {

            if($(this).find("select").val()!=null && $(this).find("select").val()!="") {
                clearSelect($(this).find("select"),0);
                $(this).find("select").empty();
                $(this).find("input").val("");
                $(this).children(".menu_header_count").removeClass().addClass("menu_header_count");
            }
            
        });
    }
    if(stepNr>=1) {
        $(that).parents(".menu_header").children(".menu_header_count").removeClass().addClass("menu_header_count menu_fill");
        visualize();
    }
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
function executeAjaxCall( action, selectid, ajaxParameters ) {
	var data = gatherData( action );

	// If no parameters are given, create an empty map
	if( !ajaxParameters ) 
		ajaxParameters = {}

	if( ajaxParameters[ "errorMessage" ] ) {
		var message = ajaxParameters[ "errorMessage" ];
		ajaxParameters[ "error" ] = function( jqXHR, textStatus, errorThrown ) {
			// An error occurred while retrieving fields from the server
			showError( ["An error occurred while retrieving variables from the server. Please try again or contact a system administrator.<br />"+textStatus], "message_error" );
            $( "#"+selectid ).parents(".menu_header").find(".menu_header_count").switchClass("menu_fill", "menu_error", 1000);
            $( "#"+selectid ).parents(".menu_header").find("img.spinner").hide();
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

/*
 * Create autocomplete with a select
 * From : http://jqueryui.com/demos/autocomplete/#combobox
 */
(function( $ ) {
    $.widget( "ui.combobox", $.ui.autocomplete, {
        _create: function() {
            var self = this,
                select = this.element.hide(),
                selected = select.children( ":selected" ),
                value = selected.val() ? selected.text() : "";
            var input = this.input = $( "<input>" )
                .insertAfter( select )
                .val( value )
                .autocomplete({
                    delay: 0,
                    minLength: 0,
                    source: function( request, response ) {
                        var matcher = new RegExp( $.ui.autocomplete.escapeRegex(request.term), "i" );
                        var currCat = "";
                        response( select.find( "option, optgroup" ).map(function() {
                            if(this.nodeName=="OPTION") {
                                var text = $( this ).text();
                                if ( this.value && (( !request.term || matcher.test(text) ) || matcher.test(currCat)) ) {
                                    return {
                                        category: currCat,
                                        label: text.replace(
                                            new RegExp(
                                                "(?![^&;]+;)(?!<[^<>]*)(" +
                                                $.ui.autocomplete.escapeRegex(request.term) +
                                                ")(?![^<>]*>)(?![^&;]+;)", "gi"
                                            ), "$1" ),
                                        value: text,
                                        option: this
                                    };
                                }
                            } else {
                                currCat = $( this ).attr("label");
                            }
                        }) );
                    },
                    select: function( event, ui ) {
                        ui.item.option.selected = true;
                        self._trigger( "selected", event, {
                            item: ui.item.option
                        });
                        select.trigger("change");
                    },
                    change: function( event, ui ) {
                        if ( !ui.item ) {
                            var matcher = new RegExp( "^" + $.ui.autocomplete.escapeRegex( $(this).val() ) + "$", "i" ),
                                valid = false;
                            select.children( "option" ).each(function() {
                                if ( $( this ).text().match( matcher ) ) {
                                    this.selected = valid = true;
                                    return false;
                                }
                            });
                            if ( !valid ) {
                                // remove invalid value, as it didn't match anything
                                $( this ).val( "" );
                                select.val( "" );
                                input.data( "ui-autocomplete" ).term = "";
                                return false;
                            }
                        } 
                    }
                });

            input.data( "ui-autocomplete" )._renderMenu = function( ul, items ) {
                var self = this, currentCategory = "";
                $.each( items, function( index, item ) {
                    if ( item.category != currentCategory ) {
                        ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
                        currentCategory = item.category;
                    }
                    self._renderItem( ul, item );
                });
            };

            input.data( "ui-autocomplete" )._renderItem = function( ul, item ) {
                return $( "<li></li>" )
                    .data( "ui-autocomplete-item", item )
                    .append( "<a>" + item.label + "</a>" )
                    .appendTo( ul );
            };
        },

        destroy: function() {
            this.input.remove();
            this.element.show();
            $.Widget.prototype.destroy.call( this );
        }
    });
})( jQuery );

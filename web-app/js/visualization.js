var Visualization = {
	initialize: function() {
		// Initialize dialogs
		Visualization.dialogs.initialize();
		
		// Initialize comboboxes
	    $( "#select_study" ).combobox().on("change", Visualization.handlers.study );
	    $( "#select_rows" ).combobox().on("change", Visualization.handlers.field );
	    $( "#select_columns" ).combobox().on("change", Visualization.handlers.field );
	    $( "#select_groups" ).combobox().on("change", Visualization.handlers.field );

	    // Initialize handlers
	    $( "input[name=aggregation]" ).on( "click", Visualization.handlers.aggregation );
	    $( "input[name=types]" ).on( "click", Visualization.handlers.type );
	    
	    // Enable visualize and dialog buttons
	    $( "#button_visualize" ).on( "click", function() { Visualization.visualize.perform(); return false; } );
	    
	    $( "#button_advanced_settings" ).on( "click", function() { Visualization.dialogs.openAdvancedSettings(); return false; } );
	    $( "#button_messages" ).on( "click", function() { Visualization.dialogs.openMessages(); return false; } );

	    // Do auto visualization when changing settings
	    $( "#dialog_advanced_settings [type=checkbox]" ).on( "click", Visualization.visualization.auto );
	    
	    $(".ui-autocomplete-input").click(function() {
	        $( this ).blur();
	        // pass empty string as value to search for, displaying all results
	        $( this ).autocomplete( "search", "" );
	        $( this ).focus();
	    });
		
	    // Make sure to enable plugins for jqplot
	    $.jqplot.config.enablePlugins = true;
	},
	
	dialogs: {
		initialize: function() {
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
		},
		
		openMessages: function() {
			$('#dialog_messages').dialog('open'); 
		},
		
		openAdvancedSettings: function() {
			$('#dialog_advanced_settings').dialog('open'); 
		}
		
	},
	
	/**
	 * Methods to handle messages for the user
	 */
	messages: {
		/**
		 * Show a new message
		 */
		show: function( messages, strClass ) {
			// Add the message to the container
		    for (index in messages) {
		        var newClose = $( "<div>" ).css("position","absolute").css("top","3px").css("right","10px").html("<a href='#' onclick='Visualization.messages.remove(this); return false;'>x</a>");
			    $( '#message_container' ).prepend( $( "<div>" ).addClass("message_box "+strClass).html( messages[index] ).css("position","relative").fadeIn().append(newClose) );
		    }
		    
		    // Update the link to the messages
		    this.updateMessageCount();
		    
		    // Highlight the message count
		    switch( strClass ) {
		    	case "message_error": highlightColor = "#FDA5A5"; break;
		    	case "message_warning": highlightColor = "#FDF9A5"; break;
		    	default: highlightColor = "#A5DCFD"; break;
		    }
		    this.highlightMessageCount(highlightColor);
		},
		
		// Remove a single message
		remove: function(strSelector) {
		    $( strSelector ).closest(".message_box").remove();
		    
		    this.updateMessageCount();
		    
		    if($(".message_box").length==0) {
		        $('#dialog_messages').dialog('close');
		    }			
		},
		
		/**
		 * Update the message count to reflect the actual number of messages
		 */
		updateMessageCount: function() {
		    var newMessage = "&nbsp;" + $('.message_box').length + " message";
		    if($('.message_box').length!=1) {
		        newMessage = newMessage + "s";
		    }
		    $( '#messages_link' ).html(newMessage);
		},
		
		/**
		 * Highlights the message count to indicate something has changed
		 */
		highlightMessageCount: function(color) {
		    $( '#messages_link' ).hide().show( 
			    	"highlight", 
			    	{ color: color }, 
			    	1000 );
		},
		
		/**
		 * Adds field indications (red, green or blue)
		 */
		indicate: {
			getElement: function(selector) {
				return $(selector).parents(".menu_header").find(".menu_header_count");
			},
			error: function(selector) {
                this.getElement(selector).switchClass("menu_fill", "menu_error", 1000);
			},
			ready: function(selector) {
                this.getElement(selector).removeClass().addClass( "menu_header_count").addClass("menu_fill");
			},
			done: function(selector) {
				this.getElement(selector).switchClass("menu_fill", "menu_done", 1000);				
			}
		}
	},
	
	/**
	 * Methods to show/hide a spinner
	 */
	spinner: {
		show: function(selector) {
            $(selector).parents(".menu_header").find("img.spinner").show();
		},
		hide: function(selector) {
            $(selector).parents(".menu_header").find("img.spinner").hide();
		}
	},
	
	/**
	 * Information and methods for the current status
	 */
	current: {
		visualization: null,
		type: null,
		aggregation: null,
		
		// Create a chart based on the given parameters
		draw: function( dataPoints, plotOptions ) {
            this.visualization = $.jqplot('visualization', dataPoints, plotOptions );
		},
		
		// Show HTML in the visualization pane
		showHTML: function( html ) {
			this.clearVisualization();
			$( '#visualization' ).html(html);
		},

		// Remove the chart, if it exists
		clearVisualization: function() {
		    if( this.visualization )
		    	this.visualization.destroy();
		},
	},
	
	/**
	 * Event handles to be used when something changes in the interface. Please note,
	 * 'this' will refer to the object to which the handler is attached, instead of to
	 * the current object.
	 */
	handlers: {
		study: function() {
			// Clear all select boxes for fields
		    $( '#select_rows, #select_columns, #select_groups' ).empty();
		    $( '#select_rows, #select_columns, #select_groups' ).next().val("");

		    // Clear visualization
		    Visualization.current.clearVisualization();

		    // Update fields
		    if($( '#select_study' ).find( 'option:selected' ).length>0) {
		    	Visualization.update.fields();
		    }			
		},
		
		field: function() {
			Visualization.messages.indicate.done(this);

			// If both the rows and columns have been selected, update aggregation and visualization types
		    if($( '#select_rows' ).val().length>0 && $( '#select_columns' ).val().length>0) {
		    	Visualization.update.types(this);
		    }			
		},
		
		aggregation: function() {
			Visualization.messages.indicate.done(this);
	        Visualization.current.aggregation = $(this).val();
	
	        // Boxplot is only allowed for no aggregation
	        if(Visualization.current.aggregation != "none") {
	            $("#vis_boxplot").attr("disabled","disabled");
	            
	            // If boxplot was chosen, reset the type
	            if(Visualization.current.type=="boxplot") {
	                $("#vis_boxplot").attr("checked",false);
	    			Visualization.messages.indicate.ready($("#vis_boxplot"));
	    	        Visualization.current.type = null;
	            }
	        } else {
	            $("#vis_boxplot").attr("disabled",false);
	        }
	        
            // If we already have enough data to visualize, and the automatic visualization is on,
            // start the visualization already
            Visualization.visualization.auto();
		},
		type: function() {
			Visualization.messages.indicate.done(this);
	        Visualization.current.type = $(this).val();

	        if(Visualization.current.type == "boxplot") {
	            $( "#aggr_none" )
	            	.attr("checked","checked")
	            	.trigger("click");
	        } else {
                // If we already have enough data to visualize, and the automatic visualization is on,
                // start the visualization already
                Visualization.visualization.auto();
	        }			
		}
	},
	
	update: {
		/**
		 * Gathers data for the given request type from the form elements on the page
		 * @param type	String	Can be 'getStudies', 'getFields', 'getVisualizationType' or 'getData'
		 * @return Object		Object with the data to be sent to the server
		 */
		gatherData: function( type ) {
			// For simplicity, we send the whole form to the server. In the
			// future this might be enhanced, based on the given type
			return $( '#visualizationForm' ).serialize();
		},
		
		/**
		 * Checks whether the data in the getData call can be handled correctly
		 * @param	JSON object to check
		 * @return	boolean	True if the data is correctly formatted, false otherwise
		 */
		checkCorrectData: function( data ) {
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
		},
	
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
		executeAjaxCall: function( action, ajaxParameters ) {
			var data = this.gatherData( action );

			// If no parameters are given, create an empty map
			if( !ajaxParameters ) 
				ajaxParameters = {}

			// Retrieve a new list of fields from the controller
			// based on the study we chose
			return $.ajax($.extend({
				url: visualizationUrls[ action ],
		        type: 'POST',
				data: data,
				dataType: "json"
			}, ajaxParameters ) );
		},
		
		/**
		 * Retrieve a list of fields from the server and put them into the select boxes for rows, columns and groups
		 */
		fields: function() {
			var spinnerSelector = "#select_rows, #select_columns, #select_groups";
	    	Visualization.spinner.show( spinnerSelector );
			
	        this.executeAjaxCall( "getFields" )
	        	.done(function( data, textStatus, jqXHR ) {
	                if(data.infoMessage) {
	                    Visualization.messages.show(data.infoMessage,"message_warning");
	                }

	                // Add all fields to the lists
	                if( data.returnData && data.returnData.studyIds==$( '#select_study' ).val() ) {
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
		                	Visualization.messages.indicate.ready( spinnerSelector );
	                        
	                    } else {
	                    	Visualization.current.showHTML('<div style="padding: 30px">No fields could be found. This visualization step requires studies with samples.</div>');
		                	Visualization.messages.indicate.error( spinnerSelector );
	                    }
	                    
	                    Visualization.messages.indicate.done( '#select_study' );
	                }
	            })
	        	.fail(function( jqXHR, textStatus, errorThrown ) {
					// An error occurred while retrieving fields from the server
					Visualization.messages.show( "An error occurred while retrieving variables from the server. Please try again or contact a system administrator<br />" + textStatus, "message_error" );
					Visualization.messages.indicate.error( '#select_study' );
				})
	        	.always(function() {
					Visualization.spinner.hide( spinnerSelector );
	        	});
		},
		
		/**
		 * Retrieve a list of types from the server, based on the chosen fields
		 */		
		types: function(originatingElement) {
			var spinnerSelector = "#select_types, #select_aggregation";
	    	Visualization.spinner.show( spinnerSelector );
			
	        this.executeAjaxCall( "getVisualizationTypes" )
	        	.done(function( data, textStatus, jqXHR ) {
	                if( data.infoMessage!=null ) {
	                    Visualization.messages.show(data.infoMessage,"message_warning");
	                }

	                if( data.returnData && data.returnData.rowIds==$( '#select_rows' ).val() && data.returnData.columnIds==$( '#select_columns' ).val() ) {
	                    // Add all fields to the lists
	                    var returnDataTypes = data.returnData.types;
	                    var returnDataAggregation = data.returnData.aggregations;

	                    // Store current aggregation and type
	                    if(Visualization.current.aggregation==null) {
	                        Visualization.current.aggregation = "average";
	                    } else {
	                        Visualization.current.aggregation = $("#select_aggregation input:checked").val();
	                    }
	                    Visualization.current.type = $("#select_types input:checked").val();

	                    // Indicate the types and aggreation and ready
	                    Visualization.messages.indicate.ready( "#select_types, #select_aggregation" );

	                    // Disable all aggregation- and visualizationoptions
	                    $("#select_aggregation input, #select_types input").each(function(index) {
	                        $(this).attr("disabled","disabled");
	                    });

	                    // Enable some visualizationoptions
	                    $.each( returnDataTypes, function( idx, field ) {
	                        $("#vis_"+field.id).attr("disabled",false);
	                        if( field.id==Visualization.current.type || returnDataTypes.length==1 ) {
	                            Visualization.current.type = field.id;
	                            $("#vis_"+field.id).attr("checked","checked");
	                            
	    	                    Visualization.messages.indicate.done( "#select_types" );
	                        };
	                    });

	                    // Enable some aggregationoptions
	                    $.each( returnDataAggregation, function( idx, field ) {
	                        if(!field.disabled)
	                            $("#aggr_"+field.id).attr("disabled",false);
	                            if( field.id==Visualization.current.aggregation || returnDataAggregation.length==1 ) {
	                                Visualization.current.aggregation = field.id;
	                                $("#aggr_"+field.id).attr("checked","checked");
	                                
		    	                    Visualization.messages.indicate.done( "#select_aggregation" );
	                            };
	                    });

	                    // If we already have enough data to visualize, and the automatic visualization is on,
	                    // start the visualization already
	                    Visualization.visualization.auto();
	                }	        		
	            })
	        	.fail(function( jqXHR, textStatus, errorThrown ) {
					// An error occurred while retrieving fields from the server
					Visualization.messages.show( "An error occurred while retrieving visualization types from the server. Please try again or contact a system administrator<br />" + textStatus, "message_error" );
					Visualization.messages.indicate.error( originatingElement );
				})
	        	.always(function() {
					Visualization.spinner.hide( spinnerSelector );
	        	});	        
		},
	},
	
	types: {
		barchart: {},
		horizontal_barchart: {},
		linechart: {},
		table: {},
		scatterplot: {},
		boxplot: {},
	},
	
	visualization: {
		auto: function() {
		    if($("#autovis").attr("checked")=="checked") {
		        this.perform();
		    }			
		},
		
		/**
		 * Create a visualization based on the parameters entered in the form
		 * The data for the visualization is retrieved from the serverside getData method
		 */ 
		perform: function() {

		    if($( "#select_rows" ).val() &&
		        $( "#select_columns" ).val() &&
		        $( "#select_types input:checked" ).length>0 &&
		        $( "#select_aggregation input:checked" ).length>0
		       ) {

		        $( "#menu_go" ).find(".spinner").show();

		        Visualization.update.executeAjaxCall( "getData" )
		        	.done(function(data, textStatus, jqXHR) {
		                // Remove old chart, if available
		            	Visualization.current.clearVisualization();

		                if(data.infoMessage!=null) {
		                    Visualization.messages.show(data.infoMessage,"message_warning");
		                }

		                // Handle erroneous data
		                if( !Visualization.update.checkCorrectData( data.returnData ) && data.returnData.type!="boxplot" ) {
		                    Visualization.messages.show( ["Unfortunately the server returned data in a format that we did not expect."], "message_error" );
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
		                    Visualization.messages.show( ["Unfortunately the server returned data without any measurements"], "message_error" );
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
		                            },
		                            title: 'Please note: outliers are not shown'
		                        };
		                        break;
		                }
		                
		                // If a chart has been created, show it
		                if( plotOptions != null ) {

		                    $( "#visualization" ).css("width",$( "#visualization_container" ).innerWidth()-2);
		                    $( "#visualization" ).css("height",$( "#visualization_container" ).innerHeight()-2);

		                    $( "#visualization" ).empty();
		                    
		                    if(returnData.type=="table") {
		                    	Visualization.current.showHTML(plotOptions);
		                    } else {
		                    	Visualization.current.draw( dataPoints, plotOptions );
		                    }
		                    $( "#visualization" ).show();
		                }
		        		
		        	})
		        	.fail(function( jqXHR, textStatus, errorThrown ) {
						// An error occurred while retrieving fields from the server
						Visualization.messages.show( "An error occurred while retrieving data from the server. Please try again or contact a system administrator<br />" + textStatus, "message_error" );
						Visualization.messages.indicate.error( "#menu_go" );
					})
		        	.always(function() {
		                $( "#menu_go" ).find(".spinner").hide();
		        	});	           	
		        	
		    }			
		},
	},
	
	/**
	 * Clears a select in the interface, and optionally the select boxes dependant on it
	 */
	clearSelect: function (that, stepNr) {

	    var block = $(that).parents(".menu_header").find(".block_variable");
	    $(block).children("input, select").val("");
	    $(block).children("input").data( "ui-autocomplete" ).term = "";

    	Visualization.messages.indicate.ready(that);
    	
	    if(stepNr==1) {
	        $(that).parents(".menu_item").children(".menu_header").each(function(index) {
	            if($(this).find("select").val()!=null && $(this).find("select").val()!="") {
	                this.clearSelect($(this).find("select"),0);
	                $(this).find("select").empty();
	                $(this).find("input").val("");
	                $(this).children(".menu_header_count").removeClass().addClass("menu_header_count");
	            }
	            
	        });
	    }
	    if(stepNr>=1) {
	    	Visualization.visualization.perform();
	    }
	}
		
};

$(Visualization.initialize);

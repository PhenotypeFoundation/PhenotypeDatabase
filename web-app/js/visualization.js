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
	    $( "#dialog_advanced_settings [type=number]" ).on( "change", Visualization.visualization.auto );
	    
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
			// Handle special case where only one message is given
			if( typeof messages == "string" )
				messages = [messages];
			
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
		draw: function( type, dataPoints, plotOptions ) {
			if( type == "table" ) 
            	this.showHTML(plotOptions);
			else
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
	
	        // Update type and aggregation combinations
	        Visualization.update.typesAndAggregations();
	        
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
	                    var returnDataAggregations = data.returnData.aggregations;

	                    // Store current aggregation and type
	                    if(Visualization.current.aggregation == null) {
	                        Visualization.current.aggregation = "average";
	                    } else {
	                        Visualization.current.aggregation = $("#select_aggregation input:checked").val();
	                    }
	                    Visualization.current.type = $("#select_types input:checked").val();

	                    // Indicate the types and aggreation and ready
	                    Visualization.messages.indicate.ready( "#select_types, #select_aggregation" );

	                    // Disable all aggregation- and visualizationoptions
	                    $("#select_aggregation input, #select_types input").each(function(index) {
	                        $(this).attr("disabled","disabled").attr( "checked", false ).removeData( "allowed" );
	                    });

	                    // Enable some visualizationoptions
	                    $.each( returnDataTypes, function( idx, field ) {
	                        $("#vis_"+field.id).attr("disabled",false).data( "allowed", true);
	                        if( field.id == Visualization.current.type || returnDataTypes.length==1 ) {
	                            Visualization.current.type = field.id;
	                            $("#vis_"+field.id).attr("checked","checked");
	                            
	    	                    Visualization.messages.indicate.done( "#select_types" );
	                        };
	                    });

	                    // Enable some aggregationoptions
	                    var enabledAggregations = $.grep(returnDataAggregations, function(field) { return !field.disabled; } );
	                    $.each( enabledAggregations, function( idx, field ) {
                            $("#aggr_"+field.id).attr("disabled",false).data( "allowed", true);
                            if( field.id == Visualization.current.aggregation || enabledAggregations.length==1 ) {
                                Visualization.current.aggregation = field.id;
                                $("#aggr_"+field.id).attr("checked", "checked");
                                
	    	                    Visualization.messages.indicate.done( "#select_aggregation" );
                            };
	                    });
	                    
	                    // Enable/disable options based on other choices
	                    Visualization.update.typesAndAggregations();

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
		
		/**
		 * Enable/disable certain checkboxes based on other choices made by the user
		 */
		typesAndAggregations: function() {
	        // Boxplot is only allowed for no aggregation
	        if(Visualization.current.aggregation == "none") {
	        	// Allow boxplots, if it would fit the data
	            $("#vis_boxplot").attr("disabled", !$("#vis_boxplot").data( "allowed" ));
	            
	            // If no aggregation is selected and grouping is selected, only allow boxplots
	            if( $( '#select_groups' ).val() ) {
	            	$( "#select_types input:not(#vis_boxplot)" ).attr( "disabled", true ).attr( "checked", false );
	            }
	        } else {
	            $("#vis_boxplot").attr("disabled","disabled");
	            
	            // If aggregation is selected and grouping is selected, also allow other items than boxplots
            	$( "#select_types input:not(#vis_boxplot)" ).each(function(idx, el) { 
            		$(el).attr( "disabled", !$(el).data( "allowed" ) ); 
            	} );
	        }
	        
            // If current type is disabled, reset the checkboxes
            if(Visualization.current.type) {
            	var el = $("#vis_" + Visualization.current.type);
                if( el.attr( "disabled" ) ) {
                	el.attr("checked",false);
	    			Visualization.messages.indicate.ready(el);
	    	        Visualization.current.type = null;
                }
            }
		}
	},
	
	/**
	 * Methods for converting server side data into the proper format for plotting charts
	 */
	data: {
		_generic: {
			/**
			 * Converts raw data into series and datapoints to be used for plotting
			 * @param returnData
			 * @param convertElementIntoDataPoint	Function that converts a raw element into a datapoint
			 * @param serieOptions					Function that returns the serieOptions for a given element
			 */
			convertData: function(returnData, convertElementIntoDataPoint, serieOptions) {
				// Use defaults if no methods are specified
				if( !serieOptions )
					serieOptions = this.serieOptions;
				
				// Use defaults if no methods are specified
				if( !convertElementIntoDataPoint )
					convertElementIntoDataPoint = this.convertElementIntoDataPoint
				
				// Initialize empty lists
				var series = [];
				var dataPoints = [];
				
                $.each(returnData.series, function(idx, element ) {
                	if( element.y && element.y.length > 0 ) {
                		dataPoints.push(convertElementIntoDataPoint(element));
                		series.push(serieOptions(element));
                	}
                });
                
                return { series: series, dataPoints: dataPoints };
			},
			convertElementIntoDataPoint: function(element) {
				return element.y
			},
			serieOptions: function(element) {
				return { "label": element.name };
			},
			
			plotOptions: function(returnData, series, settings) {
                return {
                    // Tell the plot to stack the bars.
                    stackSeries: false,
                    legend: {
                        show: settings.showLegend,
                        placement: settings.legendPlacement
                    },
                    series: series,
                    highlighter: {
                        show: !settings.showDataValues,
                        sizeAdjust: 7.5,
                    },                    
                    axes: {
                        xaxis: {
                            label: settings.labels.x,
                            labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                            tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                            tickOptions: {
                                angle: settings.xangle,
                                labelPosition: 'middle'
                            }
                        },
                        yaxis: {
                            labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                            label: settings.labels.y,
                        }
                    },
                    axesDefaults: {
                        pad: 1.2
                    }
                };
			}
		},
		
		barchart: {
			convertData: function(returnData) {
				return Visualization.data._generic.convertData(returnData);
			},
			plotOptions: function(returnData, series, settings) {
        		// Compute barwidth ourselves, as jqPlot fails at that
				var numSeries = series.length;
				var numWithinSerie = Visualization.data.maxOfArray($.map( returnData.series, function(el) { return el.y.length; } ));
        		var bWidth = 560 / ( numSeries * numWithinSerie ) * 0.6;
        						
                return $.extend( true, Visualization.data._generic.plotOptions(returnData, series, settings), {
                        // Tell the plot to stack the bars.
                        seriesDefaults:{
                            renderer:$.jqplot.BarRenderer,
                            rendererOptions: {
                                barWidth: bWidth,
                                barMargin: 5,
                                barPadding: 3,

                                highlightMouseDown: true,
                                fillToZero: true,

                                shadowOffset: 0
                            },
                            pointLabels: {show: settings.showDataValues}
                        },
                        highlighter: {
                            tooltipAxes: "y"
                        },
                        axes: {
                            xaxis: {
                                renderer: $.jqplot.CategoryAxisRenderer,
                                ticks: returnData.series[ 0 ].x,		// Use the x-axis of the first serie
                            },
                            yaxis: {
                                formatString:'%.2f'
                            }
                        },
                    });
			}
		},
		
		horizontal_barchart: {
			convertData: function(returnData) {
				return Visualization.data._generic.convertData(returnData, this.convertElementIntoDataPoint);
			},
			convertElementIntoDataPoint: function(element) {
                // The horizontal barchart needs special dataPoints
                var dataPoint = new Array();
                for(var i=0; i < element.y.length; i++) {
                	dataPoint.push( new Array(element.y[i], i+1) );
                }
                
                return dataPoint;
			},
			plotOptions: function( returnData, series, settings ) {
        		// Compute barwidth ourselves, as jqPlot fails at that
				var numSeries = series.length;
				var numWithinSerie = Visualization.data.maxOfArray($.map( returnData.series, function(el) { return el.x.length; } ));
        		var bWidth = 460 / ( numSeries * numWithinSerie ) * 0.6;
        		
                // Tell the plot to stack the bars.
                return $.extend( true, Visualization.data._generic.plotOptions(returnData, series, settings), {
                        stackSeries: false,
                        seriesDefaults:{
                            renderer:$.jqplot.BarRenderer,
                            rendererOptions: {
                                // Highlight bars when mouse button pressed.
                                // Disables default highlighting on mouse over.
                                highlightMouseDown: true,
                                highlightMouseDown: true,
                                fillToZero: true,
                                
                                barDirection: 'horizontal',
                                barWidth: bWidth,
                                barMargin: 5,
                                barPadding: 3,
                                
                                shadowOffset: 0
                            },
                            pointLabels: {show: settings.showDataValues}
                        },
                        highlighter: {
                            tooltipAxes: "x"
                        },
                        axes: {
                            xaxis: {
                                formatString:'%.2f',
                                min: 0,
                            },
                            yaxis: {
                                tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                                ticks: returnData.series[ 0 ].x,		// Use the x-axis of the first serie

                                renderer: $.jqplot.CategoryAxisRenderer
                            }
                        },
                    });				
			}
		},
		
		linechart: {
			convertData: function(returnData) {
				return Visualization.data._generic.convertData(returnData);
			},
			plotOptions: function(returnData, series, settings) {
				return $.extend( true, Visualization.data._generic.plotOptions(returnData, series, settings), {
                    stackSeries: false,
                    seriesDefaults:{
                        renderer:$.jqplot.LineRenderer,
                        pointLabels: {show: settings.showDataValues}
                    },
                    highlighter: {
                        tooltipAxes: "y"
                    },
                    axes: {
                        xaxis: {
                            renderer: $.jqplot.CategoryAxisRenderer,
                            ticks: returnData.series[ 0 ].x,	// Use the x-axis of the first serie
                        },
                        yaxis: {
                            formatString:'%.2f'
                        }
                    },
                });				
			}
		},
		
		table: {
			convertData: function(returnData) {
				return Visualization.data._generic.convertData(returnData);
			},
			
			// Plot options for tables are used to store the HTML table structure
			plotOptions: function(returnData, series, settings) {
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
                row.append("<td class='caption' colspan='"+returnData.series[0].x.length+"'>"+settings.labels.x+"</td>");
                row.appendTo(table);

                // create header-row
                var row = $("<tr>");
                // create headers
                if(series.length>1) {
                    row.append("<td class='caption'>"+settings.labels.group+"</td>");
                    row.append("<td class='caption'>"+settings.labels.y+"</td>");
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
                                        row.append("<td class='caption' rowspan='"+returnData.series[0].y.length+"'>"+settings.labels.y+"</td>");
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

                return table;				
			}
		},
		scatterplot: {
			convertData: function(returnData) {
				return Visualization.data._generic.convertData(returnData, this.convertElementIntoDataPoint, this.serieOptions);
			},
			
			convertElementIntoDataPoint: function(element) {
				return Visualization.data.transpose([element.x, element.y]);
			},			
			serieOptions: function(element) {
				var options = Visualization.data._generic.serieOptions(element);
				options.showLine = false;
				options.markerOptions = { "size": 7, "style":"filledCircle" };
				
				return options;
			},
			
			plotOptions: function(returnData, series, settings ) { 
				return $.extend( true, Visualization.data._generic.plotOptions(returnData, series, settings), {
                    stackSeries: false,
                    seriesDefaults:{
                        renderer:$.jqplot.LineRenderer,
                        pointLabels: {show: settings.showDataValues}
                    },
                    highlighter: {
                        tooltipAxes: "y"
                    },
                    axes: {
                        yaxis: {
                            formatString:'%.2f'
                        }
                    },
                });		
			}
		},
		boxplot: {
			convertData: function(returnData) {
				return Visualization.data._generic.convertData(returnData, this.convertElementIntoDataPoint);
			},
			convertElementIntoDataPoint: function(element) {
	            // The fifth element of the list should be repeated on the second position
				var data = element.y;
				for(i = 0; i < data.length; i++ ) {
					var dataPoint = data[i];
					data[i] = [dataPoint[0], dataPoint[4],dataPoint[1],dataPoint[2],dataPoint[3],dataPoint[4],dataPoint[5],dataPoint[6],dataPoint[7]]; 
				}
                return data;
			},
			
			plotOptions: function(returnData, series, settings) {
                return $.extend( true, Visualization.data._generic.plotOptions(returnData, series, settings), {
                        seriesDefaults: {
                            renderer: $.jqplot.BoxplotRenderer,
                        },
                        highlighter: {
                            show: false,
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
                        axes: {
                            xaxis: {
                                renderer: $.jqplot.CategoryAxisRenderer,
                                
                            },
                        },
                        title: 'Please note: outliers are not shown'
                    });				
			}
		},		

		/**
		 * Transposes a matrix (2d array)
		 */
		transpose: function(a) {
			return Object.keys(a[0]).map(
	            function (c) { return a.map(function (r) { return r[c]; }); }
            );
        },
        
        /**
         * Returns the maximum value of an array
         */
        maxOfArray: function(array) { return Math.max.apply(Math, array); }
	},
	
	visualization: {
		auto: function() {
		    if($("#autovis").attr("checked")=="checked") {
		        Visualization.visualization.perform();
		    }			
		},
		
		/**
		 * Create a visualization based on the parameters entered in the form
		 * The data for the visualization is retrieved from the serverside getData method
		 */ 
		perform: function() {
			// Do nothing if we don't have the right parameters
		    if( !$( "#select_rows" ).val() || !$( "#select_columns" ).val() || $( "#select_types input:checked" ).length == 0 || $( "#select_aggregation input:checked" ).length == 0 ) 
		    	return;

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
	                    Visualization.messages.show( "Unfortunately the server returned data in a format that we did not expect.", "message_error" );
	                    return;
	                }

	                // Retrieve the datapoints from the json object
	                var returnData = data.returnData;
	                
	                // Convert the raw data into the proper format for plotting. 
	                // Plotdata will contain series and dataPoints
	                var plotData = Visualization.data[returnData.type].convertData(returnData);
	                var dataPoints = plotData.dataPoints;
	                var series = plotData.series;

	                // If no datapoints are found, return an error
	                if( dataPoints.length == 0 ) {
	                    Visualization.messages.show( "Unfortunately the server returned data without any measurements", "message_error" );
	                    $( "#menu_go" ).find(".spinner").hide();
	                    return;
	                }
	                
	                var xlabel = Visualization.visualization.axisLabel(returnData[ "xaxis" ]);
	                var ylabel = Visualization.visualization.axisLabel(returnData[ "yaxis" ]);
	                var grouplabel = "";
	                if(returnData[ "groupaxis" ]!==undefined) {
	                    grouplabel = Visualization.visualization.axisLabel(returnData[ "groupaxis" ]);
	                }

	                // TODO: create a chart based on the data that is sent by the user and the type of chart
	                // chosen by the user
	                var plotOptions = null;

	                // Determine options to show
	                var blnShowDataValues = $("#showvalues").attr("checked")=="checked";
	                var blnShowLegend = returnData.series.length > 1;
	                var strLegendPlacement = $("#legendplacement").attr("checked")=="checked" && blnShowLegend ? "outsideGrid" : "insideGrid";
	                var xangle = ( 360 + parseInt( $("#anglelabels").val() ) ) % 360;

	                var plotOptions = Visualization.data[returnData.type].plotOptions(returnData, series, {
	                	 showDataValues: blnShowDataValues, 
	                	 showLegend: blnShowLegend, 
	                	 legendPlacement: strLegendPlacement,
	                	 xangle: xangle,
	                	 labels: {
	                		 x: xlabel,
	                		 y: ylabel,
	                		 group: grouplabel
	                	 }
	                });

	                // If a chart has been created, show it
	                if( plotOptions != null ) {

	                    $( "#visualization" ).css("width",$( "#visualization_container" ).innerWidth()-2);
	                    $( "#visualization" ).css("height",$( "#visualization_container" ).innerHeight()-2);

	                    $( "#visualization" ).empty();
	                    
                    	Visualization.current.draw( returnData.type, dataPoints, plotOptions );
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
	    },
	    
	    /**
	     * Generate a label to show on the axis
	     */
	    axisLabel: function(axisData) {
	    	return axisData.unit=="" ? axisData.title : axisData.title + " (" + axisData.unit + ")";		    	
	    }
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
	            if($(this).find("select").length > 0) {
	                Visualization.clearSelect($(this).find("select"),0);
	            }
	            
	        });
	    }
	    if(stepNr>=1) {
	    	Visualization.update.typesAndAggregations();
	    	Visualization.visualization.auto();
	    }
	}
		
};

$(Visualization.initialize);

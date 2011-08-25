<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Visualization</title>
	
	<!--[if lt IE 9]><g:javascript src="jqplot/excanvas.js" /><![endif]-->
	<g:javascript src="jqplot/jquery.jqplot.min.js" />
	<link rel="stylesheet" type="text/css" href="<g:resource dir='css' file='jquery.jqplot.min.css' />" />
	
	<!-- jqPlot plugins -->
	<g:javascript src="jqplot/plugins/jqplot.barRenderer.min.js" />
	<g:javascript src="jqplot/plugins/jqplot.categoryAxisRenderer.min.js" />
	<g:javascript src="jqplot/src/plugins/jqplot.pointLabels.min.js" />	
	<g:javascript src="jqplot/src/plugins/jqplot.canvasTextRenderer.min.js" />	
	<g:javascript src="jqplot/src/plugins/jqplot.canvasAxisLabelRenderer.min.js" />	
	
	<script type="text/javascript">
		// We store urls here because they depend on the grails configuration.
		// This way, the URLs are always correct
		var visualizationUrls = {
			"getStudies": "<g:createLink action="getStudies" />", 
			"getFields": "<g:createLink action="getFields" />", 
			"getVisualizationTypes": "<g:createLink action="getVisualizationTypes" />", 
			"getData": "<g:createLink action="getData" />" 
		};

		function showError( message ) {
			$( '#ajaxError' ).text( message );
			$( '#ajaxError' ).show();
		}

		/**
		 * Gathers data for the given request type from the form elements on the page
		 * @param type	String	Can be 'getStudies', 'getFields', 'getVisualizationType' or 'getData'
		 * @return Object		Object with the data to be sent to the server
		 */
		function gatherData( type ) {
			var data = {};
		
			// different types of request require different data arrays
			// However, some data is required for all types. For that reason, 
			// the fallthrough option in the switch statement is used.
			switch( type ) {
				case "getData":
					var typeElement = $( '#type' );
					data[ "type" ] = { "id": typeElement.val() }; 					
				case "getVisualizationTypes":
					var rowsElement = $( '#rows' );
					var columnsElement = $( '#columns' );
					data[ "rows" ] = [
						{ "id": rowsElement.val() }
					]; 					
					data[ "columns" ] = [
							{ "id": columnsElement.val() }
					]; 					
				case "getFields":
					var studyElement = $( '#study' );
					data[ "studies" ] = [
						{ "id": studyElement.val() }
					]; 					
					
				case "getStudies":
			}

			return data;
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
		function executeAjaxCall( action, ajaxParameters ) {
			var data = gatherData( action );

			// If no parameters are given, create an empty map
			if( !ajaxParameters ) 
				ajaxParameters = {}

			if( ajaxParameters[ "errorMessage" ] ) {
				var message = ajaxParameters[ "errorMessage" ];
				ajaxParameters[ "error" ] = function( jqXHR, textStatus, errorThrown ) {
					// An error occurred while retrieving fields from the server
					showError( "An error occurred while retrieving variables from the server. Please try again or contact a system administrator." );
				}

				// Remove the error message
				delete ajaxParameters[ "errorMessage" ];
			}
			
			// Retrieve a new list of fields from the controller
			// based on the study we chose
			$.ajax($.extend({
				url: visualizationUrls[ action ],
				data: "data=" + JSON.stringify( data ),
				dataType: "json",
			}, ajaxParameters ) );
		}
		
		function changeStudy() {
			executeAjaxCall( "getFields", {
				"errorMessage": "An error occurred while retrieving variables from the server. Please try again or contact a system administrator.",
				"success": function( data, textStatus, jqXHR ) {
					// Remove all previous entries from the list
					$( '#rows, #columns' ).empty();

					// Add all fields to the lists
					$.each( data, function( idx, field ) {
						$( '#rows, #columns' ).append( $( "<option>" ).val( field.id ).text( field.name ) );
					});
					
					$( "#step2" ).show();
					$( "#step3" ).hide();
				}
			});
		}

		function changeFields() {
			executeAjaxCall( "getVisualizationTypes", {
				"errorMessage": "An error occurred while retrieving visualization types from the server. Please try again or contact a system administrator.",
				"success": function( data, textStatus, jqXHR ) {
					// Remove all previous entries from the list
					$( '#types' ).empty();

					// Add all fields to the lists
					$.each( data, function( idx, field ) {
						$( '#types' ).append( $( "<option>" ).val( field.id ).text( field.name ) );
					});

					$( "#step3" ).show();
				}
			});
		}

		function visualize() {
			executeAjaxCall( "getData", {
				"errorMessage": "An error occurred while retrieving data from the server. Please try again or contact a system administrator.",
				"success": function( data, textStatus, jqXHR ) {
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

					// TODO: error handling if incorrect data is returned
					 
					// Retrieve the datapoints from the json object
					var dataPoints = [];
					var series = [];

					$.each(data.series, function(idx, element ) {
						dataPoints[ dataPoints.length ] = element.y;
						series[ series.length ] = { "label": element.name };
					});
					
					// TODO: create a chart based on the data that is sent by the user and the type of chart
					// chosen by the user
					chart = $.jqplot('visualization', dataPoints, {
						// Tell the plot to stack the bars.
						stackSeries: true,
						captureRightClick: true,
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
									ticks: data.x,
									label: data[ "xaxis" ].title + " (" + data[ "xaxis" ].unit + ")",
									labelRenderer: $.jqplot.CanvasAxisLabelRenderer
							},
							yaxis: {
								// Don't pad out the bottom of the data range.	By default,
								// axes scaled as if data extended 10% above and below the
								// actual range to prevent data points right on grid boundaries.
								// Don't want to do that here.
								padMin: 0,
								label: data[ "yaxis" ].title + " (" + data[ "yaxis" ].unit + ")",
								labelRenderer: $.jqplot.CanvasAxisLabelRenderer
							}
						},
						legend: {
							show: true,
							location: 'e',
							placement: 'outside'
						}			
					});
					
					$( "#visualization" ).show();
				},
			});
		}
	</script>
	<style type="text/css">
		/* #step2, #step3 { display: none; } */
		#ajaxError { 
			display: none;
			border: 1px solid #f99; /* #006dba; */
			margin-bottom: 10px;
			margin-top: 10px;
		
			background: #ffe0e0 url(${fam.icon( name: 'error' )}) 10px 10px no-repeat;
			padding: 10px 10px 10px 33px;
		}
		
		label { display: inline-block; zoom: 1; *display: inline; width: 110px; margin-top: 10px;  }

		#visualizationForm { position: relative; margin: 10px 0; font-size: 11px; }
		#visualizationForm h3 { font-size: 13px; }
		#visualizationForm h3 .nummer { display: inline-block; zoom: 1; *display: inline; width: 25px; } 
		
		#visualizationForm p { margin-left: 25px; }
		
		table.jqplot-table-legend { width: 100px; }
	</style>
</head>
<body>
	
	<h1>Visualize your study</h1>
	
	<g:if test="${flash.error}">
		<div class="errormessage">
			${flash.error.toString().encodeAsHTML()}
		</div>
	</g:if>
	<g:if test="${flash.message}">
		<div class="message">
			${flash.message.toString().encodeAsHTML()}
		</div>
	</g:if>
	
	<div id="ajaxError">
	</div>	
	
	<p class="explanation">
		Choose a study to visualize
	</p>

	<form id="visualizationForm">
		<h3><span class="nummer">1</span>Studies</h3>
		<p>
			<label>Study</label><g:select from="${studies}" optionKey="id" optionValue="title" name="study" onChange="changeStudy();"/>
		</p>
		
		<div id="step2">
			<h3><span class="nummer">2</span>Variables</h3>
			<p>
				<label for="rows">Rows</label> <select id="rows" name="rows" onChange="changeFields();"></select><br />
				<label for="columns">Columns</label> <select id="columns" name="columns" onChange="changeFields();"></select>
			</p>
		</div>
	
		<div id="step3">
			<h3><span class="nummer">3</span>Visualization type</h3>
			<p>
				<label for"types">Type</label><select id="types" name="types"></select>
			</p>
			<p>
				<label> </label><input type="button" value="Visualize" onClick="visualize();"/>
			</p>
		</div>
	</form>
	
	<div id="visualization">
	</div>
</body>
</html>




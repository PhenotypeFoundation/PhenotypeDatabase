<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Visualization</title>
	
	<!--[if lt IE 9]><g:javascript src="jqplot/excanvas.js" /><![endif]-->
	<g:javascript src="jqplot/jquery.jqplot.min.js" />
	<link rel="stylesheet" type="text/css" href="<g:resource dir='css' file='jquery.jqplot.min.css' />" />
	
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
		 */
		function gatherData( type ) {
			var data = new Array();
		
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
		
		function changeStudy() {
			var data = gatherData( "getFields" );
			
			// Retrieve a new list of fields from the controller
			// based on the study we chose
			$.ajax({
				url: visualizationUrls[ "getFields" ],
				data: data,
				dataType: "json",
				success: function( data, textStatus, jqXHR ) {
					// Remove all previous entries from the list
					$( '#rows, #columns' ).empty();

					// Add all fields to the lists
					$.each( data, function( idx, field ) {
						$( '#rows, #columns' ).append( $( "<option>" ).val( field.id ).text( field.name ) );
					});
					
					$( "#step2" ).show();
					$( "#step3" ).hide();
				},
				error: function( jqXHR, textStatus, errorThrown ) {
					// An error occurred while retrieving fields from the server
					showError( "An error occurred while retrieving variables from the server. Please try again or contact a system administrator." );
				}
			});
		}

		function changeFields() {
			var data = gatherData( "getVisualizationTypes" );
			
			// Retrieve a new list of visualization types from the controller
			// based on the study and fields we chose
			$.ajax({
				url: visualizationUrls[ "getVisualizationTypes" ],
				data: data,
				dataType: "json",
				success: function( data, textStatus, jqXHR ) {
					// Remove all previous entries from the list
					$( '#types' ).empty();

					// Add all fields to the lists
					$.each( data, function( idx, field ) {
						$( '#types' ).append( $( "<option>" ).val( field.id ).text( field.name ) );
					});

					$( "#step3" ).show();
				},
				error: function( jqXHR, textStatus, errorThrown ) {
					// An error occurred while retrieving fields from the server
					showError( "An error occurred while retrieving visualization types from the server. Please try again or contact a system administrator." );
				}
			});
		}

		function visualize() {
			var data = gatherData( "getData" );

			// Retrieve the data for visualization from the controller
			// based on the study, fields and type we chose
			$.ajax({
				url: visualizationUrls[ "getData" ],
				data: data,
				dataType: "json",
				success: function( data, textStatus, jqXHR ) {
					// TODO: create a chart based on the data that is sent by the user and the type of chart
					// chosen by the user
					
					var plot1 = $.jqplot ('visualization', data);
					$( "#visualization" ).show();
				},
				error: function( jqXHR, textStatus, errorThrown ) {
					// An error occurred while retrieving fields from the server
					showError( "An error occurred while retrieving data from the server. Please try again or contact a system administrator." );
				}
			});
			
		}
		
	</script>
	<style type="text/css">
		#step2, #step3 { display: none; }
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
	
	<form id="visualizationForm">
		<h3><span class="nummer">1</span>Studies</h3>
		<p class="explanation">
			Choose a study to visualize
		</p>
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
				<select id="types" name="types"></select>
				<input type="button" value="Visualize" onClick="visualize();"/>
			</p>
		</div>
	</form>
	
	<div id="visualization">
	</div>
</body>
</html>




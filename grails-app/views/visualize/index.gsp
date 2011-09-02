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
	<g:javascript src="jqplot/plugins/jqplot.pointLabels.min.js" />	
	<g:javascript src="jqplot/plugins/jqplot.canvasTextRenderer.min.js" />	
	<g:javascript src="jqplot/plugins/jqplot.canvasAxisLabelRenderer.min.js" />	

	<g:javascript src="visualization.js" />
	<link rel="stylesheet" type="text/css" href="<g:resource dir='css' file='visualization.css' />" />
	
	<script type="text/javascript">
		// We store urls here because they depend on the grails configuration.
		// This way, the URLs are always correct
		var visualizationUrls = {
			"getStudies": "<g:createLink action="getStudies" />", 
			"getFields": "<g:createLink action="getFields" />", 
			"getVisualizationTypes": "<g:createLink action="getVisualizationTypes" />", 
			"getData": "<g:createLink action="getData" />" 
		};
	</script>
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




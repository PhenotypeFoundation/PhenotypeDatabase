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
    <style type="text/css">
        /** NEEDED FOR RESOURCES PLUGIN **/
        .menu_arrow {background-image: url(${resource(dir: 'images/visualization', file: 'down_arrow.png')}); }
        .error { background: #ffe0e0 url(${fam.icon( name: 'exclamation' )}) 10px 10px no-repeat; }
        .warning{ background: #eee url(${fam.icon( name: 'information' )}) 10px 10px no-repeat; }
    </style>
	
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
	
    <div id="data">
        <div id="menu_container">
            <form id="visualizationForm">
                <div class="menu_spacer"> </div>
                <div class="menu_item menu_item_fill" id="menu_study">
                    <div class="menu_item_label">Studies <img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" /></div>
                    <div class="menu_item_info"></div>
                    <div class="formulier"><label for="study">Study</label><g:select from="${studies}" optionKey="id" optionValue="title" name="study" onChange="changeStudy();" noSelection="${['':'[SELECT OPTION]']}"/></div>
                </div>
                <div class="menu_arrow"> </div>
                <div class="menu_item" id="menu_row">
                    <div class="menu_item_label">Rows <img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" /></div>
                    <div class="menu_item_info"></div>
                    <div class="formulier"><label for="rows">Rows</label> <select id="rows" name="rows" onChange="changeFields('menu_row');"></select></div>
                </div>
                <div class="menu_item" id="menu_column">
                    <div class="menu_item_label">Columns <img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" /></div>
                    <div class="menu_item_info"></div>
                    <div class="formulier"><label for="columns">Columns</label> <select id="columns" name="columns" onChange="changeFields('menu_column');"></select></div>
                </div>
                <div class="menu_arrow"> </div>
                <div class="menu_item" id="menu_vis">
                    <div class="menu_item_label">Type <img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" /></div>
                    <div class="menu_item_info"></div>
                    <div class="formulier"><label for="types">Type</label><select id="types" name="types" onChange="changeVis();"></select></div>
                </div>
                <div class="menu_arrow"> </div>
                <div class="menu_item" id="menu_go">
                    <div id="button_visualize" onClick="visualize();">Visualize</div>
                    <input type="checkbox" name="autovis" id="autovis" CHECKED/><span style="font-size: small;">auto</span></div>
                <div class="menu_spacer"> </div>
            </form>
        </div>

        <div id="visualization_container">

            <h1>Visualize your study</h1>

            <div id="message" class="error">
                <g:if test="${flash.error}">
                    ${flash.error.toString().encodeAsHTML()}
                </g:if>
                <g:if test="${flash.message}">
                    ${flash.message.toString().encodeAsHTML()}
                </g:if>
            </div>

            <div id="visualization">
            </div>
        </div>
    </div>
</body>
</html>




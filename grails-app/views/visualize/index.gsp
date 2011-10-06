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
    <g:javascript src="jqplot/plugins/jqplot.ohlcRenderer.min.js" />
    <g:javascript src="jqplot/plugins/jqplot.highlighter.min.js" />

	<g:javascript src="visualization.js" />
	<link rel="stylesheet" type="text/css" href="<g:resource dir='css' file='visualization.css' />" />
    <style type="text/css">
        /** NEEDED FOR RESOURCES PLUGIN **/
        .menu_seperator {background-image: url(${resource(dir: 'images/visualization', file: 'seperator.gif')}); }
        .message_error { background: #ffe0e0 url(${fam.icon( name: 'exclamation' )}) 10px 5px no-repeat; }
        .message_warning { background: #eee url(${fam.icon( name: 'information' )}) 10px 5px no-repeat; }
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
        <form id="visualizationForm">
            <div id="top_container">

                <span class="menu_seperator">&nbsp;</span>

                <span class="topmenu_item" id="menu_study">
                    <span class="topmenu_item_label"><img src="${fam.icon( name: 'report' )}" style="vertical-align: text-bottom; display: inline-block;"/>&nbsp;Study<img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" />:</span>
                    <span class="topmenu_item_info">no study selected</span>
                    <img src="${fam.icon( name: 'bullet_arrow_down' )}" style="vertical-align: text-bottom; display: inline-block;"/>
                    <div class="formulier">
                        <p class="info">Select a study from the list below.</p>
                        <p>
                            <g:select from="${studies}" size="4" optionKey="id" optionValue="title" name="study" onChange="changeStudy();"/>
                        </p>
                    </div>
                </span>

               

                <span class="menu_seperator">&nbsp;</span>

                <span class="topmenu_item" id="menu_advanced">
                    <span class="topmenu_item_label"><img src="${fam.icon( name: 'cog' )}" style="vertical-align: text-bottom; display: inline-block;"/>&nbsp;Advanced settings</span>
                    <img src="${fam.icon( name: 'bullet_arrow_down' )}" style="vertical-align: text-bottom; display: inline-block;"/>
                    <div class="formulier">
                        <p class="info">Advanced settings.</p>
                        <p>
                            Visualize the data as soon as enough parameters are known.
                            <input type="checkbox" name="autovis" id="autovis" CHECKED/>
                        </p>
                    </div>
                </span>

                <span class="menu_seperator">&nbsp;</span>

                <span class="topmenu_item" id="message_counter" onClick="; return false;">
                    <span class="topmenu_item_label"><img src="${fam.icon( name: 'email_error' )}" style="vertical-align: text-bottom; display: inline-block;"/>&nbsp;Messages:</span>
                    <span class="topmenu_item_info">0</span>
                    <img src="${fam.icon( name: 'bullet_arrow_down' )}" style="vertical-align: text-bottom; display: inline-block;"/>
                    <div class="formulier">
                        <p class="info">Messages:</p>
                        <div id="message_container">
                        <g:if test="${flash.error}">
                            <div class="message_box message_error">
                                ${flash.error.toString().encodeAsHTML()}
                            </div>
                        </g:if>
                        <g:if test="${flash.message}">
                            <div class="message_box message_warning">
                                ${flash.message.toString().encodeAsHTML()}
                            </div>
                        </g:if>
                        </div>
                    </div>
                </span>

                <span class="menu_seperator">&nbsp;</span>
                    
            </div>

            <div id="bottom_container">

                <div id="menu_container">
                    <div class="menu_item" id="menu_column">
                        <div class="menu_item_label">X-Axis <img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" /></div>
                        <p class="info">Select a field for the X-Axis from the list below. This field will be visible as columns in the table visualization.</p>
                        <p>
                            <select id="columns" name="columns" size="6" onChange="changeFields('menu_column');"></select>
                        </p>
                    </div>
                    <div class="menu_item" id="menu_row">
                        <div class="menu_item_label">Y-Axis <img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" /></div>
                        <p class="info">Select a field for the Y-Axis from the list below. This field will be visible as rows in the table visualization.</p>
                        <p>
                            <select id="rows" name="rows" size="6" onChange="changeFields('menu_row');"></select>
                        </p>
                    </div>
                    <div class="menu_item" id="menu_vis">
                        <div class="menu_item_label">Type <img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" /></div>
                        <p class="info">Select visualization type.</p>
                        <p>
                            <select id="types" name="types"  size="3" onChange="changeVis();"></select>
                        </p>
                    </div>
                    <div class="menu_item" id="menu_go">
                        <button id="button_visualize" onClick="visualize(); return false;" >
                            VISUALIZE
                        </button>
                    </div>
                </div>
                
                <div id="visualization_container">
                    <div id="visualization"><div style="padding: 30px">Select a study to start.</div>
                    </div>
                </div>

                <br clear="all"/>

            </div>
        </form>
    </div>
</body>
</html>




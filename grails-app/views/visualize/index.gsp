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
    <g:javascript src="jqplot/plugins/jqplot.canvasAxisTickRenderer.min.js" />
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
                    <div class="topmenu_item_label"><img src="${fam.icon( name: 'report' )}" style="vertical-align: text-bottom; display: inline-block;"/>&nbsp;Study<img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" />:</div>
                    <div class="topmenu_item_info">no study selected</div>
                    <img src="${fam.icon( name: 'bullet_arrow_down' )}" style="vertical-align: text-bottom; display: inline-block;"/>
                    <div class="formulier">
                        <div class="menu_item_label_count">1</div><div class="menu_item_label">Study</div>
                        <p class="info">Select a study from the list below.</p>
                        <p style="margin-bottom: 0px;">
                            <g:select from="${studies}" size="6" optionKey="id" name="study" onChange="changeStudy();"/>
                            <div class="block_search">
                                <img src="${fam.icon( name: 'magnifier' )}" alt="search"/>:
                                <input type="text" onKeyUp="doSearch('menu_study','study');" />
                                <img src="${fam.icon( name: 'cancel' )}" class="imgbutton" onClick="clearSearch('menu_study','study');" alt="clear search"/>
                            </div>
                        </p>
                    </div>
                </span>

                <span class="menu_seperator">&nbsp;</span>

                <span class="topmenu_item" id="menu_aggregation">
                    <span class="topmenu_item_label"><img src="${fam.icon( name: 'server_chart' )}" style="vertical-align: text-bottom; display: inline-block;"/>&nbsp;Aggregation:</span>
                    <span class="topmenu_item_info">AVERAGE</span>
                    <img src="${fam.icon( name: 'bullet_arrow_down' )}" style="vertical-align: text-bottom; display: inline-block;"/>
                    <div class="formulier">
                        <p class="info">Select a way to aggregate the data.</p>
                        <p>
                            <select name="aggregation" size="5" onchange="$('#menu_aggregation').children('.topmenu_item_info').html($(this).val().toUpperCase()); changeVis();">
                                <option value="average" SELECTED>Average</option>
                                <option value="count">Count</option>
                                <option value="median">Median</option>
                                <option value="none">No aggregation</option>
                                <option value="sum">Sum</option>
                            </select>
                        </p>
                    </div>
                </span>

                <span class="menu_seperator">&nbsp;</span>

                <span class="topmenu_item" id="menu_advanced">
                    <span class="topmenu_item_label"><img src="${fam.icon( name: 'cog' )}" style="vertical-align: text-bottom; display: inline-block;"/>&nbsp;Advanced settings</span>
                    <img src="${fam.icon( name: 'bullet_arrow_down' )}" style="vertical-align: text-bottom; display: inline-block;"/>
                    <div class="formulier">
                        <table>
                            <tr>
                                <td><label for="autovis">Visualize the data as soon as enough parameters are known.</label></td>
                                <td><input type="checkbox" name="autovis" id="autovis" CHECKED/></td>
                            </tr>
                            <tr>
                                <td><label for="showvalues">Always show values in the graph.<br />(if this box is unchecked, value's are only shown when you hover over a datapoint. Note that hover on a barchart with value 0 isn't possible)</label></td>
                                <td><input type="checkbox" name="showvalues" id="showvalues" CHECKED onClick="changeVis();"/></td>
                            </tr>
                            <tr>
                                <td><label for="anglelabels">Angle labels on the x-axis.<br />(if this box is checked, labels are shown at a 45&deg; angle)</label></td>
                                <td><input type="checkbox" name="anglelabels" id="anglelabels" CHECKED onClick="changeVis();"/></td>
                            </tr>
                        </table>
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
                        <div class="menu_item_label_count"><span>2</span></div><div class="menu_item_label">X-Axis <img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" /></div>
                        <div>
                            <select id="columns" name="columns" size="6" onChange="changeFields('menu_column');"></select>
                            <div class="block_search">
                                <img src="${fam.icon( name: 'magnifier' )}" alt="search"/>:
                                <input type="text" onKeyUp="doSearch('menu_column','columns');" />
                                <img src="${fam.icon( name: 'cancel' )}" class="imgbutton" onClick="clearSearch('menu_column','columns');" alt="clear search"/>
                            </div>
                        </div>
                    </div>
                    <div class="menu_item" id="menu_row">
                        <div class="menu_item_label_count">3</div><div class="menu_item_label">Y-Axis <img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" /></div>
                        <div>
                            <select id="rows" name="rows" size="6" onChange="changeFields('menu_row');"></select>
                            <div class="block_search">
                                <img src="${fam.icon( name: 'magnifier' )}" alt="search"/>:
                                <input type="text" onKeyUp="doSearch('menu_row','rows');" />
                                <img src="${fam.icon( name: 'cancel' )}" class="imgbutton" onClick="clearSearch('menu_row','rows');" alt="clear search"/>
                            </div>
                        </div>
                    </div>
                    <div class="menu_item" id="menu_groups">
                        <div class="menu_item_label_count">4</div><div class="menu_item_label">Group by<img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" /></div> (<a href='#' onclick="clearSelect(); return false;">clear</a>)
                        <div>
                            <select id="groups" name="groups" size="6" onChange="changeFields('menu_groups');"></select>
                            <div class="block_search">
                                <img src="${fam.icon( name: 'magnifier' )}" alt="search"/>:
                                <input type="text" onKeyUp="doSearch('menu_groups','groups');" />
                                <img src="${fam.icon( name: 'cancel' )}" class="imgbutton" onClick="clearSearch('menu_groups','groups');" alt="clear search"/>
                            </div>
                        </div>
                    </div>
                    <div class="menu_item" id="menu_vis">
                        <div class="menu_item_label_count">5</div><div class="menu_item_label">Type <img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" /></div>
                        <div>
                            <select id="types" name="types"  size="3" onChange="changeVis();"></select>
                        </div>
                    </div>
                    <div class="menu_item" id="menu_go">
                        <button id="button_visualize" onClick="visualize(); return false;" >
                            VISUALIZE
                        </button>
                    </div>
                </div>
                
                <div id="visualization_container">
                    <div id="visualization"><div style="padding: 30px">Click on the study menu and select a study to start.</div>
                    </div>
                </div>

                <br clear="all"/>

            </div>
        </form>
    </div>
</body>
</html>




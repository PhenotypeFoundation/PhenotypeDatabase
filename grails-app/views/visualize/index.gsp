<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Visualization</title>
	<r:require modules="visualization"/>
	 
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

            <div id="bottom_container">

                <div id="menu_container">
                    <div class="menu_item">
                        <div class="menu_header">
                            <span class="menu_header_count menu_fill">1</span>
                            <span class="menu_header_label">Study <img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" /></span>
                            <span class="menu_header_clear">(<a href='#' onclick="Visualization.clearSelect(this, 1); return false;">clear</a>)</span>
                            <div class="block_variable">
                                <g:select from="${studies}" optionKey="id" id="select_study" name="study" noSelection="${['':'Select One...']}"/>
                            </div>
                        </div>
                        <div class="menu_header">
                            <span class="menu_header_count">2</span>
                            <span class="menu_header_label">X-Axis <img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" /></span>
                            <span class="menu_header_clear">(<a href='#' onclick="Visualization.clearSelect(this, 2); return false;">clear</a>)</span>
                            <div class="block_variable">
                                <select id="select_columns" name="columns"></select>
                            </div>
                        </div>
                        <div class="menu_header">
                            <span class="menu_header_count">3</span>
                            <span class="menu_header_label">Y-Axis <img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" /></span>
                            <span class="menu_header_clear">(<a href='#' onclick="Visualization.clearSelect(this, 3); return false;">clear</a>)</span>
                            <div class="block_variable">
                                <select id="select_rows" name="rows"></select>
                            </div>
                        </div>
                        <div class="menu_header">
                            <span class="menu_header_count">4</span>
                            <span class="menu_header_label"><span style="font-size: small">OPTIONAL:</span> Group by <img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" /></span>
                            <span class="menu_header_clear">(<a href='#' onclick="Visualization.clearSelect(this, 4); return false;">clear</a>)</span>
                            <div class="block_variable">
                                <select id="select_groups" name="groups"></select>
                            </div>
                        </div>
                        <div class="menu_header">
                            <span class="menu_header_count">5</span>
                            <span class="menu_header_label">Aggregation <img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" /></span>
                            <div class="block_variable" id="select_aggregation">
                                <label for="aggr_sum">Sum</label><input type="radio" name="aggregation" id="aggr_sum" value="sum"/>
                                <label for="aggr_count">Count</label><input type="radio" name="aggregation" id="aggr_count" value="count"/>
                                <label for="aggr_average">Average</label><input type="radio" name="aggregation" id="aggr_average" value="average"/>
                                <label for="aggr_median">Median</label><input type="radio" name="aggregation" id="aggr_median" value="median"/>
                                <label for="aggr_none">No Aggregation</label><input type="radio" name="aggregation" id="aggr_none" value="none"/>
                            </div>
                        </div>
                        <div class="menu_header">
                            <span class="menu_header_count">6</span>
                            <span class="menu_header_label">Visualization type <img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" /></span>
                            <div class="block_variable" id="select_types">
                                <label for="vis_barchart">Barchart</label><input type="radio" name="types" id="vis_barchart" value="barchart"/>
                                <label for="vis_horizontal_barchart">Horizontal barchart</label><input type="radio" name="types" id="vis_horizontal_barchart" value="horizontal_barchart"/>
                                <label for="vis_linechart">Linechart</label><input type="radio" name="types" id="vis_linechart" value="linechart"/>
                                <label for="vis_scatterplot">Scatterplot</label><input type="radio" name="types" id="vis_scatterplot" value="scatterplot"/>
                                <label for="vis_table">Table</label><input type="radio" name="types" id="vis_table" value="table"/>
                                <label for="vis_boxplot">Boxplot</label><input type="radio" name="types" id="vis_boxplot" value="boxplot"/>
                            </div>
                        </div>
                    </div>
                    <div class="menu_header">
	                    <div class="menu_item" id="menu_go">
	                        <button id="button_visualize">
	                            <img src="${resource(dir: 'images/ajaxflow', file: 'spacer.gif')}" class="spinner" />
	                            VISUALIZE
	                            <img src="${resource(dir: 'images', file: 'spinner.gif')}" class="spinner" />
	                        </button>
	                    </div>
	                </div>

                    <div class="settings">
                        <a href="#" id="button_advanced_settings"><img src="${fam.icon( name: 'cog' )}" style="vertical-align: text-bottom; display: inline-block;"/>&nbsp;advanced settings</a>
                        <br />
                        <a href="#" id="button_messages"><img src="${fam.icon( name: 'email_error' )}" style="vertical-align: text-bottom; display: inline-block;"/><span id="messages_link">&nbsp;0 messages</span></a>
                    </div>

                </div>

                
                <div id="visualization_container">
                    <div id="visualization"></div>
                </div>

                <br clear="all"/>

            </div>

            <div id="dialog_messages">
                <p class="message info">Messages:</p>
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
             <div id="dialog_advanced_settings">
                <table>
                    <tr>
                        <td><label for="autovis">Visualize the data as soon as enough parameters are known.</label></td>
                        <td><input type="checkbox" name="autovis" id="autovis" CHECKED /></td>
                    </tr>
                    <tr>
                        <td><label for="showvalues">Always show values in the graph.<br /><span class="settingInfo">(if this box is unchecked, value's are only shown when you hover over a datapoint. Note that hover on a barchart with value 0 isn't possible)</span></label></td>
                        <td><input type="checkbox" name="showvalues" id="showvalues" CHECKED /></td>
                    </tr>
                    <tr>
                        <td><label for="anglelabels">Angle labels on the x-axis.<br /><span class="settingInfo">(in degrees; enter 0 to have no angle at all)</span></label></td>
                        <td><input type="number" min="-360" max="360" name="anglelabels" id="anglelabels" value="315" style="width: 50px;" /></td>
                    </tr>
                    <tr>
                        <td><label for="legendplacement">Place legend outside the graph.<br /><span class="settingInfo">(legend is only shown when the group-by option is used)</span></label></td>
                        <td><input type="checkbox" name="legendplacement" id="legendplacement" CHECKED /></td>
                    </tr>
                </table>
            </div>
        </form>
    </div>
</body>
</html>




<html>
<head>
	<title>Generic Study Capture Framework</title>
	<meta property="og:title" content="Generic Study Capture Framework"/>
	<meta property="og:description" content="A generic tool for planning scientific studies, and capturing study meta-data, integrating with analysis platform(s) / LIMS systems and searching relevant studies."/>
	<meta name="layout" content="main"/>
	<g:if test="${showstats}"><script type="text/javascript" src="${resource(dir: 'js', file: 'highcharts.js')}"></script></g:if>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.ui.autocomplete.html.js', plugin: 'gdt')}"></script>
	<script type="text/javascript">
		$(document).ready(function() {
<g:if test="${showstats}">
			Highcharts.theme = { colors: ['#4572A7'] };
			var highchartsOptions = Highcharts.getOptions();
			var studiesPieChart, dailyStatistics;

			studiesPieChart = new Highcharts.Chart({
				chart: {
					renderTo: 'studies-pie',
					plotBackgroundColor: null,
					plotBorderWidth: null,
					plotShadow: false
				},
				title: {
					text: '${studyCount} Studies'
				},
				tooltip: {
					formatter: function() {
						return '<b>' + this.point.name + '</b>: ' + this.y + ' ' + ((this.y == 1) ? 'study' : 'studies');
					}
				},
				plotOptions: {
					pie: {
						allowPointSelect: true,
						cursor: 'pointer',
						dataLabels: {
							enabled: true,
							color: Highcharts.theme.textColor || '#000000',
							connectorColor: Highcharts.theme.textColor || '#000000',
							formatter: function() {
								return '<b>' + this.point.name + '</b>: ' + this.y + ' ' + ((this.y == 1) ? 'study' : 'studies');
							}
						}
					}
				},
				series: [
					{
						type: 'pie',
						name: 'Your statistics',<sec:ifLoggedIn>
						size: '45%',
						innerSize: '25%',
						</sec:ifLoggedIn><sec:ifNotLoggedIn>
						innerSize: '45%',
						</sec:ifNotLoggedIn>
						data: [
							{ name: 'Public', y: ${publicStudyCount}, color: '#89A54E' },
							{ name: 'Private', y: ${privateStudyCount}, color: '#AA4643' }
						],
						dataLabels: {
							enabled: <sec:ifLoggedIn>false</sec:ifLoggedIn><sec:ifNotLoggedIn>true</sec:ifNotLoggedIn>,
						}
					},
					{
						type: 'pie',
						name: 'Your statistics',<sec:ifLoggedIn>
						size: '25%',
						innerSize: '10%',
						</sec:ifLoggedIn><sec:ifNotLoggedIn>
						size: '45%',
						</sec:ifNotLoggedIn>
						data: [
							{ name: 'Published public', y: ${publishedPublicStudyCount}, color: '#89A54E' },
							{ name: 'Unpublished Public', y: ${unPublishedPublicStudyCount}, color: '#bbc695' },
							{ name: 'Published Private', y: ${publishedPrivateStudyCount}, color: '#AA4643' },
							{ name: 'Unpublished Private', y: ${unPublishedPrivateStudyCount}, color: '#ae6e6c' }
						],
						dataLabels: {
							enabled: false
						}
					}
					<sec:ifLoggedIn>
					,
					{
						type: 'pie',
						name: 'Total statistics',

						innerSize: '45%',
						data: [
							{ name: 'Read only', y: ${readOnlyStudyCount}, color: '#80699B' },
							{ name: 'Readable & writable', y: ${readWriteStudyCount}, color: '#89A54E' },
							{ name: 'Not accessible', y: ${noAccessStudyCount}, color: '#AA4643' }
						],
						dataLabels: {
							enabled: true
						}
					},
					</sec:ifLoggedIn>
				]
			});

			<g:if test="${startDate && dailyStatistics?.size()}">
			dailyStatistics = new Highcharts.Chart({
				chart: {
					renderTo: 'daily-statistics',
					zoomType: 'x',
					spacingRight: 20
				},
				title: {
					text: 'Users, studies and templates'
				},
				subtitle: {
					text: document.ontouchstart === undefined ?
						'Click and drag in the plot area to zoom in' :
						'Drag your finger over the plot to zoom in'
				},
				xAxis: {
					type: 'datetime',
					maxZoom: 14 * 24 * 3600000, // fourteen days
					title: {
						text: null
					}
				},
				yAxis: {
					title: {
						text: 'Total'
					},
					min: 0,
					startOnTick: false,
					showFirstLabel: false
				},
				tooltip: {
					shared: true
				},
				legend: {
					enabled: false
				},
				plotOptions: {
					area: {

						lineWidth: 1,
						marker: {
							enabled: false,
							states: {
								hover: {
									enabled: true,
									radius: 5
								}
							}
						},
						shadow: false,
						states: {
							hover: {
								lineWidth: 1
							}
						}
					}
				},

				series: [
					{
						type: 'area',
						name: 'Studies',
						pointInterval: 24 * 3600 * 1000,
						pointStart: Date.UTC(${startDate.year+1900}, ${startDate.month}, ${startDate.date}),
						data: [
							<g:each var="day" in="${dailyStatistics}">${day.value.studyTotal},</g:each>
						]
					},
					{
						type: 'area',
						name: 'Templates',
						pointInterval: 24 * 3600 * 1000,
						pointStart: Date.UTC(${startDate.year+1900}, ${startDate.month}, ${startDate.date}),
						data: [
							<g:each var="day" in="${dailyStatistics}">${day.value.templateTotal},</g:each>
						]
					},
					{
						type: 'area',
						name: 'Users',
						pointInterval: 24 * 3600 * 1000,
						pointStart: Date.UTC(${startDate.year+1900}, ${startDate.month}, ${startDate.date}),
						data: [
							<g:each var="day" in="${dailyStatistics}">${day.value.userTotal},</g:each>
						]
					}
				]
			});
			</g:if>
</g:if>
			var quickSearch = $("#search_term");
			quickSearch.autocomplete({
				minLength: 2,
				delay: 300,
				search: function(event, ui) {
					quickSearch.css({ 'background': 'url(${resource(dir: 'images', file: 'spinner.gif')}) no-repeat left top' });
				},
				source: function(request, response) {
					quickSearch.css({ 'background': 'none' });

					$.ajax({
						//url: "http://ws.geonames.org/searchJSON",
						url: "${createLink(action:'ajaxQuickSearch')}",
						dataType: "jsonp",
						data: {
							featureClass: "P",
							style: "full",
							maxRows: 12,
							name_startsWith: request.term
						},
						success: function(data) {
							response($.map(data.data, function(item) {
								return {
									label		: '<span class="about">'+item.category+'</div> <span class="from">'+item.name+'</span>',
									value		: item.link
								}
							}));
						}
					});
				},
				minLength: 2,
				select: function(event, ui) {
					// redirect ?
					if (ui.item.value) {
						// hide, so the URL does not show in the input field
						quickSearch.css( { 'display': 'none' } );

						// and redirect
						window.location = ui.item.value;
					}
				},
				open: function() {
					$(this).removeClass("ui-corner-all").addClass("ui-corner-top");
				},
				close: function() {
					$(this).removeClass("ui-corner-top").addClass("ui-corner-all");
				},
				html: true
			});
		});
	</script>
	<style type="text/css">
	#simpleQuery {
	}

	#simpleQuery .search {
		display: block;
		height: 30px;
		margin-bottom: 10px;
		zoom: 1; /* IE 6 & 7 hack */
		*display: inline; /* IE 6 & 7 hack */
	}

	#simpleQuery .search .begin {
		margin: 0px;
		padding: 0px;
		display: inline-block;
		background-image: url(${resource(dir: 'images', file: 'simpleQuery/spotlight-begin.png')});
		height: 30px;
		width: 140px;
		vertical-align: top;
		text-align: right;
		zoom: 1; /* IE 6 & 7 hack */
		*display: inline; /* IE 6 & 7 hack */
	}

	#simpleQuery .search .begin .label {
		color: #fff;
		font-face: Arial;
		line-height: 30px;
		text-shadow: 0px 0px 1px #006DBA;
		font-size: 12px;
		margin-right: 23px;
	}

	#simpleQuery .search .middle {
		margin: 0px 0px -20px 0px;
		padding: 0;
		display: inline-block;
		background-image: url(${resource(dir: 'images', file: 'simpleQuery/spotlight-middle.png')});
		height: 30px;
		width: 300px;
		vertical-align: top;
		zoom: 1; /* IE 6 & 7 hack */
		*display: inline; /* IE 6 & 7 hack */
	}

	#simpleQuery .search .searchfield {
		vertical-align: middle;
		width: 100%;
		height: 100%;
		color: #006DBA;
		border-width: 0px;
		border: none;
		background-color: Transparent;
		zoom: 1; /* IE 6 & 7 hack */
		*display: inline; /* IE 6 & 7 hack */
	}

	#simpleQuery .search .end {
		margin: 0px;
		padding: 0px;
		display: inline-block;
		background-image: url(${resource(dir: 'images', file: 'simpleQuery/spotlight-end.png')});
		height: 30px;
		width: 28px;
		zoom: 1; /* IE 6 & 7 hack */
		*display: inline; /* IE 6 & 7 hack */
	}

	</style>
</head>
<body>

<div style="clear:both;display:block;">
	<div style="margin-right:8px;width:472px;display:inline-block;float:left;zoom:1;*display:inline;">
		<h1>Introduction</h1>
		<p>
			The phenotype database (dbNP) is an application that can store any biological study. It contains
			templates which makes it possible to customize
		</p>
		<p>
			In order to allow flexibility to capture all information you require within a study, <i>and</i> to
			make it possible to compare studies or study data, the system uses customizable templates and ontologies.
			It is especially designed to store complex study designs including cross-over designs and challenges.
		</p>
		<p>
			The application facilitates sharing of data within a research group or consortium, as the study owner can
			decide who can view or access the data. In addition, the application can stimulate collaborations by making
			study information publically visible. New studies can be based on study data within the database, as
			standardized storage is stimulated by the system.
		</p>
	</div>
	<div style="margin-left:8px;width:472px;display:inline-block;float:left;zoom:1;*display:inline;">
		<h1>Quicksearch</h1>
		<div id="simpleQuery" class="simplequery">
		<g:form action="pages" name="simpleQueryForm" id="simpleQueryForm">
			<g:if test="${search_term}"><g:set var="preterm" value="${search_term}"/></g:if>
			<div class="searchContainer">
				<div class="search">
					<div class="begin"><span class="label">Search term</span></div><div class="middle"><g:textField name="search_term" id="search_term" class="searchfield" value="${preterm}"/></div><div class="end"><a onClick="$('#search_term').val('');"><img src="${resource(dir: 'images', file: 'simpleQuery/spotlight-end.png')}" value="Reset" alt="Reset" border="0"></a></div>
				</div>
				<span style="font-style:italic;color:#aaa;font-size:10px;">more advanced searches can be performed <g:link controller="advancedQuery">here</g:link>...</span>
			</div>
			</g:form>
			<h1>Quick Start</h1>
			<p>
				Through the <i>studies</i> menu you can either <i>create</i>, <i>view</i> or <i>import</i> studies
				(or study data). '<g:link controller="studyWizard" action="index" params="[jump:'create']">Create a new study</g:link>' will guide you through several steps to include your study
				into the system where question marks (<img src="${fam.icon(name: 'help')}">) will explain what information is
				required. You can (quick)save your study to complete it at another point in time, or use
				<i>import study data</i> to import large datasets (for example: many subjects) from an excel sheet
				into your study. Several data-types of different platforms (assays) can
				be linked to your study, like <i>simple assays</i> (e.g. clinical chemistry or Western blot)
				or <i>metabolomics</i>.
			</p>
		</div>
	</div>
</div>

<g:if test="${showstats}">
<div style="clear:both;display:block;padding-top:10px;">
	<h1>Usage Statistics</h1>
	<div id="graphs" style="display:block;border:1px solid #6c6f70;width:100%;height:300px;">
		<div id="studies-pie" style="margin:2px;width:476px;height:296px;display:inline-block;float:left;zoom:1;*display:inline;"></div>
		<div id="daily-statistics" style="margin:2px;width:476px;height:296px;display:inline-block;float:left;zoom:1;*display:inline;"></div>
	</div>
</div>
</g:if>

</body>
</html>
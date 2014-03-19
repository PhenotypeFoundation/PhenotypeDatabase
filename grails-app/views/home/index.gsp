<html>
<head>
	<meta name="layout" content="main"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'home.css')}" type="text/css"/>
	<g:if test="${showstats}"><script type="text/javascript" src="${resource(dir: 'js', file: 'highcharts.js')}"></script></g:if>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.ui.autocomplete.html.js', plugin: 'gdt')}"></script>
	<script type="text/javascript">
		$(document).ready(function() {
            <g:if test="${showstats && studyCount}">
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
							{ name: 'Public', y: ${publicStudyCount}, color: '#45bee1' },
							{ name: 'Private', y: ${privateStudyCount}, color: '#2087a3' }
						],
						dataLabels: {
							enabled: <sec:ifLoggedIn>false</sec:ifLoggedIn><sec:ifNotLoggedIn>true</sec:ifNotLoggedIn>
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
							{ name: 'Published public', y: ${publishedPublicStudyCount}, color: '#45bee1' },
							{ name: 'Unpublished Public', y: ${unPublishedPublicStudyCount}, color: '#e9644f' },
							{ name: 'Published Private', y: ${publishedPrivateStudyCount}, color: '#2087a3' },
							{ name: 'Unpublished Private', y: ${unPublishedPrivateStudyCount}, color: '#2087a3' }
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
							{ name: 'Read only', y: ${readOnlyStudyCount}, color: '#2087a3' },
							{ name: 'Readable & writable', y: ${readWriteStudyCount}, color: '#45bee1' },
							{ name: 'Not accessible', y: ${noAccessStudyCount}, color: '#e9644f' }
						],
						dataLabels: {
							enabled: true
						}
					},
					</sec:ifLoggedIn>
				]
			});

			<g:if test="${startDate && dailyStatistics?.size() && studyCount}">
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
                        name: 'Users',
                        color: '#2087a3',
                        pointInterval: 24 * 3600 * 1000,
                        pointStart: Date.UTC(${startDate.year+1900}, ${startDate.month}, ${startDate.date}),
                        data: [
                            <g:each var="day" in="${dailyStatistics}">${day.value.userTotal},</g:each>
                        ]
                    },
					{
						type: 'area',
						name: 'Studies',
                        color: '#e9644f',
						pointInterval: 24 * 3600 * 1000,
						pointStart: Date.UTC(${startDate.year+1900}, ${startDate.month}, ${startDate.date}),
						data: [
							<g:each var="day" in="${dailyStatistics}">${day.value.studyTotal},</g:each>
						]
					},
					{
						type: 'area',
						name: 'Templates',
                        color: '#45bee1',
						pointInterval: 24 * 3600 * 1000,
						pointStart: Date.UTC(${startDate.year+1900}, ${startDate.month}, ${startDate.date}),
						data: [
							<g:each var="day" in="${dailyStatistics}">${day.value.templateTotal},</g:each>
						]
					}
				]
			});
			</g:if>
            </g:if>
			var quickSearch = $("#search_term");
            var search_spinner = $("#search_spinner");
            var search_button =  $("#search_button");
			quickSearch.autocomplete({
				minLength: 2,
				delay: 300,
				search: function(event, ui) {
                    search_spinner.css ({ 'opacity': 100 });
                    search_button.css ({ 'color': '#2087a3' });
				},
				source: function(request, response) {
                    search_spinner.css ({ 'opacity': 0 });
                    search_button.css ({ 'color': '#fff' });

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
									label		: '<span class="about">'+item.category+'</span> <span class="from">'+item.name+'</span>',
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
</head>
<body>
    <div class="intro">
        <div class="content editor">
            <h1 class="title">Introduction</h1>
            <div class="subtitle"><p>${grailsApplication.config.application.title} is an application that can store any biological study. It contains templates which makes it possible to customize.</p></div>
            <div class="toggleCont">
                <p class="shortCont">In order to allow flexibility to capture all information you require within a study, and to make it possible to compare studies or study data, the system uses customizable templates and ontologies. It is especially designed to store complex study designs including. <a class="more" href="#" title="">Read more...</a></p>
                <div class="fullCont">
                    <p>
                        ${grailsApplication.config.application.title} facilitates sharing of data within a research group or consortium,
                        as the study owner can decide who can view or access the data. In addition, ${grailsApplication.config.application.title}
                        can stimulate collaborations by making study information publicly visible. New studies can be based on study
                        data within the database, as standardized storage is stimulated by the system.
                    </p>
                    <h1>Quick Start</h1>
                    <p>
                        Through the <i>studies</i> menu you can either <i>create</i>, <i>view</i> or <i>import</i> studies
                        (or study data). '<g:link controller="studyWizard" action="index" params="[jump:'create']">Create a new study</g:link>' will guide you through several steps to include your study
                        into the system where question marks (<img src="${fam.icon(name: 'help')}">) will explain what information is
                        required. You can (quick) save your study to complete it at another point in time, or use
                        <i>import study data</i> to import large datasets (for example: many subjects) from an excel sheet
                        into your study. Several data-types of different platforms (assays) can
                        be linked to your study, like <i>simple assays</i> (e.g. clinical chemistry or Western blot)
                        or <i>metabolomics</i>.
                    </p>
                    <p>
                    <p><a class="less" href="#" title="">Read less...</a></p>
                </div>
            </div>
            <p class="buttons"><g:link class="button-3 pie" controller="studyEdit" action="add">Share your study</g:link></p>
        </div>
        <div class="aside">
            <div class="document">
                <h2>User Guide Downloads</h2>
                <ul class="doclist">
                    <li><a href="${resource(dir:'downloads', file: 'quickstart_study_capturing.pdf')}" title="">Quick Start User Guide</a></li>
                    <li><a href="${resource(dir:'downloads', file: 'gscf_user_guide.pdf')}" title="">In Depth User Guide</a></li>
                </ul>
            </div>
            <p class="note">If you encounter a problem or have a suggestion for improvement feel free to submit an issue <a href="#" title="">here</a></p>
        </div>
    </div>
    <div class="usageStatistics">
        <g:if test="${showstats && studyCount}">
            <h1>Usage Statistics</h1>
            <div id="graphs" style="display:block;width:100%;height:300px;">
                <div id="studies-pie" style="width:476px;height:296px;display:inline-block;float:left;zoom:1;*display:inline;"></div>
                <div id="daily-statistics" style="margin:2px;width:476px;height:296px;display:inline-block;float:left;zoom:1;*display:inline;"></div>
            </div>
        </g:if>
    </div>
</div>
</body>
</html>
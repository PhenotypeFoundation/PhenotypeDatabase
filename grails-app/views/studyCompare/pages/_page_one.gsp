<%
/**
 * first wizard page / tab
 *
 * @author Jeroen Wesbeek
 * @since  20120123
 *
 * Revision information:
 * $Rev:  67319 $
 * $Author:  duh $
 * $Date:  2010-12-22 17:45:42 +0100 (Wed, 22 Dec 2010) $
 */
%>
<af:page>
<script type="text/javascript">
var criteria = {};

handleCheckEvent();

function handleCheckEvent(event) {
	var check = $(event);
	var value = check.attr('value');
	var parent = check.parent();
	var parentId = parent.attr('id');

	if (criteria[parentId] == undefined) criteria[parentId] = [];

	// add or remove data
	if (check.is(':checked') && criteria[parentId].indexOf(value) < 0) {
		criteria[parentId].push(value);
	} else if (criteria[parentId].indexOf(value) >= 0) {
		criteria[parentId].splice(criteria[parentId].indexOf(value),1);
	}

	// count number of matches
	$('#matchedStudies').html('').addClass('waitForLoad');
	$.getJSON(
		baseUrl + "/ajax/studyCount",
		criteria,
		function(data) {
			$('#matchedStudies').html(data.matched+' of '+data.total+' readable studies matched your criteria').removeClass('waitForLoad');
		}
	);

	// fetch matched studies
	$('#studyOverview').html('').addClass('waitForLoad').removeClass('waitForLoad');
	$.getJSON(
		baseUrl + "/ajax/studies",
		criteria,
		function(data) {
			var studies = '';
			for (var i=0; i<data.studies.length; i++) {
				studies = studies + data.studies[i] + '<br/>';
			}
			$('#studyOverview').html(studies);
		}
	);
}
</script>

<div class="selector">
	<div name="species" id="uniqueSpecies" class="ajax"></div>
	<div name="event templates" id="uniqueEventTemplateNames" class="ajax"></div>
	<div name="sampling event templates" id="uniqueSamplingEventTemplateNames" class="ajax"></div>
</div>
<div id="matchedStudies"></div>
<div id="studyOverview"></div>

</af:page>

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

function handleCheckEvent(event) {
	var check = $(event);
	var value = check.attr('value');
	var parent = check.parent();
	var parentId = parent.attr('id');
	if (criteria[parentId] == undefined) criteria[parentId] = [];
	var c = criteria[parentId];

	// add or remove data
	if (check.is(':checked') && c.indexOf(value) < 0) {
		c.push(value);
	} else if (c.indexOf(value) >= 0) {
		c.splice(c.indexOf(value),1);
	}

	console.log(criteria);

	// ajax call
	$.getJSON(
		"<g:createLink controller="ajax" action="studyCount"/>",
		criteria,
		function(data) {
			$('#matchedStudies').html(data.count+' studies matched your criteria');
		}
	);

//	$.ajax({
//		url: baseUrl + "/ajax/studyCount",
//		dataType: 'json',
//		data: data,
//		success: function(data) {
//			$('#matchedStudies').html(data.count+' studies matched your criteria');
//		}
//	});

}
</script>


<div class="selector">
	<div name="species" id="uniqueSpecies" class="ajax"></div>
	<div name="event templates" id="uniqueEventTemplateNames" class="ajax"></div>
	<div name="sampling event templates" id="uniqueSamplingEventTemplateNames" class="ajax"></div>
</div>
<div id="matchedStudies"></div>

</af:page>

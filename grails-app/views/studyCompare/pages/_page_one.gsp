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
	var parent = check.parent().parent();
	var parentId = parent.attr('id');

	if (criteria[parentId] == undefined) criteria[parentId] = [];

	// add or remove data
	if (check.is(':checked') && criteria[parentId].indexOf(value) < 0) {
		criteria[parentId].push(value);
	} else if (criteria[parentId].indexOf(value) >= 0) {
		criteria[parentId].splice(criteria[parentId].indexOf(value),1);
	}

	// fetch matched studies
	$('#studyOverview').html('').addClass('waitForLoad');//.removeClass('waitForLoad');
    $('#matchedStudies').html('').addClass('waitForLoad');
    $.getJSON(
		baseUrl + "/ajax/studies",
		criteria,
		function(data) {
            // update matched study feedback
            $('#matchedStudies').html(data.matched+' of '+data.total+' readable studies matched your criteria').removeClass('waitForLoad');

            // show matching studies
			var studies = '';
			for (var i=0; i<data.studies.length; i++) {
				studies = studies + data.studies[i] + '<br/>';
			}
			$('#studyOverview').removeClass('waitForLoad').html(studies);

            // (un)mark property checkboxes
            $.each(['uniqueSpecies','uniqueEventTemplateNames','uniqueSamplingEventTemplateNames','modules'],function(index,property) {
                $('input:checkbox[name="'+property+'[]"]').each(function() {
                    element = $(this);
                    if (data[property][ element.val() ]) {
                        element.parent().removeClass('dimmed');
                    } else {
                        element.parent().addClass('dimmed');
                    }
                });
            });
		}
	);
}
</script>

<div class="selector">
	<div name="species" id="uniqueSpecies" class="ajax"></div>
	<div name="event templates" id="uniqueEventTemplateNames" class="ajax"></div>
	<div name="sampling event templates" id="uniqueSamplingEventTemplateNames" class="ajax"></div>
    <div name="modules" id="modules" class="ajax"></div>
</div>
<div id="matchedStudies"></div>
<div id="studyOverview"></div>

</af:page>

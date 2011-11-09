<%@ page import="dbnp.studycapturing.Study" contentType="text/html;charset=UTF-8" %>
<html>
<head>
	<g:if test="${GALAXY_URL}">
		<meta name="layout" content="galaxy"/>
	</g:if>
	<g:else>
		<meta name="layout" content="main"/>
	</g:else>

	<title>Select an assay</title>
	<script type="text/javascript">
		function updateAssay(jsonData, selectID) {
			var a = eval(jsonData);
			var sel = $('#' + selectID).empty();

			$('#submit').attr("disabled", a.length == 0);

			$(a).each(function(i, el) {
				sel.append($("<option></option>").attr("value", el.id).text(el.name))
			})
		}

		$(document).ready(function() {
			// trigger change event to load assay based on currently selected study.
			// After pressing 'Back', the browser may use last selected study.
			$('#study').change()

		})
	</script>
</head>

<body>
<div style="color:red;">
	${flash.errorMessage}
</div>

<h1>Select the assay you want to export data from</h1>

With this exporter you can export (meta) data about samples from an assay to a file.
First, select a study from the first list and then select an assay from that study from the second list.<br/>

<g:form name="assaySelect" action="assayExport">
	<g:select optionKey="id" optionValue="title" name="studyId" from="${userStudies}" id="study"
			  onChange="${remoteFunction(controller:'study',action:'ajaxGetAssays',params:'\'id=\'+escape(this.value)',onComplete: 'updateAssay(XMLHttpRequest.responseText, \'assay\')')}"/>
	<g:select optionKey="id" name="assayId" id="assay" from=""/>
	<g:submitButton name="submit" value="Submit" id="submit"/>
</g:form>
</body>
</html>
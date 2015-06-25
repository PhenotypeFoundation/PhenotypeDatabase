<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <title>Assay exporter</title>
    
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
			$('#study').change();
		})
	</script>    
</head>
<body>

  <g:form action="exportAssays" name="assayExportForm">
  <div class="body">
  	<h1>Select the assay you want to export data from</h1>
  	
  	<p>
		With this exporter you can export (meta) data about samples from an assay to a file.
		First, select a study from the first list and then select an assay from that study from the second list.
	</p>
  
  	<g:render template="/common/flashmessages" />

	<h3>Study</h3>
	<g:select style="width: 400px;" optionKey="id" optionValue="title" name="studyId" from="${studies}" id="study"
			  onChange="${remoteFunction(controller:'study',action:'ajaxGetAssays',params:'\'id=\'+escape(this.value)',onComplete: 'updateAssay(XMLHttpRequest.responseText, \'assay\')')}"/>
			  
	<h3>Assay</h3>
	<g:select style="width: 400px;" multiple="multiple" optionKey="id" name="assayId" id="assay" from=""/>
	
    <div class="buttons">
    	<g:each in="${formats}" var="format">
    		<g:submitButton class="button-2" style="margin-right: 5px;" title="${format}" value="${format}" name="format" />
    	</g:each>
    </div>
		
  </div>

</g:form>

</body>
</html>

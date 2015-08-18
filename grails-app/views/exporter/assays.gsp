<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <title>Assay exporter</title>
	<r:require modules="exporter" />
    
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

  <div class="body exporter">
  	<h1>Export assays</h1>
  	
	<span class="message info"> 
		With this exporter you can export (meta) data about samples from an assay to a file.
	</span>
	 
  	<g:render template="/common/flashmessages" />
  	
	<g:form action="exportAssays" name="assayExportForm">
		<fieldset id="exportData">
			<div class="element">
				<div class="description">Study</div>
				<div class="input">
					<g:select name="studyId"  id="study"
						optionKey="id" optionValue="title" from="${studies}"
				  		onChange="${remoteFunction(controller:'study',action:'ajaxGetAssays',params:'\'id=\'+escape(this.value)',onComplete: 'updateAssay(XMLHttpRequest.responseText, \'assay\')')}"
				  		/>
				</div>
				
				<div class="helpIcon"></div>
				<div class="helpContent">
					Choose the study you want to export data from
				</div>
			</div>
			<div class="element">
				<div class="description">Assay</div>
				<div class="input">
					<g:select style="width: 400px;" multiple="multiple" optionKey="id" name="assayId" id="assay" from=""/>
				</div>
				
				<div class="helpIcon"></div>
				<div class="helpContent">
					Choose one or more assays you want to export data from
				</div>
			</div>		
		</fieldset>
		
		<fieldset id="exportParameters">
			<legend>Parameters</legend>
			<div class="element">
				<div class="description">Decimal separator</div>
				<div class="input">
					<g:select name="exportParameters.decimal" 
						optionKey="key" optionValue="value" from="${[ '.': '.', ',': ',']}"
				  		/>
				</div>
				
				<div class="helpIcon"></div>
				<div class="helpContent">
					Choose the decimal separator to be used in the output file
				</div>
			</div>
		</fieldset>
				
		<p class="options">
			<g:each in="${formats}" var="format">
    			<g:submitButton class="button-2" style="margin-right: 5px;" title="${format}" value="${format}" name="format" />
    		</g:each>
		</p>
		
	</g:form>
  </div>

</body>
</html>

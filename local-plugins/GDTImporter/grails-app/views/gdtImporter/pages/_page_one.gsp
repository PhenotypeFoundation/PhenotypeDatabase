<%@ page import="org.dbnp.gdt.GdtService" %>
<%
	/**
	 * first wizard page / tab
	 *
	 * @author Jeroen Wesbeek
	 * @since 20101206
	 *
	 * Revision information:
	 * $Rev: 1536 $
	 * $Author: t.w.abma@umcutrecht.nl $
	 * $Date: 2011-02-17 16:52:50 +0100 (Thu, 17 Feb 2011) $
	 */
%>
<af:page>
<title>Importer wizard (simple)</title>

<h1>Importer wizard</h1>

<p>You can import your Excel data to the server by choosing a file from your local harddisk in the form below.</p>
<input type="hidden" id="refreshPageOne" name="refreshPageOne"
	   value="${(refreshParams?.refrshPageOne != null) ? refreshParams?.refreshPageOne : refreshPageOne}"/>
<table border="0">
	<colgroup width="30%">
		<tr>
			<td width="100px">
				Choose your Excel file to import:
			</td>
			<td width="100px">
				<af:fileFieldElement name="importFileName"
									 value="${ (refreshParams?.importFileName!=null) ? refreshParams?.importFileName : importFileName}"
									 id="importFileName"/>
			</td>
		</tr>
		<tr>
			<td width="100px">
				Date format:
			</td>
			<td width="100px">
				<g:select name="dateFormat" value="${refreshParams?.dateFormat}"
						  from="${['dd/MM/yyyy (EU/India/South America/North Africa/Asia/Australia)', 'yyyy/MM/dd (China/Korea/Iran/Japan)', 'MM/dd/yyyy (US)']}"
						  keys="${['dd/MM/yyyy','yyyy/MM/dd','MM/dd/yyyy']}"/>
			</td>
		</tr>
		<tr>
			<td width="100px">
				Use data from sheet:
			</td>
			<td width="100px">
				<g:select name="sheetIndex" value="${refreshParams?.sheetIndex}"
						  from="${sheetList}"
						  optionKey="${{it-1}}"
						  onchange="updateDatamatrixPreview()"/>
			</td>
		</tr>
		<tr>
			<td width="100px">
				Column header is at:
			</td>
			<td width="100px">
				<g:select name="headerRowIndex" from="${1..9}"
						  value="${refreshParams?.headerRowIndex}" optionKey="${{it-1}}"/>
			</td>
		</tr>
		<tr>
			<td>
				Choose type of data:
			</td>
			<td>
				<g:select
					name="templateBasedEntity"
					id="templateBasedEntity"
					from="${GdtService.cachedEntities}"
					value="${(refreshParams?.entityToImport == null)? entityToImport?.encoded : refreshParams?.entityToImport.encoded}"
					optionValue="${{it.name}}"
					optionKey="${{it.encoded}}"
					noSelection="${['null':'-Select type of data-']}"
					onChange="${remoteFunction( controller: 'gdtImporter',
					    action:'ajaxGetTemplatesByEntity',
					    params: '\'templateBasedEntity=\'+escape(this.value)',
					    onSuccess:'updateSelect(\'entityToImportSelectedTemplateId\',data,false,false,\'default\',false)')}"/>
				<div id="attachSamplesDiv">
					<script type="text/javascript">
						$(document).ready(function() {
							$(':checkbox[name=attachSamples]').bind('click', function() {
								${remoteFunction( controller: 'gdtImporter',
                 	    action:'ajaxGetTemplatesByEntity',
					    params: '\'templateBasedEntity='+samplingEventEntity.encodeAsURL()+'\'',
					    onSuccess:'updateSelect(\'samplingEvent_template_id\',data,false,false,\'default\',true)')}
							})
						});
					</script>
					<g:checkBox name="attachSamples"
								id="attachSamples"
								value="${false}"/>
					Attach Samples to Existing Subjects<br/>
				</div>

				</td>
		</tr>
		<tr id="parentEntityField">
			<td>
				Choose your study:
			</td>
			<td>
				<g:select name="parentEntityId"
						  noSelection="${['null':'-Select study-']}"
						  value="${ (refreshParams?.parentEntityId == null)? parentEntityId :  refreshParams?.parentEntityId}"
						  from="${persistedParentEntities}" optionKey="id"
						  optionValue="${{ (it.toString().length()<80) ? it.toString() : it.toString()[0..Math.min(80, it.toString().length()-1)] + ' (...)' }}"/>
			</td>
		</tr>
		<tr>
			<td>
				<div id="datatemplate">Choose type of data template:</div>
			</td>
			<td><g:if test="${refreshParams?.templateBasedEntity}">
				<g:set var="templateBasedEntity" value="${refreshParams?.templateBasedEntity}"/>
			</g:if>
			<g:else>
				<g:set var="templateBasedEntity" value="None"/>
			</g:else>

			<g:select rel="template" entity="${templateBasedEntity}" name="entityToImportSelectedTemplateId"
					  noSelection="${['null':'-Select template-']}"
					  optionKey="id" optionValue="name"
					  from="${entityToImportTemplates}"
					  value="${ (refreshParams?.entityToImportSelectedTemplateId == null) ? entityToImportSelectedTemplateId : refreshParams?.entityToImportSelectedTemplateId}"/>
			</td>
		</tr>
</table>

<div id="datamatrixpreview"></div>

<script type="text/javascript">

	// Study entity is selected, hide study chooser
	<g:if test="${ (entityToImport?.name == 'Study') }">
	$('#parentEntityField').hide();
	</g:if>

	if (pageOneTimer) clearTimeout(pageOneTimer);
	var pageOneTimer = null;
	$(document).ready(function() {

		// Create listener which is checking whether a (new) file has been uploaded
		oldImportFileName = $("#importFileName").val();
		pageOneTimer = setInterval(function() {

			// A file was uploaded and a next page call was issued which failed?
			if ($("#importFileName").val().length > "existing*".length && $("#refreshPageOne").val() == "true") {

				updateDatamatrixPreview()
				// Reset the refresh page value
				$("#refreshPageOne").val("")
			}

			if (($("#importFileName").val() != oldImportFileName) || $("#refreshPageOne").val() == "true") {
				// Reset the refresh page value
				$("#refreshPageOne").val("")

				$('#datamatrixpreview').html('<table cellpadding="0" cellspacing="0" border="0" class="display" id="datamatrix"></table>');

				// show the please wait spinner
				showSpinner();

				// perform the ajax call to render the excel preview
				$.ajax({
					type: "POST",
					data: "importFileName=" + $("#importFileName").val() + "&sheetIndex=0", //+ $("#sheetIndex").val() ,
					url: "getDatamatrixAsJSON",
					success: function(msg) {
						var jsonDatamatrix = eval(msg);

						// Update sheet selector by first clearing it and appending the sheets user can choose from
						$("select[name='sheetIndex']").find('option').remove().end()

						for (i = 0; i < jsonDatamatrix.availableSheets.length; i++) {
                            var sheetNumber = jsonDatamatrix.availableSheets[i];
							$("select[name='sheetIndex']").append(new Option(sheetNumber + 1, sheetNumber));
						}

						dataTable = $('#datamatrix').dataTable({
							"oLanguage": {
								"sInfo": "Showing rows _START_ to _END_ out of a total of _TOTAL_ (inluding header)"
							},
							"sScrollX": "100%",
							"bScrollCollapse": true,
							"iDisplayLength": 5,
							"aLengthMenu": [
								[5, 10, 25, 50],
								[5, 10, 25, "All"]
							],
							"bSort" : false,
							"aaData": jsonDatamatrix.aaData,
							"aoColumns": jsonDatamatrix.aoColumns
						});
					},
					complete: function() {
						// hide the spinner
						hideSpinner();
					}
				});

				// Update the original
				oldImportFileName = $("#importFileName").val()

			}
		}, checkEverySeconds * 200);


//      // initialize Sampling Template select box for attach samples to subjects mode
//      new SelectAddMore().init({
//                rel     : 'samplingTemplate',
//                url     : baseUrl + '/templateEditor',
//                vars    : 'entity', // can be a comma separated list of variable names to pass on
//                label   : 'add / modify ...',
//                style   : 'modify',
//                onClose : function(scope) {
//                  refreshFlow()
//                }
//              });

		<g:if test="${refreshParams?.entityToImport?.name != 'Sample'}">
			$('#attachSamplesDiv').hide();
		</g:if>
		$('#attachEventsDiv').hide();
		$('#attachSamplesSamplingTemplateDiv').hide();

	});

</script>
</af:page>

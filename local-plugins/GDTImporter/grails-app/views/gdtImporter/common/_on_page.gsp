<%
	/**
	 * wizard refresh flow action
	 *
	 * When a page (/ partial) is rendered, any DOM event handlers need to be
	 * (re-)attached. The af:ajaxButton, af:ajaxSubmitJs and af:redirect tags
	 * supports calling a JavaScript after the page has been rendered by passing
	 * the 'afterSuccess' argument.
	 *
	 * Example:     af:redirect afterSuccess="onPage();"
	 *              af:redirect afterSuccess="console.log('redirecting...');"
	 *
	 * Generally one would expect this code to add jQuery event handlers to
	 * DOM objects in the rendered page (/ partial).
	 *
	 * @author Jeroen Wesbeek
	 * @since 20101206
	 *
	 * Revision information:
	 * $Rev: 1555 $
	 * $Author: t.w.abma@umcutrecht.nl $
	 * $Date: 2011-02-24 11:15:00 +0100 (Thu, 24 Feb 2011) $
	 */
%>
<script type="text/javascript">
	var oldImportFileName = '';
	var checkEverySeconds = 2;
	var dataTable;

	// Initially called when starting the import wizard
	function onPage() {
		// GENERAL
		onStudyWizardPage();

		// attach event to apply fuzzy matching
		$('#fuzzymatchselect').click(function() {
			$("#useFuzzymatching").val("true")
			refreshFlow()
		});

		// open load box
		$('#loadpropertiesbutton').click(function() {
			$("#loadmapping").toggle("scale")
		});

		// mapping has been chosen and OK button pressed? refresh the flow page
		$('#loadmappingok').click(function() {
			if ($("#loadImportMappingId").val()) refreshFlow()
		});

		// open delete box
		$('#deletepropertiesbutton').click(function() {
			$("#deletemapping").toggle("scale")
		});

		// mapping has been chosen and OK button pressed? refresh the flow page
		$('#deletemappingok').click(function() {
			if ($("#deleteImportMappingId").val()) refreshFlow()
		});

		// open save box
		$('#savepropertiesbutton').click(function() {
			$("#savemapping").toggle("scale")

			if ($("#importMappingName").val()) refreshFlow();
		});

		// mapping given a name and OK button pressed? Then refresh the flow
		$('#savemappingok').click(function() {
			if ($("#importMappingName").val()) refreshFlow();
		});

		// Disable Enter key
		function stopRKey(evt) {
			var evt = (evt) ? evt : ((event) ? event : null);
			var node = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null);
			if ((evt.keyCode == 13) && (node.type == "text")) {
				return false;
			}
		}

		document.onkeypress = stopRKey;

		// attach function to clear button to reset all selects to "don't import"
		$('#clearselect').click(function() {
			$("#currentmapping").html("Current import mapping: none")

			// for each select field on the page
			$("select").each(function() {
				// set its value to its first option
				$(this).val($('option:first', this).val());
			});
		});

		// attach change event function to prevent duplicate selection of properties
		$('select[name^="columnproperty.index."]').each(function() {
			$(this).bind('change', function(e) {
				var selection = $(this)

				$('select[name^="columnproperty.index."] option:selected').each(function() {
					var selector = $(this)

					if (selection.attr('id') != selector.parent().attr('id') && (selection.val() != "dontimport"))
						if ($(this).val() == selection.val()) {
							selection.val($('option:first', selection).val());

							alert("Property is already set for an other column, please choose a different property.")
							return false
						}
				});
			});
		});
	}

	/**
	 * Update one select based on another select
	 *
	 * @author
	 * @see  http://www.grails.org/Tag+-+remoteFunction
	 * @param   string  select (form) name
	 * @param   string  JSON data
	 * @param   boolean keep the first option
	 * @param   int  selected option
	 * @param   string  if null, show this as option instead
	 * @void
	 */
	function updateSelect(name, data, keepFirstOption, selected, presentNullAsThis, calledByCheckBox) {
		var rselect = $('#' + name).get(0)
		var items = data

		var selectedEntity = $('#templateBasedEntity :selected').text();

		$('#parentEntityField').show();
		$('#attachSamplesDiv').hide();
		$('#attachEventsDiv').hide();
		$('#attachSamplesSamplingTemplateDiv').hide();

		$('#attachEvents').attr('checked', false);

		var uncheckAttachSamples = true

		switch (selectedEntity) {
			case 'Study':
				$('#parentEntityField').hide();
				break;
			case 'Sample':
				$('#attachSamplesDiv').show();
				if ($('#attachSamples').attr('checked')) {
					$('#attachSamplesSamplingTemplateDiv').show();
//          $('#samplingEvent_template_id').show();
					uncheckAttachSamples = false;
				}
				break
			case 'Event':
				$('#attachEventsDiv').show();
				break
		}

		if (uncheckAttachSamples) $('#attachSamples').attr('checked', false);

		$('select[name=entityToImportSelectedTemplateId]').attr('entity', $('#' + 'templateBasedEntity').val());

		if (items) {

			// remove old options
			var start = (keepFirstOption) ? 0 : -1;
			var i = rselect.length

			while (i > start) {
				rselect.remove(i)
				i--
			}

			// Add first option which tells the user to select a data template
			rselect.options[0] = new Option("-Select data template-", "null")

			// add new options
			$.each(items, function() {
				var i = rselect.options.length

				rselect.options[i] = new Option(
					(presentNullAsThis && this.name == null) ? presentNullAsThis : this.name,
					this.id
				);
				if (this.id == selected) rselect.options[i].selected = true
			});

		}

		if (calledByCheckBox) {
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

		} else {
			// handle template selects
			new SelectAddMore().init({
				rel	 : 'template',
				url	 : baseUrl + '/templateEditor',
				vars	: 'entity', // can be a comma separated list of variable names to pass on
				label   : 'add / modify ...',
				style   : 'modify',
				onClose : function(scope) {
					refreshFlow()
				}
			});
		}
	}

	/**
	 * This function will update the datamatrix preview, based on the sheet index supplied
	 */
	function updateDatamatrixPreview() {
		$.ajax({
			type: "POST",
			data: "importFileName=" + $("#importFileName").val().replace("existing*", "") + "&sheetIndex=" + $("#sheetIndex").val() ,
			url: "getDatamatrixAsJSON",
			success: function(msg) {

				var jsonDatamatrix = eval(msg);
				var sheetIndex = $("#sheetIndex").val()

				// Update sheet selector by first clearing it and appending the sheets user can choose from
				$("select[name='sheetIndex']").find('option').remove().end()

                for (i = 0; i < jsonDatamatrix.availableSheets.length; i++) {
                    var sheetNumber = jsonDatamatrix.availableSheets[i];
                    $("select[name='sheetIndex']").append(new Option(sheetNumber + 1, sheetNumber));
                }

				// Set selected sheet
				$("#sheetIndex").val(sheetIndex).attr('selected', true);

				dataTable.fnDestroy();

				$('#datamatrixpreview').html('<table cellpadding="0" cellspacing="0" border="0" class="display" id="datamatrix"></table>');

				dataTable = $('#datamatrix').dataTable({
					"oLanguage": {
						"sInfo": "Showing rows _START_ to _END_ out of a total of _TOTAL_ (inluding header)"
					},

					"sScrollX": "100%",
					"bScrollCollapse": true,
					"bRetrieve": false,
					"bDestroy": true,
					"iDisplayLength": 5,
					"bSort" : false,
					"aaData": jsonDatamatrix.aaData,
					"aoColumns": jsonDatamatrix.aoColumns
				});
			}
		});

	}


</script>
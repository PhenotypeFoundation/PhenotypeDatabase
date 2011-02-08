<%
	/**
	 * wizard refresh flow action
	 *
	 * When a page (/ partial) is rendered, any DOM event handlers need to be
	 * (re-)attached. The af:ajaxButton, af:ajaxSubmitJs and af:redirect tags
	 * supports calling a JavaScript after the page has been rendered by passing
	 * the 'afterSuccess' argument.
	 *
	 * Example:	af:redirect afterSuccess="onPage();"
	 * 		af:redirect afterSuccess="console.log('redirecting...');"
	 *
	 * Generally one would expect this code to add jQuery event handlers to
	 * DOM objects in the rendered page (/ partial).
	 *
	 * @author Jeroen Wesbeek
	 * @since 20101206
	 *
	 * Revision information:
	 * $Rev$
	 * $Author$
	 * $Date$
	 */
%>
<script type="text/javascript">
	// Initially called when starting the import wizard
	function onPage() {
		// GENERAL
		onStudyWizardPage();

		$('#simplewizardform').submit(function() {
			if ($('#file').val() == "") {
				alert("Please choose your Excel file to import.");
				return false
			} else
			if ($('#entity').val() == "") {
				$('#datatemplate').addClass("validationfail");
				return false
			} else {
				$('#simplewizardform').submit();
			}

			return false;
		});

         $('#fuzzymatchselect').click(function() {
            refreshFlow()
          });

          // attach function to clear button to reset all selects to "don't import"
          $('#clearselect').click(function() {
            // for each select field on the page
            $("select").each( function(){
            // set its value to its first option
            $(this).val($('option:first', this).val());
            });
          });
	}

	/**
	 * Update one select based on another select
	 *
	 * @author
	 * @see	 http://www.grails.org/Tag+-+remoteFunction
	 * @param   string  select (form) name
	 * @param   string  JSON data
	 * @param   boolean keep the first option
	 * @param   int	 selected option
	 * @param   string  if null, show this as option instead
	 * @void
	 */
	function updateSelect(name, data, keepFirstOption, selected, presentNullAsThis) {
		var rselect = $('#' + name).get(0)
		var items = data

		// If a study has been selected, don't show the "Choose study" field, otherwise do
		if ($('#' + 'entity :selected').text() == 'Study')
			$('#studyfield').hide();
		else $('#studyfield').show();

		$('select[name=template_id]').attr('entity', $('#' + 'entity').val());

		if (items) {

			// remove old options
			var start = (keepFirstOption) ? 0 : -1;
			var i = rselect.length

			while (i > start) {
				rselect.remove(i)
				i--
			}

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

</script>


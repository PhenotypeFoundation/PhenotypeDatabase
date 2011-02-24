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

         // attach event to apply fuzzy matching
         $('#fuzzymatchselect').click(function() {
            $("#fuzzymatching").val("true")
            refreshFlow()
          });

          // open load box
          $('#loadpropertiesbutton').click(function() {
            $("#loadmapping").toggle("scale")            
            if ($("#importmapping_id").val()) refreshFlow()
          });

          // open save box
          $('#savepropertiesbutton').click(function() {
            var width = 500
            var height = 200
            var vars = ""

            $("#savemapping").toggle("scale")

            if ($("#mappingname").val()) refreshFlow();

            // get all properties
            //$('select[name^=columnproperty.index.]').each ( function() {
            //}           
            
            /*$('#propertiesManager').dialog({
                    title       : "Properties manager",
                    autoOpen    : true,
                    width       : width,
                    vars        : vars,
                    height      : height,
                    modal       : true,
                    position    : 'center',
                    buttons     : {
                                    Save  : function() {
                                                          //alert($(this).parent().$('input[name="mappingname"]').val())
                                                          alert($(this))
                                                          var p = $(this).parent().parent().parent()
                                                          console.log(p.('input[name="mappingname"]').val())
                                                          //alert(vars)
                                                          $(this).dialog('close'); }
                                  },
                    close       : function() {
                                    //onClose(this);
                                    refreshFlow()
                                  }
                }).width(width - 10).height(height)
              */

          });
          

          // Disable Enter key 
          function stopRKey(evt) {
            var evt = (evt) ? evt : ((event) ? event : null);
            var node = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null);
            if ((evt.keyCode == 13) && (node.type=="text"))  {return false;}
          }
          document.onkeypress = stopRKey;
          

          // attach function to clear button to reset all selects to "don't import"
          $('#clearselect').click(function() {
            // for each select field on the page
            $("select").each( function(){
            // set its value to its first option
            $(this).val($('option:first', this).val());
            });
          });

          // attach change event function to prevent duplicate selection of properties
          $('select[name^=columnproperty.index.]').each ( function() {
          $(this).bind('change', function(e) {
              //console.log($(this).val())
              var selection = $(this)

              $('select[name^=columnproperty.index.] option:selected').each ( function() {
                var selector = $(this)

                if (selection.attr('id') != selector.parent().attr('id') && (selection.val()!="dontimport"))
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

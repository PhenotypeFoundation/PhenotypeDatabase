if( typeof( StudyEdit ) === "undefined" ) { 
	StudyEdit = {};
}

StudyEdit.initialize = function() {
	attachHelpTooltips();
}

/**
 * Marks a specific input field (select or input) as an error to the user
 * @param cellname		Name of the input field
 * @param cellvalue		Value that the user has entered and gives an error
 */
StudyEdit.markFailedField = function( cellname, cellvalue ) {
	// First handle the field with an error if it is a selectbox
	var element = $("select[name=" + cellname + "]");
	
	element.addClass('error')
	element.append( new Option("Invalid: " + cellvalue, "#invalidterm", false, false) );

	// Also try to handle the field if it is a textbox
	element = $("input[name=" + cellname + "]");
	element.addClass('error')
}

StudyEdit.showOpenStudyDialog = function() {
	// Only load the studies list once
	if( $( '#openstudy select' ).length == 0 ) {
		$.get( baseUrl + '/study/json', function(data) {
			var select = $( '<select>' ).attr( 'name', 'id' );
			
			$.each( data, function( idx, el) {
				console.log( el );
				select.append( $( "<option>" ).attr( "value", el.id ).text( el.title ) );
			});
			$( "#openstudy" ).append( select );
			
			$( '#openStudyDialog' ).dialog( 'open' ); 
		});
	} else {
		$( '#openStudyDialog' ).dialog( 'open' ); 
	}
}

StudyEdit.initializePropertiesPage = function() {
	StudyEdit.form.initialize();
	
    // show creative commons agreement popup
    $(":checkbox[name^='public']").on('change', function() {
        var box = $(this);
        if (box.is(':checked')) {
            $( "#dialog-creative-commons" ).dialog({
                resizable: false,
                height:250,
                width: 800,
                modal: true,
                buttons: {
                    "Yes": function() {
                        $( this ).dialog( "close" );
                    },
                    "No": function() {
                        $( this ).dialog( "close" );
                        box.attr('checked', false);
                    }
                }
            });
        }
    });

    new SelectAddMore().init({
        rel	 : 'template',
        url	 : baseUrl + '/templateEditor',
        vars	: 'entity,ontologies',
        label   : 'add / modify..',
        style   : 'modify',
        onClose : function() {
            refreshFlow();
        }
    });
    
	new SelectAddMore().init({
		rel	 : 'term',
		url	 : baseUrl + '/termEditor',
		vars	: 'ontologies',
		label   : 'add more...',
		style   : 'addMore',
		onClose : function(scope) {
            refreshFlow();
		}
	});

    new SelectAddMore().init({
        rel	 : 'person',
        url	 : baseUrl + '/person/list?dialog=true',
        vars	: 'person',
        label   : 'add / modify persons...',
        style   : 'modify',
        onClose : function() {
            refreshFlow();
        }
    });

    new SelectAddMore().init({
        rel	 : 'role',
        url	 : baseUrl + '/personRole/list?dialog=true',
        vars	: 'role',
        label   : 'add / modify roles...',
        style   : 'modify',
        onClose : function(scope) {
            refreshFlow();
        }
    });

    function refreshFlow() {
        StudyEdit.form.submit( 'studyProperties', 'refresh' ); return false;
    }

    // Initialize help tooltips
    attachHelpTooltips();
}

/*******************************************************************
 * 
 * Functions for date and timepickers 
 * 
 *******************************************************************/
StudyEdit.form = {
	initialize: function( selector ) {
		StudyEdit.form.attachDatePickers( selector );
		StudyEdit.form.attachDateTimePickers( selector );
	},
	
	//add datepickers to date fields
	attachDatePickers: function( selector ) {
		if( selector ) {
			elements = $( selector ).find("input[type=text][rel$='date']");
		} else {
			elements = $("input[type=text][rel$='date']");
		}
		elements.each(function() {
			$(this).datepicker({
				changeMonth : true,
				changeYear  : true,
				/*numberOfMonths: 3,*/
				showButtonPanel: true,
				dateFormat  : 'yy-mm-dd',
				yearRange   : 'c-80:c+20',
				altField	: '#' + $(this).attr('name') + 'Example',
				altFormat   : 'DD, d MM, yy'
			});
		});
	},
	
	//add datetimepickers to date fields
	attachDateTimePickers: function( selector ) {
		if( selector ) {
			elements = $( selector ).find("input[type=text][rel$='datetime']");
		} else {
			elements = $("input[type=text][rel$='datetime']");
		}
		elements.each(function() {
			$(this).datepicker({
				changeMonth	 : true,
				changeYear	  : true,
				dateFormat	  : 'yy-mm-dd',
				altField		: '#' + $(this).attr('name') + 'Example',
				altTimeField	: '#' + $(this).attr('name') + 'Example2',
				altFormat	   : 'DD, d MM, yy',
				showTime		: true,
				time24h		 : true
			});
		});
	},
	
	submit: function( formId, action ) {
		var form = $( 'form#' + formId );
		
		if( action != undefined ) {
			$( 'input[name=_action]', form ).val( action );
		}
			
		form.submit();
	}
}


StudyEdit.studyChildren = {
		refresh: function( table ) {
			if( table.length == 0 ) {
				location.reload();
				return;
			}
			$.each( table, function( idx, datatable ) {
				$(datatable).dataTable().fnDraw();
			});
		},
		initialize: function( entityMethods, title ) {
			var dialog = $( "#addDialog" );
			
			dialog.dialog({ 
				modal: true, 
				autoOpen: false,
				width: 900,
				title: title,
				buttons: {
					Ok: function() {
						entityMethods.save();
					},
					Cancel: function() {
						$( "#addDialog" ).dialog( "close" );
					}
				},
			});
			
			return dialog
		},
		add: function(  entityMethods, url ) {
			var dialog = $( "#addDialog" );
			 dialog.load( url, function() {
				entityMethods.onLoad();
				dialog.dialog( "open" );
			});
		},
		save: function() {
			var dialog = $( "#addDialog" );
			dialog.find( "[name=_action]" ).val( "save" );
			dialog.find( 'form' ).submit();
		},
		deleteItem: function( form, question ) {
			// Collect the ids from all datatables
		    var ids = []; 
		    for( tableId in StudyEdit.datatables.elementsSelected ) {
		    	ids = ids.concat( StudyEdit.datatables.elementsSelected[ tableId ] );
		    }
		    
		    var formFilled = ( ids.length > 0 );
		    
		    if( !formFilled ) {
		    	alert( "Please select one or more items to delete" );
		    	return;
		    }
		    
			if( confirm( question ) ) {
			    $.each( ids, function(idx, id) {
			        var input = $( '<input type="hidden" class="created" name="ids">');
			        input.attr( 'value', id );
			        form.append( input );
			    });
		
			    form.submit();
			}

			return false;
		},
		
		/**
		 * Handles loading new data into the popup dialog
		 */
		onLoad: function( entityMethods ) {
			var dialog = $( "#addDialog" );
			// Handle form with ajax
			//callback handler for form submit
			dialog.find( 'form' ).submit(function(e)
			{
			    var postData = $(this).serializeArray();
			    var form = $(this);
			    var formURL = $(this).attr("action");
			    
			    $.ajax({
			        url : formURL,
			        type: "POST",
			        data : postData
			    })
			    .done(function(data, textStatus, jqXHR) 
			        {
			    		if( jqXHR.status == 210 ) {
			    			// Everything is OK
			    			dialog.dialog( "close" );
			    			if( entityMethods.isTemplateOnScreen( data.templateId ) ) {
				    			// If the template for the added entities was already on the screen
				    			// we can just refresh the datatables
			    				entityMethods.refresh();
			    			} else {
				    			// Otherwise, we should refresh the whole page, because a new datatable was added
				    			location.reload();
			    			}
			    		} else {
			    			dialog.html( data );
			    			entityMethods.onLoad();
			    		}
			    		
			        })
			    .fail( function(jqXHR, textStatus, errorThrown) {
		    		dialog.html( jqXHR.responseText );   
		    		entityMethods.onLoad();
			        }
			    );
			    e.preventDefault(); //STOP default action
			});
			
			// Make sure to add the template and term editors
			// Add add/modify option again for all selects
			StudyEdit.addMore.initialize( "#" + dialog.attr( "id" ) );
			
			// Initialize datepickers
			StudyEdit.form.attachDatePickers( "#" + dialog.attr( "id" ) );

			// Initialize help icons
			attachHelpTooltips();
		},
		

}

/**
 * Handles adding and deleting subjects
 */
StudyEdit.subjects = {
	// Reload data for the datatable
	refresh: function() {
		StudyEdit.studyChildren.refresh( $( "#subjects .dataTables_scrollBody .dataTable" ) )
	},
	initialize: function() {
		StudyEdit.studyChildren.initialize( StudyEdit.subjects, "Add subject(s)" );
	},
	add: function() {
		StudyEdit.studyChildren.add( StudyEdit.subjects, $( '#subjects .add' ).data( 'url' ) );
	},
	save: function() {
		StudyEdit.studyChildren.save();
	},
	deleteItem: function() {
		StudyEdit.studyChildren.deleteItem( $( "#deleteSubjects" ), "Deleting these subjects will also delete all samples that originated from them. Are you sure you want to delete the subjects?" );
	},
	
	isTemplateOnScreen: function( templateId ) {
		return $( "#subjectsTable_" + templateId ).length > 0;
	},
	
	/**
	 * Handles loading new data into the popup dialog
	 */
	onLoad: function() {
		return StudyEdit.studyChildren.onLoad( StudyEdit.subjects );
	}
};

/**
 * Handles adding and deleting samples
 */
StudyEdit.samples = {
	// Reload data for the datatable
	refresh: function() {
		StudyEdit.studyChildren.refresh( $( "#samples .dataTables_scrollBody .dataTable" ) )
	},
	initialize: function() {
		StudyEdit.studyChildren.initialize( StudyEdit.samples, "Add sample(s)" );
	},
	add: function() {
		StudyEdit.studyChildren.add( StudyEdit.samples, $( '#samples .add' ).data( 'url' ) );
	},
	save: function() {
		StudyEdit.studyChildren.save();
	},
	deleteItem: function() {
		StudyEdit.studyChildren.deleteItem( $( "#deleteSamples" ), "Are you sure you want to delete the samples?" );
	},
	isTemplateOnScreen: function( templateId ) {
		return $( "#samplesTable_" + templateId ).length > 0;
	},
	
	
	/**
	 * Handles loading new data into the popup dialog
	 */
	onLoad: function() {
		return StudyEdit.studyChildren.onLoad( StudyEdit.samples );
	}
};

/**
 * Handles adding and deleting assays
 */
StudyEdit.assays = {
	// Reload data for the datatable
	refresh: function() {
		StudyEdit.studyChildren.refresh( $( "#assays .dataTables_scrollBody .dataTable" ) )
	},
	initialize: function() {
		StudyEdit.studyChildren.initialize( StudyEdit.assays, "Add assay(s)" );
	},
	add: function() {
		StudyEdit.studyChildren.add( StudyEdit.assays, $( '#assays .add' ).data( 'url' ) );
	},
	save: function() {
		StudyEdit.studyChildren.save();
	},
	deleteItem: function() {
		StudyEdit.studyChildren.deleteItem( $( "#deleteAssays" ), "Are you sure you want to delete the assays?" );
	},
	isTemplateOnScreen: function( templateId ) {
		return $( "#assaysTable_" + templateId ).length > 0;
	},
	
	/**
	 * Handles loading new data into the popup dialog
	 */
	onLoad: function() {
		return StudyEdit.studyChildren.onLoad( StudyEdit.assays );
	}
};

StudyEdit.spinner = {
	show: function( text ) {
		$('body').append( $( "<div>" ).addClass( "spinner overlay" ).append( $( "<div>" ).addClass( "message" ).text(text) ) );
	},
	hide: function() {
		$( ".overlay.spinner" ).remove();
	}
}
/*
 * This script can be used to initialize datatable behavior,  show wizard.
 * 
 * The datatable has a default layout, contains checkboxes to selected rows and is made
 * editable, if prototypes of the editable fields are available. The datatable is used serverside
 * to speed up the processing. Each datatable can only contain data for entities with the same template
 *
 * Usage:
 * 
 * 1. Create an HTML table with unique ID (important!). The table can have multiple arguments that are used within the datatable:
 * 		data-templateId: 	contains the templateId that is used within this table. This is used to lookup 
 * 							the specific field types for each field, if the editable behavior is initialized
 * 		data-fieldPrefix:	contains the prefix that is put in front of each field, sent to the server. If a field value is changed
 * 							and the data is saved, the data is sent to the server in the form [fieldPrefix].[rowId].[templateFieldName].
 * 							If the fieldPrefix is not present, the tableId is used
 * 		data-formId:		contains the ID of the form that is used to send data to the server. If not provided, the id <tableId>_form 
 * 							is used. If no form is present, no data will be saved.
 * 		rel:				contains the URL to lookup data from the server. The serverside method should be consistent with
 * 							the specifications from http://datatables.net/usage/server-side. The serverside data should contain 
 * 							the displayed data, just like a normal table (so no IDs of terms or listitems). Additionally, the first
 * 							column of each row should contain its unique ID.
 * 
 * 2. Create a THEAD within the table, with one TR and a TH for each field in the template. No field has to be included 
 * 	  for the checkbox column
 *    Each TH within the THEAD should contain a data-fieldname property, with the escaped name of the field (so it can 
 *    be used within HTML ids and names.
 *    
 * 3. (optional) Add a TFOOT with two TRs. One TR (with class 'selectAll') pops up if the user clicks the select all button
 * 	  and might want to select items in other pages as well.
 *    The other TR (with class 'saveChanges') pops up if the user has changed anything, and might want to save changes. 
 * 
 * 4. If you want editable behavior to be applied, you need to provide a DIV with prototypes of the input fields. The div must
 *    have ID <tableID>_prototype. For each field in the table, a editableFieldPrototype div must be present, with the ID 
 *    prototype_<templateID>_<escapedTemplateName>. The contents of that div are copied to each table cell, to enable editing.
 * 
 * An example:
 * 
	<table id="subjectsTable_10511" data-templateId="10511" data-fieldPrefix="subject" data-formId="subjectForm" class="subjectsTable selectMulti" rel="/gscf/studyEdit/dataTableSubjects/10674?template=10511">
		<thead>
			<tr>
				<th data-fieldname="name">name</th>
				<th data-fieldname="genotype_type">Genotype type</th>
			</tr>
		</thead>
		<tfoot>
			<tr class="messagebar selectAll">
				<td  colspan="13">
					You selected all items on this page. Would you <a href="#">select all items on other pages</a> as well? 
				</td>
			</tr>						
			<tr class="messagebar saveChanges">
				<td class="" colspan="13">
					<a href="#" onClick="StudyView.datatables.editable.save(this); return false">Save</a>
					<span class="saving">Saving...</span>
				</td>
			</tr>
		</tfoot>
	</table>
	
	<div id="subjectsTable_10511_prototype" style="display: none" class="editable prototype">
		<div class="editableFieldPrototype" id="prototype_10511_name">
			<input type="text" description="Name" name="name" value="" required="true" id="name" />
		</div>
		<div class="editableFieldPrototype" id="prototype_10511_genotype_type">
			<select description="Genotype type" name="genotype_type" required="false" id="genotype_type" >
				<option value="wildtype" >wildtype</option>
				<option value="transgenic" >transgenic</option>
				<option value="knock-out" >knock-out</option>
				<option value="knock-in" >knock-in</option>
			</select>
		</div>
	</div>
							
	<form action="/gscf/studyEdit/editSubjects" method="post" name="subjectForm" id="subjectForm" >
		<input type="hidden" class="original" name="id" value="10674" id="id" />
	</form>							
 * 
 */

if( typeof( StudyView ) === "undefined" ) { 
	StudyView = {};
};

StudyView.datatables = {
	numElements: new Array(),		// Hashmap with the key being the id of the table, in order to facilitate multiple tables
	elementsSelected: new Array(),	// Hashmap with the key being the id of the table, in order to facilitate multiple tables
	tableType: new Array(),			// Hashmap with the key being the id of the table, in order to facilitate multiple tables
	selectType: new Array(),       	// Hashmap with the key being the id of the table, in order to facilitate multiple tables
	allElements: new Array(),		// Hashmap with the key being the id of the table, in order to facilitate multiple tables

	initialize: function( selector ) {
		if( selector == undefined ) {
			selector = ''
		}
	
		// Initialize serverside pagination and (if needed) editable behavior
		$( selector ).each(function(idx, el) {
			var $el = $(el);
			
			// Determine data url from rel attribute
			var dataUrl = $el.attr('rel');
			var id = $el.attr( 'id' );
			
			tableType[ id ] = "serverside";
			StudyView.datatables.elementsSelected[ id ] = new Array();
			
			// Determine default options
			var defaultOptions = { 
				"bProcessing": true,
				"bServerSide": true,
				"sAjaxSource": dataUrl,
				sDom: '<"H"lf>rt<"F"ip>',
	
				bJQueryUI: true, 
				bAutoWidth: false,
				bFilter: true, 
				bLengthChange: true, 
				bPaginate: true,
				bSort: true,
				bInfo: !$el.hasClass( 'hideInfo' ),
				iCookieDuration: 86400,				// Save cookie one day
				sPaginationType: 'full_numbers',
				iDisplayLength: 10,					// Number of items shown on one page.
				aoColumnDefs: [
					{ "bSortable": false, "aTargets": ["nonsortable"] },				// Disable sorting on all columns with th.nonsortable
					{ "sSortDataType": "formatted-num", "aTargets": ["formatted-num"] }	// Make sorting possible on formatted numbers
				],
	           			
				// Override the fnServerData in order to show/hide the paginated
				// buttons if data is loaded
				"fnServerData": function ( sSource, aoData, fnCallback ) {
					StudyView.datatables.retrieveData( sSource, aoData, fnCallback, id );

				},
			};
			
			// Check if the datatables has extended options
			var opts;
			
			if( $el.data( "datatable-options" ) ) {
				opts = $.extend( true, {}, defaultOptions, $el.data( "datatable-options" ) );	
			} else {
				opts = defaultOptions;	
			}
			
			// Convert into datatale
			$el.dataTable(opts);
		});
	},
	
	destroy: function( id ) {
		delete this.numElements[id];
		delete this.elementsSelected[id];
		delete this.tableType[id];
		delete this.selectType[id];
		delete this.allElements[id];
	},
	
	retrieveData: function( sSource, aoData, fnCallback, id ) {
        if( StudyView.datatables.selectType[ id ] != "selectNone") {
            aoData = StudyView.datatables.removeColumnInParam(aoData);
        }
		$.ajax( {
			"dataType": 'json', 
			"type": "POST", 
			"url": sSource, 
			"data": aoData, 
			"success": function( data, textStatus, jqXHR ) {
				fnCallback( data, textStatus, jqXHR );
				
				// Save total number of elements
				StudyView.datatables.numElements[ id ] = data[ "iTotalRecords" ];
				StudyView.datatables.allElements[ id ] = data[ "aIds" ];
				
				// Find which checkboxes are selected
                if(StudyView.datatables.selectType[ id ] != "selectNone") {
				    StudyView.datatables.selection.checkSelectedCheckboxes( id );
                }
			}
		} );		
	},

	removeColumnInParam: function( aoData ) {
	    var arrParam = new Array("bSearchable_","sSearch_","bRegex_","bSortable_","mDataProp_");
	
	    for(var i = 0; i < aoData.length; i++ ) {
	        var key = aoData[i].name;
	        for(var j = 0; j < arrParam.length; j++ ) {
	            if(key.indexOf(arrParam[j]) != -1) {
	                var iNum = parseInt(key.replace(arrParam[j],""));
	                if(iNum==0) {
	                    aoData[i].name = "aa";
	                } else {
	                    iNum = iNum -1;
	                    aoData[i].name = arrParam[j]+iNum;
	                }
	                break;
	            }
	        }
	        if(key.indexOf("iSortCol_0") != -1) {
	            var iNum = parseInt(aoData[i].value);
	            if(iNum>0) {
	                aoData[i].value = iNum-1;
	            }
	        }
	        if(key.indexOf("iColumns") != -1) {
	            var iNum = parseInt(aoData[i].value);
	            aoData[i].value = iNum-1;
	        }
	    }
	    return aoData;
	},
	
	/**********************************************************************
	 * 
	 * These functions are used to provide editable behavior to the datatable
	 * 
	 * initialize				  	Initializes editable behavior on the datatable. 
	 * markChanged					Marks a specific input field as changed
	 * propagateChange				Propagates the change in a specific input field to other
	 * 								selected rows.				
	 * save							Saves the editable form 
	 **********************************************************************/
	editable: {

		/**
		 * Initializes the editable behavior on a datatable. This requires having
		 * a div with the prototypes of input fields for each field in the template.
		 * Within the div, for each element the HTML used for a field should have the id
		 * 		prototype_<templateId>_<fieldName>
		 */
		initialize: function( element ) {
			$(element).each(function( tableIndex, table ) {
				var rowHeaders = $( "thead th", table );
				var tableId = $(table).attr( "id" );
				
				$(table).data( "datatable-options", {
					"sScrollX": "100%",
					"bScrollCollapse": true,
					
					bFilter: true, 
					bLengthChange: true, 
					bSort: true,
					bInfo: true,
				    aoColumnDefs: [
				      { sWidth: "30px", aTargets: [ 0 ] }
				    ],
				    fnDrawCallback: function() {
						// Initialize field prototypes
						StudyView.datatables.editable.fields.initialize( tableId );
				    },
				    fnRowCallback: function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
				    	// The rowId is retrieved from the first cell within the
				    	// row, since the items have not been altered into editable fields yet
				    	var rowId = $( "td:first-child", nRow ).text();
						$.each( $( "td", nRow ), function( idx, td ) {
							// Don't update first ID column
							if( idx == 0 )
								return
							
							var fieldName = $( rowHeaders.get( idx ) ).data( "fieldname" );
							StudyView.datatables.editable.cloning.cloneField( tableId, rowId, fieldName, $(td), aData[idx] );

						});
				    }
				});
			});		
		},
		
		cloning: {
			cloneField: function( tableId, rowId, fieldName, td, value ) {
				// Check whether we have a prototype of an input for this type of data
				var table = $( '#' + tableId );
				var prototypeId = "#prototype_" + $(table).data( "templateid" ) + "_" + fieldName;
				var fieldPrefix = StudyView.datatables.editable.getFieldPrefix( tableId );
				
				// Cloning the field depends on the type: file fields are handled differently
				// from other fields
				var fieldType = $( prototypeId ).data( "fieldtype" );
				var fieldId = fieldPrefix + "." + rowId + "." + fieldName;
				if( fieldType == "FILE" ) {
					// Append the cloned input elements
					$( td ).empty().html("<div class=\"fileWrapper\"></div>")
                    $(td).find("div").html(
                          StudyView.datatables.editable.cloning.cloneFileField( prototypeId, fieldId, value, rowId, tableId )
                    );
				} else {
					// Append the cloned input elements
					$( td ).empty().append(
						StudyView.datatables.editable.cloning.cloneDefaultField( prototypeId, fieldId, value, rowId, tableId, fieldType )
					);
				}
			},
			
			cloneFileField: function( prototypeId, fieldId, value, rowId, tableId ) {
				var field = $( prototypeId ).children().clone();
				var fieldName = fieldId;
				var fieldId = fieldId.replace( /\./g, "_" );
				
				// Update the hidden field
				field.filter( "input" )
					.attr( "name", fieldName )
					.attr( "id", fieldId )
					.addClass( "fileField" )
					.val( value ? "existing*" + value : "" )
					.data( "original", value )
					.data( "prototype", prototypeId )
					.on( "change", function( event ) {
						// Mark this cell as being changed
						StudyView.datatables.editable.markChanged( event.target );
						
						// Change other cells as well, if this row is selected
						var selectedIds = StudyView.datatables.elementsSelected[ tableId ];
						if( $.inArray( parseInt( rowId ), selectedIds ) > -1 ) {
							var td = $(event.target).closest( "td" );
							StudyView.datatables.editable.propagateChangeInFileField( td, selectedIds );
						}
					});
				
				// Update the delete button
				field.filter( ".upload_del" )
					.attr( "id", fieldId + "Delete" )
					.attr( "onclick", "" )
					.on( "mousedown", function(e) {
						e.stopPropagation();
						if( confirm( "Are you sure you want to delete this file?" ) ) {
							FileUpload.deleteFile( fieldId );

							// Mark this cell as being changed
							StudyView.datatables.editable.markChanged( $(event.target).parent() );
							
							// Change other cells as well, if this row is selected
							var selectedIds = StudyView.datatables.elementsSelected[ tableId ];
							if( $.inArray( parseInt( rowId ), selectedIds ) > -1 ) {
								var td = $(event.target).closest( "td" );
								StudyView.datatables.editable.propagateChangeInFileField( td, selectedIds );
							}							
						}
						return false;
					})
					.toggle( value != "" );
					
				// Update the info field
				field.filter( ".upload_info" )
					.attr( "id", fieldId + "Example" )
					.html( value ? "File: " + FileUpload.createFileHTML( value ) : "" );

				// Update the upload button
				field.filter( ".upload_button" )
					.attr( "id", "upload_button_" + fieldId );

				// Update the upload icon
				field.filter( ".upload_icon" )
					.attr( "id", "upload_icon_" + fieldId );

				// Connect the click event of the icon to the (hidden) button
				$('#upload_icon_' + fieldId).on('click', function() {
					$('#upload_button_' + fieldId).click();
					return false;
				})

				return field;
			},
			
			cloneDefaultField: function(prototypeId, fieldId, value, rowId, tableId, fieldType ) {
				fieldInput = $( prototypeId ).children().clone();
				
				// If so, store it and set the correct value
				if( fieldInput.length > 0 ) {
					// Check whether this value is required, but not entered
					if( fieldInput.is( "[required=true]" ) && !value ) {
						$td.addClass( "invalid" );
					}
					
					fieldInput
						.attr( "name", fieldId )
						.attr( "id", fieldId )
						.val( value )
						.data( "original", value )
						.data( "prototype", prototypeId );
					
					StudyView.datatables.editable.fields.addEventsToInput( fieldInput, tableId, rowId );
					
					// Handle special cases:
					// checkbox must have a value and be checked if given value != empty
					if( fieldType == "BOOLEAN" ) {
						fieldInput
							.val( "on" )
							.attr( "checked", value != "false" );
					}
				}
				
				return fieldInput;
			}
		
		},
		
		fields: {
			// Initialize editable fields in the datatable, after they have been drawn on the screen
			initialize: function( tableId ) {
				var prototypeSelector = "#" + tableId ;
				
				// Handle date pickers
				StudyView.form.attachDatePickers( prototypeSelector );
				StudyView.form.attachDateTimePickers( prototypeSelector );
				
				// Handle extendable lists
				StudyView.datatables.editable.fields.updateAutoCompletes( prototypeSelector );
				
				// Initialize file upload fields
				StudyView.datatables.editable.fields.initializeFileFields( prototypeSelector );
				
				// Initialize ontology and template fields
				StudyView.datatables.editable.fields.initializeSelectAddMore( prototypeSelector );
			},
			
			/**
			 * Adds onChange and keyUp events to an input fields that handle changes 
			 * and pressing enter or escape
			 */
			addEventsToInput: function( input, tableId, rowId ) {
				StudyView.datatables.editable.fields.addChangeEventToInput( input, tableId, rowId );
				StudyView.datatables.editable.fields.addKeyEventToInput( input );
			},

			/**
			 * Adds onChange and keyUp events to an input fields that handle changes 
			 * and pressing enter or escape
			 */
			addChangeEventToInput: function( input, tableId, rowId ) {
				input
					.on( "change", function( event ) {
						// Mark this cell as being changed
						StudyView.datatables.editable.markChanged( event.target );
						
						// Change other cells as well, if this row is selected
						var selectedIds = StudyView.datatables.elementsSelected[ tableId ];
						if( $.inArray( parseInt( rowId ), selectedIds ) > -1 ) {
							StudyView.datatables.editable.propagateChange( event.target, selectedIds );
						}
					});
			},

			/**
			 * Adds onChange and keyUp events to an input fields that handle changes 
			 * and pressing enter or escape
			 */
			addKeyEventToInput: function( input ) {
				input
					.on( "keyup", function( e ) {
						// Handle esc and enter key presses
	                    if (e.keyCode == 27) {
	                        e.preventDefault();
	                        
	                        var input = $(e.target);
	                        input.val( input.data( "original" ) );
							input.closest( "td" ).removeClass( "changed" );
	                        
	                    }
	                    if (e.keyCode == 13) {
	                        e.preventDefault();
	                        
	                        StudyView.datatables.editable.save(e.target);
	                    }
	                });
			},
			
			updateAutoCompletes: function( selector ) {
				$(selector).find( ".ui-autocomplete-input" ).each( function( idx, el ) {
					// Reinitialize autocompletes, since they break when cloned. See http://stackoverflow.com/questions/13664020/jquery-autocomplete-and-clone
					$(el).autocomplete({
						source: $( $(el).data( "prototype" ) ).find( ".ui-autocomplete-input" ).autocomplete( "option", "source" )
					});
				});
			},
			
			initializeFileFields: function( selector ) {
				$(selector).find( ".fileField" ).each( function( idx, el ) {
					var fieldId = $(el).attr( "id" );
					FileUpload.convertFileField( fieldId );
					$(el).removeClass( "fileField" );
				});
			},
			
			initializeSelectAddMore: function( selector ) {
				StudyView.datatables.editable.fields.initializeSelectAddMoreTerms( selector );
				StudyView.datatables.editable.fields.initializeSelectAddMoreTemplates( selector );
			},
			
			initializeSelectAddMoreTerms: function( selector ) {
				new SelectAddMore().init({
					rel	 : 'term',
					selector: selector,
					url	 : baseUrl + '/termEditor',
					vars	: 'ontologies',
					label   : 'add more...',
					style   : 'addMore',
					onClose : function(scope) {
						StudyView.datatables.editable.fields.reloadPrototypes( selector, "term" );
					}
				});
			},

			initializeSelectAddMoreTemplates: function( selector ) {
				// handle template selects
				new SelectAddMore().init({
					rel	 : 'template',
					selector: selector,
					url	 : baseUrl + '/templateEditor',
					vars	: 'entity,ontologies',
					label   : 'add / modify..',
					style   : 'modify',
					onClose : function(scope) {
						StudyView.datatables.editable.fields.reloadPrototypes( selector, "template" );
					}
				});
			},

			
			/**
			 * Reloads the prototyped input fields, and propagates changes in selectboxes
			 * to the fields in the datatable. This can be used after the term-editor or
			 * tempalte-editor have been called.
			 */
			reloadPrototypes: function( selector, rel ) {
				var templateId = $( selector ).data( "templateid" );
				
				$.get( baseUrl + '/studyEdit/prototypes/' + templateId )
					.done( function( data, textStatus, jqXhr ) {
						// Store the newly created prototypes of select boxes in HTML
						$(data).find( "select[rel=" + rel + "]" ).each( function( idx, field ) {
							var $field = $(field);
							var $div = $field.parent();
							
							// Replace the prototype
							$( "#" + $field.attr( "id" ) ).replaceWith( $field );
						});
						
						// Update all selects in the datatable
						$( selector ).find( "tbody select[rel=" + rel + "]" ).each( function( idx, select ) {
							// Store old value
							var oldValue = $(select).val();
							
							// Get new items from the prototype, and store them
							var prototypeId = $(select).data( 'prototype' );
							
							if( prototypeId ) {
								$(select).html( $(prototypeId).html() );
							}
							
							// Restore old value only if add/modify was not selected
							if( oldValue != '' ) {
								$(select).val( oldValue );
							}
						});
						
						// Add add/modify option again for all selects
						if( rel == "template" ) {
							StudyView.datatables.editable.fields.initializeSelectAddMoreTemplates( selector );
						} else if( rel == "term" ) {
							StudyView.datatables.editable.fields.initializeSelectAddMoreTerms( selector );
						}
												
						
					});
			},
			
		},
		
		/**
		 * Marks an input field as changed
		 */
		markChanged: function( input ) {
			$(input).parents( "td" ).first().addClass( "changed" );
			$(input).parents( ".dataTables_wrapper" ).first().find( ".saveChanges td" ).slideDown( 100 );
			$(input).parents( ".dataTables_wrapper" ).first().find( ".dataTables_scrollBody .dataTable" ).data( "changed", true );
		},

		/**
		 * Propagates the change in a given input field to other 
		 */
		propagateChange: function( input, selectedIds ) {
			var value = $(input).val();
			
			var nameParts = $(input).attr( "name" ).split( "." );
			var container = $(input).closest( ".dataTables_wrapper" );
			
			// Check for each selectedId, if a field with this name already exists,
			// change that field. Otherwise, create a new field
			$.each( selectedIds, function( idx, id ) {
				// Skip the current one
				if( id == nameParts[ 1 ] ) {
					return true;
				}
				
				// If the field exists, update it
				var fieldName = [ nameParts[ 0 ], id, nameParts[ 2 ] ].join( "." );
				var escapedFieldName = fieldName.replace( /\./g, "\\." );	// Escape dots, because they have special meaning in jquery selectors
				var field = $( "[name=" + escapedFieldName + "]" );
				
				if( $(input).is( "[type=checkbox]" ) ) {
					value = $(input).is( ":checked" ) ? "on" : "";
				}
				
				if( field.length > 0 ) {
					field.val( value );
					
					if( $(input).is( "[type=checkbox]" ) ) {
						field.attr( "checked", $(input).is( ":checked" ) );
					}
					
					// Only mark visible fields as changed
					if( !field.is( ".hiddenAndChanged" ) ) {
						StudyView.datatables.editable.markChanged( field );
					}
				} else {
					// Add a hidden field to the wrapper div
					container.append( 
							$( "<input type='hidden' />" )
								.attr( "name", fieldName )
								.addClass( "hiddenAndChanged" )
								.val( value )
						);
				}
			});
		},

		/**
		 * Propagates the change in a given input field to other 
		 */
		propagateChangeInFileField: function( td, selectedIds ) {
			var input = td.find( "input" );
			var value = input.val();
			
			var nameParts = $(input).attr( "name" ).split( "." );
			var container = $(input).closest( ".dataTables_wrapper" );
			
			// Check for each selectedId, if a field with this name already exists,
			// change that field. Otherwise, create a new field
			$.each( selectedIds, function( idx, id ) {
				// Skip the current one
				if( id == nameParts[ 1 ] ) {
					return true;
				}
				
				// If the field exists, update it
				var fieldName = [ nameParts[ 0 ], id, nameParts[ 2 ] ].join( "." );
				var escapedFieldName = fieldName.replace( /\./g, "\\." );	// Escape dots, because they have special meaning in jquery selectors
				var field = $( "[name=" + escapedFieldName + "]" );
				if( field.length > 0 ) {
					if( field.is( ".hiddenAndChanged" ) ) {
						// Hidden fields will only have to be changed, but nothing visual has to be done
						field.val( value );
					} else {
						// For visible fields, many things have to be changed
						var otherTd = field.closest( "td" );

						// Update the field
						field.val( value );
						
						// Update info text and button
						if( value == "" || value == "*deleted*" ) {
							otherTd.find( ".upload_del" ).hide();
							otherTd.find( ".upload_info" ).html( "" );
						} else {
							otherTd.find( ".upload_del" ).show();
							otherTd.find( ".upload_info" ).html( FileUpload.createFileHTML(value) );
						}
						
						StudyView.datatables.editable.markChanged( field );
					}
				} else {
					// Add a hidden field to the wrapper div
					container.append( 
						$( "<input type='hidden' />" )
							.attr( "name", fieldName )
							.addClass( "hiddenAndChanged" )
							.val( value )
					);	
				}
			});
		},		
		/**
		 * Submits data from an editable studyedit datatable, to the server using ajax
		 */
		save: function( link ) {
			var wrapper = $(link).parents( ".dataTables_wrapper" );
			var newData = {};
			
			// If nothing has changed, just reset all fields
			if( wrapper.find( "td.changed" ).length == 0 ) {
				return;
			}
			
			// Show a message that the system is saving data in the status bar
			wrapper.find( ".saveChanges .links" ).hide();
			wrapper.find( ".saveChanges .saving" ).show();

			// Copy all data into the form
			wrapper.find( ".changed input, .changed select, .changed textarea" ).each( function( idx, el ) {
				// Checkboxes must be handled differently
				if( $(el).is( "[type=checkbox]" ) ) {
					newData[ $(el).attr( "name" ) ] = $(el).is( ":checked" ) ? $(el).val() : "";
				} else {
					newData[ $(el).attr( "name" ) ] = $(el).val();
				}
			});
			
			// Add data from the hidden fields (fields on other pages that have been changed
			// because they were selected
			wrapper.find( ".hiddenAndChanged" ).each( function( idx, el ) {
				$el = $(el);
				newData[ $el.attr( "name" ) ] = $el.val();
				$el.remove();
			});
			
			// Make sure all inputs are disabled during save. When the datatable is refreshed,
			// these fields will be editable again
			wrapper.find( "tbody input, tbody select, tbody textarea" ).attr( "disabled", true );
			
			// Send the data to the server
			var table = wrapper.find( ".dataTables_scrollBody .dataTable" );
			var formId = StudyView.datatables.editable.getFormId( table.attr( "id" ) );
			var form = $( "#" + formId );
			newData[ "id" ] = form.find( "[name=id]" ).val();
			
			$.post( form.attr( "action" ), newData )
				.fail( function() { 
					StudyView.datatables.editable.showError( table.attr( "id" ), "An unknown error occurred while saving your data. Please try again." );
				} )
				.always( function(data) {
					// TODO: handle the case that the save failed
					if( data.errors ) {
						StudyView.datatables.editable.showError( table.attr( "id" ), "An error occurred while saving your data. Please try again." );
					}
					
					// Set the 'changed' flag to false
					wrapper.find( ".dataTables_scrollBody .dataTable" ).data( "changed", false );
					
					// Reload data for the datatable
					table.dataTable().fnDraw();
					
					// Clear the uploaded fields in the form
					form.find( ":not(.original)" ).remove();
					
					// Reset the saveChanges row
					wrapper.find( ".saveChanges .links" ).show();
					wrapper.find( ".saveChanges .saving" ).hide();
					wrapper.find( ".saveChanges td" ).slideUp(100);
				});
		},
		
		/**
		 * Discards all changes in an editable datatable
		 */
		discardChanges: function(link) {
			var wrapper = $(link).parents( ".dataTables_wrapper" );

			// Reset all changed fields
			wrapper.find( "td.changed" ).each( function( idx, el ) {
				$td = $(el);
				
				$td.find( "input, select, textarea" ).each( function( inputIdx, input ) {
					if( $(input).is( "[type=checkbox], [type=radio]" ) ) {
						$(input).attr( "checked", $(input).data( "original" ) != "" && $(input).data( "original" ) != "false" );
					} else {
						$(input).val( $(input).data( "original" ) );
					}
				});
				$td.removeClass( "changed" );
			});
			
			// Set the 'changed' flag to false
			wrapper.find( ".dataTables_scrollBody .dataTable" ).data( "changed", false );
			
			// Clear the uploaded fields in the form
			var table = wrapper.find( ".dataTables_scrollBody .dataTable" );
			
			var formId = StudyView.datatables.editable.getFormId( table.attr( "id" ) );
			var form = $( "#" + formId );			
			form.find( ":not(.original)" ).remove();
			
			wrapper.find( ".hiddenAndChanged" ).remove();
			
			// Reset the saveChanges row
			wrapper.find( ".saveChanges .links" ).show();
			wrapper.find( ".saveChanges .saving" ).hide();
			wrapper.find( ".saveChanges td" ).slideUp(100);			
			
		},

		showError: function( tableId, message ) {
			var table = $( '#' + tableId );
			var wrapper = table.parents( ".dataTables_wrapper" );
			var footer = wrapper.find( ".dataTables_scrollFoot .dataTable tfoot" );
			var numColumns = table.find( "tr" ).first().children().length;

			var td = $( "<td>" )
			.attr( "colspan", numColumns )
			.text( message )
			.append( 
				$( "<a>" )
					.attr( "href", "#" )
					.addClass( "close" )
					.text( "x" )
					.on( "click", function() {
						// Hide the error bar
						td.slideUp( 100, function() {
							tr.remove();
						});
					})
			);	
		
			var tr = $( "<tr>" )
				.addClass( "messagebar" )
				.addClass( "errorbar" )
				.append( td );

			footer.prepend( tr );
			td.slideDown( 100 );
		},
		
		/**
		 * Returns the fieldprefix used for fields within a table
		 */
		getFieldPrefix: function( tableId ) {
			var table = $( '#' + tableId );
			var fieldPrefix = table.data( "fieldprefix" );
			if( !fieldPrefix ) 
				fieldPrefix = tableId;

			return fieldPrefix;
		},
		
		/**
		 * Returns the formId to use to submit data from a specific table
		 */
		getFormId: function( tableId ) {
			var table = $( '#' + tableId );
			var formId = table.data( "formid" );
			if( !formId )
				formId = table.attr( "id" ) + "_form";
			
			return formId;
		},
		
	},
	
	/**********************************************************************
	 * 
	 * These function are used for handling selectboxes and select-all boxes. In fact, there are
	 * four methods:
	 * 
	 * checkSelectedCheckboxes  	checks selectboxes based on the ids previously selected (when 
	 * 								showing a new page in a serverside paginated table)
	 * checkAllPaginated (cs & ss)	handles a click on the 'checkAll' button: checks all items if
	 * 								not all items were selected, and deselects all items if all
	 * 								items were selected
	 * updateCheckAll (cs & ss)		updates the checkAll checkbox so it shows the current status:
	 * 								checked if everything is selected, checked but transparent if
	 * 								some items are selected and deselected if no items are selected
	 * submitPaginatedForm			submits a form with the selected selectboxes in it.
	 * 
	 **********************************************************************/
	selection: {

		/**
		 * Initializes the select boxes for a datatables
		 */
		initialize: function( selector ) {
		    var $el = $(selector);
		
		    var id = $el.attr( 'id' );
		
		    if($el.hasClass( 'selectMulti' )) {
		    	StudyView.datatables.selectType[ id ] = "selectMulti";
		        $("#"+ id + ' thead tr').prepend("<th class='selectColumn nonsortable'><input id='"+id+"_checkAll' class='checkall' type='checkbox' onClick='StudyView.datatables.selection.clickCheckAll(this);'></th>");
		        $("#"+ id + ' tbody tr').each(function(idxrow, row) {
		            if($(row).attr('id') == null) {
		                alert("No [id] in the tbody:tr found. Each row needs an unique id that is passed as value of the checkbox. Please report this error to your system administrator.");
		                rowid = -1;
		            } else {
		                rowid = $(row).attr('id');
		                rowid = rowid.replace("rowid_","");
		            }
		            
		            var input = $( "<input id='"+id+"_ids' type='checkbox' value='"+rowid+"' class='selectable-datatable-checkbox' name='"+id+"_ids'>" );
			        input.on( "click", function() {
			        	StudyView.datatables.selection.clickRow(this);
			        });
			        $(row).prepend( $( "<td class='selectColumn'></td>" ).append( input ) );
		        });
		    } else if($el.hasClass( 'selectOne' )) {
		    	StudyView.datatables.selectType[ id ] = "selectOne";
		        $("#"+ id + ' thead tr').prepend("<th class='selectColumn nonsortable'></th>");
		        $("#"+ id + ' tbody tr').each(function(idxrow, row) {
		            if($(row).attr('id') == null) {
		                alert("No [id] in the tbody:tr found. Each row needs an unique id that is passed as value of the radio. Please report this error to your system administrator.");
		                rowid = -1;
		            } else {
		                rowid = $(row).attr('id');
		                rowid = rowid.replace("rowid_","");
		            }
		            
		            var input = "<input id='"+id+"_ids' type='radio' class='selectable-datatable-checkbox' value='"+rowid+"' name='"+id+"_ids'>";
			        input.on( "click", function() {
			        	StudyView.datatables.selection.clickRow(this);
			        });
			        $(row).prepend( $( "<td class='selectColumn'></td>" ).append( input ) );
		        });
		    } else {
		    	StudyView.datatables.selectType[ id ] = "selectNone";
		    }
		    
		    // Make sure the select all link works
		    $el.find( ".selectAll a" ).on( "click", function() {
		    	StudyView.datatables.selection.selectAll( id ); return false;
		    	$(this).closest( "td" ).slideUp( 100 );
		    } );
		    
		},
		
		/**
		 * Handle a click on an input to select a specific row
		 */
		clickRow: function(inputrow ) {
		    var input = $(inputrow);

		    var wrapper = $(input).closest( '.dataTables_wrapper' );
		    var paginatedTable = wrapper.find( ".dataTables_scrollBody .dataTable" );
			var dataTable = wrapper;
			var tableId = paginatedTable.attr( 'id' );
			var checked = input.attr( 'checked' );
			
			// If the input is a normal checkbox, the user clicked on it. Update the elementsSelected array
			if( StudyView.datatables.selectType[ tableId ] == "selectMulti" ) {
				if( checked ) {
					input.closest( "tr" ).addClass( "ui-selected" );
				} else {
					input.closest( "tr" ).removeClass( "ui-selected" );
				}
				StudyView.datatables.selection.select( tableId, parseInt( input.val() ), checked );
		        StudyView.datatables.selection.updateCheckAll( inputrow );
			} else {
		        // Assumption: selectType[ tableId ] == "selectOne"
				StudyView.datatables.elementsSelected[ tableId ][0] = parseInt( input.val() );
		    }
			
			// Disable the 'select all pages' button
			wrapper.find( ".selectAll td" ).slideUp( 100 );
		},

		/**
		 * Selects or deselects a given id within a table
		 */
		select: function( tableId, id, flag ) {
			var arrayPos = jQuery.inArray( id, StudyView.datatables.elementsSelected[ tableId ] );
			
			if( flag ) {
				// Put the id in the elementsSelected array, if it is not present
				if( arrayPos == -1 ) {
					StudyView.datatables.elementsSelected[ tableId ][ StudyView.datatables.elementsSelected[ tableId ].length ] = id;
				}
			} else {
				// Remove the id from the elementsSelected array, if it is present
				if( arrayPos > -1 ) {
					StudyView.datatables.elementsSelected[ tableId ].splice( arrayPos, 1 );
				}
			}
		},
		
		/**
		 * Handle a click on the checkAll checkbox
		 */
		clickCheckAll: function( input ) {
			var wrapper = $(input).closest( '.dataTables_wrapper' );
		    var paginatedTable = wrapper.find( ".dataTables_scrollBody .dataTable" );
		    var tableId = paginatedTable.attr( 'id' );
			var checkAll = $( '#'+tableId+'_checkAll', wrapper );
		
		    var inputsOnScreen = $( 'tbody input.selectable-datatable-checkbox', paginatedTable );
		
		    if( checkAll.attr( 'checked' ) ) {
		        // Select all on current page
		        for( var i = 0; i < inputsOnScreen.length; i++ ) {
		            var input = $(inputsOnScreen[ i ] );
		            if( !input.hasClass( "checkall" ) ) {
		                input.attr( 'checked', true );
		                StudyView.datatables.elementsSelected[ tableId ][ StudyView.datatables.elementsSelected[ tableId ].length ] = parseInt( input.val() );
		            }
		        }
		        
		        // Show a message whether the user wants to select all items on all pages
		        if( 
		        		typeof( StudyView.datatables.allElements[ tableId ] ) != "undefined" && 
		        		StudyView.datatables.allElements[ tableId ].length >  StudyView.datatables.elementsSelected[ tableId ].length 
		        ) {
		        	wrapper.find( ".selectAll td" ).slideDown( 100 );
		        }
		        
		    } else {
		        // Deselect all on current page
		        for( var i = 0; i < inputsOnScreen.length; i++ ) {
		            var input = $(inputsOnScreen[ i ] );
		            if( !input.hasClass( "checkall" ) ) {
		                var arrPos = jQuery.inArray( parseInt( input.val() ), StudyView.datatables.elementsSelected[ tableId ] );
		                if( arrPos > -1 ) {
		                    input.attr( 'checked', false );
		                    StudyView.datatables.elementsSelected[ tableId ].splice( arrPos, 1 );
		                }
		            }
		        }
		        checkAll.removeClass( 'transparent' );
		        
				// Disable the 'select all pages' button
				wrapper.find( ".selectAll td" ).slideUp( 100 );
		    }
		    
		    // The ui-selected class is used by the selectable element, and is not needed 
		    // for the internal selection method. If the classes remain on the row, the checkbox
		    // will be checked again after selecting one or more rows with selectable.
		    $( 'tbody tr', paginatedTable ).removeClass( "ui-selected" );
		    
		    StudyView.datatables.selection.updateCheckAll( input );
		},


		/**
		 * Deselect everything
		 */
		selectAll: function( tableId ) {
			if( typeof( StudyView.datatables.allElements[ tableId ] ) != "undefined" ) {
			    var paginatedTable = $( "#" + tableId );
				var wrapper = paginatedTable.closest( '.dataTables_wrapper' );
				var checkAll = $( '#'+tableId+'_checkAll', wrapper );
			    var inputsOnScreen = $( 'tbody input.selectable-datatable-checkbox', paginatedTable );			
				
				// Add elements to internal storage
				StudyView.datatables.elementsSelected[ tableId ] = StudyView.datatables.allElements[ tableId ].slice(0);
				
				// Make sure the inputs are selected
				inputsOnScreen.attr( "checked", true );
				
				// Update the checkAll button
				checkAll.removeClass( "transparent" ).attr( "checked", true );
				
				// Update the labels
				StudyView.datatables.selection.updateLabel(tableId);
				wrapper.find( ".selectAll td " ).slideUp( 100 );
			}
		},		
		
		/**
		 * Deselect everything
		 */
		deselectAll: function( tableId ) {
		    var paginatedTable = $( "#" + tableId );
			var wrapper = paginatedTable.closest( '.dataTables_wrapper' );
			var checkAll = $( '#'+tableId+'_checkAll', wrapper );
		    var inputsOnScreen = $( 'tbody input.selectable-datatable-checkbox', paginatedTable );			
			
			// Remove elements from internal storage
			StudyView.datatables.elementsSelected[ tableId ] = new Array();
			
			// Make sure the inputs are deselected
			inputsOnScreen.attr( "checked", false );
			inputsOnScreen.each( function( index, input ) {
				$(input).closest( "tr" ).removeClass( "ui-selected" );
			});
			
			// Update the checkAll
			checkAll.removeClass( "transparent" ).attr( "checked", false );
			
			StudyView.datatables.selection.updateLabel(tableId);
			
			// Disable the 'select all pages' button
			wrapper.find( ".selectAll td" ).slideUp( 100 );
		},
		
		/**
		 * Update the status of the checkAll button, after an element has been selected or deselected
		 */
		updateCheckAll: function( input ) {
			var wrapper = $(input).closest( '.dataTables_wrapper' );
		    var paginatedTable = wrapper.find( ".dataTables_scrollBody .dataTable" );
		    var inputsOnScreen = $( 'tbody input.selectable-datatable-checkbox', paginatedTable );
		
		    var tableId = paginatedTable.attr( 'id' );
		    var checkAll = $( '#'+tableId+'_checkAll', wrapper );
		    
		    var blnSelected = false;
		    var blnAllSelected = true;
		
		    // If the list is empty, disable the checkall and remove the check
		    if( inputsOnScreen.length == 0 ) {
		    	checkAll.attr( 'checked', false );
		    	checkAll.attr( 'disabled', true );
		    	return;
		    } else {
		    	checkAll.attr( 'disabled', false );
		    }
		    
		    for( var i = 0; i < inputsOnScreen.length; i++ ) {
		        var input = $(inputsOnScreen[ i ] );
		        if( !input.hasClass( "checkall" ) ) {
		            if(input.attr( 'checked' )) {
		                blnSelected = true;
		            } else {
		                blnAllSelected = false;
		            }
		        }
		    }
		
		    checkAll.removeClass( 'transparent' );
		    if(blnAllSelected) {
		        checkAll.attr('checked', true);
			} else {
				if(blnSelected) {
		            checkAll.addClass( 'transparent' );
		        }
		        checkAll.attr('checked', blnSelected);
		    }
		
		    StudyView.datatables.selection.updateLabel( tableId );
		},
		
		/**
		 * Updates the label underneath the table, specifying how many items are selected
		 */
		updateLabel: function( tableId ) {
		    if(StudyView.datatables.elementsSelected[ tableId ].length > 0) {
		        $("#"+tableId+"_selectinfo")
		        	.text(" (" + StudyView.datatables.elementsSelected[ tableId ].length + " selected)")
		        	.append( " (" )
		        	.append( $( "<a href='#'></a>" ).text( "clear selection" ).on( "click", function() {
		        		StudyView.datatables.selection.deselectAll( tableId );
		        		return false;
		        	}))
		        	.append( ")" )
		        	;
		    } else {
		        $("#"+tableId+"_selectinfo").html("");
		    }
		},
		
		/**
		 * Check all checkboxes that have been selected, due to another event.
		 */
		checkSelectedCheckboxes: function( tableId ) {
		
			// Add a selectbox or radiobutton to each row
			var trsOnScreen = $( 'tbody tr', $("#"+tableId) );
		
			for( var i = 0; i < trsOnScreen.length; i++ ) {
				var tr = $(trsOnScreen[ i ] );
		        var td = $( 'td:first',tr);
		        
		        // Only add the input if the list is not empty. The list is empty if
		        // the td we've selected has the class dataTables_empty
		        if( !td.hasClass( 'dataTables_empty' ) ) {
			        var rowid = td.html().trim();
			
			        // Determine whether the field should be checked
			        var strChecked = "";
			        if( jQuery.inArray( parseInt( rowid ), StudyView.datatables.elementsSelected[ tableId ] ) > -1 ) {
			            strChecked = " CHECKED ";
			        }
			
			        // Add a radio button for selectOnce and a checkbox for selectMulti
			        var strType = "radio";
			        if( StudyView.datatables.selectType[ tableId ] == "selectMulti") {
			            strType = "checkbox";
			        }
			        
			        // Replace the current contents of the cell with the newly created input field
			        var input = $("<input id='"+tableId+"_ids_" + rowid + "' class='selectable-datatable-checkbox' type='"+strType+"' value='"+rowid+"' name='"+tableId+"_ids'"+strChecked+">");
			        input.on( "click", function() {
			        	StudyView.datatables.selection.clickRow(this);
			        });
			        td.empty().append( input );
			        
			        // Add the id as id for the row
			        tr.attr( "id", rowid );
		        }
		    }
			
			StudyView.datatables.selection.updateCheckAll( trsOnScreen.parent() );
		},

		/**
		 * Submit a form taking into account its paginated nature (i.e. it has been spread over multiple pages)
		 */
		submitPaginatedForm: function( id, url, nothingInFormMessage ) {
		
		    var form = $("#"+id+"_form");
		
			// Remove all inputs created before
			$( '.created', form ).remove();
		
			// Find paginated form elements
			var paginatedTable = $("#"+id+"_table");
			var tableId = paginatedTable.attr( 'id' );
		
		    var ids = StudyView.datatables.elementsSelected[ tableId ];
		    var formFilled = ( ids.length > 0 );
		
		    $.each( ids, function(idx, id) {
		        var input = $( '<input type="hidden" class="created" name="ids">');
		        input.attr( 'value', id );
		        form.append( input );
		    });
		
			// Show a message if the form is not filled
			if( !formFilled ) {
				if( nothingInFormMessage != undefined ) {
					alert( nothingInFormMessage );
				}
		
				return false;
			}
		
			// Set form method to POST in order to be able to handle all items
			form.attr( 'method', 'POST' );
		
			if( url != '' )
				form.attr( 'action', url );
		
			form.submit();
		},
		
		/**
		 * Initializes jQuery selectable on the datatable, so the user can select rows by dragging
		 */
		initializeSelectable: function( table ) {
			table.bind( "mousedown", function ( e ) {
			    e.metaKey = true;
			} ).selectable
			({
				 filter: "tbody tr",
				 cancel: ':input,option,a',
			     stop: function(event, ui)
			     {
			    	var tableId = table.attr( "id" );
			    	
			        table.find("tr").each(function()
			        {
			        	var id = $(this).attr( "id" );
			        	if( id && !isNaN( parseInt( id ) ) ) {
			        		var rowId = parseInt( id );
			        		flag = $(this).hasClass('ui-selected');
			        		
			        		// Make sure the checkbox reflects the selected state
			        		var input = $(this).find( "input.selectable-datatable-checkbox");
			        		if( input.length > 0 ) {
				        		input.attr( "checked", flag );
				        		StudyView.datatables.selection.clickRow( input );
			        		}
			        	}
			        })
			    }
			});				
		}
	}
}

$(function() { initializePagination(); });

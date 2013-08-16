<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Study edit wizard</title>
	
	<r:require modules="studyEdit,gscf-datatables" />
</head>
<body>
	<div class="studyEdit studySubjects">
		<h1>
			Edit study [${study.title?.encodeAsHTML()}]
			<g:render template="steps" model="[study: study, active: 'subjects']"  />
		</h1>
		
		<g:if test="${error}">
			<div class="errormessage">
				${error.toString().encodeAsHTML()}
			</div>
		</g:if>
		<g:if test="${message}">
			<div class="message">
				${message.toString().encodeAsHTML()}
			</div>
		</g:if>	
		
		<span class="info"> 
			<span class="title">Define or import your subjects</span> 
			List all subjects and enter information about them. You can also import your subjects from an excel sheet.
		</span>
		
		<g:if test="${flash.validationErrors}">
			<div class="errormessage">
				<g:each var="error" in="${flash.validationErrors}">
					${error.value}<br />
				</g:each>
			</div>
		</g:if>  
		 
		<g:form action="subjects" name="subjects">
			<g:hiddenField name="_action" />
			<g:hiddenField name="id" value="${study.id}" />
			
			<g:each in="${subjectTemplates}" var="template">
				<h3>Template: ${template.name}</h3>
				<table id="subjectsTable.${template.id}" data-templateId="${template.id}" class="subjectsTable serverside filter" rel="${g.createLink(action:"dataTableSubjects", id: study.id, params: [template: template.id])}">
					<thead>
						<tr>
							<th class="nonsortable checkbox"><input type="checkbox" id="checkAll" onClick="clickCheckAll(this);" /></th>
							<g:each in="${domainFields + template.getFields()}" var="field">
								<th data-fieldname="${field.escapedName()}">${field.name}</th>
							</g:each>
						</tr>
					</thead>
					<tfoot>
						<tr>
							<td class="saveChanges" colspan="${1 + domainFields.size() + template.getFields().size()}">
								<a href="#" onClick="StudyEdit.submit(this); return false">Save</a>
								<span class="saving">Saving...</span>
							</td>
						</tr>
					</tfoot>
				</table>
				
				<div style="display: none" class="editable">
					<g:each in="${domainFields + template.getFields()}" var="field">
						<div class="editableFieldPrototype" id="prototype_subject_${template.id}_${field.escapedName()}">
							<af:renderTemplateField value="" templateField="${field}" />
						</div>
					</g:each>
				</div>						
				
			</g:each>			
			
			<br clear="all" />
		</g:form>

		<g:form action="editSubjects" name="subjectForm">
			<g:hiddenField class="original" name="id" value="${study.id}" />
		</g:form>
		
		<r:script>
			$(function() {
				$( ".subjectsTable" ).each(function( tableIndex, table ) {
					var rowHeaders = $( "thead th", table );
					
					$(table).data( "datatable-options", {
						"sScrollX": "100%",
						"bScrollCollapse": true,
						
						bFilter: true, 
						bLengthChange: true, 
						bPaginate: true,
						bSort: true,
						bInfo: true,
					    aoColumnDefs: [
					      { sWidth: "30px", aTargets: [ 0 ] }
					    ],
					    fnRowCallback: function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
					    	var subjectId = $( "td:first-child input[type=checkbox]", nRow ).val();
							$.each( $( "td", nRow ), function( idx, td ) {
								// Update first ID column
								if( idx == 0 )
									return
					
								// Check whether we have a prototype of an input for this type of data
								var fieldName = $( rowHeaders.get( idx ) ).data( "fieldname" );
								fieldInput = $( "#prototype_subject_" + $(table).data( "templateid" ) + "_" + fieldName ).children().clone();
								
								// If so, store it and set the correct value
								if( fieldInput.length > 0 ) {
									// Check whether this value is required, but not entered
									if( fieldInput.is( "[required=true]" ) && !aData[ idx ]  ) {
										$(td).addClass( "invalid" );
									}
								
									fieldInput
										.attr( "name", "subject." + subjectId + "." + fieldName )
										.val( aData[ idx ] )
										.data( "original", aData[ idx ] )
										.on( "change", function( event ) {
											$(event.target).parents( "td" ).first().addClass( "changed" );
											$(event.target).parents( ".dataTables_wrapper" ).first().find( ".saveChanges" ).slideDown( 100 );
										})
										.on( "keyup", function( e ) {
											// Handle esc and enter key presses
						                    if (e.keyCode == 27) {
						                        e.preventDefault();
						                        
						                        var input = $(e.target);
						                        input.val( input.data( "original" ) );
												input.parents( "td" ).first().removeClass( "changed" );
						                        
						                    }
						                    if (e.keyCode == 13) {
						                        e.preventDefault();
						                        
						                        StudyEdit.submit(e.target);
						                    }
						                });
					                									
									$( td ).empty().append( fieldInput );
								}				    	
							});
					    }
					});
				});
				
				initializeDatatables( ".subjectsTable" );
			});
		</r:script>
	</div>
</body>
</html>

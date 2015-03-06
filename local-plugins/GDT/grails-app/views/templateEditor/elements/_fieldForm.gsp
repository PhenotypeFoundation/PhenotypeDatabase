  <g:hiddenField name="id" value="${templateField?.id}" />
  <g:hiddenField name="version" value="${templateField?.version}" />
  <g:if test="${is_new}"><g:hiddenField name="entity" value="${encryptedEntity}" /></g:if>
  <g:if test="${is_selected}"><g:hiddenField name="renderTemplate" value="selected" /></g:if>
  <g:if test="${template}"><g:hiddenField name="templateId" value="${template.id}" /></g:if>
<label for="name">Name:</label> <g:textField name="name" value="${templateField?.name}" /><br />
	<label for="type">Type:</label>
	  <%
		/* Create a list of field types grouped on category */
		def grouped = [:]
		fieldTypes.each {
		  if( !grouped[ it.category ] )
			grouped[ it.category ] = []

			grouped[ it.category ].add( it )
		}
	  %>
	  <select name="type" onChange="showExtraFields( ${templateField ? templateField.id : '\'new\''} );">
		<g:each in="${grouped}" var="group">
		  <optgroup label="${group.key}">
			<g:each in="${group.value}" var="field">
			  <option
				<g:if test="${templateField?.type == field}">selected="selected"</g:if>
				value="${field}">${field.name} <g:if test="${field.example}">(${field.example})</g:if></option>
			</g:each>
		  </optgroup>
		</g:each>
	  </select>

		<br />

	<div class="extra stringlist_options extendablestringlist_options" <g:if test="${templateField?.type.toString() == 'STRINGLIST' || templateField?.type.toString() == 'EXTENDABLESTRINGLIST'}">style='display: block;'</g:if>>
	  <label for="type">Items (every item on a new line):</label>
		<g:textArea name="listEntries" value="${templateField?.listEntries?.name?.join( '\n' )}" />
	</div>
	<div class="extra ontologyterm_options" <g:if test="${templateField?.type.toString() == 'ONTOLOGYTERM'}">style='display: block;'</g:if>>
        <label for="type">Ontologies:<br /><a href="#" style="text-decoration: underline;" onClick="openOntologyDialog();">Add new</a><br /><br /> <a href="#" style="text-decoration: underline;" onClick="deleteOntology(${templateField?.id});">Remove</a></label>
		<g:select multiple="yes" size="5" from="${templateField?.ontologies}" class="ontologySelect" optionValue="name" optionKey="id" name="ontologies" id="ontologies_${templateField?.id}" /><br />
	</div>

	<label for="unit">Unit:</label> <g:textField name="unit" value="${templateField?.unit}" /><br />
	<label for="comment">Comment:</label> <g:textArea name="comment" value="${templateField?.comment}" /><br />
	<label for="required">Required:</label> <g:checkBox name="required" value="${templateField?.required}" /><br />

	<div class="templateFieldButtons">
	  <g:if test="${is_new}">
		<input type="button" value="Save" onClick="createTemplateField( 'new' );">
		<input type="button" value="Cancel" onClick="hideTemplateFieldForm( 'new' );">
	  </g:if>
	  <g:else>
		<input type="button" value="Save" onClick="updateTemplateField( ${templateField?.id} );">
		<input type="button" value="Close" onClick="hideTemplateFieldForm( ${templateField?.id} );">
	  </g:else>
	</div>

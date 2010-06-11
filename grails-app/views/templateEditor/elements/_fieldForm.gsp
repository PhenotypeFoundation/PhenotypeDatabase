	<label for="name">Name:</label> <g:textField name="name" value="${templateField?.name}" /><br />
	<label for="type">Type:</label> <g:select from="${fieldTypes}" name="type" value="${templateField?.type}" onChange="showExtraFields( ${templateField ? templateField.id : '\'new\''} );" /><br />

	<div class="extra stringlist_options" <g:if test="${templateField?.type.toString() == 'STRINGLIST'}">style='display: block;'</g:if>>
	  <label for="type">Items (every item on a new line):</label>
		<g:textArea name="listEntries" value="${templateField?.listEntries?.name?.join( '\n' )}" />
	</div>
	<div class="extra ontologyterm_options" <g:if test="${templateField?.type.toString() == 'ONTOLOGYTERM'}">style='display: block;'</g:if>>
	  <label for="type">Ontology:<br /><br /><a href="#" style="text-decoration: underline;" onClick="openOntologyDialog();">Add new</a></label>
		<g:select multiple="yes" size="5" from="${ontologies}" class="ontologySelect" optionValue="name" optionKey="id" name="ontologies" id="ontologies_${templateField?.id}" value="${templateField?.ontologies}" /><br />
	</div>

	<label for="unit">Unit:</label> <g:textField name="unit" value="${templateField?.unit}" /><br />
	<label for="comment">Comment:</label> <g:textArea name="comment" value="${templateField?.comment}" /><br />
	<label for="required">Required:</label> <g:checkBox name="required" value="${templateField?.required}" /><br />

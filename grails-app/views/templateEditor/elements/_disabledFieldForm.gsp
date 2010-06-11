	<label for="name">Name:</label> <g:textField disabled="disabled" name="name" value="${templateField?.name}" /><br />
	<label for="type">Type:</label> <g:textField disabled="disabled" name="type" value="${templateField?.type}" /><br />

	<div class="extra stringlist_options" <g:if test="${templateField?.type.toString() == 'STRINGLIST'}">style='display: block;'</g:if>>
	  <label for="type">Items:</label>
		<g:textArea name="listEntries" disabled="disabled" value="${templateField?.listEntries?.name?.join( '\n' )}" />
	</div>
	<div class="extra ontologyterm_options" <g:if test="${templateField?.type.toString() == 'ONTOLOGYTERM'}">style='display: block;'</g:if>>
	  <label for="type">Ontology:</label> <g:textArea name="ontology" disabled="disabled" value="${templateField?.ontologies?.name?.join( '\n' )}" /><br />
	</div>
	
	<label for="unit">Unit:</label> <g:textField disabled="disabled" name="unit" value="${templateField?.unit}" /><br />
	<label for="comment">Comment:</label> <g:textArea disabled="disabled" name="comment" value="${templateField?.comment}" /><br />
	<label for="required">Required:</label> <input type="checkbox" disabled <g:if test="${templateField?.required}">checked</g:if><br />

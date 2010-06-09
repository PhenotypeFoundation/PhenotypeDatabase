	<label for="name">Name:</label> <g:textField name="name" value="${templateField?.name}" /><br />
	<label for="type">Type:</label> <g:select from="${fieldTypes}" name="type" value="${templateField?.type}" /><br />
	<label for="unit">Unit:</label> <g:textField name="unit" value="${templateField?.unit}" /><br />
	<label for="comment">Comment:</label> <g:textArea name="comment" value="${templateField?.comment}" /><br />
	<label for="required">Required:</label> <g:checkBox name="required" value="${templateField?.required}" /><br />

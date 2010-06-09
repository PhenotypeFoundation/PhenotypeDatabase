<a class="title" href="#" onClick="showTemplateFieldForm( 'templateField_' + ${templateField.id}); this.blur(); return false;">
	<span class="ui-icon ui-icon-arrowthick-2-n-s"></span>
	<b>${templateField.name}</b>
	(<g:render template="elements/${templateField.type.toString().toLowerCase().replaceAll(/ /,'_')}" />)
</a>

<form class="templateField_form" id="templateField_${templateField.id}_form" action="update">
	<g:hiddenField name="id" value="${templateField.id}" />
	<g:hiddenField name="version" value="${templateField.version}" />
	<g:render template="elements/fieldForm" model="['templateField': templateField, 'fieldTypes': fieldTypes]"/>
	<div class="templateFieldButtons">
		<input type="button" value="Save" onClick="updateTemplateField( ${templateField.id} );">
		<input type="button" value="Delete" onClick="deleteTemplateField( ${templateField.id} );">
		<input type="button" value="Close" onClick="hideTemplateFieldForm( ${templateField.id} );">
	</div>
</form>

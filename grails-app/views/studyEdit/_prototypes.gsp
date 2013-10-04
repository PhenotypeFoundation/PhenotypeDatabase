	<g:each in="${template.entity.domainFields + template.getFields()}" var="field">
		<div class="editableFieldPrototype" id="prototype_${template.id}_${field.escapedName()}" data-fieldtype="${field.type}">
			<af:renderTemplateField value="" templateField="${field}" />
		</div>
	</g:each>

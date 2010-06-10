<span class="listButtons">
  <img onClick="editTemplate( ${template.id} );" src="${createLinkTo( dir: 'images', file: 'icons/famfamfam/application_edit.png')}" alt="Edit template properties" title="Edit template properties">
  <img onClick="editFields( ${template.id} );"src="${createLinkTo( dir: 'images', file: 'icons/famfamfam/application_form.png')}" alt="Add/remove template fields" title="Add/remove template fields">
  <img onClick="if( confirm( 'Are you sure?' ) ) { deleteTemplate( ${template.id} ); }" src="${createLinkTo( dir: 'images', file: 'icons/famfamfam/delete.png')}" alt="Delete this template" title="Delete this template">
</span>
${template.name}


<form class="templateField_form" id="template_${template.id}_form" action="updateTemplate">
	<g:hiddenField name="id" value="${template.id}" />
	<g:hiddenField name="version" value="${template.version}" />
	<g:render template="elements/templateForm" model="['template': template]"/>
	<div class="templateFieldButtons">
		<input type="button" value="Save" onClick="updateTemplate( ${template.id} );">
		<input type="button" value="Close" onClick="hideTemplateForm( ${template.id} );">
	</div>
</form>

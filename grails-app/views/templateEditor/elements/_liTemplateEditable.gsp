<span class="listButtons">
  <img onClick="editTemplate( ${template.id} );" src="${createLinkTo( dir: 'images/icons', file: 'application_edit.png', plugin: 'famfamfam' )}" alt="Edit template properties" title="Edit template properties">
  <img onClick="editFields( ${template.id} );"src="${createLinkTo( dir: 'images/icons', file: 'application_form.png', plugin: 'famfamfam' )}" alt="Add/remove template fields" title="Add/remove template fields">
  <img onClick="if( confirm( 'Are you sure?' ) ) { deleteTemplate( ${template.id} ); }" src="${createLinkTo( dir: 'images/icons', file: 'delete.png', plugin: 'famfamfam' )}" alt="Delete this template" title="Delete this template">
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

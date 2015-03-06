<span class="listButtons">
  <img class="disabled" src="${resource( dir: 'images/icons', file: 'application_edit.png', plugin: 'famfamfam' )}" alt="Template properties are only editable for (template)Admins." title="Template properties are only editable for (template)Admins.">
  <img onClick="editFields( ${template.id} );" src="${resource( dir: 'images/icons', file: 'application_form_magnify.png', plugin: 'famfamfam' )}" alt="View available templatefields" title="View available templatefields">
  <img class="disabled" src="${resource( dir: 'images/icons', file: 'page_copy.png', plugin: 'famfamfam' )}" alt="Templates can only be cloned by (template)Admins." title="Templates can only be cloned by (template)Admins.">
  <img class="disabled" src="${resource( dir: 'images/icons', file: 'delete.png', plugin: 'famfamfam' )}" alt="Templates can only be deleted by (template)Admins." title="Templates can only be deleted by (template)Admins.">
</span>
${template.name}


<form class="templateField_form" id="template_${template.id}_form" action="updateTemplate">
	<g:hiddenField name="id" value="${template.id}" />
	<g:hiddenField name="version" value="${template.version}" />
	<g:hiddenField name="standalone" value="${standalone}" />
	<g:render template="elements/templateForm" model="['template': template]"/>
	<div class="templateFieldButtons">
		<input type="button" value="Save" onClick="updateTemplate( ${template.id} );">
		<input type="button" value="Close" onClick="hideTemplateForm( ${template.id} );">
	</div>
</form>

<li class="ui-state-default" id="templateField_${it.id}">
  <span class="ui-icon ui-icon-arrowthick-2-n-s"></span>
  <b>${it.name}</b>
  (<g:render template="elements/${it.type.toString().toLowerCase().replaceAll(/ /,'_')}" />)

  <form class="templateField_form" id="templateField_${it.id}_form" action="update">
    <g:hiddenField name="id" value="${it.id}" />
    <g:hiddenField name="version" value="${it.version}" />
    <label for="name">Name:</label> <g:textField name="name" value="${it.name}" /><br />
    <label for="type">Type:</label> <g:select from="${fieldTypes}" name="type" value="${it.type}" /><br />
    <label for="unit">Unit:</label> <g:textField name="unit" value="${it.unit}" /><br />
    <label for="comment">Comment:</label> <g:textArea name="comment" value="${it.comment}" /><br />
    <label for="required">Required:</label> <g:checkBox name="required" value="${it.required}" /><br />

    <input type="button" value="Save" onClick="updateTemplateField( ${it.id} );">
    <input type="button" value="Delete" onClick="">
    <input type="button" value="Close" onClick="hideTemplateFieldForm( ${it.id} );">
  </form>
</li>
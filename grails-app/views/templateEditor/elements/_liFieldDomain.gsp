  <b>${templateField.name}</b>
  (<g:if test="${templateField.unit}">${templateField.unit}, </g:if><g:render template="elements/${templateField.type.toString().toLowerCase().replaceAll(/ /,'_')}" model="[templateField: templateField]"/>)
    


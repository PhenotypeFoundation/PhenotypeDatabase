    <g:hiddenField name="requestcat" value="template" />
    <g:select name="requestnm" from="${['New', 'Modification to']}" value="New" /><br />
    <br />
    <label for="rname">Name <img src="${resource( dir: 'images/icons', file: 'help.png', plugin: 'famfamfam' )}" alt="Name of the template that should be added/modified" title="Name of the template that should be added/modified">:</label>
    <g:textField name="rname" value="" /><br />
    <label for="rtype">Type <img src="${resource( dir: 'images/icons', file: 'help.png', plugin: 'famfamfam' )}" alt="Type of the template that should be added/modified" title="Type of the template that should be added/modified">:</label>
    <g:textField name="rtype" value="${templateType}" disabled="true" /><br />
    <g:hiddenField name="rtype" value="${templateType}"/>
    <label for="specification">Specification: <img src="${resource( dir: 'images/icons', file: 'help.png', plugin: 'famfamfam' )}" alt="Reason for template addition/modification" title="Reason for addition/modification"></label>
    <g:textArea name="specification" value="" rows='1' cols='1'/>
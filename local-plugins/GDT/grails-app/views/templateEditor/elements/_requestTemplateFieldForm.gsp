    <g:hiddenField name="requestcat" value="templatefield" />
    <g:select name="requestnm" from="${['New', 'Modification to', 'Add existing field to this template']}" value="New" /><br />
    <br />
    <label for="rname">Name <img src="${resource( dir: 'images/icons', file: 'help.png', plugin: 'famfamfam' )}" alt="Name of the template that should be added/modified" title="Name of the templatefield that should be added/modified">:</label>
    <g:textField name="rname" value="" /><br />
    <label for="rtype">Type <img src="${resource( dir: 'images/icons', file: 'help.png', plugin: 'famfamfam' )}" alt="Type of the template that should be added/modified" title="Type of the templatefield that should be added/modified">:</label>
    <%
        /* Create a list of field types grouped on category */
        def grouped = [:]
        fieldTypes.each {
            if( !grouped[ it.category ] )
                grouped[ it.category ] = []

            grouped[ it.category ].add( it )
        }
    %>
    <select name="rtype" onChange="showExtraFields( ${templateField ? templateField.id : '\'new\''} );">
        <g:each in="${grouped}" var="group">
            <optgroup label="${group.key}">
                <g:each in="${group.value}" var="field">
                    <option
                        <g:if test="${templateField?.type == field}">selected="selected"</g:if>
                        value="${field}">${field.name} <g:if test="${field.example}">(${field.example})</g:if></option>
                </g:each>
            </optgroup>
        </g:each>
    </select>
    <label for="specification">Specification: <img src="${resource( dir: 'images/icons', file: 'help.png', plugin: 'famfamfam' )}" alt="Reason for templatefield addition/modification" title="Reason for templatefield addition/modification"></label>
    <g:textArea name="specification" value="" rows="1" cols="1"/>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="sammain"/>
        <title>Measurement properties</title>
    </head>

    <body>
        <g:hasErrors bean="${measurementInstance}">
           <div class="errors">
               <g:renderErrors bean="${measurementInstance}" as="list"/>
           </div>
        </g:hasErrors>
        <content tag="contextmenu">
            <g:render template="contextmenu" />
        </content>
        <h1>Measurement propertie</h1>

        <div class="data">
            <g:form method="post">
                <g:hiddenField name="ids" value="${measurementInstance?.id}"/>
                <g:hiddenField name="version" value="${measurementInstance?.version}"/>
                <g:hiddenField name="module" value="${module}"/>
                <div class="dialog">
                    <table>
                        <tbody>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="measurement.id.label" default="Id"/></td>

                            <td valign="top" class="value">${fieldValue(bean: measurementInstance, field: "id")}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="measurement.value.label" default="Value"/></td>

                            <td valign="top" class="value">${measurementInstance.value}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="measurement.operator.label" default="Operator"/></td>

                            <td valign="top" class="value">${fieldValue(bean: measurementInstance, field: "operator")}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="measurement.comments.label" default="Comments"/></td>

                            <td valign="top" class="value">${fieldValue(bean: measurementInstance, field: "comments")}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="measurement.sample.label" default="Sample"/></td>

                            <td valign="top" class="value"><g:link controller="sample" action="show"
                                                                   id="${measurementInstance?.sample?.id}">${measurementInstance?.sample?.encodeAsHTML()}</g:link></td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="measurement.feature.label" default="Feature"/></td>

                            <td valign="top" class="value"><g:link controller="feature" action="show"
                                                                   id="${measurementInstance?.feature?.id}">${measurementInstance?.feature?.encodeAsHTML()}</g:link></td>

                        </tr>

                        </tbody>
                    </table>
                </div>

                <ul class="data_nav buttons">
                    <li><g:link controller="measurement" action="edit" id="${measurementInstance?.id}" class="edit" params="${[module: module]}">Edit</g:link></li>
                    <li><g:actionSubmit class="delete" action="delete" value="Delete" onclick="return confirm('Are you sure?');"/></li>
                    <li><g:link controller="measurement" action="list" class="cancel" params="${[module: module]}">Cancel</g:link></li>
                </ul>
            </g:form>
        </div>
    </body>
</html>

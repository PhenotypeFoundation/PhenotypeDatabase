<%@ page import="org.dbxp.sam.Feature; org.dbxp.sam.Platform" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <title>Create a new feature</title>
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'sam.css')}"/>
    </head>
    <body>
        <g:if test="${flash.message}">
            <p class="message">${flash.message.toString()}</p>
        </g:if>
        <g:if test="${flash.error}">
            <p class="error">${flash.error.toString()}</p>
        </g:if>
        <g:hasErrors bean="${featureInstance}">
            <div class="errors">
                <g:renderErrors bean="${featureInstance}" as="list"/>
            </div>
        </g:hasErrors>
        <h1>Create a new feature</h1>
        <div class="data">
            You will be able to add additional detail to this feature when you have finished importing your data.
            <g:form action="save">
                <input type="hidden" name="nextPage" id="nextPage" value="minimalCreate"/>
                <div class="dialog">
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name">Name</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: featureInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${featureInstance?.name}" size="maxlength"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="platform">Platform</label>
                                </td>
                                <td valign="top" class="value">
                                    <g:select name="platform" from="${Platform.list()}" optionKey="id"/>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <g:submitButton name="create" class="save" value="Create"/>
            </g:form>
        </div>
    </body>
</html>

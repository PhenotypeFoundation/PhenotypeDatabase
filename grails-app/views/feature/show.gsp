<%@ page import="org.dbnp.gdt.TemplateFieldType; org.dbxp.sam.Feature; org.dbxp.sam.Platform" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="sammain"/>
        <title>Feature ${featureInstance.name}'s properties</title>
    </head>
    <body>
        <g:hasErrors bean="${featureInstance}">
           <div class="errors">
               <g:renderErrors bean="${featureInstance}" as="list"/>
           </div>
        </g:hasErrors>
        <content tag="contextmenu">
      		<g:render template="contextmenu" />
        </content>
        <h1>Feature ${featureInstance.name}'s properties</h1>

        <div class="data">
            <div class="dialog">
                <table class="">
                    <tr class="prop even">
                        <td valign="top" class="fieldName">
                            Platform
                        </td>
                        <td valign="top">
                            ${featureInstance.platform.name}
                        </td>
                    </tr>
                    <% def ii = 0%>
                    <g:each in="${featureInstance.giveFields()}" var="field" status="i">
                        <tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td valign="top">
                                ${field.name.capitalize()}
                                <g:if test="${field.type==TemplateFieldType.DOUBLE}">
                                    [${field.unit}]
                                </g:if>
                            </td>
                            <td valign="top" >
                                <%-- Here follows a hack, that enables us to show 'false' for a boolean template field, when it is not true (i.e., it is false). This is the sensible thing to do, but GDT disappears the boolean when it is not set, so it can't be shown to be 'false' (something that does not exist is, after all, null). --%>
                                <g:if test="${field.type==TemplateFieldType.BOOLEAN && featureInstance.getFieldValue(field.toString())==null}">
                                    false
                                </g:if>
                                <g:else>
                                    ${featureInstance.getFieldValue(field.toString())}
                                </g:else>
                            </td>
                            <% ii = i + 1%>
                        </tr>
                    </g:each>
                </table>
            </div>

            <ul class="data_nav buttons">
                <g:form>
                    <g:hiddenField name="id" value="${featureInstance?.id}"/>
                    <g:hiddenField name="ids" value="${featureInstance?.id}"/>
                    <g:hiddenField name="module" value="${module}"/>
                    <li><g:actionSubmit class="edit" action="edit" value="Edit"/></li>
                    <li><g:actionSubmit class="delete" action="delete" value="Delete" onclick="return confirm('Are you sure?');"/></li>
                    <li><g:link controller="feature" action="list" class="cancel" params="${[module: module]}">Back to list</g:link></li>
                </g:form>
            </ul>
        </div>
    </body>
</html>
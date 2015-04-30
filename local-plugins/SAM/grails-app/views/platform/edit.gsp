<%@ page import="org.dbnp.gdt.TemplateField; org.dbxp.sam.Platform" %>
<%@ page import="org.dbnp.gdt.GdtTagLib" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="sammain"/>
    <title>Edit platform ${platformInstance.name} for ${module}</title>
    <r:require module="templateFieldsMisc"/>
    <r:script type="text/javascript" disposition="head">
        $(document).ready(function() {
            entityName = "platform";
            formSection = "form#edit";
            insertSelectAddMore(); // add add/modify select option
            onTemplateFieldPage(); // Add datepickers
        });
    </r:script>
</head>

<body>
<g:hasErrors bean="${platformInstance}">
    <div class="errors">
        <g:renderErrors bean="${platformInstance}" as="list"/>
    </div>
</g:hasErrors>
<content tag="contextmenu">
    <g:render template="contextmenu" />
</content>
<h1>Edit platform ${platformInstance.name}</h1>

<div class="data">
    <g:form class="Feature" action="update" name="edit" method="post" novalidate="novalidate">
        <g:hiddenField name="id" value="${platformInstance?.id}"/>
        <g:hiddenField name="version" value="${platformInstance?.version}"/>
        <g:hiddenField name="module" value="${module}"/>
        <div class="dialog">
            <table>
                <tr class="prop">
                    <td valign="top">
                        Common fields:
                    </td>
                </tr>

                <%--
                 List common fields on the left, and all platform group items on the right
                 Do this with two tables so that the length of the lists don't mess up the other list's layout.
                 --%>
                <tr>
                    <td>
                        <table>
                            <g:each in="${platformInstance.giveDomainFields()}" var="field" status="i">
                                <tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'}">
                                    <td valign="top" class='fieldName'>
                                        ${field.name.capitalize()}
                                        <g:if test="${field.required}">
                                            <i>(required)</i>
                                        </g:if>
                                    </td>
                                    <td valign="top" >
                                        <g:textField name="${field.escapedName()}" value="${platformInstance.getFieldValue(field.toString())}"/>
                                    </td>
                                </tr>
                            </g:each>
                            <tr class="prop ${(platformInstance.giveDomainFields().size() % 2) == 0 ? 'odd' : 'even'}">
                                <td>Template</td>
                                <td id="templateSelection">
                                    <g:render template="templateSelection" model="['template' : platformInstance.template]" />
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>

                <%--
                 End of 'common fields' tables
                --%>

                <tr class="prop">
                    <td>
                        <g:if test="${platformInstance.template!=null}">
                            Template specific fields:
                        </g:if>
                    </td>
                </tr>

                <tr class="prop">
                    <td id="templateSpecific">
                        <%
                            def values = [:];
                            platformInstance.template?.fields.each {
                                values[ it.escapedName() ] = platformInstance.getFieldValue( it.name )
                            }
                        %>
                        <g:render template="templateSpecific" model="['template': platformInstance.template, values: values ]" />
                    </td>
                </tr>

            </table>
        </div>

        <ul class="data_nav buttons">
            <li><g:actionSubmit class="save" action="update" value="Update"/></li>
            <li><g:actionSubmit class="delete" action="delete" value="Delete" onclick="return confirm('Are you sure?');"/></li>
            <li><g:link controller="platform" action="list" class="cancel" params="${[module: module]}">Cancel</g:link></li>
        </ul>
    </g:form>
</div>
</body>
</html>
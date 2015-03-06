<%@ page import="org.dbxp.sam.Platform" %>
<%@ page import="org.dbnp.gdt.AssayModule" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="sammain"/>
    <title>Create a new platform for ${module}</title>
    <r:require module="templateFieldsMisc"/>
    <r:script type="text/javascript" disposition="head">
        $(document).ready(function() {
            entityName = "platform";
            formSection = "form#create";
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
<h1>Create a new platform for ${module}</h1>

<div class="data">
    <g:form action="save" name="create" novalidate="novalidate">
        <input type="hidden" name="nextPage" id="nextPage" value="list" />
        <input type="hidden" name="module" id="module" value="${module}" />
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
                                        <g:if test="${field.escapedName() == "platformtype"}">
                                            <g:select name="${field.escapedName()}" from="${AssayModule.findAll().name}" value="${module}"/>
                                        </g:if>
                                        <g:else>
                                            <g:textField name="${field.escapedName()}" value="${platformInstance.getFieldValue(field.toString())}"/>
                                        </g:else>
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
                            /*platformInstance.template?.fields.each {
                                values[ it.name ] = platformInstance.getFieldValue( it.name )
                            }*/
                            // This platform is brand new. As such, it will not have any template fields set
                        %>
                        <g:render template="templateSpecific" model="['template': platformInstance.template, values: values ]" />
                    </td>
                </tr>

            </table>
        </div>
        <ul class="data_nav buttons">
            <li><g:submitButton name="create" class="save" value="Create"/></li>
            <li><g:link controller="platform" action="list" params="${[module: module]}" class="cancel">Cancel</g:link></li>
        </ul>
    </g:form>
</div>
</body>
</html>

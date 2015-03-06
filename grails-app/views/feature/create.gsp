<%@ page import="org.dbxp.sam.Feature" %>
<%@ page import="org.dbxp.sam.Platform" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="sammain"/>
        <title>Create a new feature for ${module}</title>
        <r:require module="templateFieldsMisc"/>
        <r:script type="text/javascript" disposition="head">
            $(document).ready(function() {
                entityName = "feature";
                formSection = "form#create";
            	insertSelectAddMore(); // add add/modify select option
                onTemplateFieldPage(); // Add datepickers
            });
        </r:script>
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
        <h1>Create a new feature for ${module}</h1>

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
                         List common fields on the left, and all feature group items on the right
                         Do this with two tables so that the length of the lists don't mess up the other list's layout.
                         --%>
                         <tr>
                            <td>
                                <table>
                                    <tr class="prop even">
                                        <td valign="top" class="fieldName">
                                            Platform
                                        </td>
                                        <td valign="top">
                                            <g:select name="platform" from="${Platform.list()}" optionKey="id"/>
                                        </td>
                                    </tr>
                                    <g:each in="${featureInstance.giveDomainFields()}" var="field" status="i">
                                        <tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'}">
                                            <td valign="top" class='fieldName'>
                                                ${field.name.capitalize()}
                                                <g:if test="${field.required}">
                                                    <i>(required)</i>
                                                </g:if>
                                            </td>
                                            <td valign="top" >
                                                <g:textField name="${field.escapedName()}" value="${featureInstance.getFieldValue(field.toString())}"/>
                                            </td>
                                        </tr>
                                    </g:each>
									<tr class="prop ${(featureInstance.giveDomainFields().size() % 2) == 0 ? 'odd' : 'even'}">
										<td>Template</td>
			                            <td id="templateSelection">
			                            	<g:render template="templateSelection" model="['template' : featureInstance.template]" />
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
                                <g:if test="${featureInstance.template!=null}">
                                    Template specific fields:
                                </g:if>
                            </td>
                        </tr>

                        <tr class="prop">
                            <td id="templateSpecific">
                            	<%
									def values = [:];
									/*featureInstance.template?.fields.each {
										values[ it.name ] = featureInstance.getFieldValue( it.name )
									}*/
                                    // This feature is brand new. As such, it will not have any template fields set
								%>
                            	<g:render template="templateSpecific" model="['template': featureInstance.template, values: values ]" />
                            </td>
                        </tr>

                    </table>
                </div>
                <ul class="data_nav buttons">
                    <li><g:submitButton name="create" class="save" value="Create"/></li>
                    <li><g:link controller="feature" action="list" class="cancel" params="${[module: module]}">Cancel</g:link></li>
                </ul>
            </g:form>
        </div>
    </body>
</html>

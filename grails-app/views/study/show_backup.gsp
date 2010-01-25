<%@ page import="dbnp.studycapturing.Study" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
      <script type="text/javascript">
	$(function() {
		$("#accordions").accordion();
	});
      </script>
    </head>


    <body>

<resource:accordion skin="default" />
<richui:accordion> <richui:accordionItem id="1" caption="Sample 1"> A sample text. </richui:accordionItem>
<richui:accordionItem caption="Sample 2"> Another sample text. </richui:accordionItem>
<richui:accordionItem caption="Sample 3"> Even another sample text. </richui:accordionItem> </richui:accordion>

      <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                  <richui:accordion>
                    <tbody>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.id.label" default="Id" /></td>

                            <td valign="top" class="value">${fieldValue(bean: studyInstance, field: "id")}</td>
                        </tr>


                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.template.label" default="Template" /></td>

                            <td valign="top" class="value"><g:link controller="template" action="show" id="${studyInstance?.template?.id}">${studyInstance?.template?.encodeAsHTML()}</g:link></td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.startDate.label" default="Start Date" /></td>

                            <td valign="top" class="value"><g:formatDate date="${studyInstance?.startDate}" /></td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.samplingEvents.label" default="Sampling Events" /></td>

                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${studyInstance.samplingEvents}" var="s">
                                    <li><g:link controller="samplingEvent" action="show" id="${s.id}">${s?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.subjects.label" default="Subjects" /></td>

                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${studyInstance.subjects}" var="s">
                                    <li><g:link controller="subject" action="show" id="${s.id}">${s?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.events.label" default="Events" /></td>

                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${studyInstance.events}" var="e">
                                    <li><g:link controller="event" action="show" id="${e.id}">${e?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.lastUpdated.label" default="Last Updated" /></td>

                            <td valign="top" class="value"><g:formatDate date="${studyInstance?.lastUpdated}" /></td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.readers.label" default="Readers" /></td>

                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${studyInstance.readers}" var="r">
                                    <li><g:link controller="user" action="show" id="${r.id}">${r?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.code.label" default="Code" /></td>

                            <td valign="top" class="value">${fieldValue(bean: studyInstance, field: "code")}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.editors.label" default="Editors" /></td>

                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${studyInstance.editors}" var="e">
                                    <li><g:link controller="user" action="show" id="${e.id}">${e?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.ecCode.label" default="Ec Code" /></td>

                            <td valign="top" class="value">${fieldValue(bean: studyInstance, field: "ecCode")}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.researchQuestion.label" default="Research Question" /></td>

                            <td valign="top" class="value">${fieldValue(bean: studyInstance, field: "researchQuestion")}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.title.label" default="Title" /></td>

                            <td valign="top" class="value">${fieldValue(bean: studyInstance, field: "title")}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.description.label" default="Description" /></td>

                            <td valign="top" class="value">${fieldValue(bean: studyInstance, field: "description")}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.owner.label" default="Owner" /></td>

                            <td valign="top" class="value"><g:link controller="user" action="show" id="${studyInstance?.owner?.id}">${studyInstance?.owner?.encodeAsHTML()}</g:link></td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.dateCreated.label" default="Date Created" /></td>

                            <td valign="top" class="value"><g:formatDate date="${studyInstance?.dateCreated}" /></td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.groups.label" default="Groups" /></td>

                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${studyInstance.groups}" var="g">
                                    <li><g:link controller="subjectGroup" action="show" id="${g.id}">${g?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>

                        </tr>

                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${studyInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>


<%@ page import="dbnp.studycapturing.Study" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${studyInstance}">
            <div class="errors">
                <g:renderErrors bean="${studyInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${studyInstance?.id}" />
                <g:hiddenField name="version" value="${studyInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="template"><g:message code="study.template.label" default="Template" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'template', 'errors')}">
                                    <g:select name="template.id" from="${dbnp.studycapturing.Template.list()}" optionKey="id" value="${studyInstance?.template?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="startDate"><g:message code="study.startDate.label" default="Start Date" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'startDate', 'errors')}">
                                    <g:datePicker name="startDate" precision="day" value="${studyInstance?.startDate}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="samplingEvents"><g:message code="study.samplingEvents.label" default="Sampling Events" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'samplingEvents', 'errors')}">
                                    <g:select name="samplingEvents" from="${dbnp.studycapturing.SamplingEvent.list()}" multiple="yes" optionKey="id" size="5" value="${studyInstance?.samplingEvents}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="subjects"><g:message code="study.subjects.label" default="Subjects" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'subjects', 'errors')}">
                                    <g:select name="subjects" from="${dbnp.studycapturing.Subject.list()}" multiple="yes" optionKey="id" size="5" value="${studyInstance?.subjects}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="events"><g:message code="study.events.label" default="Events" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'events', 'errors')}">
                                    <g:select name="events" from="${dbnp.studycapturing.Event.list()}" multiple="yes" optionKey="id" size="5" value="${studyInstance?.events}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="lastUpdated"><g:message code="study.lastUpdated.label" default="Last Updated" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'lastUpdated', 'errors')}">
                                    <g:datePicker name="lastUpdated" precision="day" value="${studyInstance?.lastUpdated}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="readers"><g:message code="study.readers.label" default="Readers" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'readers', 'errors')}">
                                    <g:select name="readers" from="${nimble.User.list()}" multiple="yes" optionKey="id" size="5" value="${studyInstance?.readers}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="code"><g:message code="study.code.label" default="Code" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'code', 'errors')}">
                                    <g:textField name="code" value="${studyInstance?.code}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="editors"><g:message code="study.editors.label" default="Editors" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'editors', 'errors')}">
                                    <g:select name="editors" from="${nimble.User.list()}" multiple="yes" optionKey="id" size="5" value="${studyInstance?.editors}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="ecCode"><g:message code="study.ecCode.label" default="Ec Code" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'ecCode', 'errors')}">
                                    <g:textField name="ecCode" value="${studyInstance?.ecCode}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="researchQuestion"><g:message code="study.researchQuestion.label" default="Research Question" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'researchQuestion', 'errors')}">
                                    <g:textField name="researchQuestion" value="${studyInstance?.researchQuestion}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="title"><g:message code="study.title.label" default="Title" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'title', 'errors')}">
                                    <g:textField name="title" value="${studyInstance?.title}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="description"><g:message code="study.description.label" default="Description" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'description', 'errors')}">
                                    <g:textField name="description" value="${studyInstance?.description}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="owner"><g:message code="study.owner.label" default="Owner" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'owner', 'errors')}">
                                    <g:select name="owner.id" from="${nimble.User.list()}" optionKey="id" value="${studyInstance?.owner?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="dateCreated"><g:message code="study.dateCreated.label" default="Date Created" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'dateCreated', 'errors')}">
                                    <g:datePicker name="dateCreated" precision="day" value="${studyInstance?.dateCreated}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="groups"><g:message code="study.groups.label" default="Groups" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'groups', 'errors')}">
                                    <g:select name="groups" from="${dbnp.studycapturing.SubjectGroup.list()}" multiple="yes" optionKey="id" size="5" value="${studyInstance?.groups}" />
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>

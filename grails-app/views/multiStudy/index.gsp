<%@ page import="dbnp.studycapturing.Study" contentType="text/html;charset=UTF-8" %>
<html>
<head>
	<meta name="layout" content="main"/>
    <title>Select desired studies</title>
</head>

<body>
<div style="color:red;">
	${flash.errorMessage}
</div>

<h1>Select the studies and the assay you want to export</h1>

<p>With this exporter you can export (meta) data about multiple studies and an assay to a file.
First, check the desired studies from the first list and then select an assay from that study from the second list.</p><br/>

<div class="list">
    <g:form name="studySelect" action="studyExport">
        <table>
            <thead>
            <tr>
                <th>Select Study</th>
                <g:sortableColumn property="code" title="${message(code: 'study.code.label', default: 'Code')}"/>
                <th>Title</th>
                <th>Select Assay to export</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${userStudies}" var="studyInstance" status="i">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <td><input type="checkbox" name="studies" value="${studyInstance.id}" id="${studyInstance.title}"></td>
                    <td>${fieldValue(bean: studyInstance, field: "code")}</td>
                    <td>
                        ${fieldValue(bean: studyInstance, field: "title")}
                    </td>

                    <td>
                        <g:if test="${studyInstance.assays.size()==0}">
                            <b>No assays attached to study</b>
                        </g:if>
                        <g:else>
                            <g:select optionKey="id" name="study-${studyInstance.id}-assay" id="assayId" from="${studyInstance.assays}" multiple="true" />
                        </g:else>
                    </td>

                </tr>
            </g:each>
            </tbody>
        </table>
        <g:submitButton name="submit" value="Export Studies" id="submit"/>
    </g:form>
</div>

</body>
</html>
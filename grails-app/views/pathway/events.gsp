<%@ page import="dbnp.query.Operator" %>
<%@ page import="dbnp.studycapturing.*" %>
<%@ page import="org.dbnp.gdt.*" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Pathway</title>
</head>
<body>

<h1>Visualize Study</h1>

<g:if test="${flash.error}">
	<div class="errormessage">
		${flash.error.toString().encodeAsHTML()}
	</div>
</g:if>
<g:if test="${flash.message}">
	<div class="message">
		${flash.message.toString().encodeAsHTML()}
	</div>
</g:if>

<div id="pathwayEventForm">
	<g:form action="calculate" method="get">
        <p>
		    Choose study:<br/>
			<g:select from="${studyNames}" value="${studyName}" noSelection="['':'Select Study']" name="selectedstudy" onchange="submit()"/>
                <table>
                <g:each in="${eGroupMap.keySet()}" var="groupName">
                    <thead>
                    <th>${groupName}</th>
                    <th>Event</th>
                    <th>Compound</th>
                    <th>StartTime</th>
                    </thead>
                    <g:each in="${eGroupMap.get(groupName)}" var="event">
                        <tr>
                        %{--Work around voor event.get("template"), welke geen value terug geeft in de var--}%
                        <g:set var="info" value="${(event.values().toList()[0])+':'+groupName}"/>
                        <td><g:checkBox name="selectedEventGroups" value="${info}" checked="false"/></td>
                        <td>${event.get("template")}</td>
                        <td>${event.get("templateStringFields").get("Compound")}</td>
                        <td>${event.get("startTime")}</td>
                        </tr>
                     </g:each>
                </g:each>
                </table>
                <br>
                <g:submitButton name="Confirm" value="Confirm events"/>
            </div>
        </p>
    </g:form>
	<br clear="all" />
</div>
</body>
</html>

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

<div id="pathwayCalculateForm">
	<g:form action="visualize" method="get">
        <p>
        <div id="sampleEventsForm">
                <table>
                <g:each in="${seGroupMap.keySet()}" var="groupName">
                    <thead>
                    <th>${groupName}</th>
                    <th>Samples</th>
                    <th>Sample Event</th>
                    <th>Material</th>
                    <th>StartTime</th>
                    <th>Related Event</th>
                    <th>Relative time to challenge start</th>
                    </thead>

                             <g:each in="${seGroupMap.get(groupName)}" var="sEvent">
                                    <tr>
                                    <td>[ ${sEvent.get("id")} ]</td>
                                    %{--<td>${sEvent.get("filteredSamples")}</td>--}%
                                    <td>Samples</td>
                                    <td>${sEvent.get("template")}</td>
                                    <td>${sEvent.get("sampleTemplate")}</td>
                                    <td>${sEvent.get("startTime")}</td>
                                    <td>${sEvent.get("relatedEvent")}</td>
                                    <td>${sEvent.get("templateTimeFields").get("RTTCS")}</td>
                                    </tr>
                             </g:each>
                </g:each>
                </table>
                </div>
                <br>
                <div id="equationForm">
                <g:set var="equationCount" value="${ec}"/>
                <g:set var="equation" value="${1}"/>
                Number of equantions: <b>${equationCount}</b>
                <g:submitButton name="cEq" value="Add New Equation"/>
                <g:submitButton name="cEq" value="Delete Last Equation"/>
                <br>
                <br>
                <g:select id="index" name="calcValues" optionValue="key" optionKey="value" from="${indexMap}" value=""/>
                <br>
                <g:submitButton name="Confirm" value="Confirm sampleEvents"/>
            </div>
        </p>
    </g:form>
	<br clear="all" />
</div>
</body>
</html>

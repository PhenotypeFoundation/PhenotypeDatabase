<%--
  Created by IntelliJ IDEA.
  User: Ferry
  Date: 20/6/13
  Time: 10:17
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="sammain"/>
    <title>Assay summary view</title>
</head>
<body>
<div class="data">
    <content tag="contextmenu">
        <li><g:link action="list" class="list"  params="${[module: module]}">Back to list</g:link></li>
    </content>
    <h1>${module} ${assayInstance.name} / ${assayInstance.parent.title}</h1>
    <p>This view is designed for assays with over 10000 measurements.</p>
    <p>Measurements are found for <b>${samples.size()}</b> of the total number of <b>${totalSamples}</b> samples:</p>
    <ul class="data_nav buttons ontop">
        <li><g:link class="delete" controller="measurement" action="deleteByAssay" id="${assayInstance.id}" params="${[module: module]}" onClick="return confirm('Are you sure?');">Delete all measurements</g:link></li>
    </ul>
    <table>
        <thead>
            <th>Sample name</th>
            <th># Of measurements per Sample</th>
        </thead>
        <g:each var='sample' in='${samples}'>
            <tr>
                <td>${sample[1]}</td>
                <td>${measurementCounts[sample[0]]}</td>
            </tr>
        </g:each>
    </table>
</div>
</body>
</html>
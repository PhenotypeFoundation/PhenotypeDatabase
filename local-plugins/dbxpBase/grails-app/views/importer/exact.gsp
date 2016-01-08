<%--
  Created by IntelliJ IDEA.
  User: Ferry
  Date: 08/01/16
  Time: 09:42
--%>

<%! import grails.converters.JSON %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Import data</title>

    <r:require modules="gscfimporter" />
</head>
<body>
<div class="basicTabLayout importer uploadFile">
    <h1>
        <span class="truncated-title">
            Match data
        </span>
        <g:render template="/importer/steps" model="[active: 'matchData']" />
    </h1>

    <g:render template="/common/flashmessages" />

    <span class="message info">
        <span class="title">Exact matchting</span>
    </span>

    <g:form action="exact" name="exactData" params="${defaultParams}">
        <g:hiddenField name="_action" />
        <g:hiddenField name="key" value="${sessionKey}" />

        <div id="match-headers" data-match-url="${g.createLink(action: 'matchHeaders', params: defaultParams, id: 'exact')}">
        </div>

        <div id="match-text">
            Matching in progress, please wait patiently
        </div>

        <br clear="all" />

        <p class="options">
            <a href="#" onClick="Importer.form.submit( 'exactData', 'previous' ); return false;" class="previous">Previous</a>
            <a href="#" onClick="Importer.form.submit( 'exactData', 'import' ); return false;" class="import">Import</a>
        </p>
    </g:form>

    <r:script>
			Importer.exact.initialize( '${sessionKey}' );
    </r:script>
</div>
</body>
</html>

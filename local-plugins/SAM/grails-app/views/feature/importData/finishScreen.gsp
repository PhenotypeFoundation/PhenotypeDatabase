<html>
    <head>
      <meta name="layout" content="sammain"/>
      <title>Feature importer</title>
        
        <r:require module="importer" />
		<r:script disposition="">
			// This variable is set to false, so no warnings are given that the user exits
			// the importer
			warnOnRedirect = false;
		</r:script>          
    </head>
    <body>
        <content tag="contextmenu">
      		<g:render template="contextmenu" />
        </content>
        <div class="data">

            <imp:importerHeader pages="${pages}" page="saveData" />

            <h1>The importing process has finished.</h1>
            <g:if test="${message}">
				<p class="message">${message.toString()}</p>
			</g:if>
			<g:if test="${error}">
				<p class="error">${error.toString()}</p>
			</g:if>
            <p>The following features should now be available:<br />
                <g:each in="${featureList}" var="featureInstance" status="j">
                    <g:if test="${j>0}">, </g:if>
                    <g:link controller="feature" action="show" id="${featureInstance?.id}" params="${[module: module]}">${featureInstance?.name.encodeAsHTML()}</g:link>
                </g:each>
            </p>
        </div>
    </body>
</html>
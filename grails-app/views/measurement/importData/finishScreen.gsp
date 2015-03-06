<html>
    <head>
		<meta name="layout" content="sammain"/>
		<title>Measurement importer</title>
        
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

            <p>Your data has been successfully imported and is available now. It can be seen on the <g:link controller="SAMAssay" action="show" params="${[module: module]}" id="${assay.id}">${assay.name}"</g:link> overview page. If you wish to add more data you can do so <g:link controller="measurement" action="importData" params="${[module: module]}">by clicking here</g:link>.</p>
            <g:if test="${message}">
				<p class="message">${message.toString()}</p>
			</g:if>
			<g:if test="${error}">
				<p class="error">${error.toString()}</p>
			</g:if>
        </div>
    </body>
</html>
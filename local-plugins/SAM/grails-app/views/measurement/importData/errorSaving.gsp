<html>
    <head>
      <meta name="layout" content="sammain"/>
      <title>Your data has not been saved</title>
        
      <r:require module="importer" />
      
    </head>
    <body>
        <content tag="contextmenu">
            <g:render template="contextmenu" />
        </content>
        <div class="data">

            <imp:importerHeader pages="${pages}" page="saveData" />

            <g:if test="${message}">
                <p class="message">${message.toString()}</p>
            </g:if>
            <g:if test="${error}">
                <p class="error">${error.toString()}</p>
            </g:if>
            <p>
                Please review the issue(s) and try again.
                You can use the 'previous' button to go back to the previous page.
                Please note that none of your data has been saved yet.
            </p
            <g:form method="post" name="importData" action="importData">
                <imp:importerFooter>
                    <g:submitButton name="previous" value="« Previous" action="previous"/>
                </imp:importerFooter>
            </g:form>
        </div>
    </body>
</html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="sammain" />
    <title>SAM Feature Mapper</title>

    <r:require modules="mapper,gscfimporter"/>
</head>
<body>
<div class="basicTabLayout importer">
    <h1>
        <span class="truncated-title">
            Feature Mapper for ${module}
        </span>
    </h1>

    <span class="message info">
        %{--<span class="title">Choose platform</span>--}%
        Successfully added an external identifier to the Features in the list below.
    </span>

    <g:form controller="SAMHome" id="submitForm" name="submitForm">
        <g:hiddenField name="module" value="${module}"/>
        <fieldset>
            %{--<legend>Parameters</legend>--}%
            <table>
                <g:if test="${featureList.size() == 0}">
                    No new identifiers added
                </g:if>
                <g:else>
                    <g:each in="${featureList}" var="feature">
                        <tr>
                            <td><b>${feature.name}</b></td>
                            <td>${feature.externalIdentifier}</td>
                        </tr>
                    </g:each>
                </g:else>
            </table>
        </fieldset>

        <p class="options">
            <a href="#" onClick="$('#submitForm').submit()" class="next">Back to Home</a>
            %{--<g:link controller="SAMHome" params="${[module: module]}" class="next">Back to home</g:link>--}%
        </p>
    </g:form>
</div>
</body>
</html>

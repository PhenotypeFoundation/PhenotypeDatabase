<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    </head>
    <body>
        <table class="">
            <% def ii = 0%>
            <g:each in="${featureInstance.giveFields()}" var="field" status="i">
                <tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'}">
                    <td valign="top">
                        ${field.name.capitalize()}
                    </td>
                    <td valign="top" >
                        ${featureInstance.getFieldValue(field.toString())}
                    </td>
                    <% ii = i + 1%>
                </tr>
            </g:each>
        </table>
    </body>
</html>
<af:page>
    <g:each in="${mapResultPerEquationPerFeaturePerAssay}" var="a">
        <%-- Should display an Assay name --%>
        <h2>${assays[Integer.valueOf(a.key)]}</h2>
        <table>
            <% def sorted = a.value.sort{ b -> b.key }%>
            <%-- Should contain a sorted list of feature names --%>
            <g:each in="${sorted}" var="b">
                <%-- Should contain a bunch of equation/result pairs --%>
                <g:each in="${b.value}" var="c">
                    <tr>
                        <td>${b.key}</td>
                        <td>${c.key}</td>
                        <td>${c.value}</td>
                    </tr>
                </g:each>
            </g:each>
        </table>
    </g:each>
</af:page>
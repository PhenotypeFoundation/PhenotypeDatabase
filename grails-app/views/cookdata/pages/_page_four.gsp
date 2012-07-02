<af:page>
    <h1>Results</h1>
    <g:each in="${results}" var="pair" status="i">
        <h2>Resultset ${i}</h2>
        <table>
            <tr>
                <th>Dataset name</th>
                <th>Equation</th>
                <th>Aggregation</th>
                <th># samples in A</th>
                <th># samples in B</th>
                <th>Download</th>
            </tr>
            <tr>
                <td>${pair[0].datasetName}</td>
                <td>${pair[0].equation}</td>
                <td>${pair[0].aggr}</td>
                <td>${pair[0].samplesA.size()}</td>
                <td>${pair[0].samplesB.size()}</td>
                <td>
                  <g:actionSubmitImage alt="Download ${pair[0].datasetName} as Excel" value="result_${i}"  action="downloadExcel" src="${resource(dir: 'images/icons', file: 'page_excel.png', plugin: 'famfamfam')}"/>
                </td>
            </tr>
        </table>
        <table>
            <tr>
                <th>Feature</th>
                <th>Result</th>
            </tr>
            <g:each in="${pair[1]}" var="pair2">
                <tr>
                    <td>${pair2.key}</td>
                    <td>${pair2.value}</td>
                </tr>
            </g:each>
        </table>
    </g:each>
</af:page>
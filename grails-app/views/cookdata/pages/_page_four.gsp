<af:page>
    <h1>Select what format you wish to get the results in</h1>
    <g:each in="${results}" var="pair" status="i">
        <h2>Resultset ${i}</h2>
        <table>
            <tr>
                <th>Dataset name</th>
                <th>Equation</th>
                <th>Aggregation</th>
                <th># samples in A</th>
                <th># samples in B</th>
                <th>Individual download</th>
            </tr>
            <tr>
                <td>${pair[0].datasetName}</td>
                <td>${pair[0].equation}</td>
                <td>${pair[0].aggr}</td>
                <td>${pair[0].samplesA.size()}</td>
                <td>${pair[0].samplesB.size()}</td>
                <td>
                    <g:link action="pages" event="downloadOneResultAsExcel" params="[downloadResultId: i]">
                        <img src="${resource(dir: 'images/icons', file: 'page_excel.png', plugin: 'famfamfam')}"/>
                    </g:link>
            </tr>
        </table>
    </g:each>
    <p>
        <g:link action="pages" event="downloadAllResultsAsZip">
            <img src="${resource(dir: 'images/icons', file: 'page_white_zip.png', plugin: 'famfamfam')}"/>
            Download all results as a zip file
        </g:link>
        <g:link action="pages" event="downloadMeanAndMedianResults">
            <img src="${resource(dir: 'images/icons', file: 'table.png', plugin: 'famfamfam')}"/>
            Download mean and median results as an Excel file
        </g:link>

    </p>
</af:page>
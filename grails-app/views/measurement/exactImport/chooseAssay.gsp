<html>
    <head>
        <meta name="layout" content="sammain"/>
        <title>Measurement importer</title>
        <r:require module="importer" />
        
    </head>
    <body>
    <p><h2>Attention: All previous imported measurements for this assay will be lost!</h2></p>
        <div class="data">
            <p></p>
            <p></p>
            <div class="list">
                <dt:dataTable id="fList" class="paginate sortable filter selectOne" rel="${g.createLink( controller: 'feature', action: 'datatables_list' )}">
                    <thead>
                    <tr>
                        <th>Study</th>
                        <th>Assay</th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${assayList}" status="i" var="assayInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}" id="rowid_${assayInstance.id}">
                            <td>
                                ${assayInstance.parent.title}
                            </td>
                            <td>
                                ${assayInstance.name}
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </dt:dataTable>
            </div>
            <g:uploadForm controller="measurement" action="exactImport">
                <imp:importerHeader pages="${pages}" page="chooseAssay" />
            	<%-- 
            		If an assay has been selected before, and the user returns here, we don't select that assay. That has two reasons:
            		- the datatables scripts don't support a checkbox or radio being selected on load
            		- the user does want to select another assay, so the current selection is not important
            	 --%>
                <h2>Select the platform of the measurements you want to upload</h2>
                <g:select name="platform" from="${org.dbxp.sam.Platform.list()}" />

                <h2>Select the import file layout)</h2>
                <g:select name="layout" from="[ 'Sample layout', 'Subject layout']" />

                <h2>Please select a tab delimited text file (decimal: ".")</h2>

                <g:hiddenField name="id" value="${module}"/>
                <input name="contents" type="file" id="f">
                <imp:importerFooter>
                    <g:submitButton name="previous" value="« Previous" action="" disabled="true"/>

                    <g:hiddenField name="assay" value=""/>
                    <g:hiddenField name="module" value="${module}"/>
                    <g:submitButton name="next" value="Next »" action="next" onClick="
                        if( elementsSelected == undefined || elementsSelected[ 'fList_table' ] == undefined || elementsSelected[ 'fList_table' ].length == 0 ) {
                            return false;
                        } else {
                            \$( '#assay' ).val( elementsSelected[ 'fList_table' ][ 0] );
                            return true;
                        }
                    "/>
                </imp:importerFooter>
            </g:uploadForm>
        </div>
    </body>
</html>
<html>
    <head>
        <meta name="layout" content="sammain"/>
        <title>Measurement importer</title>
        
        <r:require module="importer" />
        
    </head>
    <body>
        <content tag="contextmenu">
            <g:render template="contextmenu" />
        </content>
        <div class="data">

            <imp:importerHeader pages="${pages}" page="chooseAssay" />
            <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_TEMPLATEADMIN"><h2>Good day (Template) Admin, for large datasets please use the <g:link controller="measurement" action="exactImportSelect" params="${[module: module]}"><b><u>Exact Importer</u></b></g:link></h2></sec:ifAnyGranted>
            <p>The data you wish to import must be related to an assay. Choose the assay in question from the following list.</p>
            <p>Please note:  Assays without samples are not shown in this list. To be able to upload measurements, make sure your assay contains samples. </p>
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
            <g:form method="post" name="importData" action="importData">
            	<%-- 
            		If an assay has been selected before, and the user returns here, we don't select that assay. That has two reasons:
            		- the datatables scripts don't support a checkbox or radio being selected on load
            		- the user does want to select another assay, so the current selection is not important
            	 --%>
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
            </g:form>
        </div>
    </body>
</html>
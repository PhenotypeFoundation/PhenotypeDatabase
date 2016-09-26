<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="sammain" />
        <g:set var="entityName" value="${message(code: 'assay.label', default: 'Assay')}" />
        <title><g:message code="default.module.label" args="[entityName, module]" /></title>
    </head>
    <body>
        <h1><g:message code="default.samlist.label" args="[entityName, module]" /></h1>

        <div class="data">
            <dt:dataTable id="fList" class="paginate sortable filter serverside" rel="${g.createLink( controller: 'SAMAssay', action: 'datatables_list', params: [module: module] )}">
                <thead>
                    <tr>
                        <th>Study</th>
                        <th>Assay</th>
                        <th class="nonsortable">Samples/ModuleSamples</th>
						<th class="nonsortable"></th>
                    </tr>
                </thead>
            </dt:dataTable>
        </div>
    </body>
</html>

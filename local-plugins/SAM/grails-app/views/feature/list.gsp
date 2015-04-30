<%@ page import="org.dbnp.gdt.Template; org.dbxp.sam.Feature" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="sammain"/>
        <g:set var="entityName" value="${message(code: 'feature.label', default: 'Feature')}" />
        <title><g:message code="default.module.label" args="[entityName, module]" /></title>
    </head>

    <body>
      	<content tag="contextmenu">
      		<g:render template="contextmenu" />
        </content>
        <h1><g:message code="default.samlist.label" args="[entityName, module]" /></h1>

        <div class="data">
            <dt:dataTable id="fList" class="paginate sortable filter selectMulti serverside" rel="${g.createLink( controller: 'feature', action: 'datatables_list', params: [module: module] )}">
                <thead>
                    <tr>

                        <th>Platform</th>

                        <th>Name</th>

                        <th>Unit</th>

                        <th>Template</th>

                        <dt:buttonsHeader/>

                    </tr>
                </thead>
            </dt:dataTable>
            <br />
            <ul class="data_nav buttons">
                    <li><a href="#" class="delete" onclick="if(confirm('Are you sure?')) {submitPaginatedForm('fList','delete?module=${module}', 'No rows selected');} else {return false;}">Delete all marked features</a></li>
            </ul>
        </div>
    </body>
</html>

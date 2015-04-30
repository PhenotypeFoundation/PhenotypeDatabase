<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="sammain"/>
        <g:set var="entityName" value="${message(code: 'platform.label', default: 'Measurement')}" />
        <title><g:message code="default.module.label" args="[entityName, module]" /></title>
    </head>

    <body>
        <content tag="contextmenu">
            <g:render template="contextmenu" />
        </content>
        <h1><g:message code="default.samlist.label" args="[entityName, module]" /></h1>

        <div class="data">
            <dt:dataTable id="mList" class="paginate sortable filter selectMulti">
                <thead>
                <tr>
                    <th>Assay</th>
                    <th>Sample</th>
                    <th>Feature</th>

                    <th>Value</th>
                    <th>Operator</th>
                    <th>Comments</th>

                    <dt:buttonsHeader/>

                </tr>
                </thead>
                <tbody>
                    <g:each in="${measurementInstanceList}" status="i" var="measurementInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}" id="rowid_${measurementInstance.id}">
                            <td></td>
                            <td>${measurementInstance.sample.name}</td>

                            <td>${measurementInstance.feature.name}</td>

                            <td>
                                <g:link action="show" id="${measurementInstance.id}">${measurementInstance.value}</g:link>
                            </td>

                            <td>${fieldValue(bean: measurementInstance, field: "operator")}</td>

                            <td>${fieldValue(bean: measurementInstance, field: "comments")}</td>

                            <dt:buttonsShowEditDelete controller="measurement" id="${measurementInstance.id}" />
                        </tr>
                    </g:each>
                </tbody>
            </dt:dataTable>
            <br />
            <ul class="data_nav buttons">
                <li><a href="#" class="delete" onclick="if(confirm('Are you sure?')) {submitPaginatedForm('mList','delete?module=${module}', 'No rows selected');} else {return false;}">Delete all marked measurements</a></li>
            </ul>
        </div>
    </body>
</html>

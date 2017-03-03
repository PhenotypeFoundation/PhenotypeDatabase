<%@ page import="java.math.MathContext" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="sammain"/>
    <g:set var="entityName"
           value="${message(code: 'study.label', default: 'Study')}"/>
    <title>Show ${module} assay ${assayInstance.name}</title>

    <style type="text/css">
    .delete_button {
        display: none;
    }

    td:hover .delete_button {
        display: inline;
    }
    </style>
    <r:require modules="tiptip"/>
</head>

<body>
<content tag="contextmenu">
    <li><g:link action="list" class="list"
                params="${[module: module]}">Back to list</g:link></li>
</content>

<h1>${module} ${assayInstance.name} / ${assayInstance.parent.title}</h1>

<span class="message info">
    <span class="title">List of measurements</span>
    You can select measurements by clicking on them. Comments to the measurements are denoted with an icon.
</span>

<g:if test="${measurements.size() > 0}">
    <ul class="data_nav buttons ontop">
        <li><g:link class="delete" controller="measurement" action="delete"
                    onClick="if( \$( '#deleteform input:checked' ).length != 0 && confirm('Are you sure?') ) { \$( '#deleteform' ).submit(); } return false; ">Delete selected measurements</g:link></li>
        <li><g:link class="delete" controller="measurement"
                    action="deleteByAssay" id="${assayInstance.id}"
                    params="${[module: module]}"
                    onClick="return confirm('Are you sure?');">Delete all measurements</g:link></li>
        <li><g:link class="delete" controller="SAMSample" action="deleteByAssay"
                    id="${assayInstance.id}" params="${[module: module]}"
                    onClick="return confirm('Are you sure?');">Delete all measurements & module samples</g:link></li>
    </ul>

    <form id="deleteform"
          action="<g:createLink controller="measurement" action="delete"/>"
          method="post">
        <input type="hidden" name="assayId" value="${assayInstance.id}"/>
        <input type="hidden" name="module" value="${module}"/>
        <table class="measurements">
            <thead>
            <tr>
                <th></th>
                <g:each var="feature" in="${features}">
                    <th>${feature} [${feature.unit}]</th>
                </g:each>
            </tr>
            </thead>
            <tbody>
            <g:set var="measurementIndex" value="${0}"/>
            <g:each var="sample" in="${samples}">
                <tr>
                    <th style="min-width: 200px">${sample.name}</th>

                    <g:each var="feature" in="${features}">
                    <%--
                      In every table cell, we should lookup the measurement that belongs to this sample and feature.
                      Because the measurements are ordered in the same way as they are outputted to the screen
                      ( sample.name, feature.name ), we can easily check whether the 'current' measurement belongs
                      to this cell. If not, we keep this cell empty.

                      Because there might be multiple measurements for one cell, we first find all measurements for this cell.
                      We show always the value/operator of the first measurement, but show all data in the comments field.
                    --%>
                        <g:set var="currentMeasurement"
                               value="${measurements[measurementIndex]}"/>
                        <g:set var="measurementIndex" value="${measurementIndex + 1}" />

                    <%--
                      Now we know all measurements for this cell and the measurementIndex points to the
                      next measurement. If there are multiple measurements, we combine the data.
                    --%>
                        <g:if test="${currentMeasurement}">
                            <%
                                def comments = currentMeasurement.comments?.encodeAsHTML()
                                def isNumeric = currentMeasurement.value.toString().isNumber() %>

                            <td id="td${currentMeasurement.id}"
                                class="${comments && isNumeric ? 'comments' : ''}">
                                <input type="checkbox"
                                       id="check${currentMeasurement.id}"
                                       name="ids"
                                       value="${currentMeasurement.id}"
                                       style="display:none;"/>

                                <g:if
                                    test="${currentMeasurement.operator}">${currentMeasurement.operator}</g:if>
                                <g:if test="${isNumeric}">
                                    <g:if test="${comments}">
                                    <%-- numeric value and comments --%>
                                        <span class="tooltip"
                                              title="${comments}">${currentMeasurement.value}</span>
                                    </g:if>
                                    <g:else>
                                        <g:if
                                            test="${currentMeasurement.value == currentMeasurement.value.round(3)}">
                                        <%-- short numeric value without comments --%>
                                            <span>${currentMeasurement.value}</span>
                                        </g:if>
                                        <g:else>
                                        <%-- long numeric value without comments; render short version and put entire number in tooltip --%>
                                            <span class="tooltip"
                                                  title="${currentMeasurement.value}">${currentMeasurement.value.round(3).toString()}</span>
                                        </g:else>
                                    </g:else>
                                </g:if>
                                <g:else>
                                <%-- measurement is not numeric, so use text value from comments --%>
                                    <span class="tooltip"
                                          title="${comments}">${comments}</span>
                                </g:else>
                            </td>
                        </g:if>
                        <g:else>
                            <td></td>
                        </g:else>
                    </g:each>
                </tr>
            </g:each>
            </tbody>
        </table>

        <div class="paginateButtons">
            <g:paginate controller="measurements" mapping="showAssayPagination"
                        action="show" id="${assayInstance.id}"
                        total="${numberOfSamples}" offset="${offset}"
                        params='[numberOfSamples: numberOfSamples, module: "${module}"]'/>
        </div>
    </form>

    <br/>
    <ul class="data_nav buttons">
        <li><g:link class="delete" controller="measurement" action="delete"
                    onClick="if( \$( '#deleteform input:checked' ).length != 0 && confirm('Are you sure?') ) { \$( '#deleteform' ).submit(); } return false; ">Delete selected measurements</g:link></li>
        <li><g:link class="delete" controller="measurement"
                    action="deleteByAssay" id="${assayInstance.id}"
                    params="${[module: module]}"
                    onClick="return confirm('Are you sure?');">Delete all measurements</g:link></li>
        <li><g:link class="delete" controller="SAMSample" action="deleteByAssay"
                    id="${assayInstance.id}" params="${[module: module]}"
                    onClick="return confirm('Are you sure?');">Delete all measurements & module samples</g:link></li>
    </ul>

    <g:if test="${hideEmpty}">
        <g:if test="${emptySamples > 0}">
            <p>
                ${emptySamples} sample(s) are not shown because they have no measurements.
                Click <g:link action="show"
                              params="['id': assayInstance.id, 'hideEmpty': false, module: module]">here</g:link> to show all.
            </p>
        </g:if>
    </g:if>
    <g:else>
        <p>
            Click <g:link action="show"
                          params="['id': assayInstance.id, 'hideEmpty': true, module: module]">here</g:link> to hide samples without measurements.
        </p>
    </g:else>
</g:if>
<g:else>
    <p>
        No measurements were found for this assay. Use the
        <g:link controller="SAMImporter" action="upload"
                params="${[importer: "Measurements", module: module]}">importer</g:link>
        to import your data  or add your measurements <g:link
            controller="measurement" action="create"
            params="${[module: module]}">manually</g:link>.
    </p>
</g:else>

<r:script>
    $('.measurements td').on('click', function () {
        // Update checkbox on click
        var checkbox = $(this).find("input[type=checkbox]");
        if (checkbox.length > 0) {
            checkbox.prop('checked', !checkbox.prop('checked'));
            $(this).toggleClass('selected', checkbox.prop('checked'));
        }
    }).each(function (idx, el) {
        // Initialize styling based on checkboxes
        var checkbox = $(this).find("input[type=checkbox]");
        if (checkbox.length > 0) {
            $(this).toggleClass('selected', checkbox.prop('checked'));
        }
    });
</r:script>
</body>
</html>

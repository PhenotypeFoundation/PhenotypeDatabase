<%@ page import="org.dbnp.gdt.RelTime" %>
<%@ page import="org.dbnp.gdt.TemplateFieldType" %>
<af:page>
<script type="text/javascript">
    // Global that is needed to give each new dataset a unique name
    <g:if test="${pageThreeDatasetCounter}">
        var datasetCounter = ${pageThreeDatasetCounter};
    </g:if>
    <g:else>
        var datasetCounter = 1;
    </g:else>
</script>

<h1>Datasets:</h1>
<p>Create datasets by adding group A, group B and an equation.</p>

<table id="datasettable">
    <g:if test="${pageThreeDatasetTableHtml}">
        ${pageThreeDatasetTableHtml.decodeHTML()}
    </g:if>
    <g:else>
        <thead>
        <tr>
            <th></th>
            <th>Dataset name</th>
            <th>Group A</th>
            <th>Group B</th>
            <th>Equation</th>
            <th>Aggregation</th>
        </tr>
        </thead>
        <tbody>
        <tr id="addnewdatasetrow" class="fontsmall">
            <td colspan="6"><a href="#" onclick="addRow(); return false;"><img src="${fam.icon( name: 'add' )}" style="vertical-align: text-bottom; display: inline-block;"/> add new dataset</a></td>
        </tr>
        </tbody>
    </g:else>
</table>
<p class="fontsmall">
    <a href="#" onclick="duplicateRow(); return false;"><img src="${fam.icon( name: 'page_copy' )}" style="vertical-align: text-bottom; display: inline-block;"/> duplicate selected dataset</a>&nbsp;
    <a href="#" onclick="deleteRow(); return false;"><img src="${fam.icon( name: 'delete' )}" style="vertical-align: text-bottom; display: inline-block;"/> delete selected dataset</a>
</p>

<div id="settingsDiv">
    <h1>1. Edit sets A and B for: <span class="datasetTitleHere">...</span></h1>
    <p>Please indicate to which set you wish to assign each series of samples:</p>
    <g:each in="${samplingEventTemplates}" var="template">
        <h2>${template.name}</h2>
        <table id="sampleTable">
            <thead>
            <tr>
                <th># samples in event</th>
                <g:each in="${samplingEventFields}" var="field">
                    <th>${field.name}</th>
                </g:each>
                <th>Group</th>
                <th>Set A</th>
                <th>Set B</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${selectionTriples}" var="pair" status="p">
                <g:if test="${samplingEvents[pair[0]]?.template == template}">
                    <tr>
                        <td class="numSamples"><%
                        // This should be: out << samplingEvents[pair[0]]?.samples?.count { it.parentEventGroup.equals(eventGroups[pair[1]]) == true};
                        // But for some strange reason the count closure does not seem to work. This works:
                        def samples = samplingEvents[pair[0]]?.samples
                        def eventGroup = eventGroups[pair[1]]
                        def counts = samples.collect { it.parentEventGroup.equals(eventGroup)}
                        out << counts.count(true)%></td>
                        <g:each in="${samplingEventFields}" var="field">
                            <td>
                                <g:if test="${field.type == TemplateFieldType.RELTIME}">
                                    <g:if test="${samplingEvents[pair[0]]?.fieldExists(field.name)}">
                                        ${new RelTime( samplingEvents[pair[0]]?.getFieldValue(field.name) ).toString()}
                                    </g:if>
                                </g:if>
                                <g:else>
                                    <g:if test="${samplingEvents[pair[0]]?.fieldExists(field.name)}">
                                        ${samplingEvents[pair[0]]?.getFieldValue(field.name)}
                                    </g:if>
                                </g:else>
                            </td>
                        </g:each>
                        <td>${eventGroups[pair[1]]?.name}</td>
                        <td><g:checkBox name="A_${p}" onClick="checkSampleGroup();"/></td>
                        <td><g:checkBox name="B_${p}" onClick="checkSampleGroup();"/></td>
                    </tr>
                </g:if>
            </g:each>
            </tbody>
        </table>
    </g:each>

    <h1>2. Define aggregation for: <span class="datasetTitleHere">...</span></h1>
    <div id="calculatorRadios">
        Indicate how you wish to handle the values in sets:<br />
        <g:radio name="calculatorMode" id="modeAverage" value="average" checked="true" onclick="changeAggr(this);"/>
        <label for="modeAverage">average: A and B represent the average of the values in group A and group B.</label><br />
        <g:radio name="calculatorMode" id="modeMedian" value="median" onclick="changeAggr(this);"/>
        <label for="modeMedian">median: A and B represent the median of the values in group A and group B.</label><br />
        <g:radio name="calculatorMode" id="modePairwise" value="pairwise" onclick="changeAggr(this);"/>
        <label for="modePairwise">pairwise: A and B represent matching samples from group A and group B.</label><br />
        <g:radio name="calculatorMode" id="modeValues" value="values" onclick="changeAggr(this);"/>
        <label for="modeValues">values: Only use A in order to return the measurements.</label>
    </div>

    <h1>3. Build equation for: <span class="datasetTitleHere">...</span></h1>
    <div id="calculator">
        <div id="calculatorButtons">
            <button type="button" onclick="addSymbol('(');">(</button>
            <button type="button" onclick="addSymbol(')');">)</button>
            <button type="button" onclick="addSymbol('A');">A</button>
            <button type="button" onclick="addSymbol('B');">B</button>
            <button type="button" onclick="addSymbol('-');">-</button>
            <button type="button" onclick="addSymbol('+');">+</button>
            <button type="button" onclick="addSymbol('/');">/</button>
            <button type="button" onclick="addSymbol('*');">*</button>
            <button type="button" onclick="addSymbol('2log(');">2log</button>
            <button type="button" onclick="addSymbol('ln(');">ln</button>
            <button type="button" onclick="addSymbol('median(');">median</button>
            <button type="button" onclick="addSymbol('avg(');">avg</button>
            <button type="button" onclick="validateEquation();" id="buttonAddEquation">Validate equation</button>
        </div>
        <div>
            <input id="calculatorInput" type="text" onkeyup="addSymbol('');">
        </div>
    </div>
    <div id="settingsDivBlurr"></div>
</div>

<%-- This input is used to transmit #datasettable contents to the server, for preservation in the flow --%>
<div id="datasetTableHtmlDiv" style="display: none"></div>

</af:page>

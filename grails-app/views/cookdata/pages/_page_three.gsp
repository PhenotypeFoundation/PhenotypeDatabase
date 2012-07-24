<%@ page import="org.dbnp.gdt.RelTime" %>
<%@ page import="org.dbnp.gdt.TemplateFieldType" %>
<af:page>
<style>
.selectionSummaryTd{
    padding: 0px !important;
    border-style: none !important;
}
.selectionSummaryDiv{
    border: 1px solid #CCCCCC;
    margin-left: 5px;
    margin-right: 5px;
}
#buttonAddEquation{
    background-color: #F77777;
    color: #FFFFFF;
}
#calculatorInput{
    width: 90%;
}
#addnewdatasetrow td{
    text-align: center;
    color: #AAA;
}
#datasettable td {
    margin: 1px;
    padding: 3px;
    border-top: 2px solid white;
    border-bottom: 2px solid white;
}
#datasettable td:first-child {
    border-left: 2px solid white;
}
#datasettable td:last-child {
    border-right: 2px solid white;
}
.fontsmall, .fontsmall td{
    font-size: 10px;
}
div#settingsDiv {
    position: relative;
}
div#settingsDivBlurr {
    position: absolute;
    top: 0;
    left: 0;
    height: 100%;
    width: 100%;
    background-color: #EEE;
    z-index: 300;
    opacity: 0.7;
    border: 1px solid #888;
}
#datasettable tr.rowselected td {
    border-top: 2px solid #006DBA;
    border-bottom: 2px solid #006DBA;
}
#datasettable tr.rowselected td:first-child {
    border-left: 2px solid #006DBA;
}
#datasettable tr.rowselected td:last-child {
    border-right: 2px solid #006DBA;
}

</style>

<h1>Datasets:</h1>
<p>Create datasets by adding group A, group B and an equation.</p>
<script type="text/javascript">
    // Global that is needed to give each new dataset a unique name
    var datasetCounter = 1;

    // Function that is called each time a tr that represents a dataset in the datasettable is clicked
    function rowClick(that) {
        // If a row is selected the blurr must be removed
        $("#settingsDivBlurr").hide();
        // We want to check the radiobutton of this row
        $(that).find(".radiobutton").attr("CHECKED","CHECKED");
        // Remove all selected styling from the table
        $("#datasettable .rowselected").removeClass("rowselected");
        // Add selected styling to this row
        $(that).addClass("rowselected");
        // Add the title of the current dataset to the settings
        $(".datasetTitleHere").html( $(that).find(".datasetname").val() );
        // Add the equation of the current dataset to the settings
        $("#calculatorInput").val( $(that).find(".equation").val() );
        // Add the equationtype of the current dataset to the settings
        $("#calculatorRadios input[value="+$(that).find(".aggregation").val()+"]").attr("CHECKED","CHECKED");
        // Remove the validate img
        $("#validationEqImg").remove();
        // Add the groupselections of group A and group B to the settings
        var strGroupSelect = $(that).find(".groupAselection").val();
        if(strGroupSelect.length>0) {
            strGroupSelect = strGroupSelect + ".";
        }
        var strGroupSelect = strGroupSelect + $(that).find(".groupBselection").val();
        var arrGroupSelect = strGroupSelect.split(".");

        // Uncheck all
        $("#sampleTable input[type=checkbox]").attr("CHECKED",false);
        // Check the ones representing the current selected row
        $.each(arrGroupSelect, function() {
            $("#sampleTable input[name="+this+"]").attr("CHECKED",true);
        });
    }

    // Function that is called each time the "add dataset" link is clicked
    function addRow() {
        var newRow = $("<tr>").click(function(){rowClick(this);})
                .addClass("rowselected")
                .append('<td>                                             <input type="radio"  class="radiobutton"     name="radiobutton"  CHECKED/></td>')
                .append('<td>                                             <input type="text"   class="datasetname"     name="dataset_name" onkeyup="changeDatasetName(this);" style="width:90%" value="Dataset '+datasetCounter+'"/></td>')
                .append('<td><span class="samplesgroupA">0</span> samples <input type="hidden" class="groupAselection" name="dataset_grpA" value="" /></td>')
                .append('<td><span class="samplesgroupB">0</span> samples <input type="hidden" class="groupBselection" name="dataset_grpB" value="" /></td>')
                .append('<td><span class="equationshow"></span>           <input type="hidden" class="equation"        name="dataset_equa" value="" /></td>')
                .append('<td><span class="aggregationshow">average</span> <input type="hidden" class="aggregation"     name="dataset_aggr" value="average" /></td>');
        $("#addnewdatasetrow").before(newRow);
        // Click this new row to select it (trigger rowClick())
        newRow.click();

        datasetCounter = datasetCounter + 1;
    }

    // Function that is called each time the "delete dataset" link is clicked
    function deleteRow() {
        var optionChecked = $("#datasettable input.radiobutton:checked");
        if(optionChecked.size()>0) {
            optionChecked.parent().parent().remove();
            $("#settingsDivBlurr").show();
        }
        // Clear some settings
        $(".datasetTitleHere").html('...');
        $("#sampleTable input[type=checkbox]").attr("CHECKED",false);
        $("#calculatorInput").val("");
    }

    // Function that is called each time the "duplicate dataset" link is clicked
    function duplicateRow() {
        var optionChecked = $("#datasettable input.radiobutton:checked");
        if(optionChecked.size()>0) {
            // If a row is selected, clone (incl data) this row
            var newRow = optionChecked.parent().parent().clone(true);
            $("#datasettable .rowselected").removeClass("rowselected");
            $("#addnewdatasetrow").before(newRow);
            $("#settingsDivBlurr").hide();
        }
    }

    // Function that is called each time a datasetname is changed
    // param that = the context (this) of the input where the name is typed
    function changeDatasetName(that) {
        // Add the changed title of the current dataset to the settings
        $(".datasetTitleHere").html($(that).val());
    }

    // Function that is called when an eventgroupcheckbox is checked or unchecked
    function checkSampleGroup() {
        var countA = 0;
        var countB = 0;
        var strGroupA = "";
        var strGroupB = "";

        $("#sampleTable tbody tr").each(function () {

            var numSamp = parseInt($(this).find("td.numSamples").html());

            var objCheckB = $(this).find("input[type=checkbox]").get(0);

            if($(objCheckB).attr("CHECKED")=="checked") {
                countA = countA + numSamp;
                if(strGroupA.length>0) {
                    strGroupA = strGroupA + ".";
                }
                strGroupA = strGroupA + $(objCheckB).attr("name");
            }

            objCheckB = $(this).find("input[type=checkbox]").get(1);
            if($(objCheckB).attr("CHECKED")=="checked") {
                countB = countB + numSamp;
                if(strGroupB.length>0) {
                    strGroupB = strGroupB + ".";
                }
                strGroupB = strGroupB + $(objCheckB).attr("name");
            }
        });

        $("#datasettable .rowselected .samplesgroupA").html( countA );
        $("#datasettable .rowselected .groupAselection").val( strGroupA );

        $("#datasettable .rowselected .samplesgroupB").html( countB );
        $("#datasettable .rowselected .groupBselection").val( strGroupB );
    }

    // Function that is called when a normal button of the equation editor is clicked or when there is typed in the equation textbox
    function addSymbol(symbol){
        $("#calculatorInput").val($("#calculatorInput").val()+symbol);
        $("#datasettable .rowselected .equation").val( $("#calculatorInput").val() );
        $("#datasettable .rowselected .equationshow").html( $("#calculatorInput").val() );
    }

    // Function that is called when the validate button is pressed
    function validateEquation(){
        $.ajax({
            url: 'testEquation',
            data: {equation: $("#calculatorInput").val()},
            dataType: 'JSON',
            async: false,
            success: function(data) {
                $("#validationEqImg").remove();
                if(!data.status) {
                    $("#calculatorInput").before('<img id="validationEqImg" src="${fam.icon( name: 'exclamation' )}" style="vertical-align: text-bottom; display: inline-block;"/>');
                } else {
                    $("#calculatorInput").before('<img id="validationEqImg" src="${fam.icon( name: 'accept' )}" style="vertical-align: text-bottom; display: inline-block;"/>');
                }
            }
        });
    }

    // Function that is called when the aggregation option is changed
    function changeAggr(that) {
        $("#datasettable .rowselected .aggregation").val($(that).val());
        $("#datasettable .rowselected .aggregationshow").html($(that).val());
    }

</script>

<table id="datasettable">
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
                        <td class="numSamples">${samplingEvents[pair[0]]?.samples?.size()}</td>
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
            <input id="calculatorInput" type="text" onkeyup="addSymbol('');"></input>
        </div>
    </div>
    <div id="settingsDivBlurr"></div>
</div>
</af:page>

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

    // Update #datasettableHTML to ensure up-to-date table is sent to server. See #datasettableHTML for details.
    updateFlowItems();
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
        // Update #datasettableHTML to ensure up-to-date table is sent to server. See #datasettableHTML for details.
        updateFlowItems();
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
    // Update #datasettableHTML to ensure up-to-date table is sent to server. See #datasettableHTML for details.
    updateFlowItems();
}

// Function that is called when a normal button of the equation editor is clicked or when there is typed in the equation textbox
function addSymbol(symbol){
    $("#calculatorInput").val($("#calculatorInput").val()+symbol);
    $("#datasettable .rowselected .equation").val( $("#calculatorInput").val() );
    $("#datasettable .rowselected .equationshow").html( $("#calculatorInput").val() );
    // Update #datasettableHTML to ensure up-to-date table is sent to server. See #datasettableHTML for details.
    updateFlowItems();
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
                $("#calculatorInput").before('<img id="validationEqImg" src="${fam.icon( name: \'exclamation\' )}" style="vertical-align: text-bottom; display: inline-block;"/>');
            } else {
                $("#calculatorInput").before('<img id="validationEqImg" src="${fam.icon( name: \'accept\' )}" style="vertical-align: text-bottom; display: inline-block;"/>');
            }
        }
    });
}

// Function that is called when the aggregation option is changed
function changeAggr(that) {
    $("#datasettable .rowselected .aggregation").val($(that).val());
    $("#datasettable .rowselected .aggregationshow").html($(that).val());
    // Update #datasettableHTML to ensure up-to-date table is sent to server. See #datasettableHTML for details.

    }

function updateFlowItems(){
    $("#datasetTableHtmlDiv").html(
        '<input id="datasetTableHtml" name="datasetTableHtml" value="' +
            htmlEscape($("#datasettable tbody").html())+'" type="text">'+
            '<input id="datasetCounter" name="datasetCounter" value="' +
            datasetCounter+'" type="text">'
    );
}

// http://stackoverflow.com/questions/1219860/javascript-jquery-html-encoding
function htmlEscape(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}
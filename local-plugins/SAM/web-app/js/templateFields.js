// This JS contains templateField time/date related functionality, taken from GSCF's studyWizard.js.

function onTemplateFieldPage() {
	// GENERAL
	attachDatePickers();
	attachDateTimePickers();
	disableKeys();
	disableDatePickerKeys();
}

// disable all key presses in every text element which has a datapicker
// attached
function disableDatePickerKeys() {
	$(".hasDatepicker").each(function() {
		$(this).bind('keydown', function(e) { return false; });
	});
}

// add datepickers to date fields
function attachDatePickers() {
	$("input[type=text][rel$='date']").each(function() {
		$(this).datepicker({
			changeMonth : true,
			changeYear  : true,
			/*numberOfMonths: 3,*/
			showButtonPanel: true,
			dateFormat  : 'dd/mm/yy',
			yearRange   : 'c-80:c+20',
			altField	: '#' + $(this).attr('name') + 'Example',
			altFormat   : 'DD, d MM, yy'
		});
	});
}

// add datetimepickers to date fields
function attachDateTimePickers() {
	$("input[type=text][rel$='datetime']").each(function() {
		$(this).datepicker({
			changeMonth	 : true,
			changeYear	  : true,
			dateFormat	  : 'dd/mm/yy',
			altField		: '#' + $(this).attr('name') + 'Example',
			altTimeField	: '#' + $(this).attr('name') + 'Example2',
			altFormat	   : 'DD, d MM, yy',
			showTime		: true,
			time24h		 : true
		});
	});
}

function disableKeys() {
	// disable enter key in input boxes to make sure
	// accidental submits do not happen
	$('input:text').each(function() {
		$(this).bind('keypress', function(e) {
			if (e.keyCode == 13) {
				return false;
			}
		});
	});
}

// insert add / modify option in template menu
function insertSelectAddMore() {
    new SelectAddMore().init({
        rel     : 'template',
        url     : baseUrl + '/templateEditor',
        vars    : 'entity,ontologies',
        label   : 'add / modify',
        style   : 'modify',
        onClose : updateTemplateDropdown
    });
}

function insertSelectAddMoreForTemplateFields() {
    new SelectAddMore().init({
        rel	    : 'term',
        url	    : baseUrl + '/termEditor',
        vars	: 'ontologies',
        label   : 'add more...',
        style   : 'modify'
    });
}

var entityName;
var formSection;
var lastSelectedOption = $("#template option:selected").val();

//var handleTemplateChange = function (entityName, formSection) {
    var handleTemplateChange = function (selectedOption) {
        if( $(selectedOption).hasClass( 'modify' ) ){
            // If the user has selected the add/modify option, the form shouldn't be updated yet.
            // That should only happen if the template editor is closed
            return;
        }

        lastSelectedOption = $(selectedOption).val();

        $.ajax({
            url: baseUrl + "/" + entityName + "/returnUpdatedTemplateSpecificFields",
            data: $( formSection ).serialize(),
            type: "POST",
            success: function( returnHTML, textStatus, jqXHR ) {
                $( "#templateSpecific" ).html( returnHTML );
                onTemplateFieldPage(); // Add datepickers
                insertSelectAddMoreForTemplateFields();
            }
        });
    };

    var updateTemplateDropdown = function () {
        $.ajax({
            url: baseUrl + "/" + entityName + "/templateSelection",
            data: $( formSection ).serialize() ,
            type: "POST",
            success: function( returnHTML, textStatus, jqXHR ) {
                $( "td#templateSelection" ).html( returnHTML );
                insertSelectAddMore();

                if(lastSelectedOption != "") {
                    $("select[rel*='template']").val(lastSelectedOption).change();
                }
            }
        });
    };
    //return handleFunction;
//};

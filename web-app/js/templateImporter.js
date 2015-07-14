if( typeof( TemplateImporter ) === "undefined" ) { 
	TemplateImporter = {};
}

TemplateImporter.initialize = function() {
	TemplateImporter.bindUI();
};

TemplateImporter.bindUI = function() {
	// Enable clicking the 'change name' button
	$( "#importTemplates" ).on( "click", ".changeName", function() {
		var td = $(this).parents("td");
		td.find( "h3" ).hide();
		td.find( ".otherTemplateName" ).show();
		
		return false;
	});
	
	$( "#importTemplates" ).on( "change", "input[type=text]", TemplateImporter.checkName );
};

// Checks whether a given template name is valid
TemplateImporter.checkName = function() {
	var name = $(this).val().trim();
	var td = $(this).parents("td");
	var message = "";
	
	// Check for empty name
	if( name == "" ) {
		message = "The name cannot be left blank.";
	}
	
	// Check whether the name already exists
	var existingNames = $.map( td.find( ".existingNames li" ), function(el) { return $(el).text(); } );
	if( $.inArray(name, existingNames ) > -1 ) {
		message = "A template with this name already exists. Please choose another name.";
	}
	
	if( message ) {
		td.find( ".error" ).text(message).show();
	} else {
		td.find( ".error" ).hide();
	}
}


$(TemplateImporter.initialize);
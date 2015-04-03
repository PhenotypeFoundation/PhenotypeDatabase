if( typeof( StudyView ) === "undefined" ) { 
	StudyView = {};
}

StudyView.initialize = function() {
	attachHelpTooltips();
}

StudyView.initializePropertiesPage = function() {
	StudyView.form.initialize();
	
    // Initialize help tooltips
    attachHelpTooltips();
}

StudyView.studyChildren = {
	refresh: function( table ) {
		if( table.length == 0 ) {
			location.reload();
			return;
		}
		$.each( table, function( idx, datatable ) {
			$(datatable).dataTable().fnDraw();
		});
	},
	initialize: function( entityMethods, title ) {
	},
}

/**
 * Handles adding and deleting subjects
 */
StudyView.subjects = {
	// Reload data for the datatable
	refresh: function() {
		StudyView.studyChildren.refresh( $( "#subjects .dataTables_scrollBody .dataTable" ) )
	},
	initialize: function() {
		StudyView.studyChildren.initialize( StudyView.subjects, "Add subject(s)" );
	},
	
};

/**
 * Handles adding and deleting samples
 */
StudyView.samples = {
	// Reload data for the datatable
	refresh: function() {
		StudyView.studyChildren.refresh( $( "#samples .dataTables_scrollBody .dataTable" ) )
	},
	initialize: function() {
		StudyView.studyChildren.initialize( StudyView.samples, "Add sample(s)" );
	},
};

/**
 * Handles adding and deleting assays
 */
StudyView.assays = {
	// Reload data for the datatable
	refresh: function() {
		StudyView.studyChildren.refresh( $( "#assays .dataTables_scrollBody .dataTable" ) )
	},
	initialize: function() {
		StudyView.studyChildren.initialize( StudyView.assays, "Add assay(s)" );
	},
};

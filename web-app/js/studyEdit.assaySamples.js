if( typeof( StudyEdit ) === "undefined" ) { 
	StudyEdit = {};
};

StudyEdit.assaySamples = {
	initialize: function() {
		// Initialize samples table
		$(".samplesTable").data( "datatable-options", {
			"sScrollX": "100%",
			"bScrollCollapse": true,
			
			bFilter: true, 
			bLengthChange: true, 
			bPaginate: true,
			bSort: true,
			bInfo: true,
		    aoColumnDefs: [
		      { sWidth: "30px", aTargets: [ 0 ] },
				{
					"mRender": function ( val, type, full ) {
						return '<input type="checkbox" data-original="' + val + '" ' + ( val ? 'yy checked="checked" ' : "" ) + '> ';
					},
					"fnCreatedCell": function( nTd, sData, oData, iRow, iCol ) {
						if( iCol > 5 ) {
							var fieldInput = $(nTd).find( "input" );
							var rowId = oData[0];
							var colId = $( "#samplestable_wrapper th:eq(" + iCol + ")" ).data( "id" );
							fieldInput.attr( "name", "assay." + rowId + "." + colId );
							
							StudyEdit.datatables.editable.fields.addEventsToInput( fieldInput, "samplestable", rowId );
						}
					},
					bSortable: false,
					"aTargets": [ "assay" ]
				}
		    ]
		});	
	
		StudyEdit.datatables.initialize( ".samplesTable" );
	},
	save: function() {},

}

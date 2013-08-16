
// Enable sorting on HTML data
 /* Create an array with the values of all the input boxes in a column */
 $.fn.dataTableExt.afnSortData['dom-text'] = function  ( oSettings, iColumn )
 {
 	return $.map( oSettings.oApi._fnGetTrNodes(oSettings), function (tr, i) {
 		return $('td:eq('+iColumn+') input', tr).val();
 	} );
 };

 /* Create an array with the values of all the select options in a column */
 $.fn.dataTableExt.afnSortData['dom-select'] = function  ( oSettings, iColumn )
 {
 	return $.map( oSettings.oApi._fnGetTrNodes(oSettings), function (tr, i) {
 		return $('td:eq('+iColumn+') select option:selected', tr).text();
 	} );
 };

 /* Create an array with the values of all the checkboxes in a column */
 $.fn.dataTableExt.afnSortData['dom-checkbox'] = function  ( oSettings, iColumn )
 {
 	return $.map( oSettings.oApi._fnGetTrNodes(oSettings), function (tr, i) {
 		return $('td:eq('+iColumn+') input', tr).prop('checked') ? '1' : '0';
 	} );
 };
 
 $(function() {
	 $( ".templatedDatatable" ).each( function( idx, el ) {
		 var datatable = templatedDatatable( $( el), {
			 "aoColumnDefs": [
					 { "sSortDataType": "dom-text", "aTargets": [ "text" ] },
					 { "sSortDataType": "dom-select", "bSearchable": false, "aTargets": [ "select" ] }
			],
			"sScrollX": "100%",
			"bScrollCollapse": true    		 
		});
	 })
 });
 
 
 function templatedDatatable( element, options ) {
	var o = $.extend( options, {
        "fnDrawCallback": function ( oSettings ) {
            if ( oSettings.aiDisplay.length == 0 )
            {
                return;
            }
             
            var nTrs = $('tbody tr', element);
            var iColspan = nTrs[0].getElementsByTagName('td').length;
            var sLastGroup = "";
            for ( var i=0 ; i<nTrs.length ; i++ )
            {
                var iDisplayIndex = oSettings._iDisplayStart + i;
                var sGroup = oSettings.aoData[ oSettings.aiDisplay[iDisplayIndex] ]._aData[0];
                if ( sGroup != sLastGroup )
                {
                	// Create a TR with the template name
                	var tr = $( '<tr>' );
                	tr.addClass( 'group' );
                	tr.addClass( 'templatename' );
                	tr.append( 
                		$( '<td>' ).addClass( "group" ).attr( "colspan", iColspan ).text( sGroup ) 
                	);
                    nTrs[i].parentNode.insertBefore( tr.get(0), nTrs[i] );

                	// Create a TR with the template fields
                	var tr = $( '<tr>' );
                	tr.addClass( 'group' );
                	tr.addClass( 'templatefields' );
                	
                	var tds = $( 'td', nTrs[i] );
                	tds.each(function( idx, td ) {
                    	tr.append( 
                         	$( '<td>' ).addClass( "group field" ).text( $(td).data( "name") ) 
                        );
                	})
                	
                    nTrs[i].parentNode.insertBefore( tr.get(0), nTrs[i] );
                    sLastGroup = sGroup;
                }
            }
        },
        "aoColumnDefs": [
            { "bVisible": false, "aTargets": [ 0 ] }
        ],
        "aaSortingFixed": [[ 0, 'asc' ]],
        "aaSorting": [[ 1, 'asc' ]],
        "sDom": '<"templatedDatatable"lfrtip>',
        
		bJQueryUI: true, 
		iCookieDuration: 86400,				// Save cookie one day
		sPaginationType: 'full_numbers',
	});
	
    oTable = element.dataTable(o);
}
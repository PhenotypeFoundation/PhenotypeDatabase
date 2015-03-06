<%
/**
 * second wizard page / tab
 *
 * @author Jeroen Wesbeek
 * @since  20101206
 *
 * Revision information:
 * $Rev: 1430 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-21 21:05:36 +0100 (Fri, 21 Jan 2011) $
 */
%>
<af:page>
	<h1>Assign properties to columns</h1>

	Below you see a preview of your imported file, please correct the automatically detected types.<br/>
    <p><GdtImporter:showTableEditorCheckBox size="${dataMatrix.size()}" checkedIfRowsLessThan="50" warningIfRowsMoreThan="300"/></p>
	<GdtImporter:properties header="${header}" dataMatrix="${dataMatrix}"/>
	<script language="text/javascript">
		$('html, body').animate({scrollTop:0}, 'fast');
		if (pageOneTimer) clearTimeout(pageOneTimer);
		$(document).ready(function() {
			if (dataTable) dataTable.fnDestroy();
			dataTable = $('#datamatrix').dataTable(
				{   "sScrollX": "100%",
					"bScrollCollapse": true,
					"iDisplayLength": 5,
					"bFilter": false,
					"aLengthMenu": [
						[5, 10, 25, 50],
						[5, 10, 25, "All"]
					],
					"bSort": false
				}
			);
		});
	</script>
</af:page>

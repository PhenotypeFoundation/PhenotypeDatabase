<%
/**
 * second wizard page / tab
 *
 * @author Jeroen Wesbeek
 * @since  20101206
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
<af:page>
<h1>Assign properties to columns</h1>
  <p>Below you see a preview of your imported file, please correct the automatically detected types.</p>  
  <importer:properties entities="${importer_entities}" header="${importer_header}" datamatrix="${session.importer_datamatrix}" templates="${importer_templates}" allfieldtypes="true"/>
</af:page>

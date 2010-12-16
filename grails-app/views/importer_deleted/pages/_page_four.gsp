<%
/**
 * fourth wizard page / tab
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
  <h1>Import wizard imported data postview</h1>
    <p>A total of ${importer_importeddata.size()} rows were imported, below an overview of the rows is shown.</span>
    <importer:postview datamatrix="${importer_importeddata}"/>
</af:page>

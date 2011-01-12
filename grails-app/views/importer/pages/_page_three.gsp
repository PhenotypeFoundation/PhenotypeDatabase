<%
/**
 * third wizard page / tab
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
 <h1>Fill in missing mappings</h1>
    You must fill in the missing mappings.
    <importer:missingProperties datamatrix="${importer_importeddata}" failedcells="${importer_failedcells}"/>
</af:page>

<g:render template="common/error"/>

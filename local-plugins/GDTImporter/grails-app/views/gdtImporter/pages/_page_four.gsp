<%
/**
 * third wizard page / tab
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
 <h1>Confirmation</h1>
  The import preparation was successful. You are about to import ${importedEntitiesList.size()}
  <g:if test="${importedEntitiesList.size() == 1}">entity.</g:if>
  <g:else >entities.</g:else>

  <g:if test="${numberOfUpdatedEntities}" >
    Of those entities, ${numberOfUpdatedEntities} already
    <g:if test="${numberOfUpdatedEntities == 1}" >
      exists
    </g:if>
    <g:else>
      exist
    </g:else>
   in the database according to their preferred identifier and will be updated.
  </g:if>
  If this is correct, please click 'next' to continue.
</af:page>

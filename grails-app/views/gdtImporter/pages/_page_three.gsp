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

  <g:if test="${failedFields && showTableEditor}">
    <h1>Please correct the failed property assignments and make any further adjustments if required</h1>
  </g:if>
  <g:elseif test="${failedFields && !showTableEditor}">
    <h1>You are seeing a preview of the data, you will not be able to in-browser edit the data</h1>
  </g:elseif>
  <g:else>
    <h1>The following data will be imported</h1>
  </g:else>

  <g:if test="${showTableEditor == 'on'}">
  <GdtImporter:validation entityList="${importedEntitiesList}" failedFields="${failedFields}"/>
  </g:if>
  <g:else>
    <GdtImporter:previewImportedAndFailedEntities entityList="${importedEntitiesList}" failedFields="${failedFields}"/>
  </g:else>
</af:page>

<script type="text/javascript">
    $(document).ready(function() {
        var previousElement = $(".navigation input[name='previous']");
        previousElement.data('onclick', previousElement.attr('onclick'));
        previousElement[0].onclick = function() {
            if(!confirm('Warning: When returning to the mapping page you will lose any edits you made on this page. Are you sure you want to continue?')) {
                return false;
            }
            eval($(this).data('onclick'));
        };
    });
</script>

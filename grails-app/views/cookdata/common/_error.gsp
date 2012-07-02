<%
/**
 * error page
 *
 * @author Jeroen Wesbeek
 * @since  20120620
 *
 * Revision information:
 * $Rev:  66849 $
 * $Author:  duh $
 * $Date:  2010-12-08 15:12:54 +0100 (Wed, 08 Dec 2010) $
 */
%>
<g:if test="${wizardErrors}">
  <div id="wizardError" class="error" title="Errors">
    <g:each in="${wizardErrors}" var="error" status="e">
      <p>
        ${error.getMessage()}
      </p>
    </g:each>
  </div>
  <script type="text/javascript">

    // show error dialog
    var we = $("div#wizardError");
    we.dialog({
      modal: true,
      width: 600,
      maxHeight: 400,
      open: function(event, ui) {
        $(this).css({'max-height': 400, 'overflow-y': 'auto'});
      },
      buttons: {
        Ok: function() {
          $(this).dialog('close');
          we.remove();
        }
      }
    });
  </script>
</g:if>
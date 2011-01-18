<%
/**
 * last wizard page / tab
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
<script type="text/javascript">
                // disable redirect warning
                var warnOnRedirect = false;
</script>

<af:page>
<h1>Final Page</h1>
<p>
This concludes the example wizard. You can click <g:link action="index">here</g:link> to restart the wizard.
</p>

All rows were imported succesfully.

    <g:if test="${importer_referer}">
      <p>Click <a href="${importer_referer}">here</a> to return to the page you came from.</p>
    </g:if>
</af:page>

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
<af:page>
<h1>Final Page</h1>
<p>
This concludes the example wizard. You can click <g:link action="index">here</g:link> to restart the wizard.
</p>

${importer_validatedsuccesfully} of ${importer_totalrows} rows were imported succesfully.

    <g:if test="${importer_failedtopersist}">
      <p>The following entities could not be persisted:</p>
      <table>
      <g:each var="entity" in="${importer_failedtopersist}">
        <tr>
        <g:each var="field" in="${entity.giveFields()}">
          <td>
            <g:if test="${entity.getFieldValue(field.name)!=null}">
              <b>${field.name}</b> ${entity.getFieldValue(field.name)}
            </g:if>
           <g:else><b>${field.name}</b> &#215;
            </g:else>
          </td>
        </g:each>
        <td>
           <g:each var="error" in="${entity.errors.allErrors}">
             <b>error</b>: field `${error.getField()}` rejected value: ${error.getRejectedValue()}</b>
        </g:each>
        </td>
      </tr>
      </g:each>
      </table>
    </g:if>


    <g:if test="${importer_referer}">
      <p>Click <a href="${importer_referer}">here</a> to return to the page you came from.</p>
    </g:if>
</af:page>

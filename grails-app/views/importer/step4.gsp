<%
	/**
	 * Thirds step in the importer, showing the imported data
	 *
	 * @author Tjeerd Abma
	 * @since 20100318
	 * @package importer
	 *
	 * Revision information:
	 * $Rev$
	 * $Author$
	 * $Date$
	 */
%>

<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
    <meta name="layout" content="main"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'importer.css')}"/>
    <title>Step 5: import wizard finished</title>
  </head>
  <body>
    <h1>Step 5: import wizard finished</h1>
    <p>${validatedSuccesfully} of ${totalrows} rows were imported succesfully.</p>



    <g:if test="${failedtopersist}">
      <p>The following entities could not be persisted:</p>
      <table>     
      <g:each var="entity" in="${failedtopersist}">       
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


    <g:if test="${referer}">
      <p>Click <a href="${referer}">here</a> to return to the page you came from.</p>
    </g:if>
  </body>
</html>

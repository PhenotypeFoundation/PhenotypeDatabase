<!--
  To change this template, choose Tools | Templates
  and open the template in the editor.
-->

<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
    <meta name="layout" content="main"/>
    <title>Import wizard preview</title>
  </head>
  <body>
    <h1>Import wizard preview</h1>
    <p>Below you see a preview of your imported file, please correct the automatically detected types.</p>
    <table>
      <tr>
        <g:each var="column" in="${header}">
          <td>${column} 
            <select name="celltype">
              <option>String</option>
              <option>Numeric</option>
              <option>Date</option>
              <option>Unknown</option>
            </td>
        </g:each>
      </tr>
  </table>
  </body>
</html>

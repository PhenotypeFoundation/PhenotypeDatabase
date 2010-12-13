<%--
  Created by IntelliJ IDEA.
  User: siemensikkema
  Date: Nov 30, 2010
  Time: 4:07:28 PM
--%>

<%@ page import="dbnp.studycapturing.Study" contentType="text/html;charset=UTF-8" %>
<html>
  <head>
    <meta name="layout" content="main" />
    <title>Select an assay</title>
    <script type="text/javascript">
      function updateAssay(jsonData, selectID) {
        var a = eval(jsonData);
        var sel = $('#'+selectID).empty()
        
        $(a).each(function(i, el){
          sel.append($("<option></option>").attr("value",el.id).text(el.name));
        });
      }
    </script>
  </head>
  <body>
  <div style="color:red;">
    flash.errorMessage
  </div>
  <g:form name="assaySelect" action="">
    <g:select optionKey="id" optionValue="title" name="study" from="${userStudies}"
            onChange="${remoteFunction(controller:'study',action:'ajaxGetAssays',params:'\'id=\'+escape(this.value)',onComplete: 'updateAssay(XMLHttpRequest.responseText, \'assay\')')}"/>
    <g:select name="assay" id="assay" from="${assays}"/>
    <g:submitButton name="submit" value="Submit"/>
  </g:form>
  </body>
</html>
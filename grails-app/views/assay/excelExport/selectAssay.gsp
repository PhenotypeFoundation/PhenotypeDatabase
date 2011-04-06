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
        var sel = $('#'+selectID).empty();

        $(a).each(function(i, el){
          sel.append($("<option></option>").attr("value",el.id).text(el.name))
        })
      }

      $(document).ready(function(){
        // trigger change event to load assay based on currently selected study.
        // After pressing 'Back', the browser may use last selected study.
        $('#study').change()

      })
    </script>
  </head>
  <body>
  <div style="color:red;">
    ${flash.errorMessage}
  </div>

  <h1>Select the assay you want to export data from</h1>

  <g:form name="assaySelect" action="excelExport">
    <g:select optionKey="id" optionValue="title" name="studyId" from="${userStudies}" id="study"
      onChange="${remoteFunction(controller:'study',action:'ajaxGetAssays',params:'\'id=\'+escape(this.value)',onComplete: 'updateAssay(XMLHttpRequest.responseText, \'assay\')')}"/>
    <g:select optionKey="id" name="assayId" id="assay" from=""/>
    <g:submitButton name="submit" value="Submit"/>
  </g:form>
  </body>
</html>
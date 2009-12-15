
<html>
    <head>
      <title>Generic Study Capture Framework - Load data</title>
      <meta name="layout" content="main" />
      <my:jqueryui/>
      <script type="text/javascript">
	$(function() {
		$("#tabs").tabs();
	});
      </script>
    </head>
    <body>

    <div id="tabs">
      <ul>
        <li><a href="#tab-simple">Loading MageTab studies</a></li>
      </ul>
      <div id="tab-simple">
        <g:form action="load" controller="load" method="post" enctype="multipart/form-data">
          <input type="hidden" name="targetUri" value="${targetUri}" />
          <label class="grey" for="uploadfile">Select the IDF file to load into dbNP :</label>
          <!--input class="field" type="text" name="querytext" id="querytext" size="40" /-->
          <input type="file" name="uploadfile" />
          <input type ="submit" name="submit" />
        </g:form>
      </div>
      <div id="tab-advanced">
      </div>
    </div>
  </body>
</html>
<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
	<meta name="layout" content="main"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Import templates</title>
    <r:require modules="gscfimporter" />
  </head>
  <body>
    <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_TEMPLATEADMIN">
	    <h1>Select XML file to import </h1>
	    
		<g:render template="/common/flashmessages" />
		
	    <g:form controller="template" method="post" action="handleImportedFile" enctype="multipart/form-data">
          <fieldset>
            <div class="element">
              <div class="description">Choose a file: </div>
              <div class="input">
                <div id="upload-file-container" class="upload_file_container">
                  <input type="file" name="file" id="upload-file-input" />
                </div>
                <span id="upload-file-label">no file selected</span>
              </div>
            </div>
          </fieldset>
          <p>
            <input type="submit" value="Import" />
          </p>
	    </g:form>
    </sec:ifAnyGranted>
    <sec:ifNotGranted roles="ROLE_ADMIN, ROLE_TEMPLATEADMIN">
        <h1>Template import </h1>
        Only (template)Admins are able to import templates, please request a template/templatefield at the specific type.
    </sec:ifNotGranted>

    <r:script>
      $("document").ready(function(){
        $("#upload-file-input").change(function(e) {
          $("#upload-file-label").text(e.currentTarget.files[0].name);
        });
      });
    </r:script>

  </body>
</html>

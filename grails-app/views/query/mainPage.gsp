<html>
    <head>
      <title>Generic Study Capture Framework - Query studies</title>
      <meta name="layout" content="main" />
      <script type="text/javascript">
	$(function() {
		$("#tabs").tabs();
	});
      </script>
    </head>
    <body>

aaaaaaa



<div id="wizard" class="wizard">
	<h1>Create a new study</h1>
	<g:form action="pages" name="wizardForm" id="wizardForm">
	<g:hiddenField name="do" value="" />
		<div id="wizardPage">
			<wizard:ajaxFlowRedirect form="form#wizardForm" name="next" url="[controller:'query',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" />
		</div>
	</g:form>
</div>




  </body>
</html>
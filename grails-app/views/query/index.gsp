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

    <div id="tabs">
      <ul>
        <li><a href="#tab-simple">Simple query</a></li>
      </ul>
      <div id="tab-simple">
        <g:form url="[action:'results',controller:'query',params: 'q']">
          <input type="hidden" name="targetUri" value="${targetUri}" />
          <label class="grey" for="q">Search for:</label>
          <input class="field" type="text" name="q" id="q" size="40" />
          <input type="submit" name="submit" value="Query"/>
        </g:form>
        <p>
        <n:isNotLoggedIn>
        <small>To query private studies, please login above.</small>
        </n:isNotLoggedIn>
        </p>
      </div>

    </div>
  </body>
</html>
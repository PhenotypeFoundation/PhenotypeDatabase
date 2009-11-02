<html>
    <head>
      <title>Generic Study Capture Framework - Query studies</title>
      <meta name="layout" content="main" />
      <g:javascript library="jquery"/>
      <link rel="stylesheet" href="${createLinkTo(dir:'css/jquery-ui', file: 'jquery-ui-1.7.2.custom.css')}"/>
      <script src="${createLinkTo(dir: 'js', file: 'jquery-ui-1.7.2.custom.min.js')}" type="text/javascript"></script>
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
        <li><a href="#tab-advanced">Advanced query</a></li>
      </ul>
      <div id="tab-simple">
        <g:form url="[action:'list',controller:'study']">
          <input type="hidden" name="targetUri" value="${targetUri}" />
          <label class="grey" for="querytext">Search for:</label>
          <input class="field" type="text" name="querytext" id="querytext" size="40" />
          <input type="submit" name="submit" value="Query"/>
        </g:form>
        <p>
        <n:isNotLoggedIn>
        <small>To query private studies, please login above.</small>
        </n:isNotLoggedIn>
        </p>
      </div>
      <div id="tab-advanced">
        TODO: add advanced query here.
        <n:isNotLoggedIn>
          <p><small>To query private studies, please login above.</small></p>
        </n:isNotLoggedIn>
      </div>
    </div>
  </body>
</html>
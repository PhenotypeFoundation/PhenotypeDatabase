<html>
    <head>
      <title>Generic Study Capture Framework</title>
      <meta name="layout" content="main" />
      <g:javascript library="jquery"/>
      <link rel="stylesheet" href="${createLinkTo(dir:'css/jquery-ui', file: 'jquery-ui-1.7.2.custom.css')}">
      <script src="${createLinkTo(dir: 'js', file: 'jquery-ui-1.7.2.custom.min.js')}" type="text/javascript"></script>
      <script type="text/javascript">
	$(function() {
		$("#accordion").accordion();
		$("#tabs").tabs();
	});
      </script>
    </head>
    <body>

    <div id="tabs">
      <ul>
        <li><a href="#tab-query">Query database</a></li>
        <li><a href="#tab-add">My studies</a></li>
      </ul>
      <div id="tab-query">
        <g:form url="[action:'list',controller:'study']">
          <input type="hidden" name="targetUri" value="${targetUri}" />
          <label class="grey" for="querytext">Search for:</label>
          <input class="field" type="text" name="querytext" id="querytext" size="40" />
          <input type="submit" name="submit" value="Query"/>
        </g:form>
        <p><a href="<g:createLink url="[action:'create',controller:'query']"/>">Create advanced query</a></p>
        <p>
        <jsec:isNotLoggedIn>
        <small>To query private studies, please login above.</small>
        </jsec:isNotLoggedIn>
        </p>
      </div>
      <div id="tab-add">
        <jsec:isNotLoggedIn>
        To add or change study data, please login above.
        </jsec:isNotLoggedIn>
      </div>
    </div>
  </body>
</html>
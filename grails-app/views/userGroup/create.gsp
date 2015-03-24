<head>
	<meta name='layout' content='main'/>
	<g:set var="entityName" value="${message(code: 'userGroup.label', default: 'UserGroup')}"/>
	<title><g:message code="default.create.label" args="[entityName]"/></title>

    <script type="text/javascript">
      // This method is called on the event body.onLoad
      $(function() {
              $("#tabs").tabs();
      });
    </script>
	<style type="text/css">
	  div.usermanagement { font-size: 0.8em; }
	</style>
</head>

<body>
    <script src="${resource(dir: 'js', file: 'jquery-callback-1.2.js')}" type="text/javascript"></script>
<h3><g:message code="default.create.label" args="[entityName]"/></h3>

<g:form action="save" name='userCreateForm' class="button-style">

      <div id="tabs" class="usermanagement">
        <ul>
          <li><a href="#groupinfo">Group Info</a></li>
        </ul>

        <div id="groupinfo">

		  <table>
		  <tbody>
			<tr><td>Group Name</td><td><g:textField name="groupName" value="${userGroup?.groupName}"/></td></tr>
			<tr><td>Group Description</td><td><g:textArea name="groupDescription" value="${userGroup?.groupDescription}"/></td></tr>
		  </tbody>
		  </table>
	  </div>
          
	  </div>

<div style='float:left; margin-top: 10px;'>
  <input type="submit" value="Save" />

</div>

</g:form>

<script>
$(document).ready(function() {
	$('#groupName').focus();
});
</script>

</body>
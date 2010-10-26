<head>
	<meta name='layout' content='main'/>
	<g:set var="entityName" value="${message(code: 'user.label', default: 'User')}"/>
	<title><g:message code="default.edit.label" args="[entityName]"/></title>

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
<h3><g:message code="default.edit.label" args="[entityName]"/></h3>

<g:form action="update" name='userEditForm' class="button-style">
<g:hiddenField name="id" value="${user?.id}"/>
<g:hiddenField name="version" value="${user?.version}"/>

      <div id="tabs" class="usermanagement">
        <ul>
          <li><a href="#userinfo">User info</a></li>
          <li><a href="#roles">Roles</a></li>
        </ul>

        <div id="userinfo">

		  <table>
		  <tbody>
			<tr><td>Username</td><td><g:textField name="username" value="${user?.username}"/></td></tr>
			<tr><td>Password</td><td><g:passwordField name="password" value="${user?.password}"/></td></tr>
			<tr><td>Email address</td><td><g:textField name="email" value="${user?.email}"/></td></tr>
			<tr><td>User confirmed</td><td><g:checkBox name="userConfirmed" value="${user?.userConfirmed}"/></td></tr>
			<tr><td>Admin confirmed</td><td><g:checkBox name="adminConfirmed" value="${user?.adminConfirmed}"/></td></tr>
			<tr><td>Account expired</td><td><g:checkBox name="accountExpired" value="${user?.accountExpired}"/></td></tr>
			<tr><td>Account locked</td><td><g:checkBox name="accountLocked" value="${user?.accountLocked}"/></td></tr>
			<tr><td>Password expired</td><td><g:checkBox name="passwordExpired" value="${user?.passwordExpired}"/></td></tr>

		  </tbody>
		  </table>
	  </div>
	  <div id="roles">
		<g:each var="entry" in="${roleMap}">
		<div>
			<g:checkBox name="${entry.key.authority}" value="${entry.value}"/>
			<g:link controller='role' action='edit' id='${entry.key.id}'>${entry.key.authority.encodeAsHTML()}</g:link>
		</div>
		</g:each>
	  </div>

	  </div>

<div style='float:left; margin-top: 10px;'>
  <input type="submit" value="Save" />

  <g:if test='${user}'>
    <input type="button" value="Delete" onClick="$('#userDeleteForm').submit(); return false;"/>
  </g:if>

</div>

</g:form>

<g:if test='${user}'>
  <g:form action="delete" name='userDeleteForm'>
	<g:hiddenField name="id" value="${user?.id}"/>
  </g:form>
</g:if>

<script>
$(document).ready(function() {
	$('#username').focus();
});
</script>

</body>

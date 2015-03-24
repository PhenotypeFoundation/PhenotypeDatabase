<head>
	<meta name='layout' content='main'/>
	<g:set var="entityName" value="${message(code: 'user.label', default: 'User')}"/>
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
          <li><a href="#userinfo">User info</a></li>
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

	  </div>

<div style='float:left; margin-top: 10px;'>
  <input type="submit" value="Save" />

</div>

</g:form>

<script>
$(document).ready(function() {
	$('#username').focus();
});
</script>

</body>
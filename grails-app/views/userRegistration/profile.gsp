<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>User profile</title>
</head>

<body>
<div class="body" id="register">
	<g:if test="${user.shibbolethUser}">
		<table>
			<tr>
				<td>Username</td>
				<td>${user.uid}</td>
			</tr>
			<tr>
				<td>Email</td>
				<td>${user.email}</td>
			</tr>
			<tr>
				<td>Organization</td>
				<td>${user.organization}</td>
			</tr>
			<sec:ifAllGranted roles="ROLE_ADMIN">
			<tr>
				<td>Administrator</td>
				<td><b>You are an administrator</b></td>
			</tr>
			</sec:ifAllGranted>
		</table>
	</g:if>
	<g:else>
		<div class="inner">
		<g:if test="${flash.message}"><div class='login_message'>${flash.message}</div></g:if>

		<div class='fheader'>You can change your user details here. If you don't want to change your password, keep it empty.</div>

		<g:hasErrors bean="${command}">
			<g:renderErrors bean="${command}" as="list"/>
		</g:hasErrors>

		<form action='<g:createLink controller="userRegistration" action="updateProfile"/>' method='POST' id='loginForm'
			  class='cssform' autocomplete='off'>
			<div class="dialog">
				<table>
					<tbody>

					<tr class="prop">
						<td valign="top" class="name">
							<label for="username">Username</label>
						</td>
						<td valign="top" class="value ${hasErrors(bean: command, field: 'username', 'errors')}">
							<g:textField disabled="disabled" name="title" value="${user?.username}"/>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name">
							<label for="password">Password</label>
						</td>
						<td valign="top" class="value ${hasErrors(bean: command, field: 'password', 'errors')}">
							<g:passwordField name="password" value=""/>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name">
							<label for="password2">Repeat password</label>
						</td>
						<td valign="top" class="value ${hasErrors(bean: command, field: 'password2', 'errors')}">
							<g:passwordField name="password2" value=""/>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name">
							<label for="email">Email address</label>
						</td>
						<td valign="top" class="value ${hasErrors(bean: command, field: 'email', 'errors')}">
							<g:textField name="email" value="${user?.email}"/>
						</td>
					</tr>
					</tbody>
				</table>
			</div>

			<div class="buttons">
				<g:each in="${extraparams}" var="param">
					<input type="hidden" name="${param.key}" value="${param.value}">
				</g:each>
				<span class="button"><g:submitButton name="edit" class="save" value="Change profile"/></span>
				<span class="button"><g:link class="cancel" controller="home">Cancel</g:link></span>
			</div>
		</form>
	</g:else>

</div>
</body>
</html>

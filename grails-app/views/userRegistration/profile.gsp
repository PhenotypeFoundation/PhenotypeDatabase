<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>User profile</title>
	<r:require module="tiptip" />
</head>

<body>
	<g:if test="${user.shibbolethUser}">
		<table>
            <g:if test="user?.email">
                <tr>
                    <td></td>
                    <td>
                        <a href="http://gravatar.com/emails/" target="_new"><img src="${icon.userIcon(user:user, size: 200)}" class="tooltip" title="Change your avatar at gravatar.com"></a>
                    </td>
                </tr>
            </g:if>
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
            <tr>
                <td>API key</td>
                <td valign="top" class="value">
                    <g:textField disabled="disabled" name="secret" value="${user.apiKey}" style="width:250px" />
                    <img src="${fam.icon(name: 'help')}" class="tooltip" title="in order to programmatically interface with gscf, you need the api key to communicate with the api. Refer to the api documentation at ${createLink(controller:'api')} for more information about how to use the api and the api key." />
                </td>
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

                    <g:if test="user?.email">
                    <tr class="prop">
                        <td></td>
                        <td>
                            <a href="http://gravatar.com/emails/" target="_new"><img src="${icon.userIcon(user:user, size: 200)}" class="tooltip" title="Change your avatar at gravatar.com"></a>
                        </td>
                    </tr>
                    </g:if>
                    <tr class="prop">
                        <td valign="top" class="name">API Key</td>
                        <td valign="top" class="value">
                            <g:textField disabled="disabled" name="secret" value="${user.apiKey}" style="width:250px" />
                            <img src="${fam.icon(name: 'help')}" class="tooltip" title="in order to programmatically interface with gscf, you need the api key to communicate with the api. Refer to the api documentation at ${createLink(controller:'api')} for more information about how to use the api and the api key." />
                        </td>
                    </tr>
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
							<label for="email">Email address</label>
						</td>
						<td valign="top" class="value ${hasErrors(bean: command, field: 'email', 'errors')}">
							<g:textField disabled="disabled" name="email" value="${user?.email}"/>
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
					</tbody>
				</table>
			</div>

			<div class="buttons">
				<g:each in="${extraparams}" var="param">
					<input type="hidden" name="${param.key}" value="${param.value}">
				</g:each>

				<span class="button"><g:submitButton name="edit" class="button-4 margin10 pie" value="Change profile"/></span>
                <g:link class="cancel" controller="home"><input type="button" class="button-4 margin10 pie" value="Cancel"/></g:link>
			</div>
		</form>
	</g:else>
</body>

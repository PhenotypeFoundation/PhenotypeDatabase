<head>
	<meta name='layout' content='main'/>
	<g:set var="entityName" value="${message(code: 'user.label', default: 'User')}"/>
	<title><g:message code="default.edit.label" args="[entityName]"/></title>
	<r:require module="tiptip" />
    <r:script type="text/javascript">
        $(document).ready(function() {
            $("#tabs").tabs();
        });
    </r:script>
	<style type="text/css">
	div.usermanagement {
		font-size: 0.8em;
	}
	</style>
</head>

<body>

<g:if test="${flash.userError}">
	<div id="error" class="error" title="errors">
		<p>
			${flash.userError}
		</p>
	</div>
</g:if>
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
                <g:if test="user?.email">
                <tr>
                    <td></td>
                    <td><img src="${icon.userIcon(user:user, size: 200)}"></td>
                </tr>
                </g:if>
                <g:if test="${user.shibbolethUser}">
					<tr><td>Shibboleth user</td><td>yes</td></tr>
                    <tr>
                        <td>API key</td>
                        <td valign="top" class="value">
                            <g:textField disabled="disabled" name="secret" value="${user.apiKey}" style="width:250px" />
                            <img src="${fam.icon(name: 'help')}" class="tooltip" title="in order to programmatically interface with gscf, a user needs his api key to communicate with the api. Refer to the api documentation at ${createLink(controller:'api')} for more information about how to use the api and the api key." />
                        </td>
                    </tr>
                    <tr><td>Username/Urn</td><td>${user?.username}</td></tr>
					<tr><td>Uid</td><td>${user?.uid}</td></tr>
					<tr><td>Email address</td><td><g:textField name="email" value="${user?.email}"/></td></tr>
					<tr><td>Organization</td><td>${user?.organization}</td></tr>
					<tr><td>VO Name</td><td>${user?.voName}</td></tr>
					<tr><td>User Status</td><td>${user?.userStatus}</td></tr>
				</g:if>
				<g:else>
					<tr><td>Shibboleth user</td><td>no</td></tr>
                    <tr>
                        <td>API key</td>
                        <td valign="top" class="value">
                            <g:textField disabled="disabled" name="secret" value="${user.apiKey}" style="width:250px" />
                            <img src="${fam.icon(name: 'help')}" class="tooltip" title="in order to programmatically interface with gscf, a user needs his api key to communicate with the api. Refer to the api documentation at ${createLink(controller:'api')} for more information about how to use the api and the api key." />
                        </td>
                    </tr>
                    <tr><td>Username</td><td><g:textField name="username" value="${user?.username}"/></td></tr>
					<tr><td>Password</td><td><g:passwordField name="password" value="${user?.password}"/></td></tr>
					<tr><td>Email address</td><td><g:textField name="email" value="${user?.email}"/></td></tr>
					<tr><td>User confirmed</td><td><g:checkBox name="userConfirmed"
															   value="${user?.userConfirmed}"/></td></tr>
					<tr><td>Admin confirmed</td><td><g:checkBox name="adminConfirmed"
																value="${user?.adminConfirmed}"/></td></tr>
					<tr><td>Account expired</td><td><g:checkBox name="accountExpired"
																value="${user?.accountExpired}"/></td></tr>
					<tr><td>Password expired</td><td><g:checkBox name="passwordExpired"
																 value="${user?.passwordExpired}"/></td></tr>
				</g:else>
				<g:if test="${user != session.gscfUser}">
					<tr><td>Account locked</td><td><g:checkBox name="accountLocked"
															   value="${user?.accountLocked}"/></td></tr>
				</g:if>
				</tbody>
			</table>
		</div>

		<div id="roles">
			<g:each var="entry" in="${roleMap}">
				<div>
					<g:checkBox name="${entry.key.authority}" value="${entry.value}"/>
					${entry.key.authority.encodeAsHTML()}
				</div>
			</g:each>
		</div>

	</div>

	<div style='float:left; margin-top: 10px;'>
		<input type="submit" value="Save"/>

		<g:if test='${user}'>
			<input type="button" value="Delete" onClick="$('#userDeleteForm').submit();
			return false;"/>
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

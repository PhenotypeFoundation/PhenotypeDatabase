<head>
<title><g:message code='spring.security.ui.resetPassword.title'/></title>
<meta name='layout' content='main'/>
</head>

<body>

<p/>

	<g:form action='resetPassword' name='resetPasswordForm' autocomplete='off'>
  	<g:hiddenField name='t' value='${token}'/>
	<div class="sign-in">

	<br/>
	<h4><g:message code='spring.security.ui.resetPassword.description'/></h4>

	<table>
		<s2ui:passwordFieldRow name='password' labelCode='resetPasswordCommand.password.label' bean="${command}"
                             labelCodeDefault='Password' value="${command?.password}"/>

		<s2ui:passwordFieldRow name='password2' labelCode='resetPasswordCommand.password2.label' bean="${command}"
                             labelCodeDefault='Password (again)' value="${command?.password2}"/>
	</table>

	<input type="submit" value="Reset my password" />


	</div>
	</g:form>


</body>

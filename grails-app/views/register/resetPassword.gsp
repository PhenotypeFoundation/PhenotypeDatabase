<head>
<title><g:message code='spring.security.ui.resetPassword.title'/></title>
<meta name='layout' content='main'/>
</head>

<body>

<p/>
	<g:form action='resetPassword' name='resetPasswordForm' autocomplete='off'>
    	<g:hiddenField name='t' value='${token}'/>
	  <div class="sign-in">

		<g:hasErrors bean="${command}">
		  <p>Both passwords should match, contain more than 8 and less than 64 characters.</p>
		  <p>Also, passwords should contain at least one letter, one number and one symbol.</p>
		</g:hasErrors>

		<br/>
		<h4><g:message code='spring.security.ui.resetPassword.description'/></h4>

		<table>
		  <tr><td>Password</td><td><g:passwordField name="password" value="${command?.password}" /></td></tr>
		  <tr><td>Password (again)</td><td>
			<g:passwordField name="password2" value="${command?.password2}" />
		  </td></tr>
		</table>

		<input type="submit" value="Reset my password" />

	  </div>
	</g:form>


</body>

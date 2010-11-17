<head>
  <title><g:message code='spring.security.ui.forgotPassword.title'/></title>
  <meta name='layout' content='main'/>
</head>

<body>

<p/>

	<g:form action='forgotPassword' name="forgotPasswordForm" autocomplete='off'>



	<g:if test='${emailSent}'>
          <br/>
          <g:message code='spring.security.ui.forgotPassword.sent'/>
	</g:if>
	<g:else>

          <br/>
          <h4><g:message code='spring.security.ui.forgotPassword.description'/></h4>

		  <g:if test='${flash.userError}'>
			${flash.userError}
			<br/>
		  </g:if>
		<table>
			<tr>
				<td><label for="username"><g:message code='spring.security.ui.forgotPassword.username'/></label></td>
				<td><g:textField name="username" size="25" /></td>
			</tr>
		</table>

          <input type="submit" value="Reset my password">

	</g:else>

	</g:form>


</body>

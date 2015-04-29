<head>
	<meta name='layout' content='main'/>
	<title><g:message code='spring.security.ui.register.title'/></title>
		<r:require module="jquery" />
        <r:external uri='jquery/jquery.jgrowl.js'/>
        <r:external uri='jquery/jquery.checkbox.js'/>
        <r:external uri='spring-security-ui.js'/>

<r:external uri="${resource(dir:'css',file:'reset.css')}"/>
<r:external uri="${resource(dir:'css',file:'spring-security-ui.css')}"/>
<jqui:resources />
<r:external uri="${resource(dir:'css/smoothness',file:'jquery-ui-1.8.23.custom.css',plugin:'spring-security-ui')}"/>
<r:external uri="${resource(dir:'css',file:'jquery.jgrowl.css')}"/>
<r:external uri="${resource(dir:'css',file:'jquery.safari-checkbox.css')}"/>
<r:external uri="${resource(dir:'css',file:'auth.css')}"/>


</head>

<body>

<p/>

<s2ui:form width='650' height='300' elementId='loginFormContainer'
           titleCode='spring.security.ui.register.description' center='true'>

<g:form action='register' name='registerForm'>

	<g:if test='${emailSent}'>
	<br/>
	<g:message code='spring.security.ui.register.sent'/>
	</g:if>
	<g:else>

	<br/>

	<table>
	<tbody>

		<s2ui:textFieldRow name='username' labelCode='user.username.label' bean="${command}"
                         size='40' labelCodeDefault='Username' value="${command.username}"/>

		<s2ui:textFieldRow name='email' bean="${command}" value="${command.email}"
		                   size='40' labelCode='user.email.label' labelCodeDefault='E-mail'/>

		<s2ui:passwordFieldRow name='password' labelCode='user.password.label' bean="${command}"
                             size='40' labelCodeDefault='Password' value="${command.password}"/>

		<s2ui:passwordFieldRow name='password2' labelCode='user.password2.label' bean="${command}"
                             size='40' labelCodeDefault='Password (again)' value="${command.password2}"/>

	</tbody>
	</table>

	<s2ui:submitButton elementId='create' form='registerForm' messageCode='spring.security.ui.register.submit'/>

	</g:else>

</g:form>

</s2ui:form>

<script>
$(document).ready(function() {
	$('#username').focus();
});
</script>

<s2ui:showFlash/>


</body>

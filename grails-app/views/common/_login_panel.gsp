<%@ page import="org.codehaus.groovy.grails.commons.ConfigurationHolder" %>
<!-- LOGIN PANEL -->
<div id="toppanel" class="toppanel">
	<div id="panel">
		<div class="content clearfix">
			<div class="left">
				<h1>Welcome to the ${grailsApplication.config.application.title}</h1>

				<h2>Version <b>${meta(name: 'app.version')}, build #<g:meta name="app.build.svn.revision"/></b></h2>

				<p class="grey">Please use the forms on the right to either log in if you already have an account, or sign up if you think this data support platform suits your needs.</p>
				<g:if test="${flash.message}"><p class="red">${flash.message}</p></g:if>
			</div>

			<div class="left">
				<g:form controller="." action="j_spring_security_check" method='POST' class="clearfix">
					<h1>Member Login</h1>
					<label class="grey" for="username">Username:</label>
					<input class="field" type="text" name="j_username" id="j_username" value="${username}" size="23"/>
					<label class="grey" for="password">Password:</label>
					<input class="field" type="password" name="j_password" id="password" size="23"/>
					<label><input type='checkbox' class='chk' name='_spring_security_remember_me' id='remember_me'
								  <g:if test='${hasCookie}'>checked='checked'</g:if>/> Remember me</label>

					<div class="clear"></div>
					<input type="submit" name="submit" value="Login" class="bt_login"/>

					<g:if test="${redirectUrl}">
						<g:hiddenField name="spring-security-redirect" value="${redirectUrl}"/>
					</g:if>

					<a class="lost-pwd" href="<g:createLink url="[action:'forgotPassword',controller:'register']"
															class="lost-pwd"/>">Lost your password?</a>
				</g:form>
			</div>

			<div class="left right">
				<g:form url="[action:'add',controller:'userRegistration']" class="clearfix registration">
					<input type="hidden" name="targetUri" value="${targetUri}"/>

					<h1>Not a member yet? Sign Up!</h1>
					<g:hasErrors bean="${command}">
						<g:renderErrors bean="${command}" as="list"/>
						<g:if test="${addSendUserLink}">
							<a class="resend_confirmation" href="<g:createLink
								url="[action:'sendUserConfirmation',controller:'userRegistration', params: [username: username]]"/>">Resend confirmation message</a><br/>
						</g:if>
					</g:hasErrors>

					<label class="grey" for="signup">Username:</label>
					<input class="field" type="text" name="username" id="username" value="${username}" size="23"/>
					<label class="grey" for="email">Email:</label>
					<input class="field" type="text" name="email" id="email" value="${email}" size="23"/>
					<label>A password will be e-mailed to you</label>

					<input type="submit" name="submit" value="Register" class="bt_register"/>
				</g:form>
			</div>
		</div>
	</div>

	<div class="tab">
		<ul class="login">
			<li class="left">&nbsp;</li>
			<li>Hello <sec:ifLoggedIn>
				<g:if test="${session.gscfUser.shibbolethUser && session.gscfUser.displayName}">
					${session.gscfUser.displayName}
				</g:if><g:else>
					<sec:username/>
				</g:else>
			</sec:ifLoggedIn>
			<sec:ifNotLoggedIn>Guest</sec:ifNotLoggedIn>!</li>
			<sec:ifLoggedIn>
				<li class="sep">|</li>
                <li><g:link controller="userRegistration" action="profile"><img src="${icon.userIcon(user:session.gscfUser, size: 20, transparent: false)}"></g:link></li>
                <li id="toggle">
					<g:link controller="userRegistration" action="profile">profile</g:link>
				</li>
			</sec:ifLoggedIn>
			<li class="sep">|</li>
			<li id="toggle">
				<sec:ifLoggedIn><g:link controller="logout" action="index">sign out</g:link></sec:ifLoggedIn>
				<sec:ifNotLoggedIn>
					<g:if test="${grailsApplication.config.authentication.shibboleth.toString().toBoolean()}">
						<g:link class="open" controller="login">Log in</g:link>
					</g:if>
					<g:else>
						<a id="open" class="open" href="#">Log In | Register</a>
						<a id="close" style="display: none;" class="close" href="#">Close Panel</a>
					</g:else>
				</sec:ifNotLoggedIn>
			</li>
			<li class="right">&nbsp;</li>
		</ul>
	</div>
</div>
<!-- /LOGIN PANEL -->
package dbnp.authentication

import grails.converters.JSON

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class LoginController {
	/**
	 * Dependency injection for the authenticationTrustResolver.
	 */
	def authenticationTrustResolver

	/**
	 * Dependency injection for the springSecurityService.
	 */
	def springSecurityService

	/**
	 * Dependency injection for the GSCF authentication service
	 */
	def authenticationService

	/**
	 * Default action; redirects to 'defaultTargetUrl' if logged in, /login/auth otherwise.
	 */
	def index = {
		if (springSecurityService.isLoggedIn()) {
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
		}
		else {
			redirect action: auth, params: params
		}
	}

	/**
	 * Show the login page.
	 */
	def auth = {
		def config = SpringSecurityUtils.securityConfig
		if (springSecurityService.isLoggedIn()) {
			if (params.returnURI) {
				// see basefilters
				redirect uri: params.returnURI
			} else {
				redirect uri: config.successHandler.defaultTargetUrl
			}
			return
		}

		String view = 'auth'
		String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
		render view: view, model: [postUrl: postUrl, rememberMeParameter: config.rememberMe.parameter]
	}

	/**
	 * Shows the login page for users from a module
	 */
	def auth_remote = {
		def consumer = params.consumer
		def token = params.token
		
		if (consumer == null || token == null) {
			throw new Exception("Consumer and Token must be given!");
		}

		def returnUrl;
		
		// If no returnUrl is given, find the previous one from the session
		if( params.returnUrl ) {
			returnUrl = params.returnUrl;
			session.authRemoteUrl = returnUrl;
		} else if( session.authRemoteUrl ) {
			returnUrl = session.authRemoteUrl;
		}

		// If the user is already authenticated with this session_id, redirect
		// him
		if (authenticationService.isRemotelyLoggedIn(consumer, token)) {
			if (returnUrl) {
				redirect url: returnUrl
			} else {
				redirect controller: 'home'
			}
		}

		// If the user is already logged in locally, we log him in and
		// immediately redirect him
		if (authenticationService.isLoggedIn()) {
			authenticationService.logInRemotely(consumer, token, authenticationService.getLoggedInUser())

			if (returnUrl) {
				redirect url: returnUrl
			} else {
				redirect controller: 'home'
			}
		}

		// Otherwise we show the login screen
		def config = SpringSecurityUtils.securityConfig
		String view = 'auth'
		String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
		
		String redirectUrl = g.createLink(controller: 'login', action: 'auth_remote', params: [consumer: params.consumer, token: params.token], absolute: true)
		render view: view, model: [postUrl: postUrl,
			rememberMeParameter: config.rememberMe.parameter, redirectUrl: redirectUrl]
	}

	/**
	 * Show denied page.
	 */
	def denied = {
		if (springSecurityService.isLoggedIn() &&
			authenticationTrustResolver.isRememberMe(SCH.context?.authentication)) {
			// have cookie but the page is guarded with IS_AUTHENTICATED_FULLY
			redirect action: full, params: params
		}
	}

	/**
	 * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
	 */
	def full = {
		def config = SpringSecurityUtils.securityConfig
		render view: 'auth', params: params,
			model: [hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication),
				postUrl: "${request.contextPath}${config.apf.filterProcessesUrl}"]
	}

	/**
	 * Callback after a failed login. Redirects to the auth page with a warning message.
	 */
	def authfail = {
		def username = session[UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY]
		String msg = ''
		def exception = session[AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY]
		if (exception) {
			if (exception instanceof AccountExpiredException) {
				msg = SpringSecurityUtils.securityConfig.errors.login.expired
			}
			else if (exception instanceof CredentialsExpiredException) {
				msg = SpringSecurityUtils.securityConfig.errors.login.passwordExpired
			}
			else if (exception instanceof DisabledException) {
				msg = SpringSecurityUtils.securityConfig.errors.login.disabled
			}
			else if (exception instanceof LockedException) {
				msg = SpringSecurityUtils.securityConfig.errors.login.locked
			}
			else {
				msg = SpringSecurityUtils.securityConfig.errors.login.fail
			}
		}

		if (springSecurityService.isAjax(request)) {
			// set output header to json
			response.contentType = 'application/json'

			render([error: msg] as JSON)
		}
		else {
			flash.message = msg
			redirect action: auth, params: params
		}
	}

	/**
	 * The Ajax success redirect url.
	 */
	def ajaxSuccess = {
		// set output header to json
		response.contentType = 'application/json'

		render([success: true, username: springSecurityService.authentication.name] as JSON)
	}

	/**
	 * The Ajax denied redirect url.
	 */
	def ajaxDenied = {
		// set output header to json
		response.contentType = 'application/json'

		render([error: 'access denied'] as JSON)
	}
}

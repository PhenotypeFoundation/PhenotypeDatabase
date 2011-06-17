package dbnp.authentication

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class LogoutController {
	def authenticationService
	
	/**
	 * Index action. Redirects to the Spring security logout uri.
	 */
	def index = {
		if( params[ SpringSecurityUtils.securityConfig.successHandler.targetUrlParameter ] ) {
			redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl + "?" + SpringSecurityUtils.securityConfig.successHandler.targetUrlParameter + '=' + params[ SpringSecurityUtils.securityConfig.successHandler.targetUrlParameter ] // '/j_spring_security_logout'
		} else {
			redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/j_spring_security_logout'
		}

		// Remove all remote sessions for this user.
		def user = authenticationService.getLoggedInUser();
		if( user )
			authenticationService.deleteRemoteSessions( user );
		
		// Remove all queries from session
		session.queries = [];
	}

	def remote = {
		if( params.consumer || params.token ) {
			// Log out the remote user
			authenticationService.logOffRemotely( params.consumer, params.token )
		}
		
		def returnUrl;
		
		// If a returnUrl is given, use it for redirect
		if( params.returnUrl ) {
			returnUrl = params.returnUrl;
		} else {
			returnUrl = g.createLink(controller: 'home', absolute: true)
		}
		
		// Try to rest the redirect url
		if( params[ SpringSecurityUtils.securityConfig.successHandler.targetUrlParameter ] ) {
			redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl + "?spring-security-redirect=" + returnUrl?.encodeAsURL() + "&" + SpringSecurityUtils.securityConfig.successHandler.targetUrlParameter + '=' + params[ SpringSecurityUtils.securityConfig.successHandler.targetUrlParameter ] // '/j_spring_security_logout'
		} else {
			redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl + "?spring-security-redirect=" + returnUrl?.encodeAsURL() // '/j_spring_security_logout'
		}

		// Remove all remote sessions for this user.
		def user = authenticationService.getLoggedInUser();
		if( user )
			authenticationService.deleteRemoteSessions( user );
		
		// Remove all queries from session
		session.queries = [];
	}
}

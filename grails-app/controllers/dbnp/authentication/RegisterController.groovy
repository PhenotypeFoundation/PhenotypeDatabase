/* Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dbnp.authentication

import groovy.text.SimpleTemplateEngine

import grails.plugin.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.util.Holders

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class RegisterController {

	def springSecurityService
	def mailService
	
	static defaultAction = 'index'

	def saltSource

	def index = {
		[command: new RegisterCommand()]
	}

	def forgotPassword = {

		if (!request.post) {
			// show the form
			return
		}

		String username = params.username
		if (!username) {
			flash.userError = message(code: 'spring.security.ui.forgotPassword.username.missing')
			return
		}

		def user = SecUser.findByUsername(username)
		if (!user) {
			flash.userError = message(code: 'spring.security.ui.forgotPassword.user.notFound')
			return
		}

        // remove all previous registration codes for this user
        RegistrationCode.findAllByUser(user).each { it.delete() }

		// create a new registration code
        def registrationCode = new RegistrationCode(user: user, expiryDate: new Date() + 1 )
		if( !registrationCode.save() ) {
			flash.userError = "Your password could not be reset because of database errors. Please contact the system administrator."
			return
		}
		
		String url = generateLink('resetPassword', [t: registrationCode.token])

		def conf = SpringSecurityUtils.securityConfig
		def body = g.render(template:'/email/passwordReset', model: [user: user, url: url])

        if (grails.util.GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT) {
            println "development mode, no mail is sent. Reset URL for user ${user.username} is:"
            println registrationCode
            println "/gscf/register/resetPassword?t=${registrationCode.token}"
        } else {
            mailService.sendMail {
                to user.email
                from conf.ui.forgotPassword.emailFrom
                subject conf.ui.forgotPassword.emailSubject
                html body.toString()
            }
        }

		[emailSent: true]
	}

	def resetPassword = { ResetPasswordCommand command ->
		String token = params.t

		def registrationCode = token ? RegistrationCode.findByToken(token) : null
		if (!registrationCode) {
			flash.userError = message(code: 'spring.security.ui.resetPassword.badCode')
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
			return
		}

		if (!request.post) {
			return [token: token, command: new ResetPasswordCommand()]
		}

		command.username = registrationCode.user.username
		command.validate()

		if (command.hasErrors()) {
			return [token: token, command: command]
		}

		String salt = registrationCode.user.username
		RegistrationCode.withTransaction { status ->
            def user = registrationCode.user
			user.password = springSecurityService.encodePassword(command.password, salt)
			user.save()
			registrationCode.delete()
		}

		springSecurityService.reauthenticate registrationCode.user.username

		flash.message = message(code: 'spring.security.ui.resetPassword.success')

		def conf = SpringSecurityUtils.securityConfig
		String postResetUrl = conf.ui.register.postResetUrl ?: conf.successHandler.defaultTargetUrl
		redirect uri: postResetUrl
	}

	protected String generateLink(String action, linkParams) {
        createLink(controller: 'register', action: action, params: linkParams, absolute: true)
	}

	protected String evaluate(s, binding) {
		new SimpleTemplateEngine().createTemplate(s).make(binding)
	}

	static final passwordValidator = { String password, command ->
		if (command.username && command.username.equals(password)) {
			return 'command.password.error.username'
		}

		if (!password || password.length() < 8 || password.length() > 64 ||
				(!password.matches('^.*\\p{Alpha}.*$') ||
				!password.matches('^.*\\p{Digit}.*$') ||
				!password.matches('^.*[!@#$%^&].*$'))) {
			return 'command.password.error.strength'
		}
	}

	static final password2Validator = { value, command ->
		if (command.password != command.password2) {
			return 'command.password2.error.mismatch'
		}
	}
}

class RegisterCommand {

	String username
	String email
	String password
	String password2

	static constraints = {
		username blank: false, validator: { value, command ->
			if (value) {
				def User = Holders.grailsApplication.getDomainClass(
					SpringSecurityUtils.securityConfig.userLookup.userDomainClassName).clazz
				if (User.findByUsername(value)) {
					return 'registerCommand.username.unique'
				}
			}
		}
		email blank: false, email: true
		password blank: false, minSize: 8, maxSize: 64, validator: RegisterController.passwordValidator
		password2 validator: RegisterController.password2Validator
	}
}

class ResetPasswordCommand {
	String username
	String password
	String password2

	static constraints = {
		password blank: false, minSize: 8, maxSize: 64, validator: RegisterController.passwordValidator
		password2 validator: RegisterController.password2Validator
	}
}

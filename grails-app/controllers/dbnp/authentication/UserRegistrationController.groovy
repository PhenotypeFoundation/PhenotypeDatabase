/**
 * RegisterController Controler
 *
 * This controller handles user subscription
 *
 * @author      robert@isdat.nl (Robert Horlings)
 * @since	20101016
 * @package	dbnp.authentication
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.authentication
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.util.GrailsUtil

class UserRegistrationController {
	static int DAYS_BEFORE_EXPIRY = 3;
	static int DAYS_BEFORE_EXPIRY_ADMIN = 365;

    def springSecurityService
	def authenticationService
    
    /**
     * Shows a simple registration form
     */
    def index = {
    }

    /**
     * Registers a new user. Also sends an e-mail to the user and to the administrators
     * for confirmation
     */
    def add = { RegisterUserCommand command ->

		command.validate()

		if (command.hasErrors()) {
			def addSendUserLink = false;

			// Check the errors and append a link if needed
			command.errors.allErrors.each {
				if( it.code == "registerUserCommand.username.notyetconfirmed" ) {
					addSendUserLink = true;
				}
			}

			flash.message = "";
            render(view: "index", model: [username: params.username, email: params.email, command: command, addSendUserLink: addSendUserLink])
			return
		}

        // Generate a random password
        def password = this.generatePassword(8)

        def user = new SecUser(
           username	: params.username,
           email: params.email,
           password: springSecurityService.encodePassword(password, params.username),
           userConfirmed: false, adminConfirmed: false)
       
        // Redirect user if save fails
        if( !user.save(failOnError: true) ) {
            render(view: "index", model: [username: params.username, email: params.email])
            return
        }

        // Clear the flash message so the user does not see old messages
        flash.message = ""

		sendUserConfirmationMail( user, password );
		sendAdminConfirmationMail( user );

        // Give the user a nice welcome page
    }

	def sendUserConfirmation = {
		def user = SecUser.findByUsername( params.username );
		if( !user ) {
			flash.message = "No user with this username is found in the database.";
            render(view: "index", model: [username: params.username])
		}
		if( user.userConfirmed ) {
			flash.message = "This user has already been confirmed";
            render(view: "index", model: [username: params.username])
		}

		// Remove old registration codes
		RegistrationCode.deleteByUser(user);

		// Create a new password
        def password = this.generatePassword(8)
		user.password = springSecurityService.encodePassword(password, user.username)

		// Send a message
		sendUserConfirmationMail(user, password);

		flash.message = "A new confirmation email has been sent to your registered email address";
		render(view: "index", model: [username: params.username])
	}

	private sendUserConfirmationMail( SecUser user, String password ) {
		def userCode = new RegistrationCode(user: user, expiryDate: new Date() + UserRegistrationController.DAYS_BEFORE_EXPIRY).save( flush: true );
        def userLink = createLink( controller: 'userRegistration', action: 'confirmUser', params: [code: userCode.token], absolute: true )

        // Send an email to the user
        try {
            sendMail {
                to      user.email
                subject "Registration at Phenotype Database"
                html    g.render(template:'/email/registrationConfirmationUser', model:[username: user.username, password: password, expiryDate: userCode.expiryDate, link: userLink])
            }
        } catch(Exception e) {
            log.error "Problem sending email $e.message", e
            flash.message = 'Email could not be sent'

			return false
        }

		return true
	}

	private sendAdminConfirmationMail( SecUser user ) {
		def adminCode = new RegistrationCode(user: user, expiryDate: new Date() + UserRegistrationController.DAYS_BEFORE_EXPIRY_ADMIN).save(flush: true)
        def adminLink = createLink( controller: 'userRegistration', action: 'confirmAdmin', params: [code: adminCode.token], absolute: true )

		// If we are in production, send the mails to all administrators
		// Otherwise, send it to a default (spam) mail address
		def adminMail = "gscfproject@gmail.com";
		if ( !GrailsUtil.getEnvironment().equals(GrailsApplication.ENV_DEVELOPMENT) ) {
			def administrators = SecRole.findUsers( 'ROLE_ADMIN' );
			if( administrators.size() > 0 ) {
				adminMail = administrators.email.toArray();
			}
		}

        // Send an email to the administrators
        try {
			// Determine administrator email addresses
            sendMail {
                to      adminMail
                subject "New user (" + user.username + ") at GSCF"
                html    g.render(template:"/email/registrationConfirmationAdmin", model:[username: user.username, email: user.email, link: adminLink])
            }
        } catch(Exception e) {
            log.error "Problem sending email $e.message", e
            flash.message = "Email could not be sent to administrators"
			return false
        }
		return true
	}


    def confirmUser = {
        def token = params.code

		def registrationCode = token ? RegistrationCode.findByToken(token) : null
		if (!registrationCode) {
            flash.message = "No user found with given parameters. Please make sure you have copied the URL correctly."
            return
		}

		if (registrationCode.expiryDate.before(new Date())) {
            flash.message = "Your registration should have been confirmed within " + UserRegistrationController.DAYS_BEFORE_EXPIRY + " days. This confirmation link has expired. Please register again."
            return
		}

		def user = registrationCode.user

        if( user.userConfirmed ) {
            flash.message = "This registration has already been confirmed."
            return
        }

        user.userConfirmed = true
        user.save(flush: true)

		// Remove the registrationCode
		registrationCode.delete();

        if( user.adminConfirmed ) {
            flash.message = "Your registration has been confirmed. You can now login."
        } else {
            flash.message = "Your registration has been confirmed. An administrator has to approve your registration before you can use it."
        }
    }

    def confirmAdmin() {

        def token = params.code

		def registrationCode = token ? RegistrationCode.findByToken(token) : null
		if (!registrationCode) {
            flash.message = "No user found with specified code. Please make sure you have copied the URL correctly."
            return
		}

		if (registrationCode.expiryDate.before(new Date())) {
            flash.message = "You should have approved this registration within " + UserRegistrationController.DAYS_BEFORE_EXPIRY_ADMIN + " days. This confirmation link has expired."
            return
		}

		def user = registrationCode.user

        if( user.adminConfirmed ) {
            flash.message = "This user has already been approved. This might be done by another administrator"
            return
        }

        user.adminConfirmed = true
        user.save(flush: true)

		// Remove the registrationCode
		registrationCode.delete();

        flash.message = "The registration of " + user.username + " is approved."
    }

	def profile() {
		[ user: authenticationService.getLoggedInUser() ]
	}

	def updateProfile(ProfileCommand command) {
		def user = authenticationService.getLoggedInUser();
		command.username = user.username
		command.oldPass = user.password
		command.validate()

		if (command.hasErrors()) {
			render( view: 'profile', model: [user: user, command: command]);
			return
		}

		String salt = user.username
		RegistrationCode.withTransaction { status ->
			if( command.password != "" ) 
				user.password = springSecurityService.encodePassword(command.password, salt)
			user.email = command.email
			user.save()
		}

		redirect controller: 'home'
	}

    private String generatePassword( int length ) {
        String validChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-!@#%^&*()/\\;:"
        int maxIndex = validChars.length()
        
        java.util.Random rnd = new java.util.Random(System.currentTimeMillis()*(new java.util.Random().nextInt()))
        String resultID = ""
        
        for ( i in 0..length ) {
            int rndPos = Math.abs(rnd.nextInt() % maxIndex);
            resultID += validChars.charAt(rndPos)
        }

        return resultID
    }

	static final passwordValidator = { String password, command ->
		if( password == "" ) {
			return
		}

		if (command.username && command.username.equals(password)) {
			return 'command.password.error.username'
		}

		if (password && password.length() >= 8 && password.length() <= 64 &&
				(!password.matches('^.*\\p{Alpha}.*$') ||
				!password.matches('^.*\\p{Digit}.*$') ||
				!password.matches('^.*[!@#$%+^&].*$'))) {
			return 'command.password.error.strength'
		}
	}

	static final password2Validator = { value, command ->
		if (command.password != command.password2) {
			return 'command.password2.error.mismatch'
		}
	}

	static final usernameValidator = { value, command ->
		def user = SecUser.findByUsername( command.username );
		if( user ) {
            if( user.enabled ) {
				return "registerUserCommand.username.unique"
			} else if( user.dateCreated.after( new Date() - DAYS_BEFORE_EXPIRY ) ) {
				return "registerUserCommand.username.notyetconfirmed"
			} else {
				RegistrationCode.deleteByUser(user);
				user.delete(flush:true);
			}
        }
	}

}

class ProfileCommand {

	String username
	String oldPass
	String email
	String password
	String password2

	static constraints = {
		username blank: false
		email blank: false, email: true
		password blank: true, minSize: 8, maxSize: 64, validator: UserRegistrationController.passwordValidator
		password2 validator: UserRegistrationController.password2Validator
	}
}

class RegisterUserCommand {

	String username
	String email

	static constraints = {
		email(blank: false, email: true)
		username(blank: false, validator: UserRegistrationController.usernameValidator)
	}
}

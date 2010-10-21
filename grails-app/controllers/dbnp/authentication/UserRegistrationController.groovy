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
import grails.plugins.springsecurity.Secured

class UserRegistrationController {
    def springSecurityService
    
    /**
     * Shows a simple registration form
     */
    def index = {

    }

    /**
     * Registers a new user. Also sends an e-mail to the user and to the administrators
     * for confirmation
     */
    def add = {
        if( !params.username || !params.email ) {
            flash.message = "You must enter a username and provide an email address"
            render(view: "index", model: [username: params.username, email: params.email])
            return
        }

        // Check whether this username already exists
        if( SecUser.findByUsername( params.username ) ) {
            flash.message = "Username already exists"
            render(view: "index", model: [username: params.username, email: params.email])
            return
        }

        // Generate a random password
        def password = this.generatePassword(8)

        def user = new SecUser(
           username: params.username,
           email: params.email,
           password: springSecurityService.encodePassword(password, params.username),
           userConfirmed: true, adminConfirmed: true)
       
        // Redirect user if save fails
        if( !user.save(failOnError: true) ) {
            render(view: "index", model: [username: params.username, email: params.email])
            return
        }

        // Clear the flash message so the user does not see old messages
        flash.message = ""

        // Create links for the user and administrator to click on. These codes are built from
        // the username and encrypted password. They do not provide 100% security, since the codes
        // could be broken, but it is enough for the confirmation step
        def userCode = ( user.username + user.password + 'user' ).encodeAsMD5();
        def adminCode = ( user.username + user.password + 'admin' ).encodeAsMD5();
        def userLink = createLink( controller: 'userRegistration', action: 'confirmUser', params: [id: user.id, code: userCode], absolute: true )
        def adminLink = createLink( controller: 'userRegistration', action: 'confirmAdmin', params: [id: user.id, code: adminCode], absolute: true )

        // Send an email to the user
        try {
            sendMail {
                to      params.email
                subject "Registration at GSCF"
                html    g.render(template:'/email/registrationConfirmationUser', model:[username: user.username, password: password, link: userLink])
            }
        } catch(Exception e) {
            log.error "Problem sending email $e.message", e
            flash.message = 'Email could not be sent'
        }

        // Send an email to the administrators
        try {
            sendMail {
                to      "gscfproject@gmail.com"
                subject "New user (" + user.username + ") at GSCF"
                html    g.render(template:"/email/registrationConfirmationAdmin", model:[username: user.username, email: user.email, link: adminLink])
            }
        } catch(Exception e) {
            log.error "Problem sending email $e.message", e
            flash.message = "Email could not be sent to administrators"
        }

        // Give the user a nice welcome page
        [username: user.username, password: password]
    }

    def confirmUser = {
        def code = params.code
        def id = params.id

        def user = SecUser.findById(id)

        def generatedCode = ( user.username + user.password + "user" ).encodeAsMD5()
        if( !user || code != generatedCode ) {
            flash.message = "No user found with given parameters. Please make sure you have copied the URL correctly."
            return
        }

        if( user.userConfirmed ) {
            flash.message = "This registration has already been confirmed."
            return
        }

        user.userConfirmed = true
        user.save(flush: true)

        if( user.adminConfirmed ) {
            flash.message = "Your registration has been confirmed. You can now login."
        } else {
            flash.message = "Your registration has been confirmed. An administrator has to approve your registration before you can use it."
        }
    }

    @Secured(['ROLE_ADMIN', 'IS_AUTHENTICATED_FULLY'])
    def confirmAdmin = {
        def code = params.code
        def id = params.id

        def user = SecUser.findById(id)

        def generatedCode = ( user.username + user.password + "admin" ).encodeAsMD5();
        if( !user || code != generatedCode ) {
            flash.message = "No user found with specified code. Please make sure you have copied the URL correctly."
            return;
        }

        if( user.adminConfirmed ) {
            flash.message = "This user has already been approved. This might be done by another administrator"
            return
        }

        user.adminConfirmed = true
        user.save(flush: true)

        flash.message = "The registration of " + user.username + " is approved."
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
}

package dbnp.authentication

import grails.plugins.springsecurity.Secured
import grails.plugins.springsecurity.ui.*
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.plugins.springsecurity.ui.RegistrationCode

class RegisterController extends grails.plugins.springsecurity.ui.RegisterController {

    // The registration should be done using the UserRegistration controller
    @Secured(['ROLE_ADMIN'])
    def index = {
        throw new Exception( "Method should not be called!" )
    }

}

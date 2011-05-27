import org.codehaus.groovy.grails.commons.GrailsApplication

/**
 * Application Configuration
 *
 * @author Jeroen Wesbeek 
 * @since 20100520
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts
//
grails.config.locations = [
	// the default per-environment configuration
	// (e.g. src/java/config-development.properties)
	"classpath:config-${grails.util.GrailsUtil.environment}.properties",

	// the external configuration to override the default
	// configuration (e.g. ~/.gscf/ci.properties)
	"file:${userHome}/.${appName}/${grails.util.GrailsUtil.environment}.properties"
]

grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [html: ['text/html', 'application/xhtml+xml'],
	xml: ['text/xml', 'application/xml'],
	text: 'text/plain',
	js: 'text/javascript',
	rss: 'application/rss+xml',
	atom: 'application/atom+xml',
	css: 'text/css',
	csv: 'text/csv',
	all: '*/*',
	json: ['application/json', 'text/json'],
	form: 'application/x-www-form-urlencoded',
	multipartForm: 'multipart/form-data'
]
// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true

// log4j configuration
log4j = {
	// Example of changing the log pattern for the default console
	// appender:
	//
	//appenders {
	//    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
	//}
	
	// info "grails.app"

	error 'org.codehaus.groovy.grails.web.servlet',  //  controllers
		'org.codehaus.groovy.grails.web.pages', //  GSP
		'org.codehaus.groovy.grails.web.sitemesh', //  layouts
		'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
		'org.codehaus.groovy.grails.web.mapping', // URL mapping
		'org.codehaus.groovy.grails.commons', // core / classloading
		'org.codehaus.groovy.grails.plugins', // plugins
		'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
		'org.springframework',
		'org.hibernate'

    error stdout:"StackTrace"

	warn 'org.mortbay.log'

	//info 'org.codehaus.groovy.grails.web.servlet',
	//	 'org.codehaus.groovy.grails.plugins'
	//
	//debug 'org.codehaus.groovy.grails.plugins'
}

graphviz {
	// graphviz installation path is dependent on OS
	// (requirement for class diagram plugin)
	switch (System.properties["os.name"]) {
		case "Mac OS X":
			// define mac path to Graphviz dot executable
			// (install using macports: sudo port install graphviz)
			dot.executable = "/opt/local/bin/dot"
			break
		default:
			// assume the linux default path
			dot.executable = "/usr/bin/dot"
	}
}

// jquery plugin
grails.views.javascript.library = "jquery"

// Needed for the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'dbnp.authentication.SecUser'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'dbnp.authentication.SecUserSecRole'
grails.plugins.springsecurity.authority.className = 'dbnp.authentication.SecRole'
grails.plugins.springsecurity.password.algorithm = 'SHA-256'
grails.plugins.springsecurity.password.encodeHashAsBase64 = true
grails.plugins.springsecurity.dao.reflectionSaltSourceProperty = 'username' // Use the persons username as salt for encryption
grails.plugins.springsecurity.securityConfigType = grails.plugins.springsecurity.SecurityConfigType.Annotation
grails.plugins.springsecurity.successHandler.targetUrlParameter = 'spring-security-redirect'

// Needed for the (copy of) the Spring Security UI plugin
grails.mail.default.from = 'gscf@dbnp.org'
grails.plugins.springsecurity.ui.forgotPassword.emailFrom = 'gscf@dbnp.org'
grails.plugins.springsecurity.ui.forgotPassword.emailSubject = 'Password reset GSCF'

// Make sure the different controllers provided by springsecurity.ui are only accessible by administrators
// NB: the RegisterController is used for forgotten passwords. It should be accessible by anyone
grails.plugins.springsecurity.controllerAnnotations.staticRules = [
	'/user/**': ['ROLE_ADMIN'],
	'/role/**': ['ROLE_ADMIN'],
	'/aclclass/**': ['ROLE_ADMIN'],
	'/aclentry/**': ['ROLE_ADMIN'],
	'/aclobjectidentity/**': ['ROLE_ADMIN'],
	'/aclsid/**': ['ROLE_ADMIN'],
	'/persistentlogin/**': ['ROLE_ADMIN'],
	'/registrationcode/**': ['ROLE_ADMIN'],
	'/requestmap/**': ['ROLE_ADMIN'],
	'/securityinfo/**': ['ROLE_ADMIN']
]

// Temporary directory to upload files to.
// If the directory is given relative (e.g. 'fileuploads/temp'), it is taken relative to the web-app directory
// Otherwise, it should be given as an absolute path (e.g. '/home/user/sequences')
// The directory should be writable by the webserver user
if (grails.util.GrailsUtil.environment == GrailsApplication.ENV_TEST) {
    uploads.uploadDir = "webtestfiles"
} else {
    uploads.uploadDir = (new File("/tmp")?.canWrite()) ? "/tmp" : "fileuploads"    
}

// Required configuration variables for gdtImporter plugin
// Does the parent entity have an owner (SecUser in GSCF)
gdtImporter.parentEntityHasOwner = true
// How do children refer to the parent (belongsTo)
gdtImporter.childEntityParentName = "parent"
// What is the class name of the parent entity
gdtImporter.parentEntityClassName = "dbnp.studycapturing.Study"

// default application title
application.title = "Generic Study Capture Framework"

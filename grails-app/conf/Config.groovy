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
	// the default configuration properties
	"classpath:default.properties",

	// the external configuration to override the default
	// configuration (e.g. ~/.gscf/ci.properties)
	"file:${userHome}/.${appName}/${grails.util.GrailsUtil.environment}.properties",
    "file:${userHome}/.${appName}/${grails.util.GrailsUtil.environment}.groovy"
]

grails.config.locations.each { println "Reading configuration from ${it}" }

grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
	html: ['text/html', 'application/xhtml+xml'],
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

log4j = {
    error  'org.codehaus.groovy.grails.web.servlet',        // controllers
            'org.codehaus.groovy.grails.web.pages',          // GSP
            'org.codehaus.groovy.grails.web.sitemesh',       // layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping',        // URL mapping
            'org.codehaus.groovy.grails.commons',            // core / classloading
            'org.codehaus.groovy.grails.plugins',            // plugins
            'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
            'org.springframework',
            'org.hibernate',
            'net.sf.ehcache.hibernate'

    warn   'org.codehaus.groovy.grails.orm.hibernate',
            'org.hibernate'                                  // hibernate integration

    all    'grails.app', 'dbnp.query', 'dbnp.importer', 'org.dbxp.matriximporter'

    // Disable logging for resources plugin
    error  'grails.app.services.org.grails.plugin.resource',
            'grails.app.taglib.org.grails.plugin.resource',
            'grails.app.resourceMappers.org.grails.plugin.resource',
            'org.grails.plugin.resource'
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
grails.plugin.springsecurity.userLookup.userDomainClassName = 'dbnp.authentication.SecUser'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'dbnp.authentication.SecUserSecRole'
grails.plugin.springsecurity.authority.className = 'dbnp.authentication.SecRole'
grails.plugin.springsecurity.password.algorithm = 'SHA-256'
grails.plugin.springsecurity.password.hash.iterations = 1
grails.plugin.springsecurity.password.encodeHashAsBase64 = true
grails.plugin.springsecurity.dao.reflectionSaltSourceProperty = 'username' // Use the persons username as salt for encryption
grails.plugin.springsecurity.successHandler.targetUrlParameter = 'spring-security-redirect'

// Security is defined below. By default, users that have
// been logged in are allowed access to all controllers and
// other users are denied access. As this is the default,
// we specify the 'open' urls as well as the administrator 
grails.plugin.springsecurity.rejectIfNoRule = true
grails.plugin.springsecurity.fii.rejectPublicInvocations = false
grails.plugin.springsecurity.securityConfigType = grails.plugin.springsecurity.SecurityConfigType.InterceptUrlMap

grails.plugin.springsecurity.interceptUrlMap = [
        '/*':                  						        ['permitAll'],
        '/home':              						        ['permitAll'],
        '/study/**':          						        ['permitAll'],
        '/publication/list':  						        ['permitAll'],
        '/assets/**':         						        ['permitAll'],
        '/**/js/**':          						        ['permitAll'],
        '/**/css/**':         						        ['permitAll'],
        '/**/images/**':      						        ['permitAll'],
        '/**/favicon.ico':    						        ['permitAll'],
        '/login/**':          						        ['permitAll'],
        '/logout/**':         						        ['permitAll'],
        '/downloads/**':          						    ['permitAll'],
		'/error/**':										['permitAll'],
        '/info':                                            ['permitAll'],

        // Registration and confirming new accounts
        '/register/forgotPassword':                         ['permitAll'],
        '/register/resetPassword':                          ['permitAll'],
        '/userRegistration/add':                            ['permitAll'],
        '/userRegistration/sendUserConfirmation':           ['permitAll'],
        '/userRegistration/confirmUser':                    ['permitAll'],

        // Design view of a public studies
        '/studyEditDesign/eventGroupDetails/*':		        ['permitAll'],
        '/studyEditDesign/subjectGroupDetails/*':			['permitAll'],
        '/studyEditDesign/dataTableSubjectSelection/*':		['permitAll'],

		// View of assay list for public assays
        '/measurements/*/assay/list':				        ['permitAll'],
        '/measurements/*/assay/datatables_list':			['permitAll'],

        // View of measurements of a public assay
        '/measurements/*/assay/show/*':				        ['permitAll'],
		'/measurements/*/assay/showByToken/*':				['permitAll'],

        // Rest controllers have their own authentication
        '/rest/**':                                         ['permitAll'],
        '/measurements/*/rest/**':                          ['permitAll'],

        // API authentication is only accessible for specific users
        '/api/**':											['permitAll'],
        '/api/authenticate/**':								['ROLE_ADMIN', 'ROLE_CLIENT'],

        // Template editor is only accessible for specific users
        '/template/**':                         	        ['ROLE_ADMIN', 'ROLE_TEMPLATEADMIN'],

        // Configuration by administrators
        '/assayModule/**':                      	        ['ROLE_ADMIN'],
        '/setup/**':                            	        ['ROLE_ADMIN'],
        '/info/**':                             	        ['ROLE_ADMIN'],
        '/user/**':                             	        ['ROLE_ADMIN', 'isFullyAuthenticated()'],
        '/userRegistration/confirmAdmin':       	        ['ROLE_ADMIN', 'isFullyAuthenticated()'],

        // All other urls are allowed for logged in users
        '/**':										        ['IS_AUTHENTICATED_REMEMBERED']
 ]


//Login errors
grails.plugin.springsecurity.errors.login.expired= 'Sorry, your account has expired.'
grails.plugin.springsecurity.errors.login.passwordExpired='Sorry, your password has expired.'
grails.plugin.springsecurity.errors.login.disabled='Sorry, your account is disabled.'
grails.plugin.springsecurity.errors.login.locked='Sorry, your account is locked.'
grails.plugin.springsecurity.errors.login.fail='Sorry, we were not able to find a user with that username and password.'
grails.plugin.springsecurity.denied.message='Sorry, you\'re not authorized to view this page.'

// Spring Security configuration
grails.plugin.springsecurity.useBasicAuth = true
grails.plugin.springsecurity.basic.realmName = "Authentication Required"
grails.plugin.springsecurity.useSessionFixationPrevention = true
grails.plugin.springsecurity.filterChain.chainMap = [
        '/rest/hello': 'JOINED_FILTERS,-exceptionTranslationFilter',
        '/api/authenticate': 'JOINED_FILTERS,-exceptionTranslationFilter',
        '/**': 'JOINED_FILTERS,-basicAuthenticationFilter,-basicExceptionTranslationFilter'
]

// default application properties
application.title = "Phenotype Database"

grails.resources.modules = {
	overrides {
		'jquery-theme' {
			resource id:'theme', url:'https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.3/themes/cupertino/jquery-ui.min.css'
		}
	}
}

// SAM Configuration

// Fuzzy matching configuration
fuzzyMatching.threshold = [
        'default': 0.2,
        'featureImporter' : [
                'feature': 0.4
        ],
        'measurementImporter': [
                'feature' : 0.4,
                'sample' : 0.2,
                'timepoint' : 0.2,
                'subject' : 0.2
        ]
]

// Example email server configurations, to be put in external configuration file
// (production.groovy or development.groovy)

// Example gmail configuration
/*
grails {
    mail {
        host = "smtp.gmail.com"
        port = 465
        username = "<your_address>@gmail.com"
        password = "<your_password>"
        props = [
            "mail.smtp.auth": "true",
            "mail.smtp.socketFactory.port": "465",
            "mail.smtp.socketFactory.class": "javax.net.ssl.SSLSocketFactory",
            "mail.smtp.socketFactory.fallback": "false",
            "mail.smtp.starttls.enable": "true"
        ]
    }
}
*/

// Example configuration for mail server without authentication
/*
grails {
    mail {
        "default" {
            from = "<from_address>@example.com"
        }
        host = "127.0.0.1"
        port = 25
        props = [:]
    }
}
*/

// Alternatively, the configuration can be put in the external production.properties or
// development.properties:
/*
#grails.mail.host=127.0.0.1
#grails.mail.port=2525
*/

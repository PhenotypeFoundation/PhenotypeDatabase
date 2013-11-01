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
	"file:${userHome}/.${appName}/${grails.util.GrailsUtil.environment}.properties"
]

grails.config.locations.each { println "Reading configuration from ${it}" }

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
//log4j = {
	// Example of changing the log pattern for the default console
	// appender:
	//
	//appenders {
	//    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
	//}
	/*appenders {
		rollingFile name: "stacktrace", maxFileSize: 1024, file: "/tmp/gscf-${grails.util.GrailsUtil.environment}.log"
	}

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

	warn 'org.mortbay.log'*/

	//info 'org.codehaus.groovy.grails.web.servlet',
	//	 'org.codehaus.groovy.grails.plugins'
	//
	//debug 'org.codehaus.groovy.grails.plugins'
//}

log4j = {
    // Example of changing the log pattern for the default console appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

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

	warn  'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
			'org.hibernate'
		 
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


// Spring Security configuration
grails.plugins.springsecurity.useBasicAuth = true
grails.plugins.springsecurity.basic.realmName = "Authentication Required"
grails.plugins.springsecurity.useSessionFixationPrevention = true
grails.plugins.springsecurity.filterChain.chainMap = [
        '/rest/hello': 'JOINED_FILTERS,-exceptionTranslationFilter',
        '/api/authenticate': 'JOINED_FILTERS,-exceptionTranslationFilter',
        '/**': 'JOINED_FILTERS,-basicAuthenticationFilter,-basicExceptionTranslationFilter'
]

// Temporary directory to upload files to.
// If the directory is given relative (e.g. 'fileuploads/temp'), it is taken relative to the web-app directory
// Otherwise, it should be given as an absolute path (e.g. '/home/user/sequences')
// The directory should be writable by the webserver user
if (grails.util.GrailsUtil.environment == GrailsApplication.ENV_TEST) {
    uploads.uploadDir = "webtestfiles"
    uploads.storageDir = "storagetestfiles"
} else {
    uploads.uploadDir = (new File("/tmp/fileuploads")?.canWrite()) ? "/tmp/fileuploads" : "fileuploads"
    uploads.storageDir = (new File("/tmp/filestorage")?.canWrite()) ? "/tmp/filestorage" : "filestorage"
}

// Required configuration variables for gdtImporter plugin
// Does the parent entity have an owner (SecUser in GSCF)
gdtImporter.parentEntityHasOwner = true
// How do children refer to the parent (belongsTo)
gdtImporter.childEntityParentName = "parent"
// What is the class name of the parent entity
gdtImporter.parentEntityClassName = "dbnp.studycapturing.Study"

// default application properties
application.title = "Generic Study Capture Framework"
application.template.admin.email = "me@example.com"


grails.resources.modules = {
	overrides {
		'jquery-theme' {
			resource id:'theme', url:'/css/cupertino/jquery-ui-1.8.23.custom.css'
		}
	}
}

// ****** trackR Config ******
trackr.path = "/tmp/trackr/"
trackr.prefix = "gscf.${grails.util.GrailsUtil.environment}."

// SAM Configuration

// Temporary directory to upload files to.
// If the directory is given relative (e.g. 'fileuploads/temp'), it is taken relative to the web-app directory
// Otherwise, it should be given as an absolute path (e.g. '/home/user/sequences')
// The directory should be writable by the webserver user
if (grails.util.GrailsUtil.environment == GrailsApplication.ENV_TEST) {
    uploads.uploadDir = "webtestfiles"
} else {
    uploads.uploadDir = (new File("/tmp")?.canWrite()) ? "/tmp" : "fileuploads"
}

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

gscf.baseURL = grails.serverURL
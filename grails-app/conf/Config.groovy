// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

grails.config.locations = [
	// the default configuration properties
	"classpath:default.properties",
	"file:${ basedir }/grails-app/conf/default.properties", /*<- "run-app" mode */

	// the external configuration to override the default
	// configuration (e.g. ~/.dbxp/production-sam.properties)
	"file:${userHome}/.dbxp/${grails.util.GrailsUtil.environment}-${appName}.properties",
]

if(System.properties["${appName}.config.location"]) {
	grails.config.locations << "file:" + System.properties["${appName}.config.location"]
}


grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// Temporary directory to upload files to.
// If the directory is given relative (e.g. 'fileuploads/temp'), it is taken relative to the web-app directory
// Otherwise, it should be given as an absolute path (e.g. '/home/user/sequences')
// The directory should be writable by the webserver user
if (grails.util.GrailsUtil.environment == GrailsApplication.ENV_TEST) {
	uploads.uploadDir = "webtestfiles"
} else {
	uploads.uploadDir = (new File("/tmp")?.canWrite()) ? "/tmp" : "fileuploads"
}

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate',
		   
           'grails.app.service.org.grails.plugin.resource.ResourceService',
           'grails.app.tagLib.org.grails.plugin.resource.ResourceTagLib'

    warn   'org.mortbay.log'

			
	// Change log4j properties for production
	environments {
        development {
            warn   "grails.app"
                    //'org.codehaus.groovy.grails.plugins' // plugins
        }
		production {
			info	"grails.app"
		}
	}
}

module.synchronization.classes.sample = "org.dbxp.sam.SAMSample"


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
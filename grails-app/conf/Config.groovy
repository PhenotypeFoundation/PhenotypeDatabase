import dbnp.rest.common.CommunicationManager

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

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }
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

// set per-environment serverURL stem for creating absolute links
environments {
	development {
		grails.serverURL = "http://localhost:8080/${appName}"
		modules {
			sam {
				url = "http://localhost:8182/sam"
			}
			metabolomics {
				url = "http://localhost:8183/nmcdsp"
			}
		}
	}
	ci {
		// used by build script
		grails.serverURL = "http://ci.gscf.nmcdsp.org"
		modules {
			sam {
				url = "http://ci.sam.nmcdsp.org"
			}
			metabolomics {
				url = "http://ci.metabolomics.nmcdsp.org"
			}
		}
	}
	test {
		// used by build script
		grails.serverURL = "http://test.gscf.nmcdsp.org"
		modules {
			sam {
				url = "http://test.sam.nmcdsp.org"
			}
			metabolomics {
				url = "http://test.metabolomics.nmcdsp.org"
			}
		}
	}
	dbnptest {
		// used by build script
		grails.serverURL = "http://test.dbnp.org"
		modules {
			sam {
				url = "http://test.sam.dbnp.org"
			}
			metabolomics {
				url = "http://test.metabolomics.dbnp.org"
			}
		}
	}
	dbnpdemo {
		// used by build script
		grails.serverURL = "http://demo.dbnp.org"
		modules {
			sam {
				url = "http://demo.sam.dbnp.org"
			}
			metabolomics {
				url = "http://demo.metabolomics.dbnp.org"
			}
		}
	}
	production {
		grails.serverURL = "http://www.nmcdsp.org"
		modules {
			sam {
				url = "http://sam.nmcdsp.org"
			}
			metabolomics {
				url = "http://metabolomics.nmcdsp.org"
			}
		}
	}
	www {
		// used by build script
		grails.serverURL = "http://www.nmcdsp.org"
		modules {
			sam {
				url = "http://sam.nmcdsp.org"
			}
			metabolomics {
				url = "http://metabolomics.nmcdsp.org"
			}
		}
	}
}

// log4j configuration
log4j = {
	// Example of changing the log pattern for the default console
	// appender:
	//
	//appenders {
	//    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
	//}

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

	warn 'org.mortbay.log'

	//info 'org.codehaus.groovy.grails.web.servlet',
	//	 'org.codehaus.groovy.grails.plugins'

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

// cryptography settings
// @see WizardTaglib.groovy (encrypt)
// @see TemplateEditorController.groovy (decrypt)
crypto {
	shared.secret = "U73reG*mE^\$t@7s!e%"
}

// GSCF specific configuration
gscf {
	domain = [
		// importable entities
		// use: grailsApplication.config.gscf.domain.entities
		entities: [
			// dbnp.data
			'dbnp.data.FeatureBase',
			'dbnp.data.FeatureType',
			'dbnp.data.Ontology',
			'dbnp.data.Term',

			// dbnp.studycapturing
			'dbnp.studycapturing.Assay',
			'dbnp.studycapturing.AssayModule',
			'dbnp.studycapturing.AssayType',
			'dbnp.studycapturing.Compound',
			'dbnp.studycapturing.Event',
			'dbnp.studycapturing.EventGroup',
			'dbnp.studycapturing.Person',
			'dbnp.studycapturing.PersonAffilitation',
			'dbnp.studycapturing.PersonRole',
			'dbnp.studycapturing.Publication',
			'dbnp.studycapturing.Sample',
			'dbnp.studycapturing.SamplingEvent',
			'dbnp.studycapturing.Study',
			'dbnp.studycapturing.StudyPerson',
			'dbnp.studycapturing.Subject',
			'dbnp.studycapturing.Template',
			'dbnp.studycapturing.TemplateEntity',
			'dbnp.studycapturing.TemplateField',
			'dbnp.studycapturing.TemplateFieldListItem',
			'dbnp.studycapturing.TemplateFieldType'
		],

		// importable entities
		// use: grailsApplication.config.gscf.domain.importableEntities
		// @see ImporterController
		importableEntities: [
			event: [name: 'Event', entity: 'dbnp.studycapturing.Event', encrypted:''],
			sample: [name: 'Sample', entity: 'dbnp.studycapturing.Sample', encrypted:''],
			study: [name: 'Study', entity: 'dbnp.studycapturing.Study', encrypted:''],
			subject: [name: 'Subject', entity: 'dbnp.studycapturing.Subject', encrypted:''],
			samplingevent: [name: 'SamplingEvent', entity: 'dbnp.studycapturing.SamplingEvent', encrypted:'']

		]
	]
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

// Needed for sending emails
grails {
	mail {
		host = "smtp.gmail.com"
		port = 465
		username = "gscfproject@gmail.com"
		password = "gscf2010"
		props = [
			"mail.smtp.auth": "true",
			"mail.smtp.socketFactory.port": '465',
			"mail.smtp.socketFactory.class": "javax.net.ssl.SSLSocketFactory",
			"mail.smtp.socketFactory.fallback": "false"
		]
	}
}

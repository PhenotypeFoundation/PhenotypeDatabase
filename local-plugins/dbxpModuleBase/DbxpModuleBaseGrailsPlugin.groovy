class DbxpModuleBaseGrailsPlugin {
    // the plugin version
    def version = "0.6.2.0"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.7 > *"

    // the other plugins this plugin depends on are declared in BuildConfig.groovy
	def dependsOn		= [jquery: "1.7.1 > *", jqueryDatatables: "1.7.5 > *", jqueryUi: "1.7.1 > *" ]
	// resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/views/index.gsp",
            "grails-app/controllers/test"
    ]

	def author			= "Robert Horlings"
	def authorEmail 	= "robert@isdat.nl"
	def title			= "Base for Phenotype Database modules that communicate with GSCF"
	def description 	= '''\\
dbxpModuleBase is a Grails plugin that provides basic functionality to create a dbXP module that contains molecular
data for studies defined in GSCF, the Generic Study Capture Framework.
dbxpModuleBase is part of the Phenotype Database software ecosystem.

As of version 0.6.2.0 this module is ready for grails 2.4.0 and higher
'''
	def documentation   = "https://github.com/thehyve/dbxpModuleBase/blob/master/README.md"
	def license         = "APACHE"
	def issueManagement = [system: "github", url: "https://github.com/thehyve/dbxpModuleBase/issues"]
	def scm             = [url: "https://github.com/thehyve/dbxpModuleBase"]
	def organization    = [name: "Phenotype Foundatiom", url: "http://phenotypefoundation.org/" ]
	def developers      = [
			[ name: "Kees van Bochove", email: "kees@thehyve.nl" ],
			[ name: "Robert Horlings", email: "robert@isdat.nl" ],
			[ name: "Siemen Sikkema", email: "s.h.sikkema@gmail.com" ],
			[ name: "Jeroen Wesbeek", email: "work@osx.eu" ],
			[ name: "Taco Steemers", email: "taco@thehyve.nl" ],
			[ name: "Tjeerd van Dijk", email: "tjeerd@thehyve.nl" ]
	]
}

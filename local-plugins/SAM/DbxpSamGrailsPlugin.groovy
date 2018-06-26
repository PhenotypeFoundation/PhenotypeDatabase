class DbxpSamGrailsPlugin {
    // the plugin version
    def version = "0.9.7.5"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.1 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp",
        "grails-app/conf/DataSource.groovy"
    ]
    def loadAfter = ['gdt','dbxpBase','dbxpModuleBase','springSecurity']
    // TODO Fill in these fields
    def title = "dbXP SAM Plugin" // Headline display name of the plugin
    def author = "Kees van Bochove"
    def authorEmail = "kees@thehyve.nl"
    def description = '''\
SAM is an open source application for storing numerical phenotype observations related to biological studies, within the framework of the Phenotype Database.
Background: The Phenotype Database (dbXP, see http://dbnp.org) project is an open source ecosystem for multi-omics databases.
One of the modules of the Phenotype Database (dbXP) is the Simple assay module (SAM), in which it is easy to import bulk numerical data from e.g. Excel files.
It can be used to store clinical data, such as Rules Based Medicine assays, but also physical measurements such as body weight of mice.
'''

    // URL to the plugin's documentation
    def documentation = "http://dbxp.org"
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "The Hyve", url: "http://www.thehyve.nl" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "github", url: "https://github.com/PhenotypeFoundation/SAM/issues" ]

    def developers = [
            [ name: "Kees van Bochove", email: "kees@thehyve.nl" ],
            [ name: "Robert Horlings", email: "robert@thehyve.nl"],
            [ name: "Taco Steemers", email: "taco@thehyve.nl" ],
            [ name: "Tjeerd van Dijk", email: "tjeerd@thehyve.nl" ]
    ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/PhenotypeFoundation/SAM" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}

class MatrixImporterGrailsPlugin {
    // the plugin version
    def version = "0.2.5.0"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.7 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def author = "Robert Horlings"
    def authorEmail = "robert@isdat.nl"
    def title = "MatrixImporter"
    def description = '''\\
Plugin for reading files with matrix-like data structure (excel, csv etc.) into a two dimensional List
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/matrix-importer"
}

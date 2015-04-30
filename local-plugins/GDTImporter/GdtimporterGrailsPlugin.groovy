/**
 *  GDTImporter, a plugin for to import data into Grails Domain Templates
 *  Copyright (C) 2011 Tjeerd Abma et al
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  $Author$
 *  $Rev$
 *  $Date$
 */

class GdtimporterGrailsPlugin {
    def version         = "0.6.0.5"
    def grailsVersion   = "1.3.7 > *"
    def dependsOn       = [ajaxflow: "0.2.1 > *", gdt: "0.3.5 > *"]
    def pluginExcludes  = [
            "grails-app/views/error.gsp",
            "grails-app/conf/DataSource.groovy"
    ]
    def author          = "Tjeerd Abma"
    def authorEmail     = "t.w.abma@gmail.com"
    def title           = "Importer plugin for Grails Domain Templates (GDT)"
    def description     = '''\\
This plugin allows one to import data into Grails Domain Templates (GDT).
'''
    def documentation   = "https://github.com/PhenotypeFoundation/GDTImporter/blob/master/README.md"
    def license         = "APACHE"
    def issueManagement = [system: "github", url: "https://github.com/PhenotypeFoundation/GDTImporter/issues"]
    def scm             = [url: "https://github.com/PhenotypeFoundation/GDTImporter"]
    def organization    = [ name: "Phenotype Foundatiom", url: "http://phenotypefoundation.org/" ]
    def developers      = [
            [ name: "Jeroen Wesbeek", email:  "work@osx.eu" ],
            [ name: "Kees van Bochove", email: "kees@thehyve.nl" ],
            [ name: "Michael van Vliet", email: "m.s.vanvliet@lacdr.leidenuniv.nl" ],
            [ name: "Siemen Sikkema", email: "s.h.sikkema@gmail.com" ],
            [ name: "Seth Snel", email: "seth@thehyve.nl" ]
    ]

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
}

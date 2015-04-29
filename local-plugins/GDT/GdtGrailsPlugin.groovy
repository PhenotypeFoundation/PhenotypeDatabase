/**
 *  GDT, a plugin for Grails Domain Templates
 *  Copyright (C) 2011 Jeroen Wesbeek, Kees van Bochove
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

class GdtGrailsPlugin {
    def version			= "0.5.0.5"
    def grailsVersion	= "2.0.0 > *"
    def dependsOn		= [ajaxflow: "0.2.1 > *", jquery: "1.7.1 > *" ]
    def pluginExcludes	= [
            "grails-app/views/error.gsp",
            "grails-app/conf/DataSource.groovy",
            "web-app/css",
            "web-app/images",
            "web-app/js/prototype",
            "web-app/js/application.js"
    ]
    def author			= "Jeroen Wesbeek"
    def authorEmail 	= "work@osx.eu"
    def title			= "Grails Domain Templates"
    def description 	= '''\\
Grails Domain Templates allows you to dynamically extend domain classes with unlimited user definable fields based on a Template for a specific Domain Class.
If you, for example, have a Company Domain Class, but companies would like to register different fields for such an entity, several Templates could be created
for every type of company (e.g. a financial institution template, university template, etc). Using these Templateted Domain Classed creates more dynamic
flexibility in the application.
'''
    def documentation   = "https://github.com/PhenotypeFoundation/GDT/blob/master/README.md"
    def license         = "APACHE"
    def issueManagement = [system: "github", url: "https://github.com/PhenotypeFoundation/GDT/issues"]
    def scm             = [url: "https://github.com/PhenotypeFoundation/GDT"]
    def organization    = [ name: "Phenotype Foundatiom", url: "http://phenotypefoundation.org/" ]
    def developers      = [
            [ name: "Kees van Bochove", email: "kees@thehyve.nl" ],
            [ name: "Tjeerd Abma", email: "t.w.abma@gmail.com" ],
            [ name: "Siemen Sikkema", email: "s.h.sikkema@gmail.com" ],
            [ name: "Ferry Jagers", email: "ferryjagers@gmail.com" ],
            [ name: "Taco Steemers", email: "taco@thehyve.nl" ],
            [ name: "Tjeerd van Dijk", email: "tjeerd@thehyve.nl" ]
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

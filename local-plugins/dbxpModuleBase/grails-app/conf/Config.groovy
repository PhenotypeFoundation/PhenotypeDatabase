// configuration for plugin testing - will not be included in the plugin zip
 
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
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
}

// Base URL of GSCF instance
gscf.baseURL = "http://localhost:8080"

// Server URL of the module
//grails.serverURL = "http://..."

// Consumer ID of this module that is used in communication with GSCF
//module.consumerId = "http://..."

// See BaseFilters.groovy
module.defaultAuthenticationRequired = true

grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"

// Default to throw exception on failed save (tests don't respect this setting)
grails.gorm.failOnError = true
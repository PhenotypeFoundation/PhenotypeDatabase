import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.util.GrailsUtil

/**
 * Application Bootstrapper
 * @Author  Jeroen Wesbeek
 * @Since   20091021
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class BootStrap {
     def init = { servletContext ->	 
	 // check if we're in development
	 if (GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT) {
	     printf("development bootstrapping....\n\n");
	     
	 }
     }
     def destroy = {
     }
} 
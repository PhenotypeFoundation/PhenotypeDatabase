/**
 * SynchronizationService Service
 * 
 * Description of my service
 *
 * @author  your email (+name?)
 * @since	2010mmdd
 * @package	???
 *
 * Revision information:
 * $Rev: 1442 $
 * $Author: robert@isdat.nl $
 * $Date: 2011-01-26 17:02:05 +0100 (Wed, 26 Jan 2011) $
 */
package dbnp.modules

import nl.grails.plugins.gdt.*
import dbnp.studycapturing.*

class ModuleNotificationService implements Serializable {
    boolean transactional = false
	
    /**
     * Sends a notification to assay modules that some part of a study has changed.
     * 
     * Only modules that have the notify flag set to true will be notified. They will be notified on the URL
     * 
     * [moduleUrl]/rest/notifyStudyChange?studyToken=abc
     * 
     * Errors that occur when calling this URL are ignored. The module itself is responsible of
     * maintaining a synchronized state.
     * 
     * @param study
     * @return
     */
	def invalidateStudy( Study study ) {
		if( !study )
			return
			
		log.info( "Invalidate " + study.code )

		def modules = AssayModule.findByNotify(true);
		
		def urls = []
		modules.each { module ->
			urls << module.url + '/rest/notifyStudyChange?studyToken=' + study.giveUUID()
		}
		
		Thread.start { 
			urls.each { url ->
				try {
					def connection = url.toURL().openConnection()
					if( connection.responseCode == 200 ) {
						log.info( "GSCF NOTIFY-CALL SUCCEEDED: ${url}" )
					} else {
						log.info( "GSCF NOTIFY-CALL FAILED: ${url}: " + connection.responseCode )
					}
				} catch( Exception ignore) {
					log.info( "GSCF NOTIFY-CALL ERROR: ${url} - " + ignore.getMessage() )
				}
			}
		 };
    }
}

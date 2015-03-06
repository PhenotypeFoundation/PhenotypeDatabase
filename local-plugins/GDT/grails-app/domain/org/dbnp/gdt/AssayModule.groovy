package org.dbnp.gdt

/**
 * This entity describes actual dbNP submodule instances: what type of data they store, and how to reach them  
 */
class AssayModule extends Identity {
	
	/** The name of the module, for user-reference purposes  */
	String name

	/** A descriptive string describing the 'platform' of the assay data in the module */
	//String platform

	/** The URL at which the module instance is located. This is also used
	 * as a consumer parameter to identify the module in the authentication process.
	 */
	String url

    /** Set the base URL of the module, in case the normal url is a specific landing page.
     */
    String baseUrl
	
	/** Determines whe ther this module will be notified of changes in studies. This can be used
	 * to determine when synchronization should take place in a module. The URL called is
	 * 
	 * [url]/rest/notifyStudyChange?studyToken=abc
	 * 
	 * @see synchronizationService
	 */
	Boolean notify = Boolean.FALSE;

	/**
	 * Determines whether assays with this module will be opened in a frame or in fullscreen 
	 * (when opened from the study view). When the module is opened in fullscreen, it should provide
	 * a link to GSCF itself! 
	 */
	Boolean openInFrame = Boolean.TRUE;

	static constraints = {
		notify(nullable:true)
		openInFrame(nullable:false)
	}

	def String toString() {
		return name;
	}

}

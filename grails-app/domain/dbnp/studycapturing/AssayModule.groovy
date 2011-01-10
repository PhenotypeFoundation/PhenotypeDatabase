package dbnp.studycapturing

/**
 * This entity describes actual dbNP submodule instances: what type of data they store, and how to reach them  
 */
class AssayModule extends Identity {
	
	/** The name of the module, for user-reference purposes  */
	String name

	/** A descriptive string describing the 'platform' of the assay data in the module */
	String platform

	/** The base URL at which the module instance is located. This is also used
	 * as a consumer parameter to identify the module in the authentication process.
	 */
	String url
	
	/** Determines whether this module will be notified of changes in studies. This can be used 
	 * to determine when synchronization should take place in a module. The URL called is
	 * 
	 * [url]/rest/notifyStudyChange?studyToken=abc
	 * 
	 * @see synchronizationService
	 */
	boolean notify = false;

	static constraints = {
	}

	def String toString() {
		return name;
	}

}

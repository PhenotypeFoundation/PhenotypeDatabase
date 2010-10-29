package dbnp.studycapturing

/**
 * This entity describes actual dbNP submodule instances: what type of data they store, and how to reach them  
 */
class AssayModule extends Identity {
	
	/** The name of the module, for user-reference purposes  */
	String name

	/** A descriptive string describing the 'platform' of the assay data in the module */
	String platform

	/** Consumer id (e.g., OAuth consuemr key) of module where instance is located */
	String consumer 

	static constraints = {
	}

	def String toString() {
		return name;
	}

}

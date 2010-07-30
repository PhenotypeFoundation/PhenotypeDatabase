package dbnp.studycapturing

/**
 * This entity describes actual dbNP submodule instances: what type of data they store, and how to reach them  
 */
class AssayModule implements Serializable {
	
	/** The name of the module, for user-reference purposes  */
	String name

	/** The type of the module */
	AssayType type

	/** A descriptive string describing the 'platform' of the assay data in the module */
	String platform

	/** The base URL at which the module instance is located */
	String url

	static constraints = {
	}

	def String toString() {
		return name;
	}


}

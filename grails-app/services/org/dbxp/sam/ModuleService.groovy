package org.dbxp.sam

import org.dbnp.gdt.AssayModule

class ModuleService {

    static transactional = false
	
	/**
	 *
	 *
	 * 
	 * @param patterns		List with patterns to search for
	 * @param candidates	List with candidates to search in
	 * @param threshold		Threshold the matches have to be above. This input variable is either 'default', or a map whose keys can be used to look up the requested threshold value. The 'retrieveThresholdFromConfig' function will look for a value associated to this key in a map located in the configurationHolder, at 'config.fuzzyMatching.threshold'.
	 * @return				A list with each element being a map with three elements:
	 * 							pattern:	the pattern that has been matched
	 * 							candidate:	the best matching candidate for this pattern or null if no match has been found
	 * 							index:		the index of the candidate in the original list				
	 */
	static def validateModule( modulename ) {
        AssayModule.findAll().name.contains(modulename)
    }
}

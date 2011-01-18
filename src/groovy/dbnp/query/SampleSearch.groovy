/**
 * SampleSearch Domain Class
 *
 * This class provides querying capabilities for searching for samples 
 *
 * @author  Robert Horlings (robert@isdat.nl)
 * @since	20110118
 * @package	dbnp.query
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.query

class SampleSearch extends Search {
	public SampleSearch() {
		this.entity = "Sample";
	}

	/**
	 * Executes a search based on the given criteria. Should be filled in by
	 * subclasses searching for a specific entity
	 */
	@Override
	void execute() {
		
	}
}

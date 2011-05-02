/**
 * StudySearch Domain Class
 *
 * This class provides querying capabilities for searching for studies 
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

import groovy.lang.Closure;

import java.util.List;
import java.util.Map;

import dbnp.studycapturing.*
import org.dbnp.gdt.*
import org.apache.commons.logging.LogFactory;

class StudySearch extends Search {
	private static final log = LogFactory.getLog(this);

	public StudySearch() {
		super();
		this.entity = "Study";
	}

	/**
	 * Searches for studies based on the given criteria. All criteria have to be satisfied and 
	 * criteria for the different entities are satisfied as follows:
	 * 
	 * 		Study.title = 'abc'		
	 * 				All returned studies will have title 'abc'
	 * 		
	 * 		Subject.species = 'human'
	 * 				All returned studies will have one or more subjects with species = 'human'  
	 * 
	 * 		Sample.name = 'sample 1'
	 * 				All returned studies will have one or more samples with name = 'sample 1'
	 * 
	 * 		Event.startTime = '0s'
	 * 				All returned studies will have one or more events with start time = '0s'  
	 * 
	 * 		Assay.module = 'metagenomics'
	 * 				All returned studies will have one or more assays with module = 'metagenomics'  
	 *
	 * When searching the system doesn't look at the connections between different entities. This means,
	 * the system doesn't look for human subjects having a sample with name 'sample 1'. The sample 1 might
	 * as well belong to a mouse subject and still the study satisfies the criteria.
	 * 
	 * When searching for more than one criterion per entity, these are taken combined. Searching for
	 * 
	 * 		Subject.species = 'human'
	 * 		Subject.name = 'Jan'
	 * 
	 *  will result in all studies having a human subject named 'Jan'. Studies with only a mouse subject 
	 *  named 'Jan' or a human subject named 'Kees' won't satisfy the criteria. 
	 *	
	 */
	@Override
	protected void executeAnd() {
		def studies = Study.list().findAll { it.canRead( this.user ) };

		executeAnd( studies );
	}

	/**
	 * Searches for studies based on the given criteria. Only one criteria have to be satisfied and
	 * criteria for the different entities are satisfied as follows:
	 *
	 * 		Study.title = 'abc'
	 * 				The returned study will have title 'abc'
	 *
	 * 		Subject.species = 'human'
	 * 				The returned study will have one or more subjects with species = 'human'
	 *
	 * 		Sample.name = 'sample 1'
	 * 				The returned study will have one or more samples with name = 'sample 1'
	 *
	 * 		Event.startTime = '0s'
	 * 				The returned study will have one or more events with start time = '0s'
	 *
	 * 		Assay.module = 'metagenomics'
	 * 				The returned study will have one or more assays with module = 'metagenomics'
	 *
	 * When searching the system doesn't look at the connections between different entities. This means,
	 * the system doesn't look for human subjects having a sample with name 'sample 1'. The sample 1 might
	 * as well belong to a mouse subject and still the study satisfies the criteria.
	 *
	 * When searching for more than one criterion per entity, these are taken separately. Searching for
	 *
	 * 		Subject.species = 'human'
	 * 		Subject.name = 'Jan'
	 *
	 *  will result in all studies having a human subject or a subject named 'Jan'. Studies with only a 
	 *  mouse subject named 'Jan' or a human subject named 'Kees' will satisfy the criteria.
	 *
	 */
	@Override
	protected void executeOr() {
		def allStudies = Study.list().findAll { it.canRead( this.user ) };
		executeOr( allStudies );
	}

	/**
	* Returns a closure for the given entitytype that determines the value for a criterion
	* on the given object. The closure receives two parameters: the object and a criterion.
	*
	* This method should be implemented by all searches
	*
	* For example:
	* 		For a study search, the object given is a study. How to determine the value for that study of
	* 		the criterion field of type sample? This is done by returning the field values for all
	* 		samples in the study
	* 			{ study, criterion -> return study.samples?.collect { criterion.getFieldValue( it ); } }
	* @return
	*/
	protected Closure valueCallback( String entity ) {
		switch( entity ) {
			case "Study":
				return { study, criterion -> return criterion.getFieldValue( study ) }
			case "Subject":
				return { study, criterion -> return study.subjects?.collect { criterion.getFieldValue( it ); } }
			case "Sample":
				return { study, criterion -> return study.samples?.collect { criterion.getFieldValue( it ); } }
			case "Event":
				return { study, criterion -> return study.events?.collect { criterion.getFieldValue( it ); } }
			case "SamplingEvent":
				return { study, criterion -> return study.samplingEvents?.collect { criterion.getFieldValue( it ); } }
			case "Assay":
				return { study, criterion -> return study.assays?.collect { criterion.getFieldValue( it ); } }
			default:
				return super.valueCallback( entity );
		}
	}

	/**
	 * Returns the saved field data that could be shown on screen. This means, the data
	 * is filtered to show only data of the query results. Also, the study title and sample
	 * name are filtered out, in order to be able to show all data on the screen without
	 * checking further
	 *
	 * @return	Map with the entity id as a key, and a field-value map as value
	 */
	public Map getShowableResultFields() {
		Map showableFields = super.getShowableResultFields()
		showableFields.each { sampleElement ->
			sampleElement.value = sampleElement.value.findAll { fieldElement ->
				fieldElement.key != "Study title" && fieldElement.key != "Subject species"
			}
		}
		return showableFields
	}
}

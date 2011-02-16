/**
 * AssaySearch Domain Class
 *
 * This class provides querying capabilities for searching for assays 
 *
 * @author  Robert Horlings (robert@isdat.nl)
 * @since	20110118
 * @package	dbnp.query
 *
 * Revision information:
 * $Rev: 1524 $
 * $Author: robert@isdat.nl $
 * $Date: 2011-02-15 15:05:23 +0100 (Tue, 15 Feb 2011) $
 */
package dbnp.query

import groovy.lang.Closure;

import java.util.Map;

import dbnp.studycapturing.*
import org.dbnp.gdt.*
import org.apache.commons.logging.LogFactory;

class AssaySearch extends Search {
	private static final log = LogFactory.getLog(this);

	public AssaySearch() {
		super();

		this.entity = "Assay";
	}

	/**
	 * Searches for assays based on the given criteria. All criteria have to be satisfied and 
	 * criteria for the different entities are satisfied as follows:
	 * 
	 * 		Study.title = 'abc'		
	 * 				Only assays are returned from studies with title 'abc'
	 * 		
	 * 		Subject.species = 'human'
	 * 				Only assays are returned with samples from subjects with species = 'human'  
	 * 
	 * 		Sample.name = 'sample 1'
	 * 				Only assays are returned with samples with name = 'sample 1'
	 * 
	 * 		Event.startTime = '0s'
	 * 				Only assays are returned with samples from subjects that have had an event with start time = '0s'  
	 * 
	 * 		SamplingEvent.startTime = '0s'
	 * 				Only assays are returned with samples that have originated from a sampling event with start time = '0s'  
	 * 
	 * 		Assay.module = 'metagenomics'
	 * 				Only assays are returned with module = metagenomics  
	 * 
	 * When searching for more than one criterion per entity, these are taken combined. Searching for
	 * 
	 * 		Subject.species = 'human'
	 * 		Subject.name = 'Jan'
	 * 
	 *  will result in all samples from a human subject named 'Jan'. Samples from a mouse subject 
	 *  named 'Jan' or a human subject named 'Kees' won't satisfy the criteria. 
	 *	
	 */
	@Override
	protected void executeAnd() {
		def assays = Assay.list().findAll { it.parent?.canRead( this.user ) };

		executeAnd( assays );
	}

	/**
	 * Searches for samples based on the given criteria. Only one of the criteria have to be satisfied and
	 * criteria for the different entities are satisfied as follows:
	 * 
	 * 		Study.title = 'abc'		
	 * 				Only assays are returned from studies with title 'abc'
	 * 		
	 * 		Subject.species = 'human'
	 * 				Only assays are returned with samples from subjects with species = 'human'  
	 * 
	 * 		Sample.name = 'sample 1'
	 * 				Only assays are returned with samples with name = 'sample 1'
	 * 
	 * 		Event.startTime = '0s'
	 * 				Only assays are returned with samples from subjects that have had an event with start time = '0s'  
	 * 
	 * 		SamplingEvent.startTime = '0s'
	 * 				Only assays are returned with samples that have originated from a sampling event with start time = '0s'  
	 * 
	 * 		Assay.module = 'metagenomics'
	 * 				Only assays are returned with module = metagenomics  
	 *
	 * When searching for more than one criterion per entity, these are taken separately. Searching for
	 *
	 * 		Subject.species = 'human'
	 * 		Subject.name = 'Jan'
	 *
	 *  will result in all samples from a human subject or a subject named 'Jan'. Samples from a mouse subject
	 *  named 'Jan' or a human subject named 'Kees' will also satisfy the criteria.
	 *
	 */
	@Override
	void executeOr() {
		def allAssays = Assay.list().findAll { it.parent?.canRead( this.user ) }.toList();
		executeOr( allAssays );
	}

	/**
	 * Returns a closure for the given entitytype that determines the value for a criterion
	 * on the given object. The closure receives two parameters: the object and a criterion.
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
				return { assay, criterion -> return criterion.getFieldValue( assay.parent ) }
			case "Subject":
				return { assay, criterion -> return assay.samples?.parentSubject?.collect { criterion.getFieldValue( it ) } }
			case "Sample":
				return { assay, criterion -> return assay.samples?.collect { criterion.getFieldValue( it ) } }
			case "Event":
				return { assay, criterion ->
					def values = []
					assay.samples?.each { sample ->
						if( sample && sample.parentEventGroup && sample.parentEventGroup.events && sample.parentEventGroup.events.size() > 0 ) {
							values << sample.parentEventGroup.events.collect { criterion.getFieldValue( it ) };
						}
					}
					return values;
				}
			case "SamplingEvent":
				return { assay, criterion -> return assay.samples?.parentEvent?.collect { criterion.getFieldValue( it ) } }
			case "Assay":
				return { assay, criterion -> return criterion.getFieldValue( assay ) }
			default:
				return super.valueCallback( entity );
		}
	}

	/**
	 * Returns the saved field data that could be shown on screen. This means, the data 
	 * is filtered to show only data of the query results. Also, the study title and assay
	 * name are filtered out, in order to be able to show all data on the screen without
	 * checking further
	 *
	 * @return	Map with the entity id as a key, and a field-value map as value
	 */
	public Map getShowableResultFields() {
		Map showableFields = super.getShowableResultFields()
		showableFields.each { sampleElement ->
			sampleElement.value = sampleElement.value.findAll { fieldElement ->
				fieldElement.key != "Study title" && fieldElement.key != "Assay name"
			}
		}
	}
}

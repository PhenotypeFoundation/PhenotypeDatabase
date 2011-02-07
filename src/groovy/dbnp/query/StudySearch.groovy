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
	void executeAnd() {
		def studies = Study.list().findAll { it.canRead( this.user ) };

		// If no criteria are found, return all studies
		if( !criteria || criteria.size() == 0 ) {
			results = studies;
			return;
		}

		// Perform filters
		studies = filterOnStudyCriteria( studies );
		studies = filterOnSubjectCriteria( studies );
		studies = filterOnSampleCriteria( studies );
		studies = filterOnEventCriteria( studies );
		studies = filterOnSamplingEventCriteria( studies );
		studies = filterOnAssayCriteria( studies );

		studies = filterOnModuleCriteria( studies );

		// Save matches
		results = studies;
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
	void executeOr() {
		def allStudies = Study.list().findAll { it.canRead( this.user ) };

		// If no criteria are found, return all studies
		if( !criteria || criteria.size() == 0 ) {
			results = allStudies;
			return;
		}

		// Perform filters
		def studies = []
		studies = ( studies + filterOnStudyCriteria( allStudies - studies ) ).unique();
		studies = ( studies + filterOnSubjectCriteria( allStudies - studies ) ).unique();
		studies = ( studies + filterOnSampleCriteria( allStudies - studies ) ).unique();
		studies = ( studies + filterOnEventCriteria( allStudies - studies ) ).unique();
		studies = ( studies + filterOnSamplingEventCriteria( allStudies - studies ) ).unique();
		studies = ( studies + filterOnAssayCriteria( allStudies - studies ) ).unique();
		
		studies = ( studies + filterOnModuleCriteria( allStudies - studies ) ).unique();
		
		// Save matches
		results = studies;
	}

	/**
	 * Filters the given list of studies on the study criteria
	 * @param studies	Original list of studies
	 * @return			List with all studies that match the Study criteria
	 */
	protected List filterOnStudyCriteria( List studies ) {
		return filterOnTemplateEntityCriteria(studies, "Study", { study, criterion -> return criterion.getFieldValue( study ) })
	}

	/**
	 * Filters the given list of studies on the subject criteria
	 * @param studies	Original list of studies
	 * @return			List with all studies that match the Subject-criteria
	 */
	protected List filterOnSubjectCriteria( List studies ) {
		return filterOnTemplateEntityCriteria(studies, "Subject", { study, criterion ->
			return study.subjects?.collect { criterion.getFieldValue( it ); }
		})
	}

	/**
	 * Filters the given list of studies on the sample criteria
	 * @param studies	Original list of studies
	 * @return			List with all studies that match the sample-criteria
	 */
	protected List filterOnSampleCriteria( List studies ) {
		return filterOnTemplateEntityCriteria(studies, "Sample", { study, criterion ->
			return study.samples?.collect { criterion.getFieldValue( it ); }
		})
	}

	/**
	 * Filters the given list of studies on the event criteria
	 * @param studies	Original list of studies
	 * @return			List with all studies that match the event-criteria
	 */
	protected List filterOnEventCriteria( List studies ) {
		return filterOnTemplateEntityCriteria(studies, "Event", { study, criterion ->
			return study.events?.collect { criterion.getFieldValue( it ); }
		})
	}

	/**
	 * Filters the given list of studies on the sampling event criteria
	 * @param studies	Original list of studies
	 * @return			List with all studies that match the event-criteria
	 */
	protected List filterOnSamplingEventCriteria( List studies ) {
		return filterOnTemplateEntityCriteria(studies, "SamplingEvent", { study, criterion ->
			return study.samplingEvents?.collect { criterion.getFieldValue( it ); }
		})
	}

	/**
	 * Filters the given list of studies on the assay criteria
	 * @param studies	Original list of studies
	 * @return			List with all studies that match the assay-criteria
	 */
	protected List filterOnAssayCriteria( List studies ) {
		return filterOnTemplateEntityCriteria(studies, "Assay", { study, criterion ->
			return study.assays?.collect { criterion.getFieldValue( it ); }
		})
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

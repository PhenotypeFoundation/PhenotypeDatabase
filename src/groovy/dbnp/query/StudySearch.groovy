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

import dbnp.studycapturing.*
import org.dbnp.gdt.*

class StudySearch extends Search {
	public StudySearch() {
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
	void execute() {
		// TODO: check for authorization for these studies?
		def studies = Study.list();

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
}

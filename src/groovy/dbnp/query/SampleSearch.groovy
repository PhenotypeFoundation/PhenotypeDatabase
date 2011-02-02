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

import java.util.Map;

import dbnp.studycapturing.*
import org.dbnp.gdt.*
import org.apache.commons.logging.LogFactory;

class SampleSearch extends Search {
	private static final log = LogFactory.getLog(this);
	
	public SampleSearch() {
		super();
				
		this.entity = "Sample";
	}

	/**
	 * Searches for samples based on the given criteria. All criteria have to be satisfied and 
	 * criteria for the different entities are satisfied as follows:
	 * 
	 * 		Sample.title = 'abc'		
	 * 				Only samples are returned from studies with title 'abc'
	 * 		
	 * 		Subject.species = 'human'
	 * 				Only samples are returned from subjects with species = 'human'  
	 * 
	 * 		Sample.name = 'sample 1'
	 * 				Only samples are returned with name = 'sample 1'
	 * 
	 * 		Event.startTime = '0s'
	 * 				Only samples are returned from subjects that have had an event with start time = '0s'  
	 * 
	 * 		SamplingEvent.startTime = '0s'
	 * 				Only samples are returned that have originated from a sampling event with start time = '0s'  
	 * 
	 * 		Assay.module = 'metagenomics'
	 * 				Only samples are returned that have been processed in an assay with module = metagenomics  
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
	void execute() {
		super.execute();

		// If no criteria are found, return all samples
		if( !criteria || criteria.size() == 0 ) {
			results = Sample.list().findAll { it.parent?.canRead( this.user ) };
			return;
		}

		// We expect the sample criteria to be the most discriminative, and discard
		// the most samples. (e.g. by searching on sample title of sample type). For
		// that reason we first look through the list of studies. However, when the
		// user didn't enter any sample criteria, this will be an extra step, but doesn't
		// cost much time to process.
		def samples = []
		if( getEntityCriteria( 'Study' ).size() > 0 ) {
			def studies = Study.findAll().findAll { it.canRead( this.user ) };

			studies = filterOnStudyCriteria( studies );

			if( studies.size() == 0 ) {
				results = [];
				return;
			}

			def c = Sample.createCriteria()
			samples = c.list {
				'in'( 'parent', studies )
			}

			// Save data about the resulting studies in the
			// result fields array. The data that is now in the array
			// is saved based on the study id, not based on the sample id
			clearResultFields();
			saveResultFields( samples, getEntityCriteria( "Study" ), { sample, criterion ->
				return criterion.getFieldValue( sample.parent );
			});
		} else {
			samples = Sample.findAll().findAll { it.parent?.canRead( this.user ) }
		}

		samples = filterOnSubjectCriteria( samples );
		samples = filterOnSampleCriteria( samples );
		samples = filterOnEventCriteria( samples );
		samples = filterOnSamplingEventCriteria( samples );
		samples = filterOnAssayCriteria( samples );

		samples = filterOnModuleCriteria( samples );

		// Save matches
		results = samples;
	}

	/**
	 * Filters the given list of samples on the sample criteria
	 * @param samples	Original list of samples
	 * @return			List with all samples that match the Sample-criteria
	 */
	protected List filterOnStudyCriteria( List studies ) {
		return filterOnTemplateEntityCriteria(studies, "Study", { study, criterion -> return criterion.getFieldValue( study ) })
	}

	/**
	 * Filters the given list of samples on the subject criteria
	 * @param samples	Original list of samples
	 * @return			List with all samples that match the Subject-criteria
	 */
	protected List filterOnSubjectCriteria( List samples ) {
		return filterOnTemplateEntityCriteria(samples, "Subject", { sample, criterion ->
			return criterion.getFieldValue( sample.parentSubject );
		})
	}

	/**
	 * Filters the given list of samples on the sample criteria
	 * @param samples	Original list of samples
	 * @return			List with all samples that match the sample-criteria
	 */
	protected List filterOnSampleCriteria( List samples ) {
		return filterOnTemplateEntityCriteria(samples, "Sample", { sample, criterion ->
			return criterion.getFieldValue( sample );
		})
	}

	/**
	 * Filters the given list of samples on the event criteria
	 * @param samples	Original list of samples
	 * @return			List with all samples that match the event-criteria
	 */
	protected List filterOnEventCriteria( List samples ) {
		return filterOnTemplateEntityCriteria(samples, "Event", { sample, criterion ->
			if( !sample || !sample.parentEventGroup || !sample.parentEventGroup.events || sample.parentEventGroup.events.size() == 0 )
				return null

			return criterion.getFieldValue( sample.parentEventGroup.events.toList() );
		})
	}

	/**
	 * Filters the given list of samples on the sampling event criteria
	 * @param samples	Original list of samples
	 * @return			List with all samples that match the event-criteria
	 */
	protected List filterOnSamplingEventCriteria( List samples ) {
		return filterOnTemplateEntityCriteria(samples, "SamplingEvent", { sample, criterion ->
			return criterion.getFieldValue( sample.parentEvent );
		})
	}

	/**
	 * Filters the given list of samples on the assay criteria
	 * @param samples	Original list of samples
	 * @return			List with all samples that match the assay-criteria
	 */
	protected List filterOnAssayCriteria( List samples ) {
		if( !samples?.size() )
			return [];

		if( getEntityCriteria( 'Assay' ).size() == 0 )
			return samples

		// There is no sample.assays property, so we have to look for assays another way: just find
		// all assays that match the criteria
		def criteria = getEntityCriteria( 'Assay' );
		def assays = filterEntityList( Assay.list(), criteria, { assay, criterion ->
			if( !assay )
				return false

			return criterion.matchOne( assay );
		});

		// If no assays match these criteria, then no samples will match either
		if( assays.size() == 0 )
			return [];

		// Save sample data for later use
		saveResultFields( samples, criteria, { sample, criterion ->
			def sampleAssays = Assay.findByStudy( sample.parent ).findAll { it.samples?.contains( sample ) };
			if( sampleAssays && sampleAssays.size() > 0 )
				return sampleAssays.collect( criterion.getFieldValue( it ) )
			else
				return null
		});

		// Now filter the samples on whether they are attached to the filtered assays
		return samples.findAll { sample ->
			if( !sample.parent )
				return false;

			def studyAssays = assays.findAll { it.parent.equals( sample.parent ); }

			// See if this sample is present in any of the matching assays. If so,
			// this sample matches the criteria
			for( def assay in studyAssays ) {
				if( assay.samples?.contains( sample ) )
					return true;
			}

			return false;
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
				fieldElement.key != "Study title" && fieldElement.key != "Sample name"
			}
		}
	}
}

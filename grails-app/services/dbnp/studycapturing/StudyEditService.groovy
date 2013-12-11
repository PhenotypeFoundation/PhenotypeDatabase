package dbnp.studycapturing

import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder
import org.dbnp.gdt.*

class StudyEditService {

	/**
	 * Returns a proper list of data to generate a datatable with templated entities.
	 * @param params	Parameters to search
			int			offset			Display start point in the current data set.
			int			max				Number of records that the table can display in the current draw. It is expected that the number of records returned will be equal to this number, unless the server has fewer records to return.
			
			string		search			Global search field
			
			int			sortColumn		Column being sorted on (you will need to decode this number for your database)
			string		sortDirection	Direction to be sorted - "desc" or "asc".

	  		Template	template		Template for the entities to read
	 * @return			A map with all data. For example:
	 		List		entities		List with all entities
	 		int			total			Total number of records in the whole dataset (without taking search, offset and max into account)
	 		int			totalFiltered	Total number of records in the search (without taking offset and max into account)
	 		int			ids				Total list of filtered ids
	 */
    def getEntitiesForTemplate( searchParams, study, template ) {
		def output = [:]
		def query = generateHQL( searchParams, study, template )
		
		// First select the number of results
		def filteredIds = template.entity.executeQuery( "SELECT s.id FROM " + query.from + " WHERE " + query.where, query.params );
		output.totalFiltered = filteredIds.size()
		output.ids = filteredIds
		
		// Also count the total number of results in the dataset
		output.total = template.entity.countByParentAndTemplate( study, template )
		
		// Now find the results themselves
		def hql = "SELECT " + query.select + " FROM " + query.from + " WHERE " + query.where + " " + ( query.order ? " ORDER BY " + query.order : "" )
		output.entities = template.entity.executeQuery( hql, query.params, [ max: searchParams.max, offset: searchParams.offset ] )

		output
    }

	/**
	 * Generates the HQL to search
	 * @return Map	
	 * 		select
	 * 		from
	 * 		where
	 * 		order
	 * 		params
	 */
	def generateHQL( searchParams, study, template ) {
		def entity = template.entity
		def domainFields = entity.domainFields
		
		// Find the table name from the entity
		def tableName = entity.simpleName
		
		// Create an HQL query as it gives us the most flexibility in searching and ordering
		def from = tableName + " s "
		def joins = []
		def whereClause = []
		def hqlParams = [ study: study, template: template ]
		def orderBy = ""

		// First add searching
		if( searchParams.search ) {
			// With searching, retrieving the data requires joining all text and term fields
			def searchTerm = searchParams.search.toLowerCase()
			
			// Only allow for searching in textual fields
			def fieldTypesAllowed = [
				TemplateFieldType.STRING,
				TemplateFieldType.TEXT,

				TemplateFieldType.STRINGLIST,
				TemplateFieldType.EXTENDABLESTRINGLIST,
				
				TemplateFieldType.ONTOLOGYTERM,
				TemplateFieldType.TEMPLATE
			]
			
			// List of field types that have a reference to another table (and use
			// the name in the other table as value), instead of a direct value
			def fieldTypesAsReference = [
				TemplateFieldType.STRINGLIST,
				TemplateFieldType.EXTENDABLESTRINGLIST,
				TemplateFieldType.ONTOLOGYTERM,
				TemplateFieldType.TEMPLATE
			]
			
			// Domain fields are handled differently from template fields
			domainFields.each { field ->
				// Continue if this type is not allowed
				if( !( field.type in fieldTypesAllowed ) )
					return true;
					
				if( field.type in fieldTypesAsReference ) {
					def joinName = "domainField" + field.name
					joins << "s." + field.name + " as " + joinName
	
					whereClause << "lower( " + joinName + ".name ) LIKE :search"
				} else {
					whereClause << "lower( s." + field + " ) LIKE :search"
				}
					
				hqlParams[ "search" ] = "%" + searchTerm + "%"
			}
			
			template.fields.each { field ->
				// Continue if this type is not allowed
				if( !( field.type in fieldTypesAllowed ) )
					return true;

				def store = "template${field.type.casedName}Fields"
				def joinName = "templateField" + field.id
				
				joins << "s." + store + " as " + joinName + " WITH index( " + joinName + " ) = :fieldName${joinName}"
				hqlParams[ "fieldName${joinName}" ] = field.name
										
				if( field.type in fieldTypesAsReference )
					whereClause << "lower( ${joinName}.name ) LIKE :search"
				else
					whereClause << "lower( ${joinName} ) LIKE :search"
					
				hqlParams[ "search" ] = "%" + searchTerm + "%"
			}
		}
		
		// Add ordering; to determine the column to sort on
		def sortColumnIndex = searchParams.sortColumn ?: 0
		def sortOrder = searchParams.sortDirection ?: "ASC"
		
		if( sortColumnIndex != null || sortColumnIndex >= ( domainFields.size() + template.fields.size() ) ) {
			if( sortColumnIndex < domainFields.size() ) {
				def sortOn = domainFields[ sortColumnIndex ]?.name;
				orderBy = "s." + sortOn + " " + sortOrder
			} else {
				// Sort on template field: use a join in the sql
				// select * from subjects inner join template_fields sortField on ....
				def sortField = template.fields[ sortColumnIndex - domainFields.size() ]
				def store = "template${sortField.type.casedName}Fields"
				
				joins << "s." + store + " as orderJoin WITH index( orderJoin ) = :sortField"
				hqlParams[ "sortField" ] = sortField.name
				orderBy = "orderJoin " + sortOrder
			}
		}
			
		// Now build up the query, except for the SELECT part.
		if( joins )
			from += " LEFT JOIN " + joins.join( " LEFT JOIN " )
		
		def where =  "s.parent = :study AND s.template = :template"
			
		if( whereClause )
			where += " AND (" + whereClause.join( " OR " ) + ") "
			
		[
			select: "s",
			from: from,
			where: where,
			order: orderBy,
			params: hqlParams
		]
	}
	
	def putParentIntoEntity( entity, params ) {
		// Was a parentID given
		if( params.parentId ) {
			entity.parent = Study.read( params.long( 'parentId' ) )
		}
	}
		
	def putParamsIntoEntity( entity, params ) {
		// did the template change?
		if (params.get('template') && entity.template?.name != params.get('template')) {
			// set the template
			// TODO: find the template with the right entity
			entity.template = Template.findByName(params.remove('template') )
		}

		// does the study have a template set?
		if (entity.template && entity.template instanceof Template) {
			// yes, iterate through template fields
			entity.giveFields().each() {
				// and set their values
				entity.setFieldValue(it.name, params.get(it.escapedName()))
			}
		}

		return entity
	}
	
	/**
	 * Generate new samples for a newly created subjectEventGroup
	 * @param subjectEventGroup
	 * @return
	 */
	protected def generateSamples( SubjectEventGroup subjectEventGroup ) {
		def study = subjectEventGroup.parent
		
		// Make sure we have a sample for each subject in combination with each samplingevent
		subjectEventGroup.subjectGroup.subjects?.each { subject ->
			subjectEventGroup.eventGroup.samplingEventInstances?.each { samplingEventInstance ->
				createSample( study, subject, samplingEventInstance, subjectEventGroup )
			}
		}
	}
	
	/**
	 * Generate new samples for a newly created samplingEventInEventGroup
	 * @param subjectEventGroup
	 * @return
	 */
	protected def generateSamples( SamplingEventInEventGroup samplingEventInEventGroup ) {
		def study = samplingEventInEventGroup.event.parent
		def eventGroup = samplingEventInEventGroup.eventGroup
		
		// Create a new sample for this sampling event and each subject that is connected to this eventgroup
		eventGroup.subjectEventGroups?.each { subjectEventGroup ->
			subjectEventGroup.subjectGroup.subjects?.each { subject ->
				createSample( study, subject, samplingEventInEventGroup, subjectEventGroup )
			}
		}
	}

	/**
	 * Generate new samples for an updated subjectGroup
	 * @param subjectEventGroup
	 * @return
	 */
	protected def generateSamples( SubjectGroup subjectGroup ) {
		def study = subjectGroup.parent
		
		// Find all samples that reference this subjectgroup
		def criteria = Sample.createCriteria()
		def samples = criteria {
			parentSubjectEventGroup {
				eq( 'subjectGroup', subjectGroup )
			}
		}
		
		// Make sure we have a sample for each subject in combination with each samplingevent
		subjectGroup.subjects?.each { subject ->
			subjectGroup.subjectEventGroups?.each { subjectEventGroup ->
				subjectEventGroup.eventGroup.samplingEventInstances?.each { samplingEventInstance ->
					def currentSample = samples.find {
						it.parentSubject.id == subject.id &&
						it.parentEvent.id == samplingEventInstance.id
					}
					
					// If the currentSample is found, remove it from the list to be removed
					if( currentSample ) {
						log.debug "Sample generation: Sample already exists: " + currentSample
						samples -= currentSample
					} else {
						createSample( study, subject, samplingEventInstance, subjectEventGroup )
						log.debug "Sample generation: Creating new sample for subject: " + subject + " / " + samplingEventInstance
					}
				}
			}
		}
		
		// Remove samples from subjects that have been removed
		samples.each { sample ->
			log.debug "Sample generation: Deleting sample: " + sample
			study.deleteSample( sample )
		}
	}
	
	/**
	 * Creates a new sample, based on the parent properties given
	 * @param study
	 * @param subject
	 * @param samplingEventInstance
	 * @param subjectEventGroup
	 * @return	The newly created sample
	 */
	protected boolean createSample( Study study, Subject subject, SamplingEventInEventGroup samplingEventInstance, SubjectEventGroup subjectEventGroup ) {
		// Make sure we have a fresh subject instance. Otherwise, calling this method after altering the subjectgroup
		// will raise Hibernate exceptions
		subject.refresh()
		
		def currentSample = new Sample(
			parent: study,
			parentSubject: subject,
			parentEvent: samplingEventInstance,
			parentSubjectEventGroup: subjectEventGroup,
			template: samplingEventInstance.event.sampleTemplate
		);
	
		currentSample.generateName()
		study.addToSamples( currentSample )
		currentSample.save( flush: true );
		
		currentSample
	}
	
}
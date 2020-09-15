package dbnp.studycapturing

import org.dbnp.gdt.*
import grails.plugin.springsecurity.annotation.Secured
import dbnp.authentication.SecUser
import dbnp.authentication.SecUserGroup
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import grails.converters.JSON
import org.hibernate.ObjectNotFoundException

/**
 * Controller to handle adding and editing studies
 * @author robert
 */
@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class StudyEditController {

    def authenticationService
        def datatablesService
        def studyEditService
        def dataSource
        def grailsApplication

        /**
         * Instance of the validation tag library used to retrieve validation errors
         * @see getHumanReadableErrors()
         */
        def validationTagLib = new ValidationTagLib()

        def add() {
                render(view: "properties", model: [ study: new Study() ] )
        }

        def edit() {
                def study = getStudyFromRequest( params )
                render( view: "properties", model: [ study: study ] )
        }

        def finished() {
                def study = getStudyFromRequest( params )
                [study: study]
        }

        /***********************************************
         *
         * Different parts of the editing process
         *
         ***********************************************/

        /**
         * Shows the properties page to edit study details
         * @return
         */
        def properties() {
                def study = getStudyFromRequest( params )

                // If this page is posted to, handle the input
                if( study && request.post ) {
                    handleStudyProperties(study, params)

                    // If the user wants to continue to another page, validate and save the object
                    if (params._action == "save") {
                        if (validateObject(study)) {
                            study.save(flush: true)
                            flash.message = "The study details have been saved."
                            redirect controller: "study", action: "list"
                        }
                    }

                    if (params._action == "next") {
                        if (validateObject(study)) {
                            if (study.save(flush: true)) {
                                redirect action: "subjects", id: study.id
                                return
                            } else {
                                log.error "Study " + study + " could not be saved, even though it has been validated."
                                flash.error = "The study could not be saved. Please contact an administrator"
                            }
                        }
                    }
                }

                [ study: study ]
        }

        /**
         * Shows the overview page to edit subject details.
         * @return
         */
        def subjects() {
                prepareDataForDatatableView( Subject )
        }

        /**
         * Stores changes in the subject details
         * @return
         */
        def editSubjects() {
                render editEntities( "subject", Subject ) as JSON
        }

        /**
         * Shows a screen to add subjects
         * @return
         */
        def addSubjects() {
                def model = addEntities( new Subject() )

                render template: "subject", model: model
        }

        /**
         * Deletes one or more subjects
         * @return
         */
        def deleteSubjects() {
                if( !request.post ) {
                        response.status = 400
                        render "Bad Request"
                        return
                }

                def numDeleted = deleteEntities( Subject )

                if( numDeleted )
                flash.message = "" + numDeleted + " subject(s) were deleted"
                else
                flash.error= "No subjects were selected"

                redirect action: "subjects", id: params.id
        }

        /**
         * Shows the overview page to edit subject details.
         * @return
         */
        def samples() {
                prepareDataForDatatableView( Sample )
        }

        /**
         * Shows a screen to add samples
         * @return
         */
        def addSamples() {
                def model = addEntities( new Sample() )

                render template: "sample", model: model
        }

        /**
         * Stores changes in the subject details
         * @return
         */
        def editSamples() {
                render editEntities( "sample", Sample ) as JSON
        }

        /**
         * Deletes one or more samples
         * @return
         */
        def deleteSamples() {
                if( !request.post ) {
                        response.status = 400
                        render "Bad Request"
                        return
                }

                def numDeleted = deleteEntities( Sample )

                if( numDeleted )
                flash.message = "" + numDeleted + " sample(s) were deleted"
                else
                flash.error= "No samples were selected"

                redirect action: "samples", id: params.id
        }

        /**
         * Generates samples based on the study design
         * @return
         */
        def generateSamples() {
            def subjectEventGroupIds = params.list( "subjectEventGroup" )

            def result = [:]

            if ( !subjectEventGroupIds ) {
                result = [ "No event groups were checked" ]
            }
            else {

                def study = Study.read( params.id )
                def subjectEventGroups = []
                subjectEventGroupIds.each {
                    def subjectEventGroup = SubjectEventGroup.read(it as Long)

                    if( subjectEventGroup ) {
                        subjectEventGroups << subjectEventGroup
                    }
                }

                def count = studyEditService.generateSamples( study, subjectEventGroups )

                log.info( "Generated/updated ${count} samples for study ${study.id}" )
                result = [ "OK" ]
            }

            render result as JSON
        }

        /**
         * Regenerates the sample names for all samples, based on the subject and event it belongs to
         * @param id
         * @see Sample.generateName
         * @return
         */
        def regenerateSampleNames( long id ) {
            def study = Study.read( id );

            if( !study ) {
                response.status = 404
                render "Not found"
                return
            }

            def count = studyEditService.regenerateSampleNames( study )

            log.info( "Regenerated ${count} sampleNames for study ${study.id}" )
            flash.message = "Samples names have been regenerated for ${count} samples in this study."
            redirect controller: "studyEdit", action: "samples", id: id
        }


        def assays() {
                prepareDataForDatatableView( Assay )
        }

        /**
         * Shows a screen to add assays
         * @return
         */
        def addAssays() {
                def model = addEntities( new Assay() )

                render template: "assay", model: model
        }

        /**
         * Stores changes in the subject details
         * @return
         */
        def editAssays() {
                render editEntities( "assay", Assay ) as JSON
        }

        /**
         * Deletes one or more samples
         * @return
         */
        def deleteAssays() {
                if( !request.post ) {
                    response.status = 400
                    render "Bad Request"
                    return
                }

                def numIds = params.list( 'ids' )?.size()
                def numDeleted = deleteEntities( Assay )

                if( numIds ) {
                    if( numDeleted == numIds )
                        flash.message = "All " + numDeleted + " selected assay(s) were deleted"
                    else
                        flash.error = "" + numDeleted + " of the " + numIds + " selected assay(s) were deleted. The assays that could not be deleted probably contain measurements in one of the modules and can't be deleted for that reason."
                } else {
                    flash.error= "No assays were selected"
                }

                redirect action: "assays", id: params.id
        }

        /**
         * Shows a screen with assaysamples
         * @return
         */
        def assaysamples() {
                def study = getStudyFromRequest( params )
                if( !study ) {
                        redirect action: "add"
                        return
                }

                [ study: study		]
        }

        /**
         * Stores changes in the assay sample value
         * @return
         */
        def editAssaySamples() {
                def study = getStudyFromRequest( params )
                if( !study || !study.id ) {
                        response.status = 404
                        render "Study not found"
                        return
                }

                def data = getDataFromParams()
                def paramsProperty = "assay"

                if(!data[ paramsProperty ] ) {
                        // Not a big problem, apparently no entities are altered
                        log.warn "No data given while editing assaysamples"
                        render [ "OK" ] as JSON
                        return
                }

                // Only updates to the current situation are given. That could be two ways:
                // Either the value of the property resolves to true, then the sample should
                // be associated with an assay. Or the value resolves to false (is empty), then
                // the sample should be removed from the assay
                def assayIds = [] as Set
                def sampleIds = [] as Set
                def updates = [:]

                // Loop through the parameters, create a list of assay IDs and sample IDs so
                // the objects can be retrieved in a performant way. Also, build a map with
                // updates
                data[ paramsProperty ].each { sampleId, assayData->
                        // Key should be a subject ID
                        if( !sampleId.isLong() ) {
                                return;
                        }

                        def sampleIdLong = sampleId.toLong()
                        sampleIds << sampleIdLong

                        assayData.each { assayId, value ->
                            if( !assayId.isLong() ) {
                                return
                            }
                            def assayIdLong = assayId.toLong()
                            assayIds << assayIdLong

                            if( !updates[assayIdLong] )
                                updates[assayIdLong] = ["add": [], "remove": []]

                            if( value )
                                updates[assayIdLong].add << sampleIdLong
                            else
                                updates[assayIdLong].remove << sampleIdLong
                        }
                }

                // Retrieve the objects from the database
                def assays = Assay.getAll(assayIds).groupBy { it.id }

                // The database cannot handle queries that are too long (e.g. contains a list of many ids)
                // This results in the database closing the connection with status 08003 and 08006
                // For that reason, retrieve the data in batches
                def sampleBatchSize = 2500
                def batches = ( sampleIds as List ).collate(sampleBatchSize)
                def samples = []
                batches.each { batch ->
                    samples += Sample.getAll(batch)
                }

                def groupedSamples = samples.groupBy { it.id }

                // Perform the updates themselves
                def success = true
                def errors = [:]
                updates.each { assayId, assayUpdates ->
                    log.debug "Start adding samples to " + assayId
                    def assay = assays[assayId][0]

                    if( !assay ) {
                        errors[ assayId + " not found" ] = "No assay could be found with id " + assayId
                        return
                    }

                    def i = 0
                    def limit = 1000
                    assayUpdates.each { type, actionSampleIds ->
                        actionSampleIds.each { sampleId ->
                            def sample = groupedSamples[sampleId][0]
                            if( type == "add" )
                                assay.addToSamples(sample)
                            else
                                assay.removeFromSamples(sample)
                        }

                        if( i++ > limit ) {
                            assay.save(flush: true)
                            i = 0
                        }
                    }

                    log.debug "Start saving assay " + assayId
                    assay.save(flush: true)
                    log.debug "Finished saving assay " + assayId
                }

                def result
                if( success ) {
                        result = ["OK"]
                } else {
                        result = [
                                message: "Validation errors occurred",
                                errors: errors
                        ]
                }

                render result as JSON
        }

        protected def getDataFromParams( def propertyName = "data" ) {
            // Data is provided as JSON
            def providedData = JSON.parse(params[propertyName])

            // Loop through all data entries, and create a map if the name contains a dot
            def data = [:]
            providedData.each { key, value ->
                addValueToMap(key, value, data)
            }

            data
        }

        protected def addValueToMap(key, value, data) {
            if( !key )
                return

            def idx = key.indexOf( "." )

            if( idx == 0 && key.size() > 1 ) {
                // If the key starts with a dot, ignore the dot
                addValueToMap(key[1..-1], value, data)
            } else if( idx > 0 && key.size() > (idx+1) ) {
                def head = key[0..idx-1]
                def tail = key[idx+1..-1]

                if( !data[head] )
                    data[head] = [:]

                addValueToMap( tail, value, data[head])
            }

            data[key] = value
        }

        /**
         * Returns data for the assaysample datatable
         * @return
         */
        def dataTableAssaySamples() {
                def study = Study.read( params.long( "id" ) )

                if( !study ) {
                        render dataTableError( "Invalid study given: " + study ) as JSON
                        return
                }

                def searchParams = datatablesService.parseParams( params )
                def data = studyEditService.getSamplesForAssaySamplePage( searchParams, study )

                def assays = study.assays?.sort { it.name }

                render datatablesService.createDatatablesOutput( data, params, { entry ->
                                def output = entry as List
                                def sample = entry[ 0 ]

                                // Convert columns
                                output[ 0 ] = sample.id
                                output[ 6 ] = new RelTime( entry[ 6 ] ).toString()

                                // Collect values for the assays
                                def assaySamples = []
                                assays.each { assay ->
                                    def sampleIds = assay.samples*.id
                                    assaySamples << sampleIds.contains( sample.id )
                                }

                                // Generate output (the checkbox columns should be first
                                [output[0]] + assaySamples + output[1..6]
                        }) as JSON
        }

        /**
         * Returns a page without layout with the prototypes of the given template
         * @return
         */
        def prototypes() {
                if( !params.id || !params.id.isLong() ) {
                        response.status = 400
                        render "Bad request"
                        return;
                }

                def template = Template.read( params.long( 'id' ) )

                if( !template ) {
                        response.status = 404
                        render "Template not found"
                        return
                }

                render(
                        template: 'prototypes',
                        model: [ template: template ]
                )
        }

        /**
         * Adds one or more entities (Subject, Sample, Assay) to the database
         * based on the parameters given
         * 		count	Number of entities to add
         * 		id		ID of the study to add the entities to
         * 		...		Values to put into the domain and template fields of the entity
         * @param entity
         * @return
         */
        protected def addEntities( def entity ) {
                def clone = null

                studyEditService.putParentIntoEntity( entity, params )

                def num = params.remove( 'count' );
                def numEntities = num && num.isLong() ? num.toLong() : 1

                if( request.post ) {
                        def index = 0

                        studyEditService.putParamsIntoEntity( entity, params )
                        def template = entity.template

                        if( params._action == "save" ) {
                                if( entity.validate() ) {
                                        // Now we know that the entity validates, see how many entites
                                        // we have to store
                                        def originalEntityName = entity.name
                                        entity.discard();

                                        for( index = 0; index < numEntities; index++ ) {
                                            def entityToSave = entity.class.newInstance()
                                            entityToSave.template = template
                                            studyEditService.putParentIntoEntity( entityToSave, params )
                                            studyEditService.putParamsIntoEntity( entityToSave, params )

                                            // Add _<num> postfix to the entity name if multiple entities
                                            // are created to ensure uniqueness
                                            if( numEntities > 1 ) {
                                                entityToSave.name = originalEntityName + "_" + ( index + 1 )
                                            }

                                            entityToSave.save(flush:true)
                                        }

                                        // Tell the frontend the save has succeeded
                                        response.status = 210
                                        def returnJSON = [ templateId: template.id ]
                                        render returnJSON as JSON
                                }
                        }
                }

                return [entity: entity, number: numEntities ]
        }


        /**
         * Deletes one or more selected entities from the system
         * @param entityType
         * @return
         */
        protected def deleteEntities( Class entityType ) {
                if( request.post ) {
                        def ids = params.list( 'ids' )
                        def study = getStudyFromRequest( params )

                        if( ids ) {
                                def deleted = 0
                                ids.each { id ->
                                        if( id.isLong() ) {
                                                def entity = entityType.get( id.toLong() )

                                                switch( entityType ) {
                                                    case Subject:
                                                        study.deleteSubject( entity )
                                                        break
                                                    case Sample:
                                                        study.deleteSample( entity )
                                                        break
                                                    case Assay:
                                                        // Can't delete assays with measurements
                                                        if( entity.hasMeasurements() ) {
                                                            return
                                                        }
                                                        study.deleteAssay( entity )
                                                        break
                                                }
                                                deleted++
                                        }
                                }

                                return deleted
                        } else {
                                return 0
                        }
                }
        }

        /**
         * Returns data for a templated datatable. The type of entities is based on the template given.
         * @return
         */
        def dataTableEntities() {
                def template = Template.read( params.long( "template" ) )
                def study = Study.read( params.long( "id" ) )

                if( !study ) {
                        render dataTableError( "Invalid study given: " + study ) as JSON
                        return
                }

                if( !template ) {
                        render dataTableError( "Invalid template given: " + template ) as JSON
                        return
                }

                def searchParams = datatablesService.parseParams( params )

                def data = studyEditService.getEntitiesForTemplate( searchParams, study, template )

                render datatablesService.createDatatablesOutputForEntities( data, params ) as JSON
        }

        /**
         * Prepares the data for the datatable view
         * @param entityClass	Class for the type of entities to show. E.g. Subject
         * @return	a list of data to return to the view
         */
        protected def prepareDataForDatatableView( entityClass ) {
                def study = getStudyFromRequest( params )
                if( !study ) {
                        redirect action: "add"
                        return
                }

                // Check the distinct templates for these entities, without loading all
                // entities for efficiency reasons
                def templates = entityClass.executeQuery("select distinct s.template from " + entityClass.simpleName + " s WHERE s.parent = :study", [ study: study ] )

                [
                        study: study,
                        templates: templates,
                        domainFields: entityClass.domainFields
                ]

        }

        /**
         * Updates entities in the database with new properties, as entered through the templated datatable
         * @param paramsProperty	Name of the property in the HTTP request that contains changed data.
         * 							The structure of the HTTP paramaeters should be similar to this:
         * 								[propertyName].[entityID].[fieldName]=[newValue]
         * 							So the map will be like this:
         * 								[ entityID: 	[
         * 													fieldName: newValue,
         * 													otherFieldName: newValue
         *												],
         *								  otherEntityID:[
         * 													fieldName: newValue,
         * 													otherFieldName: newValue
         *												]
         * 								]
         * @param entityClass		Class for the type of entities to update. E.g. Subject
         * @return
         */
        protected def editEntities( paramsProperty, entityClass ) {
                def study = getStudyFromRequest( params )
                if( !study || !study.id ) {
                        response.status = 404
                        render "Study not found"
                        return
                }

                def data = getDataFromParams()

                if(!data[ paramsProperty ] ) {
                        // Not a big problem, apparently no entities are altered
                        log.warn "No entities given while editing " + entityClass
                        return [ "OK" ]
                }

                // Retrieve all entities at once to change something for
                def entityIds = data[paramsProperty].keySet().findAll { it.isLong() }.collect { it.toLong() }
                def entities = entityClass.getAll(entityIds).groupBy { it.id }

                // Loop over all entities
                def success = true
                def errors = [:]
                def entitiesToSave = []

                data[ paramsProperty ].each { key, newProperties ->
                        // Key should be a subject ID
                        if( !key.isLong() ) {
                                return;
                        }

                        def entity = entities[ key.toLong() ]?.get(0)

                        // If no proper subject is found, (or it belongs to another study), return
                        if( !entity || entity.parent != study ) {
                                return
                        }

                        // Store the new values into each entity field
                        entity.giveFields().each() { field ->
                                if( newProperties.containsKey( field.escapedName() ) ) {
                                        // set field
                                        entity.setFieldValue(
                                                field.name,
                                                newProperties[ field.escapedName() ]
                                        )
                                }
                        }

                        if( entity.validate() ) {
                                entitiesToSave << entity
                        } else {
                                success = false

                                entity.errors.allErrors.each { error ->
                                    if( !errors[entity.id] )
                                        errors[entity.id] = [:]

                                    errors[entity.id][ error.getArguments()[0] ] = g.message(error: error)
                                }

                                entity.discard()
                        }
                }

                log.debug( "Finished updating entities, start saving" );

                def result
                if( success ) {
                        // Save all subjects
                        entitiesToSave.each {
                            it.save(flush: true)
                        }

                        result = ["OK"]
                } else {
                        result = [
                                message: "Validation errors occurred",
                                errors: errors
                        ]
                }

                return result
        }

        /**
         * Returns an error response for the datatable
         * @param error
         * @return
         */
        protected def dataTableError( error ) {
                return [
                        sEcho: 					params.sEcho,
                        iTotalRecords: 			0,
                        iTotalDisplayRecords: 	0,
                        aaData:					[],
                        errorMessage: 			error
                ]
        }

        /**
         * Retrieves the required study from the database or return an empty Study object if
         * no id is given
         *
         * @param params	Request parameters with params.id being the ID of the study to be retrieved
         * @return			A study from the database or an empty study if no id was given
         */
        protected Study getStudyFromRequest(params) {
                SecUser user = authenticationService.getLoggedInUser();
                Study study  = (params.containsKey('id')) ? Study.findById(params.get('id')) : new Study(title: "New study", owner: user);

                // got a study?
                if (!study) {
                        flash.error = "No study found with given id";
                } else if(!study.canWrite(user)) {
                        flash.error = "No authorization to edit this study."
                        study = null;
                }

                return study;
        }

        /**
         * Handles study properties input
         * @param study		Study to update
         * @param params	Request parameter map
         * @return			True if everything went OK, false otherwise. An error message is put in flash.error
         */
        def handleStudyProperties( study, params ) {
                // did the study template change?
                if (params.get('template') && study.template?.name != params.get('template')) {
                        // set the template
                        study.template = Template.findAllByName(params.remove('template') ).find { it.entity == Study }
                }

                // does the study have a template set?
                if (study.template && study.template instanceof Template) {
                        // yes, iterate through template fields
                        study.giveFields().each() {
                                // and set their values
                                study.setFieldValue(it.name, params.get(it.escapedName()))
                        }
                }

                // handle public checkbox
                study.publicstudy = params.get("publicstudy") ? true : false

                // handle publications
                handleStudyPublications(study, params)

                // handle contacts
                handleStudyContacts(study, params)

                // handle users (readers, writers)
                handleStudyUsers(study, params, 'readers')
                handleStudyUserGroups(study, params, 'readerGroups')
                handleStudyUsers(study, params, 'writers')
                handleStudyUserGroups(study, params, 'writerGroups')

                return true
        }


        /**
         * re-usable code for handling publications form data
         * @param study	Study object to update
         * @param params GrailsParameterMap (the flow parameters = form data)
         * @returns boolean
         */
        def handleStudyPublications(Study study,  params) {
                if (study.publications) study.publications = []

                // Check the ids of the pubblications that should be attached
                // to this study. If they are already attached, keep 'm. If
                // studies are attached that are not in the selected (i.e. the
                // user deleted them), remove them
                def publicationIDs = params.get('publication_ids')
                if (publicationIDs) {
                        // Find the individual IDs and make integers
                        publicationIDs = publicationIDs.split(',').collect { Integer.parseInt(it, 10) }

                        // First remove the publication that are not present in the array
                        if( study.publications ) {
                                study.publications.findAll { publication -> !publicationIDs.find { id -> id == publication.id } }.each {
                                        study.removeFromPublications(it)
                                }
                        }

                        // Add those publications not yet present in the database
                        publicationIDs.each { id ->
                                if (!study.publications.find { publication -> id == publication.id }) {
                                        def publication = Publication.get(id)
                                        if (publication) {
                                                study.addToPublications(publication)
                                        } else {
                                                log.info('.publication with ID ' + id + ' not found in database.')
                                        }
                                }
                        }

                } else {
                        log.info('.no publications selected.')
                        if( study.publications ) {
                                study.publications.each {
                                        study.removeFromPublications(it)
                                }
                        }
                }
        }

        /**
         * re-usable code for handling contacts form data
         * @param study	Study object to update
         * @param Map GrailsParameterMap (the flow parameters = form data)
         * @return boolean
         */
        def handleStudyContacts(Study study, params) {
                if (!study.persons) study.persons = []

                // Check the ids of the contacts that should be attached
                // to this study. If they are already attached, keep 'm. If
                // studies are attached that are not in the selected (i.e. the
                // user deleted them), remove them

                // Contacts are saved as [person_id]-[role_id]
                def contactIDs = params.get('contacts_ids')
                if (contactIDs) {
                        // Find the individual IDs and make integers
                        contactIDs = contactIDs.split(',').collect {
                                def parts = it.split('-')
                                return [person: Integer.parseInt(parts[0]), role: Integer.parseInt(parts[1])]
                        }

                        // First remove the contacts that are not present in the array
                        if( study.persons ) {
                                study.persons.findAll {
                                        studyperson -> !contactIDs.find { ids -> (ids.person == studyperson.person.id) && (ids.role == studyperson.role.id) }
                                }.each {
                                        study.removeFromPersons(it)
                                        it.delete()
                                }
                        }

                        // Add those contacts not yet present in the database
                        contactIDs.each { ids ->
                                if (!study.persons.find { studyperson -> (ids.person == studyperson.person.id) && (ids.role == studyperson.role.id) }) {
                                        def person = Person.get(ids.person)
                                        def role = PersonRole.get(ids.role)
                                        if (person && role) {
                                                // Create a new StudyPerson object representing the relation, and attach it to the study
                                                // Note that because StudyPerson objects belong to a study, they can not and should not be re-used across studies
                                                def studyPerson = new StudyPerson(
                                                        person: person,
                                                        role: role
                                                )
                                                studyPerson.save(flush: true)
                                                study.addToPersons(studyPerson)
                                        } else {
                                                log.info('.person ' + ids.person + ' or Role ' + ids.role + ' not found in database.')
                                        }
                                }
                        }
                } else {
                        log.info('.no persons selected.')
                        if( study.persons ) {
                                // removing persons from study
                                // Create a clone of persons list in order to avoid
                                // concurrentModification exceptions. See http://blog.springsource.com/2010/07/02/gorm-gotchas-part-2/
                                def persons = [] + study.persons;
                                persons.each {
                                        study.removeFromPersons(it)
                                        it.delete()
                                }
                        }
                }
        }

        /**
         * re-usable code for handling contacts form data
         * @param study	Study object to update
         * @param Map GrailsParameterMap (the flow parameters = form data)
         * @param String    'readers' or 'writers'
         * @return boolean
         */
        def handleStudyUsers(Study study, params, type) {
                def users = []

                if (type == "readers" && study.readers ) {
                        users += study.readers
                } else if (type == "writers" && study.writers ) {
                        users += study.writers
                }

                // Check the ids of the contacts that should be attached
                // to this study. If they are already attached, keep 'm. If
                // studies are attached that are not in the selected (i.e. the
                // user deleted them), remove them

                // Users are saved as user_id
                def userIDs = params.get(type + '_ids')

                if (userIDs) {
                        // Find the individual IDs and make integers
                        userIDs = userIDs.split(',').collect { Long.valueOf(it, 10) }

                        // First remove the publication that are not present in the array
                        users.removeAll { user -> !userIDs.find { id -> id == user.id } }

                        // Add those publications not yet present in the database
                        userIDs.each { id ->
                                if (!users.find { user -> id == user.id }) {
                                        def user = SecUser.get(id)
                                        if (user) {
                                                users.add(user)
                                        } else {
                                                log.info('.user with ID ' + id + ' not found in database.')
                                        }
                                }
                        }

                } else {
                        log.info('.no users selected.')
                        users.clear()
                }

                if (type == "readers") {
                        if (study.readers) {
                                study.readers.clear();
                        }

                        users.each { study.addToReaders(it) }
                } else if (type == "writers") {

                        if (study.writers) {
                                study.writers.clear();
                        }

                        users.each { study.addToWriters(it) }

                }
        }

        /**
         * re-usable code for handling contacts form data
         * @param study	Study object to update
         * @param Map GrailsParameterMap (the flow parameters = form data)
         * @param String    'readerGroups' or 'writerGroups'
         * @return boolean
         */
        def handleStudyUserGroups(Study study, params, type) {

            def newUserGroupIds = params.get(type + '_ids') ? params.get(type + '_ids').split(',').collect() { it.toLong() } : []
            def currentUserGroupIds = type.equals('readerGroups') ? (study.readerGroups?.id ?: []) : (study.writerGroups?.id ?: [])

            def removeUserGroups = (currentUserGroupIds - newUserGroupIds).collect() { SecUserGroup.get(it) }
            def addUserGroups = (newUserGroupIds - currentUserGroupIds ).collect() { SecUserGroup.get(it) }

            removeUserGroups.each() { userGroup ->

                if ( type.equals('readerGroups') ) {
                    study.removeFromReaderGroups( userGroup )
                }
                else {
                    study.removeFromWriterGroups( userGroup )
                }

                study.save( flush: true )

                def userIdsInOtherGroups = (type.equals('readerGroups') ? study.readerGroups.collect { it.getUsers().id } : study.writerGroups.collect { it.getUsers().id }).flatten().unique()

                userGroup.getUsers().each() { user ->
                    // If user does not have access (of type) to study via another group, remove access
                    if ( !userIdsInOtherGroups.contains(user.id) ) {
                        if ( type.equals('readerGroups') ) {
                            study.removeFromReaders( user )
                        }
                        else {
                            study.removeFromWriters( user )
                        }
                    }
                }
            }

            addUserGroups.each() { userGroup ->

                if ( type.equals('readerGroups') ) {
                    study.addToReaderGroups( userGroup )
                }
                else {
                    study.addToWriterGroups( userGroup )
                }

                userGroup.getUsers().each() { user ->
                    if ( type.equals('readerGroups') && !study.readers.contains( user ) ) {
                        study.addToReaders( user )
                    }
                    else if ( type.equals('writerGroups') && !study.writers.contains( user ) ) {
                        study.addToWriters( user )
                    }
                }
            }

            study.save( flush: true )
        }

        /**
         * Validates an object and puts human readable errors in validationErrors variable
         * @param entity		Entity to validate
         * @return			True iff the entity validates, false otherwise
         */
        protected boolean validateObject( def entity ) {
                if( !entity.validate() ) {
                        flash.validationErrors = getHumanReadableErrors( entity )
                        return false;
                }
                return true;
        }

        /**
         * transform domain class validation errors into a human readable
         * linked hash map
         * @param object validated domain class
         * @return object  linkedHashMap
         */
        def getHumanReadableErrors(def object) {
                def errors = [:]
                object.errors.getAllErrors().each() { error ->
                        // error.codes.each() { code -> println code }

                        // generally speaking g.message(...) should work,
                        // however it fails in some steps of the wizard
                        // (add event, add assay, etc) so g is not always
                        // availably. Using our own instance of the
                        // validationTagLib instead so it is always
                        // available to us
                        errors[error.getArguments()[0]] = validationTagLib.message(error: error)
                }

                return errors
        }


    /**
     * Proxy for searching PubMed articles (or other articles from the Entrez DB).
     *
     * This proxy is needed because it is not allowed to fetch XML directly from a different
     * domain using javascript. So we have the javascript call a function on our own domain
     * and the proxy will fetch the data from Entrez
     *
     * @since       20100609
     * @param       _utility        The name of the utility, without the complete path. Example: 'esearch.fcgi'
     * @return      XML
     */
    def entrezProxy = {
            // Remove unnecessary parameters
            params.remove( "action" )
            params.remove( "controller" )

            def url = grailsApplication.config.gscf.entrez.url ?:
                    "https://eutils.ncbi.nlm.nih.gov/entrez/eutils";
            def util = params.remove( "_utility" )

            if( !util ) {
                    response.setStatus( 404, "File not found" );
                    return;
            }

            def paramString = params.collect { k, v -> k + '=' + v.encodeAsURL() }.join( '&' );

            def fullUrl = url + '/' + util + '?' + paramString;

            // Return the output of the request
            response.setContentType("text/xml; charset=UTF-8")

            response <<  new URL( fullUrl ).getText('UTF-8')

            render ""
    }
}

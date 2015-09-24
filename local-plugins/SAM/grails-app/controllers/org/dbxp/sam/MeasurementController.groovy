package org.dbxp.sam

import org.dbnp.gdt.AssayModule
import org.dbxp.matriximporter.MatrixImporter
import dbnp.studycapturing.*
import org.dbnp.gdt.RelTime
import groovy.sql.Sql

class MeasurementController {
	public static final SAMPLE_LAYOUT = "Sample layout"
	public static final SUBJECT_LAYOUT = "Subject layout"
	
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
    def fuzzySearchService
    def moduleService
    def dataSource

    def sessionFactory
    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
		// Find all measurements this user has access to
		def measurements = Measurement.giveReadableMeasurements( session.gscfUser );
        if (moduleService.validateModule(params?.module)) {
            [measurementInstanceList: measurements, measurementInstanceTotal: measurements.size(), module: params.module]
        }
        else {
            redirect(controller: 'error', action: 'notFound')
        }
    }

    def create = {
        if (moduleService.validateModule(params?.module)) {
            // If no samples are present, we can't add measurements
            def features = Feature.list();
            def samples = SAMSample.giveWritableSamples( session.gscfUser )

            if( samples.size() == 0 ) {
                redirect(action: 'noassays')
            }

            def measurementInstance = new Measurement()
            measurementInstance.properties = params

            return [measurementInstance: measurementInstance, samples: samples, features: features, module: params.module]
        }
        else {
            redirect(controller: 'error', action: 'notFound')
        }
    }

    def save = {
        def measurementInstance = Measurement.findByFeatureAndSample(Feature.get(params.feature.id), SAMSample.get(params.sample.id))
        if(measurementInstance!=null){
            bindData(measurementInstance, params)
        } else {
            measurementInstance = new Measurement( params )
        }

		// Unfortunately, grails is unable to handle double values correctly. If
		// one enters 10.20, the value of 1020.0 is stored in the database. For that
		// reason, we convert the value ourselves
		if( params.value?.isDouble() )
			measurementInstance.value = params.value as Double

        if (measurementInstance.save(flush: true)) {
            flash.message = "The measurement has been created."
            redirect(action: "show", id: measurementInstance.id, params: [module: params.module])
        }
        else {
			def features = Feature.list();
			def samples = SAMSample.giveWritableSamples( session.gscfUser )

            render(view: "create", model: [measurementInstance: measurementInstance, samples: samples, features: features], module: params.module)
        }
    }

    def show = {
        if (moduleService.validateModule(params?.module)) {
            def measurementInstance = Measurement.get(params.id)
            if (!measurementInstance) {
                flash.message = "The requested measurement could not be found."
                redirect(action: "list", params: [module: params.module])
            } else if( !measurementInstance.sample.parentAssay.parent.canRead( session.gscfUser ) ) {
                flash.message = "You are not allowed to access the requested measurement."
                redirect( action: "list", params: [module: params.module]);
            } else {
                [measurementInstance: measurementInstance, module: params.module]
            }
        }
        else {
            redirect(controller: 'error', action: 'notFound')
        }
    }

    def edit = {
        if (moduleService.validateModule(params?.module)) {
            def measurementInstance = Measurement.get(params.id)
            if (!measurementInstance) {
                flash.message = "The requested measurement could not be found."
                redirect(action: "list", params: [module: params.module])
            }
            else {
                return [measurementInstance: measurementInstance, module: params.module]
            }
        }
        else {
            redirect(controller: 'error', action: 'notFound')
        }
    }

    def update = {
        def measurementInstance = Measurement.get(params.id)
        if (measurementInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (measurementInstance.version > version) {

                    measurementInstance.errors.rejectValue("Another user has updated this feature while you were editing. Because of this, your changes have not been saved to the database.")
                    render(view: "edit", model: [measurementInstance: measurementInstance], module: params.module)
                    return
                }
            }
            measurementInstance.properties = params

			// Unfortunately, grails is unable to handle double values correctly. If
			// one enters 10.20, the value of 1020.0 is stored in the database. For that
			// reason, we convert the value ourselves
			if( params.value?.isDouble() )
				measurementInstance.value = params.value as Double

            if (!measurementInstance.hasErrors() && measurementInstance.save(flush: true)) {
                flash.message = "The measurement has been updated."
                redirect(action: "show", id: measurementInstance.id)
            }
            else {
                render(view: "edit", model: [measurementInstance: measurementInstance], module: params.module)
            }
        }
        else {
            flash.message = "The requested measurement could not be found."
            redirect(action: "list", params: [module: params.module])
        }
    }

    def delete = {
        def ids = params.list( 'ids' ).findAll { it.isLong() }.collect { it.toDouble() };

		if( !ids ) {
			response.sendError( 404 );
			return;
		}

		def numDeleted = 0;
		def numErrors = 0;
		def numNotFound = 0;

		ids.each { id ->
			def measurementInstance = Measurement.get(id)
	        if (measurementInstance) {
                def samSample = SAMSample.get(measurementInstance.sampleId)
                try {
					measurementInstance.delete(flush: true)
                    // If this was the last measurement in the SAMSample, delete it also
                    if (samSample.measurements.size() == 0) {
                        samSample.delete(flush: true)
                    }
					numDeleted++;
	            } catch (org.springframework.dao.DataIntegrityViolationException e) {
	                log.error(e)
					numErrors++;
	            }
	        }
	        else {
				numNotFound++;
	        }
		}

		if( numDeleted == 1  )
			flash.message = "1 measurement has been deleted from the database"
		if( numDeleted > 1 )
			flash.message = numDeleted + " measurements have been deleted from the database"

		flash.error = ""
		if( numNotFound == 1 )
			flash.error += "1 measurement has been deleted before."
		if( numNotFound > 1 )
			flash.error += numNotFound+ " measurements have been deleted before."

		if( numErrors == 1 )
			flash.error += "1 measurement could not be deleted. Please try again"
		if( numErrors > 1 )
			flash.error += numErrors + " measurements could not be deleted. Please try again"

		// Redirect to the assay list, because that is the only place where a
		// delete button exists.
		if( params.assayId ) {
			redirect( controller: "SAMAssay", action: "show", id: params.assayId, params: [module: params.module] )
		} else {
			redirect(action: "list", params: [module: params.module])
		}
    }


    def nofeatures = {
	    flash.message = "There are no features defined. Without features, you can't add measurements."
	    redirect( controller: 'feature', action: 'list', params: [module: params.module] );
    }

	def noassays = {
		flash.message = "You have no assays that you are allowed to edit. Without writable samples, you can't add measurements."
		redirect( controller: 'SAMAssay', action: 'list', params: [module: params.module] );
	}


	/**
	 * Deletes all measurements from the given assay
	 */
	def deleteByAssay = {
		def assayId = params.id

		if( !assayId || !assayId.isLong() ) {
			flash.error = "No assay selected"
			redirect( controller: "SAMAssay", view: "list", params: [module: params.module] );
			return;
		}

		def assay = Assay.get( assayId.toLong() );

		if( !assay ) {
			flash.error = "Incorrect assay Id given"
			redirect( controller: "SAMAssay", view: "list", params: [module: params.module]  );
			return;
		}

		if( Measurement.deleteByAssay( assay ) ) {
			flash.message = "Your measurements for assay " + assay + " have been deleted."
		} else {
			flash.error = "An error occurred while deleting measurements for this assay. Please try again or contact your system administrator."
		}

		redirect( controller: "SAMAssay", view: "list", params: [module: params.module] );
	}

}

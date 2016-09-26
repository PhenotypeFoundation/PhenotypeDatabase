package org.dbxp.sam

import dbnp.studycapturing.Assay

class SAMSampleController {
    def authenticationService


    def delete = {
        def ids = params.list( 'ids' ).findAll { it.isLong() }.collect { it.toDouble() };

        if( !ids ) {
            response.sendError( 404 );
            return;
        }

        def user = authenticationService.getLoggedInUser()

        def numDeleted = 0
        def numErrors = 0
        def numNotFound = 0
        def numNoPermission = 0

        ids.each { id ->
            def measurementInstance = Measurement.get(id)
            if (measurementInstance) {

                //Check if loggedInUser is authorized to delete the measurement
                if ( measurementInstance.sample.parentAssay.canWrite( user ) ) {
                    def samSample = SAMSample.get(measurementInstance.sampleId)
                    try {
                        measurementInstance.delete(flush: true)
                        // If this was the last measurement in the SAMSample, delete it also
                        if (samSample.measurements.size() == 0) {
                            samSample.delete(flush: true)
                        }
                        numDeleted++
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        log.error(e)
                        numErrors++
                    }
                }
                else {
                    numNoPermission++
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

        if( numNoPermission == 1 )
            flash.error += "You do not have permission to delete 1 measurement"
        if( numNoPermission > 1 )
            flash.error += "You do not have permission to delete ${numNoPermission} measurements"

        // Redirect to the assay list, because that is the only place where a
        // delete button exists.
        if( params.assayId ) {
            redirect( controller: "SAMAssay", action: "show", id: params.assayId, params: [module: params.module] )
        } else {
            redirect(action: "list", params: [module: params.module])
        }
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

        def assay = Assay.get( assayId.toLong() )

        if( !assay ) {
            flash.error = "Incorrect assay Id given"
            redirect( controller: "SAMAssay", view: "list", params: [module: params.module]  );
            return;
        }

        if ( assay.canWrite( authenticationService.getLoggedInUser() ) ) {
            //Delete measurements linked to SAMSamples followed by the SAMSamples
            if( Measurement.deleteByAssay( assay ) && SAMSample.deleteByAssay( assay ) ) {
                flash.message = "Your measurements & module samples for assay " + assay + " have been deleted."
            } else {
                flash.error = "An error occurred while deleting measurements for this assay. Please try again or contact your system administrator."
            }
        }
        else {
            flash.error = "You do not have permission to delete these measurements."
        }

        redirect( controller: "SAMAssay", view: "list", params: [module: params.module] );
    }
}

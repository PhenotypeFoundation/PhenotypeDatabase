/**
 * SimpleWizardController Controler
 *
 * Description of my controller
 *
 * @author  your email (+name?)
 * @since	2010mmdd
 * @package	???
 *
 * Revision information:
 * $Rev: 1591 $
 * $Author: robert@isdat.nl $
 * $Date: 2011-03-07 12:01:52 +0100 (Mon, 07 Mar 2011) $
 */
package dbnp.studycapturing

import org.apache.poi.ss.usermodel.DataFormatter
import org.dbnp.gdt.*
import grails.plugins.springsecurity.Secured
import dbnp.authentication.SecUser
import dbnp.importer.ImportCell
import dbnp.importer.ImportRecord
import dbnp.importer.MappingColumn

@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class SimpleController extends StudyWizardController {
	def authenticationService
	def fileService
	def importerService
	def gdtService
    def simpleService

	/**
	 * index closure
	 */
	def index = {
		redirect( action: "simpleWizard" );
	}

	def simpleWizardFlow = {
		entry {
			action{
				flow.study = simpleService.getStudyFromRequest( params )
				if (!flow.study) retrievalError()
			}
			on("retrievalError").to "handleError"
			on("success").to "study"
		}

		study {
			on("next") {
				simpleService.handleStudy( flow.study, params )
				if( !simpleService.validateObject( flow.study ) )
					error()
			}.to "decisionState"
			on("refresh") { simpleService.handleStudy( flow.study, params ) }.to "study"
			on( "success" ) { simpleService.handleStudy( flow.study, params ) }.to "study"
		}

		decisionState {
			action {
				// Create data in the flow
				flow.templates = [
							'Sample': Template.findAllByEntity( Sample.class ),
							'Subject': Template.findAllByEntity( Subject.class ),
							'Event': Template.findAllByEntity( Event.class ),
							'SamplingEvent': Template.findAllByEntity( SamplingEvent.class )
				];
				flow.encodedEntity = [
							'Sample': gdtService.encryptEntity( Sample.class.name ),
							'Subject': gdtService.encryptEntity( Subject.class.name ),
							'Event': gdtService.encryptEntity( Event.class.name ),
							'SamplingEvent': gdtService.encryptEntity( SamplingEvent.class.name )
						]

				if (flow.study.samples)
					simpleService.checkStudySimplicity(flow.study) ? existingSamples() : complexStudy()
				else
					samples()
			}
			on ("existingSamples").to "startExistingSamples"
			on ("complexStudy").to "complexStudy"
			on ("samples").to "samples"
		}
		
		startExistingSamples {
			action {
				def records = importerService.getRecords( flow.study );
				flow.records = records
				flow.templateCombinations = records.templateCombination.unique()
				
				success();
			}
			on( "success" ).to "existingSamples"
		}

		existingSamples {
			on("next") {
				simpleService.handleExistingSamples( flow.study, params, flow ) ? success() : error()
			}.to "startAssays"
			on("previous").to "study"
			on("update") {
				simpleService.handleExistingSamples( flow.study, params, flow ) ? success() : error()
			}.to "samples"

			on("skip").to "startAssays"
		}

		complexStudy {
			on("save").to "save"
			on("previous").to "study"
		}

		samples {
			on("next") {
				simpleService.handleSamples( flow.study, params, flow ) ? success() : error ()
				
				// Add domain fields for all entities
				flow.domainFields = [:]
				
				flow.templates.each { 
					if( it.value ) {
						flow.domainFields[ it.key ] = it.value[0].entity.giveDomainFields();
					}
				}
				
			}.to "columns"
			on("previous").to "returnFromSamples"
			on("study").to "study"
			on("skip").to "startAssays"
		}

		returnFromSamples {
			action {
				flow.study.samples ? existingSamples() : study();
			}
			on( "existingSamples" ).to "startExistingSamples"
			on( "study" ).to "study"
		}
		
		columns {
			on( "next" ) {
				simpleService.handleColumns( flow.study, params, flow ) ? success() : error()
			}.to "checkImportedEntities"
			on( "previous" ).to "samples" 
		}
		
		checkImportedEntities {
			action {
				// Only continue to the next page if the information entered is correct
				if( flow.imported.numInvalidEntities > 0 ) {
					missingFields();
				} else {
					// The import of the excel file has finished. Now delete the excelfile
					if( flow.excel.filename )
						fileService.delete( flow.excel.filename );
	
					flow.sampleForm = null
	
					assays();
				}
			}
			on( "missingFields" ).to "missingFields"
			on( "assays" ).to "startAssays" 
		}
		
		missingFields {
			on( "next" ) {
				if( !simpleService.handleMissingFields( flow.study, params ) )
					error();
				
				// The import of the excel file has finished. Now delete the excelfile
				if( flow.excel.filename )
					fileService.delete( flow.excel.filename );

				flow.sampleForm = null
					
			}.to "startAssays"
			on( "previous" ).to "columns"
		}
		
		startAssays {
			action {
				println "Assay: " + flow.assay
				if( !flow.assay ) 
					flow.assay = new Assay( parent: flow.study );
					
				success();
			}
			on( "success" ).to "assays"
		}
		
		assays {
			on( "next" ) { 
				simpleService.handleAssays( flow.assay, params, flow );
				if( !simpleService.validateObject( flow.assay ) )
					error();
			 }.to "overview"
			on( "skip" ) {
				// In case the user has created an assay before he clicked 'skip', it should only be kept if it
				// existed before this step
				if( flow.assay != null && !flow.assay.id ) {
					flow.remove( "assay" )
				}

			 }.to "overview"
			on( "previous" ).to "returnFromAssays"
			on("refresh") { simpleService.handleAssays( flow.assay, params, flow ); }.to "assays"
		}

		returnFromAssays {
			action {
				flow.study.samples ? existingSamples() : samples();
			}
			on( "existingSamples" ).to "existingSamples"
			on( "samples" ).to "samples"
		}
		
		overview { 
			on( "save" ).to "saveStudy" 
			on( "previous" ).to "startAssays"
		}
		
		saveStudy {
			action {
				if( flow.assay && !flow.study.assays?.contains( flow.assay ) ) {
					flow.study.addToAssays( flow.assay );
				}
				
				if( flow.study.save( flush: true ) ) {
					// Make sure all samples are attached to all assays
					flow.study.assays.each { assay ->
						def l = []+ assay.samples;
						l.each { sample ->
							if( sample )
								assay.removeFromSamples( sample );
						}
						assay.samples?.clear();
		
						flow.study.samples.each { sample ->
							assay.addToSamples( sample )
						}
					}
			
					flash.message = "Your study is succesfully saved.";
					
					finish();
				} else {
					// Remove the assay from the study again, since it is still available
					// in the session
					if( flow.assay ) {
						flow.study.removeFromAssays( flow.assay );
						flow.assay.parent = flow.study;
					}
					
					overview();
				}
			}
			on( "finish" ).to "finish"
			on( "overview" ).to "overview"
		}
		
		finish()
		
		handleError{
			redirect action: "errorPage"
		}
	}
}

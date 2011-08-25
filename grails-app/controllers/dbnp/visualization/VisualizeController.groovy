/**
 * Visualize Controller
 *
 * This controller enables the user to visualize his data
 *
 * @author  robert@thehyve.nl
 * @since	20110825
 * @package	dbnp.visualization
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.visualization

import dbnp.studycapturing.*;
import grails.converters.JSON
import org.dbnp.gdt.TemplateField

class VisualizeController {
	def authenticationService
	
	/**
	 * Shows the visualization screen
	 */
    def index = {
		[ studies: Study.giveReadableStudies( authenticationService.getLoggedInUser() )]
	}
	
	def getStudies = {
		def studies = Study.giveReadableStudies( authenticationService.getLoggedInUser() );
		
		render studies as JSON
	}
	
	def getFields = {
		def entities = [ Study, Subject, Event, SamplingEvent, Sample, Assay ]
		def fields = [];
		
		entities.each { entity ->
			def entityFields = TemplateField.findAll( "from TemplateField where entity = ?", [ entity ] );
			
			println "Entity " + entity + " -> " + entityFields
			
			def domainFields = entity.giveDomainFields();
			
			( domainFields + entityFields ).each { field ->
				fields << [ "id": field.name, "source": "GSCF", "category": entity.toString(), "name": field.name ]
			}
		}
		
		render fields as JSON
	}
	
	def getVisualizationTypes = {
		def types = [ [ "id": "barchart", "name": "Barchart"] ];
		render types as JSON
	}
	
	def getData = {
		def data = [[3,7,9,1,4,6,8,2,5]];
		
		render data as JSON
	}
	
}

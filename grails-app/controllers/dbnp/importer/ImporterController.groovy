package dbnp.importer

import dbnp.studycapturing.*
import org.dbnp.gdt.*
import grails.converters.JSON
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import grails.plugins.springsecurity.Secured
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import grails.util.GrailsUtil


/**
 * Wizard Controller
 *
 * @author Jeroen Wesbeek
 * @since 20101206
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class ImporterController {
	// the pluginManager is used to check if the Grom
	// plugin is available so we can 'Grom' development
	// notifications to the unified notifications daemon
	// (see http://www.grails.org/plugin/grom)
	def pluginManager
	def authenticationService
	def fileService
	def importerService
	def validationTagLib = new ValidationTagLib()
	def gdtService

	/**
	 * index method, redirect to the webflow
	 * @void
	 */
	def index = {
		// Grom a development message
		if (pluginManager.getGrailsPlugin('grom')) "redirecting into the webflow".grom()

		/**
		 * Do you believe it in your head?
		 * I can go with the flow
		 * Don't say it doesn't matter (with the flow) matter anymore
		 * I can go with the flow (I can go)
		 * Do you believe it in your head?
		 */
		redirect(action: 'pages')
	}

	/**
	 * WebFlow definition
	 * @void
	 */
	def pagesFlow = {
		// start the flow
		onStart {
			// Grom a development message
			if (pluginManager.getGrailsPlugin('grom')) "entering the WebFlow".grom()

			// define variables in the flow scope which is availabe
			// throughout the complete webflow also have a look at
			// the Flow Scopes section on http://www.grails.org/WebFlow
			//
			// The following flow scope variables are used to generate
			// wizard tabs. Also see common/_tabs.gsp for more information
			flow.page = 0
			flow.pages = [
				[title: 'Import file'],
				[title: 'Assign properties'],
				[title: 'Check imported data'],
				//[title: 'Imported'],
				[title: 'Done']
			]
			flow.cancel = true;
			flow.quickSave = true;

			success()
		}

		// render the main wizard page which immediately
		// triggers the 'next' action (hence, the main
		// page dynamically renders the study template
		// and makes the flow jump to the study logic)
		mainPage {
			render(view: "/importer/index")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the main Ajaxflow page (index.gsp)".grom()

				// let the view know we're in page 1
				flow.page = 1
				success()
			}
			on("next").to "pageOne"
		}

		// File import and entitie template selection page
		pageOne {
			render(view: "_page_one")
			onRender {
				log.info ".entering import wizard"
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial: pages/_page_one.gsp".grom()

				flow.page = 1
				flow.studies = Study.findAllWhere(owner: authenticationService.getLoggedInUser())
				flow.importer_fuzzymatching = "false"

				success()
			}

			on("refresh") {

				if (params.entity) {
					flash.importer_datatemplates = Template.findAllByEntity(gdtService.getInstanceByEntity(params.entity.decodeURL()))
				}

				flash.importer_params = params

				// If the file already exists an "existing*" string is added, but we don't
				// want that after a refresh of the first step in the import wizard, so remove
				// that string
				flash.importer_params.importfile = params.importfile.replace('existing*', '')
                //flash.importer_params.importfile = new XmlSlurper().parseText(flash.importer_params.importfile[flash.importer_params.importfile.indexOf('<pre')..-1]).toString()

				success()
			}.to "pageOne"

			on("next") {
				flash.wizardErrors = [:]

				flash.importer_params = params
				flash.importer_params.importfile = params.importfile.replace('existing*', '')                
                //flash.importer_params.importfile = new XmlSlurper().parseText(flash.importer_params.importfile[flash.importer_params.importfile.indexOf('<pre')..-1]).toString()

				if (params.entity) {
					flash.importer_datatemplates = Template.findAllByEntity(gdtService.getInstanceByEntity(params.entity.decodeURL()))
					def importer_entity_type = gdtService.decryptEntity(params.entity.decodeURL()).toString().split(/\./)
					flow.importer_entity_type = importer_entity_type[importer_entity_type.size()-1]
				}

				// Study selected?
				flow.importer_study = (params.study) ? Study.get(params.study.id.toInteger()) : null

				// Trying to import data into an existing study?
				if (flow.importer_study)
					if (flow.importer_study.canWrite(authenticationService.getLoggedInUser()))
						fileImportPage(flow, flash, params) ? success() : error()
					else {
						log.error ".importer wizard wrong permissions"
						this.appendErrorMap(['error': "You don't have the right permissions"], flash.wizardErrors)
						error()
					}
				else {
					fileImportPage(flow, flash, params) ? success() : error()
				}

				// put your bussiness logic (if applicable) in here
			}.to "pageTwo"
		}

		// Property to column assignment page
		pageTwo {
			render(view: "_page_two")
			onRender {
				log.info ".import wizard properties page"

				def template = Template.get(flow.importer_template_id)

				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial: pages/_page_two.gsp".grom()

				flow.importer_importmappings = ImportMapping.findAllByTemplate(template)

				flow.page = 2
				success()
			}
			on("refresh") {
				def template = Template.get(flow.importer_template_id)
				flow.importer_importmappings = ImportMapping.findAllByTemplate(template)

				// a name was given to the current property mapping, try to store it
				if (params.mappingname) {
					flash.importer_columnproperty = params.columnproperty
					propertiesSaveImportMappingPage(flow, flash, params)
				} else // trying to load an existing import mapping
				if (params.importmapping_id) {
					propertiesLoadImportMappingPage(flow, flash, params)
				}

				if (params.fuzzymatching == "true")
					flow.importer_fuzzymatching = "true" else
					flow.importer_fuzzymatching = "false"

				success()
			}.to "pageTwo"

			on("next") {
				flow.importer_fuzzymatching = "false"
				if (propertiesPage(flow, flash, params)) {
					success()
				} else {
					log.error ".import wizard, properties are set wrong"
					error()
				}
			}.to "pageThree"
			on("previous").to "pageOne"
		}

		// Mapping page
		pageThree {
			render(view: "_page_three")
			onRender {
				log.info ".import wizard mapping page"
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial pages/_page_three.gsp".grom()

				flow.page = 3
				success()
			}
			on("refresh") {
				success()
			}.to "pageThree"
			on("next") {
				if (mappingsPage(flow, flash, params)) {
					flow.page = 4
					success()
				} else {
					log.error ".import wizard mapping error, could not validate all entities"
					error()
				}
			}.to "save"
			on("previous").to "pageTwo"
		}

		// Imported data overview page
		pageFour {
			render(view: "_page_four")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial pages/_page_four.gsp".grom()

				flow.page = 4
				success()
			}
			on("next") {
				if (importedPage(flow, params)) {
					flow.page = 4
					success()
				} else {
					log.error ".import wizard imported error, something went wrong showing the imported entities"
					error()
				}
			}.to "save"
			on("previous").to "pageThree"
		}

		// Save the imported data
		save {
			action {
				// here you can validate and save the
				// instances you have created in the
				// ajax flow.
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".persisting instances to the database...".grom()

				// Always delete the uploaded file in the save step to be sure it doesn't reside there anymore
				if (GrailsUtil.environment != "test") fileService.delete(flow.importer_importedfile)

				// Save all entities
				if (saveEntities(flow, params)) {
					success()
				} else {
					log.error ".import wizard, could not save entities:\n"
					flow.page = 4
					error()
				}
			}
			on("error").to "error"
			on(Exception).to "error"
			on("success").to "finalPage"
		}

		// render errors
		error {
			render(view: "_error")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial pages/_error.gsp".grom()

				// set page to 4 so that the navigation
				// works (it is disabled on the final page)
				flow.page = 4
			}
			on("next").to "save"
			on("previous").to "pageFour"
		}

		// last wizard page
		finalPage {
			render(view: "_final_page")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial pages/_final_page.gsp".grom()
				success()
			}
			onEnd {
				// clean flow scope
				flow.clear()
			}
		}
	}

	def propertiesManager = {
		render(view: "common/_propertiesManager")
	}

	/**
	 * Return templates which belong to a certain entity type
	 *
	 * @param entity entity name string (Sample, Subject, Study et cetera)
	 * @return JSON object containing the found templates
	 */
	def ajaxGetTemplatesByEntity = {
		// fetch all templates for a specific entity
		def templates = Template.findAllByEntity(gdtService.getInstanceByEntity(params.entity.decodeURL()))

		// set output header to json
		response.contentType = 'application/json'

		// render as JSON
		render templates as JSON
	}

	/**
	 * Handle the file import page.
	 *
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean true if correctly validated, otherwise false
	 */
	boolean fileImportPage(flow, flash, params) {        
		def importedfile = fileService.get(params['importfile'])
		flow.importer_importedfile = params['importfile']
        
        if (importedfile.exists()) {
			try {
				session.importer_workbook = importerService.getWorkbook(new FileInputStream(importedfile))
			} catch (Exception e) {
				log.error ".importer wizard could not load file: " + e
				this.appendErrorMap(['error': "Wrong file (format), the importer requires an Excel file as input"], flash.wizardErrors)
				return false
			}
		}

		if (params.entity && params.template_id) {

			try {
				session.importer_workbook = importerService.getWorkbook(new FileInputStream(importedfile))
			} catch (Exception e) {
				log.error ".importer wizard could not load file: " + e
				this.appendErrorMap(['error': "Excel file required as input"], flash.wizardErrors)
				return false
			}

			def selectedentities = []

			def entityName = gdtService.decryptEntity(params.entity.decodeURL())
			def entityClass = gdtService.getInstanceByEntityName(entityName)

			// Initialize some session variables
			//flow.importer_workbook = wb // workbook object must be serialized for this to work

			flow.importer_template_id = params.template_id
			flow.importer_sheetindex = params.sheetindex.toInteger() - 1 // 0 == first sheet
			flow.importer_datamatrix_start = params.datamatrix_start.toInteger() - 1 // 0 == first row
			flow.importer_headerrow = params.headerrow.toInteger()
			flow.importer_entityclass = entityClass
			flow.importer_entity = gdtService.cachedEntities.find { it.entity == entityName }

			// Get the header from the Excel file using the arguments given in the first step of the wizard
			flow.importer_header = importerService.getHeader(session.importer_workbook,
				flow.importer_sheetindex,
				flow.importer_headerrow,
				flow.importer_datamatrix_start,
				entityClass)

			session.importer_datamatrix = importerService.getDatamatrix(
				session.importer_workbook, flow.importer_header,
				flow.importer_sheetindex,
				flow.importer_datamatrix_start,
				5)

			flow.importer_templates = Template.get(flow.importer_template_id)
			flow.importer_allfieldtypes = "true"

			return true
		}


		log.error ".importer wizard not all fields are filled in"
		this.appendErrorMap(['error': "Not all fields are filled in, please fill in or select all fields"], flash.wizardErrors)
		return false
	}

	/**
	 * Load an existing import mapping
	 *
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean true if correctly validated, otherwise false
	 */
	boolean propertiesLoadImportMappingPage(flow, flash, params) {
		def im = ImportMapping.get(params.importmapping_id.toInteger())
		im.refresh()

		im.mappingcolumns.each { mappingcolumn ->
			//def mc = new MappingColumn()
			//mc.properties = mappingcolumn.properties

			flow.importer_header[mappingcolumn.index.toInteger()] = mappingcolumn
		}
	}

	/**
	 * Save the properties as an import mapping.
	 *
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean true if correctly validated, otherwise false
	 */
	boolean propertiesSaveImportMappingPage(flow, flash, params) {
		flash.wizardErrors = [:]
		def isPreferredIdentifier = false

		// Find actual Template object from the chosen template name
		def template = Template.get(flow.importer_template_id)

		// Create new ImportMapping instance and persist it
		def im = new ImportMapping(name: params.mappingname, entity: flow.importer_entityclass, template: template).save()

		params.columnproperty.index.each { columnindex, property ->
			// Create an actual class instance of the selected entity with the selected template
			// This should be inside the closure because in some cases in the advanced importer, the fields can have different target entities			
			//def entityClass = gdtService.getInstanceByEntityName(flow.importer_header[columnindex.toInteger()].entity.getName())
			def entityObj = flow.importer_entityclass.newInstance(template: template)

			def dontimport = (property == "dontimport") ? true : false

			// Loop through all fields and find the preferred identifier
			entityObj.giveFields().each {
				isPreferredIdentifier = (it.preferredIdentifier && (it.name == property)) ? true : false
			}

			// Create new MappingColumn instance
			def mc = new MappingColumn(importmapping: im,
				name: flow.importer_header[columnindex.toInteger()].name,
				property: property,
				index: columnindex,
				entityclass: flow.importer_entityclass,
				templatefieldtype: entityObj.giveFieldType(property),
				dontimport: dontimport,
				identifier: isPreferredIdentifier)

			// Save mappingcolumn
			if (mc.validate()) {
				im.addToMappingcolumns(mc)
			}
			else {
				mc.errors.allErrors.each {
					println it
				}
			}

			// Save importmapping
			if (im.validate()) {
				try {
					im.save(flush: true)
				} catch (Exception e) {
					//getNextException
					log.error "importer wizard save importmapping error: " + e
				}
			}
			else {
				im.errors.allErrors.each {
					println it
				}
			}

		}
	}

	/**
	 * Handle the property mapping page.
	 *
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean true if correctly validated, otherwise false
	 */
	boolean propertiesPage(flow, flash, params) {
		flash.wizardErrors = [:]

		// Find actual Template object from the chosen template name
		def template = Template.get(flow.importer_template_id)

		params.columnproperty.index.each { columnindex, property ->
			// Create an actual class instance of the selected entity with the selected template
			// This should be inside the closure because in some cases in the advanced importer, the fields can have different target entities
			def entityClass = Class.forName(flow.importer_header[columnindex.toInteger()].entityclass.getName(), true, this.getClass().getClassLoader())
			def entityObj = entityClass.newInstance(template: template)

			// Store the selected property for this column into the column map for the ImporterService
			flow.importer_header[columnindex.toInteger()].property = property

			// Look up the template field type of the target TemplateField and store it also in the map
			flow.importer_header[columnindex.toInteger()].templatefieldtype = entityObj.giveFieldType(property)

			// Is a "Don't import" property assigned to the column?
			flow.importer_header[columnindex.toInteger()].dontimport = (property == "dontimport") ? true : false

			//if it's an identifier set the mapping column true or false
			entityObj.giveFields().each {
				(it.preferredIdentifier && (it.name == property)) ? flow.importer_header[columnindex.toInteger()].identifier = true : false
			}
		}

		// Import the workbook and store the table with entity records and store the failed cells
		def (table, failedcells) = importerService.importData(flow.importer_template_id,
			session.importer_workbook,
			flow.importer_sheetindex,
			flow.importer_datamatrix_start,
			flow.importer_header)

		flow.importer_importeddata = table

		// loop through all entities to validate them and add them to wizardErrors flash when invalid
		/*table.each { record ->
					record.each { entity ->
						if (!entity.validate()) {
						this.appendErrors(entity, flash.wizardErrors, 'entity_' + entity.getIdentifier() + '_')
						}
					}
				}*/

		flow.importer_failedcells = failedcells

		return true
	}

	/**
	 * Handle the mapping page.
	 *
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean true if correctly validated, otherwise false
	 */
	boolean mappingsPage(flow, flash, params) {
		flash.wizardErrors = [:]
		flow.importer_invalidentities = 0

		flow.importer_importeddata.each { table ->
			table.each { entity ->
				def invalidfields = 0

				// Set the fields for this entity by retrieving values from the params
				entity.giveFields().each { field ->

					// field is a date field, try to set it with the value, if someone enters a non-date value it throws
					// an error, this should be caught to prevent a complete breakdown
					if (field.type == org.dbnp.gdt.TemplateFieldType.DATE) {
						try {
							entity.setFieldValue(field.toString(), params["entity_" + entity.getIdentifier() + "_" + field.escapedName()])
						} catch (Exception e) {
							log.error ".importer wizard could not set date field with value: " +
								params["entity_" + entity.getIdentifier() + "_" + field.escapedName()]
						}
					} else

					// field of type ontology and value "#invalidterm"?
					if (field.type == org.dbnp.gdt.TemplateFieldType.ONTOLOGYTERM &&
						params["entity_" + entity.getIdentifier() + "_" + field.escapedName()] == "#invalidterm"
					) {
						invalidfields++
					} else
					if (field.type == org.dbnp.gdt.TemplateFieldType.ONTOLOGYTERM &&
						params["entity_" + entity.getIdentifier() + "_" + field.escapedName()] != "#invalidterm") {
						if (entity) removeFailedCell(flow.importer_failedcells, entity, field)
						entity.setFieldValue(field.toString(), params["entity_" + entity.getIdentifier() + "_" + field.escapedName()])
					}
					else

					if (field.type == org.dbnp.gdt.TemplateFieldType.STRINGLIST &&
						params["entity_" + entity.getIdentifier() + "_" + field.escapedName()] != "#invalidterm") {
						if (entity) removeFailedCell(flow.importer_failedcells, entity, field)
						entity.setFieldValue(field.toString(), params["entity_" + entity.getIdentifier() + "_" + field.escapedName()])
					} else
					if (field.type == org.dbnp.gdt.TemplateFieldType.STRINGLIST &&
						params["entity_" + entity.getIdentifier() + "_" + field.escapedName()] == "#invalidterm"
					) {
						invalidfields++
					} else

						entity.setFieldValue(field.toString(), params["entity_" + entity.getIdentifier() + "_" + field.escapedName()])
				}

				// Determine entity class and add a parent (defined as Study in first step of wizard)
				switch (entity.getClass()) {
					case [Subject, Sample, Event]: entity.parent = flow.importer_study
				}

				// Try to validate the entity now all fields have been set
				if (!entity.validate() || invalidfields) {
					flow.importer_invalidentities++

					// add errors to map
					this.appendErrors(entity, flash.wizardErrors, "entity_" + entity.getIdentifier() + "_")

					entity.errors.getAllErrors().each() {
						log.error ".import wizard imported validation error:" + it
					}
				} else {
					//removeFailedCell(flow.importer_failedcells, entity)
				} // end else if

			} // end of record
		} // end of table

		return (flow.importer_invalidentities == 0) ? true : false
	} // end of method

	/**
	 * @param failedcell failed ontology cells
	 * @param entity entity to remove from the failedcells list
	 */
	def removeFailedCell(failedcells, entity, field) {
		// Valid entity, remove it from failedcells
		def entityidfield = "entity_" + entity.getIdentifier() + "_" + field.name.toLowerCase()

		failedcells.each { record ->
			def tempimportcells = []


			record.importcells.each { cell ->
				// remove the cell from the failed cells session
				if (cell.entityidentifier != entityidfield) {
					//record.removeFromImportcells(cell)
					tempimportcells.add(cell)
				}
			}

			record.importcells = tempimportcells
			// } // end of importcells
		} // end of failedcells
	}

	/**
	 * Handle the imported entities page.
	 *
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean true if correctly validated, otherwise false
	 */
	boolean importedPage(flow, params) {
		return true
	}

	boolean saveEntities(flow, params) {
		//def (validatedSuccesfully, updatedEntities, failedToPersist) =
		try {
			importerService.saveDatamatrix(flow.importer_study, flow.importer_entity_type, flow.importer_importeddata, authenticationService, log)
		} catch (Exception e) {
			log.error ".import wizard saveEntities error\n" + e.dump()
			return false
		}

		//flow.importer_validatedsuccesfully = validatedSuccesfully
		//flow.importer_failedtopersist = failedToPersist
		//flow.imported_updatedentities = updatedEntities
		//flow.importer_totalrows = flow.importer_importeddata.size
		//flow.importer_referer = ""

		return true
	}

	/**
	 * append errors of a particular object to a map
	 * @param object
	 * @param map linkedHashMap
	 * @void
	 */
	def appendErrors(object, map) {
		this.appendErrorMap(getHumanReadableErrors(object), map)
	}

	def appendErrors(object, map, prepend) {
		this.appendErrorMap(getHumanReadableErrors(object), map, prepend)
	}

	/**
	 * append errors of one map to another map
	 * @param map linkedHashMap
	 * @param map linkedHashMap
	 * @void
	 */
	def appendErrorMap(map, mapToExtend) {
		map.each() {key, value ->
			mapToExtend[key] = ['key': key, 'value': value, 'dynamic': false]
		}
	}

	def appendErrorMap(map, mapToExtend, prepend) {
		map.each() {key, value ->
			mapToExtend[prepend + key] = ['key': key, 'value': value, 'dynamic': true]
		}
	}

	/**
	 * transform domain class validation errors into a human readable
	 * linked hash map
	 * @param object validated domain class
	 * @return object  linkedHashMap
	 */
	def getHumanReadableErrors(object) {
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

}

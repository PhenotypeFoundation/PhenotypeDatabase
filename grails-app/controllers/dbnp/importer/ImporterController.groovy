package dbnp.importer
import dbnp.studycapturing.Study
import dbnp.studycapturing.Subject
import dbnp.studycapturing.Sample
import dbnp.studycapturing.Event
import dbnp.studycapturing.Template

import org.apache.poi.ss.usermodel.Workbook

import grails.converters.JSON
import cr.co.arquetipos.crypto.Blowfish

import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib

/**
 * Wizard Controller
 *
 * @author	Jeroen Wesbeek
 * @since	20101206
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class ImporterController {
    // the pluginManager is used to check if the Grom
    // plugin is available so we can 'Grom' development
    // notifications to the unified notifications daemon
    // (see http://www.grails.org/plugin/grom)
    def pluginManager
    def AuthenticationService
    def fileService
    def ImporterService
    def validationTagLib = new ValidationTagLib()
	
    /**
     * index method, redirect to the webflow
     * @void
     */
    def index = {
        // Grom a development message
        if (pluginManager.getGrailsPlugin('grom')) "redirecting into the webflow".grom()

        // encrypt the importable entities
        grailsApplication.config.gscf.domain.importableEntities.each {
            it.value.encrypted =            
            URLEncoder.encode(Blowfish.encryptBase64(
                it.value.entity.toString().replaceAll(/^class /, ''),
                grailsApplication.config.crypto.shared.secret
            ))
        }

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
                [title: 'Properties'],
                [title: 'Mappings'],
                [title: 'Imported'],
                [title: 'Persist']
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
                flow.studies = Study.findAllWhere(owner:AuthenticationService.getLoggedInUser())
                flow.importer_importableentities = grailsApplication.config.gscf.domain.importableEntities
                
                success()
            }
            on("next") {
                // Study selected?
                flow.importer_study = (params.study) ? Study.get(params.study.id.toInteger()) : null

                // Trying to import a new study?
                if (flow.importer_study)
                    if (flow.importer_study.canWrite(AuthenticationService.getLoggedInUser())) {
                        if (fileImportPage(flow, params)) {
                            success()
                        } else {
                            error.log ".importer wizard not all fields are filled in"
                            error()
                        }
                    } else
                    {
                        error.log ".importer wizard wrong permissions"
                    }
                else {
                   if (fileImportPage(flow, params)) {
                            success()
                        } else {
                            error.log ".importer wizard not all fields are filled in"
                            error()
                        }
                }
                
                // put your bussiness logic (if applicable) in here
            }.to "pageTwo"
            on("toPageTwo") {
                // put your bussiness logic (if applicable) in here
            }.to "pageTwo"
            on("toPageThree") {
                // put your bussiness logic (if applicable) in here
            }.to "pageThree"
            on("toPageFour") {
                // put your bussiness logic (if applicable) in here
            }.to "pageFour"
            on("toPageFive") {
                // put your bussiness logic (if applicable) in here
                flow.page = 5
            }.to "save"
        }

        // Property to column assignment page
        pageTwo {            
            render(view: "_page_two")
            onRender {
                log.info ".import wizard properties page"
                // Grom a development message
                if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial: pages/_page_two.gsp".grom()
                
                flow.page = 2
                success()
            }
            on("next") {
                if (propertiesPage(flow, params)) {                    
                    success()
                } else {
                    println "properties are wrong"
                    error()
                }
            }.to "pageThree"
            on("previous").to "pageOne"
            on("toPageOne").to "pageOne"
            on("toPageThree").to "pageThree"
            on("toPageFour").to "pageFour"
            on("toPageFive") {
                flow.page = 5
            }.to "save"
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
                    success()
                } else {
                    log.error ".import wizard mapping error, could not validate all entities"
                    error()
                }
            }.to "pageFour"
            on("previous").to "pageTwo"
            on("toPageOne").to "pageOne"
            on("toPageTwo").to "pageTwo"
            on("toPageFour").to "pageFour"
            on("toPageFive") {
                flow.page = 5
            }.to "save"
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
                    success()
                } else {
                    log.error ".import wizard imported error, something went wrong showing the imported entities"
                    error()
                }
                flow.page = 5
            }.to "save"
            on("previous").to "pageThree"
            on("toPageOne").to "pageOne"
            on("toPageTwo").to "pageTwo"
            on("toPageThree").to "pageThree"
            on("toPageFive") {
                flow.page = 5
            }.to "save"
        }

        // Save the imported data
        save {
            action {
                // here you can validate and save the
                // instances you have created in the
                // ajax flow.
                try {
                    // Grom a development message
                    if (pluginManager.getGrailsPlugin('grom')) ".persisting instances to the database...".grom()                    

                    if (saveEntities(flow, params)) {                        
                        success()
                    } else {
                        log.error ".import wizard imported error, something went wrong showing the imported entities"                        
                        //throw Exception
                    }                    
                } catch (Exception e) {
                    // put your error handling logic in
                    // here                    
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
            on("toPageOne").to "pageOne"
            on("toPageTwo").to "pageTwo"
            on("toPageThree").to "pageThree"
            on("toPageFour").to "pageFour"
            on("toPageFive").to "save"
        }

        // last wizard page
        finalPage {
            render(view: "_final_page")
            onRender {
                // Grom a development message
                if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial pages/_final_page.gsp".grom()
				
                success()
            }
        }
    }

    /**
     * Return templates which belong to a certain entity type
     *
     * @param entity entity name string (Sample, Subject, Study et cetera)
     * @return JSON object containing the found templates
     */
    def ajaxGetTemplatesByEntity = {        
        def entityName = Blowfish.decryptBase64(            
            URLDecoder.decode(params.entity),
            grailsApplication.config.crypto.shared.secret
        )

        //def entityClass = grailsApplication.config.gscf.domain.importableEntities.get(params.entity).entity
        def entityClass = entityName

        // fetch all templates for a specific entity
        def templates = Template.findAllByEntity(Class.forName(entityClass, true, this.getClass().getClassLoader()))

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
    boolean fileImportPage(flow, params) {
        def importedfile = fileService.get(params['importfile'])
        //fileService.delete(YourFile)

        if (params.entity && params.template_id && importedfile.exists()) {
            // create a workbook instance of the file
            session.importer_workbook = ImporterService.getWorkbook(new FileInputStream(importedfile))
            
            def selectedentities = []
            
            def entityName = Blowfish.decryptBase64(
                URLDecoder.decode(params.entity),
                grailsApplication.config.crypto.shared.secret
            )
            
            def entityClass = Class.forName(entityName, true, this.getClass().getClassLoader())
            
            // Initialize some session variables
            //flow.importer_workbook = wb // workbook object must be serialized for this to work            
             
                flow.importer_template_id = params.template_id
                flow.importer_sheetindex = params.sheetindex.toInteger() -1 // 0 == first sheet
                flow.importer_datamatrix_start = params.datamatrix_start.toInteger() -1 // 0 == first row
                flow.importer_headerrow = params.headerrow.toInteger()

                // Get the header from the Excel file using the arguments given in the first step of the wizard
                flow.importer_header = ImporterService.getHeader(session.importer_workbook,
                    flow.importer_sheetindex,
                    flow.importer_headerrow,
                    flow.importer_datamatrix_start,
                    entityClass)

                // Initialize 'selected entities', used to show entities above the columns
                flow.importer_header.each {
                    selectedentities.add([name:entityName, columnindex:it.key.toInteger()])
                }
                
                flow.importer_selectedentities = selectedentities                

                session.importer_datamatrix = ImporterService.getDatamatrix(
                            session.importer_workbook, flow.importer_header,
                            flow.importer_sheetindex,
                            flow.importer_datamatrix_start,
                            5)
              
                flow.importer_templates = Template.get(flow.importer_template_id)
                flow.importer_allfieldtypes = "true"     
            /*else {
                render (template:"common/error",
                    model:[error:"Wrong permissions: you are not allowed to write to the study you selected (${flow.importer_study})."])
            }*/

            return true
        }
    }

    /**
     * Handle the property mapping page.
     *
     * @param Map LocalAttributeMap (the flow scope)
     * @param Map GrailsParameterMap (the flow parameters = form data)
     * @returns boolean true if correctly validated, otherwise false
     */
    boolean propertiesPage (flow, params) {
        // Find actual Template object from the chosen template name
        def template = Template.get(flow.importer_template_id)

        params.columnproperty.index.each { columnindex, property ->
            // Create an actual class instance of the selected entity with the selected template
            // This should be inside the closure because in some cases in the advanced importer, the fields can have different target entities
            def entityClass = Class.forName(flow.importer_header[columnindex.toInteger()].entity.getName(), true, this.getClass().getClassLoader())
            def entityObj = entityClass.newInstance(template:template)

            // Store the selected property for this column into the column map for the ImporterService
            flow.importer_header[columnindex.toInteger()].property = property

            // Look up the template field type of the target TemplateField and store it also in the map
            flow.importer_header[columnindex.toInteger()].templatefieldtype = entityObj.giveFieldType(property)

            // Is a "Don't import" property assigned to the column?
            flow.importer_header[columnindex.toInteger()].dontimport = (property=="dontimport") ? true : false

            //if it's an identifier set the mapping column true or false
            entityObj.giveFields().each {
                (it.preferredIdentifier && (it.name==property)) ? flow.importer_header[columnindex.toInteger()].identifier = true : false
            }
        }

        // Import the workbook and store the table with entity records and store the failed cells
        def (table, failedcells) = ImporterService.importData(flow.importer_template_id,
                                                              session.importer_workbook,
                                                              flow.importer_sheetindex,
                                                              flow.importer_datamatrix_start,
                                                              flow.importer_header)

        flow.importer_importeddata = table        
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
                def invalidontologies = 0

                // Set the fields for this entity by retrieving values from the params
                entity.giveFields().each { field ->                    
                        // field of type ontology and value "#invalidterm"?
                        if (field.type == dbnp.studycapturing.TemplateFieldType.ONTOLOGYTERM &&
                            params["entity_" + entity.getIdentifier() + "_" + field.escapedName()] == "#invalidterm"
                        ) {
                            invalidontologies++
                        } else
                        if (field.type == dbnp.studycapturing.TemplateFieldType.ONTOLOGYTERM &&
                            params["entity_" + entity.getIdentifier() + "_" + field.escapedName()] != "#invalidterm") {
                            removeFailedCell(flow.importer_failedcells, entity)
                            entity.setFieldValue (field.toString(), params["entity_" + entity.getIdentifier() + "_" + field.escapedName()])
                        }
                        else
                            entity.setFieldValue (field.toString(), params["entity_" + entity.getIdentifier() + "_" + field.escapedName()])
                }

                // Determine entity class and add a parent (defined as Study in first step of wizard)
                switch (entity.getClass()) {
                    case [Subject, Sample, Event]:   entity.parent = flow.importer_study
                }

                // Try to validate the entity now all fields have been set
                if (!entity.validate() || invalidontologies) {
                    flow.importer_invalidentities++

                    // add errors to map                    
                    this.appendErrors(entity, flash.wizardErrors, "entity_"+entity.getIdentifier() + "_")
                    
					entity.errors.getAllErrors().each() {
						log.error ".import wizard imported validation error:" + it
					}
                } else {                    
                    removeFailedCell(flow.importer_failedcells, entity)
                } // end else if

            } // end of record
        } // end of table

        return (flow.importer_invalidentities == 0) ? true : false
    } // end of method

    /**
     * @param failedcell failed ontology cells
     * @param entity entity to remove from the failedcells list
     */
    def removeFailedCell(failedcells, entity) {        
        // Valid entity, remove it from failedcells
        failedcells.each { record ->
            record.importcells.each { cell ->
            // remove the cell from the failed cells session
                if (cell.entityidentifier == entity.getIdentifier()) {
                    //record.removeFromImportcells(cell)
                    cell.remove()
                }
            } // end of importcells
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
            def (validatedSuccesfully, updatedEntities, failedToPersist) = ImporterService.saveDatamatrix(flow.importer_study, flow.importer_importeddata)

            flow.importer_validatedsuccesfully = validatedSuccesfully
            flow.importer_failedtopersist = failedToPersist
            flow.imported_updatedentities = updatedEntities
            flow.importer_totalrows = flow.importer_importeddata.size
            flow.importer_referer = ""

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
            println "errors is " + error

			// generally speaking g.message(...) should work,
			// however it fails in some steps of the wizard
			// (add event, add assay, etc) so g is not always
			// availably. Using our own instance of the
			// validationTagLib instead so it is always
			// available to us
			errors[ error.getArguments()[0] ] = validationTagLib.message(error: error)
		}

		return errors
	}
}

/**
 * Importer controller
 *
 * The importer controller handles the uploading of tabular, comma delimited and Excel format
 * based files. When uploaded a preview is shown of the data and the user can adjust the column
 * type. Data in cells which don't correspond to the specified column type will be represented as "#error".
 *
 * The importer controller catches the actions and consecutively performs the
 * logic behind it.
 *
 * @package	importer
 * @author	t.w.abma@umcutrecht.nl
 * @since	20100126
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

package dbnp.importer

import dbnp.studycapturing.Template
import dbnp.studycapturing.Study
import dbnp.studycapturing.Subject
import dbnp.studycapturing.Event

import dbnp.studycapturing.Sample
import dbnp.studycapturing.TemplateFieldType
import dbnp.studycapturing.TemplateField
import grails.converters.JSON
import org.apache.poi.hssf.usermodel.HSSFWorkbook

class ImporterController {
    def ImporterService    

    /**
     * Default page
     **/
    def index = { 	
    }

    def simpleWizard = {
	render(view:"index_simple", model:[studies:Study.list(), entities: grailsApplication.config.gscf.domain.importableEntities])
    }

    def advancedWizard = {
	render(view:"index_advanced", model:[templates:Template.list()])
    }

    /**
    * This method will move the uploaded file to a temporary path and send the header
    * and the first n rows to the preview
    * @param importfile uploaded file to import
    * @param study.id study identifier
    */
    def upload_advanced = {
	def wb = handleUpload('importfile')

	session.importer_header = ImporterService.getHeader(wb, 0)
	session.importer_study = Study.get(params.study.id.toInteger())
	session.importer_template_id = params.template_id
	session.importer_workbook = wb

        render (view:"step1_advanced", model:[header:session.importer_header, datamatrix:ImporterService.getDatamatrix(wb, session.importer_header, 0, 5)])
    }

    /**
    * This method will move the uploaded file to a temporary path and send the header
    * and the rows to the postview
    *
    * @param importfile uploaded file to import
    * @param entity string representation of the entity chosen
    */
    def upload_simple = {
	def wb = handleUpload('importfile')
	def selectedentities = []
	def entity = grailsApplication.config.gscf.domain.importableEntities.get(params.entity).entity
	def entityClass = Class.forName(entity, true, this.getClass().getClassLoader())

	session.importer_header = ImporterService.getHeader(wb, 0, entityClass)
	session.importer_study = Study.get(params.study.id.toInteger())
	session.importer_template_id = params.template_id
	session.importer_workbook = wb

	session.importer_header.each {	    
	    selectedentities.add([name:params.entity, columnindex:it.key.toInteger()])
	}

	def templates = Template.get(session.importer_template_id)
	
	render(view:"step2_simple", model:[entities: selectedentities, header:session.importer_header, datamatrix:ImporterService.getDatamatrix(wb, session.importer_header, 0, 5), templates:templates])
    }

    /**
     * This method handles a file being uploaded and storing it in a temporary directory
     * and returning a workbook
     *
     * @param formfilename name used for the file field in the form
     * @return workbook object reference
     */
    private HSSFWorkbook handleUpload(formfilename) {

	def downloadedfile = request.getFile(formfilename);
        def tempfile = new File(System.getProperty('java.io.tmpdir') + File.separatorChar + System.currentTimeMillis() + ".nmcdsp")
        downloadedfile.transferTo(tempfile)

	return ImporterService.getWorkbook(new FileInputStream(tempfile))
    }

    /**
     * @param entity entity class we are using (dbnp.studycapturing.Subject etc.)
     */

    def saveMissingProperties = {
	println params.entity
	
	session.importer_importeddata.each { table ->
	    table.each { entity ->
		entity.giveFields().each { field ->
		    //print ":" + params["entity_" + entity.hashCode() + "_" + field.escapedName()]
		    entity.setFieldValue (field.toString(), params["entity_" + entity.hashCode() + "_" + field.escapedName()])
		}		
	    }
	}

	render(view:"step3", model:[datamatrix:session.importer_importeddata])	
    }

    /**
    * User has assigned all entities and templatefieldtypes to the columns and continues to the next step (assigning properties to columns)
    * All information of the columns is stored in a session as MappingColumn object
    *
    * @param entities list of entities and columns it has been assigned to (columnindex.entitytype)
    * @param templatefieldtype list of celltypes and columns it has been assigned to (columnindex:templatefieldtype format)
    * @return properties page
    *
    * @see celltype: http://poi.apache.org/apidocs/org/apache/poi/ss/usermodel/Cell.html
    */
    def savePreview = {
	def tft = null	
	def identifiercolumnindex = (params.identifier!=null) ? params.identifier.toInteger() : -1
	def selectedentities = []

	// loop all entities and see which column has been assigned which entitytype
	// and build an array containing the selected entities
	params.entity.index.each { columnindex, entityname ->
	    def _entity = [name:entityname,columnindex:columnindex.toInteger()]
	    selectedentities.add(_entity)
	}

	params.templatefieldtype.index.each { columnindex, _templatefieldtype ->
	    switch (_templatefieldtype) {
		case "STRING"	    : tft = TemplateFieldType.STRING
				      break
		case "TEXT"	    : tft = TemplateFieldType.TEXT
				      break
		case "INTEGER"	    : tft = TemplateFieldType.INTEGER
				      break
		case "FLOAT"	    : tft = TemplateFieldType.FLOAT
				      break
		case "DOUBLE"	    : tft = TemplateFieldType.DOUBLE
				      break
		case "STRINGLIST"   : tft = TemplateFieldType.STRINGLIST
				      break
		case "ONTOLOGYTERM" : tft = TemplateFieldType.ONTOLOGYTERM
				      break
		case "DATE"	    : tft = TemplateFieldType.DATE
				      break
		default: break
	    }
	    
	    session.importer_header[columnindex.toInteger()].templatefieldtype = tft
	}

	params.entity.index.each { columnindex, entityname ->
	    Class clazz	= null

	    switch (entityname) {
		case "Study"	: clazz = dbnp.studycapturing.Study
			break
		case "Subject"	: clazz = dbnp.studycapturing.Subject
			break
		case "Event"	: clazz = dbnp.studycapturing.Event
			break
		case "Protocol" : clazz = dbnp.studycapturing.Protocol
			break
		case "Sample"	: clazz = dbnp.studycapturing.Sample
			break
		default: clazz = Object
			break
	    }

	    session.importer_header[columnindex.toInteger()].identifier = (columnindex.toInteger() == identifiercolumnindex) ? true : false
	    session.importer_header[columnindex.toInteger()].index = columnindex.toInteger()
	    session.importer_header[columnindex.toInteger()].entity = clazz
	}

	// currently only one template is used for all entities
	// TODO: show template fields per entity
	
	def templates = Template.get(session.importer_template_id)

	render(view:"step2", model:[entities:selectedentities, header:session.importer_header, templates:templates])
    }

    /**
    * @param columnproperty array of columns containing index and property (represented as a String)
    *
    */
    def saveProperties = {

	// Find actual Template object from the chosen template name
	def template = Template.get(session.importer_template_id)

	params.columnproperty.index.each { columnindex, property ->

		// Create an actual class instance of the selected entity with the selected template
		// This should be inside the closure because in some cases in the advanced importer, the fields can have different target entities
		def entityClass = Class.forName(session.importer_header[columnindex.toInteger()].entity.getName(), true, this.getClass().getClassLoader())
		def entityObj = entityClass.newInstance(template:template)

		// Store the selected property for this column into the column map for the ImporterService
		session.importer_header[columnindex.toInteger()].property = property

		// Look up the template field type of the target TemplateField and store it also in the map
		session.importer_header[columnindex.toInteger()].templatefieldtype = entityObj.giveFieldType(property)

		// Is a "Don't import" property assigned to the column?
		session.importer_header[columnindex.toInteger()].dontimport = (property=="dontimport") ? true : false

		//if it's an identifier set the mapping column true or false
		entityObj.giveFields().each {
		    (it.preferredIdentifier && (it.name==property)) ? session.importer_header[columnindex.toInteger()].identifier = true : false
		}
	}

	//import workbook
	session.importer_importeddata = ImporterService.importdata(session.importer_template_id, session.importer_workbook, 0, 1, session.importer_header)

	if (params.layout=="horizontal")
	    render(view:"step3_simple", model:[datamatrix:session.importer_importeddata])
	else if (params.layout=="vertical")
	    render(view:"step3", model:[datamatrix:session.importer_importeddata])
    }

    def savePostview = {
	ImporterService.saveDatamatrix(session.importer_study, session.importer_importeddata)
	render(view:"step4")
    }

    /**
    * Return templates which belong to a certain entity type
    * 
    * @param entity entity name string (Sample, Subject, Study et cetera)
    * @return JSON object containing the found templates
    */
    def ajaxGetTemplatesByEntity = {
	def entityClass = grailsApplication.config.gscf.domain.importableEntities.get(params.entity).entity
	

        // fetch all templates for a specific entity
        //def templates = Template.findAllByEntity(Class.forName(entityClass, true, this.getClass().getClassLoader()))
	def templates = Template.list()

	println templates.dump()

	// render as JSON
        render templates as JSON
    }
}

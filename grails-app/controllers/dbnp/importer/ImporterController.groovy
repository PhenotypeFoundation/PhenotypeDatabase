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
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.ss.usermodel.DataFormatter
import dbnp.studycapturing.Template
import dbnp.studycapturing.Study
import dbnp.studycapturing.Subject
import dbnp.studycapturing.Event
import dbnp.studycapturing.Protocol
import dbnp.studycapturing.Sample
import dbnp.studycapturing.TemplateFieldType

class ImporterController {
    def ImporterService

    /**
     * Default page
     **/
    def index = { 
	[templates:Template.list()]
    }

    /**
    * This method will move the uploaded file to a temporary path and send the header
    * and the first n rows to the preview
    * @param importfile uploaded file to import
    */
    def upload = {
	def downloadedfile = request.getFile('importfile');
        def tempfile = new File(System.getProperty('java.io.tmpdir') + File.separatorChar + System.currentTimeMillis() + ".nmcdsp")
        downloadedfile.transferTo(tempfile)
        
	def wb = ImporterService.getWorkbook(new FileInputStream(tempfile))
        
	def header = ImporterService.getHeader(wb, 0)
	def datamatrix= ImporterService.getDatamatrix(wb, 0, 5)

	session.header = header
	session.importtemplate_id = params.template_id

        render (view:"step1", model:[header:header, datamatrix:datamatrix])
    }

    /**
    * User has assigned all entities and celltypes to the columns and continues to the next step (assigning properties to columns)
    * All information of the columns is stored in a session as MappingColumn object
    *
    * @param entity list of entities and columns it has been assigned to (columnindex:entitytype format)
    * @param celltype list of celltypes and columns it has been assigned to (columnindex:celltype format)
    * @return properties page
    *
    * @see celltype: http://poi.apache.org/apidocs/org/apache/poi/ss/usermodel/Cell.html
    */
    def savepreview = {	
	def entities  = request.getParameterValues("entity")
	def celltypes = request.getParameterValues("celltype")

	celltypes.each { c ->
	    def temp = c.split(":")
	    def celltype = temp[1].toInteger()
	    def templatefieldtype = TemplateFieldType.STRING
	    
	    session.header[temp[0].toInteger()].celltype = celltype

	    switch (celltype) {
		case 0 : templatefieldtype = TemplateFieldType.INTEGER
			 break
		case 1 : templatefieldtype = TemplateFieldType.STRING
			 break
		case 2 :  // formula cell type, cannot handle this
			 break
		case 3 : templatefieldtype = TemplateFieldType.STRING
			 break
		case 4 : templatefieldtype = TemplateFieldType.STRING
			 break
		default: break
	    }
	}

	entities.each { e ->
	    def temp = e.split(":")
	    Class clazz

	    switch (temp[1].toInteger()) {
		case 0: clazz = Study
			break
		case 1: clazz = Subject
			break
		case 2: clazz = Event
			break
		case 3: clazz = Protocol
			break
		case 4: clazz = Sample
			break
		default: clazz = Object
	    }

	    session.header[temp[0].toInteger()].index = temp[0].toInteger()
	    session.header[temp[0].toInteger()].entity = clazz
	}

	// currently only one template is used for all entities
	// TODO: show template fields per entity
	
	def templates = Template.get(session.importtemplate_id)

	render(view:"step2", model:[entities:entities, header:session.header, templates:templates])
    }

    /**
    * @param columnproperty array of columns and the assigned property in `column:property_id` format
    *
    */
    def saveproperties = {

	def columnproperties  = request.getParameterValues("columnproperty")
	columnproperties.each { cp ->
		def temp = cp.split(":")
		session.header[temp[0].toInteger()].property = temp[1].toInteger()
	}
	
	for (e in session.header) {
	    println e
	}
	render ("properties saved")
    }
}

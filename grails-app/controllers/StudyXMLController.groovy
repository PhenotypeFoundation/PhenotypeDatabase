import dbnp.authentication.*
import dbnp.studycapturing.*
import dbnp.studycapturing.*
import dbnp.studycapturing.Study
import grails.converters.XML
import grails.util.GrailsUtil
import groovy.util.* 
import groovy.util.slurpersupport.*
import groovy.util.slurpersupport.Node as Node 
import java.util.ArrayList 
import org.codehaus.groovy.grails.web.converters.configuration.ConvertersConfigurationHolder;
import org.dbnp.bgdt.*
import org.dbnp.gdt.*


/**  This class is for testing and developing the StudyXML service.
 *   It should be merged into the service as soon as the service is operating.
 *   Currently, this controller contains the methods required for importing.
 *   
 *   @author Jahn
 */
class StudyXMLController {

    def studyXMLService 

	def DomainClasses = [ 
		'Assay':dbnp.studycapturing.Assay,
		'AssayModule':org.dbnp.gdt.AssayModule,
		'Event':dbnp.studycapturing.Event,
		'EventGroup':dbnp.studycapturing.EventGroup,
		'Identity':org.dbnp.gdt.Identity,
		'PersonAffiliation':dbnp.studycapturing.PersonAffiliation,
		'Person':dbnp.studycapturing.Person,
		'PersonRole':dbnp.studycapturing.PersonRole,
		'Publication':dbnp.studycapturing.Publication,
		'RegistrationCode':dbnp.authentication.RegistrationCode,
		'RelTime':org.dbnp.gdt.RelTime,
		'Sample':dbnp.studycapturing.Sample,
		'SamplingEvent':dbnp.studycapturing.SamplingEvent,
		'SecRole':dbnp.authentication.SecRole, 
		'SecUser':dbnp.authentication.SecUser,
		'SecUserSecRole':dbnp.authentication.SecUserSecRole,
		'SessionAuthenticatedUser':dbnp.authentication.SessionAuthenticatedUser,
		'Study':dbnp.studycapturing.Study,
		'StudyPerson':dbnp.studycapturing.StudyPerson,
		'Subject':dbnp.studycapturing.Subject,
		'TemplateEntity':org.dbnp.gdt.TemplateEntity,
		'TemplateFieldListItem':org.dbnp.gdt.TemplateFieldListItem,
		'TemplateField':org.dbnp.gdt.TemplateField,
		'TemplateFieldType':org.dbnp.gdt.TemplateFieldType,
		'Template':org.dbnp.gdt.Template
	]


	def index = {

		if( GrailsUtil.environment!="development" ) {
			render "XML export/import is only available in the development environment."
			return
		}

		testExportImport()
    }




	/* This function gives an example of how to export and import with XML 
	 * using the method defined in this controller and the StudyXML service 
     * This is only for testing. All functionality should move into the StudyXML 
	 * service. */ 
	def testExportImport() {

		def list = studyXMLService.getDependentObjects( Study.get(1) ).unique() 

		def xml = (list as XML)

		def outf = new OutputStreamWriter(new FileOutputStream('/tmp/test.xml'))
		outf<<xml
		outf.close()

		def inf = new File('/tmp/test.xml')
		def lines = inf.readLines()
		def buf = new StringBuffer()
		lines.each{ buf.append(it) }

		def stuff = new XmlSlurper().parseText(buf.toString()) 
		def domainObjects = parseXMLStudyList(stuff)
		def study = createStudy(domainObjects)
		def owner = SecUser.findAll()?.get(1)
 		def person = StudyPerson.findAll()?.get(0)
		setStudyParameters(study,"Free style",owner,person)


		if( !study.validate() ) {
   			study.errors.each { render it.toString() + '<br>' }
		}


		try{
			study.save()
		} catch (Exception e) {
			//println e
		}

		render "added study with code ${study.code}<br>"
	}


	
	/** Get a class name for a given tag of the XML dump of a study. 
	  * This just makes the first letter of the tag upper case and
	  * adds the package name. 
	  *
	  * @param Tag 
	  * 
	  * @return Class corresponding to tag
	  */
	def getClassForTag( String tag ) {
			def shortName = tag.replaceFirst( tag[0], tag[0].toUpperCase() )
			return DomainClasses[ shortName ] 
	}



	/** Create a study object from a list of domain objects that are
 	  * parsed from XML. The study is not saved when returned and 
	  * still needs to be validated. Also, it can still be updated.
	  *
	  * @param parseObjects List of domain objects parsed from XML. 
	  * 
	  * @return Study object, still unvalidated and transient
	  */
	def	parseXMLStudyList( GPathResult result ) {
		def parseObjects = 
			result.childNodes().collect { 
				new ParseObject(it) 
			}		
		return parseObjects
	}


	/** Create a study object from a list of domain objects that are
 	  * parsed from XML. The study is not saved when returned and 
	  * still needs to be validated. Also, it can still be updated.
	  *
	  * @param parseObjects List of domain objects parsed from XML. 
	  * 
	  * @return Study object, still unvalidated and transient
	  */
	def createStudy( List parseObjects ) {

		treatConstraints(parseObjects) 

		parseObjects.each{ 
			if(!it.isFromDatabase)
				linkDomainFields( it.domainObject, it.node, parseObjects )
		}
		parseObjects.each{ 
			if(!it.isFromDatabase)
				populateOneToManies( it.domainObject, it.node, parseObjects )
		}
		parseObjects.each{ 
			if(!it.isFromDatabase)
				if( it.domainObject instanceof TemplateEntity ) {
					addTemplateRelations( it.domainObject, it.node, parseObjects )
					addTemplateFields( it.domainObject, it.node )
				}
		}

		def study = parseObjects*.domainObject.find { it instanceof Study }

		return study
	}



	/** Given a study object that has just been assabled from parsed xml, 
 	  * some fields have to be set manually. This is done here. 
	  *
	  * @param study
	  *
	  * @param code - String value for study code. By default, the parsed string is used. 
	  *
	  * @param owner - the owner of the study
	  *
	  * @param studyPerson - one StudyPerson has to be supplied since this informatin is 
	  *                      not supposed to be retrieved from the xml.
	  */
	def setStudyParameters(Study study, code=null, SecUser owner, studyPerson ) {

		// set subject.species by hand
		study.subjects.each { subject ->
			subject.species = Term.findAll()?.get(0)
		}

		// set samplingTemple for samplingEvents
		study.samplingEvents.each { samplingEvent ->
			samplingEvent.sampleTemplate = Template.findAll()?.get(0)
		}

		// set study.studyPerson from database 
		study.persons.clone().each {
			study.removeFromPersons(it)
		}
		study.addToPersons( studyPerson )

		study.owner = owner 

		if(code!=null) {
			study.code = code 
		}

		return study
	}




	/** Some domain objects require special tweaking by hand before they 
 	  * can be imported from xml. This tuning is done here. For example,
	  * Assays are instantiated from the database if they exist and can be
	  * identified by its name.
	  * 
	  * @param List of parse objects
	  **/
	def treatConstraints( List parseObjects ) {

		parseObjects.each { it ->
			def domainObject = it.domainObject
			switch( domainObject ) {
				case Assay: 
							// if assay with name name exists in database, 
							// this object is replaced by the object in the database.
					def existingAssay = Assay.findByName( domainObject.name )
					if(existingAssay!=null) {	
						it.domainObject = existingAssay
						it.isFromDatabase = true
					}
					break;
			}
		}
	}	


	/** Set a TemplateEntity's template field. Find the right Template in the list  
 	  *	of ParseObjects based on parsed id. If the TemplateEntity instance does
 	  * not have a matching template in the list of ParseObjects, it remains empty.
	  *
	  * @param domainObject Some Template Entity
	  *
	  * @param node Node with parse information
	  *
	  * @param parseObjects List of ParseObjects 
	  **/
	def addTemplateRelations( TemplateEntity domainObject, Node node, List parseObjects ) {
		def id = node.children().find{it.name=='template'}?.attributes()?.id
		if(id) {
			def template = parseObjects.find{ it.theClass==Template && it.id==id }?.domainObject
			if(template) {
				domainObject.template = template
			}
		}
	}



	/** Set a TemplateEntity's template fields with values from a Node. 
 	  * The template fields are fields such as TemplateStringField or TemplateFieldType.
	  *
	  * @param domainObject Some Template Entity
	  *
	  * @param node Node with parse information
	  *
	  **/
	def addTemplateFields( TemplateEntity domainObject, Node node ) {
		domainObject.metaClass.getProperties().each{ property ->
			def name = property.name      // name of templateFields, e.g., templateStringFields
			if( name ==~/template(.+)Fields/ ) {
				node.children().find{it.name==name}?.children()?.each{ fieldNode ->	
					def key = fieldNode.attributes()?.key
					def value = fieldNode.text()
					//domainObject.setFieldValue(key,value)  -> needs to be fixed based on class/type
				}
			}
		}
	}



	// maybe not needed at all!!
	// Link domain fields that are neither one-to-many nor many-to-one
	def linkDomainFields( domainObject, node, List parseObjects ) {
						
		def fields = 
			domainObject.getProperties().domainFields.collect { it.toString() }

		// deal with simple references to other domainObjects
		// (no one-to-manys, no belongs-to, no has-many
		// is this needed at all?
		node.children.each { child ->
			def name = child.name 
			def id = child.attributes()?.id
			if(id) {
					def list = parseObjects.findAll{ it.id == id }
					list.each {
						def property = it.theClass.metaClass.getProperties().find{ it.name == name }
						if(property?.getType()==it.theClass) {
							domainObject."$name" = linkedDomainObject
						}
					}
			}
		}
	}

	

    /** 
      *
      *  We are creating a new domain object called domainObject.
	  *  This method fills domainObject's to-many fields using the addTo() method.
      *  The newly added objects are from a given list. They are identified
	  *  by their IDs stored while parsing the XML document.
	  *  
      *  @param domainObject - a domain Object.
      *  @param Node - the XML parse node from which the object is constructed.
      *  @param List - a list of ParseObjects from which links may have to be made. 
	  *  
	  *  Remark: this could become a member method of ParseObject.
	  */
	def populateOneToManies( Object domainObject, Node node, List parseObjects ) {

		if( domainObject.class==Template ) {
			return
		}

		if( !domainObject.class.metaClass.getProperties().find{ it.name=='hasMany' } ) {
			return
		}

		domainObject.class.hasMany.each{ name, theClass ->
			node.children().each { child ->	
				if(child.name==name) {
					child.children().each { grandChild ->
						def id = grandChild.attributes.id
						if(id) {
							def ref = parseObjects.find{ it.theClass==theClass && it.id==id }
							if(ref) {
								def addTo = "addTo" + name.replaceFirst(name[0],name[0].toUpperCase()) 
								domainObject.invokeMethod(addTo,(Object) ref.domainObject )
							}
						}
					}
				}
			}
		}
	}


	/** This private class contains all information corresponding to one domain objects
     *  that is read from XML and then created to be linked into a new study object. 
     *
     *  A parse object contains three importants pieces of information: (1) the xml node, 
	 *  (2) the name of the tag that corresponds to a class name, and (3) the id that 
	 *  represents the instance of the exporetd object. There are several more members
	 *  that are needed in order to reconstruct the study object.
     */
	private class ParseObject { 

		String tag               // the tag used in the XML document for the object under construction.
		String id                // id for this object found in the XML document.
		Class theClass           // Class of this domain object.
		Object domainObject      // the domain object under construction or retrieved from the db.
		Node node                // an XML parse node.
		boolean isFromDatabase   // true if domainObject is from db, false if it's from xml input.


		/** The constructor gets an XML parse node and fills all other members from that.
 		  */
		public ParseObject( Node node ){
			tag = node.name()
			theClass = getClassForTag( tag ) 
			domainObject = theClass.newInstance() 
			isFromDatabase = false
			id = null
			if(node.attributes && node.attributes.id) {
				id = node.attributes.id
			}
			this.node=node

			if(theClass==Template) {
								// Templates have been imported before 
								// importing a study. Study.template is matched to a
								// Template by the template's name.
				def child = node.children.find{ "name"==it.name }
				domainObject = Template.findByName( child.text() )
			}
			else { 
				setSimpleFields()
			}

		}


		private void setSimpleFields() {

			def fields = [] 
			node.children().each{ 
				if(it.text()!="") {
					fields.add(it.name)
				}
			}

			def map = [:]
			domainObject.metaClass.getProperties().each { property ->
				def name = property.name
				def field = fields.find{ it == name }

				if(field) { 
					def type = property.type
					def value = node.children().find{ it.name == field }.text()

					switch(type) {
						case String: map[field]=value; break
						case Date: map[field] = Date.parse( 'yyyy-MM-dd', value ); break
						//default: println "${theClass}: ${field} has type ${type}"
						//case Boolean: ???; break
					}
				}

			}

			def newDomainObject = domainObject.class.newInstance(map)
			newDomainObject.id = 1
			domainObject = newDomainObject
		}

	}


}

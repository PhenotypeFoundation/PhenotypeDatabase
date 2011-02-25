/**
 *  ExporterService 
 *  
 *  @author Jahn
 *  
 *  This service for exporting a Study and all domain objects depending on it to XML.
 */ 



package dbnp.studyexport

import dbnp.authentication.*
import dbnp.studycapturing.*
import grails.converters.XML
import groovy.util.slurpersupport.*
import org.dbnp.gdt.*

class ExportService 
{

    static transactional = true
    static scope = "session"


    def grailsApplication
    /**
     *  List of classes that recursion does not go further into when building an XML  
     *  document. 
	 *  
	 *  @see #getRelatedObjects().
     */ 
	def static IgnoredClasses = [ String, long, Date, Boolean, SecUser, Publication, SecUser ]


    /**
     *  List of classes that recursion does not go further into when building an XML  
     *  document; the elements are still included. 
	 *  
	 *  (For importing Study objects)
	 *  
	 *  @see #getRelatedObjects().
     */ 
	def static TerminalClasses = [ AssayModule, Identity, Ontology, PersonAffiliation, 
			PersonRole, Template, TemplateField, 
			TemplateFieldListItem, TemplateFieldType, Term ] 


    /**
     *  List of domain classes related to Study.
     */ 
	def static DomainClasses = [ 'RegistrationCode':dbnp.authentication.RegistrationCode,
			'SecRole':dbnp.authentication.SecRole, 'SecUser':dbnp.authentication.SecUser,
			'SecUserSecRole':dbnp.authentication.SecUserSecRole,
			'SessionAuthenticatedUser':dbnp.authentication.SessionAuthenticatedUser,
			'Assay':dbnp.studycapturing.Assay,
			'Event':dbnp.studycapturing.Event, 'EventGroup':dbnp.studycapturing.EventGroup,
			'PersonAffiliation':dbnp.studycapturing.PersonAffiliation,
			'Person':dbnp.studycapturing.Person,
			'PersonRole':dbnp.studycapturing.PersonRole,
			'Publication':dbnp.studycapturing.Publication,
			'Sample':dbnp.studycapturing.Sample,
			'SamplingEvent':dbnp.studycapturing.SamplingEvent,
			'Study':dbnp.studycapturing.Study,
			'StudyPerson':dbnp.studycapturing.StudyPerson,
			'Subject':dbnp.studycapturing.Subject,
			'AssayModule':org.dbnp.gdt.AssayModule,
			'Identity':org.dbnp.gdt.Identity,
			'RelTime':org.dbnp.gdt.RelTime,
			'TemplateEntity':org.dbnp.gdt.TemplateEntity,
			'TemplateField':org.dbnp.gdt.TemplateField,
			'TemplateFieldListItem':org.dbnp.gdt.TemplateFieldListItem,
			'TemplateFieldType':org.dbnp.gdt.TemplateFieldType,
			'Template':org.dbnp.gdt.Template ]


	/** 
	 *  Returns a list of all Grails domain objects relevant for creating a full
	 *  XML representation of a Study.
	 *  
	 *  The actual XML is then created by the controller using Grails' XML converter. 
	 *  
	 *  @param Study 
	 *
	 *  @return List of all Grails domain objects 
	 */ 

	def getDependentObjects( Study study ) {
		return getRelatedObjects( study )
	}


	/** 
	 *  Returns a list of Grails domain objects.  
	 *  
	 *  Helper method for getDependentObjects().
	 *  
	 *  This method produces a list of all objects that need to be
	 *  written out in order to get an XML representation of a Study object. 
	 *  
	 *  This is achieved by recursion. The recursion stops at objects whose
	 *  class is member of IgnoredClasses or TerminalClasses. 
	 *  
	 *	Example call: 
	 *  
	 *		def objects = getDependentObjects( Study.get(1) )
	 *		(objects*.class).unique().sort().each { println it }
	 *  
	 *  @param domainObject  A grails domain object.
	 *  
	 *  @return List of all Grails domain objects 
	 */ 

	def	getRelatedObjects( domainObject ) {

		if(domainObject==null) {
			return []
		}

		def domainClass = domainObject.class
		def objects = []

		if( IgnoredClasses.contains(domainClass) )   {
			return objects 
		}

		if( domainClass.toString()==~/class dbnp.authentication.SecUser.+/ || 
		    domainClass.toString()==~/class dbnp.studycapturing.Publication.+/ ) {
			return objects 
		}



		if( TerminalClasses.contains(domainClass) )  {
			return [domainObject]
		}

		objects = [domainObject]

												// enter recursion with regular domain fields
		domainObject.properties.domainFields.each { field ->
			objects.addAll( getRelatedObjects(field) )
		}

												// enter recursion with hasMany fields 
		domainObject.getProperties().hasMany.each { property, theClass ->

			boolean isTemplateField = ( domainObject instanceof TemplateEntity  && 
				property==~/template(.+)Fields/ ) 
			if( !isTemplateField ) {
				domainObject."$property".each { 
						objects.addAll( getRelatedObjects(it) )
				}
			}

		}
		return objects.unique()
	}




	/** 
	 *  Parse XML object to List of objects to be translated into
	 *  a Study by getStudy().
	 *  
	 *  @param Study 
	 *
	 *  @return List of all Grails domain objects 
	 *
	 *  @see getStudy()
	 */ 

	def	parseXMLStudy() {
		XML.parse(new FileInputStream('/tmp/test.xml'), "UTF-8")
	}



	def	parseXMLStudyList( GPathResult result ) {
		def parseObjects = 
			result.childNodes().collect { 
				new ParseObject(it) 
			}		
		def study = createStudy( parseObjects ) 
		study.save()	
	}




	/** 
	 *  Create Study from list of objects repesenting a Study. 
	 *  
	 *  (For importing Study objects)
	 *  
	 *  @param  List of objects representing a Study. 
	 *
	 *  @return void
	 */ 

	def createStudy( List parseObjects ) {
		parseObjects.each{ 
			populateFields( it.domainObject, it.node, parseObjects )
		}
		parseObjects.each{ 
			populateOneToManies( it.domainObject, it.node, parseObjects )
		}
		parseObjects.each{ 
			populateTemplateFields( it.domainObject, it.node, parseObjects )
		}
	}



	/** 
	 *  Populate fields of a domainObject to be created.
	 *  The fields of a new object are filled with simple Class objects.
	 *  
	 *  (For importing Study objects)
	 *  
	 *  @param domainObject   The Object to be fielled  
	 *  
	 *  @param  List of ParseObjects representing a Study. 
	 *
	 *  @return the new domainObject 
	 */ 

	def populateFields( domainObject, node, List parseObjects ) {

		def fields = 
			domainObject.getProperties().domainFields.collect { it.toString() }

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
				}
			}
		}

		def newDomainObject = domainObject.class.newInstance(map)
		newDomainObject.id = 0
		domainObject = newDomainObject
	}



	/** 
	 *  Populate one-to-many maps of a new domainObject  
	 *  from list of ParseObjects. 
	 *  
	 *  (For importing Study objects)
	 *  
	 *  @param domainObject   domainObject to be fielled  
	 *  
	 *  @param  List of parseObjects representing a Study. 
	 *
	 *  @return the new domainObject 
	 */ 
	
	def populateOneToManies( domainObject, node, parseObjects ) {
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


	def populateTemplateFields( domainObject, Node node, List parseObjects ) {
	}



	/** 
	 *  Populate one-to-many maps of a new domainObject  
	 *  from list of ParseObjects. 
	 *
	 *  (For importing Study objects)
	 *
	 *  @param domainObject   domainObject to be fielled  
	 *  
	 *  @param  List of parseObjects representing a Study. 
	 *
	 *  @return the new domainObject 
	 */ 

	private class ParseObject { 
		String tag
		String id
		Class theClass 
		Object domainObject
		groovy.util.slurpersupport.Node node
	

		public ParseObject( node ){
			tag = node.name()
			theClass = getClassForTag( tag ) 
			domainObject = theClass.newInstance() 
			id = null
			if(node.attributes && node.attributes.id) {
				id = node.attributes.id
			}
			this.node=node
		}

		private static getClassForTag( String tag ) {
			def shortName = tag.replaceFirst( tag[0], tag[0].toUpperCase() )
			return DomainClasses[ shortName ] 
		}

	}

}

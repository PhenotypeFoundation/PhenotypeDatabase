/**
 *  This service is for exporting studies to a XML flat list and for
 *  importing them back. 
 *  
 *  @author Jahn
 *  
 *  The file is used in combination with a controller. 
 */ 


package dbnp.studyXML

import dbnp.authentication.*
import dbnp.studycapturing.*
import grails.converters.XML
import groovy.util.slurpersupport.*
import org.dbnp.bgdt.*
import org.dbnp.gdt.*

class StudyXMLService 
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
	def static DomainClasses = [ 
			'Assay':dbnp.studycapturing.Assay,
			'AssayModule':org.dbnp.gdt.AssayModule,
			'Event':dbnp.studycapturing.Event, 'EventGroup':dbnp.studycapturing.EventGroup,
			'Identity':org.dbnp.gdt.Identity,
			'PersonAffiliation':dbnp.studycapturing.PersonAffiliation,
			'Person':dbnp.studycapturing.Person,
			'PersonRole':dbnp.studycapturing.PersonRole,
			'Publication':dbnp.studycapturing.Publication,
			'RegistrationCode':dbnp.authentication.RegistrationCode,
			'RelTime':org.dbnp.gdt.RelTime,
			'Sample':dbnp.studycapturing.Sample,
			'SamplingEvent':dbnp.studycapturing.SamplingEvent,
			'SecRole':dbnp.authentication.SecRole, 'SecUser':dbnp.authentication.SecUser,
			'SecUserSecRole':dbnp.authentication.SecUserSecRole,
			'SessionAuthenticatedUser':dbnp.authentication.SessionAuthenticatedUser,
			'Study':dbnp.studycapturing.Study,
			'StudyPerson':dbnp.studycapturing.StudyPerson,
			'Subject':dbnp.studycapturing.Subject,
			'TemplateEntity':org.dbnp.gdt.TemplateEntity,
			'TemplateFieldListItem':org.dbnp.gdt.TemplateFieldListItem,
			'TemplateField':org.dbnp.gdt.TemplateField,
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
	 *  This is done by recursion. The recursion stops at objects whose
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


		if(domainObject instanceof TemplateEntity ) {
			objects.push(domainObject.template)
			domainObject.template.fields.each { 
					objects.push(it) 
			}

			if(domainClass==Assay) {
				domainObject.domainFields.each { 
					def memberClass = domainObject."$it".class
				}
			}

		}


		if( TerminalClasses.contains(domainClass) )  {
			objects.push(domainObject)
			return objects 
		}

		objects.push(domainObject)

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


}

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
import org.codehaus.groovy.grails.web.converters.configuration.ConvertersConfigurationHolder
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
	 *  @see #getRelatedObjects().
     */ 
	def static TerminalClasses = [ AssayModule, Identity, Ontology, PersonAffiliation, 
			PersonRole, Template, TemplateField, 
			TemplateFieldListItem, TemplateFieldType, Term ] 


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
			println "${domainClass} -- ${domainObject}"
			return objects 
		}



		if( TerminalClasses.contains(domainClass) )  {
			return [domainObject]
		}

		objects = [domainObject]

												// enter recursion with regular domain fields
		domainObject.getProperties().domainFields.each { field ->
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
	 *  Returns Study object from List of objects created by 
	 *  XML representation of a Study.
	 *  
	 *  @param List of objects 
	 *
	 *  @return Study object that has to be saved 
	 *
	 *  @see getDependentObjects()
	 */ 

	def	getStudy( Collection objectList ) {
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

	def	parseXMLStudy( XML xml ) {
	}

}

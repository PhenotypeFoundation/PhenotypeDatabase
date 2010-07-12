package dbnp.rest.common

import grails.converters.JSON
import java.net.URLEncoder
import org.codehaus.groovy.grails.web.json.*

/**  CommunicationManager
 *
 *   @author Jahn
 *
 *   This class manages communication between dbNP modules such as GSCF and SAM.
 *   By communication we mean two ways of exchanging information: (1) via Rest resources, 
 *   and (2) via Grails views that a module can make available to another module.
 *
 *   For Rest communication this class implements a Rest client that fetches data 
 *   from other modules' Rest resources. The Rest implementation transfers data in JSON.
 *
 *   Note: Do not use this class directly to fetch data. Instead use your module's 
 *   rest wrapper methods. Use this module, to create these rest wrapper methods. 
 *   For instance, use dbnp.rest.sam.registerRestWrapperMethodsGSCFtoSAM to register new methods 
 *   for accessing GSCF's Rest service in SAM; your new method shoud then use this class.
 */


class CommunicationManager {

    def        static Encoding      = "UTF-8" 
    def public static SAMServerURL  = "http://localhost:8182/sam"
    def public static GSCFServerURL = "http://localhost:8080/gscf"

     

    /**
     * Get the results of provided by a rest Rest resource.
     *
     * @params String resource The name of the resource, e.g. importer/pages 
     * @params Map params      A Map of parmater names and values., e.g. ['externalAssayID':12]
     * @return String url   
     */
    public static Object getRestResource( RestServerURL, resource, params ) {
		def url = getRestURL( RestServerURL, resource, params )
		return  JSON.parse( url.newReader() )
    }


    /**
     * Convenience method for constructing URLs for SAM that need parameters.
     * Note that parameters are first convereted to strings by calling their toString() method
     * and then Encoded to protect special characters.
     *
     * @params String resource The name of the resource, e.g. importer/pages 
     * @params Map params      A Map of parmater names and values., e.g. ['externalAssayID':12]
     * @return String url   
     */
    public static URL getRestURL( RestServerURL, resource, params ) {
        def url = RestServerURL + '/' + resource
		def first = true
		println "url: " + url
		params.each { name, value ->
			if(first) {
				first = false
				url += '/nil?' + name + "=" + URLEncoder.encode( value.toString(), Encoding )
			}
			else { 
				url += '&' + name + "=" + URLEncoder.encode( value.toString(), Encoding  )
			}
		}
		return new URL( url )
    }



    /**
     * This method dynamically adds a static method to the CommunicationManager.
     *  
     * @params String serverURL		A rest server URL.
     * @params String restName		The name of a rest resource on the server.	
     * @params Map params      		A list of parameter names to be passed to this resource.
     * @return String url   
     *  
     * Given a rest resource at serverURL called resourceName, we register a static method
     * for the CommunicationManager. The new method has the same name and arity as the resource. 
     *  
     * Example: Suppopse http://localhost:8080/gscf/rest/getSamples is a Rest resource.
     *  
     * In our grails app, we would like to connect to this service. We want to have a 
     * method getSamples() that fetches the result from the service. We do this by calling
     *  
     * 		CommunicationManager.addRestWrapper( 'http://localhost:8080/gscf/rest', 'getSamples', ['externalStudyID'] ) 
     *  
     * This registers a new method:
     *  
	 *		 public static Object CommunicationManager.getSamples( Object arg )
     *  
     * This method has arrity 1 and expects to be given a map. The map is the parameter map
     * of the rest service getSamples. It maps parameter called "externalStudyID" to some object 
     * that is passed. So, it can be called like as follows:
     *  
     *      def sampleList = CommunicationManager.getSamples( [externalStudyID:4711] )
     *  
     *  The call will deliver the results of the parameterized rest resource given at:
     *  
     *  	http://localhost:8080/gscf/rest/nil?externalStudyID=4711
     * 
     */

    public static addRestWrapper( serverURL, restName, params = [], closure = { return it } ) {
		CommunicationManager.metaClass.registerStaticMethod( restName ) { Object [] strangeGroovyArgs ->
			def map = [:]
		    def args = strangeGroovyArgs[0]        // groovy nests the parameters of the methods in some other array
			for( i in 0..(params.size-1) ) {
				def param = params[i]
			    map[param] = args[i]
			}
			return closure( getRestResource( serverURL, restName, map ) )
		}
	}





    /**
     * This method dynamically registers a static method to the CommunicationManager. The new method 
     * gives url for a Grails view on some server and takes as arguments the arguments required
     * as params by the view.
     *  
     * @params String methodname	The name for method to be registered. 
     * @params String serverURL		The server's URL.
     * @params String viewName		The view's name, e.g., '/Assay/show'
     * @params Map params      		The parameter list required by this view.
     * @return String URL 
     *  
     */  
    public static addViewWrapper( methodName, serverURL, viewName, params = [] ) {

		CommunicationManager.metaClass.registerStaticMethod( methodName ) { Object [] strangeGroovyArgs ->
			def map = [:]
		    def args = strangeGroovyArgs[0]        // groovy nests the parameters of the methods in some other array
			for( i in 0..(params.size-1) ) {
				def param = params[i]
			    map[param] = args[i]
			}
			return getRestURL( serverURL, viewName, map )
		}
    }


    /**
     *  This creates on run time new methods for accessing Rest resources that GSCF provides for SAM.
     *  This method should be called in grails-app/conf/BootStrap.groovy in the SAM module.
     */ 
    public static registerRestWrapperMethodsGSCFtoSAM() {
    	def url = GSCFServerURL + '/rest'
		addRestWrapper( url , 'getStudies' )
		addRestWrapper( url , 'getSubjects', ['externalStudyID'] )
		addRestWrapper( url , 'getAssays',   ['externalStudyID'] )
		addRestWrapper( url , 'getSamples',  ['externalAssayID'] )
    }


    /**
     *  This method creates on run time new methods for accessing Grails views that SAM provides for GSCF.
     *  This method should be called in grails-app/conf/BootStrap.groovy in the GSCF module.
     */ 
    public static registerRestWrapperMethodsSAMtoGSCF() {
		def url = SAMServerURL

		// register method that links to the SAM view for importing a SimpleAssay. 
        // parameters: externalAssayID, an externalAssayID 
		addViewWrapper( 'getAssayImportURL', url, 'importer/pages', ['externalAssayID', 'externalStudyID'] )

		// register method that links to the SAM view for showing a SimpleAssay 
        // parameters: externalAssayID
		addViewWrapper( 'getAssayShowURL', url, 'simpleAssay/show', ['externalAssayID'] )

   		// register method that links to the SAM view for editing a SimpleAssay 
        // parameters: externalAssayID
		addViewWrapper( 'getAssayEditURL', url, 'simpleAssay/show', ['externalAssayID'] )

   		// register method that links to the SAM view for editing a SimpleAssay 
        // parameters: externalAssayID
		addViewWrapper( 'getMeasurementTypesURL', url, 'simpleAssayMeasurementType/list', ['externalStudyID'] )

   		// register rest resource that returns the results of a full text query on SAM 
        // parameters:   query. A string for fulltext search on SAM
        // return value: results map. It contains two keys 'studyIds', and 'assays'. 'studyIds' 
		//               key maps to a list of Study domain objects of GSCF. 'assays' map to a
		//               list of pairs. Each pair consists of an Assay domain object of GSCF and
		//               additional assay information from SAM provided as a map.
		// Example of a returned map: 
		//               [studyIds:[PPSH], 
		//				 assays:[[isIntake:false, isDrug:false, correctionMethod:test Correction Method 1, 
		//				 detectableLimit:1, isNew:false, class:data.SimpleAssay, externalAssayID:1, id:1, 
		//				 measurements:null, unit:Insulin, inSerum:false, name:test Simple Assay 1, 
		//				 referenceValues:test Reference Values 1]]]
		def closure = { map -> 
		    def studies = [] 	
		    def assays  = [] 	
			def studiesHQ = "from dbnp.studycapturing.Study as s where s.code=?"
			map['studyIds'].each { studies.add( dbnp.studycapturing.Study.find(studiesHQ,[it]) ) }
			map['assays'].each { samAssay ->
				def assayID = samAssay['externalAssayID']
			    def assayHQ = "from dbnp.studycapturing.Assay as a where a.externalAssayID='${assayID}'"
				def assay = dbnp.studycapturing.Assay.find(assayHQ)
				assays.add( [samAssay,assay] )
			} 
			return [studies:studies, assays:assays] 
		}

		addRestWrapper( url+'/rest', 'getQueryResult',  ['query'], closure )
		
    }


}

/**
 * PilotController Controler
 *
 * Description of my controller
 *
 * @author  m.s.vanvliet@lacdr.leidenuniv.nl (Michael van Vliet)
 * @since	20101019
 * @package	nmc
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package nmc

import dbnp.studycapturing.*;

class PilotController {
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	
	/**
	* Fires after every action and determines the layout of the page
	*/
   def afterInterceptor = { model, modelAndView ->
	 
	 if ( params['dialog'] ) {
	   model.layout = 'dialog';
	   model.extraparams = [ 'dialog': 'true' ] ;
	 } else {
	   model.layout = 'main';
	   model.extraparams = [] ;
	 }
   }
	
    def index = {
        
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [studyInstanceList: Study.list(params), studyInstanceTotal: Study.count()]
    }
	
	/**
	* create closure
	*/
   def create = {
	   def studyInstance = new Study()
	   studyInstance.properties = params
	   return [studyInstance: studyInstance]
   }
   
   def save = {
	   def studyInstance = new Study(params)
	   
	   //For Pilot we do not ask for code, we generate it for the user
	   studyInstance.code = params?.title?.encodeAsMD5()
	   
	   def extraparams = new LinkedHashMap();

	   if( params[ 'dialog' ] ) {
		 extraparams[ 'dialog' ] = params[ 'dialog' ]
	   }

	   if (studyInstance.save(flush: true)) {
		   
		   //Study was created, now setup a NMC - Metabolomics Assay for testing
		   def assayInstance = new Assay()
		   assayInstance.name = "${studyInstance.title} - Metabolomics Assay"
		   assayInstance.module = AssayModule.findByName("Metabolomics module")
		   assayInstance.externalAssayID = assayInstance?.name?.encodeAsMD5()
		   studyInstance.addToAssays(assayInstance)
		   assayInstance.save(flush: true)		   
		   
		   //flash.message = "${message(code: 'default.created.message', args: [message(code: 'study.label', default: 'Study'), ( studyInstance.title ? studyInstance.title : "" ) + " " + ( studyInstance.code ? studyInstance.code : "" )])}"
		   
		   redirect(action: "show", id: studyInstance.id, params: extraparams )
	   }
	   else {
		   render(view: "create", model: [studyInstance: studyInstance])
	   }
   }
   
   def show = {
	   def studyInstance = Study.get(params.id)
	   if (!studyInstance) {
		   flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'study.label', default: 'Study'), params.id])}"
		   redirect(action: "list")
	   }
	   else {
		   [studyInstance: studyInstance]
	   }
   }
   
   def edit = {
	   def studyInstance = Study.get(params.id)
	   if (!studyInstance) {
		   flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'study.label', default: 'Study'), params.id])}"
		   redirect(action: "list")
	   }
	   else {
		   return [studyInstance: studyInstance]
	   }
   }

   def update = {
	   def studyInstance = Study.get(params.id)
	   
	   //For Pilot we do not ask for code, we generate it for the user
	   studyInstance.code = studyInstance?.title?.encodeAsMD5()

	   def extraparams = new LinkedHashMap();

	   if( params[ 'dialog' ] ) {
		 extraparams[ 'dialog' ] = params[ 'dialog' ]
	   }

	   if (studyInstance) {
		   if (params.version) {
			   def version = params.version.toLong()
			   if (studyInstance.version > version) {
				   
				   studyInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'study.label', default: 'Study')] as Object[], "Another user has updated this Study while you were editing")
				   render(view: "edit", model: [studyInstance: studyInstance])
				   return
			   }
		   }
		   studyInstance.properties = params
		   if (!studyInstance.hasErrors() && studyInstance.save(flush: true)) {
			   flash.message = "${message(code: 'default.created.message', args: [message(code: 'study.label', default: 'Study'), ( studyInstance.title ? studyInstance.title : "" ) + " " + ( studyInstance.code ? studyInstance.code : "" )])}"
			   redirect(action: "show", id: studyInstance.id, params: extraparams)
		   }
		   else {
			   render(view: "edit", model: [studyInstance: studyInstance])
		   }
	   }
	   else {
		   flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'study.label', default: 'Study'), params.id])}"
		   redirect(action: "list", params: extraparams)
	   }
   }

   def delete = {
	   def studyInstance = Study.get(params.id)

	   def extraparams = new LinkedHashMap();

	   if( params[ 'dialog' ] ) {
		 extraparams[ 'dialog' ] = params[ 'dialog' ]
	   }

	   if (studyInstance) {
		   def studyName = ( studyInstance.title ? studyInstance.title : "" ) + " " + ( studyInstance.code ? studyInstance.code : "" );
		   try {
			   studyInstance.delete(flush: true)
			   flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'study.label', default: 'Study'), studyName])}"
			   redirect(action: "list", params: extraparams)
		   }
		   catch (org.springframework.dao.DataIntegrityViolationException e) {
			   flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'study.label', default: 'Study'), studyName])}"
			   redirect(action: "show", id: params.id, params: extraparams)
		   }
	   }
	   else {
		   flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'study.label', default: 'Study'), studyName])}"
		   redirect(action: "list", params: extraparams)
	   }
   }
}

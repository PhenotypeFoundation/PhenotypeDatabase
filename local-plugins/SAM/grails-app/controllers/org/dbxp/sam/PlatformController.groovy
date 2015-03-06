package org.dbxp.sam

import org.dbnp.gdt.Template
import org.dbnp.gdt.TemplateFieldType
import org.springframework.dao.DataIntegrityViolationException

class PlatformController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
    def moduleService

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        if (moduleService.validateModule(params?.module)) {
            def platformList = Platform.findAllByPlatformtype(params.module)
            [platformInstanceList: platformList, platformInstanceTotal: platformList.size(), module: params.module]
        }
        else {
            redirect(controller: 'error', action: 'notFound')
        }
    }

    def create() {
        if (moduleService.validateModule(params?.module)) {
            [platformInstance: new Platform(params), module: params.module]
        }
        else {
            redirect(controller: 'error', action: 'notFound')
        }
    }

    def save() {
        def platformInstance = new Platform()

        // Was template set on the 'create page'?
        if( params.template) {
            // Yes, so add the template
            platformInstance.changeTemplate( params.template );
        }

        // was a template set?
        if (platformInstance.template) {
            // yes, iterate through template fields
            platformInstance.giveFields().each() {
                // and set their values
                if(it.type==TemplateFieldType.BOOLEAN){ // Set templatefields with type 'BOOLEAN'
                    def value = params.get(it.escapedName()+"_"+it.escapedName())!=null // '' becomes true, and null becomes false, as intended.
                    platformInstance.setFieldValue(it.name, value)
                } else {
                    platformInstance.setFieldValue(it.name, params.get(it.escapedName()+"_"+it.escapedName()))
                }
            }
        }

        // Remove the template parameter, since it is a string and that troubles the
        // setting of properties.
        def template = params.remove( 'template' )

        platformInstance.properties = params

        // Trim the whitespace from the name, to enable accurate validation
        platformInstance.name = platformInstance.name?.trim()

        // Attempt to save platform
        if (!platformInstance.save(flush: true)) {
            render(view: "create", model: [platformInstance: platformInstance], module: params.module)
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'platform.label', default: 'Platform'), platformInstance.name])
        redirect(action: "show", id: platformInstance.id, params: [module: params.module])
    }

    def show(Long id) {
        if (moduleService.validateModule(params?.module)) {
            def platformInstance = Platform.get(id)
            if (!platformInstance) {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'platform.label', default: 'Platform'), id])
                redirect(action: "list", params: [module: params.module])
                return
            }
            [platformInstance: platformInstance, module: params.module]
        }
        else {
            redirect(controller: 'error', action: 'notFound')
        }
    }

    def edit(Long id) {
        if (moduleService.validateModule(params?.module)) {
            def platformInstance = Platform.get(id)
            if (!platformInstance) {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'platform.label', default: 'Platform'), id])
                redirect(action: "list", params: [module: params.module])
                return
            }

            [platformInstance: platformInstance, module: params.module]
        }
        else {
            redirect(controller: 'error', action: 'notFound')
        }
    }

    def update(Long id, Long version) {
        def platformInstance = Platform.get(id)
        if (!platformInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'platform.label', default: 'Platform'), id])
            redirect(action: "list", params: [module: params.module])
            return
        }

        if (version != null) {
            if (platformInstance.version > version) {
                platformInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'platform.label', default: 'Platform')] as Object[],
                          "Another user has updated this Platform while you were editing")
                render(view: "edit", model: [platformInstance: platformInstance], module: params.module)
                return
            }
        }

        // Did the template change?
        if( params.template && params.template != platformInstance.template?.name) {
            // Yes, so change the template
            platformInstance.changeTemplate( params.template );
        }

        // was a template set?
        if (platformInstance.template) {
            // yes, iterate through template fields
            platformInstance.giveFields().each() {
                // and set their values
                if(it.type==TemplateFieldType.BOOLEAN){ // Set templatefields with type 'BOOLEAN'
                    def value = params.get(it.escapedName()+"_"+it.escapedName())!=null // '' becomes true, and null becomes false, as intended.
                    platformInstance.setFieldValue(it.name, value)
                } else {
                    platformInstance.setFieldValue(it.name, params.get(it.escapedName()+"_"+it.escapedName()))
                }
            }
        }

        // Remove the template parameter, since it is a string and that troubles the
        // setting of properties.
        def template = params.remove( 'template' )

        platformInstance.properties = params

        if (!platformInstance.save(flush: true)) {
            render(view: "edit", model: [platformInstance: platformInstance], module: params.module)
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'platform.label', default: 'Platform'), platformInstance.name])
        redirect(action: "show", id: platformInstance.id, params: [module: params.module])
    }

    def delete(Long id) {
        def platformInstance = Platform.get(id)
        if (!platformInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'platform.label', default: 'Platform'), id])
            redirect(action: "list", params: [module: params.module])
            return
        }

        try {
            platformInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'platform.label', default: 'Platform'), id])
            redirect(action: "list", params: [module: params.module])
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'platform.label', default: 'Platform'), id])
            redirect(action: "show", id: id, params: [module: params.module])
        }
    }
	
	// Get a list of template specific fields
	def templateSelection = {
		render(template: "templateSelection", model: [template: _determineTemplate()], module: params.module)
	}

    def returnUpdatedTemplateSpecificFields = {
        def template = _determineTemplate();
        def values = [:];

        // Set the correct value of all domain fields and template fields (if template exists)
        try {
            if( template ) {
                template.fields.each {
                    values[it.escapedName()] = params.get(it.escapedName()+"_"+it.escapedName());
                }
            }
        } catch( Exception e ) {
            log.error( e );
        }

        render(template: "templateSpecific", model: [template: template, values: values], module: params.module)
    }

    /**
     * Returns the template that should be shown on the screen
     */
    private _determineTemplate()  {
        def template = null;

        if( params.templateEditorHasBeenOpened == 'true') {
            // If the template editor has been opened (and closed), we should use
            // the template that we stored previously
            if( session.templateId ) {
                template = Template.get( session.templateId );
            }
        } else {
            // Otherwise, we should use the template that the user selected.
            if( params.template ) {
                def templateByEntityAndName
                Template.findAllByEntity(Platform).each {
                    if (it.name == params.template) {
                        templateByEntityAndName = it
                    }
                }
                return templateByEntityAndName
            }
        }

        // Store the template id in session, so the system will know the previously
        // selected template
        session.templateId = template?.id

        return template;
    }

}

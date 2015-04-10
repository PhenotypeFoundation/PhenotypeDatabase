package dbnp.studycapturing

import grails.plugin.springsecurity.annotation.Secured
/**
 * 888       888 888    888 8888888888 8888888b.  8888888888
 * 888   o   888 888    888 888        888   Y88b 888
 * 888  d8b  888 888    888 888        888    888 888
 * 888 d888b 888 8888888888 8888888    888   d88P 8888888
 * 888d88888b888 888    888 888        8888888P"  888
 * 88888P Y88888 888    888 888        888 T88b   888
 * 8888P   Y8888 888    888 888        888  T88b  888
 * 888P     Y888 888    888 8888888888 888   T88b 8888888888
 *
 * 8888888 .d8888b.     88888888888 888    888 8888888888
 *   888  d88P  Y88b        888     888    888 888
 *   888  Y88b.             888     888    888 888
 *   888   "Y888b.          888     8888888888 8888888
 *   888      "Y88b.        888     888    888 888
 *   888        "888        888     888    888 888
 *   888  Y88b  d88P        888     888    888 888
 * 8888888 "Y8888P"         888     888    888 8888888888
 *
 *   888888        d8888 888     888     d8888 8888888b.   .d88888b.   .d8888b.
 *     "88b       d88888 888     888    d88888 888  "Y88b d88P" "Y88b d88P  Y88b
 *      888      d88P888 888     888   d88P888 888    888 888     888 888    888
 *      888     d88P 888 Y88b   d88P  d88P 888 888    888 888     888 888
 *      888    d88P  888  Y88b d88P  d88P  888 888    888 888     888 888
 *      888   d88P   888   Y88o88P  d88P   888 888    888 888     888 888    888
 *      88P  d8888888888    Y888P  d8888888888 888  .d88P Y88b. .d88P Y88b  d88P
 *      888 d88P     888     Y8P  d88P     888 8888888P"   "Y88888P"   "Y8888P"
 *    .d88P
 *  .d88P"
 * 888P"
 *
 *  .d8888b.  888  .d8888b.  888  .d8888b.  888
 * d88P  Y88b 888 d88P  Y88b 888 d88P  Y88b 888
 *      .d88P 888      .d88P 888      .d88P 888
 *    .d88P"  888    .d88P"  888    .d88P"  888
 *    888"    888    888"    888    888"    888
 *    888     Y8P    888     Y8P    888     Y8P
 *             "              "              "
 *    888     888    888     888    888     888
 *
 *
 * TODO: add PROPER class and method documentation, just like have
 *       agreed upon hundreds of times!!!!
 */
@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class PersonController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
    def possibleGenders = [ 'Male', 'Female' ]

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
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [personInstanceList: Person.list(params), personInstanceTotal: Person.count()]
    }

    def create = {
        def personInstance = new Person()
        personInstance.properties = params
        return [personInstance: personInstance, possibleGenders:possibleGenders]
    }

    def save = {
        def personInstance = new Person(params)
        def extraparams = new LinkedHashMap();

        if( params[ 'dialog' ] ) {
          extraparams[ 'dialog' ] = params[ 'dialog' ]
        }

        if (personInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'person.label', default: 'Person'), ( personInstance.firstName ? personInstance.firstName : "" ) + " " + ( personInstance.prefix ? personInstance.prefix : "" ) + " " + ( personInstance.lastName ? personInstance.lastName : "" )])}"
            
            redirect(action: "show", id: personInstance.id, params: extraparams )
        }
        else {
            render(view: "create", model: [personInstance: personInstance])
        }
    }

    def show = {
        def personInstance = Person.get(params.id)
        if (!personInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), params.id])}"
            redirect(action: "list")
        }
        else {
            [personInstance: personInstance]
        }
    }

    def edit = {
        def personInstance = Person.get(params.id)
        if (!personInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [personInstance: personInstance,possibleGenders:possibleGenders]
        }
    }

    def update = {
        def personInstance = Person.get(params.id)

        def extraparams = new LinkedHashMap();

        if( params[ 'dialog' ] ) {
          extraparams[ 'dialog' ] = params[ 'dialog' ]
        }

        if (personInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (personInstance.version > version) {
                    
                    personInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'person.label', default: 'Person')] as Object[], "Another user has updated this Person while you were editing")
                    render(view: "edit", model: [personInstance: personInstance])
                    return
                }
            }
            personInstance.properties = params
            if (!personInstance.hasErrors() && personInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'person.label', default: 'Person'), ( personInstance.firstName ? personInstance.firstName : "" ) + " " + ( personInstance.prefix ? personInstance.prefix : "" ) + " " + ( personInstance.lastName ? personInstance.lastName : "" )])}"
                redirect(action: "show", id: personInstance.id, params: extraparams)
            }
            else {
                render(view: "edit", model: [personInstance: personInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), params.id])}"
            redirect(action: "list", params: extraparams)
        }
    }

    def delete = {
        def personInstance = Person.get(params.id)

        def extraparams = new LinkedHashMap();

        if( params[ 'dialog' ] ) {
          extraparams[ 'dialog' ] = params[ 'dialog' ]
        }

        if (personInstance) {
            def personName = ( personInstance.firstName ? personInstance.firstName : "" ) + " " + ( personInstance.prefix ? personInstance.prefix : "" ) + " " + ( personInstance.lastName ? personInstance.lastName : "" );
            try {
                personInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'person.label', default: 'Person'), personName])}"
                redirect(action: "list", params: extraparams)
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'person.label', default: 'Person'), personName])}"
                redirect(action: "show", id: params.id, params: extraparams)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'person.label', default: 'Person'), params.id])}"
            redirect(action: "list", params: extraparams)
        }
    }
}

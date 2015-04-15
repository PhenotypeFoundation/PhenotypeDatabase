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
class PersonRoleController {

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
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [personRoleInstanceList: PersonRole.list(params), personRoleInstanceTotal: PersonRole.count()]
    }

    def create = {
        def personRoleInstance = new PersonRole()
        personRoleInstance.properties = params
        return [personRoleInstance: personRoleInstance]
    }

    def save = {
        def personRoleInstance = new PersonRole(params)
        def extraparams = new LinkedHashMap();

        if( params[ 'dialog' ] ) {
          extraparams[ 'dialog' ] = params[ 'dialog' ]
        }

        if (personRoleInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'personRole.label', default: 'Role'), personRoleInstance.name])}"
            //redirect(action: "show", id: personRoleInstance.id)
            redirect(action: "list", params: extraparams)
        }
        else {
            render(view: "create", model: [personRoleInstance: personRoleInstance])
        }
    }

    def show = {
        def personRoleInstance = PersonRole.get(params.id)
        if (!personRoleInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'personRole.label', default: 'Role'), params.id])}"
            redirect(action: "list")
        }
        else {
            [personRoleInstance: personRoleInstance]
        }
    }

    def edit = {
        def personRoleInstance = PersonRole.get(params.id)
        if (!personRoleInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'personRole.label', default: 'Role'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [personRoleInstance: personRoleInstance]
        }
    }

    def update = {
        def personRoleInstance = PersonRole.get(params.id)
        def extraparams = new LinkedHashMap();

        if( params[ 'dialog' ] ) {
          extraparams[ 'dialog' ] = params[ 'dialog' ]
        }

        if (personRoleInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (personRoleInstance.version > version) {
                    
                    personRoleInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'personRole.label', default: 'Role')] as Object[], "Another user has updated this PersonRole while you were editing")
                    render(view: "edit", model: [personRoleInstance: personRoleInstance])
                    return
                }
            }
            personRoleInstance.properties = params
            if (!personRoleInstance.hasErrors() && personRoleInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'personRole.label', default: 'Role'), personRoleInstance.name])}"
                //redirect(action: "show", id: personRoleInstance.id)
                redirect(action: "list", params: extraparams)

            }
            else {
                render(view: "edit", model: [personRoleInstance: personRoleInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'personRole.label', default: 'Role'), params.id])}"
            redirect(action: "list", params: extraparams)
        }
    }

    def delete = {
        def personRoleInstance = PersonRole.get(params.id)
        def extraparams = new LinkedHashMap();

        if( params[ 'dialog' ] ) {
          extraparams[ 'dialog' ] = params[ 'dialog' ]
        }


        if (personRoleInstance) {
            def roleName = personRoleInstance.name
            try {
                personRoleInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'personRole.label', default: 'Role'), roleName])}"
                redirect(action: "list", params: extraparams)
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'personRole.label', default: 'Role'), roleName])}"
                // redirect(action: "show", id: params.id)
                redirect(action: "list", params: extraparams)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'personRole.label', default: 'Role'), params.id])}"
            redirect(action: "list", params: extraparams)
        }
    }
}

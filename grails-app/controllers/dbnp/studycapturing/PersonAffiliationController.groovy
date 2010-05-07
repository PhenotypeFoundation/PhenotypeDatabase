package dbnp.studycapturing
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
class PersonAffiliationController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [personAffiliationInstanceList: PersonAffiliation.list(params), personAffiliationInstanceTotal: PersonAffiliation.count()]
    }

    def create = {
        def personAffiliationInstance = new PersonAffiliation()
        personAffiliationInstance.properties = params
        return [personAffiliationInstance: personAffiliationInstance]
    }

    def save = {
        def personAffiliationInstance = new PersonAffiliation(params)
        if (personAffiliationInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'personAffiliation.label', default: 'Affiliation'), personAffiliationInstance.name])}"
            redirect(action: "show", id: personAffiliationInstance.id)
        }
        else {
            render(view: "create", model: [personAffiliationInstance: personAffiliationInstance])
        }
    }

    def show = {
        def personAffiliationInstance = PersonAffiliation.get(params.id)
        if (!personAffiliationInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'personAffiliation.label', default: 'Affiliation'), params.id])}"
            redirect(action: "list")
        }
        else {
            [personAffiliationInstance: personAffiliationInstance]
        }
    }

    def edit = {
        def personAffiliationInstance = PersonAffiliation.get(params.id)
        if (!personAffiliationInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'personAffiliation.label', default: 'Affiliation'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [personAffiliationInstance: personAffiliationInstance]
        }
    }

    def update = {
        def personAffiliationInstance = PersonAffiliation.get(params.id)
        if (personAffiliationInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (personAffiliationInstance.version > version) {
                    
                    personAffiliationInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'personAffiliation.label', default: 'Affiliation')] as Object[], "Another user has updated this PersonAffiliation while you were editing")
                    render(view: "edit", model: [personAffiliationInstance: personAffiliationInstance])
                    return
                }
            }
            personAffiliationInstance.properties = params
            if (!personAffiliationInstance.hasErrors() && personAffiliationInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'personAffiliation.label', default: 'Affiliation'), personAffiliationInstance.name])}"
                redirect(action: "show", id: personAffiliationInstance.id)
            }
            else {
                render(view: "edit", model: [personAffiliationInstance: personAffiliationInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'personAffiliation.label', default: 'Affiliation'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def personAffiliationInstance = PersonAffiliation.get(params.id)
        if (personAffiliationInstance) {
            def affiliationName = personAffiliationInstance.name
            try {
                personAffiliationInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'personAffiliation.label', default: 'Affiliation'), affiliationName])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'personAffiliation.label', default: 'Affiliation'), affiliationName])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'personAffiliation.label', default: 'Affiliation'), params.id])}"
            redirect(action: "list")
        }
    }
}

package dbnp.studycapturing

import grails.plugin.springsecurity.annotation.Secured
/**
 * Publications controller
 *
 * @author      Robert Horlings
 * @since	20100519
 * @package	studycapturing
 */

class PublicationController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    /**
     * Shows a form to add a new publication by searching pubmed
     */
    def add = {}

    /**
     * Adds publication selected from pubmed
     */
    def createFromPubmed = {
        // instantiate term with parameters
        def publication = new Publication(
            title: params.get( 'publication-title' ),
            authorsList: params.get( 'publication-authorsList' ),
            pubMedID: params.get( 'publication-pubMedID' ),
            DOI: params.get( 'publication-doi' )
        );
	
		// Check whether the autorsList is not too long. If it is, split it
		if( publication.authorsList.size() > 255 ) {
			def postfix = " et al.";
			def split = publication.authorsList[ 0..255 - postfix.size()].lastIndexOf( ", " );
			publication.authorsList = publication.authorsList[ 0..split-1] + postfix;
		}

        def message;
        def errors = '';

        // validate term
        if (publication.validate()) {
            publication.save()
            message = "Publication added to the system"
        } else {
            errors = publications.errors.getAllErrors().join( ', ' );
            message = "Publication addition failed"
        }

        render( 'view': 'add', model:[ message: message, errors: errors ] );
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [publicationInstanceList: Publication.list(params), publicationInstanceTotal: Publication.count()]
    }

    @Secured(['IS_AUTHENTICATED_REMEMBERED'])
    def create() {
        def publicationInstance = new Publication()
        publicationInstance.properties = params
        return [publicationInstance: publicationInstance]
    }

    def save = {
        def publicationInstance = new Publication(params)
        if (publicationInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'publication.label', default: 'Publication'), publicationInstance.title])}"
            redirect(action: "show", id: publicationInstance.id)
        }
        else {
            render(view: "create", model: [publicationInstance: publicationInstance])
        }
    }

    def show = {
        def publicationInstance = Publication.get(params.id)
        if (!publicationInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'publication.label', default: 'Publication'), params.id])}"
            redirect(action: "list")
        }
        else {
            [publicationInstance: publicationInstance]
        }
    }

    def edit = {
        def publicationInstance = Publication.get(params.id)
        if (!publicationInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'publication.label', default: 'Publication'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [publicationInstance: publicationInstance]
        }
    }

    def update = {
        def publicationInstance = Publication.get(params.id)
        if (publicationInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (publicationInstance.version > version) {
                    
                    publicationInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'publication.label', default: 'Publication')] as Object[], "Another user has updated this Publication while you were editing")
                    render(view: "edit", model: [publicationInstance: publicationInstance])
                    return
                }
            }
            publicationInstance.properties = params
            if (!publicationInstance.hasErrors() && publicationInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'publication.label', default: 'Publication'), publicationInstance.title])}"
                redirect(action: "show", id: publicationInstance.id)
            }
            else {
                render(view: "edit", model: [publicationInstance: publicationInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'publication.label', default: 'Publication'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def publicationInstance = Publication.get(params.id)
        if (publicationInstance) {
            try {
                def title = publicationInstance.title
                publicationInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'publication.label', default: 'Publication'), title])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'publication.label', default: 'Publication'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'publication.label', default: 'Publication'), params.id])}"
            redirect(action: "list")
        }
    }
 
    /**
     * Searches for an ID in the current database, based on the pubMedID
     * If the publication is not found in the database, it is added
     */
    def getID = {
        // Find the ID
        def pubMedID = params.get( 'publication-pubMedID' );
        if( pubMedID ) {
            def publication = Publication.findByPubMedID( pubMedID );
            if( !publication ) {
                publication = new Publication(
                    title: params.get( 'publication-title' ),
                    authorsList: params.get( 'publication-authorsList' ),
                    pubMedID: params.get( 'publication-pubMedID' ),
                    DOI: params.get( 'publication-doi' )
                );
			
				// Check whether the autorsList is not too long. If it is, split it
				if( publication.authorsList.size() > 255 ) {
					def postfix = " et al.";
					def split = publication.authorsList[ 0..255 - postfix.size()].lastIndexOf( ", " );
					publication.authorsList = publication.authorsList[ 0..split-1] + postfix;
				}
			
				publication.save(flush:true);
            }

            // Return the ID
			response.contentType = "text/plain"
            render publication.id;
        } else {
            response.status = 500;
			response.contentType = "text/plain"
            render "No pubMedID found in request";
        }
    }
}

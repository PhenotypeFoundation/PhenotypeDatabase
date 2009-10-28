

class StudyController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        [ studyInstanceList: Study.list( params ), studyInstanceTotal: Study.count() ]
    }

    def show = {
        def studyInstance = Study.get( params.id )

        if(!studyInstance) {
            flash.message = "Study not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ studyInstance : studyInstance ] }
    }

    def delete = {
        def studyInstance = Study.get( params.id )
        if(studyInstance) {
            try {
                studyInstance.delete(flush:true)
                flash.message = "Study ${params.id} deleted"
                redirect(action:list)
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "Study ${params.id} could not be deleted"
                redirect(action:show,id:params.id)
            }
        }
        else {
            flash.message = "Study not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def studyInstance = Study.get( params.id )

        if(!studyInstance) {
            flash.message = "Study not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ studyInstance : studyInstance ]
        }
    }

    def update = {
        def studyInstance = Study.get( params.id )
        if(studyInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(studyInstance.version > version) {
                    
                    studyInstance.errors.rejectValue("version", "study.optimistic.locking.failure", "Another user has updated this Study while you were editing.")
                    render(view:'edit',model:[studyInstance:studyInstance])
                    return
                }
            }
            studyInstance.properties = params
            if(!studyInstance.hasErrors() && studyInstance.save()) {
                flash.message = "Study ${params.id} updated"
                redirect(action:show,id:studyInstance.id)
            }
            else {
                render(view:'edit',model:[studyInstance:studyInstance])
            }
        }
        else {
            flash.message = "Study not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def studyInstance = new Study()
        studyInstance.properties = params
        return ['studyInstance':studyInstance]
    }

    def save = {
        def studyInstance = new Study(params)
        if(!studyInstance.hasErrors() && studyInstance.save()) {
            flash.message = "Study ${studyInstance.id} created"
            redirect(action:show,id:studyInstance.id)
        }
        else {
            render(view:'create',model:[studyInstance:studyInstance])
        }
    }
}

package dbnp.studycapturing
import java.text.SimpleDateFormat
import dbnp.data.Term

class EventController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [eventInstanceList: Event.list(params), eventInstanceTotal: Event.count()]
    }

    def create = {
        def eventInstance = new Event()
        eventInstance.properties = params
	//chain( view: "createForEventDescription", params:params )
        return [eventInstance:eventInstance]
    }


    def createForEventDescription = {

        if( params["id"]==null)
	{
            def eventInstance = new Event()
	    def sDate = new Date( params["startTime"])
	    def eDate = new Date( params["endTime"])
	    def description = EventDescription.findById((params["eventDescription"])["id"])
            return [testo:params.clone(), sDate:sDate, eDate:eDate, description:description ]
	}
	else
	{
	    def eventInstance = Event.get(params.id)
            if (!eventInstance) {
                flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'event.label', default: 'Event'), params.id])}"
                redirect(action: "list")
            }
            return [testo:params.clone(), sDate:eventInstance.startTime, eDate:eventInstance.endTime, description:eventInstance.eventDescription]
        }
    }


    // Convert date strings to date strings grails can deal with.
    // Input format:  "01/20/2010 08:45 am"
    // Output format: "01/20/2010 20:45"
    // Note: the "am" amd "pm" suffixes are removed.

    def parseDate = {  st ->
            def subst = st.substring(0,16)
            def ampm =  st.substring(17,19)
            if(ampm=="pm")
            {
                 def hours=st.substring(11,13)
                 hours = hours.toInteger() + 12
                 st = st.substring(0,11) + hours + st.substring(13,16)
            }
	    else { st = st.substring(0,16) }

    	    def sdfh = new SimpleDateFormat("MM/DD/yyyy hh:mm")
	    return sdfh.parse(st)
    }


    def save = {
        println "In EventController.save: ${params}"

	params["startTime"] = parseDate(params["startTime"])     // parse the date strings
	params["endTime"] = parseDate(params["endTime"])

        def eventInstance = new Event(params)

	println params.protocolInstance
                                                                  // If a protocol instnace already exists,
                                                                  // update this event's parameter values.
        if(params.protocolInstance != null) {
            params.protocolInstance.each { id, value ->
                    println id + " " + value
		    def parameter = ProtocolParameterInstance.get(id)
		    parameter.value=value
		    parameter.save()
	        }
        }

        if (eventInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'event.label', default: 'Event'), eventInstance.id])}"
            redirect(action: "show", id: eventInstance.id)
        }
        else {
            render(view: "create", model: [eventInstance: eventInstance])
        }
        render(view: "create", model: [eventInstance: eventInstance])
    }


    def show = {
        def eventInstance = Event.get(params.id)
        if (!eventInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'event.label', default: 'Event'), params.id])}"
            redirect(action: "list")
        }
        else {
            [eventInstance: eventInstance]
        }
    }





    def partial = {
        println "In action: partial"
        println params
        def eventDescription = EventDescription.get(params.id)
        if (!eventDescription) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'event.label', default: 'Event'), params.id])}"
            redirect(action: "list")
        }
        else {
            [eventDescription: event]
        }
	redirect(view: 'partial')
    }





    def edit = {

        if( params["id"]==null)
	{
            def eventInstance = new Event()
	    def sDate = new Date( params["startTime"])
	    def eDate = new Date( params["endTime"])
	    def description = EventDescription.findById((params["eventDescription"])["id"])
            return [testo:params.clone(), sDate:sDate, eDate:eDate, description:description ]
	}

	else
	{
	    def eventInstance = Event.get(params.id)
            if (!eventInstance) {
                flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'event.label', default: 'Event'), params.id])}"
                redirect(action: "list")
            }
            return [testo:params.clone(), sDate:eventInstance.startTime, eDate:eventInstance.endTime, description:eventInstance.eventDescription]
        }

    }





    def update = {
        def eventInstance = Event.get(params.id)
        if (eventInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (eventInstance.version > version) {
                    
                    eventInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'event.label', default: 'Event')] as Object[], "Another user has updated this Event while you were editing")
                    render(view: "edit", model: [eventInstance: eventInstance])
                    return
                }
            }
            eventInstance.properties = params
            if (!eventInstance.hasErrors() && eventInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'event.label', default: 'Event'), eventInstance.id])}"
                redirect(action: "show", id: eventInstance.id)
            }
            else {
                render(view: "edit", model: [eventInstance: eventInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'event.label', default: 'Event'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def eventInstance = Event.get(params.id)
        if (eventInstance) {
            try {
                eventInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'event.label', default: 'Event'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'event.label', default: 'Event'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'event.label', default: 'Event'), params.id])}"
            redirect(action: "list")
        }
    }



    def createDummies = {

         ["ONE","TWO"].each{ x -> println x }
	 def t = new Term()
	 t.name =  "homo politicus russicus"
	 t.ontology = "Ontology: Monkies and Typewriters"
	 t.accession = "up or down"
	 t.save()

         def pp = new ProtocolParameter()
	 pp.name = "LSD reatment"
         pp.unit = "Tt"
         pp.description = "feed the LSD to the subject"
         pp.reference = t
         pp.type = "String"     // should become ProtocolParameterType at some point
	 pp.save()

    	 def p = new Protocol()
	 p.name = "Hugo (dummy #1)"
	 p.reference = t
	 p.save()
	 p.addToParameters(pp)

         def ppi = new ProtocolParameterInstance()
	 ppi.value = "1.2"
	 ppi.protocolParameter = pp
	 ppi.save()

         def ppi2 = new ProtocolParameterInstance()
	 ppi2.value = "23.5"
	 ppi2.protocolParameter = pp
	 ppi2.save()

	 def pi = new ProtocolInstance()
	 pi.protocol = p.find("from Protocol p ")
	 pi.save()
	 pi.addToValues(ppi)
	 pi.addToValues(ppi2)

	 def s= new Subject()
	 s.name = "Vladimir Putin"
	 s.species = t
	 s.save()

	 def ed = new EventDescription()
	 ed.name = "dummmy name"
	 ed.description = "dummmy description"
	 ed.protocol = pi
	 ed.classification = Term.find("from Term t")
	 ed.save()

         def sdfh = new SimpleDateFormat("dd/MM/yyyy hh:mm")
         def eventInstance = new Event()
         def someDate = sdfh.parse("29/11/2008 18:00")
	 eventInstance.subject = s
	 eventInstance.eventDescription= ed
	 eventInstance.startTime = someDate
	 eventInstance.endTime = someDate
	 if( eventInstance.save() )
	 { redirect( action:show, id: eventInstance.id ) }
         else { chain( action:list ) }



    }
}
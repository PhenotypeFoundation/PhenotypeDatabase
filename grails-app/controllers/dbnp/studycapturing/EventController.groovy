package dbnp.studycapturing
import java.text.SimpleDateFormat
import dbnp.data.Term
import dbnp.data.Ontology

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

        def event = Event.get(params["id"])

	if( event==null ) {                                                        // this is an entirely new event
            render(action: "list", total:Event.count() )
	}

	params["startTime"] = parseDate(params["startTime"])                       // parse the date strings
	params["endTime"] = parseDate(params["endTime"])


        // the Subject is automatically parsed
	// update Event Description
        def oldProtocol=event.eventDescription.protocol.id.toString()
        def newProtocol=params["protocol.id"]
	def protocolParameters = params["protocolParameter"]

        println "\n\nparams"
        params.each{ println it }

        if(oldProtocol<=>newProtocol) {                                            // protocol id changed
            event.eventDescription=EventDescription.get(newProtocol)
	    event.parameterStringValues.clear()                                    // this does not propagate orphened parameters
	    def protocol=Protocol.get(newProtocol)
	    protocolParameters.each{ key, value ->
                 def parameter=ProtocolParameter.get(key).name
		 event.parameterStringValues[parameter] = value
	    }
	    println event.parameterStringValues
	    event.eventDescription.protocol=protocol
	}
        else                                                                       // protocol is the same, values changed
        {
       	    protocolParameters.each{ key, value ->
                 def parameter=ProtocolParameter.get(key)
		 event.parameterStringValues[parameter.name]=value                 // changed from key to id
	    }

	}


        if (event.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'event.label', default: 'Event'), event.id])}"
            redirect(action: "show", id: event.id)
        }

        render(action: "list", total:Event.count() )
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





    // This action is not complete yet.
    // (1) Need to include SamplingEvents.
    // (2) This probably causes orphened PrtocolPrameters that have to be delt with.
    //     The orphanes have to be managed centrally with the Protocols.
    // (3) Parts of this might have to be moved into the Domain object's save() method.
    // (4) The correspoding view's params are bloated and contain redundancy.
    // (5) The whole thing should be moved to update.
    // (6) A "create" should be added.

    def edit = {

        if( params["id"]==null)
	{
            def eventInstance = new Event()
	    def sDate = new Date( params["startTime"])
	    def eDate = new Date( params["endTime"])
	    def description = EventDescription.findById((params["eventDescription"])["id"])
            return [eventInstance:eventInstance, testo:params.clone(), sDate:sDate, eDate:eDate, description:description ]
	}
	else
	{
	    def eventInstance = Event.get(params.id)
            if (!eventInstance) {
                flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'event.label', default: 'Event'), params.id])}"
                redirect(action: "list")
            }
            return [eventInstance:eventInstance, testo:params.clone(), sDate:eventInstance.startTime, eDate:eventInstance.endTime, description:eventInstance.eventDescription]
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


    def showSample = {
	  def samples = null
          def event = Event.get(params.id)
                                              // user wants this Event to be a SamplingEvent?
	  def wantSample = params.wantSample <=>'no'?true:false
	  print wantSample
	  if( event.isSamplingEvent() ) {
	      samples=event.samples
	      println "yes ${event.id}"
          }
	  else    println "no ${event.id}"

	  render( view:"showSample", model:[samples:samples,wantSample:wantSample] )
    }

    
}

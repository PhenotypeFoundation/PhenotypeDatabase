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


    /*def createForEventDescription = {
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
    }*/


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




    // helper function for save()
    // parses the params from the edit view
    // and saves paramters as new entries in the events value lists
    def parseParamsForParameterValues( params, event ) {

	    params.each{ key,value ->
		 def pattern =/(parameterValue\.)([\d]+)/
                 def matcher = key=~pattern
		 if(matcher) {
		      def id = key.replaceAll(pattern,'$2')
		      def parameter = ProtocolParameter.get(id)

                      switch(parameter.type)
                      {
		            case dbnp.studycapturing.ProtocolParameterType.STRING:
                                 event.parameterStringValues[parameter.name]=value
			         break
		            case dbnp.studycapturing.ProtocolParameterType.FLOAT:
                                 event.parameterFloatValues[parameter.name]=value.toFloat()
			         break
		            case dbnp.studycapturing.ProtocolParameterType.INTEGER:
                                 event.parameterFloatValues[parameter.name]=value.toInteger()
			         break
		            case dbnp.studycapturing.ProtocolParameterType.STRINGLIST:
		                 def item = ParameterStringListItem.get(value)
			         event.parameterStringListValues[''+parameter.id]=item
                      }
		 }
            }
    }



    // assuming that an event has a sample
    // return the first sample's subject
    def getSubjectForEvent( event ) {
        def samples =  Sample.getSamplesFor(event)
        return samples[0].parentSubject
    }




    // helper function for save()
    // parse params from the edit view
    // and save all samples returned as a list
    def parseParamsForNewSamples( params, event ) {

        def subject = getSubjectForEvent( event )

        def samples=[]
            params.each{ k,v ->
                 def pattern = /^(sampleName)([\d]+)/
	         def matcher =  k=~pattern
	         if(matcher) {
                     def id = k.replaceAll(pattern,'$2')
	             def sample = new Sample()
	             sample.parentEvent = event
	             sample.parentSubject = subject
	             sample.name = v
	             sample.material= Term.getTerm( params['sampleMaterial'+id] )
		     saveSample(sample)
	             samples.push(sample)
	         }
	    }
        return samples
    }



    // save a samle or handle errors
    def saveSample(sample) {
           if (sample.save(flush: true)) {
                       flash.message = "${message(code: 'default.created.message', args: [message(code: 'event.label', default: 'Sample'), sample.id])}"
            }
            else {
	            sample.errors.each{ println it }
            }
    }


    // helper function for save()
    // parse params from the edit view and save changes.
    // Return all updated samples as a list
    def parseParamsForOldSamples( params ) {
        def samples=[]
            params.each{ k,v ->
                 def pattern = /^(sampleName_existing_)([\d]+)/
	         def matcher =  k=~pattern
	         if(matcher) {
                     def id = k.replaceAll(pattern,'$2')
	             def sample = Sample.get(id)
	             sample.name = v
	             sample.material= Term.getTerm( params['sampleMaterial_existing_'+id] )
	             samples.push(sample)
		     saveSample(sample)
	         }
	    }
        return samples
    }


    // helper function for save()
    // delete a sample removed by the user
    // Note: we completely delete this sample! It is also removed from the Study and the Assay!
    def deleteSampelsRemovedByUser( originalSamples, remainingSamples) {

        def toDelete = []
        originalSamples.each { original ->
            if( !remainingSamples.contains(original) ) 
	    { 
                toDelete.push( original )
            }
	}

	toDelete.each { 
            Assay.list().each{ assay ->
	        if( assay.samples.contains((Sample)it) )
	        {
		    assay.removeFromSamples(it)
	        }
	    }
            Study.list().each{ study ->
	        if( study.samples.contains((Sample)it) )
	        {
		    study.removeFromSamples(it)
	        }
	    }
	    ((Sample)it).delete()
	}
    }






    def save = {

        // create a new event from scratch

	if( !(params['id']=~/^[\d]+$/) ) {

            /* Not needed anymore: replace by template check?

            def description = new EventDescription()
	    description.name = (params['name']==null || params['name'].replaceAll(/\S/,'').size()==0 ) ? '[no Name]' : params['name']
	    description.description = (params['description']==null || params['description'].replaceAll(/\w/,'').size()==0 ) ? '[no description]' : params['description']
	    description.protocol = Protocol.get( params['protocol'] )
            description.isSamplingEvent = params['isSamplingEvent']=='on' ? true : false

            if (description.save(flush: true)) {
                flash.message = "${message(code: 'default.created.message', args: [message(code: 'description.label', default: 'Event'), description.id])}"
            }
	    else {
		description.errors.each{ println it }
	    }*/

            def event = description.isSamplingEvent ? new SamplingEvent() : new Event();

	    event.startTime = new Date(params["startTime"])                   // parse the date strings
	    event.endTime = new Date(params["endTime"])                       // parse the date strings
            event.parameterStringValues = new HashMap()
            event.parameterFloatValues = new HashMap()
            event.parameterIntegerValues = new HashMap()
            event.parameterStringListValues = new HashMap()
            event.eventDescription = description


            if (event.save(flush:true, validate:false)) {
                flash.message = "${message(code: 'default.created.message', args: [message(code: 'event.label', default: 'Event'), event.id])}"
            }
	    else {
		    event.errors.each{ println it }
	    }

            // read params and add parameter values to event.
	    // (such as ParameterStringListValues, etc.
            parseParamsForParameterValues( params, event )


            if (event.save(flush: true)) {
                flash.message = "${message(code: 'default.created.message', args: [message(code: 'event.label', default: 'Event'), event.id])}"
            }
	    else {
		    event.errors.each{ println it }
	    }


	}


        // modify an existing event

	else {  

            def event = Event.get(params['id'])

            // save basic changes in event and event description

	    /* Not needed anymore: replace by template check?

            def description = event.eventDescription
	    def oldProtocol = description.protocol

	    def name = params['name']
	    description.name = ( name==null || name.replaceAll(/\S/,'').size()==0 ) ? '[no Name]' : name
	    description.description = (params['description']==null || params['description'].replaceAll(/\w/,'').size()==0 ) ? '[no description]' : params['description']
            description.isSamplingEvent = params['isSamplingEvent']=='on' ? true : false

            */

	    event.startTime = new Date(params["startTime"])
	    event.endTime   = new Date(params["endTime"])


	    /* Not needed anymore: replace by template check?


            // save changed parameters
	    description.protocol = Protocol.get( params['protocol'] )

            // get the protocol
            if(description.protocol!=oldProtocol)  {          // protocol changed

                // remove all old parameter values

                def removeAll = { values, memberName ->
		    def list = values.getProperty(memberName)
		}

                removeAll(event, 'parameterStringValues' )
                removeAll(event, 'parameterIntegerValues' )
                removeAll(event, 'parameterFloatValues' )
                removeAll(event, 'parameterStringListValues')


                // add all new parameter values
                parseParamsForParameterValues( params, event )


            }

            */


            // update samples

            if( event.isSamplingEvent() ) {

                // remove deleted samples
		// update existing samples

		// add new samples

		def originalSamples = Sample.getSamplesFor(event)               // samples that have been in this form before the edit

		def newSamples = parseParamsForNewSamples( params, event )       //  get list of new samples as persistent sample objects
		                                                                 //  also add all the samples to this event already
										 //  by assigning event as parentEvent

		def remainingSamples = parseParamsForOldSamples( params )        // samples, that have been in the form, and not deleted by the user
		                                                                 // remainigSamples is subset of originalSamples

                deleteSampelsRemovedByUser( originalSamples, remainingSamples)   // delete sample and remove it from parentSubject and the
		                                                                 // associated study.

	    }

            //((Event)event).eventDescription=description


            if (event.save(flush: true)) {
                 flash.message = "${message(code: 'default.created.message', args: [message(code: 'event.label', default: 'Event'), event.id])}"
            }
	    else {
		 event.errors.each{ println it }
	    }

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

        // create entirely new Event

        if( params["id"]==null || params['id']=='' )
	{
            // New events cannot deal with Samples because there is not subject
	    // to assign samples to. Therefore, samples cannot be added to the a new
	    // Event, event if the user makes it a SamplingEvent by ticking a box.
	    // Therefore, showSample is set to false.

            def eventInstance = new Event()
	    def sDate = new Date()
	    def eDate = new Date()
	    //def description = new EventDescription()
            return [eventInstance:eventInstance, testo:params.clone(), sDate:sDate, eDate:eDate,
	            //description:description,
	    showSample:false, samples:null, createNew:true ]
	}


        // edit an existing Event

	else
	{
	    def eventInstance = Event.get(params.id)
            if (!eventInstance) {
                flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'event.label', default: 'Event'), params.id])}"
                redirect(action: "list")
            }
	    def samples = []
	    def showSample = false
	    if(eventInstance.isSamplingEvent() ) {
		samples = ((SamplingEvent) eventInstance).getSamples()
		if( samples.size() > 0 ) { showSample = true }
		// later, also check of eventInstance's study contains any subjects, if so, show them as list to chose from
	    }

	    return [eventInstance:eventInstance, testo:params.clone(), sDate:eventInstance.startTime, eDate:eventInstance.endTime,
		    // description:eventInstance.eventDescription,
	    showSample:showSample, samples:samples, createNew:false ]
        }

    }


    def create = {
        redirect(action:"edit", id:'')
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

	  println params
	  println "\n\nin showSample"
	  params.each{ x -> println x}
	  def samples = null
          def event = Event.get(params.id)
          if(event!=null)
	  {
	  def wantSample = params['wantSample']
                                              // user wants this Event to be a SamplingEvent?
          if( wantSample==null  &&  event.isSamplingEvent() )
	  {
                println "want sample is null"
                wantSample = true
          }	
	  else {  println "want sample is " + params['wantSample']
		  wantSample = params.wantSample <=>'no'?true:false }



	  if( event.isSamplingEvent() ) {
              samples = Sample.findAll("from Sample as s where s.parentEvent.id = ${event.id}" )
              samples.each{ println it.class }
              samples.collect{ it.name }
	      println "yes ${event.id}"
          }
	  else    println "no ${event.id}"


	  render( view:"showSample", model:[samples:samples,wantSample:wantSample,id:event.id] )
	  }
    }


   def deleteSample = {
	  // saves the samples from the page, then repaint the samples
	  println "in deleteSample"
	  println params

	  def event = Event.get(params['id'])

	  redirect( action:showSample, samples:newSample, wantSample:true,id:params['id'] )
   }


   def showEventDescription = {
         def event = Event.get( params['id'] )
         def description = EventDescription.get( params['eventDescriptionId'] )
	 render( view:"showEventDescription", model:[description:description] )
   }


   def deleteAllSamples = {
        println "in deleteSamples"
        println params
	def event = Event.get(params['id'])
        event.samples.each{ 
            event.removeFromSamples(it)
            it.delete(flush:true)
        }

	redirect( action:showSample, id:params['id'] )
   }



   def combobox = {
	def event = Event.get(1)
	def parameters = event.parameterStringValues
	render( view:"combobox", model:[event:event,parameters:parameters] )
   }
}

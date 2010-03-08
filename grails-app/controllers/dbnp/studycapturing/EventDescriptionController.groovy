package dbnp.studycapturing
import dbnp.data.Term

class EventDescriptionController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [eventDescriptionInstanceList: EventDescription.list(params), eventDescriptionInstanceTotal: EventDescription.count()]
    }

    def create = {
        def eventDescriptionInstance = new EventDescription()
            render(view:'edit', model:[eventDescriptionInstance: eventDescriptionInstance] )

		/*
        if (!eventDescriptionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), params.id])}"
            redirect(action: "list")
        }
        else {
            render(view:'edit', model:[eventDescriptionInstance: eventDescriptionInstance] )
        }
	*/
    }


    def save = {
        println "save"
        println params
        params.each { println it }



        def description = null      // the variable to be updated


        /*  update an existing description */

        if(params['id']!='')  {             


            // STEP 0  - set variables

            description = new EventDescription()
            description.name=params['name']
            // description.description=params['description']   // has to be a Term
	    // description.classification=params['classification']  // has to be a Term
	    description.isSamplingEvent= params['isSample']=='true'?true:false

	    def protocol = Protocol.get(params['protocol'])   // the protocol

            def parameters = [:]                   // parameters given by the user (in the view)
	                                           // key: id (as string), value: non-persistant ProtocolParameter object

            def options = [:]                      // store options, i.e. ParameterStringListItems given by the user
                                                   // use ids of parameter as key, store hash of id and name




            // STEP 1 parse params and fill hashes


            // collect parameters from form
            params.each { key, value ->
                 if(key=~/row_(.*)__(.*)/) {
                      def matcher = (key=~/row_(.*?)__(.*?)$/)
                      println matcher[0][1]+'  '+matcher[0][2]
                      def id = matcher[0][1]
		      def member = matcher[0][2]

                      println 'member: '+ member
		      if(member=~/parameterStringValue__(.*?)$/) {
                          matcher = member=~/parameterStringValue__(.*?)$/
		          def psv = matcher[0][1]
		          println "${id}\t${psv}:\tvalue:${value}"
			  if(options[id]==null) { options[id]=[:] }  // store paramter string value's id and value
                          (options[id])[psv]=value
                      }
		      else if(member!='reference')  { 
		          if(parameters[id]==null) { parameters[id]=new ProtocolParameter() }

		          if(member=~/^type.*/) {
                               value= ProtocolParameterType.valueOf(value)
			       member='type'
			  }
                          parameters[id].setProperty(member,value)
		      }
	         }
	    }

println "here 1"

            // collect options (i.e., ParameterStringListItem from parameters)
            parameters.each{ key, value ->
                if(value.type==ProtocolParameterType.STRINGLIST) {
                    def parameter = parameters[key]
                    options[key].each{k,v-> println "k: ${k}\t v:${v}" } // debug
                }
            }

println "here 2"




           // STEP 2  -  remove deleted parameters (from persistent protocol description)

	   protocol.parameters.each{ 
               if( parameters[it.id.toString()]==null  )
	           { protocol.removeFromParameters(it.id) }
	   }

println "here 3"


           // STEP 3  -  update altered parameters

	   protocol.parameters.each{ inDb ->

               def found = parameters[inDb.id.toString()]
               //['name','type','description','reference','unit'].each {
	       // debugging: ignoring the reference !!
               ['name','type','description','unit'].each {
		   inDb.setProperty(it,found.getProperty(it))
	       }


               // update options (i.e. ParameterStringListItem objects) of parameter inDb
       	       if(inDb.type==ProtocolParameterType.STRINGLIST ) {

		       if(found.type==ProtocolParameterType.STRINGLIST ) {

                            // add or modifiy options for existing parameters
                            options[inDb.id.toString()].each{ id, name ->
                                def item = inDb.listEntries.find{ it.id.toString()==id }
				if(!item) {  // add as new option to persistant parameter
				    item = new ParameterStringListItem()
				    item.name = name
				    inDb.addToListEntries(item)
				}
				else {       // update persistant paramter
                                    item.name = name
				}
                            }

                            // remove options that have been deleted by the user
			    def itemsToBeRemoved = []
			    inDb.listEntries.each { item ->
                                 if( ! ((options[inDb.id.toString()])[item.id.toString()]) )
				     { itemsToBeRemoved.push item }
                            }
			    itemsToBeRemoved.each { inDb.removeFromListEntries(it) }
		       }

               }
	       else { 
                        inDb.listEntries.collect{it}.each{ inDb.removeFromListEntries(it) }
               }

	  }




           // STEP 4  - add new parameters

println "here 4"
            // find new parameters, added by user
	    // remove all other parameters from paramters list
	    def newParameters = [:]
	    parameters.each{ key, value ->
                if( ! protocol.parameters.find {it.id.toString()==key} ) {
		    newParameters[key]=value
		}
	    }
	    parameters=newParameters



println "here 5"
            //  add new parameters (to persistent protocolDescription)
            parameters.each { id, parameter->
                def newParameter = new ProtocolParameter()                           // update properties
                ['name','type','description','unit'].each {
                    newParameter.setProperty( it, parameter.getProperty(it) )
	        }

                if(parameter.type==ProtocolParameterType.STRINGLIST) {               // update options
                     options[id].each { someKey, name ->
		         if(item==null) item = new ParameterStringListItem()
		         item.name=name
                         parameter.addToListEntries(item)
		     }
                }
                description.addToListEntries(parameter)
	    }



println "here 6"

           // STEP 5  - make changes persitant


            // compare paramters for protocol
            def parametersFromUser = []

            // check whether all parameters are still part of the protocol
            protocol.parameters

	}

        if (description.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'description.label', default: 'EventDescription'), description.id])}"
            redirect(action: "show", id: description.id)
        }
        else {
            render(view: "create", model: [description: description])
        }


        render( action: 'list' )
    }


    def show = {

        def eventDescriptionInstance = EventDescription.get(params.id)
        if (!eventDescriptionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), params.id])}"
            redirect(action: "list")
        }

        else {
            [eventDescriptionInstance: eventDescriptionInstance, params:params]
        }
    }


    def edit = {
        def eventDescriptionInstance = EventDescription.get(params.id)
        if (!eventDescriptionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [eventDescriptionInstance: eventDescriptionInstance]
        }
    }



    def showMyProtocol = {
	println "in showMyProtocol"
	println params
        if( EventDescription.get(params.id)==null || EventDescription.get(params.id).protocol==null ) {
	    println "in 1a"
            def protocol = Protocol.find("from Protocol p where id>=0")
	    println "protocol: ${protocol}"
            //def description = EventDescription.find("from EventDescription e where id>=0")
	    //println "description: ${description}"
	    def description=new EventDescription();
            render( view:"showMyProtocolFilled", model:[protocol:protocol,description:description] )
        }
        else {
            def description = EventDescription.get(params.id)
	    render( view: "showMyProtocolFilled", model:[protocol:description.protocol,description:description] )
        }
    }


    def showMyProtocolEmpty = {
       println "in showMyProtocolEmpty"
    }


    def showMyProtocolFilled = {
       println "in showMyProtocolFilled"
    }






    def update = {
        println "update"
        print params

        def eventDescriptionInstance = EventDescription.get(params.id)
        if (eventDescriptionInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (eventDescriptionInstance.version > version) {
                    
                    eventDescriptionInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'eventDescription.label', default: 'EventDescription')] as Object[], "Another user has updated this EventDescription while you were editing")
                    render(view: "edit", model: [eventDescriptionInstance: eventDescriptionInstance])
                    return
                }
            }
            eventDescriptionInstance.properties = params
            if (!eventDescriptionInstance.hasErrors() && eventDescriptionInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), eventDescriptionInstance.id])}"
                redirect(action: "show", id: eventDescriptionInstance.id)
            }
            else {
                render(view: "edit", model: [eventDescriptionInstance: eventDescriptionInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), params.id])}"
            redirect(action: "list")

/*
        // old shit
        def eventDescriptionInstance = EventDescription.get(params.id)
        if (eventDescriptionInstance) {
            try {
                eventDescriptionInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), params.id])}"
            redirect(action: "list")
        }
	*/
    }


    def test = { render(params) }

    def test2 = {
        def eventDescriptionInstance = EventDescription.get(params.id)
        if (!eventDescriptionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [eventDescriptionInstance: eventDescriptionInstance]
        }
    }


    def addProtocolParameter = {
        render( action:"showProtocolParameters", model:['addNew':true] )
    }


    def showProtocolParameters = {
        def description = EventDescription.get(params.id)
	def protocol = []
        def list = []

        if(description!=null) {                           // editing an existing EventDescription
            protocol = description.protocol==null ? new Protocol() : description.protocol
	}
	else {                                            // creating a new EventDescription
           protocol=Protocol.find("from Protocol p where id>=0")
	}

        protocol.parameters.each {
	    list.add(it)
            list.sort{ a,b -> a.name <=> b.name }
        }

        render( view:"showProtocolParameters", model:[protocol:protocol,description:description,list:list] )
    }

}
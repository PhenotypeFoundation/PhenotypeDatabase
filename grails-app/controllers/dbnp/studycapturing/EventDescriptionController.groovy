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
    }





    def collectParametersFromForm( params ) {

            def parameters = [:]
            def options    = [:]

            params.each { key, value ->
                 if(key=~/row_(.*)__(.*)/) {
                      def matcher = (key=~/row_(.*?)__(.*?)$/)
                      def id = matcher[0][1]
		      def member = matcher[0][2]

		      if(member=~/parameterStringValue__(.*?)$/) {
                          matcher = member=~/parameterStringValue__(.*?)$/
		          def psv = matcher[0][1]
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


            // collect options (i.e., ParameterStringListItem from parameters)

           parameters.each{ key, value ->
               if(value.type==ProtocolParameterType.STRINGLIST) {
                    def parameter = parameters[key]
               }
           }


	   return [parameters,options]
    }



    // convenience method for save()
    def saveParameterStringListItem(item) {

        if (item.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'description.label', default: 'Item'), item.id])}"
        }
	else {
            item.errors.each { println it }
	}

    }


    // convenience method for save()
    def saveParameter(item) {

        if (item.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'description.label', default: 'Parameter'), item.id])}"
        }
	else {
            item.errors.each { println it }
	}

    }






    def save = {
        

        def description = null      // the variable to be updated



        // create a new event from scratch

	if( !(params['id']=~/^[\d]+$/) ) {
            description = new EventDescription()
	}
	else { 
	    description = EventDescription.get(params['id'])
	}

            description.name=params['name']

            description.description=params['description']
	    // description.classification=params['classification']  // has to be a Term
	    description.isSamplingEvent= params['isSample']=='true'?true:false

	    def protocol = Protocol.get(params['protocol'])   // the protocol




            // STEP 1 parse params and fill hashes
	    def para_opt = collectParametersFromForm(params)

            def parameters = para_opt[0]      // parameters given by the user (in the view)
                                              // key: id (as string), value: non-persistant ProtocolParameter object

            def options = para_opt[1]         // store options, i.e. ParameterStringListItems given by the user.
                                              // use ids of parameter as key, store hash of id and name.

            def originalParameters




           // STEP 2  -  remove deleted parameters (from persistent protocol description)
	   protocol.parameters.each{
               def toRemove = []
               if( parameters[it.id.toString()]==null ) { toRemove.push(it) }
               toRemove.each{ protocol.removeFromParameters(it) }
	   }



           // STEP 3  -  update altered parameters
	   protocol.parameters.each{ 

               def inDb = ProtocolParameter.get(it.id)
	       def originalListEntries = inDb.listEntries.collect{it}
               def itemsToBeRemoved = []

               if(  parameters[inDb.id.toString()] != null )
	       {
                   def found = parameters[inDb.id.toString()]

                   // update options (i.e. ParameterStringListItem objects) of parameter inDb
       	           if(inDb.type==ProtocolParameterType.STRINGLIST ) {

		           if(found.type==ProtocolParameterType.STRINGLIST ) {

                                // add or modifiy options for existing parameters
                                options[inDb.id.toString()].each{ id, name ->

                                    def item = inDb.listEntries.find{ it.id.toString()==id }
				    if(item==null) {  // add as new option to persistant parameter
				        item = new ParameterStringListItem()
				        item.name = name
				        saveParameterStringListItem(item)
				        inDb.addToListEntries(item)
                                        inDb.save(flush: true)     // needed??
				    }
				    else {       // update persistant paramter
                                        item.name = name
				    }
                                }

		           }

		           else {                                                       // remove all options because the parameter type has changed
                               inDb.listEntries.each{ itemsToBeRemoved.push( it ) }
		           }
                   }


                   ['name','type','description','unit' ].each {           // references are missing
		       inDb.setProperty(it,found.getProperty(it))         // type has to be set after checking for
	           }                                                      // STRINGLIST above.
               }

	       // delete all options removed by the user
               originalListEntries.each { original ->
		       // if the original is not found in the user's modifications
                       def allOptionsForParameters = options[inDb.id.toString()]
		       if( allOptionsForParameters==null || allOptionsForParameters[original.id.toString()]==null )
		           { itemsToBeRemoved.push( original ) }
	       }
	       itemsToBeRemoved.each { 
                   inDb.removeFromListEntries(it)
	       }
          }


           // STEP 4  - add new parameters

            // find new parameters, added by user
	    // remove all other parameters from paramters list
	    def newParameters = [:]
	    parameters.each{ key, value ->
                if( ! protocol.parameters.find {it.id.toString()==key} ) {
		    newParameters[key]=value
		}
	    }
	    parameters=newParameters




            //  add new parameters (to persistent protocolDescription)
            parameters.each { id, parameter->
                if( id=~/new/ )
		{
                    def newParameter = new ProtocolParameter()                           // update properties
                    ['name','type','description','unit'].each {
                        newParameter.setProperty( it, parameter.getProperty(it) )
	            }

                    if(parameter.type==ProtocolParameterType.STRINGLIST) {               // update options
                         options[id].each { someKey, name ->
		             def item = new ParameterStringListItem()
			     item.name=name
                             parameter.addToListEntries(item)
                             saveParameterStringListItem(item)
		         }
                    }
		    saveParameter(newParameter)
                    protocol.addToParameters(parameter)
		}
	    }


	// Check for errors in protocol, and if none, persist it
	protocol.validate()
	if (protocol.hasErrors()) {
		render "Errors during save of protool:\n"
		for (e in description.errors) {
			render e
		}
	}
	else {
		protocol.save(flush:true)
	}


	// Important: add protocol to EventDescription
	description.protocol = protocol


	// Check for errors in EventDescription, if no errors, persist the data
	description.validate()
	if (description.hasErrors()) {
		render "Errors during save of event description:\n"
		for (e in description.errors) {
			render e
		}
	}
	else {
		if (description.save(flush: true)) {
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'description.label', default: 'EventDescription'), description.id])}"
		    redirect(view: "show", id: description.id)

		}
		else {
		    redirect(view: "create", model: [description: description])
		}
	}

    }



    def show = {

        def eventDescriptionInstance = EventDescription.get(params.id)
        if (!eventDescriptionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), params.id])}"
            redirect(view: "list")
        }

        else {
            [eventDescriptionInstance: eventDescriptionInstance, params:params]
	    // Since show.gsp is not implemented yet, redirect to edit
	    redirect(action:'edit',id:params.id)
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

        if( EventDescription.get(params.id)==null || EventDescription.get(params.id).protocol==null ) {
	    def description=new EventDescription();
            render( view:"showMyProtocolFilled", model:[protocol:null,description:description] )
        }
        else {
            def description = EventDescription.get(params.id)
	    render( view: "showMyProtocolFilled", model:[protocol:description.protocol,description:description] )
        }
    }



    def showPartial = {
        def description = EventDescription.get(params['protocolid'])
	def event       = Event.get(params['id'])
	render( view: "showPartial", model:[description:description,event:event] )    // error handling missing
    }




    def showMyProtocolEmpty = {
    }


    def showMyProtocolFilled = {
    }






    def update = {
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
    }



    def addProtocolParameter = {
        render( action:"showProtocolParameters", model:['addNew':true] )
    }


    def showProtocolParameters = {
        def description = EventDescription.get(params.id)
	def protocol = null
        def list = []

        if(description!=null) {                           // editing an existing EventDescription
            protocol = description.protocol==null ? new Protocol() : description.protocol
                protocol.parameters.each {
	            list.add(it)
                    list.sort{ a,b -> a.name <=> b.name }
		}
        }

        render( view:"showProtocolParameters", model:[protocol:null,description:description,list:list] )
    }

}
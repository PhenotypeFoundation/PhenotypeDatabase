/**
 * Visualize Controller
 *
 * This controller enables the user to visualize his data
 *
 * @author  robert@thehyve.nl
 * @since	20110825
 * @package	dbnp.visualization
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.visualization

import dbnp.studycapturing.*;
import grails.converters.JSON
import org.dbnp.gdt.*

class VisualizeController {
	def authenticationService
	def moduleCommunicationService
	
	/**
	 * Shows the visualization screen
	 */
    def index = {
		[ studies: Study.giveReadableStudies( authenticationService.getLoggedInUser() )]
	}
	
	def getStudies = {
		def studies = Study.giveReadableStudies( authenticationService.getLoggedInUser() );
		render studies as JSON
	}
	
	def getFields = {
        def input_object
        def studies

        try{
            input_object = JSON.parse(params.get('data'))
            studies = input_object.get('studies').id
        } catch(Exception e) {
            // TODO: properly handle this exception
            println e
        }

        def fields = [];
        studies.each {
            /*
            Gather fields related to this study from GSCF.
            This requires:
              - a study.
              - a category variable, e.g. "events".
              - a type variable, either "domainfields" or "templatefields".
            */
            def study = Study.get(it)
            fields += getFields(study, "subjects", "domainfields")
            fields += getFields(study, "subjects", "templatefields")
            fields += getFields(study, "events", "domainfields")
            fields += getFields(study, "events", "templatefields")
            fields += getFields(study, "samplingEvents", "domainfields")
            fields += getFields(study, "samplingEvents", "templatefields")
            fields += getFields(study, "assays", "domainfields")
            fields += getFields(study, "assays", "templatefields")
            fields += getFields(study, "samples", "domainfields")
            fields += getFields(study, "samples", "domainfields")


            /*
            Gather fields related to this study from modules.
            This will use the getMeasurements RESTful service. That service returns measurement types, AKA features.
            It does not actually return measurements (the getMeasurementData call does).
            The getFields method (or rather, the getMeasurements service) requires one or more assays and will return all measurement
            types related to these assays.
            So, the required variables for such a call are:
              - a source variable, which can be obtained from AssayModule.list() (use the 'name' field)
              - a list of assays, which can be obtained with study.getAssays()
             */
            AssayModule.list().each { module ->
                def list = []
                list = getFields(module.name, study.getAssays())
                if(list!=null){
                    if(list.size()!=0){
                        fields += list
                    }
                }
            }

            // TODO: Maybe we should add study's own fields
        }

		render fields as JSON
	}
	
	def getVisualizationTypes = {
		def types = [ [ "id": "barchart", "name": "Barchart"] ];
		render types as JSON
	}
	
	def getData = {
        println params
        def input_object
        def studies
        def rows
        def columns
        def vizualisation_type

        try{
            input_object = JSON.parse(params.get('data'))
            studies = input_object.get('studies')
            rows = input_object.get('rows')
            columns = input_object.get('columns')
            vizualisation_type = "barchart"
        } catch(Exception e) {
            // TODO: properly handle this exception
            println e
        }

        def data = [:]

        Collection row_data = []
        Collection column_data = []
		studies.each {
            // TODO: Get rid of code duplication
            def study = Study.get(it.id)
            rows.eachWithIndex { r, index ->
                println "  - field "+r
                def case_switch
                def input_id = r.id.split(",")
                def field_id = input_id[0]
                def source_module = input_id[1]
                def field_type = input_id[2]
                def field_name = input_id[3]
                def templatefield_source
                if(source_module=="GSCF"){
                    if(field_type!=TemplateField.class.toString()){
                        case_switch = "domain"
                    } else {
                        templatefield_source = input_id[4]
                    }
                    row_data[index] = getFieldData(study, case_switch, field_id, field_name, source_module, templatefield_source, field_type)
                } else {
                    // Grabbing field data from a module
                    row_data[index] = getFieldData(study, "", field_id, "", source_module, "", "")
                }
            }

            columns.eachWithIndex { r, index ->
                def case_switch
                def input_id = r.id.split(",")
                def field_id = input_id[0]
                def source_module = input_id[1]
                def field_type = input_id[2]
                def field_name = input_id[3]
                def templatefield_source

                if(source_module=="GSCF"){
                    if(field_type!=TemplateField.class.toString()){
                        case_switch = "domain"
                    } else {
                        templatefield_source = input_id[4]
                    }
                    column_data[index] = getFieldData(study, case_switch, field_id, field_name, source_module, templatefield_source, field_type)
                } else {
                    // Grabbing field data from a module
                    column_data[index] = getFieldData(study, "", field_id, "", source_module, "", "")
                }
            }
        }

        if(row_data.size()!=0 && column_data.size()!=0 && row_data[0].size()!=0 && column_data[0].size()!=0){
            // Going to build the return object now
            def return_data = [:]
            def series = []
            def possible_xaxis_title = ""
            def possible_yaxis_title = ""
            return_data.put("type", vizualisation_type)
            if(vizualisation_type=='barchart'){

                // Determining what different bars we need (x-axis)
                def list_of_row_contents = []
                rows.eachWithIndex { r, j ->
                    row_data[j].each { datapoint ->
                        list_of_row_contents.add(datapoint)
                    }
                }
                def bars = []
                // Make the list unique and stringify the individual objects
                list_of_row_contents.unique().each {
                    item ->
                    bars << item.toString()
                }
                bars.sort()
                return_data.put("x", bars)
                // Determining what different bars we need (x-axis)

                // Determine the different categories that datapoints can fall under
                def categories = []
                columns.eachWithIndex { c, i ->
                    column_data[i].each {
                        cd ->
                        categories << cd
                    }
                }
                categories.unique().sort()
                
                // Looking at the actual datapoints ...
                columns.eachWithIndex { column, column_index ->
                    // ... for each column
                    categories.each { category ->
                        def data_per_bar = [:] // To store the datapoints contained in the current category, ordered by bar
                        // Make an entry for each of the bars that will be in the barchart
                        list_of_row_contents.each { bar ->
                            data_per_bar.put(bar, 0)
                        }
                        rows.eachWithIndex { row, row_index ->
                            // ... check for each row what it contains for each bar and category combination
                            // TODO: properly determine axis titles
                            if(possible_xaxis_title==""){
                                possible_xaxis_title = row.id.split(',')[3]
                            }
                            list_of_row_contents.each { bar ->
                                // Check for the current bar, how many datapoints each category has
                                column_data[column_index].eachWithIndex { cd, cdi ->
                                    if(bar==row_data[row_index][cdi] && cd==category){
                                        // Apparently this column contains a datapoint, whose entry in the relevant row equals the bar we are currently looking for and whose column equals the category that we are currently checking for. What this means is that the current datapoint should be included in this series
                                        data_per_bar.put(bar, data_per_bar.get(bar)+1)
                                    }
                                }
                            }
                        }
                        // Now add the data in the correct order (corresponding to bar order, so that the values end up in the right bar)
                        def data_per_bar_sorted = []
                        list_of_row_contents.each { bar ->
                            data_per_bar_sorted.add(data_per_bar.get(bar))
                        }
                        series.add(["name":category.toString(),"y":data_per_bar_sorted])
                    }
                }

                if(possible_yaxis_title==""){
                    // TODO: properly determine axis titles
                    possible_yaxis_title = "Amount"
                    if(possible_xaxis_title!=""){
                        possible_yaxis_title += " of each "+possible_xaxis_title
                    }
                }
                return_data.put("yaxis", ["title" : possible_yaxis_title, "unit" : "..."])
                return_data.put("xaxis", ["title" : possible_xaxis_title, "unit": "..."])
                return_data.put("series", series)
                data = return_data
            }
        } else {
            // TODO: handle this exception properly
            // We couldn't get any data to display...
        }
        println "\n\nReturn object: "+(data as JSON)+" ... "
        render data as JSON
	}

    def getFieldData(study, case_switch, field_id, field_name, source, templatefield_source, field_type){
        if(source=="GSCF"){
            if(case_switch=="domain"){
                if(!study.getProperty(field_type)){
                    // TODO: handle this exception properly
                    println "getFieldData: domainfield: Requested property '"+field_type+"' does not appear to exist in the study '"+study+"'."
                    return
                }
                def domain_objects = study.getProperty(field_type) // Simple way of getting at the relevant domain objects

                if(domain_objects==null){
                    // TODO: handle this exception properly
                    println "getFieldData: domainfield: A problem occurred... Nothing was collected."
                }

                // Get the value of the requested field out of the domain objects
                def dat = []
                domain_objects.each{
                    try{
                        dat.add(it.getFieldValue(field_name))
                        //println "getFieldData: domainfield: *** It appears as though we were successful"
                    } catch(Exception e){
                        // TODO: handle this exception properly
                        println "getFieldData: domainfield: A problem occurred... "+e
                    }
                }
                return dat
            } else {
                TemplateField tf
                try{
                    tf = TemplateField.get(field_id)
                } catch (Exception e){
                    // TODO: handle this exception properly
                    println "getFieldData: templatefield: A problem occurred... "+e
                }
                def dat = []
                def collection
                if(templatefield_source=="subjects"){
                    collection = study.getSubjects()
                }
                if(templatefield_source=="assays"){
                    collection = study.getAssays()
                }
                if(templatefield_source=="events"){
                    collection = study.getEvents()
                }
                if(templatefield_source=="samplingEvents"){
                    collection = study.getSamplingEvents()
                }
                if(templatefield_source=="samples"){
                    collection = study.getSamples()
                }
                if(collection==null){
                    // TODO: handle this exception properly
                    println "getFieldData: templatefield: A problem occurred... Nothing was collected."
                }
                collection.each {
                    try{
                        dat.add(it.getFieldValue(tf.name))
                    } catch(Exception e){
                        // TODO: handle this exception properly
                        println "getFieldData: templatefield: A problem occurred... "+e
                    }
                }
                return dat
            }
        } else {
            // Request for module data
            def dat = []

            // User requested a particular feature
            study.getAssays().each { assay ->
                // Request for a particular assay and a particular feature
                def urlVars = "assayToken="+assay.assayUUID+"&measurementToken="+field_id
                def callUrl
                AssayModule.list().each { module ->
                    if(source==module.name){
                        try {
                            callUrl = module.url + "/rest/getMeasurementData/query?"+urlVars
                            def json = moduleCommunicationService.callModuleRestMethodJSON( module.url, callUrl );
                            // First element contains sampletokens
                            // Second element contains the featurename
                            // Third element contains the measurement value
                            // NOTE: There is no need to couple a measurement value to a sampletoken, because that just doesn't produce interesting data
                            json[2].each { val ->
                                dat << val
                            }
                        } catch(Exception e){
                            // TODO: handle this exception properly
                            println "No success with\n\t"+callUrl+"\n"+e
                            return null
                        }
                    }
                }
            }
            return dat
        }
    }

    def getFields(source, assays){
        /*
        Gather fields related to this study from modules.
        This will use the getMeasurements RESTful service. That service returns measurement types, AKA features.
        It does not actually return measurements (the getMeasurementData call does).
        The getFields method (or rather, the getMeasurements service) requires one or more assays and will return all measurement
        types related to these assays.
        So, the required variables for such a call are:
          - a source variable, which can be obtained from AssayModule.list() (use the 'name' field)
          - a list of assays, which can be obtained with study.getAssays()
         */
        def collection = []
        def callUrl = ""

        // Making a different call for each assay
        // TODO: Change this to one call that requests fields for all assays, when you get that to work (in all cases)
        assays.each { assay ->
            def urlVars = "assayToken="+assay.assayUUID
            AssayModule.list().each { module ->
                if(source==module.name){
                    try {
                        callUrl = module.url + "/rest/getMeasurements/query?"+urlVars
                        def json = moduleCommunicationService.callModuleRestMethodJSON( module.url, callUrl );
                        json.each{ jason ->
                            collection.add(jason)
                        }
                    } catch(Exception e){
                        // Todo: properly handle this exception
                        println "No success with\n\t"+callUrl+"\n"+e
                        return null
                    }
                }
            }
        }

        def fields = []
        // Formatting the data
        collection.each { field ->
            fields << [ "id": field+","+source+","+"feature"+","+field, "source": source, "category": "feature", "name": source+" feature "+field ]
        }
        return fields
    }
    
    def getFields(study, category, type){
        /*
        Gather fields related to this study from GSCF.
        This requires:
          - a study.
          - a category variable, e.g. "events".
          - a type variable, either "domainfields" or "templatefields".
        */

        // Collecting the data from it's source
        def collection
        def fields = []
        def source = "GSCF"

        // Gathering the data
        if(category=="subjects"){
            if(type=="domainfields"){
                collection = Subject.giveDomainFields()
            }
            if(type=="templatefields"){
                collection = study.giveSubjectTemplates().fields
            }
        }
        if(category=="events"){
            if(type=="domainfields"){
                collection = Event.giveDomainFields()
            }
            if(type=="templatefields"){
                collection = study.giveEventTemplates().fields
            }
        }
        if(category=="samplingEvents"){
            if(type=="domainfields"){
                collection = SamplingEvent.giveDomainFields()
            }
            if(type=="templatefields"){
                collection = study.giveSamplingEventTemplates().fields
            }
        }
        if(category=="samples"){
            if(type=="domainfields"){
                collection = Sample.giveDomainFields()
            }
            if(type=="templatefields"){
                collection = study.giveEventTemplates().fields
            }
        }
        if(category=="assays"){
            if(type=="domainfields"){
                collection = Event.giveDomainFields()
            }
            if(type=="templatefields"){
                collection = study.giveEventTemplates().fields
            }
        }

        // Formatting the data
        if(type=="domainfields"){
            collection.each { field ->
                fields << [ "id": field.name+","+source+","+category+","+field.name, "source": source, "category": category, "name": category.capitalize()+" "+field.name ]
            }
        }
        if(type=="templatefields"){
            collection.each { field ->
                for(int i = 0; i < field.size(); i++){
                    fields << [ "id": field[i].id+","+source+","+TemplateField.toString()+","+field[i].name+","+category, "source": source, "category": category, "name": category.capitalize()+" "+field[i].name ]
                }
            }
        }

        return fields
    }
}

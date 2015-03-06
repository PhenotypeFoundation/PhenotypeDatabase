package org.dbxp.sam

import grails.converters.JSON
import org.dbnp.gdt.Template

import org.dbxp.matriximporter.MatrixImporter
import org.dbnp.gdt.TemplateFieldType

class FeatureController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
    def fuzzySearchService
    def moduleService

    def index = {
        redirect(action: "list", params: params)
    }
	
    def list = {
        if (moduleService.validateModule(params?.module)) {
            [module: params.module]
        }
        else {
            redirect(controller: 'error', action: 'notFound')
        }
    }
	
	/**
	 * Returns data for datatable. 
	 * @see ! http://www.datatables.net/usage/server-side
	 */
	def datatables_list = {
		/*	Input:
			int 	iDisplayStart 		Display start point in the current data set.
			int 	iDisplayLength 		Number of records that the table can display in the current draw. It is expected that the number of records returned will be equal to this number, unless the server has fewer records to return.
			int 	iColumns 			Number of columns being displayed (useful for getting individual column search info)
			string 	sSearch 			Global search field
			bool 	bRegex 				True if the global filter should be treated as a regular expression for advanced filtering, false if not.
			bool 	bSearchable_(int) 	Indicator for if a column is flagged as searchable or not on the client-side
			string 	sSearch_(int) 		Individual column filter
			bool 	bRegex_(int) 		True if the individual column filter should be treated as a regular expression for advanced filtering, false if not
			bool 	bSortable_(int) 	Indicator for if a column is flagged as sortable or not on the client-side
			int 	iSortingCols 		Number of columns to sort on
			int 	iSortCol_(int) 		Column being sorted on (you will need to decode this number for your database)
			string 	sSortDir_(int) 		Direction to be sorted - "desc" or "asc".
			string 	mDataProp_(int) 	The value specified by mDataProp for each column. This can be useful for ensuring that the processing of data is independent from the order of the columns.
			string 	sEcho 				Information for DataTables to use for rendering.
		 */

		// Display parameters
		int displayStart = params.int( 'iDisplayStart' );
		int displayLength = params.int( 'iDisplayLength' );
		int numColumns = params.int( 'iColumns' );
		
		// Search parameters; searchable columns are determined serverside
		String search = params.sSearch;

		// Sort parameters
		int sortingCols = params.int( 'iSortingCols' );
		List sortOn = []
		for( int i = 0; i < sortingCols; i++ ) {
			sortOn[ i ] = [ 'column': params.int( 'iSortCol_' + i ), 'direction': params[ 'sSortDir_' + i ] ];
		}
		
		// What columns to return?
		def columns = [ 'f.platform.name', 'f.name', 'f.unit', 't.name' ]
		
		// Create the HQL query
		def hqlParams = [:];
		def hql = "FROM Feature f LEFT JOIN f.template as t ";
		def orderHQL = "";
		
		// Search properties
		if( search ) {
			hqlParams[ "search" ] = "%" + search.toLowerCase() + "%"
			
			def hqlConstraints = [];
			for( int i = 0; i < 3; i++ ) {
				hqlConstraints << "LOWER(" + columns[ i ] + ") LIKE :search"
			}
			
			hql += "WHERE (" + hqlConstraints.join( " OR " ) + ") "
		}
			
		// Sort properties
		if( sortOn ) {
			orderHQL = "ORDER BY " + sortOn.collect { columns[it.column] + " " + it.direction }.join( " " );
		}

        // Filter by module
        def totalFilteredFeatures = 0
        def String moduleFilter = ""

        Platform.findAllByPlatformtype(params.module).each {
            if (search) {
                if (!moduleFilter) {
                    moduleFilter = " AND (platform_id = $it.id "
                }
                else {
                    moduleFilter += "OR platform_id = $it.id "
                }
            }
            else {
                if (!moduleFilter) {
                    moduleFilter = " WHERE platform_id = $it.id "
                }
                else {
                    moduleFilter += "OR platform_id = $it.id "
                }
            }
            totalFilteredFeatures += Feature.executeQuery("SELECT COUNT(*) FROM Feature WHERE platform.id = :platform", [ platform: it.id ])[0]
        }
        if (search) {
            moduleFilter += ")"
        }

        // Display properties
		def records = Feature.executeQuery( hql + moduleFilter + orderHQL, hqlParams, [ max: displayLength, offset: displayStart ] );
		def numTotalRecords = totalFilteredFeatures
		def filteredRecords = Feature.executeQuery( "SELECT f.id " + hql + moduleFilter + orderHQL, hqlParams );
		
		/*
		int 	iTotalRecords 			Total records, before filtering (i.e. the total number of records in the database)
		int 	iTotalDisplayRecords 	Total records, after filtering (i.e. the total number of records after filtering has been applied - not just the number of records being returned in this result set)
		string 	sEcho 					An unaltered copy of sEcho sent from the client side. This parameter will change with each draw (it is basically a draw count) - so it is important that this is implemented. Note that it strongly recommended for security reasons that you 'cast' this parameter to an integer in order to prevent Cross Site Scripting (XSS) attacks.
		string 	sColumns 				Optional - this is a string of column names, comma separated (used in combination with sName) which will allow DataTables to reorder data on the client-side if required for display. Note that the number of column names returned must exactly match the number of columns in the table. For a more flexible JSON format, please consider using mDataProp.
		array 	aaData 					The data in a 2D array. Note that you can change the name of this parameter with sAjaxDataProp.
		*/



		def returnValues = [
			iTotalRecords: numTotalRecords,
			iTotalDisplayRecords: filteredRecords.size(),
			sEcho: params.int( 'sEcho' ),
			aaData: records.collect {
				[ it[0].id, it[0].platform.name, it[0].name, it[0].unit, it[1]?.name,
                    dt.buttonShow(id: it[0].id, controller: "feature", blnEnabled: true, params: [module: params.module]),
                    dt.buttonEdit(id: it[0].id, controller: "feature", blnEnabled: true, params: [module: params.module]),
                    dt.buttonDelete(id: it[0].id, controller: "feature", blnEnabled: true, params: [module: params.module])]
			},
			aIds: filteredRecords
		]
		
		response.setContentType( "application/json" );
		render returnValues as JSON
		
	}

    def create = {
        if (moduleService.validateModule(params?.module)) {
            def featureInstance = new Feature()
            featureInstance.properties = params
            return [featureInstance: featureInstance, module: params.module]
        }
        else {
            redirect(controller: 'error', action: 'notFound')
        }
    }

    def save = {
        def featureInstance = new Feature()

        // Was study template set on the 'create page'?
        if( params.template) {
            // Yes, so add the template
            featureInstance.changeTemplate( params.template );
        }

        // does the study have a template set?
        if (featureInstance.template) {
            // yes, iterate through template fields
            featureInstance.giveFields().each() {
                // and set their values
                if(it.type==TemplateFieldType.BOOLEAN){ // This is a hack that allows us to set templatefields with type 'BOOLEAN'
                    def value = params.get(it.escapedName()+"_"+it.escapedName())!=null // '' becomes true, and null becomes false, as intended.
                    featureInstance.setFieldValue(it.name, value)
                } else {
                    featureInstance.setFieldValue(it.name, params.get(it.escapedName()+"_"+it.escapedName()))
                }
            }
        }

        def platform = params.long('platform')
        if (platform) featureInstance.platform = Platform.get(platform)
        params.remove('platform')

        // Remove the template parameter, since it is a string and that troubles the
        // setting of properties.
        def template = params.remove( 'template' )

        featureInstance.properties = params

        // Trim the whitespace from the name and (if available) the unit, to enable accurate validation
        featureInstance.name = featureInstance.name?.trim()
        featureInstance.unit = featureInstance.unit?.trim()
        if (featureInstance.save(flush: true)) {
            flash.message = "The feature ${featureInstance.name} has been created."
            if(params?.nextPage=="minimalCreate"){
                redirect(action: "minimalCreate", params: [module: params.module])
            } else {
                redirect(action: "list", params: [module: params.module])
            }
        }
        else {
            if(params?.nextPage=="minimalCreate"){
                render(view: "minimalCreate", model: [featureInstance: featureInstance], module: params.module)
            } else {
                render(view: "create", model: [featureInstance: featureInstance], module: params.module)
            }
        }
    }

    def show = {
        if (moduleService.validateModule(params?.module)) {
            def featureInstance = Feature.get(params.id)
            if (!featureInstance) {
                flash.message = "The requested feature could not be found."
                redirect(action: "list", params: [module: params.module])
            }
            else {
                [featureInstance: featureInstance, module: params.module]
            }
        }
        else {
            redirect(controller: 'error', action: 'notFound')
        }
    }

    def edit = {
        if (moduleService.validateModule(params?.module)) {
            def featureInstance = Feature.get(params.id)
            session.featureInstance = featureInstance
            if (!featureInstance) {
                flash.message = "The requested feature could not be found."
                redirect(action: "list", params: [module: params.module])
            }
            else {
                // Store session template id, so it will show up correctly after
                // opening the template editor. See also _determineTemplate
                session.templateId = featureInstance.template?.id

                return [featureInstance: featureInstance, module: params.module]
            }

        }
        else {
            redirect(controller: 'error', action: 'notFound')
        }
    }

    def update = {
        def featureInstance = Feature.get(params.id)
        if (featureInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (featureInstance.version > version) {

                    featureInstance.errors.rejectValue("", "Another user has updated this feature while you were editing. Because of this, your changes have not been saved to the database.")
                    render(view: "edit", model: [featureInstance: featureInstance], module: params.module)
                    return;
                }
            }
			
			// did the study template change?
			if( params.template && params.template != featureInstance.template?.name ) {
				featureInstance.changeTemplate( params.template );
			}

            // does the study have a template set?
            if (featureInstance.template) {
                // yes, iterate through template fields
                featureInstance.giveFields().each() {
                    // and set their values
                    if(it.type==TemplateFieldType.BOOLEAN){ // This is a hack that allows us to set templatefields with type 'BOOLEAN'
                        def value = params.get(it.escapedName()+"_"+it.escapedName())!=null // '' becomes true, and null becomes false, as intended.
                        featureInstance.setFieldValue(it.name, value)
                    } else {
                        featureInstance.setFieldValue(it.name, params.get(it.escapedName()+"_"+it.escapedName()))
                    }
                }
            }

            def platform = params.long('platform')
            if (platform) featureInstance.platform = Platform.get(platform)
            params.remove('platform')

            // Remove the template parameter, since it is a string and that troubles the
			// setting of properties.
			params.remove( 'template' ) 
			
            featureInstance.properties = params
           
            // Trim the whitespace from the name and (if available) the unit, to enable accurate validation
            featureInstance.name = featureInstance.name?.trim()
            featureInstance.unit = featureInstance.unit?.trim()
            
            if (!featureInstance.hasErrors() && featureInstance.save(flush: true)) {
                flash.message = "The feature has been updated."
                redirect(action: "show", id: featureInstance.id, params: [module: params.module])
            }
            else {
                render(view: "edit", model: [featureInstance: featureInstance], module: params.module)
            }
        }
        else {
            flash.message = "The requested feature could not be found."
            redirect(action: "list", params: [module: params.module])
        }
    }

    def delete = {
        def ids = params.list( 'ids' ).findAll { it.isLong() }.collect { it.toDouble() };
		
		if( !ids ) {
			response.sendError( 404 );
			return;
		}

        def return_map = [:]
        return_map = Feature.delete(ids)
        if(return_map["message"]){
            flash.message = return_map["message"]
        }

        if(return_map["action"]){
            redirect(action: return_map["action"], params: [module: params.module])
        } else {
            redirect(action:list, params: [module: params.module])
        }
    }
		
    // Get a list of template specific fields
    def templateSelection = {
        render(template: "templateSelection", model: [template: _determineTemplate()], module: params.module)
    }
	
	def returnUpdatedTemplateSpecificFields = {
		def template = _determineTemplate();
		def values = [:];
		
		// Set the correct value of all domain fields and template fields (if template exists) 
		try {
			if( template ) {
				template.fields.each {
					values[it.escapedName()] = params.get(it.escapedName()+"_"+it.escapedName());
				}
			}
		} catch( Exception e ) {
			log.error( e );
		}

		render(template: "templateSpecific", model: [template: template, values: values], module: params.module)
    }
	
	/**
	 * Returns the template that should be shown on the screen
	 */
	private _determineTemplate()  {
		def template = null;
		if( params.templateEditorHasBeenOpened == 'true') {
			// If the template editor has been opened (and closed), we should use
			// the template that we stored previously
			if( session.templateId ) {
				template = Template.get( session.templateId );
			} 
		} else {
			// Otherwise, we should use the template that the user selected.
			if( params.template ) {
                Template.findAllByEntity(Feature).each {
                    if(it.name == params.template) {
                        template = it
                        return template
                    }
                }
			}
		}
		
		// Store the template id in session, so the system will know the previously
		// selected template
		session.templateId = template?.id
		
		return template;
	}

    def updateTemplate = {
        // A different template has been selected, so all the template fields have to be removed, added or updated with their previous values (they start out empty)
        if(!session.featureInstance.isAttached()){
           session.featureInstance.attach()
        }
        try {
            if(params.template==""){
                session.featureInstance.template = null
            } else if(params?.template && session?.featureInstance.template?.name != params.get('template')) {
                // set the template
                Template.findAllByEntity(Feature).each {
                    if (it.name == params.template) {
                        session.featureInstance.template = it
                    }
                }
            }
            // does the study have a template set?
            if (session.featureInstance.template && session.featureInstance.template instanceof Template) {
                // yes, iterate through template fields
                session.featureInstance.giveFields().each() {
                    // and set their values
                    session.featureInstance.setFieldValue(it.name, params.get(it.escapedName()))
                }
            }
        } catch (Exception e){
           log.error(e)
            e.printStackTrace()
           // TODO: Make this more informative
           flash.message = "An error occurred while updating this feature's template. Please try again.<br>${e}"
        }
    }

    /**
     * Returns a list of features as JSON
     * @return	JSON list of features with 'id' and 'name'
     */
	def ajaxList = {
		def features = Feature.list( sort: "name" );
		def lastFeature = Feature.find( "from Feature order by id desc" );
		
		def data = [ "last": [ 'id': lastFeature?.id, 'name': lastFeature?.name ] ,"features": features.collect { return [ 'id': it.id, 'name': it.name ] } ];
        render data as JSON
    }

    def minimalCreate = {
        def featureInstance = new Feature()
        def platform = params.long('platform')
        if (platform) featureInstance.platform = Platform.get(platform)
        params.remove('platform')
        featureInstance.properties = params
        return [featureInstance: featureInstance]
    }

    def minimalShow = {
        [featureInstance: params.featureInstance]
    }

    def importData = {
		redirect( action: 'importDataFlow', params: [module: params.module])
	}

    def importDataFlow = {
        startUp {
            action{

                flow.module = params.module

                flow.pages = [
                    "uploadAndSelectTemplate": "Upload",
                    "matchColumns": "Match Columns",
                    "checkInput": "Check Input",
                    "saveData": "Done"
                ]
            }

            on("success").to "uploadAndSelectTemplate"
        }

        uploadAndSelectTemplate {

            on("next") {
                flow.templateFields = null;
                flow.template = params.template;
                // Check if the user used the textarea
                if(params.pasteField!=null && params.pasteField!="") {
                    // The textarea is used
                    flow.inputField = params.pasteField;
                    flow.input = null;
                } else {
                    // The uploaded files is used
                    flow.inputfile = request.getFile('fileUpload')
                    flow.inputField = null;
                }

                flow.platform = Platform.get(params.platform);

                // Empty flow.discardRow to make sure that we don't discard the same rows as we did with a different file
                flow.discardRow = [];
            }.to "uploadDataCheck"
        }

        uploadDataCheck {
            // Check to make sure we actually received a file.
            action {
                def text = "";

                // Check if the user used the textarea
                if(flow.inputField!=null && flow.inputField!="") {
                    // Parse the content of the textarea using the Matriximporter
                    text = MatrixImporter.getInstance().importString(flow.inputField,["delimiter":"\t"],false,false);
                    // TODO Unfortunately, between the 'matchColumns''s 'previous' action and arriving
                    // here from that action, the first list item seems to get lost if it contains only
                    // one empty string. I believe this happens in the 'uploadAndSelectTemplate.gsp' view,
                    // when re-setting the inputField from the flow. This can easily be confirmed by, at
                    // strategic locations, looking at the variable. For now, to prevent this problem, I
                    // am adding a space character to the empty list item (if said item exists). This
                    // needs to be done with 'flow.inputField', not 'text' or 'flow.text'! Note: I
                    // suspect the call 'inputField?.encodeAsJavaScript()' to be the culprit, but I
                    // haven't checked.
                    if(text[0]==[""]){
                        // Unfortunately, manipulating 'text' does not help, as such a change will not be reflected in 'flow.inputField'. This is why we directly edit 'flow.inputField'
                        flow.inputField = "\n"+flow.inputField
                    }
                } else {
                    // Save the uploaded file into a variable
                    def f = flow.remove( 'inputfile' )
                    
                    if(!f.empty) {
                        // Save data of this step
                        flow.message = "It appears this file cannot be read in." // In case we get an error before finishing
                        try{
							text = MatrixImporter.getInstance().importInputStream(f.getInputStream(),[:],false,false)
                        } catch(Exception e){
                            // Something went wrong with the file...
                            flow.message += " The precise error is as follows: "+e
                            return error()
                        }

                        // Check if the uploaded file had any content
                        if(text==null){
                            // Apparently the MatrixImporter was unable to read this file
                            flow.message += ' Make sure to add a comma-separated values based or Excel based file using the upload field below.'
                            return error()
                        }

                        // Check for the largest row, and pad the rest of the rows.
                        // The reason we do this, is because empty cells are not reflected in the MatrixImporter output.
                        // This results in the table being rendered in a non-spiffy fashion, and selectors not appearing where they should.
                        // This padding occurs before the 'valid layout check', because an empty header does not pass that check.
                        int intLargestRowSize = 0
                        for(int i = 0; i < text.size(); i++){
                            if(text[i]?.size()>intLargestRowSize){
                                intLargestRowSize = text[i].size()
                            }
                        }
                        for(int i = 0; i < text.size(); i++){
                            if(text[i]?.size()<intLargestRowSize){
                                for(int j = text[i].size(); j < intLargestRowSize; j++){
                                    text[i].add("")
                                }
                            }
                        }

                        // Checking if the data has the minimum amount of information it needs (one cell that could be a header cell, one cell that could be a data cell)
                        if(text.size()<2 || text[0].size()<1){ // The text[0].size() check has been changed to <1, because having a column for units is not mandatory.
                            flow.message = "It appears the data does not have a valid layout."
                            return error()
                        }
                        // Save some variables of the file into the flow
                        flow.input = [ "file": flow.inputfile, "originalFilename": f.getOriginalFilename()]
                    } else {
                        if(flow.input==null && flow.inputField==null) {
                            // only throw the error if there is no input set at all (it might have been set in an earlier call)
                            flow.message += ' Make sure to add a file using the upload field below. The file upload field cannot be empty.'
                            return error()
                        } else {
                            println(flow.input);
                            println(flow.inputField);
                        }
                    }
                }

                // Store the content of the textarea or the file in the flow
                if(!text.equals("")) {
                    flow.text = text;
                }

                // If we've reached this point, the error message needs to be reset
                flow.message = null;

                // Store the domainfields in the flow
                flow.templateFields = Feature.domainFields

                // Store the selected template in the flow
                flow.template = params.template;

                // If a template is selected, store the templatefields in the flow
                if(flow.template!="") {
                    // Refresh the template because a user can have edited it
                    def Template objTempl
                    Template.findAllByEntity(Feature).each {
                        if (it.name == params.template) {
                            objTempl = it
                        }
                    }
                    objTempl.refresh()
                    flow.templateFields += objTempl?.fields;
                }

                // Compute fuzzy matching
                flow.columnField = [:];
                def lstFieldNames =  [];
                for(int k=0; k<flow.templateFields.size(); k++) {
                    lstFieldNames += flow.templateFields[k].escapedName();
                }
                def lstColumnHeaders = [];
                for(int j=0; j<flow.text[0].size(); j++) {
                    lstColumnHeaders += flow.text[0][j].toLowerCase();
                }

                def matches = fuzzySearchService.mostSimilarUnique( lstColumnHeaders, lstFieldNames, ['controller': 'featureImporter', 'item': 'feature']);

                for(int i=0; i<flow.text[0].size(); i++) {
                    if(matches[i].index!=null) {
                        flow.columnField.put(i,flow.templateFields[matches[i].index]);
                    }
                }

            }
            on("success").to "matchColumns"
            on("error").to "uploadAndSelectTemplate"
        }

        matchColumns {
            on("next") {
                flow.message = null;
                String newMessage = "";

                // Get the rows that need to be discarded
                flow.discardRow = [];
                flow.featureList = [];
                flow.featureAndIndexList = [:] // This list is used to retrieve the location of a feature (row-index) in the 'matchColumns.gsp' list, based on Feature name and unit. This is used to be allow the 'saveData' action to automatically un-check a Feature
                boolean blnDiscardAll = true;
                boolean bnlDuplicatesDetected = false; // Used to see if we should make the flow.message more informative
                def lstUniqueErrorItemsFromDatabase = [] // This list will hold which items in the input list triggered the error

                for(int i=1; i<flow.text.size(); i++) {
                    if(!params.get("row_"+i)) {
                        flow.discardRow.add(i);
                    } else {
                        blnDiscardAll = false;
                        Feature objFeature = new Feature();
                        if(flow.template!="")
                            objFeature.changeTemplate(flow.template);
                        for(int j=0; j<flow.text[0].size(); j++) {
                            // Trim the would-be field values
                            // This is necessary to be able to accurately search for and compare these values
                            flow.text[i][j] = flow.text[i][j].toString().trim()

                            if(params.get("column_"+j)!="") {
                                try {
                                    objFeature.setFieldValue(params.get("column_"+j),flow.text[i][j],true);
                                } catch (Exception e) {
                                    if(newMessage.length()>0) newMessage += "<br />";
                                    newMessage += "Row "+i+", column ["+params.get("column_"+j)+"] can't be set to ["+flow.text[i][j]+"]";
                                }
                            }
                        }

                        if(flow.featureAndIndexList.get(objFeature.name+","+objFeature.unit)!=null){
                            def list = flow.featureAndIndexList.get(objFeature.name+","+objFeature.unit);
                            list << i
                            flow.featureAndIndexList.put(objFeature.name+","+objFeature.unit,list);
                        } else {
                            flow.featureAndIndexList.put(objFeature.name+","+objFeature.unit,[i]);
                        }

                        objFeature.platform = flow.platform

                        objFeature.validate();
                        objFeature.getErrors().allErrors.each {
                            switch(it.code) {
                                case "nullable":
                                    if(newMessage.length()>0) newMessage += "<br />";
                                    newMessage += "The field ["+it.field+"] can't be null. ";
                                    break;
                                case "validator.invalid":
                                    if(objFeature.unit==null || objFeature.unit==""){
                                        lstUniqueErrorItemsFromDatabase.add("feature "+objFeature.name)
                                    } else {
                                        lstUniqueErrorItemsFromDatabase.add("feature "+objFeature.name+" with unit "+objFeature.unit)
                                    }
                                    // Set this feature to unchecked in the view
                                    flow.discardRow.add(i)
                                    bnlDuplicatesDetected = true
                                    break;
                                default:
                                    if(newMessage.length()>0) newMessage += "<br />";
                                    newMessage += "Errorcode ["+it.code+"] on field ["+it.field+"] with value ["+it.rejectedValue+"]";
                            }
                        }

                        if(newMessage.length()>0) flow.message = newMessage;
                        flow.featureList.add(objFeature);
                    }
                }

                if(blnDiscardAll) {
                    flow.message = "No row was checked";
                }

                // Get the columns that need to be discarded
                flow.discardColumn = [];
                flow.columnField = [:];
                blnDiscardAll = true;
                for(int j=0; j<flow.text[0].size(); j++) {
                    if(params.get("column_"+j)=="") {
                        flow.discardColumn.add(j);
                    } else {
                        blnDiscardAll = false;
                        for(int i=0; i<flow.templateFields.size(); i++) {
                            if(flow.templateFields[i].name==params.get("column_"+j)) {
                                flow.columnField.put(j,flow.templateFields[i]);
                                break;
                            }
                        }
                    }
                }

                if(blnDiscardAll) {
                    flow.message = "All columns were discarded";
                }

                if(bnlDuplicatesDetected){
                    if(lstUniqueErrorItemsFromDatabase.size()>0){
                        def message = "Unfortunately some name/unit combinations already exist. We have unchecked the relevant features for you. It concerns the following features: "+lstUniqueErrorItemsFromDatabase
                        if(flow.message!=null){
                            flow.message += message+"<br/>"+flow.message
                        } else {
                            flow.message = message
                        }
                    }
                }

                if(flow.message!=null) {
                    return error();
                }
            }.to "checkInput"
            on("previous") {
              flow.message = null;
            }.to "uploadAndSelectTemplate"
            on("error").to "matchColumns"

        }

        checkInput {
            on("save") {
                //flow.inputfile = request.getFile('fileUpload')
            }.to "saveData"
            on("previous"){
                flow.message = null;
            }.to "matchColumns"
        }

        saveData {
            action {
                flow.message = null;
                String newMessage = "";
                def newFeatureList = [];
                boolean blnUniqueErrorHasOccured = false; // Used to see if we should make the flow.message more informative
                def lstUniqueErrorItemsFromInputList = [] // This list will hold which items in the input list triggered the error
                def lstUniqueErrorItemsFromDatabase = [] // This list will hold which items in the databee triggered the error
              
                for(int i=0; i<flow.featureList.size; i++) {
                    boolean blnFeatureIsDuplicate = false
                    Feature objFeature = flow.featureList[i];
                    String strIdent = objFeature.getIdentifier();

                    // Here we check to see if this feature name and unit combination is presented more than once, in the user's input
                    // If it is we mark it as a duplicate and do not further process it
                    for(int j = 0; j < newFeatureList.size(); j++){
                        if(newFeatureList[j].name.toLowerCase()==params.get("entity_"+strIdent+"_name").toLowerCase() &&
                            (   (newFeatureList[j].unit==null && params.get("entity_"+strIdent+"_unit")=='')
                                    ||
                                (newFeatureList[j].unit==params.get("entity_"+strIdent+"_unit"))
                            )
                        ) {
                            // Now we will add all duplicates to the ignore list.
                            flow.featureAndIndexList.each{ fai ->
                                if(fai.key.toString()==(objFeature.name.toString()+","+objFeature.unit.toString())){
                                    // We found a duplicate. Add all but one entry to the discard list.
                                    fai.value.eachWithIndex { val, valIndex ->
                                        if(valIndex!=0) flow.discardRow.add(val)
                                    }
                                }
                            }
                            (objFeature.unit==null)? lstUniqueErrorItemsFromInputList.add("feature "+objFeature.name) : lstUniqueErrorItemsFromInputList.add("feature "+objFeature.name+" with unit "+objFeature.unit)
                            // This feature occurs in the list more than once. We don't need to further check the list to see if it appears again
                            blnUniqueErrorHasOccured = true;
                            blnFeatureIsDuplicate = true;
                            break
                        }
                    }
                    if(blnFeatureIsDuplicate){
                        // We already know that his is a duplicate, so we don't further process it
                        continue
                    }

                    // Set all variables from POST var
                    for(int j=0; j<flow.templateFields.size(); j++) {
                        String strFieldVal = params.get("entity_"+strIdent+"_"+flow.templateFields[j].escapedName());
                        if(flow.templateFields[j].required && strFieldVal==null) {
                            if(newMessage.length()>0) newMessage += "<br />";
                            newMessage += "Column ["+flow.templateFields[j]+"] is required";
                        } else {
                            try {
                                objFeature.setFieldValue(flow.templateFields[j].name,strFieldVal,true)
                            } catch (Exception e) {
                                if(newMessage.length()>0) newMessage += "<br />";
                                newMessage += "Column ["+flow.templateFields[j]+"] can't be set to ["+strFieldVal+"]";
                            }
                        }
                    }

                    objFeature.platform = flow.platform

                    objFeature.validate();
                    objFeature.getErrors().allErrors.each {
                        switch(it.code) {
                            case "nullable":
                                if(newMessage.length()>0) newMessage += "<br />";
                                newMessage += "The field ["+it.field+"] can't be null";
                                break;
                            case "validator.invalid":
                                if(objFeature.unit==null || objFeature.unit==""){
                                    lstUniqueErrorItemsFromDatabase.add("feature "+objFeature.name)
                                } else {
                                    lstUniqueErrorItemsFromDatabase.add("feature "+objFeature.name+" with unit "+objFeature.unit)
                                }
                                flow.discardRow.add(flow.featureAndIndexList.get(objFeature.name+","+objFeature.unit))
                                blnFeatureIsDuplicate = true
                                blnUniqueErrorHasOccured = true
                                break;
                            default:
                                if(newMessage.length()>0) newMessage += "<br />";
                                newMessage += "Errorcode ["+it.code+"] on field ["+it.field+"] with value ["+it.rejectedValue+"]";
                        }
                    }
                    if(!blnFeatureIsDuplicate) {
                        newFeatureList.add(objFeature);
                    }
                }

                lstUniqueErrorItemsFromInputList.unique()
                lstUniqueErrorItemsFromDatabase.unique()
                
                if(blnUniqueErrorHasOccured || newMessage.length()>0) {
                    flow.featureList = newFeatureList;
                    flow.message = newMessage;
                    if(blnUniqueErrorHasOccured){
                        def message = ""
                        if(flow.message!=null){
                            message = "<br/>"+flow.message
                        }
                        if(lstUniqueErrorItemsFromInputList.size()>0) flow.message = "Unfortunately some name/unit combinations occur more than once in your input. We have unchecked the relevant features for you. It concerns the following features: "+lstUniqueErrorItemsFromInputList+message
                        if(lstUniqueErrorItemsFromDatabase.size()>0) flow.message = "Unfortunately some name/unit combinations already exist. We have unchecked the relevant features for you. It concerns the following features: "+lstUniqueErrorItemsFromDatabase+message
                    }
                    return error();
                } else {
                    // SAVE DATA
                    for(int i=0; i<newFeatureList.size(); i++) {
                        Feature objFeature = newFeatureList[i];
                        objFeature.save();
                    }

                    flow.featureList = newFeatureList;
                }
            }
            on("success").to "finishScreen"
			on("error").to "matchColumns"
		}

        errorSaving {
			on("previous"){
                flow.message = null;
            }.to "checkInput"
		}

        finishScreen()
    }

}
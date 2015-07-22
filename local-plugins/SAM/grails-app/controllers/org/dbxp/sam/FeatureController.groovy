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
}
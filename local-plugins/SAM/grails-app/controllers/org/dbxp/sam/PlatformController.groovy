package org.dbxp.sam

import grails.converters.JSON
import org.dbnp.gdt.Template
import org.dbnp.gdt.TemplateFieldType
import org.springframework.dao.DataIntegrityViolationException

class PlatformController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
    def moduleService

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        if (moduleService.validateModule(params?.module)) {
            def platformList = Platform.findAllByPlatformtype(params.module)
            [platformInstanceList: platformList, platformInstanceTotal: platformList.size(), module: params.module]
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
        def columns = [ 'p.name', 'p.comments', 'p.platformtype', 'p.platformversion', 't.name' ]

        // Create the HQL query
        def hqlParams = [:];
        def hql = "FROM Platform p LEFT JOIN p.template as t ";
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

        // Display properties
        def records = Feature.executeQuery( hql + orderHQL, hqlParams, [ max: displayLength, offset: displayStart ] );
        def numTotalRecords = records.size()
        def filteredRecords = Feature.executeQuery( "SELECT p.id " + hql + orderHQL, hqlParams );

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
                    [ it[0].name, it[0].comments, it[0].platformtype, it[0].platformversion, it[1]?.name,
                      dt.buttonShow(id: it[0].id, controller: "platform", blnEnabled: true, params: [module: params.module]),
                      dt.buttonEdit(id: it[0].id, controller: "platform", blnEnabled: true, params: [module: params.module])]
                },
                aIds: filteredRecords
        ]

        response.setContentType( "application/json" );
        render returnValues as JSON

    }

    def create() {
        if (moduleService.validateModule(params?.module)) {
            [platformInstance: new Platform(params), module: params.module]
        }
        else {
            redirect(controller: 'error', action: 'notFound')
        }
    }

    def save() {
        def platformInstance = new Platform()

        // Was template set on the 'create page'?
        if( params.template) {
            // Yes, so add the template
            platformInstance.changeTemplate( params.template );
        }

        // was a template set?
        if (platformInstance.template) {
            // yes, iterate through template fields
            platformInstance.giveFields().each() {
                // and set their values
                if(it.type==TemplateFieldType.BOOLEAN){ // Set templatefields with type 'BOOLEAN'
                    def value = params.get(it.escapedName()+"_"+it.escapedName())!=null // '' becomes true, and null becomes false, as intended.
                    platformInstance.setFieldValue(it.name, value)
                } else {
                    platformInstance.setFieldValue(it.name, params.get(it.escapedName()+"_"+it.escapedName()))
                }
            }
        }

        // Remove the template parameter, since it is a string and that troubles the
        // setting of properties.
        def template = params.remove( 'template' )

        platformInstance.properties = params

        // Trim the whitespace from the name, to enable accurate validation
        platformInstance.name = platformInstance.name?.trim()

        // Attempt to save platform
        if (!platformInstance.save(flush: true)) {
            render(view: "create", model: [platformInstance: platformInstance], module: params.module)
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'platform.label', default: 'Platform'), platformInstance.name])
        redirect(action: "show", id: platformInstance.id, params: [module: params.module])
    }

    def show(Long id) {
        if (moduleService.validateModule(params?.module)) {
            def platformInstance = Platform.get(id)
            if (!platformInstance) {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'platform.label', default: 'Platform'), id])
                redirect(action: "list", params: [module: params.module])
                return
            }
            [platformInstance: platformInstance, module: params.module]
        }
        else {
            redirect(controller: 'error', action: 'notFound')
        }
    }

    def edit(Long id) {
        if (moduleService.validateModule(params?.module)) {
            def platformInstance = Platform.get(id)
            if (!platformInstance) {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'platform.label', default: 'Platform'), id])
                redirect(action: "list", params: [module: params.module])
                return
            }

            [platformInstance: platformInstance, module: params.module]
        }
        else {
            redirect(controller: 'error', action: 'notFound')
        }
    }

    def update(Long id, Long version) {
        def platformInstance = Platform.get(id)
        if (!platformInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'platform.label', default: 'Platform'), id])
            redirect(action: "list", params: [module: params.module])
            return
        }

        if (version != null) {
            if (platformInstance.version > version) {
                platformInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'platform.label', default: 'Platform')] as Object[],
                          "Another user has updated this Platform while you were editing")
                render(view: "edit", model: [platformInstance: platformInstance], module: params.module)
                return
            }
        }

        // Did the template change?
        if( params.template && params.template != platformInstance.template?.name) {
            // Yes, so change the template
            platformInstance.changeTemplate( params.template );
        }

        // was a template set?
        if (platformInstance.template) {
            // yes, iterate through template fields
            platformInstance.giveFields().each() {
                // and set their values
                if(it.type==TemplateFieldType.BOOLEAN){ // Set templatefields with type 'BOOLEAN'
                    def value = params.get(it.escapedName()+"_"+it.escapedName())!=null // '' becomes true, and null becomes false, as intended.
                    platformInstance.setFieldValue(it.name, value)
                } else {
                    platformInstance.setFieldValue(it.name, params.get(it.escapedName()+"_"+it.escapedName()))
                }
            }
        }

        // Remove the template parameter, since it is a string and that troubles the
        // setting of properties.
        def template = params.remove( 'template' )

        platformInstance.properties = params

        if (!platformInstance.save(flush: true)) {
            render(view: "edit", model: [platformInstance: platformInstance], module: params.module)
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'platform.label', default: 'Platform'), platformInstance.name])
        redirect(action: "show", id: platformInstance.id, params: [module: params.module])
    }

    def delete(Long id) {
        def platformInstance = Platform.get(id)
        if (!platformInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'platform.label', default: 'Platform'), id])
            redirect(action: "list", params: [module: params.module])
            return
        }

        try {
            platformInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'platform.label', default: 'Platform'), id])
            redirect(action: "list", params: [module: params.module])
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'platform.label', default: 'Platform'), id])
            redirect(action: "show", id: id, params: [module: params.module])
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
                def templateByEntityAndName
                Template.findAllByEntity(Platform).each {
                    if (it.name == params.template) {
                        templateByEntityAndName = it
                    }
                }
                return templateByEntityAndName
            }
        }

        // Store the template id in session, so the system will know the previously
        // selected template
        session.templateId = template?.id

        return template;
    }

}

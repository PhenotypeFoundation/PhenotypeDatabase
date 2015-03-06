package org.dbxp.sam

import grails.converters.JSON
import org.dbxp.matriximporter.*
import dbnp.studycapturing.*
import org.dbnp.gdt.AssayModule

class SAMAssayController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
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
	   int displayStart = params.int( 'iDisplayStart' ) ?: 0;
	   int displayLength = params.int( 'iDisplayLength' ) ?: 10;
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
	   def columns = [ 'name', 'unit' ]

	   // Create the HQL query
	   def hqlParams = [:];
	   def columnsHQL = "SELECT a.id, a.parent.title AS name, a.name, COUNT( s ) "
	   def hql = "FROM Assay a ";

	   def joinHQL  = " LEFT JOIN a.samples s "
	   def groupByHQL = " GROUP BY a.id, a.parent.title, a.name "
	   def whereHQL = "WHERE module_id = ${AssayModule.findByName(params.module).id} AND ";
	   def orderHQL = "";

	   // Add authorization
	   if( session.gscfUser ) {
		   if( !session.gscfUser.hasAdminRights() ) {
			   // Make sure only visible studies are shown
               // 			   whereHQL += " ( a.parent.publicstudy = true OR a.parent.owner = :user OR :user IN a.parent.readers OR :user IN a.parent.writers )" renders a very interesting query, possible Hibernate bug
			   //same here: whereHQL += " ( a.parent.publicstudy = true OR a.parent.owner = :user OR EXISTS( FROM Study x WHERE x.id = a.parent.id AND :user IN x.readers OR :user IN x.writers) )"
               whereHQL += " ( a.parent.publicstudy = true OR a.parent.owner = :user OR :user in elements(a.parent.readers) OR :user in elements(a.parent.writers))"
		   	   hqlParams[ "user" ] = session.gscfUser
		   } else {
		   		// Administrators are allowed to see all assays, but we have to add some HQL here,
		   		// since otherwise the HQL on line 101 can't be added with a " AND "
		   		whereHQL += " 1 = 1 "
		   }
	   } else {
	   		whereHQL += " a.parent.publicstudy = true ";
	   }

	   // Search properties
	   if( search ) {
		   hqlParams[ "search" ] = "%" + search.toLowerCase() + "%"

		   def hqlConstraints = [];
		   hqlConstraints << "LOWER(a.name) LIKE :search"
		   hqlConstraints << "LOWER(a.parent.title) LIKE :search"

		   whereHQL += " AND ( " + hqlConstraints.join( " OR " ) + ") "
	   }

	   // Sort properties
	   if( sortOn ) {
		   // column number must be increased by 2, because the database column number start with 1, but
		   // the first column that is retrieved from the database is the id column.
		   orderHQL = "ORDER BY " + sortOn.collect { ( it.column + 2 ) + " " + it.direction }.join( " " );
	   }

	   // Display properties
	   def records = Assay.executeQuery( columnsHQL + hql + joinHQL + whereHQL + groupByHQL + orderHQL, hqlParams, [ max: displayLength, offset: displayStart ] );
       //Should roughly equal to: def records = Assay.giveReadableAssays( session.gscfUser )

	   // Calculate the total records as the number of assays that are readable for the user
	   def numTotalRecords = records.size()

	   // Calculate filtered records
	   def filteredRecords = Assay.executeQuery( "SELECT id " + hql + whereHQL, hqlParams );

	   // Retrieve the number of samples with measurements for each assay
	   // This is not the most efficient way of performing this query, but still
	   def extendedRecords = []

	   if( records.size() > 0 ) {
		   records.each { record ->
               //Should display number of filled samples but this gives performance issues with a large amount of assays. Something like:
               //def measurementList = Measurment.executeQuery("SELECT sample.id FROM Measurment")
               //def numFilledSamples = SAMSample.executeQuery("SELECT COUNT(*) FROM SAMSample s WHERE s.parentAssay.id = :assay AND s.id IN :mList", [ assay: record[ 0 ], mList: measurementList ] )
               def numSAMSamples = SAMSample.executeQuery("SELECT COUNT(*) FROM SAMSample s WHERE s.parentAssay.id = :assay", [ assay: record[ 0 ] ] )
			   def extendedRecord = record as List;
			   extendedRecord[ 3 ] = numSAMSamples[0] + " / " + extendedRecord[ 3 ]

			   extendedRecords << extendedRecord
		   }
	   }

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
		   aaData: extendedRecords.collect {
               [
                it[1], it[2], it[3],
			    dt.buttonShow( 'controller': "SAMAssay", 'id': it[ 0 ], 'params': [module: params.module] ) ]
		   },
		   aIds: filteredRecords
	   ]

	   response.setContentType( "application/json" );
	   render returnValues as JSON

   }

    def show = {
        if (moduleService.validateModule(params?.module)) {
            def hideEmpty = params.hideEmpty ? Boolean.parseBoolean( params.hideEmpty ) : true
            def assayInstance = Assay.get(params.id)

            if (!assayInstance) {
                flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'assay.label', default: 'Assay'), params.id])}"
                redirect(action: "list", params: [module: params.module])
                return
            }

            if( !assayInstance.parent.canRead( session.gscfUser ) ) {
                flash.message = "You are not allowed to access assay " + assayInstance
                redirect(action: "list", params: [module: params.module])
                return
            }

            def ssamples = SAMSample.findAllByParentAssay(assayInstance)
            //If number of measurements in assay is higher then 10000; switch to summery view
            if (ssamples && Measurement.executeQuery("SELECT COUNT(*) FROM Measurement m WHERE m.sample IN (:samples)", [ samples: ssamples ])[0]  > 10000) {
                redirect(controller: 'SAMAssay', action: 'summary', params: [id: assayInstance.id, module: params.module])
                return
            }

            // Lookup all samples for this assay
            def numberOfSamples = assayInstance.samples.size();
            def samples;

            // If samples without measurements should be hidden, we don't retrieve them from the database at all
            if( hideEmpty ) {
                //samples = assayInstance.samples.sort { it.name}
                samples = SAMSample.findAll( "from SAMSample s WHERE s.parentAssay = :assay AND s.measurements.size > 0 ORDER BY s.parentSample.name", [ assay: assayInstance ] );
            } else {
                samples = SAMSample.findAll( "from SAMSample s WHERE s.parentAssay = :assay ORDER BY s.name", [ assay: assayInstance ] );
            }

            // Compute the number of samples without measurements
            def emptySamples = numberOfSamples - samples.size();

            def measurements = [];
            def features = [];

            if( samples ) {
                // If samples are found lookup all measurements. They are ordered by sample name and after that feature name.
                // This order ensures that we can easily walk through the list when showing them on screen
                measurements = Measurement.findAll( "from Measurement m WHERE m.sample IN (:samples) ORDER BY m.sample.parentSample.name, m.feature.name", [ samples: samples ] );
                if( measurements ) {
                    features = Feature.findAll( "from Feature f WHERE EXISTS( FROM Measurement m WHERE m IN (:measurements) AND m.feature = f ) ORDER BY f.name", [ measurements: measurements ] )
                }
            }
            return [assayInstance: assayInstance, samples: samples, features: features, measurements: measurements, hideEmpty: hideEmpty, module: params.module]
        }
        else {
            redirect(controller: 'error', action: 'notFound')
        }
    }

	def showByToken = {

        def assay = Assay.findWhere(UUID: params.id)

        if (!assay) {
            flash.message = "The assay you requested could not be found. PLease use your browser back button."
            redirect(action: "list")
			return
        }

        redirect(action:"show", params:[id: assay.id, module: assay.module.name])
    }

    def summary = {

        def assayInstance = Assay.findById(params.id)

        def totalSamples = assayInstance.samples.size()
        def samples = SAMSample.findAll( "from SAMSample s WHERE s.parentAssay = :assay ORDER BY s.parentSample.name", [ assay: assayInstance ] )

        def measurementCounts = []
        samples.each() {
            measurementCounts << Measurement.executeQuery("SELECT COUNT(*) FROM Measurement m WHERE m.sample = :sample", [ sample: it ])[0]
        }

        [ module: params.module, assayInstance: assayInstance, totalSamples: totalSamples, samples: samples, measurementCounts: measurementCounts ]
    }
}

package dbnp.calculation

import org.apache.jasper.compiler.Node.ParamsAction;
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import grails.plugins.springsecurity.Secured;
import dbnp.authentication.AuthenticationService;
import dbnp.studycapturing.Study;
import dbnp.studycapturing.SamplingEvent;
import dbnp.studycapturing.EventGroup;
import dbnp.studycapturing.Sample;
import grails.converters.JSON
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.servlet.http.HttpServletResponse

/**
 * Cookdata Controller
 *
 */
@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class CookdataController {
    // the pluginManager is used to check if the Grom
    // plugin is available so we can 'Grom' development
    // notifications to the unified notifications daemon
    // (see http://www.grails.org/plugin/grom)
    def pluginManager
    def authenticationService
    def moduleCommunicationService
    def cookdataService
	def assayService
	
	static final int BUFFER = 2048;

    /**
     * index method, redirect to the webflow
     * @void
     */
    def index = {
        // Grom a development message
        if (pluginManager.getGrailsPlugin('grom')) "redirecting into the webflow".grom()

        redirect(action: 'pages')
    }

    /**
     * WebFlow definition
     * @void
     */
    def pagesFlow = {
        // start the flow
        onStart {
            // Grom a development message
            if (pluginManager.getGrailsPlugin('grom')) "entering the WebFlow".grom()

            // define variables in the flow scope which is availabe
            // throughout the complete webflow also have a look at
            // the Flow Scopes section on http://www.grails.org/WebFlow
            //
            // The following flow scope variables are used to generate
            // wizard tabs. Also see common/_tabs.gsp for more information
            flow.page = 0
            flow.pages = [
                    [title: 'Select Assays'],
                    [title: 'Select Sampling Events'],
                    [title: 'Build Datasets'],
                    [title: 'Select Download Format'],
                    [title: 'Done']
            ]
            flow.cancel = true;
            flow.quickSave = true;

            flow.studies = Study.giveReadableStudies(authenticationService.getLoggedInUser())

            success()
        }

        // render the main wizard page which immediately
        // triggers the 'next' action (hence, the main
        // page dynamically renders the study template
        // and makes the flow jump to the study logic)
        mainPage {
            render(view: "/cookdata/index")
            onRender {
                // Grom a development message
                if (pluginManager.getGrailsPlugin('grom')) "rendering the main Ajaxflow page (index.gsp)".grom()

                // let the view know we're in page 1
                flow.page = 1
                success()
            }
            on("next").to "pageOne"
        }

        // first wizard page
        pageOne {
            render(view: "_page_one")
            onRender {
                // Grom a development message
                if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial: pages/_page_one.gsp".grom()

                flow.user = authenticationService.getLoggedInUser()
                flow.studies = Study.giveReadableStudies(flow.user)
                // TODO: check if readable for this user

                flow.page = 1
                success()
            }
            on("next") {
                println params
                flow.study = Study.get(params.selectStudy)
                flow.eventGroups = flow.study.eventGroups // retain order of event groups as defined in study
                flow.assays = [];
                flow.study.assays.each{
                    if(params["assay_"+it.id].equals("on")) {
                        //println "ASSAY ADDED: "+it
                        flow.assays.add(it);
                    }
                }
                flow.samplingEvents = flow.study.samplingEvents // sampling events order will be retained as defined in study
                flow.samplingEventTemplates = flow.samplingEvents*.template.unique() // Has meaningful order
                flow.samplingEventFields = cookdataService.retrieveInterestingFieldsList(flow.samplingEvents)

            }.to "pageTwo"
        }

        // second wizard page
        pageTwo {
            render(view: "_page_two")
            onRender {
                // Grom a development message
                if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial: pages/_page_two.gsp".grom()

                flow.page = 2
                success()
            }
            on("next") {
                // Each triple will consist of a samplingevent, an eventgroup and the number of samples in that selection
                List selectionTriples = []
                
				/* selectedSamplingEvents will contain only sampling events that occur 
				 * in selections. It is used for getting the interesting assays later on.
				 * Does not have a meaningful order. */
				List selectedSamplingEvents = []
				
				// Processing the uer's selections
                params.each{ key, val ->
                    if(val=="on"){
                        def splitKey = key.split("_")
                        def se = Integer.valueOf(splitKey[0]) // sampl. ev.
                        def eg = Integer.valueOf(splitKey[1]) // ev. group
                        def numItems = Sample.createCriteria().get {
                            projections {
                                count('id')
                            }
                            eq("parentEvent", flow.samplingEvents[se])
                            eq("parentEventGroup", flow.eventGroups[eg])
                        }
                        selectionTriples.add(
                                [
                                        se,
                                        eg,
                                        numItems
                                ]
                        )
                        selectedSamplingEvents.add(flow.samplingEvents[se])
                    }
                }
                flow.selectionTriples = selectionTriples
                flow.selectedSamplingEvents = selectedSamplingEvents.unique()

                // Update samplingEvents list
                flow.samplingEventTemplates = []
                selectionTriples.each{
                    flow.samplingEventTemplates.add(flow.samplingEvents[it[0]].template)
                }
                flow.samplingEventTemplates = flow.samplingEventTemplates.unique()

            }.to "pageThree"
            on("previous"){
                flow.mapSelectionSets = [:]
                flow.selectionTriples = []
                flow.study = null
                flow.eventGroups = []
                flow.samplingEvents = []
                flow.samplingEventFields = []
                flow.samplingEventTemplates = []
            }.to "pageOne"
        }

        // second wizard page
        pageThree {
            render(view: "_page_three")
            onRender {
                // Grom a development message
                if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial pages/_page_three.gsp".grom()

                flow.page = 3
                success()
            }
            on("next"){
	            flash.wizardErrors = []

                println "p3 next params: " + params
                println "dataset names" + params.dataset_name
                println "dataset equa" + params.dataset_equa
                println "dataset aggr" + params.dataset_aggr
                println "dataset grpA" + params.dataset_grpA
                println "dataset grpB" + params.dataset_grpB

                List listToBeComputed = []
                List samples = [] /* Will be compiled based on the user's 
                	selections, so that we can retrieve all interesting 
                	measurements in one call per module */
				
                if(params.dataset_equa.class == String) {
                    // In case only one dataset was selected, we will now have a String
                    // We need a list
                    params.dataset_name = [params.dataset_name]
                    params.dataset_equa = [params.dataset_equa]
                    params.dataset_aggr = [params.dataset_aggr]
                    params.dataset_grpA = [params.dataset_grpA]
                    params.dataset_grpB = [params.dataset_grpB]
                }
				
                
				
				int numItems = params.dataset_equa.size()
                flow.mapSelectionSets = ["A":[], "B":[]]
                flow.mapEquations = [:]

				// Parse the user's input, gather requested items
				try {
					// For each dataset and equation, gather the required samples
					// Package everything up in a map and add the map to listToBeComputed
	                for(int k = 0; k < numItems; k++){
						
						/* Some aggregation types require both dataset groups
							to actually contain content.
						*/
						boolean blnRestrictiveAggrType = false
						if(params.dataset_aggr[k].equals("average") ||
							params.dataset_aggr[k].equals("median") ||
							params.dataset_aggr[k].equals("pairwise")){
							blnRestrictiveAggrType = true
						}
							
						// Get the samples, per group
	                    List samplesA = cookdataService.getSamplesForDatasetGroup(params.dataset_grpA[k], 
							blnRestrictiveAggrType, flow.selectionTriples, flow.samplingEvents, flow.eventGroups)
		                List samplesB = cookdataService.getSamplesForDatasetGroup(params.dataset_grpB[k], 
							blnRestrictiveAggrType, flow.selectionTriples, flow.samplingEvents, flow.eventGroups)

						// Add the samples here so that we can request their measurements in one call
						samples.addAll(samplesA)
						samples.addAll(samplesB)
						
						// Queue what we just gathered for processing
	                    Map mapInfo = [
	                            "datasetName" : params.dataset_name[k],
	                            "equation" : params.dataset_equa[k],
	                            "aggr" : params.dataset_aggr[k],
	                            "samplesA" : samplesA,
	                            "samplesB" : samplesB
	                    ]
	                    listToBeComputed.add(mapInfo)
	                }
	
		            // Check if samples are present
		            if (!samples) {
			            throw new IllegalArgumentException("Based on your selections, no samples could be found. Because of that, there was nothing to compute.")
		            }
					
		            // Check which assays we need.
	                flow.assays = cookdataService.getInterestingAssays(flow.study, flow.selectedSamplingEvents, flow.assays)
					
					// Get the measurements, per sampletoken, per feature
	                Map mapSampleTokenToMeasurementPerFeature = cookdataService.getDataFromModules(flow.assays, samples)
					
	                flow.results = cookdataService.getResults(listToBeComputed, mapSampleTokenToMeasurementPerFeature)

		            success()
	            }
	            catch (Exception e) {
		            println "catching ${e.getMessage()}"
		            flash.wizardErrors << e
		            error()
	            }
            }.to "pageFour"
            on("previous").to "pageTwo"
        }

        // second wizard page
        pageFour {
            render(view: "_page_four")
            onRender {
                // Grom a development message
                if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial pages/_page_four.gsp".grom()
				session.downloadResultId = null
				session.results = null
                flow.page = 4
                success()
            }
            on("previous").to "pageThree"
			on("downloadOneResultAsExcel"){
				session.results = flow.results[Integer.valueOf(params.downloadResultId)]
				println "results.size(): "+session.results.size()
			}.to "downloadOneResultAsExcel"
			on("downloadAllResultsAsZip"){
				session.results = flow.results
				println "results.size(): "+session.results.size()
			}.to "downloadAllResultsAsZip"
        }
		
		downloadOneResultAsExcel {
			redirect(action: 'downloadExcel')
		}
		
		downloadAllResultsAsZip {
			redirect(action: 'downloadExcelsInZip')
		}

        // render errors
        error {
            render(view: "_error")
            onRender {
                // Grom a development message
                if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial pages/_error.gsp".grom()

                // set page to the number of pages minus one, so that the 
				// navigation works (it is disabled on the final page)
                flow.page = 3
            }
            on("next").to "save"
            on("previous").to "pageFour"
            on("toPageOne").to "pageOne"
            on("toPageTwo").to "pageTwo"
            on("toPageThree").to "pageThree"
            on("toPageFour").to "pageFour"
            on("toPageFive").to "save"

        }

        // last wizard page
        finalPage {
            render(view: "_final_page")
            onRender {
                // Grom a development message
                if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial pages/_final_page.gsp".grom()

                success()
            }
        }
    }
	
	/**
	 * Writes an excel file containing measurements for one dataset, with  
	 * additional information on the first row, and sends it back to the client
	 * as an attachment. 
	 */
	def downloadExcel = {
		println "entered downloadExcel..."
		
		def data = [["name", session.results[0].datasetName]]
		data.addAll(session.results[1])
		session.results[1] = null
		
		response.setHeader "Content-disposition", "attachment;filename=\""+session.results[0].datasetName+".xlsx\""
		response.setContentType "application/octet-stream"
		assayService.exportRowWiseDataToExcelFile(
			data, 
			response.getOutputStream())
		response.outputStream.flush()		
		
		
		session.results = null
		println "exiting downloadExcel..."
	}

	/**
	* Writes an zip file containing excel files with measurements, and sends it 
	* back to the client as an attachment.
	* It is possible that the zip file will end up containing just one file.
	* One excel file will contain results for those datasets where the user
	* marked "median" or "average" as aggregation type.
	* 
	* TODO: when "pairwise" and "measurements" aggregation types are 
	* implemented, write these results to files of their own.
	*/
    def downloadExcelsInZip = {

	}

    def testEquation = {
        println "testEquation params: "+params
        // Tests if an equation can be parsed
        // Uses arbitrary values for testing purposes.
        boolean success = true
        String equation = params.equation.replaceAll("\\s",""); // No whitespace
        try{
            double res = cookdataService.computeWithVals(equation, 5.0, 10.0)
        } catch (Exception e){
            // No joy
            log.error("CookdataController: testEquation: " + e)
            success = false
        }
        Map mapResults = [:]
        mapResults.put("status", success)
        render mapResults as JSON
    }

    def getAssays = {
        // Get the assays of a study

        def study = Study.get(params.selectStudy)
        def assayList = [];
        study.assays.each{
            assayList.add([name: it.name, assayUUID: it.assayUUID, modulename: it.module.name, id: it.id]);
        }

        Map mapResults = [:]
        mapResults.put("assays", assayList)
        mapResults.put("studyId", study.id)
        render mapResults as JSON
    }
}

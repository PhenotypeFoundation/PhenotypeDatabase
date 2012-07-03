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
                flow.samplingEventTemplates = flow.samplingEvents*.template.unique()
                flow.samplingEventFields = retrieveInterestingFieldsList(flow.samplingEvents)

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
                List selectedSamplingEvents = []
                params.each{ key, val ->
                    if(val=="on"){
                        def splitKey = key.split("_")
                        def se = Integer.valueOf(splitKey[0])
                        def eg = Integer.valueOf(splitKey[1])
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
                List samples = []
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
                
				try {
					// For each dataset and equation, gather the required samples
					// Package everything up in a map and add the map to listToBeComputed
	                for(int k = 0; k < numItems; k++){
	                    List samplesA = []
		                List samplesB = []
		                def listA = params.dataset_grpA[k].split("\\.")
	                    if (listA) {
			                samplesA = retrieveSamplesForGroup(
		                            listA,
		                            flow.selectionTriples,
		                            flow.samplingEvents,
		                            flow.eventGroups
		                    )
			                samples.addAll(samplesA)
	                    }
		                def listB = params.dataset_grpB[k].split("\\.")
	                    if (listB) {
			                samplesB = retrieveSamplesForGroup(
		                            listB,
		                            flow.selectionTriples,
		                            flow.samplingEvents,
		                            flow.eventGroups
		                    )
	                        samples.addAll(samplesB)
	                    }
	                    Map mapInfo = [
	                            "datasetName" : params.dataset_name[k],
	                            "equation" : params.dataset_equa[k],
	                            "aggr" : params.dataset_aggr[k],
	                            "samplesA" : samplesA,
	                            "samplesB" : samplesB
	                    ]
	                    listToBeComputed.add(mapInfo)
	                }
	
		            //println "listToBeComputed:\n"+listToBeComputed+"\n"
	            
		            // Check if samples are present
		            if (!samples) {
			            throw new IllegalArgumentException("The resultset contains no samples!")
		            }
		            // Check which assays we need.
	                flow.assays = getInterestingAssays(flow.study, flow.selectedSamplingEvents, flow.assays)
	                Map mapSampleTokenToMeasurementPerFeature = getDataFromModules(flow.assays, samples)
	                flow.results = getResults(listToBeComputed, mapSampleTokenToMeasurementPerFeature)
	                /*flow.results.each{ pair ->
	                    println pair[0].aggr+" for "+pair[0].datasetName+" & "+pair[0].equation
	                    pair[1].each{ value ->
	                        println "\t"+value
	                    }
	                }*/
		            success()
	            }
	            catch (Exception e) {
		            println "catching ${e.getMessage()}"
		            flash.wizardErrors << e
		            error()
	            }
            }.to "pageFour"
            on("previous"){
                //flow.mapSelectionSets = [:]
                //flow.samplingEventTemplates = flow.samplingEvents*.template.unique()
            }.to "pageTwo"
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
			on("downloadResultAsExcel"){
				session.downloadResultId = Integer.valueOf(params.downloadResultId)
				println "downloadResultId: "+session.downloadResultId
				session.results = flow.results[session.downloadResultId]
				println "results.size(): "+session.results.size()
			}.to "downloadResultsAsExcel"
        }
		
		downloadResultsAsExcel {
			redirect(action: 'downloadExcel')
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
	
	def downloadExcel = {
		println "entered downloadExcel..."
		def index = session.downloadResultId
		def results = session.results
		// todo: data should start with ["name", results[0].datasetName]
		def data = results[1]
		println "data: "+data
		response.setHeader "Content-disposition", "attachment;filename=\""+results[0].datasetName+".xlsx\""
		response.setContentType "application/octet-stream"
		assayService.exportRowWiseDataToExcelFile(data, response.getOutputStream())
		response.outputStream.flush()		
		
		println "exiting downloadExcel..."
	}

    def downloadExcelsInZip = {
		// todo: package multiple files
		def results = session.results
		println "entered downloadExcelsInZip..."
		String strFilename = "results.zip"
		ByteArrayOutputStream dest = new ByteArrayOutputStream();
		ZipOutputStream out = new	ZipOutputStream(dest);
		
		for (int i=0; i<results.size(); i++) {
			ZipEntry entry = new ZipEntry(results[i][0].datasetName)
			out.putNextEntry(entry)
			Workbook wb = new XSSFWorkbook()
			Sheet sheet = wb.createSheet()
			assayService.exportRowWiseDataToExcelSheet( [['r0c0','r0c1'],['r1c0','r1c1']], sheet );
			wb.write(out)
		}
		out.close();
		dest.flush()
		dest.close()
		
		response.setHeader "Content-disposition", "attachment;filename=\"${strFilename}\""
		response.setContentType "application/octet-stream"
		response.outputStream.write(dest.toByteArray())
		response.outputStream.flush()
		println "exiting downloadExcelsInZip..."
	}

    private List retrieveSamplesForGroup(listOfSelectionsTripleIndexes, selectionTriples, samplingEvents, eventGroups){
        List samples = []
        listOfSelectionsTripleIndexes.each{ val ->
            try{
                def selectionTripleIndex = val.split("_")[1]
                def st = selectionTriples[Integer.valueOf(selectionTripleIndex)]
                samples += Sample.findAllByParentEventAndParentEventGroup(
                        samplingEvents[st[0]], eventGroups[st[1]]
                )
            } catch(Exception e){
                throw new IllegalArgumentException("Error while creating list of samples (${e.getMessage()}): value ${val} from "+listOfSelectionsTripleIndexes)
            }
        }
        return samples
    }

    private List retrieveInterestingFieldsList(List objects){
        List fields = []
        objects.each{
            fields.addAll(it.giveDomainFields())
            fields.addAll(it.giveTemplateFields())
        }
        fields.unique()
        List toRemove = []
        for(int i = 0; i < fields.size(); i++){
            if(!fields[i].isFilledInList(objects)){
                toRemove.add(fields[i])
            }
        }
        fields.removeAll(toRemove)
        return fields
    }

    private List getResults(List listToBeComputed, Map mapSTokenToMsrmentsPerF){
        List listItemAndFeatureToResult = []
		
        listToBeComputed.each{ item ->
            List listResultAndFeaturePairs = []
			int intResultCounter = 0;
            def dblResult
            switch(item.aggr){
                case "average":
                    //println "average for "+item.datasetName+" & "+item.equation

                    String equation = item.equation.replaceAll("\\s","") // No whitespace
                    if (!equation) {
	                    throw new IllegalArgumentException("No equation was provided.")
                    }

	                //println "mapSTokenToMsrmentsPerF size: "+mapSTokenToMsrmentsPerF
                    // Per feature, map the sample tokens to measurements
	                mapSTokenToMsrmentsPerF.eachWithIndex{ feature, mapStToM, featureIndex ->
                        //println "now for "+feature
                        //println mapStToM
	                    List dataA = []
                        item.samplesA.each{ s ->
	                        def m = mapStToM[s.sampleUUID]
                            if (m) dataA.add(m)
                        }
                        if (dataA.size() == 0) {
	                        throw new IllegalArgumentException("The samples from group A of dataset ${item.datasetName} do not have any measurements for ${feature}. Cannot compute average.")
                        }
	                    double avgA = cookdataService.computeMean(dataA)
                        //println "avgA: "+avgA+" dataA size: "+dataA.size()
                        dataA = []
                        List dataB = []
                        item.samplesB.each{ s ->
	                        def m = mapStToM[s.sampleUUID]
	                        if (m) dataB.add(m)
                        }
		                if (dataB.size() == 0) {
			                throw new IllegalArgumentException("The samples from group B of dataset ${item.datasetName} do not have any measurements for ${feature}. Cannot compute average.")
		                }
		                double avgB = cookdataService.computeMean(dataB)
                        //println "avgB: "+avgB+" dataB size: "+dataB.size()
                        dataB = []
						listResultAndFeaturePairs[intResultCounter] = 
							[feature, cookdataService.computeWithVals(equation, avgA, avgB)]
		                //println listResultAndFeaturePairs[intResultCounter] 
						intResultCounter++
                    }
                    break

                default:
                    dblResult = null
            }
            listItemAndFeatureToResult.add([item, listResultAndFeaturePairs])
        }
        return listItemAndFeatureToResult
    }

    /**
     * For the CookData controller, an assay is interesting when it has samples that the user has selected.
     * Unfortunately the fastest way seems to be checking for each assay, if they have a sample that is in one of the selected samplingEvents.
     * This involves requesting a lot of samples from the database.
     */
    private List getInterestingAssays(Study study, List samplingEvents, List lstAssays) {
        List assays = []
        int numAssays = study.assays.size()
        int numSEvents = samplingEvents.size();
        for(int i = 0; i < numAssays; i++){
            def assay =  study.assays[i]
            if(lstAssays.contains(assay)) {

                def listAssaySamples = []
                assay.samples.each{
                    listAssaySamples << it
                }

                if(assay.samples.size()==0){
                    // No samples in the assay, so not an interesting assay
                    continue
                }

                boolean success = false
                for(int j = 0; j <numSEvents; j++){
                    if(success){
                        break
                    }

                    SamplingEvent event = samplingEvents[j]
                    int numEventSamples = event.samples.size()
                    for(int k = 0; k < numEventSamples; k++){
                        List listEventSamples = []
                        event.samples.each{
                            listEventSamples << it
                        }

                        Sample sample = listEventSamples[k]
                        if(listAssaySamples.contains(sample)){
                            success = true
                            break
                        }
                    }
                }
                if(success){
                    assays << assay
                }

            }
        }
        return assays
    }

    /**
     * For each assay, call related modules and ask for features
     * @param assays
     * @return list of features
     */
    private Map getFeaturesFromModules(assays){
        List blacklistedModules = []
        Map mapResults = [:]
        assays.eachWithIndex { assay, index ->
            if (!blacklistedModules.contains(assay.module.id)) {
                try{
                    def urlVars = "assayToken=" + assay.assayUUID
                    def strURL = assay.module.url + "/rest/getMeasurementMetaData/query?" + urlVars
                    def callResult = moduleCommunicationService.callModuleRestMethodJSON(assay.module.url, strURL);
                    def result = []
                    callResult.each{ cR ->
                        Map map = [:]
                        cR.each{ key, val ->
                            map.put(key, val)
                        }
                        result.add(map)
                    }
                    if (result != null) {
                        if (result.size() != 0) {
                            mapResults.put(index.toString(), result)
                        }
                    }
                } catch (Exception e) {
                    blacklistedModules.add(assay.module.id)
                    log.error("CookdataController: getFeaturesFromModules: " + e)
                }
            }
        }
        return mapResults
    }

    // Returns each measurement per sampleToken, per feature
    private Map getDataFromModules(List assays, List samples){
        List blacklistedModules = []
        Map mapResults = [:]

        assays.each{ assay ->
            Map mapTmp = [:]
            if (!blacklistedModules.contains(assay.module.id)) {
                try{
                    // Request for a particular assay
                    def urlVars = "assayToken=" + assay.assayUUID
                    // All samples
                    urlVars += "&"+samples.collect { "sampleToken=" + it.sampleUUID }.join("&");
                    def strUrl = assay.module.url + "/rest/getMeasurementData"
                    def callResult = moduleCommunicationService.callModuleMethod(assay.module.url, strUrl, urlVars, "POST")
                    // Store measurements per sampleToken, per feature.
                    // [1] contains a list of features
                    callResult[1].eachWithIndex{ feature, featureIndex ->
                        mapTmp.put(feature, [:])
                        Map mapSampleTokenToMeasurement = [:]
                        // [0] contains a list of sampleTokens
                        callResult[0].eachWithIndex{ sample, sampleIndex ->
                            if(sample!=null && sample.class!=org.codehaus.groovy.grails.web.json.JSONObject$Null){
                                // We have a sample for this feature
                                // This sample may have a measurement
                                def measurement = callResult[2][featureIndex*sampleIndex]
                                if(measurement!=null && measurement.class!=org.codehaus.groovy.grails.web.json.JSONObject$Null){
                                    mapTmp[feature].put(sample, measurement)
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    blacklistedModules.add(assay.module.id)
                    log.error("CookdataController: getDataFromModules: " + e)
                }
            }
            if(mapTmp!=[:]){
                // Add the individial result to the total
                mapTmp.each{ k, v ->
                    mapResults.put(k,  v)
                }
            }
        }
        return mapResults
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
        //println "getAssays params: "+params
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

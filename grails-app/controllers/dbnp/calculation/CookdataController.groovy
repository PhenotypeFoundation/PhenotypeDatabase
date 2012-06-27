package dbnp.calculation

import grails.plugins.springsecurity.Secured;
import dbnp.authentication.AuthenticationService;
import dbnp.studycapturing.Study;
import dbnp.studycapturing.SamplingEvent;
import dbnp.studycapturing.EventGroup;
import dbnp.studycapturing.Sample;
import grails.converters.JSON

/**
 * ajaxflow Controller
 *
 * @author	Jeroen Wesbeek
 * @since	20120620
 *
 * Revision information:
 * $Rev:  66849 $
 * $Author:  duh $
 * $Date:  2010-12-08 15:12:54 +0100 (Wed, 08 Dec 2010) $
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
	def cookDataService
	
	/**
	 * index method, redirect to the webflow
	 * @void
	 */
	def index = {
		// Grom a development message
		if (pluginManager.getGrailsPlugin('grom')) "redirecting into the webflow".grom()

		/**
		 * Do you believe it in your head?
		 * I can go with the flow
		 * Don't say it doesn't matter (with the flow) matter anymore
		 * I can go with the flow (I can go)
		 * Do you believe it in your head?
		 */
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
				[title: 'Select Study'],
				[title: 'Select Sampling Events'],
				[title: 'Build Datasets'],
				[title: 'Select Assays'],
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
				flow.study = Study.get(params.selectStudy) 
				flow.eventGroups = EventGroup.findAllByParent(flow.study)
				flow.samplingEvents = SamplingEvent.findAllByParent(flow.study)
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
						List selectedSamplingEvents.add(flow.samplingEvents[se])
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
				println "p3 next params: " + params
				flow.mapSelectionSets = ["A":[], "B":[]]
				flow.mapEquations = [:]
				params.each{ key, val ->
					if(val=="on"){
						def splitKey = key.split("_")
						flow.mapSelectionSets[splitKey[0]].add(
								flow.selectionTriples[
										Integer.valueOf(splitKey[1])
									]
							)						
					}
					if(key.startsWith("eq_")){
						def splitKey = key.split("_")
						if(!flow.mapEquations.containsKey(splitKey[2])){
							flow.mapEquations.put(splitKey[2], [:])
						}
						flow.mapEquations[splitKey[2]].put(splitKey[1], val)
					}
				}
				// Check which assays we need.
				flow.assays = getInterestingAssays(flow.study, flow.selectedSamplingEvents)
				
				// For each assay, call related modules and ask for features
				flow.mapFeaturesPerAssay = getFeaturesFromModules(flow.assays)
				
				// For each feature, get data from the modules
				flow.mapFeaturesAndDataPerAssay = getDataFromModules(flow.mapFeaturesPerAssay, flow.assays)
				
				println "About to call getResults"
				flow.results = getResults(flow.study, flow.mapSelectionSets, flow.mapEquations, flow.mapFeaturesAndDataPerAssay, flow.samplingEvents, flow.eventGroups)
				
				flow.mapResultPerEquationPerFeaturePerAssay = [:]
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
				flow.page = 4
				success()
			}
			on("next") {
				println "p4 next params: " + params
				
				flow.page = 5
			}.to "save"
			on("previous").to "pageThree"
		}

		// save action
		save {
			action {
				// here you can validate and save the
				// instances you have created in the
				// ajax flow.
				try {
					// Grom a development message
					if (pluginManager.getGrailsPlugin('grom')) ".persisting instances to the database...".grom()

					// put your bussiness logic in here
					success()
				} catch (Exception e) {
					// put your error handling logic in
					// here
					flow.page = 4
					error()
				}
			}
			on("error").to "error"
			on(Exception).to "error"
			on("success").to "finalPage"
		}

		// render errors
		error {
			render(view: "_error")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial pages/_error.gsp".grom()

				// set page to 4 so that the navigation
				// works (it is disabled on the final page)
				flow.page = 4
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
	
	private List getResults(Study study, Map mapSelectionSets, Map mapEquations, Map mapFeaturesAndDataPerAssayy, List samplingEvents, List eventGroups){
		mapSelectionSets.each{ set, selections ->
			println "set: "+set
			selections.each{ samplingEvent, eventGroup, numSamples ->
				println "SE "+samplingEvent+" EG "+eventGroup+" NS "+numSamples
				/*def samples = Sample.createCriteria().get {
					eq("parentEvent", samplingEvent)
					eq("parentEventGroup", eventGroup)
				}
				println samples*/
			}
		}
		/*mapFeaturesAndDataPerAssay{ assayIndex, dataPerFeature ->
			dataPerFeature.each{ featureName, data->
				
			}
		}*/
		// Step 1, get the average
		//Step 2) while parsing the equation, calculate the results
		// Easy for average or median, difficult for pairwise calculation
		// Only calculate something when both sets have measurements related to the relevant feature
		// Question: What constitutes a pair?
		def results = []
		/*
		mapEquations.each{ key, val ->
			String equations = val.val.replaceAll("\\s",""); // No whitespace
			double stepbystep = cookDataService.computeWithVals(equations, 0, testA, testB)
			results.add(stepbystep)
		}
		mapEquations.eachWithIndex{ key, val, i ->
			println key+": "+val.label+": "+val.val+" -> "+results[i]
		}
		*/
		return results
	}
	
	/**
	 * For the CookData controller, an assay is interesting when it has samples that the user has selected.
	 * Unfortunately the fastest way seems to be checking for each assay, if they have a sample that is in one of the selected samplingEvents.
	 * This involves requesting a lot of samples from the database.
	 */
	private List getInterestingAssays(Study study, List samplingEvents){
		List assays = []
		int numAssays = study.assays.size()
		int numSEvents = samplingEvents.size()
		for(int i = 0; i < numAssays; i++){
			def assay =  study.assays[i]
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
					log.error("CookDataController: getFeaturesFromModules: " + e)
				}
			}
		}
		return mapResults
	}

	private Map getDataFromModules(Map mapFeaturesPerAssay, List assays){
		List blacklistedModules = []
		Map mapResults = [:]
		mapFeaturesPerAssay.eachWithIndex { pair, index ->
			def assay = assays[index]
			Map mapResultsForAssay = [:]
			if (!blacklistedModules.contains(assay.module.id)) {
				try{
					// Request for a particular assay
					def urlVars = "assayToken=" + assay.assayUUID
					urlVars += "&"+assay.samples.collect { "sampleToken=" + it.sampleUUID }.join("&");
					def strUrl = assay.module.url + "/rest/getMeasurementData"
					def callResult = moduleCommunicationService.callModuleMethod(assay.module.url, strUrl, urlVars, "POST");
					/*int upto = 5
					if(callResult[0].size()<upto){
						upto = callResult.size()
					}
					for(int i = 0; i < upto; i++){
						println i+": "+callResult[0][i]
						println i+": "+callResult[1][i]
						println i+": "+callResult[2][i]
					}*/
					def result = [:]
					println "callResult[0].size(): "+callResult[0].size()
					pair.value.each{ feature ->
						// We wish to order the measurements per feature
						Map mapSampleTokenToMeasurement = [:]
						callResult[1].eachWithIndex{ f, fi ->
							if(feature.name == f){
								// This data belongs to the current feature, 
								// so add sample and the measurement 
								mapSampleTokenToMeasurement.put(
									callResult[0][fi], callResult[2][fi])
							}
						}
						if(mapSampleTokenToMeasurement!=[:]){
							println "\t"+feature.name+" num measurements: "+mapSampleTokenToMeasurement.size()
							result.put(feature.name, mapSampleTokenToMeasurement)
						}
					}
					println "result.size(): "+result.size()
					if (callResult != null) {
						if (callResult.size() != 0) {
							mapResultsForAssay.put(index.toString(), result)
						}
					}
				} catch (Exception e) {
					blacklistedModules.add(assay.module.id)
					log.error("CookDataController: getDataFromModules: " + e)
				}
			}
			if(mapResultsForAssay.size()!=0){
				mapResultsForAssay.each{ k, v ->
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
			double res = cookDataService.computeWithVals(equation, 0, 5.0, 10.0)
		} catch (Exception e){
			// No joy 
			log.error("CookDataController: testEquation: " + e)
			success = false
		}
		Map mapResults = [:]
		mapResults.put("status", success)
		render mapResults as JSON
	}
}

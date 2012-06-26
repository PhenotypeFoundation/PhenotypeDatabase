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
				List selectionTriples = []
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
					}
				}
				flow.selectionTriples = selectionTriples	
				
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
				println "mapEquations: "+flow.mapEquations
				println "mapSelectionSets: "+flow.mapSelectionSets
				println "samplingEvents: "+flow.samplingEvents
				println "eventGroups: "+flow.eventGroups
				
				// Current code does not retrieve the actual values yet. These two doubles are passed to be able to test the parsing.
				double testA = 5.0
				double testB = 10.0
				flow.results = getResults(flow.mapSelectionSets, flow.mapEquations, flow.samplingEvents, flow.eventGroups, testA, testB)
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
				// put some logic in here
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
	
	private List getFeatures(study){
		if(study!=null){
			fields += getFields(study, "subjects", "domainfields")
			fields += getFields(study, "subjects", "templatefields")
			/*fields += getFields(study, "events", "domainfields")
			fields += getFields(study, "events", "templatefields")*/
			fields += getFields(study, "samplingEvents", "domainfields")
			fields += getFields(study, "samplingEvents", "templatefields")
			fields += getFields(study, "assays", "domainfields")
			fields += getFields(study, "assays", "templatefields")
			fields += getFields(study, "samples", "domainfields")
			fields += getFields(study, "samples", "templatefields")

			// Also make sure the user can select eventGroup to visualize
			fields += formatGSCFFields( "domainfields", [ name: "name" ], "GSCF", "eventGroups" );
			
			/*
			Gather fields related to this study from modules.
			This will use the getMeasurements RESTful service. That service returns measurement types, AKA features.
			It does not actually return measurements (the getMeasurementData call does).
			The getFields method (or rather, the getMeasurements service) requires one or more assays and will return all measurement
			types related to these assays.
			So, the required variables for such a call are:
			  - a source variable, which can be obtained from AssayModule.list() (use the 'name' field)
			  - an assay, which can be obtained with study.getAssays()
			 */
			study.getAssays().each { assay ->
				def list = []
				if(!offlineModules.contains(assay.module.id)){
					list = getFields(assay.module.toString(), assay)
					if(list!=null){
						if(list.size()!=0){
							fields += list
						}
					}
				}
			}
			offlineModules = []

			// Make sure any informational messages regarding offline modules are submitted to the client
			setInfoMessageOfflineModules()


			// TODO: Maybe we should add study's own fields
		} else {
			log.error("VisualizationController: getFields: The requested study could not be found. Id: "+studies)
			return returnError(404, "The requested study could not be found.")
		}
	}
	
	// Current code does not retrieve the actual values yet. These two doubles are passed to be able to test the parsing.
	private List getResults(mapSelectionSets, mapEquations, samplingEvents, eventGroups, double testA, double testB){
		//Step 1) Get data 
		// Get assays
		// For each assay, call related modules and ask for features
		// For each feature, call related module and ask for measurements
		
		//Step 2) while parsing the equation, calculate the results
		// Easy for average or median, difficult for pairwise calculation
		// Only calculate something when both sets have measurements related to the relevant feature
		// Question: What constitutes a pair?
		def results = []
		mapEquations.each{ key, val ->
			String equations = val.val.replaceAll("\\s",""); // No whitespace
			double stepbystep = computeWithVals(equations, 0, testA, testB)
			results.add(stepbystep)
		}
		mapEquations.eachWithIndex{ key, val, i ->
			println key+": "+val.label+": "+val.val+" -> "+results[i]
		}
		return results
	}
	
	
	
	
	
	////////////////// Start of Equation handling code
	
	private boolean checkForOpeningAndClosingBrackets(String eq){
		boolean ret = (
			// ( ... ) and
			eq.startsWith("(") && eq.endsWith(")")
			&&
			// First ) is at the last index
			eq.indexOf(")")==(eq.size()-1)
		)
		return ret
	}	
	
	private Map parseWellFormedLeftHandSide(String eq){
		Map mapReturn = [:]
		int countOpening = 0
		int countClosing = 0
		int latestClosingIndex = -1
		for(int i = 0; i < eq.size(); i++){
			if(eq[i]=="("){
				countOpening++
			}
			if(eq[i]==")"){
				countClosing++
				latestClosingIndex = i
			}
			if(	countOpening!=0 && countClosing!=0 && 
				countOpening==countClosing
			){
				// Left-hand side is wellformed
				break
			}
		}
		
		mapReturn.endIndex = 1 + latestClosingIndex
		if(mapReturn.endIndex==eq.size()){
			// Brackets are part of right-hand side, not left...
			mapReturn.endIndex = 0
		}
		mapReturn.success = (countOpening==countClosing)
		return mapReturn
	}
	
	private double computeWithVals(String eq, int counter, double dblA, double dblB){
		double dblReturn = -1.0
		// Check for "(x)"
		if(checkForOpeningAndClosingBrackets(eq)){
			int index0 = eq.indexOf(")")
			double result = computeWithVals(eq.substring(1, index0), counter+1, dblA, dblB)
			dblReturn = result
			return dblReturn
		}
		
		/* Check for "x/y" and make sure "(x/y)/z" detects the second operator,
		 * not the last. 
		 */
		Map mapParseLHSResults = parseWellFormedLeftHandSide(eq)
		if(mapParseLHSResults.success){
			// A wellformed LHS could be found. Any operator after the LHS?
			int intOpIndex = eq.substring(mapParseLHSResults.endIndex,
				eq.size()).indexOf("/")
			if(intOpIndex!=-1){
				int index0 = intOpIndex+mapParseLHSResults.endIndex
				double result1 = computeWithVals(eq.substring(0, index0), counter+1, dblA, dblB)
				double result2 = computeWithVals(eq.substring(index0+1, eq.size()), counter+2, dblA, dblB)
				dblReturn = result1 / result2
				return dblReturn
			}
			intOpIndex = eq.substring(mapParseLHSResults.endIndex,
				eq.size()).indexOf("*")
			if(intOpIndex!=-1){
				int index0 = intOpIndex+mapParseLHSResults.endIndex
				double result1 = computeWithVals(eq.substring(0, index0), counter+1, dblA, dblB)
				double result2 = computeWithVals(eq.substring(index0+1, eq.size()), counter+2, dblA, dblB)
				dblReturn = result1 * result2
				return dblReturn
			}
			intOpIndex = eq.substring(mapParseLHSResults.endIndex,
				eq.size()).indexOf("+")
			if(intOpIndex!=-1){
				int index0 = intOpIndex+mapParseLHSResults.endIndex
				double result1 = computeWithVals(eq.substring(0, index0), counter+1, dblA, dblB)
				double result2 = computeWithVals(eq.substring(index0+1, eq.size()), counter+2, dblA, dblB)
				dblReturn = result1 + result2
				return dblReturn
			}
			intOpIndex = eq.substring(mapParseLHSResults.endIndex,
				eq.size()).indexOf("-")
			if(intOpIndex!=-1){
				int index0 = intOpIndex+mapParseLHSResults.endIndex
				double result1 = computeWithVals(eq.substring(0, index0), counter+1, dblA, dblB)
				double result2 = computeWithVals(eq.substring(index0+1, eq.size()), counter+2, dblA, dblB)
				dblReturn = result1 - result2
				return dblReturn
			}
		} else {
			println "Received malformed string: unbalanced brackets: "+eq
		}
		
		// Check for A
		if(eq.equals("A")){
			dblReturn = dblA
			return dblReturn
		}
		
		// Check for B
		if(eq.equals("B")){
			dblReturn = dblB
			return dblReturn
		}
		
		// If we get here, nothing has fired
		dblReturn = -1.0
		return dblReturn
	}
	
	
	def testCalculations = {
		// Current code does not retrieve the actual values yet. These two doubles are passed to be able to test the parsing.
		assert 0.05 == 
			getResults(
				["A":[], "B":[]], 
				[0:["val":"(A/B)/B", "label":""]],
				[:],
				[:],
				5,
				10
			)[0]
		assert 5.0 == 
			getResults(
				["A":[], "B":[]], 
				[0:["val":"(A/B)*B", "label":""]],
				[:],
				[:],
				5,
				10
			)[0]
		assert -15.0 == 
			getResults(
				["A":[], "B":[]], 
				[0:["val":"(A-B)-B ", "label":""]],
				[:],
				[:],
				5,
				10
			)[0]
		assert 5.0 == 
			getResults(
				["A":[], "B":[]], 
				[0:["val":"(A+B)-B", "label":""]],
				[:],
				[:],
				5,
				10
			)[0]
		assert -1.0 == 
			getResults(
				["A":[], "B":[]], 
				[0:["val":"", "label":""]],
				[:],
				[:],
				5,
				10
			)[0]
		assert 15.0 == 
			getResults(
				["A":[], "B":[]], 
				[0:["val":"B+A", "label":""]],
				[:],
				[:],
				5,
				10
			)[0]
		assert 45.0 == 
			getResults(
				["A":[], "B":[]], 
				[0:["val":"B+B+B+B+B-(A)", "label":""]],
				[:],
				[:],
				5,
				10
			)[0]
		assert -50.0 == 
			getResults(
				["A":[], "B":[]], 
				[0:["val":"   B           *   (A   -B    )    ", "label":""]],
				[:],
				[:],
				5,
				10
			)[0]
		
		Map ret = [:]
		ret.put("result", "Great success!")
		render ret as JSON
	}
	
	////////////////// End of Equation handling code
}

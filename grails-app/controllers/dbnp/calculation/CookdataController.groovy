package dbnp.calculation

import grails.plugins.springsecurity.Secured;
import dbnp.authentication.AuthenticationService;
import dbnp.studycapturing.Study;
import dbnp.studycapturing.SamplingEvent;
import dbnp.studycapturing.EventGroup;
import dbnp.studycapturing.Sample;

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
				println "mapSelectionSets: "+flow.mapSelectionSets
				println "mapEquations: "+flow.mapEquations
				flow.results = getResults(flow.mapSelectionSets, flow.mapEquations, flow.samplingEvents, flow.eventGroups)
			}.to "pageFour"
			on("previous"){
				flow.mapSelectionSets = [:]
				flow.samplingEventTemplates = flow.samplingEvents*.template.unique()
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
		println "fields: "+fields
		List toRemove = []
		for(int i = 0; i < fields.size(); i++){
			if(!fields[i].isFilledInList(objects)){
				toRemove.add(fields[i])
			}
		}
		fields.removeAll(toRemove)
		println "fields: "+fields
		return fields
	}
	
	private List getResults(mapSelectionSets, mapEquations, samplingEvents, eventGroups){
		//Step 1) Get data 
		// Get assays
		// For each assay, call related modules and ask for features
		// For each feature, call related module and ask for measurements
		
		//Step 2) while parsing the equation, calculate the results
		// Easy for average or median, difficult for pairwise calculation
		// Question: What constitutes a pair?
		return []
	}
	
	
}

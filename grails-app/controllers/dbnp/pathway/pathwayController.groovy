/**
 * Visualize Controller
 *
 * This controller enables the user to visualize his data
 *
 * @author  Ferry Jagers
 * @since	20110910
 * @package	dbnp.pathway
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

package dbnp.pathway

import dbnp.studycapturing.*
import org.dbnp.gdt.*

class pathwayController {
	def moduleCommunicationService
	def authenticationService

	/**
	 * Controller for the study selection page.
     * Only the studies that are available for the current logged in user will be shown
	 */
	def index = {
        def studyName
        //if there was a loopback to index, selectedStudy will be defined.
        if (params.get("selectedstudy")) {
             studyName = params.get("selectedstudy")
        }
        [studyName: studyName, studyNames: getStudyNames(studies)]
	}

    /**
	 * Controller for the event selection page.
     * The events defined within the selected study will by shown in a list and available for selection
	 */
    def events = {
        println(params)
        //get the selected study
        def studyName = params.get("selectedstudy")
        def Study selectedStudy = getSelectedStudy(studyName, studies)
        //if there are no assays (thus no measurements) in the selected study, the following message will be shown.
        if (selectedStudy.assays.isEmpty()) {
            flash.message = "No assays in study"
            redirect( action: "index" )
            return
        }
        def showTemplates = selectedStudy.giveAllEventTemplates()
        //Create map with all events by group.
        def eGroupMap = [:]
        selectedStudy.eventGroups.each { eGroup ->
            def eList = []
            showTemplates.each { currentEventTemplate ->
            def currentEvents = (eGroup.events).findAll { it.template == currentEventTemplate }.sort { a, b -> a.startTime <=> b.startTime }.asType(List)
            currentEvents.each {
                def currentE = [:]
                currentE.put("template", it.template.name)
                currentE.put("templateStringFields", it.templateStringFields)
                currentE.put("startTime", it.getStartTimeString())
                eList.add(currentE)
                }
            }
            eGroupMap.put(eGroup.name, eList)
        }
        [eGroupMap: eGroupMap, studyName: studyName, studyNames: getStudyNames(studies)]
    }

     /**
	 * Controller for the calculate page.
     * i.g. table with all filtered, available sampleEvents.
     * User can define calculations referring to those sampleEvents.
	 */
    def calculate = {
        //redirect for changed selectedstudy
        if (!params.containsKey("Confirm")) {
            redirect( action: "events", params: [ selectedstudy: params.get("selectedstudy") ])
        }
        else {
            //get the selected study
            def Study selectedStudy = getSelectedStudy(params.get("selectedstudy"), studies)
            def showTemplates = selectedStudy.giveAllEventTemplates()
            def selectedEventGroupsList = []
            def selectedEventGroupsMap = [:]
            def seGroupMap = [:]
            def indexMap = [:]
            def i = 1
            //Add all selected eventGroups to a list.
            selectedEventGroupsList.addAll(params.get("selectedEventGroups"))
            selectedEventGroupsList.unique().each() { eventInfo ->
                def split = eventInfo.split(":")
                if (selectedEventGroupsMap.containsKey(split[1])) {
                    selectedEventGroupsMap.get(split[1]).add(split[0])
                }
                else {
                    def selectedEvents = []
                    selectedEvents.add(split[0])
                    selectedEventGroupsMap.put(split[1], selectedEvents)
                }
            }
            selectedStudy.eventGroups.each { eGroup ->
                def seList = []
                showTemplates.each { currentEventTemplate ->
                    if (selectedEventGroupsMap.containsKey(eGroup.name.toString())) {

                        def currentSamplingEvents = (eGroup.samplingEvents).findAll { it.template == currentEventTemplate }.sort { a, b -> a.startTime <=> b.startTime }.asType(List)
                        currentSamplingEvents.each {
                            //The samplingEvent will only be added if it has a "related event feature"
                            if (it.template.getFieldsByType(TemplateFieldType.STRING).name.contains('related event name') && !it.getFieldValue('related event name').equals(null)) {
                                def fieldValue = it.getFieldValue('related event name')
                                if (selectedEventGroupsMap.get(eGroup.name.toString()).contains(fieldValue)) {
                                    def currentSE = [:]
                                    def filteredSamples = []
                                    currentSE.put("id", i)
                                    currentSE.put("template", it.template.name)
                                    currentSE.put("sampleTemplate", it.sampleTemplate)
                                    currentSE.put("startTime", it.getStartTimeString())
                                    currentSE.put("relatedEvent", fieldValue)
                                    def templateTimeFields = [:]
                                    it.template.getFieldsByType(TemplateFieldType.RELTIME).each { field ->
                                        templateTimeFields.put("RTTCS", new RelTime(it.getFieldValue(field.name)).toString())
                                    }
                                    currentSE.put("templateTimeFields", templateTimeFields)
                                    it.samples.each() { sample ->
                                        if (sample.parentEventGroup.name.equals(eGroup.name)) {
                                            filteredSamples.add(sample.name)
                                        }
                                    }
                                    currentSE.put("filteredSamples", filteredSamples)
                                    indexMap.put(i, filteredSamples)
                                    seList.add(currentSE)
                                    i ++
                                }
                            }
                        }
                        seGroupMap.put(eGroup.name, seList)
                    }

                }
            }
            [selectedEventGroups: selectedEventGroupsList, indexMap: indexMap, seGroupMap: seGroupMap]
        }
    }

    /**
	 * Controller for the event selection page.
     * The events defined within the selected study will by shown in a list and available for selection
	 */
    def visualize = {
        [test: params.get("calcValues")]
    }

    /**
	 * Returns a list of the studies available for logged in user.
	 */
	protected def getStudies() {
        def user = authenticationService.getLoggedInUser()
        return  Study.list().findAll { it.canRead( user ) }
    }

    /**
	 * Returns a list with Names (String) of the studies available for logged in user.
	 */
	protected def getStudyNames(studies) {
        def studyNames = []
        studies.each { study ->
           studyNames.add(study.code + " - " + study.title)
        }
        return studyNames
    }

     /**
	 * Returns the actual study behind the String studyName.
	 */
    protected def getSelectedStudy(studyName, studies) {
        for (study in studies) {
            if (studyName.equals(study.code + " - " + study.title)) {
                return study
            }
        }
    }

    /**
    * Returns the measurementData for certain sample.
    */
    protected def getAssayMeasurementData(assayName, assays) {
        def json
        for (assay in assays) {
            if (assay.name.equals(assayName)) {
                def callUrl = assay.module.url + '/rest/getMeasurementData/query?assayToken=' + assay.giveUUID()
                json = moduleCommunicationService.callModuleMethod( assay.module.url, callUrl )
                def meta = sample.module.url + '/rest/getMeasurementMetaData/query?sampleToken=' + sample.giveUUID() +'&measurementToken=' + json[0][0]
                return meta
            }
        }
    }
}
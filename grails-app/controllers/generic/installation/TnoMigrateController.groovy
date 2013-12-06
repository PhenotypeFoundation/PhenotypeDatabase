package generic.installation

import dbnp.studycapturing.Event
import dbnp.studycapturing.EventGroup
import dbnp.studycapturing.EventInEventGroup
import dbnp.studycapturing.SamplingEvent
import dbnp.studycapturing.SamplingEventInEventGroup
import dbnp.studycapturing.Study
import dbnp.studycapturing.SubjectGroup
import dbnp.studycapturing.SubjectEventGroup

/**
 * User: Seth
 * Date: 28-11-13
 * Time: 12:10
 */
class TnoMigrateController {

    def index() {
    }

    def migrate() {
        def message = "Good luck migrating!";

        def studies = Study.findAll()

        for (Study study in studies) {
            def oldEventGroups = [] + study.eventGroups
            HashMap<String, EventGroup> newEventGroups = []

            if (study.id == 13478) {
                for(EventGroup eventGroup in oldEventGroups) {
                    HashMap<String, ArrayList<Event>> temporaryEventGroups = []
                    def eventInstances = eventGroup.eventInstances + eventGroup.samplingEventInstances

                    // Collect unique set of eventgroups and their respective events
                    eventInstances.each {
                        def migrateEventGroups = it.event.getFieldValue("migration").split(";");
                        for (String newEventGroup in migrateEventGroups) {
                            this.registerUniqueEventGroup(temporaryEventGroups, newEventGroup)
                            this.addEventToEventGroupSet(temporaryEventGroups, it.event)
                        }
                    }

                    // Persist new eventgroups in database that:
                    // - have multiple events
                    // - or single event with only one eventgroup specified in migration column
                    // Else ignore eventgroup
                    temporaryEventGroups.each { eventGroupName, ArrayList eventSet ->
                        if(!(eventSet.size() == 1 && eventSet.first().getFieldValue("Migration").split(";").size() > 1)) {

                            // Collect minimum start to normalize starttimes
                            def eventgroupEventInstances = eventInstances.findAll { eventSet.contains(it.event) }
                            def minimumStartTime = eventgroupEventInstances.collect { it.startTime }.min()

                            // Create and persist eventgroup
                            EventGroup newEventGroup = this.createNewEventGroup(study, eventGroupName)

                            // Associate events to the new eventgroup
                            eventgroupEventInstances.each {
                                this.addEventToEventGroup(it.event, newEventGroup, it.startTime - minimumStartTime)
                            }

                            // Associate subject groups with the new eventgroup
                            SubjectGroup subjectGroup = SubjectGroup.find {id == eventGroup.id}
                            this.updateSubjectGroups(study, subjectGroup, newEventGroup, minimumStartTime)
                        }
                    }
                }

                //Remove old eventgroups
                for (eventGroup in oldEventGroups) {
                    study.deleteEventGroup(eventGroup)
                }
                study.save(failOnError: true)
            }
        }

        [message: message]
    }

    private void registerUniqueEventGroup(HashMap<String, ArrayList<Event>> newEventGroups, String name) {
        if (!newEventGroups.containsKey(name)) {
            newEventGroups.putAt(name, [] as ArrayList)
        }
    }

    private void addEventToEventGroupSet(HashMap<String, ArrayList<Event>> newEventGroups, event) {
        ArrayList<Event> eventListForEG = newEventGroups.get(name)
        eventListForEG.push(event)
        newEventGroups.putAt(name, eventListForEG)
    }

    private EventGroup createNewEventGroup(Study study, String name) {
            def eventGroup = new EventGroup()
            eventGroup.name = name
            study.addToEventGroups(eventGroup)
            eventGroup.save(failOnError: true)
            eventGroup
    }

    private void addEventToEventGroup(Event event, EventGroup eventGroup, startTime) {
        def eventInEventGroup;

        if(event instanceof SamplingEvent) {
            eventInEventGroup = new SamplingEventInEventGroup()
            eventGroup.addToSamplingEventInstances(eventInEventGroup)
        } else {
            eventInEventGroup = new EventInEventGroup()
            eventGroup.addToEventInstances(eventInEventGroup)
        }

        eventInEventGroup.startTime = startTime
        event.addToEventGroupInstances(eventInEventGroup)
        eventInEventGroup.save(failOnError: true)
    }

    private void updateSubjectGroups(Study study, SubjectGroup subjectGroup, EventGroup eventGroup, startTime) {
        SubjectEventGroup subjectEventGroup = new SubjectEventGroup()
        subjectEventGroup.startTime = startTime

        eventGroup.addToSubjectEventGroups(subjectEventGroup)
        subjectGroup.addToSubjectEventGroups(subjectEventGroup)
        study.addToSubjectEventGroups(subjectEventGroup)
        subjectEventGroup.save(failOnError: true)
    }
}
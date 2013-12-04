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

                    for (Event event in study.events) {
                        def migrateEventGroups = event.getFieldValue("migration").split(";");
                        for (String newEventGroup in migrateEventGroups) {
                            this.addEventGroup(temporaryEventGroups, newEventGroup, event)
                        }
                    }

                    for (SamplingEvent samplingEvent in study.samplingEvents) {
                        def migrateEventGroups = samplingEvent.getFieldValue("Migration").split(";");
                        for (String newEventGroup in migrateEventGroups) {
                            this.addEventGroup(temporaryEventGroups, newEventGroup, samplingEvent)
                        }
                    }

                    for(Event event in eventGroup.eventInstances*.event.unique()) {
                        def migrateEventGroup = event.getFieldValue("migration");
                        if(!migrateEventGroup.contains(';')) {
                            EventGroup newEventGroup = this.addNewEventGroup(study, newEventGroups, migrateEventGroup,
                                    temporaryEventGroups.getAt(migrateEventGroup))
                            SubjectGroup subjectGroup = SubjectGroup.find {id == eventGroup.id}
                            this.updateSubjectGroups(study, newEventGroup,subjectGroup)
                        }
                    }
                }

                for (eventGroup in oldEventGroups) {
                    study.deleteEventGroup(eventGroup)
                }
                study.save(failOnError: true)
            }
        }

        [message: message]
    }

    private void addEventGroup(HashMap<String, ArrayList<Event>> newEventGroups, String name, event) {
        if (newEventGroups.containsKey(name)) {
            ArrayList<Event> eventListForEG = newEventGroups.get(name)
            eventListForEG.push(event)
            newEventGroups.putAt(name, eventListForEG)
        } else {
            newEventGroups.putAt(name, [event] as ArrayList)
        }
    }

    private EventGroup addNewEventGroup(Study study, HashMap<String, EventGroup> newEventGroups,
                                        String name, ArrayList events) {
        if(!newEventGroups.containsKey(name)) {
            def eventGroup = new EventGroup()
            eventGroup.name = name
            study.addToEventGroups(eventGroup)
            eventGroup.save(failOnError: true)
            for (event in events) {
                this.addEventToEventGroup(eventGroup,event)
            }
            newEventGroups.putAt(name,eventGroup)
            eventGroup
        } else {
            newEventGroups.getAt(name)
        }
    }

    private void addEventToEventGroup(EventGroup eventGroup, event) {
        def eventInEventGroup;

        if(event instanceof SamplingEvent) {
            eventInEventGroup = new SamplingEventInEventGroup()
            eventGroup.addToSamplingEventInstances(eventInEventGroup)
        } else {
            eventInEventGroup = new EventInEventGroup()
            eventGroup.addToEventInstances(eventInEventGroup)
        }
        event.addToEventGroupInstances(eventInEventGroup)

        eventInEventGroup.save(failOnError: true)
    }

    private void updateSubjectGroups(Study study, EventGroup eventGroup, SubjectGroup subjectGroup) {
        SubjectEventGroup subjectEventGroup = new SubjectEventGroup()
        eventGroup.addToSubjectEventGroups(subjectEventGroup)
        subjectGroup.addToSubjectEventGroups(subjectEventGroup)
        study.addToSubjectEventGroups(subjectEventGroup)
        subjectEventGroup.save(failOnError: true)
    }
}
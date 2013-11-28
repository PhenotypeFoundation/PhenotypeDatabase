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
    ArrayList<EventGroup> newEventGroups

    def index() {
    }

    def migrate() {
        def message = "Good luck migrating";

        def studies = Study.findAll()

        for (Study study in studies) {
            newEventGroups = new ArrayList<EventGroup>()

            if (study.id == 13478) {
                for (Event event in study.events) {
                    def MigrateEventGroups = event.getFieldValue("migration").split(";");
                    for (String newEventGroup in MigrateEventGroups) {
                        this.addEventGroup(newEventGroup, event)
                    }
                }

                for (SamplingEvent samplingEvent in study.samplingEvents) {
                    def MigrateEventGroups = samplingEvent.getFieldValue("Migration").split(";");
                    for (String newEventGroup in MigrateEventGroups) {
                        this.addEventGroup(newEventGroup, samplingEvent)
                    }
                }

                for(EventGroup eventGroup in study.eventGroups) {
                    for(Event event in eventGroup.eventInstances*.event.unique()) {
                        def MigrateEventGroup = event.getFieldValue("migration");
                        if(!MigrateEventGroup.contains(';')) {
                            EventGroup newEventGroup = newEventGroups.find {it.name.equals(MigrateEventGroup)}
                            SubjectGroup subjectGroup = SubjectGroup.find {id == eventGroup.id}
                            this.updateSubjectGroups(newEventGroup,subjectGroup)
                        }
                    }

                    for(SamplingEvent samplingEvent in eventGroup.samplingEventInstances*.event.unique()) {
                        def MigrateEventGroup = samplingEvent.getFieldValue("Migration");
                        if(!MigrateEventGroup.contains(';')) {
                            EventGroup newEventGroup = newEventGroups.find {it.name.equals(MigrateEventGroup)}
                            SubjectGroup subjectGroup = SubjectGroup.find {id == eventGroup.id}
                            this.updateSubjectGroups(newEventGroup,subjectGroup)
                        }
                    }
                }

                while(study.eventGroups.size() > 0) {
                    study.deleteEventGroup(study.eventGroups.first())
                }
                newEventGroups.each {
                    study.addToEventGroups(it)
                }
                study.save(flush:true)
            }
        }


        [message: message, newEventGroups: newEventGroups]
    }

    private void addEventGroup(String name, event) {
        EventGroup eventGroup = newEventGroups.find {it.name.equals(name)}
        if (eventGroup != null) {
            this.addEventToEventGroup(eventGroup,event)
        } else {
            newEventGroups.add(this.newEventGroup(name,event))
        }
    }

    private EventGroup newEventGroup(String name, Event event) {
        def eventGroup = new EventGroup()
        eventGroup.name = name
        this.addEventToEventGroup(eventGroup,event)
        eventGroup.save()
        eventGroup
    }

    private EventGroup addEventToEventGroup(EventGroup eventGroup, event) {
        def eventInEventGroup;

        if(event instanceof SamplingEvent) {
            eventInEventGroup = new SamplingEventInEventGroup()
        } else {
            eventInEventGroup = new EventInEventGroup()
        }

        eventInEventGroup.event = event
        eventInEventGroup.eventGroup = eventGroup
        eventInEventGroup.save()

        if(event instanceof SamplingEvent) {
            eventGroup.addToSamplingEventInstances(eventInEventGroup)
        } else {
            eventGroup.addToEventInstances(eventInEventGroup)
        }

        eventGroup
    }

    private void updateSubjectGroups(EventGroup eventGroup, subjectGroup) {
        SubjectEventGroup subjectEventGroup = new SubjectEventGroup()
        subjectEventGroup.eventGroup = eventGroup
        subjectEventGroup.subjectGroup = subjectGroup
        subjectEventGroup.save()
        eventGroup.addToSubjectEventGroups(subjectEventGroup)
    }
}
package generic.installation

import dbnp.studycapturing.Event
import dbnp.studycapturing.EventGroup
import dbnp.studycapturing.EventInEventGroup
import dbnp.studycapturing.Sample
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
                    def temporaryEventGroups = [:]
                    def eventInstances = eventGroup.eventInstances + eventGroup.samplingEventInstances

                    // Collect unique set of eventgroups and their respective events
                    eventInstances.each {
                        def migrateEventGroups = it.event.getFieldValue( getColumnName(it.event) ).split(";");
                        for (String newEventGroup in migrateEventGroups) {
                            this.registerUniqueEventGroup(temporaryEventGroups, newEventGroup)
                            this.addEventToEventGroupSet(temporaryEventGroups, newEventGroup, it)
                        }
                    }

                    // Persist new eventgroups in database that:
                    // - have multiple events
                    // - or single event with only one eventgroup specified in migration column
                    // Else ignore eventgroup
                    temporaryEventGroups.each { eventGroupName, eventInstanceSet ->
                        if(!(eventInstanceSet.size() == 1 && eventInstanceSet.first().event.getFieldValue( getColumnName(eventInstanceSet.first().event) ).contains(";"))) {

                            // Collect minimum start to normalize starttimes
                            def minimumStartTime = eventInstanceSet.collect { it.startTime }.min()

                            // Create and persist eventgroup
                            EventGroup newEventGroup = this.createNewEventGroup(study, eventGroupName)

                            // Associate events to the new eventgroup
                            eventInstanceSet.each {
                                this.addEventToEventGroupAndUpdateAssociatedSamples(it, newEventGroup, it.startTime - minimumStartTime)
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

    private String getColumnName (event) {
        if(event instanceof SamplingEvent) {
            "Migration"
        } else {
            "migration"
        }
    }

    private void registerUniqueEventGroup(newEventGroups, name) {
        if (!newEventGroups.containsKey(name)) {
            newEventGroups.putAt(name, [] as ArrayList)
        }
    }

    private void addEventToEventGroupSet(eventListForEventGroup, name, event) {
        def eventListForEG = eventListForEventGroup.get(name)
        eventListForEG.push(event)
        eventListForEventGroup.putAt(name, eventListForEG)
    }

    private EventGroup createNewEventGroup(Study study, String name) {
            def eventGroup = new EventGroup()
            eventGroup.name = name
            study.addToEventGroups(eventGroup)
            eventGroup.save(failOnError: true)
            eventGroup
    }

    private void addEventToEventGroupAndUpdateAssociatedSamples(eventInstance, EventGroup eventGroup, startTime) {
        def eventInEventGroup;
        def event = eventInstance.event

        if(event instanceof SamplingEvent) {
            eventInEventGroup = new SamplingEventInEventGroup()
            eventGroup.addToSamplingEventInstances(eventInEventGroup)
        } else {
            eventInEventGroup = new EventInEventGroup()
            eventGroup.addToEventInstances(eventInEventGroup)
        }

        eventInEventGroup.startTime = startTime
        eventInEventGroup.duration = eventInstance.duration
        event.addToEventGroupInstances(eventInEventGroup)
        eventInEventGroup.save(failOnError: true)

        if(event instanceof SamplingEvent) {
            this.updateSampleAssociations(eventInstance, eventGroup, eventInEventGroup)
        }
    }

    private void updateSampleAssociations(oldEventInstance, newEventGroup, newEventInstance) {
        ([] + oldEventInstance.samples.each).each { Sample sample ->
            sample.parentSubjectEventGroup.removeFromSamples(sample)
            sample.parentEvent.removeFromSamples(sample)

            newEventGroup.addToSamples( sample )
            newEventInstance.addToSamples( sample )

            sample.save( flush: true )
        }
    }

    private void updateSubjectGroups(Study study, SubjectGroup subjectGroup, EventGroup eventGroup, startTime) {
        SubjectEventGroup subjectEventGroup = new SubjectEventGroup()
        subjectEventGroup.startTime = startTime

        eventGroup.addToSubjectEventGroups(subjectEventGroup)
        subjectGroup.addToSubjectEventGroups(subjectEventGroup)
        study.addToSubjectEventGroups(subjectEventGroup)
        subjectEventGroup.save(failOnError: true)
    }
	
	/**
	 * Does a deduplication on the events and sampling events, as they may have been copied during the migration
	 * @param study
	 */
	protected void deduplicate( Study study ) {
		// Loop through all events/samplingevents.
		deduplicateEntities( study, study.events );
		deduplicateEntities( study, study.samplingEvents );
	}
	
	/**
	 * Deduplicate a list of entities within a study
	 * @param study
	 * @param entities
	 */
	protected void deduplicateEntities( Study study, entities ) {
		if( !entities )
			return 
			 
		def events = [] + entities
		def deletedIds = []
		entities.each { entity
			// Check whether this event had been deleted before, if it was a 
			// duplicate of another event. In that case, we can skip handling this event 
			if( deletedIds.contains( entity ) )
				return
			
			// If there are duplicates of an event/samplingevent in the database (i.e. another event with the exact same values, even in the templatefields)
			def duplicates = findDuplicates( entities, entity );
			if( duplicates ) {
				deletedIds += handleDuplicates( study, entity, duplicates )
			}
		}
	}
	
	/**
	 * Returns a list of ids that have been deleted
	 * @param study
	 * @param event
	 * @param duplicates
	 * @return
	 */
	protected List handleDuplicates( Study study, def original, def duplicates ) {
		duplicates.each { duplicate ->
			// TODO: 	Loop over associations for the duplicate entity
			// 			and replace each duplicate in the associations with the event itself
		}
		
		// TODO: 	Delete all duplicates (because they don't have any associations anymore)
		//			Please note: use Study.deleteEvent and Study.deleteSamplingEvent for this
		
		return duplicates*.id
	}
	
	/**
	 * Returns a list of duplicate entities for a given entity
	 * @param entities
	 * @param entity
	 * @return
	 */
	protected List findDuplicates( entities, entity ) {
		if( !entities || !entity )
			return null
			
		def fieldsToIgnore = getFieldsToIgnore( entity.class )
		return entities.findAll { otherEntity ->
			for( field in entity.giveFields ) {
				// Ignore some fields in comparison
				if( fieldsToIgnore.contains( field.name ) )
					continue
				
				// If the field in this entity is different from the reference entity, return false
				if( otherEntity.getFieldValue( field.name ) != entity.getFieldValue( field.name ) )
					return false;
			}
			
			return true;
		}
	}
	
	/**
	 * Returns a list of fields to ignore when comparing entities of some type
	 * @param type
	 * @return
	 */
	protected List getFieldsToIgnore( def type ) {
		switch( type ) {
			case Event:
				return [ "Migration" ]
			case SamplingEvent:
				return [ "migration", "sampling name short", "related event/challenge", "related time in related event" ]
			default:
				throw new Exception( "Invalid type: " + type )
		}
	}
}
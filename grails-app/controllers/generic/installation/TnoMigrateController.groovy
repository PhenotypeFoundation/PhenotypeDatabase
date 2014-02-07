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
import grails.plugins.springsecurity.Secured

/**
 * User: Seth
 * Date: 28-11-13
 * Time: 12:10
 */
@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class TnoMigrateController {

    def index() {
    }


    def deduplicatetest() {
        def message = "Succes!"
        def study = Study.get(params.get("studyID"))
        deduplicate(study)
        [message: message]
    }

    def migrate() {
        def message = "Good luck migrating!";

        def studies = Study.findAll()

        for (Study study in studies) {
            def oldEventGroups = [] + study.eventGroups
            HashMap<String, EventGroup> newEventGroups = []
            //35827,1983094
            //def selectedIDS = [35827,30986,1983094,96239,358848]
            def selectedIDS = [1983094]
            def collectedSurplusSamplingEvents = []

            if (selectedIDS.contains((int)study.id)) {
				log.info( "Start migrating study " + study.id + " / " + study.code  )
				log.info( "  Study has " + oldEventGroups?.size() + " subjectgroups" )
				
				for(EventGroup eventGroup in oldEventGroups) {
					log.info( "  Migrating subjectgroup/eventgroup " + eventGroup.id + " / " + eventGroup )
					
                    def temporaryEventGroups = [:]
                    def eventInstances = eventGroup.eventInstances + eventGroup.samplingEventInstances

					log.info( "    Eventgroup has " + eventInstances?.size() + " events and samplingevents" )
					
                    // Collect unique set of eventgroups and their respective events
                    eventInstances.each {
                        def migrateEventGroups
                        try {
                            migrateEventGroups = it.event.getFieldValue( getColumnName(it.event) ).split(";");
                        } catch (Exception e) {
                            println (e)
                        }
                        for (String newEventGroup in migrateEventGroups) {
                            this.registerUniqueEventGroup(temporaryEventGroups, newEventGroup)
                            this.addEventToEventGroupSet(temporaryEventGroups, newEventGroup, it)
                        }
                    }
					
					log.info( "    Eventgroup has " + temporaryEventGroups?.size() + " new eventgroups, based on the migration:" )
					temporaryEventGroups?.each  {
						log.info( "      - " + it.key + " with " + it.value?.size() + " (sampling)events" )
					}

                    // Persist new eventgroups in database that:
                    // - have multiple events
                    // - or single event with only one eventgroup specified in migration column
                    // Else ignore eventgroup
                    temporaryEventGroups.each { eventGroupName, eventInstanceSet ->
                        if(eventInstanceSet && eventInstanceSet.find { it instanceof EventInEventGroup }) {
							log.info( "    New eventgroup " + eventGroupName + " contains at least an event, and will be persisted" )
							
                            // Collect minimum start to normalize starttimes
                            def minimumStartTime = eventInstanceSet.collect { it.startTime }.min()

                            // Create and persist eventgroup
                            eventGroupName += "_" + eventGroup.name
                            EventGroup newEventGroup = this.createNewEventGroup(study, eventGroupName)

                            // Associate subject groups with the new eventgroup
                            SubjectGroup subjectGroup = SubjectGroup.find {id == eventGroup.id}
                            def newSubjectEventGroup = this.updateSubjectGroups(study, subjectGroup, newEventGroup, minimumStartTime)

                            // Associate events to the new eventgroup
                            eventInstanceSet.each {
                                def newEventInEventGroup = this.addEventToEventGroup(it, newEventGroup, it.startTime - minimumStartTime)
                                if(it.event instanceof SamplingEvent) {
									log.info( "      SamplingEvent " + it.event + " is moved to the new eventgroup, and sample associations are updated." )
                                    this.updateSampleAssociations(it, newEventInEventGroup, newSubjectEventGroup)
                                    it.event.name = it.event.template.name;
                                } else {
                                    try {
										log.info( "      Event " + it.event + " is moved to the new eventgroup." )
                                        it.event.name = it.event.getFieldValue("Event name (STRING)");
                                    } catch(NoSuchFieldException nsfe) {
                                        log.warn( "      Event name field is not present in event " + it.event.id + ". No name is assigned." )
                                    }
                                }
                                it.event.save()
                            }
                        } else {
							log.info( "    New eventgroup " + eventGroupName + " doesn't contain events, so the sampling events will be moved to the SurplusSamplingEvents group." )
                            collectedSurplusSamplingEvents = (collectedSurplusSamplingEvents << eventInstanceSet).flatten()
                        }
                    }

                    def minimumStartTime = 0
                    Random rand = new Random()
                    def eventGroupName = "collectedSurplusSamplingEvents" + rand.nextInt(1000)
                    EventGroup newEventGroup = this.createNewEventGroup(study, eventGroupName)

                    SubjectGroup subjectGroup = SubjectGroup.find {id == eventGroup.id}
                    def newSubjectEventGroup = this.updateSubjectGroups(study, subjectGroup, newEventGroup, minimumStartTime)
					
					log.info( "    Created a new surplusSamplingEvents group for subjectGroup " + subjectGroup )
					
                    collectedSurplusSamplingEvents.each {
                        if(it.event instanceof SamplingEvent) {
							log.info( "      Moving samplingevent " + it + " / " + it.event + " to the surplus group. Also updating sample associations." )
                            def newEventInEventGroup = this.addEventToEventGroup(it, newEventGroup, it.startTime - minimumStartTime)
                            this.updateSampleAssociations(it, newEventInEventGroup, newSubjectEventGroup)
                            it.event.name = it.event.template.name+" (surplus)";
                            it.event.save()
                        } else {
                            log.error( "      The collection of surplus events contains something other than a SamplingEvent: " + it + " / " + it.event?.class )
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

    private String getColumnName (event) {
        if(event instanceof SamplingEvent) {
            "Migration"
        } else {
            "Migration"
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
            eventGroup.save(failOnError: true, flush: true)
            eventGroup
    }

    def addEventToEventGroup(eventInstance, EventGroup eventGroup, startTime) {
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
        eventInEventGroup.save(failOnError: true, flush: true)
        return eventInEventGroup
    }

    private void updateSampleAssociations(oldEventInstance, eventInEventGroup, newSubjectEventGroup) {
		log.info( "      Updating sample associations from " + oldEventInstance + " to " + eventInEventGroup + " and " + newSubjectEventGroup )
            		
        ([] + oldEventInstance.samples).each { Sample sample ->
			log.info( "        Updating association for sample " + sample.id + " / " + sample )
            sample.parentSubjectEventGroup.removeFromSamples(sample)
            sample.parentEvent.removeFromSamples(sample)

            eventInEventGroup.addToSamples( sample )
            newSubjectEventGroup.addToSamples ( sample )

            sample.save( flush: true )
        }
    }

    private SubjectEventGroup updateSubjectGroups(Study study, SubjectGroup subjectGroup, EventGroup eventGroup, startTime) {
        SubjectEventGroup subjectEventGroup = new SubjectEventGroup()
        subjectEventGroup.startTime = startTime

        eventGroup.addToSubjectEventGroups(subjectEventGroup)
        subjectGroup.addToSubjectEventGroups(subjectEventGroup)
        study.addToSubjectEventGroups(subjectEventGroup)
        subjectEventGroup.save(failOnError: true, flush: true)
        return subjectEventGroup
    }

    private void updateEventInEventGroup(event, oldEvent) {
        def eventInEventGroups = oldEvent.eventGroupInstances;

        ( [] + eventInEventGroups).each {
            oldEvent.removeFromEventGroupInstances(it)
            event.addToEventGroupInstances(it)
            it.event = event
            it.save(failOnError: true, flush: true)
        }

        oldEvent.save(failOnError: true, flush: true)
        event.save(failOnError: true, flush: true)
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

        ( [] + entities ).each { entity ->
			// Check whether this event had been deleted before, if it was a 
			// duplicate of another event. In that case, we can skip handling this event 
			if( deletedIds.contains( entity.id ) )
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


        ( [] + duplicates).each { duplicate ->
            updateEventInEventGroup(original, duplicate)

            if(duplicate instanceof SamplingEvent) {
                study.deleteSamplingEvent(duplicate)
            } else {
                study.deleteEvent(duplicate)
            }
		}

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
            if(otherEntity.id == entity.id || otherEntity.template.id != entity.template.id)
                return false;

			for( field in entity.giveFields() ) {
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
				return [ "Migration", "Sampling name short", "Related Event/Chall.", "Relative time in related event" ]
			default:
				throw new Exception( "Invalid type: " + type )
		}
	}
}
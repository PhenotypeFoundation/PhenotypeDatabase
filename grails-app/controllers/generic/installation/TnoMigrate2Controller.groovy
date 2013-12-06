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
class TnoMigrate2Controller {

    def index() {
    }

    def migrate() {
        def message = "Good luck migrating!";
		def columns = [ Event: "migration", SamplingEvent: "Migration" ]
		
        def studies = Study.findAll()

        for (Study study in studies) {
            def oldEventGroups = [] + study.eventGroups
            HashMap<String, EventGroup> newEventGroups = []

            if (study.id == 13478) {
                for(EventGroup eventGroup in oldEventGroups) {
					// Each eventgroup is associated with one subjectgroup
					def subjectGroup = eventGroup.subjectEventGroups.get(0)?.subjectGroup
					 
					// Create a unique set of values in the 'migration' column
					def allEvents = eventGroup.eventInstances + eventGroup.samplingEventInstances
					def migrations = allEvents.collect { 
						it.event.getFieldValue( columns[ it.class ] )?.split(";") 
					}.flatten().unique()
					
					
					// For each of those unique values:
					migrations.each { migration ->
						// Create an eventgroup in the new database with the value in the migration column as name
						def newEventGroup = new EventGroup( name: migration )
						
						// Determine the set of events in that new eventgroup, based on the migration column
						def groupEvents = allEvents.findAll {  it.event.getFieldValue( columns[ it.class ] )?.split(";")?.contains( migration ) }
						
						// If this (new) eventgroup contains only one (sampling)event with multiple 
						// values in the migration column, discard this (new) eventgroup
						if( groupEvents == 0 || ( groupEvents.size() == 1 && groupEvents[ 0 ].migration.contains( ";" ) ) ) {
							return	// from closure
						}
						
						// Save the new eventgroup
						newEventGroup.parent = study
						study.addToEventGroups( newEventGroup )
						newEventGroup.save( flush: true )
						
						// Determine the first starttime of all events/samplingevents in that new eventgroup (called eventgroup-starttime)
						def minimumStartTime = groupEvents.collect { it.startTime }.min()
						
						// Associate all events and sampling events to the new eventgroup
						// And move the samples that were associated with the last one
						//
						// Normalize the starttimes of all events by subtracting that eventgroup-starttime 
						// from all start and end times, so the eventgroup itself starts at t=0
						groupEvents.each { instance ->
							switch( instance.event.class ) {
								case Event:
									def newInstance = new EventInEventGroup(
										event: instance.event,
										eventGroup: newEventgroup,
										startTime: instance.startTime - minimumStartTime,
										duration: instance.duration
									)
									
									eventGroup.addToEventInstances( newInstance )
									newInstance.save( flush: true )
									break
								case SamplingEvent:
									def newInstance = new SamplingEventInEventGroup(
										event: instance.event,
										eventGroup: newEventgroup,
										startTime: instance.startTime - minimumStartTime,
										duration: instance.duration
									)
									
									eventGroup.addToSamplingEventInstances( newInstance )
									newInstance.save( flush: true )
									
									// Move all samples that originate from this instance
									def samples = [] + instance.samples
									samples.each { sample ->
										eventGroup.removeFromSamples( sample )
										instance.removeFromSamples( sample )
										
										newEventGroup.addToSamples( sample )
										newInstance.addToSamples( sample )
										
										sample.save( flush: true )
									}
									
									break
							}
						}
						
						// Associate the newly created eventgroup with the corresponding subjectgroup, with the correct eventgroup-starttime
						SubjectEventGroup subjectEventGroup = new SubjectEventGroup( startTime: minimumStartTime )
						newEventGroup.addToSubjectEventGroups(subjectEventGroup)
						subjectGroup.addToSubjectEventGroups(subjectEventGroup)
						study.addToSubjectEventGroups(subjectEventGroup)
						subjectEventGroup.save(failOnError: true)
						
					}
					
					/*
					After this migration, a deduplication step can be done:
					For each study
					Loop through all events/samplingevents.
					If there are duplicates of an event/samplingevent in the database (i.e. another event with the exact same values, even in the templatefields)
					Remove the duplicates, keeping only one of the occurrences. Make sure that all (other) objects referring to the event/sampling event are updated as well (e.g. eventInEventGroup and Sample)
					*/
					
				}
            }
        }
    }
}
package dbnp.studycapturing
import org.dbnp.gdt.*

/**
 * EventGroup groups events
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class EventGroup extends Identity {
	String name

	static belongsTo = [parent : Study]
	static hasMany = [
		eventInstances: EventInEventGroup,
		samplingEventInstances: SamplingEventInEventGroup,
		subjectEventGroups: SubjectEventGroup
	]

	static constraints = {
		// Ensure that the event group name is unique within the study
		name(unique:['parent'])
	}
	
	
	/**
	 * Get the duration of the event as RelTime
	 * @return RelTime
	 */
	def getDuration() {
		// Determine the maximum startTime + duration 
		def lastEndTime = 0
		def start = 0
		(eventInstances + samplingEventInstances).findAll().each {
			if( it.startTime < start && it.startTime >= 0 )
				start = it.startTime
				
			def endTime = it.startTime + it.duration 
			if( endTime > lastEndTime )
				lastEndTime = endTime; 
		}
		
		return new RelTime(lastEndTime - start)
	}
	
	/**
	 * Returns a human readable list of the contents of this event group
	 * @param maxEvents		Number of events/samplingEvents to return at most. Defaults to 3
	 * @return
	 */
	def getContents( def maxEvents = 3 ) {
		// Determine contents
		def contents = [:]
		
		// Find a list of unique events and counts
		def allInstances = ( eventInstances + samplingEventInstances ).findAll() 
		allInstances?.each { instance ->
			if( instance.event ) {
				if( contents.containsKey( instance.event.name ) ) {
					contents[ instance.event.name ]++
				} else {
					contents[ instance.event.name ] = 1
				}
			} 
		}
		
		if( !contents ) 
			return ""
		
		// Sort the map by count, take the most occurring events from the list
		def contentDescription = ""
		def items = contents.sort { a, b ->
			a.value == b.value ? a.key <=> b.key : b.value <=> a.value 
		}.take( Math.min( contents.size(), maxEvents ) ).collect { it.value + "x " + it.key } 
		
		items.join( ", " ) + ( contents.size() > items.size() ? "..." : "" )
	}

}

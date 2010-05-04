package dbnp.studycapturing

import groovy.time.*

/**
 * The Event class describes an actual event, as it has happened to a certain subject. Often, the same event occurs
 * to multiple subjects at the same time. That is why the actual description of the event is factored out into a more
 * general EventDescription class. Events that also lead to sample(s) should be instantiated as SamplingEvents.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Event extends TemplateEntity implements Serializable {

        Date startTime
        Date endTime
        
	static constraints = {
		endTime(validator: {val, obj ->
           if (val && val.before(obj.startTime)) {
                return 'endTimeshouldbegreater'
           }
       	})
	}
	

	List<TemplateField> giveDomainFields() {
		[ new TemplateField( 
                            name: 'startTime',
                            type: TemplateFieldType.DATE),
                        new TemplateField( 
                            name: 'endTime',
                            type: TemplateFieldType.DATE) ];
	}

	// time diff between end and start date
	// thus, do this manually as follows

	static def getDuration(date1, date2) {
		def timeMillis = (date2.getTime() - date1.getTime()).abs()
		def days = (timeMillis / (1000 * 60 * 60 * 24)).toInteger()
		def hours = (timeMillis / (1000 * 60 * 60)).toInteger()
		def minutes = (timeMillis / (1000 * 60)).toInteger()
		def seconds = (timeMillis / 1000).toInteger()
		def millis = (timeMillis % 1000).toInteger()

		return new Duration(days, hours, minutes, seconds, millis)
	}

	def getDuration() {
		return getDuration(startTime, endTime)
	}

	// return a string that prints the duration sensibly.
	// the largest date unit (sec, min, h, day, week, month, or year)
	// is output

	static def getPrettyDuration(duration) {
		def handleNumerus = {number, string ->
			return number.toString() + (number == 1 ? string : string + 's')
		}
		if (duration.getYears() > 0) return handleNumerus(duration.getYears(), " year")
		if (duration.getMonths() > 0) return handleNumerus(duration.getMonths(), " month")
		if (duration.getDays() > 0) return handleNumerus(duration.getDays(), " day")
		if (duration.getHours() > 0) return handleNumerus(duration.getHours(), " hour")
		if (duration.getMinutes() > 0) return handleNumerus(duration.getMinutes(), " minute")
		return handleNumerus(duration.getSeconds(), " second")
	}

	// convenience method. gives formatted string output for a duration
	def getPrettyDuration() {
		getPrettyDuration(getDuration())
	}

	// convenience method. gives formatted string output for a duration
	static def getPrettyDuration(date1, date2) {
		return getPrettyDuration(getDuration(date1, date2))
	}

	def getDurationString() {
		def d = getDuration()
		return "${d.days} days, ${d.hours} hrs, ${d.minutes} min, ${d.seconds} sec."
	}

	def getShortDuration() {
		def d = getDuration()
		def days = d.days
		def hours = d.hours - (24 * days)
		def minutes = d.minutes - (24 * 60 * days) - (60 * hours)
		return "${days}d ${hours}:${minutes}"
	}

	def isSamplingEvent() {
		return (this instanceof SamplingEvent)
	}

	def String toString() {
		return fieldExists( 'Description' ) ? getFieldValue( 'Description') : ""
	}

}
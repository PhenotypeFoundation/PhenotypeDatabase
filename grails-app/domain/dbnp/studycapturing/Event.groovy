package dbnp.studycapturing

import groovy.time.*

/**
 * The Event class describes an actual event, as it has happened to a certain subject. Often, the same event occurs
 * to multiple subjects at the same time. That is why the actual description of the event is factored out into a more
 * general EventDescription class. Events that also lead to sample(s) should be instantiated as SamplingEvents.
 */
class Event {

	Subject subject
	EventDescription eventDescription
	Date startTime
	Date endTime
	Map parameterStringValues
	Map parameterIntegerValues
	Map parameterFloatValues

	static hasMany = [
	        parameterStringValues : String, // stores both STRING and STRINGLIST items (latter should be checked against the list)
		parameterIntegerValues : int,
		parameterFloatValues : float
	]

	// static constraints = { }

	def getDuration() {
		// time diff between end and start date
		// thus, do this manually as follows

		def timeMillis = (endTime.getTime() - startTime.getTime()).abs()
		def days = (timeMillis / (1000 * 60 * 60 * 24)).toInteger()
		def hours = (timeMillis / (1000 * 60 * 60)).toInteger()
		def minutes = (timeMillis / (1000 * 60)).toInteger()
		def seconds = (timeMillis / 1000).toInteger()
		def millis = (timeMillis % 1000).toInteger()

		return new Duration(days, hours, minutes, seconds, millis)
	}


	def getPrettyDuration() {
	     // return a string that prints the duration sensibly.
	     // the largest date unit (sec, min, h, day, month, or year)
	     // is output
	}


	def getDurationString() {
		def d = getDuration()
		return "${d.days} days, ${d.hours} hrs, ${d.minutes} min, ${d.seconds} sec."
	}


	def isSamplingEvent() {
		return ( this instanceof SamplingEvent )
        }
}
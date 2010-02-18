package dbnp.studycapturing

class EventGroup {

	String name

	static hasMany = [
	        subjects : Subject,
		events : Event
	]

    static constraints = {
    }
}

package org.dbnp.gdt

/**
 * RelTime Domain Class
 *
 * A RelTime is a TemplateFieldType that specifies a relative time aka timespan.
 * The timespan is saved as a long value representing the number of seconds in the timespan.
 * A human-readable representation of this number is generated, which renders e.g. '4d 2h' for 4 days 2 hours.
 * Also, a parser is implemented which can interpret such entries when they are entered by the user.
 *
 * This class is purely a helper class to manipulate long fields that represent relative times.
 * There will be no data in the database in the table that is created for this class (probably named rel_time).
 *
 * @author Robert Horlings
 * @since 20100529
 * @package dbnp.studycapturing
 *
 * Revision information:
 * $Rev: 959 $
 * $Author: j.a.m.wesbeek@umail.leidenuniv.nl $
 * $Date: 2010-10-20 21:13:14 +0200 (Wed, 20 Oct 2010) $
 */
class RelTime implements Comparable {
	final static long s = 1L;
	final static long m = 60L * s;
	final static long h = 60L * m;
	final static long d = 24L * h;
	final static long w = 7L * d;

	private long reltimeValue;

	public RelTime() {
		this(0L);
	}

	public RelTime(long reltime) {
		setValue(reltime);
	}

	public RelTime(String reltime) {
		parse(reltime);
	}

	/**
	 * Constructor to create a relative time from two given dates
	 *
	 * The reltime will be the second date, with the first date as reference,
	 * i.e. date2 - date1
	 */
	public RelTime(Date date1, Date date2) {
		computeDifference(date1, date2);
	}

	/**
	 * Constructor to create a relative time from two given relative times
	 *
	 * The reltime will be the second date, with the first date as reference,
	 * i.e. date2 - date1
	 */
	public RelTime(RelTime date1, RelTime date2) {
		this(date1.getValue(), date2.getValue());
	}

	/**
	 * Constructor to create a relative time from two given relative times
	 *
	 * The reltime will be the second date, with the first date as reference,
	 * i.e. date2 - date1
	 */
	public RelTime(long date1, long date2) {
		setValue(date2 - date1);
	}

	/**
	 * Return simple string version of this reltime
	 */
	public String toString() {
		def negative = this.reltimeValue < 0;
		def reltime = this.reltimeValue.abs();

		def seconds = Math.floor((reltime % m) / s).toInteger();
		def minutes = Math.floor((reltime % h) / m).toInteger();
		def hours = Math.floor((reltime % d) / h).toInteger();
		def days = Math.floor((reltime % w) / d).toInteger();
		def weeks = Math.floor(reltime / w).toInteger();

		def stringValue = negative ? "-" : "";
		if (weeks > 0) { stringValue += weeks + "w "; }
		if (days > 0) { stringValue += days + "d "; }
		if (hours > 0) { stringValue += hours + "h "; }
		if (minutes > 0) { stringValue += minutes + "m "; }
		if (seconds > 0) { stringValue += seconds + "s "; }

		if (reltime == 0) stringValue = "0s";

		return stringValue.trim();
	}

	/**
	 * Return pretty human readable string of this reltime
	 */
	public String toPrettyString() {
		// Method to handle the difference between 1 day and 2 dayS
		def handleNumerus = {number, string ->
			return number.toString() + (number == 1 ? string : string + 's')
		}

		def negative = this.reltimeValue < 0;
		def reltime = this.reltimeValue.abs();

		def seconds = Math.floor((reltime % m) / s).toInteger();
		def minutes = Math.floor((reltime % h) / m).toInteger();
		def hours = Math.floor((reltime % d) / h).toInteger();
		def days = Math.floor((reltime % w) / d).toInteger();
		def weeks = Math.floor(reltime / w).toInteger();

		def stringValue = negative ? "-" : "";
		def values = [];
		if (weeks > 0) { values << handleNumerus(weeks, " week") }
		if (days > 0) { values << handleNumerus(days, " day") }
		if (hours > 0) { values << handleNumerus(hours, " hour") }
		if (minutes > 0) { values << handleNumerus(minutes, " minute") }
		if (seconds > 0) { values << handleNumerus(seconds, " second") }

		if (reltime == 0) values << "0 seconds";

		return stringValue + values.join(', ').trim();
	}

	/**
	 * Return pretty human readable string of this reltime
	 */
	public String toPrettyRoundedString() {
		// Method to handle the difference between 1 day and 2 dayS
		def handleNumerus = {number, string ->
			return number.toString() + (number == 1 ? string : string + 's')
		}

		def negative = this.reltimeValue < 0;
		def reltime = this.reltimeValue.abs();

		def seconds = Math.floor((reltime % m) / s).toInteger();
		def minutes = Math.floor((reltime % h) / m).toInteger();
		def hours = Math.floor((reltime % d) / h).toInteger();
		def days = Math.floor((reltime % w) / d).toInteger();
		def weeks = Math.floor(reltime / w).toInteger();

		def stringValue = negative ? "-" : "";
		if (weeks > 0) { return stringValue + handleNumerus(weeks, " week") }
		if (days > 0) { return stringValue + handleNumerus(days, " day") }
		if (hours > 0) { return stringValue + handleNumerus(hours, " hour") }
		if (minutes > 0) { return stringValue + handleNumerus(minutes, " minute") }
		if (seconds > 0) { return stringValue + handleNumerus(seconds, " second") }

		if (reltime == 0) return "0 seconds";

		return "";
	}

	/**
	 * Returns the value in seconds
	 */
	public long getValue() {
		return reltimeValue;
	}

	/**
	 * Sets the value in seconds
	 */
	public void setValue(long value) {
		reltimeValue = value;
	}

	/**
	 * Sets the value as a string.
	 */
	public void setValue(String value) {
		parse(value);
	}

	/**
	 * Return a sentence that may be used in interfaces to give the user an instruction on how to enter RelTimes in string format
	 */
	public static final String getHelpText() {
		return "Use the first letter of weeks/days/hours/minutes/seconds, e.g. '1w 2d' for 1 week + 2 days or '10m30s' for 10 minutes and 30 seconds.";
	}

	/**
	 * Parses a string into a RelTime long
	 *
	 * The relative time may be set as a string, using the following format
	 *
	 *     #w #d #h #m #s
	 *
	 * Where w = weeks, d = days, h = hours, m = minutes, s = seconds
	 *
	 * The spaces between the values are optional. Every timespan
	 * (w, d, h, m, s) must appear at most once. You can also omit
	 * timespans if needed or use a different order.
	 * Other characters are disregarded, allthough results may not
	 * always be as expected.
	 *
	 * If an incorrect format is used, which can't be parsed
	 * an IllegalArgumentException is thrown.
	 *
	 * An empty span is treated as zero seconds.
	 *
	 * Examples:
	 * ---------
	 *    5d 3h 20m     // 5 days, 3 hours and 20 minutes
	 *    6h 2d         // 2 days, 6 hours
	 *    10m 200s      // 13 minutes, 20 seconds (200s == 3m + 20s)
	 *    5w4h15m       // 5 weeks, 4 hours, 15 minutes
	 *
	 *    16x14w10d     // Incorrect. 16x is disregarded, so the
	 *                  // result is 15 weeks, 3 days
	 *    13days        // Incorrect: days should be d, but this is
	 *                  // parsed as 13d, 0 seconds
	 */
	public void parse(String value) {
		long newvalue;

		// An empty string should be parsed as 0
		if (value == null || value.trim() == "" || value.trim() == "-") {
			newvalue = 0L;
		} else {
			// Check whether it is a negative number
			// this is indicated by a dash in front
			def multiplier = 1L;
			if (value.trim()[0] == '-') {
				multiplier = -1L;
			}

			// Find all parts that contain numbers with
			// a character w, d, h, m or s after it
			def periodMatch = value =~ /([0-9\.]+)\s*([wdhms])/
			if (periodMatch.size() > 0) {
				def seconds = 0L;

				// Now check if every part contains data for
				// the time interval
				periodMatch.each {
					def partValue

					if (it[1].isFloat()) {
						// this is a float
						partValue = Float.parseFloat(it[1])
					} else {
						partValue = 0;
					}

					switch (it[2]) {
						case 'w':
							seconds += w * partValue;
							break;
						case 'd':
							seconds += d * partValue;
							break;
						case 'h':
							seconds += h * partValue;
							break;
						case 'm':
							seconds += m * partValue;
							break;
						case 's':
							seconds += s * partValue;
							break;
						default:
							adf.error.warn('Parsing relative time: ' + it[0] + it[1] + ' is not understood and disregarded');
							break;
					}
				}

				// Continue with the computed value
				newvalue = (multiplier * seconds).toLong()
			} else {
				throw new IllegalArgumentException("String " + value + " cannot be parsed as a relative time. Use format #w #d #h #m #s.");
				return;
			}
		}

		setValue(newvalue);
	}

	public void computeDifference(Date start, Date end) {
		if (start && end) {
			long seconds = (end.getTime() - start.getTime()) / 1000L;
			setValue(seconds);
		} else {
			setValue(0);
		}
	}

	static RelTime parseRelTime(String value) {
		RelTime reltime = new RelTime();
		reltime.parse(value);
		return reltime;
	}

	public boolean equals(Object o) {
		if (o == null)
		return false;

		if (!(o instanceof RelTime))
		return false;

		RelTime rt = (RelTime) o;

		return rt.reltimeValue == this.reltimeValue;
	}

	public int compareTo(Object o) throws ClassCastException {
		if (o == null)
		throw new ClassCastException("Can't cast object to RelTime");

		if (!(o instanceof RelTime))
		throw new ClassCastException("Can't cast object to RelTime");

		RelTime rt = (RelTime) o;
		return this.reltimeValue <=> rt.reltimeValue;
	}
}

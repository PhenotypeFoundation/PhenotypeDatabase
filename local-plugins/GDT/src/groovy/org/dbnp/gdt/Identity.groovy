package org.dbnp.gdt

/**
 * Identity Domain Class
 *
 * This class keeps an internal incremented static identity
 * which can be used to identify objects. This is particularly
 * handy for usage in large dynamic web forms. You could do
 * something like:
 *
 * <g:each var="subject" in="${Subject.findAll()}">
 *   <g:each var="field" in="${Subject.giveFields}">
 *		<g:textField name="subject_${subject.getIdentifier()}_${field.escapedName()}" ... />
 *   </g:each>
 * </g:each>
 *
 * So you can easily handle the post data in the controller
 * without relying on an iterator of your own as this proves
 * to be very unreliable and quite some extra code and effort
 * in both controller and views:
 *
 * Subject.findAll().each() { subject->
 * 	 subject.giveFields() { field->
 *		subject.setFieldValue(
 *			field.name,
 * 			params.get('subject_${subject.getIdentifier()}_${field.escapedName()')
 * 		)
 * 	 }
 * 	 if (!subject.validate()) { .... }
 * }
 *
 * Comparing the internal object identifier makes things a lot
 * easier.
 *
 * @Author	Jeroen Wesbeek	<work@osx.eu>
 * @Since	20100805
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
abstract class Identity implements Serializable {
	// keep an internal identifier for use in dynamic forms
	private int identifier = 0
	private int maximumIdentity = 99999
	static int iterator = 0

	// UUID of this instance
	String UUID

	// transients
	static transients = [ "identifier", "iterator", "maximumIdentity" ]

	// constraints
	static constraints = {
		UUID(nullable:true, unique:true, maxSize: 255)
	}

	/**
	 * Method to increment the static iterator variable. This method
	 * is synchronized to assure a thread-safe increment.
	 * @visibility private
	 * @int
	 */
	synchronized final private int setIdentifier() {
		// increment the iterator variable
		// reset iterator to 1 if it is becoming too high
		iterator = (iterator >= maximumIdentity) ? 1 : iterator+1

		// set the instance identifier
		identifier = iterator

		return identifier
	}

	/**
	 * Return the identifier
	 * @visibility public
	 * @return int
	 */
	final public int getIdentifier() {
		// set identifier if not yet set
		if (!identifier) {
			return setIdentifier()
		} else {
			return identifier
		}
	}

	/**
	 * reset the identifier, this can be used in tests
	 * to be able to predict identifiers
	 */
	synchronized final static resetIdentifier = {->
		iterator = 0
	}

	/**
	 * Returns the UUID of this instance and generates one if needed
	 *
	 * @return String UUID
	 */
	final public String giveUUID() {

		// does this instance have an UUID?
		if (!this.UUID) {
			// no, generate generic UUID
			this.UUID = java.util.UUID.randomUUID().toString()
		}

		return this.UUID
	}

    // Assume GORM is present, this class should only be extended by domain classes
    def beforeInsert() {
        giveUUID()
    }
}
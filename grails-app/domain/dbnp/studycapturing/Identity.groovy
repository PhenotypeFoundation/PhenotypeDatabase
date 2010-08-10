package dbnp.studycapturing

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
 * @Author	Jeroen Wesbeek	<J****n.W*****k@gmail.com>
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

	// transients
	static transients = [ "identifier", "iterator", "maximumIdentity" ]

	/**
	 * Class constructor increments that static iterator
	 * and sets the object's identifier
	 * @visibility public
	 * @void
	 */
	public Identity() {
		// set the local identifier
		setIdentifier()

		// feedback
		println "instantiating [${identifier}:${super.getClass()}]"
	}

	/**
	 * Method to increment the static iterator variable. This method
	 * is synchronized to assure a thread-safe increment.
	 * @visibility private
	 * @void
	 */
	synchronized final private void setIdentifier() {
		// increment the iterator variable
		// reset iterator to 1 if it is becoming too high
		iterator = (iterator >= maximumIdentity) ? 1 : iterator+1

		// set the instance identifier
		identifier = iterator
	}

	/**
	 * Return the identifier
	 * @visibility public
	 * @return int
	 */
	final public int getIdentifier() {
		return identifier
	}
}

package dbnp.studycapturing

import org.apache.log4j.Logger


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
 * to be very unreliable:
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
	 * and sets the object's identifier (used in dynamic webforms)
	 * @void
	 */
	public Identity() {
		// does this instance have an identifier?
		if (!identifier) {
			// no, increment the iterator
			identifier = iterator++

			// has the iterator become too large?
			if (iterator >= maximumIdentity) {
				// yes, reset it back to 0
				iterator = 0
			}
		}

		println ".instantiating [" + super.getClass() + "] ("+ identifier + ")"
	}

	/**
	 * Return the identifier
	 * @return int
	 */
	final public int getIdentifier() {
		return identifier
	}
}

package dbnp.studycapturing

import dbnp.data.Term

/**
 * The Sample class describes an actual sample which results from a SamplingEvent.
 */
class Sample {
	static searchable = true



    String name      // should be unique with respect to the parent study (which can be inferred)
    Term material
    // don't we need a member that describes the quantity of the sample? --> should be in the templates

    static constraints = {
    }

}

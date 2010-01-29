package dbnp.studycapturing

import dbnp.data.Term

/**
 * The Sample class describes an actual sample which results from a SamplingEvent.
 */
class Sample {
	static searchable = true

    // TODO: should Sample also carry a reference to its parent study,
    // or should this be inferred via the parent SamplingEvent?

    String name      // should be unique with respect to the parent study (which can be inferred)
    Term material
    // don't we need a member, that describes the quantity of the sample?

    static constraints = {
    }

}

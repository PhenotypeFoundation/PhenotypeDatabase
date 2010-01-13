package dbnp.studycapturing

/**
 * The Sample class describes an actual sample which results from a SamplingEvent.
 */
class Sample {

    // TODO: should Sample also carry a reference to its parent study,
    // or should this be inferred via the parent SamplingEvent?

    // TODO: should Sample also carry a reference to its parent subject,
    // or should this be inferred via the parent SamplingEvent?

    String name // should be unique with respect to the parent study (which can be inferred
    Term material

    static constraints = {
    }
}

package dbnp.studycapturing

import dbnp.data.Term

/**
 * The ProtocolParameter class describes a protocol parameter, and belongs to the Protocol class.
 * Actual values of this parameter are stored in the corresponding field of the ProtocolInstance class.
 */
class ProtocolParameter {

    String name
    ProtocolParameterType type
    String unit
    String description
    Term reference

    static hasMany = [listEntries : String] // to store the entries to choose from when the type is 'item from predefined list'

    static constraints = {
    }
}

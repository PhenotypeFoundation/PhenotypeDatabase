package dbnp.studycapturing

import dbnp.data.Term

class Compound {

    String name
    Term compound
    float dose
    String doseUnit
    boolean isCarrier

    static constraints = {
        compound(nullable:true)
        dose(nullable:true)
        doseUnit(nullable:true,blank:true)
    }
}

package dbnp.studycapturing

/**
 * Domain class describing the basic entity in the study capture part: the Study class.
 */
class Study {

    nimble.User owner
    String title
    String code
    String researchQuestion
    String description
    String ecCode
    Date dateCreated
    Date lastUpdated
    Date startDate
    Template template

    static hasMany = [ editors : nimble.User, readers : nimble.User,
            subjects: Subject, groups : SubjectGroup,
            events: Event, samplingEvents : SamplingEvent]

    static constraints = {
        template(nullable:true,blank:true)
    }

    static mapping = {
        researchQuestion type:'text'
        description type:'text'
    }

}

package dbnp.studycapturing

import org.dbnp.gdt.*
import dbnp.authentication.*
import org.dbxp.sam.Measurement

/**
 * This class describes an Assay, which describes the application of a certain (omics) measurement to multiple samples.
 * The actual data of these measurements are described in submodules of dbNP. The type property describes in which module
 * this data can be found.
 */
class Assay extends TemplateEntity {
    // The name of the assay, which should indicate the measurements represented in this assay to the user.
    String name

    // The dbNP module in which the assay omics data can be found. */
    AssayModule module
    
    // Determines whether anonymous users are allowed to see this study. This has only effect when published = true    
    boolean publicassay = false  

    /**
     * return the domain fields for this domain class
     * @return List
     */
    @Override
    List<TemplateField> giveDomainFields() { return domainFields }

    static final List<TemplateField> domainFields = [
            new TemplateField(
                    name: 'name',
                    type: TemplateFieldType.STRING,
                    preferredIdentifier: true,
                    comment: 'The name you give here is used to discern this assay within the study (e.g. \'liver transcriptomics\', \'blood lipidomics\')',
                    required: true
            ),
            new TemplateField(
                    name: 'module',
                    type: TemplateFieldType.MODULE,
                    comment: 'Select the dbNP module where the actual assay measurement data is stored',
                    required: true
            ),
            new TemplateField(
                    name: 'publicassay',
                    type: TemplateFieldType.BOOLEAN,
                    comment: 'By selecting this option you accept that the measurements of this assay will become <b>public</b> under the <b>Creative Commons Attribution-ShareAlike 3.0 Unported License</b>',
                    required: false
            )
    ]

    // An Assay always belongs to one study.
    static belongsTo = [parent: Study]

    // An Assay can have many samples on which it is performed, but all samples should be within the 'parent' Study.
    static hasMany = [samples: Sample]

    static constraints = {
        // Ensure that the assay name is unique within the study
        name(unique: ['parent'])
    }

    static mapping = {
        sort "name"

        // Workaround for bug http://jira.codehaus.org/browse/GRAILS-6754
        templateTextFields type: 'text'
        
        // Set default value for publicAssay field. This will ensure
        // that the application will properly upgrade the database for
        // older versions of the applications
        publicassay defaultValue: "false"
    }

    def String toString() {
        return name;
    }

    /**
     * Basic equals method to check whether objects are equals, by comparing the ids
     * @param o Object to compare with
     * @return True iff the id of the given Study is equal to the id of this Study
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;

        if (!(o instanceof Assay))
            return false

        Assay s = (Assay) o;

        return this.id == s.id
    }

        /**
     * Returns all assays this user can read
     * @param user  User for which the assays should be returned
     * @return      List of assay objects
     */
    public static def giveReadableAssays( SecUser user ) {
        Study.giveReadableStudies(user).collect { it.assays }.flatten()
    }

    /**
     * Returns all studies this user can write
     * @param user  User for which the assays should be returned
     * @return      List of assay objects
     */
    public static def giveWritableAssays( SecUser user ) {
        Study.giveWritableStudies(user).collect { it.assays }.flatten()
    }

    /**
     * Returns all assays this user owns
     * @param user  User for which the assays should be returned
     * @return      List of assay objects
     
    public static def giveMyAssays( User user ) {
        if( user )
            return Assay.executeQuery( "SELECT DISTINCT a FROM Assay a, Auth auth WHERE ( auth.user = :user AND auth.study = a.study AND auth.isOwner = true )", [ "user": user ] )
        else
            return []
    }*/
	
    /**
     * Return 
     * @return
     */
    public int getSampleCount() {
            def c = Assay.createCriteria()
            def result = c.list {
                    eq("id", this.id)
                    createAlias('samples', 'samples')
                    projections {
                            rowCount()
                    }
                    groupProperty("id")
            }

            if( result && result[0] ) {
                    return result[0][0]
            } else {
                    log.warn "Invalid result for retrieving samples counts on assay " + this + ": " + result
                    return 0
            }
    }

    /**
     * Returns true if the given user is allowed to read this study
     */
    public boolean canReadAssay(SecUser loggedInUser) {
            // Public studies may be read by anyone
            if( this.parent.publicstudy && this.publicassay) {
                    return true;
            }

            // Anonymous readers are only given access when published and public
            if (loggedInUser == null) {
                    return false;
            }

            // Administrators are allowed to read every study
            if (loggedInUser.hasAdminRights()) {
                    return true;
            }

            // Owners and writers are allowed to read this study
            if (this.parent.owner == loggedInUser || this.parent.writers.contains(loggedInUser)) {
                    return true
            }

            // Readers are allowed to read this study when it is published
            //		if (this.readers.contains(loggedInUser) && this.published) {
            if (this.parent.readers.contains(loggedInUser)) {
                    return true
            }

            return false
    }
  
    /**
     * Checks whether the SAM module has measurements associated with this assay
     */
    public boolean hasMeasurements() {
        def num = Measurement.executeQuery( "SELECT COUNT(*) FROM Measurement m WHERE m.sample.parentAssay = :assay", [ "assay": this ] )
        
        return num[0] > 0
    }
}

package dbnp.studycapturing

/**
 * The Publication class represents a PubMed-registered publication.
 * Publication entries should be created using the study wizard, which connects to PubMed to fill in the fields.
 * Since a Publication can apply to multiple studies, the entries in this table form an independent 'library'
 * and are not connected to Study instances via a cascading relation.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Publication extends Identity {
	String title
	String pubMedID
	String DOI      // document identifier, see dx.doi.org
	String authorsList
	String comments

	static constraints = {
		pubMedID(nullable: true, blank: true)
		DOI(nullable: true, blank: true)
		authorsList(nullable: true, blank: true)
		comments(nullable: true, blank: true)
	}
}

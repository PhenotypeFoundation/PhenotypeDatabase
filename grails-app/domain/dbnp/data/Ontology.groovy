package dbnp.data

/**
 * This class describes an existing ontology, of which terms can be stored (actually 'cached' would be a better description)
 * in the (global) Term store.
 * This information is mapped from the BioPortal NCBO REST service, e.g.: http://rest.bioontology.org/bioportal/ontologies/38802
 * @see http://www.bioontology.org/wiki/index.php/NCBO_REST_services
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Ontology implements Serializable {
	String name             // BioPortal: displayLabel
	String description      // BioPortal: description
	String url              // BioPortal: homepage
	String versionNumber    // BioPortal: versionNumber
	int ncboId              // BioPortal: ontologyId
	int ncboVersionedId     // BioPortal: id
}

package dbnp.data

/**
 * This class describes an existing ontology, of which terms can be stored (actually 'cached' would be a better description)
 * in the (global) Term store.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Ontology implements Serializable {
    String name
    String shortName
    String url
}

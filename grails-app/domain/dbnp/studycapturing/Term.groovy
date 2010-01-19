package dbnp.studycapturing

/**
 * The Term object describes a term in the ontology that is referred to in other entities such as events.
 * The Term object should point to an existing term in an online ontology, therefore instances of this class can also
 * be seen as a cache of elements of the external ontology.
 */
class Term {

    String name
    Ontology ontology
    String accession

    static constraints = {
    }

  def String toString() {
    return name
  }


}

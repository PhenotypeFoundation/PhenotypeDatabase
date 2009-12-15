/**
 * The ChipAnnotation entity.
 *
 * @author    
 *
 *
 */
class ChipAnnotation {
    static mapping = {
         table 'chip_annotation'
         // version is set to false, because this isn't available by default for legacy databases
         version false
         id generator:'identity', column:'id'
         chipIdChip column:'chip_id'
    }
    Integer id
    String probeset
    String accession
    String geneSymbol
    String description
    String databaseType
    // Relation
    Chip chipIdChip

    static constraints = {
        id(max: 2147483647)
        probeset(size: 0..50)
        accession(size: 0..50)
        geneSymbol(size: 0..80)
        description(size: 0..300)
        databaseType(size: 0..45)
        chipIdChip()
    }
    String toString() {
        return "${id}" 
    }
}

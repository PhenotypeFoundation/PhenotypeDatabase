/**
 * The Expression Domain Class
 *
 * What is this?
 *
 * @author  Robert Kerkhoven
 * @since   20091215
 * @package Proteomics
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Expression {
    static mapping = {
         table 'expression'
         // version is set to false, because this isn't available by default for legacy databases
         version false
         id generator:'identity', column:'id'
         chipAnnotationIdChipAnnotation column:'chip_annotation_id'
    }
    Integer id
    Integer studySampleAssayId
    String expressionValue
    // Relation
    ChipAnnotation chipAnnotationIdChipAnnotation

    static constraints = {
        id(max: 2147483647)
        studySampleAssayId(max: 2147483647)
        expressionValue()
        chipAnnotationIdChipAnnotation()
    }
    String toString() {
        return "${id}" 
    }
}

package dbnp.clinicaldata

/**
 * Enum describing the data type of a clinical measurement
 */
public enum ClinicalMeasurementType {
	NUMBER('Number'), // measurement is stored in ClinicalFloatData
	STRING('String')  // measurement is stored in ClinicalStringData

	String name

	ClinicalMeasurementType(String name) {
		this.name = name
	}

	static list() {
		[STRING, NUMBER]
	}

	// It would be nice to see the description string in the scaffolding,
	// and the following works, but then the item cannot be saved properly.
	// TODO: find a way to display the enum description but save the enum value in the scaffolding
	/*def String toString() {
    return this.name
}*/

}
package dbnp.data

/**
 * Enum describing the different assay types (aka omics submodules).
 */
public enum FeatureType {
	QUANTITATIVE('Quantitative'),
	QUALITATIVE('Qualitative'),
	PAIRED('Paired'),
	DIFFERENTIAL('Differential')

	String name

	FeatureType(String name) {
		this.name = name
	}

	static list() {
		[QUANTITATIVE, QUALITATIVE, PAIRED, DIFFERENTIAL]
	}
}
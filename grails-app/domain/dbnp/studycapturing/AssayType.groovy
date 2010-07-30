package dbnp.studycapturing

/**
 * Enum describing the different assay types (aka known dbNP submodules).
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
public enum AssayType implements Serializable {
	TRANSCRIPTOMICS('Transcriptomics'),
	METABOLOMICS('Metabolomics'),
	SIMPLE_ASSAY('Simple Assay')

	String name

	AssayType(String name) {
		this.name = name
	}

	static list() {
		[TRANSCRIPTOMICS, METABOLOMICS, SIMPLE_ASSAY]
	}
}
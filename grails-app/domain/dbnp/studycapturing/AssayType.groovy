package dbnp.studycapturing

/**
 * Enum describing the different assay types (aka omics submodules).
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
public enum AssayType {
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
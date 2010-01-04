package dbnp.transcriptomics.magetab.adf

class ArrayDesign {

	String arrayDesignName
	String arrayDesignFile
	String versionNumber // renamed from version to versionNumber to circumvent problems with Hibernate
	String provider
	String printingProtocol
	dbnp.transcriptomics.magetab.idf.OntologyTerm arrayDesignRef
	dbnp.transcriptomics.magetab.idf.OntologyTerm surfaceType
	dbnp.transcriptomics.magetab.idf.OntologyTerm sequencePolymerType
	dbnp.transcriptomics.magetab.idf.OntologyTerm technologyType
	dbnp.transcriptomics.magetab.idf.OntologyTerm substrateType

	static hasMany = [
		designElements:DesignElement,
		userDefinedAttributes:dbnp.transcriptomics.magetab.idf.UserDefinedAttribute
	]

	static constraints = {
     		arrayDesignRef(nullable:true)
		surfaceType(nullable:true)
		sequencePolymerType(nullable:true)
		technologyType(nullable:true)
		substrateType(nullable:true)
}

}


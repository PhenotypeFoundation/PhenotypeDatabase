package magetab.adf

class ArrayDesign {

	String arrayDesignName
	String arrayDesignFile
	String versionNumber // renamed from version to versionNumber to circumvent problems with Hibernate
	String provider
	String printingProtocol
	magetab.idf.OntologyTerm arrayDesignRef
	magetab.idf.OntologyTerm surfaceType
	magetab.idf.OntologyTerm sequencePolymerType
	magetab.idf.OntologyTerm technologyType
	magetab.idf.OntologyTerm substrateType

	static hasMany = [
		designElements:DesignElement,
		userDefinedAttributes:magetab.idf.UserDefinedAttribute
	]

	static constraints = {
     		arrayDesignRef(nullable:true)
		surfaceType(nullable:true)
		sequencePolymerType(nullable:true)
		technologyType(nullable:true)
		substrateType(nullable:true)
}

}


package org.dbnp.gdt

/**
 * Enum describing the type of a TemplateField.
 *
 * Revision information:
 * $Rev: 1385 $
 * $Author: business@keesvanbochove.nl $
 * $Date: 2011-01-13 00:24:47 +0100 (Thu, 13 Jan 2011) $
 */
public enum TemplateFieldType implements Serializable {
	STRING		('String'		,'Short text'			, 'Text'		, 'max 255 chars'),			// string
	TEXT		('Text'			,'Long text'			, 'Text'		, 'unlimited number of chars'),	// text
	DOUBLE		('Double'		,'Decimal number'		, 'Numerical'	, '1.31'),					// double
	STRINGLIST	('StringList'	,'Dropdown selection of terms', 'Text'	, ''),						// string list
	EXTENDABLESTRINGLIST	('ExtendableStringList'	, 'Extendable selection of terms', 'Text'	, ''),						// user extendable string list
	ONTOLOGYTERM('Term'			,'Term from ontology'	, 'Other'		, 'A term that comes from one or more selected ontologies'),// ontology reference
	DATE		('Date'			,'Date'					, 'Date'		, '2010-01-01'),			// date
	RELTIME		('RelTime'		,'Relative time'		, 'Date'		, '3 days'),				// relative date, e.g. days since start of study
	FILE		('File'			,'File'					, 'Other'		, '')		,				// file
	BOOLEAN		('Boolean'		,'True/false'			, 'Other'		, 'true/false'),			// boolean
	TEMPLATE	('Template'		,'Template'				, 'Other'		, ''),						// template
	MODULE		('Module'		,'Omics module'			, 'Other'		, ''),						// third party connected module,
	LONG		('Long'			,'Natural number'		, 'Numerical'	, '100')					// long

    String name
	String casedName
	String category
	String example

	TemplateFieldType(String casedName, String name, String category, String example) {
		this.name		= name
		this.casedName	= casedName
		this.category	= category
		this.example	= example
	}

	/**
	 * return the valid template field types
	 * @return
	 */
	static list() {
		TemplateFieldType.enumConstantsShared
	}
}
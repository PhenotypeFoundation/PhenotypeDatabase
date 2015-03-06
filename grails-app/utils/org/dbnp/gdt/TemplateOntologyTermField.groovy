/**
 *  GDT, a plugin for Grails Domain Templates
 *  Copyright (C) 2011 Jeroen Wesbeek
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  $Author$
 *  $Rev$
 *  $Date$
 */
package org.dbnp.gdt

class TemplateOntologyTermField extends TemplateFieldTypeNew {
	static contains				= Term
	static String type			= "ONTOLOGYTERM"
	static String casedType		= "Term"
	static String description	= "Term from ontology"
	static String category		= "Other"
	static String example		= "A term that comes from one or more selected ontologies"

	// GDT dynamic options
    // as of 20130118 we have removed the AST transformations
    // see ticket https://github.com/PhenotypeFoundation/GSCF/issues/64
    // therefore the maps is now hardcoded and not injected anymore
	// static gdtAddTemplateFieldHasMany = [ontologies: org.dbnp.gdt.Ontology] // to store the ontologies to choose from when the type is 'ontology term'
    // see TemplateField's hasMany map...
    // end change

	/**
	 * Static validator closure
	 * @param fields
	 * @param obj
	 * @param errors
	 */
	static def validator = { fields, obj, errors ->
		genericValidator(fields, obj, errors, TemplateFieldType.ONTOLOGYTERM, { value -> (value as Term) })
	}

	/**
	 * cast value to the proper type (if required and if possible)
	 * @param TemplateField field
	 * @param mixed value
	 * @return Term
	 * @throws IllegalArgumentException
	 */
	static Term castValue(org.dbnp.gdt.TemplateField field, value, def currentValue) {
		if (value) {
			if (value instanceof Term) {
				return value
			} else if (value.class == String) {
				// find ontology that has this term
				def ontology = field.ontologies.find{it.giveTermByName(value)}
				if (ontology) {
					return ontology.giveTermByName(value)
				} else {
					// TODO: search ontology for the term online (it may still exist) and insert it into the Term cache
					// if not found, throw exception
					throw new IllegalArgumentException("Ontology term not recognized (not in the ontology cache): ${value}")
				}
			} else {
				throw new IllegalArgumentException("Ontology term not recognized (not in the ontology cache): ${value}")
			}
		} else {
			return null
		}
	}
}
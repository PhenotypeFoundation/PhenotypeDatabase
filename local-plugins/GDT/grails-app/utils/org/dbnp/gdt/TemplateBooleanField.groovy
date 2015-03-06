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

class TemplateBooleanField extends TemplateFieldTypeNew {
	static contains				= Boolean
	static String type			= "BOOLEAN"
	static String casedType		= "Boolean"
	static String description	= "true/false"
	static String category		= "Other"
	static String example		= "A term that comes from one or more selected ontologies"

	/**
	 * Static validator closure
	 * @param fields
	 * @param obj
	 * @param errors
	 */
	static def validator = { fields, obj, errors ->
		genericValidator(fields, obj, errors, TemplateFieldType.BOOLEAN, { value -> (value) ? true : false })
	}

	/**
	 * cast value to the proper type (if required and if possible)
	 * @param TemplateField field
	 * @param mixed value
	 * @return Boolean
	 * @throws IllegalArgumentException
	 */
	static Boolean castValue(org.dbnp.gdt.TemplateField field, value, def currentValue) {
		if (!value) {
			return false
		} else if (value instanceof Boolean) {
			return value
		} else if (value instanceof String) {
			def lower	= value.toLowerCase()
			def trueMap	= ["true","on","x","yes","ja","aan","+","*"]
			def falseMap= ["false","off","","no","nee","uit","-"]

			// do some 'smart' recognitions
			if (trueMap.find{it == lower}) {
				return true
			} else if (falseMap.find{it == lower}) {
				return false
			} else {
				throw new IllegalArgumentException("Boolean not recognized and could not be cast to Boolean: ${value}")
			}
		}
	}
}

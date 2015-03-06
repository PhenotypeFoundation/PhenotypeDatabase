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

class TemplateDoubleField extends TemplateFieldTypeNew {
	static contains				= Double
	static String type			= "DOUBLE"
	static String casedType		= "Double"
	static String description	= "Decimal number"
	static String category		= "Numerical"
	static String example		= "1.31"

	/**
	 * Static validator closure
	 * @param fields
	 * @param obj
	 * @param errors
	 */
	static def validator = { fields, obj, errors ->
		genericValidator(fields, obj, errors, TemplateFieldType.DOUBLE, { value -> (value.toDouble()) }, { value -> println "obsolete?"; return (value as Double) })
	}

	/**
	 * cast value to the proper type (if required and if possible)
	 * @param TemplateField field
	 * @param mixed value
	 * @return String
	 * @throws IllegalArgumentException
	 */
	static Double castValue(org.dbnp.gdt.TemplateField field, value, def currentValue) {
		if (value) {
			if (value instanceof Double) {
				return value
			} else if (value.class == String) {
				return Double.parseDouble(value)
			} else if (isNumeric(value)) {
				return value.toDouble()
			} else {
				// invalid value
				throw new IllegalArgumentException("Double value not recognized: ${value} (${value.class})")
			}
		} else {
			return new Double(0)
		}
	}
}

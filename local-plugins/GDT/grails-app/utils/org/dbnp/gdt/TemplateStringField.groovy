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

class TemplateStringField extends TemplateFieldTypeNew {
	static contains				= String
	static String type			= "STRING"
	static String casedType		= "String"
	static String description	= "Short text (< 255 chars)"
	static String category		= "Text"
	static String example		= "max 254 characters"

	/**
	 * Static validator closure
	 * @param fields
	 * @param obj
	 * @param errors
	 */
	static def validator = { fields, obj, errors ->
		genericValidator(fields, obj, errors, TemplateFieldType.STRING, { value -> (value as String) }, { value -> throw new Exception('dummy') }, { value -> return (value.class == String && value.size() > 255) ? 'templateEntity.tooLong.string' : true })
	}

	/**
	 * cast value to the proper type (if required and if possible)
	 * @param TemplateField field
	 * @param mixed value
	 * @return String
	 * @throws IllegalArgumentException
	 */
	static String castValue(org.dbnp.gdt.TemplateField field, value, def currentValue) {
		if (!value) {
			return null
		} else if (value.class != String) {
			value = value.toString()
		}

		// is the value > 255 characters?
		// - it seems like PostgreSQL does not like 255 characters and throws an error:
		//   ERROR: value too long for type character varying(255)
		//   so we are cutting it off at 254 characters which DOES work
		if (value.size() > 254) {
			// cut it off at 255
			value = value[0..250]+"..."
		}

		return value
	}
}

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

class TemplateTemplateField extends TemplateFieldTypeNew {
	static contains				= Template
	static String type			= "TEMPLATE"
	static String casedType		= "Template"
	static String description	= "Template"
	static String category		= "Other"
	static String example		= ""

	/**
	 * Static validator closure
	 * @param fields
	 * @param obj
	 * @param errors
	 */
	static def validator = { fields, obj, errors ->
		genericValidator(fields, obj, errors, TemplateFieldType.TEMPLATE, { value -> (value as Template) })
	}

	/**
	 * cast value to the proper type (if required and if possible)
	 * @param TemplateField field
	 * @param mixed value
	 * @return Template
	 * @throws IllegalArgumentException
	 */
	static Template castValue(org.dbnp.gdt.TemplateField field, value, def currentValue) {
		if (value) {
			if (value instanceof Template) {
				return value
			} else if (value instanceof String) {
				def template = Template.findByName(value)

				if (template) {
					return template
				} else {
					// invalid value
					throw new IllegalArgumentException("Template value not recognized: ${value} (${value.class})")
				}
			} else {
					// invalid value
					throw new IllegalArgumentException("Template value not recognized: ${value} (${value.class})")
			}
		} else {
			return null
		}
	}
}

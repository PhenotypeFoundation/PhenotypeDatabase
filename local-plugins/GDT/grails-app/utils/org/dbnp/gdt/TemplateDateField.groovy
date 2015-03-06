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

class TemplateDateField extends TemplateFieldTypeNew {
	static contains				= Date
	static String type			= "DATE"
	static String casedType		= "Date"
	static String description	= "Date"
	static String category		= "Date"
	static String example		= "2010-01-01"

	/**
	 * Static validator closure
	 * @param fields
	 * @param obj
	 * @param errors
	 */
	static def validator = { fields, obj, errors ->
		genericValidator(fields, obj, errors, TemplateFieldType.DATE, { value -> (value as Date) })
	}

	/**
	 * cast value to the proper type (if required and if possible)
	 * @param TemplateField field
	 * @param mixed value
	 * @return Date
	 * @throws IllegalArgumentException
	 */
	static Date castValue(org.dbnp.gdt.TemplateField field, value, def currentValue ) {
		if (value) {
			if (value instanceof Date) {
				return value
			} else if (value instanceof String) {

				// a string was given, attempt to transform it into a date instance
				// and -for now- assume the dd/mm/yyyy format
				def dateMatch = value =~ /^([0-9]{1,})([^0-9]{1,})([0-9]{1,})([^0-9]{1,})([0-9]{1,})((([^0-9]{1,})([0-9]{1,2}):([0-9]{1,2})){0,})/
				if (dateMatch.matches()) {
					// create limited 'autosensing' datetime parser
					// assume dd mm yyyy  or dd mm yy
					def parser = 'd' + dateMatch[0][2] + 'M' + dateMatch[0][4] + (((dateMatch[0][5] as int) > 999) ? 'yyyy' : 'yy')

					// add time as well?
					if (dateMatch[0][7] != null) {
						parser += dateMatch[0][8] + 'HH:mm'
					}

					return new Date().parse(parser, value)
				} else {
					throw new IllegalArgumentException("Date string not recognized: ${value} (${value.class})")
				}
			} else {
				throw new IllegalArgumentException("Date value not recognized: ${value} (${value.class})")

			}
		} else {
			return null
		}
	}
}

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

class TemplateRelTimeField extends TemplateFieldTypeNew {
	static contains				= Long
	static String type			= "RELTIME"
	static String casedType		= "RelTime"
	static String description	= "Relative time"
	static String category		= "Date"
	static String example		= "3w 5d 2h"

	/**
	 * Static validator closure
	 * @param fields
	 * @param obj
	 * @param errors
	 */
	static def validator = { fields, obj, errors ->
		genericValidator(fields, obj, errors, TemplateFieldType.RELTIME, { value -> (value as long) })
	}

	/**
	 * cast value to the proper type (if required and if possible)
	 * @param TemplateField field
	 * @param mixed value
	 * @return RelTime
	 * @throws IllegalArgumentException
	 */
	static Long castValue(org.dbnp.gdt.TemplateField field, value, def currentValue) {
		if (value) {
			if (value instanceof Long) {
				return value
			} else if (value instanceof String) {
				try {
					return RelTime.parseRelTime(value).getValue();
				} catch (IllegalArgumentException e) {
					return Long.MIN_VALUE;
				}
			} else if (isNumeric(value)) {
				return value.toLong()
			} else {
				// invalid value
				throw new IllegalArgumentException("RelTime value not recognized: ${value} (${value.class})")
			}
		} else {
			return Long.MIN_VALUE
		}
	}
}

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

abstract class TemplateFieldTypeNew implements Serializable {
	// inject the GdtService
	def gdtService

	// enable / disable class debugging
	def static debug = false

	/**
	 * class constructor
	 */
	def public TemplateFieldTypeNew() {
		if (debug) println ".instantiating ${this}"

		// make sure the GdtService is available
		if (!gdtService) {
			gdtService = new GdtService()
		}
	}

	/**
	 * Magic setter
	 * @param mixed value
	 */
	//abstract public castValue(org.dbnp.gdt.TemplateField field, java.lang.String value)

	/**
	 * Static validator closure
	 * @param fields
	 * @param obj
	 * @param errors
	 */
	//abstract validator(fields, obj, errors)

	/**
	 * Generic Validator
	 * @param fields
	 * @param obj
	 * @param errors
	 * @param templateFieldType
	 * @param castClosure
	 * @param parseClosure
	 * @param extraValidationClosure
	 * @return
	 */
	static def genericValidator(fields, obj, errors, templateFieldType, castClosure) {
		genericValidator(fields, obj, errors, templateFieldType, castClosure, { value -> throw new Exception('dummy')}, { value -> return true })
	}
	static def genericValidator(fields, obj, errors, templateFieldType, castClosure, parseClosure) {
		genericValidator(fields, obj, errors, templateFieldType, castClosure, parseClosure, { value -> return true })
	}
	static def genericValidator(fields, obj, errors, templateFieldType, castClosure, parseClosure, extraValidationClosure) {
		def error = false
		def fieldTypeName = templateFieldType.toString()
		def lowerFieldTypeName = fieldTypeName.toLowerCase()
		def capitalizedFieldTypeName = lowerFieldTypeName[0].toUpperCase() + lowerFieldTypeName.substring(1)

		if (debug) println ".validating ${obj} (${obj.class}) :: ${fieldTypeName} with fields: ${fields}"

		// catch exceptions
		try {
			// iterate through values
			fields.each { key, value ->
				// check if the value exists and is of the proper type
				if (value) {
					// check if it is of the proper type
					if (value.class.toString().toLowerCase() != lowerFieldTypeName) {
						// no, try to cast value
						try {
							fields[key] = castClosure(value)
						} catch (Exception castException) {
							if (debug) println "    - 1 ${castException.getMessage()}"

							// could not cast value to the proper type, try to parse value
							try {
								fields[key] = parseClosure(value)
							} catch (Exception parseException) {
								if (debug) println "    - 2 ${parseException.getMessage()}"

								// cannot cast nor parse value, invalid value
								error = true
								errors.rejectValue(
									"template${capitalizedFieldTypeName}Fields",
									"templateEntity.typeMismatch.${lowerFieldTypeName}",
									[key, value.class] as Object[],
									"Property {0} must be of type ${fieldTypeName} and is currently of type {1}"
								)
							}
						}
					} else {
						// yes, try extra validation
						// 	- return boolean: validation success
						//	- return string: validation failed (contains i18n translation
						//    location, e.g. templateEntity.tooLong.string)
						def extraValidation = extraValidationClosure(value)
						if (extraValidation.class == String) {
							if (debug) println "    - 3"

							error = true
							errors.rejectValue(
								"template${capitalizedFieldTypeName}Fields",
								extraValidation,
								[key] as Object[],
								"Property {0} does not pass extra validation (${extraValidation})"
							)
						}
					}
				}
			}

			// validating required fields
			obj.getRequiredFields().findAll { it.type == templateFieldType }.each { field ->
				if (!fields.find { key, value -> key == field.name }) {
					if (debug) println "    - 4 required field (${field.name}) not found!"

					// required field is missing
					error = true
					errors.rejectValue(
						"template${capitalizedFieldTypeName}Fields",
						'templateEntity.required',
						[field.name] as Object[],
						'Property {0} is required but it missing'
					)
				}
			}
		} catch (Exception e) {
			if (debug) {
				println "Exception in the genericValidators: ${e.getMessage()}"
				println e.stackTrace
			}
		}

		return (!error)
	}

	/**
	 * Check of a certain value is of a numeric type (which
	 * can be cast to eachother)
	 * @param value
	 * @return
	 */
	static Boolean isNumeric(value) {
		return (value instanceof Double ||
				value instanceof Float ||
				value instanceof Integer ||
				value instanceof Long ||
				value instanceof BigDecimal ||
				value instanceof BigInteger ||
				value instanceof Short ||
				value instanceof Byte)
	}
}
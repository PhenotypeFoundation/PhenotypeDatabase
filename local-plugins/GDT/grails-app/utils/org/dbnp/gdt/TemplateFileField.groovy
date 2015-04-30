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

class TemplateFileField extends TemplateFieldTypeNew {
	static contains				= String
	static String type			= "FILE"
	static String casedType		= "File"
	static String description	= "File"
	static String category		= "Other"
	static String example		= ""

	// Inject the service for storing files (for TemplateFields of TemplateFieldType FILE).
	//def fileService

	/**
	 * Static validator closure
	 * @param fields
	 * @param obj
	 * @param errors
	 */
	static def validator = { fields, obj, errors ->
		genericValidator(fields, obj, errors, TemplateFieldType.FILE, { value -> (value as String) })
		// currently the validator only casts to String, perhaps we also
		// need to look on the filesystem if the file actually exists using
		// the 'extraValidationClosure' ?
	}

	/**
	 * cast value to the proper type (if required and if possible)
	 * @param TemplateField field
	 * @param mixed value
	 * @return mixed value
	 * @throws IllegalArgumentException
	 */
	static def castValue(org.dbnp.gdt.TemplateField field, value, def currentValue) {
		// Sometimes the fileService is not created yet
		// See http://burtbeckwith.com/blog/?p=993
		def fileService = new FileService();
		fileService.grailsApplication = new Template().domainClass.grailsApplication

		// Magic setter for files: handle values for file fields
		//
		// If NULL is given or "*deleted*", the field value is emptied and the old file is removed
		// If an empty string is given, the field value is kept as was
		// If a file is given, it is moved to the right directory. Old files are deleted. If
		//   the file does not exist, the field is kept
		// If a string is given, it is supposed to be a file in the upload directory. If
		//   it is different from the old one, the old one is deleted. If the file does not
		//   exist, the old one is kept.
		def currentFile = currentValue

		if (value == null || (value.class == String && value == '*deleted*')) {
			// If NULL is given, the field value is emptied and the old file is removed
			value = "";
			if (currentFile) {
				fileService.delete(currentFile)
			}
		} else if (value.class == File) {
			// a file was given. Attempt to move it to the upload directory, and
			// afterwards, store the filename. If the file doesn't exist
			// or can't be moved, "" is returned
			value = fileService.moveFileToUploadDir(value);

			if (value) {
				if (currentFile) {
					fileService.delete(currentFile)
				}
			} else {
				value = currentFile;
			}
		} else if (value == "") {
			value = currentFile;
		} else {
			// Check whether there is 'existing*' in the beginning of the string
			// In that case, the original file should be kept
			if (value == "existing*") {
				value = "";
			} else if (value.size() >= 9 && value[0..8] == "existing*") {
				// Keep current file
				value = currentFile;
			} else {
				if (fileService.fileExists(value)) {
					// When a FILE field is filled, and a new file is set
					// the existing file should be deleted
					if (currentFile) {
						fileService.delete(currentFile)
					}
				} else {
					// If the file does not exist, the field is kept
					value = currentFile;
				}
			}
		}

		return value
	}
}

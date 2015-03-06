/**
 *  GDTImporter, a plugin for importing data into Grails Domain Templates
 *  Copyright (C) 2011 Tjeerd Abma
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

package org.dbnp.gdtimporter

import org.dbnp.gdt.*

/**
 * The ImportMapping contains a collection of mappings (MappingColumn)
 *
 * <b>name</b> - column name in the dataset (Excel sheet)
 * <b>templatefieldtype</b> - TemplateFieldType derived from the GDT-TemplateField
 * <b>entityclass</b> - entity this mapping belongs to (TemplateEntity domain class)
 * <b>property</b> - field name from the GDT-Template where the column will be mapped to
 * <b>index</b> - index of the column in the dataset (0=first column)
 * <b>identifier</b> - boolean true if this column is an identifier, otherwise false
 * <b>dontimport</b> - boolean true if this column should not be imported, otherwise false
 *
 * @see GdtMappingColumn
 */

class GdtImportMapping implements Serializable {

	static hasMany = [gdtmappingcolumns: GdtMappingColumn]
    Template template
    Class entity
	String name

	static constraints = {
	}
}

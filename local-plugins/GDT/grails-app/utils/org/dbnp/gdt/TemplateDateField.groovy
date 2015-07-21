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
                // and -for now- assume the yyyy-mm-dd format
                def format = "yyyy-MM-dd"
                def date = null
                
                try { 
                    // Date parsing using the Date class also accepts many other formats, without warning.
                    // For that reason, we do a custom validation first
                    def dateMatch = /^([0-9]{4})-([0-9]{2})-([0-9]{2})$/
                    if( value.size() == format.size() && value =~ dateMatch ) {
                        return Date.parse(format, value)
                    } else {
                        throw new IllegalArgumentException("Date string not recognized: ${value} (${value.class})")
                    }
                } catch( Exception e ) {
                    throw new IllegalArgumentException("Date string not recognized: ${value} (${value.class})", e)
                }
            } else {
                throw new IllegalArgumentException("Date value not recognized: ${value} (${value.class})")
            }
        } else {
            return null
        }
    }
}

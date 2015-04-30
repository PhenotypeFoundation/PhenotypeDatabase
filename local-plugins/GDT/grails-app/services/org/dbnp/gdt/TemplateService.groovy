package org.dbnp.gdt

import groovy.sql.Sql

/**
 * These functions can not be part of the Template Domain Class due to the usage of dataSource,
 * this conflicts with the usage of webflow.
 */
class TemplateService {
    def dataSource

    /**
     * Checks whether this template is used by any object
     *
     * @returns	boolean true if this template is used by any object, false otherwise
     */
    def inUse( Template template ) {
        return (numUses( template ) > 0 )
    }

    /**
     * The number of objects that use this template
     *
     * @returns	integer the number of objects that use this template.
     */
    def numUses( Template template ) {
        def sql = new Sql(dataSource)

        def query = "SELECT COUNT(template_id) FROM ${template.entity.simpleName.replaceAll(/\B[A-Z]/) { '_' + it }.toLowerCase()} WHERE template_id = ${template.id}"

        def count = sql.rows(query.toString())[0].size()

        return count.toInteger()
    }
}

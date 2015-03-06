package org.dbnp.gdt

import groovy.sql.Sql


/**
 * These functions can not be part of the TemplateField Domain Class due to the usage of dataSource,
 * this conflicts with the usage of webflow.
 */
class TemplateFieldService {
    def dataSource

    /**
     * Retrieves all list items of a stringlist template field that have been used in an object
     *
     * @return ArrayList containing all list items of this template field that have been used in an object.
     */
    def List getUsedListEntries( TemplateField templateField ) {

        if ((templateField.type != TemplateFieldType.STRINGLIST && templateField.type != TemplateFieldType.EXTENDABLESTRINGLIST) || templateField.listEntries.size() == 0)
            return []

        def sql = new Sql(dataSource)

        def query

        if (templateField.type == TemplateFieldType.STRINGLIST) {
            query = "SELECT DISTINCT y.templatefieldlistitemname FROM ${templateField.entity.simpleName.replaceAll(/\B[A-Z]/) { '_' + it }.toLowerCase()}_template_string_list_fields x, template_field_list_item y WHERE x.template_string_list_fields_idx = '${templateField.name}' AND x.template_field_list_item_id = y.id;"
        }
        else if(templateField.type == TemplateFieldType.EXTENDABLESTRINGLIST) {
            query = "SELECT DISTINCT y.templatefieldlistitemname FROM ${templateField.entity.simpleName.replaceAll(/\B[A-Z]/) { '_' + it }.toLowerCase()}_template_extendable_string_list_fields x, template_field_list_item y WHERE x.template_extendable_string_list_fields_idx = '${templateField.name}' AND x.template_field_list_item_id = y.id;"
        }
        else {
            return []
        }

        return sql.rows(query.toString()).collectAll() { it.templatefieldlistitemname }
    }

    /**
     * Retrieves all list items of a stringlist template field that have never been used in an object
     *
     * @return ArrayList containing all list items of this template field that have never been used in an object.
     */
    def List getNonUsedListEntries( TemplateField templateField ) {

        if ((templateField.type != TemplateFieldType.STRINGLIST && templateField.type != TemplateFieldType.EXTENDABLESTRINGLIST) || templateField.listEntries.size() == 0)
            return []

        def usedFields = getUsedListEntries()

        return TemplateFieldListItem.findAllByParent(templateField).name - usedFields
    }

    /**
     * Retrieves all list items of a stringlist template field that have never been used in an object based on a previous executed getUsedTemplateFieldListEntries.
     *
     * @return ArrayList containing all list items of this template field that have never been used in an object.
     */
    def List getNonUsedListEntries( TemplateField templateField, List usedFields ) {

        if ((templateField.type != TemplateFieldType.STRINGLIST && templateField.type != TemplateFieldType.EXTENDABLESTRINGLIST) || templateField.listEntries.size() == 0)
            return []

        return TemplateFieldListItem.findAllByParent(templateField).name - usedFields
    }


    /**
     * Checks whether this template field is used in a template
     *
     * @returns true iff this template field is used in a template (even if the template is never used), false otherwise
     */
    def inUse( TemplateField templateField ) {
        return numUses( templateField ) > 0;
    }

    /**
     * The number of templates that use this template field
     *
     * @returns the number of templates that use this template field.
     */
    def numUses( TemplateField templateField ) {
        def sql = new Sql(dataSource)
        def query = "SELECT COUNT(template_fields_id) FROM template_template_field WHERE template_field_id = ${templateField.id};"

        return sql.rows(query.toString())[0].count.toInteger()
    }
}

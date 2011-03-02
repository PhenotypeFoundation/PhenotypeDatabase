/**
 * AssayTagLib Tag Library
 *
 * Description of my tab library
 *
 * @author  your email (+name?)
 * @since	2010mmdd
 * @package	???
 *
 * Revision information:
 * $Rev: 1430 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-21 21:05:36 +0100 (Fri, 21 Jan 2011) $
 */
package dbnp.studycapturing

class AssayExporterTagLib {

    static namespace = 'assayExporter'

    def categorySelector = {attrs, body ->

        out << '<div class="element">'
        out << g.checkBox(name: attrs.ref, value: true, class: 'category')
        out << attrs.category
        out << '</div>'

    }

    def fieldSelectors = { attrs, body ->

        attrs.fields.eachWithIndex { it, index ->

            def helpText = it.comment

            out << '<div class="element">'
            out << g.checkBox(name: "${attrs.ref}_$index", value: true, class: 'field')
            out << it.name
            if (helpText) {
                out << '<div class="helpIcon"></div>'
                out << "<div class=\"helpContent\">$helpText</div>"
            }
            out << '</div>'
        }
    }
}

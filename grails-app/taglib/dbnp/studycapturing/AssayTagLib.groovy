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

class AssayTagLib {

    static namespace = 'assay'

    def categorySelector = {attrs, body ->

        out << "$attrs.category"

        out << "${g.checkBox(name: attrs.ref, value: true)}"

    }

    def fieldSelector = { attrs, body ->

        attrs.fieldNames.size().times {

            out << attrs.fieldNames[it]
            out << "${g.checkBox(name: "${attrs.ref}_$it", value: true)}"

        }

    }
}

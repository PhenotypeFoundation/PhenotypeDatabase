/**
 * RestController Controler
 *
 * Description this is for testing the dbNP.rest.CCMCommunicationManager only and should be removed,
 * once the CCMCommunicationManager works with an exteneral server (nbx5)!!!!
 * 
 * This class renders two REST related requests (features and get_json). 
 *
 * @author  Jahn 
 * @since   20100526
 *
 */

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.*
import dbnp.studycapturing.TemplateFieldListItem
import dbnp.studycapturing.Template
import dbnp.rest.CCMCommunicationManager


class RestController {


    /**
     * result of querying the Clinical Chemistry Module
     * if assay in the database : return the Clinical Assay
     * else : return the list of all Assays in the database
     * @return Clinical Assay
     */
    def features = {
            //def items = TemplateFieldListItem.list()
            def items = Template.list()
            items.each{ render (it as JSON) } 
            render params
    }


    /* Use a REST resource to get data using the CCMCommunicationManager */
    def get_json = {
        def json_result = new CCMCommunicationManager().getFeatures()
        json_result.each { render "Value : ${it}\n"}
    }

}

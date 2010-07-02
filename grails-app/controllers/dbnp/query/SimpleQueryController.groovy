/**
 * SimpleQueryController Controler
 *
 * Description of my controller
 *
 * @author  vincent@ludden.nl
 * @since	20100526
 * @package	dbnp.query
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.query

import dbnp.data.*
import dbnp.studycapturing.Study
import org.compass.core.engine.SearchEngineQueryParseException
import dbnp.rest.common.CommunicationManager

class SimpleQueryController {
	/**
	 * index closure
	 */
    def index = {
      redirect( action:'pages')
    }

    def searchableService

    def pagesFlow = {

        // Starting simpleQuery flow, initialize variables
        onStart {
            println "Starting webflow simpleQuery"
            flow.search_term            = null
            flow.page                   = 0
			flow.pages = [
                [title: 'Query'],
				[title: 'Results']
			]
	    }

        // Render the query page and handle its actions
		query {
			render(view: "/simpleQuery/mainPage")

            onRender {
              println "Rendering mainPage"
              flow.operators              = ['>', '=', '<']
              flow.showFirstRowCompounds  = true
              flow.species = Term.findAll()
              flow.page = 1
            }

            on("search") {
              println "Search!"
              if (!params.search_term.trim()) {
                return [:]
              }
            }.to "searching"

            on("refresh").to "query"
		}


        // Searching for results
        searching {
           action {
              println "Starting search..."
              def searchResult
              def searchGscfResult
              def searchSamResult

              // TODO: walk parameters, remove empty entries

              // Map parameters
              flow.search_term            = params.search_term
              flow.search_sa_compounds    = params.sa_compound
              flow.search_sa_operators    = params.sa_operator
              flow.search_sa_values       = params.sa_value
              flow.search_tt_genepaths    = params.sa_genepath
              flow.search_tt_regulations  = params.sa_regulation

              // Check to see how parameters are being handled
              if (flow.search_sa_compounds.class.getName() == "java.lang.String") {
                println "string"
              } else {
                println "array of size " + flow.search_sa_compounds.length
              }

              // Search the keyword with the Searchable plugin
              try {
                searchGscfResult = searchableService.search(flow.search_term)
              } catch (SearchEngineQueryParseException ex) {
                println ex
                return [parseException: true]
              }

              // Map non-study objects to Studies
              // ... todo when the plugin works and I can see the output

              // Search in the SAM module
              println "checking compounds"
              if (flow.search_sa_compounds) {
                objComs = new CommunicationManager()

                if (flow.search_sa_compounds.class.getName() == "java.lang.String") {
                  searchSamResult = objComs.getSAMStudies(flow.search_sa_compounds, flow.search_sa_values, flow.search_sa_operators)
                } else {
                  def tmpSamResult
                  flow.search_sa_compounds.each {
                    obj, i -> println " ${i}: ${obj}" // objComs.getSAMStudies()
                    tmpSamResult = objComs.getSAMStudies(flow.search_sa_compounds[i], flow.search_sa_values[i], flow.search_sa_operators[i])

                    // Combine each search
                    // searchSamResult = Merge(searchSamResult, tmpSamResult)
                    searchSamResult = tmpSamResult
                  };
                }
              }

             // Merge the results of all searches
             /*
             if (searchGscfResult.size() > 0) {
                searchResult = Merge(searchSamResult, searchGscfResult)
             }             
             */


             // Save the results in the flow
             flow.listStudies = searchGscfResult.results

           }

          on("error").to "query"
          on("success").to "results"
        }


        // Render result page including search options
        results {
            render(view: "/simpleQuery/mainPage")

            onRender {
              println "Rendering resultPage"
              flow.page = 2

              // TODO: Fix the showing of entered data, broke with plugin develeopment
              if (flow.search_sa_compounds) {
                if (flow.search_sa_compounds.class.getName() == "java.lang.String") {
                  flow.resultString = true
                } else {
                  flow.resultString = false
                }
              }
            }

            on("reset") {
              flow.search_term            = null
              flow.studies                = null
              flow.search_sa_compounds    = null
              flow.search_sa_values       = null
              flow.search_tt_genepaths    = null
              flow.search_tt_regulations  = null
              println "Resetting query flow"
            }.to "query"

            on("search").to "searching"
            on("refresh").to "results"
        }

    }
}
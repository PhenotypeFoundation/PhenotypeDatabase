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

class SimpleQueryController {
	/**
	 * index closure
	 */
    def index = {
      redirect( action:'pages')
    }

    def searchableService

    def pagesFlow = {

        onStart {
            println "Starting webflow simpleQuery"
            flow.search_term            = null
            flow.page = 0
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

              println "Searching"
              println params
             
              flow.search_term            = params.search_term
              flow.search_sa_compounds    = params.sa_compound
              flow.search_sa_values       = params.sa_value
              flow.search_tt_genepaths    = params.sa_genepath
              flow.search_tt_regulations  = params.sa_regulation

              // Searchable plugin not yielding results yet
              /*
              try {
                println searchableService.countHits("mouse")
              } catch (SearchEngineQueryParseException ex) {
                //return [parseException: true]
                println ex
              }
              */

              // Search for the term in Terms
              // results = searchableService.search(flow.search_term, type:"Term")

              // Map the Terms to Studies
              // ...

              // Save the results in the flow
              // flow.studies = results



              // As a usable result set we will use all studies for now
              flow.listStudies = Study.findAll()

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

              if (flow.search_sa_compounds) {
                if (flow.search_sa_compounds.class.getName() == "java.lang.String") {
                  flow.resultString = true
                } else {
                  flow.resultString = false
                }
              }

              println flow.search_sa_compounds.getClass()
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
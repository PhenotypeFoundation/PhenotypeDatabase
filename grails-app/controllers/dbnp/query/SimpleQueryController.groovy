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
            flow.term = null
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
            on("addCompound") {
                println "addCompound"              
            }.to "query"

            on("addTransciptome") {
              println "addTransciptome"
            }.to "query"

            on("search") {
              println "Search!"
              println params
              if (!params.term.trim()) {
                return [:]
              }

              flow.term = params.term
            }.to "searching"

            on("refresh").to "query"
		}


        // Searching for results
        searching {
           action {
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
              // results = searchableService.search(flow.term, type:"Term")

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
              println flow.term
            }

            on("reset") {
              flow.term = null
              flow.studies = null
              println "Resetting query flow"
            }.to "query"

            on("search").to "searching"
            on("refresh").to "results"
        }

    }
  




}
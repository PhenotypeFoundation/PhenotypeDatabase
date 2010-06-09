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
            flow.page = 0
			flow.pages = [
                [title: 'Query'],
				[title: 'Results']
			]
	    }

		query {
			render(view: "/simpleQuery/mainPage")
            onRender {
              println "Rendering mainPage"
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
            }.to "results"

            on("refresh").to "query"
		}

        results {
            def results

            render(view: "/simpleQuery/mainPage")
            onRender {
              println "Rendering resultPage"
              println flow.term



              Study.findAll().each() {
                println it
              }
            }

            on("search").to "searching"
            on("refresh").to "results"
        }

        searching {
           action {
              try {
                println searchableService.search(params.q)
              } catch (SearchEngineQueryParseException ex) {
                return [parseException: true]
              }
           } on("success").to ("query")
        }
    }
  




}
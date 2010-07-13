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
            flow.search_sa_compounds    = []
            flow.search_sa_operators    = []
            flow.search_sa_values       = []
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

              if (flow.search_sa_compounds.size() == 0) {
                flow.showFirstRowCompounds  = true
                println "showRow true"
              } else {
                flow.showFirstRowCompounds  = false
                println "showRow false"
              }

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
              println "Starting simpleQuery search..."
              def searchResult
              def searchGscfResult
              def searchSamResult   = []

              // TODO: walk parameters, remove empty entries

              // Map GSCF parameters
              flow.search_term            = params.search_term        // String

              // Map SAM parameters
              if (params.sa_compound instanceof String) {
                //flow.search_sam = [:]
                flow.search_sa_compounds = []
                flow.search_sa_operators = []
                flow.search_sa_values    = []

                flow.search_sa_compounds.add(params.sa_compound)
                flow.search_sa_operators.add(params.sa_operator)
                flow.search_sa_values.add(params.sa_value)
              } else {
                flow.search_sa_compounds  = params.sa_compound as List
                flow.search_sa_operators  = params.sa_operator as List
                flow.search_sa_values     = params.sa_value as List
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

              // Search in the SAM module when a compound is entered
              // Todo: check whether the module is active and to be used
              // ...
              if (flow.search_sa_compounds.size() > 0) {
                def resultSAM = []
                resultSAM = this.searchSAM(flow.search_sa_compounds)
                println "Sam result: " + resultSAM
              }
                                         
             // Merge the results of all searches
             if (searchGscfResult.size() > 0) {
               
                searchResult = searchSamResult + searchGscfResult
             }             


             // Save the results in the flow
             flow.listStudies = searchGscfResult.results
             println flow.listStudies

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

              flow.showFirstRowCompounds  = false
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


   static List searchSAM (List compounds) {
     if (compounds.size() == 1) {
       println "Single SAM call"
       def mapSamResult
       mapSamResult = CommunicationManager.getQueryResult(compounds.get(0))
       
       return mapSamResult.studies

     } else {
       println "Multiple SAM calls"
       def tmpSamResult
       def i = 0

       compounds.each() {
         println compounds.get(i)

         println "set tmpSamResult"

         // Combine each search
         // searchSamResult = Merge(searchSamResult, tmpSamResult)
         // searchSamResult += tmpSamResult
         i++
       };
     }

   }



   static List merge (List list1, List list2) {

     def resultList = []
     resultList = list1.intersect(list2)

     return resultList
   }

}
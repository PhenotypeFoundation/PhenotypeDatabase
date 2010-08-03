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
import dbnp.studycapturing.Assay
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

              if (!flow.search_sa_compounds) {
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
                println "Compounds as String"
                //flow.search_sam = [:]
                flow.search_sa_compounds = []
                flow.search_sa_operators = []
                flow.search_sa_values    = []

                if (params.sa_compound) { 
                  flow.search_sa_compounds.add(params.sa_compound)
                  flow.search_sa_operators.add(params.sa_operator)
                  flow.search_sa_values.add(params.sa_value)
                }
              } else {
                println "Compounds as List"
                flow.search_sa_compounds  = params.sa_compound as List
                flow.search_sa_operators  = params.sa_operator as List
                flow.search_sa_values     = params.sa_value as List
              }

              // Search the keyword with the Searchable plugin
              try {
                searchGscfResult = searchableService.search(flow.search_term)
                println "RESULT: " + searchGscfResult
              } catch (SearchEngineQueryParseException ex) {
                println ex
                return [parseException: true]
              }

              // Map non-study objects to Studies
              // ... todo when the plugin works and I can see the output

              // Search in the SAM module when a compound is entered
              // Todo: check whether the module is active and to be used
              // ...
              def listSamStudies = []
              def listGscfStudies = []
              def listStudies = []

              if ((flow.search_sa_compounds) && (flow.search_sa_compounds.size() > 0)) {
                def resultSAM = [:]
                resultSAM = this.searchSAM(flow.search_sa_compounds, flow.search_sa_operators, flow.search_sa_values)
                println "Sam result: " + resultSAM
                listSamStudies = resultSAM.get('studies')
              }

             for (i in searchGscfResult.results) {
               //def x = i.id
               def objStudy = Study.get(i.id)
               println objStudy
               listGscfStudies.add(objStudy.id)
             }


             println "GSCF studies: " + listGscfStudies
             println "Sam studies " + listSamStudies
                                         
             // Merge the results of all searches
             if (listSamStudies.size() > 0) {
               listStudies = listGscfStudies.intersect(listSamStudies)
               println "Combined: " + listStudies
             } else {
               if (!flow.search_sa_compounds) {
                listStudies = listGscfStudies
               } else {
                listStudies = []
               }
             }

             def listObjStudies = []
             for (i in listStudies) {
               def objStudy = Study.get(i)
               listObjStudies.add(objStudy)
             }

             // Save the results in the flow
             flow.listStudies = listObjStudies
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
              flow.search_sa_compounds    = []
              flow.search_sa_operators    = []
              flow.search_sa_values       = []
              flow.search_tt_genepaths    = null
              flow.search_tt_regulations  = null
              println "Resetting query flow"
            }.to "query"

            on("search").to "searching"
            on("refresh").to "results"
        }

    }

  
   static Map searchSAM (List compounds, List operators, List values) {
     if (compounds.size() == 1) {
       println "Single SAM call"
       def mapSamResult = [:]

       //def listAssays = [3, 1]
       //mapSamResult.put("assays", listAssays)
       //println "CommMngr result: " + mapSamResult

       CommunicationManager.addRestWrapper( 'http://localhost:8182/sam/rest', 'getQueryResult', ['query'] )
       mapSamResult = CommunicationManager.getQueryResult( compounds.get(0) )
       println "SAM REST query: " + compounds.get(0)
       println "SAM REST result: " + mapSamResult

       // mapSamResult = CommunicationManager.getQueryResult(compounds.get(0), operators.get(0), values.get(0))

       //objAssay = objAssay.get(i)
       //println "Assay: " + objAssay

       /*
       for (i in mapSamResult.assays) {
         //def listStudies = Study.findAll("from Study as s where s.assays.id = " + i)
         def listStudies = Study.findAll("from Study as s where exists (from Assay as a where a.id = s.assays and a.id = ${i})")
         println "Studies found: " + listStudies
       }
       */

       def listStudies = []

       for (i in mapSamResult.assays) {
         def objAssay = Assay.get(i)
         listStudies.add(objAssay.parent.id)
       }

       mapSamResult.put("studies", listStudies)

       return mapSamResult

     } else {
       println "Multiple SAM calls"
       def tmpSamResult = [:]
       def mapSamResult = [assays:[]]
       def i = 0

       compounds.each { compound ->
         println "SAM Search with " + compound
         CommunicationManager.addRestWrapper( 'http://localhost:8182/sam/rest', 'getQueryResult', ['query'] )
         tmpSamResult = CommunicationManager.getQueryResult(compound)
         println "tmpsamres: " + tmpSamResult

         if (i == 0) {
           mapSamResult.assays = tmpSamResult.assays
         } else {
           if (mapSamResult.assays) {
             mapSamResult.assays = mapSamResult.assays.intersect(tmpSamResult.assays)
           }
         }

         i++
       }

       def listStudies = []

       for (j in mapSamResult.assays) {
         def objAssay = Assay.get(j)
         listStudies.add(objAssay.parent.id)
       }

       mapSamResult.put("studies", listStudies)

       return mapSamResult
     }

   }



   static List merge (List list1, List list2) {

     def resultList = []
     resultList = list1.intersect(list2)

     return resultList
   }

}
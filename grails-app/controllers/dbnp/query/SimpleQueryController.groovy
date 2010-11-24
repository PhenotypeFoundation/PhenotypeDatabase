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
              flow.operators              = ['>', '=', '<']

              if (!flow.search_sa_compounds) {
                flow.showFirstRowCompounds  = true
              } else {
                flow.showFirstRowCompounds  = false
              }

              flow.species = Term.findAll()
              flow.page = 1
            }

            on("search") {
              if (!params.search_term.trim()) {
                return [:]
              }
            }.to "searching"

            on("refresh").to "query"
		}


        // Searching for results
        searching {
           action {
              def searchResult
              def searchGscfResult
              def searchSamResult   = []

              // Map GSCF parameters
              flow.search_term            = params.search_term        // String

              // Map SAM parameters
              if (params.sa_compound instanceof String) {
                flow.search_sa_compounds = []
                flow.search_sa_operators = []
                flow.search_sa_values    = []

                if (params.sa_compound) { 
                  flow.search_sa_compounds.add(params.sa_compound)
                  flow.search_sa_operators.add(params.sa_operator)
                  flow.search_sa_values.add(params.sa_value)
                }
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
              def listSamStudies = []
              def listGscfStudies = []
              def listStudies = []

              if ((flow.search_sa_compounds) && (flow.search_sa_compounds.size() > 0)) {
                def resultSAM = [:]
                resultSAM = this.searchSAM(flow.search_sa_compounds, flow.search_sa_operators, flow.search_sa_values)
                listSamStudies = resultSAM.get('studies')
              }

             for (i in searchGscfResult.results) {
               def objStudy = Study.get(i.id)
               listGscfStudies.add(objStudy.id)
             }


                                         
             // Merge the results of all searches
             if (listSamStudies.size() > 0) {
               listStudies = listGscfStudies.intersect(listSamStudies)
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

           }

          on("error").to "query"
          on("success").to "results"
        }


        // Render result page including search options
        results {
            render(view: "/simpleQuery/mainPage")

            onRender {
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
            }.to "query"

            on("search").to "searching"
            on("refresh").to "results"
        }

    }

  
   static Map searchSAM (List compounds, List operators, List values) {


     if (compounds.size() == 1) {
       def tmpResult = CommunicationManager.getQueryResult( compounds.get(0) )
       def studies = tmpResult.studiesIds.collect{ Study.findByCode(it) }
       def assays  = tmpResult.assays.collect { [it, Assay.findByExternalAssayID( it.externalAssayID ) ] } 
	   def mapSamResult = [studies:studies, assays:assays] 


       def listStudies = []

       for (i in mapSamResult.assays) {
         def objAssay = Assay.get(i)
         listStudies.add(objAssay.parent.id)
       }

       mapSamResult.put("studies", listStudies)

       return mapSamResult

     } else {
       def tmpResult = CommunicationManager.getQueryResult( compounds.get(0) )
       def studies = tmpResult.studiesIds.collect{ Study.findByCode(it) }
	   def mapSamResult = [studies:studies, assays:[]] 

       def i = 0
       compounds.each { compound ->
         tmpSamResult = CommunicationManager.getQueryResult(compound)

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

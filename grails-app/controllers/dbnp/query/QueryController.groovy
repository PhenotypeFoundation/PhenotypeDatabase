package dbnp.query

import org.compass.core.engine.SearchEngineQueryParseException

/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * Basic web interface for Grails Searchable Plugin
 *
 * @author Adem and Jahn
 */
class QueryController {

    def searchableService

    def index = {
        redirect( action:'pages')
    }

        def results= {
          println('query controller')
        if (!params.q?.trim()) {
            return [:]
        }
        try {
            return [searchResult: searchableService.search(params.q, params)]
        } catch (SearchEngineQueryParseException ex) {
            return [parseException: true]
        }
    }

    def pagesFlow = {
        
        onStart {
                flow.page = 0
			flow.pages = [
                                [title: 'Query'],
				[title: 'Study'],
				[title: 'Samples'],
				[title: 'Biomarkers'],
				[title: 'Groups'],
				[title: 'Done']
			]
	}

	mainPage {

             render( view:'/query/mainPage')

             onRender {
                println "done randering index"
                flow.page=1
	     }
             on("next") {
                println "clicked next in sample"
	     } .to 'inputQuery'
	}	 

        inputQuery {
            render(view:'_inputQuery')
            onRender {
                flow.page=1
            }
        }

        study {
             render( view:'_study')
            onRender {
                flow.page=2
	     }
             on("next") {
                println "clicked next in sample"
	     } .to 'sample'
             on("previous"){}.to 'inputQuery'
	}

        sample {
             render( view:'_sample')
            onRender {
                flow.page=3
	     }
             on("next") {
                println "clicked next in sample"
	     } .to 'biomarker'
             on("previous"){}.to 'study'
	}


        biomarker {
             render( view:'_biomarker')
            onRender {
                flow.page=4
	     }
             on("next") {
                println "clicked next in sample"
	     } .to 'group'
             on("previous"){}.to 'sample'
	}

        group {
             render( view:'_group')
            onRender {
                flow.page=5
	     }
             on("next") {
                println "clicked next in sample"
	     } .to 'result'
             on("previous"){}.to 'biomarkers'
	}

        result {
             render( view:'_result')

            onRender {
                flow.page=6
	     }
             on("previous"){}.to 'group'
	}

    }

}

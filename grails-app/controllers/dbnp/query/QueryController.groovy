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

    def pagesFlow = {
        
        onStart {
                flow.page = 0
			flow.pages = [
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
	     }


             on("next") {
                println "clicked next in sample"
	     } .to 'study'

	}	 


        study {
             render( view:'_study')

             on("next") {
                println "clicked next in sample"
	     } .to 'sample'

	}



        sample {
             render( view:'_sample')

             on("next") {
                println "clicked next in sample"
	     } .to 'biomarker'

	}


        biomarker {
             render( view:'_biomarker')

             on("next") {
                println "clicked next in sample"
	     } .to 'group'

	}


        group {
             render( view:'_group')

             on("next") {
                println "clicked next in sample"
	     } .to 'result'

	}



        result {
             render( view:'_result')

	}













    }

}

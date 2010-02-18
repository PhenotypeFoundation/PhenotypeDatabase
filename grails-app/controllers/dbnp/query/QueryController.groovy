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

    def selectsample = {


            // produce error message here if studies don't contain samples!
	    // redirect back or use error


            println "in selectsample: "
	    params.each{println it}

            // prepare showing all studies selected in the previous view
            def selectedStudies = []
            def selectedStudyIds = params['selectedStudyIds']

            if(selectedStudyIds!=null)
	    {
                 def list = selectedStudyIds.findAll(/(\d)+/)
		 selectedStudies = list.collect{ dbnp.studycapturing.Study.get(it) }
            }
            else
            {
                 def tmpList = []
                 def studyList = dbnp.studycapturing.Study.list()
                 selectedStudyIds = []
                 params.each{ key,values->
                     if (values=="on")  tmpList.add(key)
	         }

                 for (i in studyList)
                     if (tmpList.contains(i.getId().toString()))
                     {
                         selectedStudyIds.add(i.id)
                         selectedStudies.add(i)
                     }
           }


        // subgroups
	// provide list of subgroups depending on the type of subgrouping
	// selected by the user
        def subgroups = []
        def submitButton = params["submit"]  // this button's value determines the kind of subgrouping

        switch(submitButton)
	{
	     case "Subject Groups":
	          render(params)
	          render("Subject Groups")
                  def studyGroups = []
		  if(selectedStudies!=null)
		  {
		     selectedStudies.each{ study ->
		         study.groups.each{ group -> studyGroups.add[group] }
		     }
		     println "study groups: "
		     studyGroups.each{x-> println x}
		  }

		  // testing:
		  // there is a lack of data in the mockup (subject groups are still missing)
		  // as long as there are no groups in the boot script,
		  // we use this
		  subgroups = studyGroups.size()<=0 ?
		       ["subject group 1","subject group 2"] : studyGroups

	          render(view:"selectsample",model:[selectedStudies:selectedStudies,selectedStudyIds:selectedStudyIds,subgroups:subgroups])
	          break

	     case "Event Groups":
                  def eventGroups = []
		  if(selectedStudies!=null)
		  {
		    selectedStudies.each{ study ->
			 println study.id
		         println study.samplingEvents.each{ eventGroups.add(it) }
		    }
		  }
		  subgroups=eventGroups
	          render(view:"selectsample",model:[selectedStudies:selectedStudies,selectedStudyIds:selectedStudyIds,subgroups:subgroups])
	          break

	     case "Starting Time Groups":

                  def timeGroups = []
		  if(selectedStudies!=null)
		  {
		    selectedStudies.each{ study ->
		         println study.samplingEvents.each{
                             def timeDiff = it.getPrettyDuration( study.startDate, it.startTime )
			     if( !timeGroups.contains(timeDiff) ) timeGroups.add(timeDiff)
			 }
		    }
		  }
		  subgroups=timeGroups
	          render("Starting Time Groups")
	          render(view:"selectsample",model:[selectedStudies:selectedStudies,selectedStudyIds:selectedStudyIds,subgroups:subgroups])
	          break

             case ">> Execute and continue with biomarker selection":
	          render("Starting Time Groups")
	          break
             case "<< Back to study selection":
	          break
	}
	render(view:"selectsample",model:[selectedStudies:selectedStudies,selectedStudyIds:selectedStudyIds,subgroups:subgroups])
    }



    /**
     * Index page with search form and results
     */
    def results = {
        if (!params.q?.trim()) {
            return [:]
        }
        try {
            return [searchResult: searchableService.search(params.q, params)]
        } catch (SearchEngineQueryParseException ex) {
            return [parseException: true]
        }
    }


    /**
     * Index page with search form and results
     */
    def index = {
    }


    /**
     * Perform a bulk index of every searchable object in the database
     */
    def indexAll = {
        Thread.start {
            searchableService.index()
        }
        render("bulk index started in a background thread")
    }

    /**
     * Perform a bulk index of every searchable object in the database
     */
    def unindexAll = {
        searchableService.unindex()
        render("unindexAll done")
    }


    def subjectGroups = { render ("hello") }


}
package dbnp.studycapturing
import dbnp.studycapturing.*
import grails.converters.*

/**
 * Wizard Controler
 *
 * The wizard controller handles the handeling of pages and data flow
 * through the study capturing wizard.
 *
 * @author  Jeroen Wesbeek
 * @since   20100107
 * @package studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class WizardController {
    /**
     * index method, redirect to the webflow
     * @void
     */
    def index = {
      /**
       * Do you believe it in your head?
       * I can go with the flow
       * Don't say it doesn't matter (with the flow) matter anymore
       * I can go with the flow (I can go)
       * Do you believe it in your head?
       */
      redirect(action:'pages')
    }

    /**
     * WebFlow definition
     * @see http://grails.org/WebFlow
     * @void
     */
    def pagesFlow = {
      // start the flow
      onStart {
        println "wizard started"
        
        // define flow variables
        flow.page = 0
        flow.pages = [
                [title:'Een'],
                [title:'Twoooo'],
                [title:'Trois']
        ]
        
      }
      onEnd {
	    println "wizard ended"
	  }

	  // render the main wizard page
	  mainPage {
	    onRender {
		  println "render main page"
          flow.page = 1
	    }
	    render(view:"/wizard/index")
        on("next") {
          println "next page!"
        }.to "pageTwo"
      }

      pageOne {
	    onRender {
		  println "render page one"
          flow.page = 1
	    }
	    render(view:"_one")
        on("next") {
          println "next page!"
        }.to "pageTwo"
      }

	  // render page two
	  pageTwo {
	    onRender {
		  println "render page two"
          flow.page = 2
	    }
        render(view:"_two")
        on("next") {
          println "next page!"
        }.to "pageThree"
        on("previous") {
          println "previous page!"
        }.to "pageOne"
	  }

	  // render page three
	  pageThree {
	    onRender {
		  println "render page three"
          flow.page = 3
	    }
        render(view:"_three")
        on("previous") {
          println "previous page!"
        }.to "pageTwo"
	  }
    }
}

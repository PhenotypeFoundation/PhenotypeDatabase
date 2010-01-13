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
      }
      onEnd {
	    println "wizard ended"
	  }

	  // render the main wizard page
	  mainPage {
	    onRender {
		  println "render main page"
	    }
	    render(view:"/wizard/index")
        on("next") {
          println "next page!"
        }.to "pageTwo"
      }

      pageOne {
	    onRender {
		  println "render page one"
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
	    }
        render(view:"_three")
        on("previous") {
          println "previous page!"
        }.to "pageTwo"
	  }
    }
}

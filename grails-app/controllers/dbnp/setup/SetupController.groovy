package dbnp.setup

/**
 * Setup / migration assistant
 *
 * @author Jeroen Wesbeek
 * @since 20101111
 * @package studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class SetupController {
	def index = {
		redirect(action: 'pages')
	}

	def pagesFlow = {
		// start the flow
		onStart {
			// define flow variables
			flow.page = 0
			flow.pages = [
				//[title: 'Templates'],			// templates
				[title: 'Start'],				// load or create a study
				[title: 'Study'],				// study
				[title: 'Subjects'],			// subjects
				[title: 'Events'],				// events and event grouping
				//[title: 'Event Groups'],		// groups
				[title: 'Samples'],				// samples
				[title: 'Assays'],				// assays
				//[title: 'Assay Groups'],		// assays
				[title: 'Confirmation'],		// confirmation page
				[title: 'Done']					// finish page
			]
			flow.jump = session.jump
			success()
		}

		// render the main wizard page which immediately
		// triggers the 'next' action (hence, the main
		// page dynamically renders the study template
		// and makes the flow jump to the start logic)
		mainPage {
			render(view: "/setup/index")
			onRender {
				flow.page = 1
				success()
			}
			on("next").to "start"
		}

		// create or modify a study
		start {
			render(view: "_start")
			onRender {
				flow.page = 1
				success()
			}
			on("next") {
				// clean the flow scope
				flow.remove('study')

				// set 'quicksave' variable to false
				flow.quickSave = false
			}.to "study"
			on("modify").to "modify"
			on("import").to "redirectToImport"
		}
	}
}
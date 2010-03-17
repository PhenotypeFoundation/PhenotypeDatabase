/**
 * Controller to handle client feedback
 *
 * @author  Jeroen Wesbeek
 * @since   20100317
 * @package main
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class FeedbackController {
    def index = {
    	render('todo: create a feedback overview here')
    }

	def add = {
		println params

		render('Thank you for your feedback!')
	}
}

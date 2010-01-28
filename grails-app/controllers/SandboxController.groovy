import dbnp.studycapturing.*

// The sandbox is meant for internal communication over code examples etc.

class SandboxController {

	def index = {

		// Get the example study in a lazy way
		def st = Study.get(1)
		def f = st.template.subjectFields

		// This is a way to iterate over the fields in your controller
		// And print them to the console
		// Most of the time, you would just iterate over them in the view using <g:each>
		// See also views/sandbox/index.gsp
		f.each {field ->
			println field.name + "(" + field.type + ")"
		}

		//Let's get a certain field for a certain subject
		def subject = Subject.findByName('A1')
		println st.template.getSubjectFieldType('Age')
		println subject.getFieldValue('Age')


		// Specify which variables we want to be available in the controller (implicit return statement)
		[fields: f, subjects: st.subjects]
}
}

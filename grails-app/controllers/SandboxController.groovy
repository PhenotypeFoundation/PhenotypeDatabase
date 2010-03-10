import dbnp.studycapturing.*
import dbnp.clinicaldata.ClinicalFloatData
import dbnp.clinicaldata.ClinicalMeasurement

// The sandbox is meant for internal communication over code examples etc.

class SandboxController {

	def clinicalDataLayerService

	def index = {

		// Get the example study in a lazy way
		def st = Study.get(1)
		def fieldsAll = st.giveSubjectTemplates().fields
        def f = fieldsAll[0]
        println fieldsAll.class
	    println f.class
      f.each {
        println "" + it + "-" + it.class
      }
      //println st.giveAllFields()

		// This is a way to iterate over the fields in your controller
		// And print them to the console
		// Most of the time, you would just iterate over them in the view using <g:each>
		// See also views/sandbox/index.gsp
		f.each { field ->
			println field.name + "(" + field.type + ")"
		}

		//Let's get a certain field for a certain subject
		/*def subject = st.subjects.get(1)

		if (subject) {
		println st.template.getSubjectFieldType('Age')
		println subject.getFieldValue('Genotype')
		subject.setFieldValue('Genotype','wildtype')
		println subject.getFieldValue('Genotype')
		subject.setFieldValue('name','hallo')
		println subject.name }*/


		// Demonstration of querying mechanism
		println "Features available for first assay of PPSH study: " + clinicalDataLayerService.getFeaturesQuantitative(Study.findByCode("PPSH").assays*.id[0])
		println "LDL feature value for two subjects: " + clinicalDataLayerService.getDataQuantitative('LDL',1,['A1_B','A3_B'] as String[])

		// Specify which variables we want to be available in the controller (implicit return statement)
		[fields: f, subjects: st.subjects, studyInstance: st]
	}
}

import dbnp.studycapturing.*

// The sandbox is meant for internal communication over code examples etc.
class SandboxController {

	def assayService

	def testAssay = {
		def assay = Assay.findByAssayUUID('7a9b81bb-4708-4879-a36f-dee335d2f054')
		//for 4 random samples: def samples = assay.samples.toList()[0..4]
		def samples = assay.samples.findAll { it.name.startsWith("02-126_BloodSamplingForChallenge_Group2_15w")}
		samples = samples.asList()
		[samples: samples, measurements: assayService.requestModuleMeasurements(assay,[],samples)]
	}

	def index = {
		println Study.list();
		
		// Get the example study in a lazy way
		def st = Study.get(1)
		println st.title
		println st.subjects
		def fieldsAll = st.giveSubjectTemplates().asList().first().fields
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
		//println "Features available for first assay of PPSH study: " + clinicalDataLayerService.getFeaturesQuantitative(Study.findByCode("PPSH").assays*.id[0])
		//println "LDL feature value for two subjects: " + clinicalDataLayerService.getDataQuantitative('LDL',1,['A1_B','A3_B'] as String[])

		// Specify which variables we want to be available in the controller (implicit return statement)
		[fields: f, subjects: st.subjects, studyInstance: st, subject: Subject.findByName('A1')]
	}

        def oauth = {            
            def secret = session.oauthToken.secret
            def response = oauthService.accessResource('http://www.myexperiment.org/whoami.xml', 'myExperiment',
                           [key: session.oauthToken.key, secret: session.oauthToken.secret], 'GET')
            
            render ("Calling whoami from myExperiment [key:" +  session.oauthToken.key + ", secret:"+ session.oauthToken.secret+"]")

            render ("Response: " + response)

        }


}

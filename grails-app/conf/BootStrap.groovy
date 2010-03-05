import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.util.GrailsUtil
import dbnp.studycapturing.*

import dbnp.data.Ontology
import dbnp.data.Term
import java.text.SimpleDateFormat

/**
 * Application Bootstrapper
 * @Author Jeroen Wesbeek
 * @Since 20091021
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class BootStrap {
	def init = {servletContext ->
		// define timezone
		System.setProperty('user.timezone', 'CET')	

		if (GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT) {
			printf("development bootstrapping....\n\n");

			// ontologies
			def speciesOntology = new Ontology(
				name: 'NCBI Taxonomy',
				shortName: 'Taxon',
				url: 'http://www.obofoundry.org/cgi-bin/detail.cgi?id=ncbi_taxonomy'
			).with { if (!validate()) { errors.each { println it} } else save()}

			def humanBodyOntology = new Ontology(
				name: 'Foundational Model of Anatomy',
				shortName: 'HumanBody',
				url: 'http://bioportal.bioontology.org/ontologies/39966'
			).with { if (!validate()) { errors.each { println it} } else save()}

			// terms
			def mouseTerm = new Term(
				name: 'Mus musculus',
				ontology: speciesOntology,
				accession: '10090'
			).with { if (!validate()) { errors.each { println it} } else save()}
			def humanTerm = new Term(
				name: 'Homo sapiens',
				ontology: speciesOntology,
				accession: '9606'
			).with { if (!validate()) { errors.each { println it} } else save()}

			def bloodTerm = new Term(
				name: 'Portion of blood',
				ontology: humanBodyOntology,
				accession: '9670'
			).with { if (!validate()) { errors.each { println it} } else save()}

			def madmaxOntology = new Ontology(
				name: 'Madmax ontology',
				shortName: 'MDMX',
				url: 'madmax.bioinformatics.nl'
			).with { if (!validate()) { errors.each { println it} } else save()}

			def treatmentTerm = new Term(
				name: 'ExperimentalProtocol',
				ontology: madmaxOntology,
				accession: 'P-MDMXGE-264'
			).with { if (!validate()) { errors.each { println it} } else save()}

			def dietProtocol = new Protocol(
				name: 'Diet treatment Protocol NuGO PPS3 leptin module',
				reference: treatmentTerm
			).with { if (!validate()) { errors.each { println it} } else save()}

			def boostProtocol = new Protocol(
				name: 'Boost treatment Protocol NuGO PPS3 leptin module',
				reference: treatmentTerm
			).with { if (!validate()) { errors.each { println it} } else save()}

			def fastingProtocol = new Protocol(
				name: 'Fasting',
				reference: treatmentTerm
			).with { if (!validate()) { errors.each { println it} } else save()}


                        // ParameterStringListItems
                        def oil10= new ParameterStringListItem(
			        name: '10% fat (palm oil)'
			).with { if (!validate()) { errors.each { println it} } else save()}
                        def oil45= new ParameterStringListItem(
			        name: '45% fat (palm oil)'
			).with { if (!validate()) { errors.each { println it} } else save()}
                        def vehicle= new ParameterStringListItem(
			        name: 'Vehicle'
			).with { if (!validate()) { errors.each { println it} } else save()}
                        def leptin= new ParameterStringListItem(
			        name: 'Leptin'
			).with { if (!validate()) { errors.each { println it} } else save()}


			dietProtocol
			.addToParameters(new ProtocolParameter(
				name: 'Diet',
				type: ProtocolParameterType.STRINGLIST,
				listEntries: [oil10,oil45]))
			.save()

			boostProtocol
			.addToParameters(new ProtocolParameter(
				name: 'Compound',
				type: ProtocolParameterType.STRINGLIST,
				listEntries: [vehicle,leptin]))
			.save()

			fastingProtocol
			.addToParameters(new ProtocolParameter(
				name: 'Fasting period',
				type: ProtocolParameterType.STRING))
			.save()


			// sampling event protocols

			def liverSamplingProtocol = new Protocol(
				name: 'Liver sampling'
			).with { if (!validate()) { errors.each { println it} } else save()}

			liverSamplingProtocol
			.addToParameters(new ProtocolParameter(
				name: 'Sample weight',
				unit: 'mg',
				type: ProtocolParameterType.FLOAT))
			.save()

			def bloodSamplingProtocol = new Protocol(
				name: 'Liver sampling'
			).with { if (!validate()) { errors.each { println it} } else save()}

			bloodSamplingProtocol
			.addToParameters(new ProtocolParameter(
				name: 'Sample volume',
				unit: 'ml',
				type: ProtocolParameterType.FLOAT))
			.save()

			// create system user

			/*def systemUser = userService.createUser(InstanceGenerator.user(
				username: 'system',
				pass: 'system',
				passConfirm: 'system',
				enabled: true
			))*/
			

			def genderField = new TemplateField(
				name: 'Gender',type: TemplateFieldType.STRINGLIST,
				listEntries: [new TemplateFieldListItem(name:'Male'),new TemplateFieldListItem(name: 'Female')])
			.with { if (!validate()) { errors.each { println it} } else save()}
						
			def ageField = new TemplateField(
				name: 'Age',type: TemplateFieldType.INTEGER)
			.with { if (!validate()) { errors.each { println it} } else save()}

			// Nutritional study template

			def studyTemplate = new Template(
				name: 'Nutritional study', entity: dbnp.studycapturing.Study
			).addToFields(new TemplateField(
				name: 'NuGO Code',type: TemplateFieldType.STRING)
			).with { if (!validate()) { errors.each { println it} } else save()}


			// Mouse template
			def mouseTemplate = new Template(
				name: 'Mouse', entity: dbnp.studycapturing.Subject
			).addToFields(new TemplateField(
				name: 'Genotype',type: TemplateFieldType.STRINGLIST,
				listEntries: [new TemplateFieldListItem(name:'C57/Bl6j'),new TemplateFieldListItem(name:'wild type')]))
			.addToFields(genderField)
			.addToFields(ageField)
			.addToFields(new TemplateField(
				name: 'Cage',type: TemplateFieldType.INTEGER))
			.addToFields(new TemplateField(
				name: 'Some double', type: TemplateFieldType.DOUBLE))
			.addToFields(new TemplateField(
				name: 'Some ontology', type: TemplateFieldType.ONTOLOGYTERM))
			.with { if (!validate()) { errors.each { println it} } else save()}

			// Human template
			def humanTemplate = new Template(
				name: 'Human', entity: dbnp.studycapturing.Subject)
			.addToFields(genderField)
			.addToFields(ageField)
			.addToFields(new TemplateField(
				name: 'DOB',type: TemplateFieldType.DATE))
			.addToFields(new TemplateField(
				name: 'Height',type: TemplateFieldType.DOUBLE))
			.addToFields(new TemplateField(
				name: 'Weight',type: TemplateFieldType.DOUBLE))
			.addToFields(new TemplateField(
				name: 'BMI',type: TemplateFieldType.DOUBLE))
			.with { if (!validate()) { errors.each { println it} } else save()}

			//events
			def eventDiet = new EventDescription(
				name: 'Diet treatment',
				description: 'Diet treatment (fat percentage)',
				classification: treatmentTerm,
				protocol: dietProtocol,
				isSamplingEvent: false
			).with { if (!validate()) { errors.each { println it} } else save()}

			def eventBoost = new EventDescription(
				name: 'Boost treatment',
				description: 'Boost treatment (leptin or vehicle)',
				classification: treatmentTerm,
				protocol: boostProtocol,
				isSamplingEvent: false
			).with { if (!validate()) { errors.each { println it} } else save()}

			def samplingEvent = new EventDescription(
				name: 'Liver extraction',
				description: 'Liver sampling for transcriptomics arrays',
				protocol: liverSamplingProtocol,
				isSamplingEvent: true
			).with { if (!validate()) { errors.each { println it} } else save()}

			def bloodSamplingEventDescription = new EventDescription(
				name: 'Blood extraction',
				description: 'Blood extraction targeted at lipid assays',
				protocol: bloodSamplingProtocol,
				isSamplingEvent: true
			).with { if (!validate()) { errors.each { println it} } else save()}


                        def fastingTreatment = new EventDescription(
				name: 'Fasting treatment',
				description: 'Fasting Protocol NuGO PPSH',
				protocol: fastingProtocol,
	                        isSamplingEvent: false
			).with { if (!validate()) { errors.each { println it} } else save()}

			println('Adding PPS3 study...')

			// studies
			def exampleStudy = new Study(
				template: studyTemplate,
				title:"NuGO PPS3 mouse study leptin module",
				code:"PPS3_leptin_module",
				researchQuestion:"Leptin etc.",
				description:"C57Bl/6 mice were fed a high fat (45 en%) or low fat (10 en%) diet after a four week run-in on low fat diet. After 1 week 10 mice that received a low fat diet were given an IP leptin challenge and 10 mice of the low-fat group received placebo injections. The same procedure was performed with mice that were fed the high-fat diet. After 4 weeks the procedure was repeated. In total 80 mice were culled.",
				ecCode:"2007117.c",
				startDate: Date.parse('yyyy-MM-dd','2007-12-11')
			).with { if (!validate()) { errors.each { println it} } else save()}

			def evLF = new Event(
				startTime: Date.parse('yyyy-MM-dd','2008-01-07'),
				endTime: Date.parse('yyyy-MM-dd','2008-01-14'),
				eventDescription: eventDiet,
				parameterStringValues: ['Diet':'10% fat (palm oil)']
			).with { if (!validate()) { errors.each { println it} } else save()}

			def evHF = new Event(
				startTime: Date.parse('yyyy-MM-dd','2008-01-07'),
				endTime: Date.parse('yyyy-MM-dd','2008-01-14'),
				eventDescription: eventDiet,
				parameterStringValues: ['Diet':'45% fat (palm oil)']
			).with { if (!validate()) { errors.each { println it} } else save()}

			def evBV = new Event(
				startTime: Date.parse('yyyy-MM-dd','2008-01-07'),
				endTime: Date.parse('yyyy-MM-dd','2008-01-14'),
				eventDescription: eventBoost,
				parameterStringValues: ['Compound':'Vehicle']
			).with { if (!validate()) { errors.each { println it} } else save()}

			def evBL = new Event(
				startTime: Date.parse('yyyy-MM-dd','2008-01-07'),
				endTime: Date.parse('yyyy-MM-dd','2008-01-14'),
				eventDescription: eventBoost,
				parameterStringValues: ['Compound':'Leptin']
			).with { if (!validate()) { errors.each { println it} } else save()}

			def evLF4 = new Event(
				startTime: Date.parse('yyyy-MM-dd','2008-01-07'),
				endTime: Date.parse('yyyy-MM-dd','2008-02-04'),
				eventDescription: eventDiet,
				parameterStringValues: ['Diet':'10% fat (palm oil)']
			).with { if (!validate()) { errors.each { println it} } else save()}

			def evHF4 = new Event(
				startTime: Date.parse('yyyy-MM-dd','2008-01-07'),
				endTime: Date.parse('yyyy-MM-dd','2008-02-04'),
				eventDescription: eventDiet,
				parameterStringValues: ['Diet':'45% fat (palm oil)']
			).with { if (!validate()) { errors.each { println it} } else save()}

			def evBV4 = new Event(
				startTime: Date.parse('yyyy-MM-dd','2008-01-07'),
				endTime: Date.parse('yyyy-MM-dd','2008-02-04'),
				eventDescription: eventBoost,
				parameterStringValues: ['Compound':'Vehicle']
			).with { if (!validate()) { errors.each { println it} } else save()}

			def evBL4 = new Event(
				startTime: Date.parse('yyyy-MM-dd','2008-01-07'),
				endTime: Date.parse('yyyy-MM-dd','2008-02-04'),
				eventDescription: eventBoost,
				parameterStringValues: ['Compound':'Leptin']
			).with { if (!validate()) { errors.each { println it} } else save()}

			def evS = new SamplingEvent(
					startTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					endTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					eventDescription: samplingEvent,
					parameterFloatValues: ['Sample weight':5F]
			).with { if (!validate()) { errors.each { println it} } else save()}

			def evS4 = new SamplingEvent(
					startTime: Date.parse('yyyy-MM-dd','2008-02-04'),
					endTime: Date.parse('yyyy-MM-dd','2008-02-04'),
					eventDescription: samplingEvent,
					parameterFloatValues: ['Sample weight':5F]
			).with { if (!validate()) { errors.each { println it} } else save()}

			// Add events to study
			exampleStudy
			.addToEvents(evLF)
			.addToEvents(evHF)
			.addToEvents(evBV)
			.addToEvents(evBL)
			.addToEvents(evLF4)
			.addToEvents(evHF4)
			.addToEvents(evBV4)
			.addToEvents(evBL4)
			.addToSamplingEvents(evS)
			.addToSamplingEvents(evS4)
			.save()

			def LFBV1 = new EventGroup(name:"10% fat + vehicle for 1 week")
			.addToEvents(evLF)
			.addToEvents(evBV)
			.with { if (!validate()) { errors.each { println it} } else save()}

			def LFBL1 = new EventGroup(name:"10% fat + leptin for 1 week")
			.addToEvents(evLF)
			.addToEvents(evBL)
			.with { if (!validate()) { errors.each { println it} } else save()}

			def HFBV1 = new EventGroup(name:"45% fat + vehicle for 1 week")
			.addToEvents(evHF)
			.addToEvents(evBV)
			.with { if (!validate()) { errors.each { println it} } else save()}

			def HFBL1 = new EventGroup(name:"45% fat + leptin for 1 week")
			.addToEvents(evHF)
			.addToEvents(evBL)
			.with { if (!validate()) { errors.each { println it} } else save()}

			def LFBV4 = new EventGroup(name:"10% fat + vehicle for 4 weeks")
			.addToEvents(evLF4)
			.addToEvents(evBV4)
			.with { if (!validate()) { errors.each { println it} } else save()}

			def LFBL4 = new EventGroup(name:"10% fat + leptin for 4 weeks")
			.addToEvents(evLF4)
			.addToEvents(evBL4)
			.with { if (!validate()) { errors.each { println it} } else save()}

			def HFBV4 = new EventGroup(name:"45% fat + vehicle for 4 weeks")
			.addToEvents(evHF4)
			.addToEvents(evBV4)
			.with { if (!validate()) { errors.each { println it} } else save()}

			def HFBL4 = new EventGroup(name:"45% fat + leptin for 4 weeks")
			.addToEvents(evHF4)
			.addToEvents(evBL4)
			.with { if (!validate()) { errors.each { println it} } else save()}



			def x=1
			80.times {
				def currentSubject = new Subject(
					name: "A" + x++,
					species: mouseTerm,
					template: mouseTemplate,
				)
				.setFieldValue("Gender", "Male")
				.setFieldValue("Genotype", "C57/Bl6j")
				.setFieldValue("Age", 17)
				.setFieldValue("Cage", (int)(x/2))
				.with { if (!validate()) { errors.each { println it} } else save()}

				exampleStudy.addToSubjects(currentSubject)
				.with { if (!validate()) { errors.each { println it} } else save()}

				// Add subject to appropriate EventGroup
				if (x > 70) { HFBL4.addToSubjects(currentSubject).save() }
				else if (x > 60) { HFBV4.addToSubjects(currentSubject).save() }
				else if (x > 50) { LFBL4.addToSubjects(currentSubject).save() }
				else if (x > 40) { LFBV4.addToSubjects(currentSubject).save() }
				else if (x > 30) { HFBL1.addToSubjects(currentSubject).save() }
				else if (x > 20) { HFBV1.addToSubjects(currentSubject).save() }
				else if (x > 10) { LFBL1.addToSubjects(currentSubject).save() }
				else             { LFBV1.addToSubjects(currentSubject).save() }

			}

			// Add EventGroups to study
			exampleStudy
			.addToEventGroups(LFBV1)
			.addToEventGroups(LFBL1)
			.addToEventGroups(HFBV1)
			.addToEventGroups(HFBL1)
			.addToEventGroups(LFBV4)
			.addToEventGroups(LFBL4)
			.addToEventGroups(HFBV4)
			.addToEventGroups(HFBL4)
			.save()

			println 'Adding PPSH study'

                        def humanStudy = new Study(
	                        template: studyTemplate,
				title:"NuGO PPS human study",
				code:"PPSH",
				researchQuestion:"How much are fasting plasma and urine metabolite levels affected by prolonged fasting ?",
				description:"Human study",
				ecCode:"unknown",
				startDate: Date.parse('yyyy-MM-dd','2009-01-01')
			).with { if (!validate()) { errors.each { println it} } else save()}

			def fastingEvent = new Event(
					startTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					endTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					eventDescription: fastingTreatment,
					parameterStringValues: ['Fasting period':'8h']);

			def bloodSamplingEvent = new SamplingEvent(
					startTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					endTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					eventDescription: bloodSamplingEventDescription,
					parameterFloatValues: ['Sample volume':4.5F]);

			def rootGroup = new EventGroup(name: 'Root group');
			rootGroup.addToEvents fastingEvent
			rootGroup.addToEvents bloodSamplingEvent
			rootGroup.save()

                        def y=1
			11.times {
				def currentSubject = new Subject(
					name: "" + y++,
					species: humanTerm,
					template: humanTemplate)
				.setFieldValue("Gender", (boolean)(x/2) ? "Male" : "Female")
				.setFieldValue("DOB", new java.text.SimpleDateFormat("dd-mm-yy").parse("01-02-19"+(10+(int)(Math.random()*80))))
				.setFieldValue("Age", 30)
				.setFieldValue("Height",Math.random()*2F)
				.setFieldValue("Weight",Math.random()*150F)
				.setFieldValue("BMI",20 + Math.random()*10F)
				.with { if (!validate()) { errors.each { println it} } else save()}

				def currentSample = new Sample(
					name: currentSubject.name + '_B',
					material: bloodTerm,
					parentSubject: currentSubject,
					parentEvent: bloodSamplingEvent);

				rootGroup.addToSubjects currentSubject
				rootGroup.save()

				humanStudy.addToSubjects(currentSubject)
				.addToSamples(currentSample)
				.addToEventGroups rootGroup
				.with { if (!validate()) { errors.each { println it} } else save()}
			}

			humanStudy.addToEventGroups(rootGroup).save()

//                        new Study(title:"example",code:"Excode",researchQuestion:"ExRquestion",description:"Exdescription",ecCode:"ExecCode",dateCreated:new Date(),lastUpdated:new Date(),startDate:new Date()).save()
//                        new Study(title:"testAgain",code:"testcode",researchQuestion:"testRquestion",description:"testdescription",ecCode:"testCode",dateCreated:new Date(),lastUpdated:new Date(),startDate:new Date()).save()
//                        new Study(title:"Exampletest",code:"Examplecode",researchQuestion:"ExampleRquestion",description:"Exampledescription",ecCode:"ExampleecCode",dateCreated:new Date(),lastUpdated:new Date(),startDate:new Date()).save()

			// Add clinical data

			def lipidAssay = new dbnp.clinicaldata.ClinicalAssay(
				name: 'Lipid profile',
				approved: true
			).with { if (!validate()) { errors.each { println it} } else save()}

			def ldlMeasurement = new dbnp.clinicaldata.ClinicalMeasurement(
				name: 'LDL',
				unit: 'mg/dL',
				type: dbnp.data.FeatureType.QUANTITATIVE,
				referenceValues: '100 mg/dL',
				detectableLimit: 250,
				isDrug: false, isIntake: true, inSerum: true
			).with { if (!validate()) { errors.each { println it} } else save()}

			def hdlMeasurement = new dbnp.clinicaldata.ClinicalMeasurement(
				name: 'HDL',
				unit: 'mg/dL',
				type: dbnp.data.FeatureType.QUANTITATIVE,
				referenceValues: '50 mg/dL',
				detectableLimit: 100,
				isDrug: false, isIntake: true, inSerum: true
			).with { if (!validate()) { errors.each { println it} } else save()}

			lipidAssay.addToMeasurements ldlMeasurement
			lipidAssay.addToMeasurements hdlMeasurement

			def lipidAssayInstance = new dbnp.clinicaldata.ClinicalAssayInstance(
				assay: lipidAssay
			).with { if (!validate()) { errors.each { println it} } else save()}

			humanStudy.samples*.each {
				new dbnp.clinicaldata.ClinicalFloatData(
					assay: lipidAssayInstance,
					measurement: ldlMeasurement,
					sample: it.name,
					value: Math.round(Math.random()*ldlMeasurement.detectableLimit)
				).with { if (!validate()) { errors.each { println it} } else save()}

				new dbnp.clinicaldata.ClinicalFloatData(
					assay: lipidAssayInstance,
					measurement: hdlMeasurement,
					sample: it.name,
					value: Math.round(Math.random()*hdlMeasurement.detectableLimit)
				).with { if (!validate()) { errors.each { println it} } else save()}
			}

			// Add assay to study capture module

			def clinicalModule = new AssayModule(
				name: 'Clinical data',
				type: AssayType.CLINICAL_DATA,
				platform: 'clinical measurements',
				url: 'http://localhost:8080/gscf'
			).with { if (!validate()) { errors.each { println it} } else save()}

			def lipidAssayRef = new Assay(
				name: 'Lipid profiling',
				module: clinicalModule,
				externalAssayId: lipidAssayInstance.id
			).with { if (!validate()) { errors.each { println it} } else save()}

			humanStudy.samples*.each {
				lipidAssayRef.addToSamples(it)
			}
			lipidAssayRef.save()

			humanStudy.addToAssays(lipidAssayRef);
			humanStudy.save()

		}
	}

	def destroy = {
	}
} 
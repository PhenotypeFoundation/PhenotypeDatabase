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

			def treatmentProtocol = new Protocol(
				name: 'MADMAX Experimental Protocol',
				reference: treatmentTerm
			).with { if (!validate()) { errors.each { println it} } else save()}

			def fastingProtocol = new Protocol(
				name: 'Fasting',
				reference: treatmentTerm
			).with { if (!validate()) { errors.each { println it} } else save()}

			treatmentProtocol
			.addToParameters(new ProtocolParameter(
				name: 'Diet',
				type: ProtocolParameterType.STRINGLIST,
				listEntries: ['10% fat (palm oil)','45% fat (palm oil)']))
			.addToParameters(new ProtocolParameter(
				name: 'Compound',
				type: ProtocolParameterType.STRINGLIST,
				listEntries: ['Vehicle','Leptin']))
			.addToParameters(new ProtocolParameter(
				name: 'Administration',
				type: ProtocolParameterType.STRING))
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
			/*
			def systemUser = userService.createUser(InstanceGenerator.user(
				username: 'system',
				pass: 'system',
				passConfirm: 'system',
				enabled: true
			))
			*/

			def genderField = new TemplateSubjectField(
				name: 'Gender',type: TemplateFieldType.STRINGLIST,
				listEntries: ['Male','Female'])
			.with { if (!validate()) { errors.each { println it} } else save()}
			def ageField = new TemplateSubjectField(
				name: 'Age',type: TemplateFieldType.INTEGER)
			.with { if (!validate()) { errors.each { println it} } else save()}

			// Mouse template
			def mouseTemplate = new Template(
				name: 'Mouse'
			).addToSubjectFields(new TemplateSubjectField(
				name: 'Genotype',type: TemplateFieldType.STRINGLIST,
				listEntries: ['C57/Bl6j','wild type']))
			.addToSubjectFields(genderField)
			.addToSubjectFields(ageField)
			.addToSubjectFields(new TemplateSubjectField(
				name: 'Cage',type: TemplateFieldType.INTEGER))
			.addToSubjectFields(new TemplateSubjectField(
				name: 'Some double', type: TemplateFieldType.DOUBLE))
			.addToSubjectFields(new TemplateSubjectField(
				name: 'Some ontology', type: TemplateFieldType.ONTOLOGYTERM))
			.with { if (!validate()) { errors.each { println it} } else save()}

			// Human template
			def humanTemplate = new Template(
				name: 'Human')
			.addToSubjectFields(genderField)
			.addToSubjectFields(ageField)
			.addToSubjectFields(new TemplateSubjectField(
				name: 'DOB',type: TemplateFieldType.DATE))
			.addToSubjectFields(new TemplateSubjectField(
				name: 'Height',type: TemplateFieldType.DOUBLE))
			.addToSubjectFields(new TemplateSubjectField(
				name: 'Weight',type: TemplateFieldType.DOUBLE))
			.addToSubjectFields(new TemplateSubjectField(
				name: 'BMI',type: TemplateFieldType.DOUBLE))
			.with { if (!validate()) { errors.each { println it} } else save()}

			//events
			def eventTreatment = new EventDescription(
				name: 'Treatment',
				description: 'Experimental Treatment Protocol NuGO PPS3 leptin module',
				classification: treatmentTerm,
				protocol: treatmentProtocol,
				isSamplingEvent: false
			).with { if (!validate()) { errors.each { println it} } else save()}

			def samplingEvent = new EventDescription(
				name: 'Liver extraction',
				description: 'Liver sampling for transcriptomics arrays',
				protocol: liverSamplingProtocol,
				isSamplingEvent: true
			).with { if (!validate()) { errors.each { println it} } else save()}

			def bloodSamplingEvent = new EventDescription(
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
				title:"NuGO PPS3 mouse study leptin module",
				code:"PPS3_leptin_module",
				researchQuestion:"Leptin etc.",
				description:"C57Bl/6 mice were fed a high fat (45 en%) or low fat (10 en%) diet after a four week run-in on low fat diet. After 1 week 10 mice that received a low fat diet were given an IP leptin challenge and 10 mice of the low-fat group received placebo injections. The same procedure was performed with mice that were fed the high-fat diet. After 4 weeks the procedure was repeated. In total 80 mice were culled.",
				ecCode:"2007117.c",
				startDate: Date.parse('yyyy-MM-dd','2007-12-11'),
				template: mouseTemplate
			).with { if (!validate()) { errors.each { println it} } else save()}

			def x=1
			12.times {
				def currentSubject = new Subject(
					name: "A" + x++,
					species: mouseTerm,
					template: mouseTemplate,
					templateStringFields: ["Genotype" : "C57/Bl6j", "Gender" : "Male"],
					templateIntegerFields: ["Age" : 17, "Cage" : (int)(x/2)]
				).with { if (!validate()) { errors.each { println it} } else save()}

				exampleStudy.addToSubjects(currentSubject)
				.addToEvents(new Event(
					subject: currentSubject,
					startTime: Date.parse('yyyy-MM-dd','2008-01-07'),
					endTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					eventDescription: eventTreatment,
					parameterStringValues: ['Diet':'10% fat (palm oil)','Compound':'Vehicle','Administration':'intraperitoneal injection'])
				)
				.addToSamplingEvents(new SamplingEvent(
					subject: currentSubject,
					startTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					endTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					eventDescription: samplingEvent,
					parameterFloatValues: ['Sample weight':5F])
				)
				.with { if (!validate()) { errors.each { println it} } else save()}
			}

			println 'Adding PPSH study'

            def humanStudy = new Study(
				title:"NuGO PPS human study",
				code:"PPSH",
				researchQuestion:"How much are fasting plasma and urine metabolite levels affected by prolonged fasting ?",
				description:"Human study",
				ecCode:"unknown",
				startDate: Date.parse('yyyy-MM-dd','2009-01-01'),
                                template: humanTemplate
			).with { if (!validate()) { errors.each { println it} } else save()}

                        def y=1
			11.times {
				def currentSubject = new Subject(
					name: "" + y++,
					species: humanTerm,
					template: humanTemplate,
					templateStringFields: [
						"Gender" : (boolean)(x/2) ? "Male" : "Female"
						],
					templateDateFields: [
						"DOB" : new java.text.SimpleDateFormat("dd-mm-yy").parse("01-02-19"+(10+(int)(Math.random()*80)))
					],
					templateIntegerFields: [
						"Age" : 30
					],
					templateDoubleFields: [
						"Height" : Math.random()*2F,
						"Weight" : Math.random()*150F,
						"BMI" : 20 + Math.random()*10F
					]
				).with { if (!validate()) { errors.each { println it} } else save()}

				humanStudy.addToSubjects(currentSubject)
				.addToEvents(new Event(
					subject: currentSubject,
					startTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					endTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					eventDescription: fastingTreatment,
					parameterStringValues: ['Fasting period':'8h'])
				)
				.addToSamplingEvents(new SamplingEvent(
					subject: currentSubject,
					startTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					endTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					eventDescription: bloodSamplingEvent,
					parameterFloatValues: ['Sample volume':4.5F])
					.addToSamples(new Sample(
						name: currentSubject.name + '_B',
						material: bloodTerm
				))
				).with { if (!validate()) { errors.each { println it} } else save()}
			}

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

			humanStudy.giveSamples()*.each {
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

			humanStudy.giveSamples()*.each {
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
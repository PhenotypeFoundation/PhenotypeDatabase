import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.util.GrailsUtil
import dbnp.studycapturing.*

import dbnp.data.Ontology
import dbnp.data.Term

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


			// added by Jahn for testing the event views
			def treatmentProtocol2 = new Protocol(
				name: 'MADMAX Experimental Protocol 2',
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


			// added by Jahn for testing the event views
			treatmentProtocol2
			.addToParameters(new ProtocolParameter(
				name: 'Diet',
				type: ProtocolParameterType.STRINGLIST,
				listEntries: ['99% fat (crude oil)','1% fat (palm oil)']))
			.addToParameters(new ProtocolParameter(
				name: 'Compound',
				type: ProtocolParameterType.STRINGLIST,
				listEntries: ['Vehicle','Leptin']))
			.addToParameters(new ProtocolParameter(
				name: 'Administration',
				type: ProtocolParameterType.STRING))
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

			// Mouse template
			def mouseTemplate = new Template(
				name: 'Mouse'
			).addToSubjectFields(new TemplateSubjectField(
				name: 'Genotype',type: TemplateFieldType.STRINGLIST,
				listEntries: ['C57/Bl6j','wild type']))
			.addToSubjectFields(new TemplateSubjectField(
				name: 'Gender',type: TemplateFieldType.STRINGLIST,
				listEntries: ['Male','Female']))
			.addToSubjectFields(new TemplateSubjectField(
				name: 'Age',type: TemplateFieldType.INTEGER))
			.addToSubjectFields(new TemplateSubjectField(
				name: 'Cage',type: TemplateFieldType.INTEGER))
			.with { if (!validate()) { errors.each { println it} } else save()}


			//events
			def eventTreatment = new EventDescription(
				name: 'Treatment',
				description: 'Experimental Treatment Protocol NuGO PPS3 leptin module',
				classification: treatmentTerm,
				protocol: treatmentProtocol
			).with { if (!validate()) { errors.each { println it} } else save()}

                        def eventTreatment2 = new EventDescription(
				name: 'Treatment2',
				description: 'Treatment Protocol NuGO PPS1',
				classification: treatmentTerm,
				protocol: treatmentProtocol2
			).with { if (!validate()) { errors.each { println it} } else save()}

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
				).with { if (!validate()) { errors.each { println it} } else save()}
			}


                        def secondStudy = new Study(
				title:"NuGO PPS1 mouse study leptin module",
				code:"PPS1",
				researchQuestion:"etc.",
				description:"C57Bl/6 mice were fed a high fat (45 en%) or low fat (10 en%) diet after a four week run-in on low fat diet. After 1 week 10 mice that received a low fat diet were given an IP leptin challenge and 10 mice of the low-fat group received placebo injections. The same procedure was performed with mice that were fed the high-fat diet. After 4 weeks the procedure was repeated. In total 80 mice were culled.",
				ecCode:"2007.c",
				startDate: Date.parse('yyyy-MM-dd','2007-12-11'),
                                template: mouseTemplate
			).with { if (!validate()) { errors.each { println it} } else save()}

                        def y=1
			5.times {
				def currentSubject = new Subject(
					name: "A" + y++,
					species: mouseTerm,
					template: mouseTemplate,
					templateStringFields: ["Genotype" : "C57/Bl6j", "Gender" : "Male"],
					templateIntegerFields: ["Age" : 17, "Cage" : (int)(y/2)]
				).with { if (!validate()) { errors.each { println it} } else save()}

				secondStudy.addToSubjects(currentSubject)
				.addToEvents(new SamplingEvent(
					subject: currentSubject,
					startTime: Date.parse('yyyy-MM-dd','2008-01-07'),
					endTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					eventDescription: eventTreatment2,
					parameterStringValues: ['Diet':'10% fat (palm oil)','Compound':'Vehicle','Administration':'intraperitoneal injection'])
				).with { if (!validate()) { errors.each { println it} } else save()}
			}

//                        new Study(title:"example",code:"Excode",researchQuestion:"ExRquestion",description:"Exdescription",ecCode:"ExecCode",dateCreated:new Date(),lastUpdated:new Date(),startDate:new Date()).save()
//                        new Study(title:"testAgain",code:"testcode",researchQuestion:"testRquestion",description:"testdescription",ecCode:"testCode",dateCreated:new Date(),lastUpdated:new Date(),startDate:new Date()).save()
//                        new Study(title:"Exampletest",code:"Examplecode",researchQuestion:"ExampleRquestion",description:"Exampledescription",ecCode:"ExampleecCode",dateCreated:new Date(),lastUpdated:new Date(),startDate:new Date()).save()
                }
	}

	def destroy = {
	}
} 
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

		if (GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT) {
			printf("development bootstrapping....\n\n");

			// ontologies
			def speciesOntology = new Ontology(
				name: 'NCBI Taxonomy',
				shortName: 'Taxon',
				url: 'http://www.obofoundry.org/cgi-bin/detail.cgi?id=ncbi_taxonomy'
			).save()

			// terms
			def mouseTerm = new Term(
				name: 'Mus musculus',
				ontology: speciesOntology,
				accession: '10090'
			).save()
			def humanTerm = new Term(
				name: 'Homo sapiens',
				ontology: speciesOntology,
				accession: '9606'
			).save()

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
				name: 'Genotype',type: TemplateFieldType.STRINGLIST))
			.addToSubjectFields(new TemplateSubjectField(
				name: 'Age',type: TemplateFieldType.NUMBER)
			).save()

			// studies
			def exampleStudy = new Study(
				title:"NuGO PPS3 mouse study leptin module",
				code:"PPS3_leptin_module",
				researchQuestion:"Leptin etc.",
				description:"C57Bl/6 mice were fed a high fat (45 en%) or low fat (10 en%) diet after a four week run-in on low fat diet. After 1 week 10 mice that received a low fat diet were given an IP leptin challenge and 10 mice of the low-fat group received placebo injections. The same procedure was performed with mice that were fed the high-fat diet. After 4 weeks the procedure was repeated. In total 80 mice were culled.",
				ecCode:"2007117.c",
				startDate: Date.parse('yyyy-MM-dd','2007-12-11'))
			def x=1
			12.times {
				exampleStudy.addToSubjects(new Subject(
					name: "A" + x++,
					species: mouseTerm,
					templateStringFields: ["Genotype" : "C57/Bl6j"],
					templateNumberFields: ["Age" : 17F]
				))}
			exampleStudy.save()

                        new Study(title:"example",code:"Excode",researchQuestion:"ExRquestion",description:"Exdescription",ecCode:"ExecCode",dateCreated:new Date(),lastUpdated:new Date(),startDate:new Date()).save()
                        new Study(title:"testAgain",code:"testcode",researchQuestion:"testRquestion",description:"testdescription",ecCode:"testCode",dateCreated:new Date(),lastUpdated:new Date(),startDate:new Date()).save()
                        new Study(title:"Exampletest",code:"Examplecode",researchQuestion:"ExampleRquestion",description:"Exampledescription",ecCode:"ExampleecCode",dateCreated:new Date(),lastUpdated:new Date(),startDate:new Date()).save()
                }
	}

	def destroy = {
	}
} 
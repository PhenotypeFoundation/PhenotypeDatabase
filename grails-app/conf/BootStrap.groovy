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
				name: 'Species',
				shortName: 'Species',
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

			// define template fields
			def genotypeTemplateField = new TemplateSubjectField(
				name: 'Genotype',
				type: TemplateFieldType.STRINGLIST
			).save()

			// Mouse template
			def mouseTemplate = new Template(
				name: 'Mouse'
			).addToSubjectFields(genotypeTemplateField).save()

			// studies
			new Study(title:"test",code:"code",researchQuestion:"Rquestion",description:"description",ecCode:"ecCode",dateCreated:new Date(),lastUpdated:new Date(),startDate:new Date()).save()
                        new Study(title:"example",code:"Excode",researchQuestion:"ExRquestion",description:"Exdescription",ecCode:"ExecCode",dateCreated:new Date(),lastUpdated:new Date(),startDate:new Date()).save()
                        new Study(title:"testAgain",code:"testcode",researchQuestion:"testRquestion",description:"testdescription",ecCode:"testCode",dateCreated:new Date(),lastUpdated:new Date(),startDate:new Date()).save()
                        new Study(title:"Exampletest",code:"Examplecode",researchQuestion:"ExampleRquestion",description:"Exampledescription",ecCode:"ExampleecCode",dateCreated:new Date(),lastUpdated:new Date(),startDate:new Date()).save()
                }
	}

	def destroy = {
	}
} 
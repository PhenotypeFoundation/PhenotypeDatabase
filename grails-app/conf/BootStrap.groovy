import dbnp.studycapturing.*

import dbnp.data.Ontology
import dbnp.data.Term
import org.codehaus.groovy.grails.commons.GrailsApplication

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

		// we could also check if we are in development by GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT
		if (Study.count() == 0) {
			println ".development bootstrapping...";

			// add NCBI species ontology
			println ".adding NCBI species ontology"
			def speciesOntology = new Ontology(
				name: 'NCBI organismal classification',
				description: 'A taxonomic classification of living organisms and associated artifacts for their controlled description within the context of databases.',
				url: 'http://www.ncbi.nlm.nih.gov/Taxonomy/taxonomyhome.html/',
				versionNumber: '1.2',
				ncboId: '1132',
				ncboVersionedId: '38802'
			).with { if (!validate()) { errors.each { println it} } else save()}

			println ".adding BRENDA source material ontology"
			def brendaOntology = new Ontology(
				name: 'BRENDA tissue / enzyme source',
				description: 'A structured controlled vocabulary for the source of an enzyme. It comprises terms for tissues, cell lines, cell types and cell cultures from uni- and multicellular organisms.',
				url: 'http://www.brenda-enzymes.info',
				versionNumber: '1.3',
				ncboId: '1005',
				ncboVersionedId: '40643'
			).with { if (!validate()) { errors.each { println it} } else save()}

			// add TERMS
			println ".adding mouse term"
			def mouseTerm = new Term(
				name: 'Mus musculus',
				ontology: speciesOntology,
				accession: '10090'
			).with { if (!validate()) { errors.each { println it} } else save()}
			println ".adding human term"
			def humanTerm = new Term(
				name: 'Homo sapiens',
				ontology: speciesOntology,
				accession: '9606'
			).with { if (!validate()) { errors.each { println it} } else save()}

                        // Create a few persons, roles and Affiliations
                        println ".adding persons, roles and affiliations"
                        def affiliation1 = new PersonAffiliation(
                            name: "Science Institute NYC"
                        ).save();
                        def affiliation2 = new PersonAffiliation(
                            name: "InfoStats GmbH, Hamburg"
                        ).save();
                        def role1 = new PersonRole(
                            name: "Principal Investigator"
                        ).save();
                        def role2 = new PersonRole(
                            name: "Statician"
                        ).save();

                        // Create 30 roles to test pagination
                        def roleCounter = 1;
                        30.times { new PersonRole( name: "Rol #${roleCounter++}" ).save() }

                        // Create persons
                        def person1 = new Person(
                            lastName: "Scientist",
                            firstName: "John",
                            gender: "Male",
                            initials: "J.R.",
                            email: "john@scienceinstitute.com",
                            phone: "1-555-3049",
                            address: "First street 2,NYC"
                        )
                        .addToAffiliations( affiliation1 )
                        .addToAffiliations( affiliation2 )
                        .save();
                        
                        def person2 = new Person(
                            lastName: "Statician",
                            firstName: "Jane",
                            gender: "Female",
                            initials: "W.J.",
                            email: "jane@statisticalcompany.de",
                            phone: "49-555-8291",
                            address: "Dritten strasse 38, Hamburg, Germany"
                        )
                        .addToAffiliations( affiliation2 )
                        .save();

                        // Create 30 persons to test pagination
                        def personCounter = 1;
                        30.times { new Person( firstName: "Person #${personCounter}", lastName: "Testperson", email: "email${personCounter++}@testdomain.com" ).save() }

 /*   COMMENTED OUT BECAUSE IT BREAKS EVERYTHING AFTER REFACTORING THE DATAMODEL

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

			def nciOntology = new Ontology(
				name: 'NCI Thesaurus',
				shortName: 'NCI',
				url: 'http://bioportal.bioontology.org/ontologies/42331'
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
			def arabTerm = new Term(
				name: 'Arabidopsis thaliana',
				ontology: speciesOntology,
				accession: '3702'
			).with { if (!validate()) { errors.each { println it} } else save()}
			
			def bloodTerm = new Term(
				name: 'Portion of blood',
				ontology: humanBodyOntology,
				accession: '9670'
			).with { if (!validate()) { errors.each { println it} } else save()}

			def c57bl6Term = new Term(
				name: 'C57BL/6 Mouse',
				ontology: nciOntology,
				accession: 'C14424'
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
				name: 'Blood sampling'
			).with { if (!validate()) { errors.each { println it} } else save()}

			bloodSamplingProtocol
			.addToParameters(new ProtocolParameter(
				name: 'Sample volume',
				unit: 'ml',
				type: ProtocolParameterType.FLOAT))
			.save()
 */
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
				name: 'Age (years)',type: TemplateFieldType.INTEGER,unit: 'years')
			.with { if (!validate()) { errors.each { println it} } else save()}

			def genotypeField = new TemplateField(
				name: 'Genotype', type: TemplateFieldType.ONTOLOGYTERM)
			.with { if (!validate()) { errors.each { println it} } else save()}

			def genotypeTypeField = new TemplateField(
				name: 'Genotype type',type: TemplateFieldType.STRINGLIST,
				listEntries: [new TemplateFieldListItem(name:'wildtype'),
					new TemplateFieldListItem(name:'transgenic'),
					new TemplateFieldListItem(name:'knock-out'),
					new TemplateFieldListItem(name:'knock-in')])
			.with { if (!validate()) { errors.each { println it} } else save()}


			// Nutritional study template

			println ".adding academic study template..."
			def studyTemplate = new Template(
				name: 'Academic study', entity: dbnp.studycapturing.Study)
				.addToFields(new TemplateField(name: 'Description',type: TemplateFieldType.TEXT))
				.addToFields(new TemplateField(name: 'Study code',type: TemplateFieldType.STRING, preferredIdentifier:true))
				.addToFields(new TemplateField(name: 'Objectives',type: TemplateFieldType.TEXT))
				.addToFields(new TemplateField(name: 'Consortium',type: TemplateFieldType.STRING))
				.addToFields(new TemplateField(name: 'Cohort name',type: TemplateFieldType.STRING))
				.addToFields(new TemplateField(name: 'Time zone',type: TemplateFieldType.STRING))
				.addToFields(new TemplateField(name: 'Responsible scientist',type: TemplateFieldType.STRING))
				.addToFields(new TemplateField(name: 'Lab id',type: TemplateFieldType.STRING))
				.addToFields(new TemplateField(name: 'Institute',type: TemplateFieldType.STRING))
			.with { if (!validate()) { errors.each { println it} } else save()}

			// Mouse template
			println ".adding mouse subject template..."
			def mouseTemplate = new Template(
				name: 'Mouse', entity: dbnp.studycapturing.Subject)
			.addToFields(new TemplateField(
				name: 'Strain', type: TemplateFieldType.ONTOLOGYTERM))
			.addToFields(genotypeField)
			.addToFields(genotypeTypeField)
			.addToFields(genderField)
			.addToFields(new TemplateField(
				name: 'Age (weeks)', type: TemplateFieldType.INTEGER, unit: 'weeks'))
			.addToFields(new TemplateField(
				name: 'Age type',type: TemplateFieldType.STRINGLIST,
				listEntries: [new TemplateFieldListItem(name:'postnatal'),new TemplateFieldListItem(name:'embryonal')]))
			.addToFields(new TemplateField(
				name: 'Cage',type: TemplateFieldType.STRING))
			.addToFields(new TemplateField(
				name: '#Mice in cage',type: TemplateFieldType.INTEGER))
			.addToFields(new TemplateField(
				name: 'Litter size',type: TemplateFieldType.INTEGER))
			.addToFields(new TemplateField(
				name: 'Weight (g)', type: TemplateFieldType.DOUBLE, unit: 'gram'))
			.with { if (!validate()) { errors.each { println it} } else save()}

			// Human template
			println ".adding human subject template..."
			def humanTemplate = new Template(
				name: 'Human', entity: dbnp.studycapturing.Subject)
			.addToFields(genderField)
			.addToFields(ageField)
			.addToFields(new TemplateField(
				name: 'DOB',type: TemplateFieldType.DATE))
			.addToFields(new TemplateField(
				name: 'Height',type: TemplateFieldType.DOUBLE, unit: 'm'))
			.addToFields(new TemplateField(
				name: 'Weight (kg)',type: TemplateFieldType.DOUBLE, unit: 'kg'))
			.addToFields(new TemplateField(
				name: 'BMI',type: TemplateFieldType.DOUBLE, unit: 'kg/m2'))
			.addToFields(new TemplateField(
				name: 'Race',type: TemplateFieldType.STRING))
			.addToFields(new TemplateField(
				name: 'Waist circumference',type: TemplateFieldType.FLOAT, unit: 'cm'))
			.addToFields(new TemplateField(
				name: 'Hip circumference',type: TemplateFieldType.FLOAT, unit: 'cm'))
			.addToFields(new TemplateField(
				name: 'Systolic blood pressure',type: TemplateFieldType.FLOAT, unit: 'mmHg'))
			.addToFields(new TemplateField(
				name: 'Diastolic blood pressure',type: TemplateFieldType.FLOAT, unit: 'mmHg'))
			.addToFields(new TemplateField(
				name: 'Heart rate',type: TemplateFieldType.FLOAT, unit: 'beats/min'))
			.addToFields(new TemplateField(
				name: 'Run-in-food',type: TemplateFieldType.TEXT))
			.with { if (!validate()) { errors.each { println it} } else save()}


			def sampleDescriptionField = new TemplateField(
				name: 'Description',type: TemplateFieldType.TEXT)
			.with { if (!validate()) { errors.each { println it} } else save()}
			def sampleTypeField = new TemplateField(
				name: 'SampleType',type: TemplateFieldType.STRING)
			.with { if (!validate()) { errors.each { println it} } else save()}
			def sampleProtocolField = new TemplateField(
				name: 'SampleProtocol',type: TemplateFieldType.STRING)
			.with { if (!validate()) { errors.each { println it} } else save()}
			def sampleVialTextField = new TemplateField(
				name: 'Text on vial',type: TemplateFieldType.STRING)
			.with { if (!validate()) { errors.each { println it} } else save()}

			// Human sample template
			println ".adding human sample template..."
			def humanSampleTemplate = new Template(
				name: 'Human tissue sample', entity: dbnp.studycapturing.Sample)
			.addToFields(sampleDescriptionField)
			.addToFields(sampleTypeField)
			.addToFields(sampleProtocolField)
			.addToFields(sampleVialTextField)
			.with { if (!validate()) { errors.each { println it} } else save()}

			//Plant template
			println ".adding plant template..."
			def plantTemplate = new Template(
				name: 'Plant template', entity: dbnp.studycapturing.Subject)
			.addToFields(new TemplateField(
				name: 'Variety', type: TemplateFieldType.STRING))
			.addToFields(new TemplateField(
				name: 'Ecotype', type: TemplateFieldType.STRING))
			.addToFields(genotypeField)
			.addToFields(genotypeTypeField)
			.addToFields(new TemplateField(
				name: 'Growth location', type: TemplateFieldType.STRINGLIST,
				listEntries: [new TemplateFieldListItem(name:'Greenhouse'),new TemplateFieldListItem(name: 'Field')]))
			.addToFields(new TemplateField(
				name: 'Room', type: TemplateFieldType.STRING,
				comment: 'Chamber number in case of Greenhouse'))
			.addToFields(new TemplateField(
				name: 'Position X', type: TemplateFieldType.FLOAT))
			.addToFields(new TemplateField(
				name: 'Position Y', type: TemplateFieldType.FLOAT))
			.addToFields(new TemplateField(
				name: 'Block', type: TemplateFieldType.STRING))
			.addToFields(new TemplateField(
				name: 'Temperature at day', type: TemplateFieldType.FLOAT))
			.addToFields(new TemplateField(
				name: 'Temperature at night', type: TemplateFieldType.FLOAT))
			.addToFields(new TemplateField(
				name: 'Photo period', type: TemplateFieldType.STRING))
			.addToFields(new TemplateField(
				name: 'Light intensity', type: TemplateFieldType.STRING))
			.addToFields(new TemplateField(
				name: 'Start date', type: TemplateFieldType.DATE))
			.addToFields(new TemplateField(
				name: 'Harvest date', type: TemplateFieldType.DATE))
			.addToFields(new TemplateField(
				name: 'Growth type', type: TemplateFieldType.STRINGLIST,
				listEntries: [new TemplateFieldListItem(name:'Standard'),new TemplateFieldListItem(name: 'Experimental')]))
			.addToFields(new TemplateField(
				name: 'Growth protocol', type: TemplateFieldType.TEXT))
			.addToFields(new TemplateField(
				name: 'Harvest delay', type: TemplateFieldType.TEXT))
			.with { if (!validate()) { errors.each { println it} } else save()}

			println ".adding plant sample template..."
			def plantSampleTemplate = new Template(
				name: 'Plant sample', entity: dbnp.studycapturing.Sample)
			.addToFields(sampleDescriptionField)
			.addToFields(sampleTypeField)
			.addToFields(sampleProtocolField)
			.addToFields(sampleVialTextField)
			.with { if (!validate()) { errors.each { println it} } else save()}


			// Event templates
			def dietTreatmentTemplate = new Template(
				name: 'Diet treatment', entity: dbnp.studycapturing.Event)
			.addToFields(sampleDescriptionField)
			.addToFields(new TemplateField(
				name: 'Diet', type: TemplateFieldType.STRINGLIST,
				listEntries: [new TemplateFieldListItem(name:'10% fat (palm oil)'),new TemplateFieldListItem(name: '45% fat (palm oil)')]))
			.with { if (!validate()) { errors.each { println it} } else save()}

			def boostTreatmentTemplate = new Template(
				name: 'Boost treatment', entity: dbnp.studycapturing.Event)
			.addToFields(sampleDescriptionField)
			.addToFields(new TemplateField(
				name: 'Compound', type: TemplateFieldType.STRING,
				listEntries: [new TemplateFieldListItem(name:'Vehicle'),new TemplateFieldListItem(name: 'Leptin')]))
			.with { if (!validate()) { errors.each { println it} } else save()}

			/*
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
                        */
			// studies
			if (!(grails.util.GrailsUtil.environment == GrailsApplication.ENV_TEST)) {
				println ".adding NuGO PPS3 leptin example study..."
				def exampleStudy = new Study(
					template: studyTemplate,
					title:"NuGO PPS3 mouse study leptin module",
					code:"PPS3_leptin_module",
					researchQuestion:"Leptin etc.",
					ecCode:"2007117.c",
					startDate: Date.parse('yyyy-MM-dd','2007-12-11')
				)
				.with { if (!validate()) { errors.each { println it} } else save()}

				exampleStudy.setFieldValue( 'Description', "C57Bl/6 mice were fed a high fat (45 en%) or low fat (10 en%) diet after a four week run-in on low fat diet. After 1 week 10 mice that received a low fat diet were given an IP leptin challenge and 10 mice of the low-fat group received placebo injections. The same procedure was performed with mice that were fed the high-fat diet. After 4 weeks the procedure was repeated. In total 80 mice were culled." )
				exampleStudy.save()

				println ".adding NuGO PPSH example study..."
				def exampleHumanStudy = new Study(
					template: studyTemplate,
					title:"Human example template",
					code:"Human example code",
					researchQuestion:"Leptin etc.",
					ecCode:"2007117.c",
					startDate: Date.parse('yyyy-MM-dd','2007-12-11')
				)
				.with { if (!validate()) { errors.each { println it} } else save()}

				exampleHumanStudy.setFieldValue( 'Description', "C57Bl/6 mice were fed a high fat (45 en%) or low fat (10 en%) diet after a four week run-in on low fat diet. After 1 week 10 mice that received a low fat diet were given an IP leptin challenge and 10 mice of the low-fat group received placebo injections. The same procedure was performed with mice that were fed the high-fat diet. After 4 weeks the procedure was repeated. In total 80 mice were culled." )
				exampleHumanStudy.save()

				def evLF = new Event(
					startTime: Date.parse('yyyy-MM-dd','2008-01-07'),
					endTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					template: dietTreatmentTemplate
				)
				.with { if (!validate()) { errors.each { println it} } else save()}
				evLF.setFieldValue( 'Diet','10% fat (palm oil)' )
				evLF.save(flush:true)

				def evHF = new Event(
					startTime: Date.parse('yyyy-MM-dd','2008-01-07'),
					endTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					template: dietTreatmentTemplate
				)
				.setFieldValue( 'Diet','45% fat (palm oil)' )
				.with { if (!validate()) { errors.each { println it} } else save()}

				def evBV = new Event(
					startTime: Date.parse('yyyy-MM-dd','2008-01-07'),
					endTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					template: boostTreatmentTemplate
				)
				.setFieldValue( 'Compound','Vehicle' )
				.with { if (!validate()) { errors.each { println it} } else save()}

				def evBL = new Event(
					startTime: Date.parse('yyyy-MM-dd','2008-01-07'),
					endTime: Date.parse('yyyy-MM-dd','2008-01-14'),
					template: boostTreatmentTemplate
				)
				.setFieldValue( 'Compound','Leptin' )
				.with { if (!validate()) { errors.each { println it} } else save()}

				/*
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
				*/

				// Add events to study
				exampleStudy
				.addToEvents(evLF)
				.addToEvents(evHF)
				.addToEvents(evBV)
				.addToEvents(evBL)
				/*
				.addToEvents(evLF4)
				.addToEvents(evHF4)
				.addToEvents(evBV4)
				.addToEvents(evBL4)
				.addToSamplingEvents(evS)
				.addToSamplingEvents(evS4)
				*/
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

				/*
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
				*/

		    // Add subjects and samples and compose EventGroups

				def x=1
				40.times {
					def currentSubject = new Subject(
						name: "A" + x++,
						species: mouseTerm,
						template: mouseTemplate,
					)
					.setFieldValue("Gender", "Male")
					//.setFieldValue("Genotype", c57bl6Term)
					.setFieldValue("Age (weeks)", 17)
					.setFieldValue("Cage", "" + (int)(x/2))
					.with { if (!validate()) { errors.each { println it} } else save(flush:true)}

					exampleStudy.addToSubjects(currentSubject)
					.with { if (!validate()) { errors.each { println it} } else save()}

					// Add subject to appropriate EventGroup
					/*
					if (x > 70) { HFBL4.addToSubjects(currentSubject).save() }
					else if (x > 60) { HFBV4.addToSubjects(currentSubject).save() }
					else if (x > 50) { LFBL4.addToSubjects(currentSubject).save() }
					else if (x > 40) { LFBV4.addToSubjects(currentSubject).save() }
					else if (x > 30) { HFBL1.addToSubjects(currentSubject).save() }
					else if (x > 20) { HFBV1.addToSubjects(currentSubject).save() }
					else if (x > 10) { LFBL1.addToSubjects(currentSubject).save() }
					else             { LFBV1.addToSubjects(currentSubject).save() }
					*/

					if (x > 30) { HFBL1.addToSubjects(currentSubject).save() }
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
				//.addToEventGroups(LFBV4)
				//.addToEventGroups(LFBL4)
				//.addToEventGroups(HFBV4)
				//.addToEventGroups(HFBL4)

				// Add persons to study
				def studyperson1 = new StudyPerson( person: person1, role: role1 ).save();
				def studyperson2 = new StudyPerson( person: person2, role: role2 ).save();

				exampleStudy
				.addToPersons( studyperson1 )
				.addToPersons( studyperson2 )
				.save()


				/*
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

		    def y = 1
		    11.times {
		      def currentSubject = new Subject(
			      name: "" + y++,
			      species: humanTerm,
			      template: humanTemplate).setFieldValue("Gender", (boolean) (x / 2) ? "Male" : "Female").setFieldValue("DOB", new java.text.SimpleDateFormat("dd-mm-yy").parse("01-02-19" + (10 + (int) (Math.random() * 80)))).setFieldValue("Age (years)", 30).setFieldValue("Height", Math.random() * 2F).setFieldValue("Weight (kg)", Math.random() * 150F).setFieldValue("BMI", 20 + Math.random() * 10F).with { if (!validate()) { errors.each { println it} } else save()}

		      rootGroup.addToSubjects currentSubject
		      rootGroup.save()

		      def currentSample = new Sample(
			      name: currentSubject.name + '_B',
			      material: bloodTerm,
			      parentSubject: currentSubject,
			      parentEvent: bloodSamplingEvent);


		      humanStudy.addToSubjects(currentSubject).addToSamples(currentSample).with { if (!validate()) { errors.each { println it} } else save()}
		  }

		  humanStudy.addToEvents(fastingEvent)
		  humanStudy.addToSamplingEvents(bloodSamplingEvent)
		  humanStudy.addToEventGroups rootGroup
		  humanStudy.save()

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
				*/
			}
		}
	}

	def destroy = {
	}
} 

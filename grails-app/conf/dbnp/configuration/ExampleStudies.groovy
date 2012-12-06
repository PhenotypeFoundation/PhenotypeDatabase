package dbnp.configuration

/**
 * @Author kees
 * @Since Jun 25, 2010
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

import dbnp.studycapturing.*
import org.dbnp.gdt.*
import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.util.GrailsUtil
import dbnp.rest.common.CommunicationManager
import org.codehaus.groovy.grails.commons.*


class ExampleStudies {
	public static void addTestData() {
		// get configuration
		def config = ConfigurationHolder.config

		// Look up the used ontologies which should be in the database by now
		def speciesOntology				= Ontology.getOrCreateOntologyByNcboId(1132)
		def brendaOntology				= Ontology.getOrCreateOntologyByNcboId(1005)
		def nciOntology					= Ontology.getOrCreateOntologyByNcboId(1032)
		def chebiOntology				= Ontology.getOrCreateOntologyByNcboId(1007)

		// Add terms manually, to avoid having to do many HTTP requests to the BioPortal website
		def mouseTerm = Term.getOrCreateTerm('Mus musculus',speciesOntology,'10090')
		def humanTerm = Term.getOrCreateTerm('Homo sapiens',speciesOntology,'9606')
		def arabTerm = Term.getOrCreateTerm('Arabidopsis thaliana',speciesOntology,'3702')
		def tomatoTerm = Term.getOrCreateTerm('Solanum lycopersicum',speciesOntology,'4081')
		def potatoTerm = Term.getOrCreateTerm('Solanum tuberosum',speciesOntology,'0000')
		def bloodTerm = Term.getOrCreateTerm('blood plasma',brendaOntology,'BTO:0000131')
		def c57bl6Term = Term.getOrCreateTerm('C57BL/6 Mouse',nciOntology,'C14424')
		def glucoseTerm = Term.getOrCreateTerm('glucose',chebiOntology,'CHEBI:17234')

		// Add SAM assay reference
		def clinicalModule = new AssayModule(
			name: 'SAM module for clinical data',
			url: config.modules.sam.url.toString(),
			notify: true,
			openInFrame: false
		).save(failOnError:true)

		// Add metabolomics assay reference
		def metabolomicsModule = new AssayModule(
			name: 'Metabolomics module',
			url: config.modules.metabolomics.url.toString()
		).save(failOnError:true)

		// Add metabolomics assay reference
		def massSequencingModule = new AssayModule(
			name: 'Mass Sequencing module',
			url: config.modules.massSequencing.url.toString()
		).save(failOnError:true)
	}

	/**
	 * Add example studies. This function is meant to be called only in development mode
	 */
	public static void addExampleStudies(dbnp.authentication.SecUser owner, dbnp.authentication.SecUser otherUser) {
		// Grom a development message
		if (String.metaClass.getMetaMethod("grom")) "inserting initial studies".grom()

		// get configuration
		def config = ConfigurationHolder.config

		// Look up the used ontologies which should be in the database by now
		def speciesOntology				= Ontology.getOrCreateOntologyByNcboId(1132)
		def brendaOntology				= Ontology.getOrCreateOntologyByNcboId(1005)
		def nciOntology					= Ontology.getOrCreateOntologyByNcboId(1032)
		def chebiOntology				= Ontology.getOrCreateOntologyByNcboId(1007)

		// Look up the used templates which should also be in the database by now
		def studyTemplate				= Template.findByName("NMC Study")
		def mouseTemplate				= Template.findByName("Mouse")
		def humanTemplate				= Template.findByName("Human")
		def dietTreatmentTemplate		= Template.findByName("Diet treatment")
		def boostTreatmentTemplate		= Template.findByName("Compound challenge")
		def liverSamplingEventTemplate	= Template.findByName("Liver extraction")
		def fastingTreatmentTemplate	= Template.findByName("Fasting treatment")
		def bloodSamplingEventTemplate	= Template.findByName("Blood extraction")
		def humanTissueSampleTemplate	= Template.findByName("Human tissue sample")
		def humanBloodSampleTemplate	= Template.findByName("Human blood sample")
		def ccAssayTemplate				= Template.findByName("Clinical chemistry assay")
		def metAssayTemplate			= Template.findByName("Metabolomics assay")

		// Add terms manually, to avoid having to do many HTTP requests to the BioPortal website
		def mouseTerm = Term.getOrCreateTerm('Mus musculus',speciesOntology,'10090')
		def humanTerm = Term.getOrCreateTerm('Homo sapiens',speciesOntology,'9606')
		def arabTerm = Term.getOrCreateTerm('Arabidopsis thaliana',speciesOntology,'3702')
		def tomatoTerm = Term.getOrCreateTerm('Solanum lycopersicum',speciesOntology,'4081')
		def potatoTerm = Term.getOrCreateTerm('Solanum tuberosum',speciesOntology,'0000')
		def bloodTerm = Term.getOrCreateTerm('blood plasma',brendaOntology,'BTO:0000131')
		def c57bl6Term = Term.getOrCreateTerm('C57BL/6 Mouse',nciOntology,'C14424')
		def glucoseTerm = Term.getOrCreateTerm('glucose',chebiOntology,'CHEBI:17234')

		// Create a few persons, roles and Affiliations
		def affiliation1 = new PersonAffiliation(
			institute	: "Science Institute NYC",
			department	: "Department of Mathematics"
        ).save(failOnError:true)

		def affiliation2 = new PersonAffiliation(
			institute	: "InfoStats GmbH, Hamburg",
			department	: "Life Sciences"
        ).save(failOnError:true)

		def role1 = new PersonRole(
			name		: "Principal Investigator"
        ).save(failOnError:true)

		def role2 = new PersonRole(
			name		: "Statician"
        ).save(failOnError:true)

		// Create persons
		def person1 = new Person(
			lastName	: "Scientist",
			firstName	: "John",
			gender		: "Male",
			initials	: "J.R.",
			email		: "john@scienceinstitute.com",
			phone		: "1-555-3049",
			address		: "First street 2,NYC"
		).addToAffiliations(affiliation1).addToAffiliations(affiliation2).save(failOnError:true)

		def person2 = new Person(
			lastName	: "Statician",
			firstName	: "Jane",
			gender		: "Female",
			initials	: "W.J.",
			email		: "jane@statisticalcompany.de",
			phone		: "49-555-8291",
			address		: "Dritten strasse 38, Hamburg, Germany"
		).addToAffiliations(affiliation2).save(failOnError:true)

		// Create 30 persons to test pagination
		def personCounter = 1
		30.times {
			new Person(
				firstName	: "Person #${personCounter}",
				lastName	: "Testperson",
				email		: "email${personCounter++}@testdomain.com"
			).save(failOnError:true)
		}

		// Create a few publications
		def publication1 = new Publication(
			title		: "Postnatal development of hypothalamic leptin receptors",
			authorsList	: "Cottrell EC, Mercer JG, Ozanne SE.",
			pubMedID	: "20472140",
			comments	: "Not published yet",
			DOI			: "unknown"
		).save(failOnError:true)

		def publication2 = new Publication(
			title		: "Induction of regulatory T cells decreases adipose inflammation and alleviates insulin resistance in ob/ob mice",
			authorsList	: "Ilan Y, Maron R, Tukpah AM, Maioli TU, Murugaiyan G, Yang K, Wu HY, Weiner HL.",
			pubMedID	: "20445103",
			comments	: "",
			DOI			: ""
		).save(failOnError:true)

		// Add example mouse study
		def mouseStudy = new Study(
			template	: studyTemplate,
			title		: "NuGO PPS3 mouse study leptin module",
			description	: "C57Bl/6 mice were fed a high fat (45 en%) or low fat (10 en%) diet after a four week run-in on low fat diet.",
			code		: "PPS3_leptin_module",
			researchQuestion: "Leptin etc.",
			ecCode		: "2007117.c",
			startDate	: Date.parse('yyyy-MM-dd', '2008-01-02'),
			owner		: owner,
			readers		: [otherUser]
        ).save(failOnError:true)

		def evLF = new Event(
			startTime	: 3600,
			endTime		: 3600 + 7 * 24 * 3600,
			template	: dietTreatmentTemplate
		).setFieldValue('Diet', 'low fat')

		def evHF = new Event(
			startTime	: 3600,
			endTime		: 3600 + 7 * 24 * 3600,
			template	: dietTreatmentTemplate
		).setFieldValue('Diet', 'high fat')

		def evBV = new Event(
			startTime	: 3600,
			endTime		: 3600 + 7 * 24 * 3600,
			template	: boostTreatmentTemplate
		).setFieldValue('Control', 'true')

		def evBL = new Event(
			startTime	: 3600,
			endTime		: 3600 + 7 * 24 * 3600,
			template	: boostTreatmentTemplate
		).setFieldValue('Control', 'false')

		def evLF4 = new Event(
			startTime	: 3600,
			endTime		: 3600 + 4 * 7 * 24 * 3600,
			template	: dietTreatmentTemplate
		).setFieldValue('Diet', 'low fat')

		def evHF4 = new Event(
			startTime	: 3600,
			endTime		: 3600 + 4 * 7 * 24 * 3600,
			template	: dietTreatmentTemplate
		).setFieldValue('Diet', 'high fat')

		def evBV4 = new Event(
			startTime	: 3600,
			endTime		: 3600 + 4 * 7 * 24 * 3600,
			template	: boostTreatmentTemplate
		).setFieldValue('Control', 'true')

		def evBL4 = new Event(
			startTime	: 3600,
			endTime		: 3600 + 4 * 7 * 24 * 3600,
			template	: boostTreatmentTemplate
		).setFieldValue('Control', 'false')

		def evS = new SamplingEvent(
			startTime	: 3600 + 7 * 24 * 3600,
			template	: liverSamplingEventTemplate,
			sampleTemplate: humanTissueSampleTemplate).setFieldValue('Sample weight', 5F)

		def evS4 = new SamplingEvent(
			startTime	: 3600 + 4 * 7 * 24 * 3600,
			template	: liverSamplingEventTemplate,
			sampleTemplate: humanTissueSampleTemplate).setFieldValue('Sample weight', 5F)

		// Add events to study
		mouseStudy.addToEvents(evLF).addToEvents(evHF).addToEvents(evBV).addToEvents(evBL).addToEvents(evLF4).addToEvents(evHF4).addToEvents(evBV4).addToEvents(evBL4).addToSamplingEvents(evS).addToSamplingEvents(evS4).save(failOnError:true)

		// Extra check if the SamplingEvents are saved correctly
		evS.save(failOnError:true)
		evS4.save(failOnError:true)

		def LFBV1 = new EventGroup(name: "10% fat + vehicle for 1 week").addToEvents(evLF).addToEvents(evBV).addToSamplingEvents(evS)

		def LFBL1 = new EventGroup(name: "10% fat + leptin for 1 week").addToEvents(evLF).addToEvents(evBL).addToSamplingEvents(evS)

		def HFBV1 = new EventGroup(name: "45% fat + vehicle for 1 week").addToEvents(evHF).addToEvents(evBV).addToSamplingEvents(evS)

		def HFBL1 = new EventGroup(name: "45% fat + leptin for 1 week").addToEvents(evHF).addToEvents(evBL).addToSamplingEvents(evS)

		def LFBV4 = new EventGroup(name: "10% fat + vehicle for 4 weeks").addToEvents(evLF4).addToEvents(evBV4).addToSamplingEvents(evS4)

		def LFBL4 = new EventGroup(name: "10% fat + leptin for 4 weeks").addToEvents(evLF4).addToEvents(evBL4).addToSamplingEvents(evS4)

		def HFBV4 = new EventGroup(name: "45% fat + vehicle for 4 weeks").addToEvents(evHF4).addToEvents(evBV4).addToSamplingEvents(evS4)

		def HFBL4 = new EventGroup(name: "45% fat + leptin for 4 weeks").addToEvents(evHF4).addToEvents(evBL4).addToSamplingEvents(evS4)

		// Add subjects and samples and compose EventGroups
		def x = 1
		80.times {
			def currentSubject = new Subject(
				name: "A" + x++,
				species: mouseTerm,
				template: mouseTemplate,
			).setFieldValue("Gender", "Male").setFieldValue("Genotype", c57bl6Term).setFieldValue("Age", 17).setFieldValue("Cage", "" + (int) (x / 2))

			// We have to save the subject first, otherwise the parentEvent property of the sample cannot be set
			// (this is possibly a Grails or Hibernate bug)
			mouseStudy.addToSubjects(currentSubject)

            currentSubject.save(failOnError:true)

			// Add subject to appropriate EventGroup
            def tmpEventGroup
			if (x > 70) { tmpEventGroup = HFBL4 }
			else if (x > 60) { tmpEventGroup = HFBV4 }
			else if (x > 50) { tmpEventGroup = LFBL4 }
			else if (x > 40) { tmpEventGroup = LFBV4 }
			else if (x > 30) { tmpEventGroup = HFBL1 }
			else if (x > 20) { tmpEventGroup = HFBV1 }
			else if (x > 10) { tmpEventGroup = LFBL1 }
			else { tmpEventGroup = LFBV1 }

            tmpEventGroup.addToSubjects(currentSubject)

			// Create sample
			def currentSample = new Sample(
				name: currentSubject.name + '_B',
				material: bloodTerm,
				template: humanBloodSampleTemplate,
				parentSubject: currentSubject,
				parentEvent: x > 40 ? evS4 : evS,
                parentEventGroup: tmpEventGroup
			)

			mouseStudy.addToSamples(currentSample)
            currentSample.setFieldValue("Text on vial", "T" + (Math.random() * 100L))
            currentSample.save(failOnError:true)
        }

		// Add EventGroups to study
		mouseStudy.addToEventGroups(LFBV1).addToEventGroups(LFBL1).addToEventGroups(HFBV1).addToEventGroups(HFBL1).addToEventGroups(LFBV4).addToEventGroups(LFBL4).addToEventGroups(HFBV4).addToEventGroups(HFBL4).save(failOnError:true)

		// Add persons and publications to study
		def studyperson1 = new StudyPerson(person: person1, role: role1)
		def studyperson2 = new StudyPerson(person: person2, role: role2)

		mouseStudy.addToPersons(studyperson1).addToPersons(studyperson2).addToPublications(publication1).addToPublications(publication2).save(failOnError:true)

		def humanStudy = new Study(
			template		: studyTemplate,
			title			: "NuGO PPS human study",
			code			: "PPSH",
			researchQuestion: "How much are fasting plasma and urine metabolite levels affected by prolonged fasting ?",
			description		: "Human study performed at RRI; centres involved: RRI, IFR, TUM, Maastricht U.",
			ecCode			: "unknown",
			startDate		: Date.parse('yyyy-MM-dd', '2008-01-14'),
			owner			: owner
		).save(failOnError:true)

		humanStudy.addToWriters(otherUser)

		humanStudy.save(failOnError:true)

		def rootGroup = new EventGroup(name: 'Root group')

		def fastingEvent = new Event(
			startTime		: 3 * 24 * 3600 + 22 * 3600,
			endTime			: 3 * 24 * 3600 + 30 * 3600,
			template		: fastingTreatmentTemplate).setFieldValue('Fasting period', '8h')

		def bloodSamplingEventBefore = new SamplingEvent(
			startTime		: 0,
			template		: bloodSamplingEventTemplate,
			sampleTemplate	: humanBloodSampleTemplate).setFieldValue('Sample volume', 4.5F)

		def bloodSamplingEventAfter = new SamplingEvent(
			startTime		: 3 * 24 * 3600 + 30 * 3600,
			template		: bloodSamplingEventTemplate,
			sampleTemplate	: humanBloodSampleTemplate).setFieldValue('Sample volume', 4.5F)

		rootGroup.addToEvents fastingEvent
		rootGroup.addToSamplingEvents bloodSamplingEventBefore
		rootGroup.addToSamplingEvents bloodSamplingEventAfter

		humanStudy.addToEvents(fastingEvent)
		humanStudy.addToSamplingEvents(bloodSamplingEventBefore)
		humanStudy.addToSamplingEvents(bloodSamplingEventAfter)
		humanStudy.addToEventGroups rootGroup

		humanStudy.save(failOnError:true)

		def y = 1
		11.times {
			def currentSubject = new Subject(
				name		: "" + y++,
				species		: humanTerm,
				template	: humanTemplate
			).setFieldValue("Gender", (Math.random() > 0.5) ? "Male" : "Female")
				//.setFieldValue("DOB", new java.text.SimpleDateFormat("dd-mm-yy").parse("01-02-19" + (10 + (int) (Math.random() * 80)))).setFieldValue("DOB", new Date().parse("dd/mm/yyyy", ((10 + (int) Math.random() * 18) + "/0" + (1 + (int) (Math.random() * 8)) + "/19" + (10 + (int) (Math.random() * 80))))).setFieldValue("Age", 30).setFieldValue("Height", Math.random() * 2F).setFieldValue("Weight", Math.random() * 150F).setFieldValue("BMI", 20 + Math.random() * 10F)

			humanStudy.addToSubjects(currentSubject)
			currentSubject.save(failOnError:true)

			rootGroup.addToSubjects currentSubject
			rootGroup.save(failOnError:true)

			def currentSample = new Sample(
				name		: currentSubject.name + '_B',
				material	: bloodTerm,
				template	: humanBloodSampleTemplate,
				parentSubject: currentSubject,
				parentEvent	: bloodSamplingEventBefore,
                parentEventGroup : rootGroup
			)

			humanStudy.addToSamples(currentSample)
            currentSample.setFieldValue("Text on vial", "T" + (Math.random() * 100L))
            currentSample.save(failOnError:true)

			currentSample = new Sample(
				name		: currentSubject.name + '_A',
				material	: bloodTerm,
				template	: humanBloodSampleTemplate,
				parentSubject: currentSubject,
				parentEvent	: bloodSamplingEventAfter,
                parentEventGroup : rootGroup
			)

			humanStudy.addToSamples(currentSample)
            currentSample.setFieldValue("Text on vial", "T" + (Math.random() * 100L))
            currentSample.save(failOnError:true)
        }

		// Add persons to study
		def studyperson3 = new StudyPerson(person: person1, role: role2)
		humanStudy.addToPersons(studyperson3).addToPublications(publication2).save(failOnError:true)

		// Add SAM assay reference
		def clinicalModule = new AssayModule(
			name: 'SAM module for clinical data',
			url: config.modules.sam.url.toString()
		).save(failOnError:true)

		// Add metabolomics assay reference
		def metabolomicsModule = new AssayModule(
			name: 'Metabolomics module',
			url: config.modules.metabolomics.url.toString()
		).save(failOnError:true)

		// Add metabolomics assay reference
		def massSequencingModule = new AssayModule(
			name: 'Mass Sequencing module',
			url: config.modules.massSequencing.url.toString()
		).save(failOnError:true)

		def lipidAssayRef = new Assay(
			name: 'Lipid profiling',
			template: ccAssayTemplate,
			module: clinicalModule
		)

		def metAssayRef = new Assay(
			name: 'Lipidomics profile',
			template: metAssayTemplate,
			module: metabolomicsModule
		).setFieldValue('Spectrometry technique', 'LC/MS')

		mouseStudy.samples*.each {
			lipidAssayRef.addToSamples(it)
			metAssayRef.addToSamples(it)
		}

		mouseStudy.addToAssays(lipidAssayRef);
		mouseStudy.addToAssays(metAssayRef);
		mouseStudy.save(failOnError:true)

		def glucoseAssayBRef = new Assay(
			name		: 'Glucose assay before',
			template	: ccAssayTemplate,
			module		: clinicalModule
		)

		def glucoseAssayARef = new Assay(
			name		: 'Glucose assay after',
			template	: ccAssayTemplate,
			module		: clinicalModule
		)

		def metAssayRefB = new Assay(
			name		: 'Lipidomics profile before',
			template	: metAssayTemplate,
			module		: metabolomicsModule
		).setFieldValue('Spectrometry technique', 'GC/MS')

		def metAssayRefA = new Assay(
			name		: 'Lipidomics profile after',
			template	: metAssayTemplate,
			module		: metabolomicsModule
		).setFieldValue('Spectrometry technique', 'GC/MS')


		// Add sequencing (metagenomics) assays
		def sequencingAssay16SRef = new Assay(
			name		: '16S Sequencing assay',
			template	: ccAssayTemplate,
			module		: massSequencingModule
		)

		// Add sequencing (metagenomics) assays
		def sequencingAssay18SRef = new Assay(
			name		: '18S Sequencing assay',
			template	: ccAssayTemplate,
			module		: massSequencingModule
		)

		humanStudy.samples*.each {
			if (it.parentEvent.startTime == 0) {
				glucoseAssayBRef.addToSamples(it)
				metAssayRefB.addToSamples(it)
			}
			else {
				glucoseAssayARef.addToSamples(it)
				metAssayRefA.addToSamples(it)
				sequencingAssay16SRef.addToSamples(it)
				sequencingAssay18SRef.addToSamples(it)
			}
		}

		humanStudy.addToAssays(sequencingAssay16SRef)
		humanStudy.addToAssays(sequencingAssay18SRef)
		humanStudy.addToAssays(glucoseAssayARef)
		humanStudy.addToAssays(glucoseAssayBRef)
		humanStudy.addToAssays(metAssayRefA)
		humanStudy.addToAssays(metAssayRefB)
		humanStudy.save(failOnError:true)
	}

    /**
     * Add test studies. This function is meant to be called only in test mode
     */
    static addTestStudies() {

        final String testStudyName = "Test study"
        final String testStudyTemplateName = "NMC Study"
        final String testStudyCode = "AAA-Test"
        final String testStudyDescription = "Description of Test Study"
        final Date testStudyStartDate = Date.parse('yyyy-MM-dd','2007-12-11')

        def studyTemplate = Template.findByName(testStudyTemplateName)
        assert studyTemplate

        def study = new Study(
            title: testStudyName,
            template: studyTemplate,
            startDate: testStudyStartDate,
            code: testStudyCode,
            description: testStudyDescription
        )

        study.save(failOnError: true)

    }
}
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
            baseUrl: config.modules.sam.url.toString(),
			notify: true,
			openInFrame: false
		).save(failOnError:true)

		// Add metabolomics assay reference
		def metabolomicsModule = new AssayModule(
			name: 'Metabolomics module',
			url: config.modules.metabolomics.url.toString(),
            baseUrl: config.modules.sam.url.toString()
		).save(failOnError:true)

		// Add metabolomics assay reference
		def massSequencingModule = new AssayModule(
			name: 'Mass Sequencing module',
			url: config.modules.massSequencing.url.toString(),
            baseUrl: config.modules.sam.url.toString()
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
		def seqAssayTemplate            = Template.findByName("Mass Sequencing assay")

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
			startDate	: Date.parse('yyyy-MM-dd', '2008-01-02'),
			owner		: owner,
			readers		: [otherUser]
        ).save(failOnError:true)

		
		def evLF = new Event(
			//startTime	: 3600,
			//endTime		: 3600 + 7 * 24 * 3600,
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

		def evS = new SamplingEvent(
			startTime	: 3600 + 7 * 24 * 3600,
			template	: liverSamplingEventTemplate,
			sampleTemplate: humanTissueSampleTemplate).setFieldValue('Sample weight', 5F)

		def evS4 = new SamplingEvent(
			startTime	: 3600 + 4 * 7 * 24 * 3600,
			template	: liverSamplingEventTemplate,
			sampleTemplate: humanTissueSampleTemplate).setFieldValue('Sample weight', 5F)

		// Add events to study
		mouseStudy.addToEvents(evLF).addToEvents(evHF).addToEvents(evBV).addToEvents(evBL).addToSamplingEvents(evS).addToSamplingEvents(evS4).save(flush: true, failOnError:true)
		
		// Extra check if the SamplingEvents are saved correctly
		evS.save(failOnError:true)
		evS4.save(failOnError:true)

		def startTime = 3600
		def oneWeek = 7 * 24 * 3600
		def fourWeeks = 4 * oneWeek
		
		def LFBV1 = new EventGroup(name: "10% fat + vehicle for 1 week")
			.addToEventInstances( new EventInEventGroup( event: evLF, startTime: 0, duration: oneWeek ) )
			.addToEventInstances( new EventInEventGroup( event: evBV, startTime: 0, duration: oneWeek) )
			.addToSamplingEventInstances( new SamplingEventInEventGroup( event: evS, startTime: oneWeek ) )

		def LFBL1 = new EventGroup(name: "10% fat + leptin for 1 week")
			.addToEventInstances( new EventInEventGroup( event: evLF, startTime: 0, duration: oneWeek ) )
			.addToEventInstances( new EventInEventGroup( event: evBL, startTime: 0, duration: oneWeek) )
			.addToSamplingEventInstances( new SamplingEventInEventGroup( event: evS, startTime: oneWeek ) )

		def HFBV1 = new EventGroup(name: "45% fat + vehicle for 1 week")
			.addToEventInstances( new EventInEventGroup( event: evHF, startTime: 0, duration: oneWeek ) )
			.addToEventInstances( new EventInEventGroup( event: evBV, startTime: 0, duration: oneWeek) )
			.addToSamplingEventInstances( new SamplingEventInEventGroup( event: evS, startTime: oneWeek ) )

		def HFBL1 = new EventGroup(name: "45% fat + leptin for 1 week")
			.addToEventInstances( new EventInEventGroup( event: evHF, startTime: 0, duration: oneWeek ) )
			.addToEventInstances( new EventInEventGroup( event: evBL, startTime: 0, duration: oneWeek) )
			.addToSamplingEventInstances( new SamplingEventInEventGroup( event: evS, startTime: oneWeek ) )

		def LFBV4 = new EventGroup(name: "10% fat + vehicle for 4 weeks")
			.addToEventInstances( new EventInEventGroup( event: evLF, startTime: 0, duration: fourWeeks ) )
			.addToEventInstances( new EventInEventGroup( event: evBV, startTime: 0, duration: fourWeeks) )
			.addToSamplingEventInstances( new SamplingEventInEventGroup( event: evS, startTime: fourWeeks ) )

		def LFBL4 = new EventGroup(name: "10% fat + leptin for 4 weeks")
			.addToEventInstances( new EventInEventGroup( event: evLF, startTime: 0, duration: fourWeeks ) )
			.addToEventInstances( new EventInEventGroup( event: evBL, startTime: 0, duration: fourWeeks) )
			.addToSamplingEventInstances( new SamplingEventInEventGroup( event: evS, startTime: fourWeeks ) )

		def HFBV4 = new EventGroup(name: "45% fat + vehicle for 4 weeks")
			.addToEventInstances( new EventInEventGroup( event: evHF, startTime: 0, duration: fourWeeks ) )
			.addToEventInstances( new EventInEventGroup( event: evBV, startTime: 0, duration: fourWeeks) )
			.addToSamplingEventInstances( new SamplingEventInEventGroup( event: evS, startTime: fourWeeks ) )

		def HFBL4 = new EventGroup(name: "45% fat + leptin for 4 weeks")
			.addToEventInstances( new EventInEventGroup( event: evHF, startTime: 0, duration: fourWeeks ) )
			.addToEventInstances( new EventInEventGroup( event: evBL, startTime: 0, duration: fourWeeks) )
			.addToSamplingEventInstances( new SamplingEventInEventGroup( event: evS, startTime: fourWeeks ) )
			
        // Add EventGroups to study
        mouseStudy
			.addToEventGroups(LFBV1)
			.addToEventGroups(LFBL1)
			.addToEventGroups(HFBV1)
			.addToEventGroups(HFBL1)
			.addToEventGroups(LFBV4)
			.addToEventGroups(LFBL4)
			.addToEventGroups(HFBV4)
			.addToEventGroups(HFBL4)
			.save(failOnError:true)

        mouseStudy.eventGroups.each {
            // save eventGroups explicitly, to prevent 'TransientObjectException: object references an unsaved transient instance' when referencing these later
            it.save(failOnError: true)
        }

		// Create subjectgroups and combine them with eventgroups
		def subjectGroups = [:]
		def eventGroups = [:]
		def subjectEventGroups = [:]
		8.times { 
			def group = new SubjectGroup( name: "Subjectgroup " + it)
			mouseStudy.addToSubjectGroups( group )
			group.save( failOnError: true )
			subjectGroups[ it ] = group
			
			def tmpEventGroup
			switch( it ) {
				case 0: tmpEventGroup = LFBV1; break;
				case 1: tmpEventGroup = LFBL1; break;
				case 2: tmpEventGroup = HFBV1; break;
				case 3: tmpEventGroup = HFBL1; break;
				case 4: tmpEventGroup = LFBV4; break;
				case 5: tmpEventGroup = LFBL4; break;
				case 6: tmpEventGroup = HFBV4; break;
				case 7: tmpEventGroup = HFBL4; break;
			}
			
			eventGroups[ it ] = tmpEventGroup
			
			def subjectEventGroup = new SubjectEventGroup( subjectGroup: group, eventGroup: tmpEventGroup, startTime: startTime )
			mouseStudy.addToSubjectEventGroups( subjectEventGroup )
			subjectEventGroup.save()
			subjectEventGroups[ it ] = subjectEventGroup
		}
		
        // Add subjects and samples and compose SubjectGroups
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
			def idx = (int) Math.floor( it / 10 )
            def tmpSubjectGroup = subjectGroups[ idx ]
			def tmpEventGroup = eventGroups[ idx ]
            tmpSubjectGroup.addToSubjects(currentSubject)

			// Create sample
			def currentSample = new Sample(
				name: currentSubject.name + '_B',
				material: bloodTerm,
				template: humanBloodSampleTemplate,
				parentSubject: currentSubject,
				parentEvent: tmpEventGroup.samplingEventInstances.asList().get(0),
                parentSubjectEventGroup: subjectEventGroups[ idx ]
			)

			mouseStudy.addToSamples(currentSample)
            currentSample.setFieldValue("Text on vial", "T" + (Math.random() * 100L))
            currentSample.save(failOnError:true)
        }

		subjectGroups.each {
			it.value.save( failOnError: true )
		}
		
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

		def fastingEvent = new Event(
			template		: fastingTreatmentTemplate).setFieldValue('Fasting period', '8h')

		def bloodSamplingEvent = new SamplingEvent(
			template		: bloodSamplingEventTemplate,
			sampleTemplate	: humanBloodSampleTemplate).setFieldValue('Sample volume', 4.5F)

		humanStudy.addToEvents(fastingEvent)
		humanStudy.addToSamplingEvents(bloodSamplingEvent)
		humanStudy.save( flush: true )
		
		def bloodSamplingBefore = new SamplingEventInEventGroup( 	event: bloodSamplingEvent, 	startTime: 0 )
		def bloodSamplingAfter = new SamplingEventInEventGroup( 	event: bloodSamplingEvent, 	startTime: 3 * 24 * 3600 + 30 * 3600 )
			
		def rootGroup = new EventGroup(name: 'Root group')
			.addToSamplingEventInstances( bloodSamplingBefore )
			.addToSamplingEventInstances( bloodSamplingAfter )
			.addToEventInstances( new EventInEventGroup( event: fastingEvent, startTime: 3 * 24 * 3600 + 22 * 3600, duration: 8 * 3600 ))			

		def subjectGroup = new SubjectGroup( name: "All subjects" )
		
		humanStudy.addToEventGroups(rootGroup)
		humanStudy.addToSubjectGroups subjectGroup
		
		humanStudy.save( flush: true )
		
		def seGroup = new SubjectEventGroup( subjectGroup: subjectGroup, eventGroup: rootGroup, startTime: 0 )
		humanStudy.addToSubjectEventGroups seGroup
		
		if (!humanStudy.validate()) {
			println "Human study validation errors:"
			humanStudy.errors.each {
				println it
			}
		}
		humanStudy.save(failOnError: true)
		seGroup.save( failOnError: true )
		
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

			subjectGroup.addToSubjects currentSubject
			subjectGroup.save(failOnError:true)

			def currentSample = new Sample(
				name		: currentSubject.name + '_B',
				material	: bloodTerm,
				template	: humanBloodSampleTemplate,
				parentSubject: currentSubject,
				parentEvent	: bloodSamplingBefore,
                parentSubjectEventGroup : seGroup
			)

			humanStudy.addToSamples(currentSample)
            currentSample.setFieldValue("Text on vial", "T" + (Math.random() * 100L))
            currentSample.save(failOnError:true)

			currentSample = new Sample(
				name		: currentSubject.name + '_A',
				material	: bloodTerm,
				template	: humanBloodSampleTemplate,
				parentSubject: currentSubject,
				parentEvent	: bloodSamplingAfter,
                parentSubjectEventGroup : seGroup
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
			url: config.modules.sam.url.toString(),
            baseUrl: config.modules.sam.url.toString()
		).save(failOnError:true)

		// Add metabolomics assay reference
		def metabolomicsModule = new AssayModule(
			name: 'Metabolomics module',
			url: config.modules.metabolomics.url.toString(),
            baseUrl: config.modules.sam.url.toString()
		).save(failOnError:true)

		// Add metabolomics assay reference
		def massSequencingModule = new AssayModule(
			name: 'Mass Sequencing module',
			url: config.modules.massSequencing.url.toString(),
            baseUrl: config.modules.sam.url.toString()
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

		if (!mouseStudy.validate()) {
			println "Mouse study validation errors:"
			mouseStudy.errors.each {
				println it
			}
		}
		mouseStudy.save(failOnError: true)

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
			template	: seqAssayTemplate,
			module		: massSequencingModule
		)

		// Add sequencing (metagenomics) assays
		def sequencingAssay18SRef = new Assay(
			name		: '18S Sequencing assay',
			template	: seqAssayTemplate,
			module		: massSequencingModule
		)


		humanStudy.addToAssays(sequencingAssay16SRef)
		humanStudy.addToAssays(sequencingAssay18SRef)
		humanStudy.addToAssays(glucoseAssayARef)
		humanStudy.addToAssays(glucoseAssayBRef)
		humanStudy.addToAssays(metAssayRefA)
		humanStudy.addToAssays(metAssayRefB)

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

		if (!humanStudy.validate()) {
			println "Human study validation errors:"
			humanStudy.errors.each {
				println it
			}
		}
		humanStudy.save(failOnError: true)
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
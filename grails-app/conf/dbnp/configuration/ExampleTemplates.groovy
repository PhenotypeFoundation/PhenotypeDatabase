/**
 * @Author kees
 * @Since Jun 25, 2010
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.configuration


import dbnp.studycapturing.*
import org.dbnp.gdt.*
import org.codehaus.groovy.grails.commons.GrailsApplication

class ExampleTemplates {

	/**
	 * Add the ontologies that are necessary for the templates below manually
	 * This function can be called to avoid the HTTP requests to BioPortal each time
	 * (e.g. in development or automated test environments)
	 */
	public static void initTemplateOntologies() {
		// Grom a development message
		if (String.metaClass.getMetaMethod("grom")) "inserting initial ontologies".grom()

		// If running in development or test mode, add ontologies manually to speed up development and allow running offline
		if (grails.util.GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT || grails.util.GrailsUtil.environment == GrailsApplication.ENV_TEST) {

			// add Species ontology which is used for a.o. the Subject domain field 'species'
			def speciesOntology = new Ontology(
				name: 'NCBI organismal classification',
				url: 'http://data.bioontology.org/ontologies/NCBITAXON',
                acronym: 'NCBITAXON',
				versionNumber: '1.2'
			).save(failOnError:true)

			// add Sample>material ontology
			def brendaOntology = new Ontology(
				name: 'BRENDA tissue / enzyme source',
                    url: 'http://data.bioontology.org/ontologies/BTO',
                acronym: 'BTO',
				versionNumber: '1.3'
			).save(failOnError:true)

			// add NCI ontology which is used in Mouse genotype template field
			def nciOntology = new Ontology(
				name: 'NCI Thesaurus',
				url: 'http://data.bioontology.org/ontologies/NCIT',
                acronym: 'NCIT',
				versionNumber: '10.03'
			).save(failOnError:true)

			// add CHEBI ontology which is used for describing chemicals in e.g. events
			def chebiOntology = new Ontology(
				name: 'Chemical entities of biological interest',
				url: 'http://data.bioontology.org/ontologies/CHEBI',
                acronym: 'CHEBI',
				versionNumber: '1.73'
			).save(failOnError:true, flush:true)

		}
		// otherwise, this may be a production demo instance, so initialize the ontologies dynamically from BioPortal
		else {

            def speciesOntology		= Ontology.getOrCreateOntology("http://data.bioontology.org/ontologies/NCBITAXON")
            def brendaOntology		= Ontology.getOrCreateOntology("http://data.bioontology.org/ontologies/BTO")
            def nciOntology			= Ontology.getOrCreateOntology("http://data.bioontology.org/ontologies/NCIT")
            def chebiOntology       = Ontology.getOrCreateOntology("http://data.bioontology.org/ontologies/CHEBI")
		}
	}


	/**
	 * Add example templates, this function would normally be called on an empty database
	 */
	public static void initTemplates() {
		// Grom a development message
		if (String.metaClass.getMetaMethod("grom")) "inserting initial templates".grom()

		def genderField = new TemplateField(
			name: 'Gender',type: TemplateFieldType.STRINGLIST, entity: Subject)
		.addListEntries(['Male','Female','Unknown'])
		.save(failOnError:true)

		def ageField = new TemplateField(
			name: 'Age',type: TemplateFieldType.LONG,entity: Subject,unit: 'years',comment: 'Either include age at the start of the study or date of birth (if known)')
		.save(failOnError:true)

		def genotypeField = new TemplateField(
			name: 'Genotype', type: TemplateFieldType.ONTOLOGYTERM,entity: Subject,
			comment: 'If present, indicate the genetic variance of the subject (e.g., mutagenized populations,knock-out/in,transgene etc)')
        .save(failOnError:true)

		def genotypeTypeField = new TemplateField(
			name: 'Genotype type',type: TemplateFieldType.STRINGLIST,entity: Subject,
			comment: 'If a genotype was specified, please indicate here the type of the genotype')
		.addListEntries(['wildtype','transgenic','knock-out','knock-in'])
        .save(failOnError:true)

		def varietyField = new TemplateField(
			name: 'Variety', type: TemplateFieldType.STRING,entity: Subject,
			comment: 'taxonomic category consisting of members of a species that differ from others of the same species in minor but heritable characteristics')
        .save(failOnError:true)

		def ecotypeField = new TemplateField(
			name: 'Ecotype', type: TemplateFieldType.STRING,entity: Subject,
			 comment: 'a type or subspecies of life that is especially well adapted to a certain environment'
		).save(failOnError:true)

		// Nutritional study template
		def studyTemplate = new Template(
			name: 'NMC Study',
			entity: dbnp.studycapturing.Study
		)
		.addToFields(new TemplateField(name: 'Objectives',type: TemplateFieldType.TEXT,entity: Study,comment:'Fill out the aim or questions of the study'))
		.addToFields(new TemplateField(name: 'Consortium',type: TemplateFieldType.STRING,entity: Study,comment:'If the study was performed within a consortium (e.g. NMC, NuGO), you can indicate this here'))
		.addToFields(new TemplateField(name: 'Cohort name',type: TemplateFieldType.STRING,entity: Study,comment:'If a cohort was used the name or code of the cohort can be define here (define a cohort template)'))
		.addToFields(new TemplateField(name: 'Lab id',type: TemplateFieldType.STRING,entity: Study,comment:'In which lab was the study performed; indicate the roomnumber.'))
		.addToFields(new TemplateField(name: 'Institute',type: TemplateFieldType.STRING,entity: Study,comment:'In which institute was the study performed; indicate the full address information (to be replaced by persons-affiliations?)'))
		.addToFields(new TemplateField(name: 'Study protocol',type: TemplateFieldType.FILE,entity: Study,comment:'Optionally attach a file in which the protocol in the study is described'))
        .save(failOnError:true)

		// Mouse template
		def mouseTemplate = new Template(
			name: 'Mouse', entity: dbnp.studycapturing.Subject)
		.addToFields(new TemplateField(
			name: 'Strain', type: TemplateFieldType.ONTOLOGYTERM, ontologies: [Ontology.getOrCreateOntology("http://data.bioontology.org/ontologies/NCIT")], entity: Subject, comment: "This is an ontology term, if the right strain is not in the list please add it with 'add more'"))
		.addToFields(genotypeField)
		.addToFields(genotypeTypeField)
		.addToFields(genderField)
		.addToFields(new TemplateField(
			name: 'Age', type: TemplateFieldType.LONG, entity: Subject, unit: 'weeks', comment: 'Age at start of study'))
		.addToFields(new TemplateField(
			name: 'Age type',type: TemplateFieldType.STRINGLIST,entity: Subject).addListEntries(['postnatal','embryonal']))
		.addToFields(new TemplateField(
			name: 'Cage',type: TemplateFieldType.STRING,entity: Subject,comment:'Indicate the cage used for housing (type and/or size)'))
		.addToFields(new TemplateField(
			name: '#Mice in cage',type: TemplateFieldType.LONG,entity: Subject,comment:'If known, indicate the number of mice per cage'))
		.addToFields(new TemplateField(
			name: 'Litter size',type: TemplateFieldType.LONG,entity: Subject,comment:'If known, indicate the litter size of the litter from which the subject originates'))
		.addToFields(new TemplateField(
			name: 'Weight', type: TemplateFieldType.DOUBLE, unit: 'gram',entity: Subject,comment:'If known indicate the weight of the subject in grams at the start of the study'))
        .save(failOnError:true)

		// Human template
		def humanTemplate = new Template(
			name: 'Human', entity: dbnp.studycapturing.Subject)
		.addToFields(genderField)
		.addToFields(ageField)
		.addToFields(new TemplateField(
			name: 'DOB',type: TemplateFieldType.DATE,entity: Subject,comment:'Date of birth'))
		.addToFields(new TemplateField(
			name: 'Height',type: TemplateFieldType.DOUBLE, entity: Subject, unit: 'm'))
		.addToFields(new TemplateField(
			name: 'Weight',type: TemplateFieldType.DOUBLE, entity: Subject, unit: 'kg'))
		.addToFields(new TemplateField(
			name: 'BMI',type: TemplateFieldType.DOUBLE, entity: Subject, unit: 'kg/m2',comment:'Body-mass-index'))
		.addToFields(new TemplateField(
			name: 'Race',type: TemplateFieldType.STRING,entity: Subject, comment:'If known and of interest the ethnic group can be indicated'))
		.addToFields(new TemplateField(
			name: 'Waist circumference',type: TemplateFieldType.DOUBLE, unit: 'cm',entity: Subject, comment:'The waist circumference is measured just above the hip bone. Indicate the measure at the start of the study.'))
		.addToFields(new TemplateField(
			name: 'Hip circumference',type: TemplateFieldType.DOUBLE, unit: 'cm',entity: Subject, comment:'The hip circumference is measured at the level of the two bony prominences front of the hips. Indicate the measure at the start of the study.'))
		.addToFields(new TemplateField(
			name: 'Systolic blood pressure',type: TemplateFieldType.DOUBLE, unit: 'mmHg',entity: Subject, comment:'Indicate the levels at the start of the study in mmHG'))
		.addToFields(new TemplateField(
			name: 'Diastolic blood pressure',type: TemplateFieldType.DOUBLE, unit: 'mmHg',entity: Subject, comment:'Indicate the levels at the start of the study in mmHG'))
		.addToFields(new TemplateField(
			name: 'Heart rate',type: TemplateFieldType.DOUBLE, unit: 'beats/min',entity: Subject, comment:'Indicate the heart rate at the start of in study in beats per minute'))
		.addToFields(new TemplateField(
			name: 'Run-in-food',type: TemplateFieldType.TEXT,entity: Subject, comment:'If defined, give a short description of the food used before the measurements'))
		.save(failOnError:true)

		def sampleRemarksField = new TemplateField(
			name: 'Remarks',
			type: TemplateFieldType.TEXT,
		    entity: Sample
		).save(failOnError:true)

		def sampleVialTextField = new TemplateField(
			name: 'Text on vial',
			type: TemplateFieldType.STRING,
		    entity: Sample
		).save(failOnError:true)

		// Human tissue sample template
		def humanSampleTemplate = new Template(
			name: 'Human tissue sample',
			entity: dbnp.studycapturing.Sample
		)
		.addToFields(sampleRemarksField)
		.addToFields(sampleVialTextField)
		.addToFields(
			new TemplateField(
			    name: 'Sample measured weight',
			    unit: 'mg',
			    type: TemplateFieldType.DOUBLE,
		        entity: Sample
			)
        ).save(failOnError:true)

		// Human blood sample template
		def humanBloodSampleTemplate = new Template(
		    name: 'Human blood sample',
			entity: dbnp.studycapturing.Sample
		)
		.addToFields(sampleRemarksField)
		.addToFields(sampleVialTextField)
		.addToFields(
			new TemplateField(
			    name: 'Sample measured volume',
			    unit: 'ml',
			    type: TemplateFieldType.DOUBLE,
				entity: Sample
			)
        ).save(failOnError:true)


		/*
		 * Add NMC - DCL Sample Mapping Template
		 * by Michael van Vliet
		 *
		 * For the Pilot running in Leiden (NOV2010)
		 */
		def sampleDCLTextField = new TemplateField(
			name: 'DCL Sample Reference',
			type: TemplateFieldType.STRING,
			entity: Sample
        ).save(failOnError:true)

		// Human tissue sample template
		def dclSampleTemplate = new Template(
			name: 'DCL Sample information',
			entity: dbnp.studycapturing.Sample
		)
		.addToFields(sampleDCLTextField)
        .save(failOnError:true)
		// EO DCL Sample Mapping Template**********************************


		/*
		def GrowthTreatmentTemplate = new Template(
			name: 'Growth treatment',
			entity: dbnp.studycapturing.Event
		)
		.addToFields(sampleDescriptionField)
		.addToFields(new TemplateField(name: 'position X',type: TemplateFieldType.STRING))
		.addToFields(new TemplateField(name: 'position Y',type: TemplateFieldType.STRING))
		.addToFields(new TemplateField(name: 'Block',type: TemplateFieldType.STRING))
		.addToFields(new TemplateField(name: 'Temparature Day',type: TemplateFieldType.STRING))
		.addToFields(new TemplateField(name: 'Temparature Night',type: TemplateFieldType.STRING))
		.addToFields(new TemplateField(name: 'Light Intensity',type: TemplateFieldType.STRING))
		.addToFields(new TemplateField(name: 'Harvest Delay',type: TemplateFieldType.STRING))
		.save(failOnError:true)
		 */

		//Plant template
		def greenHouseTemplate = new Template(
			name: 'Plant-green house ',
			entity: dbnp.studycapturing.Subject
		)
		.addToFields(varietyField)
		.addToFields(ecotypeField)
		.addToFields(genotypeField)
		/*
		.addToFields(genotypeTypeField)
		.addToFields(
			new TemplateField(
				name: 'Growth location', type: TemplateFieldType.STRINGLIST,
				listEntries: [
					new TemplateFieldListItem(name:'Greenhouse'),
					new TemplateFieldListItem(name: 'Field')
				]
			)
		)
		.addToFields(
			new TemplateField(
				name: 'Room', type: TemplateFieldType.STRING,
				comment: 'Chamber number in case of Greenhouse'
			)
		)
		*/
		.addToFields(
			new TemplateField(
				name: 'Chamber no.',
				type: TemplateFieldType.STRING,
				entity: Subject,
				comment: 'Chamber number in the Greenhouse'
			)
		)
		.addToFields(
			new TemplateField(
				name: 'Growth type',
				entity: Subject,
				type: TemplateFieldType.STRINGLIST
			).addListEntries(['Standard','Experimental','Unknown'])
		)
		.addToFields(new TemplateField(
			name: 'Growth protocol', entity: Subject, type: TemplateFieldType.TEXT))
		.addToFields(new TemplateField(
			name: 'Position X', entity: Subject, type: TemplateFieldType.DOUBLE))
		.addToFields(new TemplateField(
			name: 'Position Y', entity: Subject, type: TemplateFieldType.DOUBLE))
		.addToFields(new TemplateField(
			name: 'Block', entity: Subject, type: TemplateFieldType.STRING))
		.addToFields(new TemplateField(
			name: 'Temperature at day', entity: Subject, type: TemplateFieldType.DOUBLE))
		.addToFields(new TemplateField(
			name: 'Temperature at night', entity: Subject, type: TemplateFieldType.DOUBLE))
		.addToFields(new TemplateField(
			name: 'Photo period', entity: Subject, type: TemplateFieldType.STRING))
		.addToFields(new TemplateField(
			name: 'Light intensity', entity: Subject, type: TemplateFieldType.STRING))
		.addToFields(new TemplateField(
			name: 'Start date', entity: Subject, type: TemplateFieldType.DATE))
		.addToFields(new TemplateField(
			name: 'Harvest date', entity: Subject, type: TemplateFieldType.DATE))
		.addToFields(new TemplateField(
			name: 'Harvest delay', entity: Subject, type: TemplateFieldType.TEXT))
		.addToFields(new TemplateField(
			name: 'Additional info', entity: Subject, type: TemplateFieldType.TEXT))
        .save(failOnError:true)

		def FieldTemplate = new Template(
			name: 'Plant-open field',
			entity: dbnp.studycapturing.Subject
		)
		.addToFields(varietyField)
		.addToFields(ecotypeField)
		.addToFields(genotypeField)
		.addToFields(new TemplateField(
			name: 'Start date', entity: Subject, type: TemplateFieldType.DATE))
		.addToFields(new TemplateField(
			name: 'Harvest date', entity: Subject, type: TemplateFieldType.DATE))
		.addToFields(new TemplateField(
			name: 'Growth type', entity: Subject, type: TemplateFieldType.STRINGLIST).addListEntries(['Standard','Experimental']))
		.addToFields(new TemplateField(
			name: 'Growth protocol', entity: Subject, type: TemplateFieldType.TEXT))
		.addToFields(new TemplateField(
			name: 'Harvest delay', entity: Subject, type: TemplateFieldType.TEXT))
        .save(failOnError:true)

		//Plant template
		def chamberTemplate = new Template(
			name: 'Plant-chamber',
			entity: dbnp.studycapturing.Subject
		)
		.addToFields(varietyField)
		.addToFields(ecotypeField)
		.addToFields(genotypeField)
		/*
		.addToFields(genotypeTypeField)
		.addToFields(
			new TemplateField(
							name: 'Growth location',
							type: TemplateFieldType.STRINGLIST,
							listEntries: [
								new TemplateFieldListItem(name:'Greenhouse'),
								new TemplateFieldListItem(name: 'Field')
							]
						)
		)
		*/
		.addToFields(new TemplateField(
			name: 'Room', type: TemplateFieldType.STRING, entity: Subject,
			comment: 'room number'))
		.addToFields(new TemplateField(
			name: 'Chamber no.', type: TemplateFieldType.STRING, entity: Subject,
			comment: 'Chamber number'))
		.addToFields(new TemplateField(
			name: 'Block', type: TemplateFieldType.STRING, entity: Subject))
		.addToFields(new TemplateField(
			name: 'Position X', type: TemplateFieldType.DOUBLE, entity: Subject))
		.addToFields(new TemplateField(
			name: 'Position Y', type: TemplateFieldType.DOUBLE, entity: Subject))
		.addToFields(new TemplateField(
			name: 'Temperature at day', type: TemplateFieldType.DOUBLE, entity: Subject))
		.addToFields(new TemplateField(
			name: 'Temperature at night', type: TemplateFieldType.DOUBLE, entity: Subject))
		.addToFields(new TemplateField(
			name: 'Photo period', type: TemplateFieldType.STRING, entity: Subject))
		.addToFields(new TemplateField(
			name: 'Light intensity', type: TemplateFieldType.STRING, entity: Subject))
		.addToFields(new TemplateField(
			name: 'Start date', type: TemplateFieldType.DATE, entity: Subject))
		.addToFields(new TemplateField(
			name: 'Harvest date', type: TemplateFieldType.DATE, entity: Subject))
		.addToFields(new TemplateField(
			name: 'Growth type', type: TemplateFieldType.STRINGLIST, entity: Subject).addListEntries(['Standard','Experimental']))
		.addToFields(new TemplateField(
			name: 'Growth protocol', type: TemplateFieldType.TEXT, entity: Subject))
		.addToFields(new TemplateField(
			name: 'Harvest delay', type: TemplateFieldType.TEXT, entity: Subject))
		.save(failOnError:true)

		def plantSampleTemplate = new Template(
			name: 'Plant sample',
			entity: dbnp.studycapturing.Sample
		)
		.addToFields(sampleRemarksField)
		.addToFields(sampleVialTextField)
        .save(failOnError:true)

		def materialPrepTemplate = new Template(
			name: 'Plant-material preparation',
		    description: 'material preparation',
		    entity: dbnp.studycapturing.Event
		)
		.addToFields(new TemplateField(
			 name: 'Tissue',
			type: TemplateFieldType.STRING,
			entity: Event,
		    comment: 'organ/ fraction of culture/ plant part')
		)
		.addToFields(
			new TemplateField(
				 name: 'Grinding',
				type: TemplateFieldType.STRINGLIST,
				entity: Event
			).addListEntries(['yes','no','unknown'])
		)
		.addToFields(
			new TemplateField(
				name: 'Storage location',
				type: TemplateFieldType.STRING,
				entity: Event
			)
		)
		.addToFields(
			new TemplateField(
				name: 'protocol reference',
				type: TemplateFieldType.STRING,
				entity: Event
			)
		).save(failOnError:true)

		def protocolField = new TemplateField(
			name: 'Protocol',
			type: TemplateFieldType.FILE,
			entity: Event,
			comment: 'You can upload a protocol here which describes the procedure which was used when carrying out the event'
		).save(failOnError:true)


		// diet treatment template
		def dietTreatmentTemplate = new Template(
			name: 'Diet treatment',
			entity: dbnp.studycapturing.Event
		)
		.addToFields(
			new TemplateField(
				name: 'Diet',
				type: TemplateFieldType.STRINGLIST,
				entity: Event
			).addListEntries(['low fat','high fat'])
		)
		.addToFields(protocolField)
        .save(failOnError:true)

		// boost treatment template
		def boostTreatmentTemplate = new Template(
			name: 'Compound challenge',
			entity: dbnp.studycapturing.Event
		)
		.addToFields(
			new TemplateField(
				name: 'Compound',
				type: TemplateFieldType.ONTOLOGYTERM,
				entity: Event,
				ontologies: [Ontology.getOrCreateOntology("http://data.bioontology.org/ontologies/CHEBI")]
			)
		)
		.addToFields(
			new TemplateField(
				name: 'Control',
				type: TemplateFieldType.BOOLEAN,
				entity: Event
			)
		)
		.addToFields(protocolField)
        .save(failOnError:true)

		// fasting treatment template
		def fastingTreatment = new Template(
			name: 'Fasting treatment',
			description: 'Fasting for a specific amount of time',
			entity: dbnp.studycapturing.Event
		)
		.addToFields(
			new TemplateField(
				name: 'Fasting period',
				type: TemplateFieldType.RELTIME,
				entity: Event
			)
		).save(failOnError:true)

		// SamplingEvent templates
		def samplingProtocolField = new TemplateField(
			name: 'Sample Protocol',
			entity: SamplingEvent,
			type: TemplateFieldType.FILE,
			comment: 'You can upload a protocol here which describes the procedure which was used when carrying out the sampling event'
		).save(failOnError:true)

		// liver sampling event template
		def liverSamplingEventTemplate = new Template(
			name: 'Liver extraction',
			description: 'Liver sampling for transcriptomics arrays',
			entity: dbnp.studycapturing.SamplingEvent
		)
		.addToFields(samplingProtocolField)
		.addToFields(
			new TemplateField(
				name: 'Sample weight',
				unit: 'mg',
				entity: SamplingEvent,
				type: TemplateFieldType.DOUBLE
			)
		).save(failOnError:true)

		// blood sampling
		def bloodSamplingEventTemplate = new Template(
			name: 'Blood extraction',
			description: 'Blood extraction targeted at lipid assays',
			entity: dbnp.studycapturing.SamplingEvent
		)
		.addToFields(samplingProtocolField)
		.addToFields(
			new TemplateField(
				name: 'Sample volume',
				entity: SamplingEvent,
				unit: 'ml',
				type: TemplateFieldType.DOUBLE
			)
		).save(failOnError:true)

		// plant sample extraction event template
		def plantSamplingExtractEventTemplate = new Template(
			name: 'Plant sample extraction',
			description: 'sample extraction',
			entity: dbnp.studycapturing.SamplingEvent,
		    sampleTemplates: [plantSampleTemplate]
		)
		.addToFields(samplingProtocolField)
		.addToFields(
			new TemplateField(
				name: 'Sample weight',
				unit: 'ul',
				entity: SamplingEvent,
				type: TemplateFieldType.DOUBLE
			)
		)
		.addToFields(
			new TemplateField(
				name: 'Sample when measured',
				type: TemplateFieldType.STRINGLIST,
				entity: SamplingEvent
			).addListEntries(['Dried','Fresh','Unknown'])
		).save(failOnError:true)

		// plant sampling event template
		def plantSamplingEventTemplate = new Template(
			name: 'Plant-sample',
			description: 'plant sample ',
			entity: dbnp.studycapturing.SamplingEvent,
		    sampleTemplates: [plantSampleTemplate]
		)
		//.addToFields(samplingProtocolField)
		.addToFields(
			new TemplateField(
				name: 'material',
			    comment: 'physical charecteristic. e.g, grounded powder of tomato seed or liquid',
				entity: SamplingEvent,
				type: TemplateFieldType.STRING
			)
		)
		.addToFields(
			new TemplateField(
				name: 'Description',
				type: TemplateFieldType.STRING,
				entity: SamplingEvent
			)
		)
		.addToFields(
			new TemplateField(
				name: 'extracted material',
				comment: 'substance to be extracted. e.g., lipids, volatiles, primary metabolites etc',
				type: TemplateFieldType.STRING,
				entity: SamplingEvent
			)
		)
		.addToFields(
			new TemplateField(
				name: 'Text on vial',
				entity: SamplingEvent,
				type: TemplateFieldType.STRING
			)
		).save(failOnError:true)


		// assay templates
		def assayDescriptionField = new TemplateField(
				name: 'Description',
			    comment: 'add general assay information here',
				entity: Assay,
				type: TemplateFieldType.STRING
		).save(failOnError:true)

		def ccAssayTemplate = new Template(
			name: 'Clinical chemistry assay',
			description: 'Clinical chemistry assay stored in a SAM module',
			entity: dbnp.studycapturing.Assay
		)
		.addToFields(assayDescriptionField)
		.save(failOnError:true)

		def seqAssayTemplate = new Template(
			name: 'Mass Sequencing assay',
			description: 'DNA Sequencing assay stored in Mass Sequencing module',
			entity: dbnp.studycapturing.Assay
		)
		.addToFields(assayDescriptionField)
		.save(failOnError:true)

		def metAssayTemplate = new Template(
			name: 'Metabolomics assay',
			description: 'Metabolomics assay stored in a metabolomics module',
			entity: dbnp.studycapturing.Assay
		)
		.addToFields(assayDescriptionField)
		.addToFields(
			new TemplateField(
				name: 'Spectrometry technique',
			    comment: 'Select the used metabolomics technique',
				entity: Assay,
				type: TemplateFieldType.STRINGLIST
			).addListEntries(['GC/MS','LC/MS','NMR','HPLC'])
		).save(failOnError:true, flush: true)
	}

}
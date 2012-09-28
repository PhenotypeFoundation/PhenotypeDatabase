package gscf

import org.isatools.isacreator.configuration.MappingObject
import org.isatools.isacreator.io.exportisa.ISAFileOutput
import org.isatools.isacreator.io.exportisa.OutputISAFiles
import org.isatools.isacreator.managers.ConfigurationManager
import org.isatools.isacreator.model.Investigation
import org.isatools.isacreator.model.InvestigationContact
import org.isatools.isacreator.model.InvestigationPublication
import org.isatools.isacreator.model.Study
import org.isatools.isacreator.model.Assay
import grails.test.GrailsUnitTestCase
import org.isatools.isacreator.spreadsheet.model.TableReferenceObject

/**
 * Created with IntelliJ IDEA.
 * User: kees
 * Date: 28-09-12
 * Time: 17:31
 */
class WriteISATABTests extends GrailsUnitTestCase {

	final String testStudyName = "NuGO PPS human study"

	protected void setUp() {
		// Create the study
		super.setUp()

		// Add example studies, we need elaborate examples to test ISATAB output as fully as possible
		dbnp.configuration.ExampleStudies.addExampleStudies(dbnp.authentication.SecUser.findByUsername('user'), dbnp.authentication.SecUser.findByUsername('admin'))

	}

	void testWriteISATAB() {

		// Retrieve the study that should have been created in the setUp method
		dbnp.studycapturing.Study testStudy = dbnp.studycapturing.Study.findByTitle(testStudyName)
		assert testStudy

		// Load the ISATAB configuration
		// Apparently this stores the configuration in static variables in the ConfigurationManager class
		def ISAconfigDir = System.properties['base.dir'] + "/grails-app/conf/isaconfig-default_v2011-02-18"
		assert new File(ISAconfigDir).isDirectory()
		ConfigurationManager.loadConfigurations(ISAconfigDir)


		// Create the investigation using the properties from the study
        Investigation investigation = new Investigation(testStudy.code, testStudy.title);

		// Add contacts - mapping is straightforward
		testStudy.persons.each {
			investigation.addContact(new InvestigationContact(it.person.lastName,it.person.firstName,it.person.initials,it.person.email,it.person.phone,it.person.fax,it.person.address,it.person.affiliations.collect { it.toString() }.join(","),it.role.name))
		}

		// Add publications - mapping is straightforward, except status, which is used now to dump comments
		testStudy.publications.each {
			investigation.addPublication(new InvestigationPublication(it.pubMedID,it.DOI,it.authorsList,it.title,it.comments))
		}

		// Set filename
        investigation.setFileReference("target/i_investigation.txt");

		// Create a single study with the same name as the investigation
		Study study = new Study(testStudy.code);

		// TODO: add study start date, and study events and sampling events as factors

		// Create an 'assay' file describing all samples
        Assay studySample = new Assay("s_samples.txt", ConfigurationManager.selectTROForUserSelection(MappingObject.STUDY_SAMPLE));
        study.setStudySamples(studySample);

		// Loop over all samples in the study, and add the sample data to the file
		TableReferenceObject table = studySample.getTableReferenceObject();
		String[] newHeaders = ["Source Name", "Characteristics[organism]", "Term Source REF", "Term Accession Number", "Sample Name"]

		testStudy.samples.each {
			// TODO: add all other subject and sample information... will be some work to get the right headers here
			String[] newRowData = [it.parentSubject.name, it.parentSubject.species.name, it.parentSubject.species.accession, it.name]
			table.addRowData(newHeaders,newRowData);
		}

		// Add the study to the investigation
        investigation.addStudy(study);

		// Create stub files for all study assays
		testStudy.assays.each {
			println "Adding assay ${it.name} from module ${it.module.name}"

			Assay testAssay
			// TODO: really add assay, and synchronize configuration files with Phenotype Database modules

			switch(it.module.name) {
				case "Mass Sequencing module":
					testAssay = new Assay(it.name+".txt", "genome sequencing", "nucleotide sequencing",it.template.name);
					break
				case "Metabolomics module":
					testAssay = new Assay(it.name+".txt", "metabolite profiling", "mass spectrometry",it.getFieldValue("Spectrometry technique").toString());
					break
				case "SAM module for clinical data":
					testAssay = new Assay(it.name+".txt", "clinical chemistry analysis", "",it.name);
					break
			}

			if (testAssay) {
				study.addAssay(testAssay)
			}

		}

		// Save the created ISATAB meta files to the target directory
        ISAFileOutput fileOutput = new OutputISAFiles();
        fileOutput.saveISAFiles(true, investigation);
	}

}

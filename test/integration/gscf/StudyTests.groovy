package gscf

import grails.test.*
import dbnp.studycapturing.*
import dbnp.data.*

/**
 * Test the creation of a Subject and its TemplateEntity functionality on data model level
 *
 * @author keesvb
 * @since 20100511
 * @package dbnp.studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class StudyTests extends GrailsUnitTestCase {

	final String testStudyName = "Test study"
	final String testStudyTemplateName = "Academic study"
	final Date testStudyStartDate = Date.parse('yyyy-MM-dd','2007-12-11')
	final Date testStudyStartDate2 = Date.parse('yyyy-MM-dd','2008-05-11')
	final String testStudyStartDateString2 = "11-5-2010"
	// The following dates do net yet work:
	//final String testStudyStartDateString2 = "11-05-2010"
	//final String testStudyStartDateString2 = "Tue Dec 13 00:00:00 EST 2008"

	protected void setUp() {
		super.setUp()

		// Look up academic template
		def studyTemplate = Template.findByName(testStudyTemplateName)
		assert studyTemplate

		def study = new Study(
		    title: testStudyName,
		    template: studyTemplate,
		    startDate: testStudyStartDate
		)

		if (!study.validate()) {
			study.errors.each { println it}
		}
		assert study.validate()


		assert study.save(flush: true)

	}

	void testSave() {
		// Try to retrieve the study and make sure it's the same
		def studyDB = Study.findByTitle(testStudyName)
		assert studyDB
		assert studyDB.title.equals(testStudyName)
		assert studyDB.template.name.equals(testStudyTemplateName)
		assert studyDB.startDate.equals(testStudyStartDate)
	}

	void testSetDate() {
		def study = Study.findByTitle(testStudyName)
		assert study

		// Set a new date, using a string, and check whether that is stored correctly
		study.setFieldValue("startDate",testStudyStartDateString2)
		assert study.validate()
		assert study.save(flush:true)
		assert study.getFieldValue("startDate").equals(testStudyStartDate2) 

	}

	protected void tearDown() {
		super.tearDown()
	}

}

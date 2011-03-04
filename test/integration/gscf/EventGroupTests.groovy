package gscf

import gscf.StudyTests
import dbnp.studycapturing.*

/**
 * Test the creation of a EventGroup on data model level
 *
 * @author keesvb
 * @since 20100704
 * @package dbnp.studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

class EventGroupTests extends StudyTests {

	// This test extends StudyTests, so that we have a test study to assign as a parent study

	final String testEventGroupName = "Test Group"

	protected void setUp() {
		// Create the study
		super.setUp()

		// Retrieve the study that should have been created in StudyTests
		def study = Study.findByTitle(testStudyName)
		assert study

		// Create sample with the retrieved study as parent
		def group = new EventGroup(
		    name: testEventGroupName
		)

		// At this point, the event group should not validate, because it doesn't have a parent study assigned
		assert !group.validate()

		// Add the group to the retrieved parent study
		study.addToEventGroups(group)
		assert study.eventGroups.find { it.name == group.name}

		// Now, the group should validate
		if (!group.validate()) {
			group.errors.each { println it}
		}
		assert group.validate()

		// Create a subject and add it to the group
		def subject = SubjectTests.createSubject(study)
		assert subject
		study.addToSubjects(subject)
		assert study.save()
		group.addToSubjects(subject)

		// Make sure the group is saved to the database
		assert group.save(flush: true)

	}

	void testSave() {
		// Try to retrieve the group and make sure it's the same
		def groupDB = EventGroup.findByName(testEventGroupName)
		assert groupDB
		assert groupDB.name.equals(testEventGroupName)

		// A group without a name should not be saveable
		groupDB.name = null
		assert !groupDB.validate()

	}

	// This test is switched off, as event groups should be deleted via study.deleteEventGroup() and not directly
	void dontTestDelete() {
		def groupDB = EventGroup.findByName(testEventGroupName)
		assert groupDB

		groupDB.delete()
		try {
			groupDB.save()
			assert false // The save should not succeed since the group is referenced by a study
		}
		catch(org.springframework.dao.InvalidDataAccessApiUsageException e) {
			groupDB.discard()
			assert true // OK, correct exception (at least for the in-mem db, for PostgreSQL it's probably a different one...)
		}

		// Now, delete the group from the study groups collection, and then the delete action should be cascaded to the group itself
		def study = Study.findByTitle(testStudyName)
		assert study
		study.removeFromEventGroups groupDB

		// Strangely, calling study.save() gives an error here since the subject is not removed from the group?!!
		assert study.save()

		// Make sure the group doesn't exist anymore at this point
		assert !EventGroup.findByName(testEventGroupName)
		assert EventGroup.count() == 0
		assert study.eventGroups.size() == 0
	}

	void testDeleteViaStudy() {

		def groupDB = EventGroup.findByName(testEventGroupName)
		assert groupDB

		def study = Study.findByTitle(testStudyName)
		assert study
		def msg = study.deleteEventGroup(groupDB)
		println msg
		study.save()

		// Make sure the group doesn't exist anymore at this point
		assert !EventGroup.findByName(testEventGroupName)
		assert EventGroup.count() == 0
		assert study.eventGroups.size() == 0

	}

	void testParentStudy() {
		def group = EventGroup.findByName(testEventGroupName)
		assert group

		assert group.parent
		assert group.parent.code == testStudyCode
	}

	void testUniqueNameConstraint() {
		def group = EventGroup.findByName(testEventGroupName)
		assert group

		def study = group.parent
		assert study

		def group2 = new EventGroup(
		    name: testEventGroupName
		)

		// Add the group to the retrieved parent study
		study.addToEventGroups(group2)

		// At this point, the group should not validate or save, because there is already a group with that name in the study
		assert !group2.validate()
		assert !group2.save(flush:true)

	}

	protected void tearDown() {
		super.tearDown()
	}

}
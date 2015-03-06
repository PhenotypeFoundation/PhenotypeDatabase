/**
 *  GDT, a plugin for Grails Domain Templates
 *  Copyright (C) 2011 Jeroen Wesbeek, Kees van Bochove
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  $Author$
 *  $Rev$
 *  $Date$
 */

package org.dbnp.gdt

import grails.test.*


class TemplateFieldTests extends GrailsUnitTestCase {
	def testEvent;

	protected void setUp() {
		super.setUp()
		
		def RelTimeField = new TemplateField(
			name: 'testRelTime',
			type: TemplateFieldType.RELTIME
		);
	
		mockDomain( TemplateField, [RelTimeField] );
	
		def template = new Template(
			name: "Template",
			description: "For testing",
			entity: TestEntity
		);
	
		mockDomain( Template, [template] );
		
		template.addToFields( RelTimeField );

		testEvent = new TestEntity( template: template );
		
		mockDomain( TestEntity, [testEvent] );
		
		println testEvent.giveFields();
	}

	protected void tearDown() {
		super.tearDown()
	}

	void testInUse() {


	}

	void testRelTimeFieldCreation() {
		def RelTimeField = new TemplateField(
			name: 'RelTime',
			type: TemplateFieldType.RELTIME
		);
	}

	void testRelTimeSetValue() {
		// Check whether the field exists
		assert this.testEvent.fieldExists('testRelTime');

		// See that it is not a domain field
		assert !this.testEvent.isDomainField('testRelTime');
		println(this.testEvent.getStore(TemplateFieldType.RELTIME));

		/*
		this.testEvent.setFieldValue('testRelTime', 10);
		assert this.testEvent.getFieldValue('testRelTime') == 10;

		this.testEvent.setFieldValue('testRelTime', 0);
		assert this.testEvent.getFieldValue('testRelTime') == 0;

		this.testEvent.setFieldValue('testRelTime', -130);
		assert this.testEvent.getFieldValue('testRelTime') == -130;

		// RelTime must be able to handle 100 years
		long hundredYears = 100L * 365 * 24 * 3600;
		this.testEvent.setFieldValue('testRelTime', hundredYears);
		assert this.testEvent.getFieldValue('testRelTime') == hundredYears;
		*/
	}

	// Tests the parsing of a string for relative dates
	// @see TemplateEntity.setFieldValue
	//
	// The relative date may be set as a string, using the following format
	//
	//    #w #d #h #m #s
	//
	// Where w = weeks, d = days, h = hours, m = minutes, s = seconds
	//
	// The spaces between the values are optional. Every timespan
	// (w, d, h, m, s) must appear at most once. You can also omit
	// timespans if needed or use a different order.
	// Other characters are disregarded, allthough results may not
	// always be as expected.
	//
	// If an incorrect format is used, which can't be parsed
	// an IllegalFormatException is thrown.
	//
	// An empty span is treated as zero seconds.
	//
	// Examples:
	// ---------
	//    5d 3h 20m     // 5 days, 3 hours and 20 minutes
	//    6h 2d         // 2 days, 6 hours
	//    10m 200s      // 13 minutes, 20 seconds (200s == 3m + 20s)
	//    5w4h15m       // 5 weeks, 4 hours, 15 minutes
	//
	//    16x14w10d     // Incorrect. 16x is disregarded, so the
	//                  // result is 15 weeks, 3 days
	//    13days        // Incorrect: days should be d, but this is
	//                  // parsed as 13d, 0 seconds
	//

	void testRelTimeParser() {
		def s = 1L;
		def m = 60L * s;
		def h = 60L * m;
		def d = 24L * h;
		def w = 7L * d;
		/*
		this.testEvent.setFieldValue('testRelTime', '');
		assert this.testEvent.getFieldValue('testRelTime') == 0;

		this.testEvent.setFieldValue('testRelTime', '   ');
		assert this.testEvent.getFieldValue('testRelTime') == 0;

		this.testEvent.setFieldValue('testRelTime', '5d 3h 20m');
		assert this.testEvent.getFieldValue('testRelTime') == 5 * d + 3 * h + 20 * m;

		this.testEvent.setFieldValue('testRelTime', '6h 2d');
		assert this.testEvent.getFieldValue('testRelTime') == 2 * d + 6 * h;

		this.testEvent.setFieldValue('testRelTime', '10m 200s');
		assert this.testEvent.getFieldValue('testRelTime') == 10 * m + 200 * s;

		this.testEvent.setFieldValue('testRelTime', '5w4h15m');
		assert this.testEvent.getFieldValue('testRelTime') == 5 * w + 4 * h + 15 * m;

		// Should parse correctly, allthough it is not completely correct
		this.testEvent.setFieldValue('testRelTime', '16x14w10d');
		assert this.testEvent.getFieldValue('testRelTime') == 14 * w + 10 * d;

		this.testEvent.setFieldValue('testRelTime', '13days');
		assert this.testEvent.getFieldValue('testRelTime') == 13 * d;

		// Test whether an IllegalFormatException is thrown
		try {
			this.testEvent.setFieldValue('testRelTime', 'nonexistent relative date');
			fail();
		} catch (IllegalArgumentException ex) {
		} catch (Exception ex) {
			fail();
		}
		*/
	}

	void testContentEquals() {
		/*
		// Check whether the fields matter
		TemplateField tf1 = new TemplateField(entity: dbnp.studycapturing.Subject, name: 'Weight', type: TemplateFieldType.LONG, unit: 'kg', comments: 'Weight field')
		TemplateField tf2 = new TemplateField(entity: dbnp.studycapturing.Subject, name: 'Weight', type: TemplateFieldType.LONG, unit: 'kg', comments: 'Weight field 2')
		TemplateField tf3 = new TemplateField(entity: dbnp.studycapturing.Subject, name: 'Length', type: TemplateFieldType.LONG, unit: 'm', comments: 'Length field')
		TemplateField tf4 = new TemplateField(entity: dbnp.studycapturing.Subject, name: 'Length', type: TemplateFieldType.LONG, unit: 'm', comments: 'Length field', required: true)
		TemplateField tf5 = new TemplateField(entity: dbnp.studycapturing.Study, name: 'Length', type: TemplateFieldType.LONG, unit: 'm', comments: 'Length field', required: true)

		TemplateField tf6 = new TemplateField(entity: dbnp.studycapturing.Subject, name: 'Species', type: TemplateFieldType.ONTOLOGYTERM)
		TemplateField tf7 = new TemplateField(entity: dbnp.studycapturing.Subject, name: 'Species', type: TemplateFieldType.ONTOLOGYTERM)

		TemplateField tf8 = new TemplateField(entity: dbnp.studycapturing.Subject, name: 'Species', type: TemplateFieldType.STRINGLIST)
		TemplateField tf9 = new TemplateField(entity: dbnp.studycapturing.Subject, name: 'Species', type: TemplateFieldType.STRINGLIST)

		mockDomain(TemplateField, [tf1, tf2, tf3, tf4, tf5, tf6, tf7, tf8, tf9]);

		assert (tf1.contentEquals(tf1));
		assert (tf1.contentEquals(tf2));
		assert (tf2.contentEquals(tf1));
		assert (!tf1.contentEquals(tf3));
		assert (!tf3.contentEquals(tf4));
		assert (!tf5.contentEquals(tf4));

		// Test ontology fields
		Ontology o1 = new Ontology(ncboId: 1000, ncboVersionedId: 14192, name: "Ontology 1")
		Ontology o2 = new Ontology(ncboId: 1000, ncboVersionedId: 14192, name: "Ontology 2")
		Ontology o3 = new Ontology(ncboId: 1000, ncboVersionedId: 5123, name: "Ontology 3")
		Ontology o4 = new Ontology(ncboId: 4123, ncboVersionedId: 14192, name: "Ontology 4")

		tf6.addToOntologies(o1)

		// Different number of ontologies
		assert (!tf6.contentEquals(tf7));

		tf7.addToOntologies(o1);

		// Same ontologies
		assert (tf6.contentEquals(tf7));

		tf7.ontologies.clear()
		tf7.addToOntologies(o2);

		// Ontologies with the same ncboId
		assert (!tf6.contentEquals(tf7));

		tf6.ontologies.clear(); tf7.ontologies.clear()
		tf6.addToOntologies(o1)
		tf6.addToOntologies(o4)
		tf7.addToOntologies(o4);
		tf7.addToOntologies(o1);

		// Different order but same ontologies
		assert (tf6.contentEquals(tf7));

		// Test listentries

		assert (tf8.contentEquals(tf9));

		TemplateFieldListItem l1 = new TemplateFieldListItem(name: 'string1');
		TemplateFieldListItem l2 = new TemplateFieldListItem(name: 'string1');
		TemplateFieldListItem l3 = new TemplateFieldListItem(name: 'string2');
		TemplateFieldListItem l4 = new TemplateFieldListItem(name: 'string3');

		tf8.addToListEntries(l1);

		// Different number of list entries
		assert (!tf8.contentEquals(tf9));

		tf9.addToListEntries(l1);

		// Same list entries
		assert (tf8.contentEquals(tf9));

		tf9.listEntries.clear();
		tf9.addToListEntries(l2);

		// Different list entries with the same name
		assert (tf8.contentEquals(tf9));

		tf9.listEntries.clear();
		tf9.addToListEntries(l3);

		// Different list entries
		assert (!tf8.contentEquals(tf9));

		// Same entries but different order
		tf8.listEntries.clear();
		tf9.listEntries.clear();
		tf8.addToListEntries(l2);
		tf8.addToListEntries(l3);
		tf9.addToListEntries(l3);
		tf9.addToListEntries(l2);

		// Different order but same list entries
		assert (tf8.contentEquals(tf9));
        */
	}
}

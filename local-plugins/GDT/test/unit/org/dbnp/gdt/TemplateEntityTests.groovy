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

class TemplateEntityTests extends GrailsUnitTestCase {

	def testStudy;
	def template;
	def eventTemplate2;
	def sampleTemplate1;

	protected void setUp() {
		super.setUp();
		
		template = new Template(
			name: "Template",
			description: "For testing",
			entity: TestEntity
		)

		mockDomain( Template, [template] );
		
		testStudy = new TestEntity( template: template );
		mockDomain( TestEntity, [testStudy] );
		
	}

	protected void tearDown() {
		super.tearDown()
	}

	void testGiveTemplates() {
		def eventTemplates = TestEntity.giveTemplates([testStudy]);
		assert eventTemplates.size() == 1
		assert eventTemplates.contains(template);
	}

	/** *
	 *  This test assures that adding of a TemplateField that has another entity than the Template fails.
	 */
	void testTemplateFieldEntity() {
		template.addToFields(
			new TemplateField(name: 'Should fail',type: TemplateFieldType.STRING, entity: Test2Entity)
		)
		assert !template.validate()
		assert !template.save()
	}


}

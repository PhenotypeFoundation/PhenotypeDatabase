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

class TemplateTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testInUse() {
		Template t1 = new Template( entity: TestEntity );
		Template t2 = new Template( entity: TestEntity );
		Template t3 = new Template( entity: TestEntity );

		TestEntity s1 = new TestEntity( template: t1 );
		TestEntity s2 = new TestEntity( template: t1 );
		TestEntity s3 = new TestEntity( template: t2 );

		mockDomain(Template, [t1, t2, t3 ])
		mockDomain(TestEntity, [ s1, s2, s3] )

		t1.gdtService = new GdtService();
		t2.gdtService = new GdtService();
		t3.gdtService = new GdtService();
		
		//assert t1.inUse();
		//assert t2.inUse();
		//assert !t3.inUse();
    }

    void testContentEquals() {
		// Basic checks
		Template t1 = new Template( entity: TestEntity );
		Template t2 = new Template( entity: TestEntity );
		Template t3 = new Template( entity: Test2Entity );

		mockDomain( Template, [t1, t2, t3] );

		assert t1.contentEquals( t1 );
		assert t1.contentEquals( t2 );
		assert t2.contentEquals( t1 );
		assert !t3.contentEquals( t1 );
		assert !t2.contentEquals( t3 );

		// Check whether other fields matter
		t1.name = "Test 1";
		t1.description = "Long description"
		t2.name = "Test 2";
		t2.description = "Short description"

		assert( t1.contentEquals( t2 ) );

		// Check whether the fields matter
		TemplateField tf1 = new TemplateField( entity: TestEntity, name: 'Weight', type: TemplateFieldType.LONG,  unit: 'kg', comment: 'Weight field' )
		TemplateField tf2 = new TemplateField( entity: TestEntity, name: 'Weight', type: TemplateFieldType.LONG,  unit: 'kg', comment: 'Weight field 2' )
		TemplateField tf3 = new TemplateField( entity: TestEntity, name: 'Length', type: TemplateFieldType.LONG,  unit: 'm', comment: 'Length field' )
		TemplateField tf4 = new TemplateField( entity: TestEntity, name: 'BMI', type: TemplateFieldType.LONG,  unit: 'kg/m2', comment: 'BMI field', required: true )

		mockDomain( TemplateField, [tf1, tf2, tf3, tf4] );

		t1.addToFields( tf1 );
		t2.addToFields( tf1 );

		// Same fields
		assert( t1.contentEquals( t2 ) );

		t1.fields.clear();
		t1.addToFields( tf2 );

		// Fields with equal properties
		assert( t1.contentEquals( t2 ) );

		t1.fields.clear();
		t1.addToFields( tf3 );

		// Fields with inequal properties
		assert( !t1.contentEquals( t2 ) );

		t1.addToFields( tf1 );
		t2.addToFields( tf3 );

		// Same fields in different order
		assert( t1.contentEquals( t2 ) );

		t1.addToFields( tf4 )

		// Different number of fields
		assert( !t1.contentEquals( t2 ) );

    }

}

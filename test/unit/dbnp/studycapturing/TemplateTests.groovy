package dbnp.studycapturing

import grails.test.*
import dbnp.authentication.*

class TemplateTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testInUse() {
		Template t1 = new Template( entity: dbnp.studycapturing.Subject );
		Template t2 = new Template( entity: dbnp.studycapturing.Subject );
		Template t3 = new Template( entity: dbnp.studycapturing.Subject );

		Subject s1 = new Subject( template: t1 );
		Subject s2 = new Subject( template: t1 );
		Subject s3 = new Subject( template: t2 );

		mockDomain(Template, [t1, t2, t3 ])
		mockDomain(Subject, [ s1, s2, s3] )

		assert t1.inUse();
		assert t2.inUse();
		assert !t3.inUse();
    }

    void testContentEquals() {
		// Basic checks
		Template t1 = new Template( entity: dbnp.studycapturing.Subject );
		Template t2 = new Template( entity: dbnp.studycapturing.Subject );
		Template t3 = new Template( entity: dbnp.studycapturing.Study );

		mockDomain( Template, [t1, t2, t3] );

		assert t1.contentEquals( t1 );
		assert t1.contentEquals( t2 );
		assert t2.contentEquals( t1 );
		assert !t3.contentEquals( t1 );
		assert !t2.contentEquals( t3 );

		// Check whether other fields matter
		t1.name = "Test 1";
		t1.description = "Long description"
		t1.owner = new SecUser( username: "TestUser" )
		t2.name = "Test 2";
		t2.description = "Short description"

		assert( t1.contentEquals( t2 ) );

		// Check whether the fields matter
		TemplateField tf1 = new TemplateField( entity: dbnp.studycapturing.Subject, name: 'Weight', type: TemplateFieldType.LONG,  unit: 'kg', comment: 'Weight field' )
		TemplateField tf2 = new TemplateField( entity: dbnp.studycapturing.Subject, name: 'Weight', type: TemplateFieldType.LONG,  unit: 'kg', comment: 'Weight field 2' )
		TemplateField tf3 = new TemplateField( entity: dbnp.studycapturing.Subject, name: 'Length', type: TemplateFieldType.LONG,  unit: 'm', comment: 'Length field' )
		TemplateField tf4 = new TemplateField( entity: dbnp.studycapturing.Subject, name: 'BMI', type: TemplateFieldType.LONG,  unit: 'kg/m2', comment: 'BMI field', required: true )

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

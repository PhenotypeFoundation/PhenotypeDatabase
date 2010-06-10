package dbnp.studycapturing

import grails.test.*

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
}

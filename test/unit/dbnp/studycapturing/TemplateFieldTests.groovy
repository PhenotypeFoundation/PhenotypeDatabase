package dbnp.studycapturing

import grails.test.*

class TemplateFieldTests extends GrailsUnitTestCase {
    def testEvent;
    protected void setUp() {
        super.setUp()

        // Create the template itself
        def testTemplate = new Template(
                name: 'Template for testing relative date fields',
                entity: dbnp.studycapturing.Event,
                fields: [
                    new TemplateField(
                        name: 'testStartDate',
                        type: TemplateFieldType.DATE
                    ),
                    new TemplateField(
                        name: 'testRelTime',
                        type: TemplateFieldType.RELTIME
                    )
                ]
            );

        this.testEvent = new Event(
                template: testTemplate,
                startTime: 3600,
                endTime: 7200
        )
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testRelTimeFieldCreation() {
        def RelTimeField = new TemplateField(
                name: 'RelTime',
                type: TemplateFieldType.RELTIME
        );
    }

    void testRelTimeSetValue() {
        // Check whether the field exists
        assert this.testEvent.fieldExists( 'testRelTime' );

        // See that it is not a domain field
        assert !this.testEvent.isDomainField( 'testRelTime' );
        println( this.testEvent.getStore( TemplateFieldType.RELTIME ) );
        
        this.testEvent.setFieldValue( 'testRelTime', 10 );
        assert this.testEvent.getFieldValue( 'testRelTime' ) == 10;

        this.testEvent.setFieldValue( 'testRelTime', 0 );
        assert this.testEvent.getFieldValue( 'testRelTime' ) == 0;

        this.testEvent.setFieldValue( 'testRelTime', -130 );
        assert this.testEvent.getFieldValue( 'testRelTime' ) == -130;

        // RelTime must be able to handle 100 years
        long hundredYears = 100L * 365 * 24 * 3600;
        this.testEvent.setFieldValue( 'testRelTime', hundredYears );
        assert this.testEvent.getFieldValue( 'testRelTime' ) ==  hundredYears;
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

        this.testEvent.setFieldValue( 'testRelTime', '' );
        assert this.testEvent.getFieldValue( 'testRelTime' ) == 0;

        this.testEvent.setFieldValue( 'testRelTime', '   ' );
        assert this.testEvent.getFieldValue( 'testRelTime' ) == 0;

        this.testEvent.setFieldValue( 'testRelTime', '5d 3h 20m' );
        assert this.testEvent.getFieldValue( 'testRelTime' ) == 5 * d + 3 * h + 20 * m;

        this.testEvent.setFieldValue( 'testRelTime', '6h 2d' );
        assert this.testEvent.getFieldValue( 'testRelTime' ) == 2 * d + 6 * h;

        this.testEvent.setFieldValue( 'testRelTime', '10m 200s' );
        assert this.testEvent.getFieldValue( 'testRelTime' ) == 10 * m  + 200 * s;

        this.testEvent.setFieldValue( 'testRelTime', '5w4h15m' );
        assert this.testEvent.getFieldValue( 'testRelTime' ) == 5 * w + 4 * h + 15 * m;

        // Should parse correctly, allthough it is not completely correct
        this.testEvent.setFieldValue( 'testRelTime', '16x14w10d' );
        assert this.testEvent.getFieldValue( 'testRelTime' ) == 14 * w + 10 * d;

        this.testEvent.setFieldValue( 'testRelTime', '13days' );
        assert this.testEvent.getFieldValue( 'testRelTime' ) == 13 * d;

        // Test whether an IllegalFormatException is thrown
        try {
            this.testEvent.setFieldValue( 'testRelTime', 'nonexistent relative date' );
            fail();
        } catch(IllegalArgumentException ex) {
        } catch( Exception ex ) {
            fail();
        }

    }
}

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

class RelTimeTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testRelTimeCreation() {
        def r = new RelTime();
        assert r.getValue() == 0;
        
        r = new RelTime( 2000 );
        assert r.getValue() == 2000;

        r = new RelTime( "3h 20m" );
        assert r.getValue() == 3 * 3600 + 20 * 60;
    }

    void testRelTimeSetAndGet() {
        def r = new RelTime();

        r.setValue( 1000 );
        assert r.getValue() == 1000;

        r.setValue( -1000 );
        assert r.getValue() == -1000;

        r.setValue(0);
        assert r.getValue() == 0;

        long century = 100L * 365 * 24 * 3600;
        r.setValue( century );
        assert r.getValue() == century;
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

        def r = new RelTime();

        r.setValue( '' );
        assert r.getValue() == 0;

        r.setValue( '-' );
        assert r.getValue() == 0;

        r.setValue( '  ' );
        assert r.getValue() == 0;

        r.setValue( '5d 3h 20m' );
        assert r.getValue() == 5 * d + 3 * h + 20 * m;
        assert r.toString() == '5d 3h 20m';

        r.setValue( '6h 2d' );
        assert r.getValue() == 2 * d + 6 * h;
        assert r.toString() == '2d 6h';

        r.setValue( '10m 200s' );
        assert r.getValue() == 10 * m  + 200 * s;
        assert r.toString() == '13m 20s';

        r.setValue( '1w 1s' );
        assert r.getValue() == 1 * w + 1 * s;
        assert r.toString() == '1w 1s';

        // Test string without spaces
        r.setValue( '5w4h15m' );
        assert r.getValue() == 5 * w + 4 * h + 15 * m;
        assert r.toString() == '5w 4h 15m';

        // Test string with spaces between number and character
        r.setValue( '5 w 4 h 15 m' );
        assert r.getValue() == 5 * w + 4 * h + 15 * m;
        assert r.toString() == '5w 4h 15m';

        r.setValue( '5 weeks, 4 hours, 15 minutes' );
        assert r.getValue() == 5 * w + 4 * h + 15 * m;
        assert r.toString() == '5w 4h 15m';

        // Test strings with commas
        r.setValue( '5w, 4h, 15m' );
        assert r.getValue() == 5 * w + 4 * h + 15 * m;
        assert r.toString() == '5w 4h 15m';

        r.setValue( '5w,4h,15m' );
        assert r.getValue() == 5 * w + 4 * h + 15 * m;
        assert r.toString() == '5w 4h 15m';

        // Should parse correctly, allthough it is not completely correct
        r.setValue( '5weeks, 4hours,15minutes' );
        assert r.getValue() == 5 * w + 4 * h + 15 * m;
        assert r.toString() == '5w 4h 15m';

        r.setValue( '16x14w10d' );
        assert r.getValue() == 14 * w + 10 * d;
        assert r.toString() == '15w 3d';

        r.setValue( '13days' );
        assert r.getValue() == 13 * d;
        assert r.toString() == '1w 6d';

        // Test whether an IllegalFormatException is thrown
        try {
            r.setValue( 'nonexistent relative date' );
            fail();
        } catch(IllegalArgumentException ex) {
        } catch( Exception ex ) {
            fail();
        }

    }

    void testNegativeRelTimes() {
        def s = 1L;
        def m = 60L * s;
        def h = 60L * m;
        def d = 24L * h;
        def w = 7L * d;

        def r = new RelTime();

        r.setValue( -1000 );
        assert r.getValue() == -1000;

        r.setValue( -10 );
        assert r.getValue() == -10;
        assert r.toString() == "-10s"

        r.setValue(-11 * d - 4 * h);
        assert r.getValue() ==  -11* d - 4 * h;
        assert r.toString() == "-1w 4d 4h";

        r.setValue( "-5w 10d 4h" );
        assert r.getValue() ==-5 * w - 10 * d - 4 * h;
        assert r.toString() == '-6w 3d 4h';

        r.setValue( "-1w 200m 1d" );
        assert r.getValue() == -1 * w -200 * m - 1 * d;
        assert r.toString() == '-1w 1d 3h 20m';

    }

    void testComputeDifference() {
        def s = 1L;
        def m = 60L * s;
        def h = 60L * m;
        def d = 24L * h;
        def w = 7L * d;

        def r = new RelTime();

        r.computeDifference(
            new Date( 2010, 1, 1, 10, 0, 0 ),
            new Date( 2010, 1, 1, 20, 10, 0 )
        );
        assert r.getValue() == 10 * h + 10 * m;

        r.computeDifference(
            new Date( 2010, 1, 1, 10, 0, 0 ),
            new Date( 2011, 1, 1, 10, 10, 0 )
        );
        assert r.getValue() == 365 * d + 10 * m;
        assert r.toString() == "52w 1d 10m";

        // Test leap year
        r.computeDifference(
            new Date( 2008, 1, 1, 10, 0, 0 ),
            new Date( 2009, 1, 1, 10, 0, 0 )
        );
        assert r.getValue() == 366 * d;

        // Test daylight saving time. Dates are expected to be UTC
        // Mar 27 - Mar 29
        r.computeDifference(
            new Date( 2010, 2, 27, 10, 0, 0 ),
            new Date( 2010, 2, 29, 10, 0, 0 )
        );
        assert r.getValue() == 2 * d;

        // Test daylight saving time. Dates are expected to be UTC
        // Oct 30 - Nov 1
        r.computeDifference(
            new Date( 2010, 9 , 30, 10, 0, 0 ),
            new Date( 2010, 10,  1, 10, 0, 0 )
        );
        assert r.getValue() == 2 * d;

        // Test negative differences
        r.computeDifference(
            new Date( 2010, 1, 2, 10, 0, 0 ),
            new Date( 2010, 1, 1, 10, 0, 0 )
        );
        assert r.getValue() == -1 * d;
        assert r.toString() == "-1d";

    }

    void testPrettyString() {
        def r = new RelTime();

        r.setValue( "5w 10d 4h 3m 10s" );
        assert r.toString() == '6w 3d 4h 3m 10s';
        assert r.toPrettyString() == '6 weeks, 3 days, 4 hours, 3 minutes, 10 seconds';
        assert r.toPrettyRoundedString() == '6 weeks';

        r.setValue( "1w 30s" );
        assert r.toString() == '1w 30s';
        assert r.toPrettyString() == '1 week, 30 seconds';
        assert r.toPrettyRoundedString() == '1 week';

        r.setValue( "" );
        assert r.toString() == '0s';
        assert r.toPrettyString() == '0 seconds';
        assert r.toPrettyRoundedString() == '0 seconds';

        r.setValue( -1000 );
        assert r.toPrettyString() == "-16 minutes, 40 seconds";
        assert r.toPrettyRoundedString() == "-16 minutes";

    }
}

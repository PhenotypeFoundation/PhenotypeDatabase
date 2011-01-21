package dbnp.query

import dbnp.studycapturing.RelTime
import dbnp.studycapturing.TemplateFieldType;
import grails.test.*

/**
 * SearchTests Test
 *
 * Description of my test
 *
 * @author  your email (+name?)
 * @since	2010mmdd
 * @package	???
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class SearchTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
		mockLogging( Search );
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testPrepare() {
		// Test prepare method for dates
		Date d = Date.parse( "yyyy-MM-dd", "1982-10-21" );
		assert !d.equals( Search.prepare( "21-10-1982", TemplateFieldType.DATE ) )
		assert !d.equals( Search.prepare( "21-10-82", TemplateFieldType.DATE ) )
		assert d.equals( Search.prepare( "1982-10-21 00:00:00", TemplateFieldType.DATE ) )
		assert d.equals( Search.prepare( "1982-10-21", TemplateFieldType.DATE ) )
		
		// Test prepare method for RelTime
		assertEquals new RelTime( 0 ), Search.prepare( "", TemplateFieldType.RELTIME )
		assertEquals new RelTime( 3600 ), Search.prepare( "1h", TemplateFieldType.RELTIME )
		assertEquals new RelTime( 3600 ), Search.prepare( "60m", TemplateFieldType.RELTIME )
		assertEquals new RelTime( 3600 ), Search.prepare( "3600", TemplateFieldType.RELTIME )
		assertEquals "abc", Search.prepare( "abc", TemplateFieldType.RELTIME )
		
		// Test prepare method for Double
		assertEquals new Double( 100.1 ), Search.prepare( "100.1", TemplateFieldType.DOUBLE )
		assertEquals new Double( 0.0 ), Search.prepare( "0", TemplateFieldType.DOUBLE )
		assertEquals new Double( 100.0 ), Search.prepare( "100", TemplateFieldType.DOUBLE )
		assertEquals new Double( -100.1 ), Search.prepare( "-100.1", TemplateFieldType.DOUBLE )
		
		// Test prepare method for Long
		assertEquals new Long( 100 ), Search.prepare( "100", TemplateFieldType.LONG )
		assertEquals new Long( 0 ), Search.prepare( "0", TemplateFieldType.LONG )
		assertEquals new Long( -100 ), Search.prepare( "-100", TemplateFieldType.LONG )
		assertEquals "3.8", Search.prepare( "3.8", TemplateFieldType.LONG )	// Can't be parsed into long
		assertEquals "4.2", Search.prepare( "4.2", TemplateFieldType.LONG )	// Can't be parsed into long
		
		// Test prepare method for Boolean
		assertEquals Boolean.TRUE, Search.prepare( "true", TemplateFieldType.BOOLEAN )
		assertEquals Boolean.FALSE, Search.prepare( "false", TemplateFieldType.BOOLEAN )
		assertEquals Boolean.FALSE, Search.prepare( "", TemplateFieldType.BOOLEAN )
		assertEquals Boolean.FALSE, Search.prepare( "test", TemplateFieldType.BOOLEAN )
    }

}

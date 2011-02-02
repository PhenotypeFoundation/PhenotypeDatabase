package dbnp.query

import grails.test.*
import org.dbnp.gdt.RelTime

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
class CriterionTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
		
		mockLogging( Criterion );
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testOperators() {
		def crit = new Criterion( entity: "Study", field: "title", operator: Operator.equals, value: "abc" );
		
		assert crit.match( "abc" )
		assert crit.match( "aBC" )	// Should check case insensitive
		assert !crit.match( "a" )
		assert !crit.match( "cba" )
		assert !crit.match( "klm" )
		
		crit.operator = Operator.contains
		
		assert crit.match( "abcdef" )
		assert crit.match( "DEAbCde" )	// Should check case insensitive
		assert crit.match( "abc" )
		assert !crit.match( "h" )
		assert !crit.match( "bc" )
		
		crit.operator = Operator.gt
		
		assert crit.match( "b" )
		assert crit.match( "B" )	// Should check case insensitive
		assert crit.match( "klm" )
		assert !crit.match( "abc" )
		assert !crit.match( "aaa" )
		assert !crit.match( "a" )
		
		crit.operator = Operator.gte
		
		assert crit.match( "b" )
		assert crit.match( "B" )	// Should check case insensitive
		assert crit.match( "klm" )
		assert crit.match( "abc" )
		assert !crit.match( "aaa" )
		assert !crit.match( "a" )

		crit.operator = Operator.lt
		
		assert crit.match( "aaa" )
		assert crit.match( "ABa" )	// Should check case insensitive
		assert crit.match( "a" )
		assert !crit.match( "abc" )
		assert !crit.match( "klm" )
		assert !crit.match( "c" )
		
		crit.operator = Operator.lte
		
		assert crit.match( "aaa" )
		assert crit.match( "ABa" )	// Should check case insensitive
		assert crit.match( "a" )
		assert crit.match( "abc" )
		assert !crit.match( "klm" )
		assert !crit.match( "c" )
    }
	
	void testTypes() {
		Date d = Date.parse( "yyyy-MM-dd", "1982-10-10" );
		Date dt = Date.parse( "yyyy-MM-dd HH:mm:ss", "1982-10-10 10:10:30" );
		
		def crit = new Criterion( entity: "Study", field: "title", operator: Operator.equals, value: "abc" );
		
		assert crit.match( "abc" )
		assert !crit.match( 0 );
		assert !crit.match( d );
		assert !crit.match( null );
		assert !crit.match( "" );
		
		crit.value = "1.0";
		
		assert crit.match( 1.0d )
		assert crit.match( 1 )
		assert crit.match( "1.0" )
		assert !crit.match( 2 )
		assert !crit.match( -1.0 )
		assert !crit.match( "4" )
		assert !crit.match( true )
		
		crit.value = "1";
		
		assert crit.match( 1.0d )
		assert crit.match( 1 )
		assert crit.match( "1" )
		assert !crit.match( "1.0" )
		assert !crit.match( 2 )
		assert !crit.match( -1.0 )
		assert !crit.match( "4" )
		assert !crit.match( true )

		crit.value = "1982-10-10";
		
		assert crit.match( d )
		assert crit.match( dt )
		assert !crit.match( d.plus(1) )
		assert !crit.match( 2 )
		assert !crit.match( -1.0 )
		assert !crit.match( "10-10-1982" )
		assert crit.match( "1982-10-10" )
		assert !crit.match( true )
		
		crit.value = "true";
		
		assert crit.match( true )
		assert !crit.match( false )
		assert !crit.match( d )
		assert !crit.match( 2 )
		assert !crit.match( -1.0 )
		assert !crit.match( "10-10-1982" )

		crit.value = "1d";
		
		assert crit.match( new RelTime( "1d" ) )
		assert crit.match( new RelTime( 86400 ) )
		assert !crit.match( new RelTime( "1h" ) )
		assert !crit.match( 86400 )
		assert !crit.match( -1.0 )
		assert !crit.match( "10-10-1982" )

		crit.value = "86400";
		
		assert crit.match( new RelTime( "1d" ) )
		assert crit.match( new RelTime( 86400 ) )
		assert crit.match( 86400 )
		assert !crit.match( new RelTime( "1h" ) )
		assert !crit.match( -1.0 )
		assert !crit.match( "10-10-1982" )
	}
}

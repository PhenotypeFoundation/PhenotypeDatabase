package dbnp.studycapturing

import grails.test.*

class StudyTests extends GrailsUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    protected void assertCollectionEquals( Collection set1, Collection set2 ) {
        set1.each {
            assert it in set2;
        }
        set2.each {
            assert it in set1;
        }

    }

    void testGetOrphanEvents() {
        def events = [];
        10.times {
            events[ it ] = new Event(
                id: it,
                startTime: 3600,
                endTime: 3800
            )
            println( 'Created event ' + it );
        }
        def evGroup1 = new EventGroup(
            name: 'group1',
            events: events[0..4]
        )
        def evGroup2 = new EventGroup(
            name: 'group2',
            events: events[3..6]
        )

        // No events should give no orphan events
        def study1 = new Study( title: 'Studytitle 1', events: [], eventGroups: [] );
        assert study1.getOrphanEvents().size() == 0;

        // Not even with a group
        study1.eventGroups = [ evGroup1 ];
        assert study1.getOrphanEvents().size() == 0;

        // Events 0..4 are part of evGroup1
        study1.events = events[0..8];
        println(  study1.getOrphanEvents().id )
        println(  events[ 5..8].id )
        
        assertCollectionEquals( study1.getOrphanEvents(), events[ 5..8 ] );

        // Remove the evGroup
        study1.eventGroups = [];
        assertCollectionEquals( study1.getOrphanEvents(), events[ 0..8 ] );

        // Add multiple groups
        study1.eventGroups = [ evGroup1, evGroup2 ];
        assertCollectionEquals( study1.getOrphanEvents(), events[ 7..8 ] );

        // Remove events again
        study1.events = [];
        assert study1.getOrphanEvents().size() == 0;
    }
}

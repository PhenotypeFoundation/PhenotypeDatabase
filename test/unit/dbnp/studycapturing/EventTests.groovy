package dbnp.studycapturing

import grails.test.*

class EventTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testBelongsToGroup() {
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

        Set<EventGroup> set1 = new HashSet<EventGroup>();
        Set<EventGroup> set2 = new HashSet<EventGroup>();
        Set<EventGroup> setBoth = new HashSet<EventGroup>();
        Set<EventGroup> setEmpty = new HashSet<EventGroup>();
        set1.add( evGroup1 );
        set2.add( evGroup2 );
        setBoth.add( evGroup1 );
        setBoth.add( evGroup2 );

        assert events[0].belongsToGroup( set1 );
        assert events[3].belongsToGroup( set1 );
        assert !events[6].belongsToGroup( set1 );

        assert events[3].belongsToGroup( set2 );
        assert events[6].belongsToGroup( set2 );

        assert !events[0].belongsToGroup( setEmpty );
        assert !events[8].belongsToGroup( setEmpty );

        assert events[0].belongsToGroup( setBoth );
        assert events[3].belongsToGroup( setBoth );
        assert events[6].belongsToGroup( setBoth );

        assert !events[7].belongsToGroup( setBoth );

    }
}

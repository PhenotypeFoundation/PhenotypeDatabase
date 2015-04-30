package org.dbnp.gdtimporter

import grails.test.GrailsUnitTestCase

class GdtImporterServiceTests extends GrailsUnitTestCase {

    void testGenerateUniqueString() {

        def gdtImporterService = new GdtImporterService()
        
        assert gdtImporterService.generateUniqueString('a', ['a (0)', 'a (1)', 'a (3)']) == 'a (4)'
        assert gdtImporterService.generateUniqueString('a', ['a (0)', 'a (1)', 'a (3)'], true) == 'a'

        assert gdtImporterService.generateUniqueString('a', ['a', 'a (1)', 'a (3)'], true) == 'a (2)'

        // ignore iterators 0 and 1
        assert gdtImporterService.generateUniqueString('a', ['a (0)', 'a (1)'], true) == 'a'

        assert gdtImporterService.generateUniqueString('a', []) == 'a'
        assert gdtImporterService.generateUniqueString('a', [], true) == 'a'

        assert gdtImporterService.generateUniqueString('a', null) == 'a'
        assert gdtImporterService.generateUniqueString('a', null, true) == 'a'

    }

}

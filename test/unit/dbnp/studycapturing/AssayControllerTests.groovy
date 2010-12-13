package dbnp.studycapturing

import grails.test.*

import dbnp.data.Term

/**
 * AssayControllerTests Test
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
class AssayControllerTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()

        mockDomain(Term,          [ new Term(id: 1, name: 'Human')])

        mockDomain(TemplateField, [ new TemplateField(id: 1, name: 'tf1', type: TemplateFieldType.STRING),
                                    new TemplateField(id: 2, name: 'tf2', type: TemplateFieldType.STRING),
                                    new TemplateField(id: 3, name: 'tf3', type: TemplateFieldType.STRING)])

        mockDomain(Template,      [ new Template(id: 1, fields: [TemplateField.get(1), TemplateField.get(2)]),
                                    new Template(id: 2, fields: [TemplateField.get(3)])])

        mockDomain(Subject,       [ new Subject(id: 1, name:'subject1', template: Template.get(1), species: Term.get(1)),
                                    new Subject(id: 2, name:'subject2', template: Template.get(2), species: Term.get(1))])

        mockDomain(SamplingEvent, [ new SamplingEvent(id:1, startTime: 2, duration: 5, sampleTemplate: new Template())])

        mockDomain(Event,         [ new Event(id: 1, startTime: 6, endTime: 7)])//, new Event(id: 2, startTime: 8, endTime: 9)])

        mockDomain(EventGroup,    [ new EventGroup(id:1, events: [Event.get(1)]) ])

        mockDomain(Sample,        [ new Sample(id: 1, name:'sample1', parentSubject: Subject.get(1), parentEvent: SamplingEvent.get(1), parentEventGroup: EventGroup.get(1)),
                                    new Sample(id: 2, name:'sample2', parentSubject: Subject.get(2), parentEvent: SamplingEvent.get(1))])

        mockDomain(Assay,         [ new Assay(id: 1, samples:[Sample.get(1),Sample.get(2)]),
                                    new Assay(id: 2, samples:[])])

        Subject.get(1).setFieldValue('tf1', 'tfv1')
        Subject.get(1).setFieldValue('tf2', 'tfv2')
        Subject.get(2).setFieldValue('tf3', 'tfv3')
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testWrongAssayID() {
        mockParams.id = 3

        controller.exportAssayAsExcel()

        assertEquals 'Redirected action should match', [action: 'selectAssay'], redirectArgs
        assertEquals 'Error message', 'No assay found with id: 3.', mockFlash.errorMessage
    }

    void testExceptionHandling() {
        mockParams.id = 1

        controller.assayService = [exportAssayDataAsExcelFile:{throw new Exception('msg')}]
        controller.exportAssayAsExcel()

        assertEquals 'Redirected action should match', [action: 'selectAssay'], redirectArgs
        assertEquals 'Error message', 'msg', mockFlash.errorMessage
    }

    void testEmptySampleList() {
        mockParams.id = 2

        def passedAssayData = []

        controller.assayService = [exportAssayDataAsExcelFile:{a -> passedAssayData = a}]
        controller.exportAssayAsExcel()

        assertEquals 'Assay data', [], passedAssayData*.value.flatten()
    }

    void testTemplateFieldsAreCollected() {

        mockParams.id = 1

        Map passedAssayData

        controller.assayService = [exportAssayDataAsExcelFile:{a -> passedAssayData = a}]
        controller.exportAssayAsExcel()

        def sample1index = passedAssayData.'Sample Data'.'name'.findIndexOf{it == 'sample1'}
        def sample2index = passedAssayData.'Sample Data'.'name'.findIndexOf{it == 'sample2'}

        assertEquals 'Subject template field', ['tfv1',''], passedAssayData.'Subject Data'.tf1[sample1index, sample2index]
        assertEquals 'Subject template field', ['tfv2',''], passedAssayData.'Subject Data'.tf2[sample1index, sample2index]
        assertEquals 'Subject template field', ['','tfv3'], passedAssayData.'Subject Data'.tf3[sample1index, sample2index]
        assertEquals 'Subject species template field', ['Human', 'Human'], passedAssayData.'Subject Data'.species*.toString()
        assertEquals 'Subject name template field', ['subject1','subject2'], passedAssayData.'Subject Data'.name[sample1index, sample2index]

        assertEquals 'Sampling event template fields', [2], passedAssayData.'Sampling Event Data'.startTime
        assertEquals 'Sampling event template fields', [5], passedAssayData.'Sampling Event Data'.duration
        assertEquals 'Sampling event template fields', '[null]', passedAssayData.'Sampling Event Data'.sampleTemplate.toString()
        assertEquals 'Event template fields', [6], passedAssayData.'Event Data'.startTime
        assertEquals 'Event template fields', [7], passedAssayData.'Event Data'.endTime
        assertEquals 'Sample template fields', ['sample1', 'sample2'], passedAssayData.'Sample Data'.name[sample1index, sample2index]
    }
}

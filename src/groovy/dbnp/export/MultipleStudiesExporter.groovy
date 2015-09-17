package dbnp.export

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.DataFormatter

import dbnp.studycapturing.*
import org.dbnp.gdt.*
import dbnp.authentication.SecUser

import grails.util.Holders

/**
 * Exporter to export multiple studies/assay into a single excel file
 * There will be three sheets: one for study data, one for subject data and one for sample data
 */
public class MultipleStudiesExporter implements Exporter {
    @Lazy
    def assayService = {
        Holders.grailsApplication.getMainContext().getBean("assayService")
    }()
    
    @Lazy
    def authenticationService = {
        Holders.grailsApplication.getMainContext().getBean("authenticationService")
    }()
    
    @Lazy
    def apiService = {
        Holders.grailsApplication.getMainContext().getBean("apiService")
    }()
    
    /**
     * SecUser that is used for authorization
     */
    SecUser user

    /**
     * Returns an identifier that describes this export
     */
    public String getIdentifier() { "Combined excel" }
    
    /**
     * Returns the type of entitites to export. Could be Study or Assay
     */
    public String getType() { "Study" }
    
    /**
     * Returns whether this exporter supports exporting multiple entities at once
     * If so, the class should have a proper implementation of the exportMultiple method
     */
    public boolean supportsMultiple() { true }
    
    /**
     * Use the given parameters for exporting
     */
    public void setParameters(def parameters) {
        // Ignore the parameters for now
    }
    
    /**
     * Exports multiple entities to the outputstream
     */
    public void exportMultiple( def studies, OutputStream out ) { 
        
        // Collect data to export for each study
        def assays = Assay.findAll( "FROM Assay WHERE parent IN (:studies)", [ studies: studies ] )
        def sampleData = []
        
        def studyData = this.collectStudyData(studies)
        def subjectsData = this.collectSubjectData(studies)

        if(assays) {
            // Get the samples and sort them; this will be the sort order to use for
            // both retrieving the assay data and the measurements
            def samples = assays*.samples.flatten().unique().sort({it.name})
            sampleData = this.collectSampleData(assays, null, samples, authenticationService.getLoggedInUser())
        }
        
        // Combine everything into the excel workbook
        Workbook wb = new XSSFWorkbook()

        Sheet studySheet = wb.createSheet("Studies")
        Sheet subjectSheet = wb.createSheet("Subjects")
        Sheet samplesSheet = wb.createSheet("Samples")

        studyData = convertDataToStudyStructure(studyData)
        subjectsData = convertDataToSubjectStructure(subjectsData)

        assayService.exportRowWiseDataToExcelSheet(studyData, studySheet)
        assayService.exportRowWiseDataToExcelSheet(subjectsData, subjectSheet)
        assayService.exportRowWiseDataToExcelSheet(sampleData, samplesSheet)

        // Write the excel sheet to the outputstream
        wb.write(out)
    }

    /**
     * Returns the content type for the export
     */
    public String getContentType( def entity ) {
        return "application/vnd.ms-excel"
    }

    /**
     * Returns a proper filename for the given entity
     */
    public String getFilenameFor( def entities ) {
        if( entities instanceof Collection ) {
            return "multiple_studies.xls"
        } else {
            return entities.title + ".xls"
        }
    }
    
    /**
     * Export a single entity to the outputstream in SimpleTox format
     */
    public void export( def studyInstance, OutputStream outStream ) {
        exportMultiple([studyInstance], outStream)
    }
    
    /**
     * Collect study data for the given set of studies
     */
    def collectStudyData(studies) throws Exception {
        def collectedEvents = []
        def collectedSamplingEvents = []
        def collectedAssays = []

        studies.each {
            collectedEvents += it.events
            collectedSamplingEvents += it.samplingEvents
            collectedAssays += it.assays
        }

        def usedStudyTemplateFields = assayService.getAllTemplateFields(studies)
        def usedEventTemplateFields = assayService.getAllTemplateFields(collectedEvents)
        def usedSamplingEventTemplateFields = assayService.getAllTemplateFields(collectedSamplingEvents)
        def usedAssayTemplateFields = assayService.getAllTemplateFields(collectedAssays)

        def studyInformation = []
        studies.eachWithIndex { el, idx ->
            studyInformation[idx] = [assayService.getFieldValues(el, usedStudyTemplateFields),
                    assayService.getFieldValues(el.events, usedEventTemplateFields),
                    assayService.getFieldValues(el.samplingEvents, usedSamplingEventTemplateFields),
                    assayService.getFieldValues(el.assays, usedAssayTemplateFields)]
        }

        //Return a list with describing headers and return a list with the actual data
        [['Study' : usedStudyTemplateFields,
          'Event': usedEventTemplateFields,
          'Sampling event': usedSamplingEventTemplateFields,
          'Assay': usedAssayTemplateFields],
         studyInformation]
    }

    /**
     * Collect subject data for the given set of studies
     */
    def collectSubjectData(studies) {
        def collectedSubjects = []

        studies.each {
            collectedSubjects += it.subjects
        }
        def usedSubjectTemplateFields = assayService.getAllTemplateFields(collectedSubjects)

        def subjectInformation = []
        studies.eachWithIndex { el, idx ->
            subjectInformation[idx] = ([el.code] + assayService.getFieldValues(el.subjects, usedSubjectTemplateFields)).flatten()
        }
        usedSubjectTemplateFields = (["Study code"] + usedSubjectTemplateFields).flatten()

       [usedSubjectTemplateFields, subjectInformation]
    }

    /**
     * Collect sample data and measurement data for the given set of assays
     */
    def collectSampleData(assays, measurementTokens, samples, remoteUser) {
        //def data = assays.collect { assay -> assayService.collectAssayTemplateFields(assay, null) }
        //return assayService.convertColumnToRowStructure(data)
        
        
        def data = this.collectSampleDetails(samples)

        assays.each{ assay ->

            try {
                measurementTokens = assayService.requestModuleMeasurementNames(assay, remoteUser)
            } catch (e) {
                println e.message
            }

            def moduleMeasurementData
            try {
                moduleMeasurementData = apiService.getPlainMeasurementData(assay, remoteUser)
                data[ "Module measurement data: " + assay.name ] = apiService.organizeSampleMeasurements((Map)moduleMeasurementData, samples)
            } catch (e) {
                moduleMeasurementData = ['error' : [
                        'Module error, module not available or unknown assay']
                        * samples.size() ]
                e.printStackTrace()
            }
        }

        assayService.convertColumnToRowStructure(data)
    }

    /**
     * Collect clinical sample data for the given set of samples
     */
    def collectSampleDetails(samples) throws Exception {
        def samplingEventInEventGroup = [:]
        def subjectEventGroup = [:]
        def fieldMap = collectSampleTemplateFields(samples)

        //filter map on necessary data for samples sheet
        fieldMap['Subject Data'] = fieldMap['Subject Data'].findAll{ it['name'].equals('name') }
        fieldMap['Sample Data'] = fieldMap['Sample Data'].findAll{ it['name'].equals('name') }
        (samplingEventInEventGroup, subjectEventGroup) = collectSampleEventGroupInfo(samples)

        [
                'Study' : ['Code': samples.parent.code],
                'Subject Data': assayService.getFieldValues(samples, fieldMap['Subject Data'], 'parentSubject'),
                'Sample Data': assayService.getFieldValues(samples, fieldMap['Sample Data']),
                'Sampling Event in Group': samplingEventInEventGroup,
                'Subject Event Group': subjectEventGroup
        ]
    }

    /**
     * Collect clinical sample data for the given set of samples
     */
    def collectSampleTemplateFields(samples) throws Exception {
        [
                'Subject Data': assayService.getAllTemplateFields(samples*."parentSubject".unique()),
                'Sample Data': assayService.getAllTemplateFields(samples)
        ]
    }

    def collectSampleEventGroupInfo(samples) {
        def samplingEventInEventGroup = [:]
        def subjectEventGroup = [:]

        def starttimeHR = samples*.parentEvent*.getStartTimeString().flatten()
        def starttime = samples*.parentEvent*.startTime.flatten()
        if (!starttimeHR.every { !it }) samplingEventInEventGroup['starttime readable'] = starttimeHR
        if (!starttime.every { !it }) samplingEventInEventGroup['starttime'] = starttime

        starttimeHR = samples*.parentSubjectEventGroup*.getStartTimeString().flatten()
        starttime = samples*.parentSubjectEventGroup*.startTime.flatten()
        if (!starttimeHR.every { !it }) subjectEventGroup['starttime readable'] = starttimeHR
        if (!starttime.every { !it }) subjectEventGroup['starttime'] = starttime

        [samplingEventInEventGroup, subjectEventGroup]
    }

    def convertDataToStudyStructure(studyData) {
        def headers = []
        def temporaryDataCollection = []
        def iter = 0;
        def columnCount = 0;

        studyData[0].each { category ->

            if (category.value.size()) {
                if (!headers[0])
                    headers[0] = []

                headers[0][iter] = category.key

                // put non-category column headers into 2nd row
                if (!headers[1])
                    headers[1] = []

                category.value.each {
                    headers[1][iter] = it['name']
                    iter++
                }
            }
        }

        // add all column wise data into 'temporaryDataCollection'
        studyData[1].each { study ->
            iter = 0;

            study.each { category ->
                category.each { row ->
                    (!temporaryDataCollection[iter]) ? temporaryDataCollection[iter] = [] : void
                    temporaryDataCollection[iter] = (temporaryDataCollection[iter] + row.value).flatten()
                    iter++
                }
            }
            columnCount = temporaryDataCollection.collect { it?.size() }.max()
            temporaryDataCollection.each {
                try {
                    if (it.size < columnCount) {
                        for (int i = it.size(); i < columnCount; i++) {
                            it[i] = " "
                        }
                    }
                } catch (Exception) {}
            }
        }

        headers[0][headers[1].size()] = " "
        headers = headers.transpose()
        def data = []

        headers.eachWithIndex { el, idx ->
            data << el + temporaryDataCollection[ idx ]
        }

        data
    }

    def convertDataToSubjectStructure(subjectData) {
        def headers = []
        def temporaryDataCollection = []
        def tempData = []
        def studyName = "";

        subjectData[0].eachWithIndex { category, i ->
            if (category instanceof String) {
                headers[i] = category
            } else {
                headers[i] = category['name']
            }
        }

        // add all column wise data into 'temporaryDataCollection'
        subjectData[1].eachWithIndex { study, index ->
            studyName = (study[0]) ? study[0] : "Study code undefined!"
            tempData = study[1].each{it.collect{it.value}}
            tempData = tempData.collect{it.value}
            tempData = tempData.transpose()
            tempData.each{
                temporaryDataCollection.add( ([studyName] + it).flatten())
            }
        }

        [headers] + temporaryDataCollection
    }
    
    
    
    
    
    
    
}

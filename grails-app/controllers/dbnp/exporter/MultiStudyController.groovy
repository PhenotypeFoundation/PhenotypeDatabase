package dbnp.exporter

import dbnp.studycapturing.*
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import grails.plugins.springsecurity.Secured

@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class MultiStudyController {

    def assayService
    def apiService
    def authenticationService
    def fileService
    def AssayController = new AssayController()

    def index() {
        def user = authenticationService.getLoggedInUser()
        [userStudies :Study.giveReadableStudies(user)]
    }

    def studyExport() {
        def selectedStudies
        def selectedAssays

        if(!params['studies']) {
            flash.errorMessage = "<script type=\"text/javascript\">alert('Please select a study.');</script>"
            return redirect(action: "index")
        }

        (selectedStudies, selectedAssays) = collectDataFromParams()

        try {
            def filename = new Date().format('yyyy-MM-dd HH:mm') + " export.xls"
            response.setHeader("Content-disposition", "attachment;filename=\"${filename}\"")
            response.setContentType("application/octet-stream")

            generateExcelFile(selectedStudies, selectedAssays)

            response.outputStream.close()
        } catch (Exception e) {
            e.printStackTrace();
            flash.errorMessage = "<script type=\"text/javascript\">alert('An error has occurred while performing the export. Please notify an administrator');</script>"
            redirect(action: "index")
        }
    }

    def collectDataFromParams() {
        def selectedStudies = [], selectedAssays = []
        params.list('studies').each {
            def studyId = it.toInteger();
            selectedStudies << Study.read(studyId)
            if(params['study-'+studyId+'-assay']) {
                params.list('study-'+studyId+'-assay').each {
                    selectedAssays << Assay.read(it.toInteger())
                }
            }
        }
        [selectedStudies, selectedAssays]
    }

    def generateExcelFile(selectedStudies, selectedAssays) {
        def sampleData = []
        def samples = selectedAssays*.samples.flatten().unique()
        def studyData = this.collectStudyData(selectedStudies)
        def subjectsData = this.collectSubjectData(selectedStudies)
        if(selectedAssays) {
            sampleData = this.collectSampleData(selectedAssays, null, samples, authenticationService.getLoggedInUser())
        }
        Workbook wb = new XSSFWorkbook()

        Sheet studySheet = wb.createSheet("Studies")
        Sheet subjectSheet = wb.createSheet("Subjects")
        Sheet samplesSheet = wb.createSheet("Samples")

        studyData = convertDataToStudyStructure(studyData)
        subjectsData = convertDataToSubjectStructure(subjectsData)

        assayService.exportRowWiseDataToExcelSheet(studyData, studySheet)
        assayService.exportRowWiseDataToExcelSheet(subjectsData, subjectSheet)
        assayService.exportRowWiseDataToExcelSheet(sampleData, samplesSheet)

        wb.write(response.outputStream)
    }

    def collectStudyData(studies) throws Exception {
        def collectedEvents = []
        def collectedSamplingEvents = []
        def collectedAssays = []

        studies.each {
            collectedEvents += it.events
            collectedSamplingEvents += it.samplingEvents
            collectedAssays += it.assays
        }

        def usedStudyTemplateFields = assayService.getUsedTemplateFields(studies)
        def usedEventTemplateFields = assayService.getUsedTemplateFields(collectedEvents)
        def usedSamplingEventTemplateFields = assayService.getUsedTemplateFields(collectedSamplingEvents)
        def usedAssayTemplateFields = assayService.getUsedTemplateFields(collectedAssays)

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

    def collectSubjectData(studies) {
        def collectedSubjects = []

        studies.each {
            collectedSubjects += it.subjects
        }
        def usedSubjectTemplateFields = assayService.getUsedTemplateFields(collectedSubjects)

        def subjectInformation = []
        studies.eachWithIndex { el, idx ->
            subjectInformation[idx] = ([el.code] + assayService.getFieldValues(el.subjects, usedSubjectTemplateFields)).flatten()
        }
        usedSubjectTemplateFields = (["Study code"] + usedSubjectTemplateFields).flatten()

       [usedSubjectTemplateFields, subjectInformation]
    }

    def collectSampleData(assays, measurementTokens, samples, remoteUser) {
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

    def collectSampleTemplateFields(samples) throws Exception {
        [
                'Subject Data': assayService.getUsedTemplateFields(samples*."parentSubject".unique()),
                'Sample Data': assayService.getUsedTemplateFields(samples)
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
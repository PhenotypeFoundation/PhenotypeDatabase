package dbnp.exporter

import dbnp.studycapturing.*
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.dbnp.gdt.*
import grails.plugins.springsecurity.Secured

import java.text.NumberFormat

@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class MultiStudyController {

    def assayService
    def authenticationService
    def fileService
    def AssayController

    def index() {
        def user = authenticationService.getLoggedInUser()
        [userStudies :Study.giveReadableStudies(user)]
    }

    def studyExport() {
        def params = getParams();
        def selectedStudies = []
        def selectedAssays = []
        def fieldMap;

        if(!params['studies']) {
            flash.errorMessage = "<script type=\"text/javascript\">alert('Please select a study.');</script>"
            redirect(action: "index")
        }

        params.list('studies').each {
            def studyId = it.toInteger();
            selectedStudies << Study.read(studyId)
            if(params['study-'+studyId+'-assay']) {
                params.list('study-'+studyId+'-assay').each {
                    selectedAssays << Assay.read(it.toInteger())
                }
            }
        }

        if(selectedAssays.size() > 0)
            fieldMap = AssayController.mergeFieldMaps( selectedAssays.collect { assay -> assayService.collectAssayTemplateFields(assay, null) } )

        def outputDelimiter, studyData, subjectsData, sampleData

        def filename = "export.xls"
        response.setHeader("Content-disposition", "attachment;filename=\"${filename}\"")
        response.setContentType("application/octet-stream")
        try {
            studyData = this.collectStudyData(selectedStudies)
            subjectsData = this.collectSubjectData(selectedStudies)
            sampleData = this.collectAssayData(selectedAssays, fieldMap, null, selectedAssays*.samples.flatten().unique(), authenticationService.getLoggedInUser())
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
            response.outputStream.close()
        } catch (Exception e) {
            e.printStackTrace();
            render "An error has occurred while performing the export. Please notify an administrator"
        }
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
            studyInformation[idx] = assayService.getFieldValues(el, usedStudyTemplateFields) +
                    assayService.getFieldValues(el.events, usedEventTemplateFields) +
                    assayService.getFieldValues(el.samplingEvents, usedSamplingEventTemplateFields) +
                    assayService.getFieldValues(el.assays, usedAssayTemplateFields)
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

    def collectAssayData(assays, fieldMapSelection, measurementTokens, samples, remoteUser) {
        // collect the assay data according to user selection
        def data = []

        // First retrieve the subject/sample/event/assay data from GSCF, as it is the same for each list
        data = assayService.collectAssayData(assays[0], fieldMapSelection, null, samples)

        assays.each{ assay ->
            def moduleMeasurementData
            try {
                moduleMeasurementData = assayService.requestModuleMeasurements(assay, measurementTokens, samples, remoteUser)
                data[ "Module measurement data: " + assay.name ] = moduleMeasurementData
            } catch (e) {
                moduleMeasurementData = ['error' : [
                        'Module error, module not available or unknown assay']
                        * samples.size() ]
                e.printStackTrace()
            }
        }

        assayService.convertColumnToRowStructure(data)
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
        studyData[1].eachWithIndex { it, index ->
            if (index > 0) {
                it.eachWithIndex { element, i ->
                    temporaryDataCollection[i] = (temporaryDataCollection[i] + element.value).flatten()
                }
            } else {
                temporaryDataCollection += it.collect {
                    it.value
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
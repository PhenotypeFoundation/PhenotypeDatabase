package dbnp.exporter

import dbnp.studycapturing.*
import org.dbnp.gdt.*
import grails.plugins.springsecurity.Secured
import grails.converters.JSON

@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class MultiStudyController {

    def assayService
    def authenticationService
    def fileService

    def index() {
        def user = authenticationService.getLoggedInUser()
        [userStudies :Study.giveReadableStudies(user)]
    }

    def studyExport() {
        def params = getParams();
        def selectedStudies = []
        def selectedAssays = []
        def fieldMap;

        if(!params['studies'])
            throw new Exception( "No study selected" )

        params.list('studies').each {
            def studyId = it.toInteger();
            selectedStudies << Study.read(studyId)
            if(params['study-'+studyId+'-assay']) {
                params.list('study-'+studyId+'-assay').each {
                    selectedAssays << Assay.read(it.toInteger())
                }
            }
        }

        fieldMap = mergeFieldMaps( selectedAssays.collect { assay -> assayService.collectAssayTemplateFields(assay, null) } )

        def outputDelimiter, rowData, locale = java.util.Locale.US
        outputDelimiter = ';'
        locale = java.util.Locale.GERMAN // force use of comma as decimal separator

        def filename = "export.csv"
        response.setHeader("Content-disposition", "attachment;filename=\"${filename}\"")
        response.setContentType("application/octet-stream")
        try {
            rowData = collectAssayData(selectedAssays, fieldMap, [], selectedAssays*.samples.flatten().unique(), authenticationService.getLoggedInUser())

            assayService.exportRowWiseDataToCSVFile(rowData, response.outputStream, outputDelimiter, locale)

            // clear the data from the session
            session.removeAttribute('rowData')
            session.removeAttribute('measurementTokens')
            session.removeAttribute('exportFileType')
            session.removeAttribute('exportMetadata')

        } catch (Exception e) {
            e.printStackTrace();
            render "An error has occurred while performing the export. Please notify an administrator"
        }
    }

    def collectAssayData(assay, fieldMapSelection, measurementTokens, samples, remoteUser) {
        def assayData = ["Study" : ["Name" : assay.parent.title]]

        // collect the assay data according to user selection
        assayData << assayService.collectAssayData(assay, fieldMapSelection, measurementTokens, samples, remoteUser)

        flash.errorMessage      = assayData.remove('Module Error')

        assayService.convertColumnToRowStructure(assayData)
    }

    /**
     * Merges multiple fieldmaps as returned from assayService.collectAssayTemplateFields(). For each category,
     * a list is returned without duplicates
     * @param fieldMaps     ArrayList of fieldMaps
     * @return              A single fieldmap
     */
    def mergeFieldMaps( fieldMaps ) {
        if( !fieldMaps || !( fieldMaps instanceof Collection ) )
            throw new Exception( "No or invalid fieldmaps given" )

        if( fieldMaps.size() == 1 )
            return fieldMaps[ 0 ]

        // Loop over each fieldmap and combine the fields from different categories
        def mergedMap = fieldMaps[ 0 ]
        fieldMaps[1..-1].each { fieldMap ->
            fieldMap.each { key, value ->
                if( value instanceof Collection ) {
                    if( mergedMap.containsKey( key ) ) {
                        value.each {
                            if( !mergedMap[ key ].contains( it ) )
                                mergedMap[ key ] << it
                        }
                    } else {
                        mergedMap[ key ] = value
                    }
                } else {
                    if( mergedMap.containsKey( key ) ) {
                        if( !mergedMap[ key ].contains( value ) )
                            mergedMap[ key ] << value
                    } else {
                        mergedMap[ key ] = [ value ]
                    }
                }
            }
        }

        mergedMap
    }
}

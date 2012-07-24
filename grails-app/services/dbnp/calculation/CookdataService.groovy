/**
 * Cookdata Service
 * Provides business logic for Cookdata controller
 */
package dbnp.calculation

import java.util.List
import java.util.Map
import java.lang.Math
import grails.converters.JSON
import dbnp.authentication.AuthenticationService
import dbnp.studycapturing.Study
import dbnp.studycapturing.SamplingEvent
import dbnp.studycapturing.EventGroup
import dbnp.studycapturing.Sample
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CookdataService {
    def authenticationService
    def moduleCommunicationService
	def assayService

	/* Start of retrieval related functions 
	 */
	
	/**
	 * Formats and returns the samples related to a dataset group
	 * @param strGrouping				A string that contains a representation 
	 * 									of a dataset group, as received from 
	 * 									the client
	 * @param blnRestrictiveAggrType	Signals for additional checks on both 
	 * 									input and output
	 * @param selectionTriples			The list of selections made by the user,
	 * 									in the cookdata controller flow
	 * @param samplingEvents			The list of sampling events (ordered)
	 * @param eventGroups				The list of eventgroups (ordered)
	 * @return							A list of samples
	 */
    public List getSamplesForDatasetGroup(String strGrouping, boolean blnRestrictiveAggrType,
			selectionTriples, samplingEvents, eventGroups){
		strGrouping = strGrouping.replaceAll("\\s","") // Removing whitespaces
		
		if(strGrouping.equals("")){
			// Grouping is guaranteed to be empty
			if(blnRestrictiveAggrType){
				throw new IllegalArgumentException("When using the \"average\", \"median\" or \"pairwise\" aggregation types, both group A and group B need to contain samples.")
			} else {
				return []
			}
		}
		
		// We do have some samples to retrieve
		def listReturn = []
		def listGrouping = strGrouping.split("\\.")
		
		// Get the samples
		if(listGrouping) {
			listReturn = getSamplesBySamplingEventsAndEventGroups(
					listGrouping,
					selectionTriples,
					samplingEvents,
					eventGroups
			)
		}
		
		if(blnRestrictiveAggrType){
			// We didn't get any samples, this is not acceptable for some 
			// aggregation types
			if(listReturn==[]){
				throw new IllegalArgumentException("When using the \"average\", \"median\" or \"pairwise\" aggregation types, both group A and group B need to contain samples.")
			}
		}
		
		return listReturn
	}
	
	/**
	* Based on the sampling events and event groups that we can find when 
	* combining selectionTriples and listSelectionTripleIndexes (as made on 
	* page 2 ("Select sampling events") and page 3 ("Build Datasets") of the 
	* cookdata flow, respectively), we will build a list containing all the 
	* required samples for each selection/group.
	*
	* @param listSelectionTripleIndexes	The indexes in selectionTriples that
	* 											we are interested in
	* @param listSelectionTriples		Holds all the information that a
	* 									selection is composed of, such as
	* 									indexes to the relevant
	* 									sampling events and event groups.
	* @param samplingEvents				Holds the relevant samplingEvents 
	* 									(ordered)
	* @param eventGroups				Holds the relevant eventGroups
	* 									(ordered)
	* @return							An list of samples, without specific
	* 									ordering
	*/
   private List getSamplesBySamplingEventsAndEventGroups(listSelectionTripleIndexes, listSelectionTriples, samplingEvents, eventGroups){
	   List samples = []
	   listSelectionTripleIndexes.each{ val ->
		   // In case of "values" aggregation, some selections can remain blank
		   // Don't try to get samples for a blank selection
		   if(val!=[]){
			   try{
				   def selectionTripleIndex = val.split("_")[1]
				   def st = listSelectionTriples[Integer.valueOf(selectionTripleIndex)]
				   samples += Sample.findAllByParentEventAndParentEventGroup(
						   samplingEvents[st[0]], eventGroups[st[1]]
				   )
			   } catch(Exception e){
				   throw new IllegalArgumentException("Error while creating list of samples (${e.getMessage()}): value ${val} from "+listSelectionTripleIndexes)
			   }
		   }
	   }
	   return samples
   }

	/**
	 * Given a GSCF domain object, this function will, given a list of domain 
	 * objects, return a list of fields contained in these domain objects which
	 * are likely to interest the user. These fields are used in the cookdata 
	 * flow, on the "make a selection" pages.
	 * @param objects
	 * @return
	 */
    private List retrieveInterestingFieldsList(List objects){
        List fields = []
        objects.each{
            fields.addAll(it.giveDomainFields())
            fields.addAll(it.giveTemplateFields())
        }
        fields.unique()
        List toRemove = []
        for(int i = 0; i < fields.size(); i++){
            if(!fields[i].isFilledInList(objects)){
                toRemove.add(fields[i])
            }
        }
        fields.removeAll(toRemove)
        return fields
    }
	
	/**
	 * Will compute and package the equation results
	 * <p>
	 * This particular output format is in use to minimize the amount of time
	 * that the system has to deal with key / value assignments, storage and
	 * retrieval, between user input, computations and exporting the results. 
	 * 
	 * @param listToBeComputed			List of items to be computed. Each item
	 * 									contains at mimimum "equation", "aggr" 
	 * 									for aggregate type, and "samplesA" and 
	 * 									"samplesB" which contain the samples
	 * 									for group A and B.
	 * @param mapSTokenToMsrmentsPerF	Map containing measurement values.
	 * 									Allows lookup from a feature to a 
	 * 									sampletoken, to the value relevant for 
	 * 									that combination.
	 * @return							List containing as many lists as there 
	 * 									are items to be computed. Such a list
	 * 									will contain the items in question on 
	 * 									index 0, and list of feature and 
	 * 									measurement pairs on the second index. 
	 */
    public List getResults(List listToBeComputed, Map mapSTokenToMsrmentsPerF){
        List listItemAndFeatureToResult = []
		
        listToBeComputed.each{ item ->
            List listResultAndFeaturePairs = []
			int intResultCounter = 0;
            def dblResult
            switch(item.aggr){
                case "average":
                case "median":
                    String equation = item.equation.replaceAll("\\s","") // No whitespace
                    if (!equation) {
	                    throw new IllegalArgumentException("No equation was provided.")
                    }

                    // Per feature, the samples map to measurements
	                mapSTokenToMsrmentsPerF.each{ feature, mapStToM ->
                        double avgA = computeMeanOrMedian("group A", item, feature, mapStToM)
                        double avgB = computeMeanOrMedian("group B", item, feature, mapStToM)

						listResultAndFeaturePairs[intResultCounter] = 
							[feature, computeWithVals(equation, avgA, avgB)]
						intResultCounter++
                    }
                    break
	            case "values":
                    // Per feature, the samples map to measurements
                    // Per feature, we add each pair of featurename and measurement to the return value
                    mapSTokenToMsrmentsPerF.each{ feature, mapStToM ->
                        listResultAndFeaturePairs[intResultCounter] =
                            [feature, []]
                        (item.samplesB + item.samplesA).each{ s ->
                            def m = mapStToM[s.sampleUUID]
                            if (m) listResultAndFeaturePairs[intResultCounter][1].add([s.name, m])
                        }
                        intResultCounter++
                    }
                    break
                default:
                    break
            }
            listItemAndFeatureToResult.add([item, listResultAndFeaturePairs])
        }
        return listItemAndFeatureToResult
    }


	
	/**
	* For the CookData controller, an assay is interesting when it has samples that the user has selected.
	* Unfortunately the fastest way seems to be checking for each assay, if they have a sample that is in one of the selected samplingEvents.
	* This involves requesting a lot of samples from the database.
	*/
    public List getInterestingAssays(Study study, List samplingEvents, List lstAssays) {
	    List assays = []
	    int numAssays = study.assays.size()
	    int numSEvents = samplingEvents.size();
	    for(int i = 0; i < numAssays; i++){
		    def assay =  study.assays[i]
		    if(lstAssays.contains(assay)) {

			    def listAssaySamples = []
			    assay.samples.each{
				    listAssaySamples << it
			    }

			    if(assay.samples.size()==0){
				    // No samples in the assay, so not an interesting assay
				    continue
			    }

			    boolean success = false
			    for(int j = 0; j <numSEvents; j++){
				    if(success){
					    break
				    }

				    SamplingEvent event = samplingEvents[j]
				    int numEventSamples = event.samples.size()
				    for(int k = 0; k < numEventSamples; k++){
					    List listEventSamples = []
					    event.samples.each{
						    listEventSamples << it
					    }

					    Sample sample = listEventSamples[k]
					    if(listAssaySamples.contains(sample)){
						    success = true
						    break
					    }
				    }
			    }
			    if(success){
				    assays << assay
			    }
		    }
	    }
	    return assays
    }
	
	
	
	// Returns each measurement per sampleToken, per feature
    public Map getDataFromModules(List assays, List samples){
		List blacklistedModules = []
		Map mapResults = [:]

		assays.each{ assay ->
			Map mapTmp = [:]
			if (!blacklistedModules.contains(assay.module.id)) {
				try{
					// Request for a particular assay
					def urlVars = "assayToken=" + assay.assayUUID
					// All samples
					urlVars += "&"+samples.collect { "sampleToken=" + it.sampleUUID }.join("&");
					def strUrl = assay.module.url + "/rest/getMeasurementData"
					def callResult = moduleCommunicationService.callModuleMethod(assay.module.url, strUrl, urlVars, "POST")
					// Store measurements per sampleToken, per feature.
					// [1] contains a list of features
                    int numSamples = callResult[0].size()
					callResult[1].eachWithIndex{ feature, featureIndex ->
						mapTmp.put(feature, [:])
						Map mapSampleTokenToMeasurement = [:]
						// [0] contains a list of sampleTokens
						callResult[0].eachWithIndex{ sample, sampleIndex ->
							if(sample!=null && sample.class!=org.codehaus.groovy.grails.web.json.JSONObject$Null){
								// We have a sample for this feature
								// This sample may have a measurement
								def measurement = callResult[2][featureIndex*numSamples + sampleIndex]
								if(measurement!=null && measurement.class!=org.codehaus.groovy.grails.web.json.JSONObject$Null){
									mapTmp[feature].put(sample, measurement)
								}
							}
						}
					}
				} catch (Exception e) {
					blacklistedModules.add(assay.module.id)
					log.error("getDataFromModules: " + e)
				}
			}
			if(mapTmp!=[:]){
				// Add the individial result to the total
				mapTmp.each{ k, v ->
					mapResults.put(k,  v)
				}
			}
		}
		return mapResults
	}
	
	
	/* Start of parsing and computation related functions 
	 */
    
			
	private boolean checkForOpeningAndClosingBrackets(String eq){
        boolean ret = (
        // ( ... ) and
        eq.startsWith("(") && eq.endsWith(")")
                &&
                // First ) is at the last index
                eq.indexOf(")")==(eq.size()-1)
        )
        return ret
    }

    private Map parseWellFormedLeftHandSide(String eq){
        Map mapReturn = [:]
        int countOpening = 0
        int countClosing = 0
        int latestClosingIndex = -1
        for(int i = 0; i < eq.size(); i++){
            if(eq[i]=="("){
                countOpening++
            }
            if(eq[i]==")"){
                countClosing++
                latestClosingIndex = i
            }
            if(	countOpening!=0 && countClosing!=0 &&
                    countOpening==countClosing
            ){
                // Left-hand side is wellformed
                break
            }
        }

        mapReturn.endIndex = 1 + latestClosingIndex
        if(mapReturn.endIndex==eq.size()){
            // Brackets are part of right-hand side, not left...
            mapReturn.endIndex = 0
        }
        mapReturn.success = (countOpening==countClosing)
        return mapReturn
    }

    public double computeWithVals(String eq, double dblA, double dblB){
        double dblReturn = -1.0
        // Check for "(x)"
        if(checkForOpeningAndClosingBrackets(eq)){
            int index0 = eq.indexOf(")")
            double result = computeWithVals(eq.substring(1, index0), dblA, dblB)
            dblReturn = result
            return dblReturn
        }

        /* Check for "x/y" and make sure "(x/y)/z" detects the second operator,
           * not the last.
           */
        Map mapParseLHSResults = parseWellFormedLeftHandSide(eq)
        if(mapParseLHSResults.success){
            // A wellformed LHS could be found. Any operator after the LHS?
            int intOpIndex = eq.substring(mapParseLHSResults.endIndex,
                    eq.size()).indexOf("/")
            if(intOpIndex!=-1){
                int index0 = intOpIndex+mapParseLHSResults.endIndex
                double result1 = computeWithVals(eq.substring(0, index0), dblA, dblB)
                double result2 = computeWithVals(eq.substring(index0+1, eq.size()), dblA, dblB)
                dblReturn = result1 / result2
                return dblReturn
            }
            intOpIndex = eq.substring(mapParseLHSResults.endIndex,
                    eq.size()).indexOf("*")
            if(intOpIndex!=-1){
                int index0 = intOpIndex+mapParseLHSResults.endIndex
                double result1 = computeWithVals(eq.substring(0, index0), dblA, dblB)
                double result2 = computeWithVals(eq.substring(index0+1, eq.size()), dblA, dblB)
                dblReturn = result1 * result2
                return dblReturn
            }
            intOpIndex = eq.substring(mapParseLHSResults.endIndex,
                    eq.size()).indexOf("+")
            if(intOpIndex!=-1){
                int index0 = intOpIndex+mapParseLHSResults.endIndex
                double result1 = computeWithVals(eq.substring(0, index0), dblA, dblB)
                double result2 = computeWithVals(eq.substring(index0+1, eq.size()), dblA, dblB)
                dblReturn = result1 + result2
                return dblReturn
            }
            intOpIndex = eq.substring(mapParseLHSResults.endIndex,
                    eq.size()).indexOf("-")
            if(intOpIndex!=-1){
                int index0 = intOpIndex+mapParseLHSResults.endIndex
                double result1 = computeWithVals(eq.substring(0, index0), dblA, dblB)
                double result2 = computeWithVals(eq.substring(index0+1, eq.size()), dblA, dblB)
                dblReturn = result1 - result2
                return dblReturn
            }
        } else {
            throw new IllegalArgumentException( "computeWithVals encountered a malformed equation: " + eq )
            println "computeWithVals encountered a malformed equation: "+eq
        }

        // Check for A
        if(eq.equals("A")){
            dblReturn = dblA
            return dblReturn
        }

        // Check for B
        if(eq.equals("B")){
            dblReturn = dblB
            return dblReturn
        }

        throw new IllegalArgumentException( "computeWithVals encountered an equation it failed to parse: " + eq )
        println "computeWithVals encountered an equation it failed to parse: "+eq
    }

     /**
      * Computes the either the mean or the median of the values contained the value of a certain key-value pair in the
      * "item" variable.
      * are ignored.
      * @param groupName    Used to determine which key will be used to get the right values
      * @param item         Contains information on what kind of computation needs to be done, on which samples
      * @param feature      The feature for which the computation needs to be done
      * @param mapStToM     Maps sampleTokens to measurements
      * @return Either the arithmetic mean or the median of the values
      */
    private double computeMeanOrMedian(String groupName, item, feature, mapStToM){
        List data = []

        // map the groupName variable to a key for item
        String key = ""
        if(groupName == "group A"){
            key = "samplesA"
        } else {
            if(groupName == "group B"){
                key = "samplesB"
            } else {
                throw new IllegalArgumentException("computeMeanOrMedian: "+groupName+" is not a legal value for the groupName parameter.")
            }
        }

        // Proceed with the computations
        item[key].each{ s ->
            def m = mapStToM[s.sampleUUID]
            if (m) data.add(m)
        }
        if (data.size() == 0) {
            throw new IllegalArgumentException("The samples from "+groupName+" of dataset ${item.datasetName} do not have any measurements for ${feature}. Cannot compute average or median.")
        }
        double res = 0.0
        if(item.aggr == "average"){
            res = computeMean(data)
        }
        if(item.aggr == "median"){
            res = computeMedian(data)
        }

        return res
    }

    /**
     * Computes the mean of the given values. Values that can not be parsed to a number
     * are ignored.
     * @param values List of values to compute the mean for
     * @return Arithmetic mean of the values
     */
    private double computeMean(List values){
        def sumOfValues = 0
        def sizeOfValues = 0
        values.each { value ->
            sumOfValues += value
            sizeOfValues++
        }
        return sumOfValues / sizeOfValues
    }

    /**
     * Computes the median of the given values. Values that can not be parsed to a number
     * are ignored.
     * @param values List of values to compute the median for
     * @return Median of the values
     */
    private double computeMedian(List values){
        List newValues = []
        values.each { value ->
            newValues.add(value)
        }
        newValues.sort()
        int listSize = newValues.size() - 1
        def intPointer = (int) Math.abs(listSize * 0.5)
        if(intPointer == (listSize * 0.5)){
            // If we exactly end up at an item, take this item
            return newValues.get(intPointer);
        } else {
            // If we don't exactly end up at an item, take the mean of the 2 adjacent values
            return ((newValues.get(intPointer) + newValues.get(intPointer + 1)) / 2);
        }
    }
	
	/* Start of export related functions
	*/

    /**
     * Will write the results for one dataset, which should be of aggregration type "average" or "median",
     * to an xlsx, which will be added to the ZipOutputStream that this function receives
     * <p>
     * Resulting file layout:
     * <p>
     * <pre>
     * name      datasetnameA   datasetnameB ...
     * feature1  value A1       value B1
     * feature2  value A2       value B2
     * ...
     * </pre>
     * @param zipOutStream  The stream to write the resulting file to
     * @param results       Contains all the data that needs to be written into the file
     */
    public void writeMeanAndMedianResultsToStream(OutputStream outputStream, results){
        /* We write out the results for the "average" and "median"
        * aggregation types.
        * The resulting file will be called "median_and_average.xlsx"
        */

        // listData will be rowwise, and is initialized to the number of
        // result sets, plus one cell
        List listData = new List[1 + results.size()]

        // Every row will start with a featurename, except the first
        // We grab the featurenames from the first result set
        // We can do this because all result sets are supposed to describe all
        // features
        listData[0] = ["name"]
        for (int i=0; i<results[0][1].size(); i++) {
           listData[i+1] = [results[0][1][i][0]]
        }

        // Add the dataset names to the first row
        for (int i=0; i<results.size(); i++) {
           if(results[i][0].aggr == "average" || results[i][0].aggr == "median"){
               listData[0].add(results[i][0].datasetName)
           }
        }
        // Now, we add the results, per dataset, to the row that starts with the
        // correct feature name
        for (int i=0; i<results.size(); i++) {
           if(results[i][0].aggr == "average" || results[i][0].aggr == "median"){
               results[i][1].eachWithIndex{ value, index ->
                   // Offset by one, because of the header row
                   listData[index+1].add(value[1])
               }

               // Will never be read again
               results[i] = null
           }
        }

        results = null

        // Write the data to a stream
        ByteArrayOutputStream fileByteArrOutStream = new ByteArrayOutputStream()
        assayService.exportRowWiseDataToExcelFile(listData,	fileByteArrOutStream)
        outputStream.write(fileByteArrOutStream.toByteArray());
    }

    /**
     * Writes one or more xslx files containing computation results to a ZipOutPutStream.
     * <p>
     * The results format is as follows:
     * - list of result sets (pairs)
     * 	|- dataset item information (list)
     * 	|	|- datasetName
     * 	|	|- aggr
     * 	|	|- ...
     *	|
     * 	|- list of dataset item results (pairs)
     * 		|- feature
     *		|- value
     * To reach the dataset name of a specific resultset, you would do
     * results[someIndex][0].datasetName
     * To reach a feature-measurement pair, you would do
     * results[someIndex][1][someIndex]
     * <p>
     * @param zipOutStream  The stream to write to
     * @param results       Contains all the information that needs to be exported
     */
    public void writeZipOfAllResults(ZipOutputStream zipOutStream, results){
        // TODO: write out the results for the "pairwise" aggregation

        for(int r = 0; r < results.size(); r++){
            if(results[r]!=null && results[r][0].aggr=="values"){
                writeValuesToZip(zipOutStream, results[r])
                results[r]=null
                continue
            }
            if(results[r]!=null && (results[r][0].aggr == "average" || results[r][0].aggr == "median")){
                writeAverageOrMedianToZip(zipOutStream, results[r])
                results[r]=null
                continue
            }
        }
    }

    /**
     * Will write the results for one dataset, which should be of aggregration type "average" or "median",
     * to an xlsx, which will be added to the ZipOutputStream that this function receives
     * <p>
     * Resulting file layout:
     * <p>
     * <pre>
     * name      datasetname
     * feature1  value 1
     * feature2  value 2
     * ...
     * </pre>
     * @param zipOutStream  The stream to write the resulting file to
     * @param result        Contains all the data that needs to be written into the file
     * @see CookdataService#writeAverageOrMedianToStream
     */
    private void writeAverageOrMedianToZip(ZipOutputStream zipOutStream, List result){
        // Write the data to a stream
        def fileByteArrOutStream = new ByteArrayOutputStream()
        writeAverageOrMedianToStream(fileByteArrOutStream, result)

        // Write the stream to the zip
        zipOutStream.putNextEntry(new ZipEntry(result[0].aggr+"_"+result[0].datasetName+".xlsx"))
        zipOutStream.write(fileByteArrOutStream.toByteArray());
        zipOutStream.closeEntry();
    }

    /**
     * Will write the results for one dataset, which should be of aggregration type "average" or "median",
     * to an xlsx, which will be added to the ZipOutputStream that this function receives
     * <p>
     * Resulting file layout:
     * <p>
     * <pre>
     * name      datasetname
     * feature1  value 1
     * feature2  value 2
     * ...
     * </pre>
     * @param outStream  The stream to write the resulting file to
     * @param result        Contains all the data that needs to be written into the file
     */
    public void writeAverageOrMedianToStream(OutputStream outStream, List result){
        // Format the data
        def data = [["name", result[0].datasetName]]
        data.addAll(result[1])

        // Write the data to a stream
        assayService.exportRowWiseDataToExcelFile(data,	outStream)
    }

    /**
     * Will write the results for one dataset, which should have the "measurement" aggregation type, to an xlsx,
     * which will be added to the ZipOutputStream that this function receives.
     * @param zipOutStream  The stream to write the resulting file to
     * @param result        Contains all the data that needs to be written into the file
     * @see CookdataService#writeValuesToStream
     */
    private void writeValuesToZip(ZipOutputStream zipOutStream, List result){
        // Write the data to a stream
        def fileByteArrOutStream = new ByteArrayOutputStream()
        writeValuesToStream(fileByteArrOutStream, result)

        // Write the stream to the zip
        zipOutStream.putNextEntry(new ZipEntry("measurements_"+result[0].datasetName+".xlsx"))
        zipOutStream.write(fileByteArrOutStream.toByteArray());
        zipOutStream.closeEntry();
    }

    /**
     * Will write the results for one dataset, which should have the "measurement" aggregation type, to a stream.
     * For results with this aggregation type, a measurement result is a list of pairs.
     * Each pair has two values. The first spot is the samplename, the second is the measurement value.
     * <p>
     * Resulting file layout:
     * <p>
     * <pre>
     * name      sna       snb      ...
     * feature1  md1a      md2a
     * feature2  md1b      md2b
     * ...
     * </pre>
     * @param outStream  The stream to write the resulting file to
     * @param result        Contains all the data that needs to be written into the file
     */
    public void writeValuesToStream(OutputStream outStream, List result){
        // listData will be rowwise, and is initialized to the number of
        // features, plus one more
        def listData = new List[1 + result[1].size()]

        // Topleft cell will contain "name", every other row starts with a featurename
        // features
        listData[0] = ["name"]
        for (int i=0; i<result[1].size(); i++) {
            listData[i+1] = [result[1][i][0]]
        }

        // Add the samplenames to the top row. These can be grabbed from the first
        // feature (list of measurement results), because there should be no variation
        result[1][0][1].value.eachWithIndex{ value, index ->
            // Offset by one, because of the header row.
            listData[0].add(value[0])
        }

        // Add the measurement values. Each first cell in a row already contains the featurename,
        // the measurement related to each column's sample will be added to the row
        for (int i=0; i<result[1].size(); i++) {
            result[1][i][1].each{ value ->
                listData[i+1].add(value[1])
            }
        }

        // Write the data to a stream
        assayService.exportRowWiseDataToExcelFile(listData,	outStream)
    }

}

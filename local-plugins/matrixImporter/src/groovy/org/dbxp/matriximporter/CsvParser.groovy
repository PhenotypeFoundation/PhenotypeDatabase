package org.dbxp.matriximporter

/**
 * This class is capable of importing CSV files (with , (comma), ; (semicolon) and tab as separators)
 * 
 * @author robert
 *
 */
public class CsvParser extends MatrixParser{

    private static final Map delimiterNameMap = [comma: ','.bytes[0], 'semi-colon': ';'.bytes[0], tab: '\t'.bytes[0]]


	/**
	 * Returns true if this class is able to parse files with a given name. This
     * is done by checking if the extension equals '.csv' or '.txt'. Also
     * returns true if fileName is null or ''.
     *
	 * @param hints	Hints for reading the csvfile. Possible keys are:
	 * 			startRow	0-based row number of the first row to read. (1 means start reading from the second row)
	 * 						Defaults to the first row in the file
	 * 			endRow		0-based row number of the last row to read.	 (2 means the 3rd row is the last to read)
	 * 						Defaults to the last row in the file
	 * 			delimiter	Delimiter that is used between the different fields on a line. Common values are , ; or <tab>.
	 * 						Defaults to the character , ; or <tab> that is most common in the file.
	 * @return true if the parser can parse the file, false otherwise
	 */
    public boolean canParse( Map hints = [:] ) {
		def fileName = hints.fileName
        return fileName ? fileName.matches(/.+\.(csv|txt)$/) : true
	}
	
	/**
	 * Returns a description for this parser
	 * @return	Human readable description
	 */
	public String getDescription() {
		return "Matrix importer for reading CSV files"
	}

    /**
     * Parses the given inputStream as a CSV file and returns the matrix in that inputStream.
	 *
	 * @param inputStream	InputStream to parse.
	 * @param hints		    Hints for reading the csv file. Possible keys are:
	 * 			startRow	0-based row number of the first row to read. (1 means start reading from the second row)
	 * 						Defaults to the first row in the file
	 * 			endRow		0-based row number of the last row to read.	 (2 means the 3rd row is the last to read)
	 * 						Defaults to the last row in the file
	 * 			delimiter	Delimiter that is used between the different fields on a line. Common values are , ; or <tab>.
	 * 						Defaults to the character , ; or <tab> that is most common in the file.
     * 			makeRowsEqualLength
     * 		                Pad lines shorter than longest line with empty strings so all lines will contain the same number of values
     *
	 * @return		An Arraylist with:
     *               a. Two-dimensional data matrix of structure:
	 * 				[
	 * 					[ 1, 3, 5 ] // First line
	 * 					[ 9, 1, 2 ] // Second line
	 * 				]
     *
     * 			    b. A map with parse info containing:
     * 			    - String delimiter, the delimiter used to parse the file
     * 			    - String delimiterName, human readable version of the delimiter (see delimiterNameMap)
     * 			    - delimiterNameMap, maps human readable versions of delimiters to delimiters
     * 			    */
    ArrayList parse( InputStream inputStream, Map hints ) {

        if (!inputStream.markSupported()) {
            throw new RuntimeException('The given inputstream (' + inputStream.class.name + ') does not support marking. Please supply one that does.')
        }

        if (hints.endRow == null) hints.endRow = Integer.MAX_VALUE

        def startRow =  forceValueInRange(hints.startRow, 0, Integer.MAX_VALUE)
        def endRow =    forceValueInRange(hints.endRow, startRow, Integer.MAX_VALUE)

		byte delimiter = hints.delimiter
        if (delimiterNameMap.containsKey(hints.delimiterName))
            delimiter = (byte) delimiterNameMap[hints.delimiterName]

		if( !delimiter ) {
			delimiter = determineDelimiterFromInput( inputStream, hints.threshold ?: 5 )
		}

		// Now loop through all rows, retrieving data from the file
		def dataMatrix = []
		inputStream.eachLine(0) { String line, int lineNumber ->
			if( lineNumber >= startRow && lineNumber <= endRow ) {

                dataMatrix << (line.split( (delimiter as char).toString() ) as ArrayList)

			}
		}

        inputStream.close()

        // pad lines shorter than longest line with empty strings if requested
        if (hints.makeRowsEqualLength) {

            def maxSize = dataMatrix*.size().max{ it }

            dataMatrix.eachWithIndex{ line, i ->

                dataMatrix[i] += [""] * (maxSize - line.size())

            }
        }

		return [dataMatrix,
                       [delimiter: delimiter,
                        delimiterName: delimiterNameMap.find {it.value == delimiter}?.key,
                        delimiterNameMap: delimiterNameMap]]
	}

	/**
	 * Tries to guess the delimiter used in this csv file. This is done by looking which 
	 * character from , ; and <tab> is most common.
	 * @param inputStream InputStream to parse
	 * @return		Most probable delimiter. If none could be determined, null is given.
	 */
	protected byte determineDelimiterFromInput( InputStream inputStream, int threshold ) {

        def delimiterCounts = [:]
        delimiterNameMap.each {delimiterCounts[it.value as int] = 0}

        inputStream.mark(readAheadLimit)

        // Read characters in a buffer
        byte[] byteBuffer = new byte[readAheadLimit]
        int charactersRead = inputStream.read((byte[]) byteBuffer, 0, readAheadLimit)

        inputStream.reset()

        def delimiterBytes = delimiterNameMap*.value

        // tally occurrences of the possible delimiters
        byteBuffer[0..charactersRead - 1].each { c ->
            if (c in delimiterBytes) delimiterCounts[c as int]++
        }
        
		// Determine the best delimiter. It is only returned if more than value
		// of 'threshold' of those characters have been found
		byte bestDelimiter = 0
		def bestCount = 0

        delimiterNameMap*.value.each { byte it ->
            def count = delimiterCounts[it as int]

            if( count > bestCount && count >= threshold) {
                bestCount = count
                bestDelimiter = it
            }
        }

        if( !bestDelimiter )
            throw new Exception( "CSV delimiter could not be automatically determined for inputStream." )


		return bestDelimiter
    }
}